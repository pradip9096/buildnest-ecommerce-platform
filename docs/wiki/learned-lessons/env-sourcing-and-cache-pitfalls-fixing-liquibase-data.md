---
title: "Six Compounding Pitfalls When Fixing Data via a New Liquibase Changeset in This Repo's Local Dev Setup"
category: tooling
tags: [liquibase, dotenv, bash-source, redis-cache, schema-drift, ddl-auto, checksum]
keywords: [unquoted ampersand bash source, LIQUIBASE_ENABLED duplicate, ddl-auto update schema drift, duplicate column name liquibase, redis cache stale after direct db write, preConditions markran, MD5SUM checksum mismatch, DATABASECHANGELOG]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
root_cause: "six stacked, independently-masking causes (unquoted & truncating a sourced .env value, a silently-overriding duplicate .env key, ddl-auto=update running alongside Liquibase creating undocumented schema drift across three tables, a Redis @Cacheable entry never invalidated by an out-of-band JDBC write, and a checksum-reconciliation write to DATABASECHANGELOG) each hid the next until isolated one at a time"
impact: high — systemic schema drift across three tables plus repeated direct writes to the Liquibase audit/integrity table (DATABASECHANGELOG)
related_lessons:
  - docs/wiki/learned-lessons/dotenv-not-auto-loaded-by-local-processes.md
---

# Six Compounding Pitfalls When Fixing Data via a New Liquibase Changeset in This Repo's Local Dev Setup

## Problem

Writing a one-line Liquibase `<update>` changeset to fix a corrupted category name (`docs/reports/dynamic-analysis-2026-07-04.md`, Finding 4) took nine backend restarts to actually land, due to four unrelated, stacked problems — each masking the next until isolated individually.

## 1. Unquoted `&` in `.env` breaks `bash source`

`start.sh` and manual restarts both load env vars via `set -o allexport && source .env`. `.env` is a plain shell script from `source`'s perspective, not a `KEY=VALUE` parser. An unquoted value containing `&`:

```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/db?useUnicode=true&characterEncoding=UTF-8
```

is parsed by bash as two commands: `SPRING_DATASOURCE_URL=...?useUnicode=true` backgrounded (trailing `&`), then `characterEncoding=UTF-8` as a no-op foreground command. The variable ends up truncated, silently. Symptom was `Access denied for user 'root'@'localhost'` at startup — a red herring that looks like a credentials problem but is actually a truncated URL missing the schema name that got shifted into the background command.

**Fix:** quote any `.env` value containing `&`, `;`, `|`, or other shell metacharacters: `SPRING_DATASOURCE_URL="jdbc:mysql://...?useUnicode=true&characterEncoding=UTF-8"`.

## 2. A later duplicate key in `.env` silently wins

`.env` had `LIQUIBASE_ENABLED=true` on one line and `LIQUIBASE_ENABLED=false` further down. Since `source` executes top-to-bottom, the later line silently overrides the earlier one with no warning. Effective result: Liquibase never ran on this local DB, for an unknown number of prior sessions — evidenced by `DATABASECHANGELOG` being stuck at a changeset from several days before the most recent one already in the codebase.

**Fix:** `grep -n "^KEY" .env` for exact-duplicate keys before assuming a value is what you think it is — env files have no duplicate-key protection.

## 3. `SPRING_JPA_DDL_AUTO=update` + Liquibase disabled = silent schema drift

With Liquibase off, Hibernate's `ddl-auto=update` silently added a column (`inventory.reservation_expires_at`) that a real, already-committed Liquibase changeset was also supposed to add. Neither mechanism knew about the other. Turning Liquibase back on made it try to re-add the same column and fail with `Duplicate column name`, blocking every changeset after it in the changelog, including a brand new one.

**Fix (general, not just this repo):** never run `ddl-auto=update` and Liquibase against the same schema — pick one authority. Applied here without reverting `ddl-auto` (too risky to flip near end of a long session): added a `<preConditions onFail="MARK_RAN"><not><columnExists .../></not></preConditions>` to the drifted changeset so it self-heals — safe to edit because the changeset had *never* actually been recorded in `DATABASECHANGELOG`, so there was no checksum-mismatch risk from editing an "already applied" file.

Note: `<preConditions>` must be the **first** child element of `<changeSet>`, before `<comment>` — the XSD enforces this ordering and fails with a `SAXParseException` otherwise, not an obvious error to connect back to element order.

## 4. A direct DB write via Liquibase doesn't invalidate `@Cacheable` Redis entries

After the changeset genuinely fixed the data (verified via `SELECT ... HEX(name)` directly against MySQL), the API still returned the old corrupted value. Root cause: `CategoryServiceImpl` has `@Cacheable(key = "'all'")` under `@CacheConfig(cacheNames = "categories")`, and the Liquibase `UPDATE` — run outside the application, via JDBC directly at startup — has no way to trigger `@CacheEvict`. The Redis key `categories::all` kept serving the pre-fix value indefinitely.

**Fix:** after any out-of-band data fix (migration, manual `UPDATE`, seed script) touching a `@Cacheable`-backed table, evict the relevant Redis key(s) directly: `docker exec <redis-container> redis-cli del "categories::all"` (or `--scan --pattern` first to find the exact key name from `@CacheConfig(cacheNames=...)` + `@Cacheable(key=...)`).

## 5. Table drift recurred a third time — treat it as systemic, not a one-off

The same failure signature (`categories` in Finding 4, `inventory.reservation_expires_at` above) hit a **third** table in the same session: adding `products.is_featured` in a later, unrelated changeset failed identically — `Table PRODUCTS not found` in the H2 test profile, because `products` is also never created by any Liquibase changeset in this repo (only referenced later by an orphaned, unregistered index changeset that never actually runs). All three tables share the same origin: Hibernate `ddl-auto=update` created them in real dev MySQL at some point while Liquibase was disabled or not yet tracking them.

**Standing rule for this repo, not just a retrospective fix:** any *new* Liquibase changeset that alters `categories`, `products`, or `inventory` should get a `preConditions`/`tableExists` (or `columnExists`) guard **by default**, written at the same time as the changeset — don't wait for the test suite to fail first. Before writing an `addColumn`/`update` changeset against any table, it's worth a quick check whether that table has a real `createTable` changeset anywhere in `db/changelog/` — if not, assume drift and guard accordingly.

## 6. Editing an already-applied changeset requires a checksum reconciliation — confirm with the user first

Liquibase records an MD5 checksum per changeset in `DATABASECHANGELOG` the first time it runs. Editing that changeset's file afterward (e.g., adding a `preConditions` guard once a test failure reveals it's needed) causes every subsequent startup to fail with a checksum mismatch against the *local* dev database where it already ran — even though the changeset was never applied anywhere else and is still uncommitted.

The standard reconciliation is `UPDATE DATABASECHANGELOG SET MD5SUM=NULL WHERE ID='<changeset-id>'`, which makes Liquibase recompute and accept the new checksum without re-executing the changeset. This is legitimate and Liquibase-native, **but it's still a direct write to an audit/integrity table**, and the permission system correctly flagged it as exactly the kind of action that shouldn't happen silently. It happened twice in this session (once for the Décor changeset, once for `is_featured`) — the second time was done consistently with the first only because the user had already explicitly approved the pattern.

**Rule going forward:** don't treat "I've done this exact reconciliation before and it was approved" as blanket permission for future occurrences in the same session — each instance is still a direct mutation of `DATABASECHANGELOG`, worth a quick confirmation rather than silent repetition, unless the user has explicitly said "do this whenever it comes up."

## Diagnostic order that actually worked

1. `curl` the API — still wrong → not necessarily a frontend issue.
2. Query MySQL directly with `HEX()` on the column — this is the fork: if HEX shows correct UTF-8 bytes, the DB is fine and the problem is downstream (cache); if HEX shows the mangled bytes, the DB write itself never took effect (migration/env issue).
3. Only after confirming the DB write path works should cache be suspected — checking cache first would have wasted time if the migration truly hadn't run.

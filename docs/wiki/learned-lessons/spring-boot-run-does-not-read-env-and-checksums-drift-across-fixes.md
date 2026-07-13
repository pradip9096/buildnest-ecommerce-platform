---
title: "`./mvnw spring-boot:run` Doesn't Read `.env`, and This Repo Has Two MySQL Instances Competing on Similar Ports"
category: tooling
tags: [spring-boot, dotenv, mysql, liquibase, checksum-mismatch, local-dev-environment]
keywords: [spring-boot:run does not read .env, competing MySQL instances port 3306 3307, Liquibase checksum mismatch, MD5SUM reset dev database, docker-compose env loading]
source_conversations: [Session 2026-07-07, issue #308]
last_updated: 2026-07-07
confidence: high
evidence_strength: strong
root_cause: "three independent environment-only failure modes stacked in one manual verification session: ./mvnw spring-boot:run never sources .env (docker-compose-only behavior), a native MySQL competed with the Dockerized instance on a similar port, and previously-edited Liquibase changesets triggered a checksum mismatch against a long-lived dev database's recorded checksums"
impact: medium — none of the three failures were caused by the feature under test (#308), but each looked like a real bug until traced to environment/tooling causes, costing real debugging time before the actual feature could be verified
related_lessons:
  - docs/wiki/learned-lessons/dotenv-not-auto-loaded-by-local-processes.md
  - docs/wiki/learned-lessons/known-table-drift-list-should-be-checked-before-writing-changesets.md
---

# `./mvnw spring-boot:run` doesn't read `.env`, and this repo has two MySQL instances competing on similar ports

While manually verifying #308 (frontend forgot/reset-password pages) in a real browser, starting the backend directly via `./mvnw spring-boot:run` failed twice in a row with two different, unrelated errors — neither caused by the #308 code changes themselves.

**1. `.env` is docker-compose-only, not Spring-Boot-aware.** `docker compose up -d mysql redis elasticsearch` reads `.env` automatically (that's docker compose's own behavior), but `./mvnw spring-boot:run` run directly on the host does **not** — this repo has no `spring-boot-docker-compose` dependency and no dotenv loader wired into the Java process. Without exporting `.env`'s variables into the shell first, Spring falls back to `application.properties`'s defaults (`localhost:3306`, `root`, empty password).

**2. A native (non-Docker) MySQL was already listening on port 3306.** This WSL2 box has a systemd-managed native MySQL service on the default port 3306, separate from the Dockerized MySQL mapped to host port 3307. Spring's default fallback (`localhost:3306`) silently connected to the *wrong* MySQL instance and got a real, correctly-formed "Access denied" error — which looked like a credentials problem, but was actually a wrong-target problem. `docker exec`-testing the same password against the Docker container succeeded immediately, which was the tell.

**Fix:** `set -a; source .env; set +a` before `./mvnw spring-boot:run`, so the shell exports `SPRING_DATASOURCE_URL` (pointing at port 3307) and credentials into the Java process's environment.

**3. Separately, after fixing the datasource, Liquibase failed with a checksum mismatch** on `20260624-002-create-product-variants.xml` and `20260624-003-create-product-images.xml` — both had been legitimately modified in earlier sessions (#81/#82, adding `tableExists` drift guards and fixing FK references) after this same long-running dev database had already recorded their *old* checksums. This is expected per this repo's own Liquibase rule ("modifying an applied changeset causes a checksum mismatch") — not a new bug, just a consequence of a long-lived dev database predating those fixes.

**Fix (dev-database-only, never do this against a shared/production database without team sign-off):**
```sql
UPDATE DATABASECHANGELOG SET MD5SUM = NULL WHERE ID IN (
  '20260624-002-create-product-variants',
  '20260624-003-create-product-images'
);
```
Setting `MD5SUM` to `NULL` makes Liquibase recompute and accept the current file's checksum on next startup, without re-running the changeset (it's already marked executed).

**How to apply:** Before assuming a `spring-boot:run` failure is caused by whatever code change prompted the run, check whether it's actually an environment issue: (a) was `.env` exported into this shell? (b) is something else already listening on the port Spring's *default* config would use, distinct from the Docker-mapped port? (c) if Liquibase complains about a checksum, check whether the changeset was legitimately modified in a previous session after this database last ran it — if so, clearing `MD5SUM` on a disposable dev database is correct, not a workaround.

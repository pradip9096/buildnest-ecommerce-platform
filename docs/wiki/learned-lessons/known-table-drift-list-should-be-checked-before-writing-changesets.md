---
title: "A Repo's Own 'Not Liquibase-Managed' Table List Should Be Checked Before Writing a New Changeset, Not Rediscovered by a Failing Test"
category: process
tags: [liquibase, tech-debt-tracking, changeset-authoring, table-drift, preconditions]
keywords: [products not created by liquibase, cart_items not liquibase managed, tableExists precondition, MARK_RAN, rediscovering known drift, check existing docs before empirical debugging]
source_conversations: [Session 2026-07-05]
last_updated: 2026-07-05
confidence: high
evidence_strength: strong
root_cause: "a new changeset was written against products/cart_items without first checking an existing sibling changeset's inline comment or carried-forward project memory, both of which already documented that those tables are Hibernate-created drift, not Liquibase-managed"
impact: low — avoidable rediscovery cost (a standalone probe test) of an already-documented fact; caught before merge with no lasting effect
related_lessons:
  - docs/wiki/learned-lessons/liquibase-seed-verification-under-hibernate-create-drop.md
  - docs/wiki/learned-lessons/verify-issue-premises-against-repo-before-implementing.md
---

# A Repo's Own "Not Liquibase-Managed" Table List Should Be Checked Before Writing a New Changeset

## Problem

While implementing #305, a new Liquibase changeset was written referencing `products` and
`cart_items` with plain `UPDATE` statements. Running it against a fresh H2 database built purely
from this repo's Liquibase changelog failed with `Table "CART_ITEMS" not found`. Root-causing it
took a standalone probe test (run the full master changelog, list resulting tables) that showed
only singular-named tables (`product`, `cart_item`) exist in the legacy `.sql` master — the
plural `products`/`cart_items` tables the application actually uses are created **only by
Hibernate**, never by Liquibase.

This was already a known, previously-documented fact in this project. `20260704-012-alter-
product-add-is-featured.xml`'s own inline comment says outright: *"'products' is not created by
any Liquibase changeset in this repo — same drift class as #241 and the categories/inventory
precedents."* And this session's own carried-forward project memory (from session 8) already
listed `categories`, `products`, and `inventory.reservation_expires_at` as drift-affected tables
under "Local Dev Environment Gotchas."

The failing-test rediscovery wasn't wrong to run — verifying is still correct practice — but it
was avoidable. Checking the existing comment on the most recently added sibling changeset
(`20260704-012`, touching the same `products` table) or the carried-forward project memory
would have surfaced the exact same fact in seconds, before writing any SQL.

## Rule

Before writing a Liquibase changeset that touches a table, check two cheap sources first:

1. **Grep for the table name across existing changesets** (`grep -rl "products" backend/src/main/resources/db/changelog/`). If a `preConditions`/`tableExists` guard already exists on that table elsewhere, its accompanying comment usually explains why — read it.
2. **Check carried-forward project memory / this repo's own `docs/wiki/learned-lessons/` and `CLAUDE.md`-adjacent notes** for a "known drift" or "gotchas" list before assuming a table is Liquibase-managed just because a `CREATE TABLE` exists somewhere in the changelog history — that `CREATE TABLE` may be for a differently-named, orphaned, or legacy table that the application doesn't actually use.

Only fall back to an empirical probe (run the changelog, inspect the resulting schema) when neither source has the answer, or when confirming that a documented drift is still current — not as the first move.

## Current list of tables not created by any Liquibase changeset in this repo (only exist via Hibernate)

`categories`, `products`, `cart_items`, `order_items`, `inventory.reservation_expires_at` column.
Any new changeset touching these needs `<preConditions onFail="MARK_RAN"><tableExists .../></preConditions>` (or `columnExists` for the inventory column) so it no-ops cleanly in any environment where Liquibase runs before Hibernate creates the table (e.g. the `test` profile), instead of failing every app/test startup.

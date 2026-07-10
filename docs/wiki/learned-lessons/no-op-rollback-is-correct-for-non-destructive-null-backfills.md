---
title: "An Empty <rollback/> Is Correct for a NULL-to-Default Backfill, Not a Violation of 'Always Write Rollback'"
category: process
tags: [liquibase, rollback, data-migration, backfill, changeset-authoring]
keywords: [rollback not possible, NULL backfill rollback, distinguishing destructive vs safe changesets, cannot identify touched rows after the fact]
source_conversations: [Session 2026-07-05]
last_updated: 2026-07-05
confidence: medium
evidence_strength: moderate
related_lessons:
  - docs/wiki/learned-lessons/known-table-drift-list-should-be-checked-before-writing-changesets.md
---

# An Empty `<rollback/>` Is Correct for a NULL-to-Default Backfill

## Context

This project's Liquibase rules (`spring/liquibase.md`) say rollback is mandatory on every
changeset, and explicitly warn: *"Do not skip `<rollback>` with a comment like `<!-- no
rollback needed -->` — write the inverse DDL."* That rule is aimed at DDL operations
(`createTable`/`addColumn`/etc.) where an inverse is always definable (`dropTable`/
`dropColumn`) and skipping it is usually just laziness.

Issue #306's fix included a changeset backfilling `NULL` `is_deleted` values to `false` on
`users`/`orders`. Here, a true inverse genuinely doesn't exist: after the `UPDATE ... WHERE
is_deleted IS NULL` runs, there is no way to tell which rows were `NULL` before (and should
become `NULL` again on rollback) versus rows that were already legitimately `false`. Rolling
back by re-nulling everything currently `false` would corrupt rows that were never touched.

## Rule

For a **data-only backfill** (not DDL) where the correction is one-directional-safe (the new
value was always the intended default, per the entity's own field initializer) and no
audit/before-value column exists to reconstruct the prior state, an empty `<rollback/>` is the
correct choice — not a violation of the "always write rollback" rule. Document *why* directly in
the changeset's `<comment>` (not just an XML comment), the same way the liquibase rules already
require documenting an undoable DDL drop. The distinguishing question: would attempting a
"rollback" corrupt more data than it restores? If yes, no-op is correct; if a real inverse
exists, write it.

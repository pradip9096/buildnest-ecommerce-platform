---
title: Liquibase "Skip If Table Exists" Guard Masks Permanent Schema Drift, Not Just Dev-Volume Drift
category: technical
tags: [liquibase, schema-migration, jpa, coupons]
keywords: [liquibase preconditions tableExists, dual changeset same table, schema drift, valid_from not null, coupons table, createTable skip if exists]
source_conversations: [#435]
last_updated: 2026-07-19
confidence: high
evidence_strength: strong
related_lessons: []
root_cause: A second Liquibase `createTable` changeset for an already-existing table, guarded with `preConditions onFail="MARK_RAN"` + `not tableExists`, permanently no-ops on any environment where an older, conflicting changeset for the same table already ran — the guard is defensive idempotency in isolation, but wrong when the actual intent was to supersede the older changeset's design.
impact: high — blocked every coupon creation on any environment following the changesets' normal chronological order, not just a drifted dev volume
---

# Liquibase "Skip If Table Exists" Guard Masks Permanent Schema Drift, Not Just Dev-Volume Drift

## What Happened

`AdminCouponController`'s `POST /api/v1/admin/coupons` (create coupon) returned a generic 500 on
every real attempt during live browser verification for #435 (admin coupon CRUD UI). The
controller's own catch block swallows the real exception (`"Error creating coupon"`, no detail),
so the actual cause required checking backend logs directly, then `DESCRIBE coupons` against the
dev MySQL database.

The table had **both** an old and a new column set simultaneously:

```
minimum_order_amount, valid_from, valid_until, maximum_discount_amount, description   -- old
min_order_value, expires_at                                                            -- new
```

Two Liquibase changesets both define `coupons`:

- `20260624-005-create-coupons.xml` — the original, using `minimum_order_amount`/`valid_from`
  (`NOT NULL`, no default)/`valid_until`.
- `20260711-019-create-coupons.xml` — a later rewrite matching the actual `Coupon` JPA entity
  (`min_order_value`/`expires_at`), guarded with:
  ```xml
  <preConditions onFail="MARK_RAN">
      <not><tableExists tableName="coupons"/></not>
  </preConditions>
  ```

Since `#005` runs first (its normal chronological include order in `db.changelog-master.xml`),
`#019`'s precondition always fails — the table already exists — so it **always** marks itself ran
without ever executing. The live schema permanently matches `#005`'s design. The `Coupon` entity
never sets `valid_from`, so every `INSERT` fails a `NOT NULL` constraint with no default.

## Why This Is Easy to Miss

The `not tableExists` guard reads as legitimate defensive idempotency — it matches an existing,
documented pattern in this repo for a different problem (a `@DataJpaTest`-style Hibernate-managed
schema racing ahead of Liquibase for an isolated test context). It looks safe read in isolation.
The actual bug only exists in the *relationship* between two changesets targeting the same table —
nothing in either file alone signals that a sibling changeset for the same table exists and wins
by running first. The only way to catch it is diffing the live schema against the entity directly
(`DESCRIBE <table>` vs. the entity's `@Column` mappings), not reading migration files.

**This is not dev-volume drift** (the standard "stale local MySQL volume" pattern already
documented elsewhere in this repo's lessons) — it would hit a genuinely fresh deployment too,
since `#005` always runs before `#019` on any environment following the changesets' own
chronological order.

## Fix

Write a reconciling changeset that:
1. Adds whatever the entity actually needs, each addition independently guarded with its own
   `preConditions` (a drifted dev volume, like the one that surfaced this bug, may already have
   *some* of the new columns from an earlier out-of-band Hibernate run but not others).
2. Drops the dead legacy columns the older changeset created that the entity never maps.

```xml
<changeSet id="...-reconcile-coupons-schema" author="buildnest-team">
    <preConditions onFail="MARK_RAN">
        <not><columnExists tableName="coupons" columnName="min_order_value"/></not>
    </preConditions>
    <comment>...</comment>
    <addColumn tableName="coupons">
        <column name="min_order_value" type="DECIMAL(19,2)" defaultValueNumeric="0.00">
            <constraints nullable="false"/>
        </column>
    </addColumn>
</changeSet>
```

**A dependent index must be dropped before the columns it references, or `DROP COLUMN` fails
outright.** `#005`'s own `idx_coupons_active_validity` indexed `valid_from`/`valid_until` —
MySQL didn't surface this as a blocker during manual dev verification, but H2 (used by the
backend's `test` profile) did: `liquibase.exception.DatabaseException: Column may be referenced by
"PUBLIC.IDX_COUPONS_ACTIVE_VALIDITY"`. This asymmetry is itself worth noting — testing only
against the "production-like" database can miss a real migration-ordering bug that a stricter
test-profile database catches; the fix (`dropIndex` before `dropColumn`) had to be added only
after running the full backend test suite, not just the new feature's own targeted tests.

## Generalizes Beyond This Repo

Any Liquibase-managed project. The takeaway is not "never use `tableExists` preconditions" — they
are legitimate for the Hibernate-race case this repo already documents elsewhere. It is that
adding a **second `createTable` changeset for an already-existing table's logical entity is the
wrong pattern entirely**; the correct move when a table's design needs to change is always an
`addColumn`/`dropColumn`/`renameColumn` migration against the *original* changeset, never a second
`createTable` guarded to "only run if missing."

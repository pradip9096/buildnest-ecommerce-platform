---
title: Trace Every Consumer of Shared Data Before Trusting a Bug Report's Stated Severity
category: process
tags: [github-issues, severity-miscalibration, data-integrity, dataflow-tracing, checkout, inventory]
keywords: [severity miscalibration, dual data model, trace all readers and writers, checkout silently broken, low priority mislabeled, hasStock returns false instead of throwing]
source_conversations: [Session 2026-07-18]
last_updated: 2026-07-18
confidence: high
evidence_strength: strong
root_cause: "the issue reporter verified the symptom they personally observed (an empty admin tab) but never traced the other consumers of the same underlying data (Inventory), so a severity claim ('Low, storefront unaffected') was asserted without checking whether anything else depended on that data"
impact: high — checkout silently rejected every product with no backing Inventory row (InventoryServiceImpl.hasStock() returns false, not an exception, when no row exists), filed and nearly fixed as a cosmetic priority:low admin-UI bug
related_lessons:
  - docs/wiki/learned-lessons/verify-issue-premises-against-repo-before-implementing.md
  - docs/wiki/learned-lessons/verifying-a-stale-premise-can-surface-a-bigger-bug.md
---

# Trace Every Consumer of Shared Data Before Trusting a Bug Report's Stated Severity

## Problem

Issue #309 was filed as `priority: low` — "Admin Inventory tab shows 'No inventory data found'
for all seeded products, despite the storefront correctly showing 'In Stock'." The issue's own
evidence was accurate: a direct DB query showed `inventory` had 0 rows while `products` had 8.
The stated root cause was also accurate: `Product.stockQuantity` (read by the storefront) and the
`inventory` table (read exclusively by the Admin Inventory tab, per the issue) are two independent
representations of stock with no seed data keeping them in sync. The issue explicitly asserted
"Low severity functionally... storefront stock display unaffected."

That severity claim was wrong. Grepping every consumer of `Inventory` (not just the one the
reporter had personally observed — the admin tab) found `CheckoutServiceImpl.hasStock()` and
`reserveStock()` both go through `InventoryRepository.findByProduct()`, and `hasStock()` returns
`false` — not an exception — when no `Inventory` row exists. That `false` feeds directly into
`validateCartForCheckout()`, which rejects the cart as "insufficient stock." The empty `inventory`
table wasn't just breaking an admin UI — it was silently rejecting checkout for every real product
in the catalog, confirmed live against the running dev database before scoping the fix.

## Why this is distinct from "verify an issue's claims" and "read the whole function you're
already in"

Two related lessons already exist in this repo and don't fully cover this case:

- [Verify Issue Premises Against Repo Before Implementing](verify-issue-premises-against-repo-before-implementing.md)
  is about *specific factual claims* (which file, which metric) being stale or wrong — #309's
  factual claims (the DB query results, the root-cause description) were all **correct**.
- [Verifying a Stale Premise Can Surface a Bigger Bug](verifying-a-stale-premise-can-surface-a-bigger-bug.md)
  is about reading *further in the same code region you're already investigating* and finding an
  **unrelated, adjacent** bug sitting nearby. #309 wasn't an adjacent bug — it was the exact same
  root cause (empty `inventory` table) having a different, larger blast radius than the reporter
  checked for.

The gap this lesson fills: even when every fact in an issue is correct, a **severity/impact
claim** is a separate assertion that needs its own verification — specifically, grepping every
reader *and* writer of the data structure named in the bug, not just the one location the reporter
happened to look at.

## Rule

When a bug report describes a data-integrity defect (a missing/stale/duplicated table, column, or
field) and states a severity or blast-radius conclusion ("Low, X is unaffected"), don't accept that
conclusion at face value even when the underlying facts check out. Grep every consumer of the
affected data across the codebase before scoping the fix:

```bash
grep -rln "Inventory\b" src/main/java/.../service   # every service touching the entity
```

Then read each match to determine whether it *reads* the data in a way that could silently
degrade (return `false`/`null`/empty instead of throwing) rather than loudly fail — a silent
degradation is exactly the shape that lets a real defect hide behind a "low severity" label,
since nobody sees an error, they just see stock/checkout/whatever quietly not working. If tracing
surfaces a materially different severity than what's filed, treat it as a premise correction: state
it explicitly to the user (not silently reclassify or silently keep the original label) and let
that drive both the priority label and the process tier the fix goes through.

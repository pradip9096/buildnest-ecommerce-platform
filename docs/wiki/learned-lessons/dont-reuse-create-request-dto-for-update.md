---
title: Don't reuse a Create*Request DTO for update when field requirements diverge
category: api-design
tags: [dto, validation, rest, create-vs-update]
last_updated: 2026-07-06
root_cause: "the update endpoint reused a Create*Request DTO whose @NotNull fields (initialStockQuantity, minimumStockLevel) were only meaningful for the creation side-effect of seeding a new inventory row, forcing every update to pad or fail validation on fields it never uses"
impact: low — caught immediately by the integration test for the new endpoint, fixed by splitting into a dedicated Update*Request DTO before merge
---

# Don't Reuse a Create*Request DTO for Update When Field Requirements Diverge

## What happened

While implementing variant CRUD endpoints (PROD-01, #81), the admin `PUT
/products/{id}/variants/{variantId}` endpoint initially reused `CreateProductVariantRequest` —
the same DTO used for `POST` — for its request body. That DTO has `@NotNull` on
`initialStockQuantity` and `minimumStockLevel`, since a *new* variant must specify its starting
inventory. But `updateVariant()` never touches inventory (only sku/size/colour/priceAdjustment/
isActive) — so every update request was forced to resend stock fields the endpoint ignored, and
omitting them (the natural thing to do on an update call) failed bean validation with a 400.

This surfaced immediately in the integration test written for the update endpoint, which sent a
realistic update-shaped body without stock fields — not a contrived edge case.

## Why this is non-obvious

Reusing one DTO for create and update looks like reasonable reuse — same entity, similar fields,
avoids duplicating a class. The trap is that "similar fields" isn't "same validation
requirements": fields that are mandatory at creation time (because they seed a *different*
resource, like the inventory row) are often meaningless or already-set at update time. Sharing
the DTO couples the two operations' validation rules together even though their actual field
requirements have quietly diverged.

## Rule

Before reusing a `Create*Request` DTO for an update endpoint, check whether every `@NotNull`/
required field on it is still meaningful for update. If any required field belongs to a
side-effect that only happens at creation (spawning a related row, setting an immutable value),
split into a dedicated `Update*Request` DTO with only the fields the update path actually uses —
don't force callers to pad requests with values the endpoint will silently discard.

## Related
- [Setting a cascade=ALL mappedBy field after save() can trigger a duplicate insert](setting-a-cascade-all-mappedby-field-after-save-can-duplicate-insert.md)

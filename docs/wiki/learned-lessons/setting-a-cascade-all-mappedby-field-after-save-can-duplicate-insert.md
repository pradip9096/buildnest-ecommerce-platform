---
title: Setting a cascade=ALL mappedBy field after save() can trigger a duplicate insert
category: jpa
tags: [jpa, hibernate, cascade, onetoone, mappedby, duplicate-insert]
last_updated: 2026-07-06
root_cause: "setting a mappedBy/cascade=ALL back-reference field on an already-saved, still-managed parent marks it dirty, and Hibernate's cascade check is 'is this association dirty' not 'does this instance already have a row', so the next auto-flush re-cascades a second INSERT for an already-persisted child"
impact: medium — an intermittent DataIntegrityViolationException that only surfaced in real H2 integration tests, invisible to Mockito-based unit tests since mocks don't cascade
---

# Setting a cascade=ALL mappedBy Field After save() Can Trigger a Duplicate Insert

## What happened

While implementing per-variant inventory (PROD-01, #81), `ProductVariantServiceImpl.createVariant()`
did this:

```java
ProductVariant savedVariant = productVariantRepository.save(variant);

Inventory inventory = new Inventory();
inventory.setVariant(savedVariant);
inventoryRepository.save(inventory);   // Inventory row inserted here, with a real ID

savedVariant.setInventory(inventory);  // looked harmless — just wiring the in-memory graph
return savedVariant;
```

`ProductVariant.inventory` is the mappedBy (inverse) side of a `@OneToOne` with `cascade =
CascadeType.ALL`. Setting it on `savedVariant` — an entity still managed in the current
persistence context — marked the entity dirty. On the next flush (triggered by an unrelated
`productVariantRepository.findByProductId()` query later in the same transaction), Hibernate
cascaded the `inventory` association again and attempted a second `INSERT` for the *same*
`Inventory` instance, which collided with the unique index on `inventory.variant_id` and threw
`DataIntegrityViolationException`.

This did not show up in Mockito-based unit tests (mocks don't cascade), only in the real H2
integration test — and even there, only intermittently depending on which later query in the same
test method happened to force the auto-flush.

## Why this is non-obvious

The intuition is that setting a field to an object that's *already been persisted and has an ID*
should be a no-op for Hibernate — "it's already saved, so setting the reference again doesn't
re-save it." That's true for the owning side of most associations, but cascade rules on a
mappedBy association don't check "does this specific instance already have a database row" —
they check "is this association dirty," and cascade the persist/merge operation regardless.
Whether that manifests as a harmless duplicate-persist-of-a-managed-entity (no-op) or an actual
second `INSERT` depends on subtle state (was the entity the *exact* one already known to that
session, is IDENTITY generation involved, etc.) — the failure mode is not something you can
reason about from the annotations alone without knowing this Hibernate cascade behavior.

## Rule

After explicitly `save()`-ing a related entity through its own repository, don't also assign it
to the mappedBy/cascade=ALL back-reference field on the just-saved parent, if the parent is still
managed in the same persistence context. Either:

- Skip setting the back-reference in-memory entirely (the FK is already correct in the DB via the
  owning side), and re-fetch the parent (with an `@EntityGraph` covering the association) when the
  caller needs the populated object graph in the response, or
- Set the back-reference *before* the parent's own first `save()`, so both sides are established
  in a single cascade-persist pass instead of two separate explicit saves plus a manual field set.

## Related
- [A Repo's Own "Not Liquibase-Managed" Table List Should Be Checked Before Writing a New Changeset](known-table-drift-list-should-be-checked-before-writing-changesets.md)

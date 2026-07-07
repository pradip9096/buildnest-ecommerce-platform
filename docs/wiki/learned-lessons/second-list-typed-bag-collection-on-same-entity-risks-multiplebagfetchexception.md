# Second List-typed bag collection on the same entity risks MultipleBagFetchException

When adding `Product.images` (PROD-02, #82) alongside the pre-existing `Product.variants`
(PROD-01, #81) — both naturally `List<T>`-typed `@OneToMany` collections — wiring both as
eagerly-fetch-joinable bags on `Product` was deliberately avoided.

**Why:** Hibernate throws `MultipleBagFetchException` if a query ever tries to fetch-join two
`List` (bag) collections on the same root entity in one query — the Cartesian product makes
row mapping ambiguous. This wouldn't surface at compile time or even in most tests; it only
fails the moment someone writes (or an `@EntityGraph` implicitly generates) a query that joins
both collections together. #82's acceptance criteria didn't require images to appear embedded
in `Product` JSON responses (unlike #81's variants), so `ProductImageService` queries
`ProductImageRepository` directly — the same pattern already used by
`ProductVariantService.getVariantsByProduct` — instead of adding a second bag association.

**How to apply:** Before adding any new `@OneToMany` collection to an entity that already has
one `List`-typed collection, check whether the new collection is actually required to appear
in the same fetch-joined query as the existing one. If not, keep it as a repository-level query
instead of an entity-level collection. If both truly must be embedded together, use `Set`
instead of `List` for at least one of them (bags only conflict with other bags, not with sets).

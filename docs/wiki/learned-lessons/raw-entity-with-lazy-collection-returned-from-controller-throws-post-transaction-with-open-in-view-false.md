---
title: Raw Entity With a Lazy Collection Returned From a Controller Throws Post-Transaction When open-in-view Is False
category: jpa
tags: [hibernate, jpa, lazy-loading, open-in-view, serialization, jackson]
keywords: [LazyInitializationException, open-in-view, Hibernate.initialize, ManyToMany, Jackson serialization, ResponseEntity]
source_conversations: ["#425", "#426", "#440", "#427", "#441"]
last_updated: 2026-07-19 (extended by #441 — cache round-trip asymmetry)
confidence: high
evidence_strength: verified — reproduced live against a running backend, root-caused via stack trace, fixed and re-verified
related_lessons: [service-layer-mocked-unit-tests-can-fully-cover-a-method-while-its-query-logic-stays-untested.md]
root_cause: with spring.jpa.open-in-view=false, the Hibernate session/transaction closes when the @Transactional service method returns; Jackson serializes the ResponseEntity body afterward, and accessing an uninitialized lazy collection proxy at that point throws LazyInitializationException instead of a lazy-load
impact: high — every real edit through AdminProductController's update endpoint returned HTTP 500, even though the underlying UPDATE had already committed successfully
---

## What happened

#425 (admin Product CRUD UI) live-verified `AdminProductController`'s update endpoint against a
real running backend/MySQL. Creating a product worked; editing an existing one returned HTTP 500
on save. The frontend form itself was correct — the bug was entirely server-side and pre-existing,
just never previously exercised end-to-end against a persisted product with `open-in-view=false`.

## Root cause

`Product.tags` is `@ManyToMany(fetch = LAZY)`. `AdminProductController.updateProduct()` returns the
raw `Product` JPA entity inside `ApiResponse` — a violation of this repo's own `jpa.md` rule
("never return a JPA entity directly from a controller"), but one that had never actually broken
anything until now:

- On **create**, the `Product` is a brand-new instance; `tags` is a plain `new HashSet<>()`, never
  loaded from the DB, so it's not a Hibernate proxy — Jackson serializes it as an empty array with
  no problem.
- On **update**, `getProductById()` loads the *persisted* entity via the repository. Its `tags`
  field becomes a real Hibernate `PersistentSet` proxy, uninitialized (never `.size()`d or
  iterated) because no code path touches it. `spring.jpa.open-in-view` is `false` in both
  `application.properties` and `application-production.properties` — so the Hibernate session
  closes the instant the `@Transactional` service method returns. Jackson then serializes the
  `ResponseEntity` body *after* the transaction has closed, and touching the uninitialized proxy at
  that point throws `LazyInitializationException` — surfacing as a bare HTTP 500 with no useful
  message reaching the client (the controller's own `catch (Exception e)` never even runs, since
  the exception happens later, during response-body writing by the `HttpMessageConverter`).

A second, quieter symptom of the identical root cause showed up in the logs at the same moment:
`AuditLogService`'s `@Async` audit-logging call also tries to `ObjectMapper.writeValueAsString()`
the same `Product`, off the request thread, and threw the same `LazyInitializationException` —
silently swallowed since audit logging already runs in a try/catch, but real evidence the same
class of entity is unsafe to serialize from *any* code path once the session has closed.

## Why it went unnoticed until now

`getAllProducts()`/`getProductById()` share the same latent bug, but nothing before #425 had ever:
1. created a real product,
2. then fetched or updated *that exact persisted row* through one of these endpoints,
3. in a live run with `open-in-view=false` (as opposed to a `@Transactional`-wrapped test method,
   which keeps the session open through assertions and masks the bug entirely — this is why
   `AdminProductControllerIntegrationTest`'s existing tests never caught it).

## The fix

Force-initialize the lazy collection *while the session is still open*, inside the
`@Transactional` service method, before the entity is returned:

```java
// ProductServiceImpl
public Product getProductById(Long productId) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    Hibernate.initialize(product.getTags());
    return product;
}

public List<Product> getAllProducts() {
    List<Product> products = productRepository.findAll();
    products.forEach(product -> Hibernate.initialize(product.getTags()));
    return products;
}
```

This is the standard, minimal fix for this class of bug — smaller in scope than reworking every
controller to return DTOs instead of entities (the deeper architectural fix `jpa.md` actually
calls for, but out of scope for a UI issue; tracked as a separate follow-up).

## The generalizable rule

**Any `@ManyToMany`/`@OneToMany` (or lazy `@ManyToOne`/`@OneToOne`) field on an entity returned
directly from a controller is a latent `LazyInitializationException` the moment two conditions
both hold: `open-in-view=false`, and some code path loads a persisted (not brand-new) instance of
that entity without ever having touched the lazy field.** A freshly-constructed, not-yet-persisted
entity's collection fields are plain in-memory collections (not proxies) and will serialize fine —
this is exactly why bugs of this shape hide behind a passing "create" path and only surface on
"read" or "update" of an existing row. Before trusting that a `@Transactional` service method
returning an entity is safe to serialize outside the transaction, check every lazy field on that
entity: either it's already force-initialized before return (`Hibernate.initialize(...)`, an
explicit fetch-join, or an `@EntityGraph`), or the controller maps to a DTO instead. A green
`@SpringBootTest`/integration test is not proof of safety here, since the test's own transaction
context can keep the session open through the assertion, masking exactly the failure mode a real,
non-transactional HTTP request would hit.

## Recurrence

**#426** hit a sibling instance the very next issue: `ProductImage.product`'s back-reference had no
`@JsonIgnore` at all (unlike `Inventory.product`'s already-correct pattern), so any image-endpoint
response walked into `Product`'s own lazy fields post-transaction. Fixed with `@JsonIgnore` rather
than `Hibernate.initialize()`, since the back-reference was never meant to serialize at all — a
narrower fix than #425's, but the same root cause.

**#440** hit a third instance: `Inventory.thresholdBreaches` (a lazy `@OneToMany`) had no
`@JsonIgnore`, even though its *own sibling fields on the same entity* (`product`, `variant`)
already did — the entity had partially applied the fix to itself and still shipped with one lazy
field unguarded. `AdminInventoryController.getInventory()` returns the raw `Inventory` entity
directly (the same `jpa.md` violation #425 already flags), and the moment any inventory record
with an initialized threshold-breach history was fetched, the response failed with a bare 500
(`"Failed to write request"`) — no useful message reaching the client, same as #425's symptom.

**The sharper generalization from three occurrences**: checking whether "the entity already has
`@JsonIgnore` on its lazy fields" is not sufficient — every individual lazy field on the entity
needs the check, since a prior fix (or a partially-consistent original author) can leave some
fields ignored and others not, on the very same class. Grep the entity source for every
`@OneToMany`/`@ManyToMany`/lazy `@OneToOne`/`@ManyToOne` field and verify each one individually
before trusting that "this entity is already handled."

**#427** hit a fourth instance, and a new sub-shape of the same root cause:
`ProductVariantRepository.findByProductId`'s `@EntityGraph(attributePaths = {"inventory"})` never
listed `"product"` at all — unlike this bug's first three instances (a missing `@JsonIgnore` on an
already-loaded field), here the lazy `product` association was never loaded in the first place, so
Jackson threw on `product` itself, not on something reachable through it. The sibling `findById`
query on the same repository *did* correctly list `{"product", "inventory"}` — the same
"partially-consistent" pattern #440 already generalized, but at the query/`@EntityGraph` level
instead of the entity/`@JsonIgnore` level: two queries on the same repository, same entity, one
fixed and one not. Fixing the entity graph then exposed a second-order case of the *original* bug
shape one level deeper: once `product` was correctly loaded, Jackson walked into `product`'s own
lazy `tags` collection (the exact field #425 originally found) and threw the same
`LazyInitializationException` again — `ProductVariant.product` had no `@JsonIgnore` at all. Fixed
by `@JsonIgnore`-ing `product` entirely (the API consumer never needed the nested product object),
rather than `Hibernate.initialize()`-ing `tags` — the narrower fix already established by #426.

A live-verification-only bug not related to this lesson's root cause surfaced in the same session:
`createVariant()` set `createdAt` but never `updatedAt`, and `updated_at` is `NOT NULL` with no
MySQL-side fallback once Hibernate binds an explicit `NULL` — every variant creation failed with a
raw SQL error. Worth noting alongside this lesson because it was caught the same way (live browser
verification against a real backend, not CI): the existing `AdminProductVariantControllerIntegrationTest`
never created a variant through the actual create endpoint with an assertion sharp enough to notice
a persistence failure at the field level, only through direct `entityManager.persist()` fixtures
that set every field explicitly — a general reminder that fixture-seeded integration tests don't
exercise the same code path as the endpoint's own service-layer construction logic.

**Fourth-occurrence generalization**: this bug family isn't limited to "entity already loaded, lazy
field unguarded" (`@JsonIgnore` gap) — it also shows up as "field never loaded at all"
(`@EntityGraph`/fetch-join gap upstream of serialization). Both produce the identical symptom
(`LazyInitializationException` post-transaction under `open-in-view=false`) and both are invisible
to a `@Transactional`-wrapped test. When auditing a repository method for this bug class, check two
things, not one: (1) does the query's `@EntityGraph`/fetch-join actually initialize every
association the response DTO/entity needs, and (2) does every one of those now-initialized
associations itself carry `@JsonIgnore` on any further lazy fields it exposes. Fixing only one
still leaves the other path open.

**#441 hit a fifth occurrence, and two genuinely new sub-shapes of this bug family — one through a
completely different trigger point (Redis `@Cacheable`, not HTTP response serialization) that this
lesson's prior four occurrences never covered:**

1. **The cache round-trip asymmetry.** `ProductServiceImpl.getProductById()` already called
   `Hibernate.initialize(product.getTags())` (this lesson's own #425 fix) before returning — but
   `Hibernate.initialize()` only forces the proxy's *contents* to load; it does not replace the
   collection's *runtime type*. The field stays a Hibernate `PersistentSet` (for `tags`) or
   `PersistentBag` (for `variants`), not a plain `HashSet`/`ArrayList`. The method is also
   `@Cacheable`, backed by Redis with `GenericJackson2JsonRedisSerializer`'s default typing (embeds
   `@class` in the JSON). Writing to the cache works fine — Jackson can *serialize* a
   `PersistentSet` like any `Set`. Reading it back on a cache **hit** fails: Jackson tries to
   *instantiate* `org.hibernate.collection.spi.PersistentSet` directly from the stored `@class`
   name, and that class requires a live Hibernate session constructor argument, not a no-arg
   constructor — throwing inside the `@Cacheable` proxy's cache-lookup path. The practical symptom
   was a **false "product not found"** on the very next request for any product once its first
   successful fetch got cached (a 400 with `{"success":false,"message":"Product not found"}`, since
   `HomeController`'s broad `catch (Exception e)` swallowed the real
   `SerializationException`/`InvalidTypeIdException` into that generic message) — a materially
   different symptom shape than this lesson's prior HTTP-response-serialization occurrences (bare
   500 / `"Failed to write request"`), because the failure happens on the *next* request's cache
   read, not the request that populated the cache. Fixed by copying both collections into plain
   `HashSet`/`ArrayList` after `Hibernate.initialize()`, not just initializing them.
2. **The same asymmetry hits a derived getter too.** `Inventory.getAvailableQuantity()` computes
   `quantityInStock - quantityReserved` with no backing column. It serializes into the cached JSON
   fine (Jackson serializes any getter by default), but on a cache-hit deserialize, Jackson has
   nowhere to bind it — no field, no setter — and throws `UnrecognizedPropertyException`
   (`"Unrecognized field \"availableQuantity\"..., not marked as ignorable"`). `@JsonIgnore` on the
   getter was the wrong fix here: `AdminInventoryController`'s legacy endpoints still return this
   raw entity directly and the frontend (`InventoryDetailModal.tsx`) reads this exact field from
   that (non-cached) HTTP response, so removing it from *serialization* would have broken a working
   feature. The correct fix is asymmetric: `@JsonIgnoreProperties(ignoreUnknown = true)` at the
   class level, which only affects *deserialization* (unknown fields get silently dropped) and
   leaves writes untouched.
3. **`@ManyToOne` scalar references are not automatically safe** — a real, confirmed counterexample
   to an assumption this lesson's occurrences 1-4 could plausibly suggest (all four were about
   collections, never a scalar reference). `ProductReview.user` (`@ManyToOne(fetch = LAZY)`) threw
   `"Could not initialize proxy [User#6] - no session"` when `updateReview`/`markAsHelpful`/the
   paginated list methods returned a `ProductReview` fetched via a plain
   `reviewRepository.findById(...)` with no fetch-join — the proxy was never touched anywhere in
   the transaction, so it stayed uninitialized exactly like an unguarded collection would. (A
   *sibling* scalar reference, `Product.category`, had appeared to "just work" fetch-and-cache
   in earlier testing this same session — the actual reason turned out to be incidental: something
   else in that particular request path happened to touch/initialize it, not that `@ManyToOne`
   proxies are inherently exempt from this bug class. Don't infer "scalar references are safe" from
   one working example — verify it directly, the same discipline this lesson already asks for
   collections.) Fixed by `Hibernate.initialize(review.getUser())` in the service layer before every
   return, mirroring this lesson's own established `Hibernate.initialize()` pattern.

**Fifth-occurrence generalization**: this bug family's root cause ("a Hibernate-managed reference
touched outside a live session") is not scoped to HTTP response serialization — `@Cacheable`
(or any other post-transaction consumer of the entity, e.g. a message queue serializer) hits the
identical class of failure, just with a different, sometimes actively misleading symptom (a false
"not found" instead of a bare 500) because the failure surfaces on a *later* request, not the one
that populated the cache. When adding `@Cacheable` to a method that already does
`Hibernate.initialize()` for HTTP-serialization safety, that alone is not sufficient — check
whether the *cache serializer* uses default/polymorphic typing (Redis's
`GenericJackson2JsonRedisSerializer` does by default) and, if so, convert every touched
lazy-then-initialized collection to a plain type before returning, not just initialize it.

## A second, unrelated defect found via the same live-verification pass: `User.password` had no `@JsonIgnore` anywhere in the codebase

Not part of this lesson's root cause (it's a missing security guard, not a lazy-loading bug), but
found and fixed in the same #441 session and worth recording here since it was directly adjacent:
tracing why `ProductReview.user` (a raw `User` reference, once made safely serializable per the fix
above) would have leaked meant checking `User.java`'s own Jackson annotations — and `password` had
none at all. No endpoint in the codebase had ever returned a raw `User` before this issue (`UserController`/`AdminUserController`
both already map to DTOs, per this repo's own `jpa.md` convention), so nothing had actually leaked
yet — but `ProductReview.user` would have been the first to do so had the entity-graph fix above
shipped without also checking `User`'s own serialization safety. **The generalization**: when a fix
in this lesson's family makes a previously-unreachable entity newly reachable through Jackson
(whether by `Hibernate.initialize()`, a fetch-join, or removing a blocking `@JsonIgnore`), audit
that entity's *own* field-level Jackson safety before shipping — not just whether it now
initializes/loads without throwing. "It successfully serializes" and "it's safe to serialize" are
different questions; this lesson's other occurrences only ever answered the first one.

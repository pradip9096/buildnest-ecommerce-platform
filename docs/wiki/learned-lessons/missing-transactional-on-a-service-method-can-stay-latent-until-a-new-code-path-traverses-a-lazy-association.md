---
title: "A Service Method With No @Transactional Can Stay Safe Indefinitely Until New Code Traverses a Lazy Association It Never Touched Before"
category: jpa
tags: [transactional, lazy-loading, lazyinitializationexception, proxy-id-access, spring-proxy, test-transaction-masking]
keywords: [validateCheckout no Transactional, lazy proxy getId is safe without session, non-id getter on lazy proxy forces initialization, DataJpaTest ambient transaction masks missing Transactional, TransactionTemplate committed setup for non-transactional test, controller calls service method directly with no transaction]
source_conversations: ["Session 2026-07-29, issue #564"]
last_updated: 2026-07-29
confidence: high
evidence_strength: strong
root_cause: "a Hibernate lazy proxy allows calling getId() without an active session (the identifier is already known from the FK, no query needed), but calling any other getter on that proxy forces full initialization and requires an active session -- a service method can safely run with no @Transactional for years as long as every lazy association it touches is only ever read via getId(), and the gap only becomes visible the moment new code calls a real getter on a previously ID-only-accessed proxy"
impact: medium — CheckoutServiceImpl.validateCheckout had no @Transactional and was called directly from CheckoutController with no ambient transaction; this was safe only because the existing code read item.getProduct().getId() (ID-only, safe on an uninitialized proxy). Adding a district check that called item.getProduct().getSeller() (a real getter, forcing full Product initialization) would have thrown LazyInitializationException on that exact call path if @Transactional(readOnly = true) hadn't been added in the same change
related_lessons:
  - docs/wiki/learned-lessons/raw-entity-with-lazy-collection-returned-from-controller-throws-post-transaction-with-open-in-view-false.md
  - docs/wiki/learned-lessons/spring-proxy-self-invocation-bypasses-any-aop-annotation-not-just-transactional.md
---

# A Service Method With No @Transactional Can Stay Safe Indefinitely Until New Code Traverses a Lazy Association It Never Touched Before

## Problem

`CheckoutServiceImpl.validateCheckout(Long userId, Long cartId)` had no `@Transactional`
annotation of its own. It is called from three places: two already-`@Transactional` methods in
the same class (`checkoutCart`, `checkoutWithPayment`, `initiatePayment`), and directly from
`CheckoutController.validateCheckout()` — a REST endpoint with no transaction of its own. This had
never caused a problem, because every field the method read on a lazy-loaded entity was `getId()`:

```java
for (CartItem item : cart.getItems()) {
    if (!inventoryService.hasStock(item.getProduct().getId(), item.getQuantity())) { ... }
}
```

`CartItem.product` is `@ManyToOne(fetch = FetchType.LAZY)`. `item.getProduct()` returns an
uninitialized Hibernate proxy. Calling `.getId()` on that proxy is safe with **no active
session** — the identifier is already known from the foreign key column, so Hibernate doesn't
need to query anything to answer it.

Implementing FR-LOC-04 (district-scoped checkout restriction, #564) required adding:

```java
User seller = item.getProduct().getSeller();
```

`.getSeller()` is not an ID accessor — it's a real field read on the (still uninitialized)
`Product` proxy. Any getter beyond `getId()` forces Hibernate to fully initialize the proxy (a
real query), which requires an active session/transaction. Called from
`CheckoutController.validateCheckout()`'s non-transactional path, this would throw
`LazyInitializationException: could not initialize proxy - no Session`.

## Why this stayed hidden

The method had been "working" with no `@Transactional` for as long as it existed, because nothing
in it had ever crossed the ID-only-access line. A code reviewer skimming the diff for the new
district check could easily miss this, since the risk isn't in the new code's own correctness —
it's in an existing method's transaction boundary that the new code happens to be the first thing
to actually depend on.

## The fix

Add `@Transactional(readOnly = true)` directly to the method:

```java
@Override
@Transactional(readOnly = true)
public boolean validateCheckout(Long userId, Long cartId) { ... }
```

This is safe for all three existing call sites (a nested/no-op propagation inside an already-open
`REQUIRED` transaction) and fixes the new controller-direct path.

## How to actually test this — the part with no existing precedent in this repo

The obvious approach — `@DataJpaTest` or a `@Transactional`-annotated `@SpringBootTest` — **cannot
prove this fix works**, because both wrap the entire test method in one ambient transaction
(Spring's test-transaction management). Under that ambient transaction, the service method's own
`@Transactional` annotation is irrelevant — a lazy load succeeds either way, since a session is
already open regardless of what the method itself declares. A test built this way would pass
identically whether the fix was applied or reverted, giving false confidence.

The correct test needs:
1. A real, Spring-proxied service bean (`@SpringBootTest`, not a manually-constructed instance —
   proxy interception is exactly what's being verified).
2. Setup data persisted and **committed** in its own transaction, so it's genuinely detached by
   the time the actual assertion runs.
3. The test method itself carrying **no** `@Transactional` and not using `@DataJpaTest`.

```java
@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
class CheckoutValidateNoAmbientTransactionIT {

    @Autowired private CheckoutService checkoutService;
    @Autowired private PlatformTransactionManager transactionManager;
    // ... repositories ...

    @BeforeEach
    void setUp() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            // persist and save all setup entities here — this transaction
            // commits when the lambda returns, leaving the test method with
            // zero ambient transaction
        });
    }

    @Test
    void validateCheckout_noAmbientTransaction_succeeds() {
        // No @Transactional anywhere in this test.
        assertTrue(checkoutService.validateCheckout(buyerId, cartId));
    }
}
```

`TransactionTemplate` programmatically opens and commits a transaction around the lambda,
independent of any test-level transaction management. This proved the fix genuinely mattered: the
test passed with `@Transactional(readOnly = true)` present, and (verified by temporarily removing
the annotation during development) failed with `LazyInitializationException` without it — a result
a `@DataJpaTest` version of the same test could never have produced either way.

## Generalizes beyond this repo

This applies to any Spring Data JPA codebase:
- **Reading only `getId()` off a lazy proxy is always safe, with or without a session** — this is
  a general Hibernate proxy behavior, not project-specific.
- **A missing `@Transactional` on a method with lazy-association access is a latent bug that
  waits for the right code path**, not a bug that manifests immediately — code review needs to
  check the method's *own* transaction boundary whenever a change adds the first non-ID access to
  an existing lazy field, not just review the new code in isolation.
- **`@DataJpaTest`/test-class-level `@Transactional` structurally cannot test "does this method
  need its own `@Transactional`"** — the ambient test transaction makes the method's own
  annotation unobservable. The `TransactionTemplate`-committed-setup + no-test-transaction pattern
  above is the general fix for this specific testing gap.

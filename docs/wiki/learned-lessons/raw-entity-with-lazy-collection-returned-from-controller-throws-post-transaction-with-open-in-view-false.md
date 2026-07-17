---
title: Raw Entity With a Lazy Collection Returned From a Controller Throws Post-Transaction When open-in-view Is False
category: jpa
tags: [hibernate, jpa, lazy-loading, open-in-view, serialization, jackson]
keywords: [LazyInitializationException, open-in-view, Hibernate.initialize, ManyToMany, Jackson serialization, ResponseEntity]
source_conversations: ["#425"]
last_updated: 2026-07-17
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

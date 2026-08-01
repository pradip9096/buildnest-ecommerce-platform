---
name: cacheable-entity-with-undetached-lazy-associations-succeeds-on-write-fails-on-read
description: "An @Cacheable method returning a JPA entity with incompletely-detached lazy associations can succeed on the cache WRITE (first call, cache miss) and fail on every cache READ afterward (SerializationException on deserialize) — a distinct failure shape from the already-documented Jackson-HTTP-response lazy-collection crash, since this one is invisible to any test that only exercises a single request/session"
root_cause: "The method's own detach step only copied SOME lazy associations (tags/variants) into plain collections, leaving others (category, inventory — lazy @ManyToOne/@OneToOne) as live Hibernate proxies. Redis's cache serializer can write a proxy's current state successfully, but deserializing that value back into a real object on a later, session-less request fails, since a Hibernate proxy's own deserialization normally requires an active session to re-resolve"
impact: "Every product detail page succeeds on its first-ever view and fails (as a misleading '404 Product not found', since the generic catch-and-report block in the controller swallows the real SerializationException) on every subsequent view of the same product, in any environment where Redis caching is genuinely active — confirmed reproducible, not a one-off"
metadata:
  type: lesson
  originSessionId: work-on-issue-117
---

## The pattern

A partial lazy-association detach step (one that handles collections like `tags`/`variants` but
not singular `@ManyToOne`/`@OneToOne` associations like `category`/`inventory`) can look complete
because it fixes the failure mode most commonly tested — Jackson serializing the entity straight
to an HTTP response (see the sibling lesson,
[raw-entity-with-lazy-collection-returned-from-controller-throws-post-transaction-with-open-in-view-false.md](raw-entity-with-lazy-collection-returned-from-controller-throws-post-transaction-with-open-in-view-false.md)).
But an `@Cacheable` method's return value goes through a **second**, independent serialization
path — the cache provider's own serializer (Redis, here) — which has its own susceptibility to
the same class of bug, on a delay: the write can succeed (the proxy's *current* state serializes
fine), while the *read* fails, because deserializing a Hibernate proxy typically needs a live
session to resolve, which a later, unrelated request/session doesn't have.

## Why this is easy to miss

- **Cache miss (first call) always succeeds** — so a smoke test, a manual "does it work" check, or
  even most integration tests that only ever exercise one request per test method will never
  observe the failure.
- **The failure only appears on a second read of the same cache key** — from a *different*
  request/session than the one that wrote it. A test asserting cache behavior within one
  `@Transactional` test method (with an ambient session open the whole time) won't reproduce this
  either, for the same reason `@Transactional` test methods can mask missing-`@Transactional`
  production bugs elsewhere in this codebase (see
  [missing-transactional-on-a-service-method-can-stay-latent-until-a-new-code-path-traverses-a-lazy-association.md](missing-transactional-on-a-service-method-can-stay-latent-until-a-new-code-path-traverses-a-lazy-association.md)).
- **The real exception is swallowed** by a generic `catch (Exception e)` block that reports a
  misleading, unrelated-looking error ("Product not found") instead of the actual
  `SerializationException`.

## How this surfaced

Confirmed empirically and reproducibly while diagnosing #117's Playwright CI job, once a Redis
service container was added (fixing a *different*, prior bug — see
[fixing-a-masked-infra-dependency-can-surface-a-second-previously-masked-behavior.md](fixing-a-masked-infra-dependency-can-surface-a-second-previously-masked-behavior.md)
for how that unmasked this one too): `curl` the same product-detail endpoint 3 times in a row —
call 1 returns `200` with full data, calls 2 and 3 both return `404 "Product not found"`. The
actual exception, visible only in the application log (not the HTTP response), was
`org.springframework.data.redis.serializer.SerializationException: Could not read JSON:
Unrecognized field ...`.

## The fix

Fully detach **every** lazy association reachable from a `@Cacheable` method's return value before
returning it — not just collections, but singular associations too — or cache a DTO instead of the
raw entity (DTOs have no lazy-proxy fields to begin with, sidestepping the whole class of bug).

## Generalizable takeaway

When auditing a "detach lazy associations before returning/caching" step, enumerate **every**
lazy field on the entity, not just the ones a prior bug report happened to name — a fix that
handles collections but not singular associations (or vice versa) looks complete but leaves the
exact same underlying risk on the untouched fields. Verify by testing a cache/serialization
round-trip across two genuinely separate requests/sessions, never within one ambient session.

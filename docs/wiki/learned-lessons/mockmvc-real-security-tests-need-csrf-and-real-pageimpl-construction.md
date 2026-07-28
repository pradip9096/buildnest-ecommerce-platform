---
title: "A Real @SpringBootTest+MockMvc Security Test Needs `.with(csrf())` and a Real `Pageable`, or a False-Positive 403/500 Masks the Actual Assertion"
category: testing
tags: [mockmvc, csrf, spring-security, pageable, jackson, preauthorize, springboottest]
keywords: [MockMvc 403 without csrf, PageImpl unpaged getPageNumber throws, HttpMessageNotWritableException Page serialization, testing PreAuthorize through real proxy, SecurityMockMvcRequestPostProcessors csrf]
source_conversations: ["Session 2026-07-28, issue #559"]
last_updated: 2026-07-28
confidence: high
evidence_strength: strong
root_cause: "Two independent MockMvc/Jackson pitfalls that both manifest as a wrong-looking-but-plausible HTTP status, easy to mistake for the thing actually under test: (1) this repo's TestSecurityConfig keeps real CSRF protection enabled (CookieCsrfTokenRepository) for all mutating requests except /api/auth/login and /api/auth/register — any PUT/PATCH/POST/DELETE MockMvc request missing SecurityMockMvcRequestPostProcessors.csrf() gets rejected with 403 by CsrfFilter before it ever reaches the controller, which is indistinguishable from a genuine @PreAuthorize/ownership-check 403 unless deliberately checked; (2) constructing a mocked Page<T> return value with PageImpl<>(list) alone (the single-arg constructor) defaults its internal Pageable to Pageable.unpaged(), whose getPageNumber()/getPageSize() throw UnsupportedOperationException by design — Jackson's serialization of that Page then throws HttpMessageNotWritableException, surfacing as a 500 that has nothing to do with the endpoint's actual logic"
impact: "medium — while building a real @SpringBootTest+MockMvc test proving @PreAuthorize(\"hasRole('SELLER')\") is enforced through the real Spring Security filter chain (not a mocked-service unit test, which can't observe a proxy at all), both pitfalls hit in the same session: a PUT/PATCH test's expected 403 was accidentally satisfied by the CSRF filter instead of the ownership check it was meant to prove, and a GET test's mocked Page threw 500 instead of returning 200. Neither was a production bug — both were test-construction mistakes that happened to produce a plausible-looking wrong result, the exact shape that would let a security test 'pass' (or fail) for the wrong reason if not traced to its actual cause."
related_lessons:
  - docs/wiki/learned-lessons/spring-proxy-self-invocation-bypasses-any-aop-annotation-not-just-transactional.md
  - docs/wiki/learned-lessons/cors-allowedmethods-restriction-invisible-to-curl-and-mockmvc.md
---

# A Real @SpringBootTest+MockMvc Security Test Needs `.with(csrf())` and a Real `Pageable`, or a False-Positive 403/500 Masks the Actual Assertion

## Problem

Building `SellerDataIsolationIntegrationTest` (#559) — a real `@SpringBootTest`+`MockMvc` test
proving `@PreAuthorize("hasRole('SELLER')")` and service-layer ownership checks are enforced
through the real Spring Security filter chain and method-security proxy, not just asserted by a
mocked-service unit test (the same class of blind spot as
[Spring's self-invocation proxy gap](spring-proxy-self-invocation-bypasses-any-aop-annotation-not-just-transactional.md))
— hit two distinct false-positive/false-negative traps in the same session.

## Trap 1: missing CSRF token produces an indistinguishable 403

This repo's `TestSecurityConfig` keeps real CSRF protection active (`CookieCsrfTokenRepository`,
double-submit pattern) for every mutating request except `/api/auth/login`/`/api/auth/register`
(see `spring-security.md`'s CSRF section). A `PUT`/`PATCH`/`POST`/`DELETE` MockMvc request that
omits `SecurityMockMvcRequestPostProcessors.csrf()` gets rejected by `CsrfFilter` with `403` before
the request ever reaches the controller — the exact same HTTP status a genuine `AccessDeniedException`
(ownership check) or `@PreAuthorize` rejection produces. A test asserting `.andExpect(status().isForbidden())`
on a cross-seller PUT/PATCH request will pass whether or not the ownership check actually works,
as long as CSRF is also missing — a false positive that proves nothing about the thing under test.

**Fix:** add `.with(csrf())` to every mutating MockMvc request in a real-security test, then verify
the *negative* case (missing role, wrong owner) still returns 403 — now for the right reason.

## Trap 2: `PageImpl<>(list)` alone builds an unserializable `Page`

```java
// WRONG — single-arg constructor defaults pageable to Pageable.unpaged()
Page<Product> page = new PageImpl<>(Collections.emptyList());
when(productService.getProductsForSeller(eq(5L), any())).thenReturn(page);
```

`Pageable.unpaged()`'s `getPageNumber()`/`getPageSize()` throw `UnsupportedOperationException` by
contract — Spring Data's own Javadoc says as much, but it's easy to miss when the constructor call
looks perfectly reasonable for an empty list. Jackson's default `Page` serialization (via
`PageImpl`'s `pageable` field) calls `getPageNumber()` while writing the response body, so the
exception propagates as `com.fasterxml.jackson.databind.JsonMappingException (was
java.lang.UnsupportedOperationException)`, which Spring MVC then reports as a generic `500
HttpMessageNotWritableException` — nothing in the default MockMvc failure output (`Resolved
Exception: Type = HttpMessageNotWritableException`) names the real cause; it has to be chased via
`result.getResolvedException().getCause()`.

**Fix:** always construct a mocked `Page` with an explicit `Pageable`:

```java
// CORRECT
Page<Product> page = new PageImpl<>(Collections.emptyList(),
        PageRequest.of(0, 10), 0);
```

## Generalization

Both traps share a shape: a real (`@SpringBootTest`, not mocked) test is exactly the right tool for
proving proxy/filter-chain behavior, but it also means every other real-framework mechanism in the
request path (CSRF, response serialization) is live and can fail for reasons unrelated to the
assertion being written. Before trusting a real-security test's result, trace an unexpected status
to its actual cause (`result.getResolvedException()`/its `getCause()` chain) rather than assuming
the first plausible-looking status code confirms or refutes the thing under test.

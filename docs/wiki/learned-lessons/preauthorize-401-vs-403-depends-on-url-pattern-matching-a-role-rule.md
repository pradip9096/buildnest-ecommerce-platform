---
title: "A @PreAuthorize-Protected Endpoint Returns 401, Not 403, for Anonymous Requests Unless Its URL Also Matches a Role Rule in SecurityConfig"
category: security
tags: [spring-security, preauthorize, authorizehttprequests, url-matching, gatling, load-testing]
keywords: [401 vs 403 anonymous request, PreAuthorize AccessDeniedHandler AuthenticationEntryPoint, anyRequest authenticated catch-all, checkout endpoint 401, cart endpoint 403]
source_conversations: [Session 2026-08-02, issue #118]
last_updated: 2026-08-02
confidence: high
evidence_strength: strong
root_cause: "Spring Security evaluates the URL-based authorizeHttpRequests rule chain first: if a request's path matches a specific rule (e.g. /api/user/** -> hasRole('USER')), an anonymous principal fails that rule and the request is authenticated-but-forbidden, producing 403 via AccessDeniedHandler, with @PreAuthorize never even reached. If the path instead only matches the generic anyRequest().authenticated() catch-all (true for any endpoint not covered by a more specific pattern, e.g. /api/checkout/**), an anonymous principal fails the authenticated() check itself, producing 401 via AuthenticationEntryPoint before @PreAuthorize on the controller method ever runs. The method-level @PreAuthorize annotation looks like the deciding factor but is irrelevant to which status code an anonymous caller sees — the URL-pattern rule decides it."
impact: medium — caused a Gatling load-test scenario (LoadTestSimulation.java's new checkout chain, #118) to fail its own status-code assertions on the very first run against a real running instance, even though the actual application behavior was correct; would equally have caused a manually-written integration test to assert the wrong status if written from the @PreAuthorize annotation alone without checking SecurityConfig's URL rules
related_lessons: []
---

# A `@PreAuthorize`-Protected Endpoint Returns 401, Not 403, for Anonymous Requests Unless Its URL Also Matches a Role Rule in `SecurityConfig`

## Problem

While extending `LoadTestSimulation.java` (Gatling) with a new checkout-flow scenario (#118), an
anonymous `GET /api/checkout/calculate-total/{cartId}` request — a `@PreAuthorize("hasRole('USER')")`
endpoint — returned **401**, not the **403** expected by analogy with `/api/user/cart/add` (also
`@PreAuthorize`-protected, also called anonymously in the same test file).

## Root Cause

`SecurityConfig`'s `authorizeHttpRequests` chain is evaluated top-down, first match wins (see
`spring-security.md`'s URL Authorization Rules). `/api/user/**` has an explicit
`hasRole('USER')` rule; `/api/checkout/**` does not — it isn't listed anywhere in the chain, so it
falls through to the catch-all `anyRequest().authenticated()`.

- An anonymous request to `/api/user/cart/add` **matches** the `/api/user/**` rule, fails it (no
  authentication present), and Spring Security treats this as authenticated-but-forbidden →
  **403** via `AccessDeniedHandler`. `@PreAuthorize` on the controller method is never reached at
  all — the URL-level filter already rejected the request.
- An anonymous request to `/api/checkout/calculate-total/1` matches only `anyRequest().authenticated()`,
  fails *that* check (no authentication present at all) → **401** via `AuthenticationEntryPoint`.

The controller method's own `@PreAuthorize("hasRole('USER')")` annotation is identical in both
cases and plays no role in the status code an anonymous caller receives — it only matters once a
request has *already* passed the URL-level authentication check.

## How This Was Caught

A local Gatling run against a real running instance (H2-backed, booted with the same args CI
uses) failed its status-code check (`status().in(200, 403, 404)`) with `actually found 401` —
caught by running the test for real, not by reading the code. A purely code-level review of the
controller's `@PreAuthorize` annotation would not have surfaced this, since the annotation itself
gives no signal about which URL-authorization rule (if any) the path matches.

## Fix / Generalization

When writing a test (load test, integration test, or manual curl check) that asserts a specific
status code for an anonymous/unauthenticated request against a `@PreAuthorize`-protected endpoint,
don't infer 401 vs. 403 from the annotation alone — check `SecurityConfig`'s
`authorizeHttpRequests` chain for whether the endpoint's path matches a specific role-scoped rule:

- Matches a specific rule (e.g. `/api/user/**`, `/api/admin/**`) → expect **403** for anonymous.
- Falls through to `anyRequest().authenticated()` → expect **401** for anonymous.

Any future endpoint added under a path prefix not already covered by an explicit rule in
`SecurityConfig` will exhibit the 401 behavior, regardless of its own `@PreAuthorize` annotation.

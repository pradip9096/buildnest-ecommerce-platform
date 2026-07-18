---
title: "A Missing HTTP Method in Spring Security's CORS allowedMethods Is Invisible to curl and MockMvc — Only Real Browser Preflight Catches It"
category: testing
tags: [spring-security, cors, patch, mockmvc, testing, browser-verification]
keywords: [CORS allowedMethods missing PATCH, Invalid CORS request 403, CorsFilter rejects real browser only, MockMvc CORS wildcard, curl bypasses CORS preflight]
source_conversations: [Session 2026-07-18, issue #426]
last_updated: 2026-07-18
confidence: high
evidence_strength: strong
root_cause: "SecurityConfig's CorsConfiguration.setAllowedMethods() never included PATCH, so Spring Security's CorsFilter rejected any PATCH request whose Origin the browser sent for CORS validation — but CORS is a browser-enforced mechanism (via preflight OPTIONS + Origin/Access-Control-* header checks), not a server-side authorization check, so curl (no Origin header, no preflight) and MockMvc (TestSecurityConfig wildcards allowedMethods to \"*\") both bypass the exact code path that was broken"
impact: high — silently broke every PATCH-based admin endpoint (image reorder, order status update, inventory adjustment) reachable from the real frontend, with zero automated test coverage able to catch it
related_lessons:
  - docs/wiki/learned-lessons/raw-entity-with-lazy-collection-returned-from-controller-throws-post-transaction-with-open-in-view-false.md
---

# A Missing HTTP Method in Spring Security's CORS `allowedMethods` Is Invisible to curl and MockMvc — Only Real Browser Preflight Catches It

## Problem

Live-verifying #426's new "reorder product images" UI in a real browser, the `PATCH
/api/v1/admin/products/{id}/images/reorder` request failed with `403 Forbidden`, body
`Invalid CORS request` — even though the identical request via `curl` (with a valid session
cookie and CSRF token) returned `200 OK` with the correct payload, and the backend's own
`AdminProductImageControllerIntegrationTest` (`MockMvc`) already had a passing green test for
the same endpoint.

## Root Cause

`SecurityConfig.java`'s inline `CorsConfiguration` hardcoded:

```java
corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
```

`PATCH` was never in the list. Spring Security's `CorsFilter` rejects any request whose method
isn't in `allowedMethods`, once it has determined the request is cross-origin (an `Origin` header
is present). This rejection happens **before** the request reaches the controller or
`@PreAuthorize` — it's a CORS check, not an authorization check, and the two produce different
symptoms (`403 Invalid CORS request` vs. a normal `403`/`401` from Spring Security's access
decision).

The reason this was invisible to every existing test:

- **`curl` never sends `Origin`/triggers a preflight** the way a browser's `fetch()`/XHR does.
  A same-machine `curl` request to `localhost:8080` looks like a same-origin request to the
  server — CORS is fundamentally a *browser*-enforced restriction (it stops the browser's JS
  from reading a cross-origin response), not a server-side access-control mechanism, so a
  non-browser client that doesn't participate in the CORS handshake sails straight through.
- **`TestSecurityConfig` wildcards `allowedMethods` to `"*"`** specifically to avoid exactly this
  kind of CORS friction in tests — which means the real, restrictive `SecurityConfig` (also
  `@Profile("!test")`, so it's never loaded in any test context at all) was never exercised by
  any `MockMvc` test, ever, for this or any other endpoint.

## Fix

Add the missing method to the real config:

```java
corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
```

This is necessarily a one-line, global fix — CORS `allowedMethods` can't be scoped per-endpoint
within one `CorsConfigurationSource`. Fixing the reorder endpoint's real bug also retroactively
fixed two other pre-existing, unreported breakages that shared the same root cause:
`updateOrderStatus` and `adjustInventory` (both `@PatchMapping`, both silently broken for any
real browser caller since whenever they were first added).

## Rule

When a request behaves differently through `curl`/`MockMvc` than through a real browser, and the
failure mode is specifically a `403` with a CORS-shaped error body (not a normal auth rejection),
suspect the CORS layer itself before the authorization layer — check `allowedMethods` (and
`allowedOrigins`/`allowedHeaders`) against the exact HTTP method being used. More generally: a
test harness that widens or bypasses a security mechanism for convenience (`TestSecurityConfig`'s
`"*"` wildcard, `@Profile("!test")` excluding the real config entirely) means that mechanism has
**zero automated coverage**, full stop — not "coverage with some gaps." The only verification that
can catch a defect in a mechanism a test suite structurally cannot exercise is empirical: a real
browser (or an equivalent that participates in the actual protocol) hitting the actual endpoint.
This is the same tier-4/E2E category `testing.md`'s type-selection procedure already names for
"defects only observable with real infrastructure" — CORS preflight is exactly such a case, and
this incident is now that tier's second concrete worked example.

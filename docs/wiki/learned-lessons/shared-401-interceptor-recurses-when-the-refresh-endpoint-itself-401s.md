---
title: "A Shared 401-Retry Interceptor Recurses Infinitely When the Refresh Endpoint Itself Returns 401"
category: technical
tags: [frontend, auth, token-refresh, infinite-loop, fetch-interceptor, dos]
keywords: [401 interceptor infinite loop, silent token refresh recursion, refresh endpoint returns 401, unauthorizedHandler recursion, EventSource cookie auth withCredentials]
source_conversations: [Session 2026-07-20 (#444, filed #516)]
last_updated: 2026-07-20
confidence: high
evidence_strength: strong
root_cause: "The shared HTTP client's 401 interceptor calls the refresh function on ANY 401 response, including the refresh endpoint's own response — the isRetry guard only protects the original failed request, not the refresh call itself, so a 401 from /api/auth/refresh re-triggers the same interceptor and recurses without bound"
impact: "high — reproduced as 2,984 POST requests to /api/auth/refresh in a few seconds from a single unauthenticated page load, a real client-driven DoS shape against the auth endpoint"
related_lessons: []
---

# A Shared 401-Retry Interceptor Recurses Infinitely When the Refresh Endpoint Itself Returns 401

## Problem

While doing live browser verification of #444 (wiring a frontend `EventSource` consumer for an
existing SSE endpoint), simply loading the app unauthenticated — no user interaction, just
`AuthContext`'s mount-time `restoreSession()` calling `fetchProfile()` — produced thousands of
`POST /api/auth/refresh` requests in a few seconds, all returning 401. This is unrelated to #444's
own scope (EventSource/SSE consumption) and was filed as its own follow-up (#516) rather than fixed
inline, per this repo's Mid-Implementation Scope Discovery policy.

## Why It Happened

`client.ts`'s `request()` function implements silent-refresh-and-retry: on a 401, it calls the
registered `unauthorizedHandler` (which calls `apiRefresh()`) once, guarded by an `isRetry` flag so
the *original* failed request doesn't retry more than once. But `apiRefresh()` itself calls
`requestData` → `request()` — the exact same function, with no `isRetry` flag set, because from its
own point of view it's a fresh top-level call, not a retry.

So when the refresh_token cookie is missing or invalid (true for any anonymous visitor, or anyone
whose session has genuinely expired), the chain is:

1. `fetchProfile()` → 401 → `request()` calls `unauthorizedHandler()`
2. `handleUnauthorized()` calls `apiRefresh()` → also goes through `request()`
3. `/api/auth/refresh` itself returns 401 — this call has `isRetry` unset (it's not a retry of
   step 1, it's a brand-new request)
4. Step 3's 401 triggers `unauthorizedHandler()` again → back to step 2, forever

The `isRetry` guard was designed to stop *one specific request* from retrying more than once — it
has no mechanism to stop the refresh call itself from being treated as just another 401-eligible
request that reuses the same interceptor path.

## Fix / Correct Pattern

The refresh call (and logout, for the same reason) must bypass the 401-interceptor entirely rather
than going through the same generic `request()`/`requestData()` path as ordinary API calls — either
via a raw `fetch()` call (this codebase already has a working precedent for this exact shape:
`apiFetchCsrf()` uses a bare `fetch()` instead of `request()`), or via an explicit option on
`request()` that skips the `unauthorizedHandler` call for that one path. The general principle: any
endpoint whose own failure response is the *trigger condition* for a retry-on-401 interceptor must
never be routed through that same interceptor, or its own failure becomes the interceptor's next
input.

## Generalization

This is not a BuildNest-specific bug — it applies to any frontend using a shared HTTP-client-level
401-interceptor pattern (axios interceptors, a custom fetch wrapper, etc.) for silent token refresh.
The specific failure mode — the refresh/reauth endpoint being indistinguishable, from the
interceptor's point of view, from any other API call — recurs across frameworks and is worth
checking for explicitly whenever reviewing or building this pattern: does the refresh call itself
go through the same 401-handling code path as everything else?

## Related Articles

None yet — first instance of this specific interceptor-recursion gotcha in this repo's lesson set.

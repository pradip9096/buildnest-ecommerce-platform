---
title: Spring Security Only Enforces UserDetails#isEnabled() at Login — Not on Every Request
category: technical
tags: [spring-security, jwt, authentication, isEnabled, session-termination, gdpr]
keywords: [isEnabled, isActive, DaoAuthenticationProvider, JwtAuthenticationFilter, disabled account still authenticated, stateless JWT revocation]
source_conversations: [Session 2026-08-03, #128]
last_updated: 2026-08-03
confidence: high
evidence_strength: strong
root_cause: "UserDetails#isEnabled()/isAccountNonLocked()/etc. are enforced by DaoAuthenticationProvider's AccountStatusUserDetailsChecker only during the authenticate() call at login time; a stateless JWT filter that manually builds a UsernamePasswordAuthenticationToken from a decoded token bypasses that provider entirely, so nothing re-checks isEnabled() on later requests."
impact: high — a soft-deleted/deactivated account's already-issued access token kept working until its natural expiry (up to 24h, or the recommended 15min in prod) despite isActive=false being set and correctly wired into UserDetails#isEnabled()
related_lessons: []
---

# Spring Security Only Enforces `UserDetails#isEnabled()` at Login — Not on Every Request

## Problem

BuildNest's `CustomUserDetailsService.loadUserByUsername()` correctly wires `User.isActive`
into `UserDetails#isEnabled()`. It was reasonable to assume this meant a deactivated account
(`isActive = false`) would immediately stop being able to authenticate — and a code comment in
`UserServiceImpl.deleteUser()` asserted exactly that ("Immediately invalidates the account for
future auth"). It was wrong.

`isEnabled()`/`isAccountNonExpired()`/`isAccountNonLocked()`/`isCredentialsNonExpired()` are only
ever checked by `DaoAuthenticationProvider`'s `AccountStatusUserDetailsChecker`, which runs
**inside `AuthenticationManager.authenticate()`** — i.e., only during login (`/api/auth/login`)
or a fresh password-based auth attempt. `JwtAuthenticationFilter.doFilterInternal()` never calls
`authenticate()` on a decoded JWT — it manually constructs a
`UsernamePasswordAuthenticationToken(userDetails, null, authorities)` and drops it straight into
`SecurityContextHolder`. That constructor and `SecurityContextHolder.setAuthentication()` do not
consult `isEnabled()` at all.

**Net effect**: after `DELETE /api/user/account` (#128, GDPR right to erasure) set
`isActive = false` and revoked refresh tokens, the user's *already-issued access token* kept
authenticating every subsequent request — including sensitive ones like the new data-export
endpoint — until the token's own natural expiry. Only obtaining a *new* token via refresh was
actually blocked.

## Fix

Add an explicit `userDetails.isEnabled()` check in the JWT filter itself, after loading
`UserDetails` and before building the authentication token:

```java
UserDetails userDetails = userDetailsService.loadUserByUsername(username);
if (userDetails.isEnabled()) {
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    // ... set on SecurityContextHolder
} else {
    log.debug("JWT valid but account is disabled/deleted: {}", username);
}
```

This re-queries the user's current `isActive` state on every request (via
`loadUserByUsername`, already called unconditionally), so a still-valid JWT for a disabled
account is rejected immediately rather than waiting for expiry.

## Generalizes To

Any stateless-JWT Spring Security setup that manually builds an `Authentication` token from a
decoded claim, bypassing `AuthenticationManager`/`AuthenticationProvider` entirely. If account
status (active/locked/expired) needs to be enforceable *mid-session*, not just at login, the
filter itself must re-check `isEnabled()` (or equivalent) on every request — `UserDetails`'s
account-status flags are not automatically re-validated just because they exist on the object.

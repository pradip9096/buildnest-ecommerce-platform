---
title: A Controller's "Public" Javadoc (or Even SRS/SDD's Documented Access Level) Is Not Proof of Actual permitAll() Enforcement
category: security
tags: [spring-security, documentation-drift, access-control]
keywords: [SecurityConfig, permitAll, javadoc, SRS, SDD, authorization, 401]
source_conversations: ["#443"]
last_updated: 2026-07-20
confidence: high
evidence_strength: direct — reproduced via curl (401 before fix) and live browser verification (200 after fix)
related_lessons: [profile-excluded-config-classes-need-shared-constants-with-their-test-double.md]
root_cause: SecurityConfig's authorizeHttpRequests() permitAll() list is the only thing that actually gates a request — a controller's own javadoc, and even SRS/SDD's stated "Public" access level, are descriptive documentation with no mechanical link back to that list, so they can silently drift out of sync with it.
impact: medium — no endpoint that was ever supposed to require auth stayed exposed; the drift was in the opposite, availability-breaking direction (a documented-public endpoint was actually unreachable by its real audience) rather than a security exposure
---

## What happened

`InventoryStatusController`'s class-level javadoc read "Public controller for
inventory status viewing... Accessible to all authenticated users" and both
the SRS Appendix A.15 endpoint catalogue and SDD's endpoint table already
listed its three GET endpoints (`/status`, `/details`, `/available`) as
`Public`. None of that was backed by `SecurityConfig`'s actual
`authorizeHttpRequests()` `permitAll()` block — the path was simply absent
from it, so every request fell through to the catch-all
`anyRequest().authenticated()`. Any unauthenticated visitor to a
guest-visible product page would have received a 401 the moment the
frontend was wired to call it (#443).

## Why it's non-obvious

Three independent documents (a class javadoc, an SRS appendix, an SDD
endpoint table) all agreed on "Public" — which reads as corroborating
evidence, not as three copies of the same unverified assumption. None of
the three documents has any mechanical connection to `SecurityConfig`;
each one was likely written by intent ("this *should* be public") rather
than by reading the actual `permitAll()` list at the time. Because the
gap only manifests as a 401 for *unauthenticated* callers, it stayed
invisible through every test that exercises the controller
authenticated (unit tests calling the controller method directly bypass
the security filter chain entirely) and through any manual/admin testing
session where the tester is already logged in.

## The generalizable check

Before trusting "this endpoint is public" from any source other than the
security config file itself — a javadoc comment, an SRS/SDD access-level
column, a teammate's assertion — grep `authorizeHttpRequests()`'s actual
`permitAll()`/`hasRole()` list (and, in this repo, its `TestSecurityConfig`
mirror, which must be updated in the same change per
[profile-excluded-config-classes-need-shared-constants-with-their-test-double.md](profile-excluded-config-classes-need-shared-constants-with-their-test-double.md))
for the exact path. If the endpoint is meant to be consumed by an
unauthenticated caller (a guest-visible page, in this case), verify with an
actual unauthenticated request (`curl` with no cookies/headers, or a fresh
incognito-equivalent browser context) — not a MockMvc/integration test that
happens to run under a wildcarded test security config, and not a manual
session where you're already logged in.

---
title: "A @Profile-Excluded Config Class Can Drift Silently From Its Test Double — Extract Shared Constants"
category: architecture
tags: [spring-boot, spring-security, profile, test-double, drift, dependency-injection]
keywords: [SecurityConfig Profile test never loads, TestSecurityConfig drift, hand-duplicated test config, shared constants prevent drift, untested production bean]
source_conversations: [Session 2026-07-05]
last_updated: 2026-07-05
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/known-table-drift-list-should-be-checked-before-writing-changesets.md
  - docs/wiki/learned-lessons/webmvctest-scans-filters-and-interceptors.md
---

# A `@Profile`-Excluded Config Class Can Drift Silently From Its Test Double

## Problem

`SecurityConfig` is annotated `@Profile("!test")` — it never loads during any test run in this codebase. `TestSecurityConfig`, a hand-written `@TestConfiguration` with `@Primary` beans, stands in for it in every `@SpringBootTest`. Investigating #311 (CSP hardening) surfaced that the two had drifted apart: `SecurityConfig`'s CSP had `frame-ancestors 'none'; form-action 'self'` and HSTS `preload(true)`; `TestSecurityConfig`'s CSP was just `"default-src 'self'"` with `preload` missing and no Swagger-chain isolation at all. The #237 CSP fix (removing `unsafe-inline`) had **zero regression coverage** as a direct result — no test could ever exercise the real bean to catch a future regression.

## Root Cause

Whenever a `@Configuration` class is excluded from the test profile (commonly `@Profile("!test")`, used here because the real `SecurityConfig` depends on production-only concerns like HTTPS enforcement and real CORS origins), a parallel test double is usually written to fill the gap. Nothing structurally keeps the two in sync — the test double is often written once, early, and never revisited as the real config evolves. Every literal value duplicated between them (CSP directives, HSTS settings, CORS origins, matcher rules) is a silent drift opportunity, and the drift is invisible precisely because no test ever loads the real bean to compare against.

## Fix

Extract the literal values both classes need into a small, shared, `main`-side constants class (not a test class, so both can reference it):

```java
public final class SecurityHeaderPolicies {
    public static final String MAIN_CSP = "default-src 'self'; script-src 'self'; ...";
    public static final String SWAGGER_CSP = "default-src 'self'; script-src 'self' 'unsafe-inline'; ...";
    public static final long HSTS_MAX_AGE_SECONDS = 31536000L;
    private SecurityHeaderPolicies() {}
}
```

Both `SecurityConfig` and `TestSecurityConfig` reference `SecurityHeaderPolicies.MAIN_CSP` instead of restating the string. This doesn't make the test double *identical* to production (structural differences like single-chain-vs-dual-chain can remain, and should be explicitly documented as deliberate when they do) — but it makes the specific values that matter for security assertions impossible to silently diverge, since changing one now requires touching the shared constant, which is a visible, reviewable diff rather than an easy-to-miss duplicate edit.

## Rule

When adding or modifying a `@Profile`-excluded (or otherwise test-invisible) config class that has a hand-maintained test double, check whether the values being changed are duplicated in the double. If so, extract them to a shared constants class in `main`, not just edit the production side and assume the double will be updated "later." Also worth an explicit code comment on the test double stating *why* it exists (which real class it stands in for) and *what*, if anything, is a deliberate rather than accidental divergence — this is the difference between a documented trade-off and silent drift.

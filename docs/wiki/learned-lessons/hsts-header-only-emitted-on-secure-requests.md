---
title: "Spring Security Only Emits Strict-Transport-Security on Requests It Considers Secure"
category: testing
tags: [spring-security, hsts, mockmvc, security-headers, testing]
keywords: [Strict-Transport-Security header null in test, HSTS not present MockMvc, HttpServletRequest isSecure, StrictTransportSecurityHeaderWriter requestMatcher, testing security headers]
source_conversations: [Session 2026-07-05]
last_updated: 2026-07-05
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/known-table-drift-list-should-be-checked-before-writing-changesets.md
---

# Spring Security Only Emits `Strict-Transport-Security` on Requests It Considers Secure

## Problem

Writing `SecurityHeadersTest` (#312) to assert the `Strict-Transport-Security` header matched the configured max-age, `includeSubDomains`, and `preload` settings, a plain `mockMvc.perform(get("/actuator/health"))` returned `null` for that header — even though `SecurityConfig`/`TestSecurityConfig` both configure `.httpStrictTransportSecurity(...)` unconditionally on the chain.

## Root Cause

Spring Security's `StrictTransportSecurityHeaderWriter` only writes the HSTS header when the incoming request is considered secure (`HttpServletRequest.isSecure()` — effectively, an HTTPS request). This is correct, spec-conforming behavior: HSTS tells a *browser* to only connect via HTTPS in future, so emitting it on a plain HTTP response is meaningless and some clients/proxies treat it as suspicious. MockMvc's default `get(...)` builds a non-secure (HTTP) mock request, so the header writer's own guard suppresses the header — this has nothing to do with the security config being wrong.

## Fix

Explicitly mark the mock request as secure to exercise the HSTS path:

```java
mockMvc.perform(get("/actuator/health").secure(true))
        .andExpect(header().string("Strict-Transport-Security",
                "max-age=31536000 ; includeSubDomains ; preload"));
```

Other headers (CSP, `X-Frame-Options`) are not conditioned on request security and appear on a plain non-secure `GET` — only HSTS needs this.

## Rule

Don't assume a `null` security header in a MockMvc test means the header writer/config is missing or broken — check whether that specific header type has its own request-security precondition first. When asserting the exact header string, verify the real format by running the test and reading the actual (or actual-null) value rather than asserting a remembered/assumed format — this is the same trigger-condition class as the "Verifying Standards" clause in the user's global `CLAUDE.md`: low confidence about a specific framework's exact runtime output should be checked, not asserted.

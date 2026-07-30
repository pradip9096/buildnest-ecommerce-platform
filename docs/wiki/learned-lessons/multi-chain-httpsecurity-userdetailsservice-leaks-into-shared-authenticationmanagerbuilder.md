---
title: "A Second `SecurityFilterChain`'s `.userDetailsService()` Leaks Into the App's Shared, Global `AuthenticationManagerBuilder` — Use an Explicit `ProviderManager` for Chain Isolation"
category: technical
tags: [spring-security, multi-chain, authenticationmanager, providermanager, userdetailsservice, basic-auth]
keywords: [second SecurityFilterChain wrong user store, HttpSecurity userDetailsService leaks, isolate AuthenticationManager per chain, ProviderManager explicit, DaoAuthenticationProvider chain-local, Basic Auth resolves against DB user instead of in-memory user]
source_conversations: ["Session 2026-07-15, issue #359, PR #404"]
last_updated: 2026-07-15
confidence: high
evidence_strength: strong
root_cause: "HttpSecurity.userDetailsService()/.authenticationProvider() register against the shared, application-wide AuthenticationManagerBuilder rather than building a chain-local one, even when called on a securityMatcher-scoped HttpSecurity instance for a second/third SecurityFilterChain bean — if the app already populates that shared builder with its own DB-backed UserDetailsService (as almost every Spring Security app with real user accounts does), a dedicated, isolated credential on the new chain silently resolves against the app's real user store instead of the intended isolated identity"
impact: "high if unnoticed — a dedicated machine-to-machine credential (e.g. a monitoring/scraper Basic Auth user) that is supposed to be isolated from real user accounts can end up validating Basic Auth against the DB-backed UserDetailsService instead, meaning any real username/password pair (not just the intended monitoring credential) would authenticate successfully against the new chain. Caught only via a live log line during manual testing (CustomUserDetailsService.loadUserByUsername invoked with the monitoring username) — a purely code-review or unit-test pass would not have surfaced it, since the wiring compiles cleanly and looks correct"
related_lessons:
  - docs/wiki/learned-lessons/spring-proxy-self-invocation-bypasses-any-aop-annotation-not-just-transactional.md
---

# A Second `SecurityFilterChain`'s `.userDetailsService()` Leaks Into the App's Shared, Global `AuthenticationManagerBuilder`

## Problem

BuildNest already has one `AuthenticationManager` `@Bean` populated with the app's real,
DB-backed `UserDetailsService` (`CustomUserDetailsService`), used by the main API chain for JWT
login. Issue #359 added a second, narrowly `securityMatcher`-scoped `SecurityFilterChain`
(`actuatorMonitoringSecurityFilterChain`, `@Order(0)`, matched only to `/actuator/prometheus`)
authenticated via a single, dedicated, purpose-built `monitoring` identity — deliberately never
the real ADMIN account.

The first implementation attempt did the obvious thing:

```java
InMemoryUserDetailsManager monitoringUsers = new InMemoryUserDetailsManager(
        User.withUsername(monitoringUsername)
                .password(passwordEncoder().encode(monitoringPassword))
                .roles("MONITORING")
                .build());

http
        .securityMatcher("/actuator/prometheus")
        .userDetailsService(monitoringUsers)   // looks correctly scoped to this HttpSecurity
        .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("MONITORING"))
        .httpBasic(basic -> {})
        .csrf(csrf -> csrf.disable());
```

This compiles cleanly, reads as correctly isolated (the `HttpSecurity` instance is the one
`securityMatcher`-scoped to `/actuator/prometheus`, so `.userDetailsService()` looks like it should
only apply here), and there is no compiler or startup error.

**It's wrong.** A live test confirmed the monitoring Basic Auth request was actually resolving
through `CustomUserDetailsService.loadUserByUsername("monitoring")` — the app's real, DB-backed
user store — not the intended `InMemoryUserDetailsManager`. Confirmed via an actual log line:

```
Loading user by username: monitoring
```

from `CustomUserDetailsService`, which should never have been invoked for this chain at all.

## Root Cause

`HttpSecurity.userDetailsService(...)` and `.authenticationProvider(...)` both register their
argument against `HttpSecurity`'s `AuthenticationManagerBuilder` shared object — but "shared" is
the operative word. When an application already has a globally-registered `AuthenticationManager`
`@Bean` (built from its own `AuthenticationManagerBuilder`, as is standard for a JWT-based app with
real user accounts), Spring Security's configuration machinery does not give each
`@Order`-numbered `SecurityFilterChain` bean method a fully independent
`AuthenticationManagerBuilder` by default — the shared/global one can end up winning for the
`httpBasic()`/`formLogin()` filters on the new chain too, silently overriding the local
`.userDetailsService()` call's intended scope.

This is not an isolated one-off quirk of this app's config; it is a structural property of how
Spring Security resolves `AuthenticationManager` when both a global bean and per-chain
configuration exist simultaneously — any Spring Security application with more than one
`SecurityFilterChain` bean, where at least one chain needs an authentication source genuinely
different from the app's main user store, is exposed to this.

## Fix

Build an explicit, self-contained `AuthenticationManager` for the isolated chain and set it via
`.authenticationManager(...)`, never `.userDetailsService()`/`.authenticationProvider()`:

```java
private AuthenticationManager buildMonitoringAuthenticationManager(
        InMemoryUserDetailsManager monitoringUsers) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(monitoringUsers);
    provider.setPasswordEncoder(passwordEncoder());
    return new ProviderManager(provider);   // fully independent of the shared builder
}

http
        .securityMatcher("/actuator/prometheus")
        .authenticationManager(buildMonitoringAuthenticationManager(monitoringUsers))
        .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("MONITORING"))
        .httpBasic(basic -> {})
        .csrf(csrf -> csrf.disable());
```

`.authenticationManager(...)` explicitly overrides whatever the shared object would otherwise
resolve to for this specific `HttpSecurity`/chain, closing the leak. Verified via
`ActuatorMonitoringSecurityTest` (4 cases: unauthenticated rejected, correct monitoring credential
clears the security layer, wrong credential rejected, other `/actuator/**` paths still require
ADMIN not the monitoring credential) and confirmed live against a running instance — no further
`CustomUserDetailsService.loadUserByUsername` log line for the monitoring username after the fix.

## How to Apply

Whenever a new `SecurityFilterChain` bean needs an authentication source that must stay isolated
from the application's main user store (a machine-to-machine credential, a service-account login,
a test/staging-only identity, etc.) in an app that already has its own global
`AuthenticationManager`/`UserDetailsService` bean:

- **Never** call `.userDetailsService(...)` or `.authenticationProvider(...)` directly on that
  chain's `HttpSecurity` and assume `securityMatcher` scoping protects it — those calls register
  against the shared builder regardless of the matcher.
- **Always** construct the `AuthenticationProvider`(s) explicitly, wrap them in a `ProviderManager`,
  and set it via `.authenticationManager(...)` on that specific `HttpSecurity`.
- Verify with a real request (unit test or live `curl`), not just a compile check — this bug
  produces no error of any kind, and a Mockito-mocked test cannot observe which
  `AuthenticationManager` a real filter chain actually resolves to.

# Spring Security's `corsConfigurationSource` Shadows `WebMvcConfigurer` CORS Entirely

**Category:** security
**Last Updated:** 2026-07-12

## The precedence fact

When a Spring Security filter chain defines an explicit `corsConfigurationSource` (via
`HttpSecurity.cors()`), it becomes the **only** CORS configuration source consulted for any
request that passes through that filter chain. Spring Security's own documentation states this
directly: "if `corsConfigurationSource` is defined, then that `CorsConfiguration` is used" —
Spring MVC's own CORS handling (the kind `WebMvcConfigurer.addCorsMappings` produces) is only
ever consulted as a fallback, "if no `CorsConfigurationSource` is provided."

This is not a merge or a layering — it's a full shadow. If a security filter chain has a
catch-all rule like `anyRequest().authenticated()` (i.e. every request passes through the chain),
a `WebMvcConfigurer.addCorsMappings` bean sitting alongside it is **unreachable dead code**,
regardless of what it's configured to allow.

## Why this matters beyond "extra bean"

An unreachable CORS config isn't neutral — it's actively misleading. A developer reading
`WebMvcConfigurer.addCorsMappings` (say, configured with `allowedOrigins("*")`,
`allowCredentials(false)`) would reasonably believe that's the app's actual CORS policy. It
isn't, and nothing in Spring's own error output or logs says so; the class simply never gets
invoked. The real behavior is whatever `SecurityConfig`'s `corsConfigurationSource` says, and if
that's more restrictive (specific origins, `allowCredentials(true)`), the dead `WebMvcConfigurer`
bean is a false lead for anyone debugging a CORS rejection who finds it first.

## The concrete case (#352)

A codebase had both configured at once: `SecurityConfig.filterChain`'s explicit
`corsConfigurationSource` (specific origins, `allowCredentials(true)`) and a separate
`WebMvcConfigurer.addCorsMappings` (`allowedOrigins("*")`, `allowCredentials(false)`). It wasn't
obvious from reading the code alone which one actually governed requests — both looked plausible.
The `WebMvcConfigurer` bean was confirmed dead only via a live test (see below), then deleted
entirely; the full test suite passed identically before and after removal, confirming nothing had
actually been exercising it.

## How to verify live, not just from docs

Trusting the documented precedence is a reasonable first pass, but the authoritative check is a
live preflight request pair against the running app, not a read of the source:

- **Negative control**: send a preflight (`OPTIONS`) from an origin the *restrictive* config
  (e.g. `SecurityConfig`) disallows. If the *permissive* config (e.g. `WebMvcConfigurer`'s
  wildcard) were actually in effect, this would succeed with an `Access-Control-Allow-Origin`
  header. Getting a flat 403 with **no** CORS header at all confirms the restrictive config won —
  the permissive one never got a chance to grant it.
- **Positive control**: send a preflight from an origin the restrictive config *does* allow, and
  confirm the response headers match that config exactly (specific-origin echo, matching
  `Access-Control-Allow-Credentials`, matching `Access-Control-Max-Age`) rather than the
  permissive config's shape (wildcard origin, no-credentials).

Both controls returning results consistent with the restrictive config, and inconsistent with the
permissive one, is conclusive — not just suggestive — that the permissive config is dead code.

## What to do about it

Keep exactly **one** CORS configuration source per security filter chain. If a chain needs
CORS, define it via `corsConfigurationSource` on that chain directly; do not also add a
`WebMvcConfigurer.addCorsMappings` bean expecting it to apply — it won't, for any request that
chain's `anyRequest()` rule already catches. If a genuinely different CORS policy is needed for a
different set of paths (e.g. a separate public API surface), give that surface its own
`SecurityFilterChain` with its own `corsConfigurationSource`, rather than relying on Spring MVC's
CORS handling as a second, competing source.

## See also

- [Smoke, Sanity, and Regression Testing vs. CI Test-Suite Coverage](smoke-sanity-and-regression-testing-vs-ci-test-suite-coverage.md)
  — the negative/positive-control live-verification technique used here generalizes beyond CORS
  to any "which of two configs actually governs this" question

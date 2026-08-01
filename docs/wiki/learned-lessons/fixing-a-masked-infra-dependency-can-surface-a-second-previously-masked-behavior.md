---
name: fixing-a-masked-infra-dependency-can-surface-a-second-previously-masked-behavior
description: "Adding a missing infra dependency (Redis) to fix one masked failure (a graceful-degradation gap in @Cacheable) simultaneously un-masked a second, unrelated behavior (rate limiting, which had been silently fail-open the whole time Redis was absent) — both were invisible for the same underlying reason, but neither symptom pointed at the other's cause"
root_cause: "Multiple app features can share the same underlying infra dependency (Redis, here for both caching and rate-limit counters) with different fail-open/fail-closed behaviors when that dependency is absent — fixing the presenting symptom for one feature by restoring the dependency does not restore the other feature to a *previously working* state, it restores it to its *real, previously-never-exercised* state, which can itself be a new failure"
impact: "A CI job's E2E suite passed the register/login step reliably across every fix attempt right up until Redis was added — then started failing at the exact same step for a completely different reason (rate-limit exhaustion), costing a full extra investigation round-trip that looked at first like a regression from the Redis fix itself, when it was actually a pre-existing test-design issue (each test independently registering a fresh user) that Redis's absence had been silently hiding"
metadata:
  type: lesson
  originSessionId: work-on-issue-117
---

## The pattern

A CI environment missing an infrastructure dependency (Redis, in this case) can leave *multiple*
features silently degraded at once, not just the one whose failure is currently being debugged.
Each feature's specific fail-open/fail-closed behavior when Redis is unreachable is independent:

- `@Cacheable` (declarative caching): fails **closed** — throws
  `RedisConnectionFailureException`, which propagates and surfaces as an unrelated-looking error
  (see the sibling lesson,
  [spring-cacheable-has-no-built-in-resilience-unlike-manual-circuit-breaker-usage.md](spring-cacheable-has-no-built-in-resilience-unlike-manual-circuit-breaker-usage.md)).
- Rate limiting (`RateLimiterService`, manually circuit-breaker-wrapped): fails **open** — when
  Redis is unreachable, the circuit breaker's fallback allows every request through, silently
  disabling all rate limits.

Both behaviors are invisible for the *same* reason (no Redis), but they point in opposite
directions once diagnosed: one under-serves (blocks requests that should succeed), the other
over-serves (allows requests that should be blocked). Fixing the environment to add Redis doesn't
just fix the symptom being chased — it also flips every other Redis-touching feature from its
fail-open/fail-closed default back to its *real* behavior, which may never have actually been
exercised in this environment before.

## How this surfaced

While diagnosing #117's Playwright CI job, adding a Redis service container correctly fixed the
`@Cacheable` product-lookup failure being investigated. The very next CI run then failed at the
exact same pipeline stage (register → login) for a completely unrelated reason:
`RateLimitHeaderInterceptor`'s hardcoded `AUTH_LIMIT` (5 requests per window on any
`/api/auth/**` path) was now genuinely enforced, and 3 independent test-level
`registerAndLogin` calls (6 requests total) exceeded it — a pre-existing test-design choice that
had been silently fine only because rate limiting had never actually been active in this CI job
before Redis existed.

## Generalizable takeaway

When restoring a missing infrastructure dependency to fix one specific failure, explicitly ask
"what *else* silently depended on this being absent?" before assuming the fix is complete — check
every other feature that shares the same dependency, not just the one currently being debugged.
A green run immediately after the fix is not sufficient evidence that nothing else changed;
in this case, the very next run (not the fixed one) was where the second effect appeared, since
the first run after the fix was still blocked by the original symptom.

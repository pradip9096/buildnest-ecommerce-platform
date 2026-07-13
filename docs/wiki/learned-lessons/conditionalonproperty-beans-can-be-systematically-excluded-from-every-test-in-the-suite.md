---
title: "A `@ConditionalOnProperty` Bean Can Be Silently Excluded From Every Test in the Suite, Failing a New-Code Coverage Gate Even for Simple, Correct Code"
category: testing
tags: [conditional-on-property, test-coverage, sonarcloud, codecov, spring-configuration, cache-manager]
keywords: [ConditionalOnProperty never activated in tests, new_coverage gate failure, cacheManager bean untested, spring.cache.type simple vs redis, coverage gap for simple factory method]
source_conversations: ["Session 2026-07-13, issue #84, PR #370"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "every test in the suite that touches caching deliberately overrides spring.cache.type away from redis (to none or simple) so CI doesn't need a live Redis connection — which means the @ConditionalOnProperty(spring.cache.type=redis) cacheManager bean is never created by any test, so any code added inside it (a new cache region, a new helper method) has zero test coverage by construction, regardless of how simple or obviously-correct that code is"
impact: medium — failed PR #370's SonarCloud/codecov new-code coverage gate (61.5% vs. 80% required) for a small, low-risk addition (a new Redis cache-region entry and a private helper method), not because the code was risky but because the entire bean housing it was structurally unreachable by the existing test strategy
related_lessons:
  - docs/wiki/learned-lessons/testing-cacheable-proxy-behavior-needs-cache-type-override-and-has-a-scope-limit.md
---

# A `@ConditionalOnProperty` Bean Can Be Silently Excluded From Every Test in the Suite, Failing a New-Code Coverage Gate Even for Simple, Correct Code

## Problem

PR #370 (#84) added a new Redis cache region (`relatedProducts`) and a small private
`regionConfig()` helper inside `CacheConfig.cacheManager` — a `@Bean` method itself gated by
`@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")`.

The PR added `ProductServiceCachingIntegrationTest`, a `@SpringBootTest` proving `@Cacheable`
works via the real Spring proxy — but that test deliberately overrides
`spring.cache.type=simple` (see the related lesson) precisely so it doesn't need a live Redis
connection in CI. Every other test in the suite either runs under the default test profile
(`spring.cache.type=none`) or a similar override. The consequence: **no test in the entire
suite ever activates the `redis`-valued condition**, so `cacheManager()` — and everything inside
it, including the brand-new region and helper — is never invoked by any test. SonarCloud's PR
quality gate failed on `new_coverage: 61.5%` (required: 80%), and `codecov/patch` failed
identically, both correctly reporting that the new lines had zero coverage.

This wasn't caused by the new code being risky or complex — a five-line cache-region entry and a
four-line factory helper are about as low-risk as Java code gets. It was caused by the *existing
test strategy* systematically avoiding the one condition that would ever exercise that bean at
all, for an unrelated and reasonable reason (avoiding a live Redis dependency in CI).

## Rule

When a codebase's test suite deliberately avoids activating a `@ConditionalOnProperty` (or
`@ConditionalOnBean`/`@Profile`/similar conditional) bean for infrastructure reasons (avoiding a
live external dependency), treat that bean as **structurally excluded from coverage by default**
— not just under-tested by oversight. Any new code added inside it needs its own test that
activates the condition directly, independent of the integration-test suite:

1. For a `@Bean` factory method gated by a property condition, write a **plain unit test that
   instantiates the `@Configuration` class directly** (`new CacheConfig()`) and calls the bean
   method with mocked/lightweight arguments (e.g. a mocked `RedisConnectionFactory` — building a
   `RedisCacheManager` doesn't require an actual live connection, only a factory reference) rather
   than trying to get a full Spring context to activate the real property value.
2. Do not assume "there's already an integration test that exercises caching" is sufficient
   evidence this specific bean is covered — check whether that test's property overrides actually
   reach the conditional branch in question, the same "verify at the enforcing layer, don't assume"
   discipline already required for acceptance-criteria coverage generally.
3. When a coverage gate fails on a small, simple addition, check whether the *reason* is risk
   (genuinely undertested logic) or *structural exclusion* (the bean/branch is never reachable by
   the existing test strategy) — the fix differs: risky code needs more scenario coverage; a
   structurally-excluded bean needs one dedicated, narrowly-scoped unit test that finally reaches it.

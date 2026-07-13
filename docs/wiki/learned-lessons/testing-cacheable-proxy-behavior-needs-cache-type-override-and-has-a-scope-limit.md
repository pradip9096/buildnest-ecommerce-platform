---
title: "Testing `@Cacheable` Proxy Behavior Requires Overriding the Test Profile's `spring.cache.type=none`, and Such a Test Has a Real Scope Limit Worth Stating Explicitly"
category: testing
tags: [spring, cacheable, spring-boot-test, cache-type, proxy, self-invocation, regression-test]
keywords: [spring.cache.type=none test profile, TestPropertySource cache type simple, ConcurrentMapCacheManager test, Cacheable verify repository times(1), regression test scope limit]
source_conversations: ["Session 2026-07-13, issue #84, PR #370"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "this repo's application-test.properties sets spring.cache.type=none for fast, deterministic test runs, which makes every @Cacheable annotation a no-op under the default test profile — a @SpringBootTest asserting cache-hit behavior will silently pass or fail for the wrong reason unless it explicitly overrides cache type to something real (e.g. simple/ConcurrentMapCacheManager) for just that test class"
impact: medium — without the override, a well-intentioned regression test for proxy/caching behavior would give false confidence (either erroring on missing Redis if pointed at the real cacheManager bean, or silently observing no caching at all and misreporting what it verified)
related_lessons:
  - docs/wiki/learned-lessons/spring-proxy-self-invocation-bypasses-any-aop-annotation-not-just-transactional.md
---

# Testing `@Cacheable` Proxy Behavior Requires Overriding the Test Profile's `spring.cache.type=none`, and Such a Test Has a Real Scope Limit Worth Stating Explicitly

## Problem

Following up on the self-invocation bug documented in the related lesson
(`getRelatedProducts` calling `getProductById` via `this`, silently bypassing `@Cacheable`), the
fix needed a regression test — but writing one surfaced two non-obvious issues.

**1. The test profile disables caching entirely.** `application-test.properties` sets
`spring.cache.type=none` repo-wide, for fast/deterministic test runs. Under this profile, every
`@Cacheable` annotation becomes a genuine no-op — Spring's caching abstraction still proxies the
bean, but the "cache" is `NoOpCacheManager`, so a naive `@SpringBootTest` asserting a repository
is only hit once across two calls to a `@Cacheable` method will fail (repository hit both times,
correctly, since there's no cache to hit) regardless of whether the proxy/annotation wiring is
actually correct. The fix: `@TestPropertySource(properties = "spring.cache.type=simple")` on the
test class, which activates Spring Boot's auto-configured `ConcurrentMapCacheManager` — real
caching, in-memory, no live Redis connection required (this repo's Redis-backed `cacheManager`
bean in `CacheConfig` is itself `@ConditionalOnProperty(spring.cache.type=redis)`, so it simply
doesn't activate when overridden to `simple`).

**2. The resulting test has a narrower scope than it might appear to guarantee.** The natural
regression test — call `productService.getProductById(id)` twice via the real Spring-managed
bean, assert `productRepository.findById` was invoked exactly once — genuinely proves `@Cacheable`
functions through the real proxy for *external* calls to that method. It does **not** prove that
`getRelatedProducts` won't regress back to a self-invoked `this.getProductById(...)` call,
because — traced through carefully — both the fixed version (`getRelatedProducts` calls
`productRepository.findById` directly) and the original buggy version (self-invocation silently
skipping the cache) produce the **identical observable repository-call count** for that specific
internal call path. Neither version ever benefits from `getProductById`'s own cache during a
`getRelatedProducts` invocation, so there is no behavioral difference a test of that call path
could distinguish. The only two things that actually reintroduce this exact bug are the *code
structure itself* (call via `this` vs. the repository) — caught by static analysis (SonarCloud
`java:S6809`), not a runtime test — while the regression test's real, honest value is proving the
underlying caching *mechanism* still functions correctly in general.

## Rule

1. Any `@SpringBootTest` intended to verify `@Cacheable`/`@Transactional`/other proxy-dependent
   behavior in a repo whose test profile disables caching (`spring.cache.type=none` or
   equivalent) must explicitly override that property (`@TestPropertySource` or
   `@DynamicPropertySource`) to a real, test-safe cache provider (`simple` for in-memory,
   avoiding a live Redis/Memcached dependency) — otherwise the test either fails for the wrong
   reason or silently verifies nothing.
2. Before claiming a proxy-behavior regression test "prevents bug X from recurring," trace through
   whether the specific code change actually produces a different *externally observable* result
   than the bug did. If two implementations (buggy and fixed) are behaviorally indistinguishable
   from a given call site's perspective — as here — say so explicitly rather than overclaiming
   coverage. State what the test *does* prove (the caching mechanism itself functions via the real
   proxy) as distinct from what it *cannot* prove (that a specific internal call site will never
   regress to self-invocation) — the latter is a static-analysis job, not a runtime-test job.

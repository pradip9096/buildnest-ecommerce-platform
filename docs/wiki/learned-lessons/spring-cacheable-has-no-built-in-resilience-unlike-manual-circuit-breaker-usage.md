---
name: spring-cacheable-has-no-built-in-resilience-unlike-manual-circuit-breaker-usage
description: "Spring's declarative @Cacheable propagates a cache-store connection failure (e.g. RedisConnectionFailureException) straight through the annotated method by default, even in a codebase whose documented resilience architecture says the cache tier should fall through to the database — that fallback only applies where a circuit breaker was manually wired, @Cacheable doesn't inherit it"
root_cause: "@Cacheable is a Spring AOP proxy around the method, not a call through the app's own CircuitBreaker-wrapped RedisTemplate usage. The two are structurally unrelated code paths to the same Redis instance, so a documented fallback rule for one (manual circuit-breaker usage) says nothing about the other's actual behavior."
impact: "A Redis outage causes @Cacheable-annotated read methods to throw and surface as an unrelated-looking error (in this case, a genuine product being reported as '404 not found'), rather than degrading to a real database read as the app's own resilience documentation promises for every cache-tier dependency"
metadata:
  type: lesson
  originSessionId: work-on-issue-117
---

## The pattern

A codebase can have a real, working resilience story for manually-wrapped Redis usage (a
`CircuitBreaker`-wrapped `RedisTemplate` call with an explicit `CallNotPermittedException` catch
that falls through to the database) while `@Cacheable`-annotated methods elsewhere in the same
codebase have none of that protection at all. The two look like the same "Redis dependency" from
a documentation standpoint, but are structurally unrelated at the code level:

- Manual usage: `circuitBreaker.executeSupplier(() -> redisTemplate.get(key))`, with the
  circuit breaker's own state machine and an explicit fallback in the catch block.
- `@Cacheable`: a Spring AOP proxy that calls into `RedisCacheManager`/`RedisCacheWriter`
  directly, with **no** circuit breaker in the chain at all, unless one is added via a custom
  `CacheErrorHandler`.

A team's own resilience documentation ("Redis — fall through to database, do not throw") can be
accurate for the first pattern and silently wrong for the second, since nothing enforces that a
documented fallback rule actually applies uniformly across every code path that touches the same
downstream dependency.

## How this surfaced

Confirmed empirically (not from documentation) while diagnosing a Playwright E2E CI job with no
Redis service running: `GET /api/public/products/{id}` returned `404 Product not found` for a
product confirmed to exist in the products list. The actual exception, visible in the app's own
log, was `RedisConnectionFailureException` thrown from `DefaultRedisCacheWriter.get` — inside
`ProductServiceImpl#getProductById`'s `@Cacheable` proxy — caught generically by the controller
and reported as if the product itself didn't exist. Starting a local Redis container made the
exact same request return `200` with full product data, confirming Redis reachability (not the
product's existence) was the actual variable.

## The fix

Spring Cache's extension point for this is `CacheErrorHandler` — register a custom implementation
that logs cache get/put/evict failures and lets the annotated method proceed to its real body
(i.e., hits the database) instead of the default behavior of propagating the exception:

```java
public class GracefulCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache GET failed for {}, falling through", cache.getName(), exception);
        // swallow — the @Cacheable method body still executes normally
    }
    // handleCachePutError / handleCacheEvictError / handleCacheClearError similarly
}
```

Registered via a `CachingConfigurer` bean's `errorHandler()` override.

## Generalizable takeaway

A resilience/fallback rule documented for "the cache dependency" needs to be verified against
**every actual code path** that touches that dependency, not assumed to cover a newer or
different usage pattern (declarative annotations vs. manual client calls) just because both are
nominally "the same dependency." When auditing resilience coverage, grep for both the manual
wrapping pattern *and* every `@Cacheable`/`@CacheEvict`/`@CachePut` usage separately — they are
not the same enforcement surface.

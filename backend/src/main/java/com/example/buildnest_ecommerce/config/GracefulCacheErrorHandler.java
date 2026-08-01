package com.example.buildnest_ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.RedisConnectionFailureException;

/**
 * Falls through to the underlying method (or a no-op) instead of
 * propagating on a Redis <em>connection</em> failure, matching
 * {@code resilience4j.md}'s "Redis (cache) — Fall through to database —
 * do not throw" fallback table. Spring's {@code @EnableCaching}
 * auto-detects the single {@link CacheErrorHandler} bean of this type
 * without needing a {@code CachingConfigurer}.
 *
 * <p>Only exceptions caused by a {@link RedisConnectionFailureException}
 * are swallowed. Any other cache exception (e.g. a serialization
 * failure indicating real data corruption) is rethrown, per issue #650's
 * acceptance criterion that this must not mask genuine cache-poisoning
 * bugs.
 */
@Slf4j
public class GracefulCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(
            RuntimeException exception, Cache cache, Object key) {
        handle(exception, "GET", cache.getName());
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception, Cache cache, Object key,
            Object value) {
        handle(exception, "PUT", cache.getName());
    }

    @Override
    public void handleCacheEvictError(
            RuntimeException exception, Cache cache, Object key) {
        handle(exception, "EVICT", cache.getName());
    }

    @Override
    public void handleCacheClearError(
            RuntimeException exception, Cache cache) {
        handle(exception, "CLEAR", cache.getName());
    }

    private void handle(
            RuntimeException exception, String operation,
            String cacheName) {
        if (isConnectionFailure(exception)) {
            log.debug(
                    "Redis unreachable during cache {} on '{}', "
                            + "falling through: {}",
                    operation, cacheName, exception.getMessage());
            return;
        }
        log.error(
                "Cache {} failed on '{}' due to a non-connection error",
                operation, cacheName, exception);
        throw exception;
    }

    private boolean isConnectionFailure(Throwable exception) {
        for (Throwable cause = exception; cause != null;
                cause = cause.getCause()) {
            if (cause instanceof RedisConnectionFailureException) {
                return true;
            }
        }
        return false;
    }
}

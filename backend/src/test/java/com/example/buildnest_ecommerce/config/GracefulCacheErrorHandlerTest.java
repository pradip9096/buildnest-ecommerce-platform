package com.example.buildnest_ecommerce.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link GracefulCacheErrorHandler} (#650): a Redis
 * connection failure must be swallowed (fall through), while any other
 * cache exception (e.g. a serialization failure indicating real data
 * corruption, see #651) must still propagate.
 */
class GracefulCacheErrorHandlerTest {

    private final GracefulCacheErrorHandler handler =
            new GracefulCacheErrorHandler();

    @Test
    @DisplayName("GET swallows a RedisConnectionFailureException")
    void getSwallowsConnectionFailure() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("products");
        RedisConnectionFailureException ex =
                new RedisConnectionFailureException("down");

        assertThatCode(() -> handler.handleCacheGetError(ex, cache, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("PUT swallows a wrapped RedisConnectionFailureException")
    void putSwallowsWrappedConnectionFailure() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("products");
        RuntimeException wrapped = new RuntimeException("wrapper",
                new RedisConnectionFailureException("down"));

        assertThatCode(() -> handler.handleCachePutError(
                wrapped, cache, 1L, "value"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("EVICT swallows a RedisConnectionFailureException")
    void evictSwallowsConnectionFailure() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("products");
        RedisConnectionFailureException ex =
                new RedisConnectionFailureException("down");

        assertThatCode(() -> handler.handleCacheEvictError(ex, cache, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CLEAR swallows a RedisConnectionFailureException")
    void clearSwallowsConnectionFailure() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("products");
        RedisConnectionFailureException ex =
                new RedisConnectionFailureException("down");

        assertThatCode(() -> handler.handleCacheClearError(ex, cache))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("GET rethrows a non-connection exception "
            + "(serialization/corruption)")
    void getRethrowsNonConnectionFailure() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("products");
        SerializationException ex =
                new SerializationException("corrupt payload");

        assertThatThrownBy(() ->
                handler.handleCacheGetError(ex, cache, 1L))
                .isSameAs(ex);
    }
}

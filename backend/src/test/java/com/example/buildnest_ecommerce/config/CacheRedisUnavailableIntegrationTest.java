package com.example.buildnest_ecommerce.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce
                .LettuceConnectionFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Real-Spring-proxy test for issue #650 (AC #3): a
 * {@code @Cacheable}-annotated method must still return real data — not
 * throw — when Redis is genuinely unreachable. Uses a real
 * {@code @EnableCaching} AOP proxy over a
 * {@link LettuceConnectionFactory} pointed at a closed local port
 * (fast TCP refusal, no live Redis needed) rather than mocks, since a
 * mocked {@code CacheManager} can't exercise the real cache-aspect →
 * {@link CacheErrorHandler} dispatch this fix relies on (testing.md's
 * framework/mapping-level real-context tier).
 */
class CacheRedisUnavailableIntegrationTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @DisplayName("cached method returns real data instead of throwing "
            + "when Redis is unreachable")
    void cachedMethodFallsThroughWhenRedisUnreachable() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        DummyCachedService service =
                context.getBean(DummyCachedService.class);

        assertThatCode(() -> {
            String first = service.getValue("k1");
            assertThat(first).isEqualTo("real-data-k1");
        }).doesNotThrowAnyException();

        // A second call (would be a cache hit if Redis were up) must
        // also fall through and still return real data, not a stale
        // or thrown result.
        String second = service.getValue("k1");
        assertThat(second).isEqualTo("real-data-k1");
    }

    @Configuration
    @EnableCaching
    static class TestConfig implements CachingConfigurer {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            // Port 1 on localhost refuses the TCP connection almost
            // instantly (no live Redis process required), simulating
            // "Redis is down" without a slow connect timeout.
            LettuceConnectionFactory factory =
                    new LettuceConnectionFactory("localhost", 1);
            factory.afterPropertiesSet();
            return factory;
        }

        // Must come via CachingConfigurer, not a plain @Bean -- see
        // CacheConfig#errorHandler's own javadoc for why.
        @Override
        public CacheErrorHandler errorHandler() {
            return new GracefulCacheErrorHandler();
        }

        @Bean
        CacheManager cacheManager(RedisConnectionFactory factory) {
            return RedisCacheManager.builder(factory).build();
        }

        @Bean
        DummyCachedService dummyCachedService() {
            return new DummyCachedService();
        }
    }

    @Service
    static class DummyCachedService {
        @Cacheable("testCache")
        public String getValue(String key) {
            return "real-data-" + key;
        }
    }
}

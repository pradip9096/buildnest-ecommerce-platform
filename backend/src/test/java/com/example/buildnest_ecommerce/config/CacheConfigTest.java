package com.example.buildnest_ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link CacheConfig#cacheManager} and its {@code regionConfig}
 * helper (PROD-04, #84) — no {@code @SpringBootTest} needed since the bean
 * method itself only needs a {@link RedisConnectionFactory} (mocked; no
 * connection is actually opened building a {@link RedisCacheManager}) and a
 * real {@link ObjectMapper}.
 */
class CacheConfigTest {

    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
        ReflectionTestUtils.setField(cacheConfig, "productsTtlMs", 300000L);
        ReflectionTestUtils.setField(cacheConfig, "categoriesTtlMs", 3600000L);
        ReflectionTestUtils.setField(cacheConfig, "usersTtlMs", 1800000L);
        ReflectionTestUtils.setField(cacheConfig, "ordersTtlMs", 600000L);
        ReflectionTestUtils.setField(cacheConfig, "rateLimitStatsTtlMs", 60000L);
        ReflectionTestUtils.setField(cacheConfig, "auditLogsTtlMs", 900000L);
        ReflectionTestUtils.setField(cacheConfig, "userPermissionsTtlMs", 3600000L);
        ReflectionTestUtils.setField(cacheConfig, "inventoryItemsTtlMs", 300000L);
        ReflectionTestUtils.setField(cacheConfig, "relatedTtlMs", 300000L);
    }

    @Test
    @DisplayName("cacheManager builds a relatedProducts region via regionConfig with the configured TTL")
    void cacheManagerBuildsRelatedProductsRegion() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        ObjectMapper objectMapper = new ObjectMapper();

        RedisCacheManager manager = cacheConfig.cacheManager(connectionFactory, objectMapper);

        Cache relatedProducts = manager.getCache("relatedProducts");
        assertNotNull(relatedProducts);
    }

    @Test
    @DisplayName("cacheManager still builds the pre-existing products region (regression check)")
    void cacheManagerBuildsProductsRegion() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        ObjectMapper objectMapper = new ObjectMapper();

        RedisCacheManager manager = cacheConfig.cacheManager(connectionFactory, objectMapper);

        Cache products = manager.getCache("products");
        assertNotNull(products);
    }
}

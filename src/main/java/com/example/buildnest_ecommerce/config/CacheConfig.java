package com.example.buildnest_ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * Cache Configuration for application-level caching.
 * Implements distributed caching using Redis to improve performance (2.3
 * Performance Optimization).
 * 
 * LOW PRIORITY #14: Single Source of Truth for Cache TTLs
 * All cache TTL values are externalized to application.properties for
 * environment-specific configuration without code changes.
 *
 * Cache names defined (with externalized TTL):
 * - "products": Product catalog data (300s default)
 * - "categories": Product categories (3600s default)
 * - "auditLogs": Audit log entries (900s default)
 * - "userPermissions": User role/permission checks (3600s default)
 * - "inventoryItems": Product inventory data (300s default)
 * - "rateLimitStats": Rate limit statistics (60s default)
 * - "orders": Order summary data (600s default)
 * - "users": User profile data (1800s default)
 */
@Configuration
@EnableCaching
@SuppressWarnings("null")
public class CacheConfig {

        // LOW PRIORITY #14: Cache TTL configuration values from properties
        // Single source of truth for all cache TTLs - externalized to
        // application.properties
        // This allows changing TTL values without code changes or recompilation
        @Value("${cache.ttl.products:300000}")
        private long productsTtlMs;
        @Value("${cache.ttl.categories:3600000}")
        private long categoriesTtlMs;
        @Value("${cache.ttl.users:1800000}")
        private long usersTtlMs;
        @Value("${cache.ttl.orders:600000}")
        private long ordersTtlMs;
        @Value("${cache.ttl.rate-limit-stats:60000}")
        private long rateLimitStatsTtlMs;
        @Value("${cache.ttl.audit-logs:900000}")
        private long auditLogsTtlMs;
        @Value("${cache.ttl.user-permissions:3600000}")
        private long userPermissionsTtlMs;
        @Value("${cache.ttl.inventory-items:300000}")
        private long inventoryItemsTtlMs;

        /**
         * Configure Redis Cache Manager with custom TTL for different cache regions.
         * LOW PRIORITY #14: Single source of truth for cache TTLs via externalized
         * configuration.
         * Improves performance by reducing database queries for frequently accessed
         * data.
         * 
         * @param redisConnectionFactory Redis connection factory
         * @return Configured RedisCacheManager with externalized TTL values
         */
        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .disableCachingNullValues();

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(defaultConfig)
                                // Products cache: TTL from application.properties
                                .withCacheConfiguration("products",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(productsTtlMs))
                                                                .disableCachingNullValues())
                                // Categories cache: TTL from application.properties
                                .withCacheConfiguration("categories",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(categoriesTtlMs))
                                                                .disableCachingNullValues())
                                // Audit logs cache: TTL from application.properties
                                .withCacheConfiguration("auditLogs",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(auditLogsTtlMs))
                                                                .disableCachingNullValues())
                                // User permissions cache: TTL from application.properties
                                .withCacheConfiguration("userPermissions",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(userPermissionsTtlMs))
                                                                .disableCachingNullValues())
                                // Inventory items cache: TTL from application.properties
                                .withCacheConfiguration("inventoryItems",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(inventoryItemsTtlMs))
                                                                .disableCachingNullValues())
                                // Rate limit statistics cache: TTL from application.properties
                                .withCacheConfiguration("rateLimitStats",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(rateLimitStatsTtlMs))
                                                                .disableCachingNullValues())
                                // Orders cache: TTL from application.properties
                                .withCacheConfiguration("orders",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(ordersTtlMs))
                                                                .disableCachingNullValues())
                                // Users cache: TTL from application.properties
                                .withCacheConfiguration("users",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMillis(usersTtlMs))
                                                                .disableCachingNullValues())
                                .build();
        }
}

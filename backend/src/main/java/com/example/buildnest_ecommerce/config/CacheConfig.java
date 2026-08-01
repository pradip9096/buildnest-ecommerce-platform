package com.example.buildnest_ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer
                .GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext
                .SerializationPair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

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
 * - "relatedProducts": Related-products recommendation (300s default)
 * - "rateLimitStats": Rate limit statistics (60s default)
 * - "orders": Order summary data (600s default)
 * - "users": User profile data (1800s default)
 */
@Configuration
@EnableCaching
@SuppressWarnings("null")
public class CacheConfig implements CachingConfigurer {

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
        /** TTL for the {@code relatedProducts} cache region (PROD-04, #84). */
        @Value("${cache.ttl.related-products:300000}")
        private long relatedTtlMs;

        /**
         * Falls through instead of throwing on a Redis connection
         * failure (issue #650). Must come via {@link CachingConfigurer}
         * — {@code @EnableCaching} does not auto-detect a plain
         * {@link CacheErrorHandler} bean by type.
         *
         * @return the graceful cache error handler
         */
        @Override
        public CacheErrorHandler errorHandler() {
                return new GracefulCacheErrorHandler();
        }

        /**
         * Configure Redis Cache Manager with custom TTL for different
         * cache regions.
         * LOW PRIORITY #14: Single source of truth for cache TTLs via
         * externalized configuration.
         * Improves performance by reducing database queries for
         * frequently accessed data.
         *
         * @param redisConnectionFactory Redis connection factory
         * @param objectMapper the app's shared Jackson mapper
         * @return Configured RedisCacheManager with externalized TTL
         *         values
         */
        @Bean
        @ConditionalOnProperty(name = "spring.cache.type",
                        havingValue = "redis")
        public RedisCacheManager cacheManager(
                        RedisConnectionFactory redisConnectionFactory,
                        ObjectMapper objectMapper) {
                // Redis serializer needs a dedicated ObjectMapper copy
                // with default typing enabled so that
                // GenericJackson2JsonRedisSerializer embeds @class type
                // information in the JSON. Without it, deserialization
                // returns LinkedHashMap instead of the entity class.
                // We copy the app ObjectMapper to retain all registered
                // modules (Java time, etc.) rather than creating a new
                // instance.
                var validator = BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .build();
                ObjectMapper redisMapper = objectMapper.copy()
                                .activateDefaultTyping(validator,
                                                ObjectMapper.DefaultTyping
                                                                .NON_FINAL);
                var jsonSerializer = SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(
                                                redisMapper));
                RedisCacheConfiguration defaultConfig =
                                RedisCacheConfiguration.defaultCacheConfig()
                                .serializeValuesWith(jsonSerializer)
                                .entryTtl(Duration.ofMinutes(10))
                                .disableCachingNullValues();

                RedisCacheConfiguration products =
                                regionConfig(jsonSerializer, productsTtlMs);
                RedisCacheConfiguration categories =
                                regionConfig(jsonSerializer, categoriesTtlMs);
                RedisCacheConfiguration auditLogs =
                                regionConfig(jsonSerializer, auditLogsTtlMs);
                RedisCacheConfiguration userPermissions = regionConfig(
                                jsonSerializer, userPermissionsTtlMs);
                RedisCacheConfiguration inventoryItems = regionConfig(
                                jsonSerializer, inventoryItemsTtlMs);
                RedisCacheConfiguration relatedProducts =
                                regionConfig(jsonSerializer, relatedTtlMs);
                RedisCacheConfiguration rateLimitStats = regionConfig(
                                jsonSerializer, rateLimitStatsTtlMs);
                RedisCacheConfiguration orders =
                                regionConfig(jsonSerializer, ordersTtlMs);
                RedisCacheConfiguration users =
                                regionConfig(jsonSerializer, usersTtlMs);

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withCacheConfiguration("products", products)
                                .withCacheConfiguration("categories",
                                                categories)
                                .withCacheConfiguration("auditLogs",
                                                auditLogs)
                                .withCacheConfiguration("userPermissions",
                                                userPermissions)
                                .withCacheConfiguration("inventoryItems",
                                                inventoryItems)
                                .withCacheConfiguration("relatedProducts",
                                                relatedProducts)
                                .withCacheConfiguration("rateLimitStats",
                                                rateLimitStats)
                                .withCacheConfiguration("orders", orders)
                                .withCacheConfiguration("users", users)
                                .build();
        }

        /**
         * Builds a {@link RedisCacheConfiguration} for one cache region
         * (PROD-04, #84).
         *
         * @param jsonSerializer the shared value serializer for all
         *                       regions
         * @param ttlMs the region's TTL in milliseconds
         * @return the region's cache configuration
         */
        private RedisCacheConfiguration regionConfig(
                        final SerializationPair<Object> jsonSerializer,
                        final long ttlMs) {
                return RedisCacheConfiguration.defaultCacheConfig()
                                .serializeValuesWith(jsonSerializer)
                                .entryTtl(Duration.ofMillis(ttlMs))
                                .disableCachingNullValues();
        }
}

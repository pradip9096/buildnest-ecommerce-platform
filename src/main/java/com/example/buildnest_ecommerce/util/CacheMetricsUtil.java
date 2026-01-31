package com.example.buildnest_ecommerce.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache Metrics Utility for distributed cache performance monitoring.
 *
 * Tracks cache performance metrics including hit rate, miss rate, and
 * eviction counts. Provides visibility into cache effectiveness for
 * performance optimization and monitoring.
 *
 * Features:
 * - Thread-safe metrics collection using ConcurrentHashMap
 * - Per-cache-region metric tracking
 * - Calculates cache hit rate percentage dynamically
 * - Monitors cache evictions and performance degradation
 *
 * Usage:
 * 
 * <pre>
 * &#64;Autowired
 * private CacheMetricsUtil cacheMetricsUtil;
 *
 * public void getUserFromCache(String userId) {
 *     User user = cache.get(userId);
 *     if (user != null) {
 *         cacheMetricsUtil.recordCacheHit("users");
 *     } else {
 *         cacheMetricsUtil.recordCacheMiss("users");
 *     }
 * }
 * </pre>
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 * @see CacheMetrics
 */
@Slf4j
@Component
public class CacheMetricsUtil {

    private final Map<String, CacheMetrics> metricsMap = new ConcurrentHashMap<>();

    /**
     * Record a cache hit for specified cache region.
     *
     * Increments hit count and calculates hit rate percentage.
     * Thread-safe operation using concurrent map.
     *
     * @param cacheName the name of the cache region - required, must not be null
     * @throws NullPointerException if cacheName is null
     */
    public void recordCacheHit(String cacheName) {
        if (cacheName == null) {
            throw new NullPointerException("Cache name cannot be null");
        }
        metricsMap.computeIfAbsent(cacheName, k -> new CacheMetrics(cacheName))
                .incrementHits();
    }

    /**
     * Record a cache miss for specified cache region.
     *
     * Increments miss count and calculates hit rate percentage.
     * Thread-safe operation using concurrent map.
     *
     * @param cacheName the name of the cache region - required, must not be null
     * @throws NullPointerException if cacheName is null
     */
    public void recordCacheMiss(String cacheName) {
        if (cacheName == null) {
            throw new NullPointerException("Cache name cannot be null");
        }
        metricsMap.computeIfAbsent(cacheName, k -> new CacheMetrics(cacheName))
                .incrementMisses();
    }

    /**
     * Record cache eviction (when entry is removed due to TTL or memory pressure).
     *
     * Tracks eviction events to identify cache memory pressure or TTL issues.
     * High eviction rate may indicate insufficient cache size or aggressive TTL
     * settings.
     *
     * @param cacheName the name of the cache region - required, must not be null
     * @throws NullPointerException if cacheName is null
     */
    public void recordEviction(String cacheName) {
        if (cacheName == null) {
            throw new NullPointerException("Cache name cannot be null");
        }
        metricsMap.computeIfAbsent(cacheName, k -> new CacheMetrics(cacheName))
                .incrementEvictions();
    }

    /**
     * Get metrics for specific cache region.
     *
     * Returns current hit/miss/eviction statistics for the specified cache.
     * Returns null if no metrics have been recorded for the cache region yet.
     *
     * @param cacheName the name of the cache region - required, must not be null
     * @return CacheMetrics with hit/miss/eviction statistics, or null if not found
     * @throws NullPointerException if cacheName is null
     */
    public CacheMetrics getMetrics(String cacheName) {
        if (cacheName == null) {
            throw new NullPointerException("Cache name cannot be null");
        }
        return metricsMap.get(cacheName);
    }

    /**
     * Get all cache metrics across all regions.
     *
     * Returns a snapshot copy of all accumulated metrics across all cache regions.
     * The returned map is independent and thread-safe.
     *
     * @return Map of cache name to metrics (thread-safe copy)
     */
    public Map<String, CacheMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(metricsMap);
    }

    /**
     * Reset metrics for specific cache region.
     *
     * @param cacheName the name of the cache region - required
     */
    public void resetMetrics(String cacheName) {
        metricsMap.remove(cacheName);
        log.info("Cache metrics reset for: {}", cacheName);
    }

    /**
     * Reset all cache metrics.
     */
    public void resetAllMetrics() {
        metricsMap.clear();
        log.info("All cache metrics reset");
    }

    /**
     * Cache Metrics Data Class
     *
     * Holds metrics for individual cache region including hit count, miss count,
     * eviction count, and calculated hit rate percentage.
     */
    @Data
    public static class CacheMetrics {
        private String cacheName;
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong evictions = new AtomicLong(0);

        /**
         * Default constructor for CacheMetrics.
         */
        public CacheMetrics() {
        }

        /**
         * Constructor with cache name.
         *
         * @param cacheName the name of the cache region - required
         */
        public CacheMetrics(String cacheName) {
            this.cacheName = cacheName;
        }

        /**
         * Increment cache hit count.
         */
        public void incrementHits() {
            hits.incrementAndGet();
        }

        /**
         * Increment cache miss count.
         */
        public void incrementMisses() {
            misses.incrementAndGet();
        }

        /**
         * Increment eviction count.
         */
        public void incrementEvictions() {
            evictions.incrementAndGet();
        }

        /**
         * Calculate cache hit rate percentage.
         *
         * @return hit rate as percentage (0-100), or 0 if no requests
         */
        public double getHitRate() {
            long total = hits.get() + misses.get();
            if (total == 0) {
                return 0.0;
            }
            return (double) hits.get() / total * 100;
        }

        /**
         * Get total number of cache accesses (hits + misses).
         *
         * @return total access count
         */
        public long getTotalAccesses() {
            return hits.get() + misses.get();
        }

        @Override
        public String toString() {
            return String.format(
                    "CacheMetrics{name=%s, hits=%d, misses=%d, evictions=%d, hitRate=%.2f%%}",
                    cacheName, hits.get(), misses.get(), evictions.get(), getHitRate());
        }
    }
}

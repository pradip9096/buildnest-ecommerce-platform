package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CacheMetricsUtil - Conditional Branch Coverage Tests")
class CacheMetricsUtilConditionalTest {

    private CacheMetricsUtil cacheMetricsUtil;

    @BeforeEach
    void setUp() {
        cacheMetricsUtil = new CacheMetricsUtil();
    }

    @Test
    @DisplayName("Record cache hit - increment hits counter")
    void testRecordCacheHit() {
        cacheMetricsUtil.recordCacheHit("users");
        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();

        assertTrue(metrics.containsKey("users"));
        assertEquals(1L, metrics.get("users").getHits().get());
    }

    @Test
    @DisplayName("Record cache miss - increment misses counter")
    void testRecordCacheMiss() {
        cacheMetricsUtil.recordCacheMiss("products");
        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();

        assertTrue(metrics.containsKey("products"));
        assertEquals(1L, metrics.get("products").getMisses().get());
    }

    @Test
    @DisplayName("Record cache hit with null cacheName - throws NullPointerException")
    void testRecordCacheHitNullCacheName() {
        assertThrows(NullPointerException.class, () -> cacheMetricsUtil.recordCacheHit(null));
    }

    @Test
    @DisplayName("Record cache miss with null cacheName - throws NullPointerException")
    void testRecordCacheMissNullCacheName() {
        assertThrows(NullPointerException.class, () -> cacheMetricsUtil.recordCacheMiss(null));
    }

    @Test
    @DisplayName("Record cache eviction - increment evictions counter")
    void testRecordCacheEviction() {
        cacheMetricsUtil.recordEviction("orders");
        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();

        assertTrue(metrics.containsKey("orders"));
        assertEquals(1L, metrics.get("orders").getEvictions().get());
    }

    @Test
    @DisplayName("Get all metrics - returns all cached regions")
    void testGetAllMetrics() {
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheMiss("products");
        cacheMetricsUtil.recordEviction("orders");

        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();

        assertEquals(3, metrics.size());
        assertTrue(metrics.containsKey("users"));
        assertTrue(metrics.containsKey("products"));
        assertTrue(metrics.containsKey("orders"));
    }

    @Test
    @DisplayName("Reset metrics for specific cache")
    void testResetMetricsForSpecificCache() {
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheHit("products");

        cacheMetricsUtil.resetMetrics("users");
        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();

        assertFalse(metrics.containsKey("users"));
        assertTrue(metrics.containsKey("products"));
    }

    @Test
    @DisplayName("Reset all metrics - clears all caches")
    void testResetAllMetrics() {
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheMiss("products");
        cacheMetricsUtil.recordEviction("orders");

        cacheMetricsUtil.resetAllMetrics();
        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();

        assertTrue(metrics.isEmpty());
    }

    @Test
    @DisplayName("Get metrics for specific cache - null when not exists")
    void testGetMetricsForSpecificCacheNotExists() {
        CacheMetricsUtil.CacheMetrics metrics = cacheMetricsUtil.getMetrics("nonexistent");
        assertNull(metrics);
    }

    @Test
    @DisplayName("Get metrics for specific cache - returns metrics when exists")
    void testGetMetricsForSpecificCacheExists() {
        cacheMetricsUtil.recordCacheHit("users");
        CacheMetricsUtil.CacheMetrics metrics = cacheMetricsUtil.getMetrics("users");

        assertNotNull(metrics);
        assertEquals("users", metrics.getCacheName());
        assertEquals(1L, metrics.getHits().get());
    }

    @Test
    @DisplayName("Calculate hit rate percentage - 100% hits")
    void testCalculateHitRatePercentageAllHits() {
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheHit("users");

        double hitRate = cacheMetricsUtil.getMetrics("users").getHitRate();
        assertEquals(100.0, hitRate);
    }

    @Test
    @DisplayName("Calculate hit rate percentage - 0% hits (all misses)")
    void testCalculateHitRatePercentageAllMisses() {
        cacheMetricsUtil.recordCacheMiss("users");
        cacheMetricsUtil.recordCacheMiss("users");

        double hitRate = cacheMetricsUtil.getMetrics("users").getHitRate();
        assertEquals(0.0, hitRate);
    }

    @Test
    @DisplayName("Calculate hit rate percentage - 50% hits")
    void testCalculateHitRatePercentage50Percent() {
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheMiss("users");
        cacheMetricsUtil.recordCacheMiss("users");

        double hitRate = cacheMetricsUtil.getMetrics("users").getHitRate();
        assertEquals(50.0, hitRate);
    }

    @Test
    @DisplayName("Calculate hit rate percentage - nonexistent cache")
    void testCalculateHitRatePercentageNonexistentCache() {
        CacheMetricsUtil.CacheMetrics metrics = cacheMetricsUtil.getMetrics("nonexistent");
        assertNull(metrics);
    }

    @Test
    @DisplayName("Multiple cache regions with independent metrics")
    void testMultipleCacheRegionsIndependentMetrics() {
        // Users cache: 3 hits, 2 misses
        for (int i = 0; i < 3; i++) {
            cacheMetricsUtil.recordCacheHit("users");
        }
        for (int i = 0; i < 2; i++) {
            cacheMetricsUtil.recordCacheMiss("users");
        }

        // Products cache: 1 hit, 4 misses
        cacheMetricsUtil.recordCacheHit("products");
        for (int i = 0; i < 4; i++) {
            cacheMetricsUtil.recordCacheMiss("products");
        }

        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();
        assertEquals(3L, metrics.get("users").getHits().get());
        assertEquals(2L, metrics.get("users").getMisses().get());
        assertEquals(1L, metrics.get("products").getHits().get());
        assertEquals(4L, metrics.get("products").getMisses().get());
    }

    @Test
    @DisplayName("CacheMetrics default constructor")
    void testCacheMetricsDefaultConstructor() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics();
        assertNull(metrics.getCacheName());
        assertEquals(0L, metrics.getHits().get());
        assertEquals(0L, metrics.getMisses().get());
        assertEquals(0L, metrics.getEvictions().get());
    }

    @Test
    @DisplayName("CacheMetrics constructor with cacheName")
    void testCacheMetricsConstructorWithCacheName() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("testCache");
        assertEquals("testCache", metrics.getCacheName());
    }

    @Test
    @DisplayName("CacheMetrics canEqual and hashCode behavior")
    void testCacheMetricsCanEqualAndHashCode() {
        CacheMetricsUtil.CacheMetrics base = new CacheMetricsUtil.CacheMetrics("cache");
        CacheMetricsUtil.CacheMetrics same = new CacheMetricsUtil.CacheMetrics("cache");

        assertTrue(base.canEqual(same));
        assertFalse(base.canEqual("other"));
        assertEquals(base.hashCode(), base.hashCode());
        assertNotEquals(base, same);
    }

    @Test
    @DisplayName("CacheMetrics increment operations - hits")
    void testCacheMetricsIncrementHits() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("test");
        metrics.incrementHits();
        metrics.incrementHits();
        assertEquals(2L, metrics.getHits().get());
    }

    @Test
    @DisplayName("CacheMetrics increment operations - misses")
    void testCacheMetricsIncrementMisses() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("test");
        metrics.incrementMisses();
        metrics.incrementMisses();
        metrics.incrementMisses();
        assertEquals(3L, metrics.getMisses().get());
    }

    @Test
    @DisplayName("CacheMetrics increment operations - evictions")
    void testCacheMetricsIncrementEvictions() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("test");
        metrics.incrementEvictions();
        assertEquals(1L, metrics.getEvictions().get());
    }

    @Test
    @DisplayName("CacheMetrics get total accesses")
    void testCacheMetricsGetTotalAccesses() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("test");
        metrics.incrementHits();
        metrics.incrementHits();
        metrics.incrementMisses();

        assertEquals(3L, metrics.getTotalAccesses());
    }

    @Test
    @DisplayName("CacheMetrics calculate hit rate")
    void testCacheMetricsCalculateHitRate() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("test");
        metrics.incrementHits();
        metrics.incrementHits();
        metrics.incrementMisses();
        metrics.incrementMisses();

        double hitRate = metrics.getHitRate();
        assertEquals(50.0, hitRate);
    }

    @Test
    @DisplayName("CacheMetrics hit rate with zero total accesses")
    void testCacheMetricsHitRateZeroAccesses() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("test");
        double hitRate = metrics.getHitRate();
        assertEquals(0.0, hitRate);
    }

    @Test
    @DisplayName("Record eviction with null cacheName - throws NullPointerException")
    void testRecordEvictionNullCacheName() {
        assertThrows(NullPointerException.class, () -> cacheMetricsUtil.recordEviction(null));
    }

    @Test
    @DisplayName("Get metrics with null cacheName - throws NullPointerException")
    void testGetMetricsNullCacheName() {
        assertThrows(NullPointerException.class, () -> cacheMetricsUtil.getMetrics(null));
    }

    @Test
    @DisplayName("Eviction count tracking and reporting")
    void testEvictionCountTracking() {
        cacheMetricsUtil.recordCacheHit("cache1");
        cacheMetricsUtil.recordEviction("cache1");
        cacheMetricsUtil.recordEviction("cache1");
        cacheMetricsUtil.recordEviction("cache1");

        CacheMetricsUtil.CacheMetrics metrics = cacheMetricsUtil.getMetrics("cache1");
        assertEquals(3L, metrics.getEvictions().get());
    }

    @Test
    @DisplayName("Cache name variations")
    void testCacheNameVariations() {
        cacheMetricsUtil.recordCacheHit("users");
        cacheMetricsUtil.recordCacheHit("products");
        cacheMetricsUtil.recordCacheHit("orders");
        cacheMetricsUtil.recordCacheHit("payments");
        cacheMetricsUtil.recordCacheHit("inventory");

        Map<String, CacheMetricsUtil.CacheMetrics> metrics = cacheMetricsUtil.getAllMetrics();
        assertEquals(5, metrics.size());
    }

    @Test
    @DisplayName("CacheMetrics toString representation")
    void testCacheMetricsToString() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("test");
        metrics.incrementHits();
        metrics.incrementMisses();

        String str = metrics.toString();
        assertNotNull(str);
        assertTrue(str.contains("test"));
        assertTrue(str.contains("CacheMetrics"));
    }

    @Test
    @DisplayName("Multiple sequential operations on same cache")
    void testMultipleSequentialOperations() {
        cacheMetricsUtil.recordCacheHit("session");
        cacheMetricsUtil.recordCacheHit("session");
        cacheMetricsUtil.recordCacheMiss("session");
        cacheMetricsUtil.recordEviction("session");
        cacheMetricsUtil.recordEviction("session");

        CacheMetricsUtil.CacheMetrics metrics = cacheMetricsUtil.getMetrics("session");
        assertEquals(2L, metrics.getHits().get());
        assertEquals(1L, metrics.getMisses().get());
        assertEquals(2L, metrics.getEvictions().get());
        assertEquals(3L, metrics.getTotalAccesses());
    }

    @Test
    @DisplayName("Mix of operations across multiple caches")
    void testMixOfOperationsMultipleCaches() {
        for (int i = 0; i < 5; i++) {
            String cacheName = "cache_" + i;
            for (int j = 0; j <= i; j++) {
                cacheMetricsUtil.recordCacheHit(cacheName);
            }
            for (int j = 0; j < i; j++) {
                cacheMetricsUtil.recordCacheMiss(cacheName);
            }
        }

        Map<String, CacheMetricsUtil.CacheMetrics> allMetrics = cacheMetricsUtil.getAllMetrics();
        assertEquals(5, allMetrics.size());

        for (int i = 0; i < 5; i++) {
            String cacheName = "cache_" + i;
            assertEquals((long) (i + 1), allMetrics.get(cacheName).getHits().get());
            assertEquals((long) i, allMetrics.get(cacheName).getMisses().get());
        }
    }

    @Test
    @DisplayName("CacheMetrics equals with shared AtomicLong references")
    void testCacheMetricsEqualsWithSharedReferences() throws Exception {
        CacheMetricsUtil.CacheMetrics base = new CacheMetricsUtil.CacheMetrics("shared");
        CacheMetricsUtil.CacheMetrics other = new CacheMetricsUtil.CacheMetrics("shared");

        Field hits = CacheMetricsUtil.CacheMetrics.class.getDeclaredField("hits");
        Field misses = CacheMetricsUtil.CacheMetrics.class.getDeclaredField("misses");
        Field evictions = CacheMetricsUtil.CacheMetrics.class.getDeclaredField("evictions");

        hits.setAccessible(true);
        misses.setAccessible(true);
        evictions.setAccessible(true);

        hits.set(other, base.getHits());
        misses.set(other, base.getMisses());
        evictions.set(other, base.getEvictions());

        assertEquals(base, other);
        assertEquals(base.hashCode(), other.hashCode());
        assertNotEquals(base, new CacheMetricsUtil.CacheMetrics("shared"));
    }

    @Test
    @DisplayName("CacheMetrics hashCode matches Lombok formula")
    void testCacheMetricsHashCodeMatchesObjectsHash() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("hash");
        metrics.incrementHits();
        metrics.incrementMisses();

        int result = 1;
        result = result * 59 + (metrics.getCacheName() == null ? 43 : metrics.getCacheName().hashCode());
        result = result * 59 + (metrics.getHits() == null ? 43 : metrics.getHits().hashCode());
        result = result * 59 + (metrics.getMisses() == null ? 43 : metrics.getMisses().hashCode());
        result = result * 59 + (metrics.getEvictions() == null ? 43 : metrics.getEvictions().hashCode());

        assertEquals(result, metrics.hashCode());
    }
}

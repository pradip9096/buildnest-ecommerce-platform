package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CacheMetricsUtilTest {

    @Test
    void recordsHitsMissesAndEvictions() {
        CacheMetricsUtil util = new CacheMetricsUtil();

        util.recordCacheHit("users");
        util.recordCacheHit("users");
        util.recordCacheMiss("users");
        util.recordEviction("users");

        CacheMetricsUtil.CacheMetrics metrics = util.getMetrics("users");
        assertNotNull(metrics);
        assertEquals(2, metrics.getHits().get());
        assertEquals(1, metrics.getMisses().get());
        assertEquals(1, metrics.getEvictions().get());
        assertEquals(3, metrics.getTotalAccesses());
        assertTrue(metrics.getHitRate() > 0);
    }

    @Test
    void resetsMetrics() {
        CacheMetricsUtil util = new CacheMetricsUtil();
        util.recordCacheHit("orders");
        assertNotNull(util.getMetrics("orders"));

        util.resetMetrics("orders");
        assertNull(util.getMetrics("orders"));

        util.recordCacheHit("products");
        util.resetAllMetrics();
        Map<String, CacheMetricsUtil.CacheMetrics> all = util.getAllMetrics();
        assertTrue(all.isEmpty());
    }

    @Test
    void nullCacheNameThrows() {
        CacheMetricsUtil util = new CacheMetricsUtil();
        assertThrows(NullPointerException.class, () -> util.recordCacheHit(null));
        assertThrows(NullPointerException.class, () -> util.recordCacheMiss(null));
        assertThrows(NullPointerException.class, () -> util.recordEviction(null));
        assertThrows(NullPointerException.class, () -> util.getMetrics(null));
    }

    @Test
    void cacheMetricsDefaultsAndToString() {
        CacheMetricsUtil.CacheMetrics metrics = new CacheMetricsUtil.CacheMetrics("sessions");
        assertEquals(0.0, metrics.getHitRate());
        assertEquals(0, metrics.getTotalAccesses());
        assertTrue(metrics.toString().contains("sessions"));

        CacheMetricsUtil.CacheMetrics emptyMetrics = new CacheMetricsUtil.CacheMetrics();
        assertEquals(0.0, emptyMetrics.getHitRate());
    }
}

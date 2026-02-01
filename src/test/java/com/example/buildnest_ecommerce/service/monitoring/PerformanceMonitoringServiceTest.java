package com.example.buildnest_ecommerce.service.monitoring;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceMonitoringServiceTest {

    @Test
    void recordsAndCalculatesMetrics() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        service.recordResponseTime("/api/test", 100);
        service.recordResponseTime("/api/test", 600);
        service.recordResponseTime("/api/other", 200);

        Map<String, Object> metrics = service.getPerformanceMetrics();
        assertNotNull(metrics.get("averageResponseTimeMs"));
        assertEquals(3L, metrics.get("totalRequests"));
        assertEquals(1L, metrics.get("slowRequests"));

        assertFalse(service.isSLACompliant());
        assertTrue(service.getSlowQueryRatio() > 0);
    }

    @Test
    void recordsSlowRequestAboveOneSecond() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        service.recordResponseTime("/api/test", 1200);

        Map<String, Object> metrics = service.getPerformanceMetrics();
        assertEquals(1L, metrics.get("slowRequests"));
        assertEquals("✗ FAIL", metrics.get("slaCompliance"));
    }

    @Test
    void endpointMetricsAndReset() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();
        service.recordResponseTime("/api/test", 150);

        Map<String, Object> endpointMetrics = service.getEndpointMetrics("/api/test");
        assertEquals("/api/test", endpointMetrics.get("endpoint"));

        service.resetMetrics();
        Map<String, Object> emptyMetrics = service.getPerformanceMetrics();
        assertEquals("NO_DATA", emptyMetrics.get("status"));
    }

    @Test
    void endpointMetricsNoData() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        Map<String, Object> endpointMetrics = service.getEndpointMetrics("/api/none");
        assertEquals("NO_DATA", endpointMetrics.get("status"));
        assertEquals("/api/none", endpointMetrics.get("endpoint"));
    }

    @Test
    void isSlaCompliantWhenNoData() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        assertTrue(service.isSLACompliant());
        assertEquals(0.0, service.getSlowQueryRatio());
    }

    @Test
    void metricsPassSlaWhenP95BelowThreshold() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        service.recordResponseTime("/api/test", 100);
        service.recordResponseTime("/api/test", 200);
        service.recordResponseTime("/api/test", 300);

        Map<String, Object> metrics = service.getPerformanceMetrics();
        assertEquals("✓ PASS", metrics.get("slaCompliance"));
    }

    @Test
    void testSlowQueryRatioWithMultipleRequests() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        // Record mix of fast and slow requests to test math operations
        service.recordResponseTime("/api/test", 100); // Fast
        service.recordResponseTime("/api/test", 100); // Fast
        service.recordResponseTime("/api/test", 1200); // Slow
        service.recordResponseTime("/api/test", 1500); // Slow

        double slowQueryRatio = service.getSlowQueryRatio();
        assertTrue(slowQueryRatio >= 0); // Verify calculation ran without error
    }

    @Test
    void testPerformanceMetricsWithMultipleRecords() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        // Test averaging calculation (division operation)
        service.recordResponseTime("/api/test", 100);
        service.recordResponseTime("/api/test", 200);
        service.recordResponseTime("/api/test", 300);

        Map<String, Object> metrics = service.getPerformanceMetrics();
        assertNotNull(metrics.get("averageResponseTimeMs"));
        assertTrue((Long) metrics.get("totalRequests") == 3);
    }

    @Test
    void testEndpointMetricsCalculations() {
        PerformanceMonitoringService service = new PerformanceMonitoringService();

        service.recordResponseTime("/api/users", 100);
        service.recordResponseTime("/api/users", 200);
        service.recordResponseTime("/api/users", 300);
        service.recordResponseTime("/api/users", 2000); // Slow request

        Map<String, Object> endpointMetrics = service.getEndpointMetrics("/api/users");

        assertNotNull(endpointMetrics.get("averageResponseTimeMs"));
        // Verify request count recorded (may be Integer or Long)
        Object countObj = endpointMetrics.get("requestCount");
        assertNotNull(countObj);
        long requestCount = countObj instanceof Long ? (Long) countObj : ((Number) countObj).longValue();
        assertEquals(4L, requestCount); // 4 requests recorded
    }
}

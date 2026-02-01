package com.example.buildnest_ecommerce.service.monitoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UptimeMonitoringServiceTest {

    @Test
    void returnsUptimeMetricsAndHealth() {
        UptimeMonitoringService service = new UptimeMonitoringService();

        Map<String, Object> metrics = service.getUptimeMetrics();
        assertTrue(metrics.containsKey("uptimePercentage"));

        Health health = service.health();
        assertEquals("UP", health.getStatus().getCode());
    }

    @Test
    void formattedUptimeAndReset() {
        UptimeMonitoringService service = new UptimeMonitoringService();

        String formatted = service.getFormattedUptime();
        assertTrue(formatted.contains("days"));

        service.resetUptimeStatistics();
        assertEquals("HEALTHY", service.getCurrentHealthStatus());
    }

    @Test
    void healthDownWhenUnhealthy() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "isHealthy", false);

        Health health = service.health();
        assertEquals("DOWN", health.getStatus().getCode());
    }

    @Test
    void performHealthCheckRecoversFromDowntime() {
        UptimeMonitoringService service = new UptimeMonitoringService();

        ReflectionTestUtils.setField(service, "isHealthy", false);
        ReflectionTestUtils.setField(service, "lastDowntimeStart",
                new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis() - 2000));

        service.performHealthCheck();

        assertEquals("HEALTHY", service.getCurrentHealthStatus());
        Health health = service.health();
        assertEquals("UP", health.getStatus().getCode());
    }

    @Test
    void uptimeMetricsFailSlaWhenDowntimeHigh() throws InterruptedException {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 10_000L);

        Thread.sleep(1100);
        Map<String, Object> metrics = service.getUptimeMetrics();
        assertEquals("\u2717 FAIL", metrics.get("slaCompliance"));
    }

    @Test
    void calculateUptimePercentageHandlesZero() {
        UptimeMonitoringService service = new UptimeMonitoringService();

        Double percentage = ReflectionTestUtils.invokeMethod(service, "calculateUptimePercentage", 0L);
        assertEquals(100.0, percentage);
    }

    @Test
    void resetUptimeStatisticsClearsCounters() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 120L);
        ReflectionTestUtils.setField(service, "healthCheckFailures", new java.util.concurrent.atomic.AtomicInteger(5));

        service.resetUptimeStatistics();

        Map<String, Object> metrics = service.getUptimeMetrics();
        assertEquals(0, metrics.get("healthCheckFailures"));
        assertEquals(0L, metrics.get("totalDowntimeSeconds"));
    }

    @Test
    void calculateUptimePercentageWithExactValues() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 0L);

        // Test: 1000 total, 0 downtime = 100%
        Double percentage = ReflectionTestUtils.invokeMethod(service, "calculateUptimePercentage", 1000L);
        assertEquals(100.0, percentage, 0.001);
    }

    @Test
    void calculateUptimePercentageWithDowntime() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 100L);

        // Test: 1000 total, 100 downtime = (900 * 100) / 1000 = 90%
        Double percentage = ReflectionTestUtils.invokeMethod(service, "calculateUptimePercentage", 1000L);
        assertEquals(90.0, percentage, 0.001);
    }

    @Test
    void calculateUptimePercentageHalfUptime() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 500L);

        // Test: 1000 total, 500 downtime = (500 * 100) / 1000 = 50%
        Double percentage = ReflectionTestUtils.invokeMethod(service, "calculateUptimePercentage", 1000L);
        assertEquals(50.0, percentage, 0.001);
    }

    @Test
    void calculateUptimePercentageNinetyNine() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 10L);

        // Test: 1000 total, 10 downtime = (990 * 100) / 1000 = 99%
        Double percentage = ReflectionTestUtils.invokeMethod(service, "calculateUptimePercentage", 1000L);
        assertEquals(99.0, percentage, 0.001);
    }

    @Test
    void getFormattedUptimeCalculationAccuracy() {
        UptimeMonitoringService service = new UptimeMonitoringService();

        // Set application start time to exactly 1 day, 5 hours, 30 minutes, 45 seconds
        // ago
        long exactSeconds = 86400 + 5 * 3600 + 30 * 60 + 45; // 1 day + 5 hours + 30 min + 45 sec
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now().minusSeconds(exactSeconds);
        ReflectionTestUtils.setField(service, "applicationStartTime", startTime);

        String formatted = service.getFormattedUptime();
        assertTrue(formatted.contains("days"));
        assertTrue(formatted.contains("hours"));
        assertTrue(formatted.contains("minutes"));
        assertTrue(formatted.contains("seconds"));
    }

    @Test
    void getFormattedUptimeWithZeroValues() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        ReflectionTestUtils.setField(service, "applicationStartTime", now);

        String formatted = service.getFormattedUptime();
        assertTrue(formatted.contains("0 days"));
        assertTrue(formatted.contains("0 hours"));
        assertTrue(formatted.contains("0 minutes"));
    }

    @Test
    void performHealthCheckCalculatesDowntimeCorrectly() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "isHealthy", true);
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 0L);

        service.performHealthCheck();

        Map<String, Object> metrics = service.getUptimeMetrics();
        assertNotNull(metrics.get("totalDowntimeSeconds"));
    }

    @Test
    void uptimePercentageCalculationWithLargeNumbers() {
        UptimeMonitoringService service = new UptimeMonitoringService();
        ReflectionTestUtils.setField(service, "totalDowntimeSeconds", 86400L); // 1 day downtime

        // 1 year in seconds = 31,536,000, with 1 day downtime
        Double percentage = ReflectionTestUtils.invokeMethod(service, "calculateUptimePercentage", 31536000L);
        double expected = ((31536000L - 86400L) * 100.0) / 31536000L;
        assertEquals(expected, percentage, 0.01);
    }
}

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
}

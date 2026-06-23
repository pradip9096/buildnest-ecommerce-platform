package com.example.buildnest_ecommerce.service.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Application Uptime and Health Monitoring Service
 * 
 * Monitors application availability and health status.
 * SYS-HA-001: 99.5% uptime requirement
 * 
 * Metrics tracked:
 * - Uptime duration
 * - Total incidents
 * - Last incident time
 * - Health status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UptimeMonitoringService implements HealthIndicator {

    private final LocalDateTime applicationStartTime = LocalDateTime.now();
    private final AtomicInteger healthCheckFailures = new AtomicInteger(0);
    private final AtomicLong lastDowntimeStart = new AtomicLong(-1);
    private volatile long totalDowntimeSeconds = 0;
    private volatile boolean isHealthy = true;

    private static final long HEALTH_CHECK_INTERVAL_MS = 60000; // 1 minute
    private static final double TARGET_UPTIME_PERCENTAGE = 99.5;

    /**
     * Performs periodic health check
     * Scheduled every minute to detect downtime
     */
    @Scheduled(fixedRate = HEALTH_CHECK_INTERVAL_MS)
    public void performHealthCheck() {
        try {
            // Check database connectivity
            boolean dbHealth = checkDatabaseHealth();

            // Check cache connectivity
            boolean cacheHealth = checkCacheHealth();

            // Update overall health
            boolean currentHealth = dbHealth && cacheHealth;

            if (!currentHealth && isHealthy) {
                // Downtime detected
                lastDowntimeStart.set(System.currentTimeMillis());
                isHealthy = false;
                healthCheckFailures.incrementAndGet();
                log.warn("⚠️ APPLICATION DOWNTIME DETECTED at {}", LocalDateTime.now());
            } else if (currentHealth && !isHealthy) {
                // Recovery detected
                long downtimeDuration = (System.currentTimeMillis() - lastDowntimeStart.get()) / 1000;
                totalDowntimeSeconds += downtimeDuration;
                isHealthy = true;
                log.info("✓ APPLICATION RECOVERED after {}s downtime", downtimeDuration);
            }

        } catch (Exception e) {
            log.error("Error performing health check: {}", e.getMessage());
            isHealthy = false;
            healthCheckFailures.incrementAndGet();
        }
    }

    /**
     * Checks database connectivity
     */
    private boolean checkDatabaseHealth() {
        try {
            // Placeholder: In production, execute a simple query
            // Example: SELECT 1 from dual
            return true;
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks cache connectivity
     */
    private boolean checkCacheHealth() {
        try {
            // Placeholder: In production, test Redis connection
            return true;
        } catch (Exception e) {
            log.error("Cache health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets uptime metrics
     * 
     * @return Uptime information
     */
    public Map<String, Object> getUptimeMetrics() {
        long totalUptimeSeconds = ChronoUnit.SECONDS.between(applicationStartTime, LocalDateTime.now());
        double uptimePercentage = calculateUptimePercentage(totalUptimeSeconds);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("applicationStartTime", applicationStartTime);
        metrics.put("currentTime", LocalDateTime.now());
        metrics.put("totalUptimeSeconds", totalUptimeSeconds);
        metrics.put("totalDowntimeSeconds", totalDowntimeSeconds);
        metrics.put("uptimePercentage", String.format("%.2f%%", uptimePercentage));
        metrics.put("slaTarget", TARGET_UPTIME_PERCENTAGE + "%");
        metrics.put("slaCompliance", uptimePercentage >= TARGET_UPTIME_PERCENTAGE ? "✓ PASS" : "✗ FAIL");
        metrics.put("healthCheckFailures", healthCheckFailures.get());
        metrics.put("currentHealthStatus", isHealthy ? "HEALTHY" : "UNHEALTHY");

        return metrics;
    }

    /**
     * Calculates uptime percentage
     * 
     * @param totalUptimeSeconds Total uptime in seconds
     * @return Uptime percentage
     */
    private double calculateUptimePercentage(long totalUptimeSeconds) {
        if (totalUptimeSeconds == 0)
            return 100;
        return ((totalUptimeSeconds - totalDowntimeSeconds) * 100.0) / totalUptimeSeconds;
    }

    /**
     * Spring Boot Health indicator implementation
     * Provides health status to /actuator/health endpoint
     */
    @Override
    public Health health() {
        Map<String, Object> metrics = getUptimeMetrics();

        if (isHealthy) {
            return Health.up()
                    .withDetails(metrics)
                    .build();
        } else {
            return Health.down()
                    .withDetails(metrics)
                    .build();
        }
    }

    /**
     * Gets uptime in human-readable format
     * 
     * @return Formatted uptime string
     */
    public String getFormattedUptime() {
        long totalSeconds = ChronoUnit.SECONDS.between(applicationStartTime, LocalDateTime.now());

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
    }

    /**
     * Gets current health status
     */
    public String getCurrentHealthStatus() {
        return isHealthy ? "HEALTHY" : "UNHEALTHY";
    }

    /**
     * Resets uptime statistics
     */
    public void resetUptimeStatistics() {
        healthCheckFailures.set(0);
        totalDowntimeSeconds = 0;
        log.info("Uptime statistics reset");
    }
}

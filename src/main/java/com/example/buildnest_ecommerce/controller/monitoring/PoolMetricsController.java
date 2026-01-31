package com.example.buildnest_ecommerce.controller.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Pool Metrics Controller
 *
 * PERFORMANCE_OPTIMIZATION_GUIDE - Connection Pool Tuning (Section 5)
 *
 * Provides real-time monitoring of HikariCP database connection pool metrics.
 * Monitors active connections, idle connections, waiting threads, and alerts
 * on pool exhaustion conditions.
 *
 * Endpoints:
 * - GET /actuator/custom/pool-status : Real-time pool metrics
 * - GET /actuator/custom/pool-health : Pool health assessment
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/actuator/custom")
@RequiredArgsConstructor
@ConditionalOnBean(HikariDataSource.class)
public class PoolMetricsController {

    private final HikariDataSource dataSource;

    /**
     * Get current connection pool status metrics.
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Section 5 Implementation:
     * Tracks these metrics:
     * - activeConnections: Currently in-use connections
     * - totalConnections: Total allocated connections
     * - idleConnections: Available idle connections
     * - waitingQueue: Threads waiting for connection
     *
     * Alert Thresholds (PERFORMANCE_OPTIMIZATION_GUIDE):
     * - Active Connections > 90% of max: Scale up required
     * - Idle Connections < 20% of total: Increase minimum-idle
     * - Threads Awaiting Connection > 0: Increase pool size immediately
     *
     * @return Map containing pool status metrics
     */
    @GetMapping("/pool-status")
    public Map<String, Object> poolStatus() {
        log.debug("Fetching HikariCP pool metrics");

        Map<String, Object> status = new HashMap<>();

        // Get pool metrics from MXBean
        int activeConnections = dataSource.getHikariPoolMXBean().getActiveConnections();
        int totalConnections = dataSource.getHikariPoolMXBean().getTotalConnections();
        int idleConnections = dataSource.getHikariPoolMXBean().getIdleConnections();
        int waitingThreads = dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();

        // Add basic metrics
        status.put("activeConnections", activeConnections);
        status.put("totalConnections", totalConnections);
        status.put("idleConnections", idleConnections);
        status.put("waitingQueue", waitingThreads);

        // Calculate utilization percentage
        double utilization = totalConnections > 0 ? ((double) activeConnections / totalConnections) * 100 : 0;
        status.put("utilizationPercentage", String.format("%.2f%%", utilization));

        // Add pool configuration for context
        status.put("maxPoolSize", dataSource.getMaximumPoolSize());
        status.put("minIdle", dataSource.getMinimumIdle());

        // Add health indicators
        Map<String, Object> health = new HashMap<>();
        health.put("activeConnections_90Percent_Alert", activeConnections > (dataSource.getMaximumPoolSize() * 0.9));
        health.put("idleConnections_20Percent_Alert", idleConnections < (totalConnections * 0.2));
        health.put("waitingThreads_Alert", waitingThreads > 0);
        status.put("alerts", health);

        // Log if alerts triggered
        if ((boolean) health.get("activeConnections_90Percent_Alert")) {
            log.warn("ALERT: Connection pool utilization at 90%+. Current: {}/{}", activeConnections, totalConnections);
        }
        if ((boolean) health.get("waitingThreads_Alert")) {
            log.error("ALERT: Threads waiting for connections: {}. Pool exhausted!", waitingThreads);
        }

        return status;
    }

    /**
     * Get connection pool health assessment.
     *
     * Returns comprehensive health status with recommendations for tuning
     * based on current pool metrics and thresholds.
     *
     * @return Map containing health status and recommendations
     */
    @GetMapping("/pool-health")
    public Map<String, Object> poolHealth() {
        log.debug("Assessing HikariCP pool health");

        Map<String, Object> health = new HashMap<>();

        int activeConnections = dataSource.getHikariPoolMXBean().getActiveConnections();
        int totalConnections = dataSource.getHikariPoolMXBean().getTotalConnections();
        int idleConnections = dataSource.getHikariPoolMXBean().getIdleConnections();
        int waitingThreads = dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();

        double utilizationPercentage = totalConnections > 0 ? ((double) activeConnections / totalConnections) * 100 : 0;

        // Determine health status
        String status;
        if (waitingThreads > 0 || utilizationPercentage > 95) {
            status = "CRITICAL";
        } else if (utilizationPercentage > 90) {
            status = "WARNING";
        } else if (utilizationPercentage > 70) {
            status = "CAUTION";
        } else {
            status = "HEALTHY";
        }

        health.put("status", status);
        health.put("utilizationPercentage", String.format("%.2f%%", utilizationPercentage));

        // Add recommendations
        Map<String, String> recommendations = new HashMap<>();
        if (waitingThreads > 0) {
            recommendations.put("immediate_action", "Scale up pool size: " + waitingThreads + " threads waiting");
        }
        if (utilizationPercentage > 90) {
            recommendations.put("pool_size", "Increase maximum-pool-size from " + dataSource.getMaximumPoolSize());
        }
        if (idleConnections < (totalConnections * 0.2)) {
            recommendations.put("idle_connections", "Increase minimum-idle to reduce connection creation overhead");
        }

        health.put("recommendations", recommendations);
        health.put("metrics", Map.of(
                "activeConnections", activeConnections,
                "totalConnections", totalConnections,
                "idleConnections", idleConnections,
                "waitingThreads", waitingThreads,
                "maxPoolSize", dataSource.getMaximumPoolSize(),
                "minIdle", dataSource.getMinimumIdle()));

        return health;
    }
}

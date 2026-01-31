package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.service.monitoring.PerformanceMonitoringService;
import com.example.buildnest_ecommerce.service.monitoring.UptimeMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitoring Controller
 * 
 * Provides endpoints for monitoring system performance and health.
 * SYS-PERF-001: API response time monitoring
 * SYS-HA-001: Uptime tracking and 99.5% SLA monitoring
 * 
 * All endpoints require ADMIN role for security.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringController {

    private final PerformanceMonitoringService performanceMonitoringService;
    private final UptimeMonitoringService uptimeMonitoringService;

    /**
     * Get current performance metrics
     * 
     * Returns:
     * - Average response time
     * - 95th percentile response time
     * - 99th percentile response time
     * - SLA compliance status
     * - Slow request count and percentage
     * 
     * @return Performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<?> getPerformanceMetrics() {
        try {
            Map<String, Object> metrics = performanceMonitoringService.getPerformanceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error retrieving performance metrics: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to retrieve performance metrics",
                    "message", e.getMessage()));
        }
    }

    /**
     * Check if current SLA is being met
     * 
     * SLA Requirement: Response time < 500ms at 95th percentile
     * 
     * @return SLA compliance status
     */
    @GetMapping("/performance/sla-status")
    public ResponseEntity<?> checkSLACompliance() {
        try {
            boolean compliant = performanceMonitoringService.isSLACompliant();
            double slowRatio = performanceMonitoringService.getSlowQueryRatio();

            return ResponseEntity.ok(Map.of(
                    "slaCompliant", compliant,
                    "status", compliant ? "✓ PASS" : "✗ FAIL",
                    "slowQueryRatio", String.format("%.2f%%", slowRatio),
                    "requirement", "Response time < 500ms at 95th percentile",
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error checking SLA compliance: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to check SLA compliance"));
        }
    }

    /**
     * Reset performance metrics
     * 
     * Clears all collected performance metrics.
     * Use after maintenance or to start fresh measurement period.
     * 
     * @return Confirmation message
     */
    @PostMapping("/performance/reset")
    public ResponseEntity<?> resetPerformanceMetrics() {
        try {
            performanceMonitoringService.resetMetrics();
            log.info("Performance metrics reset");
            return ResponseEntity.ok(Map.of(
                    "message", "Performance metrics reset successfully",
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error resetting performance metrics: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to reset performance metrics"));
        }
    }

    /**
     * Get current uptime metrics
     * 
     * Returns:
     * - Application start time
     * - Total uptime duration
     * - Total downtime
     * - Uptime percentage
     * - 99.5% SLA compliance status
     * 
     * @return Uptime metrics
     */
    @GetMapping("/uptime")
    public ResponseEntity<?> getUptimeMetrics() {
        try {
            Map<String, Object> metrics = uptimeMonitoringService.getUptimeMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error retrieving uptime metrics: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to retrieve uptime metrics"));
        }
    }

    /**
     * Get formatted uptime string
     * 
     * @return Human-readable uptime format
     */
    @GetMapping("/uptime/formatted")
    public ResponseEntity<?> getFormattedUptime() {
        try {
            String formattedUptime = uptimeMonitoringService.getFormattedUptime();
            String healthStatus = uptimeMonitoringService.getCurrentHealthStatus();

            return ResponseEntity.ok(Map.of(
                    "uptime", formattedUptime,
                    "healthStatus", healthStatus,
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error retrieving formatted uptime: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to retrieve uptime information"));
        }
    }

    /**
     * Check application health status
     * 
     * @return Health status (HEALTHY/UNHEALTHY)
     */
    @GetMapping("/health-status")
    public ResponseEntity<?> getHealthStatus() {
        try {
            String status = uptimeMonitoringService.getCurrentHealthStatus();
            return ResponseEntity.ok(Map.of(
                    "status", status,
                    "isHealthy", status.equals("HEALTHY"),
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error checking health status: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to check health status"));
        }
    }

    /**
     * Check SLA compliance (both uptime and performance)
     * 
     * Combined SLA check:
     * - Uptime: 99.5%
     * - Response time: < 500ms (p95)
     * 
     * @return Overall SLA status
     */
    @GetMapping("/sla-status")
    public ResponseEntity<?> checkOverallSLACompliance() {
        try {
            Map<String, Object> performanceMetrics = performanceMonitoringService.getPerformanceMetrics();
            Map<String, Object> uptimeMetrics = uptimeMonitoringService.getUptimeMetrics();

            boolean performanceCompliant = performanceMonitoringService.isSLACompliant();
            Object uptimeComplianceObj = uptimeMetrics.get("slaCompliance");
            boolean uptimeCompliant = uptimeComplianceObj != null && uptimeComplianceObj.toString().contains("PASS");

            boolean overallCompliant = performanceCompliant && uptimeCompliant;

            Map<String, Object> result = new HashMap<>();
            result.put("overallSLACompliant", overallCompliant);
            result.put("status", overallCompliant ? "✓ PASS" : "✗ FAIL");
            result.put("performance", Map.of(
                    "compliant", performanceCompliant,
                    "p95ResponseTimeMs", performanceMetrics.get("p95ResponseTimeMs"),
                    "requirement", "< 500ms"));
            result.put("uptime", Map.of(
                    "compliant", uptimeCompliant,
                    "uptimePercentage", uptimeMetrics.get("uptimePercentage"),
                    "requirement", "99.5%"));
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking overall SLA compliance: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to check SLA compliance"));
        }
    }

    /**
     * Reset uptime statistics
     * 
     * @return Confirmation message
     */
    @PostMapping("/uptime/reset")
    public ResponseEntity<?> resetUptimeStatistics() {
        try {
            uptimeMonitoringService.resetUptimeStatistics();
            log.info("Uptime statistics reset");
            return ResponseEntity.ok(Map.of(
                    "message", "Uptime statistics reset successfully",
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error resetting uptime statistics: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to reset uptime statistics"));
        }
    }
}

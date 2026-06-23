package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchAlertingService;
import com.example.buildnest_ecommerce.service.admin.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Admin Analytics Controller - provides centralized analytics and reporting
 * endpoints (RQ-ES-VIS-01, RQ-ES-VIS-02, RQ-ES-VIS-03).
 * Restricted to ADMIN users only. Supports querying audit logs and metrics from
 * Elasticsearch.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class AdminAnalyticsController {

    private final ElasticsearchIngestionService ingestionService;
    private final ElasticsearchAlertingService alertingService;
    private final AdminAnalyticsService analyticsService;

    /**
     * Get audit logs for a specific user (RQ-ES-EL-02, RQ-ES-VIS-01).
     */
    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<?> getAuditLogsByUser(@PathVariable Long userId) {
        try {
            List<ElasticsearchAuditLog> logs = ingestionService.getAuditLogsByUser(userId);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("userId", userId);
            response.put("totalRecords", logs.size());
            response.put("logs", logs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving audit logs for user {}", userId, e);
            return ResponseEntity.internalServerError().body("Error retrieving audit logs");
        }
    }

    /**
     * Get audit logs by action type (RQ-ES-EL-02, RQ-ES-VIS-01).
     */
    @GetMapping("/audit-logs/action/{action}")
    public ResponseEntity<?> getAuditLogsByAction(@PathVariable String action) {
        try {
            List<ElasticsearchAuditLog> logs = ingestionService.getAuditLogsByAction(action);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("action", action);
            response.put("totalRecords", logs.size());
            response.put("logs", logs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving audit logs for action {}", action, e);
            return ResponseEntity.internalServerError().body("Error retrieving audit logs");
        }
    }

    /**
     * Get audit logs by time range (RQ-ES-EL-04, RQ-ES-VIS-02).
     */
    @GetMapping("/audit-logs/range")
    public ResponseEntity<?> getAuditLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<ElasticsearchAuditLog> logs = ingestionService.getAuditLogsByTimeRange(startTime, endTime);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("period", Map.of("start", startTime, "end", endTime));
            response.put("totalRecords", logs.size());
            response.put("logs", logs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving audit logs for time range", e);
            return ResponseEntity.internalServerError().body("Error retrieving audit logs");
        }
    }

    /**
     * Get metrics by time range for historical trend analysis (RQ-ES-MON-03,
     * RQ-ES-VIS-02).
     */
    @GetMapping("/metrics/range")
    public ResponseEntity<?> getMetricsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<ElasticsearchMetrics> metrics = ingestionService.getMetricsByTimeRange(startTime, endTime);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("period", Map.of("start", startTime, "end", endTime));
            response.put("totalRecords", metrics.size());
            response.put("metrics", metrics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving metrics for time range", e);
            return ResponseEntity.internalServerError().body("Error retrieving metrics");
        }
    }

    /**
     * Get recent metrics for real-time monitoring (RQ-ES-MON-02, RQ-ES-VIS-03).
     */
    @GetMapping("/metrics/recent")
    public ResponseEntity<?> getRecentMetrics(
            @RequestParam(defaultValue = "5") int minutesBack) {
        try {
            List<ElasticsearchMetrics> metrics = ingestionService.getRecentMetrics(minutesBack);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("period", String.format("Last %d minutes", minutesBack));
            response.put("totalRecords", metrics.size());
            response.put("metrics", metrics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving recent metrics", e);
            return ResponseEntity.internalServerError().body("Error retrieving metrics");
        }
    }

    /**
     * Get alert summary for the last hour (RQ-ES-ALRT-03, RQ-ES-VIS-03).
     */
    @GetMapping("/alerts/summary")
    public ResponseEntity<?> getAlertSummary() {
        try {
            Map<String, Object> summary = alertingService.getAlertSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving alert summary", e);
            return ResponseEntity.internalServerError().body("Error retrieving alert summary");
        }
    }

    /**
     * Get comprehensive dashboard metrics (RQ-ES-VIS-04).
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            Map<String, Object> dashboard = new LinkedHashMap<>();
            dashboard.put("timestamp", LocalDateTime.now());

            // Get recent metrics
            List<ElasticsearchMetrics> recentMetrics = ingestionService.getRecentMetrics(10);
            dashboard.put("recentMetrics", recentMetrics);

            // Get recent audit logs
            LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
            List<ElasticsearchAuditLog> recentAudits = ingestionService.getAuditLogsByTimeRange(lastHour,
                    LocalDateTime.now());
            dashboard.put("recentAuditLogs", recentAudits);

            // Get alert summary
            Map<String, Object> alerts = alertingService.getAlertSummary();
            dashboard.put("alertSummary", alerts);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error retrieving dashboard data", e);
            return ResponseEntity.internalServerError().body("Error retrieving dashboard");
        }
    }

    @GetMapping("/api-errors/by-status")
    public ResponseEntity<?> getApiErrorsByStatusCode(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            var errors = analyticsService.getApiErrorsByStatusCode(startDate, endDate);
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            log.error("Error retrieving API errors by status code", e);
            return ResponseEntity.internalServerError().body("Error retrieving API errors");
        }
    }

    @GetMapping("/api-errors/by-endpoint")
    public ResponseEntity<?> getApiErrorsByEndpoint(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            var errors = analyticsService.getApiErrorsByEndpoint(startDate, endDate);
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            log.error("Error retrieving API errors by endpoint", e);
            return ResponseEntity.internalServerError().body("Error retrieving API errors");
        }
    }
}

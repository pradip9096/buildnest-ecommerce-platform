package com.example.buildnest_ecommerce.service.admin;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics service for admin dashboard metrics and API error analysis
 * (RQ-SRCH-04, RQ-MON-01).
 * Provides comprehensive analytics on API errors, performance, and system
 * health.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAnalyticsService implements IAdminAnalyticsService {

    private final ElasticsearchAuditLogRepository auditLogRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Retrieve API errors filtered by HTTP status code (RQ-SRCH-04).
     * Returns aggregated error statistics by status code.
     */
    public Map<String, Object> getApiErrorsByStatusCode(String startDateStr, String endDateStr) {
        try {
            LocalDateTime startDate = parseDate(startDateStr, LocalDateTime.now().minusDays(7));
            LocalDateTime endDate = parseDate(endDateStr, LocalDateTime.now());

            // Get all audit logs within date range
            List<ElasticsearchAuditLog> allLogs = auditLogRepository
                    .findByTimestampBetween(startDate, endDate);

            // Group by HTTP status code
            Map<Integer, List<ElasticsearchAuditLog>> grouped = allLogs.stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 400)
                    .collect(Collectors.groupingBy(ElasticsearchAuditLog::getHttpStatusCode));

            // Build response with statistics
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("startDate", startDate);
            response.put("endDate", endDate);

            Map<String, Object> statusCodeStats = new LinkedHashMap<>();
            grouped.forEach((statusCode, logs) -> {
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("count", logs.size());
                stats.put("percentage", String.format("%.2f%%", (logs.size() * 100.0 / allLogs.size())));
                stats.put("category", getErrorCategory(statusCode));
                stats.put("errors", logs.stream()
                        .limit(10)
                        .map(log -> Map.of(
                                "id", log.getId(),
                                "action", log.getAction(),
                                "timestamp", log.getTimestamp(),
                                "endpoint", log.getEndpoint() != null ? log.getEndpoint() : "N/A"))
                        .collect(Collectors.toList()));
                statusCodeStats.put(statusCode.toString(), stats);
            });

            response.put("statusCodeStatistics", statusCodeStats);
            response.put("totalErrors", allLogs.stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 400)
                    .count());

            return response;
        } catch (Exception e) {
            log.error("Error retrieving API errors by status code", e);
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    /**
     * Retrieve API errors aggregated by endpoint (RQ-SRCH-04, RQ-MON-02).
     * Shows which endpoints are generating the most errors.
     */
    public Map<String, Object> getApiErrorsByEndpoint(String startDateStr, String endDateStr) {
        try {
            LocalDateTime startDate = parseDate(startDateStr, LocalDateTime.now().minusDays(7));
            LocalDateTime endDate = parseDate(endDateStr, LocalDateTime.now());

            // Get all error logs within date range
            List<ElasticsearchAuditLog> errorLogs = auditLogRepository
                    .findByTimestampBetween(startDate, endDate)
                    .stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 400)
                    .collect(Collectors.toList());

            // Group by endpoint
            Map<String, List<ElasticsearchAuditLog>> grouped = errorLogs.stream()
                    .collect(Collectors.groupingBy(log -> log.getEndpoint() != null ? log.getEndpoint() : "UNKNOWN"));

            // Sort by error count
            List<Map<String, Object>> endpointStats = grouped.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                    .map(entry -> {
                        List<ElasticsearchAuditLog> logs = entry.getValue();
                        Map<String, Object> stat = new LinkedHashMap<>();
                        stat.put("endpoint", entry.getKey());
                        stat.put("totalErrors", logs.size());

                        // Status code breakdown
                        Map<Integer, Long> statusBreakdown = logs.stream()
                                .collect(Collectors.groupingBy(
                                        ElasticsearchAuditLog::getHttpStatusCode,
                                        Collectors.counting()));
                        stat.put("statusCodeBreakdown", statusBreakdown);

                        // Error trend (sample)
                        stat.put("recentErrors", logs.stream()
                                .sorted(Comparator.comparing(ElasticsearchAuditLog::getTimestamp).reversed())
                                .limit(5)
                                .map(log -> Map.of(
                                        "timestamp", log.getTimestamp(),
                                        "statusCode", log.getHttpStatusCode(),
                                        "action", log.getAction()))
                                .collect(Collectors.toList()));

                        return stat;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalErrors", errorLogs.size());
            response.put("affectedEndpoints", grouped.size());
            response.put("endpointStatistics", endpointStats);

            return response;
        } catch (Exception e) {
            log.error("Error retrieving API errors by endpoint", e);
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    /**
     * Get comprehensive dashboard data (RQ-MON-01).
     */
    public Map<String, Object> getDashboardData() {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();

            List<ElasticsearchAuditLog> logs = auditLogRepository
                    .findByTimestampBetween(startDate, endDate);

            Map<String, Object> dashboard = new LinkedHashMap<>();

            // Overall metrics
            long totalEvents = logs.size();
            long totalErrors = logs.stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 400)
                    .count();
            long criticalErrors = logs.stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 500)
                    .count();

            dashboard.put("totalEvents", totalEvents);
            dashboard.put("totalErrors", totalErrors);
            dashboard.put("criticalErrors", criticalErrors);
            dashboard.put("errorRate",
                    totalEvents > 0 ? String.format("%.2f%%", (totalErrors * 100.0 / totalEvents)) : "0.00%");

            // Status code distribution
            Map<Integer, Long> statusDistribution = logs.stream()
                    .filter(log -> log.getHttpStatusCode() != null)
                    .collect(Collectors.groupingBy(
                            ElasticsearchAuditLog::getHttpStatusCode,
                            Collectors.counting()));
            dashboard.put("statusDistribution", statusDistribution);

            // Top endpoints by error count
            List<Map<String, Object>> topErrorEndpoints = logs.stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 400)
                    .collect(Collectors.groupingBy(ElasticsearchAuditLog::getEndpoint, Collectors.counting()))
                    .entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .limit(10)
                    .map(e -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("endpoint", e.getKey());
                        map.put("errorCount", e.getValue());
                        return map;
                    })
                    .collect(Collectors.toList());
            dashboard.put("topErrorEndpoints", topErrorEndpoints);

            // Error category distribution
            Map<String, Long> categoryDistribution = logs.stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 400)
                    .collect(Collectors.groupingBy(
                            log -> getErrorCategory(log.getHttpStatusCode()),
                            Collectors.counting()));
            dashboard.put("errorCategoryDistribution", categoryDistribution);

            dashboard.put("timeRange", Map.of("start", startDate, "end", endDate));
            return dashboard;
        } catch (Exception e) {
            log.error("Error retrieving dashboard data", e);
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    /**
     * Categorize HTTP status codes into error types.
     */
    private String getErrorCategory(Integer statusCode) {
        if (statusCode == null)
            return "UNKNOWN";
        if (statusCode >= 500)
            return "SERVER_ERROR";
        if (statusCode >= 400)
            return "CLIENT_ERROR";
        if (statusCode >= 300)
            return "REDIRECT";
        if (statusCode >= 200)
            return "SUCCESS";
        return "UNKNOWN";
    }

    /**
     * Parse date string with fallback to default.
     */
    private LocalDateTime parseDate(String dateStr, LocalDateTime defaultDate) {
        if (dateStr == null || dateStr.isEmpty()) {
            return defaultDate;
        }
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}, using default", dateStr);
            return defaultDate;
        }
    }

    @Override
    public Map<String, Object> getApiErrorRate(String startDateStr, String endDateStr) {
        try {
            LocalDateTime startDate = parseDate(startDateStr, LocalDateTime.now().minusDays(7));
            LocalDateTime endDate = parseDate(endDateStr, LocalDateTime.now());

            List<ElasticsearchAuditLog> logs = auditLogRepository
                    .findByTimestampBetween(startDate, endDate);

            long totalRequests = logs.size();
            long errorRequests = logs.stream()
                    .filter(log -> log.getHttpStatusCode() != null && log.getHttpStatusCode() >= 400)
                    .count();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalRequests", totalRequests);
            response.put("errorRequests", errorRequests);
            response.put("errorRate",
                    totalRequests > 0 ? String.format("%.2f%%", (errorRequests * 100.0 / totalRequests)) : "0.00%");
            response.put("successRate",
                    totalRequests > 0
                            ? String.format("%.2f%%", ((totalRequests - errorRequests) * 100.0 / totalRequests))
                            : "0.00%");

            return response;
        } catch (Exception e) {
            log.error("Error retrieving API error rate", e);
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getErrorByCorrelationId(String correlationId) {
        try {
            // Fallback: Search within recent logs since findByCorrelationId is not
            // available
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();

            List<ElasticsearchAuditLog> allLogs = auditLogRepository
                    .findByTimestampBetween(startDate, endDate);

            List<ElasticsearchAuditLog> logs = allLogs.stream()
                    .filter(log -> correlationId.equals(log.getId()) ||
                            (log.getAction() != null && log.getAction().contains(correlationId)))
                    .collect(Collectors.toList());

            if (logs.isEmpty()) {
                return Collections.singletonMap("error", "No logs found for correlation ID: " + correlationId);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("correlationId", correlationId);
            response.put("logCount", logs.size());
            response.put("logs", logs.stream()
                    .map(log -> {
                        Map<String, Object> logData = new LinkedHashMap<>();
                        logData.put("id", log.getId());
                        logData.put("timestamp", log.getTimestamp());
                        logData.put("action", log.getAction());
                        logData.put("endpoint", log.getEndpoint());
                        logData.put("httpStatusCode", log.getHttpStatusCode());
                        logData.put("userId", log.getUserId());
                        return logData;
                    })
                    .collect(Collectors.toList()));

            return response;
        } catch (Exception e) {
            log.error("Error retrieving error by correlation ID: {}", correlationId, e);
            return Collections.singletonMap("error", e.getMessage());
        }
    }
}

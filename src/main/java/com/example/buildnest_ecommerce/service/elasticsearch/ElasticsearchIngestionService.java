package com.example.buildnest_ecommerce.service.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for Elasticsearch event ingestion and storage (RQ-ES-ING-01,
 * RQ-ES-ING-02, RQ-ES-ING-04).
 * Handles reliable ingestion and storage of audit logs and metrics in
 * Elasticsearch.
 * Only enabled when elasticsearch.enabled=true.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ElasticsearchIngestionService {

    private final ElasticsearchAuditLogRepository auditLogRepository;
    private final ElasticsearchMetricsRepository metricsRepository;

    /**
     * Index audit log in Elasticsearch asynchronously (RQ-ES-LOG-04, RQ-ES-ING-04).
     * Ensures non-blocking ingestion for reliable delivery.
     */
    @Async
    public void indexAuditLog(Long userId, String action, String entityType, Long entityId,
            String ipAddress, String userAgent, String oldValue, String newValue) {
        indexAuditLogWithStatus(userId, action, entityType, entityId, ipAddress, userAgent, oldValue, newValue, 200,
                null, null);
    }

    /**
     * Index audit log with HTTP status code (RQ-SRCH-04, RQ-ES-LOG-04).
     * Extended method for capturing API error events with status codes.
     */
    @Async
    public void indexAuditLogWithStatus(Long userId, String action, String entityType, Long entityId,
            String ipAddress, String userAgent, String oldValue, String newValue,
            Integer httpStatusCode, String endpoint, String errorCategory) {
        try {
            String resolvedErrorCategory = errorCategory;
            if (httpStatusCode != null && resolvedErrorCategory == null) {
                if (httpStatusCode >= 200 && httpStatusCode < 300) {
                    resolvedErrorCategory = "SUCCESS";
                } else if (httpStatusCode >= 300 && httpStatusCode < 400) {
                    resolvedErrorCategory = "REDIRECT";
                } else if (httpStatusCode >= 400 && httpStatusCode < 500) {
                    resolvedErrorCategory = "CLIENT_ERROR";
                } else if (httpStatusCode >= 500) {
                    resolvedErrorCategory = "SERVER_ERROR";
                }
            }

            ElasticsearchAuditLog esLog = ElasticsearchAuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .severity(determineSeverity(action))
                    .httpStatusCode(httpStatusCode)
                    .errorCategory(resolvedErrorCategory)
                    .endpoint(endpoint)
                    .build();

            auditLogRepository.save(esLog);
            log.debug("Audit log indexed in Elasticsearch: {} - {} (Status: {})", action, userId, httpStatusCode);
        } catch (Exception e) {
            log.error("Failed to index audit log in Elasticsearch", e);
            // Graceful degradation - application continues even if Elasticsearch fails
        }
    }

    /**
     * Query API errors by HTTP status code (RQ-SRCH-04).
     * Retrieves all API error events with specific HTTP status codes.
     */
    public List<ElasticsearchAuditLog> getErrorsByHttpStatusCode(Integer httpStatusCode) {
        return auditLogRepository.findByHttpStatusCode(httpStatusCode);
    }

    /**
     * Query API errors by category (RQ-SRCH-04).
     * Retrieves errors categorized by CLIENT_ERROR, SERVER_ERROR, etc.
     */
    public List<ElasticsearchAuditLog> getErrorsByCategory(String errorCategory) {
        return auditLogRepository.findByErrorCategory(errorCategory);
    }

    /**
     * Query API errors by status code and time range (RQ-SRCH-04).
     */
    public List<ElasticsearchAuditLog> getErrorsByStatusCodeAndTimeRange(Integer httpStatusCode, LocalDateTime start,
            LocalDateTime end) {
        return auditLogRepository.findByHttpStatusCodeAndTimestampBetween(httpStatusCode, start, end);
    }

    /**
     * Index metrics in Elasticsearch asynchronously (RQ-ES-MON-01, RQ-ES-ING-04).
     * Stores performance metrics for historical trend analysis.
     */
    @Async
    public void indexMetrics(String metricName, Double value, String unit, String service,
            String host, String environment) {
        try {
            ElasticsearchMetrics metrics = ElasticsearchMetrics.builder()
                    .id(UUID.randomUUID().toString())
                    .metricName(metricName)
                    .value(value)
                    .unit(unit)
                    .service(service)
                    .timestamp(LocalDateTime.now())
                    .host(host)
                    .environment(environment)
                    .build();

            metricsRepository.save(metrics);
            log.debug("Metric indexed in Elasticsearch: {} = {}", metricName, value);
        } catch (Exception e) {
            log.error("Failed to index metrics in Elasticsearch", e);
        }
    }

    /**
     * Query audit logs by user (RQ-ES-EL-02, RQ-ES-EL-04).
     */
    public List<ElasticsearchAuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    /**
     * Query audit logs by time range for historical analysis (RQ-ES-EL-04).
     */
    public List<ElasticsearchAuditLog> getAuditLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    /**
     * Query audit logs by action for security investigation.
     */
    public List<ElasticsearchAuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    /**
     * Query metrics by time range for trend analysis (RQ-ES-MON-03).
     */
    public List<ElasticsearchMetrics> getMetricsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return metricsRepository.findByTimestampBetween(start, end);
    }

    /**
     * Query recent metrics for real-time monitoring (RQ-ES-MON-02).
     */
    public List<ElasticsearchMetrics> getRecentMetrics(int minutesBack) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
        return metricsRepository.findByTimestampAfter(since);
    }

    /**
     * Determine severity level based on action (RQ-ES-ALRT-01).
     */
    private String determineSeverity(String action) {
        if (action.contains("DELETE") || action.contains("REVOKE")) {
            return "CRITICAL";
        } else if (action.contains("UPDATE") || action.contains("RESET")) {
            return "WARN";
        } else if (action.contains("LOGIN") || action.contains("ACCESS")) {
            return "INFO";
        }
        return "INFO";
    }
}

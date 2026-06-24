package com.example.buildnest_ecommerce.service.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
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
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ElasticsearchIngestionService {

    private final ElasticsearchAuditLogRepository auditLogRepository;
    private final ElasticsearchMetricsRepository metricsRepository;
    private final CircuitBreaker elasticsearchCircuitBreaker;

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

            elasticsearchCircuitBreaker.executeRunnable(() -> auditLogRepository.save(esLog));
            log.debug("Audit log indexed in Elasticsearch: {} - {} (Status: {})", action, userId, httpStatusCode);
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, skipping audit log ingestion (graceful degradation)");
        } catch (Exception e) {
            log.error("Failed to index audit log in Elasticsearch", e);
        }
    }

    /**
     * Query API errors by HTTP status code (RQ-SRCH-04).
     * Retrieves all API error events with specific HTTP status codes.
     */
    public List<ElasticsearchAuditLog> getErrorsByHttpStatusCode(Integer httpStatusCode) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(
                    () -> auditLogRepository.findByHttpStatusCode(httpStatusCode));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getErrorsByHttpStatusCode");
            return Collections.emptyList();
        }
    }

    /**
     * Query API errors by category (RQ-SRCH-04).
     * Retrieves errors categorized by CLIENT_ERROR, SERVER_ERROR, etc.
     */
    public List<ElasticsearchAuditLog> getErrorsByCategory(String errorCategory) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(
                    () -> auditLogRepository.findByErrorCategory(errorCategory));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getErrorsByCategory");
            return Collections.emptyList();
        }
    }

    /**
     * Query API errors by status code and time range (RQ-SRCH-04).
     */
    public List<ElasticsearchAuditLog> getErrorsByStatusCodeAndTimeRange(Integer httpStatusCode, LocalDateTime start,
            LocalDateTime end) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(
                    () -> auditLogRepository.findByHttpStatusCodeAndTimestampBetween(httpStatusCode, start, end));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getErrorsByStatusCodeAndTimeRange");
            return Collections.emptyList();
        }
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

            elasticsearchCircuitBreaker.executeRunnable(() -> metricsRepository.save(metrics));
            log.debug("Metric indexed in Elasticsearch: {} = {}", metricName, value);
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, skipping metrics ingestion (graceful degradation)");
        } catch (Exception e) {
            log.error("Failed to index metrics in Elasticsearch", e);
        }
    }

    /**
     * Query audit logs by user (RQ-ES-EL-02, RQ-ES-EL-04).
     */
    public List<ElasticsearchAuditLog> getAuditLogsByUser(Long userId) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(() -> auditLogRepository.findByUserId(userId));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getAuditLogsByUser");
            return Collections.emptyList();
        }
    }

    /**
     * Query audit logs by time range for historical analysis (RQ-ES-EL-04).
     */
    public List<ElasticsearchAuditLog> getAuditLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(
                    () -> auditLogRepository.findByTimestampBetween(start, end));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getAuditLogsByTimeRange");
            return Collections.emptyList();
        }
    }

    /**
     * Query audit logs by action for security investigation.
     */
    public List<ElasticsearchAuditLog> getAuditLogsByAction(String action) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(() -> auditLogRepository.findByAction(action));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getAuditLogsByAction");
            return Collections.emptyList();
        }
    }

    /**
     * Query metrics by time range for trend analysis (RQ-ES-MON-03).
     */
    public List<ElasticsearchMetrics> getMetricsByTimeRange(LocalDateTime start, LocalDateTime end) {
        try {
            return elasticsearchCircuitBreaker.executeSupplier(
                    () -> metricsRepository.findByTimestampBetween(start, end));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getMetricsByTimeRange");
            return Collections.emptyList();
        }
    }

    /**
     * Query recent metrics for real-time monitoring (RQ-ES-MON-02).
     */
    public List<ElasticsearchMetrics> getRecentMetrics(int minutesBack) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
        try {
            return elasticsearchCircuitBreaker.executeSupplier(() -> metricsRepository.findByTimestampAfter(since));
        } catch (CallNotPermittedException e) {
            log.debug("Elasticsearch circuit breaker OPEN, returning empty result for getRecentMetrics");
            return Collections.emptyList();
        }
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

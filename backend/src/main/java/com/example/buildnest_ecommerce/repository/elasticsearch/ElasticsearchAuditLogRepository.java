package com.example.buildnest_ecommerce.repository.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Elasticsearch audit log documents (RQ-ES-EL-02, RQ-ES-LOG-04).
 * Enables full-text search and filtering of audit logs stored in Elasticsearch.
 */
@Repository
public interface ElasticsearchAuditLogRepository extends ElasticsearchRepository<ElasticsearchAuditLog, String> {

    /**
     * Query audit logs by user ID for historical analysis (RQ-ES-EL-04).
     */
    List<ElasticsearchAuditLog> findByUserId(Long userId);

    /**
     * Query audit logs by action type for security analysis.
     */
    List<ElasticsearchAuditLog> findByAction(String action);

    /**
     * Time-based query for historical analysis (RQ-ES-EL-04).
     */
    List<ElasticsearchAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Query for specific entity audit trail.
     */
    List<ElasticsearchAuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Query by severity level for alerting (RQ-ES-ALRT-01).
     */
    @Query("{\"match\": {\"severity\": \"?0\"}}")
    List<ElasticsearchAuditLog> findBySeverity(String severity);

    /**
     * Query API errors by HTTP status code (RQ-SRCH-04).
     * Enables filtering of API error events by HTTP status codes.
     */
    List<ElasticsearchAuditLog> findByHttpStatusCode(Integer httpStatusCode);

    /**
     * Query API errors by error category (RQ-SRCH-04).
     * Categories: CLIENT_ERROR (4xx), SERVER_ERROR (5xx), SUCCESS (2xx), REDIRECT
     * (3xx)
     */
    List<ElasticsearchAuditLog> findByErrorCategory(String errorCategory);

    /**
     * Query API errors by status code and time range (RQ-SRCH-04).
     * Combined query for historical API error analysis.
     */
    List<ElasticsearchAuditLog> findByHttpStatusCodeAndTimestampBetween(Integer httpStatusCode, LocalDateTime start,
            LocalDateTime end);

    /**
     * Query errors by endpoint for service-specific monitoring (RQ-MON-02).
     */
    List<ElasticsearchAuditLog> findByEndpointAndErrorCategory(String endpoint, String errorCategory);
}

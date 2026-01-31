package com.example.buildnest_ecommerce.repository.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Elasticsearch metrics documents (RQ-ES-MON-01, RQ-ES-EL-02).
 * Enables time-series queries and aggregations for real-time and historical monitoring.
 */
@Repository
public interface ElasticsearchMetricsRepository extends ElasticsearchRepository<ElasticsearchMetrics, String> {

    /**
     * Query metrics by name for monitoring specific metrics.
     */
    List<ElasticsearchMetrics> findByMetricName(String metricName);

    /**
     * Time-based query for historical trend analysis (RQ-ES-EL-04, RQ-ES-MON-03).
     */
    List<ElasticsearchMetrics> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Query metrics by service for service-specific monitoring.
     */
    List<ElasticsearchMetrics> findByService(String service);

    /**
     * Query recent metrics for real-time monitoring (RQ-ES-MON-02).
     */
    List<ElasticsearchMetrics> findByTimestampAfter(LocalDateTime timestamp);

    /**
     * Query metrics by environment for multi-environment monitoring.
     */
    List<ElasticsearchMetrics> findByEnvironment(String environment);
}

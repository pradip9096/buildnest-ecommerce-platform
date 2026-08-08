package com.example.buildnest_ecommerce.actuator;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Elasticsearch connectivity (OPS-05).
 * Only active when elasticsearch.enabled=true, matching
 * ElasticsearchConfig's own gating.
 *
 * @author BuildNest Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * Performs a health check on the Elasticsearch cluster.
     *
     * @return Health status with cluster details
     */
    @Override
    public Health health() {
        try {
            HealthResponse response = elasticsearchClient.cluster().health();
            String status = response.status().jsonValue();

            Health.Builder builder = "red".equalsIgnoreCase(status)
                    ? Health.down()
                    : Health.up();
            builder.withDetail("cluster", response.clusterName())
                    .withDetail("status", status)
                    .withDetail("numberOfNodes", response.numberOfNodes())
                    .withDetail("activeShards", response.activeShards());

            return builder.build();
        } catch (Exception e) {
            log.error("Elasticsearch health check failed", e);
            return Health.down()
                    .withDetail("cluster", "Elasticsearch")
                    .withDetail("status", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }
}

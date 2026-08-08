package com.example.buildnest_ecommerce.actuator;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch.cluster.ElasticsearchClusterClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Elasticsearch health indicator tests")
class ElasticsearchHealthIndicatorTest {

    private HealthResponse healthResponse(HealthStatus status) {
        return HealthResponse.of(b -> b
                .clusterName("buildnest-cluster")
                .status(status)
                .timedOut(false)
                .numberOfNodes(1)
                .numberOfDataNodes(1)
                .activePrimaryShards(1)
                .activeShards(1)
                .relocatingShards(0)
                .initializingShards(0)
                .unassignedShards(0)
                .unassignedPrimaryShards(0)
                .delayedUnassignedShards(0)
                .numberOfPendingTasks(0)
                .numberOfInFlightFetch(0)
                .taskMaxWaitingInQueueMillis(0)
                .activeShardsPercentAsNumber(100.0));
    }

    @Test
    @DisplayName("Elasticsearch health should be up when cluster is green")
    void testElasticsearchHealthUp() throws IOException {
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        ElasticsearchClusterClient cluster = mock(ElasticsearchClusterClient.class);
        when(client.cluster()).thenReturn(cluster);
        when(cluster.health()).thenReturn(healthResponse(HealthStatus.Green));

        ElasticsearchHealthIndicator indicator = new ElasticsearchHealthIndicator(client);
        Health health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("green", health.getDetails().get("status"));
    }

    @Test
    @DisplayName("Elasticsearch health should be down when cluster is red")
    void testElasticsearchHealthDownOnRedStatus() throws IOException {
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        ElasticsearchClusterClient cluster = mock(ElasticsearchClusterClient.class);
        when(client.cluster()).thenReturn(cluster);
        when(cluster.health()).thenReturn(healthResponse(HealthStatus.Red));

        ElasticsearchHealthIndicator indicator = new ElasticsearchHealthIndicator(client);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
    }

    @Test
    @DisplayName("Elasticsearch health should be down when connection throws")
    void testElasticsearchHealthDownOnException() throws IOException {
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        ElasticsearchClusterClient cluster = mock(ElasticsearchClusterClient.class);
        when(client.cluster()).thenReturn(cluster);
        when(cluster.health()).thenThrow(new IOException("connection refused"));

        ElasticsearchHealthIndicator indicator = new ElasticsearchHealthIndicator(client);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Connection failed", health.getDetails().get("status"));
    }
}

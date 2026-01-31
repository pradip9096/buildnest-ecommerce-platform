package com.example.buildnest_ecommerce.config;

import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import com.example.buildnest_ecommerce.service.notification.NotificationService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchAlertingService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchMetricsCollectorService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration to disable Elasticsearch dependencies and provide mocks.
 */
@TestConfiguration
public class TestElasticsearchConfig {

    @Bean
    public ElasticsearchIngestionService elasticsearchIngestionService() {
        return Mockito.mock(ElasticsearchIngestionService.class);
    }

    @Bean
    public ElasticsearchAlertingService elasticsearchAlertingService() {
        return Mockito.mock(ElasticsearchAlertingService.class);
    }

    @Bean
    public ElasticsearchMetricsCollectorService elasticsearchMetricsCollectorService() {
        return Mockito.mock(ElasticsearchMetricsCollectorService.class);
    }

    @Bean
    public ElasticsearchAuditLogRepository elasticsearchAuditLogRepository() {
        return Mockito.mock(ElasticsearchAuditLogRepository.class);
    }

    @Bean
    public ElasticsearchMetricsRepository elasticsearchMetricsRepository() {
        return Mockito.mock(ElasticsearchMetricsRepository.class);
    }

    @Bean
    public NotificationService notificationService() {
        return Mockito.mock(NotificationService.class);
    }
}

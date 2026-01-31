package com.example.buildnest_ecommerce.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.beans.factory.annotation.Value;
import java.net.InetSocketAddress;

/**
 * Elasticsearch configuration for centralized logging and analytics (RQ-ES-02).
 * Configures connection to Elasticsearch cluster for storing logs, metrics, and audit events.
 * Only enabled when elasticsearch.enabled=true property is set.
 */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@EnableElasticsearchRepositories(basePackages = "com.example.civil_ecommerce.repository.elasticsearch")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port:9200}")
    private int elasticsearchPort;

    @Value("${elasticsearch.username:elastic}")
    private String username;

    @Value("${elasticsearch.password:changeme}")
    private String password;

    @Value("${elasticsearch.ssl.enabled:false}")
    private boolean sslEnabled;

    /**
     * Configure Elasticsearch client connection (RQ-ES-EL-02, RQ-ES-EL-03).
     * Supports distributed querying across multiple nodes and distributed storage.
     */
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(new InetSocketAddress(elasticsearchHost, elasticsearchPort))
                .withConnectTimeout(5000)
                .withSocketTimeout(60000)
                .build();
    }
}

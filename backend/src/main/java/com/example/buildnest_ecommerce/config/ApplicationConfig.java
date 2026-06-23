package com.example.buildnest_ecommerce.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * Application-level configuration beans (RQ-ES-ALRT-04).
 */
@Configuration
public class ApplicationConfig {

    /**
     * Configure RestTemplate for webhook notifications and external service calls.
     * Includes timeout settings and connection pooling.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());

        return builder
            .requestFactory(() -> requestFactory)
            .build();
    }
}

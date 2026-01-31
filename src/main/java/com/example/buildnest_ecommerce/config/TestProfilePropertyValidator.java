package com.example.buildnest_ecommerce.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Fail-fast validation for required test profile properties.
 * Prevents configuration drift in CI by enforcing required settings.
 */
@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestProfilePropertyValidator {

    private final Environment environment;

    @PostConstruct
    void validateRequiredProperties() {
        List<String> required = List.of(
                "spring.datasource.url",
                "spring.datasource.driver-class-name",
                "jwt.secret",
                "jwt.expiration",
                "jwt.refresh.expiration",
                "spring.liquibase.change-log");

        for (String key : required) {
            String value = environment.getProperty(key);
            if (!StringUtils.hasText(value)) {
                String message = "Missing required test property: " + key;
                log.error(message);
                throw new IllegalStateException(message);
            }
        }

        log.info("Test profile configuration validation passed.");
    }
}

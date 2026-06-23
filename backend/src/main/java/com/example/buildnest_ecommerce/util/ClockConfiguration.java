package com.example.buildnest_ecommerce.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a configurable Clock bean for deterministic time-based testing.
 * Default uses system clock. In tests, override with Clock.fixed().
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

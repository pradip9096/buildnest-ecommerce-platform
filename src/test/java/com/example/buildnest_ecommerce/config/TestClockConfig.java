package com.example.buildnest_ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Provides a fixed Clock for deterministic time-based testing.
 * Overrides the system Clock bean in test profile.
 * 
 * Usage: In tests that need specific timestamps, autowire Clock and use:
 * - clock.instant() for current instant
 * - LocalDateTime.now(clock) for current date-time
 */
@TestConfiguration
@Profile("test")
public class TestClockConfig {

    // Fixed instant: 2024-01-15 10:00:00 UTC
    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:00:00Z");

    @Bean
    @Primary
    public Clock clock() {
        return Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    }
}

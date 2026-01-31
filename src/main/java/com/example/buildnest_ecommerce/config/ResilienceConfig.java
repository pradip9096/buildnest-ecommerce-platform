package com.example.buildnest_ecommerce.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience Configuration for fault tolerance and high availability (RQ-NFR-02).
 * Implements circuit breaker and time limiter patterns to handle transient failures
 * and prevent cascading failures in distributed system.
 * 
 * Circuit Breaker Configuration:
 * - Failure threshold: 50% of calls
 * - Slow call duration threshold: 2 seconds
 * - Wait duration in OPEN state: 30 seconds
 * - Auto-transition from OPEN to HALF_OPEN after wait duration
 * 
 * Time Limiter Configuration:
 * - Default timeout: 5 seconds
 * - Cancels long-running operations to prevent resource exhaustion
 */
@Configuration
public class ResilienceConfig {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceConfig.class);

    /**
     * Circuit breaker configuration for database operations.
     * Protects against database overload and connection pool exhaustion.
     * 
     * @return Configured CircuitBreakerRegistry
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .minimumNumberOfCalls(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
        registry.getEventPublisher()
                .onEntryAdded(event -> logger.info("CircuitBreaker created: {}", event.getAddedEntry().getName()))
                .onEntryRemoved(event -> logger.info("CircuitBreaker removed: {}", event.getRemovedEntry().getName()));

        return registry;
    }

    /**
     * Circuit breaker for Redis operations.
     * Handles Redis connection failures gracefully with fallback behavior.
     * 
     * @param circuitBreakerRegistry The circuit breaker registry
     * @return CircuitBreaker configured for Redis
     */
    @Bean
    public CircuitBreaker redisCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("redis-circuit-breaker",
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .minimumNumberOfCalls(3)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .recordExceptions(Exception.class)
                        .ignoreExceptions(IllegalArgumentException.class)
                        .build());
    }

    /**
     * Circuit breaker for database operations.
     * Protects against database downtime and query timeouts.
     * 
     * @param circuitBreakerRegistry The circuit breaker registry
     * @return CircuitBreaker configured for database
     */
    @Bean
    public CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("database-circuit-breaker",
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(60)
                        .slowCallRateThreshold(50)
                        .slowCallDurationThreshold(Duration.ofSeconds(3))
                        .waitDurationInOpenState(Duration.ofSeconds(60))
                        .minimumNumberOfCalls(5)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .recordExceptions(Exception.class)
                        .build());
    }

    /**
     * Time limiter registry for timeout management.
     * Prevents long-running operations from consuming resources indefinitely.
     * 
     * @return Configured TimeLimiterRegistry
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig defaultConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(defaultConfig);
    }

    /**
     * Time limiter for database operations with 8-second timeout.
     * 
     * @param timeLimiterRegistry The time limiter registry
     * @return TimeLimiter configured for database
     */
    @Bean
    public TimeLimiter databaseTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter("database-time-limiter",
                TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(8))
                        .cancelRunningFuture(true)
                        .build());
    }

    /**
     * Time limiter for Redis operations with 3-second timeout.
     * 
     * @param timeLimiterRegistry The time limiter registry
     * @return TimeLimiter configured for Redis
     */
    @Bean
    public TimeLimiter redisTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter("redis-time-limiter",
                TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))
                        .cancelRunningFuture(true)
                        .build());
    }
}

package com.example.buildnest_ecommerce.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Performance Monitoring Configuration.
 * Configures metrics collection, custom meters, and monitoring dashboards.
 * 
 * Features:
 * - Custom application metrics
 * - Response time tracking
 * - Throughput monitoring
 * - Error rate tracking
 * - Resource utilization metrics
 * 
 * Access metrics at: http://localhost:8080/actuator/metrics
 * Prometheus endpoint: http://localhost:8080/actuator/prometheus
 */
@Configuration
@EnableAspectJAutoProxy
public class PerformanceMonitoringConfig {

    /**
     * Customize meter registry with common tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "buildnest-ecommerce",
                        "version", "1.0.0");
    }

    /**
     * Enable @Timed annotation support for method-level metrics.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Custom metrics for business operations.
     */
    @Bean
    public PerformanceMetrics performanceMetrics(MeterRegistry registry) {
        return new PerformanceMetrics(registry);
    }

    /**
     * Custom metrics collector for business-specific measurements.
     */
    public static class PerformanceMetrics {
        private final MeterRegistry registry;
        private final Timer checkoutTimer;
        private final Timer productSearchTimer;
        private final Timer authenticationTimer;

        public PerformanceMetrics(MeterRegistry registry) {
            this.registry = registry;

            // Custom timers for critical operations
            this.checkoutTimer = Timer.builder("checkout.processing.time")
                    .description("Time taken to process checkout")
                    .tag("operation", "checkout")
                    .register(registry);

            this.productSearchTimer = Timer.builder("product.search.time")
                    .description("Time taken to search products")
                    .tag("operation", "search")
                    .register(registry);

            this.authenticationTimer = Timer.builder("authentication.time")
                    .description("Time taken to authenticate user")
                    .tag("operation", "auth")
                    .register(registry);
        }

        public Timer getCheckoutTimer() {
            return checkoutTimer;
        }

        public Timer getProductSearchTimer() {
            return productSearchTimer;
        }

        public Timer getAuthenticationTimer() {
            return authenticationTimer;
        }

        public void recordCheckoutSuccess() {
            registry.counter("checkout.success", "status", "success").increment();
        }

        public void recordCheckoutFailure(String reason) {
            registry.counter("checkout.failure", "status", "failure", "reason", reason).increment();
        }

        public void recordAuthenticationAttempt(boolean success) {
            registry.counter("authentication.attempts",
                    "status", success ? "success" : "failure").increment();
        }

        public void recordCartOperation(String operation) {
            registry.counter("cart.operations", "operation", operation).increment();
        }
    }
}

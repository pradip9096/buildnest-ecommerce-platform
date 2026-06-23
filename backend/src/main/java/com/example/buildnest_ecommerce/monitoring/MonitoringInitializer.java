package com.example.buildnest_ecommerce.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Monitoring Initialization - Initializes business metrics on startup
 * 
 * Metrics are exposed at: /actuator/prometheus
 * Grafana dashboard: http://grafana:3000
 */
@Slf4j
@Component
public class MonitoringInitializer {

    @Autowired
    private BusinessMetricsService businessMetricsService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeMonitoring() {
        log.info("Starting monitoring and observability initialization...");

        try {
            // Initialize business metrics
            businessMetricsService.initializeMetrics();

            log.info("✅ Monitoring initialization complete");
            log.info("📊 Prometheus metrics available at: /actuator/prometheus");
            log.info("📈 Grafana dashboard: http://localhost:3000/d/buildnest-ecommerce");

        } catch (Exception e) {
            log.error("Failed to initialize monitoring: {}", e.getMessage(), e);
            throw new RuntimeException("Monitoring initialization failed", e);
        }
    }
}

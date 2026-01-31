package com.example.buildnest_ecommerce.service.monitoring;

import java.util.Map;

/**
 * Interface for Performance Monitoring Service operations.
 * Defines contract for tracking and monitoring API performance.
 */
public interface IPerformanceMonitoringService {

    /**
     * Records API endpoint response time.
     * 
     * @param endpoint       API endpoint
     * @param responseTimeMs Response time in milliseconds
     */
    void recordResponseTime(String endpoint, long responseTimeMs);

    /**
     * Get current performance metrics.
     * 
     * @return Map containing performance statistics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Get endpoint-specific metrics.
     * 
     * @param endpoint API endpoint
     * @return Map containing endpoint-specific statistics
     */
    Map<String, Object> getEndpointMetrics(String endpoint);

    /**
     * Reset all metrics (for testing or periodic cleanup).
     */
    void resetMetrics();
}

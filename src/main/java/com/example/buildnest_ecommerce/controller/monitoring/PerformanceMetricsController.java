package com.example.buildnest_ecommerce.controller.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Performance Monitoring Endpoint
 *
 * PERFORMANCE_OPTIMIZATION_GUIDE - Section 7: Monitoring &amp; Metrics
 *
 * Provides comprehensive performance metrics for the application including
 * JVM statistics, GC information, memory usage, and uptime data.
 *
 * Endpoints:
 * - GET /actuator/custom/performance-metrics : Aggregated performance data
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/actuator/custom")
public class PerformanceMetricsController {

    /**
     * Get comprehensive application performance metrics.
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Section 7:
     * Returns performance baselines for comparison:
     * - JVM Metrics: Heap, GC, Thread count
     * - Application Metrics: Uptime, Active Threads
     * - Performance Baselines: Expected values per guide
     *
     * @return Map containing all performance metrics
     */
    @GetMapping("/performance-metrics")
    public Map<String, Object> getPerformanceMetrics() {
        log.debug("Fetching performance metrics");

        Map<String, Object> metrics = new HashMap<>();

        // JVM Metrics
        Map<String, Object> jvmMetrics = new HashMap<>();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

        jvmMetrics.put("heapUsed_MB", heapUsed / (1024 * 1024));
        jvmMetrics.put("heapMax_MB", heapMax / (1024 * 1024));
        jvmMetrics.put("heapUsagePercentage", String.format("%.2f%%", (double) heapUsed / heapMax * 100));
        jvmMetrics.put("nonHeapUsed_MB", nonHeapUsed / (1024 * 1024));

        metrics.put("jvm", jvmMetrics);

        // Runtime Metrics
        Map<String, Object> runtimeMetrics = new HashMap<>();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        long uptimeSeconds = runtimeBean.getUptime() / 1000;
        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        int peakThreadCount = ManagementFactory.getThreadMXBean().getPeakThreadCount();

        runtimeMetrics.put("uptimeSeconds", uptimeSeconds);
        runtimeMetrics.put("currentThreadCount", threadCount);
        runtimeMetrics.put("peakThreadCount", peakThreadCount);
        runtimeMetrics.put("processorCount", Runtime.getRuntime().availableProcessors());

        metrics.put("runtime", runtimeMetrics);

        // Performance Baselines (PERFORMANCE_OPTIMIZATION_GUIDE Table)
        Map<String, Object> baselines = new HashMap<>();
        baselines.put("api_response_time_p95_ms", 200);
        baselines.put("api_response_time_good_ms", 1000);
        baselines.put("throughput_requests_per_sec", 1000);
        baselines.put("cache_hit_rate_percentage", 85);
        baselines.put("error_rate_threshold_percentage", 0.1);
        baselines.put("gc_pause_time_ms", 100);

        metrics.put("baselines", baselines);

        // Health Assessment
        Map<String, Object> health = new HashMap<>();
        double heapUtilization = (double) heapUsed / heapMax * 100;

        String status = "HEALTHY";
        if (heapUtilization > 95) {
            status = "CRITICAL";
        } else if (heapUtilization > 80) {
            status = "WARNING";
        }

        health.put("status", status);
        health.put("timestamp", System.currentTimeMillis());

        if (heapUtilization > 80) {
            log.warn("ALERT: Heap utilization at {:.2f}%. Consider increasing -Xmx", heapUtilization);
        }

        metrics.put("health", health);

        return metrics;
    }

    /**
     * Get cache performance metrics.
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Section 4 &amp; 7
     *
     * @return Map containing cache hit rate and metrics
     */
    @GetMapping("/cache-metrics")
    public Map<String, Object> getCacheMetrics() {
        log.debug("Fetching cache performance metrics");

        Map<String, Object> cacheMetrics = new HashMap<>();

        // These would be populated from actual cache metrics
        // For now, provide structure for integration

        Map<String, Object> cacheStats = new HashMap<>();
        cacheStats.put("hitRate", "Use CacheMetricsUtil for actual data");
        cacheStats.put("missRate", "Use CacheMetricsUtil for actual data");
        cacheStats.put("evictionCount", "Use CacheMetricsUtil for actual data");

        cacheMetrics.put("cacheStatistics", cacheStats);

        // Baseline expectations
        Map<String, Object> expectations = new HashMap<>();
        expectations.put("targetHitRate", ">85%");
        expectations.put("warningHitRate", "70-85%");
        expectations.put("poorHitRate", "<70%");

        cacheMetrics.put("expectations", expectations);

        return cacheMetrics;
    }

    /**
     * Get database performance metrics.
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Section 3 &amp; 7
     *
     * @return Map containing database query and connection metrics
     */
    @GetMapping("/database-metrics")
    public Map<String, Object> getDatabaseMetrics() {
        log.debug("Fetching database performance metrics");

        Map<String, Object> dbMetrics = new HashMap<>();

        // Query performance baselines
        Map<String, Object> queryBaselines = new HashMap<>();
        queryBaselines.put("query_time_good_ms", 50);
        queryBaselines.put("query_time_acceptable_ms", 200);
        queryBaselines.put("query_time_investigate_ms", 1000);

        dbMetrics.put("queryPerformanceBaselines", queryBaselines);

        // Connection metrics
        Map<String, Object> connectionMetrics = new HashMap<>();
        connectionMetrics.put("instruction", "Use PoolMetricsController at /actuator/custom/pool-status");

        dbMetrics.put("connectionMetrics", connectionMetrics);

        return dbMetrics;
    }

    /**
     * Get comprehensive performance report.
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Complete Overview
     *
     * @return Aggregated performance report
     */
    @GetMapping("/performance-report")
    public Map<String, Object> getPerformanceReport() {
        log.info("Generating comprehensive performance report");

        Map<String, Object> report = new HashMap<>();

        report.put("title", "BuildNest E-Commerce Performance Report");
        report.put("guide", "PERFORMANCE_OPTIMIZATION_GUIDE v1.0");
        report.put("generatedAt", System.currentTimeMillis());

        // Sections
        Map<String, String> sections = new HashMap<>();
        sections.put("jvm_tuning", "GET /actuator/custom/performance-metrics (jvm section)");
        sections.put("database_optimization", "GET /actuator/custom/database-metrics");
        sections.put("cache_optimization", "GET /actuator/custom/cache-metrics");
        sections.put("connection_pool_tuning", "GET /actuator/custom/pool-status");
        sections.put("monitoring_metrics", "GET /actuator/prometheus");

        report.put("sections", sections);

        // Quick Assessment
        Map<String, String> assessment = new HashMap<>();
        assessment.put("status", "Review individual endpoints for detailed metrics");
        assessment.put("baselines", "See performance-metrics endpoint for expected values");
        assessment.put("troubleshooting", "Refer to PERFORMANCE_OPTIMIZATION_GUIDE Troubleshooting Section");

        report.put("quickAssessment", assessment);

        return report;
    }
}

package com.example.buildnest_ecommerce.service.elasticsearch;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Service for collecting application metrics and pushing to Elasticsearch (RQ-ES-MON-01, RQ-ES-MON-02, RQ-ES-MON-03).
 * Collects JVM, HTTP, and database metrics every minute for storage and analysis.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ElasticsearchMetricsCollectorService {

    private final ElasticsearchIngestionService ingestionService;
    private final MeterRegistry meterRegistry;

    @Value("${spring.application.name:civil-ecommerce}")
    private String applicationName;

    @Value("${elasticsearch.metrics.enabled:true}")
    private boolean metricsEnabled;

    /**
     * Collect and push metrics every 60 seconds (RQ-ES-MON-01, RQ-ES-MON-02).
     */
    @Scheduled(fixedDelay = 60000)
    public void collectAndPushMetrics() {
        if (!metricsEnabled) {
            return;
        }

        try {
            collectJVMMetrics();
            collectHTTPMetrics();
            collectDatabaseMetrics();
            log.debug("Metrics successfully collected and pushed to Elasticsearch");
        } catch (Exception e) {
            log.error("Error collecting metrics", e);
        }
    }

    /**
     * Collect JVM memory and CPU metrics (RQ-ES-MON-01).
     */
    private void collectJVMMetrics() {
        try {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
            double heapUsagePercent = (double) heapUsed / heapMax * 100;

            ingestionService.indexMetrics(
                    "jvmMemoryUsagePercent",
                    heapUsagePercent,
                    "percent",
                    applicationName,
                    getHostname(),
                    "production"
            );

            long heapCommitted = memoryMXBean.getHeapMemoryUsage().getCommitted();
            ingestionService.indexMetrics(
                    "jvmHeapCommittedBytes",
                    (double) heapCommitted,
                    "bytes",
                    applicationName,
                    getHostname(),
                    "production"
            );

            // CPU metrics from MeterRegistry
            var cpuGauge = meterRegistry.find("process.cpu.usage").gauge();
            if (cpuGauge != null) {
                double processCpuUsage = cpuGauge.value() * 100;
                ingestionService.indexMetrics(
                        "processCpuUsagePercent",
                        processCpuUsage,
                        "percent",
                        applicationName,
                        getHostname(),
                        "production"
                );
            }
        } catch (Exception e) {
            log.error("Error collecting JVM metrics", e);
        }
    }

    /**
     * Collect HTTP request metrics (RQ-ES-MON-01, RQ-ES-MON-02).
     */
    private void collectHTTPMetrics() {
        try {
            // HTTP request count
            var httpCounter = meterRegistry.find("http.server.requests").counter();
            if (httpCounter != null) {
                double httpRequests = httpCounter.count();
                ingestionService.indexMetrics(
                        "httpRequestCount",
                        httpRequests,
                        "count",
                        applicationName,
                        getHostname(),
                        "production"
                );
            }

            // HTTP response time - find max response time from timers
            var httpTimer = meterRegistry.find("http.server.requests").timer();
            if (httpTimer != null) {
                double maxResponseTime = httpTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS);
                ingestionService.indexMetrics(
                        "httpResponseTimeMs",
                        maxResponseTime,
                        "milliseconds",
                        applicationName,
                        getHostname(),
                        "production"
                );
            }
        } catch (Exception e) {
            log.error("Error collecting HTTP metrics", e);
        }
    }

    /**
     * Collect database connection and query metrics (RQ-ES-MON-01, RQ-ES-MON-02).
     */
    private void collectDatabaseMetrics() {
        try {
            // Database connection pool size
            var dbConnGauge = meterRegistry.find("db.connection.pool.size").gauge();
            if (dbConnGauge != null) {
                double dbConnections = dbConnGauge.value();
                ingestionService.indexMetrics(
                        "dbConnectionPoolSize",
                        dbConnections,
                        "count",
                        applicationName,
                        getHostname(),
                        "production"
                );
            }

            // Database active connections
            var activeConnGauge = meterRegistry.find("db.connection.active").gauge();
            if (activeConnGauge != null) {
                double activeConnections = activeConnGauge.value();
                ingestionService.indexMetrics(
                        "dbActiveConnections",
                        activeConnections,
                        "count",
                        applicationName,
                        getHostname(),
                        "production"
                );
            }

            // Database idle connections
            var idleConnGauge = meterRegistry.find("db.connection.idle").gauge();
            if (idleConnGauge != null) {
                double idleConnections = idleConnGauge.value();
                ingestionService.indexMetrics(
                        "dbIdleConnections",
                        idleConnections,
                        "count",
                        applicationName,
                        getHostname(),
                        "production"
                );
            }
        } catch (Exception e) {
            log.error("Error collecting database metrics", e);
        }
    }

    /**
     * Get hostname for metric identification.
     */
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}

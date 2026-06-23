package com.example.buildnest_ecommerce.service.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application Performance Monitoring Service
 * 
 * Tracks and monitors API response times to ensure SLA compliance.
 * SYS-PERF-001: API response time &lt; 500ms (95th percentile)
 * SYS-PERF-002: Support 1000 concurrent users
 * 
 * Collects metrics:
 * - Average response time
 * - 95th percentile response time
 * - 99th percentile response time
 * - Slow request count
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceMonitoringService implements IPerformanceMonitoringService {

    private static final int MAX_METRICS = 10000;
    private static final long SLOW_REQUEST_THRESHOLD_MS = 500;

    private final ConcurrentHashMap<String, Long> responseTimes = new ConcurrentHashMap<>();
    private volatile long slowRequestCount = 0;
    private volatile long totalRequestCount = 0;
    private volatile LocalDateTime lastMetricsReset = LocalDateTime.now();

    /**
     * Records API endpoint response time
     * 
     * @param endpoint       API endpoint
     * @param responseTimeMs Response time in milliseconds
     */
    public void recordResponseTime(String endpoint, long responseTimeMs) {
        try {
            String key = endpoint + "_" + System.nanoTime();
            responseTimes.put(key, responseTimeMs);
            totalRequestCount++;

            if (responseTimeMs > SLOW_REQUEST_THRESHOLD_MS) {
                slowRequestCount++;
                if (responseTimeMs > 1000) {
                    log.warn("SLOW_REQUEST detected: {} took {}ms", endpoint, responseTimeMs);
                }
            }

            // Prevent memory overflow
            if (responseTimes.size() > MAX_METRICS) {
                responseTimes.clear();
            }
        } catch (Exception e) {
            log.error("Error recording response time: {}", e.getMessage());
        }
    }

    /**
     * Gets current performance metrics
     * 
     * @return Performance metrics map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        if (responseTimes.isEmpty()) {
            metrics.put("status", "NO_DATA");
            return metrics;
        }

        java.util.List<Long> times = new java.util.ArrayList<>(responseTimes.values());
        times.sort(Long::compareTo);

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long p95 = getPercentile(times, 95);
        long p99 = getPercentile(times, 99);
        long min = times.get(0);
        long max = times.get(times.size() - 1);

        metrics.put("averageResponseTimeMs", String.format("%.2f", avg));
        metrics.put("p95ResponseTimeMs", p95);
        metrics.put("p99ResponseTimeMs", p99);
        metrics.put("minResponseTimeMs", min);
        metrics.put("maxResponseTimeMs", max);
        metrics.put("totalRequests", totalRequestCount);
        metrics.put("slowRequests", slowRequestCount);
        metrics.put("slowRequestPercentage", String.format("%.2f%%", (slowRequestCount * 100.0 / totalRequestCount)));
        metrics.put("slaCompliance", p95 <= SLOW_REQUEST_THRESHOLD_MS ? "✓ PASS" : "✗ FAIL");
        metrics.put("sampledMetrics", times.size());
        metrics.put("lastResetTime", lastMetricsReset);

        return metrics;
    }

    /**
     * Calculates percentile from sorted list
     * 
     * @param sortedList Sorted list of values
     * @param percentile Percentile to calculate (0-100)
     * @return Value at percentile
     */
    private long getPercentile(java.util.List<Long> sortedList, int percentile) {
        if (sortedList.isEmpty())
            return 0;
        int index = (int) Math.ceil((percentile / 100.0) * sortedList.size()) - 1;
        return sortedList.get(Math.max(0, index));
    }

    /**
     * Scheduled task to log performance metrics every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void logPerformanceMetrics() {
        try {
            Map<String, Object> metrics = getPerformanceMetrics();
            log.info("PERFORMANCE_METRICS: {}", metrics);
        } catch (Exception e) {
            log.error("Error logging performance metrics: {}", e.getMessage());
        }
    }

    /**
     * Resets performance metrics
     */
    public void resetMetrics() {
        responseTimes.clear();
        slowRequestCount = 0;
        totalRequestCount = 0;
        lastMetricsReset = LocalDateTime.now();
        log.info("Performance metrics reset");
    }

    /**
     * Checks if SLA is being met
     * 
     * @return true if p95 response time &lt;= 500ms
     */
    public boolean isSLACompliant() {
        if (responseTimes.isEmpty())
            return true;

        java.util.List<Long> times = new java.util.ArrayList<>(responseTimes.values());
        times.sort(Long::compareTo);
        long p95 = getPercentile(times, 95);

        return p95 <= SLOW_REQUEST_THRESHOLD_MS;
    }

    /**
     * Gets slow query ratio
     * 
     * @return Percentage of slow requests
     */
    public double getSlowQueryRatio() {
        if (totalRequestCount == 0)
            return 0;
        return (slowRequestCount * 100.0) / totalRequestCount;
    }

    @Override
    public Map<String, Object> getEndpointMetrics(String endpoint) {
        Map<String, Object> metrics = new LinkedHashMap<>();

        // Filter times for this specific endpoint
        java.util.List<Long> endpointTimes = responseTimes.entrySet().stream()
                .filter(e -> e.getKey().startsWith(endpoint + "_"))
                .map(Map.Entry::getValue)
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        if (endpointTimes.isEmpty()) {
            metrics.put("status", "NO_DATA");
            metrics.put("endpoint", endpoint);
            return metrics;
        }

        double avg = endpointTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long p95 = getPercentile(endpointTimes, 95);
        long p99 = getPercentile(endpointTimes, 99);
        long min = endpointTimes.get(0);
        long max = endpointTimes.get(endpointTimes.size() - 1);

        metrics.put("endpoint", endpoint);
        metrics.put("averageResponseTimeMs", String.format("%.2f", avg));
        metrics.put("p95ResponseTimeMs", p95);
        metrics.put("p99ResponseTimeMs", p99);
        metrics.put("minResponseTimeMs", min);
        metrics.put("maxResponseTimeMs", max);
        metrics.put("requestCount", endpointTimes.size());
        metrics.put("slaCompliant", p95 <= SLOW_REQUEST_THRESHOLD_MS);

        return metrics;
    }
}

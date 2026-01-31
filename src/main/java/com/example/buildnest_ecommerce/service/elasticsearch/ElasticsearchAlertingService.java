package com.example.buildnest_ecommerce.service.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for monitoring metrics and triggering alerts (RQ-ES-ALRT-01, RQ-ES-ALRT-02, RQ-ES-ALRT-03, RQ-ES-ALRT-04).
 * Implements threshold-based alerting with webhook notifications.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ElasticsearchAlertingService {

    private final ElasticsearchMetricsRepository metricsRepository;
    private final RestTemplate restTemplate;

    @Value("${elasticsearch.alert.cpu.threshold:80}")
    private double cpuThreshold;

    @Value("${elasticsearch.alert.memory.threshold:90}")
    private double memoryThreshold;

    @Value("${elasticsearch.alert.error-rate.threshold:5}")
    private double errorRateThreshold;

    @Value("${elasticsearch.alert.webhook.url:}")
    private String webhookUrl;

    @Value("${elasticsearch.alert.enabled:true}")
    private boolean alertingEnabled;

    /**
     * Monitor metrics every minute for threshold violations (RQ-ES-ALRT-02, RQ-ES-ALRT-03).
     */
    @Scheduled(fixedDelay = 60000)
    public void monitorMetrics() {
        if (!alertingEnabled || webhookUrl.isEmpty()) {
            return;
        }

        try {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            List<ElasticsearchMetrics> recentMetrics = metricsRepository.findByTimestampAfter(oneMinuteAgo);

            for (ElasticsearchMetrics metric : recentMetrics) {
                checkAndAlert(metric);
            }
        } catch (Exception e) {
            log.error("Error during metrics monitoring", e);
        }
    }

    /**
     * Check metric against thresholds and trigger alert if exceeded (RQ-ES-ALRT-01).
     */
    private void checkAndAlert(ElasticsearchMetrics metric) {
        List<String> violations = new ArrayList<>();

        if (metric.getMetricName().contains("jvmMemory") && metric.getValue() > memoryThreshold) {
            violations.add(String.format("Memory usage %.2f%% exceeds threshold %.2f%%",
                    metric.getValue(), memoryThreshold));
        } else if (metric.getMetricName().contains("cpu") && metric.getValue() > cpuThreshold) {
            violations.add(String.format("CPU usage %.2f%% exceeds threshold %.2f%%",
                    metric.getValue(), cpuThreshold));
        } else if (metric.getMetricName().contains("error") && metric.getValue() > errorRateThreshold) {
            violations.add(String.format("Error rate %.2f%% exceeds threshold %.2f%%",
                    metric.getValue(), errorRateThreshold));
        }

        if (!violations.isEmpty()) {
            sendAlert(metric, violations);
        }
    }

    /**
     * Send alert via webhook (RQ-ES-ALRT-04).
     */
    private void sendAlert(ElasticsearchMetrics metric, List<String> violations) {
        try {
            Map<String, Object> alertPayload = new LinkedHashMap<>();
            alertPayload.put("timestamp", LocalDateTime.now());
            alertPayload.put("service", metric.getService());
            alertPayload.put("host", metric.getHost());
            alertPayload.put("metric", metric.getMetricName());
            alertPayload.put("value", metric.getValue());
            alertPayload.put("violations", violations);
            alertPayload.put("severity", "HIGH");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(alertPayload, headers);
            restTemplate.postForObject(webhookUrl, request, String.class);

            log.warn("Alert sent: {} - {}", metric.getMetricName(), violations);
        } catch (Exception e) {
            log.error("Failed to send alert webhook", e);
        }
    }

    /**
     * Get alert summary for the last hour (RQ-ES-ALRT-03).
     */
    public Map<String, Object> getAlertSummary() {
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        List<ElasticsearchMetrics> metrics = metricsRepository.findByTimestampAfter(lastHour);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("timestamp", LocalDateTime.now());
        summary.put("monitoringPeriod", "Last 1 hour");
        summary.put("metricsCollected", metrics.size());

        long criticalCount = metrics.stream()
                .filter(m -> m.getValue() > cpuThreshold || m.getValue() > memoryThreshold)
                .count();
        summary.put("criticalAlerts", criticalCount);

        return summary;
    }
}

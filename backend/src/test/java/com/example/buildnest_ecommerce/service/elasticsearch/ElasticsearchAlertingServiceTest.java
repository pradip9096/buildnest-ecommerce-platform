package com.example.buildnest_ecommerce.service.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("ElasticsearchAlertingService tests")
class ElasticsearchAlertingServiceTest {

    @Test
    @DisplayName("Should send alert when thresholds exceeded")
    void testMonitorMetricsSendsAlert() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 50.0);
        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("cpu.usage");
        metric.setValue(80.0);
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();

        verify(restTemplate).postForObject(eq("http://webhook"), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should not monitor when alerting disabled")
    void testMonitorMetricsDisabled() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "alertingEnabled", false);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");

        service.monitorMetrics();

        verify(repository, never()).findByTimestampAfter(any(LocalDateTime.class));
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should not monitor when webhook is empty")
    void testMonitorMetricsEmptyWebhook() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "alertingEnabled", true);
        ReflectionTestUtils.setField(service, "webhookUrl", "");

        service.monitorMetrics();

        verify(repository, never()).findByTimestampAfter(any(LocalDateTime.class));
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should alert on memory threshold violation")
    void testMonitorMetricsMemoryAlert() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "cpuThreshold", 80.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("jvmMemory.usage");
        metric.setValue(95.0);
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();

        verify(restTemplate).postForObject(eq("http://webhook"), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should alert on error rate threshold violation")
    void testMonitorMetricsErrorRateAlert() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("error.rate");
        metric.setValue(10.0);
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();

        verify(restTemplate).postForObject(eq("http://webhook"), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should not send alert when no thresholds exceeded")
    void testMonitorMetricsNoViolations() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 80.0);
        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("cpu.usage");
        metric.setValue(20.0);
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();

        verify(restTemplate, never()).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should handle repository errors during monitoring")
    void testMonitorMetricsHandlesRepositoryException() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        when(repository.findByTimestampAfter(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("db error"));

        assertDoesNotThrow(service::monitorMetrics);
    }

    @Test
    @DisplayName("Should handle webhook failures gracefully")
    void testMonitorMetricsHandlesWebhookException() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 50.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("cpu.usage");
        metric.setValue(80.0);
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));
        when(restTemplate.postForObject(eq("http://webhook"), any(), eq(String.class)))
                .thenThrow(new RuntimeException("webhook failed"));

        assertDoesNotThrow(service::monitorMetrics);
    }

    @Test
    @DisplayName("Should return alert summary")
    void testGetAlertSummary() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 50.0);
        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setValue(60.0);
        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        Map<String, Object> summary = service.getAlertSummary();
        assertEquals(1, summary.get("metricsCollected"));
        assertEquals(1L, summary.get("criticalAlerts"));
    }

    @Test
    @DisplayName("Boundary: CPU threshold at exact boundary")
    void testCpuThresholdAtBoundary() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 50.0);
        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("cpu.usage");
        metric.setValue(50.0); // Exactly at threshold
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();
        // Should not trigger alert when exactly at boundary (> not >=)
    }

    @Test
    @DisplayName("Boundary: CPU just above threshold")
    void testCpuThresholdJustAbove() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 50.0);
        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("cpu.usage");
        metric.setValue(50.1); // Just above threshold
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();
        verify(restTemplate).postForObject(eq("http://webhook"), any(), eq(String.class));
    }

    @Test
    @DisplayName("Boundary: Memory threshold at exact boundary")
    void testMemoryThresholdAtBoundary() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "cpuThreshold", 80.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("jvmMemory.usage");
        metric.setValue(90.0); // Exactly at threshold
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();
        // Should not trigger when exactly at boundary
    }

    @Test
    @DisplayName("Boundary: Error rate at exact boundary")
    void testErrorRateThresholdAtBoundary() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 80.0);
        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("error.rate");
        metric.setValue(5.0); // Exactly at threshold
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();
        // Should not trigger when exactly at boundary
    }

    @Test
    @DisplayName("Boundary: Zero metrics value")
    void testZeroMetricsValue() {
        ElasticsearchMetricsRepository repository = mock(ElasticsearchMetricsRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ElasticsearchAlertingService service = new ElasticsearchAlertingService(repository, restTemplate);

        ReflectionTestUtils.setField(service, "cpuThreshold", 50.0);
        ReflectionTestUtils.setField(service, "memoryThreshold", 90.0);
        ReflectionTestUtils.setField(service, "errorRateThreshold", 5.0);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "alertingEnabled", true);

        ElasticsearchMetrics metric = new ElasticsearchMetrics();
        metric.setMetricName("cpu.usage");
        metric.setValue(0.0); // Zero value
        metric.setService("svc");
        metric.setHost("host");

        when(repository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        service.monitorMetrics();
        // Should not trigger alert for zero metrics
    }
}

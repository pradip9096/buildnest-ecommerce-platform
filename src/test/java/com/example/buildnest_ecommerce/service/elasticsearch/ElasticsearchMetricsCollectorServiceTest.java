package com.example.buildnest_ecommerce.service.elasticsearch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ElasticsearchMetricsCollectorService tests")
class ElasticsearchMetricsCollectorServiceTest {

    @Test
    @DisplayName("Should collect and push metrics")
    void testCollectAndPushMetrics() {
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class, Mockito.RETURNS_DEEP_STUBS);

        Gauge cpuGauge = mock(Gauge.class);
        when(cpuGauge.value()).thenReturn(0.5);
        when(meterRegistry.find("process.cpu.usage").gauge()).thenReturn(cpuGauge);

        Counter httpCounter = mock(Counter.class);
        when(httpCounter.count()).thenReturn(10.0);
        when(meterRegistry.find("http.server.requests").counter()).thenReturn(httpCounter);

        Timer httpTimer = mock(Timer.class);
        when(httpTimer.max(any())).thenReturn(123.0);
        when(meterRegistry.find("http.server.requests").timer()).thenReturn(httpTimer);

        Gauge dbGauge = mock(Gauge.class);
        when(dbGauge.value()).thenReturn(5.0);
        when(meterRegistry.find("db.connection.pool.size").gauge()).thenReturn(dbGauge);

        ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(ingestionService,
                meterRegistry);
        ReflectionTestUtils.setField(service, "applicationName", "app");
        ReflectionTestUtils.setField(service, "metricsEnabled", true);

        service.collectAndPushMetrics();

        verify(ingestionService, atLeastOnce()).indexMetrics(anyString(), anyDouble(), anyString(), anyString(),
                anyString(), anyString());
    }

    @Test
    @DisplayName("Should skip collection when metrics disabled")
    void testCollectAndPushMetricsDisabled() {
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(ingestionService,
                meterRegistry);
        ReflectionTestUtils.setField(service, "metricsEnabled", false);

        service.collectAndPushMetrics();

        verifyNoInteractions(ingestionService);
    }

    @Test
    @DisplayName("Should handle missing meters without errors")
    void testCollectAndPushMetricsWithMissingMeters() {
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class, Mockito.RETURNS_DEEP_STUBS);

        when(meterRegistry.find(anyString()).gauge()).thenReturn(null);
        when(meterRegistry.find(anyString()).counter()).thenReturn(null);
        when(meterRegistry.find(anyString()).timer()).thenReturn(null);

        ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(ingestionService,
                meterRegistry);
        ReflectionTestUtils.setField(service, "applicationName", "app");
        ReflectionTestUtils.setField(service, "metricsEnabled", true);

        service.collectAndPushMetrics();

        verify(ingestionService, atLeast(2)).indexMetrics(anyString(), anyDouble(), anyString(), anyString(),
                anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle ingestion errors gracefully")
    void testCollectAndPushMetricsHandlesIngestionException() {
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class, Mockito.RETURNS_DEEP_STUBS);

        doThrow(new RuntimeException("ingest failed")).when(ingestionService)
                .indexMetrics(anyString(), anyDouble(), anyString(), anyString(), anyString(), anyString());

        ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(ingestionService,
                meterRegistry);
        ReflectionTestUtils.setField(service, "applicationName", "app");
        ReflectionTestUtils.setField(service, "metricsEnabled", true);

        service.collectAndPushMetrics();
    }
}

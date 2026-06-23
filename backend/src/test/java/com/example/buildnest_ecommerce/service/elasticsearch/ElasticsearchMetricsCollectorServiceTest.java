package com.example.buildnest_ecommerce.service.elasticsearch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

                ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(
                                ingestionService,
                                meterRegistry);
                ReflectionTestUtils.setField(service, "applicationName", "app");
                ReflectionTestUtils.setField(service, "metricsEnabled", true);

                service.collectAndPushMetrics();

                verify(ingestionService, atLeastOnce()).indexMetrics(anyString(), anyDouble(), anyString(), anyString(),
                                anyString(), anyString());
        }

        @Test
        @DisplayName("Should record CPU, HTTP, and DB metrics with expected values")
        void testCollectAndPushMetricsValues() {
                ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
                MeterRegistry meterRegistry = mock(MeterRegistry.class, Mockito.RETURNS_DEEP_STUBS);

                Gauge cpuGauge = mock(Gauge.class);
                when(cpuGauge.value()).thenReturn(0.25);
                when(meterRegistry.find("process.cpu.usage").gauge()).thenReturn(cpuGauge);

                Counter httpCounter = mock(Counter.class);
                when(httpCounter.count()).thenReturn(42.0);
                when(meterRegistry.find("http.server.requests").counter()).thenReturn(httpCounter);

                Timer httpTimer = mock(Timer.class);
                when(httpTimer.max(any())).thenReturn(250.0);
                when(meterRegistry.find("http.server.requests").timer()).thenReturn(httpTimer);

                Gauge dbPoolGauge = mock(Gauge.class);
                when(dbPoolGauge.value()).thenReturn(7.0);
                when(meterRegistry.find("db.connection.pool.size").gauge()).thenReturn(dbPoolGauge);

                Gauge dbActiveGauge = mock(Gauge.class);
                when(dbActiveGauge.value()).thenReturn(3.0);
                when(meterRegistry.find("db.connection.active").gauge()).thenReturn(dbActiveGauge);

                Gauge dbIdleGauge = mock(Gauge.class);
                when(dbIdleGauge.value()).thenReturn(4.0);
                when(meterRegistry.find("db.connection.idle").gauge()).thenReturn(dbIdleGauge);

                ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(
                                ingestionService,
                                meterRegistry);
                ReflectionTestUtils.setField(service, "applicationName", "app");
                ReflectionTestUtils.setField(service, "metricsEnabled", true);

                service.collectAndPushMetrics();

                var nameCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
                var valueCaptor = org.mockito.ArgumentCaptor.forClass(Double.class);
                verify(ingestionService, atLeastOnce()).indexMetrics(nameCaptor.capture(), valueCaptor.capture(),
                                anyString(), anyString(), anyString(), anyString());

                List<String> metricNames = nameCaptor.getAllValues();
                List<Double> metricValues = valueCaptor.getAllValues();

                assertTrue(containsMetric(metricNames, metricValues, "processCpuUsagePercent", 25.0));
                assertTrue(containsMetric(metricNames, metricValues, "httpResponseTimeMs", 250.0));
                assertTrue(containsMetric(metricNames, metricValues, "dbConnectionPoolSize", 7.0));
                assertTrue(containsMetric(metricNames, metricValues, "dbActiveConnections", 3.0));
                assertTrue(containsMetric(metricNames, metricValues, "dbIdleConnections", 4.0));
        }

        @Test
        @DisplayName("Should skip collection when metrics disabled")
        void testCollectAndPushMetricsDisabled() {
                ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
                MeterRegistry meterRegistry = mock(MeterRegistry.class);

                ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(
                                ingestionService,
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

                ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(
                                ingestionService,
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
                                .indexMetrics(anyString(), anyDouble(), anyString(), anyString(), anyString(),
                                                anyString());

                ElasticsearchMetricsCollectorService service = new ElasticsearchMetricsCollectorService(
                                ingestionService,
                                meterRegistry);
                ReflectionTestUtils.setField(service, "applicationName", "app");
                ReflectionTestUtils.setField(service, "metricsEnabled", true);

                service.collectAndPushMetrics();
        }

        private boolean containsMetric(List<String> names, List<Double> values, String expectedName,
                        double expectedValue) {
                for (int i = 0; i < names.size(); i++) {
                        if (expectedName.equals(names.get(i)) && Double.compare(values.get(i), expectedValue) == 0) {
                                return true;
                        }
                }
                return false;
        }
}

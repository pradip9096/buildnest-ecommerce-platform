package com.example.buildnest_ecommerce.monitoring;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonitoringInitializerTest {

    @Test
    void initializesMetricsOnStartup() {
        BusinessMetricsService metricsService = mock(BusinessMetricsService.class);
        MonitoringInitializer initializer = new MonitoringInitializer();
        ReflectionTestUtils.setField(initializer, "businessMetricsService", metricsService);

        initializer.initializeMonitoring();

        verify(metricsService).initializeMetrics();
    }

    @Test
    void throwsWhenInitializationFails() {
        BusinessMetricsService metricsService = mock(BusinessMetricsService.class);
        doThrow(new RuntimeException("fail")).when(metricsService).initializeMetrics();

        MonitoringInitializer initializer = new MonitoringInitializer();
        ReflectionTestUtils.setField(initializer, "businessMetricsService", metricsService);

        assertThrows(RuntimeException.class, initializer::initializeMonitoring);
    }
}

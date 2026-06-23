package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.service.monitoring.PerformanceMonitoringService;
import com.example.buildnest_ecommerce.service.monitoring.UptimeMonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonitoringControllerTest {

    @Test
    void performanceAndUptimeEndpointsReturnOk() {
        PerformanceMonitoringService performanceService = mock(PerformanceMonitoringService.class);
        UptimeMonitoringService uptimeService = mock(UptimeMonitoringService.class);

        when(performanceService.getPerformanceMetrics()).thenReturn(Map.of("p95ResponseTimeMs", 100));
        when(performanceService.isSLACompliant()).thenReturn(true);
        when(performanceService.getSlowQueryRatio()).thenReturn(1.0);
        when(uptimeService.getUptimeMetrics()).thenReturn(Map.of("slaCompliance", "PASS", "uptimePercentage", 99.9));
        when(uptimeService.getFormattedUptime()).thenReturn("1h");
        when(uptimeService.getCurrentHealthStatus()).thenReturn("HEALTHY");

        MonitoringController controller = new MonitoringController(performanceService, uptimeService);

        assertEquals(HttpStatus.OK, controller.getPerformanceMetrics().getStatusCode());
        assertEquals(HttpStatus.OK, controller.checkSLACompliance().getStatusCode());
        assertEquals(HttpStatus.OK, controller.resetPerformanceMetrics().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getUptimeMetrics().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getFormattedUptime().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getHealthStatus().getStatusCode());
        assertEquals(HttpStatus.OK, controller.checkOverallSLACompliance().getStatusCode());
        assertEquals(HttpStatus.OK, controller.resetUptimeStatistics().getStatusCode());
    }

    @Test
    void handlesErrors() {
        PerformanceMonitoringService performanceService = mock(PerformanceMonitoringService.class);
        UptimeMonitoringService uptimeService = mock(UptimeMonitoringService.class);

        when(performanceService.getPerformanceMetrics()).thenThrow(new RuntimeException("fail"));
        when(uptimeService.getUptimeMetrics()).thenThrow(new RuntimeException("fail"));
        when(uptimeService.getCurrentHealthStatus()).thenThrow(new RuntimeException("fail"));

        MonitoringController controller = new MonitoringController(performanceService, uptimeService);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getPerformanceMetrics().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getUptimeMetrics().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getHealthStatus().getStatusCode());
    }

    @Test
    void handlesSlaAndResetErrors() {
        PerformanceMonitoringService performanceService = mock(PerformanceMonitoringService.class);
        UptimeMonitoringService uptimeService = mock(UptimeMonitoringService.class);

        when(performanceService.isSLACompliant()).thenThrow(new RuntimeException("fail"));
        doThrow(new RuntimeException("fail")).when(performanceService).resetMetrics();
        when(uptimeService.getFormattedUptime()).thenThrow(new RuntimeException("fail"));
        doThrow(new RuntimeException("fail")).when(uptimeService).resetUptimeStatistics();

        MonitoringController controller = new MonitoringController(performanceService, uptimeService);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.checkSLACompliance().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.resetPerformanceMetrics().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getFormattedUptime().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.resetUptimeStatistics().getStatusCode());
    }

    @Test
    void overallSlaComplianceHandlesFailure() {
        PerformanceMonitoringService performanceService = mock(PerformanceMonitoringService.class);
        UptimeMonitoringService uptimeService = mock(UptimeMonitoringService.class);

        when(performanceService.getPerformanceMetrics()).thenReturn(Map.of("p95ResponseTimeMs", 900));
        when(performanceService.isSLACompliant()).thenReturn(false);
        when(uptimeService.getUptimeMetrics()).thenReturn(Map.of("slaCompliance", "FAIL", "uptimePercentage", 90.0));

        MonitoringController controller = new MonitoringController(performanceService, uptimeService);
        assertEquals(HttpStatus.OK, controller.checkOverallSLACompliance().getStatusCode());
    }

    @Test
    void overallSlaComplianceHandlesErrors() {
        PerformanceMonitoringService performanceService = mock(PerformanceMonitoringService.class);
        UptimeMonitoringService uptimeService = mock(UptimeMonitoringService.class);

        when(performanceService.getPerformanceMetrics()).thenThrow(new RuntimeException("fail"));

        MonitoringController controller = new MonitoringController(performanceService, uptimeService);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.checkOverallSLACompliance().getStatusCode());
    }

    @Test
    void checkSlaComplianceFailureStatus() {
        PerformanceMonitoringService performanceService = mock(PerformanceMonitoringService.class);
        UptimeMonitoringService uptimeService = mock(UptimeMonitoringService.class);

        when(performanceService.isSLACompliant()).thenReturn(false);
        when(performanceService.getSlowQueryRatio()).thenReturn(12.5);

        MonitoringController controller = new MonitoringController(performanceService, uptimeService);
        assertEquals(HttpStatus.OK, controller.checkSLACompliance().getStatusCode());
    }

    @Test
    void overallSlaComplianceHandlesNullUptimeCompliance() {
        PerformanceMonitoringService performanceService = mock(PerformanceMonitoringService.class);
        UptimeMonitoringService uptimeService = mock(UptimeMonitoringService.class);

        when(performanceService.getPerformanceMetrics()).thenReturn(Map.of("p95ResponseTimeMs", 120));
        when(performanceService.isSLACompliant()).thenReturn(true);
        when(uptimeService.getUptimeMetrics()).thenReturn(Map.of("uptimePercentage", 99.9));

        MonitoringController controller = new MonitoringController(performanceService, uptimeService);
        assertEquals(HttpStatus.OK, controller.checkOverallSLACompliance().getStatusCode());
    }
}

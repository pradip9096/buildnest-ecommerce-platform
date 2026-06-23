package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.service.admin.AdminAnalyticsService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchAlertingService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAnalyticsControllerTest {

    @Test
    void auditAndMetricsEndpointsReturnOk() {
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        ElasticsearchAlertingService alertingService = mock(ElasticsearchAlertingService.class);
        AdminAnalyticsService analyticsService = mock(AdminAnalyticsService.class);

        when(ingestionService.getAuditLogsByUser(1L))
                .thenReturn(Collections.singletonList(new ElasticsearchAuditLog()));
        when(ingestionService.getAuditLogsByAction("LOGIN"))
                .thenReturn(Collections.singletonList(new ElasticsearchAuditLog()));
        when(ingestionService.getAuditLogsByTimeRange(any(), any()))
                .thenReturn(Collections.singletonList(new ElasticsearchAuditLog()));
        when(ingestionService.getMetricsByTimeRange(any(), any()))
                .thenReturn(Collections.singletonList(new ElasticsearchMetrics()));
        when(ingestionService.getRecentMetrics(5)).thenReturn(Collections.singletonList(new ElasticsearchMetrics()));
        when(alertingService.getAlertSummary()).thenReturn(Map.of("total", 1));
        when(analyticsService.getApiErrorsByStatusCode(null, null)).thenReturn(Map.of("500", 2));
        when(analyticsService.getApiErrorsByEndpoint(null, null)).thenReturn(Map.of("/api", 3));

        AdminAnalyticsController controller = new AdminAnalyticsController(ingestionService, alertingService,
                analyticsService);

        assertEquals(HttpStatus.OK, controller.getAuditLogsByUser(1L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAuditLogsByAction("LOGIN").getStatusCode());
        assertEquals(HttpStatus.OK, controller
                .getAuditLogsByTimeRange(LocalDateTime.now().minusHours(1), LocalDateTime.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller
                .getMetricsByTimeRange(LocalDateTime.now().minusHours(1), LocalDateTime.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getRecentMetrics(5).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAlertSummary().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDashboard().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getApiErrorsByStatusCode(null, null).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getApiErrorsByEndpoint(null, null).getStatusCode());
    }

    @Test
    void auditEndpointsHandleErrors() {
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        ElasticsearchAlertingService alertingService = mock(ElasticsearchAlertingService.class);
        AdminAnalyticsService analyticsService = mock(AdminAnalyticsService.class);

        when(ingestionService.getAuditLogsByUser(1L)).thenThrow(new RuntimeException("fail"));
        when(ingestionService.getMetricsByTimeRange(any(), any())).thenThrow(new RuntimeException("fail"));
        when(alertingService.getAlertSummary()).thenThrow(new RuntimeException("fail"));
        when(analyticsService.getApiErrorsByStatusCode(any(), any())).thenThrow(new RuntimeException("fail"));

        AdminAnalyticsController controller = new AdminAnalyticsController(ingestionService, alertingService,
                analyticsService);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAuditLogsByUser(1L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller
                .getMetricsByTimeRange(LocalDateTime.now().minusHours(1), LocalDateTime.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAlertSummary().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getApiErrorsByStatusCode(null, null).getStatusCode());
    }
}

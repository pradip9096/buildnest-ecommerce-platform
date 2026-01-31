package com.example.buildnest_ecommerce.service.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElasticsearchIngestionService Tests")
class ElasticsearchIngestionServiceTest {

    @Mock
    private ElasticsearchAuditLogRepository auditLogRepository;

    @Mock
    private ElasticsearchMetricsRepository metricsRepository;

    @Captor
    private ArgumentCaptor<ElasticsearchAuditLog> auditLogCaptor;

    @Captor
    private ArgumentCaptor<ElasticsearchMetrics> metricsCaptor;

    private ElasticsearchIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new ElasticsearchIngestionService(auditLogRepository, metricsRepository);
    }

    @Test
    @DisplayName("Should map 2xx status to SUCCESS category")
    void testIndexAuditLogWithStatusSuccessCategory() {
        ingestionService.indexAuditLogWithStatus(10L, "USER_LOGIN", "AUTH", 99L,
                "127.0.0.1", "agent", "old", "new", 201, "/api/login", null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        ElasticsearchAuditLog saved = auditLogCaptor.getValue();

        assertEquals(10L, saved.getUserId());
        assertEquals("USER_LOGIN", saved.getAction());
        assertEquals("AUTH", saved.getEntityType());
        assertEquals(99L, saved.getEntityId());
        assertEquals("SUCCESS", saved.getErrorCategory());
        assertEquals(201, saved.getHttpStatusCode());
        assertEquals("/api/login", saved.getEndpoint());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    @DisplayName("Should map 4xx status to CLIENT_ERROR and set CRITICAL severity for DELETE")
    void testIndexAuditLogWithStatusClientErrorCategory() {
        ingestionService.indexAuditLogWithStatus(20L, "DELETE_USER", "USER", 77L,
                "10.0.0.1", "agent", null, null, 404, "/api/users/77", null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        ElasticsearchAuditLog saved = auditLogCaptor.getValue();

        assertEquals("CLIENT_ERROR", saved.getErrorCategory());
        assertEquals("CRITICAL", saved.getSeverity());
        assertEquals(404, saved.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should preserve explicit error category when provided")
    void testIndexAuditLogWithStatusRespectsProvidedCategory() {
        ingestionService.indexAuditLogWithStatus(30L, "ORDER_UPDATE", "ORDER", 55L,
                "10.0.0.2", "agent", null, null, 500, "/api/orders/55", "CUSTOM_CATEGORY");

        verify(auditLogRepository).save(auditLogCaptor.capture());
        ElasticsearchAuditLog saved = auditLogCaptor.getValue();

        assertEquals("CUSTOM_CATEGORY", saved.getErrorCategory());
        assertEquals(500, saved.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should index metrics in Elasticsearch")
    void testIndexMetrics() {
        ingestionService.indexMetrics("cpu.usage", 75.5, "%", "order-service", "host-1", "test");

        verify(metricsRepository).save(metricsCaptor.capture());
        ElasticsearchMetrics saved = metricsCaptor.getValue();

        assertEquals("cpu.usage", saved.getMetricName());
        assertEquals(75.5, saved.getValue());
        assertEquals("%", saved.getUnit());
        assertEquals("order-service", saved.getService());
        assertEquals("host-1", saved.getHost());
        assertEquals("test", saved.getEnvironment());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    @DisplayName("Should query audit logs by status code")
    void testGetErrorsByHttpStatusCode() {
        when(auditLogRepository.findByHttpStatusCode(500)).thenReturn(List.of());

        List<ElasticsearchAuditLog> result = ingestionService.getErrorsByHttpStatusCode(500);

        assertNotNull(result);
        verify(auditLogRepository).findByHttpStatusCode(500);
    }

    @Test
    @DisplayName("Should query errors by category")
    void testGetErrorsByCategory() {
        when(auditLogRepository.findByErrorCategory("SERVER_ERROR")).thenReturn(List.of());

        List<ElasticsearchAuditLog> result = ingestionService.getErrorsByCategory("SERVER_ERROR");

        assertNotNull(result);
        verify(auditLogRepository).findByErrorCategory("SERVER_ERROR");
    }

    @Test
    @DisplayName("Should query errors by status code and time range")
    void testGetErrorsByStatusCodeAndTimeRange() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        when(auditLogRepository.findByHttpStatusCodeAndTimestampBetween(eq(400), eq(start), eq(end)))
                .thenReturn(List.of());

        List<ElasticsearchAuditLog> result = ingestionService.getErrorsByStatusCodeAndTimeRange(400, start, end);

        assertNotNull(result);
        verify(auditLogRepository).findByHttpStatusCodeAndTimestampBetween(400, start, end);
    }

    @Test
    @DisplayName("Should query audit logs by user and action")
    void testGetAuditLogsByUserAndAction() {
        when(auditLogRepository.findByUserId(42L)).thenReturn(List.of());
        when(auditLogRepository.findByAction("USER_LOGIN")).thenReturn(List.of());

        assertNotNull(ingestionService.getAuditLogsByUser(42L));
        assertNotNull(ingestionService.getAuditLogsByAction("USER_LOGIN"));

        verify(auditLogRepository).findByUserId(42L);
        verify(auditLogRepository).findByAction("USER_LOGIN");
    }

    @Test
    @DisplayName("Should query audit logs and metrics by time range")
    void testTimeRangeQueries() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        when(auditLogRepository.findByTimestampBetween(eq(start), eq(end))).thenReturn(List.of());
        when(metricsRepository.findByTimestampBetween(eq(start), eq(end))).thenReturn(List.of());

        assertNotNull(ingestionService.getAuditLogsByTimeRange(start, end));
        assertNotNull(ingestionService.getMetricsByTimeRange(start, end));

        verify(auditLogRepository).findByTimestampBetween(start, end);
        verify(metricsRepository).findByTimestampBetween(start, end);
    }

    @Test
    @DisplayName("Should query recent metrics")
    void testGetRecentMetrics() {
        when(metricsRepository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of());

        List<ElasticsearchMetrics> result = ingestionService.getRecentMetrics(30);

        assertNotNull(result);
        verify(metricsRepository).findByTimestampAfter(any(LocalDateTime.class));
    }
}

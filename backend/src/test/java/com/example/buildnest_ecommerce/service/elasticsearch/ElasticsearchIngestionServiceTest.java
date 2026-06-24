package com.example.buildnest_ecommerce.service.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchMetrics;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
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
    private CircuitBreaker closedCircuitBreaker;

    @BeforeEach
    void setUp() {
        closedCircuitBreaker = CircuitBreaker.ofDefaults("elasticsearch-circuit-breaker-test");
        ingestionService = new ElasticsearchIngestionService(auditLogRepository, metricsRepository, closedCircuitBreaker);
    }

    private ElasticsearchIngestionService withOpenCircuitBreaker() {
        CircuitBreaker openCb = CircuitBreaker.of("open-cb-test",
                CircuitBreakerConfig.custom()
                        .minimumNumberOfCalls(1)
                        .failureRateThreshold(1)
                        .build());
        openCb.transitionToOpenState();
        return new ElasticsearchIngestionService(auditLogRepository, metricsRepository, openCb);
    }

    private ElasticsearchAuditLog sampleAuditLog() {
        return ElasticsearchAuditLog.builder()
                .id("test-id")
                .userId(1L)
                .action("TEST_ACTION")
                .entityType("TEST")
                .entityId(1L)
                .timestamp(LocalDateTime.now())
                .severity("INFO")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .endpoint("/api/test")
                .build();
    }

    private ElasticsearchMetrics sampleMetric() {
        return ElasticsearchMetrics.builder()
                .id("metric-id")
                .metricName("cpu.usage")
                .value(50.0)
                .unit("%")
                .service("test-service")
                .host("host-1")
                .environment("test")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ===== indexAuditLogWithStatus — HTTP status category mapping =====

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
    @DisplayName("Should map boundary 200 to SUCCESS")
    void testIndexAuditLogStatusBoundary200() {
        ingestionService.indexAuditLogWithStatus(1L, "LOGIN", "AUTH", 1L,
                "127.0.0.1", "agent", null, null, 200, "/api/login", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("SUCCESS", auditLogCaptor.getValue().getErrorCategory());
    }

    @Test
    @DisplayName("Should map 199 to null category (below 200 boundary)")
    void testIndexAuditLogStatusBoundary199() {
        ingestionService.indexAuditLogWithStatus(1L, "LOGIN", "AUTH", 1L,
                "127.0.0.1", "agent", null, null, 199, "/api/login", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertNull(auditLogCaptor.getValue().getErrorCategory());
    }

    @Test
    @DisplayName("Should map boundary 300 to REDIRECT")
    void testIndexAuditLogStatusBoundary300() {
        ingestionService.indexAuditLogWithStatus(1L, "ACCESS", "RESOURCE", 1L,
                "127.0.0.1", "agent", null, null, 300, "/api/resource", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("REDIRECT", auditLogCaptor.getValue().getErrorCategory());
    }

    @Test
    @DisplayName("Should map 299 to SUCCESS (below 300 boundary)")
    void testIndexAuditLogStatusBoundary299() {
        ingestionService.indexAuditLogWithStatus(1L, "LOGIN", "AUTH", 1L,
                "127.0.0.1", "agent", null, null, 299, "/api/login", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("SUCCESS", auditLogCaptor.getValue().getErrorCategory());
    }

    @Test
    @DisplayName("Should map boundary 400 to CLIENT_ERROR")
    void testIndexAuditLogStatusBoundary400() {
        ingestionService.indexAuditLogWithStatus(1L, "LOGIN", "AUTH", 1L,
                "127.0.0.1", "agent", null, null, 400, "/api/login", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("CLIENT_ERROR", auditLogCaptor.getValue().getErrorCategory());
    }

    @Test
    @DisplayName("Should map 399 to REDIRECT (below 400 boundary)")
    void testIndexAuditLogStatusBoundary399() {
        ingestionService.indexAuditLogWithStatus(1L, "LOGIN", "AUTH", 1L,
                "127.0.0.1", "agent", null, null, 399, "/api/login", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("REDIRECT", auditLogCaptor.getValue().getErrorCategory());
    }

    @Test
    @DisplayName("Should map boundary 500 to SERVER_ERROR")
    void testIndexAuditLogStatusBoundary500() {
        ingestionService.indexAuditLogWithStatus(1L, "ORDER_UPDATE", "ORDER", 1L,
                "127.0.0.1", "agent", null, null, 500, "/api/orders", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("SERVER_ERROR", auditLogCaptor.getValue().getErrorCategory());
    }

    @Test
    @DisplayName("Should map 499 to CLIENT_ERROR (below 500 boundary)")
    void testIndexAuditLogStatusBoundary499() {
        ingestionService.indexAuditLogWithStatus(1L, "LOGIN", "AUTH", 1L,
                "127.0.0.1", "agent", null, null, 499, "/api/login", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("CLIENT_ERROR", auditLogCaptor.getValue().getErrorCategory());
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
    @DisplayName("Should map 3xx status to REDIRECT category")
    void testIndexAuditLogWithStatusRedirectCategory() {
        ingestionService.indexAuditLogWithStatus(21L, "ACCESS_RESOURCE", "RESOURCE", 12L,
                "10.0.0.1", "agent", null, null, 302, "/api/resource", null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        ElasticsearchAuditLog saved = auditLogCaptor.getValue();

        assertEquals("REDIRECT", saved.getErrorCategory());
        assertEquals("INFO", saved.getSeverity());
        assertEquals(302, saved.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should map 5xx status to SERVER_ERROR category")
    void testIndexAuditLogWithStatusServerErrorCategory() {
        ingestionService.indexAuditLogWithStatus(22L, "ORDER_UPDATE", "ORDER", 88L,
                "10.0.0.1", "agent", null, null, 503, "/api/orders/88", null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        ElasticsearchAuditLog saved = auditLogCaptor.getValue();

        assertEquals("SERVER_ERROR", saved.getErrorCategory());
        assertEquals("WARN", saved.getSeverity());
        assertEquals(503, saved.getHttpStatusCode());
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
    @DisplayName("Should allow null status and category")
    void testIndexAuditLogWithNullStatusAndCategory() {
        ingestionService.indexAuditLogWithStatus(31L, "LOGIN", "AUTH", 1L,
                "10.0.0.3", "agent", null, null, null, null, null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        ElasticsearchAuditLog saved = auditLogCaptor.getValue();

        assertNull(saved.getErrorCategory());
        assertNull(saved.getHttpStatusCode());
        assertEquals("INFO", saved.getSeverity());
    }

    @Test
    @DisplayName("Should set WARN severity for UPDATE and RESET actions")
    void testIndexAuditLogWarnSeverity() {
        ingestionService.indexAuditLogWithStatus(32L, "PASSWORD_RESET", "USER", 2L,
                "10.0.0.4", "agent", null, null, 200, "/api/users/2/reset", null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("WARN", auditLogCaptor.getValue().getSeverity());
    }

    @Test
    @DisplayName("Should set CRITICAL severity for REVOKE action")
    void testIndexAuditLogCriticalSeverityForRevoke() {
        ingestionService.indexAuditLogWithStatus(33L, "TOKEN_REVOKE", "TOKEN", 3L,
                "10.0.0.5", "agent", null, null, 200, "/api/token/revoke", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("CRITICAL", auditLogCaptor.getValue().getSeverity());
    }

    @Test
    @DisplayName("Should set INFO severity for LOGIN action")
    void testIndexAuditLogInfoSeverityForLogin() {
        ingestionService.indexAuditLogWithStatus(34L, "USER_LOGIN", "AUTH", 4L,
                "10.0.0.6", "agent", null, null, 200, "/api/auth/login", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("INFO", auditLogCaptor.getValue().getSeverity());
    }

    @Test
    @DisplayName("Should set INFO severity for ACCESS action")
    void testIndexAuditLogInfoSeverityForAccess() {
        ingestionService.indexAuditLogWithStatus(35L, "RESOURCE_ACCESS", "RESOURCE", 5L,
                "10.0.0.7", "agent", null, null, 200, "/api/resource", null);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertEquals("INFO", auditLogCaptor.getValue().getSeverity());
    }

    @Test
    @DisplayName("Should handle audit log repository failures gracefully")
    void testIndexAuditLogHandlesRepositoryException() {
        doThrow(new RuntimeException("save failed")).when(auditLogRepository).save(any(ElasticsearchAuditLog.class));

        assertDoesNotThrow(() -> ingestionService.indexAuditLogWithStatus(40L, "LOGIN", "AUTH", 3L,
                "10.0.0.5", "agent", null, null, 200, "/api/login", null));
    }

    @Test
    @DisplayName("Should skip audit log write when circuit breaker is OPEN")
    void testIndexAuditLogSkipsWhenCircuitBreakerOpen() {
        ElasticsearchIngestionService openService = withOpenCircuitBreaker();

        assertDoesNotThrow(() -> openService.indexAuditLogWithStatus(50L, "LOGIN", "AUTH", 1L,
                "127.0.0.1", "agent", null, null, 200, "/api/login", null));

        verifyNoInteractions(auditLogRepository);
    }

    // ===== indexMetrics =====

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
    @DisplayName("Should handle metrics repository failures gracefully")
    void testIndexMetricsHandlesRepositoryException() {
        doThrow(new RuntimeException("metrics save failed")).when(metricsRepository)
                .save(any(ElasticsearchMetrics.class));

        assertDoesNotThrow(() -> ingestionService.indexMetrics("mem", 10.0, "MB", "svc", "host", "test"));
    }

    @Test
    @DisplayName("Should skip metrics write when circuit breaker is OPEN")
    void testIndexMetricsSkipsWhenCircuitBreakerOpen() {
        ElasticsearchIngestionService openService = withOpenCircuitBreaker();

        assertDoesNotThrow(() -> openService.indexMetrics("cpu", 90.0, "%", "svc", "host", "test"));

        verifyNoInteractions(metricsRepository);
    }

    // ===== Read operations — happy path returns non-empty, CB-OPEN returns empty =====

    @Test
    @DisplayName("Should query audit logs by status code and return repository result")
    void testGetErrorsByHttpStatusCode() {
        ElasticsearchAuditLog log = sampleAuditLog();
        when(auditLogRepository.findByHttpStatusCode(500)).thenReturn(List.of(log));

        List<ElasticsearchAuditLog> result = ingestionService.getErrorsByHttpStatusCode(500);

        assertEquals(1, result.size());
        assertSame(log, result.get(0));
        verify(auditLogRepository).findByHttpStatusCode(500);
    }

    @Test
    @DisplayName("Should return empty list for getErrorsByHttpStatusCode when circuit breaker OPEN")
    void testGetErrorsByHttpStatusCodeCircuitBreakerOpen() {
        List<ElasticsearchAuditLog> result = withOpenCircuitBreaker().getErrorsByHttpStatusCode(500);
        assertTrue(result.isEmpty());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    @DisplayName("Should query errors by category and return repository result")
    void testGetErrorsByCategory() {
        ElasticsearchAuditLog log = sampleAuditLog();
        when(auditLogRepository.findByErrorCategory("SERVER_ERROR")).thenReturn(List.of(log));

        List<ElasticsearchAuditLog> result = ingestionService.getErrorsByCategory("SERVER_ERROR");

        assertEquals(1, result.size());
        assertSame(log, result.get(0));
        verify(auditLogRepository).findByErrorCategory("SERVER_ERROR");
    }

    @Test
    @DisplayName("Should return empty list for getErrorsByCategory when circuit breaker OPEN")
    void testGetErrorsByCategoryCircuitBreakerOpen() {
        List<ElasticsearchAuditLog> result = withOpenCircuitBreaker().getErrorsByCategory("SERVER_ERROR");
        assertTrue(result.isEmpty());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    @DisplayName("Should query errors by status code and time range and return repository result")
    void testGetErrorsByStatusCodeAndTimeRange() {
        ElasticsearchAuditLog log = sampleAuditLog();
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        when(auditLogRepository.findByHttpStatusCodeAndTimestampBetween(eq(400), eq(start), eq(end)))
                .thenReturn(List.of(log));

        List<ElasticsearchAuditLog> result = ingestionService.getErrorsByStatusCodeAndTimeRange(400, start, end);

        assertEquals(1, result.size());
        assertSame(log, result.get(0));
        verify(auditLogRepository).findByHttpStatusCodeAndTimestampBetween(400, start, end);
    }

    @Test
    @DisplayName("Should return empty list for getErrorsByStatusCodeAndTimeRange when circuit breaker OPEN")
    void testGetErrorsByStatusCodeAndTimeRangeCircuitBreakerOpen() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        List<ElasticsearchAuditLog> result = withOpenCircuitBreaker()
                .getErrorsByStatusCodeAndTimeRange(400, start, end);
        assertTrue(result.isEmpty());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    @DisplayName("Should query audit logs by user and return repository result")
    void testGetAuditLogsByUser() {
        ElasticsearchAuditLog log = sampleAuditLog();
        when(auditLogRepository.findByUserId(42L)).thenReturn(List.of(log));

        List<ElasticsearchAuditLog> result = ingestionService.getAuditLogsByUser(42L);

        assertEquals(1, result.size());
        assertSame(log, result.get(0));
        verify(auditLogRepository).findByUserId(42L);
    }

    @Test
    @DisplayName("Should return empty list for getAuditLogsByUser when circuit breaker OPEN")
    void testGetAuditLogsByUserCircuitBreakerOpen() {
        List<ElasticsearchAuditLog> result = withOpenCircuitBreaker().getAuditLogsByUser(42L);
        assertTrue(result.isEmpty());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    @DisplayName("Should query audit logs by action and return repository result")
    void testGetAuditLogsByAction() {
        ElasticsearchAuditLog log = sampleAuditLog();
        when(auditLogRepository.findByAction("USER_LOGIN")).thenReturn(List.of(log));

        List<ElasticsearchAuditLog> result = ingestionService.getAuditLogsByAction("USER_LOGIN");

        assertEquals(1, result.size());
        assertSame(log, result.get(0));
        verify(auditLogRepository).findByAction("USER_LOGIN");
    }

    @Test
    @DisplayName("Should return empty list for getAuditLogsByAction when circuit breaker OPEN")
    void testGetAuditLogsByActionCircuitBreakerOpen() {
        List<ElasticsearchAuditLog> result = withOpenCircuitBreaker().getAuditLogsByAction("USER_LOGIN");
        assertTrue(result.isEmpty());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    @DisplayName("Should query audit logs by time range and return repository result")
    void testGetAuditLogsByTimeRange() {
        ElasticsearchAuditLog log = sampleAuditLog();
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        when(auditLogRepository.findByTimestampBetween(eq(start), eq(end))).thenReturn(List.of(log));

        List<ElasticsearchAuditLog> result = ingestionService.getAuditLogsByTimeRange(start, end);

        assertEquals(1, result.size());
        assertSame(log, result.get(0));
        verify(auditLogRepository).findByTimestampBetween(start, end);
    }

    @Test
    @DisplayName("Should return empty list for getAuditLogsByTimeRange when circuit breaker OPEN")
    void testGetAuditLogsByTimeRangeCircuitBreakerOpen() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        List<ElasticsearchAuditLog> result = withOpenCircuitBreaker().getAuditLogsByTimeRange(start, end);
        assertTrue(result.isEmpty());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    @DisplayName("Should query metrics by time range and return repository result")
    void testGetMetricsByTimeRange() {
        ElasticsearchMetrics metric = sampleMetric();
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        when(metricsRepository.findByTimestampBetween(eq(start), eq(end))).thenReturn(List.of(metric));

        List<ElasticsearchMetrics> result = ingestionService.getMetricsByTimeRange(start, end);

        assertEquals(1, result.size());
        assertSame(metric, result.get(0));
        verify(metricsRepository).findByTimestampBetween(start, end);
    }

    @Test
    @DisplayName("Should return empty list for getMetricsByTimeRange when circuit breaker OPEN")
    void testGetMetricsByTimeRangeCircuitBreakerOpen() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        List<ElasticsearchMetrics> result = withOpenCircuitBreaker().getMetricsByTimeRange(start, end);
        assertTrue(result.isEmpty());
        verifyNoInteractions(metricsRepository);
    }

    @Test
    @DisplayName("Should query recent metrics and return repository result")
    void testGetRecentMetrics() {
        ElasticsearchMetrics metric = sampleMetric();
        when(metricsRepository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(metric));

        List<ElasticsearchMetrics> result = ingestionService.getRecentMetrics(30);

        assertEquals(1, result.size());
        assertSame(metric, result.get(0));
        verify(metricsRepository).findByTimestampAfter(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return empty list for getRecentMetrics when circuit breaker OPEN")
    void testGetRecentMetricsCircuitBreakerOpen() {
        List<ElasticsearchMetrics> result = withOpenCircuitBreaker().getRecentMetrics(30);
        assertTrue(result.isEmpty());
        verifyNoInteractions(metricsRepository);
    }
}

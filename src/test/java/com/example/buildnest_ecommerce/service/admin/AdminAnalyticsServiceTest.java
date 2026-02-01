package com.example.buildnest_ecommerce.service.admin;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAnalyticsServiceTest {

    private ElasticsearchAuditLogRepository repository;
    private AdminAnalyticsService service;

    @BeforeEach
    void setUp() {
        repository = mock(ElasticsearchAuditLogRepository.class);
        service = new AdminAnalyticsService(repository);
    }

    private ElasticsearchAuditLog createLog(String id, int statusCode, String endpoint, String action) {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();
        log.setId(id);
        log.setHttpStatusCode(statusCode);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        log.setEndpoint(endpoint);
        log.setUserId(123L);
        return log;
    }

    @Test
    void buildsApiErrorStats() {
        ElasticsearchAuditLog log = createLog("1", 500, "/api", "GET");
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of(log));

        Map<String, Object> byStatus = service.getApiErrorsByStatusCode(null, null);
        assertTrue(byStatus.containsKey("statusCodeStatistics"));

        Map<String, Object> byEndpoint = service.getApiErrorsByEndpoint(null, null);
        assertTrue(byEndpoint.containsKey("endpointStatistics"));

        Map<String, Object> dashboard = service.getDashboardData();
        assertTrue(dashboard.containsKey("totalEvents"));

        Map<String, Object> errorRate = service.getApiErrorRate(null, null);
        assertTrue(errorRate.containsKey("errorRate"));

        Map<String, Object> correlation = service.getErrorByCorrelationId("1");
        assertTrue(correlation.containsKey("logs"));
    }

    @Test
    void handlesRepositoryErrors() {
        when(repository.findByTimestampBetween(any(), any())).thenThrow(new RuntimeException("fail"));

        Map<String, Object> response = service.getApiErrorsByStatusCode(null, null);
        assertTrue(response.containsKey("error"));
    }

    @Test
    void testGetApiErrorsByStatusCodeWith4xxErrors() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 404, "/api/notfound", "GET"),
                createLog("2", 400, "/api/bad", "POST"),
                createLog("3", 404, "/api/missing", "GET"),
                createLog("4", 200, "/api/success", "GET"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getApiErrorsByStatusCode(null, null);

        assertNotNull(result);
        assertTrue(result.containsKey("statusCodeStatistics"));
        assertTrue(result.containsKey("totalErrors"));
        assertEquals(3L, result.get("totalErrors"));
    }

    @Test
    void testGetApiErrorsByStatusCodeWith5xxErrors() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 500, "/api/error", "GET"),
                createLog("2", 503, "/api/unavailable", "POST"),
                createLog("3", 500, "/api/error", "GET"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getApiErrorsByStatusCode(null, null);

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.get("statusCodeStatistics");
        assertNotNull(stats);
        assertTrue(stats.containsKey("500"));
        assertTrue(stats.containsKey("503"));
    }

    @Test
    void testGetApiErrorsByEndpointGroupsCorrectly() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 404, "/api/users", "GET"),
                createLog("2", 500, "/api/users", "POST"),
                createLog("3", 404, "/api/products", "GET"),
                createLog("4", 200, "/api/success", "GET"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getApiErrorsByEndpoint(null, null);

        assertNotNull(result);
        assertTrue(result.containsKey("endpointStatistics"));
        assertEquals(3, ((Number) result.get("totalErrors")).intValue());
        assertEquals(2, ((Number) result.get("affectedEndpoints")).intValue());
    }

    @Test
    void testGetApiErrorsByEndpointWithNullEndpoint() {
        ElasticsearchAuditLog log = createLog("1", 404, null, "GET");
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of(log));

        Map<String, Object> result = service.getApiErrorsByEndpoint(null, null);

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stats = (List<Map<String, Object>>) result.get("endpointStatistics");
        assertFalse(stats.isEmpty());
        assertEquals("UNKNOWN", stats.get(0).get("endpoint"));
    }

    @Test
    void testGetDashboardDataWithMixedLogs() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 200, "/api/users", "GET"),
                createLog("2", 404, "/api/missing", "GET"),
                createLog("3", 500, "/api/error", "POST"),
                createLog("4", 200, "/api/products", "GET"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getDashboardData();

        assertNotNull(result);
        assertEquals(4L, result.get("totalEvents"));
        assertEquals(2L, result.get("totalErrors"));
        assertEquals(1L, result.get("criticalErrors"));
        assertTrue(result.containsKey("errorRate"));
        assertTrue(result.containsKey("statusDistribution"));
        assertTrue(result.containsKey("topErrorEndpoints"));
        assertTrue(result.containsKey("errorCategoryDistribution"));
    }

    @Test
    void testGetDashboardDataWithNoErrors() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 200, "/api/users", "GET"),
                createLog("2", 200, "/api/products", "GET"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getDashboardData();

        assertEquals(2L, result.get("totalEvents"));
        assertEquals(0L, result.get("totalErrors"));
        assertEquals(0L, result.get("criticalErrors"));
        assertEquals("0.00%", result.get("errorRate"));
    }

    @Test
    void testGetDashboardDataWithEmptyLogs() {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getDashboardData();

        assertEquals(0L, result.get("totalEvents"));
        assertEquals(0L, result.get("totalErrors"));
        assertEquals("0.00%", result.get("errorRate"));
    }

    @Test
    void testGetApiErrorRateCalculation() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 200, "/api/users", "GET"),
                createLog("2", 404, "/api/missing", "GET"),
                createLog("3", 500, "/api/error", "POST"),
                createLog("4", 200, "/api/products", "GET"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getApiErrorRate(null, null);

        assertEquals(4L, result.get("totalRequests"));
        assertEquals(2L, result.get("errorRequests"));
        assertEquals("50.00%", result.get("errorRate"));
        assertEquals("50.00%", result.get("successRate"));
    }

    @Test
    void testGetApiErrorRateWithNoRequests() {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getApiErrorRate(null, null);

        assertEquals(0L, result.get("totalRequests"));
        assertEquals(0L, result.get("errorRequests"));
        assertEquals("0.00%", result.get("errorRate"));
        assertEquals("0.00%", result.get("successRate"));
    }

    @Test
    void testGetApiErrorRateWithOnlyErrors() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 404, "/api/missing", "GET"),
                createLog("2", 500, "/api/error", "POST"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getApiErrorRate(null, null);

        assertEquals(2L, result.get("totalRequests"));
        assertEquals(2L, result.get("errorRequests"));
        assertEquals("100.00%", result.get("errorRate"));
        assertEquals("0.00%", result.get("successRate"));
    }

    @Test
    void testGetErrorByCorrelationIdFound() {
        List<ElasticsearchAuditLog> logs = new ArrayList<>();
        logs.add(createLog("corr-123", 500, "/api/error", "GET"));
        logs.add(createLog("other-id", 404, "/api/missing", "GET"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getErrorByCorrelationId("corr-123");

        assertNotNull(result);
        assertEquals("corr-123", result.get("correlationId"));
        assertTrue(result.containsKey("logs"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> logsList = (List<Map<String, Object>>) result.get("logs");
        assertFalse(logsList.isEmpty());
    }

    @Test
    void testGetErrorByCorrelationIdNotFound() {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getErrorByCorrelationId("nonexistent");

        assertTrue(result.containsKey("error"));
        assertTrue(((String) result.get("error")).contains("No logs found"));
    }

    @Test
    void testGetErrorByCorrelationIdWithActionMatch() {
        ElasticsearchAuditLog log = createLog("1", 500, "/api/error", "GET");
        log.setAction("Process corr-456");
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of(log));

        Map<String, Object> result = service.getErrorByCorrelationId("corr-456");

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> logsList = (List<Map<String, Object>>) result.get("logs");
        assertFalse(logsList.isEmpty());
    }

    @Test
    void testParseDateWithValidString() {
        String validDate = LocalDateTime.now().toString();
        Map<String, Object> result = service.getApiErrorRate(validDate, validDate);
        assertNotNull(result);
        assertTrue(result.containsKey("startDate"));
    }

    @Test
    void testParseDateWithInvalidString() {
        String invalidDate = "invalid-date";
        Map<String, Object> result = service.getApiErrorRate(invalidDate, invalidDate);
        assertNotNull(result);
        assertTrue(result.containsKey("errorRate"));
    }

    @Test
    void testErrorHandlingInGetApiErrorsByEndpoint() {
        when(repository.findByTimestampBetween(any(), any())).thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = service.getApiErrorsByEndpoint(null, null);

        assertTrue(result.containsKey("error"));
    }

    @Test
    void testErrorHandlingInGetDashboardData() {
        when(repository.findByTimestampBetween(any(), any())).thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = service.getDashboardData();

        assertTrue(result.containsKey("error"));
    }

    @Test
    void testErrorHandlingInGetApiErrorRate() {
        when(repository.findByTimestampBetween(any(), any())).thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = service.getApiErrorRate(null, null);

        assertTrue(result.containsKey("error"));
    }

    @Test
    void testErrorHandlingInGetErrorByCorrelationId() {
        when(repository.findByTimestampBetween(any(), any())).thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = service.getErrorByCorrelationId("test-id");

        assertTrue(result.containsKey("error"));
    }

    @Test
    void testGetApiErrorsByStatusCodeWithNoErrors() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 200, "/api/success", "GET"),
                createLog("2", 201, "/api/created", "POST"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getApiErrorsByStatusCode(null, null);

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.get("statusCodeStatistics");
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
        assertEquals(0L, result.get("totalErrors"));
    }

    @Test
    void testGetApiErrorsByStatusCodeIgnoresNullStatusCode() {
        ElasticsearchAuditLog log = createLog("1", 500, "/api/error", "GET");
        ElasticsearchAuditLog nullStatus = createLog("2", 200, "/api/ok", "GET");
        nullStatus.setHttpStatusCode(null);
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of(log, nullStatus));

        Map<String, Object> result = service.getApiErrorsByStatusCode(null, null);

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.get("statusCodeStatistics");
        assertTrue(stats.containsKey("500"));
        assertEquals(1L, result.get("totalErrors"));
    }

    @Test
    void testGetApiErrorsByEndpointWithNoErrors() {
        List<ElasticsearchAuditLog> logs = List.of(
                createLog("1", 200, "/api/success", "GET"),
                createLog("2", 201, "/api/created", "POST"));
        when(repository.findByTimestampBetween(any(), any())).thenReturn(logs);

        Map<String, Object> result = service.getApiErrorsByEndpoint(null, null);

        assertEquals(0, ((Number) result.get("totalErrors")).intValue());
        assertEquals(0, ((Number) result.get("affectedEndpoints")).intValue());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stats = (List<Map<String, Object>>) result.get("endpointStatistics");
        assertTrue(stats.isEmpty());
    }

    @Test
    void testGetDashboardDataWithNullEndpointError() {
        ElasticsearchAuditLog log = createLog("1", 500, null, "GET");
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of(log));

        Map<String, Object> result = service.getDashboardData();

        assertTrue(result.containsKey("error"));
    }

    @Test
    void testGetErrorCategoryViaReflection() throws Exception {
        var method = AdminAnalyticsService.class.getDeclaredMethod("getErrorCategory", Integer.class);
        method.setAccessible(true);

        assertEquals("UNKNOWN", method.invoke(service, new Object[] { null }));
        assertEquals("SUCCESS", method.invoke(service, 200));
        assertEquals("REDIRECT", method.invoke(service, 302));
        assertEquals("CLIENT_ERROR", method.invoke(service, 404));
        assertEquals("SERVER_ERROR", method.invoke(service, 503));
    }

    @Test
    void testParseDateEmptyStringFallsBackToDefault() {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getApiErrorsByStatusCode("", "");

        assertTrue(result.containsKey("startDate"));
        assertTrue(result.containsKey("endDate"));
    }
}

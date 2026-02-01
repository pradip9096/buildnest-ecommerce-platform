package com.example.buildnest_ecommerce.service.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.ElasticsearchAuditLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ElasticsearchQueryOptimizationService tests")
class ElasticsearchQueryOptimizationServiceTest {

    @Test
    @DisplayName("Should return empty results for optimized search")
    void testSearchAuditLogsOptimized() {
        ElasticsearchQueryOptimizationService service = new ElasticsearchQueryOptimizationService();

        List<ElasticsearchAuditLog> results = service.searchAuditLogsOptimized("1",
                LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    @DisplayName("Should return pageable with constrained size")
    void testGetOptimizedPageable() {
        ElasticsearchQueryOptimizationService service = new ElasticsearchQueryOptimizationService();

        var pageable = service.getOptimizedPageable(0, 500);
        assertEquals(100, pageable.getPageSize());
    }

    @Test
    @DisplayName("Should execute search with timeout wrapper")
    void testSearchWithTimeout() {
        ElasticsearchQueryOptimizationService service = new ElasticsearchQueryOptimizationService();

        List<ElasticsearchAuditLog> results = service.searchWithTimeout("1",
                LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertNotNull(results);
    }

    @Test
    @DisplayName("Should return empty list on timeout")
    void testExecuteWithTimeoutTimeoutPath() throws Exception {
        ElasticsearchQueryOptimizationService service = new ElasticsearchQueryOptimizationService();

        Callable<List<ElasticsearchAuditLog>> slowQuery = () -> {
            Thread.sleep(50);
            return List.of(new ElasticsearchAuditLog());
        };

        List<ElasticsearchAuditLog> results = ReflectionTestUtils.invokeMethod(
                service, "executeWithTimeout", slowQuery, 1L);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list on execution failure")
    void testExecuteWithTimeoutExceptionPath() {
        ElasticsearchQueryOptimizationService service = new ElasticsearchQueryOptimizationService();

        Callable<List<ElasticsearchAuditLog>> failingQuery = () -> {
            throw new RuntimeException("boom");
        };

        List<ElasticsearchAuditLog> results = ReflectionTestUtils.invokeMethod(
                service, "executeWithTimeout", failingQuery, 10L);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}

package com.example.buildnest_ecommerce.controller.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.management.MemoryMXBean;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PerformanceMetricsController Tests")
class PerformanceMetricsControllerTest {

    private PerformanceMetricsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new PerformanceMetricsController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Should return performance metrics successfully")
    void testGetPerformanceMetrics() throws Exception {
        mockMvc.perform(get("/actuator/custom/performance-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jvm").exists())
                .andExpect(jsonPath("$.runtime").exists())
                .andExpect(jsonPath("$.jvm.heapUsed_MB").exists())
                .andExpect(jsonPath("$.runtime.uptimeSeconds").exists());
    }

    @Test
    @DisplayName("Should return JVM metrics with correct structure")
    void testJvmMetricsStructure() {
        Map<String, Object> metrics = controller.getPerformanceMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.containsKey("jvm"));

        @SuppressWarnings("unchecked")
        Map<String, Object> jvmMetrics = (Map<String, Object>) metrics.get("jvm");

        assertTrue(jvmMetrics.containsKey("heapUsed_MB"));
        assertTrue(jvmMetrics.containsKey("heapMax_MB"));
        assertTrue(jvmMetrics.containsKey("heapUsagePercentage"));
        assertTrue(jvmMetrics.containsKey("nonHeapUsed_MB"));
    }

    @Test
    @DisplayName("Should return runtime metrics with correct structure")
    void testRuntimeMetricsStructure() {
        Map<String, Object> metrics = controller.getPerformanceMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.containsKey("runtime"));

        @SuppressWarnings("unchecked")
        Map<String, Object> runtimeMetrics = (Map<String, Object>) metrics.get("runtime");

        assertTrue(runtimeMetrics.containsKey("uptimeSeconds"));
        assertTrue(runtimeMetrics.containsKey("currentThreadCount"));
        assertTrue(runtimeMetrics.containsKey("peakThreadCount"));
        assertTrue(runtimeMetrics.containsKey("processorCount"));
    }

    @Test
    @DisplayName("Should return cache metrics endpoint")
    void testGetCacheMetrics() throws Exception {
        mockMvc.perform(get("/actuator/custom/cache-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cacheStatistics").exists())
                .andExpect(jsonPath("$.expectations").exists());
    }

    @Test
    @DisplayName("Should return database metrics endpoint")
    void testGetDatabaseMetrics() throws Exception {
        mockMvc.perform(get("/actuator/custom/database-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryPerformanceBaselines").exists());
    }

    @Test
    @DisplayName("Should return health metrics")
    void testGetHealthMetrics() {
        Map<String, Object> metrics = controller.getPerformanceMetrics();
        assertNotNull(metrics);

        // Verify health-related data exists
        assertTrue(metrics.containsKey("jvm") || metrics.containsKey("runtime"));
    }

    @Test
    @DisplayName("Should return positive uptime seconds")
    void testUptimeIsPositive() {
        Map<String, Object> metrics = controller.getPerformanceMetrics();

        @SuppressWarnings("unchecked")
        Map<String, Object> runtimeMetrics = (Map<String, Object>) metrics.get("runtime");

        Long uptimeSeconds = (Long) runtimeMetrics.get("uptimeSeconds");
        assertNotNull(uptimeSeconds);
        assertTrue(uptimeSeconds >= 0);
    }

    @Test
    @DisplayName("Should return valid thread count")
    void testThreadCountIsValid() {
        Map<String, Object> metrics = controller.getPerformanceMetrics();

        @SuppressWarnings("unchecked")
        Map<String, Object> runtimeMetrics = (Map<String, Object>) metrics.get("runtime");

        Integer threadCount = (Integer) runtimeMetrics.get("currentThreadCount");
        assertNotNull(threadCount);
        assertTrue(threadCount > 0);
    }

    @Test
    @DisplayName("Should return valid processor count")
    void testProcessorCountIsValid() {
        Map<String, Object> metrics = controller.getPerformanceMetrics();

        @SuppressWarnings("unchecked")
        Map<String, Object> runtimeMetrics = (Map<String, Object>) metrics.get("runtime");

        Integer processorCount = (Integer) runtimeMetrics.get("processorCount");
        assertNotNull(processorCount);
        assertTrue(processorCount > 0);
    }

    @Test
    @DisplayName("Should return heap usage percentage in valid format")
    void testHeapUsagePercentageFormat() {
        Map<String, Object> metrics = controller.getPerformanceMetrics();

        @SuppressWarnings("unchecked")
        Map<String, Object> jvmMetrics = (Map<String, Object>) metrics.get("jvm");

        String heapUsagePercentage = (String) jvmMetrics.get("heapUsagePercentage");
        assertNotNull(heapUsagePercentage);
        assertTrue(heapUsagePercentage.endsWith("%"));
    }

    @Test
    @DisplayName("Should return performance report structure")
    void testGetPerformanceReport() {
        Map<String, Object> report = controller.getPerformanceReport();
        assertNotNull(report);
        assertTrue(report.containsKey("title"));
        assertTrue(report.containsKey("sections"));
        assertTrue(report.containsKey("quickAssessment"));
    }

    @Test
    @DisplayName("Should determine heap status for thresholds")
    void testDetermineHeapStatusThresholds() {
        assertEquals("CRITICAL", controller.determineHeapStatus(96.0));
        assertEquals("WARNING", controller.determineHeapStatus(85.0));
        assertEquals("HEALTHY", controller.determineHeapStatus(50.0));
    }

    @Test
    @DisplayName("Should decide heap warning logging")
    void testShouldLogHeapWarning() {
        assertTrue(controller.shouldLogHeapWarning(81.0));
        assertFalse(controller.shouldLogHeapWarning(80.0));
    }

    @Test
    @DisplayName("Should log warning when heap utilization exceeds 80%")
    void testHeapWarningTriggeredInMetrics() {
        PerformanceMetricsController customController = new PerformanceMetricsController() {
            @Override
            protected long getHeapUsed(MemoryMXBean memoryBean) {
                return 90;
            }

            @Override
            protected long getHeapMax(MemoryMXBean memoryBean) {
                return 100;
            }

            @Override
            protected long getNonHeapUsed(MemoryMXBean memoryBean) {
                return 0;
            }
        };

        Map<String, Object> metrics = customController.getPerformanceMetrics();
        @SuppressWarnings("unchecked")
        Map<String, Object> health = (Map<String, Object>) metrics.get("health");
        assertEquals("WARNING", health.get("status"));
    }
}

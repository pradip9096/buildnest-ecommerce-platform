package com.example.buildnest_ecommerce.model.elasticsearch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ElasticsearchMetrics Branch Coverage Tests")
class ElasticsearchMetricsConditionalTest {

    // === ID Tests ===
    @Test
    @DisplayName("Test ID null vs non-null")
    void testIdNullAndNonNull() {
        ElasticsearchMetrics metrics1 = ElasticsearchMetrics.builder().id(null).build();
        assertNull(metrics1.getId());

        ElasticsearchMetrics metrics2 = ElasticsearchMetrics.builder().id("metric-1").build();
        assertEquals("metric-1", metrics2.getId());
    }

    // === Metric Name Tests ===
    @Test
    @DisplayName("Test metric name variations")
    void testMetricNameVariations() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().metricName(null).build();
        assertNull(m1.getMetricName());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().metricName("cpu_usage").build();
        assertEquals("cpu_usage", m2.getMetricName());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().metricName("memory_usage").build();
        assertEquals("memory_usage", m3.getMetricName());
    }

    // === Value Tests (Numeric) ===
    @Test
    @DisplayName("Test value with different numeric branches")
    void testValueNumericBranches() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().value(null).build();
        assertNull(m1.getValue());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().value(0.0).build();
        assertEquals(0.0, m2.getValue());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().value(50.5).build();
        assertEquals(50.5, m3.getValue());

        ElasticsearchMetrics m4 = ElasticsearchMetrics.builder().value(100.0).build();
        assertEquals(100.0, m4.getValue());

        ElasticsearchMetrics m5 = ElasticsearchMetrics.builder().value(-10.5).build();
        assertEquals(-10.5, m5.getValue());
    }

    // === Unit Tests ===
    @Test
    @DisplayName("Test unit field variations")
    void testUnitVariations() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().unit(null).build();
        assertNull(m1.getUnit());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().unit("percent").build();
        assertEquals("percent", m2.getUnit());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().unit("ms").build();
        assertEquals("ms", m3.getUnit());
    }

    // === Service Tests ===
    @Test
    @DisplayName("Test service field")
    void testServiceField() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().service(null).build();
        assertNull(m1.getService());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().service("auth-service").build();
        assertEquals("auth-service", m2.getService());
    }

    // === Timestamp Tests ===
    @Test
    @DisplayName("Test timestamp variations")
    void testTimestampVariations() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().timestamp(null).build();
        assertNull(m1.getTimestamp());

        LocalDateTime now = LocalDateTime.now();
        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().timestamp(now).build();
        assertEquals(now, m2.getTimestamp());
    }

    // === Host Tests ===
    @Test
    @DisplayName("Test host field")
    void testHostField() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().host(null).build();
        assertNull(m1.getHost());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().host("server-01").build();
        assertEquals("server-01", m2.getHost());
    }

    // === Environment Tests ===
    @Test
    @DisplayName("Test environment field variations")
    void testEnvironmentVariations() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().environment(null).build();
        assertNull(m1.getEnvironment());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().environment("production").build();
        assertEquals("production", m2.getEnvironment());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().environment("development").build();
        assertEquals("development", m3.getEnvironment());
    }

    // === Tags Tests (Map) ===
    @Test
    @DisplayName("Test tags map field")
    void testTagsMap() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().tags(null).build();
        assertNull(m1.getTags());

        Map<String, Object> tags = new HashMap<>();
        tags.put("region", "us-east-1");
        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().tags(tags).build();
        assertEquals(tags, m2.getTags());
        assertNotNull(m2.getTags());
    }

    // === JVM Memory Tests ===
    @Test
    @DisplayName("Test JVM memory usage percent")
    void testJvmMemoryUsagePercent() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().jvmMemoryUsagePercent(null).build();
        assertNull(m1.getJvmMemoryUsagePercent());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().jvmMemoryUsagePercent(0.0).build();
        assertEquals(0.0, m2.getJvmMemoryUsagePercent());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().jvmMemoryUsagePercent(75.5).build();
        assertEquals(75.5, m3.getJvmMemoryUsagePercent());

        ElasticsearchMetrics m4 = ElasticsearchMetrics.builder().jvmMemoryUsagePercent(100.0).build();
        assertEquals(100.0, m4.getJvmMemoryUsagePercent());
    }

    // === JVM Heap Tests ===
    @Test
    @DisplayName("Test JVM heap used bytes")
    void testJvmHeapUsedBytes() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().jvmHeapUsedBytes(null).build();
        assertNull(m1.getJvmHeapUsedBytes());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().jvmHeapUsedBytes(0L).build();
        assertEquals(0L, m2.getJvmHeapUsedBytes());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().jvmHeapUsedBytes(1073741824L).build();
        assertEquals(1073741824L, m3.getJvmHeapUsedBytes());
    }

    // === HTTP Request Count Tests ===
    @Test
    @DisplayName("Test HTTP request count")
    void testHttpRequestCount() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().httpRequestCount(null).build();
        assertNull(m1.getHttpRequestCount());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().httpRequestCount(0L).build();
        assertEquals(0L, m2.getHttpRequestCount());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().httpRequestCount(1000L).build();
        assertEquals(1000L, m3.getHttpRequestCount());
    }

    // === HTTP Response Time Tests ===
    @Test
    @DisplayName("Test HTTP response time in milliseconds")
    void testHttpResponseTimeMs() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().httpResponseTimeMs(null).build();
        assertNull(m1.getHttpResponseTimeMs());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().httpResponseTimeMs(0.0).build();
        assertEquals(0.0, m2.getHttpResponseTimeMs());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().httpResponseTimeMs(150.5).build();
        assertEquals(150.5, m3.getHttpResponseTimeMs());
    }

    // === HTTP Status Code Tests ===
    @Test
    @DisplayName("Test HTTP status code variations")
    void testHttpStatusCode() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().httpStatusCode(null).build();
        assertNull(m1.getHttpStatusCode());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().httpStatusCode(200).build();
        assertEquals(200, m2.getHttpStatusCode());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().httpStatusCode(404).build();
        assertEquals(404, m3.getHttpStatusCode());

        ElasticsearchMetrics m4 = ElasticsearchMetrics.builder().httpStatusCode(500).build();
        assertEquals(500, m4.getHttpStatusCode());
    }

    // === Database Connection Pool Tests ===
    @Test
    @DisplayName("Test database connection pool size")
    void testDbConnectionPoolSize() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().dbConnectionPoolSize(null).build();
        assertNull(m1.getDbConnectionPoolSize());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().dbConnectionPoolSize(0).build();
        assertEquals(0, m2.getDbConnectionPoolSize());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().dbConnectionPoolSize(20).build();
        assertEquals(20, m3.getDbConnectionPoolSize());
    }

    // === Database Query Time Tests ===
    @Test
    @DisplayName("Test database query time")
    void testDbQueryTimeMs() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().dbQueryTimeMs(null).build();
        assertNull(m1.getDbQueryTimeMs());

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().dbQueryTimeMs(0.0).build();
        assertEquals(0.0, m2.getDbQueryTimeMs());

        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().dbQueryTimeMs(250.75).build();
        assertEquals(250.75, m3.getDbQueryTimeMs());
    }

    // === Complex Scenarios ===
    @Test
    @DisplayName("Test complete metrics object with all fields")
    void testCompleteMetricsObject() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> tags = new HashMap<>();
        tags.put("datacenter", "dc-01");

        ElasticsearchMetrics metrics = ElasticsearchMetrics.builder()
                .id("metric-123")
                .metricName("cpu_usage")
                .value(65.5)
                .unit("percent")
                .service("api-service")
                .timestamp(now)
                .host("server-01")
                .environment("production")
                .tags(tags)
                .jvmMemoryUsagePercent(55.0)
                .jvmHeapUsedBytes(1073741824L)
                .httpRequestCount(5000L)
                .httpResponseTimeMs(120.5)
                .httpStatusCode(200)
                .dbConnectionPoolSize(15)
                .dbQueryTimeMs(45.25)
                .build();

        assertEquals("metric-123", metrics.getId());
        assertEquals("cpu_usage", metrics.getMetricName());
        assertEquals(65.5, metrics.getValue());
        assertEquals("percent", metrics.getUnit());
        assertEquals("api-service", metrics.getService());
        assertEquals(now, metrics.getTimestamp());
        assertEquals("server-01", metrics.getHost());
        assertEquals("production", metrics.getEnvironment());
        assertEquals(tags, metrics.getTags());
        assertEquals(55.0, metrics.getJvmMemoryUsagePercent());
        assertEquals(1073741824L, metrics.getJvmHeapUsedBytes());
        assertEquals(5000L, metrics.getHttpRequestCount());
        assertEquals(120.5, metrics.getHttpResponseTimeMs());
        assertEquals(200, metrics.getHttpStatusCode());
        assertEquals(15, metrics.getDbConnectionPoolSize());
        assertEquals(45.25, metrics.getDbQueryTimeMs());
    }

    // === No-args Constructor Tests ===
    @Test
    @DisplayName("Test no-args constructor")
    void testNoArgsConstructor() {
        ElasticsearchMetrics metrics = new ElasticsearchMetrics();
        assertNull(metrics.getId());
        assertNull(metrics.getMetricName());
        assertNull(metrics.getValue());
    }

    // === All-args Constructor Tests ===
    @Test
    @DisplayName("Test all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ElasticsearchMetrics metrics = new ElasticsearchMetrics(
                "id-1", "metric", 50.0, "unit", "service",
                now, "host", "env", null, 80.0, 100L,
                200L, 50.0, 200, 10, 25.0);

        assertEquals("id-1", metrics.getId());
        assertEquals("metric", metrics.getMetricName());
        assertEquals(50.0, metrics.getValue());
        assertEquals("unit", metrics.getUnit());
    }

    // === Equals/HashCode Tests ===
    @Test
    @DisplayName("Test equals and hashCode consistency")
    void testEqualsAndHashCode() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().id("1").metricName("cpu").value(50.0).build();
        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().id("1").metricName("cpu").value(50.0).build();

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    // === ToString Tests ===
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        ElasticsearchMetrics metrics = ElasticsearchMetrics.builder()
                .id("1")
                .metricName("test")
                .build();

        String str = metrics.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }
}

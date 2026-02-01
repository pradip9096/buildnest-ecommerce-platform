package com.example.buildnest_ecommerce.model.elasticsearch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Elasticsearch model tests")
class ElasticsearchModelsTest {

    @Test
    @DisplayName("Should build ElasticsearchAuditLog with all fields")
    void testAuditLogBuilderAndAccessors() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> context = Map.of("key", "value");

        ElasticsearchAuditLog auditLog = ElasticsearchAuditLog.builder()
                .id("id-1")
                .userId(10L)
                .action("LOGIN")
                .entityType("USER")
                .entityId(99L)
                .timestamp(now)
                .ipAddress("127.0.0.1")
                .userAgent("agent")
                .oldValue("old")
                .newValue("new")
                .severity("INFO")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .endpoint("/api/login")
                .additionalContext(context)
                .build();

        assertEquals("id-1", auditLog.getId());
        assertEquals(10L, auditLog.getUserId());
        assertEquals("LOGIN", auditLog.getAction());
        assertEquals("USER", auditLog.getEntityType());
        assertEquals(99L, auditLog.getEntityId());
        assertEquals(now, auditLog.getTimestamp());
        assertEquals("127.0.0.1", auditLog.getIpAddress());
        assertEquals("agent", auditLog.getUserAgent());
        assertEquals("old", auditLog.getOldValue());
        assertEquals("new", auditLog.getNewValue());
        assertEquals("INFO", auditLog.getSeverity());
        assertEquals(200, auditLog.getHttpStatusCode());
        assertEquals("SUCCESS", auditLog.getErrorCategory());
        assertEquals("/api/login", auditLog.getEndpoint());
        assertEquals(context, auditLog.getAdditionalContext());

        ElasticsearchAuditLog copy = new ElasticsearchAuditLog();
        copy.setId("id-1");
        copy.setUserId(10L);
        copy.setAction("LOGIN");
        copy.setEntityType("USER");
        copy.setEntityId(99L);
        copy.setTimestamp(now);
        copy.setIpAddress("127.0.0.1");
        copy.setUserAgent("agent");
        copy.setOldValue("old");
        copy.setNewValue("new");
        copy.setSeverity("INFO");
        copy.setHttpStatusCode(200);
        copy.setErrorCategory("SUCCESS");
        copy.setEndpoint("/api/login");
        copy.setAdditionalContext(context);

        assertEquals(auditLog, copy);
        assertEquals(auditLog.hashCode(), copy.hashCode());
    }

    @Test
    @DisplayName("Should build ElasticsearchMetrics with all fields")
    void testMetricsBuilderAndAccessors() {
        LocalDateTime now = LocalDateTime.now();

        ElasticsearchMetrics metrics = ElasticsearchMetrics.builder()
                .id("m-1")
                .metricName("cpu.usage")
                .value(12.5)
                .unit("%")
                .service("svc")
                .timestamp(now)
                .host("host-1")
                .environment("test")
                .tags(Map.of("region", "us-east"))
                .jvmMemoryUsagePercent(30.5)
                .jvmHeapUsedBytes(12345L)
                .httpRequestCount(44L)
                .httpResponseTimeMs(55.5)
                .httpStatusCode(200)
                .dbConnectionPoolSize(8)
                .dbQueryTimeMs(3.4)
                .build();

        assertEquals("m-1", metrics.getId());
        assertEquals("cpu.usage", metrics.getMetricName());
        assertEquals(12.5, metrics.getValue());
        assertEquals("%", metrics.getUnit());
        assertEquals("svc", metrics.getService());
        assertEquals(now, metrics.getTimestamp());
        assertEquals("host-1", metrics.getHost());
        assertEquals("test", metrics.getEnvironment());
        assertEquals(30.5, metrics.getJvmMemoryUsagePercent());
        assertEquals(12345L, metrics.getJvmHeapUsedBytes());
        assertEquals(44L, metrics.getHttpRequestCount());
        assertEquals(55.5, metrics.getHttpResponseTimeMs());
        assertEquals(200, metrics.getHttpStatusCode());
        assertEquals(8, metrics.getDbConnectionPoolSize());
        assertEquals(3.4, metrics.getDbQueryTimeMs());
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics equals and hashCode")
    void testMetricsEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> tags = Map.of("key", "value");

        ElasticsearchMetrics metrics1 = ElasticsearchMetrics.builder()
                .id("m-1")
                .metricName("cpu.usage")
                .value(50.0)
                .timestamp(now)
                .tags(tags)
                .build();

        ElasticsearchMetrics metrics2 = ElasticsearchMetrics.builder()
                .id("m-1")
                .metricName("cpu.usage")
                .value(50.0)
                .timestamp(now)
                .tags(tags)
                .build();

        assertEquals(metrics1, metrics2);
        assertEquals(metrics1.hashCode(), metrics2.hashCode());
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics with different values are not equal")
    void testMetricsDifferentValues() {
        ElasticsearchMetrics metrics1 = ElasticsearchMetrics.builder()
                .id("m-1")
                .metricName("cpu.usage")
                .value(50.0)
                .build();

        ElasticsearchMetrics metrics2 = ElasticsearchMetrics.builder()
                .id("m-2")
                .metricName("memory.usage")
                .value(75.0)
                .build();

        assertNotEquals(metrics1, metrics2);
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics with null and different types")
    void testMetricsEqualsNullAndDifferentType() {
        ElasticsearchMetrics metrics = ElasticsearchMetrics.builder()
                .id("m-1")
                .build();

        assertNotEquals(metrics, null);
        assertNotEquals(metrics, "Not a Metrics");
        assertEquals(metrics, metrics);
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics toString")
    void testMetricsToString() {
        ElasticsearchMetrics metrics = ElasticsearchMetrics.builder()
                .id("m-1")
                .metricName("cpu.usage")
                .value(50.0)
                .build();

        String result = metrics.toString();
        assertTrue(result.contains("ElasticsearchMetrics"));
        assertTrue(result.contains("m-1"));
        assertTrue(result.contains("cpu.usage"));
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics no-args constructor")
    void testMetricsNoArgsConstructor() {
        ElasticsearchMetrics metrics = new ElasticsearchMetrics();
        assertNotNull(metrics);
        assertNull(metrics.getId());
        assertNull(metrics.getMetricName());
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics all-args constructor")
    void testMetricsAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> tags = Map.of("env", "prod");

        ElasticsearchMetrics metrics = new ElasticsearchMetrics(
                "m-1", "cpu.usage", 50.0, "%", "service1", now, "host1", "prod",
                tags, 30.0, 1000L, 100L, 50.0, 200, 10, 5.0);

        assertEquals("m-1", metrics.getId());
        assertEquals("cpu.usage", metrics.getMetricName());
        assertEquals(50.0, metrics.getValue());
        assertEquals("%", metrics.getUnit());
        assertEquals("service1", metrics.getService());
        assertEquals(now, metrics.getTimestamp());
        assertEquals("host1", metrics.getHost());
        assertEquals("prod", metrics.getEnvironment());
        assertEquals(tags, metrics.getTags());
        assertEquals(30.0, metrics.getJvmMemoryUsagePercent());
        assertEquals(1000L, metrics.getJvmHeapUsedBytes());
        assertEquals(100L, metrics.getHttpRequestCount());
        assertEquals(50.0, metrics.getHttpResponseTimeMs());
        assertEquals(200, metrics.getHttpStatusCode());
        assertEquals(10, metrics.getDbConnectionPoolSize());
        assertEquals(5.0, metrics.getDbQueryTimeMs());
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics setters")
    void testMetricsSetters() {
        ElasticsearchMetrics metrics = new ElasticsearchMetrics();
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> tags = Map.of("region", "us-west");

        metrics.setId("m-2");
        metrics.setMetricName("memory.usage");
        metrics.setValue(75.5);
        metrics.setUnit("MB");
        metrics.setService("service2");
        metrics.setTimestamp(now);
        metrics.setHost("host2");
        metrics.setEnvironment("staging");
        metrics.setTags(tags);
        metrics.setJvmMemoryUsagePercent(45.0);
        metrics.setJvmHeapUsedBytes(2000L);
        metrics.setHttpRequestCount(200L);
        metrics.setHttpResponseTimeMs(100.0);
        metrics.setHttpStatusCode(201);
        metrics.setDbConnectionPoolSize(15);
        metrics.setDbQueryTimeMs(10.5);

        assertEquals("m-2", metrics.getId());
        assertEquals("memory.usage", metrics.getMetricName());
        assertEquals(75.5, metrics.getValue());
        assertEquals("MB", metrics.getUnit());
        assertEquals("service2", metrics.getService());
        assertEquals(now, metrics.getTimestamp());
        assertEquals("host2", metrics.getHost());
        assertEquals("staging", metrics.getEnvironment());
        assertEquals(tags, metrics.getTags());
        assertEquals(45.0, metrics.getJvmMemoryUsagePercent());
        assertEquals(2000L, metrics.getJvmHeapUsedBytes());
        assertEquals(200L, metrics.getHttpRequestCount());
        assertEquals(100.0, metrics.getHttpResponseTimeMs());
        assertEquals(201, metrics.getHttpStatusCode());
        assertEquals(15, metrics.getDbConnectionPoolSize());
        assertEquals(10.5, metrics.getDbQueryTimeMs());
    }

    @Test
    @DisplayName("Should test ElasticsearchAuditLog equals with different values")
    void testAuditLogDifferentValues() {
        ElasticsearchAuditLog log1 = ElasticsearchAuditLog.builder()
                .id("id-1")
                .action("LOGIN")
                .build();

        ElasticsearchAuditLog log2 = ElasticsearchAuditLog.builder()
                .id("id-2")
                .action("LOGOUT")
                .build();

        assertNotEquals(log1, log2);
    }

    @Test
    @DisplayName("Should test ElasticsearchAuditLog with null and different types")
    void testAuditLogEqualsNullAndDifferentType() {
        ElasticsearchAuditLog log = ElasticsearchAuditLog.builder()
                .id("id-1")
                .build();

        assertNotEquals(log, null);
        assertNotEquals(log, "Not an AuditLog");
        assertEquals(log, log);
    }

    @Test
    @DisplayName("Should test ElasticsearchAuditLog toString")
    void testAuditLogToString() {
        ElasticsearchAuditLog log = ElasticsearchAuditLog.builder()
                .id("id-1")
                .action("LOGIN")
                .userId(10L)
                .build();

        String result = log.toString();
        assertTrue(result.contains("ElasticsearchAuditLog"));
        assertTrue(result.contains("id-1"));
        assertTrue(result.contains("LOGIN"));
    }

    @Test
    @DisplayName("Should test ElasticsearchAuditLog no-args constructor")
    void testAuditLogNoArgsConstructor() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();
        assertNotNull(log);
        assertNull(log.getId());
        assertNull(log.getAction());
    }

    @Test
    @DisplayName("Should test ElasticsearchAuditLog canEqual with subclass")
    void testAuditLogCanEqualWithSubclass() {
        ElasticsearchAuditLog log1 = ElasticsearchAuditLog.builder()
                .id("id-1")
                .build();

        ElasticsearchAuditLog subclass = new ElasticsearchAuditLog() {
        };
        subclass.setId("id-1");

        assertEquals(log1, subclass);
    }

    @Test
    @DisplayName("Should test ElasticsearchMetrics canEqual with subclass")
    void testMetricsCanEqualWithSubclass() {
        ElasticsearchMetrics metrics1 = ElasticsearchMetrics.builder()
                .id("m-1")
                .build();

        ElasticsearchMetrics subclass = new ElasticsearchMetrics() {
        };
        subclass.setId("m-1");

        assertEquals(metrics1, subclass);
    }
}

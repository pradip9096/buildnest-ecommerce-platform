package com.example.buildnest_ecommerce.model.elasticsearch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ElasticsearchMetrics Comprehensive Branch Coverage")
class ElasticsearchMetricsExtendedTest {

    @ParameterizedTest
    @ValueSource(doubles = { -100.0, -50.5, -1.0, 0.0, 0.1, 1.0, 50.5, 99.99, 100.0, 999.99, 9999.99 })
    @DisplayName("Test value branches with numeric edge cases")
    void testValueEdgeCases(double value) {
        ElasticsearchMetrics m = ElasticsearchMetrics.builder().value(value).build();
        assertEquals(value, m.getValue());
        assertNotNull(m.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "metric1, cpu_usage",
            "metric2, memory_usage",
            "metric3, disk_usage",
            "metric4, network_io",
            "metric5, response_time"
    })
    @DisplayName("Test combination of ID and metric names")
    void testIdMetricNameCombinations(String id, String metricName) {
        ElasticsearchMetrics m = ElasticsearchMetrics.builder()
                .id(id)
                .metricName(metricName)
                .build();
        assertEquals(id, m.getId());
        assertEquals(metricName, m.getMetricName());
    }

    @Test
    @DisplayName("Test all fields populated simultaneously")
    void testAllFieldsPopulated() {
        LocalDateTime now = LocalDateTime.now();
        ElasticsearchMetrics m = ElasticsearchMetrics.builder()
                .id("m1")
                .metricName("cpu")
                .value(85.5)
                .unit("percent")
                .service("auth")
                .timestamp(now)
                .host("server1")
                .environment("prod")
                .tags(Map.of("important", "critical"))
                .build();

        assertAll(
                () -> assertEquals("m1", m.getId()),
                () -> assertEquals("cpu", m.getMetricName()),
                () -> assertEquals(85.5, m.getValue()),
                () -> assertEquals("percent", m.getUnit()),
                () -> assertEquals("auth", m.getService()),
                () -> assertEquals(now, m.getTimestamp()),
                () -> assertEquals("server1", m.getHost()),
                () -> assertEquals("prod", m.getEnvironment()));
    }

    @Test
    @DisplayName("Test equals and hashcode with same values")
    void testEqualsAndHashCodeIdentical() {
        LocalDateTime time = LocalDateTime.now();
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder()
                .id("m1")
                .value(50.0)
                .metricName("metric1")
                .timestamp(time)
                .build();

        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder()
                .id("m1")
                .value(50.0)
                .metricName("metric1")
                .timestamp(time)
                .build();

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    @DisplayName("Test equals with different values")
    void testNotEqualsWithDifferentValues() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().id("m1").value(50.0).build();
        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().id("m2").value(60.0).build();

        assertNotEquals(m1, m2);
        assertNotEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    @DisplayName("Test builder pattern with null values")
    void testBuilderWithNulls() {
        ElasticsearchMetrics m = ElasticsearchMetrics.builder()
                .id(null)
                .metricName(null)
                .value(null)
                .unit(null)
                .service(null)
                .timestamp(null)
                .host(null)
                .environment(null)
                .build();

        assertNull(m.getId());
        assertNull(m.getMetricName());
        assertNull(m.getValue());
        assertNull(m.getUnit());
        assertNull(m.getService());
        assertNull(m.getTimestamp());
        assertNull(m.getHost());
        assertNull(m.getEnvironment());
    }

    @Test
    @DisplayName("Test noArgs constructor")
    void testNoArgsConstructor() {
        ElasticsearchMetrics m = new ElasticsearchMetrics();
        assertNull(m.getId());
        assertNull(m.getMetricName());
        assertNull(m.getValue());
    }

    @Test
    @DisplayName("Test builder with required core fields")
    void testBuilderCoreFields() {
        LocalDateTime now = LocalDateTime.now();
        ElasticsearchMetrics m = ElasticsearchMetrics.builder()
                .id("m1")
                .metricName("cpu")
                .value(80.0)
                .unit("%")
                .service("svc")
                .timestamp(now)
                .host("host1")
                .environment("prod")
                .build();

        assertEquals("m1", m.getId());
        assertEquals("cpu", m.getMetricName());
        assertEquals(80.0, m.getValue());
        assertEquals("%", m.getUnit());
        assertEquals("svc", m.getService());
        assertEquals(now, m.getTimestamp());
        assertEquals("host1", m.getHost());
        assertEquals("prod", m.getEnvironment());
    }

    @Test
    @DisplayName("Test setters")
    void testSetters() {
        ElasticsearchMetrics m = new ElasticsearchMetrics();
        m.setId("new-id");
        m.setMetricName("new-metric");
        m.setValue(75.0);
        m.setUnit("ms");
        m.setService("payment");
        m.setHost("server2");
        m.setEnvironment("staging");

        assertEquals("new-id", m.getId());
        assertEquals("new-metric", m.getMetricName());
        assertEquals(75.0, m.getValue());
        assertEquals("ms", m.getUnit());
        assertEquals("payment", m.getService());
        assertEquals("server2", m.getHost());
        assertEquals("staging", m.getEnvironment());
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        ElasticsearchMetrics m = ElasticsearchMetrics.builder()
                .id("m1")
                .metricName("cpu")
                .value(50.0)
                .build();

        String str = m.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
        assertTrue(str.contains("ElasticsearchMetrics") || str.contains("id") || str.contains("m1"));
    }

    @Test
    @DisplayName("Test loop iteration with 10 metrics")
    void testLoopWith10Metrics() {
        List<ElasticsearchMetrics> metrics = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ElasticsearchMetrics m = ElasticsearchMetrics.builder()
                    .id("metric-" + i)
                    .metricName("name-" + i)
                    .value((double) (i * 10))
                    .build();
            metrics.add(m);
        }

        assertEquals(10, metrics.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("metric-" + i, metrics.get(i).getId());
        }
    }

    @Test
    @DisplayName("Test timestamp null and non-null branches")
    void testTimestampBranches() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().timestamp(null).build();
        assertNull(m1.getTimestamp());

        LocalDateTime past = LocalDateTime.of(2023, 1, 1, 0, 0);
        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().timestamp(past).build();
        assertEquals(past, m2.getTimestamp());

        LocalDateTime now = LocalDateTime.now();
        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().timestamp(now).build();
        assertEquals(now, m3.getTimestamp());
    }

    @Test
    @DisplayName("Test value comparison branches")
    void testValueComparisons() {
        ElasticsearchMetrics m1 = ElasticsearchMetrics.builder().value(50.0).build();
        ElasticsearchMetrics m2 = ElasticsearchMetrics.builder().value(50.0).build();
        ElasticsearchMetrics m3 = ElasticsearchMetrics.builder().value(60.0).build();

        assertEquals(m1.getValue(), m2.getValue());
        assertNotEquals(m1.getValue(), m3.getValue());
    }

    @Test
    @DisplayName("Test unit field branches (null vs various units)")
    void testUnitBranches() {
        String[] units = { null, "%", "ms", "bytes", "requests", "errors" };
        List<ElasticsearchMetrics> metrics = new ArrayList<>();

        for (String unit : units) {
            ElasticsearchMetrics m = ElasticsearchMetrics.builder().unit(unit).build();
            metrics.add(m);
        }

        assertEquals(6, metrics.size());
        assertNull(metrics.get(0).getUnit());
        assertEquals("%", metrics.get(1).getUnit());
        assertEquals("ms", metrics.get(2).getUnit());
    }

    @Test
    @DisplayName("Test builder with modified copy")
    void testBuilderWithModifiedCopy() {
        ElasticsearchMetrics original = ElasticsearchMetrics.builder()
                .id("orig")
                .metricName("test")
                .value(75.0)
                .unit("%")
                .build();

        ElasticsearchMetrics clone = ElasticsearchMetrics.builder()
                .id("clone")
                .metricName(original.getMetricName())
                .value(original.getValue())
                .unit(original.getUnit())
                .build();

        assertEquals("clone", clone.getId());
        assertEquals("test", clone.getMetricName());
        assertEquals(75.0, clone.getValue());
        assertEquals("%", clone.getUnit());
    }

    @Test
    @DisplayName("Test tags field handling")
    void testTagsField() {
        ElasticsearchMetrics m = ElasticsearchMetrics.builder()
                .id("m1")
                .tags(Map.of("tag1", "value1", "tag2", "value2", "tag3", "value3"))
                .build();

        assertNotNull(m.getId());
        assertEquals("m1", m.getId());
    }

    @Test
    @DisplayName("Test service field variations")
    void testServiceFieldVariations() {
        String[] services = { null, "", "auth-service", "payment-service", "order-service" };

        for (String svc : services) {
            ElasticsearchMetrics m = ElasticsearchMetrics.builder().service(svc).build();
            assertEquals(svc, m.getService());
        }
    }

    @Test
    @DisplayName("Test environment field variations")
    void testEnvironmentVariations() {
        String[] envs = { null, "dev", "staging", "prod", "test" };

        for (String env : envs) {
            ElasticsearchMetrics m = ElasticsearchMetrics.builder().environment(env).build();
            assertEquals(env, m.getEnvironment());
        }
    }

    @Test
    @DisplayName("Test host field variations")
    void testHostVariations() {
        String[] hosts = { null, "localhost", "server-1", "192.168.1.1", "node-prod-001" };

        for (String host : hosts) {
            ElasticsearchMetrics m = ElasticsearchMetrics.builder().host(host).build();
            assertEquals(host, m.getHost());
        }
    }
}

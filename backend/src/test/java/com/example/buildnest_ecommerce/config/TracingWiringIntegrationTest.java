package com.example.buildnest_ecommerce.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real-Spring-context test for #108 (OBS-02): a unit test mocking {@link Tracer} would pass
 * identically whether {@code micrometer-tracing-bridge-otel}'s autoconfiguration actually wired
 * a real tracer or not — the risk this issue introduces is autoconfiguration-level (testing.md's
 * framework/mapping-level tier), so only a real application context can prove Spring Boot didn't
 * silently fall back to the no-op {@code Tracer} it registers when the bridge is absent from the
 * classpath.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestElasticsearchConfig.class})
class TracingWiringIntegrationTest {

    @Autowired
    private Tracer tracer;

    @Test
    @DisplayName("a real OpenTelemetry-backed Tracer bean is wired, not the no-op fallback")
    void tracerBean_isOtelBacked_notNoop() {
        assertThat(tracer).isInstanceOf(OtelTracer.class);
    }

    @Test
    @DisplayName("a span started through the wired Tracer produces a real, non-zero trace ID")
    void startedSpan_hasRealTraceContext() {
        Span span = tracer.nextSpan().name("tracing-wiring-test-span").start();
        try {
            String traceId = span.context().traceId();
            assertThat(traceId).isNotBlank();
            assertThat(traceId).isNotEqualTo("0".repeat(32));
        } finally {
            span.end();
        }
    }
}

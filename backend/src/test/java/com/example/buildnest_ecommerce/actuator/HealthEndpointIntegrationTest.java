package com.example.buildnest_ecommerce.actuator;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real-context coverage for #123 (OPS-05): proves the readiness/liveness
 * probe groups are genuinely wired to real dependency indicators, not just
 * that the endpoints exist. A mocked unit test on each indicator class
 * cannot observe whether Spring Boot actually assembled the "readiness"
 * health group per {@code management.endpoint.health.group.readiness.include}
 * — this is framework/auto-configuration-level behavior, per
 * {@code .claude/rules/common/testing.md}'s integration-tier rule.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
class HealthEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("liveness probe is reachable without auth and reports UP")
    void livenessProbeIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("readiness probe is reachable without auth and includes db+redis components")
    void readinessProbeIncludesDbAndRedis() throws Exception {
        // Overall status depends on real MySQL/Redis reachability in the
        // running environment (not asserted here — see #123's AC2, which
        // is about the *group membership*, proven by component presence
        // below) — only the response *structure* is what this test proves,
        // per AC5. status() is intentionally not pinned to isOk(), since a
        // genuinely unreachable dependency correctly yields 503.
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.components.db").exists())
                .andExpect(jsonPath("$.components.redis").exists());
    }

    @Test
    @DisplayName("readiness probe excludes elasticsearch when elasticsearch.enabled=false")
    void readinessProbeExcludesDisabledElasticsearch() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(jsonPath("$.components.elasticsearch").doesNotExist());
    }
}

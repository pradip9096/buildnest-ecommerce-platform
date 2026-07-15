package com.example.buildnest_ecommerce.security;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression coverage for #359: Prometheus's scrape of {@code /actuator/prometheus} must be
 * rejected without the dedicated monitoring credential, and must never be satisfiable via the
 * JWT/ADMIN path that governs every other {@code /actuator/**} endpoint. A Mockito-mocked unit
 * test cannot observe this — it's filter-chain/proxy-level behavior, per
 * {@code .claude/rules/common/testing.md}'s tier-3 (framework wiring) test-type rule.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@TestPropertySource(properties = "management.endpoints.web.exposure.include=health,info,prometheus")
class ActuatorMonitoringSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("unauthenticated request to /actuator/prometheus is rejected")
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("correct monitoring credential passes the security layer (not 401/403)")
    void correctMonitoringCredentialPassesSecurityLayer() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("monitoring", "changeme-monitoring-password")))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status)
                            .as("authenticated monitoring request must clear the security layer")
                            .isNotIn(401, 403);
                });
    }

    @Test
    @DisplayName("wrong monitoring credential is rejected")
    void wrongMonitoringCredentialIsRejected() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("monitoring", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("other /actuator/** paths still require ADMIN, not the monitoring credential")
    void otherActuatorPathsStillRequireAdmin() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("monitoring", "changeme-monitoring-password")))
                .andExpect(status().isUnauthorized());
    }
}

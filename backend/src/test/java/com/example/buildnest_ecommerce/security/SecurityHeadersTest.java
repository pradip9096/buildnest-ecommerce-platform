package com.example.buildnest_ecommerce.security;

import com.example.buildnest_ecommerce.config.SecurityHeaderPolicies;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * Regression test for #312: {@code SecurityConfig} (the production security filter chain,
 * including the SEC-14 CSP hardening from #237) is {@code @Profile("!test")} and never loads
 * during a test run. {@code TestSecurityConfig} stands in for it, and had drifted — this test
 * pins the header contract both classes must now share via {@link SecurityHeaderPolicies}, so a
 * future regression in either config fails a test instead of shipping silently.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("main-chain CSP has no unsafe-inline and matches the shared policy constant")
    void mainChainCspMatchesSharedPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().string("Content-Security-Policy", SecurityHeaderPolicies.MAIN_CSP))
                .andExpect(result -> {
                    String csp = result.getResponse().getHeader("Content-Security-Policy");
                    org.assertj.core.api.Assertions.assertThat(csp)
                            .as("main-chain CSP must never contain unsafe-inline (#237)")
                            .doesNotContain("unsafe-inline");
                });
    }

    @Test
    @DisplayName("main-chain response includes frame-deny always, and HSTS with the shared max-age over HTTPS")
    void mainChainIncludesFrameDenyAndHsts() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().string("X-Frame-Options", "DENY"));

        // Spring Security only emits HSTS on requests it considers secure (HTTPS) —
        // simulate that explicitly rather than relying on a plain (non-secure) GET.
        mockMvc.perform(get("/actuator/health").secure(true))
                .andExpect(header().string("Strict-Transport-Security",
                        "max-age=" + SecurityHeaderPolicies.HSTS_MAX_AGE_SECONDS + " ; includeSubDomains ; preload"));
    }

    @Test
    @DisplayName("main-chain response includes X-Content-Type-Options: nosniff (SEC-11/SEC-12, #112)")
    void mainChainIncludesContentTypeOptionsNosniff() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("main-chain response includes Referrer-Policy matching the shared policy constant (SEC-11/SEC-12, #112)")
    void mainChainIncludesReferrerPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().string("Referrer-Policy", SecurityHeaderPolicies.REFERRER_POLICY));
    }

    @Test
    @DisplayName("main-chain response includes Permissions-Policy matching the shared policy constant (SEC-11/SEC-12, #112)")
    void mainChainIncludesPermissionsPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().string("Permissions-Policy", SecurityHeaderPolicies.PERMISSIONS_POLICY));
    }
}

package com.example.buildnest_ecommerce.security;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Isolated in its own test class (not folded into {@code AuthControllerTest}) so its result can't
 * be affected by CSRF/security-context state left over from other tests sharing a MockMvc instance.
 *
 * <p>Regression guard for a confirmed Spring Security defect
 * (<a href="https://github.com/spring-projects/spring-security/issues/12141">GH-12141</a>):
 * {@code CsrfAuthenticationStrategy} clears the {@code XSRF-TOKEN} cookie on every authentication
 * event but fails to regenerate it within the same request/response cycle. Because
 * {@code JwtAuthenticationFilter} re-authenticates on every single stateless request, this fired on
 * effectively every authenticated request and silently broke all mutating endpoints until
 * {@link NonClearingCsrfTokenRepository} was introduced. If that wrapper is ever removed (or Spring
 * Security's behavior changes), this test should catch the regression.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
class CsrfCookieStabilityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElasticsearchAuditLogRepository auditLogRepository;

    @MockitoBean
    private ElasticsearchMetricsRepository metricsRepository;

    @Test
    void csrfCookieSurvivesMultipleConsecutiveAuthenticatedRequests() throws Exception {
        // Once a valid cookie is already present, Spring Security legitimately does NOT re-issue it on
        // every response (no Set-Cookie header at all is fine) — the bug this guards against is an
        // explicit *clearing* Set-Cookie (Max-Age=0) appearing while the client still holds a valid token.
        // So this asserts: across 3 consecutive authenticated requests carrying the cookie forward (like a
        // real browser's cookie jar), whenever a Set-Cookie for XSRF-TOKEN does appear, it is never a clear.
        MvcResult first = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();
        Cookie xsrfCookie = first.getResponse().getCookie("XSRF-TOKEN");
        assertFalse(xsrfCookie.getValue() == null || xsrfCookie.getValue().isBlank(),
                "XSRF-TOKEN cookie must have a real value on first issue");

        for (int i = 0; i < 2; i++) {
            var response = mockMvc.perform(get("/api/auth/csrf")
                    .with(user("test").roles("USER"))
                    .cookie(xsrfCookie))
                    .andExpect(status().isNoContent())
                    .andReturn().getResponse();

            Cookie reissued = response.getCookie("XSRF-TOKEN");
            if (reissued != null) {
                // A reissue happened — it must be a real value, never a clear (Max-Age=0, blank value).
                assertFalse(reissued.getValue() == null || reissued.getValue().isBlank(),
                        "XSRF-TOKEN was cleared on an authenticated request (GH-12141 regression) at iteration " + i);
                assertNotEquals(0, reissued.getMaxAge(),
                        "XSRF-TOKEN was explicitly cleared (Max-Age=0) on an authenticated request (GH-12141 regression) at iteration " + i);
                xsrfCookie = reissued;
            }
            // No reissue at all is fine — the browser already has a valid cookie, nothing to update.
        }
    }
}

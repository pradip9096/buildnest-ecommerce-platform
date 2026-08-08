package com.example.buildnest_ecommerce.ratelimit;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.payload.AuthResponse;
import com.example.buildnest_ecommerce.service.auth.AuthService;
import com.example.buildnest_ecommerce.service.category.CategoryService;
import com.example.buildnest_ecommerce.service.product.ProductService;
import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for rate limiting behaviour (NFR-SEC-05 / NFR-RATELIMIT-01).
 *
 * Two mechanisms are exercised:
 *  1. Controller-level check in AuthController via RateLimitUtil → RateLimiterService.isAllowed()
 *  2. Interceptor-level check in RateLimitHeaderInterceptor via RateLimiterService.getRemainingTokens()
 *
 * No Redis or real external services are required — RateLimiterService is replaced with a
 * Mockito mock that is configured per-test to simulate allowed and exhausted states.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        // Default: bucket is healthy — interceptor passes and controller-level checks allow through.
        when(rateLimiterService.getRemainingTokens(anyString(), anyInt())).thenReturn(50);
        when(rateLimiterService.getRetryAfterSeconds(anyString())).thenReturn(0L);
        when(rateLimiterService.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(true);
        when(rateLimiterService.getTimeUntilReset(anyString())).thenReturn(0L);
        when(productService.getAllProducts()).thenReturn(List.of());
    }

    @Test
    @DisplayName("TC-RL-001: Login within rate limit returns 200")
    void loginWithinRateLimitReturns200() throws Exception {
        when(authService.login(anyString(), anyString(), any()))
                .thenReturn(new AuthResponse("token", "refresh", "Bearer", 1L, "testuser", false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-RL-002: Login endpoint returns 429 when rate limit is exceeded")
    void loginExceedsRateLimitReturns429() throws Exception {
        // Interceptor allows through (remaining > 0), but controller-level check rejects.
        when(rateLimiterService.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(false);
        when(rateLimiterService.getRetryAfterSeconds(anyString())).thenReturn(60L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    @DisplayName("TC-RL-003: Public product list within rate limit returns 200")
    void productListWithinRateLimitReturns200() throws Exception {
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-RL-004: Public product list returns 429 when interceptor rate limit is exceeded")
    void productListExceedsRateLimitReturns429() throws Exception {
        // Interceptor sees 0 remaining tokens and blocks the request.
        when(rateLimiterService.getRemainingTokens(anyString(), anyInt())).thenReturn(0);
        when(rateLimiterService.getRetryAfterSeconds(anyString())).thenReturn(30L);

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "30"));
    }

    @Test
    @DisplayName("TC-RL-005: X-RateLimit-Remaining header reflects current token count")
    void rateLimitRemainingHeaderIsPresent() throws Exception {
        when(rateLimiterService.getRemainingTokens(anyString(), anyInt())).thenReturn(42);

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "42"));
    }

    @Test
    @DisplayName("TC-RL-006: X-RateLimit-Limit header is set on public endpoint responses")
    void rateLimitLimitHeaderIsPresent() throws Exception {
        // PUBLIC_LIMIT = 50 (defined in RateLimitHeaderInterceptor for /api/public/** paths).
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Limit", "50"));
    }
}

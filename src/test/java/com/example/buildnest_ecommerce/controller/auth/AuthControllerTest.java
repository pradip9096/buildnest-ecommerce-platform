package com.example.buildnest_ecommerce.controller.auth;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.payload.LoginRequest;
import com.example.buildnest_ecommerce.model.payload.RegisterRequest;
import com.example.buildnest_ecommerce.model.payload.RefreshTokenRequest;
import com.example.buildnest_ecommerce.model.payload.AuthResponse;
import com.example.buildnest_ecommerce.model.entity.RefreshToken;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import com.example.buildnest_ecommerce.service.auth.AuthService;
import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@SuppressWarnings("null")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private RateLimitUtil rateLimitUtil;

    @MockBean
    private RateLimiterService rateLimiterService;

    @MockBean
    private ElasticsearchAuditLogRepository auditLogRepository;

    @MockBean
    private ElasticsearchMetricsRepository metricsRepository;

    @BeforeEach
    void setUp() {
        // Allow rate limiting by default
        when(rateLimitUtil.isAllowed(any(), any())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(), any(), anyLong())).thenReturn(true);
        when(rateLimiterService.getRemainingTokens(anyString(), anyInt())).thenReturn(50);
        when(rateLimiterService.getRetryAfterSeconds(anyString())).thenReturn(0L);
    }

    @Test
    void testLoginWithValidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterWithValidData() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("Password@123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegisterWithInvalidEmail() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRateLimitingOnLogin() throws Exception {
        when(rateLimitUtil.isAllowed(any(), any())).thenReturn(false);
        when(rateLimitUtil.getRetryAfterSeconds(any(), any())).thenReturn(60L);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-123");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(1L);
        when(refreshTokenService.findByToken("refresh-123")).thenReturn(Optional.of(refreshToken));
        when(authService.refreshAccessToken("refresh-123"))
                .thenReturn(new AuthResponse("access", "refresh", "Bearer", 1L, "user"));

        mockMvc.perform(post("/api/auth/refresh")
                .with(user("test").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testRefreshTokenInvalid() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("missing");

        when(refreshTokenService.findByToken("missing")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh")
                .with(user("test").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshTokenRateLimited() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-123");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(1L);
        when(refreshTokenService.findByToken("refresh-123")).thenReturn(Optional.of(refreshToken));
        when(rateLimitUtil.isAllowed(any(), eq("refresh"), eq(1L))).thenReturn(false);
        when(rateLimitUtil.getRetryAfterSeconds(any(), eq("refresh"), eq(1L))).thenReturn(30L);

        mockMvc.perform(post("/api/auth/refresh")
                .with(user("test").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void testRefreshTokenException() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-123");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(1L);
        when(refreshTokenService.findByToken("refresh-123")).thenReturn(Optional.of(refreshToken));
        org.mockito.Mockito.doThrow(new RuntimeException("fail")).when(authService).refreshAccessToken("refresh-123");

        mockMvc.perform(post("/api/auth/refresh")
                .with(user("test").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testValidateToken() throws Exception {
        when(authService.validateToken("token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/validate-token")
                .with(user("test").roles("USER"))
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testValidateTokenWithoutBearerPrefix() throws Exception {
        when(authService.validateToken("raw-token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/validate-token")
                .with(user("test").roles("USER"))
                .header("Authorization", "raw-token"))
                .andExpect(status().isOk());
    }

    @Test
    void testValidateTokenInvalid() throws Exception {
        when(authService.validateToken("token")).thenReturn(false);

        mockMvc.perform(post("/api/auth/validate-token")
                .with(user("test").roles("USER"))
                .header("Authorization", "Bearer token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testValidateTokenException() throws Exception {
        when(authService.validateToken("token")).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/api/auth/validate-token")
                .with(user("test").roles("USER"))
                .header("Authorization", "Bearer token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginFailure() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("bad");
        loginRequest.setPassword("bad");

        org.mockito.Mockito.doThrow(new RuntimeException("fail"))
                .when(authService).login("bad", "bad");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogoutSuccessAndFailure() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-123");

        mockMvc.perform(post("/api/auth/logout")
                .with(user("test").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout")
                .with(user("test").roles("USER"))
                .with(csrf()))
                .andExpect(status().isOk());

        org.mockito.Mockito.doThrow(new RuntimeException("fail")).when(authService).logout("refresh-123");

        mockMvc.perform(post("/api/auth/logout")
                .with(user("test").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isInternalServerError());
    }
}

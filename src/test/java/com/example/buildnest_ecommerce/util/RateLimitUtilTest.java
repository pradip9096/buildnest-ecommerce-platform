package com.example.buildnest_ecommerce.util;

import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RateLimitUtilTest {

    @Test
    void buildsKeyAndDelegatesToRateLimiter() {
        RateLimiterService service = mock(RateLimiterService.class);
        RateLimitUtil util = new RateLimitUtil(service);

        ReflectionTestUtils.setField(util, "loginRequests", 5);
        ReflectionTestUtils.setField(util, "loginDuration", 60);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(service.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(true);

        boolean allowed = util.isAllowed(request, "login");
        assertTrue(allowed);

        verify(service).isAllowed(eq("10.0.0.1:login"), eq(5), eq(Duration.ofSeconds(60)));
    }

    @Test
    void usesRemoteAddressWhenNoForwardedHeader() {
        RateLimiterService service = mock(RateLimiterService.class);
        RateLimitUtil util = new RateLimitUtil(service);

        ReflectionTestUtils.setField(util, "adminRequests", 30);
        ReflectionTestUtils.setField(util, "adminDuration", 60);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(service.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(true);

        assertTrue(util.isAllowed(request, "admin", 10L));
        verify(service).isAllowed(eq("127.0.0.1:admin:user:10"), eq(30), eq(Duration.ofSeconds(60)));
    }

    @Test
    void getRetryAfterDelegates() {
        RateLimiterService service = mock(RateLimiterService.class);
        RateLimitUtil util = new RateLimitUtil(service);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(service.getRetryAfterSeconds(anyString())).thenReturn(5L);

        long seconds = util.getRetryAfterSeconds(request, "login");
        assertEquals(5L, seconds);
        verify(service).getRetryAfterSeconds(eq("127.0.0.1:login"));
    }

    @Test
    void usesPasswordResetLimitsAndSubjectKey() {
        RateLimiterService service = mock(RateLimiterService.class);
        RateLimitUtil util = new RateLimitUtil(service);

        ReflectionTestUtils.setField(util, "passwordResetRequests", 3);
        ReflectionTestUtils.setField(util, "passwordResetDuration", 3600);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8");
        when(service.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(true);

        assertTrue(util.isAllowed(request, "password-reset", 99L));
        verify(service).isAllowed(eq("1.2.3.4:password-reset:user:99"), eq(3), eq(Duration.ofSeconds(3600)));
    }

    @Test
    void usesRefreshTokenLimits() {
        RateLimiterService service = mock(RateLimiterService.class);
        RateLimitUtil util = new RateLimitUtil(service);

        ReflectionTestUtils.setField(util, "refreshTokenRequests", 10);
        ReflectionTestUtils.setField(util, "refreshTokenDuration", 60);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.9");
        when(service.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(true);

        assertTrue(util.isAllowed(request, "refresh"));
        verify(service).isAllowed(eq("10.0.0.9:refresh"), eq(10), eq(Duration.ofSeconds(60)));
    }

    @Test
    void fallsBackToLoginLimitsForUnknownEndpoint() {
        RateLimiterService service = mock(RateLimiterService.class);
        RateLimitUtil util = new RateLimitUtil(service);

        ReflectionTestUtils.setField(util, "loginRequests", 5);
        ReflectionTestUtils.setField(util, "loginDuration", 60);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.2");
        when(service.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(true);

        assertTrue(util.isAllowed(request, "unknown-endpoint"));
        verify(service).isAllowed(eq("127.0.0.2:unknown-endpoint"), eq(5), eq(Duration.ofSeconds(60)));
    }
}

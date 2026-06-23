package com.example.buildnest_ecommerce.interceptor;

import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RateLimitHeaderInterceptorTest {

    @Test
    void addsHeadersForAllowedRequest() throws Exception {
        RateLimiterService rateLimiterService = mock(RateLimiterService.class);
        RateLimitHeaderInterceptor interceptor = new RateLimitHeaderInterceptor(rateLimiterService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        request.addHeader("X-User-Id", "123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(rateLimiterService.getRemainingTokens(anyString(), anyInt())).thenReturn(4);
        when(rateLimiterService.getRetryAfterSeconds(anyString())).thenReturn(0L);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals("5", response.getHeader("X-RateLimit-Limit"));
        assertEquals("4", response.getHeader("X-RateLimit-Remaining"));
        assertNotNull(response.getHeader("X-RateLimit-Reset"));
    }

    @Test
    void blocksWhenLimitExceeded() throws Exception {
        RateLimiterService rateLimiterService = mock(RateLimiterService.class);
        RateLimitHeaderInterceptor interceptor = new RateLimitHeaderInterceptor(rateLimiterService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/stats");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(rateLimiterService.getRemainingTokens(anyString(), anyInt())).thenReturn(0);
        when(rateLimiterService.getRetryAfterSeconds(anyString())).thenReturn(30L);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(429, response.getStatus());
        assertEquals("30", response.getHeader("Retry-After"));
        assertEquals("approaching_limit", response.getHeader("X-RateLimit-Warning"));
        assertTrue(response.getContentAsString().contains("RATE_LIMIT_EXCEEDED"));
    }
}

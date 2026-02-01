package com.example.buildnest_ecommerce.security;

import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class AdminRateLimitFilterTest {

    @Test
    void shouldNotFilterForNonAdminPaths() {
        RateLimitUtil rateLimitUtil = mock(RateLimitUtil.class);
        AdminRateLimitFilter filter = new AdminRateLimitFilter(rateLimitUtil);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/user/profile");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldFilterForAdminPaths() {
        RateLimitUtil rateLimitUtil = mock(RateLimitUtil.class);
        AdminRateLimitFilter filter = new AdminRateLimitFilter(rateLimitUtil);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/stats");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void blocksWhenRateLimited() throws ServletException, IOException {
        RateLimitUtil rateLimitUtil = mock(RateLimitUtil.class);
        when(rateLimitUtil.isAllowed(any(), eq("admin"))).thenReturn(false);
        when(rateLimitUtil.getRetryAfterSeconds(any(), eq("admin"), isNull())).thenReturn(42L);

        AdminRateLimitFilter filter = new AdminRateLimitFilter(rateLimitUtil);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/stats");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertEquals(429, response.getStatus());
        assertEquals("42", response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("Too many admin requests"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void allowsWhenUnderLimit() throws ServletException, IOException {
        RateLimitUtil rateLimitUtil = mock(RateLimitUtil.class);
        when(rateLimitUtil.isAllowed(any(), eq("admin"))).thenReturn(true);

        AdminRateLimitFilter filter = new AdminRateLimitFilter(rateLimitUtil);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/stats");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(any(), any());
    }
}

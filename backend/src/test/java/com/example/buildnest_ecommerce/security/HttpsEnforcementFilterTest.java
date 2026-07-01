package com.example.buildnest_ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HttpsEnforcementFilter")
class HttpsEnforcementFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("passes request through when enforcement is disabled")
    void disabled_alwaysPassesThrough() throws Exception {
        HttpsEnforcementFilter filter = new HttpsEnforcementFilter(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("passes through when enforcement is enabled and connection is already secure")
    void enabled_secureConnection_passesThrough() throws Exception {
        HttpsEnforcementFilter filter = new HttpsEnforcementFilter(true);
        when(request.isSecure()).thenReturn(true);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("sends 403 when enforcement is enabled and connection is insecure without proxy header")
    void enabled_insecureConnection_noHeader_returns403() throws Exception {
        HttpsEnforcementFilter filter = new HttpsEnforcementFilter(true);
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "HTTPS is required");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("passes through when X-Forwarded-Proto is https even if socket is not secure (reverse proxy)")
    void enabled_insecureSocket_httpsProxyHeader_passesThrough() throws Exception {
        HttpsEnforcementFilter filter = new HttpsEnforcementFilter(true);
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("sends 403 when X-Forwarded-Proto is http even if socket reports secure")
    void enabled_secureSocket_httpProxyHeader_returns403() throws Exception {
        HttpsEnforcementFilter filter = new HttpsEnforcementFilter(true);
        when(request.isSecure()).thenReturn(true);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("http");

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "HTTPS is required");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("X-Forwarded-Proto comparison is case-insensitive — HTTPS uppercase passes through")
    void enabled_proxyHeaderCaseInsensitive_passes() throws Exception {
        HttpsEnforcementFilter filter = new HttpsEnforcementFilter(true);
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("HTTPS");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }
}

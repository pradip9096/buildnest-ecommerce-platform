package com.example.buildnest_ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Enforces HTTPS by rejecting insecure requests when enabled.
 * Supports X-Forwarded-Proto for reverse proxy environments.
 */
public class HttpsEnforcementFilter extends OncePerRequestFilter {
    private final boolean enabled;

    public HttpsEnforcementFilter(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean secure = request.isSecure();
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null) {
            secure = "https".equalsIgnoreCase(forwardedProto);
        }

        if (!secure) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "HTTPS is required");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

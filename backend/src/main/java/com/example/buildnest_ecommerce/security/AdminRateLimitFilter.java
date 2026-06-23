package com.example.buildnest_ecommerce.security;

import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AdminRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitUtil rateLimitUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!rateLimitUtil.isAllowed(request, "admin")) {
            long retryAfter = rateLimitUtil.getRetryAfterSeconds(request, "admin", null);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.getWriter().write("Too many admin requests. Please try again later.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}

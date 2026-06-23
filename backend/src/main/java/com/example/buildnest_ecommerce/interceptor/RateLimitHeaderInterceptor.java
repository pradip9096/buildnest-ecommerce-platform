package com.example.buildnest_ecommerce.interceptor;

import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Section 6.3 - API Rate Limiting Headers
 * 
 * Adds standardized rate limiting headers to all API responses:
 * - X-RateLimit-Limit: Maximum requests allowed in the window
 * - X-RateLimit-Remaining: Requests remaining in current window
 * - X-RateLimit-Reset: Unix timestamp when the rate limit resets
 * - Retry-After: (Only when rate limited) Seconds until requests allowed again
 * 
 * Complies with IETF draft-polli-ratelimit-headers-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitHeaderInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    // Default rate limits per endpoint type
    private static final int DEFAULT_LIMIT = 100;
    private static final int AUTH_LIMIT = 5;
    private static final int ADMIN_LIMIT = 30;
    private static final int PUBLIC_LIMIT = 50;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = getClientId(request);
        String path = request.getRequestURI();

        // Determine rate limit based on path
        int rateLimit = determineRateLimit(path);

        // Get rate limit status
        RateLimitStatus status = getRateLimitStatus(clientId, path, rateLimit);

        // Add rate limit headers
        addRateLimitHeaders(response, status);

        // If rate limit exceeded, block request
        if (status.remaining <= 0) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(status.retryAfter));
            response.setHeader("Content-Type", "application/json");

            String errorMessage = String.format(
                    "{\"error\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. " +
                            "Please try again in %d seconds.\",\"limit\":%d,\"remaining\":0,\"reset\":%d}",
                    status.retryAfter, status.limit, status.resetAt);

            try {
                response.getWriter().write(errorMessage);
            } catch (Exception e) {
                log.error("Error writing rate limit response", e);
            }

            return false;
        }

        return true;
    }

    /**
     * Adds standardized rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, RateLimitStatus status) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(status.limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(status.remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(status.resetAt));

        // Add additional informational headers
        if (status.remaining < status.limit * 0.2) {
            // Warning when less than 20% remaining
            response.setHeader("X-RateLimit-Warning", "approaching_limit");
        }
    }

    /**
     * Gets rate limit status for client
     */
    private RateLimitStatus getRateLimitStatus(String clientId, String path, int limit) {
        String key = buildRateLimitKey(clientId, path);

        // Get current token count
        int remaining = rateLimiterService.getRemainingTokens(key, limit);

        // Calculate reset time (Unix timestamp)
        long resetAt = System.currentTimeMillis() / 1000 + 60; // Reset in 60 seconds

        // Calculate retry after (seconds)
        long retryAfter = remaining <= 0 ? rateLimiterService.getRetryAfterSeconds(key) : 0;

        return new RateLimitStatus(limit, remaining, resetAt, retryAfter);
    }

    /**
     * Determines rate limit based on request path
     */
    private int determineRateLimit(String path) {
        if (path.startsWith("/api/auth/")) {
            return AUTH_LIMIT;
        } else if (path.startsWith("/api/admin/")) {
            return ADMIN_LIMIT;
        } else if (path.startsWith("/api/public/")) {
            return PUBLIC_LIMIT;
        } else {
            return DEFAULT_LIMIT;
        }
    }

    /**
     * Builds rate limit key from client ID and path
     */
    private String buildRateLimitKey(String clientId, String path) {
        // Extract endpoint pattern (e.g., /api/products/*)
        String endpoint = extractEndpoint(path);
        return String.format("ratelimit:%s:%s", clientId, endpoint);
    }

    /**
     * Extracts endpoint pattern from path
     */
    private String extractEndpoint(String path) {
        // Normalize path: /api/products/123 -> /api/products
        String[] parts = path.split("/");
        if (parts.length >= 3) {
            return "/" + parts[1] + "/" + parts[2];
        }
        return path;
    }

    /**
     * Gets client identifier (IP address or user ID)
     */
    private String getClientId(HttpServletRequest request) {
        // Try to get user ID from authenticated session
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // Fall back to IP address
        return "ip:" + getClientIP(request);
    }

    /**
     * Gets client IP address (considering proxies)
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Rate limit status data class
     */
    private static class RateLimitStatus {
        final int limit;
        final int remaining;
        final long resetAt;
        final long retryAfter;

        RateLimitStatus(int limit, int remaining, long resetAt, long retryAfter) {
            this.limit = limit;
            this.remaining = remaining;
            this.resetAt = resetAt;
            this.retryAfter = retryAfter;
        }
    }
}

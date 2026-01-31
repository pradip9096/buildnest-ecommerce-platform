package com.example.buildnest_ecommerce.service.ratelimit;

import java.time.Duration;

/**
 * Interface for Rate Limiter Service operations.
 * Defines contract for distributed rate limiting.
 */
public interface IRateLimiterService {

    /**
     * Check if a request is allowed under the rate limit.
     * 
     * @param key    Rate limit key (usually endpoint-based)
     * @param limit  Maximum number of requests allowed
     * @param window Time window for rate limiting
     * @return true if request is allowed, false if rate limit exceeded
     */
    boolean isAllowed(String key, int limit, Duration window);

    /**
     * Get time until rate limit resets for a given key.
     * 
     * @param key Rate limit key
     * @return Seconds until reset, or 0 if no limit active
     */
    long getTimeUntilReset(String key);

    /**
     * Reset rate limit for a specific key.
     * Useful for testing or manual intervention.
     * 
     * @param key Rate limit key to reset
     */
    void resetRateLimit(String key);
}

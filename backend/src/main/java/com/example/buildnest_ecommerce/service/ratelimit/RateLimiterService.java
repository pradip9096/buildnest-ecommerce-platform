package com.example.buildnest_ecommerce.service.ratelimit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiter Service with resilience patterns (RQ-NFR-02).
 * Uses Redis for distributed rate limiting with circuit breaker protection.
 * Implements graceful degradation when Redis is unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class RateLimiterService implements IRateLimiterService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CircuitBreaker redisCircuitBreaker;

    /**
     * Check if a request is allowed under the rate limit.
     * Protected by circuit breaker to handle Redis failures gracefully.
     * 
     * @param key    Rate limit key (usually endpoint-based)
     * @param limit  Maximum number of requests allowed
     * @param window Time window for rate limiting
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        try {
            // Apply circuit breaker protection
            return redisCircuitBreaker.executeSupplier(() -> performRateLimitCheck(key, limit, window));
        } catch (Exception e) {
            // Graceful degradation: allow request if Redis is unavailable
            log.warn("Rate limit check failed for key {}, allowing request due to Redis unavailability", key, e);
            return true;
        }
    }

    /**
     * Perform the actual rate limit check against Redis.
     * 
     * @param key    Rate limit key
     * @param limit  Maximum number of requests
     * @param window Time window
     * @return true if within limit
     */
    private Boolean performRateLimitCheck(String key, int limit, Duration window) {
        try {
            Long current = redisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                redisTemplate.expire(key, window.getSeconds(), TimeUnit.SECONDS);
            }
            boolean allowed = current != null && current <= limit;
            if (!allowed) {
                log.warn("Rate limit exceeded for key {} (count={})", key, current);
            }
            return allowed;
        } catch (Exception e) {
            log.error("Error performing rate limit check for key {}", key, e);
            throw e;
        }
    }

    /**
     * Get retry-after seconds for a rate-limited key.
     * Returns 0 if Redis is unavailable.
     * 
     * @param key Rate limit key
     * @return Seconds to wait before retry
     */
    public long getRetryAfterSeconds(String key) {
        try {
            // Apply circuit breaker protection
            return redisCircuitBreaker.executeSupplier(() -> {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                return ttl != null && ttl > 0 ? ttl : 0;
            });
        } catch (Exception e) {
            log.warn("Failed to retrieve retry-after for key {}", key, e);
            return 0;
        }
    }

    /**
     * Section 6.3 - Get remaining tokens for rate limit key
     * Used for adding X-RateLimit-Remaining header
     * 
     * @param key   Rate limit key
     * @param limit Maximum allowed requests
     * @return Number of remaining requests (0 if limit exceeded or Redis
     *         unavailable)
     */
    public int getRemainingTokens(String key, int limit) {
        try {
            return redisCircuitBreaker.executeSupplier(() -> {
                Long current = (Long) redisTemplate.opsForValue().get(key);
                if (current == null) {
                    return limit; // No requests yet, all tokens available
                }
                int remaining = (int) (limit - current);
                return Math.max(0, remaining); // Never return negative
            });
        } catch (Exception e) {
            log.warn("Failed to retrieve remaining tokens for key {}", key, e);
            return limit; // Graceful degradation: assume all tokens available
        }
    }

    @Override
    public long getTimeUntilReset(String key) {
        return getRetryAfterSeconds(key);
    }

    @Override
    public void resetRateLimit(String key) {
        try {
            redisCircuitBreaker.executeSupplier(() -> {
                redisTemplate.delete(key);
                log.info("Rate limit reset for key: {}", key);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to reset rate limit for key {}", key, e);
        }
    }
}

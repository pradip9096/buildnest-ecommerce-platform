package com.example.buildnest_ecommerce.service.ratelimit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimiterService Tests")
class RateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private CircuitBreaker redisCircuitBreaker;

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Circuit breaker setup: make it execute the supplier normally (pass-through)
        lenient().when(redisCircuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
            var supplier = invocation.getArgument(0, java.util.function.Supplier.class);
            return supplier.get();
        });

        rateLimiterService = new RateLimiterService(redisTemplate, redisCircuitBreaker);
    }

    @Test
    @DisplayName("Should allow request within rate limit")
    void testIsAllowedWithinLimit() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        Duration window = Duration.ofMinutes(1);

        when(valueOperations.increment(anyString())).thenReturn(50L);

        // Act
        boolean allowed = rateLimiterService.isAllowed(key, limit, window);

        // Assert
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Should deny request exceeding rate limit")
    void testIsAllowedExceedingLimit() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        Duration window = Duration.ofMinutes(1);

        when(valueOperations.increment(anyString())).thenReturn(101L);

        // Act
        boolean allowed = rateLimiterService.isAllowed(key, limit, window);

        // Assert
        assertFalse(allowed);
    }

    @Test
    @DisplayName("Should allow exactly at limit")
    void testIsAllowedAtExactLimit() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        Duration window = Duration.ofMinutes(1);

        when(valueOperations.increment(anyString())).thenReturn(100L);

        // Act
        boolean allowed = rateLimiterService.isAllowed(key, limit, window);

        // Assert
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Should handle first request correctly")
    void testFirstRequest() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        Duration window = Duration.ofMinutes(1);

        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // Act
        boolean allowed = rateLimiterService.isAllowed(key, limit, window);

        // Assert
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Should calculate retry-after seconds correctly")
    void testGetRetryAfterSeconds() {
        // Arrange
        String key = "user:123";
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenReturn(30L);

        // Act
        long retryAfter = rateLimiterService.getRetryAfterSeconds(key);

        // Assert
        assertEquals(30L, retryAfter);
    }

    @Test
    @DisplayName("Should return zero when no expiry set")
    void testGetRetryAfterSecondsNoExpiry() {
        // Arrange
        String key = "user:123";
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenReturn(-1L);

        // Act
        long retryAfter = rateLimiterService.getRetryAfterSeconds(key);

        // Assert
        assertEquals(0L, retryAfter);
    }

    @Test
    @DisplayName("Should calculate remaining tokens correctly")
    void testGetRemainingTokens() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        when(valueOperations.get(anyString())).thenReturn((Object) 25L);

        // Act
        long remaining = rateLimiterService.getRemainingTokens(key, limit);

        // Assert
        assertEquals(75L, remaining);
    }

    @Test
    @DisplayName("Should return full limit when no requests made")
    void testGetRemainingTokensNoRequests() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        long remaining = rateLimiterService.getRemainingTokens(key, limit);

        // Assert
        assertEquals(100L, remaining);
    }

    @Test
    @DisplayName("Should return zero remaining when limit exceeded")
    void testGetRemainingTokensExceeded() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        when(valueOperations.get(anyString())).thenReturn((Object) 150L);

        // Act
        long remaining = rateLimiterService.getRemainingTokens(key, limit);

        // Assert
        assertEquals(0L, remaining);
    }

    @Test
    @DisplayName("Should handle Redis connection failure gracefully")
    void testRedisConnectionFailure() {
        // Arrange
        String key = "user:123";
        int limit = 100;
        Duration window = Duration.ofMinutes(1);

        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        // Act & Assert - should not throw, graceful degradation
        assertDoesNotThrow(() -> rateLimiterService.isAllowed(key, limit, window));
    }

    @Test
    @DisplayName("Should use different keys for different users")
    void testDifferentKeysForDifferentUsers() {
        // Arrange
        String key1 = "user:123";
        String key2 = "user:456";
        int limit = 100;
        Duration window = Duration.ofMinutes(1);

        when(valueOperations.increment(contains("123"))).thenReturn(1L);
        when(valueOperations.increment(contains("456"))).thenReturn(1L);

        // Act
        boolean allowed1 = rateLimiterService.isAllowed(key1, limit, window);
        boolean allowed2 = rateLimiterService.isAllowed(key2, limit, window);

        // Assert
        assertTrue(allowed1);
        assertTrue(allowed2);
    }
}

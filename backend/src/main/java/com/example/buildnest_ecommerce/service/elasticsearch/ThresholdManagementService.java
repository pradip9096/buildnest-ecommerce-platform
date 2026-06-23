package com.example.buildnest_ecommerce.service.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Threshold Management Service (RQ-THR-01, RQ-THR-02, RQ-THR-03, RQ-THR-04,
 * RQ-THR-05).
 * Manages dynamic metric thresholds for alerting with Redis persistence.
 * Supports authentication, API error, and system performance thresholds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThresholdManagementService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Threshold key constants
    private static final String THRESHOLD_PREFIX = "threshold:";
    private static final String CPU_THRESHOLD_KEY = THRESHOLD_PREFIX + "cpu";
    private static final String MEMORY_THRESHOLD_KEY = THRESHOLD_PREFIX + "memory";
    private static final String ERROR_RATE_THRESHOLD_KEY = THRESHOLD_PREFIX + "error-rate";
    private static final String RESPONSE_TIME_THRESHOLD_KEY = THRESHOLD_PREFIX + "response-time";
    private static final String FAILED_LOGIN_THRESHOLD_KEY = THRESHOLD_PREFIX + "failed-logins";
    private static final String JWT_REFRESH_THRESHOLD_KEY = THRESHOLD_PREFIX + "jwt-refresh-failures";
    private static final String ADMIN_OPERATIONS_THRESHOLD_KEY = THRESHOLD_PREFIX + "admin-operations";
    private static final String HTTP_STATUS_THRESHOLD_KEY = THRESHOLD_PREFIX + "http-status:";

    // Default thresholds
    private static final double DEFAULT_CPU_THRESHOLD = 80.0;
    private static final double DEFAULT_MEMORY_THRESHOLD = 90.0;
    private static final double DEFAULT_ERROR_RATE_THRESHOLD = 5.0;
    private static final long DEFAULT_RESPONSE_TIME_THRESHOLD = 5000; // ms
    private static final int DEFAULT_FAILED_LOGIN_THRESHOLD = 5;
    private static final int DEFAULT_JWT_REFRESH_THRESHOLD = 3;
    private static final int DEFAULT_ADMIN_OPERATIONS_THRESHOLD = 30;

    /**
     * Get or create CPU threshold (RQ-THR-05).
     */
    public double getCpuThreshold() {
        Object value = redisTemplate.opsForValue().get(CPU_THRESHOLD_KEY);
        return value != null ? Double.parseDouble(value.toString()) : DEFAULT_CPU_THRESHOLD;
    }

    /**
     * Set CPU threshold dynamically (RQ-THR-01, RQ-THR-05).
     */
    public void setCpuThreshold(double threshold) {
        redisTemplate.opsForValue().set(CPU_THRESHOLD_KEY, threshold);
        log.info("CPU threshold updated to: {}%", threshold);
    }

    /**
     * Get or create memory threshold (RQ-THR-05).
     */
    public double getMemoryThreshold() {
        Object value = redisTemplate.opsForValue().get(MEMORY_THRESHOLD_KEY);
        return value != null ? Double.parseDouble(value.toString()) : DEFAULT_MEMORY_THRESHOLD;
    }

    /**
     * Set memory threshold dynamically (RQ-THR-01, RQ-THR-05).
     */
    public void setMemoryThreshold(double threshold) {
        redisTemplate.opsForValue().set(MEMORY_THRESHOLD_KEY, threshold);
        log.info("Memory threshold updated to: {}%", threshold);
    }

    /**
     * Get or create error rate threshold (RQ-THR-04).
     */
    public double getErrorRateThreshold() {
        Object value = redisTemplate.opsForValue().get(ERROR_RATE_THRESHOLD_KEY);
        return value != null ? Double.parseDouble(value.toString()) : DEFAULT_ERROR_RATE_THRESHOLD;
    }

    /**
     * Set error rate threshold dynamically (RQ-THR-01, RQ-THR-04).
     */
    public void setErrorRateThreshold(double threshold) {
        redisTemplate.opsForValue().set(ERROR_RATE_THRESHOLD_KEY, threshold);
        log.info("Error rate threshold updated to: {}%", threshold);
    }

    /**
     * Get or create response time threshold (RQ-THR-05).
     */
    public long getResponseTimeThreshold() {
        Object value = redisTemplate.opsForValue().get(RESPONSE_TIME_THRESHOLD_KEY);
        return value != null ? Long.parseLong(value.toString()) : DEFAULT_RESPONSE_TIME_THRESHOLD;
    }

    /**
     * Set response time threshold dynamically (RQ-THR-01, RQ-THR-05).
     */
    public void setResponseTimeThreshold(long threshold) {
        redisTemplate.opsForValue().set(RESPONSE_TIME_THRESHOLD_KEY, threshold);
        log.info("Response time threshold updated to: {}ms", threshold);
    }

    /**
     * Get failed login threshold (RQ-THR-03, RQ-ALRT-01).
     */
    public int getFailedLoginThreshold() {
        Object value = redisTemplate.opsForValue().get(FAILED_LOGIN_THRESHOLD_KEY);
        return value != null ? Integer.parseInt(value.toString()) : DEFAULT_FAILED_LOGIN_THRESHOLD;
    }

    /**
     * Set failed login threshold dynamically (RQ-THR-01, RQ-THR-03).
     */
    public void setFailedLoginThreshold(int threshold) {
        redisTemplate.opsForValue().set(FAILED_LOGIN_THRESHOLD_KEY, threshold);
        log.info("Failed login threshold updated to: {}", threshold);
    }

    /**
     * Get JWT refresh failure threshold (RQ-ALRT-03).
     */
    public int getJwtRefreshThreshold() {
        Object value = redisTemplate.opsForValue().get(JWT_REFRESH_THRESHOLD_KEY);
        return value != null ? Integer.parseInt(value.toString()) : DEFAULT_JWT_REFRESH_THRESHOLD;
    }

    /**
     * Set JWT refresh failure threshold dynamically (RQ-THR-01).
     */
    public void setJwtRefreshThreshold(int threshold) {
        redisTemplate.opsForValue().set(JWT_REFRESH_THRESHOLD_KEY, threshold);
        log.info("JWT refresh failure threshold updated to: {}", threshold);
    }

    /**
     * Get admin operations threshold (RQ-THR-01, RQ-ALRT-02).
     */
    public int getAdminOperationsThreshold() {
        Object value = redisTemplate.opsForValue().get(ADMIN_OPERATIONS_THRESHOLD_KEY);
        return value != null ? Integer.parseInt(value.toString()) : DEFAULT_ADMIN_OPERATIONS_THRESHOLD;
    }

    /**
     * Set admin operations threshold dynamically (RQ-THR-01).
     */
    public void setAdminOperationsThreshold(int threshold) {
        redisTemplate.opsForValue().set(ADMIN_OPERATIONS_THRESHOLD_KEY, threshold);
        log.info("Admin operations threshold updated to: {}", threshold);
    }

    /**
     * Get threshold for specific HTTP status code (RQ-THR-04).
     * Returns max count of errors allowed within time window.
     */
    public int getHttpStatusThreshold(int statusCode) {
        Object value = redisTemplate.opsForValue().get(HTTP_STATUS_THRESHOLD_KEY + statusCode);
        // Default: allow 10 errors per minute
        return value != null ? Integer.parseInt(value.toString()) : 10;
    }

    /**
     * Set threshold for specific HTTP status code (RQ-THR-01, RQ-THR-04).
     */
    public void setHttpStatusThreshold(int statusCode, int threshold) {
        redisTemplate.opsForValue().set(HTTP_STATUS_THRESHOLD_KEY + statusCode, threshold);
        log.info("HTTP status code {} threshold updated to: {} errors/min", statusCode, threshold);
    }

    /**
     * Get all current thresholds (RQ-THR-01).
     * Returns a map of all configured thresholds.
     */
    public Map<String, Object> getAllThresholds() {
        Map<String, Object> thresholds = new LinkedHashMap<>();
        thresholds.put("cpuThreshold", getCpuThreshold());
        thresholds.put("memoryThreshold", getMemoryThreshold());
        thresholds.put("errorRateThreshold", getErrorRateThreshold());
        thresholds.put("responseTimeThresholdMs", getResponseTimeThreshold());
        thresholds.put("failedLoginThreshold", getFailedLoginThreshold());
        thresholds.put("jwtRefreshThreshold", getJwtRefreshThreshold());
        thresholds.put("adminOperationsThreshold", getAdminOperationsThreshold());
        return thresholds;
    }

    /**
     * Reset all thresholds to defaults (RQ-THR-01).
     */
    public void resetAllThresholds() {
        redisTemplate.delete(Arrays.asList(
                CPU_THRESHOLD_KEY,
                MEMORY_THRESHOLD_KEY,
                ERROR_RATE_THRESHOLD_KEY,
                RESPONSE_TIME_THRESHOLD_KEY,
                FAILED_LOGIN_THRESHOLD_KEY,
                JWT_REFRESH_THRESHOLD_KEY,
                ADMIN_OPERATIONS_THRESHOLD_KEY));
        log.info("All thresholds reset to defaults");
    }
}

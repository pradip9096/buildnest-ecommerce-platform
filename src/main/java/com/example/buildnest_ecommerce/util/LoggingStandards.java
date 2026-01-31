package com.example.buildnest_ecommerce.util;

import org.slf4j.Logger;

/**
 * 3.3 MEDIUM - Logging Strategy Standardization
 * 
 * Standard logging patterns and guidelines for the BuildNest application
 * Ensures consistent, structured logging across all services
 */
public class LoggingStandards {

    /**
     * DEBUG Level Examples
     * Use for development tracing - disabled in production
     */
    public static class DebugLogging {
        public static void logMethodEntry(Logger log, String methodName, Object... args) {
            log.debug("Entering method: {}({})", methodName, args);
        }

        public static void logMethodExit(Logger log, String methodName, Object result) {
            log.debug("Exiting method: {} - Result: {}", methodName, result);
        }

        public static void logVariableValue(Logger log, String varName, Object value) {
            log.debug("Variable: {}={}", varName, value);
        }

        public static void logDatabaseQuery(Logger log, String query, Object... params) {
            log.debug("Executing query: {} with params: {}", query, params);
        }
    }

    /**
     * INFO Level Examples
     * Use for business-relevant events (logins, orders, payments, etc.)
     */
    public static class InfoLogging {
        public static void logUserLogin(Logger log, String userId, String ipAddress) {
            log.info("User login: userId={}, ipAddress={}", userId, ipAddress);
        }

        public static void logOrderCreated(Logger log, Long orderId, Long userId, java.math.BigDecimal amount) {
            log.info("Order created: orderId={}, userId={}, amount={}", orderId, userId, amount);
        }

        public static void logPaymentProcessed(Logger log, Long paymentId, Long orderId, String status) {
            log.info("Payment processed: paymentId={}, orderId={}, status={}", paymentId, orderId, status);
        }

        public static void logProductCreated(Logger log, Long productId, String name, java.math.BigDecimal price) {
            log.info("Product created: productId={}, name={}, price={}", productId, name, price);
        }

        public static void logInventoryUpdate(Logger log, Long productId, int oldStock, int newStock) {
            log.info("Inventory updated: productId={}, oldStock={}, newStock={}", productId, oldStock, newStock);
        }

        public static void logCacheHit(Logger log, String cacheName, String key) {
            log.info("Cache hit: cacheName={}, key={}", cacheName, key);
        }
    }

    /**
     * WARN Level Examples
     * Use for recoverable issues that need attention
     */
    public static class WarnLogging {
        public static void logSlowQuery(Logger log, String query, long durationMs, long threshold) {
            log.warn("Slow query detected: query={}, duration={}ms, threshold={}ms",
                    query, durationMs, threshold);
        }

        public static void logDeprecatedAPICall(Logger log, String endpoint, String replacement) {
            log.warn("Deprecated API endpoint called: endpoint={}, use {} instead", endpoint, replacement);
        }

        public static void logHighMemoryUsage(Logger log, long usedMemory, long totalMemory) {
            log.warn("High memory usage: used={}MB, total={}MB",
                    usedMemory / (1024 * 1024), totalMemory / (1024 * 1024));
        }

        public static void logRateLimitApproaching(Logger log, String endpoint, int remaining, int limit) {
            log.warn("Rate limit approaching: endpoint={}, remaining={}, limit={}",
                    endpoint, remaining, limit);
        }

        public static void logExternalServiceTimeout(Logger log, String serviceName, long timeoutMs) {
            log.warn("External service timeout: service={}, timeout={}ms", serviceName, timeoutMs);
        }

        public static void logDataValidationWarning(Logger log, String fieldName, Object value, String issue) {
            log.warn("Data validation warning: field={}, value={}, issue={}", fieldName, value, issue);
        }
    }

    /**
     * ERROR Level Examples
     * Use for failures that require immediate attention
     */
    public static class ErrorLogging {
        public static void logPaymentFailure(Logger log, Long paymentId, String reason, Throwable ex) {
            log.error("Payment processing failed: paymentId={}, reason={}", paymentId, reason, ex);
        }

        public static void logDatabaseConnectionFailure(Logger log, String reason, Throwable ex) {
            log.error("Database connection failed: reason={}", reason, ex);
        }

        public static void logExternalAPIError(Logger log, String apiName, String endpoint,
                int httpStatus, String response, Throwable ex) {
            log.error("External API error: api={}, endpoint={}, status={}, response={}",
                    apiName, endpoint, httpStatus, response, ex);
        }

        public static void logAuthenticationError(Logger log, String userId, String reason, Throwable ex) {
            log.error("Authentication error: userId={}, reason={}", userId, reason, ex);
        }

        public static void logBusinessLogicError(Logger log, String operation, String reason, Throwable ex) {
            log.error("Business logic error: operation={}, reason={}", operation, reason, ex);
        }

        public static void logDataIntegrityError(Logger log, String entity, String operation, Throwable ex) {
            log.error("Data integrity error: entity={}, operation={}", entity, operation, ex);
        }
    }

    /**
     * Guidelines for structured logging
     * 
     * ✅ CORRECT PATTERNS:
     * - Use placeholders: log.info("Order created: orderId={}, userId={}", orderId,
     * userId)
     * - Include context: Always include IDs, timestamps, user info relevant to the
     * event
     * - Use consistent naming: orderCreated, orderCancelled, etc.
     * - Log both inputs and outputs for operations
     * - Include stack traces for exceptions
     * 
     * ❌ AVOID:
     * - String concatenation: log.info("Order: " + order)
     * - Logging without context: log.error("Error occurred")
     * - Logging sensitive data: passwords, credit cards, SSNs
     * - Generic exception messages: catch (Exception e) { log.error("Error", e); }
     */

    public static class BestPractices {
        // ✅ CORRECT
        public static void goodExceptionHandling(Logger log, Exception e, long orderId) {
            log.error("Failed to process order: orderId={}, errorCode={}, errorMessage={}",
                    orderId, getErrorCode(e), e.getMessage(), e);
        }

        // ✅ CORRECT - Operation with context
        public static void goodOperationLogging(Logger log, long userId, String action, boolean success) {
            if (success) {
                log.info("Operation completed successfully: userId={}, action={}", userId, action);
            } else {
                log.error("Operation failed: userId={}, action={}", userId, action);
            }
        }

        // ✅ CORRECT - Performance monitoring
        public static void goodPerformanceLogging(Logger log, String operation, long startTime) {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000) {
                log.warn("Slow operation detected: operation={}, duration={}ms", operation, duration);
            } else {
                log.debug("Operation completed: operation={}, duration={}ms", operation, duration);
            }
        }
    }

    private static String getErrorCode(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return "INVALID_ARGUMENT";
        } else if (e instanceof java.util.NoSuchElementException) {
            return "NOT_FOUND";
        }
        return e.getClass().getSimpleName();
    }
}

package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoggingStandardsTest {

    @Test
    void debugLoggingUsesLogger() {
        Logger logger = mock(Logger.class);
        LoggingStandards.DebugLogging.logMethodEntry(logger, "method", "arg");
        LoggingStandards.DebugLogging.logMethodExit(logger, "method", "result");
        LoggingStandards.DebugLogging.logVariableValue(logger, "var", 1);
        LoggingStandards.DebugLogging.logDatabaseQuery(logger, "select", "param");

        verify(logger, atLeastOnce()).debug(any(String.class), any(), any());
    }

    @Test
    void infoWarnErrorLoggingUsesLogger() {
        Logger logger = mock(Logger.class);

        LoggingStandards.InfoLogging.logUserLogin(logger, "u1", "127.0.0.1");
        LoggingStandards.InfoLogging.logOrderCreated(logger, 1L, 2L, BigDecimal.TEN);
        LoggingStandards.InfoLogging.logPaymentProcessed(logger, 1L, 2L, "OK");
        LoggingStandards.InfoLogging.logProductCreated(logger, 1L, "p", BigDecimal.ONE);
        LoggingStandards.InfoLogging.logInventoryUpdate(logger, 1L, 10, 11);
        LoggingStandards.InfoLogging.logCacheHit(logger, "cache", "key");

        LoggingStandards.WarnLogging.logSlowQuery(logger, "select", 1000, 500);
        LoggingStandards.WarnLogging.logDeprecatedAPICall(logger, "/v1", "/v2");
        LoggingStandards.WarnLogging.logHighMemoryUsage(logger, 1024 * 1024, 2048 * 1024);
        LoggingStandards.WarnLogging.logRateLimitApproaching(logger, "/api", 1, 10);
        LoggingStandards.WarnLogging.logExternalServiceTimeout(logger, "svc", 1000);
        LoggingStandards.WarnLogging.logDataValidationWarning(logger, "field", "value", "issue");

        LoggingStandards.ErrorLogging.logPaymentFailure(logger, 1L, "reason", new RuntimeException("x"));
        LoggingStandards.ErrorLogging.logDatabaseConnectionFailure(logger, "reason", new RuntimeException("x"));
        LoggingStandards.ErrorLogging.logExternalAPIError(logger, "api", "/ep", 500, "err", new RuntimeException("x"));
        LoggingStandards.ErrorLogging.logAuthenticationError(logger, "user", "reason", new RuntimeException("x"));
        LoggingStandards.ErrorLogging.logBusinessLogicError(logger, "op", "reason", new RuntimeException("x"));
        LoggingStandards.ErrorLogging.logDataIntegrityError(logger, "entity", "op", new RuntimeException("x"));

        verify(logger, atLeastOnce()).info(any(String.class), any(), any(), any());
        verify(logger, atLeastOnce()).warn(any(String.class), any(), any(), any());
        verify(logger, atLeastOnce()).error(any(String.class), any(), any(), any());
    }

    @Test
    void bestPracticesLoggingUsesLogger() {
        Logger logger = mock(Logger.class);

        LoggingStandards.BestPractices.goodExceptionHandling(logger, new IllegalArgumentException("bad"), 1L);
        LoggingStandards.BestPractices.goodOperationLogging(logger, 2L, "op", true);
        LoggingStandards.BestPractices.goodOperationLogging(logger, 2L, "op", false);
        LoggingStandards.BestPractices.goodPerformanceLogging(logger, "op", System.currentTimeMillis());

        verify(logger, atLeastOnce()).info(any(String.class), any(), any());
        verify(logger, atLeastOnce()).error(any(String.class), any(), any(), any(), any());
    }
}

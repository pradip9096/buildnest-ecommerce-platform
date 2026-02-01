package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecureLoggerTest {

    @Test
    void sanitizeMasksSensitiveData() {
        SecureLogger logger = new SecureLogger();
        String input = "Email user@example.com card 4111-1111-1111-1111 ssn 123-45-6789 " +
                "phone 123-456-7890 password=Secret123! apiKey=abc123";

        String sanitized = logger.sanitize(input);

        assertFalse(sanitized.contains("user@example.com"));
        assertFalse(sanitized.contains("4111-1111-1111-1111"));
        assertFalse(sanitized.contains("123-45-6789"));
        assertFalse(sanitized.contains("123-456-7890"));
        assertFalse(sanitized.contains("Secret123!"));
        assertTrue(sanitized.contains("password=***"));
        assertTrue(sanitized.contains("apiKey=***"));
    }

    @Test
    void sanitizeHandlesNull() {
        SecureLogger logger = new SecureLogger();
        assertNull(logger.sanitize(null));
    }

    @Test
    void sanitizeMasksShortEmailAndLoggingHelpers() {
        SecureLogger logger = new SecureLogger();
        String input = "Email a@b.com password:Secret";

        String sanitized = logger.sanitize(input);
        assertTrue(sanitized.contains("***@***"));
        assertTrue(sanitized.contains("password=***"));

        logger.info("User %s", input);
        logger.warn("Warn %s", input);
        logger.debug("Debug %s", input);
        logger.error("Error %s", new RuntimeException("fail"), input);
    }
}

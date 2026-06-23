package com.example.buildnest_ecommerce.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Secure Logger - Sanitizes sensitive data from log messages
 * 1.6 MEDIUM - Sensitive Data Logging
 * 
 * Masks personally identifiable information (PII) and payment card data
 * to prevent accidental exposure in logs
 */
@Slf4j
@Component
public class SecureLogger {

    // Regex patterns for sensitive data detection
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern CARD_PATTERN = Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");

    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(\\d{3}[-.]?){2}\\d{4}\\b");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|passwd|pwd)[\"':=\\s]+([^\\s\"']+)");

    private static final Pattern API_KEY_PATTERN = Pattern
            .compile("(?i)(api[_-]?key|apikey|auth[_-]?token)[\"':=\\s]+([^\\s\"']+)");

    /**
     * Sanitize log message by masking sensitive data
     * 
     * @param message Original log message
     * @return Sanitized message with masked sensitive data
     */
    public String sanitize(String message) {
        if (message == null) {
            return null;
        }

        // Mask email addresses
        message = EMAIL_PATTERN.matcher(message)
                .replaceAll(m -> maskEmail(m.group()));

        // Mask credit card numbers
        message = CARD_PATTERN.matcher(message)
                .replaceAll("****-****-****-****");

        // Mask SSN
        message = SSN_PATTERN.matcher(message)
                .replaceAll("***-**-****");

        // Mask phone numbers
        message = PHONE_PATTERN.matcher(message)
                .replaceAll("***-***-****");

        // Mask passwords
        message = PASSWORD_PATTERN.matcher(message)
                .replaceAll("$1=***");

        // Mask API keys
        message = API_KEY_PATTERN.matcher(message)
                .replaceAll("$1=***");

        return message;
    }

    /**
     * Mask email showing only first letter and domain
     * Example: user@example.com → u***@example.com
     */
    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1)
            return "***@***";

        String username = email.substring(0, 1) + "***";
        String domain = email.substring(atIndex);
        return username + domain;
    }

    /**
     * Log message with automatic sanitization
     */
    public void info(String message, Object... args) {
        String sanitized = sanitize(String.format(message, args));
        log.info(sanitized);
    }

    /**
     * Log error with automatic sanitization
     */
    public void error(String message, Throwable throwable, Object... args) {
        String sanitized = sanitize(String.format(message, args));
        log.error(sanitized, throwable);
    }

    /**
     * Log warning with automatic sanitization
     */
    public void warn(String message, Object... args) {
        String sanitized = sanitize(String.format(message, args));
        log.warn(sanitized);
    }

    /**
     * Log debug with automatic sanitization
     */
    public void debug(String message, Object... args) {
        String sanitized = sanitize(String.format(message, args));
        log.debug(sanitized);
    }
}

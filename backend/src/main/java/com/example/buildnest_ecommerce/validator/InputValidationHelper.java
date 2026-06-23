package com.example.buildnest_ecommerce.validator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Section 3.2 - Input Validation Enhancement
 * Comprehensive input validation utility for all business operations
 */
@Component
public class InputValidationHelper {

    // Validation Patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$");

    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile(
            "^[0-9]{5}(-[0-9]{4})?$");

    private static final Pattern SKU_PATTERN = Pattern.compile(
            "^[A-Z0-9]{3,20}$");

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]{3,20}$");

    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9\\s\\-.,&()]{3,255}$");

    // Validation Constants
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_PRODUCT_NAME_LENGTH = 3;
    private static final int MAX_PRODUCT_NAME_LENGTH = 255;
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999.99");
    private static final int MIN_QUANTITY = 0;
    private static final int MAX_QUANTITY = 1000000;

    /**
     * Validates email address
     */
    public ValidationResult validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return ValidationResult.invalid("Email is required");
        }

        if (email.length() > 255) {
            return ValidationResult.invalid("Email must not exceed 255 characters");
        }

        if (!EMAIL_PATTERN.matcher(email.toLowerCase()).matches()) {
            return ValidationResult.invalid("Invalid email format");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates password strength
     */
    public ValidationResult validatePassword(String password) {
        if (password == null) {
            return ValidationResult.invalid("Password is required");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.invalid("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.invalid("Password must not exceed " + MAX_PASSWORD_LENGTH + " characters");
        }

        // At least one uppercase
        if (!password.matches(".*[A-Z].*")) {
            return ValidationResult.invalid("Password must contain at least one uppercase letter");
        }

        // At least one lowercase
        if (!password.matches(".*[a-z].*")) {
            return ValidationResult.invalid("Password must contain at least one lowercase letter");
        }

        // At least one digit
        if (!password.matches(".*\\d.*")) {
            return ValidationResult.invalid("Password must contain at least one digit");
        }

        // At least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/].*")) {
            return ValidationResult.invalid("Password must contain at least one special character");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates phone number (E.164 format)
     */
    public ValidationResult validatePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return ValidationResult.invalid("Phone number is required");
        }

        String sanitized = phone.replaceAll("[\\s\\-().+]", "");

        if (!PHONE_PATTERN.matcher(sanitized).matches()) {
            return ValidationResult.invalid("Invalid phone number format. Use E.164 format: +1234567890");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates postal code (US ZIP format)
     */
    public ValidationResult validatePostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return ValidationResult.invalid("Postal code is required");
        }

        if (!POSTAL_CODE_PATTERN.matcher(postalCode).matches()) {
            return ValidationResult.invalid("Invalid postal code format. Use US ZIP format: 12345 or 12345-6789");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates product name
     */
    public ValidationResult validateProductName(String name) {
        if (name == null || name.isBlank()) {
            return ValidationResult.invalid("Product name is required");
        }

        if (name.length() < MIN_PRODUCT_NAME_LENGTH) {
            return ValidationResult.invalid("Product name must be at least " + MIN_PRODUCT_NAME_LENGTH + " characters");
        }

        if (name.length() > MAX_PRODUCT_NAME_LENGTH) {
            return ValidationResult.invalid("Product name must not exceed " + MAX_PRODUCT_NAME_LENGTH + " characters");
        }

        if (!PRODUCT_NAME_PATTERN.matcher(name).matches()) {
            return ValidationResult.invalid("Product name contains invalid characters");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates product price
     */
    public ValidationResult validatePrice(BigDecimal price) {
        if (price == null) {
            return ValidationResult.invalid("Price is required");
        }

        if (price.compareTo(MIN_PRICE) < 0) {
            return ValidationResult.invalid("Price must be greater than or equal to " + MIN_PRICE);
        }

        if (price.compareTo(MAX_PRICE) > 0) {
            return ValidationResult.invalid("Price must not exceed " + MAX_PRICE);
        }

        // Check decimal places (max 2)
        if (price.scale() > 2) {
            return ValidationResult.invalid("Price must have at most 2 decimal places");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates product quantity
     */
    public ValidationResult validateQuantity(Integer quantity) {
        if (quantity == null) {
            return ValidationResult.invalid("Quantity is required");
        }

        if (quantity < MIN_QUANTITY) {
            return ValidationResult.invalid("Quantity must be at least " + MIN_QUANTITY);
        }

        if (quantity > MAX_QUANTITY) {
            return ValidationResult.invalid("Quantity must not exceed " + MAX_QUANTITY);
        }

        return ValidationResult.valid();
    }

    /**
     * Validates SKU (Stock Keeping Unit)
     */
    public ValidationResult validateSKU(String sku) {
        if (sku == null || sku.isBlank()) {
            return ValidationResult.invalid("SKU is required");
        }

        if (!SKU_PATTERN.matcher(sku.toUpperCase()).matches()) {
            return ValidationResult.invalid("SKU must be 3-20 uppercase alphanumeric characters");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates username
     */
    public ValidationResult validateUsername(String username) {
        if (username == null || username.isBlank()) {
            return ValidationResult.invalid("Username is required");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ValidationResult.invalid("Username must be 3-20 characters (alphanumeric, hyphen, underscore only)");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates order date
     */
    public ValidationResult validateOrderDate(LocalDate date) {
        if (date == null) {
            return ValidationResult.invalid("Order date is required");
        }

        if (date.isAfter(LocalDate.now())) {
            return ValidationResult.invalid("Order date cannot be in the future");
        }

        if (date.isBefore(LocalDate.now().minusYears(1))) {
            return ValidationResult.invalid("Order date cannot be more than 1 year in the past");
        }

        return ValidationResult.valid();
    }

    /**
     * Validates string length
     */
    public ValidationResult validateStringLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return ValidationResult.invalid("Value is required");
        }

        if (value.length() < minLength) {
            return ValidationResult.invalid("Value must be at least " + minLength + " characters");
        }

        if (value.length() > maxLength) {
            return ValidationResult.invalid("Value must not exceed " + maxLength + " characters");
        }

        return ValidationResult.valid();
    }

    /**
     * Sanitizes user input to prevent injection attacks
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Validation result wrapper
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}

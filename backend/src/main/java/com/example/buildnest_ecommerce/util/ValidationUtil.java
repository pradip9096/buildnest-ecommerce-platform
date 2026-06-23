package com.example.buildnest_ecommerce.util;

import org.springframework.stereotype.Component;

/**
 * 3.4 MEDIUM (implied) - Code Duplication Reduction
 * Common utility methods extracted from multiple services
 * Reduces duplication across product, order, and cart services
 */
@Component
public class ValidationUtil {
    // Data validation utilities

    /**
     * Validate product request fields
     * Centralizes product validation logic used in multiple controllers
     */
    public void validateProductRequest(String name, java.math.BigDecimal price, String sku) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name must not exceed 255 characters");
        }

        if (price == null || price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }

        if (sku != null && !sku.matches("^[A-Z0-9\\-]{3,20}$")) {
            throw new IllegalArgumentException("Invalid SKU format");
        }
    }

    /**
     * Validate cart item quantity
     * Centralizes quantity validation used in cart operations
     */
    public void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (quantity > 10000) {
            throw new IllegalArgumentException("Quantity cannot exceed 10000");
        }
    }

    /**
     * Validate email format
     */
    public void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Validate password strength - OWASP guidelines
     */
    public void validatePassword(String password) {
        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException("Password must be at least 12 characters");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password must not exceed 128 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
}

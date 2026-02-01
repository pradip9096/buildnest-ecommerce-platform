package com.example.buildnest_ecommerce.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionClassesTest {

    @Test
    void testAuthenticationExceptionWithMessage() {
        AuthenticationException exception = new AuthenticationException("Invalid credentials");

        assertEquals("Invalid credentials", exception.getMessage());
        assertEquals("AUTH_FAILED", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testAuthenticationExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        AuthenticationException exception = new AuthenticationException("Auth failed", cause);

        assertEquals("Auth failed", exception.getMessage());
        assertEquals("AUTH_FAILED", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testAuthorizationExceptionWithMessage() {
        AuthorizationException exception = new AuthorizationException("Access denied");

        assertEquals("Access denied", exception.getMessage());
        assertEquals("AUTHORIZATION_FAILED", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testAuthorizationExceptionWithCause() {
        Throwable cause = new RuntimeException("Permission error");
        AuthorizationException exception = new AuthorizationException("Not authorized", cause);

        assertEquals("Not authorized", exception.getMessage());
        assertEquals("AUTHORIZATION_FAILED", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testDuplicateResourceExceptionWithDetails() {
        DuplicateResourceException exception = new DuplicateResourceException("User", "email", "test@example.com");

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("test@example.com"));
        assertEquals("DUPLICATE_RESOURCE", exception.getErrorCode());
    }

    @Test
    void testDuplicateResourceExceptionWithMessage() {
        DuplicateResourceException exception = new DuplicateResourceException("Resource already exists");

        assertEquals("Resource already exists", exception.getMessage());
        assertEquals("DUPLICATE_RESOURCE", exception.getErrorCode());
    }

    @Test
    void testInventoryExceptionWithMessage() {
        InventoryException exception = new InventoryException("Out of stock");

        assertEquals("Out of stock", exception.getMessage());
        assertEquals("INVENTORY_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testInventoryExceptionWithCause() {
        Throwable cause = new RuntimeException("Database error");
        InventoryException exception = new InventoryException("Inventory update failed", cause);

        assertEquals("Inventory update failed", exception.getMessage());
        assertEquals("INVENTORY_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testValidationExceptionWithMessage() {
        ValidationException exception = new ValidationException("Invalid input");

        assertEquals("Invalid input", exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testValidationExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("Bad value");
        ValidationException exception = new ValidationException("Validation failed", cause);

        assertEquals("Validation failed", exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
}

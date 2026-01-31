package com.example.buildnest_ecommerce.exception;

/**
 * Exception for validation failures.
 */
public class ValidationException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }
}

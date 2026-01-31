package com.example.buildnest_ecommerce.exception;

/**
 * Exception for authentication failures.
 */
public class AuthenticationException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super("AUTH_FAILED", message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("AUTH_FAILED", message, cause);
    }
}

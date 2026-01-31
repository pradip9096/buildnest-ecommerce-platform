package com.example.buildnest_ecommerce.exception;

/**
 * Exception for authorization failures.
 */
public class AuthorizationException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super("AUTHORIZATION_FAILED", message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super("AUTHORIZATION_FAILED", message, cause);
    }
}

package com.example.buildnest_ecommerce.exception;

/**
 * 3.1 MEDIUM - Exception Handling Standardization
 * Custom exception hierarchy for BuildNest application
 */

/**
 * Base exception for all BuildNest business exceptions
 */
public class BuildNestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String errorCode;
    private final String errorMessage;

    public BuildNestException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BuildNestException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

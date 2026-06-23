package com.example.buildnest_ecommerce.exception;

/**
 * Exception for external API failures.
 */
public class ExternalServiceException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR",
                "Error calling " + serviceName + ": " + message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", message, cause);
    }
}

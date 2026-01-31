package com.example.buildnest_ecommerce.exception;

/**
 * Exception for resource not found scenarios
 * Extends BuildNestException with standardized error handling
 */
public class ResourceNotFoundException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super("RESOURCE_NOT_FOUND", resourceType + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super("RESOURCE_NOT_FOUND", message, cause);
    }
}

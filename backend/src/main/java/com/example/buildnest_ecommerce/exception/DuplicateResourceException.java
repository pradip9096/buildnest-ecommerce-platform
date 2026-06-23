package com.example.buildnest_ecommerce.exception;

/**
 * Exception for duplicate resource creation.
 */
public class DuplicateResourceException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String resourceType, String field, String value) {
        super("DUPLICATE_RESOURCE",
                resourceType + " with " + field + " '" + value + "' already exists");
    }

    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
}

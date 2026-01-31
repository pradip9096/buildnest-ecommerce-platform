package com.example.buildnest_ecommerce.exception;

/**
 * Exception for inventory-related issues.
 */
public class InventoryException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public InventoryException(String message) {
        super("INVENTORY_ERROR", message);
    }

    public InventoryException(String message, Throwable cause) {
        super("INVENTORY_ERROR", message, cause);
    }
}

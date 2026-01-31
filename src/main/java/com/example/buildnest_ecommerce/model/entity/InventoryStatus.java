package com.example.buildnest_ecommerce.model.entity;

/**
 * Inventory status classification (RQ-INV-STAT-01, RQ-INV-STAT-02,
 * RQ-INV-STAT-03).
 */
public enum InventoryStatus {
    IN_STOCK("In Stock", "Inventory level is healthy"),
    LOW_STOCK("Low Stock", "Inventory level is below threshold but not empty"),
    OUT_OF_STOCK("Out of Stock", "Inventory level is zero");

    private final String displayName;
    private final String description;

    InventoryStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

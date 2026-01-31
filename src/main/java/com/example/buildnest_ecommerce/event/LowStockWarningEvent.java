package com.example.buildnest_ecommerce.event;

import org.springframework.context.ApplicationEvent;

/**
 * Fired when inventory level is critically low.
 */
public class LowStockWarningEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final Long productId;
    private final String productName;
    private final int currentStock;
    private final int minimumStock;

    public LowStockWarningEvent(Object source, Long productId, String productName,
            int currentStock, int minimumStock) {
        super(source);
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.minimumStock = minimumStock;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getMinimumStock() {
        return minimumStock;
    }
}

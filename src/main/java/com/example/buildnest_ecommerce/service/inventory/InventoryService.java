package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import java.util.List;

public interface InventoryService {
    Inventory addStock(Long productId, Integer stock);

    Inventory getInventoryByProductId(Long productId);

    Inventory updateStock(Long productId, Integer quantity);

    void deductStock(Long productId, Integer quantity);

    boolean hasStock(Long productId, Integer quantity);

    /**
     * Get inventory status classification (RQ-INV-STAT-01).
     */
    InventoryStatus getInventoryStatus(Long productId);

    /**
     * Get all low stock products (RQ-INV-MON-03, RQ-INV-STAT-02).
     */
    List<Inventory> getLowStockProducts();

    /**
     * Get all out of stock products (RQ-INV-MON-03, RQ-INV-STAT-03).
     */
    List<Inventory> getOutOfStockProducts();

    /**
     * Get all products below threshold (RQ-INV-REP-01).
     */
    List<Inventory> getProductsBelowThreshold();

    /**
     * Check if product is below threshold (RQ-INV-MON-02).
     */
    boolean isBelowThreshold(Long productId);
}

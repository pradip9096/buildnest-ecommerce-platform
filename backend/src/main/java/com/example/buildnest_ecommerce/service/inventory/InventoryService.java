package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.dto.InventoryDTO;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * List all inventory records with product summary (ADM-06, #72).
     */
    List<InventoryDTO> getAllInventorySummary();

    /**
     * Apply a delta (positive or negative) adjustment to inventory quantity,
     * recording the reason and actor in inventory_audit_log (ADM-06, #72).
     *
     * @throws IllegalArgumentException if the resulting quantity
     *         would be negative
     */
    Inventory adjustStock(
            Long productId, int delta, String reason, Long changedByUserId);

    /**
     * Reserve stock for a checkout session (INV-01, #73).
     * Increments quantityReserved and sets reservationExpiresAt.
     * Uses optimistic locking — throws
     * OptimisticLockingFailureException on concurrent conflict.
     *
     * @throws com.example.buildnest_ecommerce.exception.
     *         InventoryException if available quantity is
     *         insufficient
     */
    void reserveStock(
            Long productId, Integer quantity, LocalDateTime expiresAt);

    /**
     * Release a reservation back to the available pool (INV-01, #73).
     * Decrements quantityReserved. Safe to call even if no reservation is held.
     */
    void releaseReservation(Long productId, Integer quantity);

    /**
     * Release all expired reservations (INV-01, #73).
     * Called by the cleanup scheduler every minute.
     */
    void releaseExpiredReservations();

    /**
     * List inventory for products owned by the given seller
     * (FR-SEL-05, #556) — mirrors ProductService's
     * getProductsForSeller pattern from #555.
     */
    Page<InventoryDTO> getInventoryForSeller(
            Long sellerUserId, Pageable pageable);

    /**
     * Apply a stock delta scoped to a seller's own product
     * (FR-SEL-05, #556) — rejects the adjustment if the product
     * does not belong to the given seller.
     *
     * @throws com.example.buildnest_ecommerce.exception.
     *         ResourceNotFoundException if the product is not
     *         found or not owned by this seller
     * @throws IllegalArgumentException if the resulting quantity
     *         would be negative
     */
    Inventory adjustStockForSeller(
            Long sellerUserId, Long productId, int delta, String reason);
}

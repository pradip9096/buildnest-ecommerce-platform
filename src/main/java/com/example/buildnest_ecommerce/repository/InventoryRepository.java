package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);

    /**
     * Find inventory by product ID (RQ-INV-ANA-02).
     */
    Inventory findByProductId(Long productId);

    /**
     * Find all products with specific status (RQ-INV-STAT-01, RQ-INV-STAT-02,
     * RQ-INV-STAT-03).
     */
    List<Inventory> findByStatus(InventoryStatus status);

    /**
     * Find products below minimum threshold (RQ-INV-MON-02, RQ-INV-REP-01).
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock < i.minimumStockLevel AND i.quantityInStock > 0")
    List<Inventory> findLowStockProducts();

    /**
     * Find out of stock products (RQ-INV-STAT-03, RQ-INV-REP-01).
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock = 0")
    List<Inventory> findOutOfStockProducts();

    /**
     * Find all products below threshold (RQ-INV-REP-01).
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock <= i.minimumStockLevel")
    List<Inventory> findBelowThresholdProducts();
}

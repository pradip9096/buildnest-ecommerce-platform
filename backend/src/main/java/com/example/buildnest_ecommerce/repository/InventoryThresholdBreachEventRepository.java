package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.InventoryThresholdBreachEvent;
import com.example.buildnest_ecommerce.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for inventory threshold breach events (RQ-INV-DATA-02,
 * RQ-INV-REP-02).
 */
@Repository
public interface InventoryThresholdBreachEventRepository extends JpaRepository<InventoryThresholdBreachEvent, Long> {

    /**
     * Find all breaches for a product (RQ-INV-REP-02).
     */
    List<InventoryThresholdBreachEvent> findByProduct(Product product);

    /**
     * Find breaches within time range (RQ-INV-REP-02).
     */
    List<InventoryThresholdBreachEvent> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find breaches for product within time range (RQ-INV-REP-02).
     */
    List<InventoryThresholdBreachEvent> findByProductAndCreatedAtBetween(Product product, LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Find recent breaches for all products (RQ-INV-REP-01).
     */
    @Query("SELECT e FROM InventoryThresholdBreachEvent e WHERE e.createdAt >= :startDate ORDER BY e.createdAt DESC")
    List<InventoryThresholdBreachEvent> findRecentBreaches(@Param("startDate") LocalDateTime startDate);
}

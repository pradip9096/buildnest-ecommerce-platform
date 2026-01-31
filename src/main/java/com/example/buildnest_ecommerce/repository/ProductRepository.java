package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

        /**
         * Find products with stock below threshold for inventory monitoring
         */
        @Query("SELECT p FROM Product p WHERE " +
                        "(COALESCE(p.inventory.quantityInStock, 0) - COALESCE(p.inventory.quantityReserved, 0)) < :threshold "
                        +
                        "AND p.isActive = true")
        List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

        /**
         * Find products by name (case-insensitive search) with eager loading.
         * Uses EntityGraph to prevent N+1 queries when accessing category and
         * inventory.
         */
        @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
        @EntityGraph(attributePaths = { "category", "inventory" })
        List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

        /**
         * Find product by ID with eager loading of category and inventory.
         * Prevents N+1 queries when accessing product relationships.
         */
        @EntityGraph(attributePaths = { "category", "inventory" })
        Optional<Product> findById(Long id);

        /**
         * Find all active products with eager loading of related entities.
         * Prevents N+1 queries for bulk product retrieval.
         */
        @EntityGraph(attributePaths = { "category", "inventory" })
        List<Product> findByIsActiveTrue();

        /**
         * Advanced search with multiple filters
         * Supports filtering by name, category, price range, and stock status
         * 
         * Section 6.1.3: Advanced search implementation
         * Section 2.3.1: Uses index hints for performance optimization
         */
        @Query("""
                        SELECT p FROM Product p
                        WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
                        AND (:categoryId IS NULL OR p.category.id = :categoryId)
                        AND (:minPrice IS NULL OR p.price >= :minPrice)
                        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                                    AND (:inStock IS NULL OR (:inStock = false OR
                                            (COALESCE(p.inventory.quantityInStock, 0) - COALESCE(p.inventory.quantityReserved, 0)) > 0))
                        AND (:isActive IS NULL OR p.isActive = :isActive)
                        """)
        @EntityGraph(attributePaths = { "category", "inventory" })
        Page<Product> advancedSearch(
                        @Param("query") String query,
                        @Param("categoryId") Long categoryId,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("inStock") Boolean inStock,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        /**
         * Find products with low stock (inventory optimization)
         * Used for inventory management and reorder alerts
         * Section 2.3: Performance optimization - database queries
         */
        @Query("SELECT p FROM Product p WHERE " +
                        "(COALESCE(p.inventory.quantityInStock, 0) - COALESCE(p.inventory.quantityReserved, 0)) <= :threshold "
                        +
                        "AND p.isActive = true")
        @EntityGraph(attributePaths = { "inventory" })
        List<Product> findLowStockByInventory(@Param("threshold") Integer threshold);

        /**
         * Find products by category with pagination
         */
        @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
        @EntityGraph(attributePaths = { "category", "inventory" })
        Page<Product> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

        /**
         * Find products expiring soon (for perishable goods)
         */
        @Query("SELECT p FROM Product p WHERE p.expiryDate IS NOT NULL " +
                        "AND p.expiryDate <= :cutoff " +
                        "AND p.isActive = true")
        List<Product> findExpiringSoonByDate(@Param("cutoff") LocalDate cutoff);

        default List<Product> findExpiringSoon(Integer days) {
                int safeDays = days == null ? 0 : Math.max(0, days);
                return findExpiringSoonByDate(LocalDate.now().plusDays(safeDays));
        }

        /**
         * Calculate total revenue by product (sales optimization)
         */
        @Query("SELECT SUM(oi.quantity * oi.price) FROM OrderItem oi " +
                        "WHERE oi.product.id = :productId")
        BigDecimal calculateProductRevenue(@Param("productId") Long productId);
}

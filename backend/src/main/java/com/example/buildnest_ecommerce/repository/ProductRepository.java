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
         * Count products belonging to a category, used to block category deletion
         * while products still reference it (ADM-02, #68).
         */
        long countByCategoryId(Long categoryId);

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
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

        /**
         * Find product by ID with eager loading of category and inventory.
         * Prevents N+1 queries when accessing product relationships.
         */
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        Optional<Product> findById(Long id);

        /**
         * Find a product by ID scoped to its owning seller (FR-SEL-04) —
         * used to enforce that a seller can only update/delete their own
         * listings; empty if the product doesn't exist or belongs to a
         * different seller.
         */
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        Optional<Product> findByIdAndSeller_Id(Long id, Long sellerId);

        /**
         * Paginated listing of a seller's own product catalogue
         * (FR-SEL-04).
         */
        @EntityGraph(attributePaths = { "category", "inventory" })
        Page<Product> findBySeller_Id(Long sellerId, Pageable pageable);

        /**
         * Find all active products with eager loading of related entities.
         * Prevents N+1 queries for bulk product retrieval.
         */
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        List<Product> findByIsActiveTrue();

        /**
         * Find active, admin-curated featured products for home page merchandising.
         */
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        List<Product> findByIsFeaturedTrueAndIsActiveTrue();

        /**
         * Advanced search with multiple filters.
         * Supports filtering by name, category, price range, stock status,
         * and tag.
         *
         * Section 6.1.3: Advanced search implementation
         * Section 2.3.1: Uses index hints for performance optimization
         *
         * @param query optional name/description search term
         * @param categoryId optional category filter
         * @param minPrice optional minimum price filter
         * @param maxPrice optional maximum price filter
         * @param inStock optional in-stock-only filter
         * @param isActive optional active/inactive filter
         * @param tag optional tag name filter
         * @param pageable pagination and sort parameters
         * @return the matching page of products
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
                        AND (:tag IS NULL OR EXISTS (SELECT 1 FROM p.tags t WHERE t.name = :tag))
                        """)
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        Page<Product> advancedSearch(
                        @Param("query") String query,
                        @Param("categoryId") Long categoryId,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("inStock") Boolean inStock,
                        @Param("isActive") Boolean isActive,
                        @Param("tag") String tag,
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
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
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

        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        Page<Product> findAll(Pageable pageable);

        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        List<Product> findAll();

        /**
         * Calculate total revenue by product (sales optimization)
         */
        @Query("SELECT SUM(oi.quantity * oi.price) FROM OrderItem oi " +
                        "WHERE oi.product.id = :productId")
        BigDecimal calculateProductRevenue(@Param("productId") Long productId);

        /**
         * Find products related to {@code productId}: same category ranked
         * ahead of shared-tag matches, excluding the source product and any
         * inactive/out-of-stock product (PROD-04, #84).
         *
         * @param productId the source product's ID, excluded from results
         * @param categoryId the source product's category ID, may be null
         * @param tagIds the source product's tag IDs; must be non-empty —
         *               callers pass a sentinel value when the source
         *               product has no tags, since an empty JPQL
         *               {@code IN} collection is invalid
         * @param pageable caps the result size (e.g. top 8)
         * @return the ranked list of related products
         */
        @Query("""
                        SELECT p FROM Product p
                        WHERE p.id <> :productId
                        AND p.isActive = true
                        AND (COALESCE(p.inventory.quantityInStock, 0)
                                - COALESCE(p.inventory.quantityReserved, 0)) > 0
                        AND (p.category.id = :categoryId
                                OR EXISTS (SELECT 1 FROM p.tags t
                                        WHERE t.id IN :tagIds))
                        ORDER BY CASE WHEN p.category.id = :categoryId
                                THEN 0 ELSE 1 END, p.id
                        """)
        @EntityGraph(attributePaths = { "category", "inventory", "variants" })
        List<Product> findRelatedProducts(
                        @Param("productId") Long productId,
                        @Param("categoryId") Long categoryId,
                        @Param("tagIds") List<Long> tagIds,
                        Pageable pageable);
}

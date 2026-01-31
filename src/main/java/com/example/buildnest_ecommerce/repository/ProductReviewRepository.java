package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Review Repository
 * Handles data access operations for product reviews and ratings
 */
@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    /**
     * Find all reviews for a specific product
     */
    Page<ProductReview> findByProductIdAndIsVisibleTrue(Long productId, Pageable pageable);

    /**
     * Find all reviews by a specific user
     */
    Page<ProductReview> findByUserId(Long userId, Pageable pageable);

    /**
     * Check if user has reviewed a product
     */
    boolean existsByProductIdAndUserId(Long productId, Long userId);

    /**
     * Find user's review for a specific product
     */
    Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

    /**
     * Calculate average rating for a product
     */
    @Query("SELECT AVG(r.rating) FROM ProductReview r " +
            "WHERE r.product.id = :productId AND r.isVisible = true")
    Double calculateAverageRating(@Param("productId") Long productId);

    /**
     * Count reviews for a product
     */
    long countByProductIdAndIsVisibleTrue(Long productId);

    /**
     * Find top helpful reviews for a product
     */
    @Query("SELECT r FROM ProductReview r " +
            "WHERE r.product.id = :productId AND r.isVisible = true " +
            "ORDER BY r.helpfulCount DESC, r.createdAt DESC")
    Page<ProductReview> findTopHelpfulReviews(@Param("productId") Long productId, Pageable pageable);

    /**
     * Find verified purchase reviews
     */
    @Query("SELECT r FROM ProductReview r " +
            "WHERE r.product.id = :productId AND r.verifiedPurchase = true " +
            "AND r.isVisible = true " +
            "ORDER BY r.createdAt DESC")
    Page<ProductReview> findVerifiedPurchaseReviews(@Param("productId") Long productId, Pageable pageable);

    /**
     * Get rating distribution for a product
     */
    @Query("SELECT r.rating, COUNT(r) FROM ProductReview r " +
            "WHERE r.product.id = :productId AND r.isVisible = true " +
            "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);
}

package com.example.buildnest_ecommerce.service.review;

import com.example.buildnest_ecommerce.model.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Product Review Service Interface
 * Defines operations for managing product reviews and ratings
 */
public interface ProductReviewService {

    /**
     * Create a new product review
     */
    ProductReview createReview(Long productId, Long userId, Integer rating,
            String comment, boolean verifiedPurchase);

    /**
     * Update an existing review
     */
    ProductReview updateReview(Long reviewId, Integer rating, String comment);

    /**
     * Delete a review
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * Get reviews for a product
     */
    Page<ProductReview> getProductReviews(Long productId, Pageable pageable);

    /**
     * Get user's reviews
     */
    Page<ProductReview> getUserReviews(Long userId, Pageable pageable);

    /**
     * Mark review as helpful
     */
    ProductReview markAsHelpful(Long reviewId);

    /**
     * Get average rating for a product
     */
    Double getAverageRating(Long productId);

    /**
     * Get rating distribution
     */
    Map<Integer, Long> getRatingDistribution(Long productId);

    /**
     * Get top helpful reviews
     */
    Page<ProductReview> getTopHelpfulReviews(Long productId, Pageable pageable);

    /**
     * Get verified purchase reviews
     */
    Page<ProductReview> getVerifiedPurchaseReviews(Long productId, Pageable pageable);

    /**
     * Check if user has purchased product (for verified purchase badge)
     */
    boolean hasUserPurchasedProduct(Long userId, Long productId);
}

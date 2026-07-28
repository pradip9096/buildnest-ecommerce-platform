package com.example.buildnest_ecommerce.service.review;

import com.example.buildnest_ecommerce.model.entity.SellerReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Seller Review Service Interface (FR-SEL-07, #558)
 */
public interface SellerReviewService {

    SellerReview createReview(Long sellerId, Long userId, Integer rating,
            String comment, boolean verifiedPurchase);

    SellerReview updateReview(Long reviewId, Long userId, Integer rating,
            String comment);

    void deleteReview(Long reviewId, Long userId);

    Page<SellerReview> getSellerReviews(Long sellerId, Pageable pageable);

    Page<SellerReview> getUserReviews(Long userId, Pageable pageable);

    Double getAverageRating(Long sellerId);

    Map<Integer, Long> getRatingDistribution(Long sellerId);

    boolean hasUserPurchasedFromSeller(Long userId, Long sellerId);
}

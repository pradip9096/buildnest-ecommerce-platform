package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductReviewTest {

    @Test
    void incrementsHelpfulCountAndUpdatesTimestamps() {
        ProductReview review = ProductReview.builder()
                .rating(5)
                .comment("Great")
                .helpfulCount(0)
                .build();

        review.onCreate();
        LocalDateTime createdAt = review.getCreatedAt();
        assertNotNull(createdAt);
        assertNotNull(review.getUpdatedAt());

        review.incrementHelpfulCount();
        assertEquals(1, review.getHelpfulCount());
        assertNotNull(review.getUpdatedAt());
    }

    @Test
    void evaluatesPositiveAndNegativeReviews() {
        ProductReview positive = new ProductReview();
        positive.setRating(5);
        assertTrue(positive.isPositiveReview());
        assertFalse(positive.isNegativeReview());

        ProductReview negative = new ProductReview();
        negative.setRating(1);
        assertTrue(negative.isNegativeReview());
        assertFalse(negative.isPositiveReview());
    }

    @Test
    void onUpdateRefreshesTimestamp() {
        ProductReview review = new ProductReview();
        review.setUpdatedAt(LocalDateTime.now().minusDays(1));
        LocalDateTime before = review.getUpdatedAt();

        review.onUpdate();
        assertNotNull(review.getUpdatedAt());
        assertTrue(review.getUpdatedAt().isAfter(before) || review.getUpdatedAt().isEqual(before));
    }
}

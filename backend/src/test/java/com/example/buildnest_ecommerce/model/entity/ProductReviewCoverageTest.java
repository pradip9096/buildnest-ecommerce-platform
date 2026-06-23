package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductReviewCoverageTest {

    private static Product product(long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product" + id);
        return product;
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("secret");
        return user;
    }

    @Test
    void equalityAndHelperMethodsCoverNeutralRating() {
        LocalDateTime now = LocalDateTime.now();

        ProductReview base = ProductReview.builder()
                .id(1L)
                .product(product(1L))
                .user(user(2L))
                .rating(3)
                .comment("ok")
                .createdAt(now)
                .updatedAt(now)
                .helpfulCount(1)
                .verifiedPurchase(true)
                .isVisible(true)
                .build();

        ProductReview same = ProductReview.builder()
                .id(1L)
                .product(product(1L))
                .user(user(2L))
                .rating(3)
                .comment("ok")
                .createdAt(now)
                .updatedAt(now)
                .helpfulCount(1)
                .verifiedPurchase(true)
                .isVisible(true)
                .build();

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, null);
        assertNotEquals(base, "not-review");

        assertFalse(base.isPositiveReview());
        assertFalse(base.isNegativeReview());

        ProductReview diffRating = ProductReview.builder()
                .id(1L)
                .product(product(1L))
                .user(user(2L))
                .rating(5)
                .comment("ok")
                .createdAt(now)
                .updatedAt(now)
                .helpfulCount(1)
                .verifiedPurchase(true)
                .isVisible(true)
                .build();

        assertNotEquals(base, diffRating);
    }

    @Test
    void onCreateAndIncrementHelpfulCountUpdateTimestamps() {
        ProductReview review = new ProductReview();
        review.setRating(4);
        review.onCreate();
        assertNotNull(review.getCreatedAt());
        assertNotNull(review.getUpdatedAt());

        LocalDateTime before = review.getUpdatedAt();
        review.incrementHelpfulCount();
        assertEquals(1, review.getHelpfulCount());
        assertTrue(review.getUpdatedAt().isAfter(before) || review.getUpdatedAt().isEqual(before));
    }
}

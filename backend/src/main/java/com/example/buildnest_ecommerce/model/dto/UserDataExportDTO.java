package com.example.buildnest_ecommerce.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GDPR right-to-access export (#128, COMP-01). Every nested row is a
 * flat projection of the source entity's own fields -- never the raw
 * entity -- so this never triggers a lazy-association or
 * bidirectional-cycle Jackson failure regardless of session state.
 */
public record UserDataExportDTO(
        Profile profile,
        List<AddressRow> addresses,
        List<OrderRow> orders,
        List<ProductReviewRow> productReviews,
        List<SellerReviewRow> sellerReviews,
        List<String> wishlistProductNames,
        List<String> cartItemProductNames) {

    public record Profile(
            Long id,
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            LocalDateTime createdAt,
            LocalDateTime lastLogin,
            Boolean consentGiven,
            LocalDateTime consentAt) {
    }

    public record AddressRow(
            String streetAddress,
            String city,
            String state,
            String postalCode,
            String country,
            String addressType) {
    }

    public record OrderRow(
            String orderNumber,
            String status,
            BigDecimal totalAmount,
            LocalDateTime createdAt) {
    }

    public record ProductReviewRow(
            Long productId,
            Integer rating,
            String comment,
            LocalDateTime createdAt) {
    }

    public record SellerReviewRow(
            Long sellerId,
            Integer rating,
            String comment,
            LocalDateTime createdAt) {
    }
}

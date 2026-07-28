package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Seller Review Entity (FR-SEL-07, #558) — buyer-to-seller rating, distinct
 * from {@link ProductReview} (per-product). {@code seller} references
 * {@link User}, not {@link Seller} — mirrors {@code Product.seller} and
 * {@code SellerOrderController}'s own sellerId=User.id convention, since a
 * seller's storefront identity for review purposes is the User account, not
 * the separate business-profile Seller extension table.
 */
@Entity
@Table(name = "seller_review", indexes = {
        @Index(name = "idx_seller_review_seller_id", columnList = "seller_id"),
        @Index(name = "idx_seller_review_user_id", columnList = "user_id"),
        @Index(name = "idx_seller_review_rating", columnList = "rating")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "seller", "user", "createdAt", "updatedAt" })
@ToString(exclude = { "seller", "user" })
public class SellerReview implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 2000, message = "Review comment cannot exceed 2000 characters")
    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "verified_purchase")
    @Builder.Default
    private Boolean verifiedPurchase = false;

    @Column(name = "is_visible")
    @Builder.Default
    private Boolean isVisible = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

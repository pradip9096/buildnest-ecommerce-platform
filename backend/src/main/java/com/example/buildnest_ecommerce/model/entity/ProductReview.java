package com.example.buildnest_ecommerce.model.entity;

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
 * Product Review Entity
 * Represents customer reviews and ratings for products
 * Implements Section 6.1 - E-Commerce Features from
 * EXHAUSTIVE_RECOMMENDATION_REPORT
 */
@Entity
@Table(name = "product_review", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_product_review_user_id", columnList = "user_id"),
        @Index(name = "idx_rating", columnList = "rating"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "product", "user", "createdAt", "updatedAt" })
@ToString(exclude = { "product", "user" })
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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

    @Column(name = "helpful_count", columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer helpfulCount = 0;

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

    /**
     * Increment helpful count when users mark review as helpful
     */
    public void incrementHelpfulCount() {
        this.helpfulCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this is a positive review (4-5 stars)
     */
    public boolean isPositiveReview() {
        return this.rating >= 4;
    }

    /**
     * Check if this is a negative review (1-2 stars)
     */
    public boolean isNegativeReview() {
        return this.rating <= 2;
    }
}

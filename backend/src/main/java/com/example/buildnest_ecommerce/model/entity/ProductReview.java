package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Product Review Entity
 * Represents customer reviews and ratings for products
 * Implements Section 6.1 - E-Commerce Features from
 * EXHAUSTIVE_RECOMMENDATION_REPORT
 * Shared fields (rating/comment/timestamps/visibility) live in
 * {@link AbstractReview} (#558, resolving a SonarCloud duplication gate
 * between this class and the sibling {@link SellerReview}).
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
@SuperBuilder
@EqualsAndHashCode(callSuper = false,
        exclude = { "product", "user" })
@ToString(exclude = { "product", "user" })
public class ProductReview extends AbstractReview implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The caller already knows which product this review belongs to (it's
    // the path parameter) — never needed nested, and Product carries its own
    // lazy tags/variants collections that would throw serializing this raw
    // entity post-transaction (#441; same defect family as the ProductImage/
    // ProductVariant `product` back-references elsewhere in this codebase).
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "helpful_count", columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer helpfulCount = 0;

    /**
     * Increment helpful count when users mark review as helpful
     */
    public void incrementHelpfulCount() {
        this.helpfulCount++;
        setUpdatedAt(java.time.LocalDateTime.now());
    }

    /**
     * Check if this is a positive review (4-5 stars)
     */
    public boolean isPositiveReview() {
        return getRating() >= 4;
    }

    /**
     * Check if this is a negative review (1-2 stars)
     */
    public boolean isNegativeReview() {
        return getRating() <= 2;
    }
}

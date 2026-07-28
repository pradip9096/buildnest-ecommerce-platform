package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Seller Review Entity (FR-SEL-07, #558) — buyer-to-seller rating, distinct
 * from {@link ProductReview} (per-product). {@code seller} references
 * {@link User}, not {@link Seller} — mirrors {@code Product.seller} and
 * {@code SellerOrderController}'s own sellerId=User.id convention, since a
 * seller's storefront identity for review purposes is the User account, not
 * the separate business-profile Seller extension table. Shared fields
 * (rating/comment/timestamps/visibility) live in {@link AbstractReview}.
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
@SuperBuilder
@EqualsAndHashCode(callSuper = false,
        exclude = { "seller", "user" })
@ToString(exclude = { "seller", "user" })
public class SellerReview extends AbstractReview implements AggregateRoot {

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
}

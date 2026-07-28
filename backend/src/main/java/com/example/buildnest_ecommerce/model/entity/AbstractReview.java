package com.example.buildnest_ecommerce.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shared fields/lifecycle callbacks between {@link ProductReview} and
 * {@link SellerReview} — extracted to resolve a real SonarCloud new-code
 * duplication gate failure (#558; both entities had byte-for-byte
 * identical rating/comment/timestamp/visibility fields and
 * {@code @PrePersist}/{@code @PreUpdate} bodies). {@code @SuperBuilder}
 * (not plain {@code @Builder}) is required so subclasses' existing
 * {@code .builder()...build()} call sites keep working unchanged —
 * plain {@code @Builder} does not include inherited fields.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractReview {

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
    @lombok.Builder.Default
    private Boolean verifiedPurchase = false;

    @Column(name = "is_visible")
    @lombok.Builder.Default
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

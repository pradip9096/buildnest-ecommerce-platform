package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.SellerReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Seller Review Repository (FR-SEL-07, #558) — mirrors
 * {@code ProductReviewRepository} exactly, scoped by sellerId (User.id)
 * instead of productId.
 */
@Repository
public interface SellerReviewRepository
        extends JpaRepository<SellerReview, Long> {

    Page<SellerReview> findBySellerIdAndIsVisibleTrue(
            Long sellerId, Pageable pageable);

    Page<SellerReview> findByUserId(
            Long userId, Pageable pageable);

    boolean existsBySellerIdAndUserId(Long sellerId, Long userId);

    Optional<SellerReview> findBySellerIdAndUserId(Long sellerId, Long userId);

    @Query("SELECT AVG(r.rating) FROM SellerReview r " +
            "WHERE r.seller.id = :sellerId AND r.isVisible = true")
    Double calculateAverageRating(@Param("sellerId") Long sellerId);

    long countBySellerIdAndIsVisibleTrue(Long sellerId);

    @Query("SELECT r.rating, COUNT(r) FROM SellerReview r " +
            "WHERE r.seller.id = :sellerId AND r.isVisible = true " +
            "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("sellerId") Long sellerId);
}

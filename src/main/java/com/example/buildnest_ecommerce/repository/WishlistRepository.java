package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Wishlist Repository
 * Handles data access operations for user wishlists
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Find wishlist by user ID
     */
    Optional<Wishlist> findByUserId(Long userId);

    /**
     * Check if a product exists in user's wishlist
     */
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END " +
            "FROM Wishlist w JOIN w.products p " +
            "WHERE w.user.id = :userId AND p.id = :productId")
    boolean existsByUserIdAndProductId(@Param("userId") Long userId,
            @Param("productId") Long productId);

    /**
     * Count products in wishlist
     */
    @Query("SELECT COUNT(p) FROM Wishlist w JOIN w.products p WHERE w.user.id = :userId")
    long countProductsByUserId(@Param("userId") Long userId);
}

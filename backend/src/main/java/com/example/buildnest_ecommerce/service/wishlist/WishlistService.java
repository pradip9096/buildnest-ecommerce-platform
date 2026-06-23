package com.example.buildnest_ecommerce.service.wishlist;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Wishlist;

import java.util.Set;

/**
 * Wishlist Service Interface
 * Defines operations for managing user wishlists
 */
public interface WishlistService {

    /**
     * Add product to user's wishlist
     */
    Wishlist addProduct(Long userId, Long productId);

    /**
     * Remove product from user's wishlist
     */
    Wishlist removeProduct(Long userId, Long productId);

    /**
     * Get user's wishlist
     */
    Wishlist getWishlist(Long userId);

    /**
     * Get all products in user's wishlist
     */
    Set<Product> getWishlistProducts(Long userId);

    /**
     * Check if product is in wishlist
     */
    boolean isProductInWishlist(Long userId, Long productId);

    /**
     * Clear all products from wishlist
     */
    void clearWishlist(Long userId);

    /**
     * Get wishlist product count
     */
    long getWishlistCount(Long userId);
}

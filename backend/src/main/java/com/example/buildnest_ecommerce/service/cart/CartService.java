package com.example.buildnest_ecommerce.service.cart;

import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.payload.CartResponseDTO;

public interface CartService {
    Cart addToCart(Long userId, Long productId, Integer quantity);

    /**
     * Add a product to cart, optionally pinned to a specific variant (PROD-01, #81).
     * @param variantId nullable — pass null for products without variants
     */
    Cart addToCart(Long userId, Long productId, Long variantId, Integer quantity);
    CartResponseDTO getCartByUserId(Long userId);
    void removeItemFromCart(Long cartItemId, Long requestingUserId);
    void clearCart(Long userId);
    Double getCartTotal(Long userId);
}

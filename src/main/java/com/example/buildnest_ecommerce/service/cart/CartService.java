package com.example.buildnest_ecommerce.service.cart;

import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.payload.CartResponseDTO;

public interface CartService {
    Cart addToCart(Long userId, Long productId, Integer quantity);
    CartResponseDTO getCartByUserId(Long userId);
    void removeItemFromCart(Long cartItemId);
    void clearCart(Long userId);
    Double getCartTotal(Long userId);
}

package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.entity.Order;

public interface CheckoutService {
    /**
     * Process checkout and create order from cart
     * @param userId User performing checkout
     * @param cartId Cart to checkout
     * @return Created Order
     */
    Order checkoutCart(Long userId, Long cartId);
    
    /**
     * Process checkout with payment method
     * @param userId User performing checkout
     * @param cartId Cart to checkout
     * @param request Checkout details including payment method
     * @return Created Order
     */
    Order checkoutWithPayment(Long userId, Long cartId, CheckoutRequestDTO request);
    
    /**
     * Validate if cart is ready for checkout
     * @param userId User ID
     * @param cartId Cart ID
     * @return true if valid, false otherwise
     */
    boolean validateCheckout(Long userId, Long cartId);
    
    /**
     * Calculate final order total with taxes and shipping
     * @param cartId Cart ID
     * @return Total amount
     */
    Double calculateFinalTotal(Long cartId);
}

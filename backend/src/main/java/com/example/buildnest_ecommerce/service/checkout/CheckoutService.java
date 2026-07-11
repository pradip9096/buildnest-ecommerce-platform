package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.dto.CheckoutSessionDTO;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Order;

public interface CheckoutService {

    // ─── Multi-step checkout (CHK-01, #76) ───────────────────────────────────

    CheckoutSessionDTO setAddress(Long userId, Long addressId);

    /**
     * Apply a coupon/discount code to the active checkout session (CHK-02,
     * #77). Only valid before payment has been initiated — once payment is
     * initiated the order total is locked in with the payment gateway.
     */
    CheckoutSessionDTO applyCoupon(Long userId, String couponCode);

    CheckoutSessionDTO selectShipping(Long userId, Long shippingMethodId);

    CheckoutSessionDTO initiatePayment(Long userId);

    OrderResponseDTO confirmCheckout(Long userId);

    // ─── Legacy single-step checkout ─────────────────────────────────────────
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

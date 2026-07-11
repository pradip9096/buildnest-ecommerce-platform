package com.example.buildnest_ecommerce.service.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSession {

    private Long userId;
    private Long cartId;
    private CheckoutStep step;
    private Long addressId;
    private Long shippingMethodId;
    private BigDecimal shippingCost;
    private Long orderId;
    private String razorpayOrderId;
    private Long couponId;
    private String couponCode;
    private BigDecimal discountAmount;
}

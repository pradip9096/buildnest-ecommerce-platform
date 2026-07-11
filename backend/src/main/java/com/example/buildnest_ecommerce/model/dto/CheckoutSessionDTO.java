package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.service.checkout.CheckoutStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionDTO {

    private Long userId;
    private Long cartId;
    private CheckoutStep step;
    private Long addressId;
    private Long shippingMethodId;
    private BigDecimal shippingCost;
    private Long orderId;
    private String razorpayOrderId;
    private String couponCode;
    private BigDecimal discountAmount;
}

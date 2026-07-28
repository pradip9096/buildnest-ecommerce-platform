package com.example.buildnest_ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {

    private Long id;

    private Long userId;

    private String orderNumber;

    private Long orderGroupId;

    /**
     * Owning seller's User.id (FR-SEL-07, #558) — every item in one Order
     * belongs to a single seller (#579's checkout-split invariant), so
     * this is derived from the first order item's product.seller, not a
     * direct Order column. Null only for orders with no items.
     */
    private Long sellerId;

    private String status;

    private BigDecimal totalAmount;

    private BigDecimal taxAmount;

    private BigDecimal shippingAmount;

    private BigDecimal discountAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

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

    private String status;

    private BigDecimal totalAmount;

    private BigDecimal taxAmount;

    private BigDecimal shippingAmount;

    private BigDecimal discountAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

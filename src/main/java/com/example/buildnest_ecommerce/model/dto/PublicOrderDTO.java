package com.example.buildnest_ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for public order endpoints.
 * Excludes internal status fields and sensitive transaction details.
 * Used to expose only customer-relevant order information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicOrderDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String shippingAddress;
    private List<PublicOrderItemDTO> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PublicOrderItemDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}

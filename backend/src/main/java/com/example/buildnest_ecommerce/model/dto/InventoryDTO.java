package com.example.buildnest_ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantityInStock;
    private Integer quantityReserved;
    private Integer availableQuantity;
    private Integer minimumStockLevel;
    private String status;
    private LocalDateTime updatedAt;
}

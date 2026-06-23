package com.example.buildnest_ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for public product endpoints.
 * Excludes sensitive fields like cost and supplier details.
 * Used to prevent information leakage and maintain API security.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String sku;
    private String imageUrl;
    private Integer stockQuantity;
    private String categoryName;
    private Boolean isActive;
}

package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.validator.ValidSKU;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Update payload for an existing variant. Deliberately excludes the initial-stock
 * fields on CreateProductVariantRequest — updating a variant's descriptive fields
 * never touches its inventory row (see ProductVariantServiceImpl.updateVariant).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductVariantRequest {
    @NotNull(message = "SKU is required")
    @ValidSKU
    @Schema(example = "CEM-50KG-RED")
    private String sku;

    @Schema(example = "50kg")
    private String size;

    @Schema(example = "Red")
    private String colour;

    @NotNull(message = "Price adjustment is required")
    @Schema(example = "0.00")
    private BigDecimal priceAdjustment;

    @Schema(example = "true")
    private Boolean isActive;
}

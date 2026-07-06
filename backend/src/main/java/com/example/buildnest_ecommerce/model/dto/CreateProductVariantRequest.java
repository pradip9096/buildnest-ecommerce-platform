package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.validator.ValidQuantity;
import com.example.buildnest_ecommerce.validator.ValidSKU;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductVariantRequest {
    @NotNull(message = "SKU is required")
    @ValidSKU
    @Schema(example = "CEM-50KG-RED")
    private String sku;

    @Schema(example = "50kg")
    private String size;

    @Schema(example = "Red")
    private String colour;

    @NotNull(message = "Price adjustment is required")
    @Schema(example = "0.00", description = "Added to (or subtracted from, if negative) the parent product's price")
    private BigDecimal priceAdjustment;

    @Schema(example = "true")
    private Boolean isActive;

    @NotNull(message = "Initial stock quantity is required")
    @ValidQuantity
    @Schema(example = "50", description = "Initial quantity in stock for the new variant's inventory row")
    private Integer initialStockQuantity;

    @NotNull(message = "Minimum stock level is required")
    @Schema(example = "5")
    private Integer minimumStockLevel;
}

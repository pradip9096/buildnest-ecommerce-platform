package com.example.buildnest_ecommerce.model.payload;

import com.example.buildnest_ecommerce.validator.ValidQuantity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {
    @NotNull(message = "Product ID is required")
    @Schema(example = "2001")
    private Long productId;

    @ValidQuantity
    @Schema(example = "2")
    private Integer quantity;
}

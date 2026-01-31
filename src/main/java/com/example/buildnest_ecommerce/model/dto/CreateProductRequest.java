package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.validator.ValidPrice;
import com.example.buildnest_ecommerce.validator.ValidQuantity;
import com.example.buildnest_ecommerce.validator.ValidSKU;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    @Schema(example = "Premium Cement 50kg")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Schema(example = "High-strength cement suitable for construction and renovation projects.")
    private String description;

    @NotNull(message = "Price is required")
    @ValidPrice
    @Schema(example = "499.99")
    private BigDecimal price;

    @ValidPrice
    @Schema(example = "449.99")
    private BigDecimal discountPrice;

    @ValidQuantity
    @Schema(example = "100")
    private Integer stockQuantity;

    @ValidSKU
    @Schema(example = "CEM-50KG")
    private String sku;

    @NotNull(message = "Category ID is required")
    @Schema(example = "3")
    private Long categoryId;

    @Pattern(regexp = "^https?://.+", message = "Image URL must be a valid http/https URL")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Schema(example = "https://cdn.example.com/products/cement-50kg.jpg")
    private String imageUrl;
}

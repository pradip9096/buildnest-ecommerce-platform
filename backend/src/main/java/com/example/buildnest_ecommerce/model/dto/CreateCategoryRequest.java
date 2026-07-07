package com.example.buildnest_ecommerce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    @Schema(example = "Power Tools")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(example = "Cordless and corded power tools for construction and DIY.")
    private String description;

    @Pattern(regexp = "^https?://.+", message = "Image URL must be a valid http/https URL")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Schema(example = "https://cdn.example.com/categories/power-tools.jpg")
    private String imageUrl;

    @Schema(example = "3", description = "Optional parent category ID for hierarchical categories")
    private Long parentId;
}

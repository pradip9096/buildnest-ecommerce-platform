package com.example.buildnest_ecommerce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductTagRequest {
    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 100, message = "Tag name must be between 2 and 100 characters")
    @Schema(example = "Eco-Friendly")
    private String name;
}

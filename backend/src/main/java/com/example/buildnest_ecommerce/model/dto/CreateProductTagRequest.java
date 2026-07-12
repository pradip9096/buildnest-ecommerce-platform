package com.example.buildnest_ecommerce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating a {@code ProductTag} (PROD-03).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CreateProductTagRequest {
    /** Maximum allowed length of {@link #name}. */
    private static final int MAX_NAME_LENGTH = 100;

    /** Tag name; the slug is derived from this server-side. */
    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = MAX_NAME_LENGTH,
            message = "Tag name must be between 2 and 100 characters")
    @Schema(example = "Eco-Friendly")
    private String name;
}

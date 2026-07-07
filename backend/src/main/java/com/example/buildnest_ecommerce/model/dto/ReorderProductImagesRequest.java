package com.example.buildnest_ecommerce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderProductImagesRequest {
    @NotEmpty(message = "Image ID list is required")
    @Schema(example = "[3, 1, 2]", description = "All image IDs belonging to the product, in the desired display order")
    private List<Long> imageIds;
}

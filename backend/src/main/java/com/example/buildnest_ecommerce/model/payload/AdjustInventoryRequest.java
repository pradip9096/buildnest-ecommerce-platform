package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdjustInventoryRequest {

    @NotNull(message = "delta is required")
    private Integer delta;

    @NotBlank(message = "reason is required")
    private String reason;
}

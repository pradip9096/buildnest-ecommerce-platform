package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShippingMethodRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "baseCost is required")
    @DecimalMin(value = "0.00", message = "baseCost must be non-negative")
    private BigDecimal baseCost;

    @DecimalMin(value = "0.00", message = "costPerKg must be non-negative")
    private BigDecimal costPerKg = BigDecimal.ZERO;

    @NotNull(message = "estimatedDaysMin is required")
    @Min(value = 0, message = "estimatedDaysMin must be non-negative")
    private Integer estimatedDaysMin;

    @NotNull(message = "estimatedDaysMax is required")
    @Min(value = 0, message = "estimatedDaysMax must be non-negative")
    private Integer estimatedDaysMax;
}

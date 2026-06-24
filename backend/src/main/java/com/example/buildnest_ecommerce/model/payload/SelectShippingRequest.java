package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectShippingRequest {

    @NotNull(message = "shippingMethodId is required")
    private Long shippingMethodId;
}

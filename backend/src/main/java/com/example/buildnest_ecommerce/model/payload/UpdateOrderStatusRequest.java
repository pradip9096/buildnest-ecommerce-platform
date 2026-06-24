package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusRequest {

    @NotBlank(message = "status is required")
    private String status;

    private String cancellationReason;
}

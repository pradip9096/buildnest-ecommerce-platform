package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerVerificationDecisionRequest {

    @NotBlank(message = "status is required")
    private String status;

    @Size(max = 500,
            message = "Rejection reason must be at most 500 characters")
    private String rejectionReason;
}

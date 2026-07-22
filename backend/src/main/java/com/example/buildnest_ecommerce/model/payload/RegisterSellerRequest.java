package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSellerRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Business name must be at most 255 characters")
    private String businessName;

    @Size(max = 100, message = "Registration number must be at most 100 chars")
    private String businessRegistrationNumber;
}

package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "\\d{6}", message = "Postal code must be 6 digits")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    private String addressType;
}

package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.validator.ValidEmail;
import com.example.buildnest_ecommerce.validator.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(example = "Aarav")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Schema(example = "Sharma")
    private String lastName;

    @ValidEmail
    @Schema(example = "user@example.com")
    private String email;

    @ValidPhoneNumber
    @Schema(example = "+14155552671")
    private String phone;

    @Size(min = 10, max = 255, message = "Address must be between 10 and 255 characters")
    @Schema(example = "221B Baker Street, London")
    private String address;
}

package com.example.buildnest_ecommerce.model.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerifyRequest {
    @NotBlank(message = "TOTP code is required")
    @Pattern(regexp = "\\d{6}", message = "TOTP code must be 6 digits")
    @Schema(example = "123456")
    private String code;
}

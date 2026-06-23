package com.example.buildnest_ecommerce.model.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username is required")
    @Schema(example = "buildnest_user")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(example = "Str0ngP@ssw0rd!123")
    private String password;
}

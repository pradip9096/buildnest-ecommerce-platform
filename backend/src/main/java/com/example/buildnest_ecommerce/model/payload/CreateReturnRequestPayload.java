package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReturnRequestPayload {

    @NotBlank(message = "reason is required")
    @Size(max = 1000, message = "reason must be at most 1000 characters")
    private String reason;
}

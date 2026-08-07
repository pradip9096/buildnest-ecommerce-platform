package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReturnStatusRequest {

    @NotBlank(message = "status is required")
    private String status;

    @Size(max = 1000, message = "adminNotes must be at most 1000 characters")
    private String adminNotes;
}

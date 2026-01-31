package com.example.buildnest_ecommerce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderDTO {

    @NotNull(message = "User ID cannot be null")
    @Schema(example = "42")
    private Long userId;

    @NotNull(message = "Cart ID cannot be null")
    @Schema(example = "101")
    private Long cartId;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 255, message = "Shipping address must be between 10 and 255 characters")
    @Schema(example = "221B Baker Street, London")
    private String shippingAddress;

    @NotBlank(message = "Payment method is required")
    @Size(min = 3, max = 50, message = "Payment method must be between 3 and 50 characters")
    @Schema(example = "CREDIT_CARD")
    private String paymentMethod;
}

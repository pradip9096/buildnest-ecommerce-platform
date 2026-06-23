package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.validator.ValidEmail;
import com.example.buildnest_ecommerce.validator.ValidPhoneNumber;
import com.example.buildnest_ecommerce.validator.ValidQuantity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckoutRequestDTO {
    @NotNull(message = "Cart ID is required")
    @Schema(example = "101")
    private Long cartId;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 255, message = "Shipping address must be between 10 and 255 characters")
    @Schema(example = "221B Baker Street, London")
    private String shippingAddress;

    @NotBlank(message = "Payment method is required")
    @Size(min = 3, max = 50, message = "Payment method must be between 3 and 50 characters")
    @Schema(example = "UPI")
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, WALLET, etc.

    @ValidPhoneNumber
    @Schema(example = "+14155552671")
    private String phoneNumber;

    @ValidEmail
    @Schema(example = "user@example.com")
    private String email;

    @ValidQuantity
    @Schema(example = "2")
    private Integer quantity;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be greater than zero")
    @Schema(example = "1299.50")
    private Double totalAmount;

    @Size(max = 1000, message = "Order notes must not exceed 1000 characters")
    @Schema(example = "Deliver between 9 AM and 5 PM")
    private String orderNotes;
}

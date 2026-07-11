package com.example.buildnest_ecommerce.model.dto;

import com.example.buildnest_ecommerce.model.entity.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Schema(example = "SAVE10")
    private String code;

    @NotNull(message = "Discount type is required")
    @Schema(example = "PERCENTAGE")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    @Schema(example = "10.00")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Minimum order value cannot be negative")
    @Schema(example = "50.00", description = "Minimum order subtotal required to apply this coupon")
    private BigDecimal minOrderValue;

    @Positive(message = "Usage limit must be positive if provided")
    @Schema(example = "100", description = "Maximum number of times this coupon may be used; omit for unlimited")
    private Integer usageLimit;

    @Schema(description = "Expiry timestamp; omit for a coupon that never expires")
    private LocalDateTime expiresAt;
}

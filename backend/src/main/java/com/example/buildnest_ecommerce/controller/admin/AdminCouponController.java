package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.exception.ValidationException;
import com.example.buildnest_ecommerce.model.dto.CreateCouponRequest;
import com.example.buildnest_ecommerce.model.entity.Coupon;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.coupon.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin coupon management (CHK-02, #77).
 */
@RestController
@RequestMapping("/api/v1/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @PostMapping
    @Auditable(action = "ADMIN_CREATE_COUPON", entityType = "COUPON")
    public ResponseEntity<ApiResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        try {
            Coupon coupon = couponService.createCoupon(request.getCode(), request.getDiscountType(),
                    request.getDiscountValue(), request.getMinOrderValue(), request.getUsageLimit(),
                    request.getExpiresAt());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Coupon created successfully", coupon));
        } catch (ValidationException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error creating coupon", null));
        }
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "ADMIN_DEACTIVATE_COUPON", entityType = "COUPON")
    public ResponseEntity<ApiResponse> deactivateCoupon(@PathVariable Long id) {
        try {
            Coupon coupon = couponService.deactivateCoupon(id);
            return ResponseEntity.ok(new ApiResponse(true, "Coupon deactivated successfully", coupon));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Coupon not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error deactivating coupon", null));
        }
    }
}

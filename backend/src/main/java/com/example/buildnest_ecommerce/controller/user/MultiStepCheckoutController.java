package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.CheckoutSessionDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.SelectShippingRequest;
import com.example.buildnest_ecommerce.model.payload.SetAddressRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.checkout.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Multi-step checkout flow (CHK-01, #76).
 *
 * <p>Steps: address → shipping → payment → confirm.
 * Session is stored in Redis with a 30-minute TTL.
 * Submitting a step out of order returns 409 Conflict.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class MultiStepCheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/address")
    public ResponseEntity<ApiResponse> setAddress(
            @Valid @RequestBody SetAddressRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CheckoutSessionDTO session = checkoutService.setAddress(currentUser.getId(), request.getAddressId());
        return ResponseEntity.ok(new ApiResponse(true, "Address set. Proceed to shipping selection.", session));
    }

    @PostMapping("/shipping")
    public ResponseEntity<ApiResponse> selectShipping(
            @Valid @RequestBody SelectShippingRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CheckoutSessionDTO session = checkoutService.selectShipping(currentUser.getId(), request.getShippingMethodId());
        return ResponseEntity.ok(new ApiResponse(true, "Shipping selected. Proceed to payment.", session));
    }

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse> initiatePayment(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CheckoutSessionDTO session = checkoutService.initiatePayment(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Payment initiated. Proceed to confirm.", session));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse> confirmCheckout(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Order order = checkoutService.confirmCheckout(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Order confirmed successfully.", order));
    }
}

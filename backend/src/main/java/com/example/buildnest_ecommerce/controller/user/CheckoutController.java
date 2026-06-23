package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.checkout.CheckoutService;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Checkout and Order Processing Controller
 * Manages the checkout process including cart validation and order creation
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Checkout", description = "Endpoints for cart checkout and order processing")
@SecurityRequirement(name = "Bearer Authentication")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Operation(summary = "Process checkout", description = "Convert a shopping cart into an order. Validates cart, checks inventory, and creates order.", tags = {
            "Checkout" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(schema = @Schema(implementation = Order.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid cart or insufficient inventory"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error during checkout")
    })
    @PostMapping("/process/{cartId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> processCheckout(
            @Parameter(description = "Cart ID to checkout", example = "1", required = true) @PathVariable Long cartId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            log.info("Processing checkout for user: {}, cart: {}", userId, cartId);

            Order order = checkoutService.checkoutCart(userId, cartId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Order placed successfully", order));
        } catch (IllegalArgumentException e) {
            log.error("Invalid checkout attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error processing checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error processing checkout: " + e.getMessage(), null));
        }
    }

    @PostMapping("/process-with-payment/{cartId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> processCheckoutWithPayment(
            @PathVariable Long cartId,
            @Valid @RequestBody CheckoutRequestDTO request,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            log.info("Processing checkout with payment for user: {}, cart: {}", userId, cartId);

            Order order = checkoutService.checkoutWithPayment(userId, cartId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true,
                            "Order placed successfully with payment method: " + request.getPaymentMethod(), order));
        } catch (IllegalArgumentException e) {
            log.error("Invalid checkout attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error processing checkout with payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error processing checkout: " + e.getMessage(), null));
        }
    }

    @GetMapping("/validate/{cartId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> validateCheckout(
            @PathVariable Long cartId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            log.debug("Validating checkout for user: {}, cart: {}", userId, cartId);

            boolean isValid = checkoutService.validateCheckout(userId, cartId);

            return ResponseEntity.ok(new ApiResponse(true,
                    isValid ? "Cart is ready for checkout" : "Cart is not ready for checkout",
                    isValid));
        } catch (Exception e) {
            log.error("Error validating checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error validating checkout", null));
        }
    }

    @GetMapping("/calculate-total/{cartId}")
    public ResponseEntity<ApiResponse> calculateTotal(@PathVariable Long cartId) {
        try {
            log.debug("Calculating final total for cart: {}", cartId);

            Double finalTotal = checkoutService.calculateFinalTotal(cartId);

            return ResponseEntity.ok(new ApiResponse(true, "Total calculated successfully",
                    new CartTotalDTO(finalTotal)));
        } catch (Exception e) {
            log.error("Error calculating total", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error calculating total", null));
        }
    }

    // DTO for total response
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CartTotalDTO {
        private Double finalTotal;
    }
}

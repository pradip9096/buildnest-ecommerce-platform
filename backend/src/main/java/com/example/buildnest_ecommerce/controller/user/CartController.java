package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.payload.AddItemRequest;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.CartResponseDTO;
import com.example.buildnest_ecommerce.service.cart.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Shopping Cart Management Controller
 * Handles cart operations including add, remove, update items, and cart
 * retrieval
 */
@RestController
@RequestMapping("/api/user/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Cart Management", description = "Endpoints for managing user shopping carts")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Add item to cart", description = "Add a product to the user's shopping cart with specified quantity", tags = {
            "Cart Management" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added to cart successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request - Product not found or insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required")
    })
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(
            @Parameter(description = "User ID", example = "1", required = true) @RequestParam Long userId,
            @Parameter(description = "Product details to add to cart", required = true) @RequestBody AddItemRequest request) {
        try {
            cartService.addToCart(userId, request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(new ApiResponse(true, "Item added to cart successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error adding item to cart: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Get user cart", description = "Retrieve the complete shopping cart for a specific user including all items, quantities, and total price", tags = {
            "Cart Management" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved successfully", content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart not found for the specified user")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getCart(
            @Parameter(description = "User ID", example = "1", required = true) @PathVariable Long userId) {
        try {
            CartResponseDTO cart = cartService.getCartByUserId(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Cart retrieved successfully", cart));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Cart not found: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable Long cartItemId) {
        try {
            cartService.removeItemFromCart(cartItemId);
            return ResponseEntity.ok(new ApiResponse(true, "Item removed from cart", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error removing item", null));
        }
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Cart cleared successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error clearing cart", null));
        }
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<ApiResponse> getCartTotal(@PathVariable Long userId) {
        try {
            Double total = cartService.getCartTotal(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Cart total retrieved", total));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Error getting cart total", null));
        }
    }
}

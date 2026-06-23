package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Wishlist;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.wishlist.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Wishlist Controller
 * Manages user wishlist operations
 * Implements Section 6.1 - E-Commerce Features from
 * EXHAUSTIVE_RECOMMENDATION_REPORT
 */
@RestController
@RequestMapping("/api/user/wishlist")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
@Tag(name = "Wishlist", description = "Endpoints for managing user wishlist/favorites")
@SecurityRequirement(name = "Bearer Authentication")
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Add product to wishlist", description = "Add a product to the authenticated user's wishlist/favorites", tags = {
            "Wishlist" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product added to wishlist successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required")
    })
    @PostMapping("/items/{productId}")
    public ResponseEntity<ApiResponse> addToWishlist(
            @Parameter(description = "Product ID to add", example = "123", required = true) @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("Adding product {} to wishlist for user {}", productId, userDetails.getId());
            Wishlist wishlist = wishlistService.addProduct(userDetails.getId(), productId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Product added to wishlist", wishlist.getProductCount()));
        } catch (Exception e) {
            log.error("Error adding product to wishlist: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error adding to wishlist: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Remove product from wishlist", description = "Remove a product from the authenticated user's wishlist", tags = {
            "Wishlist" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product removed from wishlist successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or wishlist not found")
    })
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(
            @Parameter(description = "Product ID to remove", example = "123", required = true) @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("Removing product {} from wishlist for user {}", productId, userDetails.getId());
            Wishlist wishlist = wishlistService.removeProduct(userDetails.getId(), productId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Product removed from wishlist", wishlist.getProductCount()));
        } catch (Exception e) {
            log.error("Error removing product from wishlist: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error removing from wishlist: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Get wishlist", description = "Retrieve all products in the authenticated user's wishlist", tags = {
            "Wishlist" })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully", content = @Content(schema = @Schema(implementation = Product.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wishlist not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponse> getWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.debug("Fetching wishlist for user {}", userDetails.getId());
            Set<Product> products = wishlistService.getWishlistProducts(userDetails.getId());
            return ResponseEntity.ok(
                    new ApiResponse(true, "Wishlist retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error fetching wishlist: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Error fetching wishlist: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Check if product is in wishlist", description = "Check if a specific product is in the user's wishlist", tags = {
            "Wishlist" })
    @GetMapping("/contains/{productId}")
    public ResponseEntity<ApiResponse> isInWishlist(
            @Parameter(description = "Product ID to check", example = "123", required = true) @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            boolean inWishlist = wishlistService.isProductInWishlist(userDetails.getId(), productId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Check completed", inWishlist));
        } catch (Exception e) {
            log.error("Error checking wishlist: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error checking wishlist", null));
        }
    }

    @Operation(summary = "Clear wishlist", description = "Remove all products from the authenticated user's wishlist", tags = {
            "Wishlist" })
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.info("Clearing wishlist for user {}", userDetails.getId());
            wishlistService.clearWishlist(userDetails.getId());
            return ResponseEntity.ok(
                    new ApiResponse(true, "Wishlist cleared successfully", null));
        } catch (Exception e) {
            log.error("Error clearing wishlist: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error clearing wishlist: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Get wishlist count", description = "Get the number of products in the user's wishlist", tags = {
            "Wishlist" })
    @GetMapping("/count")
    public ResponseEntity<ApiResponse> getWishlistCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            long count = wishlistService.getWishlistCount(userDetails.getId());
            return ResponseEntity.ok(
                    new ApiResponse(true, "Count retrieved", count));
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ApiResponse(true, "Count retrieved", 0L));
        }
    }
}

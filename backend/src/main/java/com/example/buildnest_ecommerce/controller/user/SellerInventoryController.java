package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.InventoryDTO;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.payload.AdjustInventoryRequest;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Seller-scoped inventory management (FR-SEL-05, #556) — a verified
 * seller's own view/adjustment of stock for products they own. Mirrors
 * {@link SellerProductController}'s ownership-scoping pattern from #555.
 * Defense in depth: {@code @PreAuthorize} here plus {@code /api/user/**}'s
 * own USER-or-ADMIN gate in SecurityConfig.
 */
@RestController
@RequestMapping("/api/user/seller/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Inventory",
        description = "Seller-scoped inventory management")
@SecurityRequirement(name = "Bearer Authentication")
public class SellerInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getOwnInventory(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable) {
        Page<InventoryDTO> inventory = inventoryService
                .getInventoryForSeller(currentUser.getId(), pageable);
        return ResponseEntity.ok(
                new ApiResponse(true, "Inventory retrieved", inventory));
    }

    @PatchMapping("/{productId}")
    @Auditable(action = "SELLER_ADJUST_INVENTORY", entityType = "INVENTORY")
    public ResponseEntity<ApiResponse> adjustOwnInventory(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long productId,
            @Valid @RequestBody AdjustInventoryRequest request) {
        Inventory updated = inventoryService.adjustStockForSeller(
                currentUser.getId(), productId,
                request.getDelta(), request.getReason());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(
                        true, "Inventory adjusted successfully", updated));
    }
}

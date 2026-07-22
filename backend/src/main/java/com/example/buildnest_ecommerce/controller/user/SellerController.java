package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.RegisterSellerRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.seller.SellerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Seller registration (FR-SEL-01, #553) — an authenticated user upgrades
 * their existing buyer account to also be a seller. District assignment is
 * deferred (see the sellers changeset comment) pending ADR #561.
 */
@RestController
@RequestMapping("/api/user/seller")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Seller", description = "Seller registration and profile")
@SecurityRequirement(name = "Bearer Authentication")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/register")
    @Auditable(action = "SELLER_REGISTER", entityType = "SELLER")
    public ResponseEntity<ApiResponse> registerSeller(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody RegisterSellerRequest request) {
        SellerResponseDTO seller = sellerService.registerSeller(
                currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        true, "Seller registration successful", seller));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getSellerProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        SellerResponseDTO seller =
                sellerService.getSellerProfile(currentUser.getId());
        return ResponseEntity.ok(
                new ApiResponse(true, "Seller profile retrieved", seller));
    }
}

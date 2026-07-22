package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.SellerResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload
        .SellerVerificationDecisionRequest;
import com.example.buildnest_ecommerce.service.seller.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin seller verification/approval workflow (FR-SEL-02, #554). Mirrors
 * {@link AdminOrderController#updateOrderStatus}'s PATCH-status shape —
 * the sibling precedent for an admin-gated entity status transition.
 */
@RestController
@RequestMapping("/api/v1/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSellerController {

    private final SellerService sellerService;

    @GetMapping
    public ResponseEntity<ApiResponse> getSellers(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt")
                    Pageable pageable) {

        Seller.VerificationStatus parsedStatus =
                Seller.VerificationStatus.PENDING;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = Seller.VerificationStatus
                        .valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false, "Invalid status value: " + status, null));
            }
        }

        Page<SellerResponseDTO> page = sellerService
                .getSellersByVerificationStatus(parsedStatus, pageable);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sellers retrieved successfully",
                        page));
    }

    @PatchMapping("/{id}/verification-status")
    @Auditable(action = "ADMIN_SELLER_VERIFICATION_DECISION",
            entityType = "Seller")
    public ResponseEntity<ApiResponse> updateVerificationStatus(
            @PathVariable Long id,
            @Valid @RequestBody
                    SellerVerificationDecisionRequest request) {

        SellerResponseDTO updated = sellerService.updateVerificationStatus(
                id, request.getStatus(), request.getRejectionReason());
        return ResponseEntity.ok(new ApiResponse(
                true, "Seller verification status updated successfully",
                updated));
    }
}

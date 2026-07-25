package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.OrderResponseDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.UpdateOrderStatusRequest;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.order.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seller-scoped order API (FR-SEL-06, #580) — a verified seller's own
 * list/detail/status view over orders containing their products, scoped
 * so a seller can only ever see or update orders they own. {@code Order}
 * has no direct seller reference; ownership is derived transitively via
 * {@link OrderService#getSellerOrders} / {@code OrderRepository}'s
 * {@code EXISTS}-subquery scoping over {@code OrderItem.product.seller}.
 * Defense in depth: {@code @PreAuthorize} here plus {@code /api/user/**}'s
 * own USER-or-ADMIN gate in SecurityConfig — mirrors
 * {@link SellerProductController} (FR-SEL-03/04, #555).
 */
@RestController
@RequestMapping("/api/user/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Orders",
        description = "Seller-scoped order list/detail/status")
@SecurityRequirement(name = "Bearer Authentication")
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse> getOwnOrders(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable) {
        Page<OrderResponseDTO> orders = orderService
                .getSellerOrders(currentUser.getId(), pageable);
        return ResponseEntity.ok(
                new ApiResponse(true, "Orders retrieved", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOwnOrderDetail(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) {
        OrderResponseDTO order = orderService
                .getSellerOrderById(currentUser.getId(), id);
        return ResponseEntity.ok(
                new ApiResponse(true, "Order details retrieved", order));
    }

    @PatchMapping("/{id}/status")
    @Auditable(action = "SELLER_UPDATE_ORDER_STATUS", entityType = "Order")
    public ResponseEntity<ApiResponse> updateOwnOrderStatus(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponseDTO updated = orderService.updateSellerOrderStatus(
                currentUser.getId(), id, request.getStatus());
        return ResponseEntity.ok(
                new ApiResponse(true, "Order status updated", updated));
    }
}

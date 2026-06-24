package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.entity.ShippingMethod;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.CreateShippingMethodRequest;
import com.example.buildnest_ecommerce.service.shipping.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin shipping method management (SHIP-01, #87).
 */
@RestController
@RequestMapping("/api/v1/admin/shipping-methods")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminShippingController {

    private final ShippingService shippingService;

    @GetMapping
    public ResponseEntity<ApiResponse> listAll() {
        return ResponseEntity.ok(
                new ApiResponse(true, "Shipping methods retrieved", shippingService.getAllShippingMethods()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse(true, "Shipping method retrieved", shippingService.getShippingMethod(id)));
    }

    @PostMapping
    @Auditable(action = "ADMIN_CREATE_SHIPPING_METHOD", entityType = "SHIPPING_METHOD")
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody CreateShippingMethodRequest request) {
        ShippingMethod created = shippingService.createShippingMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Shipping method created", created));
    }

    @PutMapping("/{id}")
    @Auditable(action = "ADMIN_UPDATE_SHIPPING_METHOD", entityType = "SHIPPING_METHOD")
    public ResponseEntity<ApiResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateShippingMethodRequest request) {
        ShippingMethod updated = shippingService.updateShippingMethod(id, request);
        return ResponseEntity.ok(new ApiResponse(true, "Shipping method updated", updated));
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "ADMIN_DELETE_SHIPPING_METHOD", entityType = "SHIPPING_METHOD")
    public ResponseEntity<ApiResponse> deactivate(@PathVariable Long id) {
        shippingService.deactivateShippingMethod(id);
        return ResponseEntity.ok(new ApiResponse(true, "Shipping method deactivated", null));
    }
}

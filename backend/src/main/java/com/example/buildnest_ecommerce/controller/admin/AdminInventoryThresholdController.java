package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.inventory.InventoryThresholdManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing inventory thresholds (RQ-INV-TH-01, RQ-INV-TH-02,
 * RQ-INV-TH-03).
 * Restricted to ADMIN users only.
 */
@RestController
@RequestMapping("/api/admin/inventory-threshold")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminInventoryThresholdController {

    private final InventoryThresholdManagementService thresholdService;

    /**
     * Set minimum stock threshold for a product (RQ-INV-TH-01, RQ-INV-TH-03).
     */
    @PostMapping("/product/{productId}")
    @Auditable(action = "ADMIN_SET_PRODUCT_THRESHOLD", entityType = "INVENTORY_THRESHOLD")
    public ResponseEntity<ApiResponse> setProductThreshold(
            @PathVariable Long productId,
            @RequestParam Integer minimumLevel) {
        try {
            if (minimumLevel < 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Minimum level must be non-negative", null));
            }

            thresholdService.setProductThreshold(productId, minimumLevel);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("minimumLevel", minimumLevel);
            response.put("message", "Product threshold set successfully");

            return ResponseEntity.ok(new ApiResponse(true, "Threshold configured", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Set minimum stock threshold for a category (RQ-INV-TH-02).
     */
    @PostMapping("/category/{categoryId}")
    @Auditable(action = "ADMIN_SET_CATEGORY_THRESHOLD", entityType = "INVENTORY_THRESHOLD")
    public ResponseEntity<ApiResponse> setCategoryThreshold(
            @PathVariable Long categoryId,
            @RequestParam Integer minimumLevel) {
        try {
            if (minimumLevel < 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Minimum level must be non-negative", null));
            }

            thresholdService.setCategoryThreshold(categoryId, minimumLevel);

            Map<String, Object> response = new HashMap<>();
            response.put("categoryId", categoryId);
            response.put("minimumLevel", minimumLevel);
            response.put("message", "Category threshold set successfully");

            return ResponseEntity.ok(new ApiResponse(true, "Threshold configured", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get threshold for a product (RQ-INV-TH-01).
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getProductThreshold(@PathVariable Long productId) {
        try {
            Integer threshold = thresholdService.getProductThreshold(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("minimumLevel", threshold);

            return ResponseEntity.ok(new ApiResponse(true, "Threshold retrieved", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get threshold for a category (RQ-INV-TH-02).
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse> getCategoryThreshold(@PathVariable Long categoryId) {
        try {
            Integer threshold = thresholdService.getCategoryThreshold(categoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("categoryId", categoryId);
            response.put("minimumLevel", threshold);

            return ResponseEntity.ok(new ApiResponse(true, "Threshold retrieved", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get effective threshold for a product (RQ-INV-TH-02).
     */
    @GetMapping("/product/{productId}/effective")
    public ResponseEntity<ApiResponse> getEffectiveThreshold(@PathVariable Long productId) {
        try {
            Integer threshold = thresholdService.getEffectiveThreshold(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("effectiveThreshold", threshold);

            return ResponseEntity.ok(new ApiResponse(true, "Effective threshold retrieved", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Enable/disable category threshold inheritance (RQ-INV-TH-02).
     */
    @PutMapping("/product/{productId}/use-category")
    @Auditable(action = "ADMIN_TOGGLE_CATEGORY_THRESHOLD", entityType = "INVENTORY_THRESHOLD")
    public ResponseEntity<ApiResponse> useProductCategoryThreshold(
            @PathVariable Long productId,
            @RequestParam boolean useCategory) {
        try {
            thresholdService.useProductCategoryThreshold(productId, useCategory);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("useCategoryThreshold", useCategory);
            response.put("message",
                    useCategory ? "Product now uses category threshold" : "Product now uses individual threshold");

            return ResponseEntity.ok(new ApiResponse(true, "Threshold inheritance updated", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}

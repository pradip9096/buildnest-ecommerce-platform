package com.example.buildnest_ecommerce.controller.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Public controller for inventory status viewing (RQ-INV-ADM-02).
 * Accessible to all authenticated users (read-only).
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryStatusController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    /**
     * Get inventory status for a product (RQ-INV-ADM-02, RQ-INV-STAT-01).
     * Status: IN_STOCK, LOW_STOCK, OUT_OF_STOCK
     */
    @GetMapping("/{productId}/status")
    public ResponseEntity<ApiResponse> getProductStatus(@PathVariable Long productId) {
        try {
            InventoryStatus status = inventoryService.getInventoryStatus(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("status", status.name());
            response.put("displayName", status.getDisplayName());
            response.put("description", status.getDescription());

            return ResponseEntity.ok(new ApiResponse(true, "Status retrieved", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Product not found", null));
        }
    }

    /**
     * Get detailed inventory information for a product (RQ-INV-ADM-02).
     */
    @GetMapping("/{productId}/details")
    public ResponseEntity<ApiResponse> getInventoryDetails(@PathVariable Long productId) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Product not found", null));
            }

            Inventory inventory = product.getInventory();
            if (inventory == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Inventory not found for product", null));
            }

            InventoryStatus status = inventoryService.getInventoryStatus(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("productName", product.getName());
            response.put("status", status.name());
            response.put("quantity", inventory.getQuantityInStock());
            response.put("reserved", inventory.getQuantityReserved());
            response.put("available", inventory.getAvailableQuantity());
            response.put("minimumThreshold", inventory.getMinimumStockLevel());
            response.put("isBelowThreshold", inventoryService.isBelowThreshold(productId));

            return ResponseEntity.ok(new ApiResponse(true, "Details retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving inventory details", null));
        }
    }

    /**
     * Check if a product is available (in stock) (RQ-INV-ADM-02).
     */
    @GetMapping("/{productId}/available")
    public ResponseEntity<ApiResponse> isProductAvailable(@PathVariable Long productId) {
        try {
            InventoryStatus status = inventoryService.getInventoryStatus(productId);
            boolean available = status != InventoryStatus.OUT_OF_STOCK;

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("available", available);
            response.put("status", status.name());

            return ResponseEntity.ok(new ApiResponse(true, "Availability retrieved", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Product not found", null));
        }
    }
}

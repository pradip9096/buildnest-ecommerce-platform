package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminInventoryController {
    
    private final InventoryService inventoryService;
    
    @GetMapping("/product/{productId}")
    @Auditable(action = "ADMIN_GET_INVENTORY", entityType = "INVENTORY")
    public ResponseEntity<ApiResponse> getInventory(@PathVariable Long productId) {
        try {
            Inventory inventory = inventoryService.getInventoryByProductId(productId);
            return ResponseEntity.ok(new ApiResponse(true, "Inventory retrieved", inventory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Inventory not found: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/add-stock/{productId}")
    @Auditable(action = "ADMIN_ADD_STOCK", entityType = "INVENTORY")
    public ResponseEntity<ApiResponse> addStock(@PathVariable Long productId, 
                                                @RequestParam Integer quantity) {
        try {
            Inventory updated = inventoryService.addStock(productId, quantity);
            return ResponseEntity.ok(new ApiResponse(true, "Stock added successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error adding stock", null));
        }
    }
    
    @PostMapping("/update-stock/{productId}")
    @Auditable(action = "ADMIN_UPDATE_STOCK", entityType = "INVENTORY")
    public ResponseEntity<ApiResponse> updateStock(@PathVariable Long productId, 
                                                   @RequestParam Integer quantity) {
        try {
            Inventory updated = inventoryService.updateStock(productId, quantity);
            return ResponseEntity.ok(new ApiResponse(true, "Stock updated", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error updating stock", null));
        }
    }
    
    @GetMapping("/check-availability/{productId}")
    @Auditable(action = "ADMIN_CHECK_STOCK", entityType = "INVENTORY")
    public ResponseEntity<ApiResponse> checkAvailability(@PathVariable Long productId, 
                                                        @RequestParam Integer quantity) {
        try {
            boolean available = inventoryService.hasStock(productId, quantity);
            return ResponseEntity.ok(new ApiResponse(true, available ? "Stock available" : "Stock unavailable", available));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error checking availability", null));
        }
    }
}

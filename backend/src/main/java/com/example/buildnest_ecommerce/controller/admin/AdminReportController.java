package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReportController {
    
    private final AdminService adminService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", adminService.getTotalUsers());
            stats.put("totalProducts", adminService.getTotalProducts());
            stats.put("totalOrders", adminService.getTotalOrders());
            stats.put("totalRevenue", adminService.getTotalRevenue());
            
            return ResponseEntity.ok(new ApiResponse(true, "Dashboard statistics retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving dashboard statistics", null));
        }
    }
    
    @GetMapping("/users/count")
    public ResponseEntity<ApiResponse> getUsersCount() {
        try {
            Long count = adminService.getTotalUsers();
            Map<String, Object> data = new HashMap<>();
            data.put("totalUsers", count);
            return ResponseEntity.ok(new ApiResponse(true, "User count retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving user count", null));
        }
    }
    
    @GetMapping("/products/count")
    public ResponseEntity<ApiResponse> getProductsCount() {
        try {
            Long count = adminService.getTotalProducts();
            Map<String, Object> data = new HashMap<>();
            data.put("totalProducts", count);
            return ResponseEntity.ok(new ApiResponse(true, "Product count retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving product count", null));
        }
    }
    
    @GetMapping("/orders/count")
    public ResponseEntity<ApiResponse> getOrdersCount() {
        try {
            Long count = adminService.getTotalOrders();
            Map<String, Object> data = new HashMap<>();
            data.put("totalOrders", count);
            return ResponseEntity.ok(new ApiResponse(true, "Order count retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving order count", null));
        }
    }
    
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse> getTotalRevenue() {
        try {
            Double revenue = adminService.getTotalRevenue();
            Map<String, Object> data = new HashMap<>();
            data.put("totalRevenue", revenue);
            return ResponseEntity.ok(new ApiResponse(true, "Revenue retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving revenue", null));
        }
    }
}

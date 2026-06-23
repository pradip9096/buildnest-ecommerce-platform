package com.example.buildnest_ecommerce.controller.public_;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductService;
import com.example.buildnest_ecommerce.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getHome() {
        return ResponseEntity.ok(new ApiResponse(true,
                "Welcome to BuildNest – E-Commerce Platform for Home Construction and Décor Products API", null));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(new ApiResponse(true, "API is running", null));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Error retrieving products", null));
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", product));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Product not found", null));
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse> searchProducts(@RequestParam String keyword) {
        try {
            List<Product> products = productService.searchProducts(keyword);
            return ResponseEntity.ok(new ApiResponse(true, "Search results", products));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Error searching products", null));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            var categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse(true, "Categories retrieved successfully", categories));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Error retrieving categories", null));
        }
    }
}

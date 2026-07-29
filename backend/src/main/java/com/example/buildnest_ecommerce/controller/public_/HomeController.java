package com.example.buildnest_ecommerce.controller.public_;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductService;
import com.example.buildnest_ecommerce.service.category.CategoryService;
import com.example.buildnest_ecommerce.service.district.DistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<ApiResponse> getHome() {
        return ResponseEntity.ok(new ApiResponse(true,
                "Welcome to BuildNest – E-Commerce Platform for Home "
                        + "Construction and Décor Products API",
                null));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(
                new ApiResponse(true, "API is running", null));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(new ApiResponse(true,
                    "Products retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error retrieving products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error retrieving products", null));
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse> getProductById(
            @PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(new ApiResponse(true,
                    "Product retrieved successfully", product));
        } catch (Exception e) {
            log.error("Error retrieving product with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Product not found", null));
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam String keyword) {
        try {
            List<Product> products = productService.searchProducts(keyword);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Search results", products));
        } catch (Exception e) {
            log.error("Error searching products with keyword: {}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error searching products", null));
        }
    }

    @GetMapping("/products/featured")
    public ResponseEntity<ApiResponse> getFeaturedProducts() {
        try {
            List<Product> products = productService.getFeaturedProducts();
            return ResponseEntity.ok(new ApiResponse(true,
                    "Featured products retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error retrieving featured products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error retrieving featured products", null));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            var categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse(true,
                    "Categories retrieved successfully", categories));
        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error retrieving categories", null));
        }
    }

    /**
     * Lists every district in the fixed reference table.
     *
     * @return all districts
     */
    @GetMapping("/districts")
    public ResponseEntity<ApiResponse> getAllDistricts() {
        try {
            var districts = districtService.getAllDistricts();
            return ResponseEntity.ok(new ApiResponse(true,
                    "Districts retrieved successfully", districts));
        } catch (Exception e) {
            log.error("Error retrieving districts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,
                            "Error retrieving districts", null));
        }
    }
}

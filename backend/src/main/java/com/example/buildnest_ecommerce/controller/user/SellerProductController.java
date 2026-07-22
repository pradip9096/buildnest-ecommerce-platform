package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.product.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Seller-owned product catalogue (FR-SEL-03, FR-SEL-04, #555) — a verified
 * seller's own CRUD over their product listings, scoped so a seller can
 * only ever read/update/delete products they own. Defense in depth:
 * {@code @PreAuthorize} here plus {@code /api/user/**}'s own USER-or-ADMIN
 * gate in SecurityConfig.
 */
@RestController
@RequestMapping("/api/user/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Products",
        description = "Seller-owned product catalogue CRUD")
@SecurityRequirement(name = "Bearer Authentication")
public class SellerProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getOwnProducts(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable) {
        Page<Product> products = productService
                .getProductsForSeller(currentUser.getId(), pageable);
        return ResponseEntity.ok(
                new ApiResponse(true, "Products retrieved", products));
    }

    @PostMapping
    @Auditable(action = "SELLER_CREATE_PRODUCT", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> createOwnProduct(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateProductRequest request) {
        Product product = productService.createProductForSeller(
                request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        true, "Product created", product));
    }

    @PutMapping("/{id}")
    @Auditable(action = "SELLER_UPDATE_PRODUCT", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> updateOwnProduct(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request) {
        Product product = productService.updateProductForSeller(
                currentUser.getId(), id, request);
        return ResponseEntity.ok(
                new ApiResponse(true, "Product updated", product));
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "SELLER_DELETE_PRODUCT", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> deleteOwnProduct(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) {
        productService.deleteProductForSeller(currentUser.getId(), id);
        return ResponseEntity.ok(
                new ApiResponse(true, "Product deleted", null));
    }
}

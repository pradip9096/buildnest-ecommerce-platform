package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.dto.CreateProductVariantRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductVariantRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductService;
import com.example.buildnest_ecommerce.service.product.ProductVariantService;
import com.example.buildnest_ecommerce.service.storage.StorageException;
import com.example.buildnest_ecommerce.service.storage.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final StorageService storageService;

    @GetMapping
    @Auditable(action = "ADMIN_LIST_PRODUCTS", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving products", null));
        }
    }

    @GetMapping("/{id}")
    @Auditable(action = "ADMIN_GET_PRODUCT", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Product not found", null));
        }
    }

    @PostMapping
    @Auditable(action = "ADMIN_CREATE_PRODUCT", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        try {
            Product product = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Product created successfully", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error creating product: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @Auditable(action = "ADMIN_UPDATE_PRODUCT", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id,
                                                     @Valid @RequestBody CreateProductRequest request) {
        try {
            Product product = productService.updateProduct(id, request);
            return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error updating product: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "ADMIN_DELETE_PRODUCT", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse(true, "Product deactivated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error deleting product: " + e.getMessage(), null));
        }
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Auditable(action = "ADMIN_UPLOAD_PRODUCT_IMAGE", entityType = "PRODUCT")
    public ResponseEntity<ApiResponse> uploadProductImage(@PathVariable Long id,
                                                          @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = storageService.store(file);
            Product product = productService.updateProductImage(id, imageUrl);
            return ResponseEntity.ok(new ApiResponse(true, "Image uploaded successfully", product));
        } catch (StorageException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error uploading image: " + e.getMessage(), null));
        }
    }

    // ─── VARIANTS (PROD-01, #81) ───

    @GetMapping("/{productId}/variants")
    @Auditable(action = "ADMIN_LIST_PRODUCT_VARIANTS", entityType = "PRODUCT_VARIANT")
    public ResponseEntity<ApiResponse> getVariants(@PathVariable Long productId) {
        try {
            List<ProductVariant> variants = productVariantService.getVariantsByProduct(productId);
            return ResponseEntity.ok(new ApiResponse(true, "Variants retrieved successfully", variants));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving variants", null));
        }
    }

    @PostMapping("/{productId}/variants")
    @Auditable(action = "ADMIN_CREATE_PRODUCT_VARIANT", entityType = "PRODUCT_VARIANT")
    public ResponseEntity<ApiResponse> createVariant(@PathVariable Long productId,
                                                      @Valid @RequestBody CreateProductVariantRequest request) {
        try {
            ProductVariant variant = productVariantService.createVariant(productId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Variant created successfully", variant));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error creating variant: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{productId}/variants/{variantId}")
    @Auditable(action = "ADMIN_UPDATE_PRODUCT_VARIANT", entityType = "PRODUCT_VARIANT")
    public ResponseEntity<ApiResponse> updateVariant(@PathVariable Long productId,
                                                      @PathVariable Long variantId,
                                                      @Valid @RequestBody UpdateProductVariantRequest request) {
        try {
            ProductVariant variant = productVariantService.updateVariant(variantId, request);
            return ResponseEntity.ok(new ApiResponse(true, "Variant updated successfully", variant));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error updating variant: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    @Auditable(action = "ADMIN_DELETE_PRODUCT_VARIANT", entityType = "PRODUCT_VARIANT")
    public ResponseEntity<ApiResponse> deleteVariant(@PathVariable Long productId, @PathVariable Long variantId) {
        try {
            productVariantService.deleteVariant(variantId);
            return ResponseEntity.ok(new ApiResponse(true, "Variant deactivated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error deleting variant: " + e.getMessage(), null));
        }
    }
}

package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.CreateProductTagRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductTagRequest;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.producttag.ProductTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductTagController {

    private final ProductTagService productTagService;

    @GetMapping
    @Auditable(action = "ADMIN_LIST_PRODUCT_TAGS", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> getAllTags() {
        try {
            List<ProductTag> tags = productTagService.getAllTags();
            return ResponseEntity.ok(new ApiResponse(true, "Tags retrieved successfully", tags));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving tags", null));
        }
    }

    @GetMapping("/{id}")
    @Auditable(action = "ADMIN_GET_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> getTagById(@PathVariable Long id) {
        try {
            ProductTag tag = productTagService.getTagById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Tag retrieved successfully", tag));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Tag not found", null));
        }
    }

    @PostMapping
    @Auditable(action = "ADMIN_CREATE_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> createTag(@Valid @RequestBody CreateProductTagRequest request) {
        try {
            ProductTag tag = productTagService.createTag(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tag created successfully", tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error creating tag", null));
        }
    }

    @PutMapping("/{id}")
    @Auditable(action = "ADMIN_UPDATE_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> updateTag(
            @PathVariable Long id, @Valid @RequestBody UpdateProductTagRequest request) {
        try {
            ProductTag tag = productTagService.updateTag(id, request);
            return ResponseEntity.ok(new ApiResponse(true, "Tag updated successfully", tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Tag not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating tag", null));
        }
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "ADMIN_DELETE_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> deleteTag(@PathVariable Long id) {
        try {
            productTagService.deleteTag(id);
            return ResponseEntity.ok(new ApiResponse(true, "Tag deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Tag not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error deleting tag", null));
        }
    }
}

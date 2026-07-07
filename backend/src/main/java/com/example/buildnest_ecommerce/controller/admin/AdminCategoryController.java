package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.dto.CreateCategoryRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateCategoryRequest;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Auditable(action = "ADMIN_LIST_CATEGORIES", entityType = "CATEGORY")
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse(true, "Categories retrieved successfully", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving categories", null));
        }
    }

    @GetMapping("/{id}")
    @Auditable(action = "ADMIN_GET_CATEGORY", entityType = "CATEGORY")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Category retrieved successfully", category));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Category not found", null));
        }
    }

    @PostMapping
    @Auditable(action = "ADMIN_CREATE_CATEGORY", entityType = "CATEGORY")
    public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        try {
            Category category = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Category created successfully", category));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error creating category", null));
        }
    }

    @PutMapping("/{id}")
    @Auditable(action = "ADMIN_UPDATE_CATEGORY", entityType = "CATEGORY")
    public ResponseEntity<ApiResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(new ApiResponse(true, "Category updated successfully", category));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Category not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating category", null));
        }
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "ADMIN_DELETE_CATEGORY", entityType = "CATEGORY")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Category not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error deleting category", null));
        }
    }
}

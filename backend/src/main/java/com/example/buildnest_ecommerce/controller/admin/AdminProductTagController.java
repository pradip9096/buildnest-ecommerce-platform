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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin CRUD endpoints for {@link ProductTag} (PROD-03).
 */
@RestController
@RequestMapping("/api/v1/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductTagController {

    /** Service backing all tag operations. */
    private final ProductTagService productTagService;

    /**
     * Lists every product tag.
     *
     * @return all tags
     */
    @GetMapping
    @Auditable(action = "ADMIN_LIST_PRODUCT_TAGS", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> getAllTags() {
        try {
            final List<ProductTag> tags = productTagService.getAllTags();
            return ResponseEntity.ok(
                    new ApiResponse(true, "Tags retrieved successfully", tags));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(
                            false, "Error retrieving tags", null));
        }
    }

    /**
     * Fetches a single tag by id.
     *
     * @param id the tag id
     * @return the matching tag, or 404 if absent
     */
    @GetMapping("/{id}")
    @Auditable(action = "ADMIN_GET_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> getTagById(@PathVariable final Long id) {
        try {
            final ProductTag tag = productTagService.getTagById(id);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Tag retrieved successfully", tag));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Tag not found", null));
        }
    }

    /**
     * Creates a new tag.
     *
     * @param request the create request
     * @return the created tag
     */
    @PostMapping
    @Auditable(action = "ADMIN_CREATE_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> createTag(
            @Valid @RequestBody final CreateProductTagRequest request) {
        try {
            final ProductTag tag = productTagService.createTag(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(
                            true, "Tag created successfully", tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error creating tag", null));
        }
    }

    /**
     * Updates an existing tag.
     *
     * @param id the tag id
     * @param request the update request
     * @return the updated tag
     */
    @PutMapping("/{id}")
    @Auditable(action = "ADMIN_UPDATE_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> updateTag(
            @PathVariable final Long id,
            @Valid @RequestBody final UpdateProductTagRequest request) {
        try {
            final ProductTag tag = productTagService.updateTag(id, request);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Tag updated successfully", tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Tag not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating tag", null));
        }
    }

    /**
     * Deletes a tag by id.
     *
     * @param id the tag id
     * @return an empty success response, or 404 if the tag does not exist
     */
    @DeleteMapping("/{id}")
    @Auditable(action = "ADMIN_DELETE_PRODUCT_TAG", entityType = "PRODUCT_TAG")
    public ResponseEntity<ApiResponse> deleteTag(@PathVariable final Long id) {
        try {
            productTagService.deleteTag(id);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Tag deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Tag not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error deleting tag", null));
        }
    }
}

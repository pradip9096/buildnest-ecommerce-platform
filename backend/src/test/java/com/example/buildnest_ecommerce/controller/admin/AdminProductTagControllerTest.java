package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.CreateProductTagRequest;
import com.example.buildnest_ecommerce.model.dto.UpdateProductTagRequest;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.producttag.ProductTagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdminProductTagController}'s unexpected-exception
 * (generic 500) branches, which the MockMvc integration test doesn't exercise
 * since it never forces the service to throw an unmapped exception (#83).
 */
class AdminProductTagControllerTest {

    @Test
    @DisplayName("getTagById — returns the matching tag")
    void getTagById_found_returnsTag() {
        ProductTagService service = mock(ProductTagService.class);
        ProductTag tag = new ProductTag();
        tag.setId(1L);
        tag.setName("Eco-Friendly");
        when(service.getTagById(1L)).thenReturn(tag);

        AdminProductTagController controller = new AdminProductTagController(service);
        ResponseEntity<ApiResponse> response = controller.getTagById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("updateTag — duplicate name/slug (IllegalArgumentException) returns 400")
    void updateTag_serviceThrowsIllegalArgumentException_returns400() {
        ProductTagService service = mock(ProductTagService.class);
        UpdateProductTagRequest request = new UpdateProductTagRequest("Taken Name");
        when(service.updateTag(1L, request))
                .thenThrow(new IllegalArgumentException("Tag already exists with name: Taken Name"));

        AdminProductTagController controller = new AdminProductTagController(service);
        ResponseEntity<ApiResponse> response = controller.updateTag(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("getAllTags — unexpected exception returns 500")
    void getAllTags_serviceThrows_returns500() {
        ProductTagService service = mock(ProductTagService.class);
        when(service.getAllTags()).thenThrow(new RuntimeException("boom"));

        AdminProductTagController controller = new AdminProductTagController(service);
        ResponseEntity<ApiResponse> response = controller.getAllTags();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("createTag — unexpected exception returns 500")
    void createTag_serviceThrows_returns500() {
        ProductTagService service = mock(ProductTagService.class);
        CreateProductTagRequest request = new CreateProductTagRequest("Eco Friendly");
        when(service.createTag(request)).thenThrow(new RuntimeException("boom"));

        AdminProductTagController controller = new AdminProductTagController(service);
        ResponseEntity<ApiResponse> response = controller.createTag(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("updateTag — not-found (RuntimeException) returns 404")
    void updateTag_serviceThrowsRuntimeException_returns404() {
        ProductTagService service = mock(ProductTagService.class);
        UpdateProductTagRequest request = new UpdateProductTagRequest("New Name");
        when(service.updateTag(1L, request)).thenThrow(new RuntimeException("not found"));

        AdminProductTagController controller = new AdminProductTagController(service);
        ResponseEntity<ApiResponse> response = controller.updateTag(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteTag — not-found (RuntimeException) returns 404")
    void deleteTag_serviceThrowsRuntimeException_returns404() {
        ProductTagService service = mock(ProductTagService.class);
        doThrow(new RuntimeException("not found")).when(service).deleteTag(1L);

        AdminProductTagController controller = new AdminProductTagController(service);
        ResponseEntity<ApiResponse> response = controller.deleteTag(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

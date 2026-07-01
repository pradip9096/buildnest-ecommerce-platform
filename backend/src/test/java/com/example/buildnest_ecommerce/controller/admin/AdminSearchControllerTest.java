package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminSearchController unit tests")
class AdminSearchControllerTest {

    @Test
    @DisplayName("reindex — Elasticsearch disabled (Optional.empty) → 503 Service Unavailable")
    void reindex_elasticsearchDisabled_returns503() {
        AdminSearchController controller = new AdminSearchController(Optional.empty());

        ResponseEntity<ApiResponse> response = controller.reindex();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("reindex — Elasticsearch enabled → delegates to reindexAll and returns 200")
    void reindex_elasticsearchEnabled_returns200AndCallsService() {
        ProductSearchService searchService = mock(ProductSearchService.class);
        AdminSearchController controller = new AdminSearchController(Optional.of(searchService));

        ResponseEntity<ApiResponse> response = controller.reindex();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(searchService).reindexAll();
    }
}

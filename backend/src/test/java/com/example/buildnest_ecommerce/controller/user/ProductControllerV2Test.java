package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.elasticsearch.ProductDocument;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductSearchService;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerV2Test {

    @Test
    void getAllAndGetById() {
        ProductService productService = mock(ProductService.class);
        Page<Product> page = new PageImpl<>(Collections.singletonList(new Product()));
        when(productService.findAll(any())).thenReturn(page);
        when(productService.findById(1L)).thenReturn(new Product());

        ProductControllerV2 controller = new ProductControllerV2(productService, Optional.empty());
        assertEquals(HttpStatus.OK, controller.getAllProducts(0, 10, "id", Sort.Direction.ASC).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getProduct(1L).getStatusCode());
    }

    @Test
    @DisplayName("searchProducts — when Elasticsearch bean is present, delegates to ProductSearchService")
    void searchProducts_withElasticsearchPresent_delegatesToSearchService() {
        ProductService productService = mock(ProductService.class);
        ProductSearchService searchService = mock(ProductSearchService.class);
        Page<ProductDocument> esPage = new PageImpl<>(Collections.emptyList());
        when(searchService.search(any(), any(), any(), any(), any(), any())).thenReturn(esPage);

        ProductControllerV2 controller = new ProductControllerV2(productService, Optional.of(searchService));
        ResponseEntity<ApiResponse> response = controller.searchProducts(
                "cement", null, null, null, null, 0, 10, "id", Sort.Direction.ASC);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchService).search(eq("cement"), isNull(), isNull(), isNull(), isNull(), any());
        verify(productService, never()).advancedSearch(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchAndCategoryLimit() {
        ProductService productService = mock(ProductService.class);
        Page<Product> page = new PageImpl<>(Collections.singletonList(new Product()));
        when(productService.advancedSearch(any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(productService.findByCategory(eq(1L), any())).thenReturn(page);

        ProductControllerV2 controller = new ProductControllerV2(productService, Optional.empty());
        assertEquals(HttpStatus.OK,
                controller
                        .searchProducts("q", 1L, BigDecimal.ONE, BigDecimal.TEN, true, 0, 10, "id", Sort.Direction.DESC)
                        .getStatusCode());

        ResponseEntity<ApiResponse> response = controller.getProductsByCategory(1L, 0, 200);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

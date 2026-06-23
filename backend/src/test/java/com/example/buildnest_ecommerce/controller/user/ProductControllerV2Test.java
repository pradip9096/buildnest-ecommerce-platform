package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerV2Test {

    @Test
    void getAllAndGetById() {
        ProductService productService = mock(ProductService.class);
        Page<Product> page = new PageImpl<>(Collections.singletonList(new Product()));
        when(productService.findAll(any())).thenReturn(page);
        when(productService.findById(1L)).thenReturn(new Product());

        ProductControllerV2 controller = new ProductControllerV2(productService);
        assertEquals(HttpStatus.OK, controller.getAllProducts(0, 10, "id", Sort.Direction.ASC).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getProduct(1L).getStatusCode());
    }

    @Test
    void searchAndCategoryLimit() {
        ProductService productService = mock(ProductService.class);
        Page<Product> page = new PageImpl<>(Collections.singletonList(new Product()));
        when(productService.advancedSearch(any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(productService.findByCategory(eq(1L), any())).thenReturn(page);

        ProductControllerV2 controller = new ProductControllerV2(productService);
        assertEquals(HttpStatus.OK,
                controller
                        .searchProducts("q", 1L, BigDecimal.ONE, BigDecimal.TEN, true, 0, 10, "id", Sort.Direction.DESC)
                        .getStatusCode());

        ResponseEntity<ApiResponse> response = controller.getProductsByCategory(1L, 0, 200);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

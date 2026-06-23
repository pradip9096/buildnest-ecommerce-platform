package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminProductControllerTest {

    @Test
    void getAllProductsSuccessAndFailure() {
        ProductService productService = mock(ProductService.class);
        when(productService.getAllProducts()).thenReturn(Collections.singletonList(new Product()));

        AdminProductController controller = new AdminProductController(productService);
        ResponseEntity<ApiResponse> ok = controller.getAllProducts();
        assertEquals(HttpStatus.OK, ok.getStatusCode());

        when(productService.getAllProducts()).thenThrow(new RuntimeException("fail"));
        ResponseEntity<ApiResponse> bad = controller.getAllProducts();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, bad.getStatusCode());
    }

    @Test
    void getProductByIdSuccessAndFailure() {
        ProductService productService = mock(ProductService.class);
        when(productService.getProductById(1L)).thenReturn(new Product());
        when(productService.getProductById(2L)).thenThrow(new RuntimeException("not found"));

        AdminProductController controller = new AdminProductController(productService);
        assertEquals(HttpStatus.OK, controller.getProductById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getProductById(2L).getStatusCode());
    }

    @Test
    void createAndUpdateAndDeleteProduct() {
        ProductService productService = mock(ProductService.class);
        CreateProductRequest request = new CreateProductRequest("name", "desc desc", BigDecimal.TEN,
                BigDecimal.ONE, 1, "SKU", 1L, "http://image");
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(new Product());
        when(productService.updateProduct(eq(1L), any(CreateProductRequest.class))).thenReturn(new Product());

        AdminProductController controller = new AdminProductController(productService);
        assertEquals(HttpStatus.CREATED, controller.createProduct(request).getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateProduct(1L, request).getStatusCode());

        doThrow(new RuntimeException("bad")).when(productService).deleteProduct(2L);
        assertEquals(HttpStatus.OK, controller.deleteProduct(1L).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteProduct(2L).getStatusCode());
    }
}

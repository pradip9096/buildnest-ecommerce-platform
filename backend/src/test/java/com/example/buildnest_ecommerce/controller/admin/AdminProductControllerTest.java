package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.product.ProductService;
import com.example.buildnest_ecommerce.service.product.ProductVariantService;
import com.example.buildnest_ecommerce.service.storage.StorageException;
import com.example.buildnest_ecommerce.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminProductControllerTest {

    private ProductService productService;
    private ProductVariantService productVariantService;
    private StorageService storageService;
    private AdminProductController controller;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        productVariantService = mock(ProductVariantService.class);
        storageService = mock(StorageService.class);
        controller = new AdminProductController(productService, productVariantService, storageService);
    }

    @Test
    void getAllProductsSuccessAndFailure() {
        when(productService.getAllProducts()).thenReturn(Collections.singletonList(new Product()));
        assertEquals(HttpStatus.OK, controller.getAllProducts().getStatusCode());

        when(productService.getAllProducts()).thenThrow(new RuntimeException("fail"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllProducts().getStatusCode());
    }

    @Test
    void getProductByIdSuccessAndFailure() {
        when(productService.getProductById(1L)).thenReturn(new Product());
        when(productService.getProductById(2L)).thenThrow(new RuntimeException("not found"));

        assertEquals(HttpStatus.OK, controller.getProductById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getProductById(2L).getStatusCode());
    }

    @Test
    void createUpdateDeleteProduct() {
        CreateProductRequest request = new CreateProductRequest("name", "desc desc", BigDecimal.TEN,
                BigDecimal.ONE, 1, "SKU", 1L, "http://image", false);
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(new Product());
        when(productService.updateProduct(eq(1L), any(CreateProductRequest.class))).thenReturn(new Product());

        assertEquals(HttpStatus.CREATED, controller.createProduct(request).getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateProduct(1L, request).getStatusCode());

        doThrow(new RuntimeException("bad")).when(productService).deleteProduct(2L);
        assertEquals(HttpStatus.OK, controller.deleteProduct(1L).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteProduct(2L).getStatusCode());
    }

    @Test
    void uploadProductImageSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(storageService.store(file)).thenReturn("/uploads/img.jpg");
        when(productService.updateProductImage(1L, "/uploads/img.jpg")).thenReturn(new Product());

        ResponseEntity<ApiResponse> response = controller.uploadProductImage(1L, file);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void uploadProductImageStorageFailure() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1});
        when(storageService.store(file)).thenThrow(new StorageException("Unsupported file type"));

        ResponseEntity<ApiResponse> response = controller.uploadProductImage(1L, file);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createProduct_serviceThrows_returns400() {
        CreateProductRequest request = new CreateProductRequest("name", "desc desc", BigDecimal.TEN,
                BigDecimal.ONE, 1, "SKU", 1L, "http://image", false);
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new RuntimeException("duplicate SKU"));

        assertEquals(HttpStatus.BAD_REQUEST, controller.createProduct(request).getStatusCode());
    }

    @Test
    void updateProduct_serviceThrows_returns400() {
        CreateProductRequest request = new CreateProductRequest("name", "desc desc", BigDecimal.TEN,
                BigDecimal.ONE, 1, "SKU", 1L, "http://image", false);
        when(productService.updateProduct(eq(1L), any(CreateProductRequest.class)))
                .thenThrow(new RuntimeException("not found"));

        assertEquals(HttpStatus.BAD_REQUEST, controller.updateProduct(1L, request).getStatusCode());
    }

    @Test
    void uploadProductImage_generalException_returns500() {
        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[]{1});
        when(storageService.store(file)).thenReturn("/uploads/img.jpg");
        when(productService.updateProductImage(eq(1L), anyString()))
                .thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<ApiResponse> response = controller.uploadProductImage(1L, file);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}

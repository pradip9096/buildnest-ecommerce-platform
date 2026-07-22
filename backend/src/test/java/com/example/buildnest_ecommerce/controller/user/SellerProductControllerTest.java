package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.dto.CreateProductRequest;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SellerProductControllerTest {

    private static CustomUserDetails userDetails(Long id) {
        return new CustomUserDetails(id, "seller-user", "s@example.com",
                "hash", Collections.emptyList(), true, true, true, true);
    }

    private static CreateProductRequest sampleRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Test Product");
        request.setDescription("A sample description for testing");
        request.setPrice(BigDecimal.valueOf(100));
        request.setCategoryId(1L);
        return request;
    }

    @Test
    void getOwnProducts_returnsOkWithSellerScopedPage() {
        ProductService productService = mock(ProductService.class);
        Product product = new Product();
        product.setId(1L);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productService.getProductsForSeller(5L, PageRequest.of(0, 10)))
                .thenReturn(page);

        SellerProductController controller =
                new SellerProductController(productService);
        ResponseEntity<?> response = controller.getOwnProducts(
                userDetails(5L), PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService)
                .getProductsForSeller(5L, PageRequest.of(0, 10));
    }

    @Test
    void createOwnProduct_returnsCreatedWithBody() {
        ProductService productService = mock(ProductService.class);
        CreateProductRequest request = sampleRequest();
        Product created = new Product();
        created.setId(1L);
        when(productService.createProductForSeller(request, 5L))
                .thenReturn(created);

        SellerProductController controller =
                new SellerProductController(productService);
        ResponseEntity<?> response =
                controller.createOwnProduct(userDetails(5L), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void updateOwnProduct_returnsOkWithBody() {
        ProductService productService = mock(ProductService.class);
        CreateProductRequest request = sampleRequest();
        Product updated = new Product();
        updated.setId(1L);
        when(productService.updateProductForSeller(5L, 1L, request))
                .thenReturn(updated);

        SellerProductController controller =
                new SellerProductController(productService);
        ResponseEntity<?> response = controller.updateOwnProduct(
                userDetails(5L), 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteOwnProduct_returnsOkAndDelegatesToService() {
        ProductService productService = mock(ProductService.class);

        SellerProductController controller =
                new SellerProductController(productService);
        ResponseEntity<?> response =
                controller.deleteOwnProduct(userDetails(5L), 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService).deleteProductForSeller(5L, 1L);
    }
}

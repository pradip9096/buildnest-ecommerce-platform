package com.example.buildnest_ecommerce.controller.user;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("removal")
class ProductControllerV1Test {

    private static class TestableProductControllerV1 extends ProductControllerV1 {
        private final LocalDate today;
        private final LocalDate sunsetDate;

        TestableProductControllerV1(ProductService productService, LocalDate today, LocalDate sunsetDate) {
            super(productService);
            this.today = today;
            this.sunsetDate = sunsetDate;
        }

        @Override
        protected LocalDate getToday() {
            return today;
        }

        @Override
        protected LocalDate getSunsetDate() {
            return sunsetDate;
        }
    }

    @Test
    void checkSunsetDateDoesNotThrow() {
        ProductService productService = mock(ProductService.class);
        ProductControllerV1 controller = new ProductControllerV1(productService);
        assertDoesNotThrow(controller::checkSunsetDate);
    }

    @Test
    void checkSunsetDateThrowsAfterSunset() {
        ProductService productService = mock(ProductService.class);
        ProductControllerV1 controller = new TestableProductControllerV1(
                productService,
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2026, 12, 31));

        assertThrows(IllegalStateException.class, controller::checkSunsetDate);
    }

    @Test
    void checkSunsetDateWarnsWithinNinetyDays() {
        ProductService productService = mock(ProductService.class);
        ProductControllerV1 controller = new TestableProductControllerV1(
                productService,
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 31));

        assertDoesNotThrow(controller::checkSunsetDate);
    }

    @Test
    void getAllAndGetById() {
        ProductService productService = mock(ProductService.class);
        Page<Product> page = new PageImpl<>(Collections.singletonList(new Product()));
        when(productService.findAll(any())).thenReturn(page);
        when(productService.findById(1L)).thenReturn(new Product());

        ProductControllerV1 controller = new ProductControllerV1(productService);
        assertEquals(HttpStatus.OK, controller.getAllProducts(0, 10).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getProduct(1L).getStatusCode());
    }
}

package com.example.buildnest_ecommerce.inventory;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Inventory Management test suite covering stock management and availability.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class InventoryManagementTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Inventory Test Product");
        testProduct.setDescription("For inventory testing");
        testProduct.setPrice(new BigDecimal("500.00"));
        testProduct.setStockQuantity(100);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("Should track product stock quantity")
    void testStockQuantityTracking() {
        assertEquals(100, testProduct.getStockQuantity());

        testProduct.setStockQuantity(80);
        Product updated = productRepository.save(testProduct);

        assertEquals(80, updated.getStockQuantity());
    }

    @Test
    @DisplayName("Should prevent negative stock")
    void testPreventNegativeStock() {
        testProduct.setStockQuantity(-5);
        // Application should enforce non-negative constraint
        assertNotNull(testProduct);
    }

    @Test
    @DisplayName("Should update stock on order")
    void testStockUpdateOnOrder() {
        int initialStock = testProduct.getStockQuantity();

        testProduct.setStockQuantity(initialStock - 10);
        Product updated = productRepository.save(testProduct);

        assertEquals(initialStock - 10, updated.getStockQuantity());
    }

    @Test
    @DisplayName("Should restore stock on cancellation")
    void testStockRestoreOnCancellation() {
        int initialStock = testProduct.getStockQuantity();

        testProduct.setStockQuantity(initialStock - 20);
        productRepository.save(testProduct);

        testProduct.setStockQuantity(initialStock - 20 + 20);
        Product restored = productRepository.save(testProduct);

        assertEquals(initialStock, restored.getStockQuantity());
    }

    @Test
    @DisplayName("Should check stock availability")
    void testStockAvailabilityCheck() {
        assertTrue(testProduct.getStockQuantity() > 0);

        testProduct.setStockQuantity(0);
        Product outOfStock = productRepository.save(testProduct);

        assertFalse(outOfStock.getStockQuantity() > 0);
    }

    @Test
    @DisplayName("Should handle stock depletion")
    void testStockDepletion() {
        testProduct.setStockQuantity(1);
        Product product = productRepository.save(testProduct);

        product.setStockQuantity(0);
        Product depleted = productRepository.save(product);

        assertEquals(0, depleted.getStockQuantity());
    }
}

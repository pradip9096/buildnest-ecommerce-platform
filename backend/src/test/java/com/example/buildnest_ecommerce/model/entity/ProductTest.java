package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product entity tests")
class ProductTest {

    @Test
    @DisplayName("Should create Product with all fields")
    void testProductConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.now().plusMonths(6);
        Category category = new Category();
        category.setId(1L);
        Inventory inventory = new Inventory();
        inventory.setId(1L);

        Product product = new Product(
                10L,
                "Test Product",
                "Description here",
                new BigDecimal("99.99"),
                new BigDecimal("79.99"),
                100,
                "SKU-123",
                category,
                inventory,
                "http://image.url",
                expiryDate,
                true,
                now,
                now);

        assertEquals(10L, product.getId());
        assertEquals("Test Product", product.getName());
        assertEquals("Description here", product.getDescription());
        assertEquals(new BigDecimal("99.99"), product.getPrice());
        assertEquals(new BigDecimal("79.99"), product.getDiscountPrice());
        assertEquals(100, product.getStockQuantity());
        assertEquals("SKU-123", product.getSku());
        assertEquals(category, product.getCategory());
        assertEquals(inventory, product.getInventory());
        assertEquals("http://image.url", product.getImageUrl());
        assertEquals(expiryDate, product.getExpiryDate());
        assertTrue(product.getIsActive());
        assertEquals(now, product.getCreatedAt());
        assertEquals(now, product.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create Product with no-args constructor")
    void testNoArgsConstructor() {
        Product product = new Product();
        assertNotNull(product);
        assertNull(product.getId());
        assertNull(product.getName());
    }

    @Test
    @DisplayName("Should set and get Product fields")
    void testSettersAndGetters() {
        Product product = new Product();
        LocalDate expiryDate = LocalDate.now();
        Category category = new Category();
        category.setId(5L);

        product.setId(20L);
        product.setName("Updated Product");
        product.setDescription("New description");
        product.setPrice(new BigDecimal("150.00"));
        product.setDiscountPrice(new BigDecimal("120.00"));
        product.setStockQuantity(50);
        product.setSku("SKU-456");
        product.setCategory(category);
        product.setImageUrl("http://new-image.url");
        product.setExpiryDate(expiryDate);
        product.setIsActive(false);

        assertEquals(20L, product.getId());
        assertEquals("Updated Product", product.getName());
        assertEquals("New description", product.getDescription());
        assertEquals(new BigDecimal("150.00"), product.getPrice());
        assertEquals(new BigDecimal("120.00"), product.getDiscountPrice());
        assertEquals(50, product.getStockQuantity());
        assertEquals("SKU-456", product.getSku());
        assertEquals(category, product.getCategory());
        assertEquals("http://new-image.url", product.getImageUrl());
        assertEquals(expiryDate, product.getExpiryDate());
        assertFalse(product.getIsActive());
    }

    @Test
    @DisplayName("Should test equals excludes category and inventory")
    void testEqualsExcludesRelations() {
        Category category1 = new Category();
        category1.setId(1L);
        Category category2 = new Category();
        category2.setId(2L);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setCategory(category1);

        Product product2 = new Product();
        product2.setId(1L);
        product2.setName("Product");
        product2.setPrice(new BigDecimal("100.00"));
        product2.setCategory(category2); // Different category

        // Should still be equal because category is excluded
        assertEquals(product1, product2);
    }

    @Test
    @DisplayName("Should test equals with different core fields")
    void testEqualsDifferentFields() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product A");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product B");

        assertNotEquals(product1, product2);
    }

    @Test
    @DisplayName("Should test equals with null and different types")
    void testEqualsNullAndDifferentType() {
        Product product = new Product();
        product.setId(1L);

        assertNotEquals(product, null);
        assertNotEquals(product, "Not a Product");
        assertEquals(product, product);
    }

    @Test
    @DisplayName("Should test toString excludes category and inventory")
    void testToString() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));

        Category category = new Category();
        category.setId(1L);
        product.setCategory(category);

        String result = product.toString();
        assertTrue(result.contains("Product"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("Test Product"));
        // Category should be excluded from toString
        assertFalse(result.contains("category"));
    }

    @Test
    @DisplayName("Should test hashCode consistency")
    void testHashCodeConsistency() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Product");
        product.setPrice(new BigDecimal("100.00"));

        int hash1 = product.hashCode();
        int hash2 = product.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should test Product with null fields")
    void testWithNullFields() {
        Product product1 = new Product();
        Product product2 = new Product();

        assertEquals(product1, product2);
        assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    @DisplayName("Should test Product SKU field")
    void testSkuField() {
        Product product = new Product();
        assertNull(product.getSku());

        product.setSku("SKU-XYZ-789");
        assertEquals("SKU-XYZ-789", product.getSku());
    }

    @Test
    @DisplayName("Should test Product isActive default")
    void testIsActiveDefault() {
        Product product = new Product();
        assertTrue(product.getIsActive()); // Default is true

        product.setIsActive(true);
        assertTrue(product.getIsActive());

        product.setIsActive(false);
        assertFalse(product.getIsActive());
    }

    @Test
    @DisplayName("Should test Product with discount price")
    void testDiscountPrice() {
        Product product = new Product();
        product.setPrice(new BigDecimal("100.00"));
        product.setDiscountPrice(new BigDecimal("85.00"));

        assertEquals(new BigDecimal("100.00"), product.getPrice());
        assertEquals(new BigDecimal("85.00"), product.getDiscountPrice());
    }

    @Test
    @DisplayName("Should test Product with expiry date")
    void testExpiryDate() {
        Product product = new Product();
        LocalDate expiry = LocalDate.of(2025, 12, 31);

        product.setExpiryDate(expiry);
        assertEquals(expiry, product.getExpiryDate());
    }
}

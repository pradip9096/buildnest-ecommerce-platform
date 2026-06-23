package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Category entity tests")
class CategoryTest {

    @Test
    @DisplayName("Should create Category with all fields")
    void testCategoryConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        Set<Product> products = new HashSet<>();

        Category category = new Category(
                10L,
                "Electronics",
                "Electronic products",
                "http://image.url",
                true,
                50,
                now,
                now,
                products);

        assertEquals(10L, category.getId());
        assertEquals("Electronics", category.getName());
        assertEquals("Electronic products", category.getDescription());
        assertEquals("http://image.url", category.getImageUrl());
        assertTrue(category.getIsActive());
        assertEquals(50, category.getMinimumStockThreshold());
        assertEquals(now, category.getCreatedAt());
        assertEquals(now, category.getUpdatedAt());
        assertEquals(products, category.getProducts());
    }

    @Test
    @DisplayName("Should create Category with no-args constructor")
    void testNoArgsConstructor() {
        Category category = new Category();
        assertNotNull(category);
        assertNull(category.getId());
        assertNull(category.getName());
    }

    @Test
    @DisplayName("Should set and get Category fields")
    void testSettersAndGetters() {
        Category category = new Category();
        LocalDateTime now = LocalDateTime.now();

        category.setId(20L);
        category.setName("Books");
        category.setDescription("Book products");
        category.setImageUrl("http://books-image.url");
        category.setIsActive(false);
        category.setMinimumStockThreshold(100);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        assertEquals(20L, category.getId());
        assertEquals("Books", category.getName());
        assertEquals("Book products", category.getDescription());
        assertEquals("http://books-image.url", category.getImageUrl());
        assertFalse(category.getIsActive());
        assertEquals(100, category.getMinimumStockThreshold());
        assertEquals(now, category.getCreatedAt());
        assertEquals(now, category.getUpdatedAt());
    }

    @Test
    @DisplayName("Should test equals excludes products")
    void testEqualsExcludesProducts() {
        Set<Product> products1 = new HashSet<>();
        Product p1 = new Product();
        p1.setId(1L);
        products1.add(p1);

        Set<Product> products2 = new HashSet<>();
        Product p2 = new Product();
        p2.setId(2L);
        products2.add(p2);

        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Electronics");
        cat1.setProducts(products1);

        Category cat2 = new Category();
        cat2.setId(1L);
        cat2.setName("Electronics");
        cat2.setProducts(products2); // Different products

        // Should still be equal because products is excluded
        assertEquals(cat1, cat2);
    }

    @Test
    @DisplayName("Should test equals with different Categories")
    void testEqualsDifferentCategories() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Electronics");

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Books");

        assertNotEquals(cat1, cat2);
    }

    @Test
    @DisplayName("Should test equals with null and different types")
    void testEqualsNullAndDifferentType() {
        Category category = new Category();
        category.setId(1L);

        assertNotEquals(category, null);
        assertNotEquals(category, "Not a Category");
        assertEquals(category, category);
    }

    @Test
    @DisplayName("Should test toString excludes products")
    void testToString() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Set<Product> products = new HashSet<>();
        Product p1 = new Product();
        p1.setId(1L);
        products.add(p1);
        category.setProducts(products);

        String result = category.toString();
        assertTrue(result.contains("Category"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("Electronics"));
        // Products should be excluded from toString
        assertFalse(result.contains("products"));
    }

    @Test
    @DisplayName("Should test hashCode consistency")
    void testHashCodeConsistency() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        int hash1 = category.hashCode();
        int hash2 = category.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should test Category with null fields")
    void testWithNullFields() {
        Category cat1 = new Category();
        Category cat2 = new Category();

        assertEquals(cat1, cat2);
        assertEquals(cat1.hashCode(), cat2.hashCode());
    }

    @Test
    @DisplayName("Should test minimumStockThreshold default")
    void testMinimumStockThresholdDefault() {
        Category category = new Category();
        assertEquals(0, category.getMinimumStockThreshold()); // Default is 0

        category.setMinimumStockThreshold(0);
        assertEquals(0, category.getMinimumStockThreshold());

        category.setMinimumStockThreshold(50);
        assertEquals(50, category.getMinimumStockThreshold());
    }

    @Test
    @DisplayName("Should test isActive flag")
    void testIsActive() {
        Category category = new Category();
        assertTrue(category.getIsActive()); // Default is true

        category.setIsActive(true);
        assertTrue(category.getIsActive());

        category.setIsActive(false);
        assertFalse(category.getIsActive());
    }
}

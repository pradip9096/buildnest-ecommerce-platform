package com.example.buildnest_ecommerce.category;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
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
 * Category Management test suite covering product categorization and
 * navigation.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class CategoryManagementTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Building Materials");
        testCategory.setDescription("Construction materials");
        testCategory = categoryRepository.save(testCategory);

        testProduct = new Product();
        testProduct.setName("Cement");
        testProduct.setDescription("Portland cement");
        testProduct.setPrice(new BigDecimal("350.00"));
        testProduct.setCategory(testCategory);
        testProduct.setStockQuantity(500);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("Should create category")
    void testCreateCategory() {
        assertNotNull(testCategory.getId());
        assertEquals("Building Materials", testCategory.getName());
    }

    @Test
    @DisplayName("Should retrieve products by category")
    void testGetProductsByCategory() {
        var products = productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(testCategory.getId()))
                .toList();

        assertTrue(products.size() > 0);
        assertTrue(products.stream().allMatch(p -> p.getCategory().getId().equals(testCategory.getId())));
    }

    @Test
    @DisplayName("Should update category")
    void testUpdateCategory() {
        testCategory.setName("Updated Building Materials");
        Category updated = categoryRepository.save(testCategory);

        assertEquals("Updated Building Materials", updated.getName());
    }

    @Test
    @DisplayName("Should delete category")
    void testDeleteCategory() {
        Long categoryId = testCategory.getId();
        categoryRepository.deleteById(categoryId);

        var retrieved = categoryRepository.findById(categoryId);
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should handle category with multiple products")
    void testCategoryWithMultipleProducts() {
        Product product2 = new Product();
        product2.setName("Steel Bars");
        product2.setDescription("Reinforcement steel");
        product2.setPrice(new BigDecimal("450.00"));
        product2.setCategory(testCategory);
        product2.setStockQuantity(300);
        product2.setIsActive(true);
        productRepository.save(product2);

        var categoryProducts = productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(testCategory.getId()))
                .toList();

        assertTrue(categoryProducts.size() >= 2);
    }

    @Test
    @DisplayName("Should support category search")
    void testCategorySearch() {
        var categories = categoryRepository.findAll().stream()
                .filter(c -> c.getName().contains("Building"))
                .toList();

        assertTrue(categories.size() > 0);
    }
}

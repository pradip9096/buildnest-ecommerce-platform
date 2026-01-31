package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        entityManager.persist(testCategory);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setCategory(testCategory);
        testProduct.setIsActive(true);
        entityManager.persist(testProduct);
        entityManager.flush();
    }

    @Test
    void testFindById() {
        // Act
        Optional<Product> found = productRepository.findById(testProduct.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(testProduct.getName(), found.get().getName());
    }

    @Test
    void testSaveProduct() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");
        newProduct.setPrice(BigDecimal.valueOf(200.00));
        newProduct.setCategory(testCategory);
        newProduct.setIsActive(true);

        // Act
        Product saved = productRepository.save(newProduct);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("New Product", saved.getName());
    }

    @Test
    void testDeleteProduct() {
        // Act
        productRepository.delete(testProduct);
        entityManager.flush();
        Optional<Product> found = productRepository.findById(testProduct.getId());

        // Assert
        assertFalse(found.isPresent());
    }
}

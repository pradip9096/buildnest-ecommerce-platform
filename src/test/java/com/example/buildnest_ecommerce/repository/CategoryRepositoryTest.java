package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic devices and accessories");
        testCategory.setIsActive(true);
        testCategory.setMinimumStockThreshold(10);
        testCategory.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testCategory);
        entityManager.flush();
    }

    @Test
    @DisplayName("TC-CAT-REPO-001: Find category by name")
    void testFindByName() {
        // Act
        List<Category> categories = categoryRepository.findAll();
        Optional<Category> found = categories.stream()
                .filter(c -> "Electronics".equals(c.getName()))
                .findFirst();

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Electronics", found.get().getName());
        assertEquals("Electronic devices and accessories", found.get().getDescription());
    }

    @Test
    @DisplayName("TC-CAT-REPO-002: Find active categories")
    void testFindActiveCategories() {
        // Arrange
        Category activeCategory1 = new Category();
        activeCategory1.setName("Home & Garden");
        activeCategory1.setDescription("Home improvement products");
        activeCategory1.setIsActive(true);
        activeCategory1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(activeCategory1);

        Category activeCategory2 = new Category();
        activeCategory2.setName("Sports");
        activeCategory2.setDescription("Sports equipment");
        activeCategory2.setIsActive(true);
        activeCategory2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(activeCategory2);

        Category inactiveCategory = new Category();
        inactiveCategory.setName("Discontinued");
        inactiveCategory.setDescription("Discontinued products");
        inactiveCategory.setIsActive(false);
        inactiveCategory.setCreatedAt(LocalDateTime.now());
        entityManager.persist(inactiveCategory);

        entityManager.flush();

        // Act
        List<Category> allCategories = categoryRepository.findAll();
        long activeCount = allCategories.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .count();

        // Assert
        assertEquals(3, activeCount); // testCategory + 2 new active categories
        assertEquals(4, allCategories.size()); // Total including inactive
    }

    @Test
    @DisplayName("TC-CAT-REPO-003: Category hierarchy (flat structure)")
    void testCategoryHierarchy() {
        // Arrange
        Category parentCategory = new Category();
        parentCategory.setName("Parent Category");
        parentCategory.setDescription("Top-level category");
        parentCategory.setIsActive(true);
        parentCategory.setCreatedAt(LocalDateTime.now());
        entityManager.persist(parentCategory);

        // Note: Current schema doesn't support parent-child relationships
        // This test verifies flat category structure works correctly

        entityManager.flush();

        // Act
        List<Category> categories = categoryRepository.findAll();

        // Assert
        assertEquals(2, categories.size()); // testCategory + parentCategory
        assertTrue(categories.stream().allMatch(c -> c.getIsActive()));
    }

    @Test
    @DisplayName("TC-CAT-REPO-004: Category product count")
    void testCategoryProductCount() {
        // Arrange
        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("Gaming laptop");
        product1.setPrice(BigDecimal.valueOf(1200.00));
        product1.setCategory(testCategory);
        product1.setIsActive(true);
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("Mouse");
        product2.setDescription("Wireless mouse");
        product2.setPrice(BigDecimal.valueOf(25.00));
        product2.setCategory(testCategory);
        product2.setIsActive(true);
        entityManager.persist(product2);

        Product product3 = new Product();
        product3.setName("Keyboard");
        product3.setDescription("Mechanical keyboard");
        product3.setPrice(BigDecimal.valueOf(80.00));
        product3.setCategory(testCategory);
        product3.setIsActive(true);
        entityManager.persist(product3);

        entityManager.flush();
        Long categoryId = testCategory.getId();
        entityManager.clear();

        // Act - Query product count directly instead of loading collection
        Long productCount = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId", Long.class)
                .setParameter("categoryId", categoryId)
                .getSingleResult();

        // Assert
        assertEquals(3, productCount);
    }

    @Test
    @DisplayName("TC-CAT-REPO-005: Delete category with products (cascade)")
    void testDeleteCategoryWithProducts() {
        // Arrange
        Category categoryToDelete = new Category();
        categoryToDelete.setName("To Be Deleted");
        categoryToDelete.setDescription("This category will be deleted");
        categoryToDelete.setIsActive(true);
        categoryToDelete.setCreatedAt(LocalDateTime.now());
        categoryToDelete.setProducts(new HashSet<>());
        entityManager.persist(categoryToDelete);

        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Product in category to be deleted");
        product.setPrice(BigDecimal.valueOf(50.00));
        product.setCategory(categoryToDelete);
        product.setIsActive(true);
        entityManager.persist(product);

        categoryToDelete.getProducts().add(product);
        entityManager.flush();

        Long categoryId = categoryToDelete.getId();
        Long productId = product.getId();

        // Act
        categoryRepository.deleteById(categoryId);
        entityManager.flush();
        entityManager.clear();

        // Assert - Category should be deleted
        assertFalse(categoryRepository.findById(categoryId).isPresent());

        // Product should also be deleted due to cascade
        assertNull(entityManager.find(Product.class, productId));
    }
}

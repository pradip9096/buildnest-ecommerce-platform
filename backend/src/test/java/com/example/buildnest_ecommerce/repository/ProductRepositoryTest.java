package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.model.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

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

    @Test
    void testAdvancedSearchFiltersByTag() {
        // Arrange
        ProductTag ecoTag = new ProductTag();
        ecoTag.setName("Eco-Friendly");
        ecoTag.setSlug("eco-friendly");
        entityManager.persist(ecoTag);

        Product taggedProduct = new Product();
        taggedProduct.setName("Bamboo Shelf");
        taggedProduct.setDescription("Sustainable shelving");
        taggedProduct.setPrice(BigDecimal.valueOf(50.00));
        taggedProduct.setCategory(testCategory);
        taggedProduct.setIsActive(true);
        taggedProduct.setTags(Set.of(ecoTag));
        entityManager.persist(taggedProduct);

        // advancedSearch's inStock clause implicitly joins p.inventory (single-valued
        // association -> implicit inner join in JPQL), so any product without an
        // Inventory row is excluded from every advancedSearch result regardless of the
        // inStock filter value — every product needs one here for that reason, matching
        // production where Product->Inventory is always created together.
        com.example.buildnest_ecommerce.model.entity.Inventory inventory =
                new com.example.buildnest_ecommerce.model.entity.Inventory();
        inventory.setProduct(taggedProduct);
        inventory.setQuantityInStock(10);
        inventory.setQuantityReserved(0);
        inventory.setMinimumStockLevel(3);
        entityManager.persist(inventory);

        entityManager.flush();
        entityManager.clear();

        // Act
        Page<Product> tagged = productRepository.advancedSearch(
                null, null, null, null, null, true, "Eco-Friendly", PageRequest.of(0, 10));
        Page<Product> untagged = productRepository.advancedSearch(
                null, null, null, null, null, true, "Nonexistent Tag", PageRequest.of(0, 10));

        // Assert
        assertEquals(1, tagged.getTotalElements());
        assertEquals("Bamboo Shelf", tagged.getContent().get(0).getName());
        assertEquals(0, untagged.getTotalElements());
    }
}

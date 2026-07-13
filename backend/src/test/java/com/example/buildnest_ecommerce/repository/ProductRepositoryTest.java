package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Inventory;
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
import java.util.List;
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

    // --- findRelatedProducts (PROD-04, #84) ---
    //
    // These scenarios were added after review found #84's acceptance criteria
    // (same-category-first ranking, source exclusion, inactive/out-of-stock
    // exclusion) were only ever exercised at the service layer against a
    // *mocked* repository (ProductServiceImplTest) — proving parameters were
    // passed through correctly, but never proving the JPQL query itself
    // actually implements any of those criteria against real data.

    private Product persistProduct(String name, Category category, boolean active, Set<ProductTag> tags,
            Integer quantityInStock, Integer quantityReserved) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(name);
        product.setPrice(BigDecimal.valueOf(75.00));
        product.setCategory(category);
        product.setIsActive(active);
        if (tags != null) {
            product.setTags(tags);
        }
        entityManager.persist(product);

        if (quantityInStock != null) {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantityInStock(quantityInStock);
            inventory.setQuantityReserved(quantityReserved == null ? 0 : quantityReserved);
            inventory.setMinimumStockLevel(3);
            entityManager.persist(inventory);
        }
        return product;
    }

    @Test
    void testFindRelatedProductsRanksSameCategoryBeforeTagOnlyMatches() {
        // Arrange
        ProductTag sharedTag = new ProductTag();
        sharedTag.setName("Waterproof");
        sharedTag.setSlug("waterproof");
        entityManager.persist(sharedTag);

        Category otherCategory = new Category();
        otherCategory.setName("Other Category");
        otherCategory.setDescription("Other Description");
        entityManager.persist(otherCategory);

        Product source = persistProduct("Source Product", testCategory, true, Set.of(sharedTag), 10, 0);
        persistProduct("Same Category Match", testCategory, true, null, 10, 0);
        persistProduct("Tag-Only Match", otherCategory, true, Set.of(sharedTag), 10, 0);
        persistProduct("Unrelated Product", otherCategory, true, null, 10, 0); // control, must not appear

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Product> results = productRepository.findRelatedProducts(
                source.getId(), testCategory.getId(), List.of(sharedTag.getId()), PageRequest.of(0, 8));

        // Assert — same-category match ranked ahead of the tag-only match,
        // source excluded, unrelated product excluded
        assertEquals(2, results.size());
        assertEquals("Same Category Match", results.get(0).getName());
        assertEquals("Tag-Only Match", results.get(1).getName());
    }

    @Test
    void testFindRelatedProductsExcludesSourceProductItself() {
        // Arrange — source is the only product in its category
        Product source = persistProduct("Only In Category", testCategory, true, null, 10, 0);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Product> results = productRepository.findRelatedProducts(
                source.getId(), testCategory.getId(), List.of(-1L), PageRequest.of(0, 8));

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindRelatedProductsExcludesInactiveProduct() {
        // Arrange
        Product source = persistProduct("Source", testCategory, true, null, 10, 0);
        persistProduct("Inactive Match", testCategory, false, null, 10, 0);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Product> results = productRepository.findRelatedProducts(
                source.getId(), testCategory.getId(), List.of(-1L), PageRequest.of(0, 8));

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindRelatedProductsExcludesOutOfStockProduct() {
        // Arrange — zero available (quantityInStock == quantityReserved) and
        // negative available (reserved exceeds stock) both must be excluded
        Product source = persistProduct("Source", testCategory, true, null, 10, 0);
        persistProduct("Zero Available", testCategory, true, null, 5, 5);
        persistProduct("Over-Reserved", testCategory, true, null, 5, 8);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Product> results = productRepository.findRelatedProducts(
                source.getId(), testCategory.getId(), List.of(-1L), PageRequest.of(0, 8));

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindRelatedProductsExcludesProductWithNoInventoryRow() {
        // Arrange — a product with no Inventory row at all is excluded by the
        // implicit p.inventory.quantityInStock path navigation in the WHERE
        // clause (acts as an inner join), same mechanism documented in
        // docs/wiki/learned-lessons/jpql-implicit-singlevalued-path-in-where-acts-as-inner-join-even-under-param-is-null-guard.md.
        // Deliberate here (no inventory tracking = not a safe recommendation),
        // not the #365-class bug that lesson describes (which was about an
        // *optional* stock filter silently losing its "no filter" semantics —
        // findRelatedProducts always requires in-stock, unconditionally).
        Product source = persistProduct("Source", testCategory, true, null, 10, 0);
        persistProduct("No Inventory Row", testCategory, true, null, null, null);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Product> results = productRepository.findRelatedProducts(
                source.getId(), testCategory.getId(), List.of(-1L), PageRequest.of(0, 8));

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindRelatedProductsRespectsPageableLimit() {
        // Arrange — 3 same-category matches, limit to top 2
        Product source = persistProduct("Source", testCategory, true, null, 10, 0);
        persistProduct("Match A", testCategory, true, null, 10, 0);
        persistProduct("Match B", testCategory, true, null, 10, 0);
        persistProduct("Match C", testCategory, true, null, 10, 0);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Product> results = productRepository.findRelatedProducts(
                source.getId(), testCategory.getId(), List.of(-1L), PageRequest.of(0, 2));

        // Assert
        assertEquals(2, results.size());
    }
}

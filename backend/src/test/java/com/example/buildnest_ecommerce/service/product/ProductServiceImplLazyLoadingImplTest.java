package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Regression test for #481: getProductsByCategory, searchProducts, and
 * updateProductImage returned raw Product entities (tags/variants lazy)
 * without initializing them, sharing #425's LazyInitializationException
 * risk once Jackson serializes the response after the transaction closes
 * under open-in-view=false. Uses @DataJpaTest + TestEntityManager and
 * asserts Hibernate.isInitialized() directly, mirroring
 * WishlistServiceImplLazyLoadingImplTest's pattern (#442) — a
 * @Transactional-wrapped test method would keep the session open through
 * assertions and mask the exact failure a real HTTP request hits. See
 * docs/wiki/learned-lessons/raw-entity-with-lazy-collection-returned-from-
 * controller-throws-post-transaction-with-open-in-view-false.md.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductServiceImplLazyLoadingImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private ProductServiceImpl productService;
    private Long productId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        DomainEventPublisher domainEventPublisher =
                new DomainEventPublisher(mock(ApplicationEventPublisher.class));
        productService = new ProductServiceImpl(productRepository,
                categoryRepository, inventoryRepository, domainEventPublisher);

        Category category = new Category();
        category.setName("Lazy Load Category 481");
        category.setDescription("Test");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Lazy Load Product 481");
        product.setDescription("Test");
        product.setPrice(BigDecimal.valueOf(75.00));
        product.setCategory(category);
        product.setIsActive(true);
        entityManager.persist(product);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku("LAZY-VAR-481");
        variant.setPriceAdjustment(BigDecimal.ZERO);
        variant.setIsActive(true);
        entityManager.persist(variant);

        ProductTag tag = new ProductTag();
        tag.setName("Lazy Tag 481");
        tag.setSlug("lazy-tag-481");
        entityManager.persist(tag);
        Set<ProductTag> tags = new HashSet<>();
        tags.add(tag);
        product.setTags(tags);
        entityManager.persist(product);

        entityManager.flush();
        entityManager.clear();

        productId = product.getId();
        categoryId = category.getId();
    }

    @Test
    void getProductsByCategory_eagerlyInitializesTagsAndVariants() {
        List<Product> products =
                productService.getProductsByCategory(categoryId);

        assertEquals(1, products.size());
        Product product = products.get(0);
        assertTrue(Hibernate.isInitialized(product.getTags()),
                "tags must be eagerly initialized — an uninitialized proxy "
                        + "throws LazyInitializationException once Jackson "
                        + "serializes it outside the transaction under "
                        + "open-in-view=false");
        assertTrue(Hibernate.isInitialized(product.getVariants()),
                "variants must be eagerly initialized for the same reason");
    }

    @Test
    void searchProducts_eagerlyInitializesTagsAndVariants() {
        List<Product> products =
                productService.searchProducts("Lazy Load Product 481");

        assertEquals(1, products.size());
        Product product = products.get(0);
        assertTrue(Hibernate.isInitialized(product.getTags()),
                "tags must be eagerly initialized for the same reason");
        assertTrue(Hibernate.isInitialized(product.getVariants()),
                "variants must be eagerly initialized for the same reason");
    }

    @Test
    void updateProductImage_eagerlyInitializesTagsAndVariants() {
        Product updated = productService.updateProductImage(productId,
                "https://example.com/new-image.jpg");

        assertTrue(Hibernate.isInitialized(updated.getTags()),
                "tags must be eagerly initialized for the same reason");
        assertTrue(Hibernate.isInitialized(updated.getVariants()),
                "variants must be eagerly initialized for the same reason");
    }
}

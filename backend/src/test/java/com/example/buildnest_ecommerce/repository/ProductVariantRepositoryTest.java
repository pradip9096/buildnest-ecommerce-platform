package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for the lazy-`product`-proxy bug found while implementing #427:
 * findByProductId's @EntityGraph originally listed only "inventory", leaving the
 * `product` association an uninitialized lazy proxy. Under open-in-view=false, any
 * real (non-@Transactional-test-wrapped) HTTP request serializing the result would
 * throw LazyInitializationException — masked in the controller integration test by
 * its class-level @Transactional keeping the session open through serialization.
 * Asserting Hibernate.isInitialized() here proves the entity graph actually joined
 * `product` eagerly, independent of any transaction-boundary masking.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductVariantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        entityManager.persist(category);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setCategory(category);
        testProduct.setIsActive(true);
        entityManager.persist(testProduct);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(testProduct);
        variant.setSku("TEST-VAR-001");
        variant.setPriceAdjustment(BigDecimal.ZERO);
        variant.setIsActive(true);
        entityManager.persist(variant);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByProductId_eagerlyInitializesProductAssociation() {
        List<ProductVariant> variants = productVariantRepository.findByProductId(testProduct.getId());

        assertEquals(1, variants.size());
        ProductVariant variant = variants.get(0);
        assertTrue(
                Hibernate.isInitialized(variant.getProduct()),
                "product association must be eagerly fetched — a lazy, uninitialized proxy here "
                        + "throws LazyInitializationException once Jackson serializes it outside the "
                        + "transaction under open-in-view=false");
        assertEquals("Test Product", variant.getProduct().getName());
    }
}

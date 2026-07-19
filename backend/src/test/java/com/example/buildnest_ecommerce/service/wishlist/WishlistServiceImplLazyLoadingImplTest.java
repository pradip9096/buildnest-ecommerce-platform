package com.example.buildnest_ecommerce.service.wishlist;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.entity.Wishlist;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.repository.WishlistRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for #442: WishlistController.getWishlist() (via
 * WishlistServiceImpl.getWishlistProducts) returned raw Product entities
 * with uninitialized lazy fields (category, inventory, variants, tags).
 * Under open-in-view=false, Jackson serializing the response after the
 * transaction closes threw LazyInitializationException — a bare HTTP 500
 * ("Failed to write request"), only reproducible against a persisted
 * product with lazy associations actually populated. A Mockito-mocked
 * WishlistServiceImplTest cannot observe this: a mocked repository returns
 * whatever plain Product object the test constructs, which is never a real
 * Hibernate proxy. See docs/wiki/learned-lessons/raw-entity-with-lazy-
 * collection-returned-from-controller-throws-post-transaction-with-open-
 * in-view-false.md (6th occurrence of this bug family).
 *
 * Uses @DataJpaTest + TestEntityManager, asserting Hibernate.isInitialized()
 * directly rather than relying on serialization outside a transaction —
 * mirroring ProductVariantRepositoryTest's established pattern, since a
 * @Transactional-wrapped test method would otherwise keep the session open
 * through assertions and mask the exact failure a real HTTP request hits.
 */
@DataJpaTest
@ActiveProfiles("test")
class WishlistServiceImplLazyLoadingImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private WishlistServiceImpl wishlistService;
    private Long userId;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistServiceImpl(wishlistRepository, userRepository, productRepository);

        User user = new User();
        user.setUsername("lazyload442");
        user.setEmail("lazyload442@example.com");
        user.setPassword("irrelevant");
        entityManager.persist(user);

        Category category = new Category();
        category.setName("Lazy Load Category");
        category.setDescription("Test");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Lazy Load Product");
        product.setDescription("Test");
        product.setPrice(BigDecimal.valueOf(50.00));
        product.setCategory(category);
        product.setIsActive(true);
        entityManager.persist(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(10);
        inventory.setMinimumStockLevel(1);
        entityManager.persist(inventory);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku("LAZY-VAR-001");
        variant.setPriceAdjustment(BigDecimal.ZERO);
        variant.setIsActive(true);
        entityManager.persist(variant);

        ProductTag tag = new ProductTag();
        tag.setName("Lazy Tag");
        tag.setSlug("lazy-tag");
        entityManager.persist(tag);
        Set<ProductTag> tags = new HashSet<>();
        tags.add(tag);
        product.setTags(tags);
        entityManager.persist(product);

        Wishlist wishlist = Wishlist.builder().user(user).build();
        wishlist.addProduct(product);
        entityManager.persist(wishlist);

        entityManager.flush();
        entityManager.clear();

        userId = user.getId();
    }

    @Test
    void getWishlistProducts_eagerlyInitializesEveryLazyFieldOnEachProduct() {
        Set<Product> products = wishlistService.getWishlistProducts(userId);

        assertEquals(1, products.size());
        Product product = products.iterator().next();

        assertTrue(Hibernate.isInitialized(product.getCategory()),
                "category must be eagerly initialized — an uninitialized proxy here "
                        + "throws LazyInitializationException once Jackson serializes it "
                        + "outside the transaction under open-in-view=false");
        assertTrue(Hibernate.isInitialized(product.getInventory()),
                "inventory must be eagerly initialized for the same reason");
        assertTrue(Hibernate.isInitialized(product.getVariants()),
                "variants must be eagerly initialized for the same reason");
        assertTrue(Hibernate.isInitialized(product.getTags()),
                "tags must be eagerly initialized for the same reason");
    }
}

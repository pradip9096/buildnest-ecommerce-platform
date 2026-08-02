package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.SellerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Regression test for #651: {@code ProductServiceImpl#getProductById}'s
 * {@code @Cacheable} entry corrupted the Redis cache entry — the write
 * succeeded, but every subsequent cache-hit read threw
 * {@code SerializationException} because {@code category}/{@code inventory}
 * (lazy {@code @ManyToOne}/{@code @OneToOne} associations) were still
 * Hibernate proxy instances when handed to
 * {@link GenericJackson2JsonRedisSerializer}'s default-typing serializer —
 * the same cache round-trip asymmetry documented in this repo's own
 * {@code raw-entity-with-lazy-collection-...} wiki lesson (the #441
 * "cache round-trip asymmetry" occurrence), but for scalar references
 * instead of collections.
 *
 * <p>Mirrors {@code CacheConfig}'s own serializer configuration (default
 * typing via {@link BasicPolymorphicTypeValidator}) directly rather than
 * requiring a live Redis connection. Uses {@code @DataJpaTest} +
 * {@link TestEntityManager}, flushing and clearing the persistence context
 * before calling the service so {@code category}/{@code inventory} are real,
 * uninitialized Hibernate proxies at fetch time — the same failure
 * precondition a real second HTTP request would hit, distinct from a
 * same-session test that would mask it.
 *
 * <p><b>Scope limit</b> (per this repo's own {@code testing-cacheable-
 * proxy-behavior-...} wiki lesson, Rule 2): this test exercises the
 * serializer directly rather than the real {@code @Cacheable} Spring proxy
 * — there is no Testcontainers Redis in this repo (mirrors {@code
 * spring/elasticsearch.md}'s identical no-live-cluster rationale). It
 * proves the entity returned by {@code getProductById} survives the exact
 * serialize/deserialize round trip the Redis cache serializer performs; it
 * does not prove the {@code @Cacheable} annotation itself is correctly
 * wired through the proxy — {@link ProductServiceCachingIntegrationTest}
 * already covers that, separately, via {@code spring.cache.type=simple}.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductServiceRedisCacheRoundTripIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ProductServiceImpl productService;
    private Long productId;

    @BeforeEach
    void setUp() {
        DomainEventPublisher domainEventPublisher = new DomainEventPublisher(
                mock(ApplicationEventPublisher.class));
        productService = new ProductServiceImpl(productRepository,
                categoryRepository, mock(SellerRepository.class),
                domainEventPublisher);

        Category category = new Category();
        category.setName("Redis Round-Trip Category 651");
        category.setDescription("Test");
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Redis Round-Trip Product 651");
        product.setDescription("Test");
        product.setPrice(BigDecimal.valueOf(120.00));
        product.setCategory(category);
        product.setIsActive(true);
        entityManager.persist(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(42);
        inventory.setMinimumStockLevel(5);
        inventory.setUseCategoryThreshold(true);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        entityManager.persist(inventory);

        entityManager.flush();
        entityManager.clear();

        productId = product.getId();
    }

    @Test
    void getProductByIdSurvivesARedisSerializeDeserializeRoundTrip() {
        Product product = productService.getProductById(productId);

        var validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        ObjectMapper redisMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(validator,
                        ObjectMapper.DefaultTyping.NON_FINAL);
        RedisSerializer<Object> serializer =
                new GenericJackson2JsonRedisSerializer(redisMapper);

        byte[] written = serializer.serialize(product);
        assertNotNull(written, "cache write must succeed");

        // The bug only manifests on the read of an already-cached entry —
        // simulate that second, separate request here.
        Object readBack = serializer.deserialize(written);

        assertNotNull(readBack, "cache read must not silently drop the value");
        Product roundTripped = (Product) readBack;
        assertEquals("Redis Round-Trip Product 651",
                roundTripped.getName());
        assertEquals("Redis Round-Trip Category 651",
                roundTripped.getCategory().getName());
        assertEquals(42, roundTripped.getStockQuantity());
    }
}

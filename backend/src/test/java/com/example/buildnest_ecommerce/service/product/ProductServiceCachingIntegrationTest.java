package com.example.buildnest_ecommerce.service.product;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link ProductServiceImpl#getProductById} is actually cached
 * through Spring's real AOP proxy — a property Mockito-mocked unit tests
 * ({@link ProductServiceImplTest}) structurally cannot observe, since
 * mocking a dependency never exercises the real {@code @Cacheable} proxy.
 *
 * <p>This is a regression guard for the class of bug found in PR #370 (#84,
 * SonarCloud {@code java:S6809}): a same-class ({@code this}-scoped) call to
 * an {@code @Cacheable} sibling method silently bypasses Spring's proxy, so
 * the annotation does nothing for that call path. A test that only mocks
 * {@link ProductRepository} and asserts on argument values (as the existing
 * unit test suite does) would pass identically whether or not caching was
 * ever actually functioning — only a real Spring context, with the bean's
 * real proxy in place, can catch that class of defect.
 *
 * <p>The default {@code test} profile disables caching entirely
 * ({@code spring.cache.type=none}, see {@code application-test.properties})
 * for fast, deterministic unit/slice tests elsewhere — this class overrides
 * that to {@code simple} (Spring Boot's in-memory {@code ConcurrentMapCacheManager})
 * so caching is genuinely active here, without requiring a live Redis
 * connection (this repo's Redis-backed {@code cacheManager} bean in
 * {@code CacheConfig} is itself conditional on {@code spring.cache.type=redis}).
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cache.type=simple")
@Import(TestSecurityConfig.class)
class ProductServiceCachingIntegrationTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    void getProductByIdIsCachedThroughTheRealSpringProxy() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("OPC 53 Grade Cement");
        when(productRepository.findById(eq(productId))).thenReturn(Optional.of(product));

        Product first = productService.getProductById(productId);
        Product second = productService.getProductById(productId);

        assertEquals("OPC 53 Grade Cement", first.getName());
        assertEquals("OPC 53 Grade Cement", second.getName());
        // The repository must be hit exactly once — the second call has to be
        // served from the @Cacheable cache via the real proxy, not the mock
        // directly. If this fails with times(2), Spring's proxy is not
        // intercepting the call (e.g. a self-invocation path was reintroduced,
        // or the cache configuration regressed).
        verify(productRepository, times(1)).findById(eq(productId));
    }
}

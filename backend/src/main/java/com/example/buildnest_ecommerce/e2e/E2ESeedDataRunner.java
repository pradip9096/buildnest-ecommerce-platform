package com.example.buildnest_ecommerce.e2e;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds one active, in-stock product on startup — gated behind
 * {@code e2e.seed.enabled}, set only by the playwright-e2e CI job
 * (.github/workflows/ci-cd-pipeline.yml, #117). Lives in main source
 * (not test), because spring-boot-maven-plugin's {@code useTestClasspath}
 * reliably adds test-scope dependency jars to the run classpath but does
 * not reliably make classes compiled to target/test-classes discoverable
 * by component scan under the {@code run} goal — a known upstream
 * limitation (spring-projects/spring-boot#36115), confirmed empirically
 * here across 3 CI runs that produced zero log output and zero seeded
 * data with no exception anywhere. The {@code @ConditionalOnProperty}
 * default (false) keeps this fully inert everywhere else — no other
 * Spring profile or deployment ever sets {@code e2e.seed.enabled=true}.
 * Mirrors {@code BaseApiTest#seedProduct} (repository-based, not raw
 * SQL, to avoid guessing Hibernate's generated column names).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "e2e.seed.enabled", havingValue = "true", matchIfMissing = false)
public class E2ESeedDataRunner implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("E2ESeedDataRunner: starting seed (e2e.seed.enabled=true)");
        Category category = categoryRepository.findByName("E2E Test Category")
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("E2E Test Category");
                    cat.setDescription(
                            "Seeded for Playwright E2E tests (#117)");
                    return categoryRepository.save(cat);
                });

        Product product = new Product();
        product.setName("Playwright E2E Test Product");
        product.setDescription("Seeded for Playwright E2E tests (#117)");
        product.setPrice(new BigDecimal("999.00"));
        product.setSku("PW-E2E-SEED-001");
        product.setCategory(category);
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(saved);
        inventory.setQuantityInStock(100);
        inventory.setMinimumStockLevel(5);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        log.info("E2ESeedDataRunner: seeded product id={} sku={}",
                saved.getId(), saved.getSku());
    }
}

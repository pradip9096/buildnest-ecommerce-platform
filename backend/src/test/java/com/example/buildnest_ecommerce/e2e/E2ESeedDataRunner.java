package com.example.buildnest_ecommerce.e2e;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds one active, in-stock product on startup — gated behind {@code e2e.seed.enabled}, set
 * only by the playwright-e2e CI job (.github/workflows/ci-cd-pipeline.yml, #117). This lives in
 * the test source tree (picked up via `spring-boot:run -Dspring-boot.run.useTestClasspath=true`,
 * same mechanism that already puts the H2 driver on the run classpath) rather than main, so
 * production code carries zero awareness of this test-only concern. Mirrors BaseApiTest#seedProduct
 * (repository-based, not raw SQL, to avoid guessing Hibernate's generated column names).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "e2e.seed.enabled", havingValue = "true", matchIfMissing = false)
public class E2ESeedDataRunner implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Category category = categoryRepository.findByName("E2E Test Category")
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("E2E Test Category");
                    cat.setDescription("Seeded for Playwright E2E tests (#117)");
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
    }
}

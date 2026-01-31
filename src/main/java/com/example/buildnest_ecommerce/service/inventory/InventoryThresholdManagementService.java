package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing inventory thresholds (RQ-INV-TH-01, RQ-INV-TH-02,
 * RQ-INV-TH-03).
 * Supports dynamic threshold configuration at product and category levels with
 * Redis caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryThresholdManagementService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String THRESHOLD_PREFIX = "inventory:threshold:";
    private static final String CATEGORY_THRESHOLD_PREFIX = "category:threshold:";

    /**
     * Set minimum stock threshold for a product (RQ-INV-TH-01, RQ-INV-TH-03).
     */
    public void setProductThreshold(Long productId, Integer minimumLevel) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setMinimumStockLevel(minimumLevel);
        inventory.setUseCategoryThreshold(false);
        inventoryRepository.save(inventory);

        // Cache in Redis for fast access (RQ-INV-TH-03)
        redisTemplate.opsForValue().set(
                THRESHOLD_PREFIX + productId,
                minimumLevel,
                24, TimeUnit.HOURS);

        log.info("Threshold set for product {}: {}", productId, minimumLevel);
    }

    /**
     * Set minimum stock threshold for a category (RQ-INV-TH-02).
     */
    public void setCategoryThreshold(Long categoryId, Integer minimumLevel) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setMinimumStockThreshold(minimumLevel);
        categoryRepository.save(category);

        // Cache in Redis (RQ-INV-TH-03)
        redisTemplate.opsForValue().set(
                CATEGORY_THRESHOLD_PREFIX + categoryId,
                minimumLevel,
                24, TimeUnit.HOURS);

        log.info("Threshold set for category {}: {}", categoryId, minimumLevel);
    }

    /**
     * Get product threshold (RQ-INV-TH-01).
     */
    public Integer getProductThreshold(Long productId) {
        // Try Redis cache first
        Object cached = redisTemplate.opsForValue().get(THRESHOLD_PREFIX + productId);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        Integer threshold = inventory.getMinimumStockLevel();

        // Cache the result
        redisTemplate.opsForValue().set(
                THRESHOLD_PREFIX + productId,
                threshold,
                24, TimeUnit.HOURS);

        return threshold;
    }

    /**
     * Get category threshold (RQ-INV-TH-02).
     */
    public Integer getCategoryThreshold(Long categoryId) {
        // Try Redis cache first
        Object cached = redisTemplate.opsForValue().get(CATEGORY_THRESHOLD_PREFIX + categoryId);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Integer threshold = category.getMinimumStockThreshold() != null ? category.getMinimumStockThreshold() : 0;

        // Cache the result
        redisTemplate.opsForValue().set(
                CATEGORY_THRESHOLD_PREFIX + categoryId,
                threshold,
                24, TimeUnit.HOURS);

        return threshold;
    }

    /**
     * Enable category threshold for product (RQ-INV-TH-02).
     */
    public void useProductCategoryThreshold(Long productId, boolean useCategory) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setUseCategoryThreshold(useCategory);
        inventoryRepository.save(inventory);

        log.info("Category threshold inheritance set to {} for product {}", useCategory, productId);
    }

    /**
     * Get effective threshold for a product (considers category if enabled)
     * (RQ-INV-TH-02).
     */
    public Integer getEffectiveThreshold(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        // If category threshold is enabled and product has a category, use category
        // threshold
        if (inventory.getUseCategoryThreshold() && product.getCategory() != null) {
            return getCategoryThreshold(product.getCategory().getId());
        }

        return inventory.getMinimumStockLevel();
    }

    /**
     * Invalidate cache for dynamic updates (RQ-INV-TH-03).
     */
    public void invalidateThresholdCache(Long productId) {
        redisTemplate.delete(THRESHOLD_PREFIX + productId);
        log.debug("Cache invalidated for product {}", productId);
    }

    /**
     * Invalidate category threshold cache (RQ-INV-TH-03).
     */
    public void invalidateCategoryThresholdCache(Long categoryId) {
        redisTemplate.delete(CATEGORY_THRESHOLD_PREFIX + categoryId);
        log.debug("Cache invalidated for category {}", categoryId);
    }
}

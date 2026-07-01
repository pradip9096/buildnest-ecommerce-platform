package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryThresholdManagementService tests")
class InventoryThresholdManagementServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private InventoryThresholdManagementService thresholdService;

    @Test
    @DisplayName("Should set and get product threshold")
    void testProductThreshold() {
        Product product = new Product();
        product.setId(1L);
        Inventory inventory = new Inventory();
        inventory.setMinimumStockLevel(5);
        inventory.setUseCategoryThreshold(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        thresholdService.setProductThreshold(1L, 7);
        assertEquals(7, inventory.getMinimumStockLevel(), "minimumStockLevel must be updated to 7");
        assertFalse(inventory.getUseCategoryThreshold(), "useCategoryThreshold must be false after setProductThreshold");
        verify(inventoryRepository).save(inventory);
        verify(valueOperations).set(eq("inventory:threshold:1"), eq(7), eq(24L), any());

        when(valueOperations.get("inventory:threshold:1")).thenReturn(6);
        assertEquals(6, thresholdService.getProductThreshold(1L));
    }

    @Test
    @DisplayName("Should set and get category threshold")
    void testCategoryThreshold() {
        Category category = new Category();
        category.setId(2L);
        category.setMinimumStockThreshold(0); // start at 0 to verify the update is applied

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        thresholdService.setCategoryThreshold(2L, 4);
        assertEquals(4, category.getMinimumStockThreshold(), "minimumStockThreshold must be updated to 4");
        verify(categoryRepository).save(category);

        when(valueOperations.get("category:threshold:2")).thenReturn(null);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        assertEquals(4, thresholdService.getCategoryThreshold(2L));
    }

    @Test
    @DisplayName("Should return cached category threshold without repository access")
    void testCategoryThresholdCacheHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("category:threshold:7")).thenReturn("9");

        assertEquals(9, thresholdService.getCategoryThreshold(7L));
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should return effective threshold using category")
    void testEffectiveThreshold() {
        Product product = new Product();
        product.setId(1L);
        Category category = new Category();
        category.setId(2L);
        category.setMinimumStockThreshold(3);
        product.setCategory(category);

        Inventory inventory = new Inventory();
        inventory.setMinimumStockLevel(5);
        inventory.setUseCategoryThreshold(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("category:threshold:2")).thenReturn(null);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        assertEquals(3, thresholdService.getEffectiveThreshold(1L));
    }

    @Test
    @DisplayName("Should return cached product threshold without repository access")
    void testProductThresholdCacheHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("inventory:threshold:9")).thenReturn("11");

        assertEquals(11, thresholdService.getProductThreshold(9L));
        verify(productRepository, never()).findById(any());
        verify(inventoryRepository, never()).findByProduct(any());
    }

    @Test
    @DisplayName("Should update inventory useCategoryThreshold flag")
    void testUseProductCategoryThreshold() {
        Product product = new Product();
        product.setId(1L);

        Inventory inventory = new Inventory();
        inventory.setUseCategoryThreshold(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        thresholdService.useProductCategoryThreshold(1L, true);

        assertTrue(inventory.getUseCategoryThreshold());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("Should return effective threshold when category disabled")
    void testEffectiveThresholdWithoutCategoryUsage() {
        Product product = new Product();
        product.setId(1L);
        Category category = new Category();
        category.setId(2L);
        product.setCategory(category);

        Inventory inventory = new Inventory();
        inventory.setMinimumStockLevel(7);
        inventory.setUseCategoryThreshold(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertEquals(7, thresholdService.getEffectiveThreshold(1L));
    }

    @Test
    @DisplayName("Should invalidate caches")
    void testInvalidateCaches() {
        thresholdService.invalidateThresholdCache(1L);
        thresholdService.invalidateCategoryThresholdCache(2L);

        verify(redisTemplate).delete("inventory:threshold:1");
        verify(redisTemplate).delete("category:threshold:2");
    }

    // ── setProductThreshold — not-found paths ────────────────────────────────

    @Test
    @DisplayName("setProductThreshold — product not found → throws RuntimeException")
    void setProductThreshold_productNotFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.setProductThreshold(99L, 5));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("setProductThreshold — inventory not found → throws RuntimeException")
    void setProductThreshold_inventoryNotFound_throws() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.setProductThreshold(1L, 5));
        verify(inventoryRepository, never()).save(any());
    }

    // ── setCategoryThreshold — not-found path ────────────────────────────────

    @Test
    @DisplayName("setCategoryThreshold — category not found → throws RuntimeException")
    void setCategoryThreshold_categoryNotFound_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.setCategoryThreshold(99L, 10));
        verify(categoryRepository, never()).save(any());
    }

    // ── getProductThreshold — not-found paths on cache miss ──────────────────

    @Test
    @DisplayName("getProductThreshold — cache miss, reads from DB and re-populates Redis cache")
    void getProductThreshold_cacheMiss_readsFromDbAndCaches() {
        Product product = new Product();
        product.setId(5L);
        Inventory inventory = new Inventory();
        inventory.setMinimumStockLevel(12);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("inventory:threshold:5")).thenReturn(null);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        Integer result = thresholdService.getProductThreshold(5L);

        assertEquals(12, result, "must return threshold from inventory");
        verify(valueOperations).set(eq("inventory:threshold:5"), eq(12), eq(24L), any());
    }

    @Test
    @DisplayName("getProductThreshold — cache miss, product not found → throws RuntimeException")
    void getProductThreshold_cacheMiss_productNotFound_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("inventory:threshold:99")).thenReturn(null);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.getProductThreshold(99L));
    }

    @Test
    @DisplayName("getProductThreshold — cache miss, inventory not found → throws RuntimeException")
    void getProductThreshold_cacheMiss_inventoryNotFound_throws() {
        Product product = new Product();
        product.setId(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("inventory:threshold:1")).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.getProductThreshold(1L));
    }

    // ── getCategoryThreshold — not-found and null-threshold paths ────────────

    @Test
    @DisplayName("getCategoryThreshold — cache miss, category not found → throws RuntimeException")
    void getCategoryThreshold_cacheMiss_categoryNotFound_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("category:threshold:99")).thenReturn(null);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.getCategoryThreshold(99L));
    }

    @Test
    @DisplayName("getCategoryThreshold — cache miss, category has null threshold → returns 0")
    void getCategoryThreshold_cacheMiss_nullThreshold_returnsZero() {
        Category category = new Category();
        category.setId(5L);
        category.setMinimumStockThreshold(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("category:threshold:5")).thenReturn(null);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        assertEquals(0, thresholdService.getCategoryThreshold(5L));
    }

    // ── useProductCategoryThreshold — not-found paths ────────────────────────

    @Test
    @DisplayName("useProductCategoryThreshold — product not found → throws RuntimeException")
    void useProductCategoryThreshold_productNotFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> thresholdService.useProductCategoryThreshold(99L, true));
    }

    @Test
    @DisplayName("useProductCategoryThreshold — inventory not found → throws RuntimeException")
    void useProductCategoryThreshold_inventoryNotFound_throws() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> thresholdService.useProductCategoryThreshold(1L, true));
    }

    // ── getEffectiveThreshold — not-found and null-category paths ────────────

    @Test
    @DisplayName("getEffectiveThreshold — product not found → throws RuntimeException")
    void getEffectiveThreshold_productNotFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.getEffectiveThreshold(99L));
    }

    @Test
    @DisplayName("getEffectiveThreshold — inventory not found → throws RuntimeException")
    void getEffectiveThreshold_inventoryNotFound_throws() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> thresholdService.getEffectiveThreshold(1L));
    }

    @Test
    @DisplayName("getEffectiveThreshold — useCategoryThreshold=true but product has no category → returns product threshold")
    void getEffectiveThreshold_useCategory_noCategory_returnsProductThreshold() {
        Product product = new Product();
        product.setId(1L);
        product.setCategory(null);

        Inventory inventory = new Inventory();
        inventory.setMinimumStockLevel(8);
        inventory.setUseCategoryThreshold(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertEquals(8, thresholdService.getEffectiveThreshold(1L));
    }
}

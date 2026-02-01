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
@SuppressWarnings("unchecked")
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
        category.setMinimumStockThreshold(4);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        thresholdService.setCategoryThreshold(2L, 4);
        verify(categoryRepository).save(category);

        when(valueOperations.get("category:threshold:2")).thenReturn(null);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        assertEquals(4, thresholdService.getCategoryThreshold(2L));
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
    @DisplayName("Should invalidate caches")
    void testInvalidateCaches() {
        thresholdService.invalidateThresholdCache(1L);
        thresholdService.invalidateCategoryThresholdCache(2L);

        verify(redisTemplate).delete("inventory:threshold:1");
        verify(redisTemplate).delete("category:threshold:2");
    }
}

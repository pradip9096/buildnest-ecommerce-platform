package com.example.buildnest_ecommerce.e2e;

import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ShippingMethod;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ShippingMethodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("E2ESeedDataRunner tests")
class E2ESeedDataRunnerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ShippingMethodRepository shippingMethodRepository;

    @InjectMocks
    private E2ESeedDataRunner runner;

    @Test
    @DisplayName("creates a new category, product, and inventory when none exist")
    void run_noExistingCategory_createsAll() {
        when(categoryRepository.findByName("E2E Test Category"))
                .thenReturn(Optional.empty());
        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("E2E Test Category");
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        Product savedProduct = new Product();
        savedProduct.setId(2L);
        savedProduct.setSku("PW-E2E-SEED-001");
        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        when(shippingMethodRepository.findAllByIsActiveTrue())
                .thenReturn(Collections.emptyList());
        ShippingMethod savedMethod = new ShippingMethod();
        savedMethod.setId(4L);
        when(shippingMethodRepository.save(any(ShippingMethod.class)))
                .thenReturn(savedMethod);

        runner.run(null);

        ArgumentCaptor<Product> productCaptor =
                ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals("PW-E2E-SEED-001", productCaptor.getValue().getSku());
        assertEquals(savedCategory, productCaptor.getValue().getCategory());

        ArgumentCaptor<Inventory> inventoryCaptor =
                ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        assertEquals(savedProduct, inventoryCaptor.getValue().getProduct());
        assertEquals(InventoryStatus.IN_STOCK,
                inventoryCaptor.getValue().getStatus());
        assertEquals(100, inventoryCaptor.getValue().getQuantityInStock());

        ArgumentCaptor<ShippingMethod> shippingCaptor =
                ArgumentCaptor.forClass(ShippingMethod.class);
        verify(shippingMethodRepository).save(shippingCaptor.capture());
        assertEquals("Standard Delivery", shippingCaptor.getValue().getName());
        assertEquals(new BigDecimal("50.00"),
                shippingCaptor.getValue().getBaseCost());
        assertEquals(Boolean.TRUE, shippingCaptor.getValue().getIsActive());
    }

    @Test
    @DisplayName("reuses an already-seeded category instead of creating a duplicate")
    void run_existingCategory_reusesIt() {
        Category existingCategory = new Category();
        existingCategory.setId(9L);
        existingCategory.setName("E2E Test Category");
        when(categoryRepository.findByName("E2E Test Category"))
                .thenReturn(Optional.of(existingCategory));

        Product savedProduct = new Product();
        savedProduct.setId(3L);
        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ShippingMethod existingMethod = new ShippingMethod();
        existingMethod.setId(5L);
        when(shippingMethodRepository.findAllByIsActiveTrue())
                .thenReturn(List.of(existingMethod));

        runner.run(null);

        verify(categoryRepository, never()).save(any(Category.class));

        ArgumentCaptor<Product> productCaptor =
                ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(existingCategory, productCaptor.getValue().getCategory());

        verify(shippingMethodRepository, never())
                .save(any(ShippingMethod.class));
    }
}

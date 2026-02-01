package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.LowStockWarningEvent;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl tests")
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
    }

    private Inventory buildInventory(Product product, int quantity, int minLevel) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(quantity);
        inventory.setQuantityReserved(0);
        inventory.setMinimumStockLevel(minLevel);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        return inventory;
    }

    @Test
    @DisplayName("Should add stock and update status")
    void testAddStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.addStock(1L, 3);
        assertEquals(8, updated.getQuantityInStock());
    }

    @Test
    @DisplayName("Should throw exception when adding stock to non-existent product")
    void testAddStockProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.addStock(1L, 5));
    }

    @Test
    @DisplayName("Should update stock")
    void testUpdateStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.updateStock(1L, 1);
        assertEquals(1, updated.getQuantityInStock());
        assertEquals(InventoryStatus.LOW_STOCK, updated.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when updating stock for non-existent product")
    void testUpdateStockProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.updateStock(1L, 10));
    }

    @Test
    @DisplayName("Should throw exception when updating stock without inventory")
    void testUpdateStockInventoryNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.updateStock(1L, 10));
    }

    @Test
    @DisplayName("Should update stock to zero and trigger OUT_OF_STOCK status")
    void testUpdateStockToZero() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.updateStock(1L, 0);

        assertEquals(0, updated.getQuantityInStock());
        assertEquals(InventoryStatus.OUT_OF_STOCK, updated.getStatus());
        verify(domainEventPublisher).publish(any(LowStockWarningEvent.class));
    }

    @Test
    @DisplayName("Should deduct stock and reserve")
    void testDeductStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        inventoryService.deductStock(1L, 2);
        assertEquals(3, inventory.getQuantityInStock());
        assertEquals(2, inventory.getQuantityReserved());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("Should throw exception when deducting more stock than available")
    void testDeductStockInsufficient() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertThrows(RuntimeException.class, () -> inventoryService.deductStock(1L, 10));
    }

    @Test
    @DisplayName("Should throw exception when deducting stock from non-existent product")
    void testDeductStockProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.deductStock(1L, 1));
    }

    @Test
    @DisplayName("Should throw exception when deducting stock without inventory")
    void testDeductStockInventoryNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.deductStock(1L, 1));
    }

    @Test
    @DisplayName("Should check stock availability")
    void testHasStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertTrue(inventoryService.hasStock(1L, 2));
        assertFalse(inventoryService.hasStock(1L, 10));
    }

    @Test
    @DisplayName("Should handle hasStock for product without inventory")
    void testHasStockNoInventory() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Create inventory with quantity initialized to 0 to simulate new inventory
        Inventory emptyInventory = new Inventory();
        emptyInventory.setQuantityInStock(0);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(emptyInventory));

        assertFalse(inventoryService.hasStock(1L, 1));
    }

    @Test
    @DisplayName("Should check exact stock match")
    void testHasStockExactMatch() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertTrue(inventoryService.hasStock(1L, 5));
    }

    @Test
    @DisplayName("Should throw exception when checking stock for non-existent product")
    void testHasStockProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.hasStock(1L, 1));
    }

    @Test
    @DisplayName("Should return inventory status and threshold checks")
    void testStatusAndThreshold() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 1, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertEquals(InventoryStatus.IN_STOCK, inventoryService.getInventoryStatus(1L));
        assertTrue(inventoryService.isBelowThreshold(1L));
    }

    @Test
    @DisplayName("Should get inventory by product id")
    void testGetInventoryByProductId() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryByProductId(1L);

        assertNotNull(result);
        assertEquals(10, result.getQuantityInStock());
    }

    @Test
    @DisplayName("Should throw exception when getting inventory for non-existent product")
    void testGetInventoryByProductIdNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.getInventoryByProductId(1L));
    }

    @Test
    @DisplayName("Should throw exception when inventory not found")
    void testGetInventoryByProductIdInventoryNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.getInventoryByProductId(1L));
    }

    @Test
    @DisplayName("Should get low stock products")
    void testGetLowStockProducts() {
        List<Inventory> lowStockList = List.of(buildInventory(product, 1, 5));
        when(inventoryRepository.findLowStockProducts()).thenReturn(lowStockList);

        List<Inventory> result = inventoryService.getLowStockProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryRepository).findLowStockProducts();
    }

    @Test
    @DisplayName("Should get out of stock products")
    void testGetOutOfStockProducts() {
        Inventory outOfStock = buildInventory(product, 0, 2);
        outOfStock.setStatus(InventoryStatus.OUT_OF_STOCK);
        List<Inventory> outOfStockList = List.of(outOfStock);
        when(inventoryRepository.findOutOfStockProducts()).thenReturn(outOfStockList);

        List<Inventory> result = inventoryService.getOutOfStockProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryRepository).findOutOfStockProducts();
    }

    @Test
    @DisplayName("Should get products below threshold")
    void testGetProductsBelowThreshold() {
        List<Inventory> belowThreshold = List.of(buildInventory(product, 2, 5));
        when(inventoryRepository.findBelowThresholdProducts()).thenReturn(belowThreshold);

        List<Inventory> result = inventoryService.getProductsBelowThreshold();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryRepository).findBelowThresholdProducts();
    }

    @Test
    @DisplayName("Should check if product is below threshold when quantity equals minimum")
    void testIsBelowThresholdAtMinimum() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 5);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertTrue(inventoryService.isBelowThreshold(1L));
    }

    @Test
    @DisplayName("Should check if product is not below threshold")
    void testIsNotBelowThreshold() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 5);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertFalse(inventoryService.isBelowThreshold(1L));
    }

    @Test
    @DisplayName("Should trigger LOW_STOCK status and publish event when stock drops below threshold")
    void testLowStockStatusChangePublishesEvent() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 5);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.updateStock(1L, 3);

        assertEquals(InventoryStatus.LOW_STOCK, updated.getStatus());
        verify(domainEventPublisher).publish(any(LowStockWarningEvent.class));
    }

    @Test
    @DisplayName("Should not publish event when status remains LOW_STOCK")
    void testLowStockStatusUnchangedNoEvent() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 3, 5);
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.updateStock(1L, 2);

        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should not publish event when inventory has no product")
    void testNoEventWhenProductIsNull() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(null, 10, 5);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.updateStock(1L, 1);

        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should transition from LOW_STOCK to IN_STOCK without event")
    void testLowStockToInStockNoEvent() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 3, 5);
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.updateStock(1L, 10);

        assertEquals(InventoryStatus.IN_STOCK, updated.getStatus());
        verify(domainEventPublisher, never()).publish(any());
    }
}

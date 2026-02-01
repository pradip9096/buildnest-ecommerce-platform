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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl mutation-killing tests")
class InventoryServiceImplEnhancedTest {

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

    private Inventory buildInventory(Product prod, int quantity, int minLevel) {
        Inventory inventory = new Inventory();
        inventory.setProduct(prod);
        inventory.setQuantityInStock(quantity);
        inventory.setQuantityReserved(0);
        inventory.setMinimumStockLevel(minLevel);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        return inventory;
    }

    @Test
    @DisplayName("Should verify addStock uses addition operator correctly")
    void testAddStockIncrementsAddition() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.addStock(1L, 5);

        assertEquals(15, result.getQuantityInStock());
    }

    @Test
    @DisplayName("Should verify deductStock boundary conditions exactly")
    void testDeductStockBoundaryExact() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        inventoryService.deductStock(1L, 5);

        assertEquals(0, inventory.getQuantityInStock());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("Should verify hasStock >= boundary condition")
    void testHasStockBoundary() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertTrue(inventoryService.hasStock(1L, 5));
        assertTrue(inventoryService.hasStock(1L, 4));
        assertFalse(inventoryService.hasStock(1L, 6));
    }

    @Test
    @DisplayName("Should verify isBelowThreshold <= boundary")
    void testIsBelowThresholdBoundary() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 5);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertTrue(inventoryService.isBelowThreshold(1L));

        inventory.setQuantityInStock(6);
        assertFalse(inventoryService.isBelowThreshold(1L));
    }

    @Test
    @DisplayName("Should verify deductStock increments reserved quantity")
    void testDeductStockReserveIncrement() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 2);
        inventory.setQuantityReserved(0);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        inventoryService.deductStock(1L, 3);

        assertEquals(3, inventory.getQuantityReserved());
        assertEquals(7, inventory.getQuantityInStock());
    }

    @Test
    @DisplayName("Should verify deductStock insufficient check uses < operator")
    void testDeductStockInsufficientBoundary() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        assertThrows(RuntimeException.class, () -> inventoryService.deductStock(1L, 6));
        inventoryService.deductStock(1L, 5);
    }

    @Test
    @DisplayName("Should verify updateStatusBasedOnQuantity zero check == 0")
    void testUpdateStatusZeroQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 1, 5);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.updateStock(1L, 0);

        assertEquals(InventoryStatus.OUT_OF_STOCK, result.getStatus());
    }

    @Test
    @DisplayName("Should verify status boundary between IN_STOCK and LOW_STOCK")
    void testStatusTransitionBoundary() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 5);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result1 = inventoryService.updateStock(1L, 6);
        assertEquals(InventoryStatus.IN_STOCK, result1.getStatus());

        Inventory result2 = inventoryService.updateStock(1L, 5);
        assertEquals(InventoryStatus.LOW_STOCK, result2.getStatus());
    }

    @Test
    @DisplayName("Should verify previousStatus != condition for event publishing")
    void testPreviousStatusTracking() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 5);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.updateStock(1L, 4);
        assertEquals(InventoryStatus.LOW_STOCK, updated.getStatus());
        verify(domainEventPublisher).publish(any(LowStockWarningEvent.class));
    }

    @Test
    @DisplayName("Should verify status condition prevents duplicate LOW_STOCK events")
    void testNoEventWhenStatusUnchanged() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 3, 5);
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.updateStock(1L, 2);
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should verify event triggers for OUT_OF_STOCK transition")
    void testOutOfStockEventPublish() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.updateStock(1L, 0);

        assertEquals(InventoryStatus.OUT_OF_STOCK, updated.getStatus());
        verify(domainEventPublisher).publish(any(LowStockWarningEvent.class));
    }

    @Test
    @DisplayName("Should verify updatedAt timestamp is set")
    void testUpdatedAtTimestamp() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 5, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeUpdate = LocalDateTime.now();
        Inventory updated = inventoryService.addStock(1L, 3);
        LocalDateTime afterUpdate = LocalDateTime.now();

        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(updated.getUpdatedAt().isBefore(afterUpdate.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should verify product null check in event publishing")
    void testEventNotPublishedWhenProductNull() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = new Inventory();
        inventory.setProduct(null);
        inventory.setQuantityInStock(10);
        inventory.setQuantityReserved(0);
        inventory.setMinimumStockLevel(5);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.updateStock(1L, 3);

        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should verify event only when status changed")
    void testEventOnlyWhenStatusChanged() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory inventory = buildInventory(product, 10, 5);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.updateStock(1L, 4);
        verify(domainEventPublisher).publish(any(LowStockWarningEvent.class));

        reset(domainEventPublisher);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.updateStock(1L, 8);
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should verify deductStock saves inventory")
    void testDeductStockSaves() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Inventory inventory = buildInventory(product, 10, 2);
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        inventoryService.deductStock(1L, 3);

        verify(inventoryRepository, times(1)).save(inventory);
    }
}

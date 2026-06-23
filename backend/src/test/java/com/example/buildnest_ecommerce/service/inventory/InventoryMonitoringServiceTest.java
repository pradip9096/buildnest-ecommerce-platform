package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryThresholdBreachEventRepository;
import com.example.buildnest_ecommerce.service.notification.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryMonitoringService tests")
class InventoryMonitoringServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryThresholdBreachEventRepository breachEventRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InventoryThresholdManagementService thresholdService;

    @InjectMocks
    private InventoryMonitoringService monitoringService;

    private Inventory buildInventory(int quantity, InventoryStatus status) {
        Product product = new Product();
        product.setId(1L);
        product.setName("Product");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(quantity);
        inventory.setMinimumStockLevel(5);
        inventory.setStatus(status);
        return inventory;
    }

    @Test
    @DisplayName("Should generate alert on low stock")
    void testCheckAndGenerateAlertLowStock() {
        Inventory inventory = buildInventory(2, InventoryStatus.IN_STOCK);
        when(thresholdService.getEffectiveThreshold(1L)).thenReturn(5);

        boolean alerted = monitoringService.checkAndGenerateAlert(inventory);

        assertTrue(alerted);
        verify(breachEventRepository).save(any());
        verify(notificationService).sendAlert(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should generate back-in-stock alert")
    void testCheckAndGenerateAlertBackInStock() {
        Inventory inventory = buildInventory(10, InventoryStatus.LOW_STOCK);
        when(thresholdService.getEffectiveThreshold(1L)).thenReturn(5);

        boolean alerted = monitoringService.checkAndGenerateAlert(inventory);

        assertTrue(alerted);
        verify(notificationService).sendAlert(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should monitor all inventories")
    void testMonitorInventoryLevels() {
        Inventory inventory = buildInventory(2, InventoryStatus.IN_STOCK);
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory));
        when(thresholdService.getEffectiveThreshold(1L)).thenReturn(5);

        monitoringService.monitorInventoryLevels();

        verify(notificationService).sendAlert(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should generate out-of-stock alert")
    void testCheckAndGenerateAlertOutOfStock() {
        Inventory inventory = buildInventory(0, InventoryStatus.LOW_STOCK);
        when(thresholdService.getEffectiveThreshold(1L)).thenReturn(5);

        boolean alerted = monitoringService.checkAndGenerateAlert(inventory);

        assertTrue(alerted);
        verify(breachEventRepository).save(any());
        verify(notificationService).sendAlert(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should return false when no alert is needed")
    void testNoAlertWhenHealthy() {
        Inventory inventory = buildInventory(10, InventoryStatus.IN_STOCK);
        when(thresholdService.getEffectiveThreshold(1L)).thenReturn(5);

        boolean alerted = monitoringService.checkAndGenerateAlert(inventory);

        assertFalse(alerted);
    }
}

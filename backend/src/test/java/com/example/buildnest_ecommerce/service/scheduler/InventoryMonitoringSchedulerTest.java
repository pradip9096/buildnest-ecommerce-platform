package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.service.inventory.InventoryMonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryMonitoringSchedulerTest {

    @Mock
    private InventoryMonitoringService inventoryMonitoringService;

    @InjectMocks
    private InventoryMonitoringScheduler scheduler;

    @Test
    void monitorInventoryLevelsShouldInvokeService() {
        scheduler.monitorInventoryLevels();

        verify(inventoryMonitoringService).monitorInventoryLevels();
    }

    @Test
    void monitorInventoryLevelsShouldHandleExceptions() {
        doThrow(new RuntimeException("Service failure"))
                .when(inventoryMonitoringService).monitorInventoryLevels();

        assertDoesNotThrow(() -> scheduler.monitorInventoryLevels());
    }

    @Test
    void generateDailyInventorySummaryShouldNotThrow() {
        assertDoesNotThrow(() -> scheduler.generateDailyInventorySummary());
    }
}

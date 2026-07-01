package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryReservationCleanupJob")
class InventoryReservationCleanupJobImplTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryReservationCleanupJob job;

    @Test
    @DisplayName("releaseExpiredReservations — delegates to InventoryService")
    void releaseExpiredReservations_delegatesToService() {
        job.releaseExpiredReservations();

        verify(inventoryService).releaseExpiredReservations();
    }

    @Test
    @DisplayName("releaseExpiredReservations — service throws Exception, job swallows it without propagating")
    void releaseExpiredReservations_serviceThrows_doesNotPropagate() {
        doThrow(new RuntimeException("DB unavailable"))
                .when(inventoryService).releaseExpiredReservations();

        assertDoesNotThrow(() -> job.releaseExpiredReservations());
    }
}

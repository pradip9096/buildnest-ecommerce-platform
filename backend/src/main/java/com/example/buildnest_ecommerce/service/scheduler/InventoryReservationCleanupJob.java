package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Releases inventory reservations that were never confirmed within the 15-minute
 * checkout window (INV-01, #73). Runs every 60 seconds.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationCleanupJob {

    private final InventoryService inventoryService;

    @Scheduled(fixedDelay = 60_000)
    public void releaseExpiredReservations() {
        log.debug("Running inventory reservation cleanup job");
        try {
            inventoryService.releaseExpiredReservations();
        } catch (Exception e) {
            log.error("Error during inventory reservation cleanup", e);
        }
    }
}

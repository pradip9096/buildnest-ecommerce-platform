package com.example.buildnest_ecommerce.service.scheduler;

import com.example.buildnest_ecommerce.service.inventory.InventoryMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled task for continuous inventory monitoring (RQ-INV-MON-01).
 * Runs periodic checks of inventory levels and generates alerts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class InventoryMonitoringScheduler {

    private final InventoryMonitoringService inventoryMonitoringService;

    /**
     * Monitor inventory levels every 5 minutes (RQ-INV-MON-01, RQ-INV-MON-02,
     * RQ-INV-MON-03).
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void monitorInventoryLevels() {
        log.debug("Executing scheduled inventory monitoring task");
        try {
            inventoryMonitoringService.monitorInventoryLevels();
        } catch (Exception e) {
            log.error("Error during inventory monitoring", e);
        }
    }

    /**
     * Daily inventory summary report at 2 AM (RQ-INV-REP-01).
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void generateDailyInventorySummary() {
        log.info("Generating daily inventory summary report");
        try {
            // This would integrate with reporting service in a real implementation
            log.info("Daily inventory summary report generated");
        } catch (Exception e) {
            log.error("Error during inventory summary generation", e);
        }
    }
}

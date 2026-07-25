package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.InventoryDTO;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryAuditLog;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryAuditLogRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.LowStockWarningEvent;
import com.example.buildnest_ecommerce.exception.InventoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class InventoryServiceImpl implements InventoryService {

        private final InventoryRepository inventoryRepository;
        private final InventoryAuditLogRepository inventoryAuditLogRepository;
        private final ProductRepository productRepository;
        private final DomainEventPublisher domainEventPublisher;

        @Override
        @Transactional
        public Inventory addStock(Long productId, Integer stock) {
                log.info("Adding stock {} for product {}", stock, productId);

                if (stock == null || stock < 0) {
                        throw new IllegalArgumentException(
                                "Stock to add must not be negative: " + stock);
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElse(new Inventory());

                int resultingQuantity = inventory.getQuantityInStock() + stock;
                if (resultingQuantity < 0) {
                        throw new IllegalArgumentException(
                                "Resulting stock would be negative. Current: "
                                        + inventory.getQuantityInStock()
                                        + ", adding: " + stock);
                }

                inventory.setProduct(product);
                inventory.setQuantityInStock(resultingQuantity);
                inventory.setUpdatedAt(LocalDateTime.now());
                updateStatusBasedOnQuantity(inventory);

                return inventoryRepository.save(inventory);
        }

        @Override
        public Inventory getInventoryByProductId(Long productId) {
                log.info("Fetching inventory for product: {}", productId);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

                return inventoryRepository.findByProduct(product)
                                .orElseThrow(() -> new RuntimeException(
                                                "Inventory not found for product: " + productId));
        }

        @Override
        @Transactional
        public Inventory updateStock(Long productId, Integer quantity) {
                log.info("Updating stock for product {} to {}", productId, quantity);

                if (quantity == null || quantity < 0) {
                        throw new IllegalArgumentException(
                                "Stock quantity must not be negative: " + quantity);
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElseThrow(() -> new RuntimeException("Inventory not found"));

                inventory.setQuantityInStock(quantity);
                inventory.setUpdatedAt(LocalDateTime.now());
                updateStatusBasedOnQuantity(inventory);

                return inventoryRepository.save(inventory);
        }

        @Override
        @Transactional
        public void deductStock(Long productId, Integer quantity) {
                log.info("Permanently deducting {} units from product {} (finalising reservation)", quantity, productId);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElseThrow(() -> new RuntimeException("Inventory not found"));

                if (inventory.getQuantityInStock() < quantity) {
                        throw new RuntimeException("Insufficient stock available");
                }

                // Finalise: decrement physical stock and clear the reservation hold
                inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
                int newReserved = Math.max(0, inventory.getQuantityReserved() - quantity);
                inventory.setQuantityReserved(newReserved);
                if (newReserved == 0) {
                        inventory.setReservationExpiresAt(null);
                }
                inventory.setUpdatedAt(LocalDateTime.now());
                updateStatusBasedOnQuantity(inventory);

                inventoryRepository.save(inventory);
        }

        @Override
        public boolean hasStock(Long productId, Integer quantity) {
                log.debug("Checking available stock for product {}: requested={}", productId, quantity);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElse(null);

                if (inventory == null) {
                        return false;
                }

                return inventory.getAvailableQuantity() >= quantity;
        }

        @Override
        @Transactional
        public void reserveStock(Long productId, Integer quantity, LocalDateTime expiresAt) {
                log.info("Reserving {} units of product {} until {}", quantity, productId, expiresAt);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

                if (inventory.getAvailableQuantity() < quantity) {
                        throw new InventoryException(
                                "Insufficient available stock for product " + productId
                                + ". Available: " + inventory.getAvailableQuantity()
                                + ", requested: " + quantity);
                }

                inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
                inventory.setReservationExpiresAt(expiresAt);
                inventory.setUpdatedAt(LocalDateTime.now());

                try {
                        inventoryRepository.save(inventory);
                } catch (OptimisticLockingFailureException e) {
                        throw new InventoryException(
                                "Product " + productId + " was modified by a concurrent request. Please retry.");
                }
        }

        @Override
        @Transactional
        public void releaseReservation(Long productId, Integer quantity) {
                log.info("Releasing reservation of {} units for product {}", quantity, productId);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElse(null);

                if (inventory == null || inventory.getQuantityReserved() == 0) {
                        log.warn("No active reservation to release for product {}", productId);
                        return;
                }

                int newReserved = Math.max(0, inventory.getQuantityReserved() - quantity);
                inventory.setQuantityReserved(newReserved);
                if (newReserved == 0) {
                        inventory.setReservationExpiresAt(null);
                }
                inventory.setUpdatedAt(LocalDateTime.now());
                inventoryRepository.save(inventory);
        }

        @Override
        @Transactional
        public void releaseExpiredReservations() {
                List<Inventory> expired = inventoryRepository.findExpiredReservations(LocalDateTime.now());
                if (expired.isEmpty()) {
                        return;
                }
                log.info("Releasing expired reservations for {} inventory records", expired.size());
                for (Inventory inventory : expired) {
                        log.info("Releasing expired reservation: productId={}, reserved={}, expiredAt={}",
                                inventory.getProduct().getId(),
                                inventory.getQuantityReserved(),
                                inventory.getReservationExpiresAt());
                        inventory.setQuantityReserved(0);
                        inventory.setReservationExpiresAt(null);
                        inventory.setUpdatedAt(LocalDateTime.now());
                        inventoryRepository.save(inventory);
                }
        }

        @Override
        public InventoryStatus getInventoryStatus(Long productId) {
                Inventory inventory = getInventoryByProductId(productId);
                return inventory.getStatus();
        }

        @Override
        public List<Inventory> getLowStockProducts() {
                return inventoryRepository.findLowStockProducts();
        }

        @Override
        public List<Inventory> getOutOfStockProducts() {
                return inventoryRepository.findOutOfStockProducts();
        }

        @Override
        public List<Inventory> getProductsBelowThreshold() {
                return inventoryRepository.findBelowThresholdProducts();
        }

        @Override
        public boolean isBelowThreshold(Long productId) {
                Inventory inventory = getInventoryByProductId(productId);
                return inventory.getQuantityInStock() <= inventory.getMinimumStockLevel();
        }

        @Override
        public List<InventoryDTO> getAllInventorySummary() {
                log.info("Fetching all inventory summaries");
                return inventoryRepository.findAll().stream()
                                .map(inv -> new InventoryDTO(
                                                inv.getId(),
                                                inv.getProduct().getId(),
                                                inv.getProduct().getName(),
                                                inv.getQuantityInStock(),
                                                inv.getQuantityReserved(),
                                                inv.getAvailableQuantity(),
                                                inv.getMinimumStockLevel(),
                                                inv.getStatus().name(),
                                                inv.getUpdatedAt()))
                                .toList();
        }

        @Override
        @Transactional
        public Inventory adjustStock(Long productId, int delta, String reason, Long changedByUserId) {
                log.info("Admin adjusting stock for product {} by delta={}", productId, delta);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found with id: " + productId));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Inventory not found for product: " + productId));

                int quantityBefore = inventory.getQuantityInStock();
                int quantityAfter = quantityBefore + delta;

                if (quantityAfter < 0) {
                        throw new IllegalArgumentException(
                                "Adjustment would result in negative stock. Current: "
                                        + quantityBefore + ", delta: " + delta);
                }

                inventory.setQuantityInStock(quantityAfter);
                inventory.setUpdatedAt(LocalDateTime.now());
                updateStatusBasedOnQuantity(inventory);
                Inventory saved = inventoryRepository.save(inventory);

                inventoryAuditLogRepository.save(InventoryAuditLog.builder()
                                .inventory(saved)
                                .product(product)
                                .changedByUserId(changedByUserId)
                                .changeType("ADJUSTMENT")
                                .quantityBefore(quantityBefore)
                                .quantityChange(delta)
                                .quantityAfter(quantityAfter)
                                .referenceType("MANUAL")
                                .notes(reason)
                                .createdAt(LocalDateTime.now())
                                .build());

                return saved;
        }

        @Override
        public Page<InventoryDTO> getInventoryForSeller(
                        Long sellerUserId, Pageable pageable) {
                log.info("Fetching inventory for seller {}", sellerUserId);
                return inventoryRepository
                                .findByProduct_Seller_Id(sellerUserId, pageable)
                                .map(inv -> new InventoryDTO(
                                                inv.getId(),
                                                inv.getProduct().getId(),
                                                inv.getProduct().getName(),
                                                inv.getQuantityInStock(),
                                                inv.getQuantityReserved(),
                                                inv.getAvailableQuantity(),
                                                inv.getMinimumStockLevel(),
                                                inv.getStatus().name(),
                                                inv.getUpdatedAt()));
        }

        @Override
        @Transactional
        public Inventory adjustStockForSeller(
                        Long sellerUserId, Long productId, int delta,
                        String reason) {
                log.info("Seller {} adjusting stock for product {} by "
                                + "delta={}", sellerUserId, productId, delta);

                Inventory inventory = inventoryRepository
                                .findByProduct_IdAndProduct_Seller_Id(
                                                productId, sellerUserId)
                                .orElseThrow(() ->
                                                new ResourceNotFoundException(
                                                "Product " + productId
                                                + " does not belong to seller "
                                                + sellerUserId));

                int quantityBefore = inventory.getQuantityInStock();
                int quantityAfter = quantityBefore + delta;

                if (quantityAfter < 0) {
                        throw new IllegalArgumentException(
                                "Adjustment would result in negative stock. "
                                        + "Current: " + quantityBefore
                                        + ", delta: " + delta);
                }

                inventory.setQuantityInStock(quantityAfter);
                inventory.setUpdatedAt(LocalDateTime.now());
                updateStatusBasedOnQuantity(inventory);
                Inventory saved = inventoryRepository.save(inventory);

                inventoryAuditLogRepository.save(InventoryAuditLog.builder()
                                .inventory(saved)
                                .product(inventory.getProduct())
                                .changedByUserId(sellerUserId)
                                .changeType("SELLER_ADJUSTMENT")
                                .quantityBefore(quantityBefore)
                                .quantityChange(delta)
                                .quantityAfter(quantityAfter)
                                .referenceType("MANUAL")
                                .notes(reason)
                                .createdAt(LocalDateTime.now())
                                .build());

                return saved;
        }

        /**
         * Update status based on quantity (RQ-INV-STAT-01, RQ-INV-STAT-02,
         * RQ-INV-STAT-03).
         */
        private void updateStatusBasedOnQuantity(Inventory inventory) {
                InventoryStatus previousStatus = inventory.getStatus();

                if (inventory.getQuantityInStock() == 0) {
                        inventory.setStatus(InventoryStatus.OUT_OF_STOCK);
                } else if (inventory.getQuantityInStock() <= inventory.getMinimumStockLevel()) {
                        inventory.setStatus(InventoryStatus.LOW_STOCK);
                        if (previousStatus != InventoryStatus.LOW_STOCK) {
                                inventory.setLastThresholdBreach(LocalDateTime.now());
                        }
                } else {
                        inventory.setStatus(InventoryStatus.IN_STOCK);
                }

                if (inventory.getProduct() != null
                                && inventory.getStatus() != previousStatus
                                && (inventory.getStatus() == InventoryStatus.LOW_STOCK
                                                || inventory.getStatus() == InventoryStatus.OUT_OF_STOCK)) {
                        domainEventPublisher.publish(new LowStockWarningEvent(
                                        this,
                                        inventory.getProduct().getId(),
                                        inventory.getProduct().getName(),
                                        inventory.getQuantityInStock(),
                                        inventory.getMinimumStockLevel()));
                }
        }
}

package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.LowStockWarningEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        private final ProductRepository productRepository;
        private final DomainEventPublisher domainEventPublisher;

        @Override
        @Transactional
        public Inventory addStock(Long productId, Integer stock) {
                log.info("Adding stock {} for product {}", stock, productId);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElse(new Inventory());

                inventory.setProduct(product);
                inventory.setQuantityInStock(inventory.getQuantityInStock() + stock);
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
                log.info("Deducting {} stock from product {}", quantity, productId);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElseThrow(() -> new RuntimeException("Inventory not found"));

                if (inventory.getQuantityInStock() < quantity) {
                        throw new RuntimeException("Insufficient stock available");
                }

                inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
                inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
                inventory.setUpdatedAt(LocalDateTime.now());
                updateStatusBasedOnQuantity(inventory);

                inventoryRepository.save(inventory);
        }

        @Override
        public boolean hasStock(Long productId, Integer quantity) {
                log.info("Checking stock availability for product {}: {}", productId, quantity);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByProduct(product)
                                .orElse(new Inventory());

                return inventory.getQuantityInStock() >= quantity;
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

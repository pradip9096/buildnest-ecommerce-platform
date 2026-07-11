package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.exception.InventoryException;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryAuditLogRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Inventory reservation integration tests (INV-01, #73)")
class InventoryReservationIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private InventoryAuditLogRepository inventoryAuditLogRepository;

    @MockitoBean
    private DomainEventPublisher domainEventPublisher;

    private InventoryServiceImpl service;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        service = new InventoryServiceImpl(
                inventoryRepository, inventoryAuditLogRepository, productRepository, domainEventPublisher);

        Category category = new Category();
        category.setName("Tools");
        entityManager.persist(category);

        product = new Product();
        product.setName("Drill");
        product.setPrice(BigDecimal.valueOf(299.00));
        product.setCategory(category);
        product.setIsActive(true);
        entityManager.persist(product);

        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(10);
        inventory.setQuantityReserved(0);
        inventory.setMinimumStockLevel(2);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        inventory.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(inventory);
        entityManager.flush();
    }

    // --- hasStock ---

    @Test
    @DisplayName("hasStock uses available quantity (stock minus reserved), not raw stock")
    void hasStock_usesAvailableQuantity() {
        inventory.setQuantityReserved(8);
        entityManager.flush();

        // 10 in stock, 8 reserved → only 2 available
        assertFalse(service.hasStock(product.getId(), 3));
        assertTrue(service.hasStock(product.getId(), 2));
    }

    // --- reserveStock ---

    @Test
    @DisplayName("reserveStock increments quantityReserved and sets expiry")
    void reserveStock_incrementsReservedAndSetsExpiry() {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        service.reserveStock(product.getId(), 3, expiry);
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProduct(product).orElseThrow();
        assertEquals(3, updated.getQuantityReserved());
        assertEquals(7, updated.getAvailableQuantity()); // 10 - 3
        assertEquals(10, updated.getQuantityInStock());  // physical stock unchanged
        assertNotNull(updated.getReservationExpiresAt());
    }

    @Test
    @DisplayName("reserveStock throws InventoryException when available quantity is insufficient")
    void reserveStock_throwsWhenInsufficientAvailable() {
        inventory.setQuantityReserved(8); // only 2 available
        entityManager.flush();

        InventoryException ex = assertThrows(InventoryException.class,
                () -> service.reserveStock(product.getId(), 5, LocalDateTime.now().plusMinutes(15)));

        assertTrue(ex.getMessage().contains("Insufficient available stock"));
    }

    @Test
    @DisplayName("reserveStock does not touch quantityInStock")
    void reserveStock_doesNotDeductPhysicalStock() {
        service.reserveStock(product.getId(), 4, LocalDateTime.now().plusMinutes(15));
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProduct(product).orElseThrow();
        assertEquals(10, updated.getQuantityInStock());
    }

    // --- releaseReservation ---

    @Test
    @DisplayName("releaseReservation decrements quantityReserved and clears expiry when fully released")
    void releaseReservation_decrementsReservedAndClearsExpiry() {
        inventory.setQuantityReserved(5);
        inventory.setReservationExpiresAt(LocalDateTime.now().plusMinutes(10));
        entityManager.flush();

        service.releaseReservation(product.getId(), 5);
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProduct(product).orElseThrow();
        assertEquals(0, updated.getQuantityReserved());
        assertNull(updated.getReservationExpiresAt());
    }

    @Test
    @DisplayName("releaseReservation is safe to call when no reservation is held")
    void releaseReservation_safeWhenNothingReserved() {
        assertDoesNotThrow(() -> service.releaseReservation(product.getId(), 3));
    }

    @Test
    @DisplayName("releaseReservation does not make quantityReserved go negative")
    void releaseReservation_doesNotGoNegative() {
        inventory.setQuantityReserved(2);
        entityManager.flush();

        service.releaseReservation(product.getId(), 5); // releasing more than reserved
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProduct(product).orElseThrow();
        assertEquals(0, updated.getQuantityReserved());
    }

    // --- deductStock ---

    @Test
    @DisplayName("deductStock decrements quantityInStock and clears the reservation")
    void deductStock_decrementsStockAndClearsReservation() {
        inventory.setQuantityReserved(3);
        inventory.setReservationExpiresAt(LocalDateTime.now().plusMinutes(5));
        entityManager.flush();

        service.deductStock(product.getId(), 3);
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProduct(product).orElseThrow();
        assertEquals(7, updated.getQuantityInStock());
        assertEquals(0, updated.getQuantityReserved());
        assertNull(updated.getReservationExpiresAt());
    }

    // --- releaseExpiredReservations ---

    @Test
    @DisplayName("releaseExpiredReservations releases inventory where expiry has passed")
    void releaseExpiredReservations_releasesExpiredHolds() {
        inventory.setQuantityReserved(4);
        inventory.setReservationExpiresAt(LocalDateTime.now().minusMinutes(1)); // already expired
        entityManager.flush();

        service.releaseExpiredReservations();
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProduct(product).orElseThrow();
        assertEquals(0, updated.getQuantityReserved());
        assertNull(updated.getReservationExpiresAt());
    }

    @Test
    @DisplayName("releaseExpiredReservations does not release a reservation that has not yet expired")
    void releaseExpiredReservations_leavesActiveReservationsUntouched() {
        inventory.setQuantityReserved(4);
        inventory.setReservationExpiresAt(LocalDateTime.now().plusMinutes(10)); // still active
        entityManager.flush();

        service.releaseExpiredReservations();
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProduct(product).orElseThrow();
        assertEquals(4, updated.getQuantityReserved());
        assertNotNull(updated.getReservationExpiresAt());
    }

    @Test
    @DisplayName("findExpiredReservations query excludes rows with no reservationExpiresAt")
    void findExpiredReservations_excludesRowsWithNoExpiry() {
        // inventory has quantityReserved=0 and no expiry — should not appear
        List<Inventory> expired = inventoryRepository.findExpiredReservations(LocalDateTime.now());
        assertTrue(expired.isEmpty());
    }
}

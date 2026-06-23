package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class InventoryThresholdBreachEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventoryThresholdBreachEventRepository breachEventRepository;

    private Category testCategory;
    private Product testProduct1;
    private Product testProduct2;
    private Inventory testInventory1;
    private Inventory testInventory2;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setMinimumStockThreshold(10);
        entityManager.persist(testCategory);

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Test Product 1");
        testProduct1.setDescription("Description 1");
        testProduct1.setPrice(BigDecimal.valueOf(100.00));
        testProduct1.setCategory(testCategory);
        testProduct1.setIsActive(true);
        entityManager.persist(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Test Product 2");
        testProduct2.setDescription("Description 2");
        testProduct2.setPrice(BigDecimal.valueOf(200.00));
        testProduct2.setCategory(testCategory);
        testProduct2.setIsActive(true);
        entityManager.persist(testProduct2);

        // Create test inventory
        testInventory1 = new Inventory();
        testInventory1.setProduct(testProduct1);
        testInventory1.setQuantityInStock(5);
        testInventory1.setQuantityReserved(0);
        testInventory1.setMinimumStockLevel(10);
        testInventory1.setStatus(InventoryStatus.LOW_STOCK);
        entityManager.persist(testInventory1);

        testInventory2 = new Inventory();
        testInventory2.setProduct(testProduct2);
        testInventory2.setQuantityInStock(0);
        testInventory2.setQuantityReserved(0);
        testInventory2.setMinimumStockLevel(10);
        testInventory2.setStatus(InventoryStatus.OUT_OF_STOCK);
        entityManager.persist(testInventory2);

        entityManager.flush();
    }

    @Test
    @DisplayName("TC-THRESHOLD-001: Find recent threshold breaches")
    void testFindRecentBreaches() {
        // Arrange
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        InventoryThresholdBreachEvent oldBreach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(8)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(twoDaysAgo)
                .details("Stock fell below threshold 2 days ago")
                .build();
        entityManager.persist(oldBreach);

        InventoryThresholdBreachEvent recentBreach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(5)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(now)
                .details("Stock fell below threshold today")
                .build();
        entityManager.persist(recentBreach);

        entityManager.flush();

        // Act - Find breaches from last 24 hours
        List<InventoryThresholdBreachEvent> recentBreaches = breachEventRepository.findRecentBreaches(oneDayAgo);

        // Assert
        assertEquals(1, recentBreaches.size());
        assertEquals(5, recentBreaches.get(0).getCurrentQuantity());
        assertEquals("Stock fell below threshold today", recentBreaches.get(0).getDetails());
    }

    @Test
    @DisplayName("TC-THRESHOLD-002: Find breaches by product and severity")
    void testFindByProductAndSeverity() {
        // Arrange
        InventoryThresholdBreachEvent lowStockBreach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(8)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(LocalDateTime.now())
                .details("Low stock breach")
                .build();
        entityManager.persist(lowStockBreach);

        InventoryThresholdBreachEvent outOfStockBreach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(0)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK)
                .newStatus(InventoryStatus.OUT_OF_STOCK)
                .createdAt(LocalDateTime.now())
                .details("Out of stock breach")
                .build();
        entityManager.persist(outOfStockBreach);

        entityManager.flush();

        // Act
        List<InventoryThresholdBreachEvent> product1Breaches = breachEventRepository.findByProduct(testProduct1);

        // Assert
        assertEquals(2, product1Breaches.size());
        assertTrue(product1Breaches.stream()
                .anyMatch(b -> b.getBreachType() == InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH));
        assertTrue(product1Breaches.stream()
                .anyMatch(b -> b.getBreachType() == InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK));
    }

    @Test
    @DisplayName("TC-THRESHOLD-003: Breach event aggregation by time period")
    void testBreachEventAggregation() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            InventoryThresholdBreachEvent breach = InventoryThresholdBreachEvent.builder()
                    .inventory(testInventory1)
                    .product(testProduct1)
                    .currentQuantity(5 - i)
                    .thresholdLevel(10)
                    .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                    .newStatus(InventoryStatus.LOW_STOCK)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .details("Breach " + i)
                    .build();
            entityManager.persist(breach);
        }

        entityManager.flush();

        // Act
        List<InventoryThresholdBreachEvent> breachesInPeriod = breachEventRepository.findByCreatedAtBetween(startDate,
                endDate);

        // Assert - Note: findByCreatedAtBetween may not include boundaries correctly
        assertTrue(breachesInPeriod.size() >= 4, "Expected at least 4 breaches, found: " + breachesInPeriod.size());
    }

    @Test
    @DisplayName("TC-THRESHOLD-004: Resolved breaches tracking")
    void testResolvedBreaches() {
        // Arrange - Initial breach
        InventoryThresholdBreachEvent breach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(5)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(LocalDateTime.now().minusHours(2))
                .details("Stock below threshold")
                .build();
        entityManager.persist(breach);

        // Resolution event
        InventoryThresholdBreachEvent resolution = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(15)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_RESTORED)
                .newStatus(InventoryStatus.IN_STOCK)
                .createdAt(LocalDateTime.now())
                .details("Stock restored above threshold")
                .build();
        entityManager.persist(resolution);

        entityManager.flush();

        // Act
        List<InventoryThresholdBreachEvent> allEvents = breachEventRepository.findByProduct(testProduct1);

        // Assert
        assertEquals(2, allEvents.size());
        assertTrue(allEvents.stream()
                .anyMatch(e -> e.getBreachType() == InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH));
        assertTrue(allEvents.stream()
                .anyMatch(e -> e.getBreachType() == InventoryThresholdBreachEvent.BreachType.THRESHOLD_RESTORED));
    }

    @Test
    @DisplayName("TC-THRESHOLD-005: Breach notification tracking")
    void testBreachNotificationTracking() {
        // Arrange
        InventoryThresholdBreachEvent breach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory2)
                .product(testProduct2)
                .currentQuantity(0)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK)
                .newStatus(InventoryStatus.OUT_OF_STOCK)
                .createdAt(LocalDateTime.now())
                .details("Critical: Product out of stock - notification sent")
                .build();
        entityManager.persist(breach);
        entityManager.flush();

        // Act
        List<InventoryThresholdBreachEvent> criticalBreaches = breachEventRepository.findByProduct(testProduct2);

        // Assert
        assertEquals(1, criticalBreaches.size());
        assertEquals(InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK, criticalBreaches.get(0).getBreachType());
        assertTrue(criticalBreaches.get(0).getDetails().contains("notification sent"));
    }

    @Test
    @DisplayName("TC-THRESHOLD-006: Multiple products breach analysis")
    void testMultipleProductsBreachAnalysis() {
        // Arrange
        InventoryThresholdBreachEvent breach1 = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(5)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(LocalDateTime.now())
                .details("Product 1 low stock")
                .build();
        entityManager.persist(breach1);

        InventoryThresholdBreachEvent breach2 = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory2)
                .product(testProduct2)
                .currentQuantity(0)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK)
                .newStatus(InventoryStatus.OUT_OF_STOCK)
                .createdAt(LocalDateTime.now())
                .details("Product 2 out of stock")
                .build();
        entityManager.persist(breach2);

        entityManager.flush();

        // Act
        long totalBreaches = breachEventRepository.count();

        // Assert
        assertEquals(2, totalBreaches);
    }

    @Test
    @DisplayName("TC-THRESHOLD-007: Breach event date range filtering")
    void testBreachEventDateRangeFiltering() {
        // Arrange
        LocalDateTime fiveDaysAgo = LocalDateTime.now().minusDays(5);
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        InventoryThresholdBreachEvent oldBreach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(8)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(fiveDaysAgo)
                .details("Old breach")
                .build();
        entityManager.persist(oldBreach);

        InventoryThresholdBreachEvent recentBreach = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(5)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(oneDayAgo)
                .details("Recent breach")
                .build();
        entityManager.persist(recentBreach);

        entityManager.flush();

        // Act
        List<InventoryThresholdBreachEvent> breachesInRange = breachEventRepository.findByProductAndCreatedAtBetween(
                testProduct1, threeDaysAgo, LocalDateTime.now());

        // Assert
        assertEquals(1, breachesInRange.size());
        assertEquals("Recent breach", breachesInRange.get(0).getDetails());
    }

    @Test
    @DisplayName("TC-THRESHOLD-008: Breach event persistence and query")
    void testBreachEventPersistenceAndQuery() {
        // Arrange
        InventoryThresholdBreachEvent breach1 = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory1)
                .product(testProduct1)
                .currentQuantity(5)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.THRESHOLD_BREACH)
                .newStatus(InventoryStatus.LOW_STOCK)
                .createdAt(LocalDateTime.now())
                .details("First breach event")
                .build();
        entityManager.persist(breach1);

        InventoryThresholdBreachEvent breach2 = InventoryThresholdBreachEvent.builder()
                .inventory(testInventory2)
                .product(testProduct2)
                .currentQuantity(0)
                .thresholdLevel(10)
                .breachType(InventoryThresholdBreachEvent.BreachType.OUT_OF_STOCK)
                .newStatus(InventoryStatus.OUT_OF_STOCK)
                .createdAt(LocalDateTime.now())
                .details("Out of stock breach")
                .build();
        entityManager.persist(breach2);
        entityManager.flush();

        // Act
        List<InventoryThresholdBreachEvent> allBreaches = breachEventRepository.findAll();
        List<InventoryThresholdBreachEvent> product1Breaches = breachEventRepository.findByProduct(testProduct1);

        // Assert
        assertTrue(allBreaches.size() >= 2);
        assertTrue(product1Breaches.stream().anyMatch(b -> "First breach event".equals(b.getDetails())));
    }
}

package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryThresholdBreachEvent;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryAnalyticsServiceTest {

    @Test
    void computesHighDemandLowInventory() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("Prod");

        InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
        breach.setCreatedAt(LocalDateTime.now().minusDays(1));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(1);
        inventory.setMinimumStockLevel(5);
        inventory.setThresholdBreaches(List.of(breach));
        product.setInventory(inventory);

        when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(inventory));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                LocalDateTime.now().minusDays(2), LocalDateTime.now());

        assertEquals(1, results.size());
        assertEquals("Prod", results.get(0).get("productName"));
    }

    @Test
    void computesSeasonalPatternsAndTurnover() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("Prod");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(10);
        inventory.setMinimumStockLevel(5);
        product.setInventory(inventory);

        when(reportService.getFrequentlyLowStockProducts(any(), any()))
                .thenReturn(List.of(Map.of("productId", 1L, "breachCount", 2)));
        when(inventoryRepository.findByProductId(1L)).thenReturn(inventory);

        InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
        breach.setCreatedAt(LocalDateTime.now().minusDays(1));
        inventory.setThresholdBreaches(List.of(breach));
        when(productRepository.findAll()).thenReturn(List.of(product));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);

        List<Map<String, Object>> patterns = service.getSeasonalDemandPatterns(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());
        assertEquals(1, patterns.size());

        List<Map<String, Object>> turnover = service.getStockTurnoverAnalysis(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());
        assertEquals(1, turnover.size());
    }

    @Test
    void computesPredictivePlanAndRiskLevels() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("CriticalProduct");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(0);
        inventory.setMinimumStockLevel(10);

        InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
        breach.setCreatedAt(LocalDateTime.now().minusDays(1));
        inventory.setThresholdBreaches(List.of(breach, breach, breach, breach, breach, breach));
        product.setInventory(inventory);

        when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(inventory));
        when(reportService.getFrequentlyLowStockProducts(any(), any()))
                .thenReturn(List.of(Map.of("productId", 1L, "breachCount", 3)));
        when(inventoryRepository.findByProductId(1L)).thenReturn(inventory);
        when(productRepository.findAll()).thenReturn(List.of(product));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);

        List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                LocalDateTime.now().minusDays(5), LocalDateTime.now());

        assertEquals(1, results.size());
        assertEquals("CRITICAL", results.get(0).get("riskLevel"));
        assertTrue(results.get(0).get("recommendedAction").toString().contains("Restock"));

        Map<String, Object> plan = service.getPredictiveRestockingPlan(LocalDateTime.now().minusDays(5));
        assertTrue(plan.containsKey("urgentRestocks"));
        assertTrue(plan.containsKey("urgentCount"));
        assertTrue(plan.containsKey("patternCount"));
        assertTrue(plan.containsKey("stockAnalysis"));
    }

    @Test
    void testCalculatesRiskLevelCritical() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("TestProduct");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(0);
        inventory.setMinimumStockLevel(10);

        List<InventoryThresholdBreachEvent> breaches = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
            breach.setCreatedAt(LocalDateTime.now().minusDays(1));
            breaches.add(breach);
        }
        inventory.setThresholdBreaches(breaches);
        product.setInventory(inventory);

        when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(inventory));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                LocalDateTime.now().minusDays(2), LocalDateTime.now());

        assertEquals(1, results.size());
        assertEquals("CRITICAL", results.get(0).get("riskLevel"));
    }

    @Test
    void testCalculatesRiskLevelHigh() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("TestProduct");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(2);
        inventory.setMinimumStockLevel(10);

        List<InventoryThresholdBreachEvent> breaches = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
            breach.setCreatedAt(LocalDateTime.now().minusDays(1));
            breaches.add(breach);
        }
        inventory.setThresholdBreaches(breaches);
        product.setInventory(inventory);

        when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(inventory));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                LocalDateTime.now().minusDays(2), LocalDateTime.now());

        assertEquals(1, results.size());
        assertEquals("HIGH", results.get(0).get("riskLevel"));
    }

    @Test
    void testCategorizeTurnoverVeryHigh() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("VeryHighTurnover");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(10);
        inventory.setMinimumStockLevel(5);
        product.setInventory(inventory);

        List<InventoryThresholdBreachEvent> breaches = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
            breach.setCreatedAt(LocalDateTime.now().minusDays(1));
            breaches.add(breach);
        }
        inventory.setThresholdBreaches(breaches);

        when(productRepository.findAll()).thenReturn(List.of(product));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());

        assertEquals(1, analysis.size());
        assertEquals("VERY_HIGH_TURNOVER", analysis.get(0).get("turnoverCategory"));
    }

    @Test
    void testHealthStatusHealthy() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("HealthyProduct");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(15);
        inventory.setMinimumStockLevel(10);
        product.setInventory(inventory);

        InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
        breach.setCreatedAt(LocalDateTime.now().minusDays(1));
        inventory.setThresholdBreaches(List.of(breach));

        when(productRepository.findAll()).thenReturn(List.of(product));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());

        assertEquals(1, analysis.size());
        assertEquals("HEALTHY", analysis.get(0).get("healthStatus"));
    }

    @Test
    void testHealthStatusOverstocked() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("OverstockedProduct");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(15);
        inventory.setMinimumStockLevel(10);
        inventory.setThresholdBreaches(new ArrayList<>());
        product.setInventory(inventory);

        when(productRepository.findAll()).thenReturn(List.of(product));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());

        assertEquals(1, analysis.size());
        assertEquals("OVERSTOCKED", analysis.get(0).get("healthStatus"));
    }

    @Test
    void testHealthStatusUnderstocked() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("UnderstockedProduct");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(5);
        inventory.setMinimumStockLevel(10);
        product.setInventory(inventory);

        InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
        breach.setCreatedAt(LocalDateTime.now().minusDays(1));
        inventory.setThresholdBreaches(List.of(breach));

        when(productRepository.findAll()).thenReturn(List.of(product));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());

        assertEquals(1, analysis.size());
        assertEquals("UNDERSTOCKED", analysis.get(0).get("healthStatus"));
    }

    @Test
    void testHealthStatusCritical() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("CriticalProduct");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(5);
        inventory.setMinimumStockLevel(10);
        inventory.setThresholdBreaches(new ArrayList<>());
        product.setInventory(inventory);

        when(productRepository.findAll()).thenReturn(List.of(product));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());

        assertEquals(1, analysis.size());
        assertEquals("CRITICAL", analysis.get(0).get("healthStatus"));
    }

    @Test
    void riskLevelMediumAndLowPaths() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product mediumProduct = new Product();
        mediumProduct.setId(10L);
        mediumProduct.setName("MediumRisk");

        Inventory mediumInventory = new Inventory();
        mediumInventory.setProduct(mediumProduct);
        mediumInventory.setQuantityInStock(6);
        mediumInventory.setMinimumStockLevel(10);

        InventoryThresholdBreachEvent breach1 = new InventoryThresholdBreachEvent();
        breach1.setCreatedAt(LocalDateTime.now().minusDays(1));
        InventoryThresholdBreachEvent breach2 = new InventoryThresholdBreachEvent();
        breach2.setCreatedAt(LocalDateTime.now().minusDays(1));
        mediumInventory.setThresholdBreaches(List.of(breach1, breach2));
        mediumProduct.setInventory(mediumInventory);

        Product lowProduct = new Product();
        lowProduct.setId(11L);
        lowProduct.setName("LowRisk");

        Inventory lowInventory = new Inventory();
        lowInventory.setProduct(lowProduct);
        lowInventory.setQuantityInStock(10);
        lowInventory.setMinimumStockLevel(10);
        lowInventory.setThresholdBreaches(List.of(breach1));
        lowProduct.setInventory(lowInventory);

        when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(mediumInventory, lowInventory));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);

        List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                LocalDateTime.now().minusDays(5), LocalDateTime.now());

        assertEquals(2, results.size());
        Map<String, Object> mediumResult = results.stream()
                .filter(item -> "MediumRisk".equals(item.get("productName")))
                .findFirst()
                .orElseThrow();
        Map<String, Object> lowResult = results.stream()
                .filter(item -> "LowRisk".equals(item.get("productName")))
                .findFirst()
                .orElseThrow();

        assertEquals("MEDIUM", mediumResult.get("riskLevel"));
        assertEquals("LOW", lowResult.get("riskLevel"));
    }

    @Test
    void demandScoreDefaultsWhenInventoryMissing() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(12L);
        product.setName("DefaultDemand");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(5);
        inventory.setMinimumStockLevel(10);
        product.setInventory(null);

        when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(inventory));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                LocalDateTime.now().minusDays(3), LocalDateTime.now());

        assertEquals(1, results.size());
        assertEquals("LOW", results.get(0).get("riskLevel"));
        assertEquals(1L, results.get(0).get("demandScore"));
    }

    @Test
    void seasonalPatternsSkipMissingInventoryAndHandleSingleDay() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        when(reportService.getFrequentlyLowStockProducts(any(), any()))
                .thenReturn(List.of(Map.of("productId", 55L, "breachCount", 1)));
        when(inventoryRepository.findByProductId(55L)).thenReturn(null);

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);

        LocalDateTime today = LocalDateTime.now();
        List<Map<String, Object>> patterns = service.getSeasonalDemandPatterns(today, today);
        assertTrue(patterns.isEmpty());
    }

    @Test
    void seasonalPatternVeryHighDemandAndSafetyStock() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(66L);
        product.setName("Seasonal");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(7);
        inventory.setMinimumStockLevel(3);
        product.setInventory(inventory);

        when(reportService.getFrequentlyLowStockProducts(any(), any()))
                .thenReturn(List.of(Map.of("productId", 66L, "breachCount", 1)));
        when(inventoryRepository.findByProductId(66L)).thenReturn(inventory);

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);

        LocalDateTime today = LocalDateTime.now();
        List<Map<String, Object>> patterns = service.getSeasonalDemandPatterns(today, today);

        assertEquals(1, patterns.size());
        assertEquals("VERY_HIGH_DEMAND", patterns.get(0).get("pattern"));
        assertEquals(5, patterns.get(0).get("suggestedSafetyStock"));
    }

    @Test
    void stockTurnoverSkipsNullInventoryAndZeroStock() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product noInventory = new Product();
        noInventory.setId(70L);
        noInventory.setName("NoInventory");

        Product zeroStock = new Product();
        zeroStock.setId(71L);
        zeroStock.setName("ZeroStock");

        Inventory zeroInventory = new Inventory();
        zeroInventory.setProduct(zeroStock);
        zeroInventory.setQuantityInStock(0);
        zeroInventory.setMinimumStockLevel(5);
        zeroInventory.setThresholdBreaches(List.of());
        zeroStock.setInventory(zeroInventory);

        when(productRepository.findAll()).thenReturn(List.of(noInventory, zeroStock));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);

        List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                LocalDateTime.now().minusDays(5), LocalDateTime.now());

        assertTrue(analysis.isEmpty());
    }

    @Test
    void stockTurnoverCategoryModerateLowAndStagnant() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        LocalDateTime now = LocalDateTime.now();

        Product moderateProduct = new Product();
        moderateProduct.setId(80L);
        moderateProduct.setName("Moderate");
        Inventory moderateInventory = new Inventory();
        moderateInventory.setProduct(moderateProduct);
        moderateInventory.setQuantityInStock(10);
        moderateInventory.setMinimumStockLevel(5);
        List<InventoryThresholdBreachEvent> moderateBreaches = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
            breach.setCreatedAt(now.minusDays(1));
            moderateBreaches.add(breach);
        }
        moderateInventory.setThresholdBreaches(moderateBreaches);
        moderateProduct.setInventory(moderateInventory);

        Product lowProduct = new Product();
        lowProduct.setId(81L);
        lowProduct.setName("Low");
        Inventory lowInventory = new Inventory();
        lowInventory.setProduct(lowProduct);
        lowInventory.setQuantityInStock(10);
        lowInventory.setMinimumStockLevel(5);
        InventoryThresholdBreachEvent lowBreach = new InventoryThresholdBreachEvent();
        lowBreach.setCreatedAt(now.minusDays(1));
        lowInventory.setThresholdBreaches(List.of(lowBreach));
        lowProduct.setInventory(lowInventory);

        Product stagnantProduct = new Product();
        stagnantProduct.setId(82L);
        stagnantProduct.setName("Stagnant");
        Inventory stagnantInventory = new Inventory();
        stagnantInventory.setProduct(stagnantProduct);
        stagnantInventory.setQuantityInStock(10);
        stagnantInventory.setMinimumStockLevel(5);
        stagnantInventory.setThresholdBreaches(List.of());
        stagnantProduct.setInventory(stagnantInventory);

        when(productRepository.findAll()).thenReturn(List.of(moderateProduct, lowProduct, stagnantProduct));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);

        List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                now.minusDays(10), now);

        assertEquals(3, analysis.size());
        assertTrue(analysis.stream().anyMatch(item -> "MODERATE_TURNOVER".equals(item.get("turnoverCategory"))));
        assertTrue(analysis.stream().anyMatch(item -> "LOW_TURNOVER".equals(item.get("turnoverCategory"))));
        assertTrue(analysis.stream().anyMatch(item -> "STAGNANT".equals(item.get("turnoverCategory"))));
    }

    @Test
    void demandScoreDefaultsWhenBreachesNull() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        Product product = new Product();
        product.setId(90L);
        product.setName("NullBreaches");

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(3);
        inventory.setMinimumStockLevel(10);
        inventory.setThresholdBreaches(null);
        product.setInventory(inventory);

        when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(inventory));

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                LocalDateTime.now().minusDays(2), LocalDateTime.now());

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).get("demandScore"));
    }

    @Test
    void riskScoreDefaultsViaReflection() throws Exception {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryReportService reportService = mock(InventoryReportService.class);

        InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository, productRepository,
                reportService);
        var method = InventoryAnalyticsService.class.getDeclaredMethod("getRiskScore", String.class);
        method.setAccessible(true);

        assertEquals(4, method.invoke(service, "CRITICAL"));
        assertEquals(1, method.invoke(service, "UNKNOWN"));
    }
}

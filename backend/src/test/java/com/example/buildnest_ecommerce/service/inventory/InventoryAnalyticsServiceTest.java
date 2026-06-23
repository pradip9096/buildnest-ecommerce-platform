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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
                                reportService);

                List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(
                                now.minusDays(10), now);

                assertEquals(3, analysis.size());
                assertTrue(analysis.stream()
                                .anyMatch(item -> "MODERATE_TURNOVER".equals(item.get("turnoverCategory"))));
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
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

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
                                reportService);
                var method = InventoryAnalyticsService.class.getDeclaredMethod("getRiskScore", String.class);
                method.setAccessible(true);

                assertEquals(4, method.invoke(service, "CRITICAL"));
                assertEquals(1, method.invoke(service, "UNKNOWN"));
        }

        @Test
        void identifyPatternBoundaryValues() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                var method = InventoryAnalyticsService.class.getDeclaredMethod("identifyPattern", Integer.class,
                                int.class);
                method.setAccessible(true);

                assertEquals("VERY_HIGH_DEMAND", method.invoke(service, 2, 2));
                assertEquals("HIGH_DEMAND", method.invoke(service, 1, 2));
                assertEquals("MODERATE_DEMAND", method.invoke(service, 1, 5));
                assertEquals("LOW_DEMAND", method.invoke(service, 1, 20));
        }

        @Test
        void categorizeTurnoverBoundaryValues() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                var method = InventoryAnalyticsService.class.getDeclaredMethod("categorizeTurnover", long.class);
                method.setAccessible(true);

                assertEquals("VERY_HIGH_TURNOVER", method.invoke(service, 21L));
                assertEquals("HIGH_TURNOVER", method.invoke(service, 20L));
                assertEquals("MODERATE_TURNOVER", method.invoke(service, 6L));
                assertEquals("LOW_TURNOVER", method.invoke(service, 1L));
                assertEquals("STAGNANT", method.invoke(service, 0L));
        }

        @Test
        void calculateRiskLevelBoundaryValues() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setQuantityInStock(0);
                inventory.setMinimumStockLevel(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("calculateRiskLevel", Inventory.class,
                                long.class);
                method.setAccessible(true);

                assertEquals("HIGH", method.invoke(service, inventory, 5L));
                assertEquals("MEDIUM", method.invoke(service, inventory, 3L));
                assertEquals("LOW", method.invoke(service, inventory, 1L));
        }

        @Test
        void restockRecommendationUsesMaxRule() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(5);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("getRestockRecommendation",
                                Inventory.class,
                                long.class);
                method.setAccessible(true);

                assertEquals("Restock to 10 units", method.invoke(service, inventory, 1L));
                assertEquals("Restock to 55 units", method.invoke(service, inventory, 10L));
        }

        @Test
        void healthStatusBoundaryQuantityEqualsMinimum() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(10);
                inventory.setQuantityInStock(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("getHealthStatus", Inventory.class,
                                long.class);
                method.setAccessible(true);

                assertEquals("UNDERSTOCKED", method.invoke(service, inventory, 1L));
                assertEquals("CRITICAL", method.invoke(service, inventory, 0L));
        }

        @Test
        void highDemandResultsSortedByRiskScore() {
                InventoryRepository inventoryRepository = mock(InventoryRepository.class);
                ProductRepository productRepository = mock(ProductRepository.class);
                InventoryReportService reportService = mock(InventoryReportService.class);

                Product criticalProduct = new Product();
                criticalProduct.setId(101L);
                criticalProduct.setName("Critical");
                Inventory criticalInventory = new Inventory();
                criticalInventory.setProduct(criticalProduct);
                criticalInventory.setQuantityInStock(0);
                criticalInventory.setMinimumStockLevel(10);
                List<InventoryThresholdBreachEvent> criticalBreaches = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                        InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
                        breach.setCreatedAt(LocalDateTime.now().minusDays(1));
                        criticalBreaches.add(breach);
                }
                criticalInventory.setThresholdBreaches(criticalBreaches);
                criticalProduct.setInventory(criticalInventory);

                Product lowProduct = new Product();
                lowProduct.setId(102L);
                lowProduct.setName("Low");
                Inventory lowInventory = new Inventory();
                lowInventory.setProduct(lowProduct);
                lowInventory.setQuantityInStock(10);
                lowInventory.setMinimumStockLevel(10);
                InventoryThresholdBreachEvent lowBreach = new InventoryThresholdBreachEvent();
                lowBreach.setCreatedAt(LocalDateTime.now().minusDays(1));
                lowInventory.setThresholdBreaches(List.of(lowBreach));
                lowProduct.setInventory(lowInventory);

                when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(lowInventory, criticalInventory));

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
                                reportService);
                List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                                LocalDateTime.now().minusDays(5), LocalDateTime.now());

                assertEquals("CRITICAL", results.get(0).get("riskLevel"));
        }

        @Test
        void stockTurnoverSortedByRecentTransactions() {
                InventoryRepository inventoryRepository = mock(InventoryRepository.class);
                ProductRepository productRepository = mock(ProductRepository.class);
                InventoryReportService reportService = mock(InventoryReportService.class);

                LocalDateTime now = LocalDateTime.now();

                Product highProduct = new Product();
                highProduct.setId(201L);
                highProduct.setName("HighTurnover");
                Inventory highInventory = new Inventory();
                highInventory.setProduct(highProduct);
                highInventory.setQuantityInStock(10);
                highInventory.setMinimumStockLevel(5);
                List<InventoryThresholdBreachEvent> highBreaches = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                        InventoryThresholdBreachEvent breach = new InventoryThresholdBreachEvent();
                        breach.setCreatedAt(now.minusDays(1));
                        highBreaches.add(breach);
                }
                highInventory.setThresholdBreaches(highBreaches);
                highProduct.setInventory(highInventory);

                Product lowProduct = new Product();
                lowProduct.setId(202L);
                lowProduct.setName("LowTurnover");
                Inventory lowInventory = new Inventory();
                lowInventory.setProduct(lowProduct);
                lowInventory.setQuantityInStock(10);
                lowInventory.setMinimumStockLevel(5);
                InventoryThresholdBreachEvent lowBreach = new InventoryThresholdBreachEvent();
                lowBreach.setCreatedAt(now.minusDays(1));
                lowInventory.setThresholdBreaches(List.of(lowBreach));
                lowProduct.setInventory(lowInventory);

                when(productRepository.findAll()).thenReturn(List.of(lowProduct, highProduct));

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
                                reportService);
                List<Map<String, Object>> analysis = service.getStockTurnoverAnalysis(now.minusDays(5), now);

                assertEquals("HighTurnover", analysis.get(0).get("productName"));
        }

        // ========== BOUNDARY CONDITION TESTS FOR MUTATION COVERAGE ==========

        @Test
        void calculateRiskLevelCriticalBoundary() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("calculateRiskLevel",
                                Inventory.class, long.class);
                method.setAccessible(true);

                // Test exact boundary: quantity = 0 and demandScore > 5
                inventory.setQuantityInStock(0);
                assertEquals("CRITICAL", method.invoke(service, inventory, 6L)); // > 5 so CRITICAL
                assertEquals("HIGH", method.invoke(service, inventory, 5L)); // = 5 so not > 5, not CRITICAL
                assertEquals("HIGH", method.invoke(service, inventory, 4L)); // < 5 so not > 5, not CRITICAL

                // Test with stock > 0: shouldn't be CRITICAL
                inventory.setQuantityInStock(1);
                assertEquals("HIGH", method.invoke(service, inventory, 6L)); // First condition fails (not 0)
        }

        @Test
        void calculateRiskLevelHighBoundary() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("calculateRiskLevel",
                                Inventory.class, long.class);
                method.setAccessible(true);

                // Test exact boundary: quantity < minStockLevel/2 (< 5) and demandScore > 3
                inventory.setQuantityInStock(4);
                assertEquals("HIGH", method.invoke(service, inventory, 4L)); // 4 < 5 AND 4 > 3 = HIGH
                assertEquals("MEDIUM", method.invoke(service, inventory, 3L)); // 4 < 5 but 3 NOT > 3, fail second
                                                                               // condition

                inventory.setQuantityInStock(5); // = 5, not < 5, skip first check
                assertEquals("MEDIUM", method.invoke(service, inventory, 4L)); // Falls to MEDIUM check
        }

        @Test
        void calculateRiskLevelMediumBoundary() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("calculateRiskLevel",
                                Inventory.class, long.class);
                method.setAccessible(true);

                // Test exact boundary: quantity < minStockLevel (< 10) and demandScore > 1
                inventory.setQuantityInStock(9);
                assertEquals("MEDIUM", method.invoke(service, inventory, 2L)); // 9 < 10 AND 2 > 1 = MEDIUM
                assertEquals("LOW", method.invoke(service, inventory, 1L)); // 9 < 10 but 1 NOT > 1, fail

                inventory.setQuantityInStock(10); // = 10, not < 10
                assertEquals("LOW", method.invoke(service, inventory, 2L)); // Falls through to LOW
        }

        @Test
        void categorizeTurnoverBoundaries() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                var method = InventoryAnalyticsService.class.getDeclaredMethod("categorizeTurnover", long.class);
                method.setAccessible(true);

                // Test exact boundaries for each category
                // VERY_HIGH_TURNOVER: > 20
                assertEquals("VERY_HIGH_TURNOVER", method.invoke(service, 21L));
                assertEquals("HIGH_TURNOVER", method.invoke(service, 20L)); // = 20, not > 20

                // HIGH_TURNOVER: > 10
                assertEquals("HIGH_TURNOVER", method.invoke(service, 11L));
                assertEquals("MODERATE_TURNOVER", method.invoke(service, 10L)); // = 10, not > 10

                // MODERATE_TURNOVER: > 5
                assertEquals("MODERATE_TURNOVER", method.invoke(service, 6L));
                assertEquals("LOW_TURNOVER", method.invoke(service, 5L)); // = 5, not > 5

                // LOW_TURNOVER: > 0
                assertEquals("LOW_TURNOVER", method.invoke(service, 1L));
                assertEquals("STAGNANT", method.invoke(service, 0L)); // = 0, not > 0
        }

        @Test
        void identifyPatternBoundaries() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                var method = InventoryAnalyticsService.class.getDeclaredMethod("identifyPattern",
                                Integer.class, int.class);
                method.setAccessible(true);

                // frequency = breachCount / daysInPeriod
                // Test boundaries: > 0.5, > 0.2, > 0.05, else LOW_DEMAND

                // VERY_HIGH_DEMAND: frequency > 0.5
                assertEquals("VERY_HIGH_DEMAND", method.invoke(service, 26, 50)); // 26/50 = 0.52 > 0.5
                assertEquals("HIGH_DEMAND", method.invoke(service, 25, 50)); // 25/50 = 0.5, NOT > 0.5

                // HIGH_DEMAND: frequency > 0.2
                assertEquals("HIGH_DEMAND", method.invoke(service, 11, 50)); // 11/50 = 0.22 > 0.2
                assertEquals("MODERATE_DEMAND", method.invoke(service, 10, 50)); // 10/50 = 0.2, NOT > 0.2

                // MODERATE_DEMAND: frequency > 0.05
                assertEquals("MODERATE_DEMAND", method.invoke(service, 3, 50)); // 3/50 = 0.06 > 0.05
                assertEquals("LOW_DEMAND", method.invoke(service, 2, 50)); // 2/50 = 0.04, NOT > 0.05
        }

        // ========== MATH OPERATOR TESTS FOR MUTATION COVERAGE ==========

        @Test
        void restockRecommendationMathOperations() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("getRestockRecommendation",
                                Inventory.class, long.class);
                method.setAccessible(true);

                // Test Math.max logic: max(minStockLevel * 2, minStockLevel + demandScore * 5)
                // With minStockLevel = 10:
                // Option 1: 10 * 2 = 20
                // Option 2: 10 + demandScore * 5

                // demandScore = 0: max(20, 10) = 20
                Object result = method.invoke(service, inventory, 0L);
                assertTrue(result.toString().contains("20"), "Expected 20, got " + result);

                // demandScore = 2: max(20, 10 + 10) = max(20, 20) = 20
                result = method.invoke(service, inventory, 2L);
                assertTrue(result.toString().contains("20"), "Expected 20, got " + result);

                // demandScore = 5: max(20, 10 + 25) = max(20, 35) = 35
                result = method.invoke(service, inventory, 5L);
                assertTrue(result.toString().contains("35"), "Expected 35, got " + result);

                // demandScore = 10: max(20, 10 + 50) = max(20, 60) = 60
                result = method.invoke(service, inventory, 10L);
                assertTrue(result.toString().contains("60"), "Expected 60, got " + result);
        }

        @Test
        void calculateSafetyStockMathOperations() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("calculateSafetyStock",
                                Inventory.class, Integer.class);
                method.setAccessible(true);

                // Test formula: minStockLevel + (breachCount * 2)
                // With minStockLevel = 10:

                // breachCount = 0: 10 + (0 * 2) = 10
                assertEquals(10, method.invoke(service, inventory, 0));

                // breachCount = 1: 10 + (1 * 2) = 12
                assertEquals(12, method.invoke(service, inventory, 1));

                // breachCount = 5: 10 + (5 * 2) = 20
                assertEquals(20, method.invoke(service, inventory, 5));

                // breachCount = 10: 10 + (10 * 2) = 30
                assertEquals(30, method.invoke(service, inventory, 10));
        }

        @Test
        void getHealthStatusBoundaryConditions() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                Inventory inventory = new Inventory();
                inventory.setMinimumStockLevel(10);

                var method = InventoryAnalyticsService.class.getDeclaredMethod("getHealthStatus",
                                Inventory.class, long.class);
                method.setAccessible(true);

                // Test hasSufficientStock: quantity > minStockLevel
                // Test isMoving: recentTransactions > 0

                // Sufficient stock AND moving -> HEALTHY
                inventory.setQuantityInStock(11);
                assertEquals("HEALTHY", method.invoke(service, inventory, 1L));
                assertEquals("HEALTHY", method.invoke(service, inventory, 10L));

                // Sufficient stock (quantity = 10) but NOT > 10
                inventory.setQuantityInStock(10);
                assertEquals("UNDERSTOCKED", method.invoke(service, inventory, 1L)); // NOT > 10, so not sufficient

                // Insufficient stock AND moving -> UNDERSTOCKED
                inventory.setQuantityInStock(9);
                assertEquals("UNDERSTOCKED", method.invoke(service, inventory, 1L));

                // Insufficient stock AND not moving -> CRITICAL
                inventory.setQuantityInStock(9);
                assertEquals("CRITICAL", method.invoke(service, inventory, 0L));
        }

        @Test
        void demandScoreCalculationEdgeCases() throws Exception {
                InventoryAnalyticsService service = new InventoryAnalyticsService(
                                mock(InventoryRepository.class),
                                mock(ProductRepository.class),
                                mock(InventoryReportService.class));

                var method = InventoryAnalyticsService.class.getDeclaredMethod(
                                "calculateProductDemandScore", Product.class, LocalDateTime.class, LocalDateTime.class);
                method.setAccessible(true);

                Product product = new Product();
                LocalDateTime now = LocalDateTime.now();

                // Test: null inventory should return 1
                product.setInventory(null);
                assertEquals(1L, method.invoke(service, product, now.minusDays(5), now));

                // Test: null breaches should return 1
                Inventory inventory = new Inventory();
                inventory.setThresholdBreaches(null);
                product.setInventory(inventory);
                assertEquals(1L, method.invoke(service, product, now.minusDays(5), now));

                // Test: score = 0 should become 1 (Math.max)
                inventory.setThresholdBreaches(new ArrayList<>()); // No breaches in range
                assertEquals(1L, method.invoke(service, product, now.minusDays(5), now));

                // Test: normal case with breaches
                InventoryThresholdBreachEvent breach1 = new InventoryThresholdBreachEvent();
                breach1.setCreatedAt(now.minusDays(2));
                InventoryThresholdBreachEvent breach2 = new InventoryThresholdBreachEvent();
                breach2.setCreatedAt(now.minusDays(1));
                inventory.setThresholdBreaches(List.of(breach1, breach2));
                assertEquals(2L, method.invoke(service, product, now.minusDays(5), now));
        }

        @Test
        void highDemandLowInventoryDemandScoreBoundary() {
                InventoryRepository inventoryRepository = mock(InventoryRepository.class);
                ProductRepository productRepository = mock(ProductRepository.class);
                InventoryReportService reportService = mock(InventoryReportService.class);

                Product product = new Product();
                product.setId(1L);
                product.setName("TestProduct");

                Inventory inventory = new Inventory();
                inventory.setProduct(product);
                inventory.setQuantityInStock(1);
                inventory.setMinimumStockLevel(5);

                // Test boundary: demandScore = 0 should not appear in results (if demandScore >
                // 0)
                inventory.setThresholdBreaches(new ArrayList<>()); // 0 breaches
                product.setInventory(inventory);

                when(inventoryRepository.findLowStockProducts()).thenReturn(List.of(inventory));

                InventoryAnalyticsService service = new InventoryAnalyticsService(inventoryRepository,
                                productRepository,
                                reportService);
                List<Map<String, Object>> results = service.getHighDemandLowInventoryProducts(
                                LocalDateTime.now().minusDays(2), LocalDateTime.now());

                // With 0 breaches, demandScore becomes 1 (Math.max), so it should be included
                assertEquals(1, results.size());

                // Test with negative case: no low stock products
                when(inventoryRepository.findLowStockProducts()).thenReturn(new ArrayList<>());
                results = service.getHighDemandLowInventoryProducts(
                                LocalDateTime.now().minusDays(2), LocalDateTime.now());
                assertEquals(0, results.size());
        }
}

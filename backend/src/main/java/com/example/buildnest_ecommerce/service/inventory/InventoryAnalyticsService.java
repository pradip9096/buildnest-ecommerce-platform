package com.example.buildnest_ecommerce.service.inventory;

import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for inventory analytics and demand correlation (RQ-INV-ANA-01,
 * RQ-INV-ANA-02).
 * Correlates inventory levels with product demand/search patterns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryAnalyticsService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryReportService reportService;

    /**
     * Get products with high demand but low inventory (RQ-INV-ANA-01,
     * RQ-INV-ANA-02).
     * Identifies products that are frequently searched/viewed but have low stock.
     * This helps prioritize restocking.
     */
    public List<Map<String, Object>> getHighDemandLowInventoryProducts(
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        log.info("Analyzing high-demand low-inventory products from {} to {}", fromDate, toDate);

        List<Inventory> lowStockProducts = inventoryRepository.findLowStockProducts();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Inventory inventory : lowStockProducts) {
            Product product = inventory.getProduct();

            // Calculate demand score based on:
            // 1. View count (if available)
            // 2. Recent searches
            // 3. Wishlist additions (if available)
            long demandScore = calculateProductDemandScore(product, fromDate, toDate);

            if (demandScore > 0) {
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("productId", product.getId());
                analysis.put("productName", product.getName());
                analysis.put("currentStock", inventory.getQuantityInStock());
                analysis.put("minimumThreshold", inventory.getMinimumStockLevel());
                analysis.put("shortfall", inventory.getMinimumStockLevel() - inventory.getQuantityInStock());
                analysis.put("demandScore", demandScore);
                analysis.put("riskLevel", calculateRiskLevel(inventory, demandScore));
                analysis.put("recommendedAction", getRestockRecommendation(inventory, demandScore));

                results.add(analysis);
            }
        }

        // Sort by risk level (highest demand with lowest stock first)
        results.sort((a, b) -> {
            String riskA = (String) a.get("riskLevel");
            String riskB = (String) b.get("riskLevel");
            return getRiskScore(riskB) - getRiskScore(riskA);
        });

        log.info("Found {} high-demand low-inventory products", results.size());
        return results;
    }

    /**
     * Get seasonal demand patterns and inventory recommendations (RQ-INV-ANA-02).
     * Analyzes historical breach patterns to identify seasonal trends.
     */
    public List<Map<String, Object>> getSeasonalDemandPatterns(
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        log.info("Analyzing seasonal demand patterns from {} to {}", fromDate, toDate);

        // Get historical breach data
        List<Map<String, Object>> frequentProblems = reportService.getFrequentlyLowStockProducts(
                fromDate, toDate);

        List<Map<String, Object>> patterns = new ArrayList<>();

        for (Map<String, Object> problem : frequentProblems) {
            Long productId = ((Number) problem.get("productId")).longValue();
            Integer breachCount = ((Number) problem.get("breachCount")).intValue();

            Inventory inventory = inventoryRepository.findByProductId(productId);
            if (inventory == null)
                continue;

            Product product = inventory.getProduct();

            // Calculate frequency and trend
            int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate);
            double breachFrequency = (double) breachCount / Math.max(daysInPeriod, 1);

            Map<String, Object> pattern = new HashMap<>();
            pattern.put("productId", product.getId());
            pattern.put("productName", product.getName());
            pattern.put("totalBreaches", breachCount);
            pattern.put("breachFrequency", String.format("%.2f breaches per day", breachFrequency));
            pattern.put("pattern", identifyPattern(breachCount, daysInPeriod));
            pattern.put("currentStock", inventory.getQuantityInStock());
            pattern.put("suggestedSafetyStock", calculateSafetyStock(inventory, breachCount));

            patterns.add(pattern);
        }

        log.info("Identified {} seasonal patterns", patterns.size());
        return patterns;
    }

    /**
     * Get stock turnover analysis (RQ-INV-ANA-02).
     * Shows which products are moving quickly vs. stagnating.
     */
    public List<Map<String, Object>> getStockTurnoverAnalysis(
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        log.info("Analyzing stock turnover from {} to {}", fromDate, toDate);

        List<Product> allProducts = productRepository.findAll();
        List<Map<String, Object>> analysis = new ArrayList<>();

        for (Product product : allProducts) {
            Inventory inventory = product.getInventory();
            if (inventory == null)
                continue;

            // Get breach count as proxy for sales/turnover
            long breaches = inventory.getThresholdBreaches().stream()
                    .filter(breach -> breach.getCreatedAt().isAfter(fromDate) &&
                            breach.getCreatedAt().isBefore(toDate))
                    .count();

            if (inventory.getQuantityInStock() > 0) {
                Map<String, Object> turnover = new HashMap<>();
                turnover.put("productId", product.getId());
                turnover.put("productName", product.getName());
                turnover.put("currentStock", inventory.getQuantityInStock());
                turnover.put("recentTransactions", breaches);
                turnover.put("turnoverCategory", categorizeTurnover(breaches));
                turnover.put("healthStatus", getHealthStatus(inventory, breaches));

                analysis.add(turnover);
            }
        }

        // Sort by turnover (high turnover first)
        analysis.sort((a, b) -> ((Number) b.get("recentTransactions")).intValue() -
                ((Number) a.get("recentTransactions")).intValue());

        log.info("Analyzed stock turnover for {} products", analysis.size());
        return analysis;
    }

    /**
     * Get predictive restocking recommendations (RQ-INV-ANA-01).
     * Uses historical patterns to predict when restocking will be needed.
     */
    public Map<String, Object> getPredictiveRestockingPlan(LocalDateTime analysisPeriodStart) {
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> plan = new HashMap<>();
        plan.put("generatedAt", now);
        plan.put("analysisPeriod", analysisPeriodStart.toString() + " to " + now);

        // Get high-demand low-inventory products
        List<Map<String, Object>> urgentRestocks = getHighDemandLowInventoryProducts(
                analysisPeriodStart, now);

        // Get seasonal patterns for future planning
        List<Map<String, Object>> patterns = getSeasonalDemandPatterns(
                analysisPeriodStart, now);

        // Get stock analysis
        List<Map<String, Object>> turnover = getStockTurnoverAnalysis(
                analysisPeriodStart, now);

        plan.put("urgentRestocks", urgentRestocks);
        plan.put("urgentCount", urgentRestocks.size());
        plan.put("seasonalPatterns", patterns);
        plan.put("patternCount", patterns.size());
        plan.put("stockAnalysis", turnover);

        return plan;
    }

    // Helper methods

    private long calculateProductDemandScore(Product product, LocalDateTime fromDate, LocalDateTime toDate) {
        // In a real system, this would correlate with:
        // - Product search count from search analytics
        // - Product view count from analytics
        // - Wishlist additions
        // For now, use threshold breach count as proxy (higher breaches = higher
        // demand)

        Inventory inventory = product.getInventory();
        if (inventory == null || inventory.getThresholdBreaches() == null) {
            return 1; // Default score
        }

        long demandScore = inventory.getThresholdBreaches().stream()
                .filter(breach -> breach.getCreatedAt().isAfter(fromDate) &&
                        breach.getCreatedAt().isBefore(toDate))
                .count();

        return Math.max(demandScore, 1); // Minimum score of 1
    }

    private String calculateRiskLevel(Inventory inventory, long demandScore) {
        if (inventory.getQuantityInStock() == 0 && demandScore > 5) {
            return "CRITICAL";
        } else if (inventory.getQuantityInStock() < inventory.getMinimumStockLevel() / 2 && demandScore > 3) {
            return "HIGH";
        } else if (inventory.getQuantityInStock() < inventory.getMinimumStockLevel() && demandScore > 1) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private int getRiskScore(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }

    private String getRestockRecommendation(Inventory inventory, long demandScore) {
        int recommended = Math.max(
                inventory.getMinimumStockLevel() * 2,
                inventory.getMinimumStockLevel() + (int) (demandScore * 5));
        return "Restock to " + recommended + " units";
    }

    private Integer calculateSafetyStock(Inventory inventory, Integer breachCount) {
        // Safety stock = minimum threshold + (breaches / period * buffer)
        return inventory.getMinimumStockLevel() + (breachCount * 2);
    }

    private String identifyPattern(Integer breachCount, int daysInPeriod) {
        double frequency = (double) breachCount / Math.max(daysInPeriod, 1);

        if (frequency > 0.5) {
            return "VERY_HIGH_DEMAND";
        } else if (frequency > 0.2) {
            return "HIGH_DEMAND";
        } else if (frequency > 0.05) {
            return "MODERATE_DEMAND";
        }
        return "LOW_DEMAND";
    }

    private String categorizeTurnover(long transactionCount) {
        if (transactionCount > 20)
            return "VERY_HIGH_TURNOVER";
        if (transactionCount > 10)
            return "HIGH_TURNOVER";
        if (transactionCount > 5)
            return "MODERATE_TURNOVER";
        if (transactionCount > 0)
            return "LOW_TURNOVER";
        return "STAGNANT";
    }

    private String getHealthStatus(Inventory inventory, long recentTransactions) {
        boolean hasSufficientStock = inventory.getQuantityInStock() > inventory.getMinimumStockLevel();
        boolean isMoving = recentTransactions > 0;

        if (hasSufficientStock && isMoving) {
            return "HEALTHY";
        } else if (hasSufficientStock && !isMoving) {
            return "OVERSTOCKED";
        } else if (!hasSufficientStock && isMoving) {
            return "UNDERSTOCKED";
        }
        return "CRITICAL";
    }
}

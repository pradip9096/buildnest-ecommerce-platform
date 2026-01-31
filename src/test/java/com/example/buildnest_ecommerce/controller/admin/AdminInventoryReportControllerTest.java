package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.inventory.InventoryReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test suite for AdminInventoryReportController.
 * Tests inventory reporting endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@SuppressWarnings("null")
class AdminInventoryReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryReportService reportService;

    private CustomUserDetails adminDetails;

    @BeforeEach
    void setUp() {
        adminDetails = new CustomUserDetails(
                1L,
                "admin",
                "admin@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                true,
                true,
                true,
                true);
    }

    @Test
    @DisplayName("TC-REPORT-001: Get products below threshold")
    void testGetProductsBelowThreshold() throws Exception {
        Map<String, Object> product = new HashMap<>();
        product.put("productId", 1L);
        product.put("productName", "Test Product");
        product.put("currentStock", 5);
        product.put("threshold", 10);

        when(reportService.getProductsBelowThreshold())
                .thenReturn(Arrays.asList(product));

        mockMvc.perform(get("/api/admin/inventory-reports/below-threshold")
                .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].currentStock").value(5));
    }

    @Test
    @DisplayName("TC-REPORT-002: Get threshold breaches in range")
    void testGetThresholdBreachesInRange() throws Exception {
        Map<String, Object> breach = new HashMap<>();
        breach.put("breachId", 1L);
        breach.put("productId", 1L);
        breach.put("severity", "HIGH");
        breach.put("breachDate", "2024-01-15");

        when(reportService.getThresholdBreachesInRange(any(), any()))
                .thenReturn(Arrays.asList(breach));

        mockMvc.perform(get("/api/admin/inventory-reports/breaches")
                .with(user(adminDetails))
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].severity").value("HIGH"));
    }

    @Test
    @DisplayName("TC-REPORT-003: Get frequently low stock products")
    void testGetFrequentlyLowStockProducts() throws Exception {
        Map<String, Object> product = new HashMap<>();
        product.put("productId", 1L);
        product.put("productName", "Test Product");
        product.put("breachCount", 15);
        product.put("avgStockLevel", 3.5);

        when(reportService.getFrequentlyLowStockProducts(any(), any()))
                .thenReturn(Arrays.asList(product));

        mockMvc.perform(get("/api/admin/inventory-reports/frequent-problems")
                .with(user(adminDetails))
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].breachCount").value(15));
    }

    @Test
    @DisplayName("TC-REPORT-004: Get product inventory report")
    void testGetProductInventoryReport() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("productId", 1L);
        report.put("currentStock", 50);
        report.put("threshold", 10);
        report.put("averageConsumption", 5.5);
        report.put("daysUntilStockout", 9);

        when(reportService.getProductInventoryReport(anyLong()))
                .thenReturn(report);

        mockMvc.perform(get("/api/admin/inventory-reports/product/1")
                .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStock").value(50))
                .andExpect(jsonPath("$.data.daysUntilStockout").value(9));
    }

    @Test
    @DisplayName("TC-REPORT-005: Get inventory summary")
    void testGetInventorySummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", 100);
        summary.put("lowStockProducts", 15);
        summary.put("outOfStockProducts", 3);
        summary.put("averageStockLevel", 45.5);

        when(reportService.getInventorySummary())
                .thenReturn(summary);

        mockMvc.perform(get("/api/admin/inventory-reports/summary")
                .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalProducts").value(100))
                .andExpect(jsonPath("$.data.lowStockProducts").value(15));
    }

    @Test
    @DisplayName("TC-REPORT-006: Report endpoints require admin role")
    void testReportsRequireAdminRole() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                2L,
                "user",
                "user@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true,
                true,
                true,
                true);

        mockMvc.perform(get("/api/admin/inventory-reports/summary")
                .with(user(userDetails)))
                .andExpect(status().isForbidden());
    }
}

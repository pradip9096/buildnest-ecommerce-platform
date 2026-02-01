package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.inventory.InventoryAnalyticsService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test suite for AdminInventoryAnalyticsController.
 * Tests inventory analytics and demand correlation endpoints.
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@SuppressWarnings("null")
class AdminInventoryAnalyticsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private InventoryAnalyticsService analyticsService;

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
        @DisplayName("TC-ANALYTICS-001: Get high demand low inventory products")
        void testGetHighDemandLowInventoryProducts() throws Exception {
                Map<String, Object> product = new HashMap<>();
                product.put("productId", 1L);
                product.put("productName", "Test Product");
                product.put("currentStock", 5);
                product.put("demandScore", 95);

                when(analyticsService.getHighDemandLowInventoryProducts(any(), any()))
                                .thenReturn(Arrays.asList(product));

                mockMvc.perform(get("/api/admin/inventory-analytics/high-demand-low-inventory")
                                .with(user(adminDetails))
                                .param("startDate", "2024-01-01")
                                .param("endDate", "2024-01-31"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].productId").value(1));
        }

        @Test
        @DisplayName("TC-ANALYTICS-002: Get seasonal demand patterns")
        void testGetSeasonalDemandPatterns() throws Exception {
                Map<String, Object> pattern = new HashMap<>();
                pattern.put("season", "WINTER");
                pattern.put("averageDemand", 150);
                pattern.put("peakMonth", "DECEMBER");

                when(analyticsService.getSeasonalDemandPatterns(any(), any()))
                                .thenReturn(Arrays.asList(pattern));

                mockMvc.perform(get("/api/admin/inventory-analytics/seasonal-patterns")
                                .with(user(adminDetails))
                                .param("startDate", "2024-01-01")
                                .param("endDate", "2024-12-31"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].season").value("WINTER"));
        }

        @Test
        @DisplayName("TC-ANALYTICS-003: Get stock turnover analysis")
        void testGetStockTurnoverAnalysis() throws Exception {
                Map<String, Object> turnover = new HashMap<>();
                turnover.put("productId", 1L);
                turnover.put("turnoverRate", 8.5);
                turnover.put("averageDaysInStock", 42);

                when(analyticsService.getStockTurnoverAnalysis(any(), any()))
                                .thenReturn(Arrays.asList(turnover));

                mockMvc.perform(get("/api/admin/inventory-analytics/stock-turnover")
                                .with(user(adminDetails))
                                .param("startDate", "2024-01-01")
                                .param("endDate", "2024-03-31"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].turnoverRate").value(8.5));
        }

        @Test
        @DisplayName("TC-ANALYTICS-004: Get predictive restocking plan")
        void testGetPredictiveRestockingPlan() throws Exception {
                Map<String, Object> plan = new HashMap<>();
                plan.put("productId", 1L);
                plan.put("suggestedRestockQuantity", 200);
                plan.put("estimatedStockoutDate", "2024-02-15");
                plan.put("confidence", 0.85);

                when(analyticsService.getPredictiveRestockingPlan(any()))
                                .thenReturn(plan);

                mockMvc.perform(get("/api/admin/inventory-analytics/restocking-plan")
                                .with(user(adminDetails))
                                .param("daysPeriod", "30"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.confidence").value(0.85));
        }

        @Test
        @DisplayName("TC-ANALYTICS-005: Analytics endpoints require admin role")
        void testAnalyticsRequiresAdminRole() throws Exception {
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

                mockMvc.perform(get("/api/admin/inventory-analytics/high-demand-low-inventory")
                                .with(user(userDetails))
                                .param("startDate", "2024-01-01")
                                .param("endDate", "2024-01-31"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("TC-ANALYTICS-006: Handle invalid date range")
        void testInvalidDateRange() throws Exception {
                when(analyticsService.getSeasonalDemandPatterns(any(), any()))
                                .thenThrow(new IllegalArgumentException("Invalid date range"));

                mockMvc.perform(get("/api/admin/inventory-analytics/seasonal-patterns")
                                .with(user(adminDetails))
                                .param("startDate", "2024-12-31")
                                .param("endDate", "2024-01-01"))
                                .andExpect(status().isBadRequest());
        }
}

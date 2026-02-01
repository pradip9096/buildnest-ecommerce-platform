package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test suite for AdminInventoryController.
 * Tests admin-specific inventory management endpoints.
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class })
@SuppressWarnings("null")
class AdminInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    private CustomUserDetails adminDetails;
    private CustomUserDetails userDetails;

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

        userDetails = new CustomUserDetails(
                2L,
                "user",
                "user@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true,
                true,
                true,
                true);
    }

    @Test
    @DisplayName("TC-ADMIN-INV-001: Admin can view inventory for product")
    void testGetLowStockProducts() throws Exception {
        Inventory inv1 = new Inventory();
        inv1.setId(1L);
        inv1.setQuantityInStock(5);

        when(inventoryService.getInventoryByProductId(anyLong())).thenReturn(inv1);

        // TestSecurityConfig enables authentication, admin user can access
        mockMvc.perform(get("/api/admin/inventory/product/1")
                .with(user(adminDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-002: Admin can view inventory for another product")
    void testGetOutOfStockProducts() throws Exception {
        Inventory inv1 = new Inventory();
        inv1.setId(1L);
        inv1.setQuantityInStock(0);

        when(inventoryService.getInventoryByProductId(anyLong())).thenReturn(inv1);

        // TestSecurityConfig enables authentication, admin user can access
        mockMvc.perform(get("/api/admin/inventory/product/2")
                .with(user(adminDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-003: Admin can add stock to product")
    void testAddStockToProduct() throws Exception {
        Inventory updatedInventory = new Inventory();
        updatedInventory.setId(1L);
        updatedInventory.setQuantityInStock(100);

        when(inventoryService.addStock(anyLong(), anyInt())).thenReturn(updatedInventory);

        // TestSecurityConfig enables authentication, admin user can access
        mockMvc.perform(post("/api/admin/inventory/add-stock/1")
                .with(user(adminDetails))
                .param("quantity", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-007: Add stock returns bad request on error")
    void testAddStockError() throws Exception {
        doThrow(new RuntimeException("bad")).when(inventoryService).addStock(eq(1L), eq(50));

        mockMvc.perform(post("/api/admin/inventory/add-stock/1")
                .with(user(adminDetails))
                .param("quantity", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-008: Update stock works and errors")
    void testUpdateStock() throws Exception {
        Inventory updatedInventory = new Inventory();
        updatedInventory.setId(1L);
        updatedInventory.setQuantityInStock(20);

        when(inventoryService.updateStock(anyLong(), anyInt())).thenReturn(updatedInventory);

        mockMvc.perform(post("/api/admin/inventory/update-stock/1")
                .with(user(adminDetails))
                .param("quantity", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        doThrow(new RuntimeException("bad")).when(inventoryService).updateStock(eq(1L), eq(20));

        mockMvc.perform(post("/api/admin/inventory/update-stock/1")
                .with(user(adminDetails))
                .param("quantity", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-004: Regular user cannot access admin inventory endpoints")
    void testUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/inventory/low-stock")
                .with(user(userDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-005: Check product availability returns correct status")
    void testCheckProductAvailability() throws Exception {
        when(inventoryService.hasStock(anyLong(), anyInt())).thenReturn(true);

        // TestSecurityConfig enables authentication, admin user can access
        mockMvc.perform(get("/api/admin/inventory/check-availability/1")
                .with(user(adminDetails))
                .param("quantity", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-011: Check product availability handles unavailable")
    void testCheckProductAvailabilityUnavailable() throws Exception {
        when(inventoryService.hasStock(anyLong(), anyInt())).thenReturn(false);

        mockMvc.perform(get("/api/admin/inventory/check-availability/2")
                .with(user(adminDetails))
                .param("quantity", "99"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-009: Check availability handles errors")
    void testCheckAvailabilityError() throws Exception {
        doThrow(new RuntimeException("bad")).when(inventoryService).hasStock(eq(1L), eq(10));

        mockMvc.perform(get("/api/admin/inventory/check-availability/1")
                .with(user(adminDetails))
                .param("quantity", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-006: Get inventory for product")
    void testGetInventoryStatus() throws Exception {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setQuantityInStock(50);

        when(inventoryService.getInventoryByProductId(anyLong())).thenReturn(inv);

        // TestSecurityConfig enables authentication, admin user can access
        mockMvc.perform(get("/api/admin/inventory/product/1")
                .with(user(adminDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-ADMIN-INV-010: Get inventory returns not found on error")
    void testGetInventoryNotFound() throws Exception {
        doThrow(new RuntimeException("missing")).when(inventoryService).getInventoryByProductId(eq(99L));

        mockMvc.perform(get("/api/admin/inventory/product/99")
                .with(user(adminDetails)))
                .andExpect(status().isNotFound());
    }
}
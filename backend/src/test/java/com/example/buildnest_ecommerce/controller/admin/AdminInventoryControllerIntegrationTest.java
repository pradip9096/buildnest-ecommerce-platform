package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.InventoryStatus;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryAuditLogRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminInventoryController (SRS ADM-06, RTM ADM-06, #72).
 *
 * <p>Verifies: list, delta-adjust, audit-trail recording, and role enforcement —
 * all against a real H2 database via the full Spring MVC dispatch stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@Transactional
class AdminInventoryControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/inventory";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired InventoryRepository inventoryRepository;
    @Autowired InventoryAuditLogRepository auditLogRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean RateLimitUtil rateLimitUtil;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString(), anyLong())).thenReturn(true);

        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_ADMIN".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role(); r.setName("ROLE_ADMIN"); return roleRepository.save(r);
                });
        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role(); r.setName("ROLE_USER"); return roleRepository.save(r);
                });

        User admin = new User();
        admin.setUsername("invadmin_" + System.nanoTime());
        admin.setEmail("invadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Inv"); admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole)); admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        User regularUser = new User();
        regularUser.setUsername("invuser_" + System.nanoTime());
        regularUser.setEmail("invuser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Reg"); regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole)); regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());
    }

    private Inventory savedInventory(int quantity, int minLevel) {
        Category cat = new Category();
        cat.setName("InvTestCat_" + System.nanoTime());
        cat.setIsActive(true);
        cat = categoryRepository.save(cat);

        Product product = new Product();
        product.setName("InvProduct_" + System.nanoTime());
        product.setPrice(new BigDecimal("10.00"));
        product.setCategory(cat);
        product.setIsActive(true);
        product = productRepository.save(product);

        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setQuantityInStock(quantity);
        inv.setQuantityReserved(0);
        inv.setMinimumStockLevel(minLevel);
        inv.setStatus(quantity > minLevel ? InventoryStatus.IN_STOCK : InventoryStatus.LOW_STOCK);
        return inventoryRepository.save(inv);
    }

    // ─── LIST ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-06-001: GET /inventory — admin → 200 with list")
    void listInventory_asAdmin_returns200() throws Exception {
        savedInventory(100, 10);

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("TC-ADM-06-002: GET /inventory — USER role → 403")
    void listInventory_asUser_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ─── ADJUST ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-06-003: PATCH /{productId} positive delta → 200, stock increases")
    void adjustInventory_positiveDelta_returns200() throws Exception {
        Inventory inv = savedInventory(50, 10);
        Long productId = inv.getProduct().getId();

        mockMvc.perform(patch(BASE_URL + "/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta": 10, "reason": "Restocked from supplier"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        Inventory updated = inventoryRepository.findByProduct(inv.getProduct()).orElseThrow();
        assertEquals(60, updated.getQuantityInStock());
    }

    @Test
    @DisplayName("TC-ADM-06-004: PATCH /{productId} negative delta → 200, stock decreases")
    void adjustInventory_negativeDelta_returns200() throws Exception {
        Inventory inv = savedInventory(50, 5);
        Long productId = inv.getProduct().getId();

        mockMvc.perform(patch(BASE_URL + "/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta": -20, "reason": "Damaged goods removal"}
                                """))
                .andExpect(status().isOk());

        Inventory updated = inventoryRepository.findByProduct(inv.getProduct()).orElseThrow();
        assertEquals(30, updated.getQuantityInStock());
    }

    @Test
    @DisplayName("TC-ADM-06-005: PATCH /{productId} delta that takes stock below zero → 400")
    void adjustInventory_wouldGoBelowZero_returns400() throws Exception {
        Inventory inv = savedInventory(10, 5);
        Long productId = inv.getProduct().getId();

        mockMvc.perform(patch(BASE_URL + "/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta": -999, "reason": "Too large a removal"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-06-006: PATCH /{productId} missing reason → 400 (validation)")
    void adjustInventory_missingReason_returns400() throws Exception {
        Inventory inv = savedInventory(50, 10);
        Long productId = inv.getProduct().getId();

        mockMvc.perform(patch(BASE_URL + "/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta": 5}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-06-007: PATCH non-existent product → 404")
    void adjustInventory_productNotFound_returns404() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/99999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta": 5, "reason": "Test"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-ADM-06-008: PATCH /{productId} — USER role → 403")
    void adjustInventory_asUser_returns403() throws Exception {
        Inventory inv = savedInventory(50, 10);
        Long productId = inv.getProduct().getId();

        mockMvc.perform(patch(BASE_URL + "/" + productId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta": 5, "reason": "Should be blocked"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADM-06-009: After PATCH, InventoryAuditLog entry is recorded")
    void adjustInventory_recordsAuditLog() throws Exception {
        Inventory inv = savedInventory(100, 10);
        Long productId = inv.getProduct().getId();
        long auditCountBefore = auditLogRepository.findByInventoryIdOrderByCreatedAtDesc(inv.getId()).size();

        mockMvc.perform(patch(BASE_URL + "/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta": 25, "reason": "Quarterly restock"}
                                """))
                .andExpect(status().isOk());

        var logs = auditLogRepository.findByInventoryIdOrderByCreatedAtDesc(inv.getId());
        assertFalse(logs.isEmpty(), "Expected at least one audit log entry");
        assertEquals(auditCountBefore + 1, logs.size());
        assertEquals("ADJUSTMENT", logs.get(0).getChangeType());
        assertEquals(25, logs.get(0).getQuantityChange());
        assertEquals(100, logs.get(0).getQuantityBefore());
        assertEquals(125, logs.get(0).getQuantityAfter());
    }
}

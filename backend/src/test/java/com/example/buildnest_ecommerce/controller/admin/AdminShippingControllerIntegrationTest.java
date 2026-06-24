package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.*;
import com.example.buildnest_ecommerce.repository.*;
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
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminShippingController (SHIP-01, #87).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@Transactional
@SuppressWarnings("removal")
class AdminShippingControllerIntegrationTest {

    private static final String BASE = "/api/v1/admin/shipping-methods";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ShippingMethodRepository shippingMethodRepository;
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
                .filter(r -> "ROLE_ADMIN".equals(r.getName())).findFirst()
                .orElseGet(() -> { Role r = new Role(); r.setName("ROLE_ADMIN"); return roleRepository.save(r); });
        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName())).findFirst()
                .orElseGet(() -> { Role r = new Role(); r.setName("ROLE_USER"); return roleRepository.save(r); });

        User admin = new User();
        admin.setUsername("shipadmin_" + System.nanoTime());
        admin.setEmail("shipadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Ship"); admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole)); admin.setIsActive(true);
        adminToken = jwtTokenProvider.generateTokenFromUsername(userRepository.save(admin).getUsername());

        User user = new User();
        user.setUsername("shipuser_" + System.nanoTime());
        user.setEmail("shipuser_" + System.nanoTime() + "@test.com");
        user.setPassword(passwordEncoder.encode("User@1234!"));
        user.setFirstName("Ship"); user.setLastName("User");
        user.setRoles(Set.of(userRole)); user.setIsActive(true);
        userToken = jwtTokenProvider.generateTokenFromUsername(userRepository.save(user).getUsername());
    }

    private ShippingMethod savedMethod() {
        ShippingMethod m = new ShippingMethod();
        m.setName("Standard_" + System.nanoTime());
        m.setDescription("3-5 days");
        m.setBaseCost(new BigDecimal("50.00"));
        m.setCostPerKg(new BigDecimal("10.00"));
        m.setEstimatedDaysMin(3);
        m.setEstimatedDaysMax(5);
        m.setIsActive(true);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return shippingMethodRepository.save(m);
    }

    // ─── GET / ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-ADM-001: GET /shipping-methods — ADMIN → 200 list")
    void listAll_asAdmin_returns200() throws Exception {
        savedMethod();
        mockMvc.perform(get(BASE).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("TC-SHIP-ADM-002: GET /shipping-methods — USER → 403")
    void listAll_asUser_returns403() throws Exception {
        mockMvc.perform(get(BASE).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ─── POST / ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-ADM-003: POST /shipping-methods — valid payload → 201 created")
    void create_validPayload_returns201() throws Exception {
        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Express",
                                  "description": "1-2 days",
                                  "baseCost": 120.00,
                                  "costPerKg": 15.00,
                                  "estimatedDaysMin": 1,
                                  "estimatedDaysMax": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Express")))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @DisplayName("TC-SHIP-ADM-004: POST /shipping-methods — missing name → 400")
    void create_missingName_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCost": 50.00, "estimatedDaysMin": 3, "estimatedDaysMax": 5}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-SHIP-ADM-005: POST /shipping-methods — USER → 403")
    void create_asUser_returns403() throws Exception {
        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"x","baseCost":10,"estimatedDaysMin":1,"estimatedDaysMax":2}
                                """))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /{id} ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-ADM-006: PUT /shipping-methods/{id} — valid → 200 with updated fields")
    void update_valid_returns200() throws Exception {
        ShippingMethod method = savedMethod();

        mockMvc.perform(put(BASE + "/" + method.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Standard Updated",
                                  "baseCost": 55.00,
                                  "costPerKg": 12.00,
                                  "estimatedDaysMin": 2,
                                  "estimatedDaysMax": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Standard Updated")));
    }

    @Test
    @DisplayName("TC-SHIP-ADM-007: PUT /shipping-methods/{id} — non-existent → 404")
    void update_notFound_returns404() throws Exception {
        mockMvc.perform(put(BASE + "/99999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"x","baseCost":10,"estimatedDaysMin":1,"estimatedDaysMax":2}
                                """))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /{id} ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-SHIP-ADM-008: DELETE /shipping-methods/{id} — soft deletes (isActive=false)")
    void deactivate_setsInactive() throws Exception {
        ShippingMethod method = savedMethod();

        mockMvc.perform(delete(BASE + "/" + method.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        ShippingMethod updated = shippingMethodRepository.findById(method.getId()).orElseThrow();
        assertFalse(updated.getIsActive(), "isActive should be false after deactivation");
    }

    @Test
    @DisplayName("TC-SHIP-ADM-009: DELETE /shipping-methods/{id} — non-existent → 404")
    void deactivate_notFound_returns404() throws Exception {
        mockMvc.perform(delete(BASE + "/99999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}

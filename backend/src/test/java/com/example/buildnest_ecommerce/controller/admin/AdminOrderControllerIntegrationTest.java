package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.service.notification.INotificationService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminOrderController (SRS ADM-03, RTM ADM-03, #69).
 *
 * <p>Verifies: list/filter, detail, status transitions, role enforcement —
 * all against a real H2 database via the full Spring MVC dispatch stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class, com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig.class})
@Transactional
class AdminOrderControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/orders";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean INotificationService notificationService;
    @MockitoBean RateLimitUtil rateLimitUtil;

    private String adminToken;
    private String userToken;
    private User adminUser;

    @BeforeEach
    void setUp() {
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString(), anyLong())).thenReturn(true);

        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_ADMIN".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepository.save(r);
                });

        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });

        adminUser = new User();
        adminUser.setUsername("ordadmin_" + System.nanoTime());
        adminUser.setEmail("ordadmin_" + System.nanoTime() + "@test.com");
        adminUser.setPassword(passwordEncoder.encode("Admin@1234!"));
        adminUser.setFirstName("Order");
        adminUser.setLastName("Admin");
        adminUser.setRoles(Set.of(adminRole));
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateTokenFromUsername(adminUser.getUsername());

        User regularUser = new User();
        regularUser.setUsername("orduser_" + System.nanoTime());
        regularUser.setEmail("orduser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());
    }

    private Order savedOrder(Order.OrderStatus status) {
        Order order = new Order();
        order.setUser(adminUser);
        order.setOrderNumber("ORD-" + System.nanoTime());
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("999.99"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setShippingAmount(BigDecimal.ZERO);
        order.setIsDeleted(false);
        return orderRepository.save(order);
    }

    // ─── LIST / FILTER ────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-03-001: GET /orders — admin, no filter → 200 with page structure")
    void listOrders_asAdmin_returns200() throws Exception {
        savedOrder(Order.OrderStatus.PENDING);

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("TC-ADM-03-002: GET /orders?status=PENDING — filters by status")
    void listOrders_withStatusFilter_returnsFiltered() throws Exception {
        savedOrder(Order.OrderStatus.PENDING);
        savedOrder(Order.OrderStatus.CONFIRMED);

        mockMvc.perform(get(BASE_URL + "?status=PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("TC-ADM-03-003: GET /orders?status=INVALID — returns 400")
    void listOrders_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "?status=NONSENSE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-03-004: GET /orders — USER role → 403")
    void listOrders_asUser_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADM-03-005: GET /orders — no token → 401")
    void listOrders_noToken_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    // ─── DETAIL ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-03-006: GET /orders/{id} — found → 200 with detail fields")
    void getOrderDetail_found_returns200() throws Exception {
        Order order = savedOrder(Order.OrderStatus.PENDING);

        mockMvc.perform(get(BASE_URL + "/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(order.getId().intValue())))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.userId", is(adminUser.getId().intValue())));
    }

    @Test
    @DisplayName("TC-ADM-03-007: GET /orders/{id} — not found → 404")
    void getOrderDetail_notFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ─── STATUS UPDATE ────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-03-008: PATCH /orders/{id}/status — PENDING→CONFIRMED → 200")
    void updateStatus_pendingToConfirmed_returns200() throws Exception {
        Order order = savedOrder(Order.OrderStatus.PENDING);

        mockMvc.perform(patch(BASE_URL + "/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CONFIRMED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is("CONFIRMED")));
    }

    @Test
    @DisplayName("TC-ADM-03-009: PATCH /orders/{id}/status — invalid backward transition → 400")
    void updateStatus_backwardTransition_returns400() throws Exception {
        Order order = savedOrder(Order.OrderStatus.DELIVERED);

        mockMvc.perform(patch(BASE_URL + "/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PENDING"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-03-010: PATCH /orders/{id}/status — cancel with reason → 200")
    void updateStatus_cancelWithReason_returns200() throws Exception {
        Order order = savedOrder(Order.OrderStatus.PENDING);

        mockMvc.perform(patch(BASE_URL + "/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CANCELLED", "cancellationReason": "Customer requested cancellation"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is("CANCELLED")));
    }

    @Test
    @DisplayName("TC-ADM-03-011: PATCH /orders/{id}/status — USER role → 403")
    void updateStatus_asUser_returns403() throws Exception {
        Order order = savedOrder(Order.OrderStatus.PENDING);

        mockMvc.perform(patch(BASE_URL + "/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CONFIRMED"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADM-03-012: PATCH /orders/{id}/status — unknown status string → 400")
    void updateStatus_unknownStatus_returns400() throws Exception {
        Order order = savedOrder(Order.OrderStatus.PENDING);

        mockMvc.perform(patch(BASE_URL + "/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "FLYING"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-03-013: PATCH /orders/{id}/status — missing status body → 400")
    void updateStatus_missingStatus_returns400() throws Exception {
        Order order = savedOrder(Order.OrderStatus.PENDING);

        mockMvc.perform(patch(BASE_URL + "/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cancellationReason": "no status field"}
                                """))
                .andExpect(status().isBadRequest());
    }
}

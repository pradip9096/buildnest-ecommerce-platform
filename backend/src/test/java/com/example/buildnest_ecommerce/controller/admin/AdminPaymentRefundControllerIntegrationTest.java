package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.integration.RazorpayClientAdapter;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.PaymentRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AdminOrderController#refundPayment} (PAY-02, #61).
 *
 * <p>RazorpayClientAdapter is @MockBean so no gateway calls are made.
 * All other wiring (PaymentServiceImpl, H2 DB) is real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@Transactional
@DisplayName("AdminOrderController — POST /{id}/refund integration tests")
class AdminPaymentRefundControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/orders";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean RazorpayClientAdapter razorpayAdapter;
    @MockBean INotificationService notificationService;
    @MockBean RateLimitUtil rateLimitUtil;

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
                .orElseGet(() -> { Role r = new Role(); r.setName("ROLE_ADMIN"); return roleRepository.save(r); });

        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> { Role r = new Role(); r.setName("ROLE_USER"); return roleRepository.save(r); });

        adminUser = new User();
        adminUser.setUsername("refadmin_" + System.nanoTime());
        adminUser.setEmail("refadmin_" + System.nanoTime() + "@test.com");
        adminUser.setPassword(passwordEncoder.encode("Admin@1234!"));
        adminUser.setFirstName("Ref");
        adminUser.setLastName("Admin");
        adminUser.setRoles(Set.of(adminRole));
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateTokenFromUsername(adminUser.getUsername());

        User regularUser = new User();
        regularUser.setUsername("refuser_" + System.nanoTime());
        regularUser.setEmail("refuser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());
    }

    private Order savedOrder() {
        Order order = new Order();
        order.setUser(adminUser);
        order.setOrderNumber("ORD-REF-" + System.nanoTime());
        order.setStatus(Order.OrderStatus.PAID);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setShippingAmount(BigDecimal.ZERO);
        order.setIsDeleted(false);
        return orderRepository.save(order);
    }

    private Payment savedPayment(Long orderId, double amount, String status) {
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setAmount(amount);
        p.setRefundedAmount(0.0);
        p.setRazorpayOrderId("rzp_order_" + orderId);
        p.setRazorpayPaymentId("pay_rzp_" + orderId);
        p.setStatus(status);
        p.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(p);
    }

    @Test
    @DisplayName("TC-ADM-PAY-001: full refund — admin, SUCCESS payment → 200, status REFUNDED")
    void fullRefund_asAdmin_returns200() throws Exception {
        Order order = savedOrder();
        savedPayment(order.getId(), 1000.0, "SUCCESS");
        doNothing().when(razorpayAdapter).refundPayment(anyString(), anyDouble());

        mockMvc.perform(post(BASE_URL + "/" + order.getId() + "/refund")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000.00, \"reason\": \"Customer request\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("REFUNDED")))
                .andExpect(jsonPath("$.data.refundedAmount", is(1000.0)));
    }

    @Test
    @DisplayName("TC-ADM-PAY-002: partial refund — status PARTIALLY_REFUNDED")
    void partialRefund_returnsPartiallyRefunded() throws Exception {
        Order order = savedOrder();
        savedPayment(order.getId(), 1000.0, "SUCCESS");
        doNothing().when(razorpayAdapter).refundPayment(anyString(), anyDouble());

        mockMvc.perform(post(BASE_URL + "/" + order.getId() + "/refund")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 400.0, \"reason\": \"Partial return\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PARTIALLY_REFUNDED")))
                .andExpect(jsonPath("$.data.refundedAmount", is(400.0)));
    }

    @Test
    @DisplayName("TC-ADM-PAY-003: amount exceeds original payment → 400")
    void refundExceedsAmount_returns400() throws Exception {
        Order order = savedOrder();
        savedPayment(order.getId(), 500.0, "SUCCESS");

        mockMvc.perform(post(BASE_URL + "/" + order.getId() + "/refund")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 999.0, \"reason\": \"too much\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-PAY-004: payment not in SUCCESS status → 400")
    void nonSuccessPayment_returns400() throws Exception {
        Order order = savedOrder();
        savedPayment(order.getId(), 500.0, "PENDING");

        mockMvc.perform(post(BASE_URL + "/" + order.getId() + "/refund")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 200.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-PAY-005: missing amount field → 400 validation error")
    void missingAmount_returns400() throws Exception {
        Order order = savedOrder();

        mockMvc.perform(post(BASE_URL + "/" + order.getId() + "/refund")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"no amount\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-PAY-006: no payment record for order → 404")
    void noPaymentForOrder_returns404() throws Exception {
        Order order = savedOrder();

        mockMvc.perform(post(BASE_URL + "/" + order.getId() + "/refund")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-ADM-PAY-007: non-admin user → 403")
    void regularUser_returns403() throws Exception {
        Order order = savedOrder();
        savedPayment(order.getId(), 500.0, "SUCCESS");

        mockMvc.perform(post(BASE_URL + "/" + order.getId() + "/refund")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADM-PAY-008: unauthenticated request → 401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.0}"))
                .andExpect(status().isUnauthorized());
    }
}

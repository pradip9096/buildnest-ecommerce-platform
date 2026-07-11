package com.example.buildnest_ecommerce.payment;

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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the full payment webhook flow (PAY-01, #98).
 *
 * <p>Exercises the real {@code PaymentServiceImpl} and {@code PaymentSignatureValidationService}
 * against H2. Only {@code RazorpayClientAdapter} and {@code INotificationService} are mocked
 * so no external gateway calls are made.
 *
 * <p>Covers: payment.captured → Payment=SUCCESS + Order=PAID,
 * payment.failed → Payment=FAILED + Order=PAYMENT_FAILED,
 * invalid HMAC → 401 + no DB change, idempotency, and unknown order graceful ignore.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestElasticsearchConfig.class, com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig.class})
@Transactional
@DisplayName("Payment flow integration tests (PAY-01, #98)")
class PaymentIntegrationTest {

    private static final String WEBHOOK_URL = "/api/v1/webhooks/payment";
    private static final String WEBHOOK_SECRET = "test_webhook_secret";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean RazorpayClientAdapter razorpayAdapter;
    @MockitoBean INotificationService notificationService;
    @MockitoBean RateLimitUtil rateLimitUtil;

    @BeforeEach
    void setUp() {
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString(), anyLong())).thenReturn(true);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Order savedOrder() {
        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> { Role r = new Role(); r.setName("ROLE_USER"); return roleRepository.save(r); });

        User user = new User();
        user.setUsername("payint_" + System.nanoTime());
        user.setEmail("payint_" + System.nanoTime() + "@test.com");
        user.setPassword(passwordEncoder.encode("Pass@1234!"));
        user.setFirstName("Pay"); user.setLastName("Int");
        user.setRoles(Set.of(userRole)); user.setIsActive(true);
        user = userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-PAY-INT-" + System.nanoTime());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setShippingAmount(BigDecimal.ZERO);
        order.setIsDeleted(false);
        return orderRepository.save(order);
    }

    private Payment savedPendingPayment(Long orderId, String razorpayOrderId) {
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setAmount(500.0);
        p.setRefundedAmount(0.0);
        p.setRazorpayOrderId(razorpayOrderId);
        p.setStatus("PENDING");
        p.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(p);
    }

    private String webhookBody(String event, String razorpayOrderId, String razorpayPaymentId) {
        return String.format(
                "{\"event\":\"%s\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"%s\",\"order_id\":\"%s\"}}}}",
                event, razorpayPaymentId, razorpayOrderId);
    }

    private String computeHmac(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) { hex.append(String.format("%02x", b)); }
        return hex.toString();
    }

    // ─── Tests ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-PAY-INT-001: payment.captured with valid HMAC → Payment=SUCCESS, Order=PAID")
    void capturedEvent_updatesPaymentAndOrder() throws Exception {
        Order order = savedOrder();
        String rzpOrderId = "rzp_order_captured_" + System.nanoTime();
        Payment payment = savedPendingPayment(order.getId(), rzpOrderId);

        String body = webhookBody("payment.captured", rzpOrderId, "pay_captured_001");
        String sig = computeHmac(body);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Razorpay-Signature", sig))
                .andExpect(status().isOk());

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals("SUCCESS", updated.getStatus());
        assertEquals("pay_captured_001", updated.getRazorpayPaymentId());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(Order.OrderStatus.PAID, updatedOrder.getStatus());
    }

    @Test
    @DisplayName("TC-PAY-INT-002: payment.failed with valid HMAC → Payment=FAILED, Order=PAYMENT_FAILED")
    void failedEvent_updatesPaymentAndOrder() throws Exception {
        Order order = savedOrder();
        String rzpOrderId = "rzp_order_failed_" + System.nanoTime();
        Payment payment = savedPendingPayment(order.getId(), rzpOrderId);

        String body = webhookBody("payment.failed", rzpOrderId, "pay_failed_001");
        String sig = computeHmac(body);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Razorpay-Signature", sig))
                .andExpect(status().isOk());

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals("FAILED", updated.getStatus());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(Order.OrderStatus.PAYMENT_FAILED, updatedOrder.getStatus());
    }

    @Test
    @DisplayName("TC-PAY-INT-003: invalid HMAC → 401, DB state unchanged")
    void invalidSignature_returns401_noDbChange() throws Exception {
        Order order = savedOrder();
        String rzpOrderId = "rzp_order_badsig_" + System.nanoTime();
        Payment payment = savedPendingPayment(order.getId(), rzpOrderId);

        String body = webhookBody("payment.captured", rzpOrderId, "pay_bad_001");

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Razorpay-Signature", "0".repeat(64)))
                .andExpect(status().isUnauthorized());

        Payment unchanged = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals("PENDING", unchanged.getStatus());

        Order unchangedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(Order.OrderStatus.PENDING, unchangedOrder.getStatus());
    }

    @Test
    @DisplayName("TC-PAY-INT-004: duplicate payment.captured for already-SUCCESS payment → 200, idempotent")
    void capturedEvent_idempotent_alreadySuccess() throws Exception {
        Order order = savedOrder();
        String rzpOrderId = "rzp_order_idem_" + System.nanoTime();
        Payment payment = savedPendingPayment(order.getId(), rzpOrderId);
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        String body = webhookBody("payment.captured", rzpOrderId, "pay_idem_001");
        String sig = computeHmac(body);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Razorpay-Signature", sig))
                .andExpect(status().isOk());

        Payment unchanged = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals("SUCCESS", unchanged.getStatus());

        Order unchangedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(Order.OrderStatus.PENDING, unchangedOrder.getStatus());
    }

    @Test
    @DisplayName("TC-PAY-INT-005: unknown razorpayOrderId → 200, gracefully ignored")
    void unknownOrderId_ignoredGracefully() throws Exception {
        String body = webhookBody("payment.captured", "rzp_order_unknown_xyz", "pay_unknown_001");
        String sig = computeHmac(body);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Razorpay-Signature", sig))
                .andExpect(status().isOk());
    }
}

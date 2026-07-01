package com.example.buildnest_ecommerce.service.notification;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.model.entity.User;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl")
class NotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(mailSender, templateEngine);
        ReflectionTestUtils.setField(service, "fromAddress", "noreply@buildnest.com");
        ReflectionTestUtils.setField(service, "fromName", "BuildNest");
        ReflectionTestUtils.setField(service, "baseUrl", "https://buildnest.com");
    }

    private MimeMessage realMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    // ── sendEmail ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sendEmail constructs and sends a MIME message")
    void sendEmail_validArgs_sendsMimeMessage() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);

        service.sendEmail("user@example.com", "Hello", "<p>Hi</p>");

        verify(mailSender).send(msg);
    }

    // ── sendOrderConfirmation ────────────────────────────────────────────────

    @Test
    @DisplayName("sendOrderConfirmation sends email when order has a user with email")
    void sendOrderConfirmation_withUser_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>order</html>");

        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@example.com");

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-001");
        order.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        order.setTotalAmount(new BigDecimal("149.99"));

        service.sendOrderConfirmation(order);

        verify(mailSender).send(msg);
        verify(templateEngine).process(eq("email/order-confirmation"), any());
    }

    @Test
    @DisplayName("sendOrderConfirmation skips send when user has null email")
    void sendOrderConfirmation_nullEmail_skipsEmail() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail(null);

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-002");

        service.sendOrderConfirmation(order);

        verify(mailSender, never()).createMimeMessage();
        verify(templateEngine, never()).process(anyString(), any());
    }

    @Test
    @DisplayName("sendOrderConfirmation skips send when order has no user")
    void sendOrderConfirmation_nullUser_skipsEmail() {
        Order order = new Order();
        order.setUser(null);
        order.setOrderNumber("ORD-003");

        service.sendOrderConfirmation(order);

        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    @DisplayName("sendOrderConfirmation handles null createdAt and null shippingAddress")
    void sendOrderConfirmation_nullOptionalFields_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>order</html>");

        User user = new User();
        user.setFirstName("Bob");
        user.setLastName("Smith");
        user.setEmail("bob@example.com");

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-004");
        order.setCreatedAt(null);
        order.setShippingAddress(null);

        service.sendOrderConfirmation(order);

        verify(mailSender).send(msg);
    }

    @Test
    @DisplayName("sendOrderConfirmation includes shippingAddress.toString() when address is present")
    void sendOrderConfirmation_withShippingAddress_includesAddressInContext() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>order</html>");

        User user = new User();
        user.setFirstName("Carol");
        user.setLastName("White");
        user.setEmail("carol@example.com");

        com.example.buildnest_ecommerce.model.entity.Address address =
                new com.example.buildnest_ecommerce.model.entity.Address();
        address.setStreetAddress("10 Main St");
        address.setCity("Springfield");

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-005");
        order.setCreatedAt(LocalDateTime.of(2026, 3, 1, 8, 0));
        order.setShippingAddress(address);

        service.sendOrderConfirmation(order);

        verify(mailSender).send(msg);
    }

    // ── sendPaymentReceipt ───────────────────────────────────────────────────

    @Test
    @DisplayName("sendPaymentReceipt logs and does not throw")
    void sendPaymentReceipt_logsAndReturns() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(99.00);

        assertDoesNotThrow(() -> service.sendPaymentReceipt(payment));
        verify(mailSender, never()).createMimeMessage();
    }

    // ── sendShipmentNotification ─────────────────────────────────────────────

    @Test
    @DisplayName("sendShipmentNotification(Long, String) logs and does not throw")
    void sendShipmentNotification_orderId_logsAndReturns() {
        assertDoesNotThrow(() -> service.sendShipmentNotification(42L, "TRACK-001"));
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    @DisplayName("sendShipmentNotification(5-arg) renders template and sends email")
    void sendShipmentNotification_fullArgs_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>ship</html>");

        service.sendShipmentNotification(
                "customer@example.com", "Alice", "ORD-010", "TRACK-999", "3-5 days");

        verify(mailSender).send(msg);
        verify(templateEngine).process(eq("email/shipping-update"), any());
    }

    // ── sendDeliveryNotification ─────────────────────────────────────────────

    @Test
    @DisplayName("sendDeliveryNotification logs and does not throw")
    void sendDeliveryNotification_logsAndReturns() {
        assertDoesNotThrow(() -> service.sendDeliveryNotification(77L));
        verify(mailSender, never()).createMimeMessage();
    }

    // ── sendLowStockAlert ────────────────────────────────────────────────────

    @Test
    @DisplayName("sendLowStockAlert logs and does not throw")
    void sendLowStockAlert_logsAndReturns() {
        assertDoesNotThrow(() -> service.sendLowStockAlert(5L, 3));
        verify(mailSender, never()).createMimeMessage();
    }

    // ── sendPasswordResetEmail ───────────────────────────────────────────────

    @Test
    @DisplayName("sendPasswordResetEmail renders password-reset template and sends email")
    void sendPasswordResetEmail_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>reset</html>");

        service.sendPasswordResetEmail("user@example.com", "reset-token-abc");

        verify(mailSender).send(msg);
        verify(templateEngine).process(eq("email/password-reset"), any());
    }

    // ── sendVerificationEmail ────────────────────────────────────────────────

    @Test
    @DisplayName("sendVerificationEmail renders registration-welcome template and sends email")
    void sendVerificationEmail_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>welcome</html>");

        service.sendVerificationEmail("new@example.com", "verify-token-xyz");

        verify(mailSender).send(msg);
        verify(templateEngine).process(eq("email/registration-welcome"), any());
    }

    // ── send() error path ────────────────────────────────────────────────────

    @Test
    @DisplayName("send() wraps MessagingException in MailException when message construction fails")
    void send_messagingException_throwsMailException() throws Exception {
        MimeMessage brokenMsg = mock(MimeMessage.class);
        doThrow(new MessagingException("SMTP error")).when(brokenMsg).setFrom(any(Address.class));
        when(mailSender.createMimeMessage()).thenReturn(brokenMsg);

        assertThrows(MailException.class,
                () -> service.sendEmail("user@example.com", "Subject", "<p>body</p>"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("sendEmail constructs and sends a MIME message with correct recipient, subject, and body")
    void sendEmail_validArgs_sendsMimeMessage() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);

        service.sendEmail("user@example.com", "Hello", "<p>Hi</p>");

        verify(mailSender).send(msg);
        // Verify setTo, setSubject, setText were not mutated away
        Address[] recipients = msg.getAllRecipients();
        assertNotNull(recipients, "setTo must populate recipients");
        assertEquals(1, recipients.length);
        assertTrue(recipients[0].toString().contains("user@example.com"),
                "recipient must be user@example.com");
        assertEquals("Hello", msg.getSubject(), "setSubject must set subject");
        assertNotNull(msg.getContent(), "setText must set message content");
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
        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/order-confirmation"), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertNotNull(ctx.getVariable("customerName"), "customerName must be set in context");
        assertNotNull(ctx.getVariable("orderNumber"), "orderNumber must be set in context");
        assertNotNull(ctx.getVariable("orderDate"), "orderDate must be set in context");
        assertNotNull(ctx.getVariable("totalAmount"), "totalAmount must be set in context");
        assertNotNull(ctx.getVariable("shippingAddress"), "shippingAddress must be set in context");
        assertEquals("Jane Doe", ctx.getVariable("customerName"));
        assertEquals("ORD-001", ctx.getVariable("orderNumber"));
        assertEquals(new BigDecimal("149.99"), ctx.getVariable("totalAmount"));
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
        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/order-confirmation"), ctxCaptor.capture());
        // Null date and address fall back to "—" sentinel
        assertEquals("—", ctxCaptor.getValue().getVariable("orderDate"));
        assertEquals("—", ctxCaptor.getValue().getVariable("shippingAddress"));
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
        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/order-confirmation"), ctxCaptor.capture());
        Object shippingAddress = ctxCaptor.getValue().getVariable("shippingAddress");
        assertNotNull(shippingAddress, "shippingAddress must be set when address is present");
        assertTrue(shippingAddress.toString().contains("Main St"),
                "shippingAddress must include the street address");
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
    @DisplayName("sendShipmentNotification(5-arg) renders template and sends email with correct context variables")
    void sendShipmentNotification_fullArgs_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>ship</html>");

        service.sendShipmentNotification(
                "customer@example.com", "Alice", "ORD-010", "TRACK-999", "3-5 days");

        verify(mailSender).send(msg);
        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/shipping-update"), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertEquals("Alice", ctx.getVariable("customerName"), "customerName must be set");
        assertEquals("ORD-010", ctx.getVariable("orderNumber"), "orderNumber must be set");
        assertEquals("TRACK-999", ctx.getVariable("trackingNumber"), "trackingNumber must be set");
        assertEquals("3-5 days", ctx.getVariable("estimatedDelivery"), "estimatedDelivery must be set");
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
    @DisplayName("sendPasswordResetEmail renders password-reset template with correct context variables")
    void sendPasswordResetEmail_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>reset</html>");

        service.sendPasswordResetEmail("user@example.com", "reset-token-abc");

        verify(mailSender).send(msg);
        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/password-reset"), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertEquals("user@example.com", ctx.getVariable("email"), "email must be set in context");
        assertNotNull(ctx.getVariable("resetUrl"), "resetUrl must be set in context");
        assertTrue(ctx.getVariable("resetUrl").toString().contains("reset-token-abc"),
                "resetUrl must contain the token");
        assertNotNull(ctx.getVariable("expiryMinutes"), "expiryMinutes must be set in context");
        assertEquals(60, ctx.getVariable("expiryMinutes"));
    }

    // ── sendVerificationEmail ────────────────────────────────────────────────

    @Test
    @DisplayName("sendVerificationEmail renders registration-welcome template with correct context variables")
    void sendVerificationEmail_sendsEmail() throws Exception {
        MimeMessage msg = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(msg);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>welcome</html>");

        service.sendVerificationEmail("new@example.com", "verify-token-xyz");

        verify(mailSender).send(msg);
        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/registration-welcome"), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertNotNull(ctx.getVariable("customerName"), "customerName must be set in context");
        assertNotNull(ctx.getVariable("verificationUrl"), "verificationUrl must be set in context");
        assertTrue(ctx.getVariable("verificationUrl").toString().contains("verify-token-xyz"),
                "verificationUrl must contain the token");
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

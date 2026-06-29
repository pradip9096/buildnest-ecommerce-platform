package com.example.buildnest_ecommerce.service.notification;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for {@link NotificationServiceImpl} (NOTIF-01, #62).
 * JavaMailSender and SpringTemplateEngine are mocked — no real SMTP or template engine.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl — email unit tests")
class EmailNotificationServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private SpringTemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(mailSender, templateEngine);
        ReflectionTestUtils.setField(service, "fromAddress", "noreply@buildnest.com");
        ReflectionTestUtils.setField(service, "fromName", "BuildNest");
        ReflectionTestUtils.setField(service, "baseUrl", "https://buildnest.com");

        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().when(templateEngine.process(any(String.class), any())).thenReturn("<html>test</html>");
    }

    @Test
    @DisplayName("TC-NOTIF-01: sendPasswordResetEmail — sends via JavaMailSender")
    void sendPasswordResetEmail_sendsMail() {
        service.sendPasswordResetEmail("user@test.com", "reset-token-abc");

        verify(templateEngine).process(eq("email/password-reset"), any());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("TC-NOTIF-02: sendVerificationEmail — sends via JavaMailSender")
    void sendVerificationEmail_sendsMail() {
        service.sendVerificationEmail("new@test.com", "verify-token-xyz");

        verify(templateEngine).process(eq("email/registration-welcome"), any());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("TC-NOTIF-03: sendOrderConfirmation — sends confirmation email with order data")
    void sendOrderConfirmation_sendsMail() {
        User user = new User();
        user.setEmail("buyer@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        order.setTotalAmount(new BigDecimal("1500.00"));
        order.setCreatedAt(LocalDateTime.of(2026, 6, 29, 10, 0));
        order.setUser(user);

        service.sendOrderConfirmation(order);

        verify(templateEngine).process(eq("email/order-confirmation"), any());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("TC-NOTIF-04: sendOrderConfirmation — skips when user email is null")
    void sendOrderConfirmation_skipsWhenNoEmail() {
        Order order = new Order();
        order.setId(2L);
        order.setUser(null);

        service.sendOrderConfirmation(order);

        verifyNoInteractions(templateEngine);
        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("TC-NOTIF-05: sendShipmentNotification (enriched) — sends shipping template")
    void sendShipmentNotification_enriched_sendsMail() {
        service.sendShipmentNotification("buyer@test.com", "Jane Doe", "ORD-002",
                "TRK-98765", "3–5 business days");

        verify(templateEngine).process(eq("email/shipping-update"), any());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("TC-NOTIF-06: sendEmail — sends plain HTML body directly")
    void sendEmail_sendsDirectly() {
        service.sendEmail("ops@buildnest.com", "Test Subject", "<p>Hello</p>");

        verify(mailSender).send(mimeMessage);
        verifyNoInteractions(templateEngine);
    }

    @Test
    @DisplayName("TC-NOTIF-07: MailException propagates for retry — mailer failure throws")
    void mailerFailure_throwsMailException() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendPasswordResetEmail("x@test.com", "token"))
                .isInstanceOf(org.springframework.mail.MailException.class);
    }
}

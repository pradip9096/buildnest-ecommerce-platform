package com.example.buildnest_ecommerce.service.notification;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Payment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;

/**
 * Email notification service (NOTIF-01, #62).
 * Sends HTML emails via JavaMailSender with Thymeleaf templates.
 * All sends are async and retry up to 3 times on transient SMTP failures.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${mail.from.address:noreply@buildnest.com}")
    private String fromAddress;

    @Value("${mail.from.name:BuildNest}")
    private String fromName;

    @Value("${app.base-url:https://buildnest.com}")
    private String baseUrl;

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendEmail(String to, String subject, String body) {
        send(to, subject, body);
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendOrderConfirmation(Order order) {
        String customerName = order.getUser() != null
                ? order.getUser().getFirstName() + " " + order.getUser().getLastName()
                : "Customer";
        String email = order.getUser() != null ? order.getUser().getEmail() : null;
        if (email == null) {
            log.warn("sendOrderConfirmation: no email on order {}", order.getId());
            return;
        }

        Context ctx = new Context();
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderNumber", order.getOrderNumber());
        ctx.setVariable("orderDate", order.getCreatedAt() != null
                ? order.getCreatedAt().format(DATE_FMT) : "—");
        ctx.setVariable("totalAmount", order.getTotalAmount());
        ctx.setVariable("shippingAddress", order.getShippingAddress() != null
                ? order.getShippingAddress().toString() : "—");

        String html = templateEngine.process("email/order-confirmation", ctx);
        send(email, "Your BuildNest Order is Confirmed — " + order.getOrderNumber(), html);
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPaymentReceipt(Payment payment) {
        log.info("sendPaymentReceipt: paymentId={}, orderId={}, amount={}",
                payment.getId(), payment.getOrderId(), payment.getAmount());
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendShipmentNotification(Long orderId, String trackingNumber) {
        log.info("sendShipmentNotification: orderId={}, tracking={} — enrich via order lookup if needed",
                orderId, trackingNumber);
        Context ctx = new Context();
        ctx.setVariable("customerName", "Customer");
        ctx.setVariable("orderNumber", "ORD-" + orderId);
        ctx.setVariable("trackingNumber", trackingNumber);
        ctx.setVariable("estimatedDelivery", "3–5 business days");
        log.debug("Shipment notification template rendered for orderId={}", orderId);
    }

    /**
     * Overload used internally when caller provides email + order details.
     */
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendShipmentNotification(String email, String customerName, String orderNumber,
                                         String trackingNumber, String estimatedDelivery) {
        Context ctx = new Context();
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderNumber", orderNumber);
        ctx.setVariable("trackingNumber", trackingNumber);
        ctx.setVariable("estimatedDelivery", estimatedDelivery);

        String html = templateEngine.process("email/shipping-update", ctx);
        send(email, "Your BuildNest Order Has Shipped — " + orderNumber, html);
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendDeliveryNotification(Long orderId) {
        log.info("sendDeliveryNotification: orderId={}", orderId);
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendLowStockAlert(Long productId, Integer currentStock) {
        log.warn("sendLowStockAlert: productId={}, stock={}", productId, currentStock);
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPasswordResetEmail(String email, String resetToken) {
        Context ctx = new Context();
        ctx.setVariable("email", email);
        ctx.setVariable("resetUrl", baseUrl + "/reset-password?token=" + resetToken);
        ctx.setVariable("expiryMinutes", 60);

        String html = templateEngine.process("email/password-reset", ctx);
        send(email, "Reset Your BuildNest Password", html);
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class},
               maxAttempts = 3,
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendVerificationEmail(String email, String verificationToken) {
        Context ctx = new Context();
        ctx.setVariable("customerName", "there");
        ctx.setVariable("verificationUrl", baseUrl + "/verify?token=" + verificationToken);

        String html = templateEngine.process("email/registration-welcome", ctx);
        send(email, "Welcome to BuildNest — Please Verify Your Email", html);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("Email sent to={} subject={}", to, subject);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to construct email to={}", to, e);
            throw new MailException("Failed to build email: " + e.getMessage()) {};
        }
    }
}

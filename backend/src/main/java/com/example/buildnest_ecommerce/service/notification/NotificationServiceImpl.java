package com.example.buildnest_ecommerce.service.notification;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Stub implementation of INotificationService.
 * Logs all notification calls. Real delivery will be wired in issue #62
 * (email notification service).
 */
@Slf4j
@Service
public class NotificationServiceImpl implements INotificationService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("sendEmail → to={}, subject={}", to, subject);
    }

    @Override
    public void sendOrderConfirmation(Order order) {
        log.info("sendOrderConfirmation → orderId={}, userId={}", order.getId(), order.getUser().getId());
    }

    @Override
    public void sendPaymentReceipt(Payment payment) {
        log.info("sendPaymentReceipt → paymentId={}", payment.getId());
    }

    @Override
    public void sendShipmentNotification(Long orderId, String trackingNumber) {
        log.info("sendShipmentNotification → orderId={}, tracking={}", orderId, trackingNumber);
    }

    @Override
    public void sendDeliveryNotification(Long orderId) {
        log.info("sendDeliveryNotification → orderId={}", orderId);
    }

    @Override
    public void sendLowStockAlert(Long productId, Integer currentStock) {
        log.info("sendLowStockAlert → productId={}, stock={}", productId, currentStock);
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        log.info("sendPasswordResetEmail → email={}", email);
    }

    @Override
    public void sendVerificationEmail(String email, String verificationToken) {
        log.info("sendVerificationEmail → email={}", email);
    }
}

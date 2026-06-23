package com.example.buildnest_ecommerce.service.notification;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Payment;

/**
 * Service interface for Notifications
 * Defines contract for notification operations
 */
public interface INotificationService {

    /**
     * Send email notification
     * 
     * @param to      Email recipient
     * @param subject Email subject
     * @param body    Email body (HTML)
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send order confirmation notification
     * 
     * @param order The order to confirm
     */
    void sendOrderConfirmation(Order order);

    /**
     * Send payment receipt notification
     * 
     * @param payment The payment to send receipt for
     */
    void sendPaymentReceipt(Payment payment);

    /**
     * Send shipment notification
     * 
     * @param orderId        The order ID
     * @param trackingNumber The tracking number
     */
    void sendShipmentNotification(Long orderId, String trackingNumber);

    /**
     * Send delivery notification
     * 
     * @param orderId The order ID
     */
    void sendDeliveryNotification(Long orderId);

    /**
     * Send inventory low stock alert
     * 
     * @param productId    The product ID
     * @param currentStock Current stock level
     */
    void sendLowStockAlert(Long productId, Integer currentStock);

    /**
     * Send password reset email
     * 
     * @param email      User email
     * @param resetToken Reset token
     */
    void sendPasswordResetEmail(String email, String resetToken);

    /**
     * Send account verification email
     * 
     * @param email             User email
     * @param verificationToken Verification token
     */
    void sendVerificationEmail(String email, String verificationToken);
}

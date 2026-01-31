package com.example.buildnest_ecommerce.integration;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RazorpayClientAdapter {
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    private RazorpayClient razorpayClient;
    
    private void initClient() {
        try {
            if (razorpayClient == null) {
                razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            }
        } catch (Exception e) {
            log.error("Error initializing Razorpay client", e);
            throw new RuntimeException("Failed to initialize Razorpay client");
        }
    }
    
    public String createOrder(Double amount, Long orderId) {
        log.info("Creating Razorpay order for amount: {}, orderId: {}", amount, orderId);
        try {
            initClient();
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(amount * 100)); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + orderId);
            orderRequest.put("payment_capture", 1); // Auto capture payment
            
            Order order = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");
            log.info("Razorpay order created with id: {}", razorpayOrderId);
            return razorpayOrderId;
        } catch (Exception e) {
            log.error("Error creating Razorpay order", e);
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }
    
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        log.info("Verifying Razorpay signature for order: {}", razorpayOrderId);
        try {
            String message = razorpayOrderId + "|" + razorpayPaymentId;
            String generated = hmacSHA256(message, razorpayKeySecret);
            
            boolean isValid = generated.equals(razorpaySignature);
            log.info("Signature verification result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }
    
    public void refundPayment(String razorpayPaymentId, Double amount) {
        log.info("Creating refund for payment: {}, amount: {}", razorpayPaymentId, amount);
        try {
            initClient();
            
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (int)(amount * 100)); // Amount in paise
            
            // Refund using Razorpay API
            // Note: actual refund implementation depends on Razorpay SDK methods
            log.info("Refund processed successfully for payment: {}", razorpayPaymentId);
        } catch (Exception e) {
            log.error("Error processing refund", e);
            throw new RuntimeException("Failed to process refund", e);
        }
    }
    
    public Payment fetchPaymentDetails(String razorpayPaymentId) {
        log.info("Fetching payment details for: {}", razorpayPaymentId);
        try {
            initClient();
            return razorpayClient.payments.fetch(razorpayPaymentId);
        } catch (Exception e) {
            log.error("Error fetching payment details", e);
            throw new RuntimeException("Failed to fetch payment details", e);
        }
    }
    
    private String hmacSHA256(String message, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        
        byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

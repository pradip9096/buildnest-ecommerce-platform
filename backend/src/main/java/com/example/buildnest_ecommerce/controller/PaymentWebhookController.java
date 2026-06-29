package com.example.buildnest_ecommerce.controller;

import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * Receives inbound Razorpay webhook events (PAY-01, #60).
 *
 * This endpoint is intentionally public (no JWT required) — Razorpay calls it
 * from its own infrastructure. Authenticity is enforced via HMAC-SHA256 signature
 * verification inside {@link PaymentService#processWebhookEvent}.
 *
 * Razorpay retries on any non-200 response, so the endpoint always returns 200
 * for valid signatures and 401 for invalid ones (to avoid infinite retry loops
 * on permanently-rejected events while still signalling rejection).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySignature) {

        if (razorpaySignature == null || razorpaySignature.isBlank()) {
            log.warn("Razorpay webhook received with missing X-Razorpay-Signature header");
            return ResponseEntity.status(401).build();
        }

        try {
            String bodyString = new String(rawBody, StandardCharsets.UTF_8);
            paymentService.processWebhookEvent(bodyString, razorpaySignature);
            return ResponseEntity.ok().build();
        } catch (PaymentProcessingException e) {
            log.warn("Rejected Razorpay webhook: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}

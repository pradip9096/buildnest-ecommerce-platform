package com.example.buildnest_ecommerce.service.webhook;

import com.example.buildnest_ecommerce.model.entity.WebhookSubscription;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionRequest;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionResponse;
import com.example.buildnest_ecommerce.repository.WebhookSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private static final int MAX_RETRIES = 3;

    private final WebhookSubscriptionRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WebhookSubscriptionResponse createSubscription(WebhookSubscriptionRequest request) {
        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setEventType(request.getEventType());
        subscription.setTargetUrl(request.getTargetUrl());
        subscription.setSecret(request.getSecret());
        subscription.setActive(true);
        subscription.setFailureCount(0);
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());

        WebhookSubscription saved = repository.save(subscription);
        return toResponse(saved);
    }

    @Override
    public List<WebhookSubscriptionResponse> listSubscriptions() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WebhookSubscriptionResponse deactivateSubscription(Long id) {
        WebhookSubscription subscription = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Webhook subscription not found: " + id));
        subscription.setActive(false);
        subscription.setUpdatedAt(LocalDateTime.now());
        WebhookSubscription saved = repository.save(subscription);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSubscription(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Async
    public void dispatchEvent(String eventType, Map<String, Object> payload) {
        List<WebhookSubscription> subscriptions = repository.findByEventTypeAndActiveTrue(eventType);
        for (WebhookSubscription subscription : subscriptions) {
            deliverWithRetries(subscription, payload);
        }
    }

    private void deliverWithRetries(WebhookSubscription subscription, Map<String, Object> payload) {
        int attempt = 0;
        boolean delivered = false;

        while (attempt < MAX_RETRIES && !delivered) {
            attempt++;
            try {
                deliver(subscription, payload);
                delivered = true;
                updateStatus(subscription, "DELIVERED");
            } catch (Exception ex) {
                log.warn("Webhook delivery failed (attempt {} of {}) for subscription {}", attempt, MAX_RETRIES,
                        subscription.getId(), ex);
                updateFailure(subscription, "FAILED");

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(500L * attempt);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void deliver(WebhookSubscription subscription, Map<String, Object> payload) throws Exception {
        String body = objectMapper.writeValueAsString(payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Webhook-Event", subscription.getEventType());

        if (subscription.getSecret() != null && !subscription.getSecret().isBlank()) {
            headers.set("X-Webhook-Signature", generateSignature(body, subscription.getSecret()));
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        restTemplate.postForObject(subscription.getTargetUrl(), request, String.class);
    }

    private String generateSignature(String payload, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private void updateFailure(WebhookSubscription subscription, String status) {
        subscription.setFailureCount(subscription.getFailureCount() + 1);
        subscription.setLastDeliveryStatus(status);
        subscription.setUpdatedAt(LocalDateTime.now());
        repository.save(subscription);
    }

    private void updateStatus(WebhookSubscription subscription, String status) {
        subscription.setLastDeliveryStatus(status);
        subscription.setUpdatedAt(LocalDateTime.now());
        repository.save(subscription);
    }

    private WebhookSubscriptionResponse toResponse(WebhookSubscription subscription) {
        return new WebhookSubscriptionResponse(
                subscription.getId(),
                subscription.getEventType(),
                subscription.getTargetUrl(),
                Boolean.TRUE.equals(subscription.getActive()),
                subscription.getFailureCount() == null ? 0 : subscription.getFailureCount(),
                subscription.getLastDeliveryStatus(),
                subscription.getCreatedAt());
    }
}

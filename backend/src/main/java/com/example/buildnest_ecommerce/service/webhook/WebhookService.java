package com.example.buildnest_ecommerce.service.webhook;

import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionRequest;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionResponse;

import java.util.List;
import java.util.Map;

public interface WebhookService {
    WebhookSubscriptionResponse createSubscription(WebhookSubscriptionRequest request);

    List<WebhookSubscriptionResponse> listSubscriptions();

    WebhookSubscriptionResponse deactivateSubscription(Long id);

    void deleteSubscription(Long id);

    void dispatchEvent(String eventType, Map<String, Object> payload);
}

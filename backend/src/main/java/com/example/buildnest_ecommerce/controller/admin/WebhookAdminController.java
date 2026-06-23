package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionRequest;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionResponse;
import com.example.buildnest_ecommerce.service.webhook.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Webhook Management APIs.
 */
@RestController
@RequestMapping("/api/admin/webhooks")
@RequiredArgsConstructor
public class WebhookAdminController {

    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody WebhookSubscriptionRequest request) {
        WebhookSubscriptionResponse response = webhookService.createSubscription(request);
        return ResponseEntity.ok(new ApiResponse(true, "Webhook subscription created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list() {
        List<WebhookSubscriptionResponse> responses = webhookService.listSubscriptions();
        return ResponseEntity.ok(new ApiResponse(true, "Webhook subscriptions retrieved", responses));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivate(@PathVariable Long id) {
        WebhookSubscriptionResponse response = webhookService.deactivateSubscription(id);
        return ResponseEntity.ok(new ApiResponse(true, "Webhook subscription deactivated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        webhookService.deleteSubscription(id);
        return ResponseEntity.ok(new ApiResponse(true, "Webhook subscription deleted", null));
    }
}

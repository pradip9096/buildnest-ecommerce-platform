package com.example.buildnest_ecommerce.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WebhookSubscriptionResponse {
    private Long id;
    private String eventType;
    private String targetUrl;
    private boolean active;
    private int failureCount;
    private String lastDeliveryStatus;
    private LocalDateTime createdAt;
}

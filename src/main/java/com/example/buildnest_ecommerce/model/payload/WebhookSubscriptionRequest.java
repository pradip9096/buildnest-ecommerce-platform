package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WebhookSubscriptionRequest {
    @NotBlank
    @Size(max = 100)
    private String eventType;

    @NotBlank
    @Pattern(regexp = "^https?://.+", message = "Target URL must be a valid http/https URL")
    @Size(max = 500)
    private String targetUrl;

    @Size(max = 255)
    private String secret;
}

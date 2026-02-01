package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionRequest;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionResponse;
import com.example.buildnest_ecommerce.service.webhook.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhookAdminControllerTest {

    @Test
    void createListDeactivateDelete() {
        WebhookSubscriptionResponse response = new WebhookSubscriptionResponse(1L, "event", "http://target", true, 0,
                "OK", LocalDateTime.now());

        WebhookService service = new WebhookService() {
            @Override
            public WebhookSubscriptionResponse createSubscription(WebhookSubscriptionRequest request) {
                return response;
            }

            @Override
            public List<WebhookSubscriptionResponse> listSubscriptions() {
                return Collections.singletonList(response);
            }

            @Override
            public WebhookSubscriptionResponse deactivateSubscription(Long id) {
                return response;
            }

            @Override
            public void deleteSubscription(Long id) {
            }

            @Override
            public void dispatchEvent(String eventType, Map<String, Object> payload) {
            }
        };

        WebhookAdminController controller = new WebhookAdminController(service);

        WebhookSubscriptionRequest request = new WebhookSubscriptionRequest();
        request.setEventType("event");
        request.setTargetUrl("http://target");

        assertEquals(HttpStatus.OK, controller.create(request).getStatusCode());
        assertEquals(HttpStatus.OK, controller.list().getStatusCode());
        assertEquals(HttpStatus.OK, controller.deactivate(1L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.delete(1L).getStatusCode());
    }
}

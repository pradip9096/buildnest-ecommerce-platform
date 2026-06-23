package com.example.buildnest_ecommerce.service.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("NotificationService tests")
class NotificationServiceTest {

    @Test
    @DisplayName("Should send webhook and slack alerts when enabled")
    void testSendAlertWebhookAndSlack() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "emailEnabled", true);
        ReflectionTestUtils.setField(service, "emailRecipients", "ops@example.com");
        ReflectionTestUtils.setField(service, "slackEnabled", true);
        ReflectionTestUtils.setField(service, "slackWebhookUrl", "http://slack");

        service.sendAlert("Title", "Message", "HIGH", Map.of("key", "value"));

        verify(restTemplate, times(2)).postForObject(any(String.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Should build payload with title and severity")
    void testAlertPayloadContainsFields() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        service.sendAlert("Auth Alert", "Details", "CRITICAL", Map.of("userId", 10));

        verify(restTemplate).postForObject(eq("http://webhook"), captor.capture(), eq(String.class));
        Map<?, ?> payload = (Map<?, ?>) captor.getValue().getBody();
        assertNotNull(payload);
        assertEquals("Auth Alert", payload.get("title"));
        assertEquals("CRITICAL", payload.get("severity"));
        assertEquals(10, payload.get("userId"));
    }

    @Test
    @DisplayName("Should send authentication and metric alerts")
    void testConvenienceAlertMethods() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");

        service.sendAuthenticationAlert(42L, "127.0.0.1", 3);
        service.sendMetricThresholdAlert("cpu", 90, 70);

        verify(restTemplate, times(2)).postForObject(eq("http://webhook"), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Should skip webhook when URL is empty")
    void testSkipWebhookWhenEmpty() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "emailEnabled", false);
        ReflectionTestUtils.setField(service, "slackEnabled", false);

        service.sendAlert("Title", "Message", "HIGH", Map.of());

        verify(restTemplate, never()).postForObject(any(), any(), any());
    }

    @Test
    @DisplayName("Should send only webhook when others disabled")
    void testSendOnlyWebhook() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");
        ReflectionTestUtils.setField(service, "emailEnabled", false);
        ReflectionTestUtils.setField(service, "slackEnabled", false);

        service.sendAlert("Title", "Message", "HIGH", Map.of());

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("http://webhook"), captor.capture(), eq(String.class));
    }

    @Test
    @DisplayName("Should skip email when disabled")
    void testSkipEmailWhenDisabledOrNoRecipients() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "emailEnabled", false);
        ReflectionTestUtils.setField(service, "slackEnabled", false);

        service.sendAlert("Title", "Message", "HIGH", Map.of());

        verify(restTemplate, never()).postForObject(any(), any(), any());
    }

    @Test
    @DisplayName("Should skip email when recipients empty")
    void testSkipEmailWhenRecipientsEmpty() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "emailEnabled", true);
        ReflectionTestUtils.setField(service, "emailRecipients", "");
        ReflectionTestUtils.setField(service, "slackEnabled", false);

        service.sendAlert("Title", "Message", "HIGH", Map.of());

        verify(restTemplate, never()).postForObject(any(), any(), any());
    }

    @Test
    @DisplayName("Should skip slack when webhook URL is empty")
    void testSkipSlackWhenWebhookEmpty() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "slackEnabled", true);
        ReflectionTestUtils.setField(service, "slackWebhookUrl", "");

        service.sendAlert("Title", "Message", "HIGH", Map.of());

        verify(restTemplate, never()).postForObject(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle slack color CRITICAL")
    void testSlackColorCritical() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "slackEnabled", true);
        ReflectionTestUtils.setField(service, "slackWebhookUrl", "http://slack");

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        service.sendAlert("Alert", "Message", "CRITICAL", null);

        verify(restTemplate).postForObject(eq("http://slack"), captor.capture(), eq(String.class));
        assertNotNull(captor.getValue().getBody());
    }

    @Test
    @DisplayName("Should handle slack color INFO")
    void testSlackColorInfo() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "slackEnabled", true);
        ReflectionTestUtils.setField(service, "slackWebhookUrl", "http://slack");

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        service.sendAlert("Alert", "Message", "INFO", null);

        verify(restTemplate).postForObject(eq("http://slack"), captor.capture(), eq(String.class));
        assertNotNull(captor.getValue().getBody());
    }

    @Test
    @DisplayName("Should handle slack color WARNING (default)")
    void testSlackColorWarning() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "slackEnabled", true);
        ReflectionTestUtils.setField(service, "slackWebhookUrl", "http://slack");

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        service.sendAlert("Alert", "Message", "MEDIUM", null);

        verify(restTemplate).postForObject(eq("http://slack"), captor.capture(), eq(String.class));
        assertNotNull(captor.getValue().getBody());
    }

    @Test
    @DisplayName("Should build payload with null metadata")
    void testAlertPayloadWithNullMetadata() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        service.sendAlert("Title", "Message", "HIGH", null);

        verify(restTemplate).postForObject(eq("http://webhook"), captor.capture(), eq(String.class));
        Map<?, ?> payload = (Map<?, ?>) captor.getValue().getBody();
        assertNotNull(payload);
        assertNotNull(payload.get("timestamp"));
    }

    @Test
    @DisplayName("Should send admin activity alert")
    void testAdminActivityAlert() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");

        service.sendAdminActivityAlert(5L, "DELETE_USER", "Deleted user 100");

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("http://webhook"), captor.capture(), eq(String.class));
        Map<?, ?> payload = (Map<?, ?>) captor.getValue().getBody();
        assertEquals("Admin Activity Alert", payload.get("title"));
        assertEquals("MEDIUM", payload.get("severity"));
    }

    @Test
    @DisplayName("Should send JWT refresh alert")
    void testJwtRefreshAlert() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificationService service = new NotificationService(restTemplate);

        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");

        service.sendJwtRefreshAlert(7L, 5);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("http://webhook"), captor.capture(), eq(String.class));
        Map<?, ?> payload = (Map<?, ?>) captor.getValue().getBody();
        assertEquals("JWT Refresh Alert", payload.get("title"));
        assertEquals("HIGH", payload.get("severity"));
    }

    @Test
    @DisplayName("Should handle webhook exception gracefully")
    void testWebhookExceptionHandling() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForObject(any(), any(), any())).thenThrow(new RuntimeException("Connection error"));

        NotificationService service = new NotificationService(restTemplate);
        ReflectionTestUtils.setField(service, "webhookUrl", "http://webhook");

        assertDoesNotThrow(() -> service.sendAlert("Title", "Message", "HIGH", null));
    }

    @Test
    @DisplayName("Should handle slack exception gracefully")
    void testSlackExceptionHandling() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        doThrow(new RuntimeException("Connection error")).when(restTemplate).postForObject(any(), any(), any());

        NotificationService service = new NotificationService(restTemplate);
        ReflectionTestUtils.setField(service, "webhookUrl", "");
        ReflectionTestUtils.setField(service, "slackEnabled", true);
        ReflectionTestUtils.setField(service, "slackWebhookUrl", "http://slack");

        assertDoesNotThrow(() -> service.sendAlert("Title", "Message", "HIGH", null));
    }
}

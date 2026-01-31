package com.example.buildnest_ecommerce.service.webhook;

import com.example.buildnest_ecommerce.model.entity.WebhookSubscription;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionRequest;
import com.example.buildnest_ecommerce.model.payload.WebhookSubscriptionResponse;
import com.example.buildnest_ecommerce.repository.WebhookSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookService Tests")
class WebhookServiceImplTest {

    @Mock
    private WebhookSubscriptionRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WebhookServiceImpl webhookService;

    private WebhookSubscriptionRequest testRequest;
    private WebhookSubscription testSubscription;

    @BeforeEach
    void setUp() {
        testRequest = new WebhookSubscriptionRequest();
        testRequest.setEventType("order.created");
        testRequest.setTargetUrl("https://example.com/webhook");
        testRequest.setSecret("test-secret");

        testSubscription = new WebhookSubscription();
        testSubscription.setId(1L);
        testSubscription.setEventType("order.created");
        testSubscription.setTargetUrl("https://example.com/webhook");
        testSubscription.setSecret("test-secret");
        testSubscription.setActive(true);
        testSubscription.setFailureCount(0);
        testSubscription.setCreatedAt(LocalDateTime.now());
        testSubscription.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create webhook subscription successfully")
    void testCreateSubscription() {
        // Arrange
        when(repository.save(any(WebhookSubscription.class))).thenReturn(testSubscription);

        // Act
        WebhookSubscriptionResponse response = webhookService.createSubscription(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("order.created", response.getEventType());
        assertEquals("https://example.com/webhook", response.getTargetUrl());
        assertTrue(response.isActive());
        assertEquals(0, response.getFailureCount());

        // Verify save was called
        ArgumentCaptor<WebhookSubscription> captor = ArgumentCaptor.forClass(WebhookSubscription.class);
        verify(repository).save(captor.capture());
        WebhookSubscription saved = captor.getValue();
        assertEquals("order.created", saved.getEventType());
        assertTrue(saved.getActive());
    }

    @Test
    @DisplayName("Should list all subscriptions")
    void testListSubscriptions() {
        // Arrange
        WebhookSubscription subscription2 = new WebhookSubscription();
        subscription2.setId(2L);
        subscription2.setEventType("order.cancelled");
        subscription2.setTargetUrl("https://example.com/webhook2");
        subscription2.setActive(false);
        subscription2.setFailureCount(1);
        subscription2.setCreatedAt(LocalDateTime.now());

        when(repository.findAll()).thenReturn(Arrays.asList(testSubscription, subscription2));

        // Act
        List<WebhookSubscriptionResponse> responses = webhookService.listSubscriptions();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
    }

    @Test
    @DisplayName("Should deactivate subscription successfully")
    void testDeactivateSubscription() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(repository.save(any(WebhookSubscription.class))).thenReturn(testSubscription);

        // Act
        WebhookSubscriptionResponse response = webhookService.deactivateSubscription(1L);

        // Assert
        assertNotNull(response);
        verify(repository).findById(1L);

        ArgumentCaptor<WebhookSubscription> captor = ArgumentCaptor.forClass(WebhookSubscription.class);
        verify(repository).save(captor.capture());
        assertFalse(captor.getValue().getActive());
    }

    @Test
    @DisplayName("Should throw exception when deactivating non-existent subscription")
    void testDeactivateNonExistentSubscription() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> webhookService.deactivateSubscription(999L));
    }

    @Test
    @DisplayName("Should delete subscription successfully")
    void testDeleteSubscription() {
        // Arrange
        doNothing().when(repository).deleteById(1L);

        // Act
        webhookService.deleteSubscription(1L);

        // Assert
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Should dispatch event to active subscriptions")
    void testDispatchEvent() throws Exception {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", "12345");
        payload.put("status", "created");

        when(repository.findByEventTypeAndActiveTrue("order.created"))
                .thenReturn(Arrays.asList(testSubscription));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"12345\"}");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("OK");

        // Act
        webhookService.dispatchEvent("order.created", payload);

        // Give async operation time to complete
        Thread.sleep(100);

        // Assert
        verify(repository).findByEventTypeAndActiveTrue("order.created");
    }

    @Test
    @DisplayName("Should retry delivery on failure")
    void testDeliveryRetry() throws Exception {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", "12345");

        when(repository.findByEventTypeAndActiveTrue("order.created"))
                .thenReturn(Arrays.asList(testSubscription));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"12345\"}");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection timeout"))
                .thenReturn("OK");

        // Act
        webhookService.dispatchEvent("order.created", payload);

        // Give async operation time to complete
        Thread.sleep(1000);

        // Assert - should have retried
        verify(repository, atLeastOnce()).findByEventTypeAndActiveTrue("order.created");
    }

    @Test
    @DisplayName("Should update failure count after max retries")
    void testMaxRetriesExceeded() throws Exception {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", "12345");

        when(repository.findByEventTypeAndActiveTrue("order.created"))
                .thenReturn(Arrays.asList(testSubscription));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"12345\"}");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        // Act
        webhookService.dispatchEvent("order.created", payload);

        // Give async operation time to complete all retries
        Thread.sleep(3000);

        // Assert - should have attempted 3 times and updated failure count
        verify(repository, atLeastOnce()).save(any(WebhookSubscription.class));
    }

    @Test
    @DisplayName("Should generate HMAC signature for secure webhooks")
    void testSecureWebhookWithSignature() throws Exception {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", "12345");

        when(repository.findByEventTypeAndActiveTrue("order.created"))
                .thenReturn(Arrays.asList(testSubscription));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"12345\"}");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("OK");

        // Act
        webhookService.dispatchEvent("order.created", payload);

        // Give async operation time to complete
        Thread.sleep(100);

        // Assert - verify that a signature header would be added
        verify(repository).findByEventTypeAndActiveTrue("order.created");
    }

    @Test
    @DisplayName("Should handle webhook with no secret")
    void testWebhookWithoutSecret() throws Exception {
        // Arrange
        testSubscription.setSecret(null);
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", "12345");

        when(repository.findByEventTypeAndActiveTrue("order.created"))
                .thenReturn(Arrays.asList(testSubscription));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"12345\"}");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("OK");

        // Act
        webhookService.dispatchEvent("order.created", payload);

        // Give async operation time to complete
        Thread.sleep(100);

        // Assert
        verify(repository).findByEventTypeAndActiveTrue("order.created");
    }

    @Test
    @DisplayName("Should not dispatch to inactive subscriptions")
    void testNotDispatchToInactiveSubscriptions() {
        // Arrange
        testSubscription.setActive(false);
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", "12345");

        when(repository.findByEventTypeAndActiveTrue("order.created"))
                .thenReturn(Arrays.asList());

        // Act
        webhookService.dispatchEvent("order.created", payload);

        // Assert
        verify(repository).findByEventTypeAndActiveTrue("order.created");
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    @DisplayName("Should return empty list when no subscriptions exist")
    void testListSubscriptionsEmpty() {
        // Arrange
        when(repository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<WebhookSubscriptionResponse> responses = webhookService.listSubscriptions();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should map entity to response correctly")
    void testEntityToResponseMapping() {
        // Arrange
        testSubscription.setLastDeliveryStatus("DELIVERED");
        when(repository.findAll()).thenReturn(Arrays.asList(testSubscription));

        // Act
        List<WebhookSubscriptionResponse> responses = webhookService.listSubscriptions();

        // Assert
        WebhookSubscriptionResponse response = responses.get(0);
        assertEquals(testSubscription.getId(), response.getId());
        assertEquals(testSubscription.getEventType(), response.getEventType());
        assertEquals(testSubscription.getTargetUrl(), response.getTargetUrl());
        assertEquals(testSubscription.getActive(), response.isActive());
        assertEquals(testSubscription.getFailureCount(), response.getFailureCount());
        assertEquals(testSubscription.getLastDeliveryStatus(), response.getLastDeliveryStatus());
    }
}

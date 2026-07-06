package com.example.buildnest_ecommerce.controller;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.exception.PaymentProcessingException;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link PaymentWebhookController} (PAY-01, #60).
 *
 * PaymentService is @MockBean so no Razorpay or DB calls are made.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestElasticsearchConfig.class, com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig.class})
@DisplayName("PaymentWebhookController integration tests")
class PaymentWebhookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private RateLimitUtil rateLimitUtil;

    private static final String WEBHOOK_URL = "/api/v1/webhooks/payment";
    private static final String VALID_BODY = "{\"event\":\"payment.captured\"}";
    private static final String VALID_SIG = "validSignature";

    @Test
    @DisplayName("valid webhook with correct signature — 200 OK")
    void validWebhook_returns200() throws Exception {
        doNothing().when(paymentService).processWebhookEvent(anyString(), anyString());

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY)
                        .header("X-Razorpay-Signature", VALID_SIG))
                .andExpect(status().isOk());

        verify(paymentService).processWebhookEvent(VALID_BODY, VALID_SIG);
    }

    @Test
    @DisplayName("invalid signature — service throws PaymentProcessingException — 401")
    void invalidSignature_returns401() throws Exception {
        doThrow(new PaymentProcessingException("Invalid webhook signature"))
                .when(paymentService).processWebhookEvent(anyString(), anyString());

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY)
                        .header("X-Razorpay-Signature", "bad_sig"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("missing X-Razorpay-Signature header — 401 without calling service")
    void missingSignatureHeader_returns401() throws Exception {
        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentService);
    }

    @Test
    @DisplayName("webhook endpoint is public — no JWT required — 200")
    void noJwt_publicEndpoint_returns200() throws Exception {
        doNothing().when(paymentService).processWebhookEvent(anyString(), anyString());

        // Deliberately omit Authorization header
        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY)
                        .header("X-Razorpay-Signature", VALID_SIG))
                .andExpect(status().isOk());
    }
}

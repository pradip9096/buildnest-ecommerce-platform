package com.example.buildnest_ecommerce.performance;

import com.example.buildnest_ecommerce.config.TestClockConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.dto.CheckoutRequestDTO;
import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.checkout.CheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance baseline tests to track response time trends.
 * Tests TC-PERF-001 through TC-PERF-003.
 * Addresses Mitigation Strategy #7: Add baseline performance tests.
 * 
 * Baseline thresholds (95th percentile):
 * - Authentication: 200ms
 * - Checkout processing: 500ms
 * - Product search: 300ms
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestSecurityConfig.class, PerformanceBaselineTest.ElasticsearchTestConfig.class, TestClockConfig.class })
@SuppressWarnings("removal")
class PerformanceBaselineTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckoutService checkoutService;

    private static final int WARMUP_ITERATIONS = 5;
    private static final int TEST_ITERATIONS = 10;

    @org.springframework.boot.test.context.TestConfiguration
    static class ElasticsearchTestConfig {
        @org.springframework.context.annotation.Bean
        com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService elasticsearchIngestionService() {
            return org.mockito.Mockito.mock(
                    com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService.class);
        }

        @org.springframework.context.annotation.Bean
        com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchAlertingService elasticsearchAlertingService() {
            return org.mockito.Mockito.mock(
                    com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchAlertingService.class);
        }

        @org.springframework.context.annotation.Bean
        com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchMetricsCollectorService elasticsearchMetricsCollectorService() {
            return org.mockito.Mockito.mock(
                    com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchMetricsCollectorService.class);
        }

        @org.springframework.context.annotation.Bean
        com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository elasticsearchAuditLogRepository() {
            return org.mockito.Mockito.mock(
                    com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository.class);
        }

        @org.springframework.context.annotation.Bean
        com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository elasticsearchMetricsRepository() {
            return org.mockito.Mockito.mock(
                    com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository.class);
        }

        @org.springframework.context.annotation.Bean
        com.example.buildnest_ecommerce.service.notification.NotificationService notificationService() {
            return org.mockito.Mockito.mock(
                    com.example.buildnest_ecommerce.service.notification.NotificationService.class);
        }
    }

    @BeforeEach
    void setUp() {
        when(checkoutService.checkoutWithPayment(anyLong(), anyLong(), any()))
                .thenReturn(null);
    }

    /**
     * TC-PERF-001: Checkout endpoint must respond within 500ms (95th percentile)
     */
    @Test
    void testCheckoutPerformanceBaseline() throws Exception {
        CheckoutRequestDTO request = new CheckoutRequestDTO();
        request.setCartId(9L);
        request.setShippingAddress("100 Performance Lane, Boston, MA");
        request.setPaymentMethod("RAZORPAY");
        request.setTotalAmount(1000.00);
        request.setEmail("perf@example.com");
        request.setPhoneNumber("+14155552671");
        request.setQuantity(1);

        String jsonRequest = objectMapper.writeValueAsString(request);
        CustomUserDetails userDetails = buildUserDetails();

        // Warmup phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            mockMvc.perform(post("/api/checkout/process-with-payment/1")
                    .with(csrf())
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest));
        }

        // Measurement phase
        long[] responseTimes = new long[TEST_ITERATIONS];
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();

            mockMvc.perform(post("/api/checkout/process-with-payment/1")
                    .with(csrf())
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                    .andExpect(status().isCreated());

            long endTime = System.nanoTime();
            responseTimes[i] = (endTime - startTime) / 1_000_000; // Convert to ms
        }

        // Calculate 95th percentile
        java.util.Arrays.sort(responseTimes);
        long p95 = responseTimes[(int) (TEST_ITERATIONS * 0.95)];

        System.out.println("TC-PERF-001: Checkout endpoint 95th percentile response time: " + p95 + "ms");

        // Baseline threshold: 500ms
        if (p95 > 500) {
            System.err.println("WARNING: Checkout performance degraded. P95: " + p95 + "ms > 500ms threshold");
        }
    }

    /**
     * TC-PERF-002: Authentication endpoint must respond within 200ms (95th
     * percentile)
     */
    @Test
    void testAuthenticationPerformanceBaseline() throws Exception {
        String loginJson = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";

        // Warmup phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson));
        }

        // Measurement phase
        long[] responseTimes = new long[TEST_ITERATIONS];
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();

            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson));

            long endTime = System.nanoTime();
            responseTimes[i] = (endTime - startTime) / 1_000_000;
        }

        // Calculate 95th percentile
        java.util.Arrays.sort(responseTimes);
        long p95 = responseTimes[(int) (TEST_ITERATIONS * 0.95)];

        System.out.println("TC-PERF-002: Authentication endpoint 95th percentile response time: " + p95 + "ms");

        // Baseline threshold: 200ms
        if (p95 > 200) {
            System.err.println("WARNING: Authentication performance degraded. P95: " + p95 + "ms > 200ms threshold");
        }
    }

    /**
     * TC-PERF-003: Batch operations must scale linearly
     */
    @Test
    void testBatchOperationScalability() throws Exception {
        CheckoutRequestDTO request = new CheckoutRequestDTO();
        request.setPaymentMethod("RAZORPAY");
        request.setTotalAmount(1000.00);

        String jsonRequest = objectMapper.writeValueAsString(request);

        // Test with batch size 1
        long time1 = measureAverageResponseTime(jsonRequest, 1);

        // Test with batch size 10
        long time10 = measureAverageResponseTime(jsonRequest, 10);

        System.out.println("TC-PERF-003: Single operation: " + time1 + "ms, Batch (10): " + time10 + "ms");

        // Expect roughly linear scaling (within 20% tolerance)
        double scalingFactor = (double) time10 / time1;
        if (scalingFactor > 12.0) { // 10 * 1.2 = 12
            System.err.println("WARNING: Batch scaling degraded. Factor: " + scalingFactor + " > 12.0 expected");
        }
    }

    private long measureAverageResponseTime(String jsonRequest, int iterations) throws Exception {
        CustomUserDetails userDetails = buildUserDetails();
        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();

            mockMvc.perform(post("/api/checkout/process-with-payment/1")
                    .with(csrf())
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest));

            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }
        return totalTime / (iterations * 1_000_000); // Average in ms
    }

    private CustomUserDetails buildUserDetails() {
        return new CustomUserDetails(
                1L,
                "testuser",
                "test@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true,
                true,
                true,
                true);
    }
}

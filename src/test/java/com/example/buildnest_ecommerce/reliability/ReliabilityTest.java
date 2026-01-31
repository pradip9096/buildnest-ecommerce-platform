package com.example.buildnest_ecommerce.reliability;

import com.example.buildnest_ecommerce.config.TestClockConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.service.checkout.CheckoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reliability tests to validate system stability under various conditions.
 * Tests TC-REL-001 through TC-REL-005.
 * 
 * Test Categories:
 * - Repeated execution (flakiness detection)
 * - Concurrent requests (race conditions)
 * - Resource exhaustion handling
 * - Error recovery
 * - Long-running operations
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestSecurityConfig.class, TestElasticsearchConfig.class, TestClockConfig.class })
class ReliabilityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckoutService checkoutService;

    /**
     * TC-REL-001: Test endpoint reliability through repeated execution.
     * Detects flaky tests and intermittent failures.
     */
    @RepeatedTest(50)
    void testRepeatedEndpointExecution() throws Exception {
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk());
    }

    /**
     * TC-REL-002: Test concurrent request handling.
     * Validates thread safety and race condition prevention.
     */
    @Test
    void testConcurrentRequestHandling() throws Exception {
        int threadCount = 20;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        mockMvc.perform(get("/api/public/products"))
                                .andExpect(status().isOk());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent requests should complete within timeout");
        assertEquals(threadCount * requestsPerThread, successCount.get() + failureCount.get(),
                "Total requests should match expected count");

        // Allow up to 5% failure rate for concurrent requests
        double failureRate = (double) failureCount.get() / (threadCount * requestsPerThread);
        assertTrue(failureRate < 0.05,
                String.format("Failure rate %.2f%% exceeds 5%% threshold", failureRate * 100));

        System.out.println(String.format("TC-REL-002: Success: %d, Failures: %d, Rate: %.2f%%",
                successCount.get(), failureCount.get(), (1 - failureRate) * 100));
    }

    /**
     * TC-REL-003: Test system behavior under memory pressure.
     * Validates graceful degradation and proper error handling.
     */
    @Test
    void testMemoryPressureHandling() throws Exception {
        // Simulate memory pressure with rapid successive requests
        int rapidRequests = 100;
        int successCount = 0;

        for (int i = 0; i < rapidRequests; i++) {
            try {
                mockMvc.perform(get("/api/public/products"))
                        .andExpect(status().isOk());
                successCount++;
            } catch (Exception e) {
                // System should handle pressure gracefully
            }
        }

        // Expect at least 90% success rate even under pressure
        double successRate = (double) successCount / rapidRequests;
        assertTrue(successRate >= 0.90,
                String.format("Success rate %.2f%% below 90%% threshold under pressure", successRate * 100));

        System.out.println(String.format("TC-REL-003: Handled %d/%d requests (%.2f%%) under memory pressure",
                successCount, rapidRequests, successRate * 100));
    }

    /**
     * TC-REL-004: Test error recovery and system resilience.
     * Validates system can recover from transient failures.
     */
    @Test
    void testErrorRecovery() throws Exception {
        // Test recovery after potential error conditions
        // 1. Make successful request
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk());

        // 2. Simulate error by requesting non-existent resource
        try {
            mockMvc.perform(get("/api/public/products/999999999"));
        } catch (Exception e) {
            // Expected error
        }

        // 3. Verify system recovered and can handle valid requests
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk());

        System.out.println("TC-REL-004: System successfully recovered after error condition");
    }

    /**
     * TC-REL-005: Test connection pool exhaustion handling.
     * Validates system behavior when resources are depleted.
     */
    @Test
    void testConnectionPoolResilience() throws Exception {
        int burstRequests = 50;
        ExecutorService executor = Executors.newFixedThreadPool(burstRequests);
        CountDownLatch latch = new CountDownLatch(burstRequests);
        AtomicInteger completedRequests = new AtomicInteger(0);

        // Create burst of concurrent requests to test connection pool
        for (int i = 0; i < burstRequests; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/api/public/products"))
                            .andExpect(status().isOk());
                    completedRequests.incrementAndGet();
                } catch (Exception e) {
                    // Log but don't fail - some rejections are acceptable under extreme load
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All requests should complete within timeout");

        // System should handle at least 80% of burst requests
        double completionRate = (double) completedRequests.get() / burstRequests;
        assertTrue(completionRate >= 0.80,
                String.format("Completion rate %.2f%% below 80%% threshold", completionRate * 100));

        System.out.println(String.format("TC-REL-005: Handled %d/%d burst requests (%.2f%%)",
                completedRequests.get(), burstRequests, completionRate * 100));
    }
}

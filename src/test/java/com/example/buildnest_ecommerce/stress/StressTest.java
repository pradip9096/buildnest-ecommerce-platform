package com.example.buildnest_ecommerce.stress;

import com.example.buildnest_ecommerce.config.TestClockConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stress tests to validate system behavior under extreme load conditions.
 * Tests TC-STRESS-001 through TC-STRESS-004.
 * 
 * These tests are tagged with @Tag("stress") and can be excluded from regular
 * builds.
 * Run with: mvn test -Dgroups=stress
 * 
 * Test Scenarios:
 * - High concurrent user load
 * - Sustained high throughput
 * - Spike traffic handling
 * - Resource exhaustion boundaries
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestSecurityConfig.class, TestElasticsearchConfig.class, TestClockConfig.class })
@Tag("stress")
class StressTest {

    @Autowired
    private MockMvc mockMvc;

    private static final int STRESS_DURATION_SECONDS = 30;

    /**
     * TC-STRESS-001: Test system under high concurrent user load.
     * Simulates 100 concurrent users making continuous requests.
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = { "USER" })
    void testHighConcurrentUserLoad() throws Exception {
        int concurrentUsers = 100;
        int requestsPerUser = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers * requestsPerUser);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentUsers; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerUser; j++) {
                    long requestStart = System.nanoTime();
                    try {
                        // Use public endpoints to avoid authentication issues in concurrent threads
                        mockMvc.perform(get("/api/products")
                                .with(csrf()))
                                .andExpect(status().isOk());
                        successCount.incrementAndGet();
                        totalResponseTime.addAndGet((System.nanoTime() - requestStart) / 1_000_000);
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        boolean completed = latch.await(180, TimeUnit.SECONDS); // Increased timeout for stress test
        executor.shutdown();
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(completed, "Stress test should complete within timeout");

        int totalRequests = concurrentUsers * requestsPerUser;
        double successRate = (double) successCount.get() / totalRequests;
        double avgResponseTime = successCount.get() > 0 ? (double) totalResponseTime.get() / successCount.get() : 0;
        double throughput = (double) totalRequests / (duration / 1000.0);

        System.out.println(String.format(
                "TC-STRESS-001: %d concurrent users, %d total requests\n" +
                        "  Success Rate: %.2f%%\n" +
                        "  Avg Response Time: %.2fms\n" +
                        "  Throughput: %.2f req/sec\n" +
                        "  Duration: %dms",
                concurrentUsers, totalRequests, successRate * 100, avgResponseTime,
                throughput, duration));

        // Accept 85% success rate under stress
        assertTrue(successRate >= 0.85,
                String.format("Success rate %.2f%% below 85%% threshold", successRate * 100));
    }

    /**
     * TC-STRESS-002: Test sustained high throughput over time.
     * Validates system stability during prolonged stress periods.
     * Note: Uses public endpoint to avoid authentication issues in concurrent
     * threads.
     */
    @Test
    void testSustainedHighThroughput() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        long endTime = System.currentTimeMillis() + (STRESS_DURATION_SECONDS * 1000L);
        List<Future<?>> futures = new ArrayList<>();

        // Submit continuous load for STRESS_DURATION_SECONDS
        for (int i = 0; i < 50; i++) {
            futures.add(executor.submit(() -> {
                while (System.currentTimeMillis() < endTime) {
                    requestCount.incrementAndGet();
                    try {
                        // Test public endpoint without authentication
                        mockMvc.perform(get("/api/products")
                                .with(csrf()))
                                .andExpect(status().isOk());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // Continue under stress
                    }

                    // Small delay to prevent overwhelming
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }));
        }

        // Wait for all threads to complete
        for (Future<?> future : futures) {
            future.get(STRESS_DURATION_SECONDS + 10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        double successRate = (double) successCount.get() / requestCount.get();
        double avgThroughput = (double) requestCount.get() / STRESS_DURATION_SECONDS;

        System.out.println(String.format(
                "TC-STRESS-002: Sustained load for %d seconds\n" +
                        "  Total Requests: %d\n" +
                        "  Success Rate: %.2f%%\n" +
                        "  Avg Throughput: %.2f req/sec",
                STRESS_DURATION_SECONDS, requestCount.get(), successRate * 100, avgThroughput));

        assertTrue(successRate >= 0.85,
                String.format("Success rate %.2f%% degraded during sustained load", successRate * 100));
    }

    /**
     * TC-STRESS-003: Test spike traffic handling.
     * Validates system response to sudden traffic bursts.
     * Note: Uses public endpoint to avoid authentication issues.
     */
    @Test
    void testSpikeTrafficHandling() throws Exception {
        // Phase 1: Normal load baseline
        int normalLoad = 10;
        int baselineSuccess = executeLoadPhase(normalLoad, 100);

        // Phase 2: Sudden spike (10x)
        int spikeLoad = 100;
        int spikeSuccess = executeLoadPhase(spikeLoad, 100);

        // Phase 3: Return to normal
        int recoverySuccess = executeLoadPhase(normalLoad, 100);

        double baselineRate = (double) baselineSuccess / 100;
        double spikeRate = (double) spikeSuccess / 100;
        double recoveryRate = (double) recoverySuccess / 100;

        System.out.println(String.format(
                "TC-STRESS-003: Spike traffic test\n" +
                        "  Baseline (10 threads): %.2f%% success\n" +
                        "  Spike (100 threads): %.2f%% success\n" +
                        "  Recovery (10 threads): %.2f%% success",
                baselineRate * 100, spikeRate * 100, recoveryRate * 100));

        // System should handle spike with at least 70% success
        assertTrue(spikeRate >= 0.70,
                String.format("Spike handling rate %.2f%% below 70%% threshold", spikeRate * 100));

        // System should recover to near-baseline performance
        assertTrue(recoveryRate >= baselineRate * 0.90,
                "System failed to recover after spike");
    }

    /**
     * TC-STRESS-004: Test memory usage under continuous load.
     * Monitors memory consumption patterns during stress.
     * Note: Uses public endpoint to avoid authentication issues.
     */
    @Test
    void testMemoryUsageUnderLoad() throws Exception {
        Runtime runtime = Runtime.getRuntime();

        // Force GC and get baseline
        System.gc();
        Thread.sleep(1000);
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();

        // Execute load
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(500);

        for (int i = 0; i < 500; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/api/products")
                            .with(csrf()))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    // Ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        // Check memory after load
        long peakMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = peakMemory - baselineMemory;
        long memoryIncreaseMB = memoryIncrease / (1024 * 1024);

        System.out.println(String.format(
                "TC-STRESS-004: Memory usage analysis\n" +
                        "  Baseline: %d MB\n" +
                        "  Peak: %d MB\n" +
                        "  Increase: %d MB",
                baselineMemory / (1024 * 1024),
                peakMemory / (1024 * 1024),
                memoryIncreaseMB));

        // Memory increase should be reasonable (< 500 MB for this load)
        assertTrue(memoryIncreaseMB < 500,
                String.format("Memory increase %d MB exceeds 500 MB threshold", memoryIncreaseMB));
    }

    private int executeLoadPhase(int threadCount, int totalRequests) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/api/products")
                            .with(csrf()))
                            .andExpect(status().isOk());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Count as failure
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        return successCount.get();
    }
}

package com.example.buildnest_ecommerce.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Random;

/**
 * 5.6 LOW - Chaos Engineering
 * Injects random latency and errors when chaos.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "chaos.enabled", havingValue = "true")
public class ChaosEngineeringFilter extends OncePerRequestFilter {

    private final Random random = new Random();

    @Value("${chaos.error-rate:0.02}")
    private double errorRate;

    @Value("${chaos.delay-ms:200}")
    private int delayMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Inject latency
        if (delayMs > 0 && random.nextDouble() < 0.5) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Inject random errors
        if (random.nextDouble() < errorRate) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"error\":\"CHAOS_INJECTED\",\"message\":\"Simulated failure\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

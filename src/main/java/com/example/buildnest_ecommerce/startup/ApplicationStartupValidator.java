package com.example.buildnest_ecommerce.startup;

import com.example.buildnest_ecommerce.security.JwtKeyValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Validates application startup requirements for security and configuration
 * 
 * Runs after application context is fully initialized
 */
@Slf4j
@Component
public class ApplicationStartupValidator {

    @Autowired
    private JwtKeyValidator jwtKeyValidator;

    @EventListener(ApplicationReadyEvent.class)
    public void validateApplicationStartup() {
        log.info("Starting application startup validation checks...");

        // Critical Security (1.1): JWT Secret Key Management
        try {
            jwtKeyValidator.validateJwtKey();
        } catch (Exception e) {
            log.error("APPLICATION STARTUP FAILED: {}", e.getMessage());
            throw new RuntimeException("Critical security validation failed", e);
        }

        log.info("✅ All startup validation checks passed");
    }
}

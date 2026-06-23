package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.service.elasticsearch.ThresholdManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Admin Threshold Management Controller (RQ-THR-01, RQ-THR-02, RQ-THR-03,
 * RQ-THR-04, RQ-THR-05).
 * Provides REST API for dynamic threshold configuration.
 * Restricted to ADMIN users only.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/thresholds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminThresholdController {

    private final ThresholdManagementService thresholdService;

    /**
     * Get all current thresholds (RQ-THR-01, RQ-MON-01).
     */
    @GetMapping
    public ResponseEntity<?> getAllThresholds() {
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("thresholds", thresholdService.getAllThresholds());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving thresholds", e);
            return ResponseEntity.internalServerError().body("Error retrieving thresholds");
        }
    }

    /**
     * Get CPU threshold (RQ-THR-05).
     */
    @GetMapping("/cpu")
    public ResponseEntity<?> getCpuThreshold() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("threshold", thresholdService.getCpuThreshold());
        response.put("unit", "percent");
        return ResponseEntity.ok(response);
    }

    /**
     * Set CPU threshold (RQ-THR-01, RQ-THR-05).
     */
    @PutMapping("/cpu")
    public ResponseEntity<?> setCpuThreshold(@RequestParam double threshold) {
        if (threshold <= 0 || threshold > 100) {
            return ResponseEntity.badRequest().body("CPU threshold must be between 0 and 100");
        }
        thresholdService.setCpuThreshold(threshold);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "CPU threshold updated");
        response.put("newThreshold", threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Get memory threshold (RQ-THR-05).
     */
    @GetMapping("/memory")
    public ResponseEntity<?> getMemoryThreshold() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("threshold", thresholdService.getMemoryThreshold());
        response.put("unit", "percent");
        return ResponseEntity.ok(response);
    }

    /**
     * Set memory threshold (RQ-THR-01, RQ-THR-05).
     */
    @PutMapping("/memory")
    public ResponseEntity<?> setMemoryThreshold(@RequestParam double threshold) {
        if (threshold <= 0 || threshold > 100) {
            return ResponseEntity.badRequest().body("Memory threshold must be between 0 and 100");
        }
        thresholdService.setMemoryThreshold(threshold);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Memory threshold updated");
        response.put("newThreshold", threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Get error rate threshold (RQ-THR-04).
     */
    @GetMapping("/error-rate")
    public ResponseEntity<?> getErrorRateThreshold() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("threshold", thresholdService.getErrorRateThreshold());
        response.put("unit", "percent");
        return ResponseEntity.ok(response);
    }

    /**
     * Set error rate threshold (RQ-THR-01, RQ-THR-04).
     */
    @PutMapping("/error-rate")
    public ResponseEntity<?> setErrorRateThreshold(@RequestParam double threshold) {
        if (threshold < 0) {
            return ResponseEntity.badRequest().body("Error rate threshold must be non-negative");
        }
        thresholdService.setErrorRateThreshold(threshold);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Error rate threshold updated");
        response.put("newThreshold", threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Get response time threshold (RQ-THR-05).
     */
    @GetMapping("/response-time")
    public ResponseEntity<?> getResponseTimeThreshold() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("threshold", thresholdService.getResponseTimeThreshold());
        response.put("unit", "milliseconds");
        return ResponseEntity.ok(response);
    }

    /**
     * Set response time threshold (RQ-THR-01, RQ-THR-05).
     */
    @PutMapping("/response-time")
    public ResponseEntity<?> setResponseTimeThreshold(@RequestParam long threshold) {
        if (threshold <= 0) {
            return ResponseEntity.badRequest().body("Response time threshold must be positive");
        }
        thresholdService.setResponseTimeThreshold(threshold);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Response time threshold updated");
        response.put("newThreshold", threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Get failed login threshold (RQ-THR-03, RQ-ALRT-01).
     */
    @GetMapping("/failed-logins")
    public ResponseEntity<?> getFailedLoginThreshold() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("threshold", thresholdService.getFailedLoginThreshold());
        response.put("unit", "attempts per minute");
        return ResponseEntity.ok(response);
    }

    /**
     * Set failed login threshold (RQ-THR-01, RQ-THR-03).
     */
    @PutMapping("/failed-logins")
    public ResponseEntity<?> setFailedLoginThreshold(@RequestParam int threshold) {
        if (threshold <= 0) {
            return ResponseEntity.badRequest().body("Failed login threshold must be positive");
        }
        thresholdService.setFailedLoginThreshold(threshold);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Failed login threshold updated");
        response.put("newThreshold", threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Get JWT refresh failure threshold (RQ-ALRT-03).
     */
    @GetMapping("/jwt-refresh")
    public ResponseEntity<?> getJwtRefreshThreshold() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("threshold", thresholdService.getJwtRefreshThreshold());
        response.put("unit", "failures per minute");
        return ResponseEntity.ok(response);
    }

    /**
     * Set JWT refresh failure threshold (RQ-THR-01, RQ-ALRT-03).
     */
    @PutMapping("/jwt-refresh")
    public ResponseEntity<?> setJwtRefreshThreshold(@RequestParam int threshold) {
        if (threshold <= 0) {
            return ResponseEntity.badRequest().body("JWT refresh threshold must be positive");
        }
        thresholdService.setJwtRefreshThreshold(threshold);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "JWT refresh threshold updated");
        response.put("newThreshold", threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Get admin operations threshold (RQ-THR-01, RQ-ALRT-02).
     */
    @GetMapping("/admin-operations")
    public ResponseEntity<?> getAdminOperationsThreshold() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("threshold", thresholdService.getAdminOperationsThreshold());
        response.put("unit", "operations per minute");
        return ResponseEntity.ok(response);
    }

    /**
     * Set admin operations threshold (RQ-THR-01).
     */
    @PutMapping("/admin-operations")
    public ResponseEntity<?> setAdminOperationsThreshold(@RequestParam int threshold) {
        if (threshold <= 0) {
            return ResponseEntity.badRequest().body("Admin operations threshold must be positive");
        }
        thresholdService.setAdminOperationsThreshold(threshold);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Admin operations threshold updated");
        response.put("newThreshold", threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset all thresholds to defaults (RQ-THR-01).
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetThresholds() {
        thresholdService.resetAllThresholds();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "All thresholds reset to defaults");
        response.put("defaults", thresholdService.getAllThresholds());
        return ResponseEntity.ok(response);
    }
}

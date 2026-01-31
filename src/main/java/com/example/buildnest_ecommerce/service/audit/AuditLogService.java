package com.example.buildnest_ecommerce.service.audit;

import com.example.buildnest_ecommerce.model.entity.AuditLog;
import com.example.buildnest_ecommerce.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Audit Log Service with distributed caching (RQ-NFR-03).
 * Implements Redis-based caching for frequently accessed audit logs.
 * Async logging prevents impact on request processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuditLogService implements IAuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log an audit action asynchronously with cache invalidation.
     * 
     * @param userId     User performing the action
     * @param action     Action type (CREATE, UPDATE, DELETE, etc.)
     * @param entityType Entity type affected
     * @param entityId   Entity ID affected
     * @param ipAddress  Client IP address
     * @param userAgent  Client user agent
     * @param oldValue   Previous value (for updates)
     * @param newValue   New value (for updates)
     */
    @Async
    @Transactional
    @CacheEvict(value = { "auditLogs" }, allEntries = true)
    public void logAction(Long userId, String action, String entityType, Long entityId,
            String ipAddress, String userAgent, Object oldValue, Object newValue) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} on {} ID {}", userId, action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log authentication event asynchronously.
     * 
     * @param userId    User ID
     * @param action    Action type (LOGIN, LOGOUT, FAILED_LOGIN, etc.)
     * @param ipAddress Client IP
     * @param userAgent Client user agent
     */
    @Async
    @Transactional
    @CacheEvict(value = { "auditLogs" }, allEntries = true)
    public void logAuthenticationEvent(Long userId, String action, String ipAddress, String userAgent) {
        logAction(userId, action, "AUTHENTICATION", null, ipAddress, userAgent, null, null);
    }

    /**
     * Log password change event asynchronously.
     * 
     * @param userId    User ID
     * @param ipAddress Client IP
     * @param userAgent Client user agent
     */
    @Async
    @Transactional
    @CacheEvict(value = { "auditLogs", "userPermissions" }, allEntries = true)
    public void logPasswordChange(Long userId, String ipAddress, String userAgent) {
        logAction(userId, "PASSWORD_CHANGE", "USER", userId, ipAddress, userAgent, null, null);
    }

    /**
     * Get audit logs filtered by user ID.
     * Results are cached for 15 minutes.
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Cacheable(value = "auditLogs", key = "'user-' + #userId + '-page-' + #pageable.pageNumber")
    public Page<AuditLog> getAuditLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit logs filtered by action type.
     * Results are cached for 15 minutes.
     * 
     * @param action   Action type
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Cacheable(value = "auditLogs", key = "'action-' + #action + '-page-' + #pageable.pageNumber")
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get audit logs filtered by entity.
     * Results are cached for 15 minutes.
     * 
     * @param entityType Entity type
     * @param entityId   Entity ID
     * @param pageable   Pagination parameters
     * @return Page of audit logs
     */
    @Cacheable(value = "auditLogs", key = "'entity-' + #entityType + '-' + #entityId + '-page-' + #pageable.pageNumber")
    public Page<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    /**
     * Get audit logs within a date range.
     * Results are cached for 15 minutes.
     * 
     * @param start    Start date
     * @param end      End date
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Cacheable(value = "auditLogs", key = "'range-' + #start + '-' + #end + '-page-' + #pageable.pageNumber")
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable);
    }

    /**
     * Get all audit logs with pagination.
     * Results are cached for 15 minutes.
     * 
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Cacheable(value = "auditLogs", key = "'all-page-' + #pageable.pageNumber")
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}

package com.example.buildnest_ecommerce.service.audit;

import com.example.buildnest_ecommerce.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Interface for Audit Log Service operations.
 * Defines contract for audit logging and retrieval.
 */
public interface IAuditLogService {

    /**
     * Log an audit action asynchronously.
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
    void logAction(Long userId, String action, String entityType, Long entityId,
            String ipAddress, String userAgent, Object oldValue, Object newValue);

    /**
     * Log authentication event asynchronously.
     * 
     * @param userId    User ID
     * @param action    Action type (LOGIN, LOGOUT, FAILED_LOGIN, etc.)
     * @param ipAddress Client IP
     * @param userAgent Client user agent
     */
    void logAuthenticationEvent(Long userId, String action, String ipAddress, String userAgent);

    /**
     * Log password change event asynchronously.
     * 
     * @param userId    User ID
     * @param ipAddress Client IP
     * @param userAgent Client user agent
     */
    void logPasswordChange(Long userId, String ipAddress, String userAgent);

    /**
     * Get audit logs filtered by user ID.
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    Page<AuditLog> getAuditLogsByUserId(Long userId, Pageable pageable);

    /**
     * Get audit logs filtered by action type.
     * 
     * @param action   Action type
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable);

    /**
     * Get audit logs filtered by entity.
     * 
     * @param entityType Entity type
     * @param entityId   Entity ID
     * @param pageable   Pagination parameters
     * @return Page of audit logs
     */
    Page<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, Pageable pageable);

    /**
     * Get audit logs within a date range.
     * 
     * @param start    Start date
     * @param end      End date
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    Page<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Get all audit logs with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    Page<AuditLog> getAllAuditLogs(Pageable pageable);
}

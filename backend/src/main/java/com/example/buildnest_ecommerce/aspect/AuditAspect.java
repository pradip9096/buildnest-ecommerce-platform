package com.example.buildnest_ecommerce.aspect;

import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Audit Aspect for cross-cutting audit logging concerns.
 *
 * Intercepts methods annotated with {@link Auditable} and records
 * detailed audit logs including user, action, timestamp, IP address,
 * and request/response details.
 *
 * Features:
 * - Automatic audit trail creation for sensitive operations
 * - Captures user identity, IP address, and user agent
 * - Records before/after values for UPDATE and DELETE operations
 * - Integrates with Elasticsearch for audit log ingestion
 * - Exception handling with audit on failure
 *
 * Usage:
 * 
 * <pre>
 * &#64;Auditable(action = "UPDATE_USER", description = "User profile update")
 * public void updateUserProfile(User user) {
 *     // Method implementation
 * }
 * </pre>
 *
 * Performance Considerations:
 * - Uses AOP for zero-intrusion audit logging
 * - Async audit log ingestion to prevent performance impact
 * - Spring Security integration for current user detection
 * - Request context extracted from HTTP servlet for IP/UA tracking
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 * @see Auditable
 * @see AuditLogService
 *
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Autowired(required = false)
    private ElasticsearchIngestionService elasticsearchIngestionService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(com.example.buildnest_ecommerce.aspect.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        // Get current user
        Long userId = getCurrentUserId();

        // Get request details
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = request != null ? getClientIP(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        Object result;
        Object oldValue = null;

        try {
            // Capture input arguments as oldValue for modification actions
            if (auditable.action().contains("UPDATE") || auditable.action().contains("DELETE")
                    || auditable.action().contains("PASSWORD")) {
                oldValue = joinPoint.getArgs();
            }

            // Execute the method
            result = joinPoint.proceed();

            // Log the action
            Long entityId = extractEntityId(result, joinPoint.getArgs());
            auditLogService.logAction(
                    userId,
                    auditable.action(),
                    auditable.entityType(),
                    entityId,
                    ipAddress,
                    userAgent,
                    oldValue,
                    result);

            // Also index in Elasticsearch for centralized analytics (RQ-ES-LOG-04) if
            // available
            if (elasticsearchIngestionService != null) {
                try {
                    elasticsearchIngestionService.indexAuditLog(
                            userId,
                            auditable.action(),
                            auditable.entityType(),
                            entityId,
                            ipAddress,
                            userAgent,
                            oldValue != null ? oldValue.toString() : null,
                            result != null ? result.toString() : null);
                } catch (Exception e) {
                    log.warn("Failed to index audit log in Elasticsearch: {}", e.getMessage());
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error in audited method", e);
            throw e;
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                return userDetails.getId();
            }
        } catch (Exception e) {
            log.debug("Could not get current user ID", e);
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private Long extractEntityId(Object result, Object[] args) {
        // Try to extract ID from result
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception ignored) {
            }
        }

        // Try to extract ID from arguments
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        return null;
    }
}

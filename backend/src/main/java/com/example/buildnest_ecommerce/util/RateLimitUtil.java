package com.example.buildnest_ecommerce.util;

import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitUtil {

    private final RateLimiterService rateLimiterService;

    @Value("${rate.limit.login.requests:5}")
    private int loginRequests;

    @Value("${rate.limit.login.duration:60}")
    private int loginDuration;

    @Value("${rate.limit.password-reset.requests:3}")
    private int passwordResetRequests;

    @Value("${rate.limit.password-reset.duration:3600}")
    private int passwordResetDuration;

    @Value("${rate.limit.refresh-token.requests:10}")
    private int refreshTokenRequests;

    @Value("${rate.limit.refresh-token.duration:60}")
    private int refreshTokenDuration;

    @Value("${rate.limit.admin.requests:30}")
    private int adminRequests;

    @Value("${rate.limit.admin.duration:60}")
    private int adminDuration;

    public boolean isAllowed(HttpServletRequest request, String endpoint) {
        return isAllowed(request, endpoint, null);
    }

    public long getRetryAfterSeconds(HttpServletRequest request, String endpoint) {
        return getRetryAfterSeconds(request, endpoint, null);
    }

    public boolean isAllowed(HttpServletRequest request, String endpoint, Long subjectId) {
        String key = buildKey(request, endpoint, subjectId);
        int limit = resolveLimit(endpoint);
        Duration window = resolveWindow(endpoint);
        return rateLimiterService.isAllowed(key, limit, window);
    }

    public long getRetryAfterSeconds(HttpServletRequest request, String endpoint, Long subjectId) {
        String key = buildKey(request, endpoint, subjectId);
        return rateLimiterService.getRetryAfterSeconds(key);
    }

    private String buildKey(HttpServletRequest request, String endpoint, Long subjectId) {
        String subjectPart = subjectId != null ? ":user:" + subjectId : "";
        return getClientIP(request) + ":" + endpoint + subjectPart;
    }

    private int resolveLimit(String endpoint) {
        if (endpoint.contains("login")) {
            return loginRequests;
        }
        if (endpoint.contains("password")) {
            return passwordResetRequests;
        }
        if (endpoint.contains("refresh")) {
            return refreshTokenRequests;
        }
        if (endpoint.contains("admin")) {
            return adminRequests;
        }
        return loginRequests;
    }

    private Duration resolveWindow(String endpoint) {
        if (endpoint.contains("login")) {
            return Duration.ofSeconds(loginDuration);
        }
        if (endpoint.contains("password")) {
            return Duration.ofSeconds(passwordResetDuration);
        }
        if (endpoint.contains("refresh")) {
            return Duration.ofSeconds(refreshTokenDuration);
        }
        if (endpoint.contains("admin")) {
            return Duration.ofSeconds(adminDuration);
        }
        return Duration.ofSeconds(loginDuration);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

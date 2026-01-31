package com.example.buildnest_ecommerce.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Custom health indicator for Redis connectivity.
 * Provides detailed health checks for the Redis cache and rate limiting storage.
 * 
 * @author BuildNest Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;
    
    /**
     * Performs a health check on the Redis connection.
     * 
     * @return Health status with connection details
     */
    @Override
    public Health health() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            
            try {
                // Perform PING command
                long startTime = System.currentTimeMillis();
                String pong = connection.ping();
                long responseTime = System.currentTimeMillis() - startTime;
                
                if ("PONG".equals(pong)) {
                    // Get server info
                    @SuppressWarnings("deprecation")
                    Properties info = connection.info();
                    String redisVersion = info.getProperty("redis_version", "unknown");
                    String uptime = info.getProperty("uptime_in_seconds", "unknown");
                    String connectedClients = info.getProperty("connected_clients", "unknown");
                    String usedMemory = info.getProperty("used_memory_human", "unknown");
                    
                    Health.Builder builder = Health.up()
                            .withDetail("cache", "Redis")
                            .withDetail("status", "Available")
                            .withDetail("responseTime", responseTime + "ms")
                            .withDetail("version", redisVersion)
                            .withDetail("uptime", uptime + "s")
                            .withDetail("connectedClients", connectedClients)
                            .withDetail("usedMemory", usedMemory);
                    
                    // Warn if response time is high
                    if (responseTime > 100) {
                        builder.withDetail("warning", "High response time detected");
                    }
                    
                    return builder.build();
                } else {
                    return Health.down()
                            .withDetail("cache", "Redis")
                            .withDetail("status", "PING command failed")
                            .withDetail("response", pong)
                            .build();
                }
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("cache", "Redis")
                    .withDetail("status", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }
}

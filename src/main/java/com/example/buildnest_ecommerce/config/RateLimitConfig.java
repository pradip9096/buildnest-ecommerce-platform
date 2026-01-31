package com.example.buildnest_ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration for distributed caching and rate limiting.
 * This configuration provides RedisTemplate bean used by:
 * - RateLimiterService for distributed rate limiting
 * - CacheConfig for Spring Cache abstraction
 * - AuditLogService for distributed caching
 */
@Configuration
public class RateLimitConfig {
    
    /**
     * Configure RedisTemplate with proper serializers for distributed operations.
     * Uses StringRedisSerializer for keys and GenericToStringSerializer for values.
     * 
     * @param connectionFactory Redis connection factory
     * @return Configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(Object.class));
        return template;
    }
}

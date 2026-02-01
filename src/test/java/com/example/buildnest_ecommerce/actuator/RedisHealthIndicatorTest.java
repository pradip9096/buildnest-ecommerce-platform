package com.example.buildnest_ecommerce.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisHealthIndicatorTest {

    @Test
    @SuppressWarnings("deprecation")
    void healthUpWhenPong() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        Properties info = new Properties();
        info.setProperty("redis_version", "7");
        info.setProperty("uptime_in_seconds", "10");
        info.setProperty("connected_clients", "1");
        info.setProperty("used_memory_human", "1M");
        when(connection.info()).thenReturn(info);

        RedisHealthIndicator indicator = new RedisHealthIndicator(factory);
        Health health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("Redis", health.getDetails().get("cache"));
        verify(connection).close();
    }

    @Test
    void healthDownWhenPingFails() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("NO");

        RedisHealthIndicator indicator = new RedisHealthIndicator(factory);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        verify(connection).close();
    }

    @Test
    void healthDownWhenExceptionThrown() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        when(factory.getConnection()).thenThrow(new RuntimeException("fail"));

        RedisHealthIndicator indicator = new RedisHealthIndicator(factory);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Connection failed", health.getDetails().get("status"));
    }
}

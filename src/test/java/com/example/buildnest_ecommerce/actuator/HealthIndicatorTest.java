package com.example.buildnest_ecommerce.actuator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Custom health indicator tests")
class HealthIndicatorTest {

    @Test
    @DisplayName("Redis health should be up on PONG")
    void testRedisHealthUp() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        Properties props = new Properties();
        props.setProperty("redis_version", "7.0");
        props.setProperty("uptime_in_seconds", "10");
        props.setProperty("connected_clients", "1");
        props.setProperty("used_memory_human", "10M");
        when(connection.info()).thenReturn(props);

        RedisHealthIndicator indicator = new RedisHealthIndicator(factory);
        Health health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());
        verify(connection).close();
    }

    @Test
    @DisplayName("Redis health should be down on ping failure")
    void testRedisHealthDown() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("ERR");

        RedisHealthIndicator indicator = new RedisHealthIndicator(factory);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        verify(connection).close();
    }

    @Test
    @DisplayName("Database health should be up when connection is valid")
    void testDatabaseHealthUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);
        when(connection.getCatalog()).thenReturn("test");
        when(connection.getAutoCommit()).thenReturn(true);

        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(dataSource);
        Health health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());
        verify(connection).close();
    }

    @Test
    @DisplayName("Database health should be down on SQL exception")
    void testDatabaseHealthDown() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("fail", "state", 42));

        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(dataSource);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
    }
}

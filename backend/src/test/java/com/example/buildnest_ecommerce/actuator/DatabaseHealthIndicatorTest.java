package com.example.buildnest_ecommerce.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Test
    void healthIsUpWhenConnectionValid() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);
        when(connection.getCatalog()).thenReturn("catalog");
        when(connection.getAutoCommit()).thenReturn(true);

        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(dataSource);
        Health health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("MySQL", health.getDetails().get("database"));
        assertEquals("Available", health.getDetails().get("status"));
    }

    @Test
    void healthIsDownWhenConnectionInvalid() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(false);

        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(dataSource);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Connection validation failed", health.getDetails().get("status"));
    }

    @Test
    void healthIsDownWhenSQLExceptionOccurs() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        SQLException exception = new SQLException("boom", "42S02", 500);
        when(dataSource.getConnection()).thenThrow(exception);

        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(dataSource);
        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Connection failed", health.getDetails().get("status"));
        assertEquals("42S02", health.getDetails().get("sqlState"));
        assertEquals(500, health.getDetails().get("errorCode"));
    }
}

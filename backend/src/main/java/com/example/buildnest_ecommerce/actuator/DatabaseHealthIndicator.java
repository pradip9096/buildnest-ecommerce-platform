package com.example.buildnest_ecommerce.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom health indicator for database connectivity.
 * Provides detailed health checks for the MySQL database connection.
 * 
 * @author BuildNest Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    /**
     * Performs a health check on the database connection.
     * 
     * @return Health status with connection details
     */
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            // Test if the connection is valid within 3 seconds
            if (connection.isValid(3)) {
                return Health.up()
                        .withDetail("database", "MySQL")
                        .withDetail("status", "Available")
                        .withDetail("validationQuery", "isValid(3)")
                        .withDetail("catalog", connection.getCatalog())
                        .withDetail("autoCommit", connection.getAutoCommit())
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "MySQL")
                        .withDetail("status", "Connection validation failed")
                        .withDetail("error", "Connection.isValid() returned false")
                        .build();
            }
        } catch (SQLException e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "MySQL")
                    .withDetail("status", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .withDetail("sqlState", e.getSQLState())
                    .withDetail("errorCode", e.getErrorCode())
                    .build();
        }
    }
}

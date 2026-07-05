package com.example.buildnest_ecommerce.integration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the Liquibase changesets that create and seed {@code shipping_methods}
 * (20260624-006, 20260704-013) apply correctly against a real database (#304).
 *
 * <p>Runs Liquibase directly against a throwaway H2 connection, bypassing the
 * application context entirely. A full {@code @SpringBootTest} cannot verify this:
 * the {@code test} profile's {@code spring.jpa.hibernate.ddl-auto=create-drop} runs
 * Hibernate's schema generation *after* Liquibase and drops/recreates every
 * entity-mapped table — including {@code shipping_methods} — discarding the seeded
 * row before any test body runs. Production uses {@code ddl-auto=validate}, so this
 * is a test-environment-only interaction, not a real-world data loss risk.
 */
class ShippingMethodSeedMigrationTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:shipping_seed_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        connection = DriverManager.getConnection(jdbcUrl, "sa", "");

        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        // Not try-with-resources: Liquibase.close() closes the underlying Database,
        // which closes this shared `connection` — needed by the test bodies afterward.
        Liquibase liquibase = new Liquibase(
                "db/changelog/db.changelog-master.xml",
                new ClassLoaderResourceAccessor(),
                database);
        liquibase.update("");
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    @DisplayName("shipping_methods contains an active default 'Standard Delivery' row after migration")
    void seedsDefaultActiveShippingMethod() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name, is_active, base_cost, estimated_days_min, estimated_days_max " +
                             "FROM shipping_methods WHERE name = 'Standard Delivery'")) {

            assertThat(rs.next())
                    .as("Expected a default 'Standard Delivery' shipping method seeded by Liquibase")
                    .isTrue();
            assertThat(rs.getBoolean("is_active"))
                    .as("Seeded default shipping method must be active so checkout is never blocked on a fresh deploy")
                    .isTrue();
            assertThat(rs.getBigDecimal("base_cost")).isNotNull();
            assertThat(rs.getObject("estimated_days_min")).isNotNull();
            assertThat(rs.getObject("estimated_days_max")).isNotNull();
        }
    }

    @Test
    @DisplayName("at least one active shipping method exists after migration (checkout precondition)")
    void atLeastOneActiveShippingMethodExists() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) AS active_count FROM shipping_methods WHERE is_active = TRUE")) {
            rs.next();
            assertThat(rs.getInt("active_count"))
                    .as("Checkout requires at least one active shipping method on a fresh deploy")
                    .isGreaterThanOrEqualTo(1);
        }
    }
}

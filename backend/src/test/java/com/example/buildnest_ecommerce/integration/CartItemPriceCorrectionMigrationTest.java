package com.example.buildnest_ecommerce.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the data-correction SQL in Liquibase changeset
 * {@code 20260705-014-correct-cart-item-prices-for-discounted-products} (#305).
 *
 * <p>Neither {@code products} nor {@code cart_items} is created by any Liquibase changeset in
 * this repo (see the changeset's own comment, and the precedent in
 * {@code 20260704-012-alter-product-add-is-featured.xml}) — both tables only exist via
 * Hibernate, so a full-master-changelog Liquibase run (the pattern used in
 * {@code ShippingMethodSeedMigrationTest}) cannot exercise this changeset: its
 * {@code preConditions onFail="MARK_RAN"} guard makes it a no-op whenever the tables don't
 * exist yet, which is exactly the case Liquibase sees running on its own. Instead, this test
 * creates a minimal, real-column-matching {@code products}/{@code cart_items} schema directly
 * via JDBC, seeds rows reflecting the pre-fix bug state, and runs the changeset's own UPDATE
 * statement (duplicated below — must be kept in sync with the changeset's {@code <sql>} block)
 * against it.
 */
class CartItemPriceCorrectionMigrationTest {

    // Mirrors the <sql> block in
    // db/changelog/changes/20260705-014-correct-cart-item-prices-for-discounted-products.xml
    private static final String CORRECTION_SQL = """
            UPDATE cart_items
            SET price = (SELECT p.discount_price FROM products p WHERE p.id = cart_items.product_id)
            WHERE product_id IN (
                SELECT p.id FROM products p
                WHERE p.discount_price IS NOT NULL
                  AND p.discount_price < p.price
            )
            AND price = (SELECT p.price FROM products p WHERE p.id = cart_items.product_id)
            """;

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:cart_price_fix_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        connection = DriverManager.getConnection(jdbcUrl, "sa", "");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE products (
                        id BIGINT PRIMARY KEY,
                        price DECIMAL(19,2) NOT NULL,
                        discount_price DECIMAL(19,2)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE cart_items (
                        id BIGINT PRIMARY KEY,
                        product_id BIGINT NOT NULL,
                        quantity INT NOT NULL,
                        price DECIMAL(19,2) NOT NULL
                    )
                    """);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    @DisplayName("cart_items overpriced at a discounted product's full price are corrected to discount_price")
    void correctsOverpricedCartItemForDiscountedProduct() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO products (id, price, discount_price) VALUES (1, 380.00, 350.00)");
            stmt.execute("INSERT INTO cart_items (id, product_id, quantity, price) VALUES (100, 1, 2, 380.00)");

            stmt.executeUpdate(CORRECTION_SQL);

            try (ResultSet rs = stmt.executeQuery("SELECT price FROM cart_items WHERE id = 100")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBigDecimal("price"))
                        .as("overpriced cart item must be corrected to the product's discount_price")
                        .isEqualByComparingTo(BigDecimal.valueOf(350.00));
            }
        }
    }

    @Test
    @DisplayName("cart_items for a non-discounted product are left unchanged")
    void leavesNonDiscountedProductUnchanged() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO products (id, price, discount_price) VALUES (2, 200.00, NULL)");
            stmt.execute("INSERT INTO cart_items (id, product_id, quantity, price) VALUES (101, 2, 1, 200.00)");

            stmt.executeUpdate(CORRECTION_SQL);

            try (ResultSet rs = stmt.executeQuery("SELECT price FROM cart_items WHERE id = 101")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBigDecimal("price")).isEqualByComparingTo(BigDecimal.valueOf(200.00));
            }
        }
    }

    @Test
    @DisplayName("cart_items already priced at discount_price are left unchanged (idempotent)")
    void isIdempotentOnAlreadyCorrectedRows() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO products (id, price, discount_price) VALUES (3, 500.00, 450.00)");
            stmt.execute("INSERT INTO cart_items (id, product_id, quantity, price) VALUES (102, 3, 1, 450.00)");

            int updated = stmt.executeUpdate(CORRECTION_SQL);

            assertThat(updated).isZero();
            try (ResultSet rs = stmt.executeQuery("SELECT price FROM cart_items WHERE id = 102")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getBigDecimal("price")).isEqualByComparingTo(BigDecimal.valueOf(450.00));
            }
        }
    }
}

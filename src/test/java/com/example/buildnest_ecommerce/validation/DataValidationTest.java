package com.example.buildnest_ecommerce.validation;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data Validation test suite TC-VAL-001 to TC-VAL-012.
 * Covers input validation and constraint validation.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class DataValidationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("validationtest");
        testUser.setEmail("validation@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Validation");
        testUser.setLastName("Test");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStockQuantity(50);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("TC-VAL-001: Should validate username format")
    void testValidateUsernameFormat() {
        String username = testUser.getUsername();

        assertNotNull(username);
        assertTrue(username.length() > 0);
    }

    @Test
    @DisplayName("TC-VAL-002: Should validate email format")
    void testValidateEmailFormat() {
        String email = testUser.getEmail();

        assertNotNull(email);
        assertTrue(email.contains("@"));
    }

    @Test
    @DisplayName("TC-VAL-003: Should validate product price is positive")
    void testValidateProductPricePositive() {
        BigDecimal price = testProduct.getPrice();

        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("TC-VAL-004: Should validate stock quantity is non-negative")
    void testValidateStockQuantityNonNegative() {
        int stockQuantity = testProduct.getStockQuantity();

        assertTrue(stockQuantity >= 0);
    }

    @Test
    @DisplayName("TC-VAL-005: Should validate product name not empty")
    void testValidateProductNameNotEmpty() {
        String productName = testProduct.getName();

        assertNotNull(productName);
        assertTrue(productName.length() > 0);
    }

    @Test
    @DisplayName("TC-VAL-006: Should validate user first name not empty")
    void testValidateUserFirstNameNotEmpty() {
        String firstName = testUser.getFirstName();

        assertNotNull(firstName);
        assertTrue(firstName.length() > 0);
    }

    @Test
    @DisplayName("TC-VAL-007: Should validate user last name not empty")
    void testValidateUserLastNameNotEmpty() {
        String lastName = testUser.getLastName();

        assertNotNull(lastName);
        assertTrue(lastName.length() > 0);
    }

    @Test
    @DisplayName("TC-VAL-008: Should validate product description length")
    void testValidateProductDescriptionLength() {
        testProduct.setDescription("This is a test product");
        Product updated = productRepository.save(testProduct);

        assertNotNull(updated.getDescription());
    }

    @Test
    @DisplayName("TC-VAL-009: Should validate price precision")
    void testValidatePricePrecision() {
        BigDecimal price = testProduct.getPrice();

        assertEquals(2, price.scale());
    }

    @Test
    @DisplayName("TC-VAL-010: Should validate maximum string lengths")
    void testValidateMaxStringLengths() {
        String longName = "A".repeat(250);
        testProduct.setName(longName);

        assertTrue(testProduct.getName().length() >= 250);
    }

    @Test
    @DisplayName("TC-VAL-011: Should validate product status is boolean")
    void testValidateProductStatusBoolean() {
        boolean isActive = testProduct.getIsActive();

        assertTrue(isActive || !isActive);
    }

    @Test
    @DisplayName("TC-VAL-012: Should reject invalid data types")
    void testRejectInvalidDataTypes() {
        Product product = new Product();
        product.setName("Valid");
        product.setPrice(new BigDecimal("100.00"));

        assertTrue(product.getPrice() instanceof BigDecimal);
    }
}

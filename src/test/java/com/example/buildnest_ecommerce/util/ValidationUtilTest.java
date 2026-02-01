package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtil comprehensive tests")
class ValidationUtilTest {

    @Test
    void validateProductRequestPassesForValidData() {
        ValidationUtil util = new ValidationUtil();
        assertDoesNotThrow(() -> util.validateProductRequest("Product", BigDecimal.TEN, "ABC-123"));
    }

    @Test
    void validateProductRequestThrowsForInvalid() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validateProductRequest("", BigDecimal.TEN, "ABC"));
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest("Product", BigDecimal.ZERO, "ABC"));
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest("Product", BigDecimal.ONE, "bad"));
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest("A".repeat(256), BigDecimal.ONE, "ABC"));
    }

    @Test
    void validateQuantityAndEmailAndPassword() {
        ValidationUtil util = new ValidationUtil();

        assertDoesNotThrow(() -> util.validateQuantity(1));
        assertThrows(IllegalArgumentException.class, () -> util.validateQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> util.validateQuantity(10001));

        assertDoesNotThrow(() -> util.validateEmail("user@example.com"));
        assertThrows(IllegalArgumentException.class, () -> util.validateEmail("bad"));
        assertThrows(IllegalArgumentException.class, () -> util.validateEmail(null));

        assertDoesNotThrow(() -> util.validatePassword("StrongPass1!"));
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword("weak"));
        assertThrows(IllegalArgumentException.class,
                () -> util.validatePassword("A".repeat(129) + "a1!"));
    }

    @Test
    @DisplayName("Should reject product with null name")
    void testProductNameNull() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest(null, BigDecimal.TEN, "ABC-123"));
    }

    @Test
    @DisplayName("Should reject product with blank name")
    void testProductNameBlank() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest("   ", BigDecimal.TEN, "ABC-123"));
    }

    @Test
    @DisplayName("Should reject product with null price")
    void testProductPriceNull() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validateProductRequest("Product", null, "ABC-123"));
    }

    @Test
    @DisplayName("Should reject product with negative price")
    void testProductPriceNegative() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest("Product", new BigDecimal("-10"), "ABC-123"));
    }

    @Test
    @DisplayName("Should accept null SKU")
    void testProductSkuNull() {
        ValidationUtil util = new ValidationUtil();
        assertDoesNotThrow(() -> util.validateProductRequest("Product", BigDecimal.TEN, null));
    }

    @Test
    @DisplayName("Should reject SKU with invalid pattern")
    void testProductSkuInvalidPattern() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest("Product", BigDecimal.TEN, "abc"));
        assertThrows(IllegalArgumentException.class,
                () -> util.validateProductRequest("Product", BigDecimal.TEN, "AB"));
    }

    @Test
    @DisplayName("Should accept valid SKU patterns")
    void testProductSkuValidPattern() {
        ValidationUtil util = new ValidationUtil();
        assertDoesNotThrow(() -> util.validateProductRequest("Product", BigDecimal.TEN, "ABC123"));
        assertDoesNotThrow(() -> util.validateProductRequest("Product", BigDecimal.TEN, "X-Y-Z"));
    }

    @Test
    @DisplayName("Should validate quantity boundaries")
    void testQuantityBoundaries() {
        ValidationUtil util = new ValidationUtil();
        assertDoesNotThrow(() -> util.validateQuantity(1));
        assertDoesNotThrow(() -> util.validateQuantity(10000));
        assertDoesNotThrow(() -> util.validateQuantity(5000));
        assertThrows(IllegalArgumentException.class, () -> util.validateQuantity(-1));
        assertThrows(IllegalArgumentException.class, () -> util.validateQuantity(10001));
    }

    @Test
    @DisplayName("Should reject email without domain")
    void testEmailNoDomain() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validateEmail("user@"));
        assertThrows(IllegalArgumentException.class, () -> util.validateEmail("@example.com"));
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void testEmailValidFormats() {
        ValidationUtil util = new ValidationUtil();
        assertDoesNotThrow(() -> util.validateEmail("user@example.com"));
        assertDoesNotThrow(() -> util.validateEmail("user+tag@example.co.uk"));
        assertDoesNotThrow(() -> util.validateEmail("user_name@example.com"));
        assertDoesNotThrow(() -> util.validateEmail("user-name@example.com"));
    }

    @Test
    @DisplayName("Should validate password uppercase requirement")
    void testPasswordUppercaseRequirement() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword("lowercase12!"));
        assertDoesNotThrow(() -> util.validatePassword("Uppercase12!"));
    }

    @Test
    @DisplayName("Should validate password lowercase requirement")
    void testPasswordLowercaseRequirement() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword("UPPERCASE12!"));
        assertDoesNotThrow(() -> util.validatePassword("UpperCaseLower12!"));
    }

    @Test
    @DisplayName("Should validate password digit requirement")
    void testPasswordDigitRequirement() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword("NoDigits!abc"));
        assertDoesNotThrow(() -> util.validatePassword("WithDigit12!"));
    }

    @Test
    @DisplayName("Should validate password special character requirement")
    void testPasswordSpecialCharRequirement() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword("NoSpecialChar12"));
        assertDoesNotThrow(() -> util.validatePassword("WithSpecial12!"));
        assertDoesNotThrow(() -> util.validatePassword("WithSpecial12@"));
        assertDoesNotThrow(() -> util.validatePassword("WithSpecial12#"));
        assertDoesNotThrow(() -> util.validatePassword("WithSpecial12$"));
    }

    @Test
    @DisplayName("Should validate password length boundaries")
    void testPasswordLengthBoundaries() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword("Short1!"));
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword("A".repeat(129)));
        assertDoesNotThrow(() -> util.validatePassword("ValidPass12!"));
        assertDoesNotThrow(() -> util.validatePassword("A".repeat(10) + "a2!"));
    }

    @Test
    @DisplayName("Should reject null password")
    void testPasswordNull() {
        ValidationUtil util = new ValidationUtil();
        assertThrows(IllegalArgumentException.class, () -> util.validatePassword(null));
    }
}

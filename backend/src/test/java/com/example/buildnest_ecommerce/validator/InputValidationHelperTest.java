package com.example.buildnest_ecommerce.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InputValidationHelper tests")
class InputValidationHelperTest {

    private final InputValidationHelper helper = new InputValidationHelper();

    @Test
    @DisplayName("Should validate email and username")
    void testEmailAndUsername() {
        assertFalse(helper.validateEmail("bad").isValid());
        assertTrue(helper.validateEmail("user@example.com").isValid());
        assertFalse(helper.validateEmail(null).isValid());
        assertFalse(helper.validateEmail("   ").isValid());
        assertFalse(helper.validateEmail("a".repeat(256) + "@example.com").isValid());

        assertFalse(helper.validateUsername("ab").isValid());
        assertTrue(helper.validateUsername("valid_user").isValid());
        assertFalse(helper.validateUsername(null).isValid());
        assertFalse(helper.validateUsername(" ").isValid());
        assertFalse(helper.validateUsername("bad!name").isValid());
    }

    @Test
    @DisplayName("Should validate password and phone")
    void testPasswordAndPhone() {
        assertFalse(helper.validatePassword("short").isValid());
        assertTrue(helper.validatePassword("ValidPass@123").isValid());
        assertFalse(helper.validatePassword("nouppercase@123").isValid());
        assertFalse(helper.validatePassword("NOLOWERCASE@123").isValid());
        assertFalse(helper.validatePassword("NoDigits@abc").isValid());
        assertFalse(helper.validatePassword("NoSpecial12345").isValid());
        assertFalse(helper.validatePassword("A".repeat(129) + "a1!").isValid());

        assertFalse(helper.validatePhoneNumber("000").isValid());
        assertTrue(helper.validatePhoneNumber("+14155552671").isValid());
        assertFalse(helper.validatePhoneNumber(null).isValid());
        assertFalse(helper.validatePhoneNumber("  ").isValid());
    }

    @Test
    @DisplayName("Should validate postal code, SKU, and product name")
    void testPostalSkuProductName() {
        assertFalse(helper.validatePostalCode("abc").isValid());
        assertTrue(helper.validatePostalCode("12345").isValid());
        assertFalse(helper.validatePostalCode(null).isValid());
        assertFalse(helper.validatePostalCode("  ").isValid());

        assertFalse(helper.validateSKU("ab").isValid());
        assertTrue(helper.validateSKU("ABC123").isValid());
        assertFalse(helper.validateSKU(null).isValid());
        assertFalse(helper.validateSKU("  ").isValid());

        assertFalse(helper.validateProductName("a").isValid());
        assertTrue(helper.validateProductName("Product-1").isValid());
        assertFalse(helper.validateProductName(null).isValid());
        assertFalse(helper.validateProductName("Bad@Name").isValid());
    }

    @Test
    @DisplayName("Should validate price and quantity")
    void testPriceAndQuantity() {
        assertFalse(helper.validatePrice(new BigDecimal("-1.00")).isValid());
        assertFalse(helper.validatePrice(new BigDecimal("1.999")).isValid());
        assertTrue(helper.validatePrice(new BigDecimal("10.00")).isValid());
        assertFalse(helper.validatePrice(null).isValid());
        assertFalse(helper.validatePrice(new BigDecimal("1000000.00")).isValid());

        assertFalse(helper.validateQuantity(-1).isValid());
        assertTrue(helper.validateQuantity(10).isValid());
        assertFalse(helper.validateQuantity(null).isValid());
        assertFalse(helper.validateQuantity(1000001).isValid());
    }

    @Test
    @DisplayName("Should validate order date and string length")
    void testOrderDateAndStringLength() {
        assertFalse(helper.validateOrderDate(LocalDate.now().plusDays(1)).isValid());
        assertTrue(helper.validateOrderDate(LocalDate.now()).isValid());
        assertFalse(helper.validateOrderDate(LocalDate.now().minusYears(2)).isValid());
        assertFalse(helper.validateOrderDate(null).isValid());

        assertFalse(helper.validateStringLength("ab", 3, 5).isValid());
        assertTrue(helper.validateStringLength("abcd", 3, 5).isValid());
        assertFalse(helper.validateStringLength(null, 1, 5).isValid());
        assertFalse(helper.validateStringLength("abcdef", 3, 5).isValid());
    }

    @Test
    @DisplayName("Should sanitize input")
    void testSanitizeInput() {
        String sanitized = helper.sanitizeInput("<script>alert('x')</script>");
        assertTrue(sanitized.contains("&lt;script&gt;"));
        assertNull(helper.sanitizeInput(null));
    }
}

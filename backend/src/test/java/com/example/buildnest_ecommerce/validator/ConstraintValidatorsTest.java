package com.example.buildnest_ecommerce.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for all package-private ConstraintValidator implementations
 * in the validator package (ValidEmail, ValidPassword, ValidPhoneNumber,
 * ValidPostalCode, ValidPrice, ValidQuantity, ValidSKU).
 */
@DisplayName("ConstraintValidator unit tests")
class ConstraintValidatorsTest {

    private final ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

    // ─── EmailValidator ────────────────────────────────────────────────────────

    @Test
    @DisplayName("EmailValidator — null is valid (defer to @NotNull)")
    void email_null_isValid() {
        assertTrue(new EmailValidator().isValid(null, ctx));
    }

    @Test
    @DisplayName("EmailValidator — standard address is valid")
    void email_standard_isValid() {
        assertTrue(new EmailValidator().isValid("user@example.com", ctx));
    }

    @Test
    @DisplayName("EmailValidator — address longer than 255 chars is invalid")
    void email_tooLong_isInvalid() {
        // 250 'a's + "@x.com" = 256 chars, which exceeds the 255-char limit
        String longEmail = "a".repeat(250) + "@x.com";
        assertFalse(new EmailValidator().isValid(longEmail, ctx));
    }

    @Test
    @DisplayName("EmailValidator — missing @ is invalid")
    void email_missingAt_isInvalid() {
        assertFalse(new EmailValidator().isValid("userexample.com", ctx));
    }

    @Test
    @DisplayName("EmailValidator — missing TLD is invalid")
    void email_missingTld_isInvalid() {
        assertFalse(new EmailValidator().isValid("user@example", ctx));
    }

    // ─── PasswordStrengthValidator ─────────────────────────────────────────────

    @Test
    @DisplayName("PasswordStrengthValidator — null is valid")
    void password_null_isValid() {
        assertTrue(new PasswordStrengthValidator().isValid(null, ctx));
    }

    @Test
    @DisplayName("PasswordStrengthValidator — strong password is valid")
    void password_strong_isValid() {
        assertTrue(new PasswordStrengthValidator().isValid("Str0ng@Password!", ctx));
    }

    @Test
    @DisplayName("PasswordStrengthValidator — too short (< 12) is invalid")
    void password_tooShort_isInvalid() {
        assertFalse(new PasswordStrengthValidator().isValid("Short@1A", ctx));
    }

    @Test
    @DisplayName("PasswordStrengthValidator — too long (> 128) is invalid")
    void password_tooLong_isInvalid() {
        String longPass = "A1!a" + "x".repeat(126); // 130 chars
        assertFalse(new PasswordStrengthValidator().isValid(longPass, ctx));
    }

    @Test
    @DisplayName("PasswordStrengthValidator — no uppercase is invalid")
    void password_noUppercase_isInvalid() {
        assertFalse(new PasswordStrengthValidator().isValid("nouppercase@123", ctx));
    }

    @Test
    @DisplayName("PasswordStrengthValidator — no lowercase is invalid")
    void password_noLowercase_isInvalid() {
        assertFalse(new PasswordStrengthValidator().isValid("NOLOWERCASE@123", ctx));
    }

    @Test
    @DisplayName("PasswordStrengthValidator — no digit is invalid")
    void password_noDigit_isInvalid() {
        assertFalse(new PasswordStrengthValidator().isValid("NoDigits@abcde", ctx));
    }

    @Test
    @DisplayName("PasswordStrengthValidator — no special character is invalid")
    void password_noSpecial_isInvalid() {
        assertFalse(new PasswordStrengthValidator().isValid("NoSpecial12345A", ctx));
    }

    // ─── PhoneNumberValidator ──────────────────────────────────────────────────

    @Test
    @DisplayName("PhoneNumberValidator — null is valid")
    void phone_null_isValid() {
        assertTrue(new PhoneNumberValidator().isValid(null, ctx));
    }

    @Test
    @DisplayName("PhoneNumberValidator — E.164 format is valid")
    void phone_e164_isValid() {
        assertTrue(new PhoneNumberValidator().isValid("+14155552671", ctx));
    }

    @Test
    @DisplayName("PhoneNumberValidator — dashes are stripped before validation")
    void phone_withDashes_isValid() {
        assertTrue(new PhoneNumberValidator().isValid("+1-415-555-2671", ctx));
    }

    @Test
    @DisplayName("PhoneNumberValidator — spaces are stripped before validation")
    void phone_withSpaces_isValid() {
        assertTrue(new PhoneNumberValidator().isValid("+44 20 7946 0958", ctx));
    }

    @Test
    @DisplayName("PhoneNumberValidator — all zeros is invalid")
    void phone_allZeros_isInvalid() {
        assertFalse(new PhoneNumberValidator().isValid("000", ctx));
    }

    @Test
    @DisplayName("PhoneNumberValidator — leading zero (non-E.164) is invalid")
    void phone_leadingZero_isInvalid() {
        assertFalse(new PhoneNumberValidator().isValid("+0123456789", ctx));
    }

    // ─── PostalCodeValidator ───────────────────────────────────────────────────

    @Test
    @DisplayName("PostalCodeValidator — null is valid")
    void postal_null_isValid() {
        assertTrue(new PostalCodeValidator().isValid(null, ctx));
    }

    @Test
    @DisplayName("PostalCodeValidator — 5-digit code is valid")
    void postal_fiveDigit_isValid() {
        assertTrue(new PostalCodeValidator().isValid("12345", ctx));
    }

    @Test
    @DisplayName("PostalCodeValidator — ZIP+4 format is valid")
    void postal_zipPlusFour_isValid() {
        assertTrue(new PostalCodeValidator().isValid("12345-6789", ctx));
    }

    @Test
    @DisplayName("PostalCodeValidator — alphabetic code is invalid")
    void postal_alpha_isInvalid() {
        assertFalse(new PostalCodeValidator().isValid("ABCDE", ctx));
    }

    @Test
    @DisplayName("PostalCodeValidator — 4-digit code is invalid (too short)")
    void postal_fourDigit_isInvalid() {
        assertFalse(new PostalCodeValidator().isValid("1234", ctx));
    }

    @Test
    @DisplayName("PostalCodeValidator — 6-digit code is invalid (too long)")
    void postal_sixDigit_isInvalid() {
        assertFalse(new PostalCodeValidator().isValid("123456", ctx));
    }

    // ─── PriceValidator ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PriceValidator — null is valid")
    void price_null_isValid() {
        assertTrue(new PriceValidator().isValid(null, ctx));
    }

    @Test
    @DisplayName("PriceValidator — positive price within range is valid")
    void price_withinRange_isValid() {
        assertTrue(new PriceValidator().isValid(new BigDecimal("99.99"), ctx));
    }

    @Test
    @DisplayName("PriceValidator — maximum boundary (999999.99) is valid")
    void price_atMaxBoundary_isValid() {
        assertTrue(new PriceValidator().isValid(new BigDecimal("999999.99"), ctx));
    }

    @Test
    @DisplayName("PriceValidator — zero is invalid (must be > 0)")
    void price_zero_isInvalid() {
        assertFalse(new PriceValidator().isValid(BigDecimal.ZERO, ctx));
    }

    @Test
    @DisplayName("PriceValidator — negative price is invalid")
    void price_negative_isInvalid() {
        assertFalse(new PriceValidator().isValid(new BigDecimal("-0.01"), ctx));
    }

    @Test
    @DisplayName("PriceValidator — above maximum (1000000.00) is invalid")
    void price_aboveMax_isInvalid() {
        assertFalse(new PriceValidator().isValid(new BigDecimal("1000000.00"), ctx));
    }

    // ─── QuantityValidator ─────────────────────────────────────────────────────

    @Test
    @DisplayName("QuantityValidator — null is valid")
    void quantity_null_isValid() {
        assertTrue(new QuantityValidator().isValid(null, ctx));
    }

    @Test
    @DisplayName("QuantityValidator — minimum boundary (1) is valid")
    void quantity_atMin_isValid() {
        assertTrue(new QuantityValidator().isValid(1, ctx));
    }

    @Test
    @DisplayName("QuantityValidator — maximum boundary (10000) is valid")
    void quantity_atMax_isValid() {
        assertTrue(new QuantityValidator().isValid(10000, ctx));
    }

    @Test
    @DisplayName("QuantityValidator — zero is invalid")
    void quantity_zero_isInvalid() {
        assertFalse(new QuantityValidator().isValid(0, ctx));
    }

    @Test
    @DisplayName("QuantityValidator — 10001 exceeds maximum")
    void quantity_aboveMax_isInvalid() {
        assertFalse(new QuantityValidator().isValid(10001, ctx));
    }

    @Test
    @DisplayName("QuantityValidator — negative value is invalid")
    void quantity_negative_isInvalid() {
        assertFalse(new QuantityValidator().isValid(-1, ctx));
    }

    // ─── SKUValidator ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("SKUValidator — null is valid")
    void sku_null_isValid() {
        assertTrue(new SKUValidator().isValid(null, ctx));
    }

    @Test
    @DisplayName("SKUValidator — 3-char minimum-length code is valid")
    void sku_threeChars_isValid() {
        assertTrue(new SKUValidator().isValid("ABC", ctx));
    }

    @Test
    @DisplayName("SKUValidator — alphanumeric with hyphens is valid")
    void sku_alphanumericWithHyphen_isValid() {
        assertTrue(new SKUValidator().isValid("ABC-123-XYZ", ctx));
    }

    @Test
    @DisplayName("SKUValidator — lowercase letters are invalid")
    void sku_lowercase_isInvalid() {
        assertFalse(new SKUValidator().isValid("abc123", ctx));
    }

    @Test
    @DisplayName("SKUValidator — 2-char code is too short")
    void sku_tooShort_isInvalid() {
        assertFalse(new SKUValidator().isValid("AB", ctx));
    }

    @Test
    @DisplayName("SKUValidator — 21-char code exceeds maximum length")
    void sku_tooLong_isInvalid() {
        assertFalse(new SKUValidator().isValid("A".repeat(21), ctx));
    }

    @Test
    @DisplayName("SKUValidator — special characters other than hyphen are invalid")
    void sku_specialChars_isInvalid() {
        assertFalse(new SKUValidator().isValid("ABC@123", ctx));
    }
}

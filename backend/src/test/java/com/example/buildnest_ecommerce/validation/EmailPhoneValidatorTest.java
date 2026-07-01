package com.example.buildnest_ecommerce.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("Email and phone validators")
class EmailPhoneValidatorTest {

    @Test
    @DisplayName("Should validate email formats")
    void testEmailValidator() {
        EmailValidator validator = new EmailValidator();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(null, context));
        assertTrue(validator.isValid("user@example.com", context));
        assertFalse(validator.isValid("invalid-email", context));
        assertFalse(validator.isValid("user@invalid", context));
    }

    @Test
    @DisplayName("EmailValidator — address exceeding 254 chars is invalid")
    void testEmailValidator_tooLong_isInvalid() {
        EmailValidator validator = new EmailValidator();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        // 245 'a's + "@example.com" = 257 chars — passes regex prefix but fails length guard
        String longEmail = "a".repeat(245) + "@example.com";
        assertFalse(validator.isValid(longEmail, context));
    }

    @Test
    @DisplayName("Should validate phone number formats")
    void testPhoneNumberValidator() {
        PhoneNumberValidator validator = new PhoneNumberValidator();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(null, context));
        assertTrue(validator.isValid("+14155552671", context));
        assertTrue(validator.isValid("+1-415-555-2671", context));
        assertFalse(validator.isValid("000-000", context));
        assertFalse(validator.isValid("+0", context));
    }

    @Test
    @DisplayName("PhoneNumberValidator — parentheses and dots stripped before match")
    void testPhoneNumberValidator_parenthesesAndDots_stripped() {
        PhoneNumberValidator validator = new PhoneNumberValidator();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        // validation/ PhoneNumberValidator strips [\s\-().]+ — test the extra chars
        assertTrue(validator.isValid("+1(415)555.2671", context));
    }
}

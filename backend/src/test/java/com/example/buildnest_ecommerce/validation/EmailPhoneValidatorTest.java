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
}

package com.example.buildnest_ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Custom validator for phone number format (Section 3.2 - Input Validation
 * Enhancement)
 * Validates international phone number format: +1-999-999-9999 or similar
 * variations
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    // Pattern: +? followed by 1-3 digits, then 7-14 more digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        // null values are valid - use @NotNull for null validation
        if (phoneNumber == null) {
            return true;
        }

        // Remove common formatting characters
        String sanitized = phoneNumber.replaceAll("[\\s\\-().]+", "");

        // Validate against international phone format
        return PHONE_PATTERN.matcher(sanitized).matches();
    }
}

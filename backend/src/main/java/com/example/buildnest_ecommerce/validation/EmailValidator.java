package com.example.buildnest_ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Custom validator for email format (Section 3.2 - Input Validation
 * Enhancement)
 * RFC 5322 compliant email validation
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true; // null values are valid - use @NotNull for null validation
        }

        return EMAIL_PATTERN.matcher(email).matches() && email.length() <= 254;
    }
}

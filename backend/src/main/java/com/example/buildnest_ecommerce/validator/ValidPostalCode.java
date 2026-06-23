package com.example.buildnest_ecommerce.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

/**
 * 3.2 MEDIUM - Input Validation Enhancement
 * Validator for postal codes (US format)
 */
@Constraint(validatedBy = PostalCodeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPostalCode {
    String message() default "Invalid postal code format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class PostalCodeValidator implements ConstraintValidator<ValidPostalCode, String> {
    // Supports multiple formats: 12345, 12345-6789
    private static final Pattern PATTERN = Pattern.compile("^\\d{5}(-\\d{4})?$");

    @Override
    public boolean isValid(String postalCode, ConstraintValidatorContext context) {
        return postalCode == null || PATTERN.matcher(postalCode).matches();
    }
}

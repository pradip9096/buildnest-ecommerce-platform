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
 * Validator for product SKU (Stock Keeping Unit)
 */
@Constraint(validatedBy = SKUValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSKU {
    String message() default "Invalid SKU format. Must be alphanumeric, 3-20 characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class SKUValidator implements ConstraintValidator<ValidSKU, String> {
    private static final Pattern PATTERN = Pattern.compile("^[A-Z0-9\\-]{3,20}$");

    @Override
    public boolean isValid(String sku, ConstraintValidatorContext context) {
        return sku == null || PATTERN.matcher(sku).matches();
    }
}

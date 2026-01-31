package com.example.buildnest_ecommerce.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

/**
 * 3.2 MEDIUM - Input Validation Enhancement
 * Validator for price values
 */
@Constraint(validatedBy = PriceValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPrice {
    String message() default "Price must be positive and not exceed 999999.99";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class PriceValidator implements ConstraintValidator<ValidPrice, BigDecimal> {
    @Override
    public boolean isValid(BigDecimal price, ConstraintValidatorContext context) {
        if (price == null) {
            return true;
        }
        return price.compareTo(BigDecimal.ZERO) > 0 &&
                price.compareTo(new BigDecimal("999999.99")) <= 0;
    }
}

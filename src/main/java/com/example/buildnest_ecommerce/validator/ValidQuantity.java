package com.example.buildnest_ecommerce.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 3.2 MEDIUM - Input Validation Enhancement
 * Validator for quantity values
 */
@Constraint(validatedBy = QuantityValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQuantity {
    String message() default "Quantity must be between 1 and 10000";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class QuantityValidator implements ConstraintValidator<ValidQuantity, Integer> {
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 10000;

    @Override
    public boolean isValid(Integer quantity, ConstraintValidatorContext context) {
        return quantity == null || (quantity >= MIN_QUANTITY && quantity <= MAX_QUANTITY);
    }
}

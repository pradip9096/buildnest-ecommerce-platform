package com.example.buildnest_ecommerce.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for phone number validation (Section 3.2 - Input Validation
 * Enhancement)
 * Supports international phone number formats
 */
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number format. Must be a valid international phone number.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

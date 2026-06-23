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
 * Validator for phone numbers (E.164 format)
 */
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number format. Use E.164 format: +1-234-567-8900";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    // E.164 format: +[1-9]{1}[0-9]{1,14}
    private static final Pattern PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        return phone == null || PATTERN.matcher(phone.replaceAll("[\\s-]", "")).matches();
    }
}

package com.example.buildnest_ecommerce.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 4.1 MEDIUM - Service Layer Abstraction
 * Annotation to mark methods that should use service layer only
 * Prevents direct repository access from controllers
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLayerOnly {
    String value() default "Service layer access only - do not use repositories directly";
}

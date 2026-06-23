package com.example.buildnest_ecommerce.annotation;

import java.lang.annotation.*;

/**
 * Section 6.1 - API Versioning Sunset Management
 * 
 * Annotation to mark API endpoints with sunset dates.
 * Enables automated sunset date enforcement and deprecation warnings.
 * 
 * Usage:
 * 
 * <pre>
 * &#64;ApiSunset(date = "2026-12-31", version = "1.0", migrationGuide = "https://docs.buildnest.com/api/v2-migration")
 * &#64;RestController
 * public class ProductControllerV1 {
 *     // ...
 * }
 * </pre>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSunset {

    /**
     * Sunset date in ISO format (yyyy-MM-dd)
     */
    String date();

    /**
     * API version being deprecated
     */
    String version();

    /**
     * URL to migration guide
     */
    String migrationGuide() default "";

    /**
     * Replacement API version
     */
    String replacedBy() default "";

    /**
     * Whether to enforce sunset date (throw exception if past date)
     */
    boolean enforce() default true;

    /**
     * Number of days before sunset to start warning
     */
    int warningDays() default 90;
}

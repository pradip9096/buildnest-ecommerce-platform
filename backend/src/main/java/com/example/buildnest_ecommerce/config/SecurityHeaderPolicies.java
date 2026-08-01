package com.example.buildnest_ecommerce.config;

/**
 * Single source of truth for the CSP directives enforced by
 * {@link SecurityConfig}'s two filter chains, and the shared HSTS max-age.
 *
 * <p>{@code SecurityConfig} never loads during a test run (it is
 * {@code @Profile("!test")}), so {@code TestSecurityConfig} (the
 * test-profile stand-in) previously hand-duplicated the CSP policy string
 * and had drifted from it silently — the production hardening from #237
 * had zero regression coverage as a result (#312). Both classes now
 * reference these constants so the two configurations cannot drift apart
 * again without a compile error at the reference site.
 */
public final class SecurityHeaderPolicies {

    /**
     * Main-chain policy: all {@code /api/**} paths. No
     * {@code unsafe-inline} — see #237.
     */
    public static final String MAIN_CSP =
            "default-src 'self'; script-src 'self'; style-src 'self'; " +
            "frame-ancestors 'none'; form-action 'self'";

    /**
     * Swagger-chain policy: isolated to {@code /swagger-ui/**},
     * {@code /v3/api-docs/**} only. Retains {@code unsafe-inline}, which
     * SpringDoc requires to render its bundled inline scripts and styles
     * — never apply this policy outside the Swagger-specific filter chain.
     */
    public static final String SWAGGER_CSP =
            "default-src 'self'; script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'; img-src 'self' data:";

    public static final long HSTS_MAX_AGE_SECONDS = 31536000L;

    /**
     * Referrer-Policy is not enabled by Spring Security's default headers
     * (SEC-11/SEC-12, #112) — verified against the Spring Security 6.5
     * reference docs, which list only Cache-Control/Pragma/Expires/
     * X-Content-Type-Options/HSTS/X-Frame-Options/X-XSS-Protection as
     * defaults. {@code strict-origin-when-cross-origin} avoids leaking the
     * full referrer URL cross-origin while still allowing same-origin
     * analytics.
     */
    public static final String REFERRER_POLICY =
            "strict-origin-when-cross-origin";

    /**
     * Permissions-Policy is not added by Spring Security at all (confirmed
     * via the same 6.5 docs — "Spring Security does not add
     * Permissions-Policy headers by default"). BuildNest uses none of
     * these browser features server-side or in the SPA, so every
     * directive is denied outright rather than scoped to 'self'.
     */
    public static final String PERMISSIONS_POLICY =
            "geolocation=(), camera=(), microphone=(), payment=(), usb=()";

    private SecurityHeaderPolicies() {
    }
}

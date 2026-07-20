package com.example.buildnest_ecommerce.config;

import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationEntryPoint;
import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationFilter;
import com.example.buildnest_ecommerce.security.AdminRateLimitFilter;
import com.example.buildnest_ecommerce.security.HttpsEnforcementFilter;
import com.example.buildnest_ecommerce.security.NonClearingCsrfTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication
        .builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method
        .configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders
        .HttpSecurity;
import org.springframework.security.config.annotation.web.configuration
        .EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.session
        .NullAuthenticatedSessionStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication
        .UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf
        .CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("!test")
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private AdminRateLimitFilter adminRateLimitFilter;

    @Autowired
    private Environment environment;

    @Value("${app.cors.allowed-origins:http://localhost:5173,"
            + "http://localhost:5174,http://localhost:3000}")
    private String[] corsAllowedOrigins;

    /**
     * Read-only monitoring username for the Prometheus scrape (#359) —
     * intentionally NOT the ADMIN login account.
     */
    @Value("${monitoring.username:monitoring}")
    private String monitoringUsername;

    /** Marker for monitoring.password left at its local-dev default. */
    private static final String DEFAULT_MONITORING_PASSWORD_MARKER =
            "changeme-monitoring-password";

    /**
     * Read-only monitoring password, local-dev default only — same
     * pattern as jwt.secret's fallback. Production rejection is
     * enforced in {@link #validateHttpsInProduction()} below.
     */
    @Value("${monitoring.password:" + DEFAULT_MONITORING_PASSWORD_MARKER
            + "}")
    private String monitoringPassword;

    @PostConstruct
    public void validateHttpsInProduction() {
        boolean isProduction = Arrays.asList(
                environment.getActiveProfiles()).contains("production");
        boolean sslEnabled = environment.getProperty(
                "server.ssl.enabled", Boolean.class, false);

        if (isProduction && !sslEnabled) {
            throw new IllegalStateException(
                    "HTTPS must be enabled in production. Set "
                            + "server.ssl.enabled=true or "
                            + "SERVER_SSL_ENABLED=true");
        }

        boolean monitoringPasswordIsDefault = monitoringPassword == null
                || monitoringPassword.isBlank()
                || monitoringPassword.equals(
                        DEFAULT_MONITORING_PASSWORD_MARKER);
        if (isProduction && monitoringPasswordIsDefault) {
            throw new IllegalStateException(
                    "MONITORING_PASSWORD must be set to a non-default "
                            + "value in production. Generate: "
                            + "openssl rand -base64 32");
        }

        // FINDING #1 FIX: Enhanced fail-fast validation for keystore
        // configuration. Verify keystore path and password are
        // configured when SSL is enabled
        if (sslEnabled) {
            String keyStorePath = environment.getProperty(
                    "server.ssl.key-store");
            String keyStorePassword = environment.getProperty(
                    "server.ssl.key-store-password");

            if (keyStorePath == null || keyStorePath.trim().isEmpty()) {
                throw new IllegalStateException(
                        "server.ssl.key-store must be configured when "
                                + "SSL is enabled. Provide "
                                + "SERVER_SSL_KEY_STORE environment "
                                + "variable or server.ssl.key-store "
                                + "property");
            }

            if (keyStorePassword == null
                    || keyStorePassword.trim().isEmpty()) {
                throw new IllegalStateException(
                        "server.ssl.key-store-password must be "
                                + "configured when SSL is enabled. "
                                + "Provide "
                                + "SERVER_SSL_KEY_STORE_PASSWORD "
                                + "environment variable or "
                                + "server.ssl.key-store-password "
                                + "property");
            }
        }
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http)
            throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    /**
     * Dedicated security chain for Prometheus's /actuator/prometheus scrape
     * (#359). HTTP Basic with a single, purpose-scoped monitoring credential
     * — never the ADMIN login account, never JWT (Prometheus is a machine
     * scraper, not a browser session). Ordered ahead of the main chain so
     * this narrower matcher wins; the main chain's "/actuator/**" -&gt;
     * hasRole(ADMIN) rule still governs every other actuator path.
     *
     * @param http the HttpSecurity to configure
     * @return the built monitoring security filter chain
     */
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorMonitoringSecurityFilterChain(
            final HttpSecurity http) throws Exception {
        InMemoryUserDetailsManager monitoringUsers =
                buildMonitoringUserDetailsManager();
        AuthenticationManager monitoringAuthManager =
                buildMonitoringAuthenticationManager(monitoringUsers);

        http
                .securityMatcher("/actuator/prometheus")
                .authenticationManager(monitoringAuthManager)
                .authorizeHttpRequests(
                        auth -> auth.anyRequest().hasRole("MONITORING"))
                .httpBasic(basic -> {
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF exists to stop a browser replaying an ambient cookie a
                // victim didn't intend to send. This chain never sets or reads
                // a cookie (STATELESS, HTTP Basic via the Authorization header
                // only) and its only real client is Prometheus, a machine
                // scraper -- there is no browser session for a forged request
                // to ride. Same rationale spring-security.md already documents
                // for why CSRF was safe to disable before tokens moved to
                // cookies (#359).
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    private InMemoryUserDetailsManager buildMonitoringUserDetailsManager() {
        return new InMemoryUserDetailsManager(
                User.withUsername(monitoringUsername)
                        .password(passwordEncoder().encode(monitoringPassword))
                        .roles("MONITORING")
                        .build());
    }

    // Explicit ProviderManager, not .userDetailsService()/
    // .authenticationProvider() on HttpSecurity -- those register against
    // the shared AuthenticationManagerBuilder, which this app already
    // populates globally with the DB-backed CustomUserDetailsService,
    // causing Basic Auth here to silently authenticate against real user
    // accounts instead of this dedicated monitoring identity. Confirmed
    // empirically (#359): without this, the monitoring credential resolved
    // via CustomUserDetailsService, not this provider.
    private AuthenticationManager buildMonitoringAuthenticationManager(
            final InMemoryUserDetailsManager monitoringUsers) {
        var provider = new org.springframework.security.authentication
                .dao.DaoAuthenticationProvider(monitoringUsers);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    /**
     * Dedicated security chain for Swagger UI paths (SEC-14).
     * Retains 'unsafe-inline' scoped exclusively to documentation endpoints,
     * which SpringDoc requires to render its bundled inline scripts and styles.
     * All API paths are handled by the main chain below with a strict CSP.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .securityMatcher(
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                SecurityHeaderPolicies.SWAGGER_CSP))
                        .frameOptions(frameOptions -> frameOptions.deny()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        // Check if running in test profile
        boolean isTestProfile = Arrays.asList(
                environment.getActiveProfiles()).contains("test");

        boolean sslEnabled = environment.getProperty(
                "server.ssl.enabled", Boolean.class, false);
        boolean isProductionProfile = Arrays.asList(
                environment.getActiveProfiles()).contains("production");
        boolean enforceHttps = sslEnabled || isProductionProfile;

        http
                // Security headers for OWASP compliance
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                SecurityHeaderPolicies.MAIN_CSP))
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(SecurityHeaderPolicies
                                        .HSTS_MAX_AGE_SECONDS)))
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfig = new CorsConfiguration();
                    // Allow specific origins in production (RQ-SEC-03 -
                    // 1.3 CRITICAL HTTPS Enforcement)
                    List<String> origins = new java.util.ArrayList<>(
                            Arrays.asList("https://buildnest.com",
                                    "https://www.buildnest.com"));
                    origins.addAll(Arrays.asList(corsAllowedOrigins));
                    corsConfig.setAllowedOrigins(origins);
                    corsConfig.setAllowedMethods(Arrays.asList(
                            "GET", "POST", "PUT", "PATCH", "DELETE",
                            "OPTIONS"));
                    corsConfig.setAllowedHeaders(Arrays.asList(
                            "Authorization", "Content-Type", "Accept",
                            "X-XSRF-TOKEN"));
                    corsConfig.setExposedHeaders(
                            Arrays.asList("Authorization"));
                    corsConfig.setAllowCredentials(true);
                    corsConfig.setMaxAge(3600L);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf
                        // NonClearingCsrfTokenRepository works around a
                        // confirmed Spring Security defect (GH-12141):
                        // CsrfAuthenticationStrategy clears the
                        // XSRF-TOKEN cookie on every authentication
                        // event but fails to regenerate it in the same
                        // request/response cycle. JwtAuthenticationFilter
                        // re-authenticates on every single stateless
                        // request, so this fired constantly and wiped
                        // the cookie before the browser could ever use
                        // it. See NonClearingCsrfTokenRepository's
                        // javadoc for the full rationale.
                        .csrfTokenRepository(new NonClearingCsrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()))
                        // Plain (non-Xor) handler: the SPA reads the raw
                        // XSRF-TOKEN cookie value via JS and echoes it
                        // back verbatim as the X-XSRF-TOKEN header.
                        // XorCsrfTokenRequestAttributeHandler expects a
                        // masked header value for BREACH protection,
                        // which is incompatible with sending the raw
                        // cookie value directly — confirmed empirically
                        // (raw value rejected with 403 under Xor).
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                "/api/auth/login", "/api/auth/register"))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(
                                new NullAuthenticatedSessionStrategy()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/*/reviews/summary").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/*/reviews/top-helpful")
                                .permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/inventory/*/status").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/inventory/*/details").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/inventory/*/available").permitAll()
                        .requestMatchers("/api/auth/login",
                                "/api/auth/register", "/api/auth/refresh",
                                "/api/auth/csrf").permitAll()
                        .requestMatchers("/api/password/forgot",
                                "/api/password/reset").permitAll()
                        .requestMatchers("/api/password/change")
                                .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/webhooks/**").permitAll()
                        .requestMatchers("/swagger-ui.html",
                                "/swagger-ui/**", "/v3/api-docs/**")
                                .permitAll()
                        .requestMatchers("/error").permitAll()
                        // Actuator endpoints - health is public, others
                        // require ADMIN (RQ-ES-SEC-01, RQ-ES-SEC-02)
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Admin endpoints (legacy /api/admin/** and
                        // versioned /api/v1/admin/**)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**")
                                .hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/admin/**").hasRole("ADMIN")
                        // User endpoints
                        .requestMatchers("/api/user/**")
                                .hasAnyRole("USER", "ADMIN")
                        // Any other request
                        .anyRequest().authenticated())
                        .addFilterBefore(
                                new HttpsEnforcementFilter(
                                        enforceHttps && !isTestProfile),
                                UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminRateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

package com.example.buildnest_ecommerce.config;

import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationEntryPoint;
import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationFilter;
import com.example.buildnest_ecommerce.security.NonClearingCsrfTokenRepository;
import com.example.buildnest_ecommerce.service.ratelimit.RateLimiterService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

/**
 * Test security configuration - disables HTTPS enforcement for test
 * environment.
 *
 * <p>{@code SecurityConfig} is {@code @Profile("!test")} and never loads during a test run, so
 * this class stands in for it. It shares {@link SecurityHeaderPolicies#MAIN_CSP} and
 * {@link SecurityHeaderPolicies#HSTS_MAX_AGE_SECONDS} with the real config so the two can't
 * silently drift apart again (#312).
 *
 * <p><b>Deliberate divergence, not drift:</b> unlike {@code SecurityConfig}'s two filter chains
 * (a Swagger-isolated chain with a permissive CSP, plus this strict main-chain policy), this
 * test config uses a single chain for everything, including Swagger paths. Swagger's
 * documentation-only, developer-facing purpose makes its CSP isolation immaterial to what tests
 * verify (access — {@code permitAll} — is preserved identically here); only the main chain's
 * strict policy needs test coverage, since that's the one protecting real API responses.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class TestSecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Primary
    public RateLimiterService rateLimiterService() {
        RateLimiterService mock = Mockito.mock(RateLimiterService.class);
        Mockito.when(mock.getRemainingTokens(Mockito.anyString(), Mockito.anyInt())).thenReturn(50);
        Mockito.when(mock.getRetryAfterSeconds(Mockito.anyString())).thenReturn(0L);
        Mockito.when(mock.isAllowed(Mockito.anyString(), Mockito.anyInt(), Mockito.any())).thenReturn(true);
        return mock;
    }

    @org.springframework.beans.factory.annotation.Value("${monitoring.username:monitoring}")
    private String monitoringUsername;

    @org.springframework.beans.factory.annotation.Value("${monitoring.password:changeme-monitoring-password}")
    private String monitoringPassword;

    /**
     * Mirrors {@code SecurityConfig#actuatorMonitoringSecurityFilterChain} (#359) — kept
     * deliberately in sync since this class already drifted from the real config once (#312).
     */
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorMonitoringSecurityFilterChain(HttpSecurity http) throws Exception {
        InMemoryUserDetailsManager monitoringUserDetailsManager = new InMemoryUserDetailsManager(
                User.withUsername(monitoringUsername)
                        .password(passwordEncoder().encode(monitoringPassword))
                        .roles("MONITORING")
                        .build());
        org.springframework.security.authentication.dao.DaoAuthenticationProvider monitoringAuthProvider =
                new org.springframework.security.authentication.dao.DaoAuthenticationProvider(monitoringUserDetailsManager);
        monitoringAuthProvider.setPasswordEncoder(passwordEncoder());
        // See SecurityConfig#actuatorMonitoringSecurityFilterChain -- explicit ProviderManager,
        // not .userDetailsService() on HttpSecurity, to avoid leaking into the shared/global
        // AuthenticationManagerBuilder this test config also populates.
        org.springframework.security.authentication.ProviderManager monitoringAuthenticationManager =
                new org.springframework.security.authentication.ProviderManager(monitoringAuthProvider);

        http
                .securityMatcher("/actuator/prometheus")
                .authenticationManager(monitoringAuthenticationManager)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("MONITORING"))
                .httpBasic(basic -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Skip HTTPS enforcement for test environment (HTTP allowed for testing)
                // Security headers for OWASP compliance
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(SecurityHeaderPolicies.MAIN_CSP))
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(SecurityHeaderPolicies.HSTS_MAX_AGE_SECONDS))
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers
                                        .ReferrerPolicyHeaderWriter.ReferrerPolicy
                                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(permissions -> permissions
                                .policy(SecurityHeaderPolicies.PERMISSIONS_POLICY)))
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfig = new CorsConfiguration();
                    // #630: setAllowedOrigins("*") + allowCredentials(true) throws
                    // IllegalArgumentException the moment a real browser sends an Origin
                    // header (Spring validates this combination at request time, not
                    // startup) -- invisible to MockMvc, which never sends Origin. Only a
                    // live-browser E2E run surfaced it. allowedOriginPatterns supports the
                    // wildcard+credentials combination validly.
                    corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
                    corsConfig.setAllowedMethods(Arrays.asList("*"));
                    corsConfig.setAllowedHeaders(Arrays.asList("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new NonClearingCsrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()))
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/auth/login", "/api/auth/register"))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews/summary").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews/top-helpful").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inventory/*/status").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inventory/*/details").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inventory/*/available").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/csrf").permitAll()
                        .requestMatchers("/api/password/forgot", "/api/password/reset").permitAll()
                        .requestMatchers("/api/password/change").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/webhooks/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Actuator endpoints - health is public, others require ADMIN
                        // #123: /** so readiness/liveness sub-paths match SecurityConfig
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Admin endpoints (legacy /api/admin/** and versioned /api/v1/admin/**)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/**").hasRole("ADMIN")
                        // User endpoints
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // Any other request
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

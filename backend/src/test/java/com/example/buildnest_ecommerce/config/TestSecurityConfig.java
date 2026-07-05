package com.example.buildnest_ecommerce.config;

import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationEntryPoint;
import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationFilter;
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
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
                                .maxAgeInSeconds(SecurityHeaderPolicies.HSTS_MAX_AGE_SECONDS)))
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfig = new CorsConfiguration();
                    corsConfig.setAllowedOrigins(Arrays.asList("*"));
                    corsConfig.setAllowedMethods(Arrays.asList("*"));
                    corsConfig.setAllowedHeaders(Arrays.asList("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews/summary").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/*/reviews/top-helpful").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/password/forgot", "/api/password/reset").permitAll()
                        .requestMatchers("/api/password/change").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/webhooks/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Actuator endpoints - health is public, others require ADMIN
                        .requestMatchers("/actuator/health").permitAll()
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

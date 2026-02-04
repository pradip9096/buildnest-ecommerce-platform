package com.example.buildnest_ecommerce.config;

import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationEntryPoint;
import com.example.buildnest_ecommerce.security.Jwt.JwtAuthenticationFilter;
import com.example.buildnest_ecommerce.security.AdminRateLimitFilter;
import com.example.buildnest_ecommerce.security.HttpsEnforcementFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import jakarta.annotation.PostConstruct;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private AdminRateLimitFilter adminRateLimitFilter;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void validateHttpsInProduction() {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("production");
        boolean sslEnabled = environment.getProperty("server.ssl.enabled", Boolean.class, false);

        if (isProduction && !sslEnabled) {
            throw new IllegalStateException(
                    "HTTPS must be enabled in production. Set server.ssl.enabled=true or SERVER_SSL_ENABLED=true");
        }

        // FINDING #1 FIX: Enhanced fail-fast validation for keystore configuration
        // Verify keystore path and password are configured when SSL is enabled
        if (sslEnabled) {
            String keyStorePath = environment.getProperty("server.ssl.key-store");
            String keyStorePassword = environment.getProperty("server.ssl.key-store-password");

            if (keyStorePath == null || keyStorePath.trim().isEmpty()) {
                throw new IllegalStateException(
                        "server.ssl.key-store must be configured when SSL is enabled. " +
                                "Provide SERVER_SSL_KEY_STORE environment variable or server.ssl.key-store property");
            }

            if (keyStorePassword == null || keyStorePassword.trim().isEmpty()) {
                throw new IllegalStateException(
                        "server.ssl.key-store-password must be configured when SSL is enabled. " +
                                "Provide SERVER_SSL_KEY_STORE_PASSWORD environment variable or server.ssl.key-store-password property");
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
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Check if running in test profile
        boolean isTestProfile = Arrays.asList(environment.getActiveProfiles()).contains("test");

        http
                // Security headers for OWASP compliance
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"))
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000)))
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfig = new CorsConfiguration();
                    // Allow specific origins in production (RQ-SEC-03 - 1.3 CRITICAL HTTPS
                    // Enforcement)
                    corsConfig.setAllowedOrigins(Arrays.asList("https://buildnest.com", "https://www.buildnest.com"));
                    corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
                    corsConfig.setExposedHeaders(Arrays.asList("Authorization"));
                    corsConfig.setAllowCredentials(true);
                    corsConfig.setMaxAge(3600L);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/password/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Actuator endpoints - health is public, others require ADMIN (RQ-ES-SEC-01,
                        // RQ-ES-SEC-02)
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                        // User endpoints
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // Any other request
                        .anyRequest().authenticated())
                        .addFilterBefore(new HttpsEnforcementFilter(!isTestProfile), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

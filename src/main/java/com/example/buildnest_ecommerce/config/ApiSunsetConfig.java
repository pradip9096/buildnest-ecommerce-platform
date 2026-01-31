package com.example.buildnest_ecommerce.config;

import com.example.buildnest_ecommerce.interceptor.ApiSunsetInterceptor;
import com.example.buildnest_ecommerce.interceptor.RateLimitHeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration for API Management
 * 
 * Section 6.1: API Versioning Sunset Management
 * Section 6.3: API Rate Limiting Headers
 * 
 * Registers interceptors for:
 * - Sunset date enforcement
 * - Rate limit headers
 */
@Configuration
@RequiredArgsConstructor
public class ApiSunsetConfig implements WebMvcConfigurer {

    private final ApiSunsetInterceptor apiSunsetInterceptor;
    private final RateLimitHeaderInterceptor rateLimitHeaderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register sunset interceptor
        registry.addInterceptor(apiSunsetInterceptor)
                .addPathPatterns("/api/**");

        // Register rate limit header interceptor
        registry.addInterceptor(rateLimitHeaderInterceptor)
                .addPathPatterns("/api/**");
    }
}

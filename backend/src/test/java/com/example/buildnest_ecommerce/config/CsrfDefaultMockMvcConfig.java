package com.example.buildnest_ecommerce.config;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;

/**
 * Opt-in helper for MockMvc test classes that don't specifically test CSRF behavior
 * themselves: makes every request carry a valid CSRF token by default, since SEC-15
 * enabled real CSRF protection on all mutating endpoints (not just auth).
 *
 * <p>Deliberately NOT imported by {@code AuthControllerTest} — that class has its own
 * tests asserting CSRF *rejection*, which this default would silently defeat.
 */
@TestConfiguration
public class CsrfDefaultMockMvcConfig {

    @Bean
    public MockMvcBuilderCustomizer csrfDefaultMockMvcBuilderCustomizer() {
        return (ConfigurableMockMvcBuilder<?> builder) -> builder.defaultRequest(
                MockMvcRequestBuilders.get("/").with(SecurityMockMvcRequestPostProcessors.csrf()));
    }
}

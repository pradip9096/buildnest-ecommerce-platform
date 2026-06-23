package com.example.buildnest_ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;

/**
 * 6.2 MEDIUM - API Documentation Completeness
 * Enhanced OpenAPI/Swagger configuration with comprehensive documentation
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildNest E-Commerce API")
                        .version("2.0.1")
                        .description("Comprehensive REST API for BuildNest E-Commerce Platform\n\n" +
                                "## Overview\n" +
                                "BuildNest is a modern e-commerce platform built with Spring Boot 3.2.2, " +
                                "featuring JWT authentication, role-based access control, " +
                                "real-time inventory management, and comprehensive payment processing.\n\n" +
                                "## Authentication\n" +
                                "Most endpoints require JWT Bearer authentication. " +
                                "Obtain a token via `/api/auth/login` with credentials.\n\n" +
                                "## Response Format\n" +
                                "All responses follow a consistent format with `success`, `message`, and `data` fields.\n\n"
                                +
                                "## Error Handling\n" +
                                "Errors are returned with appropriate HTTP status codes and detailed error messages.\n\n"
                                +
                                "## Rate Limiting\n" +
                                "API endpoints are rate-limited. Check `X-RateLimit-*` headers in responses.\n\n" +
                                "## Pagination\n" +
                                "Paginated endpoints support `page` and `size` parameters (max size: 100).")
                        .contact(new Contact()
                                .name("BuildNest Support")
                                .url("https://buildnest.com")
                                .email("support@buildnest.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer authentication token.\n\n" +
                                        "Example: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`\n\n" +
                                        "### How to obtain token:\n" +
                                        "1. POST /api/auth/login with email and password\n" +
                                        "2. Receive JWT token in response\n" +
                                        "3. Include `Authorization: Bearer <token>` header in subsequent requests\n\n" +
                                        "### Token validity:\n" +
                                        "- Access Token: 15 minutes\n" +
                                        "- Refresh Token: 30 days\n" +
                                        "- Use /api/auth/refresh to get new access token")));
    }
}

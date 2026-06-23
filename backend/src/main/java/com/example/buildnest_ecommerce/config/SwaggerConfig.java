package com.example.buildnest_ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Enhanced OpenAPI/Swagger Configuration for BuildNest E-Commerce Platform
 * Provides comprehensive API documentation with security schemes, servers, and
 * external documentation
 */
@Configuration
public class SwaggerConfig {

        @Value("${app.version:1.0.0}")
        private String appVersion;

        @Value("${app.api.base-url:http://localhost:8080}")
        private String apiBaseUrl;

        @Bean(name = "swaggerOpenAPI")
        @Primary
        public OpenAPI swaggerOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("BuildNest E-Commerce Platform API")
                                                .version(appVersion)
                                                .description("""
                                                                # BuildNest REST API Documentation

                                                                BuildNest is a comprehensive e-commerce platform for home construction materials and décor products.

                                                                ## Features
                                                                - **Product Management**: Browse, search, and manage construction materials and décor products
                                                                - **Order Processing**: Complete order lifecycle from cart to delivery
                                                                - **User Management**: User registration, authentication, and profile management
                                                                - **Inventory Tracking**: Real-time inventory monitoring and alerts
                                                                - **Payment Integration**: Secure payment processing with Razorpay
                                                                - **Admin Dashboard**: Comprehensive analytics and reporting

                                                                ## Authentication
                                                                Most endpoints require JWT bearer token authentication. Obtain a token via `/api/auth/login`.

                                                                ## Rate Limiting
                                                                API requests are rate-limited to prevent abuse. Rate limit headers are included in responses.

                                                                ## Versioning
                                                                This API uses URL-based versioning. Current version: v1
                                                                """)
                                                .contact(new Contact()
                                                                .name("BuildNest API Support Team")
                                                                .email("api-support@buildnest.com")
                                                                .url("https://buildnest.com/support"))
                                                .license(new License()
                                                                .name("Apache License 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                                                .termsOfService("https://buildnest.com/terms"))
                                .servers(List.of(
                                                new Server()
                                                                .url(apiBaseUrl)
                                                                .description("Development Server"),
                                                new Server()
                                                                .url("https://api-staging.buildnest.com")
                                                                .description("Staging Environment"),
                                                new Server()
                                                                .url("https://api.buildnest.com")
                                                                .description("Production Environment")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("BuildNest API Documentation Portal")
                                                .url("https://docs.buildnest.com/api"))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                                .components(new io.swagger.v3.oas.models.Components()
                                                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("JWT token obtained from /api/auth/login endpoint")
                                                                .name("Authorization")));
        }
}

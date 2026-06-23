package com.example.buildnest_ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    @SuppressWarnings("java:S1604")
    public Object modelMapper() {
        // ModelMapper bean for DTO conversion
        // Note: ModelMapper dependency can be added in the future if needed
        // Currently using manual DTO mapping for flexibility and performance
        return new Object();
    }
}

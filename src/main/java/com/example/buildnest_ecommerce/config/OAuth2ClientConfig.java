package com.example.buildnest_ecommerce.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.beans.factory.annotation.Value;

/**
 * OAuth2 Client Configuration for external identity provider integration.
 * Supports integration with Google, GitHub, and other OAuth2 providers.
 * Only enabled when OAuth2 is enabled via application.properties.
 * 
 * Configuration should be externalized via application.properties or environment variables:
 * - oauth2.enabled=true (to enable OAuth2 configuration)
 * - spring.security.oauth2.client.registration.google.client-id
 * - spring.security.oauth2.client.registration.google.client-secret
 * - spring.security.oauth2.client.registration.github.client-id
 * - spring.security.oauth2.client.registration.github.client-secret
 * 
 * Satisfies RQ-MAINT-03: OAuth2 Support Architecture
 */
@Configuration
@ConditionalOnProperty(name = "oauth2.enabled", havingValue = "true", matchIfMissing = false)
public class OAuth2ClientConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id:#{null}}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:#{null}}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.github.client-id:#{null}}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret:#{null}}")
    private String githubClientSecret;

    /**
     * Provides Client Registration Repository for OAuth2 authentication.
     * Dynamically configures Google and GitHub OAuth2 clients.
     * 
     * @return ClientRegistrationRepository with configured OAuth2 clients
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
                googleClientRegistration(),
                githubClientRegistration()
        );
    }

    /**
     * Google OAuth2 Client Registration Configuration
     * 
     * @return ClientRegistration for Google OAuth2
     */
    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId != null ? googleClientId : "")
                .clientSecret(googleClientSecret != null ? googleClientSecret : "")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();
    }

    /**
     * GitHub OAuth2 Client Registration Configuration
     * 
     * @return ClientRegistration for GitHub OAuth2
     */
    private ClientRegistration githubClientRegistration() {
        return ClientRegistration.withRegistrationId("github")
                .clientId(githubClientId != null ? githubClientId : "")
                .clientSecret(githubClientSecret != null ? githubClientSecret : "")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("user:email")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .build();
    }
}

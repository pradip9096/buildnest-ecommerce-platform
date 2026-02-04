package com.example.buildnest_ecommerce.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private String secretPrevious;
    private long expiration;
    private long refreshExpiration;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecretPrevious() {
        return secretPrevious;
    }

    public void setSecretPrevious(String secretPrevious) {
        this.secretPrevious = secretPrevious;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
}

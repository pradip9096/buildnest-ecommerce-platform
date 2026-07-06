package com.example.buildnest_ecommerce.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Client-facing auth response body for SEC-15 — access/refresh tokens travel as httpOnly
 * cookies, never in JSON, so this deliberately excludes them (unlike the internal AuthResponse).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserResponse {
    private Long userId;
    private String username;

    public static AuthUserResponse from(AuthResponse authResponse) {
        return new AuthUserResponse(authResponse.getUserId(), authResponse.getUsername());
    }
}

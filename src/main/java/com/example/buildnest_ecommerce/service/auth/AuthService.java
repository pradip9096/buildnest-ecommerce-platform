package com.example.buildnest_ecommerce.service.auth;

import com.example.buildnest_ecommerce.model.payload.AuthResponse;
import com.example.buildnest_ecommerce.model.payload.RegisterRequest;

public interface AuthService {
    AuthResponse login(String username, String password);
    void register(RegisterRequest registerRequest);
    boolean validateToken(String token);
    void logout(String refreshToken);
    AuthResponse refreshAccessToken(String refreshToken);
}

package com.example.buildnest_ecommerce.controller.auth;

import com.example.buildnest_ecommerce.aspect.Auditable;
import com.example.buildnest_ecommerce.model.payload.LoginRequest;
import com.example.buildnest_ecommerce.model.payload.RegisterRequest;
import com.example.buildnest_ecommerce.model.payload.RefreshTokenRequest;
import com.example.buildnest_ecommerce.model.payload.AuthResponse;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.auth.AuthService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and token management")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitUtil rateLimitUtil;

    @PostMapping("/login")
    @Auditable(action = "LOGIN", entityType = "AUTH")
    @Operation(summary = "Login", description = "Authenticate user and return access/refresh tokens")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Login successful\",\"data\":{\"accessToken\":\"jwt\",\"refreshToken\":\"refresh\",\"tokenType\":\"Bearer\",\"username\":\"buildnest_user\"}}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many login attempts")
    })
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        if (!rateLimitUtil.isAllowed(request, "login")) {
            long retryAfter = rateLimitUtil.getRetryAfterSeconds(request, "login");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(retryAfter))
                    .body(new ApiResponse(false, "Too many login attempts. Please try again later.", null));
        }
        try {
            log.info("Login attempt for user: {}", loginRequest.getUsername());
            AuthResponse authResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new ApiResponse(true, "Login successful", authResponse));
        } catch (Exception e) {
            log.error("Login failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid credentials", null));
        }
    }

    @PostMapping("/register")
    @Auditable(action = "REGISTER", entityType = "AUTH")
    @Operation(summary = "Register", description = "Register a new user account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate user")
    })
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Registration attempt for user: {}", registerRequest.getUsername());
            authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "User registered successfully", null));
        } catch (Exception e) {
            log.error("Registration failed: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Registration failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/refresh")
    @Auditable(action = "TOKEN_REFRESH", entityType = "AUTH")
    @Operation(summary = "Refresh token", description = "Refresh access token using a valid refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many refresh attempts")
    })
    public ResponseEntity<ApiResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        try {
            var tokenOpt = refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken());
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Token refresh failed: Invalid refresh token", null));
            }
            Long userId = tokenOpt.get().getUserId();
            if (!rateLimitUtil.isAllowed(request, "refresh", userId)) {
                long retryAfter = rateLimitUtil.getRetryAfterSeconds(request, "refresh", userId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", String.valueOf(retryAfter))
                        .body(new ApiResponse(false, "Too many refresh token requests. Please try again later.", null));
            }

            log.info("Refresh token request");
            AuthResponse authResponse = authService.refreshAccessToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok(new ApiResponse(true, "Token refreshed successfully", authResponse));
        } catch (Exception e) {
            log.error("Token refresh failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Token refresh failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validate token", description = "Validate a JWT access token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token is valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token is invalid")
    })
    public ResponseEntity<ApiResponse> validateToken(
            @Parameter(description = "Bearer access token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean isValid = authService.validateToken(jwt);
            if (isValid) {
                return ResponseEntity.ok(new ApiResponse(true, "Token is valid", null));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Token is invalid", null));
            }
        } catch (Exception e) {
            log.error("Token validation failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Token validation failed", null));
        }
    }

    @PostMapping("/logout")
    @Auditable(action = "LOGOUT", entityType = "AUTH")
    @Operation(summary = "Logout", description = "Invalidate refresh token and logout user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Logout failed")
    })
    public ResponseEntity<ApiResponse> logout(@RequestBody(required = false) RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest != null ? refreshTokenRequest.getRefreshToken() : null;
            authService.logout(refreshToken);
            return ResponseEntity.ok(new ApiResponse(true, "Logout successful", null));
        } catch (Exception e) {
            log.error("Logout failed: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Logout failed", null));
        }
    }
}

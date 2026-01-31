package com.example.buildnest_ecommerce.service.auth;

import com.example.buildnest_ecommerce.model.entity.RefreshToken;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.AuthResponse;
import com.example.buildnest_ecommerce.model.payload.RegisterRequest;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import com.example.buildnest_ecommerce.event.UserRegisteredEvent;
import com.example.buildnest_ecommerce.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication Service Implementation
 *
 * Provides core authentication and authorization operations for the BuildNest
 * e-commerce platform.
 * Handles user login, registration, token validation, and token refresh
 * operations.
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;
    private final DomainEventPublisher domainEventPublisher;
    private final ValidationUtil validationUtil;

    /**
     * Authenticates a user with username and password.
     *
     * Validates credentials using Spring Security's AuthenticationManager,
     * generates JWT access token and refresh token, and logs the authentication
     * event.
     *
     * @param username the user's username (required)
     * @param password the user's password (required)
     * @return AuthResponse containing access token, refresh token, and token type
     * @throws RuntimeException if username not found or authentication fails
     */
    @Override
    public AuthResponse login(String username, String password) {
        log.info("User login attempt: {}", username);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            String jwt = jwtTokenProvider.generateToken(authentication);

            // Get user and create refresh token
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // Log authentication event
            auditLogService.logAuthenticationEvent(user.getId(), "LOGIN", null, null);

            AuthResponse response = new AuthResponse();
            response.setAccessToken(jwt);
            response.setRefreshToken(refreshToken.getToken());
            response.setTokenType("Bearer");
            response.setUsername(username);
            return response;
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    /**
     * Registers a new user account.
     *
     * Validates username and email uniqueness, enforces password policy,
     * creates user with default USER role, and publishes UserRegisteredEvent.
     *
     * @param registerRequest the registration request containing username, email,
     *                        password, and names
     * @throws RuntimeException if username or email already exists
     * @throws RuntimeException if password does not meet policy requirements
     * @see com.example.buildnest_ecommerce.util.ValidationUtil#validatePassword(String)
     */
    @Override
    public void register(RegisterRequest registerRequest) {
        log.info("User registration attempt: {}", registerRequest.getUsername());

        // Check if user already exists
        boolean userExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getUsername().equals(registerRequest.getUsername()) ||
                        u.getEmail().equals(registerRequest.getEmail()));

        if (userExists) {
            log.warn("Registration failed: Username or email already exists - {}", registerRequest.getUsername());
            throw new RuntimeException("Username or email already exists");
        }

        // Enforce password policy
        validationUtil.validatePassword(registerRequest.getPassword());

        // Create new user
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setIsActive(true);
        newUser.setIsDeleted(false);
        newUser.setCreatedAt(LocalDateTime.now());

        // Assign default USER role
        Set<Role> roles = new HashSet<>();
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        userRole.setDescription("Default user role");
        roles.add(userRole);
        newUser.setRoles(roles);

        // Save user
        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully: {}", registerRequest.getUsername());

        // Log registration event
        auditLogService.logAuthenticationEvent(savedUser.getId(), "REGISTER", null, null);
        domainEventPublisher.publish(new UserRegisteredEvent(this, savedUser.getId(), savedUser.getEmail()));
    }

    /**
     * Validates a JWT access token.
     *
     * Checks token signature, expiration, and overall validity.
     *
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    @Override
    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Logs out a user by revoking their refresh token.
     *
     * Invalidates the provided refresh token and logs the logout event for audit
     * purposes.
     *
     * @param refreshToken the refresh token to revoke (nullable)
     * @throws RuntimeException if refresh token is not found
     */
    @Override
    public void logout(String refreshToken) {
        log.info("User logout");
        if (refreshToken != null) {
            RefreshToken token = refreshTokenService.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            // Log logout event
            auditLogService.logAuthenticationEvent(token.getUserId(), "LOGOUT", null, null);

            // Revoke refresh token
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * Validates the refresh token, generates a new access token,
     * rotates the refresh token for security, and logs the operation.
     *
     * @param refreshToken the refresh token to use for obtaining a new access token
     * @return AuthResponse containing new access token and rotated refresh token
     * @throws RuntimeException if refresh token is invalid or expired
     * @throws RuntimeException if user associated with token is not found
     */
    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        log.info("Refresh token request");

        // Find and validate refresh token
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!refreshTokenService.validateRefreshToken(token)) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        // Get user
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        // Rotate refresh token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(token);

        // Log token refresh event
        auditLogService.logAuthenticationEvent(user.getId(), "TOKEN_REFRESH", null, null);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken.getToken());
        response.setTokenType("Bearer");
        response.setUsername(user.getUsername());
        return response;
    }
}

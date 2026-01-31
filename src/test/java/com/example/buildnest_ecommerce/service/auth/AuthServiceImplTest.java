package com.example.buildnest_ecommerce.service.auth;

import com.example.buildnest_ecommerce.model.entity.RefreshToken;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.AuthResponse;
import com.example.buildnest_ecommerce.model.payload.RegisterRequest;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import com.example.buildnest_ecommerce.util.ValidationUtil;
import com.example.buildnest_ecommerce.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("Password@123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUserId(1L);
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(username, password);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
        verify(userRepository).findByUsername(username);
        verify(refreshTokenService).createRefreshToken(1L);
        verify(auditLogService).logAuthenticationEvent(eq(1L), eq("LOGIN"), any(), any());
    }

    @Test
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.findAll()).thenReturn(new java.util.ArrayList<>());
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAuthenticationEvent(eq(1L), eq("REGISTER"), any(), any());
    }

    @Test
    void testRegisterWithExistingUsername() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("newuser");
        existingUser.setEmail("existing@example.com");

        when(userRepository.findAll()).thenReturn(java.util.List.of(existingUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRefreshAccessToken() {
        // Arrange
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("old-refresh-token");
        refreshToken.setUserId(1L);

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.validateRefreshToken(any())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateTokenFromUsername(anyString())).thenReturn("new-jwt-token");

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token");
        when(refreshTokenService.rotateRefreshToken(any())).thenReturn(newRefreshToken);

        // Act
        AuthResponse response = authService.refreshAccessToken("old-refresh-token");

        // Assert
        assertNotNull(response);
        assertEquals("new-jwt-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(refreshTokenService).rotateRefreshToken(any());
    }

    @Test
    void testLogout() {
        // Arrange
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUserId(1L);

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        // Act
        authService.logout("refresh-token");

        // Assert
        verify(refreshTokenService).revokeRefreshToken("refresh-token");
        verify(auditLogService).logAuthenticationEvent(eq(1L), eq("LOGOUT"), any(), any());
    }
}

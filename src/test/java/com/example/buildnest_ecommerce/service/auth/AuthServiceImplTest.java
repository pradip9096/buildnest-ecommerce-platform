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
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUsername());
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
    void testRegisterSetsUserFieldsAndValidatesPassword() {
        when(userRepository.findAll()).thenReturn(new java.util.ArrayList<>());
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.register(registerRequest);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("newuser", saved.getUsername());
        assertEquals("new@example.com", saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals("New", saved.getFirstName());
        assertEquals("User", saved.getLastName());
        assertTrue(saved.getIsActive());
        assertFalse(saved.getIsDeleted());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getRoles());
        assertEquals(1, saved.getRoles().size());
        assertEquals("ROLE_USER", saved.getRoles().iterator().next().getName());

        verify(validationUtil).validatePassword("Password@123");
    }

    @Test
    void testRegisterPublishesEvent() {
        when(userRepository.findAll()).thenReturn(new java.util.ArrayList<>());
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.register(registerRequest);

        verify(domainEventPublisher).publish(any());
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
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUsername());
        verify(refreshTokenService).rotateRefreshToken(any());
        verify(auditLogService).logAuthenticationEvent(eq(1L), eq("TOKEN_REFRESH"), any(), any());
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

    @Test
    void testValidateTokenReturnsTrue() {
        when(jwtTokenProvider.validateToken("token")).thenReturn(true);

        assertTrue(authService.validateToken("token"));
        verify(jwtTokenProvider).validateToken("token");
    }

    @Test
    void testValidateTokenReturnsFalse() {
        when(jwtTokenProvider.validateToken("token")).thenReturn(false);

        assertFalse(authService.validateToken("token"));
        verify(jwtTokenProvider).validateToken("token");
    }

    @Test
    void testValidateTokenReturnsFalseOnException() {
        when(jwtTokenProvider.validateToken("bad-token")).thenThrow(new RuntimeException("bad"));

        assertFalse(authService.validateToken("bad-token"));
        verify(jwtTokenProvider).validateToken("bad-token");
    }

    @Test
    void testLoginFailureThrows() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("auth failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login("user", "bad"));
        assertTrue(ex.getMessage().contains("Login failed"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRefreshAccessTokenInvalidTokenThrows() {
        when(refreshTokenService.findByToken("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("missing"));
        assertTrue(ex.getMessage().contains("Invalid refresh token"));
    }

    @Test
    void testRefreshAccessTokenExpiredTokenThrows() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expired");
        refreshToken.setUserId(1L);

        when(refreshTokenService.findByToken("expired")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("expired"));
        assertTrue(ex.getMessage().contains("expired or revoked"));
    }

    @Test
    void testLoginUserNotFoundThrows() {
        String username = "missing";
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(username, "pass"));
        assertTrue(ex.getMessage().contains("Login failed"));
    }

    @Test
    void testLogoutWithNullTokenDoesNothing() {
        authService.logout(null);

        verifyNoInteractions(refreshTokenService);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void testRefreshAccessTokenUserNotFoundThrows() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("valid");
        refreshToken.setUserId(1L);

        when(refreshTokenService.findByToken("valid")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("valid"));
        assertTrue(ex.getMessage().contains("User not found"));
    }
}

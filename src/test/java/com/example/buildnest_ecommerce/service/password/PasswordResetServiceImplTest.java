package com.example.buildnest_ecommerce.service.password;

import com.example.buildnest_ecommerce.model.entity.PasswordResetToken;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.PasswordResetTokenRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import com.example.buildnest_ecommerce.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetServiceImpl tests")
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Test
    @DisplayName("Should silently ignore missing email")
    void testInitiatePasswordResetMissingUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        passwordResetService.initiatePasswordReset("missing@example.com");

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Should create reset token for existing user")
    void testInitiatePasswordResetCreatesToken() {
        ReflectionTestUtils.setField(passwordResetService, "resetTokenExpirationMs", 60000L);

        User user = new User();
        user.setId(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.initiatePasswordReset("user@example.com");

        verify(passwordResetTokenRepository).invalidateUserTokens(1L);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Should reset password with valid token")
    void testResetPasswordWithToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("token");
        token.setUserId(1L);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);

        User user = new User();
        user.setId(1L);
        user.setPassword("old");

        when(passwordResetTokenRepository.findByToken("token")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encoded");

        passwordResetService.resetPasswordWithToken("token", "NewPass@123");

        verify(validationUtil).validatePassword("NewPass@123");
        verify(userRepository).save(any(User.class));
        verify(passwordResetTokenRepository).markTokenAsUsed(eq("token"), any(LocalDateTime.class));
        verify(refreshTokenService).revokeAllUserTokens(1L);
        verify(auditLogService).logPasswordChange(eq(1L), any(), any());
    }

    @Test
    @DisplayName("Should fail reset on expired token")
    void testResetPasswordExpiredToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("token");
        token.setUserId(1L);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        token.setUsed(false);

        when(passwordResetTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPasswordWithToken("token", "NewPass@123"));
    }

    @Test
    @DisplayName("Should change password with correct old password")
    void testChangePassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("encodedOld");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("new"))
                .thenReturn("encodedNew");

        passwordResetService.changePassword(1L, "old", "new", "ip", "agent");

        verify(validationUtil).validatePassword("new");
        verify(refreshTokenService).revokeAllUserTokens(1L);
        verify(auditLogService).logPasswordChange(1L, "ip", "agent");
    }

    @Test
    @DisplayName("Should reject incorrect old password")
    void testChangePasswordWrongOldPassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("encodedOld");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.changePassword(1L, "old", "new", "ip", "agent"));
    }
}

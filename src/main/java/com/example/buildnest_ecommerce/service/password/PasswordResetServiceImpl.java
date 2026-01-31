package com.example.buildnest_ecommerce.service.password;

import com.example.buildnest_ecommerce.model.entity.PasswordResetToken;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.PasswordResetTokenRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import com.example.buildnest_ecommerce.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ValidationUtil validationUtil;

    @Value("${password.reset.token.expiration:3600000}")
    private long resetTokenExpirationMs; // Default: 1 hour

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        // Verify user exists (silently fail for security reasons)
        var userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Don't reveal that user doesn't exist for security
            return;
        }

        User user = userOptional.get();

        // Invalidate any existing unused tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(user.getId());

        // Generate secure reset token
        String token = UUID.randomUUID().toString();

        // Create and store password reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserId(user.getId());
        resetToken.setExpiryDate(LocalDateTime.now().plusSeconds(resetTokenExpirationMs / 1000));
        resetToken.setUsed(false);

        passwordResetTokenRepository.save(resetToken);

        // Note: Email sending will be implemented via EmailService
        // In production, send email with reset link containing token
        // Example: https://yourapp.com/reset-password?token={token}
        log.info("Password reset token generated for email: {} (Token would be sent via email in production)", email);
    }

    @Override
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        log.info("Password reset attempt with token");

        // Find token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        // Validate token is not used
        if (resetToken.isUsed()) {
            log.warn("Attempted to use already-used password reset token");
            throw new IllegalArgumentException("Password reset token has already been used");
        }

        // Validate token is not expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Attempted to use expired password reset token");
            throw new IllegalArgumentException("Password reset token has expired");
        }

        // Find user
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Enforce password policy
        validationUtil.validatePassword(newPassword);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        passwordResetTokenRepository.markTokenAsUsed(token, LocalDateTime.now());

        // Revoke all refresh tokens for security
        refreshTokenService.revokeAllUserTokens(user.getId());

        // Audit password reset
        auditLogService.logPasswordChange(user.getId(), null, null);

        log.info("Password reset successfully for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword, String ipAddress,
            String userAgent) {
        log.info("Password change requested for user: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Enforce password policy
        validationUtil.validatePassword(newPassword);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens upon credential change
        refreshTokenService.revokeAllUserTokens(userId);

        // Audit password change
        auditLogService.logPasswordChange(userId, ipAddress, userAgent);

        log.info("Password changed successfully for user: {}", userId);
    }
}

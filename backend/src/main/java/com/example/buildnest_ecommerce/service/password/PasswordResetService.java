package com.example.buildnest_ecommerce.service.password;

public interface PasswordResetService {
    /**
     * Initiates password reset process by sending reset link to email
     * @param email User's email address
     */
    void initiatePasswordReset(String email);
    
    /**
     * Resets password using reset token
     * @param token Password reset token
     * @param newPassword New password to set
     */
    void resetPasswordWithToken(String token, String newPassword);
    
    /**
     * Changes user password after validating old password
     * @param userId User ID
     * @param oldPassword Current password for verification
     * @param newPassword New password to set
     * @param ipAddress IP address for audit log
     * @param userAgent User agent for audit log
     */
    void changePassword(Long userId, String oldPassword, String newPassword, String ipAddress, String userAgent);
}

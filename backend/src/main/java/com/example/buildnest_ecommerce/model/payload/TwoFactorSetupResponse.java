package com.example.buildnest_ecommerce.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned from {@code POST /api/user/2fa/enable} -- the QR code (base64
 * PNG data URI) and manual-entry secret. TOTP is not yet active; the
 * caller must confirm possession via {@code POST /api/user/2fa/verify}
 * before it is.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupResponse {
    private String secret;
    private String qrCodeDataUri;
}

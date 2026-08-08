package com.example.buildnest_ecommerce.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Returned exactly once, at 2FA-enable-confirmation time. Codes are
 * never retrievable again afterward -- only their BCrypt hashes are
 * persisted.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryCodesResponse {
    private List<String> recoveryCodes;
}

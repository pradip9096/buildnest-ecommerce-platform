package com.example.buildnest_ecommerce.service.auth;

import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.RecoveryCodesResponse;
import com.example.buildnest_ecommerce.model.payload.TwoFactorSetupResponse;

public interface TwoFactorService {
    TwoFactorSetupResponse generateSecret(User user);

    RecoveryCodesResponse verifyAndEnable(User user, String code);

    void disable(User user, String code);

    boolean validateLoginCode(User user, String code);
}

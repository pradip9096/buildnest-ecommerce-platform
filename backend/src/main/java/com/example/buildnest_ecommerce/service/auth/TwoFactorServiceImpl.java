package com.example.buildnest_ecommerce.service.auth;

import com.example.buildnest_ecommerce.model.entity.RecoveryCode;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.RecoveryCodesResponse;
import com.example.buildnest_ecommerce.model.payload.TwoFactorSetupResponse;
import com.example.buildnest_ecommerce.repository.RecoveryCodeRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * TOTP-based 2FA (#91, AUTH-02) -- RFC 6238 secret generation/verification,
 * QR provisioning, and one-time recovery codes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorServiceImpl implements TwoFactorService {
    private static final int RECOVERY_CODE_COUNT = 8;
    private static final String ISSUER = "BuildNest";
    private static final String RECOVERY_CODE_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserRepository userRepository;
    private final RecoveryCodeRepository recoveryCodeRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecretGenerator secretGenerator =
            new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(), new SystemTimeProvider());
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public TwoFactorSetupResponse generateSecret(User user) {
        // CRITICAL fix (java-reviewer, #91): without this guard, calling
        // /enable again on an already-enabled account silently overwrote
        // totpSecret and flipped totpEnabled=false with no code/reauth
        // check -- a stolen session could disable 2FA outright. Disabling
        // must go through disable(), which requires a valid TOTP code.
        if (Boolean.TRUE.equals(user.getTotpEnabled())) {
            throw new RuntimeException(
                    "2FA is already enabled; disable it first to re-provision");
        }
        String secret = secretGenerator.generate();
        user.setTotpSecret(secret);
        user.setTotpEnabled(false);
        userRepository.save(user);

        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        try {
            byte[] imageData = qrGenerator.generate(qrData);
            String encoded = Base64.getEncoder().encodeToString(imageData);
            String dataUri = "data:" + qrGenerator.getImageMimeType()
                    + ";base64," + encoded;
            return new TwoFactorSetupResponse(secret, dataUri);
        } catch (QrGenerationException e) {
            log.error("Failed to generate 2FA QR code for user {}",
                    user.getId(), e);
            throw new RuntimeException("Failed to generate QR code");
        }
    }

    @Override
    @Transactional
    public RecoveryCodesResponse verifyAndEnable(User user, String code) {
        if (user.getTotpSecret() == null) {
            throw new RuntimeException("2FA setup was not initiated");
        }
        if (!codeVerifier.isValidCode(user.getTotpSecret(), code)) {
            throw new RuntimeException("Invalid TOTP code");
        }
        user.setTotpEnabled(true);
        user.setTotpEnabledAt(LocalDateTime.now());
        userRepository.save(user);

        recoveryCodeRepository.deleteAllByUserId(user.getId());
        List<String> rawCodes = new ArrayList<>(RECOVERY_CODE_COUNT);
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            String rawCode = generateRecoveryCode();
            rawCodes.add(rawCode);
            RecoveryCode entity = new RecoveryCode();
            entity.setUser(user);
            entity.setCodeHash(passwordEncoder.encode(rawCode));
            entity.setUsed(false);
            entity.setCreatedAt(LocalDateTime.now());
            recoveryCodeRepository.save(entity);
        }
        return new RecoveryCodesResponse(rawCodes);
    }

    @Override
    @Transactional
    public void disable(User user, String code) {
        if (!Boolean.TRUE.equals(user.getTotpEnabled())) {
            throw new RuntimeException("2FA is not enabled");
        }
        if (!codeVerifier.isValidCode(user.getTotpSecret(), code)) {
            throw new RuntimeException("Invalid TOTP code");
        }
        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        user.setTotpEnabledAt(null);
        userRepository.save(user);
        recoveryCodeRepository.deleteAllByUserId(user.getId());
    }

    @Override
    @Transactional
    public boolean validateLoginCode(User user, String code) {
        if (user.getTotpSecret() != null
                && codeVerifier.isValidCode(user.getTotpSecret(), code)) {
            return true;
        }
        return consumeRecoveryCode(user, code);
    }

    private boolean consumeRecoveryCode(User user, String code) {
        List<RecoveryCode> unused = recoveryCodeRepository
                .findByUserIdAndUsedFalse(user.getId());
        for (RecoveryCode recoveryCode : unused) {
            if (passwordEncoder.matches(code, recoveryCode.getCodeHash())) {
                int updated = recoveryCodeRepository.markUsedIfUnused(
                        recoveryCode.getId(), LocalDateTime.now());
                // updated == 0 means a concurrent request already consumed
                // this code between the read above and this atomic update.
                return updated == 1;
            }
        }
        return false;
    }

    private String generateRecoveryCode() {
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                sb.append('-');
            }
            sb.append(RECOVERY_CODE_ALPHABET.charAt(
                    secureRandom.nextInt(RECOVERY_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}

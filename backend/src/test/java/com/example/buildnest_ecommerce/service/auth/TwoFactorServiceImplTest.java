package com.example.buildnest_ecommerce.service.auth;

import com.example.buildnest_ecommerce.model.entity.RecoveryCode;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.RecoveryCodesResponse;
import com.example.buildnest_ecommerce.model.payload.TwoFactorSetupResponse;
import com.example.buildnest_ecommerce.repository.RecoveryCodeRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecoveryCodeRepository recoveryCodeRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();

    private TwoFactorServiceImpl twoFactorService;
    private User user;
    private String secret;

    @BeforeEach
    void setUp() {
        twoFactorService = new TwoFactorServiceImpl(
                userRepository, recoveryCodeRepository, passwordEncoder);
        secret = new DefaultSecretGenerator().generate();
        user = new User();
        user.setId(1L);
        user.setUsername("buildnest_user");
        user.setEmail("user@buildnest.test");
        user.setTotpSecret(secret);
        user.setTotpEnabled(false);
    }

    private String currentValidCode() throws Exception {
        return codeGenerator.generate(secret,
                timeProvider.getTime() / 30);
    }

    @Test
    @DisplayName("generateSecret assigns a secret and returns a QR data URI")
    void generateSecret_assignsSecretAndReturnsQrDataUri() {
        TwoFactorSetupResponse response =
                twoFactorService.generateSecret(user);

        assertThat(response.getSecret()).isNotBlank();
        assertThat(response.getQrCodeDataUri()).startsWith("data:image/png;base64,");
        assertThat(user.getTotpSecret()).isEqualTo(response.getSecret());
        assertThat(user.getTotpEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("generateSecret rejects re-provisioning an already-enabled account")
    void generateSecret_alreadyEnabled_throwsAndDoesNotOverwriteSecret() {
        String originalSecret = secret;
        user.setTotpEnabled(true);

        assertThatThrownBy(() -> twoFactorService.generateSecret(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already enabled");

        assertThat(user.getTotpSecret()).isEqualTo(originalSecret);
        assertThat(user.getTotpEnabled()).isTrue();
    }

    @Test
    @DisplayName("verifyAndEnable with a valid code enables 2FA and returns 8 recovery codes")
    void verifyAndEnable_validCode_enablesAndReturnsEightRecoveryCodes() throws Exception {
        String validCode = currentValidCode();

        RecoveryCodesResponse response =
                twoFactorService.verifyAndEnable(user, validCode);

        assertThat(user.getTotpEnabled()).isTrue();
        assertThat(user.getTotpEnabledAt()).isNotNull();
        assertThat(response.getRecoveryCodes()).hasSize(8);
        assertThat(response.getRecoveryCodes()).doesNotHaveDuplicates();
        verify(recoveryCodeRepository).deleteAllByUserId(user.getId());
        verify(recoveryCodeRepository, org.mockito.Mockito.times(8))
                .save(any(RecoveryCode.class));
    }

    @Test
    @DisplayName("verifyAndEnable with an invalid code throws and does not enable 2FA")
    void verifyAndEnable_invalidCode_throwsAndLeavesDisabled() {
        assertThatThrownBy(() -> twoFactorService.verifyAndEnable(user, "000000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid TOTP code");

        assertThat(user.getTotpEnabled()).isFalse();
    }

    @Test
    @DisplayName("verifyAndEnable throws when setup was never initiated")
    void verifyAndEnable_noSecret_throws() {
        user.setTotpSecret(null);

        assertThatThrownBy(() -> twoFactorService.verifyAndEnable(user, "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not initiated");
    }

    @Test
    @DisplayName("disable with a valid code clears the secret and disables 2FA")
    void disable_validCode_clearsSecretAndDisables() throws Exception {
        user.setTotpEnabled(true);
        String validCode = currentValidCode();

        twoFactorService.disable(user, validCode);

        assertThat(user.getTotpEnabled()).isFalse();
        assertThat(user.getTotpSecret()).isNull();
        verify(recoveryCodeRepository).deleteAllByUserId(user.getId());
    }

    @Test
    @DisplayName("disable with an invalid code throws and leaves 2FA enabled")
    void disable_invalidCode_throwsAndLeavesEnabled() {
        user.setTotpEnabled(true);

        assertThatThrownBy(() -> twoFactorService.disable(user, "000000"))
                .isInstanceOf(RuntimeException.class);

        assertThat(user.getTotpEnabled()).isTrue();
    }

    @Test
    @DisplayName("disable throws when 2FA is not currently enabled")
    void disable_notEnabled_throws() {
        assertThatThrownBy(() -> twoFactorService.disable(user, "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not enabled");
    }

    @Test
    @DisplayName("validateLoginCode accepts a valid current TOTP code")
    void validateLoginCode_validTotpCode_returnsTrue() throws Exception {
        String validCode = currentValidCode();

        boolean result = twoFactorService.validateLoginCode(user, validCode);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateLoginCode rejects an invalid TOTP code with no matching recovery code")
    void validateLoginCode_invalidCode_returnsFalse() {
        when(recoveryCodeRepository.findByUserIdAndUsedFalse(user.getId()))
                .thenReturn(List.of());

        boolean result = twoFactorService.validateLoginCode(user, "000000");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateLoginCode consumes and marks used a matching recovery code")
    void validateLoginCode_matchingRecoveryCode_marksUsedAndReturnsTrue() {
        String rawCode = "ABCD-1234";
        RecoveryCode recoveryCode = new RecoveryCode();
        recoveryCode.setId(10L);
        recoveryCode.setUser(user);
        recoveryCode.setCodeHash(passwordEncoder.encode(rawCode));
        recoveryCode.setUsed(false);
        when(recoveryCodeRepository.findByUserIdAndUsedFalse(user.getId()))
                .thenReturn(List.of(recoveryCode));
        when(recoveryCodeRepository.markUsedIfUnused(
                org.mockito.ArgumentMatchers.eq(10L), any())).thenReturn(1);

        boolean result = twoFactorService.validateLoginCode(user, rawCode);

        assertThat(result).isTrue();
        verify(recoveryCodeRepository).markUsedIfUnused(
                org.mockito.ArgumentMatchers.eq(10L), any());
    }

    @Test
    @DisplayName("validateLoginCode rejects a recovery code raced-consumed by a concurrent request")
    void validateLoginCode_concurrentlyConsumedCode_returnsFalse() {
        String rawCode = "ABCD-1234";
        RecoveryCode recoveryCode = new RecoveryCode();
        recoveryCode.setId(11L);
        recoveryCode.setUser(user);
        recoveryCode.setCodeHash(passwordEncoder.encode(rawCode));
        recoveryCode.setUsed(false);
        when(recoveryCodeRepository.findByUserIdAndUsedFalse(user.getId()))
                .thenReturn(List.of(recoveryCode));
        // Simulates a concurrent request already having consumed this code
        // between the read above and this atomic update -- affected-row
        // count is 0, so this request must not treat it as a valid login.
        when(recoveryCodeRepository.markUsedIfUnused(
                org.mockito.ArgumentMatchers.eq(11L), any())).thenReturn(0);

        boolean result = twoFactorService.validateLoginCode(user, rawCode);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateLoginCode rejects an already-used recovery code's raw value once re-checked")
    void validateLoginCode_noUnusedCodesMatch_returnsFalse() {
        RecoveryCode usedCode = new RecoveryCode();
        usedCode.setUser(user);
        usedCode.setCodeHash(passwordEncoder.encode("WXYZ-9999"));
        usedCode.setUsed(true);
        // findByUserIdAndUsedFalse would never actually return an already-used
        // code in production; simulated here as an empty result to prove a
        // stale/used code's raw value no longer validates.
        when(recoveryCodeRepository.findByUserIdAndUsedFalse(user.getId()))
                .thenReturn(List.of());

        boolean result = twoFactorService.validateLoginCode(user, "WXYZ-9999");

        assertThat(result).isFalse();
    }
}

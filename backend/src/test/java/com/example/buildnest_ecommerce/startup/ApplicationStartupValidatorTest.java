package com.example.buildnest_ecommerce.startup;

import com.example.buildnest_ecommerce.security.JwtKeyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationStartupValidator")
class ApplicationStartupValidatorTest {

    @Mock
    private JwtKeyValidator jwtKeyValidator;

    @InjectMocks
    private ApplicationStartupValidator validator;

    @Test
    @DisplayName("validateApplicationStartup completes when JWT key is valid")
    void validateApplicationStartup_validKey_completesNormally() {
        // jwtKeyValidator.validateJwtKey() is a void no-op by default on a Mockito mock
        assertDoesNotThrow(() -> validator.validateApplicationStartup());
        verify(jwtKeyValidator).validateJwtKey();
    }

    @Test
    @DisplayName("validateApplicationStartup wraps validator failure in RuntimeException")
    void validateApplicationStartup_validatorThrows_wrapsInRuntimeException() {
        IllegalArgumentException cause = new IllegalArgumentException("JWT_SECRET is too short");
        doThrow(cause).when(jwtKeyValidator).validateJwtKey();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> validator.validateApplicationStartup());

        assertEquals("Critical security validation failed", ex.getMessage());
        assertSame(cause, ex.getCause(),
                "Original exception must be preserved as the cause");
    }
}

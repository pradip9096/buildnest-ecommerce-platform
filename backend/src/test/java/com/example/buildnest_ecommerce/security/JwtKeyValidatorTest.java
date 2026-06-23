package com.example.buildnest_ecommerce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtKeyValidatorTest {

    private JwtKeyValidator jwtKeyValidator;

    @BeforeEach
    void setUp() {
        jwtKeyValidator = new JwtKeyValidator();
    }

    @Test
    void validateJwtKeyShouldSucceedWithValidKey() {
        String validKey = Base64.getEncoder().encodeToString(new byte[64]); // 512 bits
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", validKey);

        assertDoesNotThrow(() -> jwtKeyValidator.validateJwtKey());
    }

    @Test
    void validateJwtKeyShouldFailWithNullKey() {
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtKeyValidator.validateJwtKey());
        assertTrue(exception.getMessage().contains("JWT_SECRET environment variable is not set"));
    }

    @Test
    void validateJwtKeyShouldFailWithEmptyKey() {
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", "");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtKeyValidator.validateJwtKey());
        assertTrue(exception.getMessage().contains("JWT_SECRET environment variable is not set"));
    }

    @Test
    void validateJwtKeyShouldFailWithShortKey() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[32]); // 256 bits - too short
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", shortKey);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtKeyValidator.validateJwtKey());
        assertTrue(exception.getMessage().contains("JWT_SECRET is too short"));
        assertTrue(exception.getMessage().contains("256 bits"));
        assertTrue(exception.getMessage().contains("512 bits minimum"));
    }

    @Test
    void validateJwtKeyShouldFailWithInvalidBase64() {
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", "not-valid-base64!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtKeyValidator.validateJwtKey());
        assertTrue(exception.getMessage().contains("JWT_SECRET must be valid base64"));
    }

    @Test
    void validateJwtKeyShouldSucceedWith512Bits() {
        String key512 = Base64.getEncoder().encodeToString(new byte[64]);
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", key512);

        assertDoesNotThrow(() -> jwtKeyValidator.validateJwtKey());
    }

    @Test
    void validateJwtKeyShouldSucceedWith1024Bits() {
        String key1024 = Base64.getEncoder().encodeToString(new byte[128]);
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", key1024);

        assertDoesNotThrow(() -> jwtKeyValidator.validateJwtKey());
    }

    @Test
    void validateJwtKeyShouldFailWith256Bits() {
        String key256 = Base64.getEncoder().encodeToString(new byte[32]);
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", key256);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtKeyValidator.validateJwtKey());
        assertTrue(exception.getMessage().contains("256 bits"));
    }

    @Test
    void validateJwtKeyShouldFailWith384Bits() {
        String key384 = Base64.getEncoder().encodeToString(new byte[48]);
        ReflectionTestUtils.setField(jwtKeyValidator, "jwtSecret", key384);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtKeyValidator.validateJwtKey());
        assertTrue(exception.getMessage().contains("384 bits"));
    }
}

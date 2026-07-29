package com.example.buildnest_ecommerce.util;

import com.example.buildnest_ecommerce.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Uses literal IP addresses (never hostnames) so InetAddress.getByName
 * parses them directly with no real DNS lookup, keeping this test
 * deterministic and offline-safe.
 */
@DisplayName("SsrfUrlValidator Tests")
class SsrfUrlValidatorTest {

    private final SsrfUrlValidator validator = new SsrfUrlValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "http://127.0.0.1/webhook",
            "http://localhost/webhook",
            "http://169.254.169.254/latest/meta-data/",
            "http://10.0.0.5/webhook",
            "http://172.16.0.5/webhook",
            "http://192.168.1.5/webhook",
            "http://0.0.0.0/webhook",
            "http://[::1]/webhook"
    })
    @DisplayName("Should reject loopback, link-local, and private URLs")
    void validate_privateOrLoopbackHost_throwsValidationException(String url) {
        assertThrows(ValidationException.class, () -> validator.validate(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://8.8.8.8/webhook",
            "https://1.1.1.1/webhook"
    })
    @DisplayName("Should accept public IP targets")
    void validate_publicIpHost_doesNotThrow(String url) {
        assertDoesNotThrow(() -> validator.validate(url));
    }

    @Test
    @DisplayName("Should reject a URL with no host")
    void validate_missingHost_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> validator.validate("not-a-url"));
    }
}

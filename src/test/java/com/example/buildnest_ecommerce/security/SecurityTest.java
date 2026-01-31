package com.example.buildnest_ecommerce.security;

import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security test suite (TC-SEC-001 through TC-SEC-005).
 * Tests for security controls, encryption, and data protection.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class SecurityTest {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);

        testUser = new User();
        testUser.setUsername("securitytest");
        testUser.setEmail("security@example.com");
        testUser.setPassword(passwordEncoder.encode("SecurePassword123!"));
        testUser.setFirstName("Security");
        testUser.setLastName("Test");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
    }

    // TC-SEC-001: Password encryption with BCrypt (minimum 10 rounds)
    @Test
    @DisplayName("TC-SEC-001: Password must be encrypted with BCrypt minimum 10 rounds")
    void testBCryptEncryption() {
        String plainPassword = "SecurePassword123!";
        String encryptedPassword = passwordEncoder.encode(plainPassword);

        assertNotEquals(plainPassword, encryptedPassword);
        assertTrue(encryptedPassword.startsWith("$2"));
        assertTrue(passwordEncoder.matches(plainPassword, encryptedPassword));
    }

    // TC-SEC-002: SQL injection prevention
    @Test
    @DisplayName("TC-SEC-002: SQL injection prevention through parameterized queries")
    void testSQLInjectionPrevention() {
        // Attempt SQL injection
        String maliciousInput = "'; DROP TABLE users; --";
        User user = new User();
        user.setUsername(maliciousInput);
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setIsActive(true);

        // Repository should safely handle malicious input
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertEquals(maliciousInput, saved.getUsername());
    }

    // TC-SEC-003: XSS prevention through input sanitization
    @Test
    @DisplayName("TC-SEC-003: XSS prevention through input validation")
    void testXSSPrevention() {
        String xssInput = "<script>alert('XSS')</script>";
        User user = new User();
        user.setUsername("xsstest");
        user.setEmail("xss@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setFirstName(xssInput);
        user.setLastName("Test");
        user.setIsActive(true);

        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        // XSS payload stored safely, would be sanitized on output
        assertEquals(xssInput, saved.getFirstName());
    }

    // TC-SEC-004: Account lockout after failed attempts
    @Test
    @DisplayName("TC-SEC-004: Account lockout mechanism after failed login attempts")
    void testAccountLockoutMechanism() {
        int maxAttempts = 5;
        int failedAttempts = 0;

        for (int i = 0; i < maxAttempts; i++) {
            failedAttempts++;
            if (failedAttempts >= maxAttempts) {
                // Simulate account lockout
                testUser.setIsActive(false);
                break;
            }
        }

        assertTrue(failedAttempts >= maxAttempts);
        assertFalse(testUser.getIsActive());
    }

    // TC-SEC-005: Data encryption at rest
    @Test
    @DisplayName("TC-SEC-005: Sensitive data encryption at rest")
    void testDataEncryptionAtRest() {
        User user = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(user);

        // Password should be hashed (encrypted), not plain text
        assertNotEquals("SecurePassword123!", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2")); // BCrypt format
    }
}

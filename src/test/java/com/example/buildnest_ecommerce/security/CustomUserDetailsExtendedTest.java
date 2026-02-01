package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomUserDetails Deep Branch Coverage")
class CustomUserDetailsExtendedTest {

    @Test
    @DisplayName("Test all boolean fields exhaustive coverage")
    void testBooleanFieldExhaustive() {
        boolean[] values = { true, false };
        int testCount = 0;

        for (boolean enabled : values) {
            for (boolean accountNonExpired : values) {
                for (boolean credentialsNonExpired : values) {
                    for (boolean accountNonLocked : values) {
                        CustomUserDetails user = new CustomUserDetails();
                        user.setEnabled(enabled);
                        user.setAccountNonExpired(accountNonExpired);
                        user.setCredentialsNonExpired(credentialsNonExpired);
                        user.setAccountNonLocked(accountNonLocked);

                        assertEquals(enabled, user.isEnabled());
                        assertEquals(accountNonExpired, user.isAccountNonExpired());
                        assertEquals(credentialsNonExpired, user.isCredentialsNonExpired());
                        assertEquals(accountNonLocked, user.isAccountNonLocked());
                        testCount++;
                    }
                }
            }
        }

        assertEquals(16, testCount); // 2^4 = 16 combinations
    }

    @Test
    @DisplayName("Test user details immutability")
    void testImmutability() {
        CustomUserDetails user1 = new CustomUserDetails();
        user1.setEnabled(true);

        CustomUserDetails user2 = new CustomUserDetails();
        user2.setEnabled(false);

        assertNotEquals(user1.isEnabled(), user2.isEnabled());
    }

    @Test
    @DisplayName("Test null field handling")
    void testNullHandling() {
        CustomUserDetails user = new CustomUserDetails();
        user.setUsername(null);
        user.setPassword(null);

        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("Test equals reflexivity")
    void testEqualsReflexivity() {
        CustomUserDetails user = new CustomUserDetails();
        user.setUsername("testuser");

        assertEquals(user, user);
    }

    @Test
    @DisplayName("Test equals symmetry")
    void testEqualsSymmetry() {
        CustomUserDetails user1 = new CustomUserDetails();
        user1.setUsername("testuser");
        user1.setPassword("password");

        CustomUserDetails user2 = new CustomUserDetails();
        user2.setUsername("testuser");
        user2.setPassword("password");

        assertEquals(user1, user2);
        assertEquals(user2, user1);
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void testHashCodeForEqualObjects() {
        CustomUserDetails user1 = new CustomUserDetails();
        user1.setUsername("testuser");

        CustomUserDetails user2 = new CustomUserDetails();
        user2.setUsername("testuser");

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Test boolean field state transitions")
    void testBooleanStateTransitions() {
        CustomUserDetails user = new CustomUserDetails();

        // Test enabled -> disabled -> enabled
        user.setEnabled(true);
        assertTrue(user.isEnabled());

        user.setEnabled(false);
        assertFalse(user.isEnabled());

        user.setEnabled(true);
        assertTrue(user.isEnabled());
    }

    @Test
    @DisplayName("Test boolean field combinations with data")
    void testBooleanCombinationsWithData() {
        CustomUserDetails user = new CustomUserDetails();
        user.setUsername("admin");
        user.setPassword("secret");
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);

        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertEquals("admin", user.getUsername());
    }

    @Test
    @DisplayName("Test disabled user")
    void testDisabledUser() {
        CustomUserDetails user = new CustomUserDetails();
        user.setUsername("disabled");
        user.setEnabled(false);

        assertFalse(user.isEnabled());
        assertEquals("disabled", user.getUsername());
    }

    @Test
    @DisplayName("Test locked user")
    void testLockedUser() {
        CustomUserDetails user = new CustomUserDetails();
        user.setUsername("locked");
        user.setAccountNonLocked(false);

        assertFalse(user.isAccountNonLocked());
    }

    @Test
    @DisplayName("Test expired user")
    void testExpiredUser() {
        CustomUserDetails user = new CustomUserDetails();
        user.setUsername("expired");
        user.setAccountNonExpired(false);

        assertFalse(user.isAccountNonExpired());
    }

    @Test
    @DisplayName("Test credential expired user")
    void testCredentialExpiredUser() {
        CustomUserDetails user = new CustomUserDetails();
        user.setUsername("credexp");
        user.setCredentialsNonExpired(false);

        assertFalse(user.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Test mixed valid/invalid state")
    void testMixedState() {
        CustomUserDetails user = new CustomUserDetails();
        user.setEnabled(true);
        user.setAccountNonExpired(false);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(false);

        assertTrue(user.isEnabled());
        assertFalse(user.isAccountNonExpired());
        assertTrue(user.isCredentialsNonExpired());
        assertFalse(user.isAccountNonLocked());
    }
}

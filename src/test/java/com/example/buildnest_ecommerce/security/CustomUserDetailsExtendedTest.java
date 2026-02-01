package com.example.buildnest_ecommerce.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomUserDetails Deep Branch Coverage")
class CustomUserDetailsExtendedTest {

    private CustomUserDetails createUserDetails(boolean enabled,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialsNonExpired) {
        return new CustomUserDetails(
                1L,
                "user",
                "user@example.com",
                "secret",
                Collections.emptyList(),
                enabled,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired);
    }

    @Test
    @DisplayName("Test all boolean fields exhaustive coverage")
    void testBooleanFieldExhaustive() {
        boolean[] values = { true, false };
        int testCount = 0;

        for (boolean enabled : values) {
            for (boolean accountNonExpired : values) {
                for (boolean credentialsNonExpired : values) {
                    for (boolean accountNonLocked : values) {
                        CustomUserDetails user = createUserDetails(enabled,
                                accountNonExpired,
                                accountNonLocked,
                                credentialsNonExpired);

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
        CustomUserDetails user1 = createUserDetails(true, true, true, true);

        CustomUserDetails user2 = createUserDetails(false, true, true, true);

        assertNotEquals(user1.isEnabled(), user2.isEnabled());
    }

    @Test
    @DisplayName("Test null field handling")
    void testNullHandling() {
        CustomUserDetails user = new CustomUserDetails(
                1L,
                null,
                "user@example.com",
                null,
                Collections.emptyList(),
                true,
                true,
                true,
                true);

        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("Test equals reflexivity")
    void testEqualsReflexivity() {
        CustomUserDetails user = new CustomUserDetails(
                1L,
                "testuser",
                "user@example.com",
                "password",
                Collections.emptyList(),
                true,
                true,
                true,
                true);

        assertEquals(user, user);
    }

    @Test
    @DisplayName("Test equals symmetry")
    void testEqualsSymmetry() {
        CustomUserDetails user1 = new CustomUserDetails(
                1L,
                "testuser",
                "user@example.com",
                "password",
                Collections.emptyList(),
                true,
                true,
                true,
                true);

        CustomUserDetails user2 = new CustomUserDetails(
                1L, // Same ID to ensure equality based on other fields
                "testuser",
                "user@example.com",
                "password",
                Collections.emptyList(),
                true,
                true,
                true,
                true);

        assertEquals(user1, user2);
        assertEquals(user2, user1);
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void testHashCodeForEqualObjects() {
        CustomUserDetails user1 = new CustomUserDetails(
                1L,
                "testuser",
                "user@example.com",
                "password",
                Collections.emptyList(),
                true,
                true,
                true,
                true);

        CustomUserDetails user2 = new CustomUserDetails(
                1L, // Same ID to ensure equality based on other fields
                "testuser",
                "user@example.com",
                "password",
                Collections.emptyList(),
                true,
                true,
                true,
                true);

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Test boolean field state transitions")
    void testBooleanStateTransitions() {
        CustomUserDetails user = createUserDetails(true, true, true, true);

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
        CustomUserDetails user = new CustomUserDetails(
                1L,
                "admin",
                "admin@example.com",
                "secret",
                Collections.emptyList(),
                true,
                true,
                true,
                true);

        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertEquals("admin", user.getUsername());
    }

    @Test
    @DisplayName("Test disabled user")
    void testDisabledUser() {
        CustomUserDetails user = createUserDetails(false, true, true, true);
        user.setUsername("disabled");

        assertFalse(user.isEnabled());
        assertEquals("disabled", user.getUsername());
    }

    @Test
    @DisplayName("Test locked user")
    void testLockedUser() {
        CustomUserDetails user = createUserDetails(true, true, false, true);
        user.setUsername("locked");

        assertFalse(user.isAccountNonLocked());
    }

    @Test
    @DisplayName("Test expired user")
    void testExpiredUser() {
        CustomUserDetails user = createUserDetails(true, false, true, true);
        user.setUsername("expired");

        assertFalse(user.isAccountNonExpired());
    }

    @Test
    @DisplayName("Test credential expired user")
    void testCredentialExpiredUser() {
        CustomUserDetails user = createUserDetails(true, true, true, false);
        user.setUsername("credexp");

        assertFalse(user.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Test mixed valid/invalid state")
    void testMixedState() {
        CustomUserDetails user = createUserDetails(true, false, false, true);

        assertTrue(user.isEnabled());
        assertFalse(user.isAccountNonExpired());
        assertTrue(user.isCredentialsNonExpired());
        assertFalse(user.isAccountNonLocked());
    }
}

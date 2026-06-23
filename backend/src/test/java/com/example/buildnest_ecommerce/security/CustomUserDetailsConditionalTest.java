package com.example.buildnest_ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Branch coverage tests for CustomUserDetails security entity.
 * Tests all boolean conditions and getters for branch coverage improvement.
 */
class CustomUserDetailsConditionalTest {

    @Test
    void testAccountNonExpiredTrue() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testAccountNonExpiredFalse() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, false, true, true);
        assertFalse(user.isAccountNonExpired());
    }

    @Test
    void testAccountNonLockedTrue() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void testAccountNonLockedFalse() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, false, true);
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void testCredentialsNonExpiredTrue() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testCredentialsNonExpiredFalse() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, false);
        assertFalse(user.isCredentialsNonExpired());
    }

    @Test
    void testEnabledTrue() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertTrue(user.isEnabled());
    }

    @Test
    void testEnabledFalse() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), false, true, true, true);
        assertFalse(user.isEnabled());
    }

    @Test
    void testGetIdNull() {
        CustomUserDetails user = new CustomUserDetails(
                null, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertNull(user.getId());
    }

    @Test
    void testGetIdPositive() {
        CustomUserDetails user = new CustomUserDetails(
                42L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals(42L, user.getId());
    }

    @Test
    void testGetUsernameEmpty() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals("", user.getUsername());
    }

    @Test
    void testGetUsernameNonEmpty() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "john_doe", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals("john_doe", user.getUsername());
    }

    @Test
    void testGetEmailNull() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", null, "password",
                new HashSet<>(), true, true, true, true);
        assertNull(user.getEmail());
    }

    @Test
    void testGetEmailValid() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals("user@example.com", user.getEmail());
    }

    @Test
    void testGetPasswordEmpty() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "",
                new HashSet<>(), true, true, true, true);
        assertEquals("", user.getPassword());
    }

    @Test
    void testGetPasswordEncrypted() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "$2a$10$encrypted",
                new HashSet<>(), true, true, true, true);
        assertEquals("$2a$10$encrypted", user.getPassword());
    }

    @Test
    void testGetAuthoritiesEmpty() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                authorities, true, true, true, true);
        assertEquals(0, user.getAuthorities().size());
    }

    @Test
    void testGetAuthoritiesSingle() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                authorities, true, true, true, true);
        assertEquals(1, user.getAuthorities().size());
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testGetAuthoritiesMultiple() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                authorities, true, true, true, true);
        assertEquals(3, user.getAuthorities().size());
    }

    @Test
    void testAllBooleansFalse() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), false, false, false, false);
        assertFalse(user.isEnabled());
        assertFalse(user.isAccountNonExpired());
        assertFalse(user.isAccountNonLocked());
        assertFalse(user.isCredentialsNonExpired());
    }

    @Test
    void testAllBooleansTrue() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testMixedBooleans() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, false, true, false);
        assertTrue(user.isEnabled());
        assertFalse(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertFalse(user.isCredentialsNonExpired());
    }

    @Test
    void testIdZero() {
        CustomUserDetails user = new CustomUserDetails(
                0L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals(0L, user.getId());
    }

    @Test
    void testIdNegative() {
        CustomUserDetails user = new CustomUserDetails(
                -1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals(-1L, user.getId());
    }

    @Test
    void testIdLargeValue() {
        CustomUserDetails user = new CustomUserDetails(
                Long.MAX_VALUE, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals(Long.MAX_VALUE, user.getId());
    }

    @Test
    void testUsernameLong() {
        String longUsername = "a".repeat(100);
        CustomUserDetails user = new CustomUserDetails(
                1L, longUsername, "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        assertEquals(longUsername, user.getUsername());
    }

    @Test
    void testEmailSpecialChars() {
        String email = "user+test@example.co.uk";
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", email, "password",
                new HashSet<>(), true, true, true, true);
        assertEquals(email, user.getEmail());
    }

    @Test
    void testPasswordSpecialChars() {
        String password = "$2a$10$N9qo8uLO.@!@.Xyz";
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", password,
                new HashSet<>(), true, true, true, true);
        assertEquals(password, user.getPassword());
    }

    @Test
    void testEqualsWithSameId() {
        Set<GrantedAuthority> auth1 = new HashSet<>();
        Set<GrantedAuthority> auth2 = new HashSet<>();

        CustomUserDetails user1 = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                auth1, true, true, true, true);
        CustomUserDetails user2 = new CustomUserDetails(
                1L, "different", "other@example.com", "other",
                auth2, false, false, false, false);

        // Lombok @Data generates equals based on all fields
        assertNotEquals(user1, user2);
    }

    @Test
    void testHashCodeConsistency() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);

        int hash1 = user.hashCode();
        int hash2 = user.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void testToStringNotNull() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);

        assertNotNull(user.toString());
        assertTrue(user.toString().length() > 0);
    }

    @Test
    void testSetterIdFlow() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        user.setId(99L);
        assertEquals(99L, user.getId());
    }

    @Test
    void testSetterUsernameFlow() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());
    }

    @Test
    void testSetterPasswordFlow() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        String newPassword = "$2a$10$newEncrypted";
        user.setPassword(newPassword);
        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void testSetterEnabledFlow() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void testSetterAccountNonExpiredFlow() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        user.setAccountNonExpired(false);
        assertFalse(user.isAccountNonExpired());
    }

    @Test
    void testSetterAccountNonLockedFlow() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        user.setAccountNonLocked(false);
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void testSetterCredentialsNonExpiredFlow() {
        CustomUserDetails user = new CustomUserDetails(
                1L, "user1", "user@example.com", "password",
                new HashSet<>(), true, true, true, true);
        user.setCredentialsNonExpired(false);
        assertFalse(user.isCredentialsNonExpired());
    }
}

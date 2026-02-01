package com.example.buildnest_ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void returnsUserDetailsFields() {
        CustomUserDetails details = new CustomUserDetails(
                1L,
                "user",
                "user@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true,
                true,
                true,
                true);

        assertEquals(1L, details.getId());
        assertEquals("user", details.getUsername());
        assertEquals("user@example.com", details.getEmail());
        assertEquals("password", details.getPassword());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
        assertEquals(1, details.getAuthorities().size());
    }

    @Test
    void testSettersAndLombokGeneration() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "user@test.com", "pass", null, true, true, true, true);

        userDetails.setId(2L);
        userDetails.setUsername("newuser");
        userDetails.setEmail("new@test.com");
        userDetails.setPassword("newpass");
        userDetails.setEnabled(false);
        userDetails.setAccountNonExpired(false);
        userDetails.setAccountNonLocked(false);
        userDetails.setCredentialsNonExpired(false);

        assertEquals(2L, userDetails.getId());
        assertEquals("newuser", userDetails.getUsername());
        assertEquals("new@test.com", userDetails.getEmail());
        assertEquals("newpass", userDetails.getPassword());
        assertFalse(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonExpired());
        assertFalse(userDetails.isAccountNonLocked());
        assertFalse(userDetails.isCredentialsNonExpired());
    }

    @Test
    void testDisabledAccount() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email", "pass", null, false, true, true, true);

        assertFalse(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void testExpiredAccount() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email", "pass", null, true, false, true, true);

        assertTrue(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonExpired());
    }

    @Test
    void testLockedAccount() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email", "pass", null, true, true, false, true);

        assertTrue(userDetails.isAccountNonExpired());
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void testExpiredCredentials() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email", "pass", null, true, true, true, false);

        assertTrue(userDetails.isEnabled());
        assertFalse(userDetails.isCredentialsNonExpired());
    }

    @Test
    void testAllAccountStatusFalse() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email", "pass", null, false, false, false, false);

        assertFalse(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonExpired());
        assertFalse(userDetails.isAccountNonLocked());
        assertFalse(userDetails.isCredentialsNonExpired());
    }

    @Test
    void testEqualsAndHashCode() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"));

        CustomUserDetails user1 = new CustomUserDetails(
                1L, "user", "email", "pass", authorities, true, true, true, true);

        CustomUserDetails user2 = new CustomUserDetails(
                1L, "user", "email", "pass", authorities, true, true, true, true);

        CustomUserDetails user3 = new CustomUserDetails(
                2L, "other", "other@test.com", "pass", authorities, true, true, true, true);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email@test.com", "pass", null, true, true, true, true);

        String toString = userDetails.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("user"));
        assertTrue(toString.contains("email@test.com"));
    }

    @Test
    void testNullAuthorities() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email", "pass", null, true, true, true, true);

        assertNull(userDetails.getAuthorities());
    }

    @Test
    void testEmptyAuthorities() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "user", "email", "pass", Arrays.asList(), true, true, true, true);

        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void testMultipleAuthorities() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MODERATOR"));

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "admin", "admin@test.com", "pass", authorities, true, true, true, true);

        assertEquals(3, userDetails.getAuthorities().size());
    }
}

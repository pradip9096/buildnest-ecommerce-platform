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
        void testEqualsDifferentFields() {
                Collection<GrantedAuthority> authorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_USER"));
                Collection<GrantedAuthority> otherAuthorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_ADMIN"));

                CustomUserDetails base = new CustomUserDetails(
                                1L, "user", "email", "pass", authorities, true, true, true, true);

                assertNotEquals(base, new CustomUserDetails(
                                2L, "user", "email", "pass", authorities, true, true, true, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user2", "email", "pass", authorities, true, true, true, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user", "email2", "pass", authorities, true, true, true, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user", "email", "pass2", authorities, true, true, true, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user", "email", "pass", otherAuthorities, true, true, true, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user", "email", "pass", authorities, false, true, true, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user", "email", "pass", authorities, true, false, true, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user", "email", "pass", authorities, true, true, false, true));
                assertNotEquals(base, new CustomUserDetails(
                                1L, "user", "email", "pass", authorities, true, true, true, false));
                assertNotEquals(base, null);
                assertNotEquals(base, "not-a-user");
        }

        @Test
        void testCanEqualAndHashCodeDifferences() {
                Collection<GrantedAuthority> authorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_USER"));

                CustomUserDetails base = new CustomUserDetails(
                                1L, "user", "email", "pass", authorities, true, true, true, true);
                CustomUserDetails same = new CustomUserDetails(
                                1L, "user", "email", "pass", authorities, true, true, true, true);
                CustomUserDetails different = new CustomUserDetails(
                                2L, "user", "email", "pass", authorities, true, true, true, true);

                assertTrue(base.canEqual(same));
                assertFalse(base.canEqual("other"));
                assertEquals(base.hashCode(), same.hashCode());
                assertNotEquals(base.hashCode(), different.hashCode());
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
        void testHashCodeMatchesExpectedAlgorithm() {
                CustomUserDetails userDetails = new CustomUserDetails(
                                1L,
                                "user",
                                "email@test.com",
                                "pass",
                                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                                true,
                                true,
                                false,
                                true);

                int expected = expectedHashCode(userDetails);
                assertEquals(expected, userDetails.hashCode());
        }

        @Test
        void testEqualsWithNullFields() {
                CustomUserDetails user1 = new CustomUserDetails(
                                null, null, null, null, null, false, false, false, false);
                CustomUserDetails user2 = new CustomUserDetails(
                                null, null, null, null, null, false, false, false, false);
                CustomUserDetails user3 = new CustomUserDetails(
                                1L, null, null, null, null, false, false, false, false);

                assertEquals(user1, user2);
                assertNotEquals(user1, user3);
        }

        private int expectedHashCode(CustomUserDetails userDetails) {
                int result = 1;
                result = result * 59 + (userDetails.isEnabled() ? 79 : 97);
                result = result * 59 + (userDetails.isAccountNonExpired() ? 79 : 97);
                result = result * 59 + (userDetails.isAccountNonLocked() ? 79 : 97);
                result = result * 59 + (userDetails.isCredentialsNonExpired() ? 79 : 97);
                result = result * 59 + (userDetails.getId() == null ? 43 : userDetails.getId().hashCode());
                result = result * 59 + (userDetails.getUsername() == null ? 43 : userDetails.getUsername().hashCode());
                result = result * 59 + (userDetails.getEmail() == null ? 43 : userDetails.getEmail().hashCode());
                result = result * 59 + (userDetails.getPassword() == null ? 43 : userDetails.getPassword().hashCode());
                result = result * 59 +
                                (userDetails.getAuthorities() == null ? 43 : userDetails.getAuthorities().hashCode());
                return result;
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

        @Test
        void testHashCodeChangesWhenFieldsChange() {
                Collection<GrantedAuthority> authorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_USER"));
                Collection<GrantedAuthority> otherAuthorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_ADMIN"));

                CustomUserDetails base = new CustomUserDetails(
                                5L, "user", "email@test.com", "pass", authorities, true, true, true, true);

                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                6L, "user", "email@test.com", "pass", authorities, true, true, true, true).hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user2", "email@test.com", "pass", authorities, true, true, true, true).hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user", "email2@test.com", "pass", authorities, true, true, true, true).hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user", "email@test.com", "pass", otherAuthorities, true, true, true, true)
                                .hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user", "email@test.com", "pass", authorities, false, true, true, true).hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user", "email@test.com", "pass2", authorities, true, true, true, true).hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user", "email@test.com", "pass", authorities, true, false, true, true).hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user", "email@test.com", "pass", authorities, true, true, false, true).hashCode());
                assertNotEquals(base.hashCode(), new CustomUserDetails(
                                5L, "user", "email@test.com", "pass", authorities, true, true, true, false).hashCode());
        }
}

package com.example.buildnest_ecommerce.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionEvaluatorTest {

    @InjectMocks
    private RolePermissionEvaluator rolePermissionEvaluator;

    @Test
    void hasRoleShouldReturnTrueWhenUserHasRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.hasRole(authentication, "USER");

        assertTrue(result);
    }

    @Test
    void hasRoleShouldReturnFalseWhenUserDoesNotHaveRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.hasRole(authentication, "ADMIN");

        assertFalse(result);
    }

    @Test
    void hasRoleShouldReturnFalseWhenAuthenticationIsNull() {
        boolean result = rolePermissionEvaluator.hasRole(null, "USER");

        assertFalse(result);
    }

    @Test
    void hasRoleShouldReturnFalseWhenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        boolean result = rolePermissionEvaluator.hasRole(authentication, "USER");

        assertFalse(result);
    }

    @Test
    void hasAnyRoleShouldReturnTrueWhenUserHasOneOfTheRoles() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.hasAnyRole(authentication, "ADMIN", "USER", "MODERATOR");

        assertTrue(result);
    }

    @Test
    void hasAnyRoleShouldReturnFalseWhenUserHasNoneOfTheRoles() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.hasAnyRole(authentication, "ADMIN", "MODERATOR");

        assertFalse(result);
    }

    @Test
    void isAdminShouldReturnTrueForAdminRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.isAdmin(authentication);

        assertTrue(result);
    }

    @Test
    void isAdminShouldReturnFalseForNonAdminRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.isAdmin(authentication);

        assertFalse(result);
    }

    @Test
    void isUserShouldReturnTrueForUserRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.isUser(authentication);

        assertTrue(result);
    }

    @Test
    void isUserShouldReturnFalseForNonUserRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        boolean result = rolePermissionEvaluator.isUser(authentication);

        assertFalse(result);
    }

    @Test
    void hasRoleShouldWorkWithMultipleRoles() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        assertTrue(rolePermissionEvaluator.hasRole(authentication, "USER"));
        assertTrue(rolePermissionEvaluator.hasRole(authentication, "ADMIN"));
        assertTrue(rolePermissionEvaluator.hasRole(authentication, "MODERATOR"));
        assertFalse(rolePermissionEvaluator.hasRole(authentication, "SUPER_ADMIN"));
    }

    @Test
    void hasRoleShouldBeCaseSensitive() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        assertTrue(rolePermissionEvaluator.hasRole(authentication, "USER"));
        assertFalse(rolePermissionEvaluator.hasRole(authentication, "user"));
    }
}

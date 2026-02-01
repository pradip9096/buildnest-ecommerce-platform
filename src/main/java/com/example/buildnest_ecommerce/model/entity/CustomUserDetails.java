package com.example.buildnest_ecommerce.model.entity;

import java.util.Collection;
import java.util.Objects;

public class CustomUserDetails {
    private Long id;
    private String username;
    private String password;
    private Collection<String> authorities;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;

    public CustomUserDetails() {
    }

    public CustomUserDetails(Long id, String username, String email, String password,
            Collection<String> authorities, boolean enabled, boolean accountNonExpired,
            boolean accountNonLocked, boolean credentialsNonExpired) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<String> authorities) {
        this.authorities = authorities;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomUserDetails that = (CustomUserDetails) o;
        return enabled == that.enabled
                && accountNonExpired == that.accountNonExpired
                && credentialsNonExpired == that.credentialsNonExpired
                && accountNonLocked == that.accountNonLocked
                && Objects.equals(username, that.username)
                && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked);
    }
}

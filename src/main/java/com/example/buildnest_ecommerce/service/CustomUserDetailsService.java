package com.example.buildnest_ecommerce.service;

import com.example.buildnest_ecommerce.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    // Temporary in-memory user store for testing
    // Note: In production, replace with database queries using UserRepository
    private static final Map<String, UserData> users = new HashMap<>();
    
    static {
        // Sample admin user
        users.put("admin", new UserData(
                1L, 
                "admin", 
                "admin@example.com",
                "$2a$10$slYQmyNdGzin7olVi9hFOe1lT4N6L2Aq/RnFVGV5yB9AqO2N3Z0cC", // password: admin123
                "ROLE_ADMIN"
        ));
        
        // Sample regular user
        users.put("user", new UserData(
                2L,
                "user",
                "user@example.com",
                "$2a$10$vZ2z3ZqZfZ3Z3Z3Z3Z3Z3eZ3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z", // password: user123
                "ROLE_USER"
        ));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        
        UserData userData = users.get(username);
        if (userData == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new CustomUserDetails(
                userData.id,
                userData.username,
                userData.email,
                userData.password,
                Arrays.asList(new SimpleGrantedAuthority(userData.role)),
                true,  // enabled
                true,  // accountNonExpired
                true,  // accountNonLocked
                true   // credentialsNonExpired
        );
    }

    private static class UserData {
        Long id;
        String username;
        String email;
        String password;
        String role;

        UserData(Long id, String username, String email, String password, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }
}

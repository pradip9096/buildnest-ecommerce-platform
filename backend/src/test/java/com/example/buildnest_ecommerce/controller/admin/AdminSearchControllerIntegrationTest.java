package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.service.product.ProductSearchService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;


/**
 * Integration tests for {@link AdminSearchController} (SRCH-02, #75).
 *
 * ProductSearchService is provided as a mock by TestElasticsearchConfig,
 * so no real Elasticsearch connection is required.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@Transactional
@DisplayName("AdminSearchController integration tests")
class AdminSearchControllerIntegrationTest {

    private static final String REINDEX_URL = "/api/v1/admin/search/reindex";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ProductSearchService productSearchService;

    @MockBean RateLimitUtil rateLimitUtil;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        Mockito.reset(productSearchService);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString(), anyLong())).thenReturn(true);

        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_ADMIN".equals(r.getName())).findFirst()
                .orElseGet(() -> { Role r = new Role(); r.setName("ROLE_ADMIN"); return roleRepository.save(r); });
        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName())).findFirst()
                .orElseGet(() -> { Role r = new Role(); r.setName("ROLE_USER"); return roleRepository.save(r); });

        User admin = new User();
        admin.setUsername("srchadmin_" + System.nanoTime());
        admin.setEmail("srchadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Search"); admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole)); admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        User user = new User();
        user.setUsername("srchuser_" + System.nanoTime());
        user.setEmail("srchuser_" + System.nanoTime() + "@test.com");
        user.setPassword(passwordEncoder.encode("User@1234!"));
        user.setFirstName("Search"); user.setLastName("User");
        user.setRoles(Set.of(userRole)); user.setIsActive(true);
        user = userRepository.save(user);
        userToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
    }

    @Test
    @DisplayName("TC-SRCH-02-001: POST /reindex — admin → 200, reindexAll called")
    void reindex_asAdmin_returns200() throws Exception {
        doNothing().when(productSearchService).reindexAll();

        mockMvc.perform(post(REINDEX_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(productSearchService).reindexAll();
    }

    @Test
    @DisplayName("TC-SRCH-02-002: POST /reindex — non-admin → 403")
    void reindex_asUser_returns403() throws Exception {
        mockMvc.perform(post(REINDEX_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productSearchService);
    }

    @Test
    @DisplayName("TC-SRCH-02-003: POST /reindex — unauthenticated → 401")
    void reindex_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post(REINDEX_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-SRCH-02-004: POST /reindex — reindexAll throws → 500")
    void reindex_serviceThrows_returns500() throws Exception {
        doThrow(new IllegalStateException("Re-index failed: ES unavailable"))
                .when(productSearchService).reindexAll();

        mockMvc.perform(post(REINDEX_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError());
    }
}

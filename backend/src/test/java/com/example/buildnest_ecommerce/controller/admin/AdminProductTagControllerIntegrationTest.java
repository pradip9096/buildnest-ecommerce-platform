package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.ProductTag;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.ProductTagRepository;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminProductTagController (PROD-03, #83): list, get,
 * create, update, and delete of product tags.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class, CsrfDefaultMockMvcConfig.class })
@Transactional
class AdminProductTagControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/tags";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProductTagRepository productTagRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean RateLimitUtil rateLimitUtil;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString(), anyLong())).thenReturn(true);

        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_ADMIN".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepository.save(r);
                });

        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });

        User admin = new User();
        admin.setUsername("tagadmin_" + System.nanoTime());
        admin.setEmail("tagadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Tag");
        admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole));
        admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        User regularUser = new User();
        regularUser.setUsername("taguser_" + System.nanoTime());
        regularUser.setEmail("taguser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());
    }

    // ─── LIST / GET ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-TAG-001: GET /tags — returns all tags")
    void getAllTags_returnsList() throws Exception {
        savedTag("Eco-Friendly");
        savedTag("Best Seller");

        mockMvc.perform(get(BASE_URL).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("TC-ADM-TAG-002: GET /tags/{id} — unknown id returns 404")
    void getTagById_unknownId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-TAG-003: POST /tags — creates tag and derives slug")
    void createTag_valid_returns201() throws Exception {
        String body = """
                { "name": "Eco Friendly" }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Eco Friendly")))
                .andExpect(jsonPath("$.data.slug", is("eco-friendly")));
    }

    @Test
    @DisplayName("TC-ADM-TAG-004: POST /tags — duplicate name returns 400")
    void createTag_duplicateName_returns400() throws Exception {
        ProductTag existing = savedTag("Best Seller");

        String body = """
                { "name": "%s" }
                """.formatted(existing.getName());

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-TAG-005: POST /tags — non-admin returns 403")
    void createTag_asUser_returns403() throws Exception {
        String body = """
                { "name": "New Tag" }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADM-TAG-006: POST /tags — blank name fails validation with 400")
    void createTag_blankName_returns400() throws Exception {
        String body = """
                { "name": "" }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-TAG-007: PUT /tags/{id} — updates name and slug")
    void updateTag_validRequest_returns200() throws Exception {
        ProductTag tag = savedTag("Old Name");

        String body = """
                { "name": "New Name" }
                """;

        mockMvc.perform(put(BASE_URL + "/" + tag.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("New Name")))
                .andExpect(jsonPath("$.data.slug", is("new-name")));
    }

    @Test
    @DisplayName("TC-ADM-TAG-008: PUT /tags/{id} — unknown id returns 404")
    void updateTag_unknownId_returns404() throws Exception {
        String body = """
                { "name": "New Name" }
                """;

        mockMvc.perform(put(BASE_URL + "/999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-TAG-009: DELETE /tags/{id} — deletes tag")
    void deleteTag_existing_returns200() throws Exception {
        ProductTag tag = savedTag("Temp Tag");

        mockMvc.perform(delete(BASE_URL + "/" + tag.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        org.junit.jupiter.api.Assertions.assertTrue(
                productTagRepository.findById(tag.getId()).isEmpty());
    }

    @Test
    @DisplayName("TC-ADM-TAG-010: DELETE /tags/{id} — unknown id returns 404")
    void deleteTag_unknownId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private ProductTag savedTag(String name) {
        ProductTag tag = new ProductTag();
        String unique = name + "_" + System.nanoTime();
        tag.setName(unique);
        tag.setSlug(unique.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-)|(-$)", ""));
        tag.setCreatedAt(LocalDateTime.now());
        return productTagRepository.save(tag);
    }
}

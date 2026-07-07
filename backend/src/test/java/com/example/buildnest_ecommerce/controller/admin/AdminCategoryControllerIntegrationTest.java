package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminCategoryController (ADM-02, #68): list, get, create,
 * update (including parent/hierarchy handling), and delete (including the
 * products-exist / subcategories-exist delete guards).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class, CsrfDefaultMockMvcConfig.class })
@Transactional
class AdminCategoryControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/categories";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean RateLimitUtil rateLimitUtil;

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
        admin.setUsername("categoryadmin_" + System.nanoTime());
        admin.setEmail("categoryadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Category");
        admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole));
        admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        User regularUser = new User();
        regularUser.setUsername("categoryuser_" + System.nanoTime());
        regularUser.setEmail("categoryuser_" + System.nanoTime() + "@test.com");
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
    @DisplayName("TC-ADM-CAT-001: GET /categories — returns all categories")
    void getAllCategories_returnsList() throws Exception {
        savedCategory("Tools", null);
        savedCategory("Books", null);

        mockMvc.perform(get(BASE_URL).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("TC-ADM-CAT-002: GET /categories/{id} — unknown id returns 404")
    void getCategoryById_unknownId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-CAT-003: POST /categories — creates category without a parent")
    void createCategory_noParent_returns201() throws Exception {
        String body = """
                { "name": "Electronics", "description": "Gadgets and devices" }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Electronics")));
    }

    @Test
    @DisplayName("TC-ADM-CAT-004: POST /categories — creates category with a valid parent")
    void createCategory_withParent_returns201() throws Exception {
        Category parent = savedCategory("Tools", null);

        String body = """
                { "name": "Power Tools", "description": "desc", "parentId": %d }
                """.formatted(parent.getId());

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.parentCategory.id", is(parent.getId().intValue())));
    }

    @Test
    @DisplayName("TC-ADM-CAT-005: POST /categories — non-existent parent returns 400")
    void createCategory_unknownParent_returns400() throws Exception {
        String body = """
                { "name": "Power Tools", "description": "desc", "parentId": 999999 }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-CAT-006: POST /categories — non-admin returns 403")
    void createCategory_asUser_returns403() throws Exception {
        String body = """
                { "name": "Electronics", "description": "desc" }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADM-CAT-007: POST /categories — blank name fails validation with 400")
    void createCategory_blankName_returns400() throws Exception {
        String body = """
                { "name": "", "description": "desc" }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-CAT-008: PUT /categories/{id} — updates name, description, and parent")
    void updateCategory_validRequest_returns200() throws Exception {
        Category parent = savedCategory("Tools", null);
        Category category = savedCategory("Old Name", null);

        String body = """
                { "name": "New Name", "description": "New description", "parentId": %d }
                """.formatted(parent.getId());

        mockMvc.perform(put(BASE_URL + "/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("New Name")))
                .andExpect(jsonPath("$.data.parentCategory.id", is(parent.getId().intValue())));
    }

    @Test
    @DisplayName("TC-ADM-CAT-009: PUT /categories/{id} — self-parent returns 400")
    void updateCategory_selfParent_returns400() throws Exception {
        Category category = savedCategory("Tools", null);

        String body = """
                { "name": "Tools", "description": "desc", "parentId": %d }
                """.formatted(category.getId());

        mockMvc.perform(put(BASE_URL + "/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-CAT-010: DELETE /categories/{id} — deletes empty category with no subcategories")
    void deleteCategory_empty_returns200() throws Exception {
        Category category = savedCategory("Temp Category", null);

        mockMvc.perform(delete(BASE_URL + "/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        org.junit.jupiter.api.Assertions.assertTrue(
                categoryRepository.findById(category.getId()).isEmpty());
    }

    @Test
    @DisplayName("TC-ADM-CAT-011: DELETE /categories/{id} — blocked when products reference it")
    void deleteCategory_withProducts_returns400() throws Exception {
        Category category = savedCategory("Has Products", null);
        Product product = new Product();
        product.setName("Sample Product");
        product.setDescription("Default description for integration test.");
        product.setPrice(BigDecimal.valueOf(9.99));
        product.setIsActive(true);
        product.setCategory(category);
        productRepository.save(product);

        mockMvc.perform(delete(BASE_URL + "/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        org.junit.jupiter.api.Assertions.assertTrue(
                categoryRepository.findById(category.getId()).isPresent());
    }

    @Test
    @DisplayName("TC-ADM-CAT-012: DELETE /categories/{id} — blocked when subcategories reference it as parent")
    void deleteCategory_withSubcategories_returns400() throws Exception {
        Category parent = savedCategory("Parent Category", null);
        savedCategory("Child Category", parent);

        mockMvc.perform(delete(BASE_URL + "/" + parent.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-CAT-013: DELETE /categories/{id} — unknown id returns 404")
    void deleteCategory_unknownId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Category savedCategory(String name, Category parent) {
        Category category = new Category();
        category.setName(name + "_" + System.nanoTime());
        category.setDescription("Default description for integration test.");
        category.setIsActive(true);
        category.setParentCategory(parent);
        return categoryRepository.save(category);
    }
}

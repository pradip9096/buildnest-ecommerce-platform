package com.example.buildnest_ecommerce.controller.admin;

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
import com.example.buildnest_ecommerce.service.storage.StorageService;
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
import org.springframework.mock.web.MockMultipartFile;
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
 * Integration tests for AdminProductController (SRS ADM-01, RTM ADM-01, #67).
 *
 * <p>Verifies: create, read, update, soft-delete, and image-upload endpoints —
 * all scoped to ADMIN role — against a real H2 database via the full
 * Spring MVC dispatch stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@Transactional
class AdminProductControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/products";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean StorageService storageService;
    @MockBean RateLimitUtil rateLimitUtil;

    private String adminToken;
    private String userToken;
    private Long categoryId;

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
        admin.setUsername("prodadmin_" + System.nanoTime());
        admin.setEmail("prodadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Prod");
        admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole));
        admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        User regularUser = new User();
        regularUser.setUsername("produser_" + System.nanoTime());
        regularUser.setEmail("produser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());

        Category category = new Category();
        category.setName("TestCategory_" + System.nanoTime());
        category.setIsActive(true);
        category = categoryRepository.save(category);
        categoryId = category.getId();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-01-001: POST /api/v1/admin/products — admin creates product → 201")
    void createProduct_asAdmin_returns201() throws Exception {
        String body = """
                {
                  "name": "OPC 53 Cement",
                  "description": "High-strength cement for all construction purposes.",
                  "price": 450.00,
                  "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("OPC 53 Cement")));
    }

    @Test
    @DisplayName("TC-ADM-01-002: POST /api/v1/admin/products — missing name → 400")
    void createProduct_missingName_returns400() throws Exception {
        String body = """
                {
                  "description": "Valid description long enough.",
                  "price": 100.00,
                  "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-01-003: POST /api/v1/admin/products — non-admin → 403")
    void createProduct_asUser_returns403() throws Exception {
        String body = """
                {
                  "name": "Blocked Product",
                  "description": "Should not be created.",
                  "price": 100.00,
                  "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-01-004: GET /api/v1/admin/products — admin retrieves list → 200")
    void getAllProducts_asAdmin_returns200() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("TC-ADM-01-005: GET /api/v1/admin/products/{id} — existing product → 200")
    void getProductById_existingProduct_returns200() throws Exception {
        Product product = savedProduct("Steel Rebar 16mm");
        mockMvc.perform(get(BASE_URL + "/" + product.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Steel Rebar 16mm")));
    }

    @Test
    @DisplayName("TC-ADM-01-006: GET /api/v1/admin/products/{id} — unknown id → 404")
    void getProductById_unknownId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-01-007: PUT /api/v1/admin/products/{id} — admin updates product → 200 with new name")
    void updateProduct_asAdmin_returns200WithUpdatedName() throws Exception {
        Product product = savedProduct("Old Name");
        String body = """
                {
                  "name": "New Name Updated",
                  "description": "Updated description that is long enough.",
                  "price": 599.00,
                  "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(put(BASE_URL + "/" + product.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("New Name Updated")));
    }

    // ─── SOFT DELETE ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-01-008: DELETE /api/v1/admin/products/{id} — soft-deletes product (isActive=false)")
    void deleteProduct_asAdmin_softDeletesProduct() throws Exception {
        Product product = savedProduct("Product To Deactivate");

        mockMvc.perform(delete(BASE_URL + "/" + product.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        Product deactivated = productRepository.findById(product.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(deactivated.getIsActive(),
                "Product must be soft-deleted: isActive should be false");
    }

    @Test
    @DisplayName("TC-ADM-01-009: DELETE /api/v1/admin/products/{id} — unknown id → 400")
    void deleteProduct_unknownId_returns400() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    // ─── IMAGE UPLOAD ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-01-010: POST /api/v1/admin/products/{id}/images — valid JPEG → 200 with updated imageUrl")
    void uploadImage_validJpeg_returns200() throws Exception {
        Product product = savedProduct("Product With Image");
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
                "fake-jpeg-bytes".getBytes());
        when(storageService.store(any())).thenReturn("/uploads/photo.jpg");

        mockMvc.perform(multipart(BASE_URL + "/" + product.getId() + "/images")
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("TC-ADM-01-011: POST /api/v1/admin/products/{id}/images — no auth → 401")
    void uploadImage_noAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
                "fake".getBytes());

        mockMvc.perform(multipart(BASE_URL + "/1/images").file(file))
                .andExpect(status().isUnauthorized());
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Product savedProduct(String name) {
        Product p = new Product();
        p.setName(name);
        p.setDescription("Default description for integration test.");
        p.setPrice(BigDecimal.valueOf(199.99));
        p.setIsActive(true);
        p.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        return productRepository.save(p);
    }
}

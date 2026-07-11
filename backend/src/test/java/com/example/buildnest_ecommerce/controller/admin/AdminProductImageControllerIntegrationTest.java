package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductImage;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.ProductImageRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
 * Integration tests for the image gallery endpoints nested under AdminProductController
 * (PROD-02, #82): list, reorder, and delete. Upload is covered separately in
 * AdminProductControllerIntegrationTest (pre-existing endpoint, behavior changed by #82).
 * Mirrors AdminProductVariantControllerIntegrationTest's scaffolding from #81.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class, CsrfDefaultMockMvcConfig.class })
@Transactional
class AdminProductImageControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/products";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductImageRepository productImageRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean StorageService storageService;
    @MockitoBean RateLimitUtil rateLimitUtil;

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
        admin.setUsername("imageadmin_" + System.nanoTime());
        admin.setEmail("imageadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Image");
        admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole));
        admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        User regularUser = new User();
        regularUser.setUsername("imageuser_" + System.nanoTime());
        regularUser.setEmail("imageuser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());

        Category category = new Category();
        category.setName("ImageTestCategory_" + System.nanoTime());
        category.setIsActive(true);
        category = categoryRepository.save(category);
        categoryId = category.getId();
    }

    // ─── LIST ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-IMG-001: GET /products/{id}/images — returns images ordered by displayOrder")
    void getImages_existingProduct_returnsOrderedList() throws Exception {
        Product product = savedProduct("Product With Images");
        savedImage(product, "/uploads/a.jpg", 1, false);
        savedImage(product, "/uploads/b.jpg", 0, true);

        mockMvc.perform(get(BASE_URL + "/" + product.getId() + "/images")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].imageUrl", is("/uploads/b.jpg")));
    }

    // ─── REORDER ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-IMG-002: PATCH /products/{id}/images/reorder — reassigns displayOrder")
    void reorderImages_validIdList_returns200WithNewOrder() throws Exception {
        Product product = savedProduct("Product To Reorder");
        ProductImage first = savedImage(product, "/uploads/a.jpg", 0, true);
        ProductImage second = savedImage(product, "/uploads/b.jpg", 1, false);

        String body = """
                { "imageIds": [%d, %d] }
                """.formatted(second.getId(), first.getId());

        mockMvc.perform(patch(BASE_URL + "/" + product.getId() + "/images/reorder")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id", is(second.getId().intValue())))
                .andExpect(jsonPath("$.data[0].displayOrder", is(0)))
                .andExpect(jsonPath("$.data[1].id", is(first.getId().intValue())))
                .andExpect(jsonPath("$.data[1].displayOrder", is(1)));
    }

    @Test
    @DisplayName("TC-ADM-IMG-003: PATCH /products/{id}/images/reorder — mismatched ID set → 400")
    void reorderImages_mismatchedIds_returns400() throws Exception {
        Product product = savedProduct("Product Bad Reorder");
        savedImage(product, "/uploads/a.jpg", 0, true);

        String body = """
                { "imageIds": [999999] }
                """;

        mockMvc.perform(patch(BASE_URL + "/" + product.getId() + "/images/reorder")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-IMG-004: PATCH /products/{id}/images/reorder — non-admin → 403")
    void reorderImages_asUser_returns403() throws Exception {
        Product product = savedProduct("Product Reorder Blocked");
        ProductImage img = savedImage(product, "/uploads/a.jpg", 0, true);

        String body = "{ \"imageIds\": [%d] }".formatted(img.getId());

        mockMvc.perform(patch(BASE_URL + "/" + product.getId() + "/images/reorder")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-IMG-005: DELETE /products/{id}/images/{imageId} — non-primary image removed")
    void deleteImage_nonPrimary_returns200() throws Exception {
        Product product = savedProduct("Product Delete Image");
        savedImage(product, "/uploads/primary.jpg", 0, true);
        ProductImage secondary = savedImage(product, "/uploads/secondary.jpg", 1, false);

        mockMvc.perform(delete(BASE_URL + "/" + product.getId() + "/images/" + secondary.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        org.junit.jupiter.api.Assertions.assertTrue(
                productImageRepository.findById(secondary.getId()).isEmpty());
    }

    @Test
    @DisplayName("TC-ADM-IMG-006: DELETE /products/{id}/images/{imageId} — deleting primary promotes next image")
    void deleteImage_primary_promotesNextAndSyncsProductImageUrl() throws Exception {
        Product product = savedProduct("Product Delete Primary");
        ProductImage primary = savedImage(product, "/uploads/primary.jpg", 0, true);
        savedImage(product, "/uploads/secondary.jpg", 1, false);

        mockMvc.perform(delete(BASE_URL + "/" + product.getId() + "/images/" + primary.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("/uploads/secondary.jpg", updated.getImageUrl());
    }

    @Test
    @DisplayName("TC-ADM-IMG-007: DELETE /products/{id}/images/{imageId} — unknown id → 400")
    void deleteImage_unknownId_returns400() throws Exception {
        Product product = savedProduct("Product No Images");

        mockMvc.perform(delete(BASE_URL + "/" + product.getId() + "/images/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
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

    private ProductImage savedImage(Product product, String url, int displayOrder, boolean isPrimary) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(url);
        image.setDisplayOrder(displayOrder);
        image.setIsPrimary(isPrimary);
        image.setCreatedAt(LocalDateTime.now());
        return productImageRepository.save(image);
    }
}

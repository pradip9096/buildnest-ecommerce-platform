package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig;
import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Category;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ProductVariant;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.CategoryRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ProductVariantRepository;
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
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the variant CRUD endpoints nested under AdminProductController
 * (PROD-01, #81). Mirrors AdminProductControllerIntegrationTest's scaffolding.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestElasticsearchConfig.class, TestSecurityConfig.class, CsrfDefaultMockMvcConfig.class })
@Transactional
class AdminProductVariantControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/products";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductVariantRepository productVariantRepository;
    @Autowired InventoryRepository inventoryRepository;
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
        admin.setUsername("variantadmin_" + System.nanoTime());
        admin.setEmail("variantadmin_" + System.nanoTime() + "@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234!"));
        admin.setFirstName("Variant");
        admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole));
        admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        User regularUser = new User();
        regularUser.setUsername("variantuser_" + System.nanoTime());
        regularUser.setEmail("variantuser_" + System.nanoTime() + "@test.com");
        regularUser.setPassword(passwordEncoder.encode("User@1234!"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(userRole));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateTokenFromUsername(regularUser.getUsername());

        Category category = new Category();
        category.setName("VariantTestCategory_" + System.nanoTime());
        category.setIsActive(true);
        category = categoryRepository.save(category);
        categoryId = category.getId();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-VAR-001: POST /products/{id}/variants — admin creates variant → 201 with inventory")
    void createVariant_asAdmin_returns201() throws Exception {
        Product product = savedProduct("Premium Cement 50kg");
        String body = """
                {
                  "sku": "CEM-50KG-RED",
                  "size": "50kg",
                  "colour": "Red",
                  "priceAdjustment": 10.00,
                  "isActive": true,
                  "initialStockQuantity": 50,
                  "minimumStockLevel": 5
                }
                """;

        mockMvc.perform(post(BASE_URL + "/" + product.getId() + "/variants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.sku", is("CEM-50KG-RED")))
                .andExpect(jsonPath("$.data.colour", is("Red")));

        ProductVariant saved = productVariantRepository.findByProductId(product.getId()).get(0);
        Inventory inventory = inventoryRepository.findByVariantId(saved.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(50, inventory.getQuantityInStock());
    }

    @Test
    @DisplayName("TC-ADM-VAR-002: POST /products/{id}/variants — duplicate SKU → 400")
    void createVariant_duplicateSku_returns400() throws Exception {
        Product product = savedProduct("Product A");
        savedVariant(product, "DUP-SKU");

        String body = """
                {
                  "sku": "DUP-SKU",
                  "priceAdjustment": 0.00,
                  "initialStockQuantity": 10,
                  "minimumStockLevel": 2
                }
                """;

        mockMvc.perform(post(BASE_URL + "/" + product.getId() + "/variants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ADM-VAR-003: POST /products/{id}/variants — non-admin → 403")
    void createVariant_asUser_returns403() throws Exception {
        Product product = savedProduct("Product B");
        String body = """
                {
                  "sku": "BLOCKED-SKU",
                  "priceAdjustment": 0.00,
                  "initialStockQuantity": 10,
                  "minimumStockLevel": 2
                }
                """;

        mockMvc.perform(post(BASE_URL + "/" + product.getId() + "/variants")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ADM-VAR-004: POST /products/{id}/variants — unknown product → 400")
    void createVariant_unknownProduct_returns400() throws Exception {
        String body = """
                {
                  "sku": "ORPHAN-SKU",
                  "priceAdjustment": 0.00,
                  "initialStockQuantity": 10,
                  "minimumStockLevel": 2
                }
                """;

        mockMvc.perform(post(BASE_URL + "/999999/variants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-VAR-005: GET /products/{id}/variants — returns variant list")
    void getVariants_existingProduct_returns200() throws Exception {
        Product product = savedProduct("Product With Variants");
        savedVariant(product, "VAR-001");
        savedVariant(product, "VAR-002");

        mockMvc.perform(get(BASE_URL + "/" + product.getId() + "/variants")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-VAR-006: PUT /products/{id}/variants/{variantId} — updates colour")
    void updateVariant_asAdmin_returns200WithUpdatedColour() throws Exception {
        Product product = savedProduct("Product To Update");
        ProductVariant variant = savedVariant(product, "UPD-SKU");

        String body = """
                {
                  "sku": "UPD-SKU",
                  "size": "50kg",
                  "colour": "Maroon",
                  "priceAdjustment": 20.00,
                  "isActive": true
                }
                """;

        mockMvc.perform(put(BASE_URL + "/" + product.getId() + "/variants/" + variant.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.colour", is("Maroon")));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-ADM-VAR-007: DELETE /products/{id}/variants/{variantId} — soft-deletes variant (isActive=false)")
    void deleteVariant_asAdmin_softDeletesVariant() throws Exception {
        Product product = savedProduct("Product To Deactivate Variant");
        ProductVariant variant = savedVariant(product, "DEL-SKU");

        mockMvc.perform(delete(BASE_URL + "/" + product.getId() + "/variants/" + variant.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        ProductVariant deactivated = productVariantRepository.findById(variant.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(deactivated.getIsActive(),
                "Variant must be soft-deleted: isActive should be false");
    }

    @Test
    @DisplayName("TC-ADM-VAR-008: DELETE /products/{id}/variants/{variantId} — unknown id → 400")
    void deleteVariant_unknownId_returns400() throws Exception {
        Product product = savedProduct("Product Owning Nothing");

        mockMvc.perform(delete(BASE_URL + "/" + product.getId() + "/variants/999999")
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

    private ProductVariant savedVariant(Product product, String sku) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(sku);
        variant.setSize("50kg");
        variant.setColour("Grey");
        variant.setPriceAdjustment(BigDecimal.ZERO);
        variant.setIsActive(true);
        return productVariantRepository.save(variant);
    }
}

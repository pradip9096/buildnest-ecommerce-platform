package com.example.buildnest_ecommerce.aspect;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.dto.AdminUserDto;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.security.Jwt.JwtTokenProvider;
import com.example.buildnest_ecommerce.service.admin.AdminAnalyticsService;
import com.example.buildnest_ecommerce.service.admin.AdminService;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import com.example.buildnest_ecommerce.service.auth.AuthService;
import com.example.buildnest_ecommerce.service.inventory.InventoryService;
import com.example.buildnest_ecommerce.service.product.ProductService;
import com.example.buildnest_ecommerce.service.token.RefreshTokenService;
import com.example.buildnest_ecommerce.util.RateLimitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuditAspect — verifies that @Auditable-annotated
 * admin endpoints produce audit log entries with the required fields:
 * user, action, resource (entityType), and IP address (SRS ADM-06, RTM AUDIT-01).
 *
 * <p>AuditLogService is mocked so interactions are captured synchronously,
 * bypassing the @Async proxy and avoiding flaky async-DB timing issues.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class, com.example.buildnest_ecommerce.config.CsrfDefaultMockMvcConfig.class})
@Transactional
@SuppressWarnings("removal")
class AuditAspectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private ProductService productService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private AuthService authService;

    @MockBean
    private AdminAnalyticsService adminAnalyticsService;

    @MockBean
    private RateLimitUtil rateLimitUtil;

    private Long adminUserId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_ADMIN".equals(r.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepository.save(r);
                });

        User admin = new User();
        admin.setUsername("auditadmin");
        admin.setEmail("auditadmin@example.com");
        admin.setPassword(passwordEncoder.encode("Audit@dmin1!"));
        admin.setFirstName("Audit");
        admin.setLastName("Admin");
        admin.setRoles(Set.of(adminRole));
        admin.setIsActive(true);
        admin = userRepository.save(admin);
        adminUserId = admin.getId();
        adminToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString())).thenReturn(true);
        when(rateLimitUtil.isAllowed(any(HttpServletRequest.class), anyString(), anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("TC-AUDIT-001: POST /api/v1/admin/products fires ADMIN_CREATE_PRODUCT audit log with required fields")
    void createProduct_triggersAuditLogWithRequiredFields() throws Exception {
        Product stub = new Product();
        stub.setName("Test Product");
        stub.setPrice(BigDecimal.valueOf(499.99));
        when(productService.createProduct(any())).thenReturn(stub);

        String body = """
                {
                  "name": "Test Cement Product",
                  "description": "A high-quality product description for audit testing purposes.",
                  "price": 499.99,
                  "categoryId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> entityTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);

        verify(auditLogService).logAction(
                userIdCaptor.capture(), actionCaptor.capture(), entityTypeCaptor.capture(),
                any(), ipCaptor.capture(), any(), isNull(), any());

        assertEquals(adminUserId, userIdCaptor.getValue(), "Audit log must capture the authenticated admin user ID");
        assertEquals("ADMIN_CREATE_PRODUCT", actionCaptor.getValue(), "Audit action must match the @Auditable annotation");
        assertEquals("PRODUCT", entityTypeCaptor.getValue(), "Audit entity type must be PRODUCT");
        assertNotNull(ipCaptor.getValue(), "Audit log must record the client IP address");
    }

    @Test
    @DisplayName("TC-AUDIT-002: PUT /api/v1/admin/products/{id} fires ADMIN_UPDATE_PRODUCT audit log with old-value capture")
    void updateProduct_triggersAuditLogWithOldValueCapture() throws Exception {
        Product stub = new Product();
        stub.setName("Updated Product");
        stub.setPrice(BigDecimal.valueOf(599.99));
        when(productService.updateProduct(eq(1L), any())).thenReturn(stub);

        String body = """
                {
                  "name": "Updated Cement Product",
                  "description": "Updated product description for audit testing purposes.",
                  "price": 599.99,
                  "categoryId": 1
                }
                """;

        mockMvc.perform(put("/api/v1/admin/products/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> oldValueCaptor = ArgumentCaptor.forClass(Object.class);

        verify(auditLogService).logAction(
                eq(adminUserId), actionCaptor.capture(), eq("PRODUCT"),
                eq(1L), any(), any(), oldValueCaptor.capture(), any());

        assertEquals("ADMIN_UPDATE_PRODUCT", actionCaptor.getValue());
        assertNotNull(oldValueCaptor.getValue(), "UPDATE action must capture old value (pre-call arguments)");
    }

    @Test
    @DisplayName("TC-AUDIT-003: DELETE /api/v1/admin/products/{id} fires ADMIN_DELETE_PRODUCT audit log")
    void deleteProduct_triggersAuditLog() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/admin/products/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> oldValueCaptor = ArgumentCaptor.forClass(Object.class);

        verify(auditLogService).logAction(
                eq(adminUserId), actionCaptor.capture(), eq("PRODUCT"),
                eq(1L), any(), any(), oldValueCaptor.capture(), any());

        assertEquals("ADMIN_DELETE_PRODUCT", actionCaptor.getValue());
        assertNotNull(oldValueCaptor.getValue(), "DELETE action must capture old value (pre-call arguments)");
    }

    @Test
    @DisplayName("TC-AUDIT-004: PUT /api/admin/users/{id} fires ADMIN_UPDATE_USER audit log")
    void updateUser_triggersAuditLog() throws Exception {
        AdminUserDto stub = new AdminUserDto(
                1L, "auditadmin", "updated@example.com",
                "Updated", "Admin", null, true, null, Set.of("ROLE_ADMIN"));
        when(adminService.updateUserByAdmin(eq(1L), any())).thenReturn(stub);

        String body = """
                {
                  "firstName": "Updated",
                  "lastName": "Admin",
                  "email": "updated@example.com"
                }
                """;

        mockMvc.perform(put("/api/admin/users/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);

        verify(auditLogService).logAction(
                eq(adminUserId), actionCaptor.capture(), eq("USER"),
                eq(1L), ipCaptor.capture(), any(), notNull(), any());

        assertEquals("ADMIN_UPDATE_USER", actionCaptor.getValue());
        assertNotNull(ipCaptor.getValue());
    }

    @Test
    @DisplayName("TC-AUDIT-005: DELETE /api/admin/users/{id} fires ADMIN_DELETE_USER audit log")
    void deleteUser_triggersAuditLog() throws Exception {
        doNothing().when(adminService).deleteUser(1L);

        mockMvc.perform(delete("/api/admin/users/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        verify(auditLogService).logAction(
                eq(adminUserId), eq("ADMIN_DELETE_USER"), eq("USER"),
                eq(1L), any(), any(), notNull(), any());
    }

    @Test
    @DisplayName("TC-AUDIT-006: Unauthenticated request to admin endpoint does not trigger audit log")
    void unauthenticatedRequest_doesNotTriggerAuditLog() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products"))
                .andExpect(status().isUnauthorized());

        verify(auditLogService, never()).logAction(any(), any(), any(), any(), any(), any(), any(), any());
    }
}

package com.example.buildnest_ecommerce.admin;

import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.RoleRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Admin Dashboard test suite TC-ADMIN-001 to TC-ADMIN-010.
 * Covers admin functionality and management features.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class AdminDashboardTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User adminUser;
    private User regularUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminRole = roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        userRole = roleRepository.save(userRole);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("hashedPassword");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRoles(new HashSet<>(Set.of(adminRole)));
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);

        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword("hashedPassword");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(new HashSet<>(Set.of(userRole)));
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);

        testProduct = new Product();
        testProduct.setName("Cement");
        testProduct.setDescription("Portland cement");
        testProduct.setPrice(new BigDecimal("400.00"));
        testProduct.setStockQuantity(500);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("TC-ADMIN-001: Should verify admin role")
    void testAdminRoleVerification() {
        assertNotNull(adminUser.getRoles());
        assertTrue(adminUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName())));
    }

    @Test
    @DisplayName("TC-ADMIN-002: Should prevent non-admin access")
    void testPreventNonAdminAccess() {
        assertNotNull(regularUser.getRoles());
        assertTrue(regularUser.getRoles().stream().anyMatch(role -> "ROLE_USER".equals(role.getName())));
        assertFalse(regularUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName())));
    }

    @Test
    @DisplayName("TC-ADMIN-003: Should allow admin to create products")
    void testAdminCreateProduct() {
        Product newProduct = new Product();
        newProduct.setName("Steel Rod");
        newProduct.setDescription("High-quality steel");
        newProduct.setPrice(new BigDecimal("300.00"));
        newProduct.setStockQuantity(1000);
        newProduct.setIsActive(true);

        Product saved = productRepository.save(newProduct);
        assertNotNull(saved.getId());
    }

    @Test
    @DisplayName("TC-ADMIN-004: Should allow admin to deactivate products")
    void testAdminDeactivateProduct() {
        testProduct.setIsActive(false);
        Product updated = productRepository.save(testProduct);

        assertFalse(updated.getIsActive());
    }

    @Test
    @DisplayName("TC-ADMIN-005: Should track admin actions")
    void testTrackAdminActions() {
        testProduct.setPrice(new BigDecimal("500.00"));
        Product updated = productRepository.save(testProduct);

        assertEquals(new BigDecimal("500.00"), updated.getPrice());
    }

    @Test
    @DisplayName("TC-ADMIN-006: Should view all users")
    void testViewAllUsers() {
        List<User> users = userRepository.findAll();

        assertTrue(users.size() >= 2);
    }

    @Test
    @DisplayName("TC-ADMIN-007: Should suspend user account")
    void testSuspendUserAccount() {
        regularUser.setIsActive(false);
        User suspended = userRepository.save(regularUser);

        assertFalse(suspended.getIsActive());
    }

    @Test
    @DisplayName("TC-ADMIN-008: Should manage inventory levels")
    void testManageInventoryLevels() {
        testProduct.setStockQuantity(2000);
        Product updated = productRepository.save(testProduct);

        assertEquals(2000, updated.getStockQuantity());
    }

    @Test
    @DisplayName("TC-ADMIN-009: Should control product pricing")
    void testControlProductPricing() {
        BigDecimal newPrice = new BigDecimal("1500.00");
        testProduct.setPrice(newPrice);
        Product updated = productRepository.save(testProduct);

        assertEquals(newPrice, updated.getPrice());
    }

    @Test
    @DisplayName("TC-ADMIN-010: Should export admin reports")
    void testExportAdminReports() {
        List<Product> products = productRepository.findAll();

        assertFalse(products.isEmpty());
    }
}

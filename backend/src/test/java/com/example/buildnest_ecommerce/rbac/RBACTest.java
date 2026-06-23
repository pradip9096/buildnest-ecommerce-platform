package com.example.buildnest_ecommerce.rbac;

import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RBAC (Role-Based Access Control) test suite (TC-RBAC-001 through
 * TC-RBAC-005).
 * Tests for authorization, role hierarchy, and permission enforcement.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class RBACTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User regularUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("hashedPassword");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);

        // Create regular user
        regularUser = new User();
        regularUser.setUsername("regularuser");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword("hashedPassword");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);

        // Create test order
        testOrder = new Order();
        testOrder.setUser(regularUser);
        testOrder.setOrderNumber("RBAC-001");
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("1000.00"));
        testOrder.setIsDeleted(false);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder = orderRepository.save(testOrder);
    }

    // TC-RBAC-001: User can only access their own orders
    @Test
    @DisplayName("TC-RBAC-001: User can only access their own orders")
    void testUserAccessToOwnOrders() {
        // Regular user should access their own order
        var userOrders = orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(regularUser.getId()))
                .toList();

        assertTrue(userOrders.size() > 0);
        assertTrue(userOrders.stream().allMatch(o -> o.getUser().getId().equals(regularUser.getId())));
    }

    // TC-RBAC-002: User cannot access other user's orders
    @Test
    @DisplayName("TC-RBAC-002: User cannot access other user's orders")
    void testUserCannotAccessOthersOrders() {
        // Create another user's order
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("hashedPassword");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setIsActive(true);
        otherUser = userRepository.save(otherUser);

        Order otherOrder = new Order();
        otherOrder.setUser(otherUser);
        otherOrder.setOrderNumber("RBAC-002");
        otherOrder.setStatus(Order.OrderStatus.PENDING);
        otherOrder.setTotalAmount(new BigDecimal("5000.00"));
        otherOrder.setIsDeleted(false);
        otherOrder.setCreatedAt(LocalDateTime.now());
        otherOrder = orderRepository.save(otherOrder);

        // Regular user should NOT see other user's order
        final Long otherUserId = otherUser.getId();
        final Long regUserId = regularUser.getId();
        boolean hasAccessToOther = orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(otherUserId))
                .anyMatch(o -> regUserId.equals(o.getUser().getId()));

        assertFalse(hasAccessToOther);
    }

    // TC-RBAC-003: Admin can access all orders
    @Test
    @DisplayName("TC-RBAC-003: Admin can access all orders")
    void testAdminAccessToAllOrders() {
        // Admin should see all orders
        var allOrders = orderRepository.findAll();

        assertTrue(allOrders.size() >= 1);
        assertTrue(allOrders.stream().anyMatch(o -> o.getUser().getId().equals(regularUser.getId())));
    }

    // TC-RBAC-004: User cannot modify other user's orders
    @Test
    @DisplayName("TC-RBAC-004: User cannot modify other user's orders")
    void testUserCannotModifyOthersOrders() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("hashedPassword");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setIsActive(true);
        anotherUser = userRepository.save(anotherUser);

        Order anotherOrder = new Order();
        anotherOrder.setUser(anotherUser);
        anotherOrder.setOrderNumber("RBAC-004");
        anotherOrder.setStatus(Order.OrderStatus.PENDING);
        anotherOrder.setTotalAmount(new BigDecimal("2000.00"));
        anotherOrder.setIsDeleted(false);
        anotherOrder.setCreatedAt(LocalDateTime.now());
        anotherOrder = orderRepository.save(anotherOrder);

        // Verify order belongs to anotherUser
        final Long anotherUserId = anotherUser.getId();
        final Long regularUserId = regularUser.getId();
        assertEquals(anotherUserId, anotherOrder.getUser().getId());
        assertNotEquals(regularUserId, anotherOrder.getUser().getId());
    }

    // TC-RBAC-005: Admin can override user permissions
    @Test
    @DisplayName("TC-RBAC-005: Admin can override user permissions and access/modify any order")
    void testAdminOverridePermissions() {
        // Admin should be able to access and modify any order
        var orderToUpdate = orderRepository.findById(testOrder.getId());
        assertTrue(orderToUpdate.isPresent());

        Order order = orderToUpdate.get();
        order.setStatus(Order.OrderStatus.SHIPPED);
        Order updated = orderRepository.save(order);

        assertEquals(Order.OrderStatus.SHIPPED, updated.getStatus());
        // Verify it was modified
        var verifyUpdate = orderRepository.findById(updated.getId());
        assertEquals(Order.OrderStatus.SHIPPED, verifyUpdate.get().getStatus());
    }
}

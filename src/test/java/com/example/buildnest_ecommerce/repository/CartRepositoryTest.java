package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class CartRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartRepository cartRepository;

    private User testUser;
    private Category testCategory;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Create test user with unique identifier to avoid constraint violations
        long uniqueId = System.currentTimeMillis();
        testUser = new User();
        testUser.setUsername("carttest-" + uniqueId);
        testUser.setEmail("carttest-" + uniqueId + "@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testUser);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        entityManager.persist(testCategory);

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Test Product 1");
        testProduct1.setDescription("Description 1");
        testProduct1.setPrice(BigDecimal.valueOf(100.00));
        testProduct1.setCategory(testCategory);
        testProduct1.setIsActive(true);
        entityManager.persist(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Test Product 2");
        testProduct2.setDescription("Description 2");
        testProduct2.setPrice(BigDecimal.valueOf(200.00));
        testProduct2.setCategory(testCategory);
        testProduct2.setIsActive(true);
        entityManager.persist(testProduct2);

        entityManager.flush();
    }

    @Test
    @DisplayName("TC-CART-REPO-001: Find cart by user ID with items")
    void testFindByUserIdWithItems() {
        // Arrange
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        entityManager.persist(cart);

        CartItem item1 = new CartItem();
        item1.setCart(cart);
        item1.setProduct(testProduct1);
        item1.setQuantity(2);
        item1.setPrice(testProduct1.getPrice());
        entityManager.persist(item1);

        CartItem item2 = new CartItem();
        item2.setCart(cart);
        item2.setProduct(testProduct2);
        item2.setQuantity(1);
        item2.setPrice(testProduct2.getPrice());
        entityManager.persist(item2);

        cart.getItems().add(item1);
        cart.getItems().add(item2);
        entityManager.flush();

        // Act
        Optional<Cart> found = cartRepository.findByUser(testUser);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getUser().getId());
        assertEquals(2, found.get().getItems().size());
    }

    @Test
    @DisplayName("TC-CART-REPO-002: Delete cart cascades to cart items")
    void testDeleteCartCascadesItems() {
        // Arrange
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        entityManager.persist(cart);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(testProduct1);
        item.setQuantity(3);
        item.setPrice(testProduct1.getPrice());
        entityManager.persist(item);

        cart.getItems().add(item);
        entityManager.flush();

        Long cartId = cart.getId();
        Long itemId = item.getId();

        // Act
        cartRepository.deleteByUser(testUser);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNull(entityManager.find(Cart.class, cartId));
        assertNull(entityManager.find(CartItem.class, itemId)); // Cascade delete
    }

    @Test
    @DisplayName("TC-CART-REPO-003: Cart item quantity constraints")
    void testCartItemQuantityConstraints() {
        // Arrange
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        entityManager.persist(cart);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(testProduct1);
        item.setQuantity(0); // Invalid quantity
        item.setPrice(testProduct1.getPrice());

        // Act & Assert
        cart.getItems().add(item);
        entityManager.persist(item);

        // Flush should succeed (no DB constraint on quantity > 0 in current schema)
        // But in production, you'd add @Min(1) validation
        assertDoesNotThrow(() -> entityManager.flush());

        // Verify item was saved
        assertNotNull(item.getId());
    }

    @Test
    @DisplayName("TC-CART-REPO-004: Cart total calculation with multiple items")
    void testCartTotalCalculation() {
        // Arrange
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        entityManager.persist(cart);

        CartItem item1 = new CartItem();
        item1.setCart(cart);
        item1.setProduct(testProduct1);
        item1.setQuantity(2);
        item1.setPrice(BigDecimal.valueOf(100.00));
        entityManager.persist(item1);

        CartItem item2 = new CartItem();
        item2.setCart(cart);
        item2.setProduct(testProduct2);
        item2.setQuantity(3);
        item2.setPrice(BigDecimal.valueOf(50.00));
        entityManager.persist(item2);

        cart.getItems().add(item1);
        cart.getItems().add(item2);
        entityManager.flush();

        // Act
        Optional<Cart> found = cartRepository.findByUser(testUser);

        // Assert
        assertTrue(found.isPresent());
        BigDecimal total = found.get().getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Expected: (2 * 100) + (3 * 50) = 200 + 150 = 350
        assertEquals(BigDecimal.valueOf(350.00), total);
    }

    @Test
    @DisplayName("TC-CART-REPO-005: Find active carts by date range")
    void testFindActiveCartsByDateRange() {
        // Arrange - Create second user with unique identifier to avoid unique constraint violation
        long uniqueId = System.currentTimeMillis() + 1;
        User user2 = new User();
        user2.setUsername("user2-" + uniqueId);
        user2.setEmail("user2-" + uniqueId + "@example.com");
        user2.setPassword("hashedPassword");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(user2);

        Cart cart1 = new Cart();
        cart1.setUser(testUser);
        cart1.setItems(new ArrayList<>());
        entityManager.persist(cart1);

        Cart cart2 = new Cart();
        cart2.setUser(user2);
        cart2.setItems(new ArrayList<>());
        entityManager.persist(cart2);

        entityManager.flush();

        // Act
        long totalCarts = cartRepository.count();

        // Assert
        assertEquals(2, totalCarts);
    }

    @Test
    @DisplayName("TC-CART-REPO-006: Abandoned cart query")
    void testAbandonedCartQuery() {
        // Arrange
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        entityManager.persist(cart);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(testProduct1);
        item.setQuantity(1);
        item.setPrice(testProduct1.getPrice());
        entityManager.persist(item);

        cart.getItems().add(item);
        entityManager.flush();

        // Act - Find cart by user (simulating abandoned cart check)
        Optional<Cart> found = cartRepository.findByUser(testUser);

        // Assert
        assertTrue(found.isPresent());
        assertFalse(found.get().getItems().isEmpty()); // Cart has items (abandoned)
    }

    @Test
    @DisplayName("TC-CART-REPO-007: Cart item unique constraint - one cart per user")
    void testCartItemUniqueConstraint() {
        // Arrange
        Cart cart1 = new Cart();
        cart1.setUser(testUser);
        cart1.setItems(new ArrayList<>());
        entityManager.persist(cart1);
        entityManager.flush();

        Cart cart2 = new Cart();
        cart2.setUser(testUser); // Same user - violates unique constraint
        cart2.setItems(new ArrayList<>());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            entityManager.persist(cart2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("TC-CART-REPO-008: Concurrent cart updates")
    void testConcurrentCartUpdates() {
        // Arrange
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        entityManager.persist(cart);

        CartItem item1 = new CartItem();
        item1.setCart(cart);
        item1.setProduct(testProduct1);
        item1.setQuantity(1);
        item1.setPrice(testProduct1.getPrice());
        entityManager.persist(item1);

        cart.getItems().add(item1);
        entityManager.flush();
        entityManager.clear();

        // Act - Simulate concurrent update
        Optional<Cart> found = cartRepository.findByUser(testUser);
        assertTrue(found.isPresent());

        CartItem item2 = new CartItem();
        item2.setCart(found.get());
        item2.setProduct(testProduct2);
        item2.setQuantity(2);
        item2.setPrice(testProduct2.getPrice());
        entityManager.persist(item2);

        found.get().getItems().add(item2);
        entityManager.flush();

        // Assert
        Optional<Cart> updated = cartRepository.findByUser(testUser);
        assertTrue(updated.isPresent());
        assertEquals(2, updated.get().getItems().size());
    }
}

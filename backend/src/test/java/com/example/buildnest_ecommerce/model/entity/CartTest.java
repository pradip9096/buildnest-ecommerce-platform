package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cart entity tests")
class CartTest {

    @Test
    @DisplayName("Should create Cart with all fields")
    void testCartConstructorAndGetters() {
        User user = new User();
        user.setId(1L);
        List<CartItem> items = new ArrayList<>();

        Cart cart = new Cart(10L, user, items);

        assertEquals(10L, cart.getId());
        assertEquals(user, cart.getUser());
        assertEquals(items, cart.getItems());
    }

    @Test
    @DisplayName("Should create Cart with no-args constructor")
    void testNoArgsConstructor() {
        Cart cart = new Cart();
        assertNotNull(cart);
        assertNull(cart.getId());
        assertNull(cart.getUser());
    }

    @Test
    @DisplayName("Should set and get Cart fields")
    void testSettersAndGetters() {
        Cart cart = new Cart();
        User user = new User();
        user.setId(5L);
        List<CartItem> items = new ArrayList<>();

        cart.setId(20L);
        cart.setUser(user);
        cart.setItems(items);

        assertEquals(20L, cart.getId());
        assertEquals(user, cart.getUser());
        assertEquals(items, cart.getItems());
    }

    @Test
    @DisplayName("Should test equals and hashCode for identical Carts")
    void testEqualsAndHashCode() {
        User user = new User();
        user.setId(1L);

        Cart cart1 = new Cart();
        cart1.setId(1L);
        cart1.setUser(user);

        Cart cart2 = new Cart();
        cart2.setId(1L);
        cart2.setUser(user);

        assertEquals(cart1, cart2);
        assertEquals(cart1.hashCode(), cart2.hashCode());
    }

    @Test
    @DisplayName("Should test equals with different Carts")
    void testEqualsDifferentCarts() {
        Cart cart1 = new Cart();
        cart1.setId(1L);

        Cart cart2 = new Cart();
        cart2.setId(2L);

        assertNotEquals(cart1, cart2);
    }

    @Test
    @DisplayName("Should test equals with null and different types")
    void testEqualsNullAndDifferentType() {
        Cart cart = new Cart();
        cart.setId(1L);

        assertNotEquals(cart, null);
        assertNotEquals(cart, "Not a Cart");
        assertEquals(cart, cart);
    }

    @Test
    @DisplayName("Should test toString excludes items")
    void testToString() {
        Cart cart = new Cart();
        cart.setId(1L);

        User user = new User();
        user.setId(5L);
        cart.setUser(user);

        String result = cart.toString();
        assertTrue(result.contains("Cart"));
        assertTrue(result.contains("id=1"));
        // Items should be excluded from toString
        assertFalse(result.contains("items"));
    }

    @Test
    @DisplayName("Should test Cart with items collection")
    void testItemsCollection() {
        Cart cart = new Cart();
        List<CartItem> items = new ArrayList<>();

        CartItem item1 = new CartItem();
        item1.setId(1L);
        items.add(item1);

        cart.setItems(items);
        assertEquals(1, cart.getItems().size());
        assertTrue(cart.getItems().contains(item1));
    }

    @Test
    @DisplayName("Should test Cart with null fields equals another with null fields")
    void testAllNullFieldsEquals() {
        Cart cart1 = new Cart();
        Cart cart2 = new Cart();

        assertEquals(cart1, cart2);
        assertEquals(cart1.hashCode(), cart2.hashCode());
    }

    @Test
    @DisplayName("Should test canEqual with subclass")
    void testCanEqualWithSubclass() {
        Cart cart1 = new Cart();
        cart1.setId(1L);

        Cart subclass = new Cart() {
        };
        subclass.setId(1L);

        assertEquals(cart1, subclass);
    }
}

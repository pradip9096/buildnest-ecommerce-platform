package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order entity tests")
class OrderTest {

    @Test
    @DisplayName("Should create Order with all fields")
    void testOrderConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);
        Address address = new Address();
        address.setId(2L);
        Set<OrderItem> items = new HashSet<>();

        Order order = new Order(
                10L,
                user,
                "ORD-123",
                Order.OrderStatus.CONFIRMED,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("15.00"),
                address,
                "TRACK-456",
                false,
                null,
                now,
                now,
                items);

        assertEquals(10L, order.getId());
        assertEquals(user, order.getUser());
        assertEquals("ORD-123", order.getOrderNumber());
        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
        assertEquals(new BigDecimal("10.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("5.00"), order.getTaxAmount());
        assertEquals(new BigDecimal("15.00"), order.getShippingAmount());
        assertEquals(address, order.getShippingAddress());
        assertEquals("TRACK-456", order.getTrackingNumber());
        assertFalse(order.getIsDeleted());
        assertNull(order.getDeletedAt());
        assertEquals(now, order.getCreatedAt());
        assertEquals(now, order.getUpdatedAt());
        assertEquals(items, order.getOrderItems());
    }

    @Test
    @DisplayName("Should create Order with no-args constructor")
    void testNoArgsConstructor() {
        Order order = new Order();
        assertNotNull(order);
        assertNull(order.getId());
        assertNull(order.getUser());
        assertNull(order.getOrderNumber());
    }

    @Test
    @DisplayName("Should set and get Order fields")
    void testSettersAndGetters() {
        Order order = new Order();
        User user = new User();
        user.setId(5L);

        order.setId(20L);
        order.setUser(user);
        order.setOrderNumber("ORD-999");
        order.setStatus(Order.OrderStatus.SHIPPED);
        order.setTotalAmount(new BigDecimal("250.00"));
        order.setDiscountAmount(new BigDecimal("25.00"));
        order.setTaxAmount(new BigDecimal("20.00"));
        order.setShippingAmount(new BigDecimal("10.00"));
        order.setTrackingNumber("TRACK-999");
        order.setIsDeleted(true);

        assertEquals(20L, order.getId());
        assertEquals(user, order.getUser());
        assertEquals("ORD-999", order.getOrderNumber());
        assertEquals(Order.OrderStatus.SHIPPED, order.getStatus());
        assertEquals(new BigDecimal("250.00"), order.getTotalAmount());
        assertEquals(new BigDecimal("25.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("20.00"), order.getTaxAmount());
        assertEquals(new BigDecimal("10.00"), order.getShippingAmount());
        assertEquals("TRACK-999", order.getTrackingNumber());
        assertTrue(order.getIsDeleted());
    }

    @Test
    @DisplayName("Should test equals and hashCode for identical Orders")
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setUser(user);
        order1.setOrderNumber("ORD-123");
        order1.setStatus(Order.OrderStatus.PENDING);
        order1.setTotalAmount(new BigDecimal("100.00"));
        order1.setCreatedAt(now);

        Order order2 = new Order();
        order2.setId(1L);
        order2.setUser(user);
        order2.setOrderNumber("ORD-123");
        order2.setStatus(Order.OrderStatus.PENDING);
        order2.setTotalAmount(new BigDecimal("100.00"));
        order2.setCreatedAt(now);

        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    @DisplayName("Should test equals with different Orders")
    void testEqualsDifferentOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setOrderNumber("ORD-123");

        Order order2 = new Order();
        order2.setId(2L);
        order2.setOrderNumber("ORD-456");

        assertNotEquals(order1, order2);
    }

    @Test
    @DisplayName("Should test equals with null and different types")
    void testEqualsNullAndDifferentType() {
        Order order = new Order();
        order.setId(1L);

        assertNotEquals(order, null);
        assertNotEquals(order, "Not an Order");
        assertEquals(order, order);
    }

    @Test
    @DisplayName("Should test equals with null fields")
    void testEqualsWithNullFields() {
        Order order1 = new Order();
        Order order2 = new Order();
        assertEquals(order1, order2);

        order1.setOrderNumber("ORD-123");
        assertNotEquals(order1, order2);

        order2.setOrderNumber("ORD-123");
        assertEquals(order1, order2);
    }

    @Test
    @DisplayName("Should test toString contains key fields")
    void testToString() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-123");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100.00"));

        String result = order.toString();
        assertTrue(result.contains("Order"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("ORD-123"));
        assertTrue(result.contains("PENDING"));
    }

    @Test
    @DisplayName("Should test all OrderStatus enum values")
    void testOrderStatusEnum() {
        assertEquals(5, Order.OrderStatus.values().length);
        assertEquals(Order.OrderStatus.PENDING, Order.OrderStatus.valueOf("PENDING"));
        assertEquals(Order.OrderStatus.CONFIRMED, Order.OrderStatus.valueOf("CONFIRMED"));
        assertEquals(Order.OrderStatus.SHIPPED, Order.OrderStatus.valueOf("SHIPPED"));
        assertEquals(Order.OrderStatus.DELIVERED, Order.OrderStatus.valueOf("DELIVERED"));
        assertEquals(Order.OrderStatus.CANCELLED, Order.OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    @DisplayName("Should handle soft delete fields")
    void testSoftDeleteFields() {
        Order order = new Order();
        assertFalse(order.getIsDeleted()); // Default is false
        assertNull(order.getDeletedAt());

        LocalDateTime deletedTime = LocalDateTime.now();
        order.setIsDeleted(true);
        order.setDeletedAt(deletedTime);

        assertTrue(order.getIsDeleted());
        assertEquals(deletedTime, order.getDeletedAt());
    }

    @Test
    @DisplayName("Should test canEqual with subclass")
    void testCanEqualWithSubclass() {
        Order order1 = new Order();
        order1.setId(1L);

        // Create a subclass instance
        Order subclass = new Order() {
        };
        subclass.setId(1L);
        // Synchronize timestamps to avoid precision mismatch
        subclass.setCreatedAt(order1.getCreatedAt());

        // Lombok's canEqual should handle this
        assertEquals(order1, subclass);
    }

    @Test
    @DisplayName("Should test Order with all null fields equals another with all null fields")
    void testAllNullFieldsEquals() {
        Order order1 = new Order();
        Order order2 = new Order();

        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    @DisplayName("Should test Order with OrderItems collection")
    void testOrderItemsCollection() {
        Order order = new Order();
        Set<OrderItem> items = new HashSet<>();

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        items.add(item1);

        order.setOrderItems(items);
        assertEquals(1, order.getOrderItems().size());
        assertTrue(order.getOrderItems().contains(item1));
    }
}

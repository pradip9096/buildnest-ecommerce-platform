package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.now();

    private static User buildUser(long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("secret");
        user.setCreatedAt(TEST_TIMESTAMP);
        return user;
    }

    private static Order buildOrder(long id) {
        Order order = new Order();
        order.setId(id);
        order.setUser(buildUser(id));
        order.setOrderNumber("ORD-" + id);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("25.00"));
        order.setCreatedAt(TEST_TIMESTAMP);
        return order;
    }

    private static Product buildProduct(long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product" + id);
        product.setPrice(new BigDecimal("10.00"));
        product.setCreatedAt(TEST_TIMESTAMP);
        return product;
    }

    @Test
    void equalsAndHashCodeCoverDifferences() {
        OrderItem base = new OrderItem(1L, buildOrder(1L), buildProduct(2L), 2, new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("20.00"));

        OrderItem same = new OrderItem(1L, buildOrder(1L), buildProduct(2L), 2, new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("20.00"));

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, null);
        assertNotEquals(base, "not-item");

        OrderItem diffQty = new OrderItem(1L, buildOrder(1L), buildProduct(2L), 3, new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("30.00"));

        OrderItem diffProduct = new OrderItem(1L, buildOrder(1L), buildProduct(3L), 2, new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("20.00"));

        assertNotEquals(base, diffQty);
        assertNotEquals(base, diffProduct);
    }

    @Test
    void settersUpdateFields() {
        OrderItem item = new OrderItem();
        item.setId(5L);
        item.setOrder(buildOrder(10L));
        item.setProduct(buildProduct(4L));
        item.setQuantity(1);
        item.setPrice(new BigDecimal("5.00"));
        item.setDiscountAmount(new BigDecimal("1.00"));
        item.setSubtotal(new BigDecimal("4.00"));

        assertEquals(5L, item.getId());
        assertEquals(1, item.getQuantity());
        assertEquals(new BigDecimal("5.00"), item.getPrice());
        assertEquals(new BigDecimal("1.00"), item.getDiscountAmount());
        assertEquals(new BigDecimal("4.00"), item.getSubtotal());
    }
}

package com.example.buildnest_ecommerce.util;

import com.example.buildnest_ecommerce.model.dto.PublicOrderDTO;
import com.example.buildnest_ecommerce.model.dto.PublicProductDTO;
import com.example.buildnest_ecommerce.model.dto.PublicUserDTO;
import com.example.buildnest_ecommerce.model.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MapperUtilTest {

    @Test
    void mapsProductToPublicDto() {
        MapperUtil util = new MapperUtil();
        Category category = new Category();
        category.setName("Cement");

        Product product = new Product();
        product.setId(1L);
        product.setName("Prod");
        product.setDescription("Desc");
        product.setPrice(BigDecimal.TEN);
        product.setDiscountPrice(BigDecimal.ONE);
        product.setSku("SKU");
        product.setImageUrl("img");
        product.setStockQuantity(10);
        product.setIsActive(true);
        product.setCategory(category);

        PublicProductDTO dto = util.toPublicProductDTO(product);
        assertEquals("Prod", dto.getName());
        assertEquals("Cement", dto.getCategoryName());
    }

    @Test
    void mapsProductNullsAndLists() {
        MapperUtil util = new MapperUtil();
        assertNull(util.toPublicProductDTO(null));
        assertNull(util.toPublicProductDTOs(null));

        Product product = new Product();
        product.setName("Name");
        List<PublicProductDTO> dtos = util.toPublicProductDTOs(List.of(product));
        assertEquals(1, dtos.size());
        assertEquals("Name", dtos.get(0).getName());
    }

    @Test
    void mapsUserToPublicDtoWithAddress() {
        MapperUtil util = new MapperUtil();

        User user = new User();
        user.setId(2L);
        user.setUsername("user");
        user.setEmail("u@example.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setPhoneNumber("123");

        Address address = new Address();
        address.setStreetAddress("Street");
        address.setCity("City");
        address.setState("ST");
        address.setPostalCode("12345");
        address.setIsDefault(true);

        user.setAddresses(new HashSet<>(Collections.singletonList(address)));

        PublicUserDTO dto = util.toPublicUserDTO(user);
        assertTrue(dto.getAddress().contains("Street"));
    }

    @Test
    void mapsUserToPublicDtoWithoutDefaultAddress() {
        MapperUtil util = new MapperUtil();

        User user = new User();
        user.setUsername("user");

        Address address = new Address();
        address.setStreetAddress("Street");
        address.setCity("City");
        address.setState("ST");
        address.setPostalCode("12345");
        address.setIsDefault(false);

        user.setAddresses(new HashSet<>(Collections.singletonList(address)));

        PublicUserDTO dto = util.toPublicUserDTO(user);
        assertNotNull(dto.getAddress());
        assertTrue(dto.getAddress().contains("Street"));
    }

    @Test
    void mapsUserNullsAndLists() {
        MapperUtil util = new MapperUtil();
        assertNull(util.toPublicUserDTO(null));
        assertNull(util.toPublicUserDTOs(null));

        User user = new User();
        user.setUsername("user");
        List<PublicUserDTO> dtos = util.toPublicUserDTOs(List.of(user));
        assertEquals(1, dtos.size());
        assertEquals("user", dtos.get(0).getUsername());
    }

    @Test
    void mapsOrderToPublicDto() {
        MapperUtil util = new MapperUtil();

        Product product = new Product();
        product.setId(10L);
        product.setName("Prod");

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(BigDecimal.TEN);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("O-1");
        order.setTotalAmount(BigDecimal.TEN);
        order.setStatus(Order.OrderStatus.CONFIRMED);

        Address address = new Address();
        address.setStreetAddress("Street");
        address.setCity("City");
        address.setState("ST");
        address.setPostalCode("12345");
        address.setCountry("US");
        order.setShippingAddress(address);

        Set<OrderItem> items = new HashSet<>();
        items.add(item);
        order.setOrderItems(items);

        PublicOrderDTO dto = util.toPublicOrderDTO(order);
        assertEquals("O-1", dto.getOrderNumber());
        assertEquals(1, dto.getItems().size());
    }

    @Test
    void mapsOrderNullsAndLists() {
        MapperUtil util = new MapperUtil();
        assertNull(util.toPublicOrderDTO(null));
        assertNull(util.toPublicOrderDTOs(null));

        Order order = new Order();
        order.setOrderNumber("O-2");
        order.setOrderItems(new HashSet<>());
        order.setShippingAddress(null);

        List<PublicOrderDTO> dtos = util.toPublicOrderDTOs(new ArrayList<>(List.of(order)));
        assertEquals(1, dtos.size());
        assertEquals("O-2", dtos.get(0).getOrderNumber());
        assertTrue(dtos.get(0).getItems().isEmpty());
    }
}

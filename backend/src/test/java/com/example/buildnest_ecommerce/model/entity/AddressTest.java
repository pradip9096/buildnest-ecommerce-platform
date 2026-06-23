package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.now();

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("secret");
        user.setCreatedAt(TEST_TIMESTAMP);
        return user;
    }

    @Test
    void equalsAndHashCodeCoverDifferences() {
        Address base = new Address(1L, user(1L), "Street", "City", "State", "12345", "Country", true,
                "SHIPPING", TEST_TIMESTAMP);

        Address same = new Address(1L, user(1L), "Street", "City", "State", "12345", "Country", true,
                "SHIPPING", TEST_TIMESTAMP);

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, null);
        assertNotEquals(base, "not-address");

        Address diffCity = new Address(1L, user(1L), "Street", "Other", "State", "12345", "Country", true,
                "SHIPPING", TEST_TIMESTAMP);

        Address diffDefault = new Address(1L, user(1L), "Street", "City", "State", "12345", "Country", false,
                "SHIPPING", TEST_TIMESTAMP);

        assertNotEquals(base, diffCity);
        assertNotEquals(base, diffDefault);
    }

    @Test
    void settersUpdateFields() {
        Address address = new Address();
        address.setId(10L);
        address.setUser(user(2L));
        address.setStreetAddress("123 Main");
        address.setCity("City");
        address.setState("State");
        address.setPostalCode("99999");
        address.setCountry("Country");
        address.setIsDefault(true);
        address.setAddressType("BILLING");

        assertEquals(10L, address.getId());
        assertEquals("123 Main", address.getStreetAddress());
        assertEquals("City", address.getCity());
        assertEquals("State", address.getState());
        assertEquals("99999", address.getPostalCode());
        assertEquals("Country", address.getCountry());
        assertTrue(address.getIsDefault());
        assertEquals("BILLING", address.getAddressType());
    }
}

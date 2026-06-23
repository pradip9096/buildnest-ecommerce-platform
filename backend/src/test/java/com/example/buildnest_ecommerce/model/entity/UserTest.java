package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User entity tests")
class UserTest {

    @Test
    @DisplayName("Should create User with all fields")
    void testUserConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        Set<Role> roles = new HashSet<>();
        Set<Address> addresses = new HashSet<>();

        User user = new User(
                1L,
                "john_doe",
                "john@example.com",
                "password123",
                "John",
                "Doe",
                "1234567890",
                true,
                false,
                null,
                now,
                now,
                now,
                roles,
                addresses);

        assertEquals(1L, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("1234567890", user.getPhoneNumber());
        assertTrue(user.getIsActive());
        assertFalse(user.getIsDeleted());
        assertNull(user.getDeletedAt());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals(now, user.getLastLogin());
        assertEquals(roles, user.getRoles());
        assertEquals(addresses, user.getAddresses());
    }

    @Test
    @DisplayName("Should create User with no-args constructor")
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
    }

    @Test
    @DisplayName("Should set and get User fields")
    void testSettersAndGetters() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(10L);
        user.setUsername("jane_smith");
        user.setEmail("jane@example.com");
        user.setPassword("securepass");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setPhoneNumber("9876543210");
        user.setIsActive(false);
        user.setIsDeleted(true);
        user.setDeletedAt(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLogin(now);

        assertEquals(10L, user.getId());
        assertEquals("jane_smith", user.getUsername());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("securepass", user.getPassword());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("9876543210", user.getPhoneNumber());
        assertFalse(user.getIsActive());
        assertTrue(user.getIsDeleted());
        assertEquals(now, user.getDeletedAt());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals(now, user.getLastLogin());
    }

    @Test
    @DisplayName("Should test equals and hashCode for identical Users")
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("john_doe");
        user1.setEmail("john@example.com");
        user1.setPassword("pass123");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setCreatedAt(now);

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("john_doe");
        user2.setEmail("john@example.com");
        user2.setPassword("pass123");
        user2.setFirstName("John");
        user2.setLastName("Doe");
        user2.setCreatedAt(now);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should test equals with different Users")
    void testEqualsDifferentUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("john_doe");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("jane_smith");

        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("Should test equals with null and different types")
    void testEqualsNullAndDifferentType() {
        User user = new User();
        user.setId(1L);

        assertNotEquals(user, null);
        assertNotEquals(user, "Not a User");
        assertEquals(user, user);
    }

    @Test
    @DisplayName("Should test equals with null fields")
    void testEqualsWithNullFields() {
        User user1 = new User();
        User user2 = new User();
        assertEquals(user1, user2);

        user1.setUsername("john_doe");
        assertNotEquals(user1, user2);

        user2.setUsername("john_doe");
        assertEquals(user1, user2);
    }

    @Test
    @DisplayName("Should test toString contains key fields")
    void testToString() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        String result = user.toString();
        assertTrue(result.contains("User"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("john_doe"));
        assertTrue(result.contains("john@example.com"));
    }

    @Test
    @DisplayName("Should handle soft delete fields")
    void testSoftDeleteFields() {
        User user = new User();
        assertFalse(user.getIsDeleted()); // Default is false
        assertNull(user.getDeletedAt());

        LocalDateTime deletedTime = LocalDateTime.now();
        user.setIsDeleted(true);
        user.setDeletedAt(deletedTime);

        assertTrue(user.getIsDeleted());
        assertEquals(deletedTime, user.getDeletedAt());
    }

    @Test
    @DisplayName("Should test User with roles collection")
    void testRolesCollection() {
        User user = new User();
        Set<Role> roles = new HashSet<>();

        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ADMIN");
        roles.add(role1);

        user.setRoles(roles);
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(role1));
    }

    @Test
    @DisplayName("Should test User with addresses collection")
    void testAddressesCollection() {
        User user = new User();
        Set<Address> addresses = new HashSet<>();

        Address addr1 = new Address();
        addr1.setId(1L);
        addresses.add(addr1);

        user.setAddresses(addresses);
        assertEquals(1, user.getAddresses().size());
        assertTrue(user.getAddresses().contains(addr1));
    }

    @Test
    @DisplayName("Should test canEqual with subclass")
    void testCanEqualWithSubclass() {
        User user1 = new User();
        user1.setId(1L);

        User subclass = new User() {
        };
        subclass.setId(1L);

        assertEquals(user1, subclass);
    }

    @Test
    @DisplayName("Should test all null fields equals another with all null fields")
    void testAllNullFieldsEquals() {
        User user1 = new User();
        User user2 = new User();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should test User with different passwords are different")
    void testDifferentPasswords() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("john");
        user1.setPassword("pass1");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("john");
        user2.setPassword("pass2");

        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("Should test isActive default behavior")
    void testIsActiveDefault() {
        User user = new User();
        assertTrue(user.getIsActive()); // Default is true

        user.setIsActive(true);
        assertTrue(user.getIsActive());

        user.setIsActive(false);
        assertFalse(user.getIsActive());
    }
}

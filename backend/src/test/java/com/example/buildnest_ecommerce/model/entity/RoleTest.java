package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role entity tests")
class RoleTest {

    @Test
    @DisplayName("Should create Role with all fields")
    void testRoleConstructorAndGetters() {
        Set<User> users = new HashSet<>();
        Set<Permission> permissions = new HashSet<>();

        Role role = new Role(1L, "ADMIN", "Administrator role", users, permissions);

        assertEquals(1L, role.getId());
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertEquals(users, role.getUsers());
        assertEquals(permissions, role.getPermissions());
    }

    @Test
    @DisplayName("Should create Role with no-args constructor")
    void testNoArgsConstructor() {
        Role role = new Role();
        assertNotNull(role);
        assertNull(role.getId());
        assertNull(role.getName());
    }

    @Test
    @DisplayName("Should set and get Role fields")
    void testSettersAndGetters() {
        Role role = new Role();
        Set<User> users = new HashSet<>();
        Set<Permission> permissions = new HashSet<>();

        role.setId(10L);
        role.setName("USER");
        role.setDescription("Regular user role");
        role.setUsers(users);
        role.setPermissions(permissions);

        assertEquals(10L, role.getId());
        assertEquals("USER", role.getName());
        assertEquals("Regular user role", role.getDescription());
        assertEquals(users, role.getUsers());
        assertEquals(permissions, role.getPermissions());
    }

    @Test
    @DisplayName("Should test equals and hashCode for identical Roles")
    void testEqualsAndHashCode() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ADMIN");
        role1.setDescription("Admin role");

        Role role2 = new Role();
        role2.setId(1L);
        role2.setName("ADMIN");
        role2.setDescription("Admin role");

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    @DisplayName("Should test equals with different Roles")
    void testEqualsDifferentRoles() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ADMIN");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("USER");

        assertNotEquals(role1, role2);
    }

    @Test
    @DisplayName("Should test equals with null and different types")
    void testEqualsNullAndDifferentType() {
        Role role = new Role();
        role.setId(1L);

        assertNotEquals(role, null);
        assertNotEquals(role, "Not a Role");
        assertEquals(role, role);
    }

    @Test
    @DisplayName("Should test toString contains key fields")
    void testToString() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");
        role.setDescription("Administrator");

        String result = role.toString();
        assertTrue(result.contains("Role"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("ADMIN"));
    }

    @Test
    @DisplayName("Should test Role with permissions collection")
    void testPermissionsCollection() {
        Role role = new Role();
        Set<Permission> permissions = new HashSet<>();

        Permission perm1 = new Permission();
        perm1.setId(1L);
        perm1.setName("READ");
        permissions.add(perm1);

        role.setPermissions(permissions);
        assertEquals(1, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(perm1));
    }

    @Test
    @DisplayName("Should test Role with users collection")
    void testUsersCollection() {
        Role role = new Role();
        Set<User> users = new HashSet<>();

        User user1 = new User();
        user1.setId(1L);
        users.add(user1);

        role.setUsers(users);
        assertEquals(1, role.getUsers().size());
        assertTrue(role.getUsers().contains(user1));
    }

    @Test
    @DisplayName("Should test canEqual with subclass")
    void testCanEqualWithSubclass() {
        Role role1 = new Role();
        role1.setId(1L);

        Role subclass = new Role() {
        };
        subclass.setId(1L);

        assertEquals(role1, subclass);
    }

    @Test
    @DisplayName("Should test all null fields equals another with all null fields")
    void testAllNullFieldsEquals() {
        Role role1 = new Role();
        Role role2 = new Role();

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    @DisplayName("Should test Role with different names are different")
    void testDifferentNames() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ADMIN");

        Role role2 = new Role();
        role2.setId(1L);
        role2.setName("USER");

        assertNotEquals(role1, role2);
    }
}

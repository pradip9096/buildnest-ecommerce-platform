package com.example.buildnest_ecommerce.service.admin;

import com.example.buildnest_ecommerce.model.dto.AdminUserDto;
import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.entity.Role;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    @Test
    void getAllUsersFiltersDeleted() {
        UserRepository userRepository = mock(UserRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);

        User active = new User();
        active.setId(1L);
        active.setIsDeleted(false);

        User deleted = new User();
        deleted.setId(2L);
        deleted.setIsDeleted(true);

        when(userRepository.findAll()).thenReturn(List.of(active, deleted));

        AdminServiceImpl service = new AdminServiceImpl(userRepository, orderRepository, productRepository);
        List<AdminUserDto> users = service.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    void updateUserByAdminAndDelete() {
        UserRepository userRepository = mock(UserRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);

        User existing = new User();
        existing.setId(1L);
        existing.setIsDeleted(false);
        existing.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminServiceImpl service = new AdminServiceImpl(userRepository, orderRepository, productRepository);

        UpdateUserDTO updateDTO = new UpdateUserDTO("First", "Last", "email@example.com", "+123456789", "Address");
        AdminUserDto dto = service.updateUserByAdmin(1L, updateDTO);
        assertEquals("email@example.com", dto.getEmail());

        service.deleteUser(1L);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void getTotalsAndRevenue() {
        UserRepository userRepository = mock(UserRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);

        User active = new User();
        active.setIsDeleted(false);
        User deleted = new User();
        deleted.setIsDeleted(true);

        Order orderActive = new Order();
        orderActive.setIsDeleted(false);
        Order orderDeleted = new Order();
        orderDeleted.setIsDeleted(true);

        when(userRepository.findAll()).thenReturn(List.of(active, deleted));
        when(orderRepository.findAll()).thenReturn(List.of(orderActive, orderDeleted));
        when(productRepository.count()).thenReturn(5L);

        AdminServiceImpl service = new AdminServiceImpl(userRepository, orderRepository, productRepository);
        assertEquals(1L, service.getTotalUsers());
        assertEquals(1L, service.getTotalOrders());
        assertEquals(5L, service.getTotalProducts());
        assertEquals(0.0, service.getTotalRevenue());
    }

    @Test
    void convertToDtoIncludesRoles() {
        UserRepository userRepository = mock(UserRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);

        Role role = new Role();
        role.setName("ADMIN");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setRoles(Set.of(role));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminServiceImpl service = new AdminServiceImpl(userRepository, orderRepository, productRepository);
        AdminUserDto dto = service.getUserById(1L);
        assertTrue(dto.getRoles().contains("ADMIN"));
    }
}

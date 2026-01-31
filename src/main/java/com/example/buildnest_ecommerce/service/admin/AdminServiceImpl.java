package com.example.buildnest_ecommerce.service.admin;

import com.example.buildnest_ecommerce.model.dto.AdminUserDto;
import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    @Override
    public List<AdminUserDto> getAllUsers() {
        log.info("Fetching all non-deleted users");
        return userRepository.findAll().stream()
                .filter(u -> !u.getIsDeleted())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public AdminUserDto getUserById(Long userId) {
        log.info("Fetching user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }
    
    @Override
    public AdminUserDto updateUser(Long userId, User user) {
        log.info("Updating user with id: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(existingUser);
        return convertToDto(updated);
    }
    
    @Override
    public AdminUserDto updateUserByAdmin(Long userId, UpdateUserDTO updateDTO) {
        log.info("Updating user by admin with id: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setEmail(updateDTO.getEmail());
        existingUser.setFirstName(updateDTO.getFirstName());
        existingUser.setLastName(updateDTO.getLastName());
        existingUser.setPhoneNumber(updateDTO.getPhone());
        existingUser.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(existingUser);
        return convertToDto(updated);
    }
    
    @Override
    public void deleteUser(Long userId) {
        log.info("Soft deleting user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Override
    public Long getTotalUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.getIsDeleted())
                .count();
    }
    
    @Override
    public Long getTotalProducts() {
        return productRepository.count();
    }
    
    @Override
    public Long getTotalOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> !o.getIsDeleted())
                .count();
    }
    
    @Override
    @SuppressWarnings("java:S1602")
    public Double getTotalRevenue() {
        // Note: Revenue calculation will be implemented based on completed orders
        // Current implementation returns 0.0 as placeholder
        return 0.0;
    }
    
    private AdminUserDto convertToDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}

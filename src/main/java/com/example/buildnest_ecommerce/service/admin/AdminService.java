package com.example.buildnest_ecommerce.service.admin;

import com.example.buildnest_ecommerce.model.dto.AdminUserDto;
import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.entity.User;
import java.util.List;

public interface AdminService {
    // User Management
    List<AdminUserDto> getAllUsers();
    AdminUserDto getUserById(Long userId);
    AdminUserDto updateUser(Long userId, User user);
    AdminUserDto updateUserByAdmin(Long userId, UpdateUserDTO updateDTO);
    void deleteUser(Long userId);
    
    // Dashboard Statistics
    Long getTotalUsers();
    Long getTotalProducts();
    Long getTotalOrders();
    Double getTotalRevenue();
}

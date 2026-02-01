package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.AdminUserDto;
import com.example.buildnest_ecommerce.model.dto.UpdateUserDTO;
import com.example.buildnest_ecommerce.model.payload.ApiResponse;
import com.example.buildnest_ecommerce.service.admin.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserControllerTest {

    @Test
    void getAllUsersSuccess() {
        AdminService adminService = mock(AdminService.class);
        when(adminService.getAllUsers()).thenReturn(Collections.singletonList(new AdminUserDto()));

        AdminUserController controller = new AdminUserController(adminService);
        ResponseEntity<ApiResponse> response = controller.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void getAllUsersFailure() {
        AdminService adminService = mock(AdminService.class);
        when(adminService.getAllUsers()).thenThrow(new RuntimeException("fail"));

        AdminUserController controller = new AdminUserController(adminService);
        ResponseEntity<ApiResponse> response = controller.getAllUsers();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void getUserByIdSuccessAndFailure() {
        AdminService adminService = mock(AdminService.class);
        when(adminService.getUserById(1L)).thenReturn(new AdminUserDto());
        when(adminService.getUserById(2L)).thenThrow(new RuntimeException("not found"));

        AdminUserController controller = new AdminUserController(adminService);

        ResponseEntity<ApiResponse> ok = controller.getUserById(1L);
        assertEquals(HttpStatus.OK, ok.getStatusCode());

        ResponseEntity<ApiResponse> notFound = controller.getUserById(2L);
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
    }

    @Test
    void updateUserSuccessAndFailure() {
        AdminService adminService = mock(AdminService.class);
        UpdateUserDTO dto = new UpdateUserDTO();
        when(adminService.updateUserByAdmin(eq(1L), any(UpdateUserDTO.class))).thenReturn(new AdminUserDto());
        when(adminService.updateUserByAdmin(eq(2L), any(UpdateUserDTO.class))).thenThrow(new RuntimeException("bad"));

        AdminUserController controller = new AdminUserController(adminService);

        ResponseEntity<ApiResponse> ok = controller.updateUser(1L, dto);
        assertEquals(HttpStatus.OK, ok.getStatusCode());

        ResponseEntity<ApiResponse> bad = controller.updateUser(2L, dto);
        assertEquals(HttpStatus.BAD_REQUEST, bad.getStatusCode());
    }

    @Test
    void deleteUserSuccessAndFailure() {
        AdminService adminService = mock(AdminService.class);
        doThrow(new RuntimeException("bad")).when(adminService).deleteUser(2L);

        AdminUserController controller = new AdminUserController(adminService);

        ResponseEntity<ApiResponse> ok = controller.deleteUser(1L);
        assertEquals(HttpStatus.OK, ok.getStatusCode());

        ResponseEntity<ApiResponse> bad = controller.deleteUser(2L);
        assertEquals(HttpStatus.BAD_REQUEST, bad.getStatusCode());
    }
}

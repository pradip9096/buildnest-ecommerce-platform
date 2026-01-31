package com.example.buildnest_ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public user endpoints.
 * Excludes sensitive fields like password hashes and internal security data.
 * Used to prevent information leakage in API responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
}

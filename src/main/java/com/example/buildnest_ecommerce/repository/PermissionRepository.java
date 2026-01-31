package com.example.buildnest_ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.buildnest_ecommerce.model.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
}

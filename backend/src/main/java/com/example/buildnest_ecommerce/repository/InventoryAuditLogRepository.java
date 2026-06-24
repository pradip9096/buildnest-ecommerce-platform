package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.InventoryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryAuditLogRepository extends JpaRepository<InventoryAuditLog, Long> {

    List<InventoryAuditLog> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId);

    List<InventoryAuditLog> findByProductIdOrderByCreatedAtDesc(Long productId);
}

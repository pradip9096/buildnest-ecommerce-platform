package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.OrderGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderGroupRepository
        extends JpaRepository<OrderGroup, Long> {
}

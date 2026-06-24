package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {

    List<ShippingMethod> findAllByIsActiveTrue();
}

package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUser_Id(Long userId);
}

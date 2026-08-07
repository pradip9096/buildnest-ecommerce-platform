package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.ReturnRequest;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ReturnRequestRepository
        extends JpaRepository<ReturnRequest, Long>,
        JpaSpecificationExecutor<ReturnRequest> {

    Optional<ReturnRequest> findByOrderIdAndStatusIn(
            Long orderId, Iterable<ReturnStatus> statuses);

    Page<ReturnRequest> findAll(
            org.springframework.data.jpa.domain.Specification<
                    ReturnRequest> spec,
            Pageable pageable);
}

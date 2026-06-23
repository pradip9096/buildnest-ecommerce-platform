package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find pending orders older than a specified date/time for follow-up
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' " +
            "AND o.createdAt < :threshold")
    List<Order> findPendingOrdersOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Find orders by date range and status for reporting
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end " +
            "AND o.status = :status")
    List<Order> findByDateRangeAndStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") String status);

    /**
     * Calculate total revenue for a specific period
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "AND o.status = 'COMPLETED' AND o.isDeleted = false")
    BigDecimal calculateRevenueForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find all orders for a specific user with eager loading of items.
     * Uses EntityGraph to prevent N+1 query problems.
     */
    @EntityGraph(attributePaths = { "orderItems", "user" })
    List<Order> findByUserId(Long userId);

    /**
     * Find order by ID with all related data loaded eagerly.
     * Prevents N+1 queries when accessing order items and user details.
     */
    @EntityGraph(attributePaths = { "orderItems", "user", "shippingAddress" })
    Optional<Order> findById(Long id);

    /**
     * Find all orders with eager loading of related entities.
     * Prevents N+1 queries for bulk order retrieval.
     */
    @EntityGraph(attributePaths = { "orderItems", "user" })
    List<Order> findAll();
}

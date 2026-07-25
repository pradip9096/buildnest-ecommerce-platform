package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {

    /**
     * Find pending orders older than a specified date/time for follow-up
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' " +
            "AND o.createdAt < :threshold")
    List<Order> findPendingOrdersOlderThan(
            @Param("threshold") LocalDateTime threshold);

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
     * Sum revenue for orders in [start, end) with the given status.
     * COALESCE returns 0 when no rows match so callers never receive null.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.createdAt >= :start AND o.createdAt < :end " +
            "AND o.status = :status AND o.isDeleted = false")
    BigDecimal sumRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") Order.OrderStatus status);

    /**
     * Count all non-deleted orders placed in [start, end).
     */
    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.createdAt >= :start AND o.createdAt < :end " +
            "AND o.isDeleted = false")
    Long countOrdersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Average totalAmount of orders with the given status in [start, end).
     * Returns null when no rows match; callers must handle null.
     */
    @Query("SELECT AVG(o.totalAmount) FROM Order o " +
            "WHERE o.createdAt >= :start AND o.createdAt < :end " +
            "AND o.status = :status AND o.isDeleted = false")
    BigDecimal avgOrderValueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") Order.OrderStatus status);

    /**
     * Top-selling products by units sold in [start, end) for orders with
     * the given status. Each element is Object[]{productId (Long),
     * productName (String), unitsSold (Long), revenue (BigDecimal)}.
     */
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity), " +
            "SUM(oi.subtotal) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.createdAt >= :start " +
            "AND oi.order.createdAt < :end " +
            "AND oi.order.status = :status AND oi.order.isDeleted = false " +
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") Order.OrderStatus status,
            Pageable pageable);

    /**
     * Revenue grouped by category name in [start, end) for orders with
     * the given status. Each element is Object[]{categoryName (String),
     * revenue (BigDecimal)}.
     */
    @Query("SELECT oi.product.category.name, SUM(oi.subtotal) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.createdAt >= :start " +
            "AND oi.order.createdAt < :end " +
            "AND oi.order.status = :status AND oi.order.isDeleted = false " +
            "GROUP BY oi.product.category.name")
    List<Object[]> sumRevenueGroupedByCategory(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") Order.OrderStatus status);

    /**
     * Count of distinct customers (by user) who have at least one order
     * with the given status.
     */
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o " +
            "WHERE o.status = :status AND o.isDeleted = false")
    Long countDistinctCustomersByStatus(
            @Param("status") Order.OrderStatus status);

    /**
     * Count of distinct customers who placed more than one order with the
     * given status (used as a proxy for returning / retained customers).
     */
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o " +
            "WHERE o.user.id IN (" +
            "  SELECT o2.user.id FROM Order o2 " +
            "  WHERE o2.status = :status AND o2.isDeleted = false " +
            "  GROUP BY o2.user.id HAVING COUNT(o2.id) > 1" +
            ") AND o.status = :status AND o.isDeleted = false")
    Long countReturningCustomers(@Param("status") Order.OrderStatus status);

    /**
     * Sum of totalAmount across all delivered orders for a specific user.
     * COALESCE returns 0 when the user has no qualifying orders.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.user.id = :userId AND o.status = :status " +
            "AND o.isDeleted = false")
    BigDecimal sumRevenueByUser(
            @Param("userId") Long userId,
            @Param("status") Order.OrderStatus status);

    /**
     * Find all orders for a specific user with eager loading of items.
     * Uses EntityGraph to prevent N+1 query problems.
     */
    @EntityGraph(attributePaths = { "orderItems", "user" })
    List<Order> findByUserId(Long userId);

    /**
     * Sibling per-seller orders sharing one {@code order_group_id}
     * (FR-SEL-06, #579) — used to confirm/inspect an entire multi-seller
     * checkout split as one unit.
     */
    List<Order> findByOrderGroupId(Long orderGroupId);

    /**
     * Find order by ID with all related data loaded eagerly.
     * Prevents N+1 queries when accessing order items and user details.
     */
    @EntityGraph(attributePaths = { "orderItems", "user", "shippingAddress" })
    Optional<Order> findById(Long id);

    /**
     * Seller-scoped order listing (FR-SEL-06, #580). {@code Order} carries
     * no direct seller reference — ownership is derived transitively via
     * {@code OrderItem.product.seller}, since #579's checkout split
     * guarantees every item in one {@code Order} belongs to a single
     * seller. Uses an {@code EXISTS} subquery rather than an explicit
     * join so pagination stays correct — see the wiki lesson on explicit
     * joins breaking DISTINCT/pagination under fetch-joined collections.
     */
    @Query("SELECT o FROM Order o WHERE o.isDeleted = false AND EXISTS "
            + "(SELECT 1 FROM OrderItem oi WHERE oi.order = o "
            + "AND oi.product.seller.id = :sellerId)")
    Page<Order> findBySellerId(
            @Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * Seller-scoped single-order lookup with ownership enforced in the
     * query itself (FR-SEL-06, #580) — mirrors
     * {@code ProductRepository.findByIdAndSeller_Id}'s
     * find-or-not-found-at-all pattern rather than a separate ownership
     * check after an unscoped fetch.
     */
    @Query("SELECT o FROM Order o WHERE o.id = :orderId "
            + "AND o.isDeleted = false AND EXISTS "
            + "(SELECT 1 FROM OrderItem oi WHERE oi.order = o "
            + "AND oi.product.seller.id = :sellerId)")
    Optional<Order> findByIdAndSellerId(
            @Param("orderId") Long orderId, @Param("sellerId") Long sellerId);

    /**
     * Find all orders with eager loading of related entities.
     * Prevents N+1 queries for bulk order retrieval.
     */
    @EntityGraph(attributePaths = { "orderItems", "user" })
    List<Order> findAll();
}

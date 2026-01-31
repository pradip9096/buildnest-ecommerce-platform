package com.example.buildnest_ecommerce.util;

/**
 * Database Query Optimization Utility
 *
 * PERFORMANCE_OPTIMIZATION_GUIDE - Section 3: Database Optimization
 *
 * This class contains SQL optimization patterns and best practices for
 * BuildNest application database queries.
 *
 * Key Optimization Patterns:
 * 1. JPQL Projection: SELECT specific fields instead of full entities
 * 2. Query Caching: Cache frequently executed queries with @Cacheable
 * 3. Batch Operations: Use IN queries for bulk operations
 * 4. Pagination: Always paginate large result sets
 * 5. Join Fetch: Prevent N+1 query problems with explicit JOIN FETCH
 * 6. Entity Graph: Use @EntityGraph for fine-grained fetch strategies
 * 7. DTO Projection: Map directly to DTOs for reduced memory usage
 *
 * @author BuildNest Team
 * @version 1.0
 * @since 1.0.0
 */
public final class DatabaseQueryOptimizationPatterns {

    private DatabaseQueryOptimizationPatterns() {
        // Utility class
    }

    /**
     * PATTERN 1: JPQL Projection Query
     *
     * ✅ GOOD: Selects only required fields
     * ❌ AVOID: SELECT o FROM Order o (loads all fields)
     *
     * Example:
     * 
     * <pre>
     * &#64;Query("SELECT new com.example.buildnest_ecommerce.model.dto.OrderDTO(o.id, o.orderNumber, o.totalAmount) "
     *         + "FROM Order o WHERE o.userId = :userId")
     * List<OrderDTO> getUserOrders(@Param("userId") Long userId);
     * </pre>
     *
     * Benefits:
     * - Reduces memory usage by selecting only needed columns
     * - Faster queries (fewer bytes transferred)
     * - Network bandwidth reduced
     */
    public static final String PATTERN_1_JPQL_PROJECTION = "Pattern 1: JPQL Projection";

    /**
     * PATTERN 2: Query Result Caching
     *
     * ✅ GOOD: Cache frequently accessed queries with @Cacheable
     * ❌ AVOID: Execute same query multiple times
     *
     * Example:
     * 
     * <pre>
     * @Cacheable(value = "categories", key = "#categoryId")
     * public Category getCategoryById(Long categoryId) {
     *     return categoryRepository.findById(categoryId).orElseThrow();
     * }
     * </pre>
     *
     * Benefits:
     * - Eliminates redundant database queries
     * - Reduces database load
     * - Faster response times (Redis cache < 1ms)
     */
    public static final String PATTERN_2_QUERY_CACHING = "Pattern 2: Query Result Caching (@Cacheable)";

    /**
     * PATTERN 3: Batch Operations with IN Queries
     *
     * ✅ GOOD: Use IN clause for bulk operations
     * ❌ AVOID: Loop and execute queries individually (N queries)
     *
     * Example:
     * 
     * <pre>
     * // Good: Single query for all product IDs
     * &#64;Query("SELECT p FROM Product p WHERE p.id IN (:productIds)")
     * List<Product> findByIds(@Param("productIds") List<Long> productIds);
     *
     * // Bad: N queries
     * productIds.forEach(id -> productRepository.findById(id));
     * </pre>
     *
     * Benefits:
     * - Reduces from N queries to 1 query
     * - Significant performance improvement for bulk operations
     * - Scales linearly instead of exponentially
     */
    public static final String PATTERN_3_BATCH_OPERATIONS = "Pattern 3: Batch Operations with IN Queries";

    /**
     * PATTERN 4: Pagination for Large Result Sets
     *
     * ✅ GOOD: Always paginate large queries
     * ❌ AVOID: SELECT * FROM huge_table (memory exhaustion)
     *
     * Example:
     * 
     * <pre>
     * &#64;Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
     * Page<Order> findByStatus(@Param("status") String status, Pageable pageable);
     *
     * // Usage:
     * Page<Order> page = orderRepository.findByStatus("COMPLETED",
     *         PageRequest.of(0, 50, Sort.by("createdAt").descending()));
     * </pre>
     *
     * Benefits:
     * - Prevents memory exhaustion with large datasets
     * - Improves API response time (50 items vs 10000 items)
     * - Better user experience with pagination controls
     */
    public static final String PATTERN_4_PAGINATION = "Pattern 4: Pagination with PageRequest";

    /**
     * PATTERN 5: Join Fetch for N+1 Query Prevention
     *
     * ✅ GOOD: Use JOIN FETCH to load relationships eagerly
     * ❌ AVOID: Lazy loading without explicit fetch (N+1 problem)
     *
     * Example:
     * 
     * <pre>
     * // Good: Single query with JOIN FETCH (1 query)
     * &#64;Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
     * Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
     *
     * // Bad: N+1 Query Problem
     * Order order = orderRepository.findById(orderId).orElseThrow();
     * order.getItems().forEach(item -> item.getProduct().getName()); // Triggers N queries
     * </pre>
     *
     * Benefits:
     * - Eliminates N+1 query problem
     * - Single database roundtrip instead of N+1
     * - 10-100x performance improvement for large result sets
     */
    public static final String PATTERN_5_JOIN_FETCH = "Pattern 5: JOIN FETCH for N+1 Prevention";

    /**
     * PATTERN 6: Entity Graph for Fine-Grained Fetch Strategies
     *
     * ✅ GOOD: Use @EntityGraph to control fetch strategies
     * ❌ AVOID: Global LazyInitializationException or excessive eager loading
     *
     * Example:
     * 
     * <pre>
     * @EntityGraph(attributePaths = { "items", "payment", "user" })
     * &#64;Query("SELECT o FROM Order o WHERE o.id = :orderId")
     * Optional<Order> findWithAllRelations(@Param("orderId") Long orderId);
     * </pre>
     *
     * Benefits:
     * - Fine-grained control over relationship loading
     * - Prevents LazyInitializationException
     * - Reduces unnecessary JOIN OUTERs when not needed
     */
    public static final String PATTERN_6_ENTITY_GRAPH = "Pattern 6: @EntityGraph for Fetch Strategies";

    /**
     * PATTERN 7: DTO Projection for Memory Efficiency
     *
     * ✅ GOOD: Map to lightweight DTOs
     * ❌ AVOID: Load full entities when only few fields needed
     *
     * Example:
     * 
     * <pre>
     * interface ProductDTO {
     *     Long getId();
     * 
     *     String getName();
     * 
     *     BigDecimal getPrice();
     * }
     *
     * &#64;Query("SELECT new com.example.buildnest_ecommerce.model.dto.ProductDTO(" +
     *         "p.id, p.name, p.price) FROM Product p WHERE p.category.id = :categoryId")
     * List<ProductDTO> findByCategoryId(@Param("categoryId") Long categoryId);
     * </pre>
     *
     * Benefits:
     * - Reduced memory footprint
     * - Faster serialization to JSON
     * - Better for large result sets
     */
    public static final String PATTERN_7_DTO_PROJECTION = "Pattern 7: DTO Projection";

    /**
     * Slow Query Monitoring Configuration
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Section 3.5: Slow Query Monitoring
     *
     * Enable in MySQL:
     * 
     * <pre>
     * SET GLOBAL slow_query_log = ON;
     * SET GLOBAL long_query_time = 1;  // Queries > 1 second
     * </pre>
     *
     * View slow queries:
     * 
     * <pre>
     * SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 20;
     * </pre>
     */
    public static final String SLOW_QUERY_MONITORING = "Enable MySQL slow query log for > 1 second queries";

    /**
     * Index Recommendations
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Section 3.4: Database Indexing
     *
     * Essential indexes for BuildNest:
     * 
     * <pre>
     * -- Foreign keys (required for joins)
     * CREATE INDEX idx_order_user_id ON orders(user_id);
     * CREATE INDEX idx_order_payment_id ON orders(payment_id);
     * CREATE INDEX idx_order_item_order_id ON order_items(order_id);
     *
     * -- Status queries (commonly filtered)
     * CREATE INDEX idx_order_status ON orders(status);
     * CREATE INDEX idx_payment_status ON payments(status);
     *
     * -- Date range queries
     * CREATE INDEX idx_order_created_at ON orders(created_at);
     * CREATE INDEX idx_payment_created_at ON payments(created_at);
     *
     * -- Business key queries
     * CREATE INDEX idx_product_sku ON products(sku);
     * CREATE INDEX idx_user_email ON users(email);
     * </pre>
     *
     * Verify index usage:
     * 
     * <pre>
     * EXPLAIN SELECT * FROM orders WHERE user_id = 42;
     * // Should see "Using index" in output
     * </pre>
     */
    public static final String INDEX_RECOMMENDATIONS = "Create indexes on FK, status, dates, and business keys";

    /**
     * Connection Pool Configuration Summary
     *
     * PERFORMANCE_OPTIMIZATION_GUIDE - Section 5: Connection Pool Tuning
     *
     * Calculate optimal pool size:
     * Pool Size = (core_count × 2) + effective_spindle_count
     *
     * For 4-core server: (4 × 2) + 0 = 8 minimum, 15 optimal
     *
     * Configuration in application.properties:
     * spring.datasource.hikari.maximum-pool-size=20
     * spring.datasource.hikari.minimum-idle=10
     */
    public static final String POOL_SIZE_FORMULA = "Pool Size = (core_count × 2) + effective_spindle_count";

    /**
     * Get optimization checklist
     *
     * @return String with optimization checklist
     */
    public static String getOptimizationChecklist() {
        return "Database Query Optimization Checklist:\n" +
                "[ ] Pattern 1 - Use JPQL projections for specific fields\n" +
                "[ ] Pattern 2 - Cache frequently accessed queries with @Cacheable\n" +
                "[ ] Pattern 3 - Use IN queries for bulk operations (not loops)\n" +
                "[ ] Pattern 4 - Paginate large result sets with PageRequest\n" +
                "[ ] Pattern 5 - Use JOIN FETCH to prevent N+1 query problems\n" +
                "[ ] Pattern 6 - Use @EntityGraph for fine-grained fetch control\n" +
                "[ ] Pattern 7 - Map to DTOs for memory efficiency\n" +
                "[ ] Index all foreign keys and frequently filtered columns\n" +
                "[ ] Enable slow query monitoring (queries > 1 second)\n" +
                "[ ] Monitor connection pool utilization regularly\n" +
                "[ ] Review execution plans with EXPLAIN for all critical queries";
    }
}

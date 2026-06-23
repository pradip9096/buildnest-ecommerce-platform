# Coding Standards Document

## BuildNest E-Commerce Platform

---

## DOCUMENT INFORMATION

| Attribute                | Value                                      |
| :----------------------- | :----------------------------------------- |
| **Document Title**       | Coding Standards Document                  |
| **Document ID**          | CSD-BUILDNEST-001                          |
| **Version**              | 2.0                                        |
| **Date**                 | February 11, 2026                          |
| **Status**               | Baselined                                  |
| **Classification**       | Internal Use                               |
| **Conformance Standard** | ISO/IEC 25010:2011                         |
| **Parent Document**      | [SDD (DOC-SDD-001)](SDD_IEEE_1016_2017.md) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date       | Author   | Changes                                                                          | Approval    |
| :------ | :--------- | :------- | :------------------------------------------------------------------------------- | :---------- |
| 1.0     | 2026-02-10 | Dev Team | Initial — basic naming conventions                                               | ✅ Approved |
| 2.0     | 2026-02-11 | Dev Team | ISO 25010 alignment: added Quality Model mapping, Security standards, Checklists | ✅ Pending  |

### Document Approval

| Role               | Name     | Signature      | Date             |
| :----------------- | :------- | :------------- | :--------------- |
| **Technical Lead** | Dev Lead | \***\*\_\*\*** | \***\*\_\_\*\*** |
| **QA Lead**        | QA Lead  | \***\*\_\*\*** | \***\*\_\_\*\*** |

---

## 1. Introduction

### 1.1 Purpose

This document establishes coding standards, naming conventions, and quality patterns for the BuildNest E-Commerce Platform. All contributors must follow these standards to ensure **maintainability**, **consistency**, and **testability** across the codebase (28 controllers, 56 services, 19 repositories, 48 models, 38 config classes).

### 1.2 Scope

| Layer             | Package                                                                                                                                     | Standards Covered |
| :---------------- | :------------------------------------------------------------------------------------------------------------------------------------------ | :---------------- |
| **Controllers**   | `controller.admin.*`, `controller.auth.*`, `controller.user.*`, `controller.monitoring.*`, `controller.public_.*`, `controller.inventory.*` | §3.1              |
| **Services**      | `service.*` (56 files across 22 subpackages)                                                                                                | §3.2              |
| **Repositories**  | `repository.*` (19 files)                                                                                                                   | §3.3              |
| **Models**        | `model.entity.*`, `model.dto.*`, `model.payload.*`, `model.elasticsearch.*`                                                                 | §3.4              |
| **Configuration** | `config.*` (38 files)                                                                                                                       | §3.5              |
| **Security**      | `security.*` (8 files)                                                                                                                      | §3.6              |
| **Cross-cutting** | `aspect.*`, `annotation.*`, `interceptor.*`, `validation.*`, `validator.*`                                                                  | §3.7              |

---

### 1.3 Normative References

| Reference                    | Description                               |
| :--------------------------- | :---------------------------------------- |
| **ISO/IEC 25010:2011**       | Systems and software engineering — SQuaRE |
| [SDD](SDD_IEEE_1016_2017.md) | Software Design Description               |

### 1.4 Definitions & Abbreviations

| Term / Abbr | Definition                                   |
| :---------- | :------------------------------------------- |
| **CSD**     | Coding Standards Document                    |
| **SQuaRE**  | Software Quality Requirements and Evaluation |
| **DTO**     | Data Transfer Object                         |
| **AOP**     | Aspect-Oriented Programming                  |

### 1.5 Conformance Statement

> This document conforms to **ISO/IEC 25010:2011** by defining quality characteristics (maintainability, reliability, security) and mapping them to concrete coding standards and verification criteria.

---

## 2. General Conventions

### 2.1 Code Formatting

| Rule             | Standard                                                    |
| :--------------- | :---------------------------------------------------------- |
| **Indentation**  | 4 spaces (no tabs)                                          |
| **Line Length**  | Max 120 characters                                          |
| **Brace Style**  | K&R (opening brace on same line)                            |
| **Imports**      | No wildcards; organized: java._ → jakarta._ → org._ → com._ |
| **Encoding**     | UTF-8                                                       |
| **Line Endings** | LF (Unix-style)                                             |

### 2.2 Commenting

| Type                 | When                              | Format                                           |
| :------------------- | :-------------------------------- | :----------------------------------------------- |
| **Javadoc**          | All public classes and methods    | `/** ... */` with `@param`, `@return`, `@throws` |
| **Inline**           | Complex logic only                | `// Single line`                                 |
| **TODO**             | Temporary markers for future work | `// TODO: [description] - [owner]`               |
| **Section comments** | Service methods grouping          | `// --- Section Name ---`                        |

### 2.3 Naming Conventions

| Element       | Convention                                      | Example                                              |
| :------------ | :---------------------------------------------- | :--------------------------------------------------- |
| **Package**   | `lowercase.dotnotation`                         | `com.example.buildnest_ecommerce.service.cart`       |
| **Class**     | `PascalCase`                                    | `CheckoutService`, `ProductReview`                   |
| **Interface** | `PascalCase` (prefix `I` for service contracts) | `IAdminAnalyticsService`, `IRateLimiterService`      |
| **Method**    | `camelCase` (verb-first)                        | `processCheckout()`, `validateToken()`               |
| **Variable**  | `camelCase`                                     | `quantityInStock`, `orderNumber`                     |
| **Constant**  | `UPPER_SNAKE_CASE`                              | `MAX_LOGIN_ATTEMPTS`, `TOKEN_EXPIRY_MS`              |
| **Enum**      | `PascalCase` class, `UPPER_SNAKE_CASE` values   | `OrderStatus.CONFIRMED`, `InventoryStatus.LOW_STOCK` |
| **DTO**       | `EntityNameDTO` or `ActionDTO`                  | `ProductDTO`, `LoginDTO`, `RegisterRequest`          |
| **Event**     | `SubjectVerbEvent`                              | `OrderPlacedEvent`, `LowStockWarningEvent`           |

---

## 3. Layer-Specific Standards

### 3.1 Controller Layer

```java
// PATTERN: Controllers must follow REST resource naming
@RestController
@RequestMapping("/api/user/reviews")  // Resource-based URL
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;  // Single service injection

    // RULE: Use appropriate HTTP methods
    @PostMapping("/product/{productId}")   // Create
    @GetMapping("/product/{productId}")    // Read
    @PutMapping("/{reviewId}")            // Update
    @DeleteMapping("/{reviewId}")          // Delete

    // RULE: Return ResponseEntity with consistent wrapper
    public ResponseEntity<?> submitReview(...) {
        return ResponseEntity.ok(reviewService.submitReview(...));
    }
}
```

**Controller Rules:**

| Rule                                      | Enforcement                                 |
| :---------------------------------------- | :------------------------------------------ |
| No business logic in controllers          | Service delegation only                     |
| Input validation via `@Valid`             | Bean validation annotations on request DTOs |
| Use `ResponseEntity` return type          | Consistent HTTP status codes                |
| Admin controllers under `/api/admin/**`   | `@PreAuthorize("hasRole('ADMIN')")`         |
| User controllers under `/api/user/**`     | Requires authentication                     |
| Public controllers under `/api/public/**` | `permitAll()` in `SecurityConfig`           |

### 3.2 Service Layer

```java
// PATTERN: Services implement business logic
@Service
@Transactional  // Applied at class level for write operations
public class InventoryService {

    // RULE: Interface-based contracts for testability
    // Some services implement interfaces: IAdminAnalyticsService, IRateLimiterService

    // RULE: Method names describe business action
    public void reserveStock(Long productId, int quantity) { ... }
    public void deductStock(Long productId, int quantity) { ... }
    public void releaseReservation(Long productId, int quantity) { ... }

    // RULE: Throw domain-specific exceptions
    throw new InsufficientStockException(productId);
    throw new ResourceNotFoundException("Product", productId);
}
```

**Service Rules:**

| Rule                                  | Pattern                                        |
| :------------------------------------ | :--------------------------------------------- |
| `@Transactional` for write operations | Class or method level                          |
| Domain exceptions for business errors | Custom exceptions extending `RuntimeException` |
| Event publishing for side effects     | `DomainEventPublisher.publish()`               |
| No direct HTTP concerns               | No `HttpServletRequest`, no status codes       |

### 3.3 Repository Layer

```java
// PATTERN: Spring Data JPA repositories
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // RULE: Method names follow Spring Data query derivation
    List<Product> findByCategoryId(Long categoryId);
    Optional<Product> findBySku(String sku);

    // RULE: Custom queries use @Query annotation
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity > 0")
    Page<Product> findActiveInStockProducts(Pageable pageable);
}
```

### 3.4 Model Layer

#### 3.4.1 Entities

```java
// PATTERN: JPA entities with Lombok annotations
@Entity
@Table(name = "product_review", indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_rating", columnList = "rating")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(exclude = {"product", "user", "createdAt"})
@ToString(exclude = {"product", "user"})
public class ProductReview {
    // RULE: Use @Id + @GeneratedValue(GenerationType.IDENTITY)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RULE: @PrePersist / @PreUpdate for timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // RULE: Bean validation on entity fields
    @NotNull @Min(1) @Max(5)
    private Integer rating;

    @Size(max = 2000)
    private String comment;
}
```

**Entity Rules:**

| Rule                                                      | Details                                                    |
| :-------------------------------------------------------- | :--------------------------------------------------------- |
| Exclude lazy associations from `equals/hashCode/toString` | Prevent N+1 and `LazyInitializationException`              |
| Use `@Builder.Default` for default values                 | `private Boolean isActive = true;` with `@Builder.Default` |
| Define indexes for frequently queried columns             | `@Index` annotation on `@Table`                            |
| Use `@Version` for optimistic locking where needed        | `Inventory.version`                                        |

#### 3.4.2 DTOs and Payloads

```java
// PATTERN: Separate DTO classes for API contracts
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    // No entity references — only primitive/serializable types
}
```

#### 3.4.3 Elasticsearch Documents

```java
@Document(indexName = "buildnest-audit-logs")
public class ElasticsearchAuditLog {
    @Id private String id;
    @Field(type = FieldType.Keyword) private String action;
    @Field(type = FieldType.Date) private LocalDateTime timestamp;
}
```

### 3.5 Configuration Layer

```java
// PATTERN: Spring @Configuration with descriptive names
@Configuration
public class CacheConfig {
    // RULE: Use @Bean methods with clear return type
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() { ... }
}
```

**Configuration Naming:**

| Type           | Naming Pattern              | Examples                                                         |
| :------------- | :-------------------------- | :--------------------------------------------------------------- |
| Feature config | `FeatureNameConfig`         | `CacheConfig`, `SecurityConfig`, `ElasticsearchConfig`           |
| Filter         | `FeatureNameFilter`         | `ChaosEngineeringFilter`, `AdminRateLimitFilter`                 |
| Optimization   | `FeatureOptimizationConfig` | `DatabaseQueryOptimizationConfig`, `ContainerOptimizationConfig` |
| Enhancement    | `FeatureEnhancementConfig`  | `InputValidationEnhancementConfig`                               |

### 3.6 Security Standards

| Standard                        | Implementation                                  |
| :------------------------------ | :---------------------------------------------- |
| BCrypt password hashing         | `BCryptPasswordEncoder` (strength 10)           |
| JWT signing                     | HMAC-SHA512 with secret in environment variable |
| HTTPS enforcement in production | `@PostConstruct` validation in `SecurityConfig` |
| CORS whitelist                  | Only `buildnest.com` origins                    |
| Security headers                | CSP, X-Frame-Options (DENY), HSTS               |
| Method-level security           | `@PreAuthorize`, `@Secured`, `@RolesAllowed`    |
| Input validation                | Bean validation + custom validators (9 files)   |

### 3.7 Cross-Cutting Standards

#### 3.7.1 Custom Annotations

| Annotation          | Purpose                                | Example Usage                     |
| :------------------ | :------------------------------------- | :-------------------------------- |
| `@Auditable`        | Mark methods for AOP audit logging     | `@Auditable("CREATE_ORDER")`      |
| `@ApiSunset`        | Mark deprecated API versions           | `@ApiSunset(date = "2026-03-01")` |
| `@ServiceLayerOnly` | Restrict method calls to service layer | Methodological enforcement        |

#### 3.7.2 Domain Events

```java
// PATTERN: Event naming and structure
public class OrderPlacedEvent extends ApplicationEvent {
    private final Long orderId;
    private final String orderNumber;
    private final BigDecimal totalAmount;
    // Constructor passes source + fields
}

// PATTERN: Event listener
@Component
public class DomainEventListener {
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) { ... }
}
```

#### 3.7.3 Exception Handling

| Exception Type                 | HTTP Status | Usage                      |
| :----------------------------- | :---------- | :------------------------- |
| `ResourceNotFoundException`    | 404         | Entity not found           |
| `UnauthorizedException`        | 401         | Authentication failure     |
| `ValidationException`          | 400         | Input validation failure   |
| `InsufficientStockException`   | 409         | Stock not available        |
| `CartEmptyException`           | 400         | Checkout with empty cart   |
| `PaymentVerificationException` | 402         | Payment signature mismatch |
| `OutOfStockException`          | 409         | Product out of stock       |

---

## 4. Database Standards

### 4.1 Naming Conventions

| Element               | Convention                            | Example                                      |
| :-------------------- | :------------------------------------ | :------------------------------------------- |
| **Table**             | `snake_case`, singular or descriptive | `product_review`, `wishlist_products`        |
| **Column**            | `snake_case`                          | `quantity_in_stock`, `is_active`             |
| **Index**             | `idx_` prefix + columns               | `idx_product_id`, `idx_rating`               |
| **Unique Constraint** | Table-level `@UniqueConstraint`       | `@UniqueConstraint(columnNames = "user_id")` |
| **Foreign Key**       | `entity_id` suffix                    | `user_id`, `product_id`, `order_id`          |

### 4.2 Migration Standards

| Rule                   | Standard                                                        |
| :--------------------- | :-------------------------------------------------------------- |
| Tool                   | Liquibase (YAML/XML changelogs)                                 |
| File naming            | `NNN-description.xml` (e.g., `005-add-performance-indexes.xml`) |
| Backward compatibility | All migrations must be additive (no destructive changes)        |
| Rollback               | Each changeset must have a rollback section                     |

---

## 5. Testing Standards

### 5.1 Naming Conventions

```java
// Test class: ServiceNameTest.java (same package as source)
public class ProductReviewServiceTest {

    // Method naming: should_expectedBehavior_when_condition()
    @Test
    void should_submitReview_when_validRatingProvided() { ... }

    @Test
    void should_throwException_when_ratingBelowMinimum() { ... }
}
```

### 5.2 Test Organization

| Annotation        | When to Use                     | Database             |
| :---------------- | :------------------------------ | :------------------- |
| `@SpringBootTest` | Full integration/system tests   | Full context with H2 |
| `@WebMvcTest`     | Controller slice tests          | No DB, MockMvc       |
| `@DataJpaTest`    | Repository tests                | H2 with rollback     |
| Pure JUnit 5      | Service unit tests with Mockito | None (mocked)        |

### 5.3 Test Data

- Use builder pattern via entity `@Builder` annotations
- Use `@Transactional` with auto-rollback in integration tests
- Use `TestDataFactory` for shared test entities
- Never rely on external services (mock Razorpay, SMTP)

---

## 6. Quality Attribute Traceability (ISO 25010)

| Quality Attribute          | Standard Applied                                      | Verification           |
| :------------------------- | :---------------------------------------------------- | :--------------------- |
| **Functional Suitability** | Complete API coverage (83+ endpoints)                 | Functional test cases  |
| **Performance Efficiency** | DB indexes, Redis cache, query optimization           | Performance test cases |
| **Compatibility**          | OpenAPI 3 spec, CORS, API versioning                  | Integration test cases |
| **Usability**              | Consistent error messages, API envelope               | E2E test cases         |
| **Reliability**            | Optimistic locking, retry, graceful shutdown          | Reliability test cases |
| **Security**               | JWT, RBAC, BCrypt, input validation, HTTPS            | Security test cases    |
| **Maintainability**        | Package-per-feature, AOP, DDD, DI                     | Code review checklist  |
| **Portability**            | Container-first, external config, no hard-coded paths | Deployment tests       |

---

## 7. Code Review Checklist

|  #  | Check                                          | Pass Criteria                       |
| :-: | :--------------------------------------------- | :---------------------------------- |
|  1  | Follows naming conventions (§2.3)              | All names comply                    |
|  2  | No business logic in controllers               | Service delegation only             |
|  3  | `@Transactional` on write service methods      | Applied correctly                   |
|  4  | Bean validation on request DTOs                | `@Valid` + constraints              |
|  5  | Domain exceptions (not generic)                | Custom exception classes            |
|  6  | Lombok exclusions for lazy collections         | `@EqualsAndHashCode(exclude=...)`   |
|  7  | Audit logging on sensitive operations          | `@Auditable` annotation             |
|  8  | No hardcoded credentials or URLs               | Config via `application.properties` |
|  9  | Unit test coverage for new code                | ≥ 80% line coverage                 |
| 10  | Security annotations on admin endpoints        | `@PreAuthorize("hasRole('ADMIN')")` |
| 11  | API versioning for breaking changes            | New version, sunset old             |
| 12  | Event publishing for cross-module side effects | `DomainEventPublisher`              |

---

## 8. Revision History

See [Document Control](#document-control) for full revision history and approvals.

---

**— End of Document —**

_This document was prepared in compliance with ISO/IEC 25010:2011 for the BuildNest E-Commerce Platform._

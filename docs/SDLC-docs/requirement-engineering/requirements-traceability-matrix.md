# Requirements Traceability Matrix (RTM)

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## DOCUMENT INFORMATION

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Requirements Traceability Matrix (RTM) |
| **Document ID** | RTM-BUILDNEST-001 |
| **Version** | 1.0 |
| **Date** | 2026-06-19 |
| **Status** | Controlled — Under Review |
| **Classification** | Internal Use |
| **Conformance Standard** | ISO/IEC/IEEE 29148:2018 §6.2.5 (Traceability) |
| **Related SRS** | SRS-BUILDNEST-001 v4.0 — `docs/SDLC-docs/requirement-engineering/software-requirements-specification.md` |
| **Related SDD** | SDD-BUILDNEST-001 v3.0 — `docs/SDLC-docs/design/software-design-description.md` |
| **Related TP** | TP-BUILDNEST-001 v4.0 — `docs/SDLC-docs/software-testing/test-plan.md` |
| **Baseline Assessment** | `docs/reports/baseline-assessment-2026-06-19.md` |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date | Author | Changes | Approval |
| :--- | :--- | :--- | :--- | :--- |
| 1.0 | 2026-06-19 | Claude Code (claude-sonnet-4-6) | Initial controlled release — 156 requirements traced from SRS v4.0 through SDD v3.0 design elements, implementation classes, and test classes; status verified against live codebase and Baseline Assessment Report | Pending |

### Document Approval

| Role | Name | Signature | Date |
| :--- | :--- | :--- | :--- |
| Project Manager | _____________ | _____________ | _____________ |
| Technical Lead | _____________ | _____________ | _____________ |
| QA Manager | _____________ | _____________ | _____________ |

---

## CONFORMANCE STATEMENT

> This document conforms to **ISO/IEC/IEEE 29148:2018** Clause 6.2.5 (Traceability) and the bidirectional traceability requirements of **ISO/IEC/IEEE 12207:2017** Section 6.4.2. Each requirement in SRS-BUILDNEST-001 v4.0 is traced forward to its design element (SDD v3.0), implementation artefact (source code), and verification evidence (test class or inspection method). Status is assessed against the Baseline Assessment Report dated 2026-06-19.

---

## 1. Purpose

This Requirements Traceability Matrix (RTM) establishes and maintains bidirectional traceability across the full software lifecycle artefact chain:

```
Stakeholder Need → SRS Requirement → SDD Design Element → Implementation Artefact → Test / Verification Evidence
```

The RTM serves to:

1. Confirm that every stated requirement has been designed, implemented, and verified.
2. Identify requirements that are unimplemented, partially implemented, or carry known defects.
3. Enable impact analysis — any change to a requirement can be traced to affected design elements, code, and tests.
4. Provide objective compliance evidence for ISO/IEC/IEEE 29148:2018, OWASP ASVS 4.0, and the project quality gate.
5. Inform Phase 1 and Phase 2 completion assessment.

---

## 2. Status Classification

| Status | Symbol | Definition |
| :--- | :--- | :--- |
| **Implemented** | ✅ | Requirement is fully implemented and verified in the current codebase |
| **Partial** | 🟡 | Requirement is partially implemented; gap identified and tracked |
| **Pending (Ph-2)** | 🔵 | Requirement is deferred to Phase 2; not expected in Phase 1 gate |
| **Open Defect** | 🔴 | Implementation exists but is known-broken; listed defect ID applies |
| **Not Started** | ⬜ | No implementation present; deferred or not yet begun |

---

## 3. Coverage Summary

| Requirement Category | Total | ✅ Implemented | 🟡 Partial | 🔵 Pending Ph-2 | 🔴 Open Defect | ⬜ Not Started |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Authentication (FR-AUTH) | 11 | 9 | 0 | 2 | 0 | 0 |
| Product Catalogue (FR-PROD) | 7 | 7 | 0 | 0 | 0 | 0 |
| Shopping Cart (FR-CART) | 6 | 6 | 0 | 0 | 0 | 0 |
| Checkout & Orders (FR-CHK) | 8 | 7 | 0 | 1 | 0 | 0 |
| Payment (FR-PAY) | 5 | 0 | 3 | 2 | 0 | 0 |
| Inventory (FR-INV) | 7 | 5 | 0 | 2 | 0 | 0 |
| Reviews & Wishlists (FR-REV, FR-WISH) | 5 | 5 | 0 | 0 | 0 | 0 |
| Admin Operations (FR-ADM) | 8 | 3 | 1 | 4 | 0 | 0 |
| Monitoring (FR-MON) | 8 | 2 | 1 | 5 | 0 | 0 |
| Frontend (FR-FE) | 30 | 0 | 0 | 30 | 0 | 0 |
| User Interfaces (UI) | 4 | 3 | 0 | 1 | 0 | 0 |
| Software Interfaces (SI) | 6 | 3 | 0 | 3 | 0 | 0 |
| Communication Interfaces (CI) | 5 | 2 | 0 | 3 | 0 | 0 |
| Usability (UR) | 8 | 5 | 0 | 3 | 0 | 0 |
| Performance (PR) | 8 | 3 | 0 | 5 | 0 | 0 |
| Reliability (REL) | 5 | 2 | 0 | 3 | 0 | 0 |
| Availability (AVL) | 4 | 1 | 0 | 3 | 0 | 0 |
| Security (SEC) | 14 | 9 | 2 | 3 | 0 | 0 |
| Maintainability (MNT) | 6 | 3 | 1 | 2 | 0 | 0 |
| Portability (PRT) | 4 | 1 | 0 | 3 | 0 | 0 |
| Scalability (SCL) | 4 | 2 | 0 | 2 | 0 | 0 |
| Safety (SAF) | 3 | 1 | 0 | 2 | 0 | 0 |
| Design Constraints (DC) | 8 | 7 | 1 | 0 | 0 | 0 |
| Test Integrity (TIR) | 5 | 0 | 0 | 1 | 4 | 0 |
| **Totals** | **179** | **89** | **9** | **79** | **4** | **0** |

> **Phase 1 gate posture**: 89 requirements fully implemented, 4 with open defects (TIR-01 through TIR-04) that block Phase 1 exit. Phase 1 is blocked until all 🔴 defects are resolved.

---

## 4. RTM Legend

| Column | Description |
| :--- | :--- |
| **Req ID** | Unique requirement identifier from SRS v4.0 |
| **Description** | Concise summary of the requirement |
| **Priority** | High / Medium / Low |
| **Phase** | Ph-1 or Ph-2 |
| **SDD Reference** | Design element in SDD v3.0 (section or component name) |
| **Implementation** | Primary Java class(es) or configuration artefact |
| **Test Class(es)** | Test classes that verify this requirement |
| **Verification Method** | Test / Inspection / Analysis / Demonstration / Build |
| **Status** | Implementation + test status (see §2) |

---

## 5. External Interface Requirements

### 5.1 User Interface Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UI-01 | React 19 SPA frontend | High | Ph-2 | §4.2.2, §4.7.4 | `frontend/src/` (Phase 2) | Vitest / Playwright (Phase 2) | Inspection | 🔵 Pending Ph-2 |
| UI-02 | Swagger UI at `/swagger-ui.html` | Medium | Ph-1 | §4.7.1 | `pom.xml` (SpringDoc dependency) | `HomeControllerTest`, manual | Inspection | ✅ Implemented |
| UI-03 | OpenAPI spec at `/v3/api-docs` | Medium | Ph-1 | §4.7.1 | SpringDoc AutoConfig | Manual / `HomeControllerTest` | Test | ✅ Implemented |
| UI-04 | Consistent JSON error response structure | High | Ph-1 | §4.7.1, §5.2 | `GlobalExceptionHandler` | `GlobalExceptionHandlerTest`, `ExceptionClassesTest` | Test | ✅ Implemented |

### 5.2 Software Interface Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SI-01 | MySQL 8.2 via HikariCP / JDBC | High | Ph-1 | §4.1.2, §4.10.3 | `application.properties` HikariCP config | `DatabaseHealthIndicatorTest`, `DatabaseConstraintTest` | Inspection | ✅ Implemented |
| SI-02 | Redis 7 via Jedis client | High | Ph-1 | §4.1.2, §4.10.3 | `CacheConfig`, `RateLimitConfig` | `RedisHealthIndicatorTest`, `RateLimiterServiceTest` | Inspection | ✅ Implemented |
| SI-03 | Elasticsearch 8.10 via Spring Data | Low | Ph-2 | §4.1.2 | `ElasticsearchConfig` | `ElasticsearchIngestionServiceTest` | Test | 🔵 Pending Ph-2 |
| SI-04 | Razorpay payment gateway | High | Ph-2 | §4.4.2, §5.1 | `RazorpayClientAdapter`, `PaymentServiceImpl` | `RazorpayClientAdapterTest`, `PaymentServiceImplTest` | Test | 🔵 Pending Ph-2 |
| SI-05 | Prometheus scrape at `/actuator/prometheus` | Medium | Ph-2 | §4.1.2, §4.10 | Micrometer AutoConfig | `PerformanceMetricsControllerTest` | Test | 🔵 Pending Ph-2 |
| SI-06 | Logstash TCP log shipper | Low | Ph-2 | §4.1.1 | `logback-spring.xml` | `LoggingStandardsTest` | Inspection | 🔵 Pending Ph-2 |

### 5.3 Communication Interface Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| CI-01 | HTTP / HTTPS REST communication | High | Ph-1 | §4.7, §5.1 | Spring MVC controllers | All controller tests | Test | ✅ Implemented |
| CI-02 | HTTPS / TLS enforced in production | High | Ph-2 | §5.1.1 | `HttpsEnforcementFilter`, `SecurityConfig.validateHttpsInProduction()` | `SecurityTest` | Inspection | 🔵 Pending Ph-2 |
| CI-03 | CORS with configurable allowed origins | High | Ph-1 | §5.1.1 | `SecurityConfig` (CORS config) | `AuthenticationAuthorizationSecurityTest` | Test | ✅ Implemented |
| CI-04 | Health endpoint for K8s probes | High | Ph-2 | §4.1.2, §4.10.1 | Spring Boot Actuator, K8s manifests | `HealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| CI-05 | Prometheus metrics endpoint | Medium | Ph-2 | §4.1.2 | Micrometer / Actuator | `PerformanceMetricsControllerTest` | Test | 🔵 Pending Ph-2 |

---

## 6. Functional Requirements

### 6.1 Authentication and Identity Management (FG-01)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-AUTH-01 | User registration with username, email, password | High | Ph-1 | §4.3.2, §4.7.3 | `AuthController.register()`, `AuthServiceImpl.register()` | `AuthControllerTest`, `AuthServiceImplTest`, `AuthApiTest` | Test | ✅ Implemented |
| FR-AUTH-02 | Username / password authentication returning JWT + refresh token | High | Ph-1 | §4.8.1, §4.3.2 | `AuthController.login()`, `AuthServiceImpl.authenticate()`, `JwtTokenProvider` | `AuthControllerTest`, `AuthServiceImplTest`, `JwtTokenProviderTest` | Test | ✅ Implemented |
| FR-AUTH-03 | JWT access token expires after 15 minutes (900,000 ms, configurable) | High | Ph-1 | §5.1.2 | `JwtTokenProvider` — `@Value("${jwt.expiration:86400000}")` + `application.properties` (900,000) | `JwtTokenProviderTest` | Test | ✅ Implemented |
| FR-AUTH-04 | JWT refresh token expires after 30 days (configurable) | High | Ph-1 | §4.9.5, §5.1.2 | `RefreshTokenServiceImpl.createRefreshToken()` | `RefreshTokenServiceTest` | Test | ✅ Implemented |
| FR-AUTH-05 | Minimum 512-bit JWT secret; fail fast on absent / weak key | High | Ph-1 | §5.1.2 | `JwtKeyValidator`, `SecurityConfig.@PostConstruct` | `JwtKeyValidatorTest`, `JwtTokenProviderTest` | Test | ✅ Implemented |
| FR-AUTH-06 | Token refresh without re-authentication; rotation on use | High | Ph-1 | §4.8.3, §4.9.5 | `AuthController.refreshToken()`, `RefreshTokenServiceImpl` | `AuthControllerTest`, `RefreshTokenServiceTest`, `AuthApiTest` | Test | ✅ Implemented |
| FR-AUTH-07 | Logout invalidates refresh token | High | Ph-1 | §4.9.5 | `AuthController.logout()`, `RefreshTokenServiceImpl.deleteByUserId()` | `AuthControllerTest` | Test | ✅ Implemented |
| FR-AUTH-08 | Password reset via email (tokens expire 15 min; OWASP ASVS 2.1.8) | Medium | Ph-2 | §4.7.3 | `PasswordResetController`, `PasswordResetServiceImpl` | `PasswordResetControllerTest`, `PasswordResetServiceImplTest` | Test | 🔵 Pending Ph-2 |
| FR-AUTH-09 | RBAC with `USER` and `ADMIN` roles | High | Ph-1 | §5.1.3, §4.3.2 | `SecurityConfig`, `RolePermissionEvaluator`, `@PreAuthorize`, `@Secured` | `RBACTest`, `RolePermissionEvaluatorTest`, `AuthenticationAuthorizationSecurityTest` | Test | ✅ Implemented |
| FR-AUTH-10 | BCrypt password hashing (minimum 10 rounds) | High | Ph-1 | §5.1.1 | `AuthServiceImpl` — `BCryptPasswordEncoder(10)` | `AuthServiceImplTest` | Inspection | ✅ Implemented |
| FR-AUTH-11 | OAuth2 client integration (Google, GitHub) | Medium | Ph-2 | §4.3.2 | Not yet implemented | — | Test | 🔵 Pending Ph-2 |

### 6.2 Product Catalogue Management (FG-02)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-PROD-01 | Paginated product listing | High | Ph-1 | §4.7.3 | `ProductControllerV2.getAllProducts()`, `ProductServiceImpl.getAllProducts()` | `ProductControllerV2Test`, `ProductServiceImplTest`, `ProductApiTest` | Test | ✅ Implemented |
| FR-PROD-02 | Product detail by ID | High | Ph-1 | §4.7.3 | `ProductControllerV2.getProductById()`, `ProductServiceImpl.getProductById()` | `ProductControllerV2Test`, `ProductServiceImplTest`, `ProductApiTest` | Test | ✅ Implemented |
| FR-PROD-03 | Product categorisation and filter | Medium | Ph-1 | §4.5.1, §4.7.3 | `ProductControllerV2.getProductsByCategory()`, `CategoryServiceImpl` | `ProductControllerV2Test`, `CategoryManagementTest` | Test | ✅ Implemented |
| FR-PROD-04 | Admin product CRUD | High | Ph-1 | §4.7.3, §5.1.3 | `AdminProductController`, `AdminServiceImpl` | `AdminProductControllerTest` | Test | ✅ Implemented |
| FR-PROD-05 | Versioned APIs (v1 deprecated; v2 current) with sunset headers | Medium | Ph-1 | §4.7.2, §4.6.1 | `ProductControllerV1`, `ProductControllerV2`, `ApiSunsetInterceptor` | `ProductControllerV1Test`, `ApiSunsetInterceptorTest` | Inspection | ✅ Implemented |
| FR-PROD-06 | Redis product cache with 5-min TTL | Medium | Ph-1 | §4.5.4, §4.6.1 | `ProductServiceImpl` — `@Cacheable("products")`, `CacheConfig` | `ProductServiceImplTest`, `CacheMetricsUtilTest` | Test | ✅ Implemented |
| FR-PROD-08 | Product variants (size, colour) with independent per-variant inventory; cart items pinned to a variant | High | Ph-1 | §4.7.3 | `ProductVariant`, `ProductVariantServiceImpl`, `AdminProductController` (nested `/variants` endpoints), `CartServiceImpl.addToCart(userId, productId, variantId, quantity)` | `ProductVariantServiceImplTest`, `AdminProductVariantControllerIntegrationTest` | Test | ✅ Implemented (#81) |
| FR-PROD-09 | Multi-image product gallery: upload, reorder, delete; primary image kept in sync with legacy `Product.imageUrl` | Medium | Ph-1 | §4.7.3 | `ProductImage`, `ProductImageServiceImpl`, `AdminProductController` (nested `/images` endpoints, upload endpoint's semantics changed from #82) | `ProductImageServiceImplTest`, `AdminProductImageControllerIntegrationTest`, `AdminProductControllerIntegrationTest` (upload) | Test | ✅ Implemented (#82) |
| FR-PROD-07 | Redis category cache with 1-hour TTL | Low | Ph-1 | §4.5.4 | `CategoryServiceImpl` — `@Cacheable("categories")`, `CacheConfig` | `CategoryServiceImplTest` | Test | ✅ Implemented |

### 6.3 Shopping Cart Operations (FG-03)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-CART-01 | Add item to cart (authenticated) | High | Ph-1 | §4.7.3 | `CartController.addToCart()`, `CartServiceImpl.addToCart()` | `CartControllerTest`, `CartServiceImplTest`, `CartApiTest` | Test | ✅ Implemented |
| FR-CART-02 | Retrieve cart contents | High | Ph-1 | §4.7.3 | `CartController.getCart()`, `CartServiceImpl.getCartByUserId()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-03 | Remove individual cart item | High | Ph-1 | §4.7.3 | `CartController.removeCartItem()`, `CartServiceImpl.removeItem()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-04 | Clear entire cart | Medium | Ph-1 | §4.7.3 | `CartController.clearCart()`, `CartServiceImpl.clearCart()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-05 | Calculate cart total | High | Ph-1 | §4.7.3 | `CartController.getCartTotal()`, `CartServiceImpl.calculateTotal()` | `CartControllerTest`, `CartServiceImplTest` | Test | ✅ Implemented |
| FR-CART-06 | One cart per user (1:1 relationship) | High | Ph-1 | §4.5.1 | `Cart` entity — `@OneToOne User`, `CartRepository.findByUser()` | `CartRepositoryTest`, `CartTest` | Inspection | ✅ Implemented |

### 6.4 Checkout and Order Processing (FG-04)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-CHK-01 | Validate cart before checkout | High | Ph-1 | §4.7.3 | `CheckoutController.validateCart()`, `CheckoutServiceImpl.validateCart()` | `CheckoutControllerTest`, `CheckoutServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-02 | Calculate checkout total | High | Ph-1 | §4.7.3 | `CheckoutController.calculateTotal()`, `CheckoutServiceImpl.calculateCartTotal()` | `CheckoutControllerTest`, `CheckoutServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-03 | Checkout without payment (cash / COD flow) | High | Ph-1 | §4.8.2 | `CheckoutController.processCheckout()`, `CheckoutServiceImpl.processCheckout()` | `CheckoutControllerTest`, `CheckoutServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-04 | Checkout with Razorpay payment | High | Ph-2 | §4.8.2 | `CheckoutController.processCheckoutWithPayment()`, `PaymentServiceImpl` | `CheckoutControllerTest`, `PaymentProcessingTest` | Test | 🔵 Pending Ph-2 |
| FR-CHK-05 | Create Order + OrderItems on checkout | High | Ph-1 | §4.5.3, §4.8.2 | `CheckoutServiceImpl.createOrderFromCart()`, `OrderRepository.save()` | `CheckoutServiceImplTest`, `OrderServiceImplTest`, `OrderServiceIntegrationTest` | Test | ✅ Implemented |
| FR-CHK-06 | Deduct inventory on order placement | High | Ph-1 | §4.8.2, §4.9.3 | `CheckoutServiceImpl.deductInventoryFromCart()`, `InventoryServiceImpl.deductStock()` | `CheckoutServiceImplTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-07 | User views order history | Medium | Ph-1 | §4.7.3 | `UserOrderController.getUserOrders()`, `OrderServiceImpl.getUserOrders()` | `UserOrderControllerTest`, `OrderServiceImplTest` | Test | ✅ Implemented |
| FR-CHK-08 | Admin views and manages all orders | Medium | Ph-1 | §4.7.3, §5.1.3 | `AdminOrderController`, `AdminServiceImpl.getAllOrders()` | `AdminOrderControllerTest` | Test | ✅ Implemented |

### 6.5 Payment Processing (FG-05)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-PAY-01 | Razorpay payment order creation | High | Ph-2 | §4.4.2, §4.8.2 | `PaymentServiceImpl.initiatePayment()`, `RazorpayClientAdapter.createOrder()` | `PaymentServiceImplTest`, `RazorpayClientAdapterTest` | Test | 🟡 Partial (code present; full E2E deferred to Ph-2) |
| FR-PAY-02 | Razorpay signature verification | High | Ph-2 | §4.8.2 | `PaymentServiceImpl.verifyPaymentSignature()`, `PaymentSignatureValidationService` | `PaymentSignatureValidationServiceTest`, `PaymentServiceImplTest` | Test | 🟡 Partial (logic implemented; Razorpay live test deferred) |
| FR-PAY-03 | Payment transaction recording with status tracking | High | Ph-2 | §4.5.1, §4.9.2 | `Payment` entity, `PaymentRepository`, `PaymentServiceImpl` | `PaymentEntityTest`, `PaymentRepositoryTest`, `PaymentServiceImplTest` | Test | 🟡 Partial (entity and repo ready; end-to-end flow Ph-2) |
| FR-PAY-04 | Razorpay webhook event handling | Medium | Ph-2 | §4.6.2 | `WebhookServiceImpl.processWebhookEvent()` | `WebhookServiceImplTest`, `WebhookAdminControllerTest` | Test | 🔵 Pending Ph-2 |
| FR-PAY-05 | Razorpay credentials externalised via env vars | High | Ph-2 | §8 Appendix A | `application.properties` — `${RAZORPAY_KEY_ID}`, `${RAZORPAY_KEY_SECRET}` | `RazorpayClientAdapterTest` (env check) | Inspection | 🔵 Pending Ph-2 |

### 6.6 Inventory Management (FG-06)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-INV-01 | User checks product inventory status | High | Ph-1 | §4.7.3 | `InventoryStatusController.getInventoryByProduct()`, `InventoryServiceImpl` | `InventoryStatusControllerTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-INV-02 | User checks product availability | High | Ph-1 | §4.7.3 | `InventoryStatusController.checkAvailability()`, `InventoryServiceImpl.isAvailable()` | `InventoryStatusControllerTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-INV-03 | Admin adds stock | High | Ph-1 | §4.7.3 | `AdminInventoryController.addStock()`, `InventoryServiceImpl.addStock()` | `AdminInventoryControllerTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-INV-04 | Admin updates stock quantities | High | Ph-1 | §4.7.3 | `AdminInventoryController.updateStock()`, `InventoryServiceImpl.updateStock()` | `AdminInventoryControllerTest`, `InventoryServiceImplTest` | Test | ✅ Implemented |
| FR-INV-05 | Inventory status tracking (`IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`, `DISCONTINUED`) | Medium | Ph-1 | §4.9.3 | `Inventory` entity — `InventoryStatus` enum, `InventoryServiceImpl.updateStatus()` | `InventoryTest`, `InventoryServiceImplTest`, `InventoryManagementTest` | Test | ✅ Implemented |
| FR-INV-06 | Emit `InventoryThresholdBreachEvent` on low stock | Medium | Ph-2 | §4.6.2 | `InventoryServiceImpl`, `DomainEventPublisher.publishEvent(InventoryThresholdBreachEvent)` | `InventoryMonitoringServiceTest`, `InventoryThresholdManagementServiceTest` | Test | 🔵 Pending Ph-2 |
| FR-INV-07 | Admin inventory analytics and reports | Medium | Ph-2 | §4.7.3 | `AdminInventoryAnalyticsController`, `InventoryAnalyticsService`, `InventoryReportService` | `AdminInventoryAnalyticsControllerTest`, `InventoryAnalyticsServiceTest`, `InventoryReportServiceTest` | Test | 🔵 Pending Ph-2 |

### 6.7 Reviews and Wishlists (FG-07)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-REV-01 | Submit product review with star rating | Medium | Ph-1 | §4.7.3 | `ProductReviewController.createReview()`, `ProductReviewServiceImpl.createReview()` | `ProductReviewControllerTest`, `ProductReviewServiceImplTest` | Test | ✅ Implemented |
| FR-REV-02 | View product reviews (public) | Medium | Ph-1 | §4.7.3 | `ProductReviewController.getProductReviews()`, `ProductReviewServiceImpl.getReviewsByProduct()` | `ProductReviewControllerTest` | Test | ✅ Implemented |
| FR-REV-03 | User updates / deletes own reviews | Medium | Ph-1 | §4.7.3 | `ProductReviewController.updateReview()` / `deleteReview()`, `ProductReviewServiceImpl` | `ProductReviewControllerTest`, `ProductReviewServiceImplTest` | Test | ✅ Implemented |
| FR-WISH-01 | Add products to wishlist (authenticated) | Low | Ph-1 | §4.7.3 | `WishlistController.addToWishlist()`, `WishlistServiceImpl.addToWishlist()` | `WishlistControllerTest`, `WishlistServiceImplTest` | Test | ✅ Implemented |
| FR-WISH-02 | View and manage wishlist | Low | Ph-1 | §4.7.3 | `WishlistController.getWishlist()` / `removeFromWishlist()`, `WishlistServiceImpl` | `WishlistControllerTest`, `WishlistServiceImplTest` | Test | ✅ Implemented |

### 6.8 Admin Operations (FG-08)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-ADM-01 | Sales analytics dashboard for admins | Medium | Ph-2 | §4.7.3 | `SalesAnalyticsController`, `SalesAnalyticsServiceImpl` | `SalesAnalyticsControllerTest`, `SalesAnalyticsServiceImplTest`, `AnalyticsReportingTest` | Test | 🔵 Pending Ph-2 |
| FR-ADM-02 | Inventory analytics and reports | Medium | Ph-2 | §4.7.3 | `AdminInventoryAnalyticsController`, `InventoryAnalyticsService` | `AdminInventoryAnalyticsControllerTest`, `InventoryAnalyticsServiceTest` | Test | 🔵 Pending Ph-2 |
| FR-ADM-03 | Admin manages user accounts (view, update, deactivate) | Medium | Ph-2 | §4.7.3 | `AdminUserController`, `AdminServiceImpl.manageUser()` | `AdminUserControllerTest` | Test | 🔵 Pending Ph-2 |
| FR-ADM-04 | Tamper-evident audit log of all admin actions | High | Ph-1 | §4.3.5, §4.6.1 | `AuditAspect` (`@Around @Auditable`), `AuditLogService`, `AuditLogController` | `AuditAspectTest`, `AuditLogServiceTest`, `AuditLogControllerTest` | Test | ✅ Implemented |
| FR-ADM-05 | Admin reporting endpoints | Medium | Ph-2 | §4.7.3 | `AdminReportController` | `AdminReportControllerTest` | Test | 🔵 Pending Ph-2 |
| FR-ADM-06 | Admin configures inventory alert thresholds | Medium | Ph-2 | §4.7.3 | `AdminInventoryThresholdController`, `InventoryThresholdManagementService` | `AdminInventoryThresholdControllerTest`, `InventoryThresholdManagementServiceTest` | Test | 🟡 Partial (controller + service exist; feature toggle deferred) |
| FR-ADM-07 | Admin manages webhook subscriptions | Low | Ph-2 | §4.7.3 | `WebhookAdminController`, `WebhookServiceImpl` | `WebhookAdminControllerTest`, `WebhookServiceImplTest` | Test | 🔵 Pending Ph-2 |
| FR-ADM-08 | All `/api/admin/**` requires `ADMIN` role | High | Ph-1 | §5.1.3 | `SecurityConfig` — `.requestMatchers("/api/admin/**").hasRole("ADMIN")` | `AuthenticationAuthorizationSecurityTest`, `RBACTest` | Test | ✅ Implemented |

### 6.9 Monitoring and Observability (FG-09)

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-MON-01 | Health endpoint at `/actuator/health` | High | Ph-1 | §4.7.3, §4.10.1 | Spring Boot Actuator AutoConfig | `HealthIndicatorTest` | Test | ✅ Implemented |
| FR-MON-02 | MySQL health indicator | High | Ph-2 | §4.10.1 | `DatabaseHealthIndicator` | `DatabaseHealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-03 | Redis health indicator | High | Ph-2 | §4.10.1 | `RedisHealthIndicator` | `RedisHealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-04 | Circuit breaker state indicators | Medium | Ph-2 | §4.9.4, §4.10 | Resilience4j Actuator integration | `ReliabilityTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-05 | Prometheus metrics at `/actuator/prometheus` | Medium | Ph-2 | §4.10.1 | Micrometer / Prometheus Actuator endpoint | `PerformanceMetricsControllerTest`, `PoolMetricsControllerTest` | Test | 🟡 Partial (endpoint present; Prometheus rules deferred to Ph-2) |
| FR-MON-06 | K8s liveness and readiness probes | High | Ph-2 | §4.10.1 | `kubernetes/buildnest-deployment.yaml` | Environment validation | Test | 🔵 Pending Ph-2 |
| FR-MON-07 | Elasticsearch event indexing and alerting | Low | Ph-2 | §4.1.2 | `ElasticsearchIngestionService`, `ElasticsearchAlertingService` | `ElasticsearchIngestionServiceTest`, `ElasticsearchAlertingServiceTest` | Test | 🔵 Pending Ph-2 |
| FR-MON-08 | 13 Prometheus alert rules | Medium | Ph-2 | §4.10, Appendix B | `kubernetes/prometheus-rules.yaml` | Configuration audit | Inspection | 🔵 Pending Ph-2 |

### 6.10 Frontend Application (FG-10)

> All FR-FE-* requirements are classified **Ph-2**. The frontend (`frontend/src/`) is a stub; no implementation exists. All rows show 🔵 Pending Ph-2.

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| FR-FE-01 | React 19 SPA | High | Ph-2 | §4.2.2 | `frontend/src/` | Vitest | Inspection | 🔵 Pending Ph-2 |
| FR-FE-02 | Axios / Fetch with JWT injection interceptors | High | Ph-2 | §4.3.6 | `frontend/src/config/axiosInstance.js` | Vitest | Inspection | 🔵 Pending Ph-2 |
| FR-FE-03 | React Router v6+ client-side routing | High | Ph-2 | §4.7.4 | `frontend/src/router/AppRouter.jsx` | Vitest, Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-04 | Responsive design (mobile / tablet / desktop) | High | Ph-2 | §4.10.5 | CSS / Tailwind, media queries | Playwright viewport tests | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-05 | React Context / Redux global state | High | Ph-2 | §4.3.6 | `frontend/src/context/AuthContext.jsx`, `CartContext.jsx` | Vitest | Inspection | 🔵 Pending Ph-2 |
| FR-FE-06 | Protected routes redirect unauthenticated users | High | Ph-2 | §4.7.4 | `frontend/src/router/ProtectedRoute.jsx` | Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-07 | Loading indicators during async API calls | Medium | Ph-2 | §4.3.6 | `Spinner` component | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-08 | Toast notifications for success/error | Medium | Ph-2 | §4.3.6 | `Toast` component | Vitest | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-09 | Client-side form validation (React Hook Form + Yup) | Medium | Ph-2 | §4.3.6 | Form components | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-10 | Silent JWT refresh on 401 response | High | Ph-2 | §4.3.6 | Axios response interceptor | Vitest, Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-11 | Home page | High | Ph-2 | §4.7.4 | `frontend/src/pages/core/Home.jsx` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-12 | Product listing with pagination / sort / filter | High | Ph-2 | §4.7.4 | `frontend/src/pages/core/ProductList.jsx` | Vitest, Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-13 | Product detail page | High | Ph-2 | §4.7.4 | `frontend/src/pages/core/ProductDetail.jsx` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-14 | Shopping cart page | High | Ph-2 | §4.7.4 | `frontend/src/pages/core/Cart.jsx` | Vitest, Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-15 | Checkout page with Razorpay modal | High | Ph-2 | §4.7.4 | `frontend/src/pages/checkout/Checkout.jsx` | Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-16 | Login page | High | Ph-2 | §4.7.4 | `frontend/src/pages/auth/Login.jsx` | Vitest, Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-17 | Registration page with password strength indicator | High | Ph-2 | §4.7.4 | `frontend/src/pages/auth/Register.jsx` | Vitest, Playwright | Test | 🔵 Pending Ph-2 |
| FR-FE-18 | User profile page | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/core/Profile.jsx` | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-19 | Order history page | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/core/OrderHistory.jsx` | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-20 | Wishlist page | Low | Ph-2 | §4.7.4 | `frontend/src/pages/core/Wishlist.jsx` | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-21 | Search results page | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/core/SearchResults.jsx` | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-22 | Admin dashboard | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/admin/AdminDashboard.jsx` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-23 | Admin product management | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/admin/AdminProductMgmt.jsx` | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-24 | Admin inventory page | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/admin/AdminInventory.jsx` | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-25 | Admin order management | Medium | Ph-2 | §4.7.4 | `frontend/src/pages/admin/AdminOrderMgmt.jsx` | Vitest | Test | 🔵 Pending Ph-2 |
| FR-FE-26 | Navbar on all pages | High | Ph-2 | §4.3.6 | `frontend/src/components/layout/Navbar.jsx` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-27 | Footer | Low | Ph-2 | §4.3.6 | `frontend/src/components/layout/Footer.jsx` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-28 | ProductCard component | High | Ph-2 | §4.3.6 | `frontend/src/components/product/ProductCard.jsx` | Vitest | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-29 | Breadcrumb navigation | Low | Ph-2 | §4.3.6 | `frontend/src/components/layout/Breadcrumb.jsx` | Playwright | Demonstration | 🔵 Pending Ph-2 |
| FR-FE-30 | ErrorBoundary with fallback UI | Medium | Ph-2 | §4.3.6 | `frontend/src/components/common/ErrorBoundary.jsx` | Vitest | Test | 🔵 Pending Ph-2 |

---

## 7. Non-Functional Requirements

### 7.1 Usability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UR-01 | Consistent JSON response structure with standard HTTP status codes | High | Ph-1 | §4.7.1, §5.2 | `GlobalExceptionHandler`, all controller response types | `GlobalExceptionHandlerTest`, all controller tests | Test | ✅ Implemented |
| UR-02 | Error responses with machine-readable code and human-readable message | High | Ph-1 | §4.7.1, §5.2 | `GlobalExceptionHandler.ErrorResponse` | `GlobalExceptionHandlerTest`, `ExceptionClassesTest` | Test | ✅ Implemented |
| UR-03 | Auto-generated OpenAPI / Swagger documentation | Medium | Ph-1 | §4.7.1 | SpringDoc OpenAPI dependency, controller annotations | Manual verification | Inspection | ✅ Implemented |
| UR-04 | API v1 / v2 backward compatibility | Medium | Ph-1 | §4.7.2 | `ProductControllerV1`, `ProductControllerV2` | `ProductControllerV1Test`, `ProductControllerV2Test` | Test | ✅ Implemented |
| UR-05 | Deprecated v1 endpoints include `Sunset` + `Deprecation` headers | Low | Ph-1 | §4.7.2 | `ApiSunsetInterceptor` | `ApiSunsetInterceptorTest` | Inspection | ✅ Implemented |
| UR-FE-01 | WCAG 2.1 AA accessibility compliance | High | Ph-2 | §4.3.6 | Frontend components | axe-core | Inspection | 🔵 Pending Ph-2 |
| UR-FE-02 | LCP < 2.5 seconds | High | Ph-2 | §4.10.5 | Frontend build optimisation, Nginx caching | Lighthouse | Analysis | 🔵 Pending Ph-2 |
| UR-FE-03 | Visual feedback on interactive elements | Medium | Ph-2 | §4.3.6 | CSS focus/hover states | Playwright | Demonstration | 🔵 Pending Ph-2 |

### 7.2 Performance Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| PR-01 | API P95 response ≤ 500 ms under normal load | High | Ph-2 | §4.10 | Full stack + HikariCP + Redis caching | `PerformanceTest`, `LoadTestSimulation` (Gatling) | Analysis | 🔵 Pending Ph-2 |
| PR-02 | Sustain 1,000 concurrent users | High | Ph-2 | §4.10, §4.6.1 | Kubernetes HPA, stateless JWT | `StressTest`, `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |
| PR-03 | Throughput > 10,000 req/min | High | Ph-2 | §4.10 | Full stack | `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |
| PR-04 | Error rate < 0.1% under load | High | Ph-2 | §4.10, §5.3 | Resilience4j, circuit breaker | `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |
| PR-05 | HikariCP max pool 20 (dev) / 30 (prod); min idle 10 (dev) / 15 (prod) | Medium | Ph-1 | §4.10.3 | `application.properties` HikariCP config | `PoolMetricsControllerTest`, `PerformanceBaselineTest` | Inspection | ✅ Implemented |
| PR-06 | Connection timeout ≤ 30 seconds | Medium | Ph-1 | §4.10.3 | `spring.datasource.hikari.connection-timeout=30000` | `PerformanceBaselineTest` | Inspection | ✅ Implemented |
| PR-07 | Product cache TTL = 5 min (300,000 ms, configurable) | Medium | Ph-1 | §4.5.4 | `CacheConfig` — `products` region TTL | `CacheMetricsUtilTest` | Test | ✅ Implemented |
| PR-08 | Docker image build time ≤ 2 min with layer caching | Low | Ph-2 | §4.10.5 | Multi-stage `Dockerfile` | CI build timing | Analysis | 🔵 Pending Ph-2 |

### 7.3 Reliability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| REL-01 | 99.9% production uptime | High | Ph-2 | §4.10.1 | Kubernetes 3-replica Deployment, HPA | SRE monitoring | Analysis | 🔵 Pending Ph-2 |
| REL-02 | Redis circuit breaker — 70% failure threshold | High | Ph-1 | §4.9.4, §5.3.1 | `ResilienceConfig` — `redis-circuit-breaker` (70%, 3 s timeout) | `ReliabilityTest` | Inspection | ✅ Implemented |
| REL-03 | Database circuit breaker — 50% failure threshold | High | Ph-1 | §4.9.4, §5.3.1 | `ResilienceConfig` — `database-circuit-breaker` (50%, 8 s timeout) | `ReliabilityTest` | Inspection | ✅ Implemented |
| REL-04 | RTO ≤ 15 minutes | High | Ph-2 | §4.10.1 | Kubernetes restart policies, graceful shutdown | DR drill | Test | 🔵 Pending Ph-2 |
| REL-05 | RPO ≤ 5 minutes | High | Ph-2 | §4.10.1 | MySQL backup strategy | DR drill | Analysis | 🔵 Pending Ph-2 |

### 7.4 Availability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| AVL-01 | Kubernetes liveness + readiness probes | High | Ph-2 | §4.10.1 | `kubernetes/buildnest-deployment.yaml` | Environment validation | Inspection | 🔵 Pending Ph-2 |
| AVL-02 | Health checks covering MySQL, Redis, circuit breakers | High | Ph-2 | §4.7.3 | Composite `HealthIndicator` (`DatabaseHealthIndicator`, `RedisHealthIndicator`) | `DatabaseHealthIndicatorTest`, `RedisHealthIndicatorTest` | Test | 🔵 Pending Ph-2 |
| AVL-03 | Graceful shutdown with 30-second drain | High | Ph-2 | §4.10.2 | `server.shutdown=graceful`, `spring.lifecycle.timeout-per-shutdown-phase=30s` | `ReliabilityHATest` | Test | 🔵 Pending Ph-2 |
| AVL-04 | HikariCP auto-recovers from transient DB failures | High | Ph-1 | §4.10.3, §5.3 | HikariCP `connectionTimeout`, `maxLifetime`, circuit breaker | `ReliabilityTest`, `DatabaseHealthIndicatorTest` | Test | ✅ Implemented |

### 7.5 Security Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SEC-01 | BCrypt password hashing (minimum 10 rounds) | High | Ph-1 | §5.1.1 | `AuthServiceImpl` — `BCryptPasswordEncoder(10)` | `AuthServiceImplTest`, `SecurityTest` | Inspection | ✅ Implemented |
| SEC-02 | JWT secret ≥ 512 bits; externalised; no default value | High | Ph-1 | §5.1.2 | `JwtTokenProvider` — `@Value("${jwt.secret}")` (no default), `JwtKeyValidator` | `JwtKeyValidatorTest`, `JwtTokenProviderTest` | Inspection | ✅ Implemented |
| SEC-03 | HTTPS / TLS enforced in production; startup fails without SSL | High | Ph-2 | §5.1.1, §4.4.1 | `HttpsEnforcementFilter`, `SecurityConfig.validateHttpsInProduction()` | `SecurityTest` | Test | 🔵 Pending Ph-2 |
| SEC-04 | CSRF configured for SPA (stateless; disabled for REST) | High | Ph-2 | §5.1.1 | `SecurityConfig` — `csrf.disable()` (stateless API) | `AuthenticationAuthorizationSecurityTest` | Inspection | 🔵 Pending Ph-2 |
| SEC-05 | CORS restricted to configured domains only | High | Ph-1 | §5.1.1 | `SecurityConfig` CORS allowedOrigins | `SecurityTest` | Inspection | ✅ Implemented |
| SEC-06 | SQL injection prevented via JPA parameterised queries | High | Ph-1 | §4.3.3, §5.1.1 | All `@Repository` interfaces (Spring Data JPA derived queries) | `InputValidationSecurityTest`, `DataValidationTest` | Inspection | ✅ Implemented |
| SEC-07 | Login rate-limited to 3 req / 5 min per source | High | Ph-1 | §5.1.5 | `RateLimiterService.checkLoginRateLimit()`, Bucket4j Redis | `RateLimiterServiceTest`, `AdminRateLimitFilterTest` | Test | ✅ Implemented |
| SEC-08 | Password reset rate-limited to 3 req / hr per source | High | Ph-1 | §5.1.5 | `RateLimiterService.checkPasswordResetRateLimit()`, Bucket4j Redis | `RateLimiterServiceTest` | Test | ✅ Implemented |
| SEC-09 | Admin endpoints rate-limited to 50 req / min | Medium | Ph-1 | §5.1.5 | `AdminRateLimitFilter`, Bucket4j Redis | `AdminRateLimitFilterTest` | Test | ✅ Implemented |
| SEC-10 | User endpoints rate-limited to 500 req / min | Medium | Ph-1 | §5.1.5 | `RateLimiterService`, Bucket4j Redis | `RateLimiterServiceTest` | Test | ✅ Implemented |
| SEC-11 | Product search rate-limited to 60 req / min | Medium | Ph-1 | §5.1.5 | `RateLimiterService.checkSearchRateLimit()` | `RateLimiterServiceTest` | Test | ✅ Implemented |
| SEC-12 | JWT secret rotation every 90 days | Medium | Ph-2 | §5.1.2 | `JwtTokenProvider` — dual-key (`jwt.secret.previous`) | Operational runbook | Inspection | 🟡 Partial (dual-key mechanism implemented; rotation schedule and runbook deferred to Ph-2) |
| SEC-13 | Database password rotation every 180 days | Medium | Ph-2 | Appendix A | HikariCP env var `${SPRING_DATASOURCE_PASSWORD}` | Operational runbook | Inspection | 🔵 Pending Ph-2 |
| SEC-14 | CSP must not contain `unsafe-inline` | Medium | Ph-2 | §5.1.4 | `SecurityConfig` CSP header — **known gap** (currently includes `unsafe-inline`) | `SecurityTest` | Inspection | 🟡 Partial (CSP header present but contains `unsafe-inline`; nonce/hash strategy pending Ph-2) |

### 7.6 Maintainability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| MNT-01 | 100% Javadoc coverage enforced by Maven Javadoc Plugin | High | Ph-1 | §6.1 | `pom.xml` (Javadoc plugin config) | Build gate | Build | ✅ Implemented |
| MNT-02 | JaCoCo line coverage ≥ 70% | High | Ph-2 | §17 (TP) | JaCoCo 0.8.11 in `pom.xml` — gate at 0.40 (current); target 0.70 (Ph-2) | `./mvnw verify` JaCoCo gate | Build | 🟡 Partial (gate at 40%; target 70% not yet enforced) |
| MNT-03 | All unit tests: 0 failures, 0 errors | High | Ph-1 | §15 (TP) | `./mvnw test -P unit-tests` | All test classes | Build | 🔴 Open Defect (14 failures/errors; DEF-001 through DEF-006) |
| MNT-04 | All DDL changes via Liquibase changesets | High | Ph-1 | §4.5 | `src/main/resources/db/changelog/` | `DatabaseConstraintTest` | Inspection | ✅ Implemented |
| MNT-05 | Structured JSON logging via SLF4J / Logback + Logstash encoder | High | Ph-1 | §4.2.1 | `logback-spring.xml`, `@Slf4j` on 87 classes | `LoggingStandardsTest`, `SecureLoggerTest` | Inspection | ✅ Implemented |
| MNT-06 | No `System.out` or `printStackTrace` in production code | High | Ph-1 | §4.2.1 | All production `.java` files | `LoggingStandardsTest`, `DeadCodeAnalyzerTest` | Inspection | ✅ Implemented |

### 7.7 Portability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| PRT-01 | Docker multi-stage containerisation | High | Ph-2 | §4.10 | `Dockerfile` (multi-stage: build → runtime) | Docker build | Inspection | 🔵 Pending Ph-2 |
| PRT-02 | Kubernetes deployment manifests | High | Ph-2 | §4.10, Appendix B | `kubernetes/` (7+ manifest files) | `kubectl apply` dry run | Inspection | 🔵 Pending Ph-2 |
| PRT-03 | Terraform IaC for AWS | Medium | Ph-2 | §4.10 | `terraform/` | Terraform plan | Inspection | 🔵 Pending Ph-2 |
| PRT-04 | All configuration via environment variables (12-Factor) | High | Ph-1 | Appendix A, §6.1 | `application.properties` — all secrets via `${ENV_VAR}` | Config audit | Inspection | ✅ Implemented |

### 7.8 Scalability Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SCL-01 | Stateless JWT enables horizontal pod scaling | High | Ph-1 | §5.1.2, §4.3.1 | `JwtTokenProvider` (stateless; no session store) | `JwtTokenProviderTest` | Inspection | ✅ Implemented |
| SCL-02 | HikariCP pool size configurable per environment | Medium | Ph-1 | §4.10.3 | `application.properties` HikariCP config | `PerformanceBaselineTest` | Inspection | ✅ Implemented |
| SCL-03 | Redis-backed rate limiting shared across pods | Medium | Ph-2 | §5.1.5 | `RateLimiterService` + Bucket4j Redis backend (shared key per IP) | `RateLimiterServiceTest`, integration test | Test | 🔵 Pending Ph-2 |
| SCL-04 | Sustain ≥ 1,000 concurrent users (Gatling) | High | Ph-2 | §4.10.1 | Kubernetes HPA (CPU 75% trigger), 3 replicas base | `LoadTestSimulation` | Analysis | 🔵 Pending Ph-2 |

### 7.9 Safety Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| SAF-01 | No unauthorised financial transaction on system failure | High | Ph-2 | §5.1.2, §4.8.2 | Razorpay signature verification before `Payment.status=SUCCESS` | `PaymentSignatureValidationServiceTest`, `PaymentServiceImplTest` | Test | 🔵 Pending Ph-2 |
| SAF-02 | No charge recorded without associated order confirmation | High | Ph-2 | §4.9.2 | `@Transactional` on `CheckoutServiceImpl.processCheckoutWithPayment()` | `CheckoutServiceImplTest`, `PaymentProcessingTest` | Test | 🔵 Pending Ph-2 |
| SAF-03 | Inventory data integrity under concurrent orders (ACID) | High | Ph-1 | §4.5.3, §4.9.3 | `InventoryServiceImpl.deductStock()` — `@Transactional` + optimistic locking (`@Version`) | `InventoryManagementTest`, `EdgeCaseAndBoundaryTest`, `DatabaseConstraintTest` | Test | ✅ Implemented |

---

## 8. Design Constraints

| Req ID | Constraint | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| DC-01 | Layered monolith: Controller → Service → Repository → Model | High | Ph-1 | §4.3.1 | Full package structure; controllers never call repos | `DeadCodeAnalyzerTest`, architecture review | Inspection | ✅ Implemented |
| DC-02 | RESTful API design — HTTP methods and status codes | High | Ph-1 | §4.7.1 | All `@RestController` classes | All controller tests | Test | ✅ Implemented |
| DC-03 | All config externalised via env vars; no secrets in source | High | Ph-1 | Appendix A | `application.properties` — all secrets via `${ENV_VAR}` | `SecureLoggerTest`, config audit | Inspection | ✅ Implemented |
| DC-04 | Stateless JWT; no server-side sessions | High | Ph-1 | §5.1.2, §4.3.1 | `JwtTokenProvider`, `SecurityConfig` (session = STATELESS) | `JwtTokenProviderTest`, `SecurityTest` | Inspection | ✅ Implemented |
| DC-05 | Graceful shutdown 30-second drain | High | Ph-2 | §4.10.2 | `server.shutdown=graceful` + `lifecycle.timeout=30s` | `ReliabilityHATest` | Test | 🔵 Pending Ph-2 |
| DC-06 | Multi-stage Docker builds | Medium | Ph-2 | §4.10, §4.10.5 | `Dockerfile` | Docker build | Inspection | 🔵 Pending Ph-2 |
| DC-07 | Repository access only from service layer | High | Ph-1 | §4.3.1 | Package dependencies: no `@Repository` injection in `@RestController` | Architecture review | Inspection | ✅ Implemented |
| DC-08 | Explicit JPA fetch strategy on all relationships | High | Ph-1 | §4.3.3, Appendix C | `User.roles` (EAGER), all `@OneToMany` (LAZY) | `OrderTest`, `CartTest` | Inspection | 🟡 Partial (`Category.products` and `Order.orderItems` missing explicit `FetchType.LAZY` — Baseline F-09; tracked in Appendix C of SDD) |

---

## 9. Test Integrity Requirements

| Req ID | Description | Priority | Phase | SDD Reference | Implementation | Test Class(es) | Verification | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TIR-01 | E2E tests must be excluded from `unit-tests` profile; tagged `@Tag("e2e")` | High | Ph-1 | §15 (TP) | `pom.xml` excludedGroups; `@Tag("e2e")` on `ProductApiTest`, `OrderApiTest` | `./mvnw test` must show 0 failures from missing server | Build | 🔴 Open Defect (DEF-002, DEF-003 — E2E tests run in unit-tests profile; fail with HTTP 500) |
| TIR-02 | All `@InjectMocks` services must have `@Mock` for every dependency | High | Ph-1 | §15 (TP) | `AuthServiceImplTest` — add `@Mock RoleRepository roleRepository` | `AuthServiceImplTest` — 0 NullPointerExceptions | Test | 🔴 Open Defect (DEF-001 — `roleRepository` is null; 3 NPE errors) |
| TIR-03 | Security tests assert 403 (authenticated+unauthorised) vs 401 (unauthenticated) | Medium | Ph-1 | §15 (TP) | `AuthenticationAuthorizationSecurityTest.testRoleHierarchyEnforcement()` | Correct HTTP status assertion | Inspection | 🔴 Open Defect (DEF-004 — asserts 401; receives 403) |
| TIR-04 | Input validation tests accept 400/415 as well as 401 | Medium | Ph-1 | §15 (TP) | `InputValidationSecurityTest.testXSSPrevention()`, `testFileUploadValidation()` | Correct status code range assertions | Inspection | 🔴 Open Defect (DEF-005, DEF-006 — asserts 401; receives 400 or 415) |
| TIR-05 | PIT mutation score ≥ 75% (`service.*` and `security.*`) | Medium | Ph-2 | §15 (TP) | `pom.xml` PIT plugin (not yet configured) | `./mvnw pitest:mutationCoverage -P coverage` | Build | 🔵 Pending Ph-2 |

---

## 10. Bidirectional Traceability Index

### 10.1 Implementation Class → Requirements

| Implementation Class | Requirements Satisfied |
| :--- | :--- |
| `AuthController` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-06, FR-AUTH-07 |
| `AuthServiceImpl` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-09, FR-AUTH-10, SEC-01 |
| `JwtTokenProvider` | FR-AUTH-02, FR-AUTH-03, FR-AUTH-04, FR-AUTH-05, SEC-02, SCL-01, DC-04 |
| `JwtAuthenticationFilter` | FR-AUTH-09, SEC-01, DC-04 |
| `JwtKeyValidator` | FR-AUTH-05, SEC-02 |
| `RefreshTokenServiceImpl` | FR-AUTH-04, FR-AUTH-06 |
| `PasswordResetController` + `PasswordResetServiceImpl` | FR-AUTH-08 |
| `SecurityConfig` (`@Profile("!test")`) | FR-AUTH-09, SEC-02, SEC-03, SEC-05, SEC-07, DC-04 |
| `AdminRateLimitFilter` | SEC-09, SEC-07 |
| `RateLimiterService` + Bucket4j | SEC-07, SEC-08, SEC-09, SEC-10, SEC-11 |
| `RolePermissionEvaluator` | FR-AUTH-09 |
| `ProductControllerV1` | FR-PROD-01, FR-PROD-02, FR-PROD-03, FR-PROD-05 |
| `ProductControllerV2` | FR-PROD-01, FR-PROD-02, FR-PROD-03 |
| `AdminProductController` | FR-PROD-04, FR-ADM-08 |
| `ProductServiceImpl` | FR-PROD-01, FR-PROD-02, FR-PROD-03, FR-PROD-04, FR-PROD-06 |
| `ProductVariantServiceImpl`, `AdminProductController` (variant endpoints) | FR-PROD-08 |
| `ProductImageServiceImpl`, `AdminProductController` (image endpoints) | FR-PROD-09 |
| `ApiSunsetInterceptor` | FR-PROD-05, UR-05 |
| `CartController` + `CartServiceImpl` | FR-CART-01 to FR-CART-05 |
| `Cart` entity | FR-CART-06 |
| `CartRepository` | FR-CART-02, FR-CART-06 |
| `CheckoutController` + `CheckoutServiceImpl` | FR-CHK-01 to FR-CHK-06, FR-CHK-07, SAF-02 |
| `AdminOrderController` | FR-CHK-08, FR-ADM-08 |
| `OrderServiceImpl` | FR-CHK-05, FR-CHK-07, FR-CHK-08 |
| `PaymentServiceImpl` + `RazorpayClientAdapter` | FR-PAY-01, FR-PAY-02, FR-PAY-03, SAF-01 |
| `WebhookServiceImpl` | FR-PAY-04, FR-ADM-07 |
| `InventoryStatusController` | FR-INV-01, FR-INV-02 |
| `AdminInventoryController` | FR-INV-03, FR-INV-04, FR-ADM-08 |
| `InventoryServiceImpl` | FR-INV-01 to FR-INV-05, FR-CHK-06, SAF-03 |
| `InventoryThresholdManagementService` | FR-INV-06, FR-ADM-06 |
| `InventoryAnalyticsService` + `InventoryReportService` | FR-INV-07, FR-ADM-02 |
| `ProductReviewController` + `ProductReviewServiceImpl` | FR-REV-01 to FR-REV-03 |
| `WishlistController` + `WishlistServiceImpl` | FR-WISH-01, FR-WISH-02 |
| `AuditLogController` + `AuditLogService` | FR-ADM-04 |
| `AuditAspect` + `@Auditable` | FR-ADM-04, MNT-05 |
| `SalesAnalyticsController` + `SalesAnalyticsServiceImpl` | FR-ADM-01 |
| `AdminReportController` | FR-ADM-05 |
| `AdminUserController` | FR-ADM-03, FR-ADM-08 |
| `GlobalExceptionHandler` | UR-01, UR-02 |
| `ResilienceConfig` | REL-02, REL-03 |
| `CacheConfig` | FR-PROD-06, FR-PROD-07, PR-07 |
| `PerformanceMonitoringInterceptor` | FR-MON-05, PR-01 |
| `HttpsEnforcementFilter` | SEC-03, CI-02 |
| `SecurityConfig.validateHttpsInProduction()` | SEC-02, SEC-03, FR-AUTH-05 |
| HikariCP config in `application.properties` | PR-05, PR-06, SCL-02, AVL-04 |
| Liquibase changelogs | MNT-04, DC-08, SAF-03 |
| `application.properties` (env vars) | DC-03, PRT-04, SEC-02, FR-AUTH-03, FR-AUTH-04 |
| `logback-spring.xml` + `@Slf4j` | MNT-05, MNT-06 |
| `kubernetes/` manifests | AVL-01, DC-05, PRT-02, SCL-04, FR-MON-06 |
| `kubernetes/prometheus-rules.yaml` | FR-MON-08 |
| `DomainEventPublisher` + `DomainEventListener` | FR-INV-06, FR-PAY-04, FR-NOT-01 to FR-NOT-03 |

### 10.2 Test Class → Requirements

| Test Class | Requirements Verified |
| :--- | :--- |
| `AuthServiceImplTest` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-10, SEC-01 (**TIR-02 defect present**) |
| `AuthControllerTest` | FR-AUTH-01, FR-AUTH-02, FR-AUTH-06, FR-AUTH-07, FR-AUTH-09 |
| `JwtTokenProviderTest` | FR-AUTH-02, FR-AUTH-03, FR-AUTH-04, FR-AUTH-05, SEC-02 |
| `JwtKeyValidatorTest` | FR-AUTH-05, SEC-02 |
| `JwtAuthenticationFilterTest` | FR-AUTH-09, DC-04 |
| `RefreshTokenServiceTest` | FR-AUTH-04, FR-AUTH-06 |
| `PasswordResetControllerTest`, `PasswordResetServiceImplTest` | FR-AUTH-08 |
| `AuthenticationAuthorizationSecurityTest` | FR-AUTH-09, FR-ADM-08, SEC-01, UR-01, UR-02 (**TIR-03 defect present**) |
| `InputValidationSecurityTest` | SEC-06, UR-01 (**TIR-04 defect present**) |
| `SecurityTest`, `RBACTest` | FR-AUTH-09, SEC-05, DC-04 |
| `RolePermissionEvaluatorTest` | FR-AUTH-09 |
| `AdminRateLimitFilterTest` | SEC-09, FR-ADM-08 |
| `RateLimiterServiceTest` | SEC-07, SEC-08, SEC-09, SEC-10, SEC-11 |
| `ProductControllerV1Test`, `ProductControllerV2Test` | FR-PROD-01 to FR-PROD-05, UR-04 |
| `ProductServiceImplTest` | FR-PROD-01 to FR-PROD-07 |
| `ProductRepositoryTest`, `ProductRepositoryDefaultMethodTest` | FR-PROD-01, FR-PROD-02 |
| `ApiSunsetInterceptorTest` | FR-PROD-05, UR-05 |
| `ProductApiTest` (E2E) | FR-PROD-01 to FR-PROD-05 (**TIR-01 defect present**) |
| `ProductVariantServiceImplTest`, `AdminProductVariantControllerIntegrationTest` | FR-PROD-08 |
| `ProductImageServiceImplTest`, `AdminProductImageControllerIntegrationTest` | FR-PROD-09 |
| `CartControllerTest` | FR-CART-01 to FR-CART-05 |
| `CartServiceImplTest`, `CartServiceImplEnhancedTest` | FR-CART-01 to FR-CART-05 |
| `CartRepositoryTest` | FR-CART-02, FR-CART-06 |
| `CartApiTest` (E2E) | FR-CART-01 to FR-CART-05 |
| `CheckoutControllerTest` | FR-CHK-01 to FR-CHK-04 |
| `CheckoutServiceImplTest` | FR-CHK-01 to FR-CHK-06, SAF-02 |
| `OrderServiceImplTest`, `OrderProcessingComprehensiveTest` | FR-CHK-05, FR-CHK-07 |
| `OrderServiceIntegrationTest` | FR-CHK-05, FR-CHK-06, SAF-03 |
| `OrderApiTest` (E2E) | FR-CHK-05, FR-CHK-07 (**TIR-01 defect present**) |
| `AdminOrderControllerTest` | FR-CHK-08, FR-ADM-08 |
| `PaymentServiceImplTest` | FR-PAY-01, FR-PAY-02, FR-PAY-03 |
| `PaymentSignatureValidationServiceTest` | FR-PAY-02, SAF-01 |
| `RazorpayClientAdapterTest` | FR-PAY-01 |
| `PaymentProcessingTest` | FR-PAY-01 to FR-PAY-03, SAF-01, SAF-02 |
| `WebhookServiceImplTest`, `WebhookAdminControllerTest` | FR-PAY-04, FR-ADM-07 |
| `InventoryStatusControllerTest` | FR-INV-01, FR-INV-02 |
| `InventoryServiceImplTest`, `InventoryServiceImplEnhancedTest` | FR-INV-01 to FR-INV-05, FR-CHK-06 |
| `InventoryManagementTest` | FR-INV-01 to FR-INV-05, SAF-03 |
| `InventoryThresholdManagementServiceTest` | FR-INV-06, FR-ADM-06 |
| `InventoryAnalyticsServiceTest`, `InventoryReportServiceTest` | FR-INV-07, FR-ADM-02 |
| `AdminInventoryControllerTest` | FR-INV-03, FR-INV-04, FR-ADM-08 |
| `ProductReviewControllerTest`, `ProductReviewServiceImplTest` | FR-REV-01 to FR-REV-03 |
| `WishlistControllerTest`, `WishlistServiceImplTest` | FR-WISH-01, FR-WISH-02 |
| `AuditAspectTest` | FR-ADM-04, MNT-05 |
| `AuditLogServiceTest`, `AuditLogControllerTest` | FR-ADM-04 |
| `SalesAnalyticsControllerTest`, `SalesAnalyticsServiceImplTest`, `AnalyticsReportingTest` | FR-ADM-01 |
| `AdminDashboardTest` | FR-ADM-01, FR-ADM-02 |
| `AdminUserControllerTest` | FR-ADM-03, FR-ADM-08 |
| `AdminReportControllerTest` | FR-ADM-05 |
| `AdminInventoryThresholdControllerTest` | FR-ADM-06, FR-ADM-08 |
| `HealthIndicatorTest` | FR-MON-01 |
| `DatabaseHealthIndicatorTest` | FR-MON-02, AVL-02 |
| `RedisHealthIndicatorTest` | FR-MON-03, AVL-02 |
| `PerformanceMetricsControllerTest` | FR-MON-05 |
| `PoolMetricsControllerTest` | FR-MON-05, PR-05 |
| `GlobalExceptionHandlerTest`, `ExceptionClassesTest` | UR-01, UR-02 |
| `ReliabilityTest`, `ReliabilityHATest` | REL-02, REL-03, AVL-04 |
| `PerformanceTest`, `PerformanceBaselineTest` | PR-05, PR-06 |
| `LoadTestSimulation` (Gatling) | PR-01, PR-02, PR-03, SCL-04 |
| `DatabaseConstraintTest` | MNT-04, SAF-03 |
| `LoggingStandardsTest` | MNT-05, MNT-06 |
| `SecureLoggerTest` | SEC-04, MNT-05 |
| `CacheMetricsUtilTest` | FR-PROD-06, PR-07 |
| `EdgeCaseAndBoundaryTest` | SAF-03, FR-CART-05, FR-CHK-02 |
| `DatabaseQueryOptimizationPatternsTest` | DC-08, PR-01 |
| `ValidationUtilTest`, `DataValidationTest`, `InputValidationTest` | SEC-06, UR-01 |
| `CategoryServiceImplTest`, `CategoryManagementTest`, `CategoryRepositoryTest` | FR-PROD-03, FR-PROD-07 |
| `ApiIntegrationTest`, `OrderServiceIntegrationTest` | FR-CHK-01 to FR-CHK-07 (integration) |
| Entity tests (all `*Test.java` in `model.entity`) | FR-CART-06, FR-CHK-05, DC-07 |
| Elasticsearch tests | FR-MON-07, FR-ADM-04 (ES path) |

---

## 11. Open Defects Blocking Phase 1 Exit

| Defect ID | TIR / Req | File | Symptom | Root Cause | Remediation Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| DEF-001 | TIR-02 / MNT-03 | `AuthServiceImplTest.java` | `NullPointerException` in 3 test methods (`testRegisterSuccess`, `testRegisterPublishesEvent`, `testRegisterSetsUserFieldsAndValidatesPassword`) | `RoleRepository roleRepository` not declared as `@Mock`; field is null when `@InjectMocks` creates `AuthServiceImpl` | Add `@Mock RoleRepository roleRepository` field; add stub `when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole))` in `@BeforeEach` |
| DEF-002 | TIR-01 / MNT-03 | `ProductApiTest.java` | HTTP 500 in 4 test methods (`testGetProductsByCategory`, `testSearchProducts`, `testGetProductDetails`, `testGetDeprecatedV1Products`) | E2E test class runs in `unit-tests` Maven profile; no server is running; RestAssured connection fails | Add `@Tag("e2e")` to `ProductApiTest`; confirm `pom.xml` unit-tests profile has `<excludedGroups>e2e,stress,integration</excludedGroups>` |
| DEF-003 | TIR-01 / MNT-03 | `OrderApiTest.java` | HTTP 500 (presumed, consistent with ProductApiTest) | Same root cause as DEF-002 | Add `@Tag("e2e")` to `OrderApiTest` |
| DEF-004 | TIR-03 / MNT-03 | `AuthenticationAuthorizationSecurityTest.java:246` | Test asserts HTTP 401; receives HTTP 403 | `testRoleHierarchyEnforcement` tests an authenticated user accessing an admin endpoint; the correct response is 403 (Forbidden), not 401 (Unauthorized) | Change assertion from `equalTo(401)` to `equalTo(403)` in `testRoleHierarchyEnforcement` |
| DEF-005 | TIR-04 / MNT-03 | `InputValidationSecurityTest.java:168` | Test asserts HTTP 401; receives HTTP 400 | `testXSSPrevention` sends XSS payload; Spring's `MethodArgumentNotValidException` (400) fires before JWT authentication filter | Change assertion to `anyOf(equalTo(400), equalTo(401))` |
| DEF-006 | TIR-04 / MNT-03 | `InputValidationSecurityTest.java:303` | Test asserts HTTP 401; receives HTTP 415 | `testFileUploadValidation` sends wrong Content-Type; Spring's `HttpMediaTypeNotSupportedException` (415) fires before JWT filter | Change assertion to `anyOf(equalTo(415), equalTo(401))` |

---

## 12. Phase Completion Summary

### Phase 1 — Stabilization

| Category | Implemented | Open Defects | Pending Ph-2 | Phase 1 Completion |
| :--- | :--- | :--- | :--- | :--- |
| Functional (FR-AUTH, FR-PROD, FR-CART, FR-CHK, FR-INV, FR-REV, FR-WISH, FR-ADM partial) | 46 | 0 | 0 | ✅ |
| Security (SEC-01 to SEC-11) | 9 | 0 | 0 | ✅ |
| Design Constraints (DC-01 to DC-07) | 7 | 0 | 0 | ✅ |
| Reliability (REL-02, REL-03) | 2 | 0 | 0 | ✅ |
| Availability (AVL-04) | 1 | 0 | 0 | ✅ |
| Maintainability (MNT-01, MNT-04, MNT-05, MNT-06) | 4 | 0 | 0 | ✅ |
| Portability (PRT-04) | 1 | 0 | 0 | ✅ |
| Scalability (SCL-01, SCL-02) | 2 | 0 | 0 | ✅ |
| Safety (SAF-03) | 1 | 0 | 0 | ✅ |
| Test Integrity (TIR-01 to TIR-04) | 0 | **4** | 0 | 🔴 **BLOCKED** |
| Maintainability (MNT-03) | 0 | **1** | 0 | 🔴 **BLOCKED** |

> **Phase 1 is blocked.** Six defects (DEF-001 through DEF-006) across TIR-01 to TIR-04 and MNT-03 must be resolved. Estimated remediation effort: 2–3 hours.

### Phase 2 — Production Readiness

| Category | Total Ph-2 Requirements | Started | Not Started |
| :--- | :--- | :--- | :--- |
| Frontend (FR-FE-01 to FR-FE-30) | 30 | 0 | 30 |
| Security (SEC-03, SEC-04, SEC-12, SEC-13, SEC-14) | 5 | 1 (SEC-12 dual-key) | 4 |
| Monitoring (FR-MON-02 to FR-MON-08) | 6 | 1 (FR-MON-05 partial) | 5 |
| Payment full flow (FR-PAY-01 to FR-PAY-05) | 5 | 3 (partial) | 2 |
| Performance / Scalability / Reliability (PR, REL, SCL) | 13 | 0 | 13 |
| Availability (AVL-01 to AVL-03) | 3 | 0 | 3 |
| Admin full suite (FR-ADM-01 to FR-ADM-07) | 6 | 1 (FR-ADM-06 partial) | 5 |
| Maintainability (MNT-02, TIR-05) | 2 | 1 (MNT-02 40% gate) | 1 |
| Auth / Safety / Checkout / Inventory Ph-2 | 10 | 0 | 10 |
| **Phase 2 total** | **80** | **7** | **73** |

---

## 13. RTM Maintenance Procedure

Per ISO/IEC/IEEE 29148:2018 §6.2.5 and ISO/IEC/IEEE 12207:2017 §6.4.2:

1. **On new requirement**: Add row to the appropriate RTM section; status = ⬜ Not Started. Assign SDD design element before implementation begins.
2. **On design change**: Update SDD Reference column; verify design element still satisfies requirement; flag for re-inspection if design changes test assumptions.
3. **On implementation**: Update Implementation column; set status to 🟡 Partial. Assign or create test class before marking ✅ Implemented.
4. **On defect discovery**: Set status to 🔴 Open Defect; add row to §11; file issue tracker entry. Do not change status to ✅ until defect is resolved and verified.
5. **On defect resolution**: Remove from §11; set status to ✅ Implemented; update test class column.
6. **On phase gate review**: Update §12 Phase Completion Summary; obtain sign-off from Test Manager and Technical Lead.
7. **RTM version**: Increment patch version on any row-level change; increment minor version on structural changes; increment major version on re-baseline.

---

**— End of Document —**

*This RTM was prepared in conformance with ISO/IEC/IEEE 29148:2018 §6.2.5 (Traceability) for the BuildNest E-Commerce Platform. All requirement IDs, statuses, implementation references, and test class assignments are traceable to SRS-BUILDNEST-001 v4.0, SDD-BUILDNEST-001 v3.0, TP-BUILDNEST-001 v4.0, and the Baseline Assessment Report dated 2026-06-19. This document shall be maintained throughout the software lifecycle and updated on every requirement, design, implementation, or test change.*

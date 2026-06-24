# Changelog

All notable changes to this project are documented in this file.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Issue numbers reference the [GitHub issue tracker](https://github.com/pradip9096/buildnest-ecommerce-platform/issues).

Pre-1.0 convention: MINOR increments represent completed milestones; PATCH increments represent hotfixes within a milestone. `1.0.0` will mark the first production-ready release (end of M5).

---

## [Unreleased] — M4: Feature Development

### Added
- Shipping method and cost calculation service: `GET /api/v1/checkout/shipping-options?postalCode=` returns all active methods with `calculatedCost = baseCost + (costPerKg × totalWeightKg × zoneMultiplier)`; zone derived from postal code prefix hash into configurable `app.shipping.zone-multipliers` list (SHIP-01, #87)
- `ShippingConfig` (`@ConfigurationProperties("app.shipping")`): `defaultWeightPerItemKg` (default 0.5 kg/unit) and `zoneMultipliers` (default [1.0, 1.5, 2.0]) — fully configurable without code changes (#87)
- `AdminShippingController` at `GET/POST/PUT/DELETE /api/v1/admin/shipping-methods`: full CRUD with `@Auditable` on all mutating operations; DELETE is a soft-deactivate (`isActive=false`) (#87)
- `ShippingServiceImplTest` — 16 unit tests covering cost formula, zone resolver boundary cases (null/blank postal, short postal, hash consistency), empty-cart guard, and admin CRUD (#87)
- `AdminShippingControllerIntegrationTest` — 9 integration tests covering list, create (201 + isActive assertion), update, soft-delete (DB `isActive=false` verified), validation (400), role enforcement (403), and 404 (#87)
- Multi-step checkout flow at `POST /api/v1/checkout/{address,shipping,payment,confirm}`: address selection → shipping method → payment initiation (delegates to `PaymentService`) → order confirmation; session stored in Redis with 30-minute TTL; out-of-order step returns 409 Conflict (CHK-01, #76)
- `CheckoutSessionStore` interface + `RedisCheckoutSessionStore` implementation (`StringRedisTemplate` + Jackson; key `checkout:session:{userId}`; 30-min TTL) for Redis-backed checkout session management (#76)
- `CheckoutSession` / `CheckoutStep` — serializable session POJO and step enum (`PENDING_SHIPPING → PENDING_PAYMENT → PENDING_CONFIRM`) (#76)
- `ShippingMethod` JPA entity + `ShippingMethodRepository` mapping the `shipping_methods` table (created in #104/#87): name, baseCost, costPerKg, estimatedDaysMin/Max, isActive (#76)
- `AddressRepository` — for checkout address ownership validation (#76)
- `SetAddressRequest` + `SelectShippingRequest` — validated request payloads for checkout step 1 and step 2 (#76)
- `CheckoutSessionDTO` — response DTO exposing current session step, addressId, shippingMethodId, shippingCost, orderId, razorpayOrderId (#76)
- `CheckoutFlowIntegrationTest` — 11 integration tests covering happy paths, invalid step transitions (409), wrong-owner address (404), inactive shipping method (404), and unauthenticated access (401); `@MockBean CheckoutSessionStore` and `@MockBean PaymentService` used to avoid Redis/Razorpay in tests (CHK-01, #76)
- Inventory management admin endpoints: `GET /api/v1/admin/inventory` (list all with `InventoryDTO` — productId, productName, qty, reservedQty, availableQty, status) and `PATCH /api/v1/admin/inventory/{productId}` (delta-based adjustment with required reason; validates result ≥ 0; `@Auditable`) (ADM-06, #72)
- `InventoryAuditLog` JPA entity + `InventoryAuditLogRepository` mapping the `inventory_audit_log` table (created in #104); every `PATCH` adjustment writes a record with before/change/after quantities, reason, actor, and `referenceType=MANUAL` (#72)
- `InventoryDTO` — list-view DTO for admin inventory endpoint (#72)
- `AdjustInventoryRequest` — `{ @NotNull Integer delta, @NotBlank String reason }` request payload (#72)
- `AdminInventoryControllerIntegrationTest` — 9 integration tests covering list, positive/negative delta, below-zero guard (400), missing reason (400), 404, role enforcement (403), and audit log record verification (ADM-06, #72)
- Order management admin endpoints at `GET/PATCH /api/v1/admin/orders` — list with status/userId/dateFrom/dateTo filters + pagination, full detail with nested items, forward-only status transitions (PENDING→CONFIRMED→SHIPPED→DELIVERED; any→CANCELLED) enforced server-side; `PATCH` is `@Auditable`; customer notification dispatched on each transition (ADM-03, #69)
- `OrderSpecification` — composable `JpaSpecificationExecutor`-based filter for admin order listing (#69)
- `AdminOrderDetailDTO` + `OrderItemDTO` — full admin order view with nested item lines (#69)
- `UpdateOrderStatusRequest` — validated request payload for status transitions (`@NotBlank status`, optional `cancellationReason`) (#69)
- `NotificationServiceImpl` — stub implementation of `INotificationService`; logs all calls; real email delivery wired in #62 (#69)
- `AdminOrderControllerIntegrationTest` — 13 integration tests covering list, filter, detail, valid/invalid transitions, and role enforcement (ADM-03, #69)
- Product CRUD admin endpoints at `POST/GET/PUT/DELETE /api/v1/admin/products` — all `@PreAuthorize("hasRole('ADMIN')")`, all `@Auditable`; image upload via `POST /api/v1/admin/products/{id}/images` (multipart/form-data, 10 MB cap, JPEG/PNG/WebP/GIF only) (ADM-01, #67)
- `StorageService` interface + `LocalStorageService` implementation (UUID-keyed filenames, configurable `app.storage.location`, static serving via `/uploads/**`) (#67)
- `StorageConfig` (`WebMvcConfigurer`) serving uploaded files from the configured storage directory (#67)
- `Product.updateProductImage` service method for atomic image URL updates (#67)
- `AdminProductControllerIntegrationTest` — 11 integration tests covering create, read, update, soft-delete, image upload, and role-enforcement (403/401) (ADM-01, #67)
- `/api/v1/admin/**` URL-level `hasRole("ADMIN")` rules added to `SecurityConfig` and `TestSecurityConfig` (was missing; only `/api/admin/**` was covered) (#67)
- Liquibase XML master orchestrator (`db.changelog-master.xml`) replacing direct SQL master reference; enables per-entity XML changeset files and clean include-based composition (#104)

### Changed
- `MultiStepCheckoutController` extended with `GET /api/v1/checkout/shipping-options` delegating to `ShippingService` (#87)
- `CheckoutService` extended with 4 new methods (`setAddress`, `selectShipping`, `initiatePayment`, `confirmCheckout`); `CheckoutServiceImpl` extended with implementations + `AddressRepository`, `ShippingMethodRepository`, `PaymentService`, `CheckoutSessionStore` injected via constructor (#76)
- `AdminInventoryController` migrated from `/api/admin/inventory` to `/api/v1/admin/inventory`; `GET /` and `PATCH /{productId}` added; legacy sub-path endpoints retained for backward compatibility (ADM-06, #72)
- `InventoryRepository` extended with `@EntityGraph({"product"}) findAll()` to prevent N+1 queries on admin list (#72)
- `InventoryServiceImpl` extended with `getAllInventorySummary()` and `adjustStock()` (delta validation, audit write, status recalculation, `LowStockWarningEvent` if threshold crossed) (#72)
- `AdminInventoryControllerTest` URL references updated from `/api/admin/inventory` to `/api/v1/admin/inventory` (#72)
- `AdminOrderController` rewritten at `/api/v1/admin/orders` (was `/api/admin/orders`); removed try/catch anti-pattern; delegates all error handling to `GlobalExceptionHandler` (ADM-03, #69)
- `OrderRepository` extended with `JpaSpecificationExecutor<Order>` for specification-based admin queries (#69)
- `OrderServiceImpl` extended with `adminUpdateOrderStatus` (transition validation + notification) and `getAdminOrders` / `getAdminOrderDetail` admin methods (#69)
- `AdminProductController` base path corrected from `/api/admin/products` to `/api/v1/admin/products` (ADM-01, #67)
- `deleteProduct` changed from hard delete (`deleteById`) to soft delete (`isActive = false`) (ADM-01, #67)
- `AuditAspectIntegrationTest` and `InputValidationSecurityTest` URL references updated to `/api/v1/admin/products` (#67)
- Liquibase changeset `addresses` table: user address book with user FK, default-flag, address-type, and covering indexes (USR-01, #78, #104)
- Liquibase changeset `product_variants` table: size/colour variants per product with SKU uniqueness, price adjustment, and stock quantity (PROD-01, #81, #104)
- Liquibase changeset `product_images` table: multiple ordered images per product with primary-flag (PROD-02, #82, #104)
- Liquibase changeset `product_tags` + `product_tag_map` join table: many-to-many product tagging with slug uniqueness (PROD-03, #83, #104)
- Liquibase changeset `coupons` table: PERCENTAGE/FIXED_AMOUNT discount codes with usage limits, validity windows, and min-order threshold (CHK-02, #77, #104)
- Liquibase changeset `shipping_methods` table: base cost, per-kg cost, and estimated delivery day range (SHIP-01, #87, #104)
- Liquibase changeset `return_requests` table: order return/refund flow with status lifecycle (PENDING → APPROVED/REJECTED → REFUNDED) and admin notes (RET-01, #88, #104)
- Liquibase changeset `inventory_audit_log` table: full inventory change audit trail with change type, before/after quantities, reference type/ID, and actor FK (ADM-06/INV-01, #72, #73, #104)

---

## [0.4.0] — 2026-06-24 (M3: Technical Debt Reduction)

### Changed
- Upgrade Elasticsearch, Kibana, and Logstash Docker images from 8.10.2 to 8.17.6 (#236)
- Relocate git repository root from `backend/` to `BuildNest/` project root to bring frontend and CI/CD files under version control (#233)
- Harden Content Security Policy: remove `'unsafe-inline'` from `script-src` and `style-src` on all API paths; introduce dedicated `@Order(1)` `SecurityFilterChain` scoping `'unsafe-inline'` to Swagger UI documentation paths only; add `frame-ancestors 'none'` and `form-action 'self'` to main chain (SEC-14, #237)
- Implement explicit circuit breaker fallbacks: add `elasticsearchCircuitBreaker` bean; protect all ES repository calls in `ElasticsearchIngestionService` (async writes skip silently on CB OPEN; reads return empty list); distinguish `CallNotPermittedException` from transient failures in `RateLimiterService` — CB OPEN logged at DEBUG, not WARN (#238)
- Raise JaCoCo instruction coverage gate from 50% to 55% — actual coverage 94.57%, no package below 55% (#239)
- Restore PIT mutation score from 73% to 85%: add boundary-value tests for HTTP status code classification, CB-OPEN fallback tests for all ES read/write paths, and non-empty return assertions on all read methods to kill surviving "replaced return value with emptyList" mutations (#240)
- Audit and enforce `@Auditable` AOP coverage on all admin endpoints: verify all `@PostMapping`, `@PutMapping`, `@DeleteMapping` in `admin/` carry `@Auditable`; add `AuditAspectIntegrationTest` (TC-AUDIT-001 to TC-AUDIT-006) proving audit entries capture user ID, action, entity type, and IP address through the full HTTP → AOP → AuditLogService pipeline; fix timing-sensitive `UptimeMonitoringServiceTest.uptimeMetricsFailSlaWhenDowntimeHigh` by replacing `Thread.sleep` with deterministic clock back-dating via `ReflectionTestUtils` (SRS ADM-06, RTM AUDIT-01, #59)

### Fixed
- `ElasticsearchConfig.clientConfiguration()` now passes injected credentials via `.withBasicAuth()` — previously credentials were declared but never forwarded to the client builder, causing HTTP 401 against any secured cluster (#236)
- Add explicit `FetchType.LAZY` to `Category.products` and `Order.orderItems` associations to eliminate N+1 query risk (DC-08, #54)

---

## [0.3.0] — 2026-06-22 (M2: Quality Foundation)

### Added
- OWASP Dependency-Check Maven plugin (`owasp` profile, CVSS ≥ 7.0 fails build) (#53)
- Integration tests for rate-limiting behaviour using Bucket4j/Redis (#51)
- Comprehensive unit tests for `CartService` and `WishlistService` — 15 and 14 tests respectively (#50)
- Comprehensive unit tests for `OrderServiceImpl` including edge cases (#49)
- Comprehensive unit tests for `ProductServiceImpl` including pagination edge cases (#48)
- Edge-case unit tests for `AuthServiceImpl` (#47)
- All 62 required environment variables documented across 16 sections in `.env.example` (#52)

### Changed
- Raise JaCoCo instruction coverage gate from 40% to 50% (#46)

### Fixed
- `ProductServiceImpl.advancedSearch()` and `findByCategory()` pagination bug corrected as part of test expansion (#48)

---

## [0.2.0] — 2026-06-14 (M1: Stabilisation)

### Fixed
- Add missing `RoleRepository` mock in `AuthServiceImplTest` — test context failed to load (#38)
- Add `@Tag("e2e")` to `OrderApiTest` so it is correctly excluded from the unit-test profile (#40)
- Correct HTTP status assertions in `testRoleHierarchyEnforcement` — expected 403, was asserting 200 (#41)
- Correct XSS test assertion in `InputValidationSecurityTest`: 401 → 400 (#42)
- Correct file-upload test assertions in `InputValidationSecurityTest`: 401 → 415 (#43)

---

## [0.1.0] — 2026-06 (Pre-M1: Foundation)

### Added
- Spring Boot 3.5 / Java 21 backend: REST API, JWT auth (access + refresh tokens), Spring Security RBAC with `Permission` entities
- JPA entities: `User`, `Product`, `Category`, `Order`, `OrderItem`, `Cart`, `CartItem`, `Wishlist`, `Inventory`, `Payment`, `ProductReview`, `AuditLog`, `Role`, `Permission`, `RefreshToken`, `PasswordResetToken`, `WebhookSubscription`
- Liquibase-managed schema migrations (DDL auto = `validate`)
- Redis-backed rate limiting (Bucket4j) and caching (`@Cacheable`)
- Elasticsearch 8.x integration for audit log ingestion, metrics collection, and alerting
- `@Auditable` AOP aspect for declarative audit logging
- `ApiSunsetInterceptor` adding deprecation headers to v1 product endpoints
- Dual API versioning: `ProductControllerV1` and `ProductControllerV2`
- Docker Compose stack: MySQL 8.2, Redis 7, Elasticsearch, Kibana, Logstash, Prometheus
- GitHub Actions CI/CD: build, test, JaCoCo coverage, CodeQL, OWASP Dependency-Check
- Structured Logback/Logstash JSON logging pipeline to Elasticsearch
- Resilience4j circuit breaker configuration
- SDLC documentation suite: SRS (ISO/IEC/IEEE 29148), SDD (IEEE 1016), RTM, Test Plan (ISO 29119-3), SDP, V&V Report, BRD, ICD, CSD, metrics and quality reports
- React 19 / Vite frontend scaffold (stub; real UI not yet built)

# Changelog

All notable changes to this project are documented in this file.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Issue numbers reference the [GitHub issue tracker](https://github.com/pradip9096/buildnest-ecommerce-platform/issues).

Pre-1.0 convention: MINOR increments represent completed milestones; PATCH increments represent hotfixes within a milestone. `1.0.0` will mark the first production-ready release (end of M5).

---

## [Unreleased] — M4: Feature Development

### Added
- Product CRUD admin endpoints at `POST/GET/PUT/DELETE /api/v1/admin/products` — all `@PreAuthorize("hasRole('ADMIN')")`, all `@Auditable`; image upload via `POST /api/v1/admin/products/{id}/images` (multipart/form-data, 10 MB cap, JPEG/PNG/WebP/GIF only) (ADM-01, #67)
- `StorageService` interface + `LocalStorageService` implementation (UUID-keyed filenames, configurable `app.storage.location`, static serving via `/uploads/**`) (#67)
- `StorageConfig` (`WebMvcConfigurer`) serving uploaded files from the configured storage directory (#67)
- `Product.updateProductImage` service method for atomic image URL updates (#67)
- `AdminProductControllerIntegrationTest` — 11 integration tests covering create, read, update, soft-delete, image upload, and role-enforcement (403/401) (ADM-01, #67)
- `/api/v1/admin/**` URL-level `hasRole("ADMIN")` rules added to `SecurityConfig` and `TestSecurityConfig` (was missing; only `/api/admin/**` was covered) (#67)
- Liquibase XML master orchestrator (`db.changelog-master.xml`) replacing direct SQL master reference; enables per-entity XML changeset files and clean include-based composition (#104)

### Changed
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

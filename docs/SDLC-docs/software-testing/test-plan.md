# Test Plan

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## DOCUMENT INFORMATION

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Test Plan |
| **Document ID** | TP-BUILDNEST-001 |
| **Version** | 4.5 |
| **Date** | 2026-07-30 IST |
| **Status** | Controlled — Under Review |
| **Classification** | Internal Use |
| **Conformance Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Related SRS** | SRS-BUILDNEST-001 v5.8 (docs/SDLC-docs/requirement-engineering/software-requirements-specification.md) |
| **Related SDD** | SDD-BUILDNEST-001 v4.13 (docs/SDLC-docs/design/software-design-description.md) |
| **Supersedes** | TP v3.0 (archive/docs/ISO-IEC-IEEE/Test_Plan_IEEE_29119.md, 2026-02-11) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date | Author | Changes | Approval |
| :--- | :--- | :--- | :--- | :--- |
| 1.0 | 2026-02-10 | BuildNest QA | Initial draft — 27 TC scope | Approved |
| 2.0 | 2026-02-11 | BuildNest QA | Expanded to 124 TCs; 22 categories; 12 modules | Approved |
| 3.0 | 2026-02-11 | BuildNest QA | ISO 29119-3 compliance; added conformance, definitions, responsibilities, deliverables, suspension criteria | Approved |
| 4.0 | 2026-06-19 | Test Manager | Baseline-driven update: corrected Spring Boot to 3.5.10; updated test counts from static and dynamic analysis (173 test files, 1,538 test executions, 99.1% pass rate); introduced Test Integrity Requirements (TIR) section; corrected JaCoCo gate from 40% to 70% target; updated Maven profile inventory; added mutation testing gate (PIT ≥ 75%); aligned all SRS/SDD references to v4.0/v3.0 | Pending |
| 4.1 | 2026-07-17 14:05 IST | Test Manager | Corrected §15 TIR-01–05 status (all 5 were stale "Open"/"Not yet measured" — TIR-01–04 confirmed already resolved in source, TIR-05's PIT gate confirmed configured at 77%); corrected §17.1's JaCoCo gate (actual 85%, not 40%) and frontend-coverage baseline (17 test files/121 tests, not the 3-file 2026-07-04 snapshot); corrected Elasticsearch 8.10→8.17 references; re-ran the full suite (`all-tests` profile, isolated shell) for §17.2's baseline table — 1,735 tests, 0 failures, 0 errors, superseding the stale 2026-06-19 figures (1,538 executions, 11 failed, 3 errors) (#461) | Pending |
| 4.2 | 2026-07-17 21:15 IST | Test Manager | Found during a fresh RTM/SRS/SDD/Test-Plan verification sweep: §17's 4.1 fix (actual JaCoCo 85%/PIT 77%) never propagated to §8.3/§8.4/§9.1/§9.2, which still stated the pre-fix 0.40/0.70/75% values — a direct self-contradiction within this same document. Corrected all four to the real gate values, and corrected §8.3's Counter column from LINE to INSTRUCTION (verified directly against `pom.xml`'s `<counter>INSTRUCTION</counter>`, not assumed). Also updated the `Related SRS`/`Related SDD` header fields from a long-stale v4.0/v3.0 to the current v4.5/v3.4, which had drifted through several intervening version bumps on both documents without ever being updated here | Pending |
| 4.3 | 2026-07-19 22:30 IST | Test Manager | Added `WishlistServiceImplLazyLoadingTest` to §8/#311's `service.wishlist` test-class listing — new `@DataJpaTest` regression test added for #442's fix (`WishlistServiceImpl.getWishlistProducts` was returning raw `Product` entities with uninitialized lazy fields, 6th occurrence of the raw-entity-lazy-collection bug family) | Pending |
| 4.4 | 2026-07-29 IST | Test Manager | SEC-14 (#110): §13.2's "Remove CSP `unsafe-inline`; update `SecurityTest` assertions" task struck through as done (#237 backend, #110 frontend). `Related SRS`/`Related SDD` header fields were long-stale (v4.5/v3.4, real current v5.6/v4.12 — several intervening version bumps on both documents never propagated here); corrected while already touching this document's content, though a full cross-reference-mesh sweep is the periodic 15-issue sync's job, not a per-issue one | Pending |
| 4.5 | 2026-07-30 IST | Test Manager | Periodic 15-issue SDLC documentation sync (overdue — last full sync at #452/#461, 2026-07-17; 53 issues closed since). §17.2's frontend-coverage baseline (17 test files/121 tests) corrected to the real current state via a fresh `npx vitest run`: 45 test files, 281 tests, all passing — the frontend grew substantially (Ph-3 marketplace-pivot seller/district UI, plus several M4 feature issues) with no single issue's own scope covering a re-verification of this aggregate row. Backend baseline (§17.2's own table) also drifted: re-ran a clean `./mvnw test` in an `env -i` isolated shell — 195→216 test files, 1,735→1,893 test executions, 0 failures/0 errors (matches PR #622's own test-plan citation for #111). §17.1's JaCoCo (85%)/PIT (77%) gate values re-checked directly against `pom.xml` — still accurate, no change. `Related SRS`/`Related SDD` updated (5.6→5.8, 4.12→4.13) | Pending |

### Document Approval

| Role | Name | Signature | Date |
| :--- | :--- | :--- | :--- |
| Test Manager | _____________ | _____________ | _____________ |
| Project Manager | _____________ | _____________ | _____________ |
| Technical Lead | _____________ | _____________ | _____________ |
| Quality Assurance | _____________ | _____________ | _____________ |

### Document Change Procedure

All changes require formal review and approval before incorporation. Changes must update the SRS/SDD Traceability Matrix (RTM) and the Requirements Traceability Matrix. Version increments follow semantic versioning: minor corrections = patch; new scope = minor; re-baselined plan = major.

---

## CONFORMANCE STATEMENT

> This document conforms to **ISO/IEC/IEEE 29119-3:2021** — *Software and systems engineering — Software testing — Part 3: Test Documentation*. It provides a master Test Plan for the BuildNest platform covering all test levels (unit, integration, system, acceptance) and all test types (functional, security, performance, reliability, usability) required by SRS-BUILDNEST-001 v4.0.

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Test Scope](#2-test-scope)
3. [Test Approach and Strategy](#3-test-approach-and-strategy)
4. [Test Levels](#4-test-levels)
5. [Test Types](#5-test-types)
6. [Test Execution Environment](#6-test-execution-environment)
7. [Test Data Strategy](#7-test-data-strategy)
8. [Test Tooling](#8-test-tooling)
9. [Entry and Exit Criteria](#9-entry-and-exit-criteria)
10. [Suspension and Resumption Criteria](#10-suspension-and-resumption-criteria)
11. [Test Deliverables](#11-test-deliverables)
12. [Roles and Responsibilities](#12-roles-and-responsibilities)
13. [Test Schedule](#13-test-schedule)
14. [Risks and Mitigations](#14-risks-and-mitigations)
15. [Test Integrity Requirements](#15-test-integrity-requirements)
16. [Test-to-Requirements Traceability](#16-test-to-requirements-traceability)
17. [Coverage Metrics and Acceptance Gates](#17-coverage-metrics-and-acceptance-gates)
18. [Appendices](#18-appendices)

---

## 1. Introduction

### 1.1 Purpose

This Test Plan (TP) establishes the test strategy, scope, levels, types, environment, tooling, data strategy, schedule, entry/exit criteria, suspension criteria, deliverables, and team responsibilities for the **BuildNest E-Commerce Platform**.

It governs all test activities across the two-phase delivery roadmap:

- **Phase 1 — Stabilization**: Achieve zero test failures, isolate E2E tests from the unit-test profile, repair existing test infrastructure defects, establish a trustworthy CI gate.
- **Phase 2 — Production Readiness**: Achieve 70% line coverage JaCoCo gate, 75% mutation test score (PIT), complete security and performance test suites, frontend test coverage, and confirm acceptance criteria for all SRS v4.0 Non-Functional Requirements.

**Version 4.0 Change Rationale**: This version incorporates corrections driven by the Baseline Assessment Report (`docs/reports/baseline-assessment-2026-06-19.md`) and aligns with SRS v4.0 and SDD v3.0. Test counts, tool versions, Maven profiles, and coverage targets have all been verified against the live codebase. Five Test Integrity Requirements (TIR-01 through TIR-05) identified in the Baseline Assessment are introduced as a formal test quality category.

### 1.2 Scope

This document covers testing of:

- **Backend REST API** (Spring Boot 3.5.10, Java 21) — all controller, service, repository, security, integration, and cross-cutting layers
- **Frontend SPA** (React 19.2, Vite 8.0) — component unit tests and E2E smoke tests (Phase 2)
- **Infrastructure integrations** — MySQL 8.2, Redis 7, Elasticsearch 8.17, Razorpay (mocked in CI, real in staging)

**Out of Scope**:

- Third-party service internals (Razorpay payment processing engine, Logstash pipeline internals)
- Native mobile applications (iOS / Android) — not planned
- Physical infrastructure load capacity testing (addressed by SRE team)
- Penetration testing (addressed by dedicated security team engagement; this plan covers OWASP verification)

### 1.3 Audience

| Audience | Usage |
| :--- | :--- |
| QA Engineers | Execution guidance, test level assignment, tool configuration |
| Developers | Entry criteria, unit test requirements, TIR compliance |
| DevOps | CI pipeline integration, environment provisioning |
| Project Manager | Schedule, milestones, risk register |
| Auditors | Traceability to SRS, evidence of standard compliance |

### 1.4 Definitions and Acronyms

| Term | Definition |
| :--- | :--- |
| **TIR** | Test Integrity Requirement — a quality constraint on the test suite itself (from SRS v4.0 §10) |
| **JaCoCo** | Java Code Coverage library — measures instruction, line, and branch coverage |
| **PIT** | PIT Mutation Testing tool — injects code mutations to assess test suite effectiveness |
| **SUT** | System Under Test |
| **CI** | Continuous Integration pipeline |
| **E2E** | End-to-End test — requires a running server and real database; tagged `@Tag("e2e")` |
| **H2** | In-memory relational database used by integration tests under the test Spring profile |
| **RestAssured** | HTTP client library used for E2E and integration API tests |
| **MockMvc** | Spring test framework for controller-layer unit tests without a running server |
| **Mockito** | Java mocking framework used for service-layer unit tests |
| **@DataJpaTest** | Spring Boot test slice loading only JPA layer with H2 in-memory database |
| **@WebMvcTest** | Spring Boot test slice loading only MVC layer with mocked services |
| **@SpringBootTest** | Full application context test; used for integration and E2E tests |
| **Test Profile** | Spring `test` profile — loads H2 datasource, `TestSecurityConfig`, `TestElasticsearchConfig` |

### 1.5 References

| ID | Document | Version |
| :--- | :--- | :--- |
| REF-01 | ISO/IEC/IEEE 29119-3:2021 — Test Documentation | 2021 |
| REF-02 | ISO/IEC/IEEE 29119-1:2022 — Concepts and Definitions | 2022 |
| REF-03 | ISO/IEC/IEEE 29119-4:2021 — Test Techniques | 2021 |
| REF-04 | OWASP Application Security Verification Standard | 4.0 |
| REF-05 | SRS-BUILDNEST-001 v4.0 | 2026-06-19 |
| REF-06 | SDD-BUILDNEST-001 v3.0 | 2026-06-19 |
| REF-07 | BuildNest Baseline Assessment Report | 2026-06-19 |
| REF-08 | JaCoCo Documentation | 0.8.11 |
| REF-09 | PIT Mutation Testing Documentation | 1.15.x |

---

## 2. Test Scope

### 2.1 Features Under Test

| Feature Group | Modules / Endpoints Covered | SRS Requirements | Phase |
| :--- | :--- | :--- | :--- |
| Authentication & Token Management | Register, Login, Logout, Refresh Token, JWT validation, HMAC-SHA512 | FR-AUTH-01 to FR-AUTH-11 | Ph-1 |
| Password Management | Forgot Password, Reset Password, Change Password, Token expiry | FR-AUTH-08, FR-AUTH-09 | Ph-1 |
| User Profile | View/Update profile | FR-AUTH-09 | Ph-1 |
| Product Catalog — V1 (Deprecated) | List, Detail, Search, Category filter + `Deprecation` / `Sunset` headers | FR-PROD-01 to FR-PROD-05 | Ph-1 |
| Product Catalog — V2 (Current) | List, Detail, Search, Category filter | FR-PROD-01 to FR-PROD-05 | Ph-1 |
| Admin Product Management | CRUD, image association | FR-PROD-04, FR-ADM-08 | Ph-1 |
| Shopping Cart | Add, Remove, Update, Clear, Calculate Total | FR-CART-01 to FR-CART-06 | Ph-1 |
| Checkout | Validate, Calculate, Process, Process-with-Payment | FR-CHK-01 to FR-CHK-08 | Ph-1 |
| Payment | Razorpay order creation, Signature verification, Webhook handling | FR-PAY-01 to FR-PAY-05 | Ph-1 |
| Order Management | Create, Retrieve, Status update, History | FR-CHK-07, FR-CHK-08 | Ph-1 |
| Inventory Management | Check availability, Deduct stock, Threshold alerts, Analytics | FR-INV-01 to FR-INV-07 | Ph-1 |
| Wishlist | Add, Remove, Check, Clear, Count | FR-WISH-01 to FR-WISH-04 | Ph-1 |
| Product Reviews | Submit, Edit, Delete, List by product | FR-REV-01 to FR-REV-05 | Ph-1 |
| Categories | CRUD, Listing | FR-CAT-01 to FR-CAT-04 | Ph-1 |
| RBAC & Authorisation | Role assignment, `@PreAuthorize`, `@Secured`, Admin-only endpoints | FR-AUTH-10, FR-AUTH-11 | Ph-1 |
| Rate Limiting | Login throttle, password reset throttle, admin endpoint throttle | SEC-07 to SEC-11, FR-AUTH-02 | Ph-1 |
| Audit Logging | `@Auditable` AOP, audit log retrieval, Elasticsearch ingestion | FR-ADM-04, MNT-05 | Ph-1 |
| Analytics & Reporting | Sales analytics, inventory reports, admin dashboard | FR-ADM-01 to FR-ADM-05 | Ph-1 |
| Webhook Management | Register, Trigger, Event delivery | FR-ADM-07 | Ph-1 |
| Notifications | Event-driven email/notification dispatch | FR-NOT-01 to FR-NOT-03 | Ph-1 |
| Monitoring & Health | `/actuator/health`, `/actuator/prometheus`, performance metrics | FR-MON-01 to FR-MON-08 | Ph-1 |
| Security Headers | HSTS, X-Frame-Options, X-Content-Type-Options, CSP | SEC-03, SEC-05, SEC-14 | Ph-1 |
| Resilience Patterns | Circuit breaker, time limiter, graceful degradation | REL-02, REL-03, AVL-04 | Ph-1 |
| Input Validation & Sanitisation | Injection prevention, field constraints, HTTP 400 on invalid input | SEC-06, SEC-06 | Ph-1 |
| Frontend Components | React components, routing, auth flow, cart, checkout | FR-FE-01 to FR-FE-30 | Ph-2 |
| Accessibility | WCAG 2.1 AA compliance on key flows | ACC-01, ACC-02 | Ph-2 |

### 2.2 Features Not Under Test (Exclusions)

| Excluded Feature | Reason |
| :--- | :--- |
| Elasticsearch internals | Feature-gated (`elasticsearch.enabled=false` default); integration tested only when enabled |
| Coupon / discount engine | Not implemented; not in SRS v4.0 scope |
| Cross-device cart synchronisation | Not in SRS v4.0 scope |
| OAuth 2.0 / SSO | Not implemented |
| Native mobile (iOS / Android) | Not in project scope |
| Multi-currency / multi-language | Not in SRS v4.0 scope |
| CDN media upload | Not in SRS v4.0 scope |
| Chaos Engineering (`chaos.enabled`) | Handled separately via dedicated chaos drill runbook |
| Razorpay live transaction processing | Mocked in CI; tested in staging only |

---

## 3. Test Approach and Strategy

### 3.1 Testing Philosophy

Testing at BuildNest follows four guiding principles:

1. **Test at the right level**: Unit tests for logic, slice tests for MVC/JPA layers, integration tests for component interactions, E2E for the HTTP contract. Do not test the framework.
2. **Fail fast**: Tests that take longer than 30 seconds are candidates for profiling and optimisation. The unit-test profile must complete in under 3 minutes on standard CI hardware.
3. **Trustworthy CI gate**: The CI pipeline gate is binary — zero failures or the build fails. No skipped tests, no `@Disabled` without an associated issue reference.
4. **Tests are first-class code**: Test code undergoes code review, follows the same naming conventions, and must not contain hardcoded IDs, brittle timing assumptions, or suppressed assertions.

### 3.2 Testing Pyramid

```
                     ┌─────────────────┐
                     │   E2E Tests     │  (5%)
                     │  ~5–10 classes  │  Requires running server
                     │  Tagged: e2e    │
                     └────────┬────────┘
                    ┌─────────┴──────────┐
                    │  Integration Tests │  (20%)
                    │  @SpringBootTest   │
                    │  H2 in-memory DB   │
                    └─────────┬──────────┘
             ┌────────────────┴────────────────┐
             │         Unit Tests              │  (75%)
             │  @ExtendWith(MockitoExtension)  │
             │  @WebMvcTest / @DataJpaTest     │
             │  No I/O — sub-second execution  │
             └─────────────────────────────────┘
```

### 3.3 Test Isolation Strategy

| Concern | Implementation |
| :--- | :--- |
| Database | H2 in-memory for all `test` profile tests; Liquibase `test` changelog subset |
| Security | `TestSecurityConfig` (`@Profile("test")`) — disables JWT filter; uses mock auth |
| Elasticsearch | `TestElasticsearchConfig` — disabled stub; no real ES connection in CI |
| External APIs | Razorpay and webhook targets mocked via Mockito or WireMock |
| Clock | `TestClockConfig` — fixed `Clock` bean; eliminates time-dependent flakiness |
| Cache | Redis cache evicted / replaced with `@MockBean CacheManager` in unit tests |
| Random data | Fixed seeds for all random generators; never rely on UUID ordering |

### 3.4 CI Pipeline Integration

```
git push → CI triggered
│
├── Stage 1: Build + Compile
│     ./mvnw clean compile -DskipTests
│     Gate: zero compilation errors
│
├── Stage 2: Unit Tests (default profile)
│     ./mvnw test
│     Excludes: e2e, stress, integration
│     Gate: 0 failures, 0 errors
│
├── Stage 3: Integration Tests
│     ./mvnw test -P all-tests
│     Excludes: e2e, stress
│     Gate: 0 failures, 0 errors
│
├── Stage 4: Coverage Verification (Phase 2 gate)
│     ./mvnw verify -P all-tests
│     JaCoCo gate: LINE ≥ 70%
│     PIT gate: mutation score ≥ 75%
│     Gate: both thresholds met
│
└── Stage 5: E2E Tests (on staging)
      ./mvnw test -P e2e-tests
      Requires running server + database
      Gate: 0 failures
```

---

## 4. Test Levels

### 4.1 Unit Testing

**Definition**: Tests that exercise a single class in isolation. All dependencies are replaced with Mockito mocks or stubs. No Spring context is loaded. No I/O.

**Framework**: JUnit 5 (`@ExtendWith(MockitoExtension.class)`) + Mockito 5.x

**Scope**: Service implementation classes, utility classes, entity model behaviour, exception classes, security utilities, validators

**Conventions**:
- Test class named `{SubjectClass}Test.java`
- Test method named `should{Behaviour}When{Condition}()`
- Arrange / Act / Assert (AAA) pattern enforced
- All `@Mock` fields must be declared and populated (TIR-02)
- One logical assertion per test method; multiple related assertions via AssertJ soft assertions

**Existing test classes** (verified from codebase):

| Package | Classes |
| :--- | :--- |
| `service.auth` | `AuthServiceImplTest` |
| `service.cart` | `CartServiceImplTest`, `CartServiceImplEnhancedTest` |
| `service.checkout` | `CheckoutServiceImplTest` |
| `service.order` | `OrderServiceImplTest`, `OrderProcessingComprehensiveTest` |
| `service.product` | `ProductServiceImplTest` |
| `service.payment` | `PaymentServiceImplTest`, `PaymentSignatureValidationServiceTest` |
| `service.inventory` | `InventoryServiceImplTest`, `InventoryServiceImplEnhancedTest`, `InventoryAnalyticsServiceTest`, `InventoryMonitoringServiceTest`, `InventoryReportServiceTest`, `InventoryThresholdManagementServiceTest` |
| `service.category` | `CategoryServiceImplTest` |
| `service.review` | `ProductReviewServiceImplTest` |
| `service.user` | `UserServiceImplTest` |
| `service.wishlist` | `WishlistServiceImplTest`, `WishlistServiceImplLazyLoadingTest` |
| `service.webhook` | `WebhookServiceImplTest` |
| `service.notification` | `NotificationServiceTest` |
| `service.analytics` | `SalesAnalyticsServiceImplTest` |
| `service.admin` | `AdminServiceImplTest`, `AdminAnalyticsServiceTest` |
| `service.token` | `RefreshTokenServiceTest` |
| `service.password` | `PasswordResetServiceImplTest` |
| `service.ratelimit` | `RateLimiterServiceTest` |
| `service.audit` | `AuditLogServiceTest` |
| `service.elasticsearch` | `ElasticsearchIngestionServiceTest`, `ElasticsearchAlertingServiceTest`, `ElasticsearchMetricsCollectorServiceTest`, `ElasticsearchQueryOptimizationServiceTest`, `ThresholdManagementServiceTest` |
| `service.monitoring` | `PerformanceMonitoringServiceTest`, `UptimeMonitoringServiceTest` |
| `service.scheduler` | `InventoryMonitoringSchedulerTest`, `SchedulerServiceTest`, `TokenCleanupSchedulerTest` |
| `model.entity` | `UserTest`, `OrderTest`, `OrderItemTest`, `CartTest`, `ProductTest`, `CategoryTest`, `InventoryTest`, `PaymentEntityTest`, `ProductReviewTest`, `RefreshTokenTest`, `PasswordResetTokenTest`, `AuditLogTest`, `WishlistTest`, `RoleTest`, `AddressTest`, `WebhookSubscriptionTest`, `InventoryThresholdBreachEventTest` + conditional/coverage variants |
| `security` | `JwtTokenProviderTest`, `JwtAuthenticationFilterTest`, `JwtKeyValidatorTest`, `CustomUserDetailsTest`, `RolePermissionEvaluatorTest`, `AdminRateLimitFilterTest` |
| `aspect` | `AuditAspectTest` |
| `exception` | `ExceptionClassesTest`, `GlobalExceptionHandlerTest` |
| `interceptor` | `ApiSunsetInterceptorTest`, `PerformanceMonitoringInterceptorTest`, `RateLimitHeaderInterceptorTest` |
| `util` | `ValidationUtilTest`, `SecureLoggerTest`, `MapperUtilTest`, `GenericMapperUtilTest`, `RateLimitUtilTest`, `CacheMetricsUtilTest`, `ConsolidatedUtilitiesTest` |
| `validation` | `InputValidationTest`, `DataValidationTest`, `EmailPhoneValidatorTest`, `InputValidationHelperTest` |
| `event` | `DomainEventListenerTest` |
| `monitoring` | `BusinessMetricsServiceTest`, `MonitoringInitializerTest` |
| `rbac` | `RBACTest` |

**Maven command**: `./mvnw test` (unit-tests profile is active by default; excludes: `e2e,stress,integration`)

### 4.2 Controller Layer Testing (`@WebMvcTest`)

**Definition**: Tests that exercise a single `@RestController` class with all service dependencies mocked. Spring MVC machinery (serialisation, validation, error handling) is loaded. No real database.

**Framework**: `@WebMvcTest` + `MockMvc` + Mockito + `TestSecurityConfig`

**Scope**: All 29 controllers — request deserialization, HTTP status codes, validation rejection, security annotation enforcement, response serialization

**Existing test classes**:

| Controller Package | Test Class |
| :--- | :--- |
| `controller.admin` | `AdminProductControllerTest`, `AdminOrderControllerTest`, `AdminUserControllerTest`, `AdminInventoryControllerTest`, `AdminInventoryAnalyticsControllerTest`, `AdminInventoryReportControllerTest`, `AdminInventoryThresholdControllerTest`, `AdminThresholdControllerTest`, `AdminAnalyticsControllerTest`, `SalesAnalyticsControllerTest`, `AdminReportControllerTest`, `AuditLogControllerTest`, `WebhookAdminControllerTest`, `MonitoringControllerTest` |
| `controller.auth` | `AuthControllerTest`, `PasswordResetControllerTest` |
| `controller.user` | `CartControllerTest`, `CheckoutControllerTest`, `ProductControllerV1Test`, `ProductControllerV2Test`, `ProductReviewControllerTest`, `UserControllerTest`, `UserOrderControllerTest`, `WishlistControllerTest` |
| `controller.inventory` | `InventoryStatusControllerTest` |
| `controller.monitoring` | `PerformanceMetricsControllerTest`, `PoolMetricsControllerTest` |
| `controller.public_` | `HomeControllerTest` |

**Maven command**: Included in default unit-tests profile (`./mvnw test`)

### 4.3 Repository Layer Testing (`@DataJpaTest`)

**Definition**: Tests that exercise Spring Data JPA repository interfaces against H2 in-memory database. Liquibase changelog is applied against H2 at startup. Only the JPA slice is loaded.

**Framework**: `@DataJpaTest` + H2 + JUnit 5

**Scope**: Custom JPQL queries, derived query methods, unique constraint enforcement, cascade behaviour, `@Query` correctness

**Existing test classes**:

| Test Class | Repository Tested |
| :--- | :--- |
| `ProductRepositoryTest` | `ProductRepository` |
| `ProductRepositoryDefaultMethodTest` | Default Spring Data methods on `ProductRepository` |
| `CartRepositoryTest` | `CartRepository` |
| `CategoryRepositoryTest` | `CategoryRepository` |
| `OrderRepositoryTest` | `OrderRepository` |
| `PaymentRepositoryTest` | `PaymentRepository` |
| `InventoryThresholdBreachEventRepositoryTest` | `InventoryThresholdBreachEventRepository` |
| `DatabaseConstraintTest` | Cross-entity unique constraint enforcement |

**Maven command**: Included in default unit-tests profile (`./mvnw test`)

### 4.4 Integration Testing

**Definition**: Tests that load the full Spring Boot application context with H2 in-memory database, `TestSecurityConfig`, and `TestElasticsearchConfig`. They test interactions between multiple real components. No live server required; `MockMvc` is used for HTTP.

**Framework**: `@SpringBootTest(webEnvironment = MOCK)` + `MockMvc` + H2 + JUnit 5

**Scope**: Cross-service workflows (checkout with payment, order lifecycle), full security chain integration, cache eviction correctness, event listener chain

**Existing test classes**:

| Test Class | Scenario |
| :--- | :--- |
| `ApiIntegrationTest` | Full request-response cycle across controller → service → repository |
| `OrderServiceIntegrationTest` | Order placement and status transitions via real service + H2 |
| `RazorpayClientAdapterTest` | Payment adapter integration (Razorpay mocked via Mockito) |
| `AuthenticationAuthorizationSecurityTest` | Security filter chain: JWT, RBAC, 401/403 correctness |
| `InputValidationSecurityTest` | Injection and invalid input rejection (400/415 expected) |
| `SecurityTest` | General security configuration integration |
| `AuditAspectTest` | `@Auditable` AOP cross-cutting behaviour |
| `CategoryManagementTest` | Category CRUD integration |
| `InventoryManagementTest` | Inventory deduction and threshold events |
| `PaymentProcessingTest` | Payment flow integration |
| `AdminDashboardTest` | Admin analytics and reporting integration |
| `AnalyticsReportingTest` | Sales analytics integration |
| `EdgeCaseAndBoundaryTest` | Boundary values for pricing, quantities, dates |
| `DatabaseQueryOptimizationPatternsTest` | N+1 detection and query count verification |

**Maven command**: `./mvnw test -P all-tests` (excludes: `e2e,stress`)

### 4.5 End-to-End (E2E) Testing

**Definition**: Tests that call a running HTTP server over the network using RestAssured. They test the full system including security, database, and JSON contract. They are tagged `@Tag("e2e")` and excluded from the unit-test and all-tests profiles.

**Framework**: RestAssured + JUnit 5 + `@Tag("e2e")` + `BaseApiTest`

**Scope**: Critical happy-path and negative flows across the full HTTP stack

**Existing test classes**:

| Test Class | Scenarios |
| :--- | :--- |
| `AuthApiTest` | Register, Login, Refresh, Logout flows |
| `CartApiTest` | Add, Remove, Clear, Total flows |
| `OrderApiTest` | Checkout, Order retrieval flows |
| `ProductApiTest` | Product listing V1 (deprecated) and V2, search, category filter |
| `UserApiTest` | Profile view and update flows |

**Maven command**: `./mvnw test -P e2e-tests` (requires running server at configured `BASE_URL`)

**Prerequisite**: A deployed or locally running `./mvnw spring-boot:run` instance with a real MySQL database and the required environment variables (see §6.2).

### 4.6 Performance Testing

**Definition**: Tests that measure response time, throughput, and resource utilisation under expected and peak load conditions.

**Framework**: Gatling (`LoadTestSimulation.java`), JUnit 5 (`PerformanceTest.java`, `PerformanceBaselineTest.java`)

**Scope**:
- Product listing endpoint: mean response ≤ 200 ms at 100 concurrent users (NFR PR-01)
- Checkout endpoint: mean response ≤ 500 ms at 50 concurrent users (NFR PR-01)
- JWT validation overhead: ≤ 10 ms per request (NFR PR-04)
- HikariCP pool behaviour under sustained load (NFR PR-05, PR-06)

**Maven command**: `./mvnw test -P stress-tests` (tag: `stress`)

> **Note**: Stress and load tests must not be included in the CI gate. They run only in the dedicated performance environment.

---

## 5. Test Types

### 5.1 Functional Testing

Verifies that each feature delivers the behaviour described in SRS v4.0 Functional Requirements (FR-*).

| Sub-type | Technique | Primary Tools |
| :--- | :--- | :--- |
| Positive path | Equivalence partition, representative valid input | JUnit 5, MockMvc, RestAssured |
| Negative path | Equivalence partition, boundary value for invalid inputs | JUnit 5, MockMvc |
| Boundary value | Exact boundary, one-inside, one-outside | JUnit 5 (parameterised) |
| State transition | Each valid and invalid transition per state machine | JUnit 5, service unit tests |
| Error condition | All documented exception paths return correct HTTP status and error body | JUnit 5, MockMvc, `GlobalExceptionHandlerTest` |

### 5.2 Security Testing

Verifies that the system meets SRS v4.0 Security Requirements (SEC-*) and OWASP ASVS 4.0 Level 2.

| Sub-type | Scope | SRS Req | Test Classes |
| :--- | :--- | :--- | :--- |
| Authentication | JWT issuance, expiry, HMAC-SHA512 signature | SEC-01, SEC-02 | `JwtTokenProviderTest`, `AuthControllerTest` |
| Authorisation | RBAC enforcement; 401 vs 403 distinction | SEC-01, FR-AUTH-10 | `AuthenticationAuthorizationSecurityTest`, `RBACTest` |
| Rate limiting | Login lockout; admin throttle; fail-open on Redis down | SEC-07 to SEC-11 | `AdminRateLimitFilterTest`, `RateLimiterServiceTest` |
| Input validation | SQL injection, XSS, null byte, oversized payload rejection | SEC-06 | `InputValidationSecurityTest`, `InputValidationTest` |
| HTTP security headers | HSTS, X-Frame-Options, X-Content-Type-Options, CSP | SEC-03, SEC-05, SEC-14 | `SecurityTest`, `AuthenticationAuthorizationSecurityTest` |
| JWT key strength | Reject keys shorter than 512 bits at startup | SEC-02, FR-AUTH-05 | `JwtKeyValidatorTest` |
| Sensitive data exposure | Passwords not in logs or responses; PII masked | SEC-04 | `SecureLoggerTest`, `UserTest` |
| BCrypt hashing | Password stored as BCrypt hash; rounds ≥ 12 | SEC-02 | `AuthServiceImplTest` |
| Token rotation | Refresh token invalidated on use | FR-AUTH-06 | `RefreshTokenServiceTest` |
| Audit trail | Every `@Auditable` action produces an `AuditLog` entry | MNT-05 | `AuditAspectTest`, `AuditLogServiceTest` |

### 5.3 Performance Testing

| Test Scenario | Acceptance Threshold | SRS Req |
| :--- | :--- | :--- |
| Product listing (100 concurrent users) | Mean ≤ 200 ms; P95 ≤ 500 ms; 0% error rate | PR-01 |
| Product search | Mean ≤ 200 ms | PR-01 |
| Checkout processing (50 concurrent users) | Mean ≤ 500 ms; P95 ≤ 1,000 ms | PR-01 |
| JWT validation overhead | ≤ 10 ms per request | PR-04 |
| HikariCP pool saturation | No connection timeout errors at peak load | PR-05 |
| Cache hit rate (Redis available) | ≥ 80% for product and category endpoints | PR-01 |
| Application startup time | ≤ 30 seconds | PR-07 |

### 5.4 Reliability Testing

| Test Scenario | Acceptance Threshold | SRS Req |
| :--- | :--- | :--- |
| Circuit breaker opens on Redis failure | Circuit opens within ≤ 10 failed requests at 70% threshold | REL-02 |
| Circuit breaker opens on DB failure | Circuit opens within ≤ 10 failed requests at 50% threshold | REL-03 |
| Rate limit fail-open on Redis unavailability | Requests permitted when Redis circuit is open | AVL-04 |
| Graceful shutdown drain | In-flight requests complete within 30-second drain window | REL-04 |
| Application restart under load | Availability restored within 30 seconds | AVL-01 |

**Existing test classes**: `ReliabilityTest`, `ReliabilityHATest`

### 5.5 Usability Testing (Phase 2 — Frontend)

| Area | Acceptance Criteria | SRS Req |
| :--- | :--- | :--- |
| Core navigation | Task completion rate ≥ 85% on representative user tasks | UR-01 |
| Accessibility (WCAG 2.1 AA) | Zero WCAG Level A and AA violations on key flows | ACC-01, ACC-02 |
| Responsive layout | Functional on 375 px wide (iPhone SE) through 1440 px | FR-FE-26 |
| Error messaging | All API errors presented with user-friendly message (not raw JSON) | UR-02 |

**Tools (Phase 2)**: Vitest, React Testing Library, Playwright (E2E), axe-core (accessibility), Chrome DevTools Lighthouse

### 5.6 Compatibility Testing (Phase 2)

| Dimension | Scope | SRS Req |
| :--- | :--- | :--- |
| Browser | Chrome 90+, Firefox 90+, Edge 90+, Safari 15+ | CON-07 |
| Viewport | Mobile (375 px), Tablet (768 px), Desktop (1440 px) | FR-FE-26 |
| API backward compatibility | V1 endpoints return same response shape as before versioning | FR-PROD-05 |
| Database schema | All Liquibase changesets apply cleanly on MySQL 8.2 | DC-12 |

### 5.7 Regression Testing

Every pull request that modifies any of the following must execute the full unit-test and integration-test profile before merge:

- `SecurityConfig`, `JwtTokenProvider`, `JwtAuthenticationFilter`
- Any `*ServiceImpl` class
- Any `@Entity` class
- Any Liquibase changelog file
- `pom.xml` (dependency changes)

Regression gate: **zero failures** in the `all-tests` Maven profile.

---

## 6. Test Execution Environment

### 6.1 Environment Inventory

| Environment | Purpose | Database | Redis | ES | Server |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Local (developer)** | Unit and integration tests during development | H2 (in-memory) | Mocked | Disabled | No |
| **CI (GitHub Actions / Jenkins)** | Automated gate on every push and PR | H2 (in-memory) | Mocked | Disabled | No |
| **Staging** | E2E, performance, security, and UAT | MySQL 8.2 (staging) | Redis 7 | ES 8.17 | Yes (:8080) |
| **Production** | Canary / smoke tests only post-deploy | MySQL 8.2 (prod) | Redis 7 | ES 8.17 | Yes |

### 6.2 Environment Prerequisites

**For unit and integration tests** (CI and local):
- Java 21 JDK
- Maven 3.9+
- No external dependencies — all mocked or H2-backed

**For E2E and staging tests**:

```bash
# Required environment variables (see backend/.env.example)
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/buildnest_ecommerce
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
REDIS_HOST=<host>
REDIS_PORT=6379
JWT_SECRET=<512-bit-minimum key>
RAZORPAY_KEY_ID=<key>         # may be test-mode key
RAZORPAY_KEY_SECRET=<secret>

# Start infrastructure (from backend/)
docker compose up -d mysql redis elasticsearch
```

### 6.3 Spring Test Configuration

| Config Class | Profile Condition | Purpose |
| :--- | :--- | :--- |
| `TestSecurityConfig` | `@Profile("test")` | Replaces production `SecurityConfig`; disables JWT filter; provides mock authentication |
| `TestElasticsearchConfig` | `@Profile("test")` | Provides no-op Elasticsearch stub; prevents ES connection attempts |
| `TestClockConfig` | `@Profile("test")` | Injects fixed `Clock` bean to eliminate time-dependent test flakiness |
| `IntegrationTestConfig` | Integration test base classes | Shared configuration for `@SpringBootTest` tests |
| `TestProfilePropertyValidator` | `@Profile("test")` | Validates test-specific property constraints on startup |

### 6.4 Test Data Management

See §7 (Test Data Strategy) for full details.

---

## 7. Test Data Strategy

### 7.1 Principles

1. **No production data in tests** — all test data is synthetic and contained within the test execution boundary.
2. **Deterministic** — tests produce the same result on every run. No random data without a fixed seed.
3. **Isolated** — each test owns its data. No shared mutable state between tests. `@Transactional` rolls back after each `@DataJpaTest`.
4. **Minimal** — test data contains only the fields required to exercise the scenario. Irrelevant fields use sensible defaults via builders.
5. **Realistic** — field values follow real-world constraints (valid email formats, positive prices, positive quantities, realistic product names).

### 7.2 Data Creation Patterns

| Layer | Pattern | Implementation |
| :--- | :--- | :--- |
| Unit tests | In-method builder construction | Lombok `@Builder` on entities and DTOs; `new` keyword for simple cases |
| Controller tests | `MockMvc` request body JSON strings | Jackson ObjectMapper serialises test payload objects |
| Repository tests | `@BeforeEach` repository save + `@AfterEach` deleteAll | H2 reset on `@DataJpaTest` transaction rollback |
| Integration tests | `@Sql` scripts or `@BeforeEach` setup methods | `src/test/resources/sql/` scripts per test class |
| E2E tests | API call sequence (register → login → create resources) | `BaseApiTest` sets up prerequisite state via API |

### 7.3 Test Data Catalogue

| Data Category | Standard Test Values |
| :--- | :--- |
| User (customer) | `email: testuser@buildnest.com`, `password: TestPassword@123`, `role: USER` |
| User (admin) | `email: admin@buildnest.com`, `password: AdminPassword@123`, `role: ADMIN` |
| Product | `name: "Test Ceramic Tile"`, `price: 250.00`, `stock: 100`, `category: "Tiles"` |
| Category | `name: "Test Tiles"`, `description: "Test tile category"` |
| Cart item | `productId: <seeded>`, `quantity: 2` |
| Order | Created via checkout flow; `paymentMethod: "RAZORPAY"` |
| Boundary values | Price: `0.01`, `99999.99`; Quantity: `1`, `999`; String: 1 char, 255 chars, 256 chars (reject) |
| Invalid inputs | `email: "not-an-email"`, SQL fragment: `"'; DROP TABLE users; --"`, XSS: `"<script>alert(1)</script>"` |

### 7.4 Sensitive Data in Tests

- Test passwords must not match any production credential format.
- Test JWT secrets use a fixed 512-bit test-only value declared in `src/test/resources/application-test.properties`.
- Test Razorpay keys are Razorpay test-mode keys; never production keys.
- No PII from any real user may appear in test fixtures or SQL scripts.

---

## 8. Test Tooling

### 8.1 Tool Inventory

| Tool | Version | Purpose | Phase |
| :--- | :--- | :--- | :--- |
| JUnit 5 (Jupiter) | 5.10.x (via Spring Boot 3.5.10) | Test runner and assertion framework | Ph-1 |
| Mockito | 5.x (via Spring Boot 3.5.10) | Mocking and stubbing for unit tests | Ph-1 |
| Spring Boot Test | 3.5.10 | `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`, `@MockBean` | Ph-1 |
| MockMvc | (via Spring Boot Test) | HTTP request simulation without running server | Ph-1 |
| H2 | 2.x (via Spring Boot Test) | In-memory relational database for integration tests | Ph-1 |
| RestAssured | 5.x | HTTP client for E2E API tests | Ph-1 |
| JaCoCo | 0.8.11 | Line, instruction, and branch coverage measurement | Ph-1/Ph-2 |
| PIT (Pitest) | 1.15.x | Mutation testing — assesses test suite effectiveness | Ph-2 |
| AssertJ | 3.x (via Spring Boot Test) | Fluent assertion library | Ph-1 |
| Gatling | 3.x (`LoadTestSimulation.java`) | Load and stress test simulation | Ph-2 |
| Liquibase | 4.x (via Spring Boot) | Schema migration applied against H2 in tests | Ph-1 |
| Logback (Test) | (via Spring Boot) | Captures log output in `LoggingStandardsTest` | Ph-1 |
| Vitest | Latest | React component unit testing (Phase 2) | Ph-2 |
| React Testing Library | Latest | React component rendering and interaction tests | Ph-2 |
| Playwright | Latest | Frontend E2E browser automation | Ph-2 |
| axe-core | Latest | Automated accessibility (WCAG 2.1 AA) verification | Ph-2 |
| SonarQube / SonarCloud | Latest | Static analysis, code smell detection, coverage trending | Ph-2 |

### 8.2 Maven Test Profiles

| Profile ID | Activation | Excluded Groups | Included Groups | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `unit-tests` | **Default** (active by default) | `e2e`, `stress`, `integration` | All others | Fast CI gate — unit and slice tests only |
| `all-tests` | `-P all-tests` | `e2e`, `stress` | All others (incl. `integration`) | Full test suite without live server requirement |
| `e2e-tests` | `-P e2e-tests` | none | `e2e` only | E2E tests against running staging server |
| `stress-tests` | `-P stress-tests` | none | `stress` only | Load and stress tests in performance environment |
| `coverage` | `-P coverage` (verify goal) | `e2e`, `stress` | All others | Runs `all-tests` + JaCoCo check + PIT mutation |

### 8.3 JaCoCo Configuration

| Parameter | Current Value | Phase 1 Target | Phase 2 Target |
| :--- | :--- | :--- | :--- |
| Counter | INSTRUCTION | INSTRUCTION | INSTRUCTION |
| Minimum | 0.85 (85%) PACKAGE (verified 2026-07-17, #461) | 0.40 (superseded — already exceeded) | **0.70 (70%)** (already exceeded) |
| Report location | `target/site/jacoco/index.html` | — | — |
| Excluded packages | `model.entity.*` (Lombok-generated) | Unchanged | Unchanged |

> **Note**: The current JaCoCo gate (`pom.xml`) is set to `0.85` (verified 2026-07-17, #461 — see §17.1), already exceeding both the Phase 1 (0.40) and Phase 2 (0.70) targets originally planned. The gate must not be lowered below `0.70` at any time. JaCoCo report is generated by `./mvnw verify`.

### 8.4 PIT Mutation Testing Configuration (Phase 2)

| Parameter | Value |
| :--- | :--- |
| Target classes | `com.example.buildnest_ecommerce.service.*`, `com.example.buildnest_ecommerce.security.*` |
| Mutators | `DEFAULTS` (conditionals boundary, void method calls, return values, negate conditionals, remove conditionals, increments) |
| Threshold | Mutation score ≥ **75%**, active `mutationThreshold` currently set to **77%** (verified 2026-07-17, #461 — see §17.1), already exceeding the 75% requirement |
| Excluded classes | `*Test`, `*Config`, `*Application`, Lombok-generated |
| Maven goal | `./mvnw org.pitest:pitest-maven:mutationCoverage -P coverage` |
| Report | `target/pit-reports/index.html` |

---

## 9. Entry and Exit Criteria

### 9.1 Entry Criteria

#### Phase 1 Test Execution Entry Criteria

| Criterion | Verification Method |
| :--- | :--- |
| `./mvnw clean compile` completes with zero errors | CI build log |
| All `@Mock` fields declared and populated in unit tests (TIR-02) | Code review + `./mvnw test` zero errors |
| E2E test classes tagged `@Tag("e2e")` and excluded from unit-tests profile (TIR-01) | `pom.xml` inspection + CI run |
| `TestSecurityConfig` present and loaded on `test` profile | Integration test run |
| H2 datasource configured in `application-test.properties` | Config review |
| Liquibase changelog compatible with H2 dialect | `./mvnw test` with `@DataJpaTest` |

#### Phase 2 Test Execution Entry Criteria

All Phase 1 exit criteria met, plus:

| Criterion | Verification Method |
| :--- | :--- |
| JaCoCo gate at 0.85 in `pom.xml` (already exceeds the original 0.70 Phase 2 target — verified 2026-07-17, #461) | Config review |
| PIT plugin configured in `pom.xml` | Config review |
| Staging environment provisioned and validated | Environment checklist |
| Frontend test tooling (Vitest, Playwright) installed | Vitest installed and `npm test` succeeds (2026-07-04, `#293`); Playwright E2E still pending |
| Performance environment provisioned | DevOps sign-off |

### 9.2 Exit Criteria

#### Phase 1 Exit Criteria (Stabilization Milestone)

| Criterion | Target | Measurement |
| :--- | :--- | :--- |
| Unit test failures | **0** | `./mvnw test` |
| Unit test errors | **0** | `./mvnw test` |
| Integration test failures | **0** | `./mvnw test -P all-tests` |
| Compilation errors | **0** | `./mvnw clean compile` |
| TIR-01 to TIR-05 all resolved | Confirmed | Code review + CI |
| JaCoCo gate passes at 0.85 (already exceeds the original 0.40 Phase 1 target — verified 2026-07-17, #461) | Pass | `./mvnw verify` |
| `AuthServiceImplTest` errors resolved | 0 errors | `./mvnw test -Dtest=AuthServiceImplTest` |
| Security test assertions correct (401/403, 400/415) | 0 failures | `./mvnw test -Dtest=AuthenticationAuthorizationSecurityTest,InputValidationSecurityTest` |
| E2E tests excluded from CI gate | Confirmed | `pom.xml` + CI log |

#### Phase 2 Exit Criteria (Production Readiness Milestone)

All Phase 1 exit criteria maintained, plus:

| Criterion | Target | Measurement |
| :--- | :--- | :--- |
| JaCoCo INSTRUCTION coverage | **≥ 70%** (already exceeded — real gate is 85%, verified 2026-07-17, #461) | `./mvnw verify -P coverage` |
| PIT mutation score | **≥ 75%** (already exceeded — real gate is 77%, verified 2026-07-17, #461) | `./mvnw pitest:mutationCoverage -P coverage` |
| E2E test failures (staging) | **0** | `./mvnw test -P e2e-tests` |
| Security test OWASP ASVS Level 2 | All items verified | Security test report |
| Performance SLOs | All NFR PR-* met | Gatling report |
| Reliability tests | All circuit breaker scenarios pass | `ReliabilityTest` results |
| Frontend component tests | ≥ 80% pass rate | `npm test` Vitest report |
| Accessibility audit | 0 WCAG Level A/AA violations | axe-core report |
| CSP `unsafe-inline` removed | Confirmed | `SecurityTest` header assertions |

---

## 10. Suspension and Resumption Criteria

### 10.1 Suspension Criteria

Test execution shall be suspended when any of the following conditions arise:

| Condition | Severity | Action |
| :--- | :--- | :--- |
| `./mvnw clean compile` fails — zero test execution possible | **Critical** | All test activity suspended until compilation succeeds |
| H2 schema initialisation failure — integration tests cannot start | **Critical** | Suspend integration testing; file defect; investigate Liquibase H2 compatibility |
| More than 20% of unit tests fail on a single run — indicates environmental or systemic issue | **High** | Suspend until root cause identified; do not treat as individual test defects |
| `TestSecurityConfig` not loaded — security tests return unexpected results | **High** | Suspend security testing; fix profile configuration |
| CI infrastructure outage (GitHub Actions / Jenkins down) | **High** | Suspend CI-gate testing until infrastructure restored |
| Staging database corruption (E2E environment) | **Medium** | Suspend E2E testing; restore staging from last known good snapshot |

### 10.2 Resumption Criteria

Test execution resumes when:

1. The suspension condition has been resolved and verified.
2. The test environment has been confirmed clean (databases reset, caches flushed).
3. A full clean run (`./mvnw clean test`) produces results consistent with pre-suspension baseline.
4. The Test Manager has approved resumption in writing.

---

## 11. Test Deliverables

| Deliverable | Format | Location | Phase |
| :--- | :--- | :--- | :--- |
| This Test Plan | Markdown | `docs/SDLC-docs/software-testing/test-plan.md` | Ph-1 |
| Test Case Specification | Markdown | `docs/SDLC-docs/software-testing/test-case-specification.md` | Ph-1 |
| Test Data Specification | Markdown | `docs/SDLC-docs/software-testing/test-data-specification.md` | Ph-1 |
| Test Execution Report | Markdown | `docs/SDLC-docs/software-testing/test-execution-report.md` | Per cycle |
| JaCoCo Coverage Report | HTML | `backend/target/site/jacoco/index.html` | Per verify run |
| PIT Mutation Report | HTML | `backend/target/pit-reports/index.html` | Ph-2 |
| Surefire XML Reports | XML | `backend/target/surefire-reports/` | Per test run |
| Defect / Bug Report | Markdown | `docs/SDLC-docs/software-testing/defect-report.md` | Per defect |
| Test Summary Report | Markdown | `docs/SDLC-docs/software-testing/test-summary-report.md` | Per milestone |
| Requirements Traceability Matrix | Markdown | `docs/SDLC-docs/software-testing/requirements-traceability-matrix.md` | Ph-1 |
| Performance Test Report | Gatling HTML | `backend/target/gatling/` | Ph-2 |
| Accessibility Audit Report | axe-core JSON / HTML | `frontend/test-results/accessibility/` | Ph-2 |

---

## 12. Roles and Responsibilities

| Role | Responsibilities |
| :--- | :--- |
| **Test Manager** | Owns this Test Plan; approves entry/exit criteria sign-off; escalates suspension conditions; coordinates between QA, Dev, and DevOps |
| **QA Engineer (Backend)** | Authors and executes service, controller, integration, and security test classes; maintains JaCoCo and PIT configuration; files defects |
| **QA Engineer (Frontend)** | Authors and executes Vitest component tests and Playwright E2E scenarios (Phase 2); maintains axe-core accessibility scripts |
| **Developer** | Writes unit tests for new code; resolves TIR-01 through TIR-05 findings; ensures new code maintains or improves JaCoCo coverage |
| **DevOps Engineer** | Provisions and maintains test environments; configures CI pipeline stages; manages Docker Compose and Kubernetes staging manifests |
| **Security Reviewer** | Reviews and approves security test results against OWASP ASVS 4.0 Level 2; validates CSP and header findings |
| **Project Manager** | Monitors test schedule; approves milestone exit; manages scope change requests that affect test scope |

---

## 13. Test Schedule

### 13.1 Phase 1 — Stabilization (Priority: Immediate)

| Task | Effort | Dependency | SRS Req |
| :--- | :--- | :--- | :--- |
| Fix `AuthServiceImplTest` — add `RoleRepository` mock (TIR-02) | 1 h | None | TIR-02 |
| Tag `ProductApiTest` and `OrderApiTest` with `@Tag("e2e")` (TIR-01) | 0.5 h | None | TIR-01 |
| Fix `AuthenticationAuthorizationSecurityTest` — assert 403 not 401 (TIR-03) | 0.5 h | None | TIR-03 |
| Fix `InputValidationSecurityTest` — assert 400/415 not 401 (TIR-04) | 0.5 h | None | TIR-04 |
| Confirm E2E exclusion in `pom.xml` unit-tests profile | 0.5 h | TIR-01 fix | TIR-01 |
| CI gate verification: `./mvnw test` → 0 failures | 0.5 h | All above | All TIR |
| Integration test profile verification: `./mvnw test -P all-tests` | 0.5 h | None | — |
| JaCoCo coverage report baseline: `./mvnw verify` | 0.5 h | — | MNT-02 |
| **Phase 1 test gate sign-off** | 1 h | All above | — |

**Total Phase 1 test effort**: ~5 hours

### 13.2 Phase 2 — Production Readiness

| Task | Effort | Dependency |
| :--- | :--- | :--- |
| Raise JaCoCo gate to 70% in `pom.xml` | 0.5 h | Phase 1 exit |
| Configure PIT plugin in `pom.xml` | 2 h | Phase 1 exit |
| Identify and close coverage gaps (additional unit tests) | 20 h | JaCoCo gap analysis |
| Achieve PIT mutation score ≥ 75% | 10 h | Coverage gate ≥ 70% |
| ~~Remove CSP `unsafe-inline`; update `SecurityTest` assertions~~ — ✅ done (#237 backend, #110 frontend) | 2 h | Phase 1 exit |
| Frontend: Vitest setup + component tests | 15 h | Frontend implementation |
| Frontend: Playwright E2E setup + critical path tests | 10 h | Frontend + staging |
| Accessibility audit (axe-core) + remediation | 5 h | Frontend implementation |
| Performance test execution (Gatling) + report | 8 h | Staging environment |
| Reliability circuit breaker drill + `ReliabilityTest` verification | 4 h | Staging environment |
| Security OWASP ASVS Level 2 verification | 8 h | Staging environment |
| **Phase 2 test gate sign-off** | 2 h | All above |

**Total Phase 2 test effort**: ~87 hours

---

## 14. Risks and Mitigations

| Risk ID | Risk | Likelihood | Impact | Mitigation |
| :--- | :--- | :--- | :--- | :--- |
| TR-01 | H2 dialect incompatibility with MySQL-specific Liquibase changesets | Medium | High | Use `dbms="mysql"` context tags on MySQL-only changesets; H2-compatible changesets for the rest; test on H2 in CI and MySQL in staging |
| TR-02 | JaCoCo 70% gate not achievable without significant test additions | Medium | High | Start gap analysis immediately; prioritise high-business-value services (auth, checkout, payment); phase the gate increase incrementally |
| TR-03 | Flaky E2E tests due to race conditions or timing assumptions | High | Medium | Use `@Tag("e2e")` isolation; avoid hardcoded sleeps; use explicit wait conditions; retry on transient 500 errors via RestAssured configuration |
| TR-04 | Staging environment unavailable for E2E test execution | Medium | High | CI gate uses only H2-backed unit/integration tests; E2E is non-blocking for CI; maintain docker-compose as fast-start alternative |
| TR-05 | PIT mutation testing too slow for CI gate | High | Low | Run PIT only on `coverage` profile, not default; exclude entity and config classes; use Arcmutate incremental mutation where available |
| TR-06 | `TestSecurityConfig` diverges from `SecurityConfig` — security tests pass in CI but fail in production | Low | Critical | Periodic review: compare `TestSecurityConfig` and `SecurityConfig` on each security-related PR; fail-fast validation tests confirm runtime behaviour |
| TR-07 | Redis mock behaviour diverges from real Redis (rate limiting tests) | Medium | Medium | Integration test `RateLimiterServiceTest` uses Mockito stub; supplement with `@SpringBootTest` + embedded Redis (Testcontainers) in Phase 2 |
| TR-08 | Liquibase validation failure (`ddl-auto=validate`) after entity changes | Medium | High | All schema changes must have a Liquibase changeset before merging the corresponding entity change; pre-merge CI validates against H2 |
| TR-09 | Test data interference between parallel test classes | Low | Medium | `@DataJpaTest` tests run in a transaction rolled back after each test; `@SpringBootTest` tests use `@DirtiesContext` where shared state is modified |
| TR-10 | Frontend test suite non-existent at Phase 1 start | High (known) | Medium | **Resolved 2026-07-04** — Vitest + React Testing Library installed (`#293`); baseline tests added for `AuthContext` (session restore, login, logout, expired-token handling), `useCart`, and `api/cart`. Remaining Phase 2 work: Playwright E2E, axe-core accessibility, and expanding component coverage toward the 80% gate. |

---

## 15. Test Integrity Requirements

The Baseline Assessment identified systemic defects in the test suite itself — not in the production code under test. These Test Integrity Requirements (TIR) are a formal quality category in SRS v4.0 §10 and must be resolved before Phase 1 exit criteria can be met.

| Requirement ID | Description | Current State | Remediation |
| :--- | :--- | :--- | :--- |
| **TIR-01** | E2E tests must be excluded from the `unit-tests` Maven profile. Classes `ProductApiTest` and `OrderApiTest` (and any future E2E tests) must be annotated `@Tag("e2e")`. The `unit-tests` profile `pom.xml` must specify `<excludedGroups>e2e,stress,integration</excludedGroups>`. | **Resolved** (verified 2026-07-17 13:55 IST, #461) — both classes confirmed `@Tag("e2e")`-annotated, now in a dedicated `e2e/` test package | — |
| **TIR-02** | All `@Mock`-annotated fields in unit test classes must be declared and the annotated field must be populated by `@ExtendWith(MockitoExtension.class)` or `MockitoAnnotations.openMocks(this)`. Tests must not pass a null dependency into the SUT. | **Resolved** (verified 2026-07-17 13:55 IST, #461) — `@Mock RoleRepository roleRepository` confirmed present in `AuthServiceImplTest` | — |
| **TIR-03** | Tests that verify authorisation enforcement must assert HTTP **403 Forbidden** when an authenticated user lacks the required role. HTTP 401 Unauthorized is the correct response only for *unauthenticated* requests. | **Resolved** (verified 2026-07-17 13:55 IST, #461) — `testRoleHierarchyEnforcement` confirmed asserting `status().isForbidden()` | — |
| **TIR-04** | Tests that exercise input validation must accept HTTP **400 Bad Request** (invalid request body) or **415 Unsupported Media Type** (wrong Content-Type) as valid outcomes. Input validation is enforced before authentication in the filter chain. | **Resolved** (verified 2026-07-17 13:55 IST, #461) — `testXSSPrevention`/`testFileUploadValidation` confirmed asserting `isBadRequest()`/`isUnsupportedMediaType()` respectively | — |
| **TIR-05** | Mutation test score (PIT) for the `service` and `security` packages must be **≥ 75%**. This ensures the test suite detects a meaningful proportion of injected code mutations and is not merely achieving line coverage via trivial assertions. | **Partial** (verified 2026-07-17 13:55 IST, #461) — `pitest-maven` is configured with `mutationThreshold` 77%, already exceeding this 75% floor; ratchet continues toward the M4 milestone's own 79% end-M4 target | Track ratchet progress toward 79% per M4 milestone description |

---

## 16. Test-to-Requirements Traceability

The table below maps SRS v4.0 requirement groups to the test classes that verify them. A full Requirements Traceability Matrix (RTM) is maintained separately in `docs/SDLC-docs/software-testing/requirements-traceability-matrix.md`.

| SRS Section | Requirement IDs | Primary Test Classes |
| :--- | :--- | :--- |
| Authentication | FR-AUTH-01 to FR-AUTH-11 | `AuthControllerTest`, `AuthServiceImplTest`, `AuthApiTest`, `JwtTokenProviderTest`, `JwtAuthenticationFilterTest`, `RefreshTokenServiceTest`, `JwtKeyValidatorTest` |
| Password Reset | FR-AUTH-08 | `PasswordResetControllerTest`, `PasswordResetServiceImplTest` |
| Product Catalog | FR-PROD-01 to FR-PROD-07 | `ProductControllerV1Test`, `ProductControllerV2Test`, `ProductServiceImplTest`, `ProductRepositoryTest`, `ApiSunsetInterceptorTest`, `ProductApiTest` (E2E) |
| Cart | FR-CART-01 to FR-CART-06 | `CartControllerTest`, `CartServiceImplTest`, `CartRepositoryTest`, `CartApiTest` (E2E) |
| Checkout | FR-CHK-01 to FR-CHK-08 | `CheckoutControllerTest`, `CheckoutServiceImplTest`, `OrderApiTest` (E2E) |
| Payment | FR-PAY-01 to FR-PAY-05 | `PaymentServiceImplTest`, `PaymentProcessingTest`, `PaymentSignatureValidationServiceTest`, `RazorpayClientAdapterTest` |
| Inventory | FR-INV-01 to FR-INV-07 | `InventoryStatusControllerTest`, `InventoryServiceImplTest`, `InventoryManagementTest`, `InventoryThresholdManagementServiceTest` |
| Wishlist | FR-WISH-01 to FR-WISH-04 | `WishlistControllerTest`, `WishlistServiceImplTest` |
| Reviews | FR-REV-01 to FR-REV-05 | `ProductReviewControllerTest`, `ProductReviewServiceImplTest` |
| Categories | FR-CAT-01 to FR-CAT-04 | `CategoryManagementTest`, `CategoryServiceImplTest`, `CategoryRepositoryTest` |
| Admin | FR-ADM-01 to FR-ADM-08 | `AdminProductControllerTest`, `AdminOrderControllerTest`, `AdminUserControllerTest`, `AdminAnalyticsControllerTest`, `AdminDashboardTest`, `AnalyticsReportingTest` |
| Webhooks | FR-ADM-07 | `WebhookAdminControllerTest`, `WebhookServiceImplTest` |
| Notifications | FR-NOT-01 to FR-NOT-03 | `NotificationServiceTest`, `DomainEventListenerTest` |
| Monitoring | FR-MON-01 to FR-MON-08 | `HealthIndicatorTest`, `DatabaseHealthIndicatorTest`, `RedisHealthIndicatorTest`, `PerformanceMetricsControllerTest`, `PoolMetricsControllerTest` |
| Security | SEC-01 to SEC-14 | `AuthenticationAuthorizationSecurityTest`, `InputValidationSecurityTest`, `SecurityTest`, `AdminRateLimitFilterTest`, `RateLimiterServiceTest`, `JwtKeyValidatorTest`, `CustomUserDetailsTest`, `RolePermissionEvaluatorTest` |
| Performance | PR-01 to PR-07 | `PerformanceTest`, `PerformanceBaselineTest`, `LoadTestSimulation` (Gatling) |
| Reliability | REL-01 to REL-04, AVL-01 to AVL-04 | `ReliabilityTest`, `ReliabilityHATest` |
| Auditability | MNT-05 | `AuditAspectTest`, `AuditLogServiceTest`, `AuditLogControllerTest` |
| Maintainability | MNT-01 to MNT-06 | `LoggingStandardsTest`, `DeadCodeAnalyzerTest`, JaCoCo, PIT |
| Test Integrity | TIR-01 to TIR-05 | Structural (pom.xml + test class annotations) |
| Frontend | FR-FE-01 to FR-FE-30 | Vitest, Playwright (Phase 2) |
| Accessibility | ACC-01, ACC-02 | axe-core (Phase 2) |
| RBAC | FR-AUTH-10, FR-AUTH-11 | `RBACTest`, `RolePermissionEvaluatorTest` |

---

## 17. Coverage Metrics and Acceptance Gates

### 17.1 Coverage Targets by Phase

| Metric | Tool | Phase 1 Gate | Phase 2 Gate | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| Line coverage (backend) | JaCoCo | ≥ 85% PACKAGE/INSTRUCTION (verified 2026-07-17 13:55 IST, #461 — `jacoco-check` rule), already exceeding the Phase 2 target | **≥ 70%** | MNT-02 |
| Instruction coverage (backend) | JaCoCo | Reported only | ≥ 65% | MNT-02 |
| Branch coverage (backend) | JaCoCo | Reported only | ≥ 60% | MNT-02 |
| Mutation score (backend service + security) | PIT | Active at 77% `mutationThreshold` (verified 2026-07-17 13:55 IST, #461), ratcheting to 79% per M4 milestone | **≥ 75%** | TIR-05 |
| E2E critical path coverage | Manual | Key flows identified | 100% critical paths pass | FR-* |
| Frontend component coverage | Vitest | 45 test files, 281 tests, across components/hooks/API modules (verified 2026-07-30, periodic 15-issue sync via `npx vitest run`, all passing) | ≥ 80% statements | FR-FE-* |
| WCAG 2.1 AA violations | axe-core | Not yet active | 0 violations | ACC-01 |

### 17.2 Current Baseline (Re-verified 2026-07-30, periodic 15-issue sync)

Superseding the original 2026-06-19 Baseline Assessment Report figures below, re-measured directly via a clean `./mvnw test -P all-tests` run (`env -i` isolated shell, to avoid the environment-contamination false alarm documented in the `exported-env-vars-can-leak-across-separate-bash-tool-calls-contaminating-later-test-runs.md` wiki lesson) and `./mvnw dependency:tree`/`pom.xml` inspection:

| Metric | Current State | Gap to Phase 2 Target |
| :--- | :--- | :--- |
| Total test files | 216 | — |
| Total test executions (last full run) | 1,893 | — |
| Passed | 1,893 (100%) | — |
| Failed | 0 | None |
| Errors | 0 | None |
| JaCoCo LINE coverage | 85% PACKAGE/INSTRUCTION (`jacoco-check` rule) | Already exceeds 70% target |
| Mutation score | Active — `mutationThreshold` 77% | Already exceeds 75% requirement; ratcheting to 79% per M4 milestone |

### 17.3 Coverage Exclusions

The following packages are excluded from JaCoCo coverage enforcement:

| Excluded Pattern | Reason |
| :--- | :--- |
| `com/example/buildnest_ecommerce/model/entity/**` | Lombok-generated `equals`, `hashCode`, `toString`, `builder` methods |
| `com/example/buildnest_ecommerce/model/dto/**` | Lombok-generated boilerplate |
| `com/example/buildnest_ecommerce/model/payload/**` | Lombok-generated boilerplate |
| `com/example/buildnest_ecommerce/BuildnestEcommerceApplication.java` | Spring Boot main entry point — not unit-testable in isolation |
| `com/example/buildnest_ecommerce/config/**` | Spring `@Configuration` classes — verified by integration tests, not counted in line coverage |

---

## 18. Appendices

### Appendix A: Test Execution Commands Reference

```bash
# Default CI gate (unit + slice tests; excludes e2e, stress, integration)
./mvnw test

# Full non-E2E test suite (unit + integration; excludes e2e, stress)
./mvnw test -P all-tests

# E2E tests only (requires running server)
./mvnw test -P e2e-tests

# Stress / load tests only
./mvnw test -P stress-tests

# Coverage verification (all-tests profile + JaCoCo check)
./mvnw verify

# Coverage report generation without gate enforcement
./mvnw verify -DskipTests=false -Djacoco.skip=false

# Run a single test class
./mvnw test -Dtest=OrderServiceImplTest

# Run a single test method
./mvnw test -Dtest=OrderServiceImplTest#shouldPlaceOrder

# PIT mutation testing (Phase 2)
./mvnw org.pitest:pitest-maven:mutationCoverage -P coverage

# Open JaCoCo HTML report (macOS)
open backend/target/site/jacoco/index.html

# Open JaCoCo HTML report (Linux/WSL)
xdg-open backend/target/site/jacoco/index.html
```

### Appendix B: Phase 1 Defect Summary (Open)

The following defects are confirmed by the Baseline Assessment and must be resolved before Phase 1 exit:

| Defect ID | File | Symptom | Root Cause | SRS/TIR |
| :--- | :--- | :--- | :--- | :--- |
| DEF-001 | `AuthServiceImplTest.java` | `NullPointerException` in 3 test methods | `roleRepository` not declared as `@Mock` | TIR-02 |
| DEF-002 | `ProductApiTest.java` | HTTP 500 — expected 200 in 4 test methods | E2E test running without server in unit-tests profile | TIR-01 |
| DEF-003 | `OrderApiTest.java` | HTTP 500 (presumed same) | Same as DEF-002 | TIR-01 |
| DEF-004 | `AuthenticationAuthorizationSecurityTest.java` | 403 received, 401 asserted | Test expectation wrong — 403 is correct for authenticated+unauthorised | TIR-03 |
| DEF-005 | `InputValidationSecurityTest.java:168` | 400 received, 401 asserted | Input validation fires before auth; 400 is correct | TIR-04 |
| DEF-006 | `InputValidationSecurityTest.java:303` | 415 received, 401 asserted | Content-Type validation fires before auth; 415 is correct | TIR-04 |

### Appendix C: Test Profile Configuration (pom.xml Summary)

```xml
<!-- Profile: unit-tests (default) — fast CI gate -->
<profile>
  <id>unit-tests</id>
  <activation><activeByDefault>true</activeByDefault></activation>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <excludedGroups>e2e,stress,integration</excludedGroups>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>

<!-- Profile: all-tests — full suite without live server -->
<profile>
  <id>all-tests</id>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <excludedGroups>e2e,stress</excludedGroups>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>

<!-- Profile: e2e-tests — requires running server -->
<profile>
  <id>e2e-tests</id>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <groups>e2e</groups>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>

<!-- Profile: stress-tests — performance environment only -->
<profile>
  <id>stress-tests</id>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <groups>stress</groups>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>
```

---

**— End of Document —**

*This document was prepared in conformance with ISO/IEC/IEEE 29119-3:2021 for the BuildNest E-Commerce Platform. It supersedes TP v3.0 archived at `archive/docs/ISO-IEC-IEEE/Test_Plan_IEEE_29119.md`. All corrections in v4.0 are evidence-based and traceable to the Baseline Assessment Report (`docs/reports/baseline-assessment-2026-06-19.md`), verified against the live codebase and Maven `pom.xml` as of 2026-06-19.*

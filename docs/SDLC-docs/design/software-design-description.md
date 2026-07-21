# Software Design Description (SDD)

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## DOCUMENT INFORMATION

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Software Design Description (SDD) |
| **Document ID** | SDD-BUILDNEST-001 |
| **Version** | 3.7 |
| **Date** | 2026-07-21 IST |
| **Status** | Controlled — Under Review |
| **Classification** | Internal Use |
| **Conformance Standard** | ISO/IEC/IEEE 1016:2017 |
| **Related SRS** | SRS-BUILDNEST-001 v4.9 (docs/SDLC-docs/requirement-engineering/software-requirements-specification.md) |
| **Supersedes** | SDD v2.0 (archive/docs/ISO-IEC-IEEE/SDD_IEEE_1016_2017.md, 2026-02-11) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date | Author | Changes | Approval |
| :--- | :--- | :--- | :--- | :--- |
| 1.0 | 2026-02-10 | Documentation Team | Initial controlled release per ISO/IEC/IEEE 1016:2017 | Approved |
| 2.0 | 2026-02-11 | Documentation Team | Fixed pool rows; added Wishlist/Review/Admin SRS traceability | Approved |
| 3.0 | 2026-06-19 | Software Architect | Baseline-driven update: corrected Spring Boot to 3.5.10; updated component counts from static analysis (256 source files, 173 test files); corrected circuit breaker thresholds from live configuration; added JwtTokenProvider dual-key rotation design; added `@Profile("!test")` SecurityConfig constraint; corrected Kubernetes resource limits from manifest (512Mi request / 1Gi limit); aligned all design elements with SRS-BUILDNEST-001 v4.0; referenced Baseline Assessment Report | Pending |
| 3.1 | 2026-07-17 13:53 IST | Software Architect | Recomputed all 12 rows of §4.2.3's Component Statistics table directly via the `find`/`grep` commands the table itself cites — every metric had drifted upward as real features shipped since the 2026-06-19 baseline (e.g. 256→352 source files, 29→38 controllers, 19→28 repositories); corrected Jedis→Lettuce and Elasticsearch 8.10→8.17 references throughout (§458). The 6 sections still framing the entire frontend design as "Design Intent — Phase 2" were found separately stale (the frontend is real and substantial) and filed as their own follow-up (#459) rather than fixed here, since correcting them requires authoring real design content, not a relabel | Pending |
| 3.2 | 2026-07-17 20:10 IST | Software Architect | Full re-derivation (not a relabel) of the 6 sections flagged by #459 as still framing the real, deployed frontend as unbuilt/aspirational: §1.2 Scope and §4.1.1's context diagram drop the "design intent"/"(Phase 2)" framing; §4.2.2's package structure rewritten to the real `frontend/src/` layout (`api/`, `components/{account,admin,cart,checkout,common,filters,product}/`, `contexts/`, `hooks/`, `pages/`, `types/`, `test/` — no `core/`/`auth/`/`admin/`/`router/`/`services/`); §4.3.6's component model rewritten around the actual `useAsync` hook, `RequireAuth` guard, and Tailwind-only styling (no component library, no Redux/React Query); §4.5.5's state model corrected to `AuthContext` + hook-local state (`useCart`, `useAsync`) — no `CartContext`, no server-cache library; §4.7.4's routes rewritten to match `App.tsx`'s real `react-router-dom` v7 routes and RTM's FR-FE mapping (tabs inside `AccountPage`/`AdminDashboardPage`, not standalone routes); §4.10.5 rewritten against the actual `frontend/Dockerfile`/`nginx.conf` (`nginx-unprivileged`, healthcheck, real cache-control directives) | Pending |
| 3.3 | 2026-07-17 19:11 IST | Software Architect | §4.2.3-adjacent traceability row for "React SPA design" still cited `FR-FE-01–30`, never updated when #450 added `FR-FE-31` (#470) — corrected to `FR-FE-01–31`. Also corrected the `Related SRS` cross-reference from a long-stale v4.0 to the current v4.4, which had drifted through 4 intervening SRS version bumps without ever being updated here | Pending |
| 3.4 | 2026-07-17 20:45 IST | Software Architect | Full re-derivation of §4.7.3's API Endpoint Catalogue (#471), same staleness class already fixed in SRS Appendix A (#456): 7 stale sections (wrong path prefixes — `/api/auth/refresh-token` instead of real `/api/auth/refresh`, `/api/auth/forgot-password`/`reset-password` instead of real `/api/password/forgot`/`reset`; only legacy `/api/checkout/*`, missing the current `/api/v1/checkout/*` multi-step flow entirely) expanded to 36 groups, each citing its real controller class. Unlike Appendix A, this table also carries a Rate Limit column per row — verified directly against `RateLimitHeaderInterceptor`/`AdminRateLimitFilter`/`RateLimitUtil` source and `application.properties`, surfacing a previously-undocumented gap: `/api/v1/admin/**` (base path for most admin resource controllers — products, categories, tags, coupons, shipping-methods, search, orders, inventory, sales analytics) matches neither the interceptor's nor the filter's literal `/api/admin/` prefix check, so those endpoints receive only the 100/min default header and no dedicated admin-tier blocking, unlike the literal `/api/admin/**` groups (users, analytics, audit, webhooks, monitoring, thresholds, inventory-threshold/analytics/reports) which get 30/min headers + a real 50/min block | Pending |
| 3.5 | 2026-07-17 21:15 IST | Software Architect | Found during a fresh RTM/SRS/SDD/Test-Plan verification sweep: `Related SRS` had drifted one version behind again (v4.4, SRS is now v4.5 following #474) — the same recurring cross-reference-currency gap already fixed twice before at 3.3/3.4. Updated to current | Pending |
| 3.6 | 2026-07-19 10:40 IST | Software Architect | §4.7.3's checkout endpoint catalogue row for `POST /api/v1/checkout/coupon` cited the wrong SRS requirement (`FR-CHK-02`, "calculate checkout total") — corrected to the newly-added `FR-CHK-09` (apply coupon during checkout, #436), which had never existed as a row until this issue added it to both SRS and RTM. Updated `Related SRS` from v4.5 to v4.9 (only the one edge this change directly touches — a full cross-reference-mesh sweep is the periodic 15-issue sync's job, not a per-issue one) | Pending |
| 3.7 | 2026-07-21 IST | Software Architect | Added a Dead-Code Audit Decision Record to §4.7.3 (#448) for four zero-frontend-caller endpoint groups (`ProductControllerV1`, `ProductControllerV2`, legacy `CheckoutController`, `/auth/validate-token`) — audited each against `frontend/src/api/{products,checkout}.ts` and `ApiSunsetInterceptor` directly; none removed on this pass (product-scope calls about unbuilt external/mobile consumers, not code-cleanup calls). Surfaced and filed as follow-ups: #535 (V2/`HomeController` "Current"/"Legacy" label contradiction — V2 is labeled current but carries zero traffic) and #536 (revisit V1 removal once its 2026-12-31 sunset passes) | Pending |

### Document Approval

| Role | Name | Signature | Date |
| :--- | :--- | :--- | :--- |
| Project Manager | _____________ | _____________ | _____________ |
| Technical Lead | _____________ | _____________ | _____________ |
| Software Architect | _____________ | _____________ | _____________ |
| QA Manager | _____________ | _____________ | _____________ |

### Document Change Procedure

All changes shall follow the SRS change control process (see SRS-BUILDNEST-001 §1.7). Design changes that affect SRS requirements must be reflected in both documents and the RTM.

---

## CONFORMANCE STATEMENT

> This document conforms to **ISO/IEC/IEEE 1016:2017** — *Systems and software engineering — Software design descriptions*. It addresses design stakeholder concerns through ten distinct viewpoints (Context, Composition, Logical, Dependency, Information, Patterns Use, Interface, Interaction, State Dynamics, Resource) and three cross-cutting design overlays (Security, Error Handling, Resilience), as specified in Clause 5.

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Design Stakeholders and Concerns](#2-design-stakeholders-and-concerns)
3. [Design Viewpoints](#3-design-viewpoints)
4. [Design Views](#4-design-views)
   - 4.1 [Context View](#41-context-view)
   - 4.2 [Composition View](#42-composition-view)
   - 4.3 [Logical View](#43-logical-view)
   - 4.4 [Dependency View](#44-dependency-view)
   - 4.5 [Information View](#45-information-view)
   - 4.6 [Patterns Use View](#46-patterns-use-view)
   - 4.7 [Interface View](#47-interface-view)
   - 4.8 [Interaction View](#48-interaction-view)
   - 4.9 [State Dynamics View](#49-state-dynamics-view)
   - 4.10 [Resource View](#410-resource-view)
5. [Design Overlays](#5-design-overlays)
   - 5.1 [Security Overlay](#51-security-overlay)
   - 5.2 [Error Handling Overlay](#52-error-handling-overlay)
   - 5.3 [Resilience Overlay](#53-resilience-overlay)
6. [Design Rationale](#6-design-rationale)
7. [Design-to-Requirements Traceability](#7-design-to-requirements-traceability)
8. [Appendices](#8-appendices)

---

## 1. Introduction

### 1.1 Purpose

This Software Design Description (SDD) describes the architecture and detailed design of the **BuildNest E-Commerce Platform**. It is prepared in conformance with **ISO/IEC/IEEE 1016:2017** — *Systems and software engineering — Software design descriptions*.

This document:

- Describes the internal construction of the software through ten design viewpoints and three design overlays.
- Establishes traceability between design elements and SRS requirements (SRS-BUILDNEST-001 v4.0).
- Documents design patterns, component interactions, data models, state machines, and deployment topology.
- Records the rationale for key architectural and technology decisions.
- Serves as the authoritative design reference for developers, reviewers, and maintainers.

**Version 3.0 Change Rationale**: This version incorporates corrections and updates driven by the Baseline Assessment Report (docs/reports/baseline-assessment-2026-06-19.md). All design element counts, version references, configuration values, and resource limits have been verified against the live codebase. No design decisions have been reversed; corrections are limited to factual accuracy.

### 1.2 Scope

This SDD covers the design of the **BuildNest** platform in its entirety:

**In Scope**:
- Backend REST API (Spring Boot 3.5.10, Java 21) — layered monolithic architecture
- Frontend Application (React 19.2, Vite 8.0) — Single Page Application (`frontend/src/`, 71 source files: home, product listing/detail, cart, checkout, order confirmation, login/register, forgot/reset-password, account dashboard, admin dashboard)
- Security chain — JWT authentication, RBAC, rate limiting, secure HTTP headers
- External integrations — MySQL 8.2, Redis 7, Elasticsearch 8.17, Razorpay
- Resilience patterns — Resilience4j circuit breakers, time limiters
- Deployment topology — Docker, Kubernetes, Prometheus observability stack

**Out of Scope**:
- Third-party service internals (Razorpay payment processing logic)
- Physical infrastructure management
- Native mobile applications (iOS / Android) — deferred to v2.0

### 1.3 Audience

| Audience | Design Interest |
| :--- | :--- |
| Software Developers | Package structure, class design, API contracts, data flow, naming conventions |
| System Architects | Architectural decisions, design patterns, coupling / cohesion, trade-offs |
| QA Engineers | Component boundaries, interface contracts, state machines, error paths, testability |
| DevOps Engineers | Deployment topology, resource configuration, health endpoints, configuration externalisation |
| Security Team | Security filter chain, authentication design, RBAC matrix, rate limiting, audit trail |
| Technical Writers | Component descriptions, interaction sequences, API surface |

### 1.4 Definitions and Acronyms

| Term | Definition |
| :--- | :--- |
| **Design View** | A representation of the system from a specific design perspective |
| **Design Viewpoint** | A specification of conventions for constructing and using a view |
| **Design Element** | An item in a design — component, class, interface, module |
| **Design Overlay** | A cross-cutting concern that spans multiple design views |
| **AggregateRoot** | DDD marker interface identifying entities that are aggregate boundaries |
| **Circuit Breaker** | Resilience pattern preventing cascading failures by short-circuiting calls to failing dependencies |
| **DTO** | Data Transfer Object — separates API response shape from JPA entity shape |
| **RBAC** | Role-Based Access Control |
| **SPA** | Single Page Application |
| **AOP** | Aspect-Oriented Programming |
| **HPA** | Horizontal Pod Autoscaler (Kubernetes) |

See also: SRS-BUILDNEST-001 §1.5 for full acronym list.

### 1.5 References

| ID | Document | Version |
| :--- | :--- | :--- |
| REF-01 | ISO/IEC/IEEE 1016:2017 — Software Design Descriptions | 2017 |
| REF-02 | ISO/IEC/IEEE 42010:2022 — Architecture Description | 2022 |
| REF-03 | SRS-BUILDNEST-001 — BuildNest Software Requirements Specification | 4.0 |
| REF-04 | BuildNest Baseline Assessment Report | 2026-06-19 |
| REF-05 | Spring Boot Reference Documentation | 3.5.10 |
| REF-06 | Resilience4j Documentation | 2.1.0 |
| REF-07 | JJWT Documentation | 0.12.3 |
| REF-08 | Bucket4j Documentation | 8.1.0 |
| REF-09 | Razorpay API Documentation | Latest |
| REF-10 | OWASP Application Security Verification Standard | 4.0 |

---

## 2. Design Stakeholders and Concerns

Per ISO/IEC/IEEE 1016:2017 Clause 5.3:

| Stakeholder | Design Concerns |
| :--- | :--- |
| **Developers** | Package organisation, naming conventions, dependency injection, service interface pattern, testability via mock boundaries |
| **Architects** | Layered architecture integrity, design patterns applied, scalability approach, coupling and cohesion, technology selection |
| **QA Engineers** | Component isolation, test profile separation (`@Profile("!test")`), state transitions, error paths, mock-friendly interfaces |
| **DevOps** | Containerisation, Kubernetes manifests, health endpoints, configuration externalisation, graceful shutdown, HPA behaviour |
| **Security Team** | Filter chain order, JWT dual-key rotation, BCrypt rounds, RBAC enforcement, rate limit strategy, CSP headers, audit trail |
| **Product Owner** | Feature completeness per FG-01 through FG-10, API versioning strategy, extensibility for future requirements |

---

## 3. Design Viewpoints

Per ISO/IEC/IEEE 1016:2017 Clause 5.5:

| Viewpoint | Purpose | Design Languages Used |
| :--- | :--- | :--- |
| **Context** | System boundary, external actors, and interface characteristics | ASCII diagram, table |
| **Composition** | Decomposition into packages, layers, and modules | ASCII tree, table |
| **Logical** | Classes, interfaces, and their structural relationships | Class diagram (text), table |
| **Dependency** | Component dependencies and third-party library usage | Dependency graph (text), table |
| **Information** | Data models, entity relationships, data flow, cache strategy | ER diagram (text), tables |
| **Patterns Use** | Design patterns applied and their locations | Table, descriptions |
| **Interface** | API contracts, endpoint catalogue, standard response formats, frontend routes | Tables |
| **Interaction** | Component interactions for key use cases | Sequence diagrams (text) |
| **State Dynamics** | State machines for stateful business entities | State diagrams (text), tables |
| **Resource** | Deployment topology, infrastructure resources, thread and connection pools | Topology diagram (text), tables |

---

## 4. Design Views

### 4.1 Context View

#### 4.1.1 System Context

```
┌─────────────────────────────────────────────────────────────────────┐
│  CLIENT TIER                                                        │
│  Web Browser (Chrome 90+, Firefox 90+, Edge 90+, Safari 15+)       │
│  Mobile Browser                                                     │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTPS (React SPA served via Nginx)
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│  FRONTEND                                                           │
│  BuildNest React 19.2 SPA — Vite 8.0 — nginx-unprivileged Alpine   │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTPS / REST / JSON
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│  BACKEND API                                                        │
│  BuildNest Spring Boot 3.5.10  :8080  Java 21                       │
└──────┬────────────┬────────────┬──────────┬──────────┬─────────────┘
       │ JDBC       │ Lettuce    │ HTTP     │ HTTPS    │ TCP/JSON
       ▼            ▼            ▼          ▼          ▼
   MySQL 8.2    Redis 7    Elastic 8.17  Razorpay  Logstash
   :3306        :6379       :9200         API        :5000
                                           │
                                    Prometheus (:9090)
                                    scrapes /actuator/prometheus
```

#### 4.1.2 External System Interfaces

| External System | Protocol | Port | Purpose | Failure Mode |
| :--- | :--- | :--- | :--- | :--- |
| MySQL 8.2 | JDBC / TCP | 3306 | Primary data store | **Critical** — Circuit breaker (50% threshold, 8 s timeout) |
| Redis 7 | RESP / TCP | 6379 | Cache, rate limiting | **Degraded** — Circuit breaker (70% threshold, 3 s timeout); fail-open on rate limit |
| Elasticsearch 8.17 | HTTP | 9200 | Search, analytics, audit log ingestion | **Optional** — Disabled by default (`elasticsearch.enabled=false`) |
| Razorpay | HTTPS | 443 | Payment processing | **Degraded** — Non-payment checkout path still operational |
| Prometheus | HTTP | 9090 | Metrics scraping from `/actuator/prometheus` | **None** — Passive consumer; no impact on application |
| Logstash | TCP | 5000 | Structured JSON log aggregation | **None** — Logs fall back to file (`logs/buildnest-ecommerce.log`) |

---

### 4.2 Composition View

#### 4.2.1 Backend Package Structure

```
com.example.buildnest_ecommerce/          [Root — 256 Java source files]
├── BuildnestEcommerceApplication.java    [Spring Boot entry point]
├── aspect/                               [@Auditable AOP aspect]
│   ├── Auditable.java                    [Custom annotation]
│   └── AuditAspect.java                 [@Around advice — audit logging]
├── config/                               [38 configuration classes]
│   ├── SecurityConfig.java              [@Profile("!test") security chain]
│   ├── CacheConfig.java                 [Redis CacheManager, TTL per region]
│   ├── RateLimitConfig.java             [Bucket4j token bucket configuration]
│   ├── ResilienceConfig.java            [Resilience4j circuit breakers]
│   ├── ElasticsearchConfig.java         [ES client — disabled by default]
│   ├── WebMvcConfig.java                [MVC interceptors]
│   └── properties/                       [8 @ConfigurationProperties classes]
├── controller/                           [29 controller classes]
│   ├── admin/                            [14 admin controllers]
│   ├── auth/                             [2 auth controllers]
│   ├── user/                             [6 user-facing controllers]
│   ├── inventory/                        [1 inventory status controller]
│   ├── monitoring/                       [2 performance/pool controllers]
│   └── public_/                          [1 home controller]
├── exception/                            [11 exception classes]
│   ├── BuildNestException.java          [Base custom exception]
│   ├── GlobalExceptionHandler.java      [@RestControllerAdvice]
│   └── [9 domain-specific exceptions]
├── interceptor/                          [3 HTTP interceptors]
│   ├── ApiSunsetInterceptor.java        [Deprecation/Sunset headers on v1]
│   ├── PerformanceMonitoringInterceptor [Request timing, slow-query logging]
│   └── RateLimitHeaderInterceptor       [X-RateLimit-* response headers]
├── model/
│   ├── entity/                           [32 @Entity classes]
│   ├── dto/                              [Output DTOs]
│   ├── payload/                          [Request/response payloads]
│   └── elasticsearch/                    [Elasticsearch document models]
├── monitoring/                           [Metrics collection]
├── rbac/                                 [RolePermissionEvaluator]
├── repository/                           [19 Spring Data repositories]
├── security/                             [Security filter chain]
│   ├── AdminRateLimitFilter.java        [Admin endpoint rate limiting]
│   ├── CustomUserDetails.java           [Spring Security user principal]
│   ├── CustomUserDetailsService.java    [Loads user + permissions]
│   ├── HttpsEnforcementFilter.java      [HTTP → HTTPS redirect (production)]
│   └── Jwt/
│       ├── JwtAuthenticationEntryPoint  [401 on unauthenticated requests]
│       ├── JwtAuthenticationFilter      [Extracts + validates JWT per request]
│       └── JwtTokenProvider.java        [Token generation, validation, rotation]
├── service/                              [46 @Service classes]
│   ├── admin/    auth/    cart/    category/    checkout/
│   ├── elasticsearch/    inventory/    notification/    order/
│   ├── payment/    product/    ratelimit/    review/
│   ├── token/    user/    webhook/    wishlist/
│   └── password/
├── util/                                 [13 utility classes]
└── validation/                           [5+ validation classes]
```

#### 4.2.2 Frontend Package Structure (Verified — 2026-07-17, #459)

```
frontend/src/                [71 source files, verified via `find frontend/src -name "*.ts*" | wc -l`]
├── api/                     [Fetch wrappers, one file per domain: client, auth, admin, cart,
│                             categories, checkout, orders, products, reviews, user, wishlist,
│                             addresses — plus a *.test.ts alongside several of them]
├── components/
│   ├── account/             [Account dashboard sub-components]
│   ├── admin/                [Admin dashboard sub-components]
│   ├── cart/                 [Cart page sub-components]
│   ├── checkout/             [Checkout page sub-components]
│   ├── common/                [Navbar, ErrorBoundary, RequireAuth — shared across pages]
│   ├── filters/               [Product listing filter controls]
│   └── product/                [ProductCard and related product-display components]
├── contexts/                [AuthContext.tsx — the only React Context in use, plus its test]
├── hooks/                    [useAsync (generic fetch/loading/error hook), useAuth, useCart,
│                             useCategories, useFeaturedProducts, useProduct, useProducts,
│                             useReviews]
├── pages/                    [13 route-level page components — see §4.7.4 for the route map:
│                             HomePage, ProductListingPage, ProductDetailPage, CartPage,
│                             CheckoutPage, OrderConfirmationPage, LoginPage, RegisterPage,
│                             ForgotPasswordPage, ResetPasswordPage, AccountPage,
│                             AdminDashboardPage, NotFoundPage]
├── types/                    [Shared TypeScript type definitions]
├── test/                     [Test setup/utilities shared across the suite]
├── assets/                   [Static assets]
├── App.tsx                   [Root component — BrowserRouter + AuthProvider + Routes]
└── main.tsx                  [Entry point — ReactDOM.createRoot]
```

There is no `config/`, `router/`, `services/`, or `utils/` top-level package, and no `core/`/`auth/`
subdivision under `pages/` — the structure above is the actual, current layout, verified directly
against `frontend/src/` rather than the SPA's original planning assumptions.

#### 4.2.3 Component Statistics (Verified — 2026-07-17, #458)

| Layer | Count | Verification Source |
| :--- | :--- | :--- |
| Total Java source files | **352** | `find src/main/java -name "*.java" \| wc -l` |
| Total test files | **195** | `find src/test/java -name "*.java" \| wc -l` |
| Controller classes (`@RestController`) | **38** | `find` + `grep @RestController` |
| Service classes (`@Service`) | **46** | `find` + `grep @Service` |
| Entity classes (`@Entity`) | **32** | `find` + `grep @Entity` |
| Repository interfaces | **28** | `extends JpaRepository / ElasticsearchRepository` |
| Configuration classes (`@Configuration`) | **39** | `find` + `grep @Configuration` |
| API endpoint mappings | **218** | `grep @*Mapping` across controllers |
| Classes using `@Transactional` | **24** | `grep @Transactional` |
| Classes using `@Cacheable` / `@CacheEvict` | **5** | `grep @Cacheable` |
| Classes with method-level security | **29** | `grep @PreAuthorize\|@Secured` |
| Classes using Resilience4j | **6** | `grep CircuitBreaker\|@Retry` |
| Classes using SLF4J / Logback | **102** | `grep @Slf4j\|LoggerFactory` |

---

### 4.3 Logical View

#### 4.3.1 Layered Architecture Enforcement

The system enforces a strict one-way dependency direction:

```
Controller  →  Service (interface only)  →  Repository  →  Database
                                         →  External Service
```

- Controllers never call repositories directly.
- Services depend on repository interfaces, not concrete implementations.
- Cross-service calls are mediated via Spring Application Events where loose coupling is required.

#### 4.3.2 Service Interface Pattern

All business services follow the **Interface + Implementation** pattern to maximise testability (mock by interface) and allow implementation substitution:

| Interface | Implementation | Package |
| :--- | :--- | :--- |
| `AuthService` | `AuthServiceImpl` | `service.auth` |
| `CartService` | `CartServiceImpl` | `service.cart` |
| `CheckoutService` | `CheckoutServiceImpl` | `service.checkout` |
| `OrderService` | `OrderServiceImpl` | `service.order` |
| `PaymentService` | `PaymentServiceImpl` | `service.payment` |
| `ProductService` | `ProductServiceImpl` | `service.product` |
| `CategoryService` | `CategoryServiceImpl` | `service.category` |
| `InventoryService` | `InventoryServiceImpl` | `service.inventory` |
| `ProductReviewService` | `ProductReviewServiceImpl` | `service.review` |
| `PasswordResetService` | `PasswordResetServiceImpl` | `service.password` |
| `SalesAnalyticsService` | `SalesAnalyticsServiceImpl` | `service.analytics` |
| `IAdminAnalyticsService` | `AdminAnalyticsService` | `service.admin` |
| `IAdminService` | `AdminServiceImpl` | `service.admin` |
| `IAuditLogService` | `AuditLogService` | `service.audit` |
| `IRateLimiterService` | `RateLimiterService` | `service.ratelimit` |
| `IRefreshTokenService` | `RefreshTokenService` | `service.token` |
| `INotificationService` | `NotificationService` | `service.notification` |
| `IPerformanceMonitoringService` | `PerformanceMonitoringService` | `service.monitoring` |
| `WishlistService` | `WishlistServiceImpl` | `service.wishlist` |
| `WebhookService` | `WebhookServiceImpl` | `service.webhook` |

#### 4.3.3 Entity Class Model

```
AggregateRoot (marker interface)
    └── Order (implements AggregateRoot)

JPA Entity Hierarchy:
  User ──[1:1]──► Cart ──[1:N]──► CartItem ──[N:1]──► Product
  User ──[1:N]──► Order ──[1:N]──► OrderItem ──[N:1]──► Product
  User ──[N:M]──► Role ──[N:M]──► Permission
  Order ──[1:0..1]──► Payment
  Product ──[1:1]──► Inventory
  Product ──[N:1]──► Category
  User ──[1:N]──► Address
  User ──[1:N]──► RefreshToken
  User ──[1:N]──► PasswordResetToken
  User ──[1:N]──► AuditLog
  User ──[1:N]──► ProductReview ──[N:1]──► Product
  User ──[1:N]──► Wishlist
  Inventory ──[1:N]──► InventoryThresholdBreachEvent
```

**Fetch Type Decisions**:

| Relationship | Fetch Type | Rationale |
| :--- | :--- | :--- |
| `User.roles` | `EAGER` | Required on every authenticated request for RBAC; set size bounded and small |
| `Role.permissions` | `EAGER` | Required alongside roles; bounded set |
| All `@OneToMany` | `LAZY` (explicit) | Default for collections; prevents N+1; join-fetched only where required |
| All `@ManyToOne` | `LAZY` (explicit) | Load parent only when traversed |

> **Design Note**: `Category.products` and `Order.orderItems` currently lack explicit `fetch = FetchType.LAZY` declaration (Baseline Assessment F-09). This is tracked as a pending correction under Phase 1 remediation (TIR scope).

#### 4.3.4 Exception Hierarchy

```
RuntimeException
    └── BuildNestException  (base; carries errorCode)
            ├── AuthenticationException       → HTTP 401
            ├── AuthorizationException        → HTTP 403
            ├── AccessDeniedException         → HTTP 403
            ├── ResourceNotFoundException     → HTTP 404
            ├── DuplicateResourceException    → HTTP 409
            ├── ValidationException           → HTTP 400
            ├── InventoryException            → HTTP 409
            ├── PaymentProcessingException    → HTTP 502
            └── ExternalServiceException      → HTTP 503
```

All exceptions are caught by `GlobalExceptionHandler` (`@RestControllerAdvice`) which maps them to standardised `ErrorResponse` payloads with `timestamp`, `status`, `error`, `message`, and `path` fields.

#### 4.3.5 AOP Design — `@Auditable`

```
@Auditable(action = "LOGIN", entityType = "AUTH")
public AuthResponse login(LoginRequest req) { ... }
        │
        ▼
AuditAspect (@Around advice)
        │
        ├── Extract SecurityContext → current user ID
        ├── Extract HttpServletRequest → IP address, User-Agent
        ├── Proceed with method
        ├── On success → AuditLogService.save(AuditLog)
        ├── On exception → AuditLogService.save(AuditLog with failure)
        └── Async → ElasticsearchIngestionService.ingest(auditEvent)
```

The AOP proxy is applied via `@EnableAspectJAutoProxy` (Spring Boot default). No intrusion into business method code is required.

#### 4.3.6 Frontend Component Model (Verified — 2026-07-17, #459)

| Component Category | Key Components | Responsibility |
| :--- | :--- | :--- |
| Providers | `AuthProvider` (`contexts/AuthContext.tsx`) | The single global provider — wraps the tree in `App.tsx`; there is no `CartProvider` (cart state is not Context-managed, see §4.5.5) |
| Route guard | `RequireAuth` (`components/common/RequireAuth.tsx`) | Wraps a `<Route>`'s element; redirects to `/login` if unauthenticated, renders an "Access denied" view if an optional `role` prop doesn't match the user's roles — there is no separate `ProtectedRoute`/`AdminRoute`, one component handles both cases via the optional prop |
| Pages | 13 page components in `pages/` (see §4.2.2, §4.7.4) | Top-level views mapped one-to-one to routes in `App.tsx` |
| Shared UI | `Navbar`, `ErrorBoundary` (`components/common/`) | Cross-page structural components — there is no separate Layout-wrapper tier (`MainLayout`/`AdminLayout`/`AuthLayout`); pages render directly inside `Navbar` + `ErrorBoundary` |
| Domain components | Per-page sub-components under `components/{account,admin,cart,checkout,filters,product}/` | Composites scoped to the page that owns them, not shared atomics — there is no generic `Button`/`Input`/`Modal`/`Toast` component library; styling is applied directly via Tailwind v4 utility classes |
| API modules | `api/*.ts` (one file per domain — `auth`, `admin`, `cart`, `categories`, `checkout`, `orders`, `products`, `reviews`, `user`, `wishlist`, `addresses`) plus `api/client.ts` | Plain `fetch`-based wrappers (not Axios) around each REST domain; `client.ts` centralizes the CSRF/cookie handling and a shared 401 → refresh-and-retry hook (`setUnauthorizedHandler`, consumed by `AuthContext`) |
| Hooks | `useAsync<T>` (`hooks/useAsync.ts`) — generic `data`/`loading`/`error`/`reload`/`setData` wrapper around an async fetcher, replacing ~15 hand-written fetch-in-`useEffect` call sites; `useAuth`, `useCart`, `useCategories`, `useFeaturedProducts`, `useProduct`, `useProducts`, `useReviews` — thin domain-specific wrappers built on top of `useAsync` or `AuthContext` | Encapsulate async data-fetching and Context access |

---

### 4.4 Dependency View

#### 4.4.1 Technology Stack (Verified)

| Category | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| Language | Java | 21 LTS | Core runtime |
| Framework | Spring Boot | **3.5.10** | Application framework |
| Security | Spring Security | 6.x (via Boot) | Auth, authorisation |
| Persistence | Spring Data JPA / Hibernate | 6.x | ORM, repository pattern |
| Database | MySQL | 8.2 | Primary relational store |
| Cache | Spring Data Redis (Lettuce) | (via Boot) | Distributed caching |
| Search | Spring Data Elasticsearch | (via Boot) | Full-text search, analytics |
| Auth tokens | JJWT | **0.12.3** | JWT creation and validation |
| Rate limiting | Bucket4j | **8.1.0** | Token-bucket rate limiting (Redis-backed) |
| Resilience | Resilience4j | **2.1.0** | Circuit breaker, time limiter |
| Payment | Razorpay Java SDK | 1.4.5 | Payment gateway integration |
| Metrics | Micrometer + Prometheus | (via Boot) | Metrics export |
| Logging | Logback + Logstash Encoder | 7.4 | Structured JSON logging |
| API Docs | SpringDoc OpenAPI | (via Boot) | Swagger UI / OpenAPI 3.0 |
| Code gen | Lombok | 1.18.x | Boilerplate reduction |
| Build | Apache Maven | 3.9.x | Build and dependency management |
| Frontend framework | React | **19.2.6** | SPA UI library |
| Frontend build | Vite | **8.0.12** | Build tool and dev server |
| Container | Docker | 24+ | Multi-stage containerisation |
| Orchestration | Kubernetes | 1.28+ | Pod scheduling, scaling, routing |
| IaC | Terraform | 1.x | AWS infrastructure provisioning |

#### 4.4.2 Key Internal Dependency Graph

```
CheckoutController
    └──► CheckoutServiceImpl
              ├──► CartService        ──► CartRepository  ──► MySQL
              ├──► InventoryService   ──► InventoryRepository ──► MySQL
              │         └──► DomainEventPublisher ──► LowStockWarningEvent
              ├──► OrderService       ──► OrderRepository  ──► MySQL
              │         └──► DomainEventPublisher ──► OrderPlacedEvent
              └──► PaymentService     ──► RazorpayClientAdapter ──► Razorpay API
                        └──► PaymentRepository ──► MySQL

AuthController
    └──► AuthServiceImpl
              ├──► CustomUserDetailsService ──► UserRepository ──► MySQL
              ├──► JwtTokenProvider         ──► JJWT library
              ├──► RefreshTokenService      ──► RefreshTokenRepository ──► MySQL
              └──► RateLimiterService       ──► Redis (Bucket4j)
```

---

### 4.5 Information View

#### 4.5.1 Entity-Relationship Model

```
User ──[1:1]──── Cart
 │                └──[1:N]── CartItem ──[N:1]── Product
 │                                               │
 ├──[1:N]──── Order ──[1:0..1]── Payment        ├──[1:1]── Inventory
 │              └──[1:N]── OrderItem ──[N:1]────┘           └──[1:N]── ThresholdBreachEvent
 │
 ├──[N:M]──── Role ──[N:M]── Permission
 ├──[1:N]──── Address
 ├──[1:N]──── RefreshToken
 ├──[1:N]──── PasswordResetToken
 ├──[1:N]──── AuditLog
 ├──[1:N]──── ProductReview ──[N:1]── Product ──[N:1]── Category
 └──[1:N]──── Wishlist

WebhookSubscription  (standalone — no User FK)
```

#### 4.5.2 Core Entity Details

| Entity | Table | Primary Key | Key Constraints |
| :--- | :--- | :--- | :--- |
| `User` | `users` | `id BIGINT AUTO_INCREMENT` | `email` UNIQUE, `username` UNIQUE |
| `Role` | `roles` | `id BIGINT AUTO_INCREMENT` | `name` UNIQUE (`USER`, `ADMIN`) |
| `Permission` | `permissions` | `id BIGINT AUTO_INCREMENT` | `name` UNIQUE |
| `Product` | `product` | `id BIGINT AUTO_INCREMENT` | FK → `category`, FK → `supplier_id` (users) |
| `Category` | `category` | `id BIGINT AUTO_INCREMENT` | `name` UNIQUE |
| `Cart` | `cart` | `id BIGINT AUTO_INCREMENT` | FK → `user_id` (one-to-one) |
| `CartItem` | `cart_item` | `id BIGINT AUTO_INCREMENT` | FK → `cart_id`, FK → `product_id` |
| `Order` | `orders` | `id BIGINT AUTO_INCREMENT` | `order_number` UNIQUE, FK → `user_id` |
| `OrderItem` | `order_item` | `id BIGINT AUTO_INCREMENT` | FK → `order_id`, FK → `product_id` |
| `Payment` | `payment` | `id BIGINT AUTO_INCREMENT` | FK → `order_id` |
| `Inventory` | `inventory` | `id BIGINT AUTO_INCREMENT` | FK → `product_id` (one-to-one) |
| `ProductReview` | `product_reviews` | `id BIGINT AUTO_INCREMENT` | FK → `user_id`, FK → `product_id` |
| `Wishlist` | `wishlist` | `id BIGINT AUTO_INCREMENT` | FK → `user_id` |
| `RefreshToken` | `refresh_token` | `id BIGINT AUTO_INCREMENT` | `token` UNIQUE, FK → `user_id` |
| `PasswordResetToken` | `password_reset_tokens` | `id BIGINT AUTO_INCREMENT` | `token` UNIQUE, FK → `user_id` |
| `AuditLog` | `audit_log` | `id BIGINT AUTO_INCREMENT` | FK → `user_id` (indexed) |
| `Address` | `addresses` | `id BIGINT AUTO_INCREMENT` | FK → `user_id` |
| `WebhookSubscription` | `webhook_subscription` | `id BIGINT AUTO_INCREMENT` | `event_type`, `target_url` |
| `InventoryThresholdBreachEvent` | `inventory_threshold_breach_events` | `id BIGINT AUTO_INCREMENT` | FK → `inventory_id` |

#### 4.5.3 Data Flow — Order Placement with Payment

| Step | From | To | Data Transferred |
| :--- | :--- | :--- | :--- |
| 1 | Client | `CheckoutController` | `POST /api/checkout/process-with-payment/{cartId}` + `CheckoutRequestDTO` |
| 2 | `CheckoutController` | `CheckoutServiceImpl` | `cartId`, `userId`, `CheckoutRequestDTO` |
| 3 | `CheckoutServiceImpl` | `CartService` | Cart retrieval by `userId` |
| 4 | `CheckoutServiceImpl` | `InventoryService` | Availability check per `productId` + `quantity` |
| 5 | `CheckoutServiceImpl` | Internal | `createOrderFromCart()` → `Order` + `OrderItem` entities persisted |
| 6 | `CheckoutServiceImpl` | `InventoryService` | `deductInventoryFromCart()` — stock decremented |
| 7 | `InventoryService` | `DomainEventPublisher` | `LowStockWarningEvent` (if stock ≤ threshold) |
| 8 | `CheckoutServiceImpl` | `PaymentService` | `initiatePayment(orderId, totalAmount)` |
| 9 | `PaymentService` | `RazorpayClientAdapter` | `createOrder(amount, currency)` |
| 10 | `RazorpayClientAdapter` | Razorpay API | HTTPS POST — returns `razorpayOrderId` |
| 11 | `PaymentService` | `PaymentRepository` | `Payment` entity saved (status: `PENDING`) |
| 12 | `CheckoutServiceImpl` | `CartRepository` | Cart cleared after order |
| 13 | `CheckoutServiceImpl` | `DomainEventPublisher` | `OrderPlacedEvent` published |
| 14 | `CheckoutController` | Client | `200 OK` + `OrderResponseDTO` + `PaymentDetailsDTO` |

#### 4.5.4 Cache Strategy

| Cache Region | Key Pattern | TTL | Eviction Trigger |
| :--- | :--- | :--- | :--- |
| `products` | `product:{id}` | 5 min (300,000 ms) | Admin product update/delete |
| `categories` | `category:{id}` / `'all'` | 60 min (3,600,000 ms) | Category update |
| `users` | `user:{id}` | 30 min (1,800,000 ms) | User profile update |
| `orders` | `order:{id}` | 10 min (600,000 ms) | Order status change |
| `auditLogs` | `'all-page-' + pageNum` etc. | 15 min (900,000 ms) | New audit log entry |
| `user-permissions` | `permissions:{userId}` | 60 min (3,600,000 ms) | Role assignment change |
| `inventory-items` | `inventory:{productId}` | 5 min (300,000 ms) | Stock update |
| `elasticsearchAuditLogs` | `userId + ':' + fromDate + ':' + toDate` | 15 min | New audit event |

#### 4.5.5 Client-Side State Model (Verified — 2026-07-17, #459)

| State Type | Storage | Content | Lifetime |
| :--- | :--- | :--- | :--- |
| Auth tokens | httpOnly `Secure`/`SameSite=Lax` cookies (backend-set, unreadable from JS) | JWT access token, JWT refresh token | Access: matches `jwt.expiration`; Refresh: matches `jwt.refresh-expiration` (SEC-15) |
| CSRF token | Non-httpOnly `XSRF-TOKEN` cookie (double-submit) | Opaque CSRF token, echoed via `X-XSRF-TOKEN` header | Session-scoped; reissued by `NonClearingCsrfTokenRepository` |
| User session | React Context — `AuthContext` (the only Context in the app, memory-only) | User profile (`id`, `username`, normalized `roles`), `loading` flag | Cleared on page reload; rehydrated on mount via `apiFetchCsrf()` + `fetchProfile()` against the auth cookie (`AuthContext.tsx`'s `restoreSession`) |
| Cart state | Local component state via the `useCart(userId)` hook — no Context, no external store | Cart items, loading/error flags | Re-fetched on hook mount and after every mutation (`addItem`/`removeItem` call `load()` internally) |
| UI state | Local component state (`useState`) | Form inputs, modal visibility | Component lifecycle |
| Server data (products, categories, reviews, etc.) | Local component state via the generic `useAsync<T>` hook (`hooks/useAsync.ts`) | Fetched entity data, loading/error flags | Re-fetched when the hook's dependency array changes, or on demand via its `reload()` — no React Query/SWR or other caching library is in use; each call is a fresh fetch |

There is no Redux, React Query, or SWR in the dependency tree — all client state is either the one
`AuthContext` Provider or hook-local component state, per the project's own `react/coding-style.md`
guidance ("Context for cross-cutting state... not for high-frequency updates").

---

### 4.6 Patterns Use View

#### 4.6.1 Design Patterns Applied

| Pattern | Category | Evidence Location | Description |
| :--- | :--- | :--- | :--- |
| **Repository** | Data Access | `repository/` (19 interfaces) | All data access isolated in Spring Data JPA repository interfaces |
| **Service Layer** | Architectural | `service/` (36 classes) | Business logic encapsulated; no business rules in controllers or repositories |
| **Interface / Strategy** | Behavioural | All service packages | Interface + Implementation pair enables mock-based testing and swappable implementations |
| **DTO / Payload Separation** | Structural | `model/dto/`, `model/payload/` | JPA entities never exposed directly in API responses; separate DTO shape per use case |
| **Observer / Domain Events** | Behavioural | `event/` | Spring `ApplicationEvent` + `@EventListener` decouples event producers from consumers |
| **Adapter** | Structural | `RazorpayClientAdapter` | Wraps Razorpay Java SDK with a domain-specific interface; isolates external SDK changes |
| **AOP / Decorator** | Structural | `aspect/AuditAspect.java` | `@Auditable` + `@Around` advice adds audit logging without modifying business methods |
| **Chain of Responsibility** | Behavioural | `security/` | Spring Security filter chain: HTTPS → CORS → JWT → RateLimit → Authorisation |
| **Circuit Breaker** | Resilience | `config/ResilienceConfig.java` | Resilience4j circuit breakers on Redis (70% threshold) and database (50% threshold) |
| **Cache-Aside** | Performance | `@Cacheable` / `@CacheEvict` | Application manages cache population and eviction explicitly via annotations |
| **Builder** | Creational | `model/dto/`, `model/payload/` | Lombok `@Builder` on all DTO and payload classes for immutable construction |
| **Singleton** | Creational | All `@Configuration` classes | Spring default bean scope; one instance per application context |
| **Proxy** | Structural | Spring AOP | Applied transparently by Spring for `@Cacheable`, `@Transactional`, `@Auditable` |
| **Interceptor** | Behavioural | `interceptor/` (3 classes) | `RateLimitHeaderInterceptor`, `PerformanceMonitoringInterceptor`, `ApiSunsetInterceptor` |
| **Template Method** | Behavioural | `@Transactional` | Spring's transaction proxy wraps service methods in a transaction lifecycle template |
| **Aggregate Root** | DDD | `model/entity/AggregateRoot.java` | Marker interface for DDD aggregate boundaries; implemented by `Order` |
| **Fail-Fast** | Operational | `SecurityConfig.@PostConstruct` | Validates SSL and JWT secret on startup; application refuses to start if misconfigured |

#### 4.6.2 Domain Events

| Event | Publisher | Trigger | Consumers |
| :--- | :--- | :--- | :--- |
| `UserRegisteredEvent` | `AuthServiceImpl` | Successful user registration | `NotificationService`, `AuditLogService` |
| `OrderPlacedEvent` | `CheckoutServiceImpl` | Order persisted after checkout | `InventoryService`, `NotificationService`, `WebhookService` |
| `OrderStatusChangedEvent` | `OrderServiceImpl` | Admin updates order status | `NotificationService`, `AuditLogService` |
| `PaymentSuccessfulEvent` | `PaymentServiceImpl` | Razorpay signature validated | `OrderService`, `NotificationService` |
| `PaymentFailedEvent` | `PaymentServiceImpl` | Signature invalid or timeout | `OrderService`, `NotificationService` |
| `LowStockWarningEvent` | `InventoryServiceImpl` | Stock falls below threshold | `NotificationService`, `WebhookService` |
| `InventoryThresholdBreachEvent` | `InventoryServiceImpl` | Threshold breach recorded | `AuditLogService`, `ElasticsearchIngestionService` |

All events flow through `DomainEventPublisher` → `DomainEventListener` via Spring's `ApplicationEventPublisher`. Events are synchronous within the same transaction by default; async processing requires `@Async` + `@EventListener`.

---

### 4.7 Interface View

#### 4.7.1 Standard API Response Format

**Success Response**:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { }
}
```

**Error Response**:
```json
{
  "timestamp": "2026-06-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: email must not be blank",
  "path": "/api/auth/register"
}
```

**Pagination Response** (where applicable):
```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false
}
```

#### 4.7.2 API Version Strategy

| Version | Status | Endpoint Prefix | Header Added |
| :--- | :--- | :--- | :--- |
| V1 | **Deprecated** | `/api/public/products` | `Deprecation: true`, `Sunset: <date>` |
| V2 | **Current** | `/api/v2/public/products` | None |

`ApiSunsetInterceptor` adds `Deprecation` and `Sunset` headers to all V1 responses. V1 controllers remain active for backward compatibility; no removal date is set at this time.

#### 4.7.3 API Endpoint Catalogue (Re-derived — 2026-07-17, #471)

**Re-derived directly from every controller's `@RequestMapping`/`@GetMapping`/etc. source**, mirroring
SRS Appendix A's own #456 re-derivation (not a copy of it — this table's shape needs a Rate Limit
column and per-row SRS Req citation that Appendix A doesn't carry). The prior version had wrong path
prefixes throughout (`/api/auth/refresh-token` instead of the real `/api/auth/refresh`,
`/api/auth/forgot-password`/`reset-password` instead of the real `PasswordResetController`'s
`/api/password/forgot`/`/api/password/reset`) and omitted entire endpoint groups (categories, tags,
coupons, shipping-methods, search reindex, inventory-threshold/analytics/reports, the public webhook
receiver, product reviews, notifications, the current multi-step checkout flow). Controller class
name is cited per group so a future drift check can `grep` the exact source file.

**Rate Limit column — verified against `rate-limiting.md` and the actual enforcement code, not
assumed uniform per path prefix.** Two independent, differently-scoped mechanisms are in play:

- `RateLimitHeaderInterceptor` (all paths) adds response headers using a **hardcoded, literal
  path-prefix check**: `/api/auth/**` → 5/min, `/api/admin/**` → 30/min, `/api/public/**` → 50/min,
  everything else → 100/min. Critically, `/api/v1/admin/**` (the base path for most admin resource
  controllers — products, categories, tags, coupons, shipping-methods, search, orders, inventory,
  sales analytics) does **not** match the `/api/admin/` prefix check, so those endpoints get only
  the 100/min default header, not the 30/min admin one, despite requiring `ROLE_ADMIN`.
- `AdminRateLimitFilter` (blocking, not just headers) is scoped the same way — `shouldNotFilter`
  only applies to literal `/api/admin/**` paths — and enforces `RateLimitUtil`'s `"admin"` key
  (50 requests / 60 s, from `rate.limit.admin.requests`/`.duration`). `/api/v1/admin/**` endpoints
  receive **no dedicated blocking rate limit at all** from either layer — only the 100/min
  informational header above.
- `/api/auth/login`, `/api/auth/refresh`, and `/api/password/forgot`/`reset`/`change` each call
  `RateLimitUtil.isAllowed()` explicitly with their own named key, which **overrides** the
  interceptor's path-based header for that specific endpoint: login 3/300 s, refresh 10/min,
  password forgot/reset/change 3/3600 s each (verified via `rate.limit.*` properties in
  `application.properties`, not the differing example defaults documented in `rate-limiting.md`
  itself, which describes hypothetical defaults, not this app's configured values).

Per-group Rate Limit cells below cite the actual enforcing mechanism, not just the header value.

##### Authentication (`AuthController`, base `/api/auth`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/auth/login` | Public | 3 / 5 min (explicit `"login"` key, overrides header) | FR-AUTH-02 |
| POST | `/api/auth/register` | Public | 5 / min (interceptor header only) | FR-AUTH-01 |
| POST | `/api/auth/refresh` | Public | 10 / min (explicit `"refresh"` key, overrides header) | FR-AUTH-06 |
| POST | `/api/auth/validate-token` | Public | 5 / min (interceptor header only) | FR-AUTH-02 |
| POST | `/api/auth/logout` | Authenticated | 5 / min (interceptor header only) | FR-AUTH-07 |
| GET | `/api/auth/csrf` | Public | 5 / min (interceptor header only) | — |

##### Password Reset (`PasswordResetController`, base `/api/password`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/password/forgot` | Public | 3 / hr (explicit `"password-forgot"` key) | FR-AUTH-08 |
| POST | `/api/password/reset` | Public | 3 / hr (explicit `"password-reset"` key) | FR-AUTH-08 |
| POST | `/api/password/change` | Authenticated (USER or ADMIN) | 3 / hr (explicit `"password-change"` key, user-scoped) | FR-AUTH-08 |

##### Product — Legacy Public (`HomeController`, base `/api/public`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/public` | Public | 50 / min | — |
| GET | `/api/public/health` | Public | 50 / min | — |
| GET | `/api/public/products` | Public | 50 / min | FR-PROD-01 |
| GET | `/api/public/products/{id}` | Public | 50 / min | FR-PROD-02 |
| GET | `/api/public/products/search` | Public | 50 / min | FR-PROD-01 |
| GET | `/api/public/products/featured` | Public | 50 / min | FR-PROD-01 |
| GET | `/api/public/categories` | Public | 50 / min | FR-PROD-03 |

##### Product — V1 Deprecated (`ProductControllerV1`, base `/api/v1/products`)

`@Deprecated(since = "2.0", forRemoval = true)`, sunset 2026-12-31, `X-API-Deprecated` header on
every response (`ApiSunsetInterceptor`).

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v1/products` | Public | 100 / min | FR-PROD-01, FR-PROD-05 |
| GET | `/api/v1/products/{id}` | Public | 100 / min | FR-PROD-02, FR-PROD-05 |

##### Product — V2 Current (`ProductControllerV2`, base `/api/v2/products`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v2/products` | Public | 100 / min | FR-PROD-01, FR-PROD-05 |
| GET | `/api/v2/products/{id}` | Public | 100 / min | FR-PROD-02, FR-PROD-05 |
| GET | `/api/v2/products/search` | Public | 100 / min | FR-PROD-01 |
| GET | `/api/v2/products/category/{categoryId}` | Public | 100 / min | FR-PROD-03 |
| GET | `/api/v2/products/{id}/related` | Public | 100 / min | FR-PROD-02 |

##### User Profile (`UserController`, base `/api/user`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/user/profile` | USER | 100 / min | FR-AUTH-09 |
| PUT | `/api/user/profile` | USER | 100 / min | FR-AUTH-09 |

##### Address (`AddressController`, base `/api/user/addresses`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/user/addresses` | USER | 100 / min | — |
| POST | `/api/user/addresses` | USER | 100 / min | — |
| PUT | `/api/user/addresses/{id}` | USER | 100 / min | — |
| DELETE | `/api/user/addresses/{id}` | USER | 100 / min | — |
| PUT | `/api/user/addresses/{id}/default` | USER | 100 / min | — |

##### Cart (`CartController`, base `/api/user/cart`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/user/cart/add` | USER (ownership-checked) | 100 / min | FR-CART-01 |
| GET | `/api/user/cart/{userId}` | USER (ownership-checked) | 100 / min | FR-CART-02 |
| DELETE | `/api/user/cart/item/{cartItemId}` | USER | 100 / min | FR-CART-03 |
| DELETE | `/api/user/cart/clear/{userId}` | USER (ownership-checked) | 100 / min | FR-CART-04 |
| GET | `/api/user/cart/total/{userId}` | USER (ownership-checked) | 100 / min | FR-CART-05 |

##### Checkout — Legacy Single-Step (`CheckoutController`, base `/api/checkout`)

Not `@Deprecated`-annotated in code, but superseded by the multi-step flow below (#76/CHK-01); no
frontend caller references `/api/checkout` (`frontend/src/api/checkout.ts` targets `/api/v1/checkout`
exclusively) — kept for direct payment-linked checkout, not removed.

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/checkout/validate/{cartId}` | USER | 100 / min | FR-CHK-01 |
| GET | `/api/checkout/calculate-total/{cartId}` | USER | 100 / min | FR-CHK-02 |
| POST | `/api/checkout/process/{cartId}` | USER | 100 / min | FR-CHK-03 |
| POST | `/api/checkout/process-with-payment/{cartId}` | USER | 100 / min | FR-CHK-04 |

##### Checkout — Multi-Step (Current) (`MultiStepCheckoutController`, base `/api/v1/checkout`)

Address → shipping → coupon → payment → confirm; session stored in Redis, 30-minute TTL. This is
the flow the frontend's `CheckoutPage` actually consumes.

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v1/checkout/shipping-options` | USER | 100 / min | FR-CHK-01 |
| POST | `/api/v1/checkout/address` | USER | 100 / min | FR-CHK-01 |
| POST | `/api/v1/checkout/coupon` | USER | 100 / min | FR-CHK-09 |
| POST | `/api/v1/checkout/shipping` | USER | 100 / min | FR-CHK-02 |
| POST | `/api/v1/checkout/payment` | USER | 100 / min | FR-CHK-04 |
| POST | `/api/v1/checkout/confirm` | USER | 100 / min | FR-CHK-05, FR-CHK-06 |

##### Dead-Code Audit Decision Record (#448, 2026-07-21)

Four endpoint groups with zero frontend callers were audited for disposition (candidate for
removal vs. serving a not-yet-built external/mobile consumer). Verified directly against
`frontend/src/api/{products,checkout}.ts`, `ApiSunsetInterceptor`, and the endpoint tables above:

| Endpoint group | Frontend caller? | Disposition | Rationale |
| :--- | :--- | :--- | :--- |
| **Product V1 Deprecated** (`ProductControllerV1`, `/api/v1/products`) | None | **Keep until sunset** | Already a deliberate, documented deprecation-lifecycle demonstration (`@Deprecated(since="2.0", forRemoval=true)`, sunset 2026-12-31, `ApiSunsetInterceptor` headers, §6.1) — not accidental dead code. Remove only once the sunset date passes |
| **Product V2 Current** (`ProductControllerV2`, `/api/v2/products`) | None | **Keep, but see labeling note below** | Zero real traffic despite being labeled "Current" — real traffic goes through `HomeController` (`/api/public`), labeled "Legacy" in this same document. No proof an external/mobile consumer targets `/api/v2/products` specifically, so not removed on this pass, but the "Current"/"Legacy" labels contradict actual usage and are misleading (see follow-up) |
| **Checkout — Legacy Single-Step** (`CheckoutController`, `/api/checkout`) | None (frontend uses `/api/v1/checkout` exclusively, confirmed via `checkout.ts`) | **Keep** | Functionally superseded by `MultiStepCheckoutController` (same FR-CHK-01–04 coverage), but no proof no external/mobile consumer targets a direct single-step payment flow. Removing a payment-adjacent endpoint on absence-of-evidence alone is a product-scope call, not a code-cleanup call — left to a future issue if/when that's confirmed |
| **Auth token validation** (`AuthController.validateToken`, `/api/auth/validate-token`) | None found (frontend, tests, docs) | **Keep** | Standard, stateless, public token-validation surface (FR-AUTH-02) — plausible for an external client (mobile app, third-party integration) to call independently of the SPA's own session handling. Low cost to keep, high cost (breaking, hard to undo) to remove without evidence either way |

**No code removed on this pass** — this issue's scope is the audit and decision record, not code
changes. See #535 (labeling fix) and #536 (V1 sunset-removal reminder) for the two follow-ups this
audit surfaced.

##### Order (`UserOrderController`, base `/api/user/orders`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/user/orders` | USER | 100 / min | FR-CHK-07 |
| GET | `/api/user/orders/{id}` | USER | 100 / min | FR-CHK-07 |

##### Wishlist (`WishlistController`, base `/api/user/wishlist`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/user/wishlist/items/{productId}` | USER | 100 / min | FR-WISH-01 |
| DELETE | `/api/user/wishlist/items/{productId}` | USER | 100 / min | FR-WISH-02 |
| GET | `/api/user/wishlist` | USER | 100 / min | FR-WISH-02 |
| GET | `/api/user/wishlist/contains/{productId}` | USER | 100 / min | FR-WISH-02 |
| DELETE | `/api/user/wishlist` | USER | 100 / min | FR-WISH-02 |
| GET | `/api/user/wishlist/count` | USER | 100 / min | FR-WISH-02 |

##### Product Review (`ProductReviewController`, base `/api/products/{productId}/reviews`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/products/{productId}/reviews` | USER | 100 / min | FR-REV-01 |
| GET | `/api/products/{productId}/reviews` | Public | 100 / min | FR-REV-02 |
| GET | `/api/products/{productId}/reviews/summary` | Public | 100 / min | FR-REV-02 |
| GET | `/api/products/{productId}/reviews/top-helpful` | Public | 100 / min | FR-REV-02 |
| POST | `/api/products/{productId}/reviews/{reviewId}/helpful` | Authenticated | 100 / min | FR-REV-02 |
| PUT | `/api/products/{productId}/reviews/{reviewId}` | USER | 100 / min | FR-REV-03 |
| DELETE | `/api/products/{productId}/reviews/{reviewId}` | USER | 100 / min | FR-REV-03 |

##### Notification (`NotificationController`, base `/api/user/notifications`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/user/notifications/stream` | USER | 100 / min | — |

##### Public Inventory Status (`InventoryStatusController`, base `/api/inventory`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/inventory/{productId}/status` | Public | 100 / min | FR-INV-01 |
| GET | `/api/inventory/{productId}/details` | Public | 100 / min | FR-INV-01 |
| GET | `/api/inventory/{productId}/available` | Public | 100 / min | FR-INV-02 |

##### Public Webhook Receiver (`PaymentWebhookController`, base `/api/v1/webhooks`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/v1/webhooks/payment` | Public (signature-verified) | 100 / min | FR-PAY-04 |

##### Admin Product Management (`AdminProductController`, base `/api/v1/admin/products`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/products` | ADMIN | 100 / min (no dedicated admin limit — see note above) | FR-PROD-04, FR-ADM-08 |
| GET | `/api/v1/admin/products/{id}` | ADMIN | 100 / min | FR-PROD-04, FR-ADM-08 |
| POST | `/api/v1/admin/products` | ADMIN | 100 / min | FR-PROD-04, FR-ADM-08 |
| PUT | `/api/v1/admin/products/{id}` | ADMIN | 100 / min | FR-PROD-04, FR-ADM-08 |
| DELETE | `/api/v1/admin/products/{id}` | ADMIN | 100 / min | FR-PROD-04, FR-ADM-08 |
| GET / POST | `/api/v1/admin/products/{id}/images` | ADMIN | 100 / min | FR-PROD-09, FR-ADM-08 |
| PATCH | `/api/v1/admin/products/{id}/images/reorder` | ADMIN | 100 / min | FR-PROD-09, FR-ADM-08 |
| DELETE | `/api/v1/admin/products/{id}/images/{imageId}` | ADMIN | 100 / min | FR-PROD-09, FR-ADM-08 |
| GET / POST | `/api/v1/admin/products/{productId}/variants` | ADMIN | 100 / min | FR-PROD-08, FR-ADM-08 |
| PUT / DELETE | `/api/v1/admin/products/{productId}/variants/{variantId}` | ADMIN | 100 / min | FR-PROD-08, FR-ADM-08 |

##### Admin Category Management (`AdminCategoryController`, base `/api/v1/admin/categories`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/categories` | ADMIN | 100 / min (no dedicated admin limit) | FR-ADM-09 |
| GET | `/api/v1/admin/categories/{id}` | ADMIN | 100 / min | FR-ADM-09 |
| POST | `/api/v1/admin/categories` | ADMIN | 100 / min | FR-ADM-09 |
| PUT | `/api/v1/admin/categories/{id}` | ADMIN | 100 / min | FR-ADM-09 |
| DELETE | `/api/v1/admin/categories/{id}` | ADMIN | 100 / min | FR-ADM-09 |

##### Admin Product Tagging (`AdminProductTagController`, base `/api/v1/admin/tags`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET / POST / PUT / DELETE | `/api/v1/admin/tags/**` | ADMIN | 100 / min (no dedicated admin limit) | FR-ADM-08 |

##### Admin Coupon Management (`AdminCouponController`, base `/api/v1/admin/coupons`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/v1/admin/coupons` | ADMIN | 100 / min (no dedicated admin limit) | FR-ADM-08 |
| DELETE | `/api/v1/admin/coupons/{id}` | ADMIN | 100 / min | FR-ADM-08 |

##### Admin Shipping Method Management (`AdminShippingController`, base `/api/v1/admin/shipping-methods`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET / POST / PUT / DELETE | `/api/v1/admin/shipping-methods/**` | ADMIN | 100 / min (no dedicated admin limit) | FR-ADM-08 |

##### Admin Search Management (`AdminSearchController`, base `/api/v1/admin/search`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/v1/admin/search/reindex` | ADMIN | 100 / min (no dedicated admin limit) | FR-ADM-08 |

##### Admin Order Management (`AdminOrderController`, base `/api/v1/admin/orders`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/orders` | ADMIN | 100 / min (no dedicated admin limit) | FR-CHK-08, FR-ADM-08 |
| GET | `/api/v1/admin/orders/{id}` | ADMIN | 100 / min | FR-CHK-08, FR-ADM-08 |
| PATCH | `/api/v1/admin/orders/{id}/status` | ADMIN | 100 / min | FR-CHK-08, FR-ADM-08 |
| POST | `/api/v1/admin/orders/{id}/refund` | ADMIN | 100 / min | FR-CHK-08, FR-ADM-08 |

##### Admin User Management (`AdminUserController`, base `/api/admin/users`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/users` | ADMIN | 30 / min header + 50 / min block | FR-ADM-03, FR-ADM-08 |
| GET | `/api/admin/users/{id}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-03, FR-ADM-08 |
| PUT | `/api/admin/users/{id}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-03, FR-ADM-08 |
| DELETE | `/api/admin/users/{id}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-03, FR-ADM-08 |

##### Admin Inventory Management (`AdminInventoryController`, base `/api/v1/admin/inventory`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/inventory` | ADMIN | 100 / min (no dedicated admin limit) | FR-INV-03, FR-ADM-08 |
| PATCH | `/api/v1/admin/inventory/{productId}` | ADMIN | 100 / min | FR-INV-04, FR-ADM-08 |
| GET | `/api/v1/admin/inventory/product/{productId}` | ADMIN | 100 / min | FR-INV-01, FR-ADM-08 |
| POST | `/api/v1/admin/inventory/add-stock/{productId}` | ADMIN | 100 / min | FR-INV-03, FR-ADM-08 |
| POST | `/api/v1/admin/inventory/update-stock/{productId}` | ADMIN | 100 / min | FR-INV-04, FR-ADM-08 |
| GET | `/api/v1/admin/inventory/check-availability/{productId}` | ADMIN | 100 / min | FR-INV-02, FR-ADM-08 |

##### Admin Inventory Threshold Management (`AdminInventoryThresholdController`, base `/api/admin/inventory-threshold`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST / GET | `/api/admin/inventory-threshold/product/{productId}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06, FR-ADM-08 |
| POST / GET | `/api/admin/inventory-threshold/category/{categoryId}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06, FR-ADM-08 |
| GET | `/api/admin/inventory-threshold/product/{productId}/effective` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06, FR-ADM-08 |
| PUT | `/api/admin/inventory-threshold/product/{productId}/use-category` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06, FR-ADM-08 |

##### Admin Inventory Analytics (`AdminInventoryAnalyticsController`, base `/api/admin/inventory-analytics`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/inventory-analytics/high-demand-low-inventory` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |
| GET | `/api/admin/inventory-analytics/seasonal-patterns` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |
| GET | `/api/admin/inventory-analytics/stock-turnover` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |
| GET | `/api/admin/inventory-analytics/restocking-plan` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |

##### Admin Inventory Reports (`AdminInventoryReportController`, base `/api/admin/inventory-reports`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/inventory-reports/below-threshold` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |
| GET | `/api/admin/inventory-reports/breaches` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |
| GET | `/api/admin/inventory-reports/frequent-problems` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |
| GET | `/api/admin/inventory-reports/product/{productId}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |
| GET | `/api/admin/inventory-reports/summary` | ADMIN | 30 / min header + 50 / min block | FR-ADM-02, FR-INV-07 |

##### Admin Reports (`AdminReportController`, base `/api/admin/reports`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/reports/dashboard` | ADMIN | 30 / min header + 50 / min block | FR-ADM-05 |
| GET | `/api/admin/reports/users/count` | ADMIN | 30 / min header + 50 / min block | FR-ADM-05 |
| GET | `/api/admin/reports/products/count` | ADMIN | 30 / min header + 50 / min block | FR-ADM-05 |
| GET | `/api/admin/reports/orders/count` | ADMIN | 30 / min header + 50 / min block | FR-ADM-05 |
| GET | `/api/admin/reports/revenue` | ADMIN | 30 / min header + 50 / min block | FR-ADM-05 |

##### Admin Sales Analytics (`SalesAnalyticsController`, base `/api/v1/admin/analytics/sales`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/analytics/sales/dashboard` | ADMIN | 100 / min (no dedicated admin limit) | FR-ADM-01 |
| GET | `/api/v1/admin/analytics/sales/revenue/daily` | ADMIN | 100 / min | FR-ADM-01 |
| GET | `/api/v1/admin/analytics/sales/conversion-rate` | ADMIN | 100 / min | FR-ADM-01 |
| GET | `/api/v1/admin/analytics/sales/cart-abandonment-rate` | ADMIN | 100 / min | FR-ADM-01 |
| GET | `/api/v1/admin/analytics/sales/average-order-value` | ADMIN | 100 / min | FR-ADM-01 |
| GET | `/api/v1/admin/analytics/sales/customer-lifetime-value/{userId}` | ADMIN | 100 / min | FR-ADM-01 |

##### Admin Analytics — Audit/Metrics (`AdminAnalyticsController`, base `/api/admin/analytics`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/analytics/audit-logs/user/{userId}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-04 |
| GET | `/api/admin/analytics/audit-logs/action/{action}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-04 |
| GET | `/api/admin/analytics/audit-logs/range` | ADMIN | 30 / min header + 50 / min block | FR-ADM-04 |
| GET | `/api/admin/analytics/metrics/range` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/analytics/metrics/recent` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/analytics/alerts/summary` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/analytics/dashboard` | ADMIN | 30 / min header + 50 / min block | FR-ADM-01, FR-ADM-02 |
| GET | `/api/admin/analytics/api-errors/by-status` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/analytics/api-errors/by-endpoint` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/analytics/behaviour` | ADMIN | 30 / min header + 50 / min block | FR-ADM-01 |

##### Admin Audit Log (`AuditLogController`, base `/api/admin/audit`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/audit` | ADMIN | 30 / min header + 50 / min block | FR-ADM-04 |

##### Admin Webhook Management (`WebhookAdminController`, base `/api/admin/webhooks`)

Distinct from the Public Webhook Receiver group above — this is subscription management.

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| POST | `/api/admin/webhooks` | ADMIN | 30 / min header + 50 / min block | FR-ADM-07 |
| GET | `/api/admin/webhooks` | ADMIN | 30 / min header + 50 / min block | FR-ADM-07 |
| PUT | `/api/admin/webhooks/{id}/deactivate` | ADMIN | 30 / min header + 50 / min block | FR-ADM-07 |
| DELETE | `/api/admin/webhooks/{id}` | ADMIN | 30 / min header + 50 / min block | FR-ADM-07 |

##### Admin Monitoring (`MonitoringController`, base `/api/admin/monitoring`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/monitoring/performance` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/monitoring/performance/sla-status` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| POST | `/api/admin/monitoring/performance/reset` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/monitoring/uptime` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/monitoring/uptime/formatted` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| POST | `/api/admin/monitoring/uptime/reset` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/monitoring/health-status` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |
| GET | `/api/admin/monitoring/sla-status` | ADMIN | 30 / min header + 50 / min block | FR-MON-05 |

##### Admin Adaptive Thresholds (`AdminThresholdController`, base `/api/admin/thresholds`)

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/api/admin/thresholds` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| GET / PUT | `/api/admin/thresholds/cpu` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| GET / PUT | `/api/admin/thresholds/memory` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| GET / PUT | `/api/admin/thresholds/error-rate` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| GET / PUT | `/api/admin/thresholds/response-time` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| GET / PUT | `/api/admin/thresholds/failed-logins` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| GET / PUT | `/api/admin/thresholds/jwt-refresh` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| GET / PUT | `/api/admin/thresholds/admin-operations` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |
| POST | `/api/admin/thresholds/reset` | ADMIN | 30 / min header + 50 / min block | FR-ADM-06 |

##### Actuator and Monitoring Endpoints

`/actuator/prometheus` uses a dedicated Basic Auth credential, isolated from the app's real user
accounts (`actuatorMonitoringSecurityFilterChain`, `@Order(0)` — see `spring-security.md`); every
other `/actuator/**` path requires `ROLE_ADMIN` via the main filter chain.

| Method | Path | Auth | Rate Limit | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| GET | `/actuator/health` | Public | 100 / min | FR-MON-01 |
| GET | `/actuator/prometheus` | Basic Auth (dedicated monitoring credential) | 100 / min | FR-MON-05 |
| GET | `/actuator/info` | Public | 100 / min | FR-MON-01 |
| GET | `/actuator/metrics` | ADMIN | 100 / min | FR-MON-05 |

#### 4.7.4 Frontend Route Design (Verified — 2026-07-17, #459)

All routes are declared inline as `<Route>` elements inside `App.tsx`'s single `<Routes>` block —
there is no separate `router/` package or `AppRouter` file. Route guarding is a single
`RequireAuth` component (`components/common/RequireAuth.tsx`), used with an optional `role` prop
rather than two separate `ProtectedRoute`/`AdminRoute` components.

| Route | Component | Auth Required | RTM Req |
| :--- | :--- | :--- | :--- |
| `/` | `HomePage` | No | FR-FE-11 |
| `/products` | `ProductListingPage` (also serves search via `?search=`) | No | FR-FE-12, FR-FE-21 |
| `/products/:id` | `ProductDetailPage` | No | FR-FE-13 |
| `/cart` | `CartPage` | No (route itself unguarded; cart is per-session) | FR-FE-14 |
| `/checkout` | `CheckoutPage` | No (route itself unguarded) | FR-FE-15 |
| `/orders/:id` | `OrderConfirmationPage` | No | — |
| `/login` | `LoginPage` | No | FR-FE-16 |
| `/register` | `RegisterPage` | No | FR-FE-17 |
| `/forgot-password` | `ForgotPasswordPage` | No | — |
| `/reset-password` | `ResetPasswordPage` | No | — |
| `/account` | `AccountPage` (tabs: profile, orders, wishlist — see FR-FE-18/19/20) | Yes — `<RequireAuth>` | FR-FE-18, FR-FE-19, FR-FE-20 |
| `/admin` | `AdminDashboardPage` (tabs: categories, inventory, orders — see FR-FE-24/25/31) | Yes — `<RequireAuth role="ADMIN">` | FR-FE-22, FR-FE-24, FR-FE-25, FR-FE-31 |
| `*` | `NotFoundPage` | No | — |

There are no standalone `/profile`, `/orders`, `/wishlist`, `/admin/products`,
`/admin/inventory`, or `/admin/orders` routes — those features exist as tabs inside `AccountPage`
and `AdminDashboardPage` respectively (per RTM §6.10's per-row verification, #453). Admin product
management (`FR-FE-23`) has no route or component at all yet — tracked as open in #425.

---

### 4.8 Interaction View

#### 4.8.1 User Authentication Sequence

```
Client          AuthController      AuthServiceImpl     UserDetailsService  JwtTokenProvider    RefreshTokenService
  │                   │                   │                    │                   │                    │
  │─POST /login──────►│                   │                    │                   │                    │
  │                   │─authenticate()───►│                    │                   │                    │
  │                   │                   │─loadUserByUsername►│                   │                    │
  │                   │                   │                    │─findByUsername()──►(UserRepository)     │
  │                   │                   │                    │◄──User entity──────│                   │
  │                   │                   │◄──CustomUserDetails─│                   │                    │
  │                   │                   │─BCrypt.matches()   │                   │                    │
  │                   │                   │  [password valid]  │                   │                    │
  │                   │                   │─generateToken()───────────────────────►│                    │
  │                   │                   │◄──JWT access token─────────────────────│                    │
  │                   │                   │─createRefreshToken()──────────────────────────────────────►│
  │                   │                   │◄──RefreshToken entity──────────────────────────────────────│
  │                   │◄─AuthResponse─────│                    │                   │                    │
  │◄──200 OK + tokens─│                   │                    │                   │                    │
```

#### 4.8.2 Checkout with Payment Sequence

```
Client      CheckoutController   CheckoutServiceImpl  CartService  InventoryService  PaymentService  RazorpayAdapter  EventPublisher
  │               │                     │                 │               │                │               │               │
  │─POST ────────►│                     │                 │               │                │               │               │
  │  /checkout/   │─checkoutWithPayment►│                 │               │                │               │               │
  │  {cartId}     │                     │─getCart()──────►│               │                │               │               │
  │               │                     │◄─Cart + Items───│               │                │               │               │
  │               │                     │─checkAvail()──────────────────►│                │               │               │
  │               │                     │◄─Available ✓───────────────────│                │               │               │
  │               │                     │─createOrderFromCart()           │                │               │               │
  │               │                     │  [Order + OrderItems saved]     │                │               │               │
  │               │                     │─deductInventory()─────────────►│                │               │               │
  │               │                     │                 │  [stock < threshold?]          │               │               │
  │               │                     │                 │──────────────►─publish(LowStockWarningEvent)──►│               │
  │               │                     │─initiatePayment()───────────────────────────────►│               │               │
  │               │                     │                 │               │                │─createOrder()─►│               │
  │               │                     │                 │               │                │◄─razorpayId────│               │
  │               │                     │                 │               │                │ save Payment   │               │
  │               │                     │◄─Payment entity─────────────────────────────────│               │               │
  │               │                     │─clearCart()────►│               │                │               │               │
  │               │                     │─publish(OrderPlacedEvent)──────────────────────────────────────────────────────►│
  │               │◄─OrderResponse──────│                 │               │                │               │               │
  │◄──200 OK──────│                     │                 │               │                │               │               │
```

#### 4.8.3 JWT Token Refresh Sequence

```
Client          AuthController      RefreshTokenService     JwtTokenProvider    UserRepository
  │                   │                    │                      │                   │
  │─POST /refresh────►│                    │                      │                   │
  │  {refreshToken}   │─validateRefresh()─►│                      │                   │
  │                   │                    │─findByToken()────────────────────────────►│
  │                   │                    │◄─RefreshToken entity──────────────────────│
  │                   │                    │  [isExpired? → 401]   │                   │
  │                   │                    │─deleteToken() [rotation]                  │
  │                   │                    │─createNewToken()      │                   │
  │                   │                    │─generateToken()──────►│                   │
  │                   │                    │◄─new JWT──────────────│                   │
  │                   │◄─new tokens────────│                      │                   │
  │◄──200 OK──────────│                    │                      │                   │
```

---

### 4.9 State Dynamics View

#### 4.9.1 Order State Machine

```
                    ┌─────────┐
           ┌───────►│ PENDING │◄──── Order created
           │        └────┬────┘
           │             │ Payment verified
           │             ▼
           │        ┌───────────┐
           │        │ CONFIRMED │
           │        └─────┬─────┘
           │              │ Admin ships order
           │              ▼
           │        ┌─────────┐
           │        │ SHIPPED │
           │        └────┬────┘
           │             │ Delivery confirmed
           │             ▼
           │        ┌───────────┐
           │        │ DELIVERED │ (terminal)
           │        └───────────┘
           │
     User cancels / payment fails
     Admin cancels
           ▼
     ┌───────────┐
     │ CANCELLED │ (terminal)
     └───────────┘
```

| State | Entry Condition | Exit Conditions |
| :--- | :--- | :--- |
| `PENDING` | Order created at checkout | `CONFIRMED` on payment success; `CANCELLED` on timeout or user cancel |
| `CONFIRMED` | Payment signature validated | `SHIPPED` on admin dispatch; `CANCELLED` on admin override |
| `SHIPPED` | Admin marks as dispatched | `DELIVERED` on delivery confirmation |
| `DELIVERED` | Delivery confirmed | Terminal |
| `CANCELLED` | User or admin cancellation | Terminal |

#### 4.9.2 Payment State Machine

```
         ┌─────────┐
   ──────►│ PENDING │◄──── Payment initiated
         └────┬────┘
              │
    ┌─────────┴─────────┐
    │ Razorpay signature │ Timeout / invalid
    │ validated          │ signature
    ▼                    ▼
┌─────────┐         ┌────────┐
│ SUCCESS │         │ FAILED │ (terminal)
└────┬────┘         └────────┘
     │ Admin processes refund
     ▼
┌──────────┐
│ REFUNDED │ (terminal)
└──────────┘
```

#### 4.9.3 Inventory Status Machine

```
         ┌──────────┐
   ──────►│ IN_STOCK │◄──── stock > threshold
         └────┬─────┘
              │ stock ≤ threshold
              ▼
         ┌───────────┐
         │ LOW_STOCK │
         └─────┬─────┘
               │ stock = 0
               ▼
         ┌─────────────┐
         │ OUT_OF_STOCK│
         └─────────────┘

Transitions back:
  OUT_OF_STOCK ──replenished (partial)──► LOW_STOCK
  OUT_OF_STOCK ──replenished (full)────► IN_STOCK
  LOW_STOCK    ──replenished (full)────► IN_STOCK

DISCONTINUED: Admin-set terminal state; no auto-transitions.
```

#### 4.9.4 Circuit Breaker State Machine

```
         ┌────────┐
   ──────►│ CLOSED │  (normal operation)
         └───┬────┘
             │ failure rate ≥ threshold
             ▼
         ┌────────┐
         │  OPEN  │  (fast-fail; wait duration)
         └───┬────┘
             │ wait duration elapsed
             ▼
         ┌───────────┐
         │ HALF_OPEN │  (test calls)
         └─────┬─────┘
      ┌────────┴────────┐
      │ test succeed    │ test fail
      ▼                 ▼
  ┌────────┐        ┌────────┐
  │ CLOSED │        │  OPEN  │
  └────────┘        └────────┘
```

| Circuit Breaker | Failure Threshold | Timeout | Wait Duration |
| :--- | :--- | :--- | :--- |
| `redis-circuit-breaker` | **70%** | 3 seconds | 30 seconds |
| `database-circuit-breaker` | **50%** | 8 seconds | 60 seconds |

> **Note**: Thresholds verified from `application.properties` via Baseline Assessment. The archived SDD v2.0 stated incorrect values (50% Redis, 60% DB). Corrected here.

#### 4.9.5 JWT Token Lifecycle

```
         ┌───────┐
   ──────►│ VALID │◄──── Login / Register
         └───┬───┘
             │ expiry (15 min)
             ▼
         ┌─────────┐
         │ EXPIRED │
         └────┬────┘
              │ client calls /refresh with valid refresh token
              ▼
         ┌─────────────┐
         │ ROTATED     │  (old invalidated, new issued)
         └─────────────┘

Refresh Token:
  VALID ──(30 days)──► EXPIRED
  VALID ──(rotation)──► INVALIDATED (old) + new VALID issued
  VALID ──(logout)────► INVALIDATED
```

---

### 4.10 Resource View

#### 4.10.1 Deployment Topology

```
                    ┌──────────────────────────────────────────────┐
                    │  KUBERNETES CLUSTER                          │
                    │                                              │
  Internet          │  ┌──────────────────────────────────────┐   │
  ──────────────────┼─►│  Ingress / Load Balancer             │   │
                    │  │  (TLS termination, Let's Encrypt)    │   │
                    │  └──────┬───────────┬─────────────┬─────┘   │
                    │         │           │             │          │
                    │         ▼           ▼             ▼          │
                    │  ┌─────────┐ ┌─────────┐ ┌─────────┐        │
                    │  │ Pod 1   │ │ Pod 2   │ │ Pod N   │        │
                    │  │ JVM 21  │ │ JVM 21  │ │ (HPA)  │        │
                    │  │ :8080   │ │ :8080   │ │ :8080  │        │
                    │  └────┬────┘ └────┬────┘ └────┬────┘        │
                    │       └──────┬────┘           │             │
                    │              ▼                 │             │
                    │  ┌───────────────────────────────────┐      │
                    │  │  DATA TIER                        │      │
                    │  │  MySQL 8.2    Redis 7    ES 8.17  │      │
                    │  │  :3306        :6379      :9200    │      │
                    │  └───────────────────────────────────┘      │
                    │                                              │
                    │  ┌───────────────────────────────────┐      │
                    │  │  OBSERVABILITY                    │      │
                    │  │  Prometheus   Logstash   Kibana   │      │
                    │  │  :9090        :5000      :5601    │      │
                    │  └───────────────────────────────────┘      │
                    └──────────────────────────────────────────────┘
```

#### 4.10.2 Kubernetes Resource Configuration (Verified)

| Resource | Parameter | Value | Source |
| :--- | :--- | :--- | :--- |
| Deployment replicas | Default | 3 | `kubernetes/buildnest-deployment.yaml` |
| Pod CPU request | | 250m | Kubernetes manifest |
| Pod CPU limit | | 500m | Kubernetes manifest |
| Pod memory request | | **512Mi** | Kubernetes manifest |
| Pod memory limit | | **1Gi** | Kubernetes manifest |
| HPA — CPU trigger | | 75% | `kubernetes/buildnest-deployment.yaml` |
| HPA — Memory trigger | | Configured | `kubernetes/buildnest-deployment.yaml` |

#### 4.10.3 Application Resource Configuration

| Resource | Parameter | Default | Production |
| :--- | :--- | :--- | :--- |
| JVM heap | `-Xms` / `-Xmx` | Auto | Bounded by 1 Gi pod limit |
| Server port | `server.port` | 8080 | 8080 |
| Graceful shutdown | `server.shutdown` | graceful | graceful |
| Shutdown drain | `spring.lifecycle.timeout-per-shutdown-phase` | 30 s | 30 s |
| **HikariCP max pool** | `maximum-pool-size` | **20** | **30** |
| **HikariCP min idle** | `minimum-idle` | **10** | **15** |
| HikariCP conn timeout | `connection-timeout` | 30,000 ms | 30,000 ms |
| HikariCP idle timeout | `idle-timeout` | 600,000 ms | 600,000 ms |
| HikariCP max lifetime | `max-lifetime` | 1,800,000 ms | 1,800,000 ms |
| HikariCP leak detection | `leak-detection-threshold` | 60,000 ms | 60,000 ms |
| Redis pool max-active | `jedis.pool.max-active` | 8 | **32** |
| Redis pool max-idle | `jedis.pool.max-idle` | 8 | **16** |
| Redis timeout | `timeout` | 3,000 ms | 3,000 ms |

#### 4.10.4 Thread Model

| Thread Pool | Size | Purpose |
| :--- | :--- | :--- |
| Tomcat HTTP worker | 200 (default) | Handles all inbound HTTP requests |
| HikariCP | 20 (dev) / 30 (prod) | JDBC database connections |
| Redis Lettuce pool | 8 (dev) / 32 (prod) | Cache and rate limit Redis operations |
| Spring async / scheduler | 2 threads | Token cleanup, inventory threshold monitoring |

#### 4.10.5 Frontend Deployment (Verified — 2026-07-17, #459)

Real, deployed (#125), verified against `frontend/Dockerfile`, `frontend/nginx.conf`:

| Component | Configuration | Purpose |
| :--- | :--- | :--- |
| Build artifact | `dist/` (HTML / CSS / JS) | Vite production build output (`npm run build`, `node:22-alpine` builder stage) |
| Web server | `nginxinc/nginx-unprivileged:1.27-alpine` | Non-root, listens on 8080 by default (OPS-07 acceptance criteria) — not a plain `nginx:alpine` image |
| Docker image | Multi-stage: `node:22-alpine` (builder) → `nginxinc/nginx-unprivileged:1.27-alpine` (runtime), copies `dist/` to `/usr/share/nginx/html` | Keeps the runtime image free of Node/build tooling |
| Healthcheck | `wget -q --spider http://localhost:8080/` every 30 s | Container-level liveness check |
| Static asset caching | `location ~* \.(js\|css\|woff2?\|ttf\|eot\|svg\|png\|jpg\|jpeg\|gif\|ico)$` → `Cache-Control: public, immutable`, `expires 1y` | Applied to Vite's content-hashed JS/CSS/font/image bundles |
| `index.html` caching | `Cache-Control: no-cache` (explicit, separate `location = /index.html` block) | Ensures new deploys with new hashed asset filenames are picked up immediately |
| SPA fallback | `location /` → `try_files $uri $uri/ /index.html` | All unmatched paths served as `index.html` for React Router client-side routing |
| Security headers | `security-headers.conf`, included on every `location` block | Applied at the nginx layer, not by the SPA itself |

---

## 5. Design Overlays

### 5.1 Security Overlay

#### 5.1.1 Security Filter Chain Order

Spring Security processes requests through the following chain in order:

| Position | Component | Responsibility | Active Profile |
| :--- | :--- | :--- | :--- |
| 1 | `HttpsEnforcementFilter` | Redirects HTTP → HTTPS | Production only |
| 2 | CORS Filter | Validates `Origin` header against allowlist (`buildnest.com`) | All |
| 3 | CSRF Protection | `CookieCsrfTokenRepository` double-submit pattern; validates `X-XSRF-TOKEN` header against the `XSRF-TOKEN` cookie on all mutating requests except `/api/auth/login` and `/api/auth/register` (SEC-15) | All (non-test) |
| 4 | `JwtAuthenticationFilter` | Extracts JWT from `Authorization: Bearer` header, falling back to the `access_token` cookie; populates `SecurityContext` | All (non-test) |
| 5 | `AdminRateLimitFilter` | Enforces rate limits on `/api/admin/**` | All |
| 6 | Spring Authorization | Role-based path matching; returns 401 / 403 | All |

> **Profile constraint**: `SecurityConfig` is annotated `@Profile("!test")`. Tests use a separate `TestSecurityConfig` that disables JWT validation. This is the correct pattern for test isolation.

#### 5.1.2 JWT Design

| Property | Value | SRS Req |
| :--- | :--- | :--- |
| Algorithm | HMAC-SHA512 | SEC-02 |
| Access token TTL | 15 minutes (900,000 ms) | FR-AUTH-03 |
| Refresh token TTL | 30 days (2,592,000,000 ms) | FR-AUTH-04 |
| Secret minimum length | 512 bits (enforced by `JwtKeyValidator`) | SEC-02, FR-AUTH-05 |
| Secret source | `${JWT_SECRET}` — no default | SEC-02 |
| Key rotation support | `${jwt.secret.previous}` — previous key accepted for validation during rotation | SEC-12 |
| Refresh token storage | Opaque UUID stored in `refresh_token` table | FR-AUTH-06 |
| Refresh token rotation | Old token deleted on each refresh; new token issued | FR-AUTH-06 |

**Dual-key rotation design** (`JwtTokenProvider`): The `getPreviousSigningKey()` method returns `null` when `jwt.secret.previous` is empty. During rotation, both current and previous keys are tried for validation, allowing in-flight tokens to remain valid through the rotation window.

#### 5.1.3 Authorization Matrix

| Endpoint Pattern | Public | USER | ADMIN | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| `/api/auth/**` | ✓ | — | — | FR-AUTH-01–08 |
| `/api/public/**` | ✓ | — | — | FR-PROD-01–03 |
| `/api/user/**` | ✗ | ✓ | ✓ | FR-AUTH-09 |
| `/api/admin/**` | ✗ | ✗ | ✓ | FR-ADM-08 |
| `/api/checkout/**` | ✗ | ✓ | ✓ | FR-CHK-01–04 |
| `/api/inventory/**` (GET) | ✓ | ✓ | ✓ | FR-INV-01–02 |
| `/api/inventory/**` (POST) | ✗ | ✗ | ✓ | FR-INV-03–04 |
| `/actuator/health` | ✓ | — | — | FR-MON-01 |
| `/actuator/prometheus` | ✓ | — | — | FR-MON-05 |
| `/actuator/**` (other) | ✗ | ✗ | ✓ | FR-MON-03 |
| `/swagger-ui.html` | ✓ | — | — | UI-02 |
| `/v3/api-docs/**` | ✓ | — | — | UI-03 |

#### 5.1.4 Security Headers Design

| Header | Value | SRS Req |
| :--- | :--- | :--- |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | SEC-03 |
| `X-Frame-Options` | `DENY` | SEC-05 |
| `X-Content-Type-Options` | `nosniff` | SEC-05 |
| `Content-Security-Policy` | `default-src 'self'; script-src 'self' 'unsafe-inline'` | SEC-14 (gap — `unsafe-inline` to be removed in Phase 2) |

#### 5.1.5 Rate Limiting Design

Bucket4j token-bucket strategy backed by Redis:

| Endpoint Category | Limit | Window | Enforcement Point | SRS Req |
| :--- | :--- | :--- | :--- | :--- |
| Login | 3 requests | 5 minutes | `RateLimiterService` | SEC-07 |
| Password reset | 3 requests | 60 minutes | `RateLimiterService` | SEC-08 |
| Token refresh | 10 requests | 1 minute | `RateLimiterService` | SEC-10 |
| Product search | 60 requests | 1 minute | `RateLimiterService` | SEC-11 |
| Admin endpoints | 50 requests | 1 minute | `AdminRateLimitFilter` | SEC-09 |
| User endpoints | 500 requests | 1 minute | `RateLimiterService` | SEC-10 |
| General API | 200 requests | 1 minute | `RateLimiterService` | SEC-10 |

**Fail-open design**: If Redis is unavailable (circuit breaker open), rate limiting is bypassed and requests are permitted. This prevents a Redis outage from causing an availability incident. The trade-off (temporary loss of rate protection) is accepted as the lesser risk.

---

### 5.2 Error Handling Overlay

#### 5.2.1 Exception-to-HTTP Mapping

`GlobalExceptionHandler` (`@RestControllerAdvice`) is the single point of exception-to-response mapping:

| Exception | HTTP Status | Error Code |
| :--- | :--- | :--- |
| `ResourceNotFoundException` | 404 Not Found | `RESOURCE_NOT_FOUND` |
| `DuplicateResourceException` | 409 Conflict | `DUPLICATE_RESOURCE` |
| `ValidationException` | 400 Bad Request | `VALIDATION_ERROR` |
| `AuthenticationException` | 401 Unauthorized | `AUTHENTICATION_FAILED` |
| `AuthorizationException` | 403 Forbidden | `AUTHORIZATION_FAILED` |
| `AccessDeniedException` | 403 Forbidden | `ACCESS_DENIED` |
| `InventoryException` | 409 Conflict | `INVENTORY_ERROR` |
| `PaymentProcessingException` | 502 Bad Gateway | `PAYMENT_ERROR` |
| `ExternalServiceException` | 503 Service Unavailable | `EXTERNAL_SERVICE_ERROR` |
| `MethodArgumentNotValidException` | 400 Bad Request | `VALIDATION_ERROR` |
| `Exception` (catch-all) | 500 Internal Server Error | `INTERNAL_ERROR` |

#### 5.2.2 Error Response Design Principles

- All error responses use the same JSON structure (see §4.7.1).
- Stack traces are never included in API responses.
- `SecureLogger` is used to sanitise log output and prevent PII or secrets from appearing in logs.
- Business exceptions carry an `errorCode` that is machine-readable for client error handling.

---

### 5.3 Resilience Overlay

#### 5.3.1 Circuit Breaker Configuration

| Instance | Protects | Failure Threshold | Slow Call Threshold | Wait Duration |
| :--- | :--- | :--- | :--- | :--- |
| `redis-circuit-breaker` | Redis cache and rate limit calls | **70%** | — | 30 seconds |
| `database-circuit-breaker` | All JPA / JDBC calls | **50%** | 50% > 8 s | 60 seconds |

#### 5.3.2 Time Limiter Configuration

| Instance | Protects | Timeout |
| :--- | :--- | :--- |
| `redis-time-limiter` | Redis operations | **3 seconds** |
| `database-time-limiter` | Database operations | **8 seconds** |

#### 5.3.3 Graceful Degradation Matrix

| Failure Scenario | System Behaviour | User Impact |
| :--- | :--- | :--- |
| Redis unavailable | Circuit breaker opens; rate limiting disabled (fail-open); caching bypassed | Slightly higher DB load; no rate protection |
| Redis slow (> 3 s) | Time limiter fires; operation fails fast; circuit breaker accumulates failures | Individual requests fail fast |
| Database slow (> 8 s) | Time limiter fires; circuit breaker accumulates failures | Requests fail fast after threshold |
| Database unavailable (> 50%) | Circuit breaker opens; 503 responses returned | Service unavailable until recovery |
| Elasticsearch unavailable | Feature disabled (`elasticsearch.enabled` checked at startup) | Search / analytics unavailable; core e-commerce unaffected |
| Razorpay unavailable | `PaymentProcessingException` thrown; non-payment checkout path still works | Cannot complete paid checkout; cart preserved |
| Logstash unavailable | Logs fall back to `logs/buildnest-ecommerce.log` (file appender) | No real-time log aggregation; logs preserved locally |

---

## 6. Design Rationale

### 6.1 Architectural Decisions

| Decision | Rationale | Alternatives Considered |
| :--- | :--- | :--- |
| Layered monolithic architecture | Appropriate for project scale; simpler deployment; single codebase; future decomposition remains possible without breaking API contracts | Microservices — rejected: premature complexity, operational overhead disproportionate to team size |
| Layered package organisation by domain (not by layer alone) | Improves cohesion; domain code co-located; easier to navigate and extract | Pure layer packaging (`controllers/`, `services/`) — rejected: splits related code across top-level packages |
| JWT stateless authentication | Enables horizontal pod scaling without shared session store; no sticky session requirement | Server-side sessions — rejected: requires sticky routing or shared session store, complicates K8s deployment |
| Redis for distributed rate limiting | Shared counter across all pods; sub-millisecond operations; atomic increment via Lua scripts (Bucket4j) | In-memory rate limiting — rejected: per-pod counters allow N × limit requests across N pods |
| Resilience4j circuit breakers | Prevents cascading failures; configurable thresholds per dependency; Spring Boot integration | Retry-only strategy — rejected: retries amplify load during partial outages |
| Service interface pattern | Every service is testable via interface mock; implementation swappable without controller changes | Direct class injection — rejected: tight coupling; mocking requires Spy or partial mock |
| Domain events via Spring `ApplicationEvent` | Decouples inventory, notification, and webhook concerns from checkout; reduces service-to-service coupling | Direct service calls — rejected: creates a fan-out dependency graph from `CheckoutServiceImpl` |
| Liquibase for schema management | Version-controlled, reproducible, environment-aware migrations; `validate` mode prevents silent schema drift | Hibernate `create-drop` / `update` — rejected: unsafe for production; loses history |
| `@Profile("!test")` on `SecurityConfig` | Allows tests to run without JWT infrastructure; test security config applies predictable mock auth | Single config with test bypass flags — rejected: brittle; can accidentally disable security in production |
| Fail-fast on startup (`@PostConstruct`) | Missing JWT secret or SSL in production is caught at startup, not at first request | Runtime checks — rejected: allows the application to start in an insecure state |
| `FetchType.EAGER` for `User.roles` and `Role.permissions` | RBAC evaluation requires both on every authenticated request; bounded size (< 10 roles, < 50 permissions per user) | LAZY + JOIN FETCH — rejected: requires every authentication path to explicitly join, increasing query count |
| API versioning (V1 deprecated, V2 current) | Backward compatibility for existing consumers; `ApiSunsetInterceptor` signals deprecation via headers | Header versioning — rejected: harder to test, cache, and proxy |
| Dual-key JWT rotation (`jwt.secret.previous`) | In-flight tokens remain valid during secret rotation; zero-downtime key rotation | Single key only — rejected: forces all users to re-authenticate during rotation |

### 6.2 Technology Selection Rationale

| Technology | Why Selected | Known Trade-offs |
| :--- | :--- | :--- |
| Java 21 (LTS) | Long-term support; virtual thread support (Project Loom); modern language features (records, sealed classes) | Higher memory baseline vs Go; verbose compared to Kotlin |
| Spring Boot 3.5.10 | Mature ecosystem; autoconfiguration reduces boilerplate; production-ready observability stack (Actuator) | Opinionated; startup time higher than lightweight frameworks |
| MySQL 8.2 | Proven RDBMS; strong ACID compliance; InnoDB row-level locking; wide cloud hosting support | Relational model less flexible than NoSQL for unstructured data |
| Redis 7 | Sub-millisecond latency; atomic Lua scripts for Bucket4j; pub/sub for future event use; Lettuce client maturity | Additional infrastructure component; single point of failure mitigated by circuit breaker |
| Elasticsearch 8.17 | Full-text search, structured analytics, and log aggregation in one system; Spring Data integration | Resource-intensive (minimum 512 MB JVM heap); optional for core functionality |
| Razorpay Java SDK 1.4.5 | India-focused payment gateway; comprehensive SDK; webhook support; signature verification built in | Vendor lock-in; limited to Indian domestic payments |
| JJWT 0.12.3 | Active maintenance; HMAC-SHA512 support; explicit key validation API | More verbose than `nimbus-jose-jwt` for simple use cases |
| Bucket4j 8.1.0 | Redis-backed distributed token bucket; Spring integration; per-key isolation | Configuration complexity for multiple rate limit rules |
| Resilience4j 2.1.0 | Spring Boot 3.x native support; annotation and programmatic API; fine-grained per-instance configuration | Less mature than Hystrix (now retired); fewer community examples |

---

## 7. Design-to-Requirements Traceability

| Design Element | SRS Requirement(s) | View |
| :--- | :--- | :--- |
| `AuthController`, `AuthServiceImpl` | FR-AUTH-01–11 | Logical, Interface, Interaction |
| `JwtTokenProvider`, `JwtAuthenticationFilter` | FR-AUTH-02–07, SEC-02 | Security Overlay, Logical |
| `SecurityConfig` (`@Profile("!test")`) | SEC-01–14, FR-AUTH-09 | Security Overlay |
| `AdminRateLimitFilter`, `RateLimiterService` | SEC-07–11 | Security Overlay |
| `ProductServiceImpl`, `ProductControllerV1`, `ProductControllerV2` | FR-PROD-01–07, UR-04, UR-05 | Interface, Interaction |
| `CartServiceImpl`, `CartController` | FR-CART-01–06 | Interface, Information |
| `CheckoutServiceImpl`, `CheckoutController` | FR-CHK-01–08 | Interaction, State Dynamics |
| `PaymentServiceImpl`, `RazorpayClientAdapter` | FR-PAY-01–05 | Interaction, Resilience Overlay |
| `InventoryServiceImpl` | FR-INV-01–07, SAF-03 | State Dynamics, Information |
| `AuditAspect`, `@Auditable`, `AuditLogService` | FR-ADM-04, MNT-05 | Logical, Security Overlay |
| `GlobalExceptionHandler` | UR-01, UR-02 | Error Handling Overlay |
| `ResilienceConfig` (Resilience4j) | REL-02, REL-03, AVL-04 | Resilience Overlay, State Dynamics |
| `CacheConfig`, `@Cacheable` / `@CacheEvict` | FR-PROD-06, FR-PROD-07, PR-01 | Information |
| `ApiSunsetInterceptor` | FR-PROD-05, UR-05 | Interface |
| `PerformanceMonitoringInterceptor` | FR-MON-05, PR-01 | Resource |
| Kubernetes manifests (3 replicas, HPA) | SCL-01–04, AVL-01 | Resource |
| Prometheus alert rules (13) | FR-MON-08, REL-01 | Resource |
| HikariCP configuration | PR-05, PR-06 | Resource |
| Liquibase changelogs | MNT-04, DC-12 | Information |
| `HttpsEnforcementFilter`, `@PostConstruct` SSL check | SEC-03, FR-MON-01 | Security Overlay |
| React SPA design (§4.3.6, §4.7.4, §4.10.5) | FR-FE-01–31 | Composition, Interface |
| Domain events (`DomainEventPublisher`) | FR-INV-06, FR-PAY-04 | Logical, Interaction |

---

## 8. Appendices

### Appendix A: Configuration Properties Reference

| Property | Default | Env Variable | Purpose |
| :--- | :--- | :--- | :--- |
| `server.port` | 8080 | — | HTTP listener port |
| `server.shutdown` | graceful | — | Enables drain-before-shutdown |
| `spring.lifecycle.timeout-per-shutdown-phase` | 30s | — | Max drain time |
| `jwt.secret` | *(required — no default)* | `JWT_SECRET` | JWT HMAC-SHA512 signing key |
| `jwt.secret.previous` | *(empty)* | — | Previous key for rotation window |
| `jwt.expiration` | 900,000 ms | `JWT_EXPIRATION` | Access token TTL |
| `jwt.refresh-expiration` | 2,592,000,000 ms | `JWT_REFRESH_EXPIRATION` | Refresh token TTL |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/buildnest_ecommerce` | `SPRING_DATASOURCE_URL` | MySQL JDBC URL |
| `spring.datasource.username` | `root` | `SPRING_DATASOURCE_USERNAME` | DB username |
| `spring.datasource.password` | *(empty)* | `SPRING_DATASOURCE_PASSWORD` | DB password |
| `spring.data.redis.host` | `localhost` | `REDIS_HOST` | Redis host |
| `spring.data.redis.port` | 6379 | `REDIS_PORT` | Redis port |
| `spring.data.redis.password` | *(empty)* | `REDIS_PASSWORD` | Redis auth password |
| `elasticsearch.enabled` | `false` | `ELASTICSEARCH_ENABLED` | ES feature toggle |
| `elasticsearch.host` | `localhost` | `ELASTICSEARCH_HOST` | ES host |
| `razorpay.key.id` | *(empty)* | `RAZORPAY_KEY_ID` | Razorpay API key |
| `razorpay.key.secret` | *(empty)* | `RAZORPAY_KEY_SECRET` | Razorpay secret |
| `server.ssl.enabled` | `false` | `SERVER_SSL_ENABLED` | Enable HTTPS |
| `chaos.enabled` | `false` | `CHAOS_ENABLED` | Chaos engineering fault injection |

### Appendix B: Kubernetes Manifest Inventory

| File | Purpose |
| :--- | :--- |
| `kubernetes/buildnest-deployment.yaml` | Deployment: 3 replicas, resource limits, HPA |
| `kubernetes/buildnest-secrets.yaml` | Secrets template (values must be populated before deploy) |
| `kubernetes/buildnest-secrets-template.yaml` | Documentation of required secret keys |
| `kubernetes/letsencrypt-issuer.yaml` | Cert-Manager Let's Encrypt cluster issuer |
| `kubernetes/prometheus-rules.yaml` | 13 PrometheusRule alert definitions |
| `kubernetes/buildnest-rollout.yaml` | Argo Rollouts blue-green strategy (Phase 2) |
| `k8s/base/` | Kustomize base manifests |
| `k8s/overlays/staging/` | Staging-specific overrides |
| `k8s/overlays/production/` | Production-specific overrides |

### Appendix C: Outstanding Design Constraints (Baseline Findings)

The following design items are acknowledged gaps between the current implementation and this SDD's target design. Each is tracked for remediation in Phase 1 or Phase 2 as indicated.

| Finding | Location | Phase | SRS Req |
| :--- | :--- | :--- | :--- |
| `Category.products` and `Order.orderItems` lack explicit `fetch = FetchType.LAZY` | `Category.java`, `Order.java` | Ph-1 | DC-08 |
| `Optional.get()` without guard in `PasswordResetServiceImpl.java:49` | `PasswordResetServiceImpl` | Ph-1 | SAF-03 |
| CSP header contains `unsafe-inline` | `SecurityConfig.java` | Ph-2 | SEC-14 |
| JaCoCo gate set at 40%; target is 70% | `pom.xml` | Ph-2 | MNT-02 |
| E2E tests (`ProductApiTest`, `OrderApiTest`) included in unit-tests Maven profile | `pom.xml` / test classes | Ph-1 | TIR-01 |
| `AuthServiceImplTest` missing `RoleRepository` mock | `AuthServiceImplTest.java` | Ph-1 | TIR-02 |
| `AuthenticationAuthorizationSecurityTest` asserts 401 where 403 is correct | Security test | Ph-1 | TIR-03 |
| `InputValidationSecurityTest` asserts 401 where 400 / 415 is correct | Security test | Ph-1 | TIR-04 |

---

**— End of Document —**

*This document was prepared in conformance with ISO/IEC/IEEE 1016:2017 for the BuildNest E-Commerce Platform. It supersedes SDD v2.0 archived at `archive/docs/ISO-IEC-IEEE/SDD_IEEE_1016_2017.md`. All corrections in v3.0 are evidence-based and traceable to the Baseline Assessment Report (docs/reports/baseline-assessment-2026-06-19.md) and verified against the live codebase as of 2026-06-19.*

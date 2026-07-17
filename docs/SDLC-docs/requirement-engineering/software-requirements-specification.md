# Software Requirements Specification (SRS)

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## DOCUMENT INFORMATION

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Software Requirements Specification (SRS) |
| **Document ID** | SRS-BUILDNEST-001 |
| **Version** | 4.1 |
| **Date** | 2026-07-17 13:43 IST |
| **Status** | Controlled — Under Review |
| **Classification** | Internal Use |
| **Conformance Standard** | ISO/IEC/IEEE 29148:2018 |
| **Supersedes** | SRS v3.0 (archive/docs/ISO-IEC-IEEE/SRS_IEEE_29148_2018.md, 2026-02-11) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date | Author | Changes | Approval |
| :--- | :--- | :--- | :--- | :--- |
| 1.0 | 2026-02-10 | Documentation Team | Initial controlled release per ISO/IEC/IEEE 29148:2018 | Approved |
| 2.0 | 2026-02-11 | Documentation Team | Frontend requirements; API appendix expanded to 19 sections | Approved |
| 3.0 | 2026-02-11 | Documentation Team | ISO 29148:2018 conformance statement added | Approved |
| 4.0 | 2026-06-19 | Claude Code (claude-sonnet-4-6) | Baseline-driven update: corrected Spring Boot version (3.2.2 → 3.5.10); added Phase classification (Ph-1 Stable / Ph-2 Production Ready); added test integrity requirements (TIR); updated MNT-02 coverage target (40% → 70%); corrected MNT-03 to reflect live test state; added SEC-14 CSP requirement; added CON-11 React version constraint; referenced baseline assessment report | Pending |
| 4.1 | 2026-07-17 13:43 IST | Claude Code (claude-sonnet-5) | Corrected two stale technology-stack claims found via direct source verification (#455): Redis client is Lettuce, not Jedis (`lettuce-core:6.6.0` confirmed via `mvnw dependency:tree`; Jedis absent from classpath); Elasticsearch version is 8.17 (`docker-compose.yml`'s active service), not the previously-recorded 8.10. Appendix A's API Endpoint Catalogue was found separately stale (wrong path prefixes, missing endpoint groups) and filed as its own follow-up (#456) rather than fixed here, since it needs a full per-endpoint re-derivation | Pending |

### Document Change Procedure

All changes to this document shall follow the change control process:

1. **Change Request (CR)** — Submit with rationale, impact assessment, and traceability to affected requirements.
2. **Review** — Technical Lead and QA Manager review the proposed change.
3. **Approval** — Project Manager approves or rejects the CR.
4. **Implementation** — Approved changes are implemented and peer-reviewed.
5. **Baseline Update** — New version is baselined, committed to `docs/SDLC-docs/requirement-engineering/`, and distributed.

### Document Approval

| Role | Name | Signature | Date |
| :--- | :--- | :--- | :--- |
| Project Manager | _____________ | _____________ | _____________ |
| Technical Lead | _____________ | _____________ | _____________ |
| QA Manager | _____________ | _____________ | _____________ |

---

## CONFORMANCE STATEMENT

> This document conforms to **ISO/IEC/IEEE 29148:2018** — *Systems and software engineering — Life cycle processes — Requirements engineering*. Requirements are uniquely identified, traceable, measurable, and assigned a verification method per Clause 5.2.5.

---

## Table of Contents

1. [Introduction](#1-introduction)
   - 1.1 [Purpose](#11-purpose)
   - 1.2 [Scope](#12-scope)
   - 1.3 [Product Overview](#13-product-overview)
   - 1.4 [Intended Audience](#14-intended-audience)
   - 1.5 [Definitions, Acronyms, and Abbreviations](#15-definitions-acronyms-and-abbreviations)
   - 1.6 [References](#16-references)
   - 1.7 [Document Overview](#17-document-overview)
2. [Overall Description](#2-overall-description)
   - 2.1 [Product Perspective](#21-product-perspective)
   - 2.2 [Product Functions](#22-product-functions)
   - 2.3 [User Characteristics](#23-user-characteristics)
   - 2.4 [Constraints](#24-constraints)
   - 2.5 [Assumptions and Dependencies](#25-assumptions-and-dependencies)
   - 2.6 [Apportioning of Requirements](#26-apportioning-of-requirements)
   - 2.7 [Stakeholder Needs](#27-stakeholder-needs)
   - 2.8 [Delivery Phases](#28-delivery-phases)
3. [Specific Requirements](#3-specific-requirements)
   - 3.1 [External Interface Requirements](#31-external-interface-requirements)
   - 3.2 [Functional Requirements](#32-functional-requirements)
   - 3.3 [Usability Requirements](#33-usability-requirements)
   - 3.4 [Performance Requirements](#34-performance-requirements)
   - 3.5 [Logical Database Requirements](#35-logical-database-requirements)
   - 3.6 [Design Constraints](#36-design-constraints)
   - 3.7 [Standards Compliance](#37-standards-compliance)
   - 3.8 [Software System Attributes](#38-software-system-attributes)
   - 3.9 [Test Integrity Requirements](#39-test-integrity-requirements)
   - 3.10 [Licensing Requirements](#310-licensing-requirements)
4. [Verification](#4-verification)
5. [Appendices](#5-appendices)

---

## 1. Introduction

### 1.1 Purpose

This Software Requirements Specification (SRS) defines the complete functional and non-functional requirements for the **BuildNest E-Commerce Platform**. It is prepared in conformance with **ISO/IEC/IEEE 29148:2018** — *Systems and software engineering — Life cycle processes — Requirements engineering*.

The purpose of this document is to:

- Provide a clear, complete, and verifiable description of all software requirements.
- Serve as the contractual basis between stakeholders, developers, and quality assurance teams.
- Enable traceability from stakeholder needs through system requirements to software requirements and acceptance criteria.
- Establish the target state against which gap analysis and verification activities are conducted.
- Classify requirements by delivery phase to govern incremental release planning.

**Version 4.0 Change Rationale**: This version incorporates findings from the Baseline Assessment Report (docs/reports/baseline-assessment-2026-06-19.md). Changes include corrected technology versions, updated quality targets based on measured state, addition of test integrity requirements, and explicit phase classification for all requirements. No functional requirements have been removed; scope changes are limited to corrections and additions.

### 1.2 Scope

**BuildNest** is a web-based e-commerce platform specialising in home construction materials and décor products. The system provides a backend REST API and a React-based frontend SPA that together support the full e-commerce lifecycle.

| Capability | Description |
| :--- | :--- |
| User Management | Registration, authentication, profile management, role-based access control |
| Product Catalogue | Product listing, categorisation, search, filtering, product reviews |
| Shopping Cart | Cart management, item addition/removal, quantity updates, totals |
| Order Processing | Checkout, order placement, order history, order tracking |
| Payment Integration | Razorpay payment gateway, transaction management, webhook handling |
| Inventory Management | Stock tracking, availability checking, threshold-based alerts |
| Admin Operations | Analytics, reports, user management, inventory administration, audit logs |
| Monitoring & Observability | Health checks, Prometheus metrics, alerting, structured log aggregation |
| Frontend Application | Responsive React SPA with client-side routing, state management, form validation |

**System Boundary**: This SRS covers the entire BuildNest platform — the **Spring Boot 3.5 backend API** and the **React 19 Frontend SPA**.

**Out of Scope**:
- Native mobile applications (iOS / Android)
- Third-party payment gateway internals (Razorpay)
- Physical logistics and shipping systems
- Customer support and ticketing systems

### 1.3 Product Overview

#### 1.3.1 Product Perspective

BuildNest is a full-stack web application comprising a React Single Page Application (SPA) frontend and a Spring Boot 3.5 backend API. It operates within an ecosystem of external services:

```
End User ──HTTPS──► React SPA ──REST/JSON──► Spring Boot API
                                                   │
                                    ┌──────────────┼──────────────┐
                                    │              │              │
                                 MySQL 8.2      Redis 7     Elasticsearch 8.17
                                 (Primary)     (Cache)      (Search/Analytics)
                                    │
                               Razorpay (Payments)
                               Prometheus (Metrics)
                               Logstash → Elasticsearch → Kibana (Logs)
```

The system is designed for deployment on **Kubernetes** (manifests provided) or **AWS** (Terraform IaC provided).

#### 1.3.2 Product Functions Summary

| ID | Feature Group | Description |
| :--- | :--- | :--- |
| FG-01 | Authentication & Security | User registration, login, JWT lifecycle, password reset, RBAC, OAuth2, rate limiting |
| FG-02 | Product Catalogue | Product CRUD, category management, versioned APIs (v1/v2), search and filtering |
| FG-03 | Shopping Cart | Add/remove/update items, view cart, calculate totals, clear cart |
| FG-04 | Checkout & Orders | Cart validation, checkout processing, payment integration, order history |
| FG-05 | Payment Processing | Razorpay order creation, payment verification, webhook event handling |
| FG-06 | Inventory Management | Stock tracking, availability checking, threshold alerts, admin stock operations |
| FG-07 | Reviews & Wishlists | Product reviews with ratings, wishlist management |
| FG-08 | Admin Operations | User management, order management, product management, analytics, reports, audit logs |
| FG-09 | Monitoring & Alerting | Health checks (DB, Redis), Prometheus metrics, Elasticsearch alerting, webhook events |
| FG-10 | Frontend Experience | Responsive SPA, client-side routing, form validation, error handling, state management |

#### 1.3.3 System Interfaces

| Interface | Technology | Purpose |
| :--- | :--- | :--- |
| MySQL 8.2 | JDBC / JPA (HikariCP) | Primary relational data store |
| Redis 7 | Lettuce client | Caching, rate limiting |
| Elasticsearch 8.17 | Spring Data Elasticsearch | Full-text search, analytics, log aggregation |
| Razorpay | REST SDK | Payment processing |
| Prometheus | Micrometer registry | Metrics collection |
| Logstash | TCP / JSON | Log ingestion pipeline |
| Kibana | REST (via Elasticsearch) | Log and metrics visualisation |

### 1.4 Intended Audience

| Audience | Use of This Document |
| :--- | :--- |
| Project Managers | Project scope, scheduling, milestone and phase tracking |
| Software Developers | Implementation guidance, API contract, design constraints |
| QA / Test Engineers | Test case derivation, acceptance criteria, verification plan |
| System Architects | Architecture decisions, integration requirements, technology stack |
| DevOps Engineers | Deployment requirements, infrastructure needs, monitoring setup |
| Security Auditors | Security requirements, compliance verification (OWASP, PCI-DSS) |
| Product Owners | Feature validation, scope agreement, acceptance sign-off |

### 1.5 Definitions, Acronyms, and Abbreviations

#### 1.5.1 Definitions

| Term | Definition |
| :--- | :--- |
| **BuildNest** | The e-commerce platform for home construction and décor products |
| **Cart** | A temporary collection of products selected by a user before checkout |
| **Checkout** | The process of converting a cart into a confirmed order with payment |
| **Inventory** | The quantity and availability status of products in stock |
| **Threshold Alert** | An automated notification triggered when inventory falls below a configured level |
| **Rate Limiting** | Restricting API requests per time window to prevent abuse |
| **Circuit Breaker** | A resilience pattern that prevents cascading failures by short-circuiting failing dependencies |
| **Webhook** | An HTTP callback triggered by system events (payments, orders, inventory changes) |
| **Phase 1 (Stable)** | Delivery phase targeting a fully passing test suite and stable build gate |
| **Phase 2 (Production Ready)** | Delivery phase targeting deployable, observable, operationally sound production release |
| **Test Integrity** | The property that a test suite's pass/fail signal is a truthful indicator of system correctness |

#### 1.5.2 Acronyms and Abbreviations

| Acronym | Expansion |
| :--- | :--- |
| ACID | Atomicity, Consistency, Isolation, Durability |
| API | Application Programming Interface |
| CORS | Cross-Origin Resource Sharing |
| CSP | Content Security Policy |
| CRUD | Create, Read, Update, Delete |
| CSRF | Cross-Site Request Forgery |
| DTO | Data Transfer Object |
| GDPR | General Data Protection Regulation |
| HSTS | HTTP Strict Transport Security |
| JWT | JSON Web Token |
| LCP | Largest Contentful Paint |
| OWASP | Open Web Application Security Project |
| PCI-DSS | Payment Card Industry Data Security Standard |
| RBAC | Role-Based Access Control |
| REST | Representational State Transfer |
| RTO | Recovery Time Objective |
| RPO | Recovery Point Objective |
| SPA | Single Page Application |
| SRS | Software Requirements Specification |
| TIR | Test Integrity Requirement |
| TLS | Transport Layer Security |
| TTL | Time To Live |
| WCAG | Web Content Accessibility Guidelines |

### 1.6 References

| ID | Document / Standard | Version |
| :--- | :--- | :--- |
| REF-01 | ISO/IEC/IEEE 29148:2018 — Requirements Engineering | 2018 |
| REF-02 | ISO/IEC/IEEE 12207:2017 — Software Life Cycle Processes | 2017 |
| REF-03 | ISO/IEC 25010:2011 — Systems and Software Quality Models | 2011 |
| REF-04 | OWASP Top 10 — Web Application Security Risks | 2021 |
| REF-05 | OWASP ASVS — Application Security Verification Standard | 4.0 |
| REF-06 | PCI-DSS — Payment Card Industry Data Security Standard | v4.0 |
| REF-07 | GDPR — General Data Protection Regulation | 2018 |
| REF-08 | Spring Boot Reference Documentation | 3.5.10 |
| REF-09 | Razorpay API Documentation | Latest |
| REF-10 | BuildNest Baseline Assessment Report | 2026-06-19 |
| REF-11 | BuildNest Business Rules Document (BRD-BUILDNEST-001) | 2.0 |
| REF-12 | BuildNest SRS v3.0 (archived) | 3.0 |
| REF-13 | 12-Factor App Methodology | 1.0 |

### 1.7 Document Overview

- **Section 1 — Introduction**: Purpose, scope, audience, terminology, and references.
- **Section 2 — Overall Description**: Product perspective, high-level functions, user characteristics, constraints, assumptions, phases.
- **Section 3 — Specific Requirements**: Detailed functional, non-functional, external interface, database, design, test integrity, and licensing requirements.
- **Section 4 — Verification**: Verification methods and traceability matrix.
- **Section 5 — Appendices**: API endpoint catalogue, rate limiting configuration, deployment architecture, use case scenarios.

---

## 2. Overall Description

### 2.1 Product Perspective

BuildNest is a new, self-contained product. It is not a replacement for or enhancement of any existing system. The platform uses a **layered monolithic architecture**:

```
┌──────────────────────────────────────────────────┐
│  Presentation Layer (REST Controllers)           │
│  auth/ · user/ · admin/ · inventory/ · public_/ │
├──────────────────────────────────────────────────┤
│  Business Logic Layer (Services)                 │
│  auth · cart · checkout · order · payment        │
│  product · inventory · review · wishlist         │
│  analytics · audit · elasticsearch · webhook     │
├──────────────────────────────────────────────────┤
│  Data Access Layer                               │
│  Spring Data JPA Repositories · Redis Cache      │
│  Elasticsearch Repositories                      │
├──────────────────────────────────────────────────┤
│  External Systems                                │
│  MySQL 8.2 · Redis 7 · Elasticsearch 8.17       │
│  Razorpay · Prometheus · Logstash               │
└──────────────────────────────────────────────────┘
```

#### 2.1.1 Operating Environment

| Environment | Specification |
| :--- | :--- |
| Server OS | Linux (Alpine / Debian in Docker containers) |
| Backend Runtime | JDK 21 LTS (Eclipse Temurin) |
| Backend Framework | Spring Boot 3.5.10 |
| Frontend Runtime | Node.js 18+ (build-time); Chrome 90+, Firefox 90+, Edge 90+, Safari 15+ |
| Container Engine | Docker 24+ with multi-stage builds |
| Orchestration | Kubernetes 1.28+ |
| CI/CD | GitHub Actions (6 workflows) |

### 2.2 Product Functions

| ID | Feature Group | Description |
| :--- | :--- | :--- |
| FG-01 | Authentication & Security | Registration, login, JWT lifecycle, password reset, RBAC, OAuth2, rate limiting |
| FG-02 | Product Catalogue | Product CRUD, category management, versioned APIs (v1/v2), search and filtering |
| FG-03 | Shopping Cart | Add/remove/update items, view cart, calculate totals, clear cart |
| FG-04 | Checkout & Orders | Cart validation, checkout processing, payment integration, order history |
| FG-05 | Payment Processing | Razorpay order creation, payment verification, webhook event handling |
| FG-06 | Inventory Management | Stock tracking, availability checking, threshold alerts, admin stock operations |
| FG-07 | Reviews & Wishlists | Product reviews with ratings, wishlist management |
| FG-08 | Admin Operations | User management, order management, product management, analytics, reports, audit logs |
| FG-09 | Monitoring & Alerting | Health checks, Prometheus metrics, Elasticsearch alerting, webhook events |
| FG-10 | Frontend Experience | Responsive SPA, client-side routing, form validation, error handling, state management |

### 2.3 User Characteristics

| User Role | Description | Technical Expertise | Primary Functions |
| :--- | :--- | :--- | :--- |
| **End User (USER)** | Registered customer browsing and purchasing products | Low to moderate; interacts via frontend SPA | Browse products, manage cart, place orders, write reviews, manage wishlist |
| **Administrator (ADMIN)** | Platform operator managing products, inventory, and system health | Moderate to high; may use admin UI or direct API | Manage products/inventory, view analytics/reports, manage users, audit logs |
| **API Consumer (Developer)** | Frontend or third-party developer integrating with the API | High; direct REST API interaction | All API endpoints per granted role |
| **DevOps / SRE** | Operations team managing deployment and monitoring | High; infrastructure and monitoring tools | Deployment, health monitoring, alerting, disaster recovery |

### 2.4 Constraints

| ID | Constraint | Description |
| :--- | :--- | :--- |
| CON-01 | Language & Runtime | Java 21 (LTS) is required |
| CON-02 | Backend Framework | Spring Boot **3.5.10** with Spring Security, Spring Data JPA |
| CON-03 | Database | MySQL **8.2** is the primary relational data store |
| CON-04 | Cache | Redis 7 with Lettuce client is required for caching and rate limiting |
| CON-05 | Payment Gateway | Razorpay is the sole supported payment provider |
| CON-06 | Security | JWT tokens with minimum 512-bit secret; HTTPS mandatory in production |
| CON-07 | Build System | Apache Maven with Maven Wrapper |
| CON-08 | Deployment | Docker containers on Kubernetes (manifests provided) |
| CON-09 | Regulatory | Must comply with PCI-DSS for payment data handling; GDPR for personal data |
| CON-10 | API Versioning | Backward-compatible API versioning (v1 deprecated, v2 current) |
| CON-11 | Frontend Framework | React **19.2** with Vite **8.0** build tooling |
| CON-12 | Schema Management | All DDL changes must go through Liquibase changesets; `ddl-auto=validate` |

### 2.5 Assumptions and Dependencies

#### 2.5.1 Assumptions

| ID | Assumption |
| :--- | :--- |
| ASM-01 | The frontend will be a Single Page Application (SPA) built with React 19 |
| ASM-02 | All users will access the system through HTTPS in production |
| ASM-03 | Razorpay service will maintain its published API contract |
| ASM-04 | Database schema migrations will be managed through Liquibase changelogs |
| ASM-05 | Redis will be available for rate limiting; the system falls back gracefully if Redis is unavailable via the configured circuit breaker |
| ASM-06 | Elasticsearch is optional for core functionality; the system operates without it for basic e-commerce features (`elasticsearch.enabled=false` is a valid configuration) |
| ASM-07 | The JWT secret (`JWT_SECRET`) will always be externally provided; the application will fail fast on startup if absent |

#### 2.5.2 Dependencies

| ID | Dependency | Impact if Unavailable |
| :--- | :--- | :--- |
| DEP-01 | MySQL 8.2 | **Critical** — System cannot operate; all data persistence fails |
| DEP-02 | Redis 7 | **High** — Rate limiting disabled, caching unavailable; circuit breaker activates |
| DEP-03 | Razorpay API | **High** — Payment processing unavailable; orders without payment still possible |
| DEP-04 | Elasticsearch 8.17 | **Low** — Search / analytics unavailable; core e-commerce functions unaffected |
| DEP-05 | Prometheus / Grafana | **Low** — Monitoring data unavailable; application functions normally |
| DEP-06 | JDK 21 | **Critical** — Backend cannot compile or run |
| DEP-07 | Node.js 18+ / NPM | **Critical** — Frontend build and development environment cannot function |

### 2.6 Apportioning of Requirements

Requirements deferred to future releases:

| ID | Requirement | Target Release |
| :--- | :--- | :--- |
| FUT-01 | Kafka-based event streaming (infrastructure present; not active) | v1.1 |
| FUT-02 | Multi-vendor marketplace support | v2.0 |
| FUT-03 | Native mobile application (iOS / Android) | v2.0 |
| FUT-04 | Multi-region deployment | v2.0 |
| FUT-05 | Pact consumer contract tests (infrastructure present; 0 active tests) | v1.1 |

### 2.7 Stakeholder Needs

Per ISO/IEC/IEEE 29148:2018 Clause 6.3:

| ID | Stakeholder | Need | Traced To |
| :--- | :--- | :--- | :--- |
| SN-01 | End Users | Intuitive product browsing and seamless checkout experience | FG-02, FG-03, FG-04, FG-10 |
| SN-02 | End Users | Secure account management and trusted payment processing | FG-01, FG-05 |
| SN-03 | Administrators | Real-time visibility into sales, inventory, and user activity | FG-06, FG-08, FG-09 |
| SN-04 | Business Owners | Scalable platform supporting growth to 1,000+ concurrent users | PR-02, SCL-01–04 |
| SN-05 | DevOps / SRE | Observable, container-ready application with zero-downtime deployments | FG-09, PRT-01–04 |
| SN-06 | Security Auditors | Compliance with OWASP, PCI-DSS, and GDPR standards | SEC-01–14 |
| SN-07 | QA Engineers | A test suite whose pass signal is trustworthy and reflects real system behaviour | TIR-01–05 |

### 2.8 Delivery Phases

All requirements are classified by delivery phase. Phase 1 is a prerequisite for Phase 2.

| Phase | Name | Goal | Completion Criteria |
| :--- | :--- | :--- | :--- |
| **Ph-1** | **Stable** | Backend build gate is trustworthy; zero test failures | `./mvnw test -P unit-tests` reports 0 failures, 0 errors |
| **Ph-2** | **Production Ready** | System is deployable, observable, secure, and end-user accessible | All Ph-2 requirements met; staging validation passed; frontend functional |

---

## 3. Specific Requirements

### Priority Classification

| Level | Definition |
| :--- | :--- |
| **High** | Must-have; system is non-functional or unsafe without it |
| **Medium** | Should-have; significant business or operational value but not a blocking dependency |
| **Low** | Could-have; desirable and deferrable without major impact |

---

### 3.1 External Interface Requirements

#### 3.1.1 User Interfaces

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| UI-01 | The primary user interface shall be a React 19 Single Page Application (SPA) | High | Ph-2 | Inspection |
| UI-02 | The system shall expose a Swagger / OpenAPI UI at `/swagger-ui.html` for API exploration | Medium | Ph-1 | Test |
| UI-03 | The OpenAPI specification shall be available at `/v3/api-docs` in JSON format | Medium | Ph-1 | Test |
| UI-04 | All API responses shall use JSON format with a consistent error response structure | High | Ph-1 | Test |

#### 3.1.2 Hardware Interfaces

This system has no direct hardware interfaces. It runs as a containerised application on standard server hardware or cloud infrastructure.

#### 3.1.3 Software Interfaces

| ID | External System | Interface Type | Protocol | Data Format | Phase |
| :--- | :--- | :--- | :--- | :--- | :--- |
| SI-01 | MySQL 8.2 | JDBC via HikariCP | TCP / 3306 | SQL | Ph-1 |
| SI-02 | Redis 7 | Lettuce client | TCP / 6379 | RESP | Ph-1 |
| SI-03 | Elasticsearch 8.17 | Spring Data Elasticsearch REST client | HTTP(S) / 9200 | JSON | Ph-2 |
| SI-04 | Razorpay Payment Gateway | REST SDK | HTTPS | JSON | Ph-2 |
| SI-05 | Prometheus | HTTP scrape endpoint (`/actuator/prometheus`) | HTTP / 8080 | OpenMetrics | Ph-2 |
| SI-06 | Logstash | TCP log shipper | TCP / 5000 | JSON | Ph-2 |

#### 3.1.4 Communication Interfaces

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| CI-01 | The system shall communicate over HTTP / HTTPS using REST conventions | High | Ph-1 | Test |
| CI-02 | The system shall enforce HTTPS (TLS 1.2+) in the production profile | High | Ph-2 | Inspection |
| CI-03 | The system shall support CORS with configurable allowed origins (default: `buildnest.com`) | High | Ph-1 | Test |
| CI-04 | The system shall expose health endpoints at `/actuator/health` for Kubernetes liveness and readiness probes | High | Ph-2 | Test |
| CI-05 | The system shall expose Prometheus metrics at `/actuator/prometheus` | Medium | Ph-2 | Test |

---

### 3.2 Functional Requirements

#### 3.2.1 Authentication and Identity Management (FG-01)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-AUTH-01 | The system shall allow new users to register with username, email, and password | High | Ph-1 | Test |
| FR-AUTH-02 | The system shall authenticate users via username / password and return a JWT access token and refresh token | High | Ph-1 | Test |
| FR-AUTH-03 | JWT access tokens shall expire after a configurable period (default: 15 minutes / 900,000 ms) | High | Ph-1 | Test |
| FR-AUTH-04 | JWT refresh tokens shall expire after a configurable period (default: 30 days / 2,592,000,000 ms) | High | Ph-1 | Test |
| FR-AUTH-05 | The system shall require a minimum 512-bit JWT secret key and fail fast on startup if absent | High | Ph-1 | Test |
| FR-AUTH-06 | The system shall support token refresh to obtain new access tokens without re-authentication | High | Ph-1 | Test |
| FR-AUTH-07 | The system shall support user logout by invalidating the associated refresh token | High | Ph-1 | Test |
| FR-AUTH-08 | The system shall support password reset via email with tokens expiring in 1 hour, configurable (OWASP ASVS 2.5.6 secure recovery mechanism; see #339 for a proposal to tighten this to align with OWASP's tighter 15–30 min practical guidance) | Medium | Ph-2 | Test |
| FR-AUTH-09 | The system shall enforce role-based access control (RBAC) with roles: `USER` and `ADMIN` | High | Ph-1 | Test |
| FR-AUTH-10 | The system shall hash passwords using BCrypt with a minimum of 10 rounds | High | Ph-1 | Inspection |
| FR-AUTH-11 | The system shall support OAuth2 client integration (Google, GitHub) | Medium | Ph-2 | Test |

#### 3.2.2 Product Catalogue Management (FG-02)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-PROD-01 | The system shall allow users to retrieve a paginated list of products | High | Ph-1 | Test |
| FR-PROD-02 | The system shall allow users to retrieve product details by product ID | High | Ph-1 | Test |
| FR-PROD-03 | The system shall support product categorisation | Medium | Ph-1 | Test |
| FR-PROD-04 | The system shall allow admins to create, update, and delete products | High | Ph-1 | Test |
| FR-PROD-05 | The system shall provide versioned product APIs (v1 deprecated, v2 current) with sunset headers on v1 responses | Medium | Ph-1 | Inspection |
| FR-PROD-06 | The system shall cache product data in Redis with a configurable TTL (default: 5 minutes) | Medium | Ph-1 | Test |
| FR-PROD-07 | The system shall cache category data in Redis with a configurable TTL (default: 1 hour) | Low | Ph-1 | Test |
| FR-PROD-08 | The system shall support product variants (size, colour) with independent per-variant inventory tracking and cart items pinned to a specific variant | High | Ph-1 | Test |
| FR-PROD-09 | The system shall allow admins to upload, reorder, and delete multiple images per product, with the first/primary image kept in sync with the product's legacy single-image field | Medium | Ph-1 | Test |

#### 3.2.3 Shopping Cart Operations (FG-03)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-CART-01 | The system shall allow authenticated users to add items to their cart | High | Ph-1 | Test |
| FR-CART-02 | The system shall allow users to retrieve their cart contents | High | Ph-1 | Test |
| FR-CART-03 | The system shall allow users to remove individual items from their cart | High | Ph-1 | Test |
| FR-CART-04 | The system shall allow users to clear their entire cart | Medium | Ph-1 | Test |
| FR-CART-05 | The system shall calculate and return the cart total price | High | Ph-1 | Test |
| FR-CART-06 | Each user shall have exactly one cart (one-to-one relationship) | High | Ph-1 | Inspection |

#### 3.2.4 Checkout and Order Processing (FG-04)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-CHK-01 | The system shall validate cart contents before checkout | High | Ph-1 | Test |
| FR-CHK-02 | The system shall calculate the total amount for checkout | High | Ph-1 | Test |
| FR-CHK-03 | The system shall process checkout without payment | High | Ph-1 | Test |
| FR-CHK-04 | The system shall process checkout with Razorpay payment | High | Ph-2 | Test |
| FR-CHK-05 | The system shall create an Order entity with associated OrderItems upon successful checkout | High | Ph-1 | Test |
| FR-CHK-06 | The system shall deduct inventory stock upon successful order placement | High | Ph-1 | Test |
| FR-CHK-07 | The system shall allow users to view their order history | Medium | Ph-1 | Test |
| FR-CHK-08 | The system shall allow admins to view and manage all orders | Medium | Ph-1 | Test |

#### 3.2.5 Payment Processing (FG-05)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-PAY-01 | The system shall integrate with Razorpay for payment order creation | High | Ph-2 | Test |
| FR-PAY-02 | The system shall verify Razorpay payment signatures to prevent fraudulent confirmation | High | Ph-2 | Test |
| FR-PAY-03 | The system shall record payment transactions with status tracking | High | Ph-2 | Test |
| FR-PAY-04 | The system shall handle Razorpay webhook events for asynchronous payment updates | Medium | Ph-2 | Test |
| FR-PAY-05 | Razorpay API credentials shall be externalised via environment variables and never hardcoded | High | Ph-2 | Inspection |

#### 3.2.6 Inventory Management (FG-06)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-INV-01 | The system shall allow users to check product inventory status | High | Ph-1 | Test |
| FR-INV-02 | The system shall allow users to check product availability | High | Ph-1 | Test |
| FR-INV-03 | The system shall allow admins to add stock to a product | High | Ph-1 | Test |
| FR-INV-04 | The system shall allow admins to update stock quantities | High | Ph-1 | Test |
| FR-INV-05 | The system shall track inventory status: `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`, `DISCONTINUED` | Medium | Ph-1 | Test |
| FR-INV-06 | The system shall emit `InventoryThresholdBreachEvent` when stock falls below configurable thresholds | Medium | Ph-2 | Test |
| FR-INV-07 | The system shall provide inventory analytics and reports for admins | Medium | Ph-2 | Test |

#### 3.2.7 Reviews and Wishlists (FG-07)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-REV-01 | The system shall allow authenticated users to submit product reviews with star ratings | Medium | Ph-1 | Test |
| FR-REV-02 | The system shall allow any user to view product reviews | Medium | Ph-1 | Test |
| FR-REV-03 | The system shall allow users to update and delete their own reviews | Medium | Ph-1 | Test |
| FR-WISH-01 | The system shall allow authenticated users to add products to their wishlist | Low | Ph-1 | Test |
| FR-WISH-02 | The system shall allow users to view and manage their wishlist | Low | Ph-1 | Test |

#### 3.2.8 Admin Operations (FG-08)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-ADM-01 | The system shall provide sales analytics dashboards for admins | Medium | Ph-2 | Test |
| FR-ADM-02 | The system shall provide inventory analytics and reports for admins | Medium | Ph-2 | Test |
| FR-ADM-03 | The system shall allow admins to manage user accounts (view, update, deactivate) | Medium | Ph-2 | Test |
| FR-ADM-04 | The system shall maintain a tamper-evident audit log of all administrative actions | High | Ph-1 | Test |
| FR-ADM-05 | The system shall provide admin-accessible reporting endpoints | Medium | Ph-2 | Test |
| FR-ADM-06 | The system shall allow admins to configure inventory alert thresholds | Medium | Ph-2 | Test |
| FR-ADM-07 | The system shall allow admins to manage webhook subscriptions | Low | Ph-2 | Test |
| FR-ADM-08 | Access to all `/api/admin/**` endpoints shall require the `ADMIN` role | High | Ph-1 | Test |
| FR-ADM-09 | The system shall allow admins to create, update, and delete product categories, including hierarchical parent/child relationships, and shall prevent deletion of a category that still has products or subcategories referencing it | Medium | Ph-1 | Test |

#### 3.2.9 Monitoring and Observability (FG-09)

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-MON-01 | The system shall expose health check endpoints at `/actuator/health` | High | Ph-1 | Test |
| FR-MON-02 | Health checks shall include a MySQL connectivity indicator | High | Ph-2 | Test |
| FR-MON-03 | Health checks shall include a Redis connectivity indicator | High | Ph-2 | Test |
| FR-MON-04 | Health checks shall include circuit breaker state indicators | Medium | Ph-2 | Test |
| FR-MON-05 | The system shall expose Prometheus metrics at `/actuator/prometheus` | Medium | Ph-2 | Test |
| FR-MON-06 | The system shall support Kubernetes liveness and readiness probes via the health endpoint | High | Ph-2 | Test |
| FR-MON-07 | The system shall integrate with Elasticsearch for event indexing and alerting | Low | Ph-2 | Test |
| FR-MON-08 | 13 Prometheus alert rules shall be configured covering pod health, latency, error rate, CPU/memory, database pool, Redis, cache hit rate, rate limiting, and authentication failures | Medium | Ph-2 | Inspection |

#### 3.2.10 Frontend Application (FG-10)

##### 3.2.10.1 Core Architecture

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-FE-01 | The frontend shall be a Single Page Application (SPA) developed using React 19 | High | Ph-2 | Inspection |
| FR-FE-02 | The frontend shall interact with the backend API using Axios or Fetch API with interceptors for JWT token injection and renewal | High | Ph-2 | Inspection |
| FR-FE-03 | The frontend shall implement client-side routing using React Router v6+ | High | Ph-2 | Test |
| FR-FE-04 | The frontend shall provide a responsive design supporting Desktop (≥ 1024 px), Tablet (≥ 768 px), and Mobile (< 768 px) | High | Ph-2 | Demonstration |
| FR-FE-05 | The frontend shall manage global state (user session, cart) using React Context API or Redux Toolkit | High | Ph-2 | Inspection |
| FR-FE-06 | The frontend shall implement protected routes that redirect unauthenticated users to the login page | High | Ph-2 | Test |
| FR-FE-07 | The frontend shall display loading indicators during asynchronous API calls | Medium | Ph-2 | Demonstration |
| FR-FE-08 | The frontend shall display user-friendly toast notifications for success and error events | Medium | Ph-2 | Demonstration |
| FR-FE-09 | The frontend shall validate forms client-side using React Hook Form or Formik with Yup schemas | Medium | Ph-2 | Test |
| FR-FE-10 | The frontend shall automatically refresh the JWT access token using the stored refresh token upon receiving a 401 response | High | Ph-2 | Test |

##### 3.2.10.2 Pages and Components

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-FE-11 | Home Page: featured products, category navigation, search bar, promotional content | High | Ph-2 | Demonstration |
| FR-FE-12 | Product Listing Page: pagination, sorting (price, name, rating), filtering by category and price range | High | Ph-2 | Test |
| FR-FE-13 | Product Detail Page: images, description, price, stock status, reviews, "Add to Cart" | High | Ph-2 | Demonstration |
| FR-FE-14 | Shopping Cart Page: items with quantities, subtotals, total, "Proceed to Checkout" CTA | High | Ph-2 | Test |
| FR-FE-15 | Checkout Page: shipping address, order summary, Razorpay payment modal integration | High | Ph-2 | Test |
| FR-FE-16 | Login Page: username / password fields, form validation, error display, registration link | High | Ph-2 | Test |
| FR-FE-17 | Registration Page: username, email, password with strength indicator, confirm password | High | Ph-2 | Test |
| FR-FE-18 | User Profile Page: display and edit user details, addresses, password change | Medium | Ph-2 | Test |
| FR-FE-19 | Order History Page: list past orders with status, date, total, and link to order detail | Medium | Ph-2 | Test |
| FR-FE-20 | Wishlist Page: saved products with options to move to cart or remove | Low | Ph-2 | Test |
| FR-FE-21 | Search Results Page: products matching query with highlighted terms and sorting | Medium | Ph-2 | Test |
| FR-FE-22 | Admin Dashboard: summary cards for total sales, orders, users, and low-stock items | Medium | Ph-2 | Demonstration |
| FR-FE-23 | Admin Product Management: table with CRUD actions, image upload, category assignment | Medium | Ph-2 | Test |
| FR-FE-24 | Admin Inventory Page: stock levels with colour-coded status and threshold configuration | Medium | Ph-2 | Test |
| FR-FE-25 | Admin Order Management: all orders with status filters and order status updates | Medium | Ph-2 | Test |

##### 3.2.10.3 Shared Components

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-FE-26 | Navbar: logo, search bar, cart badge, user menu — present on all pages | High | Ph-2 | Demonstration |
| FR-FE-27 | Footer: links (About, Contact, Terms, Privacy) and copyright | Low | Ph-2 | Demonstration |
| FR-FE-28 | ProductCard: product image, name, price, rating, "Add to Cart" | High | Ph-2 | Demonstration |
| FR-FE-29 | Breadcrumb: navigation hierarchy on product and category pages | Low | Ph-2 | Demonstration |
| FR-FE-30 | ErrorBoundary: catches React rendering errors and displays a fallback UI | Medium | Ph-2 | Test |

---

### 3.3 Usability Requirements

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| UR-01 | All API responses shall follow a consistent JSON structure with standard HTTP status codes | High | Ph-1 | Test |
| UR-02 | Error responses shall include a machine-readable error code and a human-readable message | High | Ph-1 | Test |
| UR-03 | The OpenAPI / Swagger documentation shall be auto-generated and reflect the current API state | Medium | Ph-1 | Inspection |
| UR-04 | API versioning (v1 / v2) shall ensure backward compatibility for existing API consumers | Medium | Ph-1 | Test |
| UR-05 | Deprecated API endpoints (v1) shall include `Sunset` and `Deprecation` response headers | Low | Ph-1 | Inspection |
| UR-FE-01 | The frontend shall comply with WCAG 2.1 AA accessibility standards (colour contrast, keyboard navigation) | High | Ph-2 | Inspection |
| UR-FE-02 | The frontend shall achieve a Largest Contentful Paint (LCP) of < 2.5 seconds | High | Ph-2 | Analysis |
| UR-FE-03 | The frontend shall provide visual feedback (hover / focus states) for all interactive elements | Medium | Ph-2 | Demonstration |

---

### 3.4 Performance Requirements

| ID | Requirement | Priority | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- | :--- |
| PR-01 | API response time at the 95th percentile shall be less than 500 ms under normal load | High | Ph-2 | P95 < 500 ms | Analysis (Gatling / JMeter) |
| PR-02 | The system shall sustain 1,000 concurrent users as validated by load testing | High | Ph-2 | 1,000 concurrent | Analysis |
| PR-03 | Throughput shall exceed 10,000 requests per minute under load test conditions | High | Ph-2 | > 10,000 req/min | Analysis |
| PR-04 | Error rate shall remain below 0.1% under load testing conditions | High | Ph-2 | < 0.1% | Analysis |
| PR-05 | The database connection pool shall support a maximum of 20 connections (30 in production) with 10 minimum idle | Medium | Ph-1 | Configurable | Inspection |
| PR-06 | Connection timeout shall not exceed 30 seconds | Medium | Ph-1 | 30,000 ms | Inspection |
| PR-07 | Redis cache TTL for products shall be 5 minutes with configurable override | Medium | Ph-1 | 300,000 ms | Test |
| PR-08 | Docker image build time shall not exceed 2 minutes with layer caching active | Low | Ph-2 | < 2 min | Analysis |

---

### 3.5 Logical Database Requirements

#### 3.5.1 Core Entities

| Entity | Description | Key Fields |
| :--- | :--- | :--- |
| `User` | Registered platform user | id, username, email, password_hash, roles, is_active |
| `Role` | Authorization role (`USER`, `ADMIN`) | id, name, permissions |
| `Permission` | Granular permission unit | id, name |
| `Product` | Sellable item in catalogue | id, name, description, price, discount_price, category, sku |
| `Category` | Product classification | id, name, description, is_active |
| `Cart` | User's shopping cart | id, user_id |
| `CartItem` | Item in a cart | id, cart_id, product_id, quantity, price |
| `Order` | Confirmed purchase order | id, user_id, order_number, total_amount, status |
| `OrderItem` | Line item in an order | id, order_id, product_id, quantity, unit_price, total_price |
| `Payment` | Payment transaction record | id, order_id, amount, method, status, transaction_id |
| `Inventory` | Stock tracking per product | id, product_id, quantity, status |
| `ProductReview` | User review with rating | id, user_id, product_id, rating, comment |
| `Wishlist` | User's saved products | id, user_id, product_ids |
| `RefreshToken` | JWT refresh token store | id, user_id, token, expiry_date |
| `PasswordResetToken` | Password reset token | id, user_id, token, expiry_date |
| `AuditLog` | System audit trail | id, action, entity_type, entity_id, user_id, old_value, new_value, ip_address, user_agent, created_at |
| `Address` | User shipping / billing address | id, user_id, street, city, state, postal_code, country, is_default |
| `WebhookSubscription` | Webhook endpoint registration | id, event_type, target_url, secret, is_active, failure_count |
| `InventoryThresholdBreachEvent` | Inventory breach event record | id, inventory_id, threshold, actual_quantity, created_at |

#### 3.5.2 Database Configuration Requirements

| Parameter | Required Value | Notes |
| :--- | :--- | :--- |
| Database Engine | MySQL 8.2 (InnoDB) | InnoDB required for ACID transactions |
| Schema Management | Liquibase | All DDL changes via changesets |
| DDL Auto Strategy | `validate` | No automatic schema modification in any environment |
| Connection Pool | HikariCP | Max 20 (dev), 30 (production); min idle 10 (dev), 15 (production) |
| Test Database | H2 in-memory | Used exclusively for test profile; never for production |

#### 3.5.3 Index Requirements

All of the following indexes shall be present in the production schema:

| Index | Table | Column(s) |
| :--- | :--- | :--- |
| `idx_user_email` | `users` | `email` |
| `idx_user_username` | `users` | `username` |
| `idx_product_category` | `product` | `category` |
| `idx_inventory_product_id` | `inventory` | `product_id` |
| `idx_inventory_status` | `inventory` | `status` |
| `idx_order_user_id` | `orders` | `user_id` |
| `idx_order_status` | `orders` | `status` |
| `idx_order_item_order_id` | `order_item` | `order_id` |
| `idx_order_item_product_id` | `order_item` | `product_id` |
| `idx_cart_user_id` | `cart` | `user_id` |
| `idx_cart_item_cart_id` | `cart_item` | `cart_id` |
| `idx_payment_order_id` | `payment` | `order_id` |
| `idx_payment_status` | `payment` | `status` |
| `idx_audit_log_user_id` | `audit_log` | `user_id` |
| `idx_audit_log_timestamp` | `audit_log` | `created_at` |
| `idx_refresh_token_user_id` | `refresh_token` | `user_id` |

---

### 3.6 Design Constraints

| ID | Constraint | Rationale |
| :--- | :--- | :--- |
| DC-01 | Layered monolithic architecture: Controller → Service → Repository → Model | Simplicity; future decomposition possible without architectural re-work |
| DC-02 | RESTful API design following HTTP method and status code conventions | Industry standard; tooling and client compatibility |
| DC-03 | All configuration externalised via environment variables; no secrets in source | 12-Factor App compliance; secure credential management |
| DC-04 | Stateless JWT authentication; no server-side session storage | Horizontal scalability |
| DC-05 | Graceful shutdown with 30-second drain period | Zero-downtime deployments on Kubernetes |
| DC-06 | Multi-stage Docker builds | Optimised image size and reproducible builds |
| DC-07 | Repository access only from service layer; no direct repository calls from controllers | Layering integrity; testability |
| DC-08 | All JPA relationships shall declare explicit fetch strategy (`FetchType.LAZY` or `FetchType.EAGER`) | Prevents implicit N+1 behaviour; makes data access intent explicit |

---

### 3.7 Standards Compliance

| Standard | Applicability | Required Status |
| :--- | :--- | :--- |
| ISO/IEC/IEEE 29148:2018 | This SRS document structure and content | Conformant |
| ISO/IEC 25010:2011 | Software quality model for NFRs | Conformant |
| OWASP Top 10 (2021) | Web application security | Fully addressed (Sections 3.8.3 and 3.9) |
| OWASP ASVS 4.0 Level 2 | Application security verification | Addressed in security requirements |
| PCI-DSS v4.0 | Payment data handling | Partial — Razorpay handles card data; platform must not store raw card data |
| GDPR 2018 | Personal data protection | Partial — data handling and retention policies required |
| 12-Factor App | Configuration, portability | Conformant via environment variable strategy |

---

### 3.8 Software System Attributes

#### 3.8.1 Reliability

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| REL-01 | The system shall achieve 99.9% uptime in production | Ph-2 | 99.9% | Analysis |
| REL-02 | The system shall implement a circuit breaker for Redis with failure threshold of 70% | Ph-1 | Configurable | Inspection |
| REL-03 | The system shall implement a circuit breaker for the database with failure threshold of 50% | Ph-1 | Configurable | Inspection |
| REL-04 | Recovery Time Objective (RTO) shall not exceed 15 minutes | Ph-2 | 15 min | Test |
| REL-05 | Recovery Point Objective (RPO) shall not exceed 5 minutes | Ph-2 | 5 min | Analysis |

#### 3.8.2 Availability

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| AVL-01 | Kubernetes liveness and readiness probes shall be configured and enabled | Ph-2 | Always | Inspection |
| AVL-02 | Health checks shall cover MySQL, Redis, and circuit breaker states | Ph-2 | Composite | Test |
| AVL-03 | Graceful shutdown shall allow 30 seconds for in-flight requests to complete | Ph-2 | 30 sec | Test |
| AVL-04 | The connection pool shall auto-recover from transient database failures within the timeout period | Ph-1 | Auto | Test |

#### 3.8.3 Security

| ID | Requirement | Phase | Priority | Verification |
| :--- | :--- | :--- | :--- | :--- |
| SEC-01 | All passwords shall be hashed using BCrypt (minimum 10 rounds) | Ph-1 | High | Inspection |
| SEC-02 | The JWT secret key shall be minimum 512 bits, externalised via `JWT_SECRET` environment variable, and absent by default | Ph-1 | High | Inspection |
| SEC-03 | HTTPS / TLS shall be enforced in the production profile; startup shall fail if SSL is not configured in production | Ph-2 | High | Test |
| SEC-04 | CSRF protection shall be configured appropriately for the SPA client model | Ph-2 | High | Inspection |
| SEC-05 | CORS shall restrict `allowedOrigins` to explicitly configured domains only | Ph-1 | High | Inspection |
| SEC-06 | SQL injection shall be prevented exclusively via JPA parameterised queries; no string-concatenated queries shall exist | Ph-1 | High | Inspection |
| SEC-07 | Login attempts shall be rate-limited to 3 requests per 5 minutes per source | Ph-1 | High | Test |
| SEC-08 | Password reset requests shall be rate-limited to 3 requests per 1 hour per source | Ph-1 | High | Test |
| SEC-09 | Admin API endpoints shall be rate-limited to 50 requests per minute | Ph-1 | Medium | Test |
| SEC-10 | User API endpoints shall be rate-limited to 500 requests per minute | Ph-1 | Medium | Test |
| SEC-11 | Product search endpoints shall be rate-limited to 60 requests per minute | Ph-1 | Medium | Test |
| SEC-12 | JWT secret rotation shall be performed every 90 days following documented procedures | Ph-2 | Medium | Inspection |
| SEC-13 | Database password rotation shall be performed every 180 days following documented procedures | Ph-2 | Medium | Inspection |
| SEC-14 | The Content-Security-Policy response header shall not include `unsafe-inline`; inline script execution shall be prevented via nonce or hash strategy | Ph-2 | Medium | Inspection |

> **Note on SEC-14**: The current implementation includes `unsafe-inline` in the CSP directive (identified in Baseline Assessment SEC-01). This is accepted as a known gap for Ph-1 and shall be resolved before Ph-2 sign-off.

#### 3.8.4 Maintainability

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| MNT-01 | 100% Javadoc coverage shall be enforced via the Maven Javadoc Plugin; the build shall fail on violations | Ph-1 | 100% | Build |
| MNT-02 | Test coverage shall meet or exceed 70% of production code lines as measured by JaCoCo | Ph-2 | ≥ 70% | Build |
| MNT-03 | All unit tests shall pass with 0 failures and 0 errors when executed against the H2 test profile | Ph-1 | 0 failures | Build (`./mvnw test -P unit-tests`) |
| MNT-04 | All database schema changes shall be implemented as Liquibase changesets | Ph-1 | Always | Inspection |
| MNT-05 | All production logging shall use structured JSON format via SLF4J / Logback with the Logstash encoder | Ph-1 | Always | Inspection |
| MNT-06 | No `System.out` or `printStackTrace` calls shall exist in production source code | Ph-1 | Zero | Inspection |

> **Note on MNT-02**: The current JaCoCo gate is set at 40% (Baseline Assessment F-08). The target of 70% reflects the industry standard per ISO/IEC 25010 testability attribute. The gate shall be raised incrementally: 50% at Ph-1 completion, 70% at Ph-2 sign-off.
>
> **Note on MNT-03**: As of 2026-06-19, 14 failures / errors are present (Baseline Assessment Section 5). Resolution of all failures is the primary acceptance criterion for Phase 1.

#### 3.8.5 Portability

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| PRT-01 | The system shall be containerised via Docker with multi-stage builds | Ph-2 | Docker | Inspection |
| PRT-02 | The system shall provide Kubernetes deployment manifests (Deployment, Service, ConfigMap, Secrets, Ingress) | Ph-2 | K8s | Inspection |
| PRT-03 | The system shall provide Terraform IaC for AWS deployment | Ph-2 | AWS | Inspection |
| PRT-04 | All configuration shall be environment-variable-driven per 12-Factor App methodology | Ph-1 | 12-Factor | Inspection |

#### 3.8.6 Scalability

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| SCL-01 | Stateless JWT authentication shall enable horizontal pod scaling without session affinity | Ph-1 | Horizontal | Inspection |
| SCL-02 | HikariCP connection pool sizing shall be configurable per deployment environment | Ph-1 | Configurable | Inspection |
| SCL-03 | Redis-backed rate limiting shall be shared across all application instances in a multi-pod deployment | Ph-2 | Distributed | Test |
| SCL-04 | The system shall sustain at least 1,000 concurrent users as validated by Gatling simulations | Ph-2 | 1,000 users | Analysis |

#### 3.8.7 Safety

This system does not control safety-critical hardware or processes. IEC 61508 safety requirements are not applicable.

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| SAF-01 | System failure shall not result in an unauthorised financial transaction | Ph-2 | Zero tolerance | Test |
| SAF-02 | Payment processing shall fail-safe: no charge shall be recorded without an associated order confirmation | Ph-2 | Fail-safe | Test |
| SAF-03 | Inventory data integrity shall be maintained via ACID database transactions under concurrent order placement | Ph-1 | ACID | Test |

---

### 3.9 Test Integrity Requirements

Test integrity requirements define the properties that the test suite itself must satisfy. A test suite that does not satisfy these requirements cannot be used as a reliable quality gate.

*This section was introduced in SRS v4.0 based on findings in the Baseline Assessment Report (REF-10), which identified 14 test failures with four distinct root causes.*

| ID | Requirement | Phase | Priority | Verification |
| :--- | :--- | :--- | :--- | :--- |
| TIR-01 | E2E tests that require a running application server (i.e., tests issuing HTTP requests to `localhost:{port}`) shall be excluded from the unit-tests Maven profile and shall only execute under the `e2e-tests` profile | Ph-1 | High | Build (unit profile must report 0 failures attributable to missing server) |
| TIR-02 | Every unit test that instantiates a service class under test via `@InjectMocks` shall declare a `@Mock` for every dependency injected into that service; missing mocks shall cause the test to fail at setup rather than at assertion | Ph-1 | High | Test (`AuthServiceImplTest` — all `@Mock` fields populated) |
| TIR-03 | Security test assertions for unauthenticated access to secured endpoints shall assert the correct RFC 9110 status code: `403 Forbidden` for authenticated-but-unauthorised requests; `401 Unauthorized` for entirely unauthenticated requests | Ph-1 | Medium | Inspection (verify `AuthenticationAuthorizationSecurityTest`) |
| TIR-04 | Security tests that exercise input validation (XSS payloads, unsupported media types) against secured endpoints shall accept both the validation error status code and the authentication status code, as filter chain order determines which fires first | Ph-1 | Medium | Inspection (verify `InputValidationSecurityTest`) |
| TIR-05 | The mutation test threshold shall be maintained at ≥ 75% as configured via PIT Maven Plugin | Ph-2 | Medium | Build (`-P ci`) |

---

### 3.10 Licensing Requirements

| ID | Component | License | Obligation |
| :--- | :--- | :--- | :--- |
| LIC-01 | Spring Boot / Spring Framework | Apache 2.0 | Include license notice in distribution |
| LIC-02 | React | MIT | Include copyright notice |
| LIC-03 | MySQL Connector/J | GPL v2 | Used as a network service; client library is GPL |
| LIC-04 | Redis | BSD 3-Clause | Include copyright notice |
| LIC-05 | Elasticsearch | Elastic License 2.0 / SSPL | Review usage restrictions for commercial deployment |
| LIC-06 | Lombok | MIT | Include copyright notice |
| LIC-07 | JJWT | Apache 2.0 | Include license notice |
| LIC-08 | Bucket4j | Apache 2.0 | Include license notice |
| LIC-09 | Resilience4j | Apache 2.0 | Include license notice |
| LIC-10 | Razorpay Java SDK | MIT | Include copyright notice |

---

## 4. Verification

### 4.1 Verification Methods

| Method | Description | Applicable Requirements |
| :--- | :--- | :--- |
| **Test** | Automated unit, integration, and E2E tests; CI pipeline gate | All `FR-*`, `TIR-*`, `PR-*`, `REL-*`, `SAF-*` |
| **Inspection** | Code review, configuration audit, schema review | `SEC-*`, `DC-*`, `MNT-*`, `CON-*`, `LIC-*` |
| **Analysis** | Performance load testing (Gatling, JMeter); coverage reports | `PR-01–04`, `SCL-04`, `UR-FE-02`, `MNT-02` |
| **Demonstration** | Live system walkthrough; browser-based UI verification | `UI-*`, `FR-FE-*` (Demonstration), `AVL-*` |
| **Build** | Maven build outcome as pass/fail gate | `MNT-01`, `MNT-03`, `TIR-01–02`, `TIR-05` |

### 4.2 Traceability Matrix

| Requirement Group | Count | Phase | Priority Distribution | Verification |
| :--- | :--- | :--- | :--- | :--- |
| Authentication (FR-AUTH-01–11) | 11 | Ph-1 / Ph-2 | 8 High, 2 Medium, 1 Medium | Test, Inspection |
| Product Catalogue (FR-PROD-01–09) | 9 | Ph-1 | 3 High, 4 Medium, 2 Low | Test, Inspection |
| Shopping Cart (FR-CART-01–06) | 6 | Ph-1 | 4 High, 1 Medium, 1 High | Test, Inspection |
| Checkout (FR-CHK-01–08) | 8 | Ph-1 / Ph-2 | 6 High, 2 Medium | Test |
| Payment (FR-PAY-01–05) | 5 | Ph-2 | 3 High, 1 Medium, 1 High | Test, Inspection |
| Inventory (FR-INV-01–07) | 7 | Ph-1 / Ph-2 | 3 High, 4 Medium | Test |
| Reviews / Wishlists (FR-REV, FR-WISH) | 5 | Ph-1 | 0 High, 3 Medium, 2 Low | Test |
| Admin Operations (FR-ADM-01–09) | 9 | Ph-1 / Ph-2 | 2 High, 6 Medium, 1 Low | Test |
| Monitoring (FR-MON-01–08) | 8 | Ph-1 / Ph-2 | 3 High, 3 Medium, 2 Low | Test, Inspection |
| Frontend (FR-FE-01–30) | 30 | Ph-2 | 15 High, 10 Medium, 5 Low | Test, Inspection, Demonstration |
| **Total Functional** | **95** | | | |
| Usability (UR-01–05, UR-FE-01–03) | 8 | Ph-1 / Ph-2 | Mixed | Test, Inspection |
| Performance (PR-01–08) | 8 | Ph-1 / Ph-2 | 4 High, 3 Medium, 1 Low | Analysis, Inspection |
| Reliability (REL-01–05) | 5 | Ph-1 / Ph-2 | Mixed | Analysis, Inspection |
| Availability (AVL-01–04) | 4 | Ph-2 | High | Test |
| Security (SEC-01–14) | 14 | Ph-1 / Ph-2 | 9 High, 5 Medium | Inspection, Test |
| Maintainability (MNT-01–06) | 6 | Ph-1 / Ph-2 | Mixed | Build, Inspection |
| Portability (PRT-01–04) | 4 | Ph-1 / Ph-2 | Mixed | Inspection |
| Scalability (SCL-01–04) | 4 | Ph-1 / Ph-2 | Mixed | Analysis, Inspection |
| Safety (SAF-01–03) | 3 | Ph-1 / Ph-2 | High | Test |
| Test Integrity (TIR-01–05) | 5 | Ph-1 / Ph-2 | 2 High, 3 Medium | Build, Inspection |
| **Total Non-Functional** | **61** | | | |
| **Grand Total** | **156** | | | |

### 4.3 Phase 1 Acceptance Criteria

Phase 1 (Stable) is complete when all of the following conditions are simultaneously satisfied:

| Criterion | Measurement | Acceptance |
| :--- | :--- | :--- |
| Unit test pass rate | `./mvnw test -P unit-tests` surefire output | 0 failures, 0 errors |
| E2E test isolation | Unit test run excludes server-dependent tests | No `ProductApiTest` or `OrderApiTest` failures in unit profile |
| Compilation | `./mvnw clean compile` | 0 errors |
| Javadoc coverage | Maven Javadoc Plugin build | Build succeeds (100% coverage) |
| JaCoCo gate | `./mvnw verify -P ci` | ≥ 50% line coverage |
| Critical code paths covered | `AuthServiceImplTest` register methods | 0 errors (RoleRepository mock present) |
| Security test assertions | HTTP status assertions correct per RFC 9110 | `AuthenticationAuthorizationSecurityTest` and `InputValidationSecurityTest` pass |

### 4.4 Phase 2 Acceptance Criteria

Phase 2 (Production Ready) is complete when Phase 1 criteria are met and all of the following additional conditions are satisfied:

| Criterion | Measurement | Acceptance |
| :--- | :--- | :--- |
| JaCoCo coverage | `./mvnw verify -P ci` | ≥ 70% line coverage |
| Mutation score | `./mvnw verify -P ci` (PIT) | ≥ 75% mutation score |
| Frontend SPA | Build and browser test | All High-priority FR-FE requirements demonstrated |
| Kubernetes secrets | `kubectl describe secret buildnest-secrets` | All required keys present |
| SSL certificate | Browser or `curl -v` HTTPS check | Valid TLS certificate, no browser warning |
| OWASP dependency scan | `mvnw verify -Dowasp` (CI `security.yml`) | No CVE with CVSS score ≥ 7.0 unaddressed |
| CSP hardened | Browser DevTools / header inspection | No `unsafe-inline` in Content-Security-Policy |
| Load test | Gatling / JMeter report | P95 < 500 ms at 1,000 concurrent users |
| Prometheus alerts | `kubectl get prometheusrule` | 13 alert rules active |
| Disaster recovery | DR runbook execution in staging | RTO ≤ 15 min, RPO ≤ 5 min demonstrated |

### 4.5 Test Infrastructure

| Component | Tool | Purpose |
| :--- | :--- | :--- |
| Unit Testing | JUnit 5, Mockito | Service, repository, and controller unit tests |
| Integration Testing | Spring Boot Test, `@DataJpaTest` | Database and security integration |
| E2E Testing | Rest Assured, Selenium WebDriver 4.16 | End-to-end API and browser testing |
| Load Testing | Gatling 3.10.3, JMeter | Performance and concurrency validation |
| Mutation Testing | PIT (pitest) 1.16.1 | Test quality validation (≥ 75% threshold) |
| Coverage | JaCoCo 0.8.11 | Code coverage enforcement |
| Security Scanning | OWASP Dependency-Check 9.0.9 | CVE vulnerability scanning |
| Contract Testing | Pact (infrastructure present; tests pending — FUT-05) | Consumer-driven contract validation |
| Structured Logging | Logback + Logstash Encoder 7.4 | JSON log output |

---

## 5. Appendices

### Appendix A: API Endpoint Catalogue

#### A.1 Authentication Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/auth/register` | Public | User registration |
| POST | `/api/auth/login` | Public | User login (returns JWT) |
| POST | `/api/auth/refresh-token` | Public | Refresh access token |
| POST | `/api/auth/logout` | Authenticated | Invalidate refresh token |
| POST | `/api/auth/forgot-password` | Public | Request password reset |
| POST | `/api/auth/reset-password` | Public | Reset password with token |
| GET | `/api/auth/validate-reset-token` | Public | Validate reset token |

#### A.2 Product Endpoints — V1 (Deprecated)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/public/products` | Public | List all products (paginated) |
| GET | `/api/public/products/{id}` | Public | Get product by ID |
| GET | `/api/public/products/search` | Public | Search products by keyword |
| GET | `/api/public/products/category/{id}` | Public | Filter by category |

#### A.3 Product Endpoints — V2 (Current)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v2/public/products` | Public | List products (paginated, enhanced) |
| GET | `/api/v2/public/products/{id}` | Public | Get product detail |
| GET | `/api/v2/public/products/search` | Public | Search products |
| GET | `/api/v2/public/products/category/{id}` | Public | Filter by category |

#### A.4 Category Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/public/categories` | Public | List all categories |
| GET | `/api/public/categories/{id}` | Public | Get category by ID |

#### A.5 User Profile Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/user/profile` | USER | Get user profile |
| PUT | `/api/user/profile` | USER | Update user profile |
| PUT | `/api/user/change-password` | USER | Change password |

#### A.6 Cart Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/user/cart/add` | USER | Add item to cart |
| GET | `/api/user/cart/{userId}` | USER | Get cart contents |
| PUT | `/api/user/cart/update` | USER | Update item quantity |
| DELETE | `/api/user/cart/item/{cartItemId}` | USER | Remove cart item |
| DELETE | `/api/user/cart/clear/{userId}` | USER | Clear entire cart |
| GET | `/api/user/cart/total/{userId}` | USER | Get cart total |

#### A.7 Checkout Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/checkout/process/{cartId}` | USER | Process checkout (no payment) |
| POST | `/api/checkout/process-with-payment/{cartId}` | USER | Checkout with Razorpay |
| GET | `/api/checkout/validate/{cartId}` | USER | Validate cart for checkout |
| GET | `/api/checkout/calculate-total/{cartId}` | USER | Calculate checkout total |

#### A.8 Order Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/user/orders` | USER | Get order history |
| GET | `/api/user/orders/{orderId}` | USER | Get order by ID |

#### A.9 Wishlist Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/user/wishlist/add/{productId}` | USER | Add product to wishlist |
| DELETE | `/api/user/wishlist/remove/{productId}` | USER | Remove product from wishlist |
| GET | `/api/user/wishlist` | USER | Get wishlist contents |
| GET | `/api/user/wishlist/check/{productId}` | USER | Check if product in wishlist |
| GET | `/api/user/wishlist/count` | USER | Get wishlist item count |
| DELETE | `/api/user/wishlist/clear` | USER | Clear entire wishlist |

#### A.10 Product Review Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/user/reviews/product/{productId}` | USER | Submit a review |
| GET | `/api/public/reviews/product/{productId}` | Public | Get reviews for product |
| GET | `/api/public/reviews/product/{productId}/summary` | Public | Get rating summary |
| PUT | `/api/user/reviews/{reviewId}` | USER | Update own review |
| DELETE | `/api/user/reviews/{reviewId}` | USER | Delete own review |
| POST | `/api/user/reviews/{reviewId}/helpful` | USER | Mark review as helpful |

#### A.11 Inventory Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/inventory/product/{productId}` | USER | Get product inventory |
| GET | `/api/inventory/check-availability/{productId}` | USER | Check stock availability |
| POST | `/api/inventory/add-stock/{productId}` | ADMIN | Add stock |
| POST | `/api/inventory/update-stock/{productId}` | ADMIN | Update stock |

#### A.12 Admin Product Management

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/products` | ADMIN | List all products |
| POST | `/api/admin/products` | ADMIN | Create product |
| PUT | `/api/admin/products/{id}` | ADMIN | Update product |
| DELETE | `/api/admin/products/{id}` | ADMIN | Delete product |

#### A.13 Admin Order Management

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/orders` | ADMIN | List all orders |
| GET | `/api/admin/orders/{orderId}` | ADMIN | Get order details |
| PUT | `/api/admin/orders/{orderId}/status` | ADMIN | Update order status |
| DELETE | `/api/admin/orders/{orderId}` | ADMIN | Soft-delete order |

#### A.14 Admin User Management

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/users` | ADMIN | List all users |
| GET | `/api/admin/users/{userId}` | ADMIN | Get user details |
| PUT | `/api/admin/users/{userId}` | ADMIN | Update user |
| DELETE | `/api/admin/users/{userId}` | ADMIN | Soft-delete user |

#### A.15 Admin Inventory Management

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/inventory/low-stock` | ADMIN | Get low-stock products |
| GET | `/api/admin/inventory/out-of-stock` | ADMIN | Get out-of-stock products |
| POST | `/api/admin/inventory/add-stock/{productId}` | ADMIN | Add stock to product |
| GET | `/api/admin/inventory/available/{productId}` | ADMIN | Check available quantity |

#### A.16 Admin Analytics and Reports

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/analytics/dashboard` | ADMIN | Get dashboard metrics |
| GET | `/api/admin/analytics/sales` | ADMIN | Get sales analytics |
| GET | `/api/admin/analytics/inventory` | ADMIN | Get inventory analytics |
| GET | `/api/admin/reports/audit-logs` | ADMIN | Get audit log history |
| GET | `/api/admin/reports/errors` | ADMIN | Get error analytics |

#### A.17 Admin Webhook Management

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/admin/webhooks` | ADMIN | Create webhook subscription |
| GET | `/api/admin/webhooks` | ADMIN | List all webhooks |
| PUT | `/api/admin/webhooks/{id}/activate` | ADMIN | Activate webhook |
| PUT | `/api/admin/webhooks/{id}/deactivate` | ADMIN | Deactivate webhook |

#### A.18 Monitoring Endpoints

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/actuator/health` | Public | System health status |
| GET | `/actuator/prometheus` | Public | Prometheus metrics |
| GET | `/actuator/info` | Public | Application info |
| GET | `/actuator/metrics` | ADMIN | Detailed metrics |
| GET | `/api/monitoring/performance` | ADMIN | Performance metrics |
| GET | `/api/monitoring/pool` | ADMIN | Connection pool stats |

---

### Appendix B: Rate Limiting Configuration

| Endpoint Group | Requests | Window (sec) | Rationale |
| :--- | :--- | :--- | :--- |
| Login | 3 | 300 | Anti-brute-force (OWASP ASVS 2.2.1) |
| Password Reset | 3 | 3,600 | Abuse prevention |
| Token Refresh | 10 | 60 | Moderate access |
| Product Search | 60 | 60 | Tuned from traffic analysis (+20% headroom) |
| Admin API | 50 | 60 | Tuned for bulk operations (+67% from baseline) |
| User API | 500 | 60 | General user activity budget |
| General API | 200 | 60 | Catch-all protection |

---

### Appendix C: Use Case Scenarios

#### C.1 UC-01 — User Browses and Purchases a Product

| Step | Actor | Action | System Response |
| :--- | :--- | :--- | :--- |
| 1 | User | Opens BuildNest home page | Displays featured products and category navigation |
| 2 | User | Searches for "cement" | Returns paginated product listing matching "cement" |
| 3 | User | Clicks on a product | Displays product detail with reviews and stock status |
| 4 | User | Clicks "Add to Cart" | Product added; cart badge count updated |
| 5 | User | Navigates to Cart | Displays items, quantities, subtotal, and total |
| 6 | User | Clicks "Proceed to Checkout" | Validates cart; calculates total |
| 7 | User | Enters shipping address, clicks "Pay" | Razorpay payment modal opens |
| 8 | User | Completes payment | Order created; inventory deducted; confirmation displayed |

**Preconditions**: User is authenticated. Product is in stock.  
**Postconditions**: Order persisted. Payment recorded. Inventory decremented.  
**Traced to**: FR-CART-01, FR-CHK-01–06, FR-PAY-01–03, FR-INV-06.

#### C.2 UC-02 — Admin Manages Inventory

| Step | Actor | Action | System Response |
| :--- | :--- | :--- | :--- |
| 1 | Admin | Navigates to Admin Dashboard | Displays summary cards (sales, orders, low-stock items) |
| 2 | Admin | Clicks "Inventory" in sidebar | Displays inventory list with colour-coded stock status |
| 3 | Admin | Selects a low-stock product | Displays product inventory detail with threshold settings |
| 4 | Admin | Clicks "Add Stock" and enters quantity | Stock updated; status changes from `LOW_STOCK` to `IN_STOCK` |
| 5 | Admin | Configures threshold for the product | Threshold saved; future low-stock alerts trigger at new level |

**Preconditions**: User holds `ADMIN` role.  
**Postconditions**: Inventory updated. Audit log entry created.  
**Traced to**: FR-INV-03–06, FR-ADM-04, FR-ADM-06.

---

### Appendix D: Prometheus Alert Rules

The following 13 alert rules shall be configured in `kubernetes/prometheus-rules.yaml`:

| Alert Rule | Category | Condition |
| :--- | :--- | :--- |
| `BuildNestPodsNotReady` | Application | Pod not in Ready state |
| `BuildNestInsufficientReplicas` | Application | Available replicas < desired |
| `BuildNestHighRequestLatency` | Performance | P95 latency > threshold |
| `BuildNestHighErrorRate` | Performance | 5xx error rate > threshold |
| `BuildNestThreadPoolSaturation` | Performance | Thread pool utilisation > threshold |
| `BuildNestHighCPUUsage` | Resources | CPU > configured limit % |
| `BuildNestHighMemoryUsage` | Resources | Memory > configured limit % |
| `BuildNestDatabaseConnectionPoolExhaustion` | Database | Active connections / max-pool-size > threshold |
| `BuildNestDatabaseSlowQueries` | Database | Query execution time > threshold |
| `BuildNestRedisDown` | Cache | Redis health check failing |
| `BuildNestLowCacheHitRate` | Cache | Cache hit rate < threshold |
| `BuildNestHighRateLimitBlocking` | Security | Rate limit rejections per minute > threshold |
| `BuildNestHighAuthenticationFailures` | Security | Authentication failures per minute > threshold |

---

### Appendix E: Glossary Cross-Reference

See [Section 1.5 — Definitions, Acronyms, and Abbreviations](#15-definitions-acronyms-and-abbreviations).

---

**— End of Document —**

*This document was prepared in conformance with ISO/IEC/IEEE 29148:2018 for the BuildNest E-Commerce Platform. It supersedes SRS v3.0 archived at `archive/docs/ISO-IEC-IEEE/SRS_IEEE_29148_2018.md`. All changes in v4.0 are evidence-based and traceable to the Baseline Assessment Report (docs/reports/baseline-assessment-2026-06-19.md).*

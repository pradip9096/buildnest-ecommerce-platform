# Software Requirements Specification (SRS)

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## DOCUMENT INFORMATION

| Attribute | Value |
| :--- | :--- |
| **Document Title** | Software Requirements Specification (SRS) |
| **Document ID** | SRS-BUILDNEST-001 |
| **Version** | 5.11 |
| **Date** | 2026-08-03 IST |
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
| 4.0 | 2026-06-19 | Technical Lead | Baseline-driven update: corrected Spring Boot version (3.2.2 → 3.5.10); added Phase classification (Ph-1 Stable / Ph-2 Production Ready); added test integrity requirements (TIR); updated MNT-02 coverage target (40% → 70%); corrected MNT-03 to reflect live test state; added SEC-14 CSP requirement; added CON-11 React version constraint; referenced baseline assessment report | Pending |
| 4.1 | 2026-07-17 13:43 IST | Technical Lead | Corrected two stale technology-stack claims found via direct source verification (#455): Redis client is Lettuce, not Jedis (`lettuce-core:6.6.0` confirmed via `mvnw dependency:tree`; Jedis absent from classpath); Elasticsearch version is 8.17 (`docker-compose.yml`'s active service), not the previously-recorded 8.10. Appendix A's API Endpoint Catalogue was found separately stale (wrong path prefixes, missing endpoint groups) and filed as its own follow-up (#456) rather than fixed here, since it needs a full per-endpoint re-derivation | Pending |
| 4.2 | 2026-07-17 16:33 IST | Technical Lead | Added FR-FE-31 (admin category management UI) to §3.2.10.2, tracing to #428's `CategoriesTab.tsx`/`CategoryFormModal.tsx` — the `FR-FE-*` series previously had no requirement row for this feature at all despite it being implemented (#450) | Pending |
| 4.3 | 2026-07-17 18:18 IST | Technical Lead | Full re-derivation of Appendix A's API Endpoint Catalogue (#456), fixing every controller base-path prefix and adding previously-missing endpoint groups (categories, tags, coupons, shipping-methods, search reindex, inventory-threshold/analytics/reports, public webhook receiver, product reviews, notifications SSE, auth validate-token/csrf) — 18 sections expanded to 36, each citing its real controller class. Determined `/api/checkout` (legacy single-step) vs `/api/v1/checkout` (multi-step, current — confirmed via `frontend/src/api/checkout.ts`) via direct investigation, not assumption | Pending |
| 4.4 | 2026-07-17 19:11 IST | Technical Lead | §4.2's Frontend aggregate row still said `FR-FE-01–30`/count 30, never updated when #450 added `FR-FE-31` (#470). Corrected to `FR-FE-01–31`/31, and recomputed the priority breakdown by reading all 31 rows' actual priorities directly rather than incrementing the stale figure — real split is 16 High/12 Medium/3 Low, not the previously-stated 15/10/5 (which was already wrong even for the original 30, independent of FR-FE-31). Recomputed **Total Functional** from the table's own row counts: 99, not the previously-stated 95 (which also didn't match the sum of its own listed rows even before this fix) | Pending |
| 4.5 | 2026-07-17 20:05 IST | Technical Lead | §4.2's Authentication, Shopping Cart, and Payment rows each had a duplicated priority label from a copy-paste-shaped defect, found incidentally during #470 (#474). Recomputed all three directly from their §3.2 requirement rows rather than guessing the intended third label: Authentication is 9 High/2 Medium (not "8 High, 2 Medium, 1 Medium"), Shopping Cart is 5 High/1 Medium (not "4 High, 1 Medium, 1 High"), Payment is 4 High/1 Medium (not "3 High, 1 Medium, 1 High") — none of the three actually contain a Low-priority requirement, contrary to the issue's own initial guess. Checked every other row in the same table for the same duplicate-label pattern; none found | Pending |
| 4.6 | 2026-07-18 20:50 IST | Technical Lead | FR-FE-25's requirement text covered only status filters and status updates, but the admin order-management UI now also processes refunds (#438). Extended §3.2.10.2's FR-FE-25 row text to name refund processing explicitly | Pending |
| 4.7 | 2026-07-19 06:00 IST | Technical Lead | Added FR-ADM-10 (admin manages product tags — create/view/update/delete) to §3.2.8 — no FR row existed for tag management at all despite `AdminProductTagController`'s backend already being fully implemented and Appendix A.19 already documenting its endpoints; the admin frontend UI for it was built in the same change (#429). Recomputed §4.2's Admin Operations aggregate row (9→10 requirements, 6→7 Medium) | Pending |
| 4.8 | 2026-07-19 09:00 IST | Technical Lead | Added FR-ADM-11 (admin manages coupons — view, create, deactivate) to §3.2.8. Appendix A.20's endpoint table was itself stale, listing only `POST`/`DELETE` — corrected to include the `GET` list endpoint added in the same change (#435), since the admin frontend UI (`CouponsTab.tsx`) can't function without one, matching the pattern already fixed once for A.19. Recomputed §4.2's Admin Operations aggregate row (10→11 requirements, 7→8 Medium) | Pending |
| 4.9 | 2026-07-19 10:40 IST | Technical Lead | Added FR-CHK-09 (users apply a coupon/discount code during checkout) to §3.2.4 — the backend endpoint (`MultiStepCheckoutController.applyCoupon`, `/api/v1/checkout/coupon`) already existed and was already documented in Appendix A.10, but no requirement row was ever added for it despite FR-ADM-11's sibling admin-side requirement existing since #435; the customer-facing frontend wiring (coupon input on the checkout Shipping step, discount reflected in order-summary/payment totals) was built in the same change (#436). Recomputed §4.2's Checkout aggregate row (8→9 requirements, 2→3 Medium) | Pending |
| 5.0 | 2026-07-22 15:00 IST | Technical Lead | **Marketplace pivot addendum.** Promoted FUT-02 ("Multi-vendor marketplace support") from a one-line deferred placeholder into a scoped requirements addendum, following a business-model discussion establishing the platform's actual direction: a district/location-scoped multi-seller marketplace connecting existing offline construction-material and décor shops to nearby buyers, phased B2C-first with a B2B (bulk/RFQ) extension to follow. Added: new Ph-3 delivery phase (§2.8) for this expansion, distinct from Ph-1/Ph-2's stabilisation-and-production-readiness scope; new SELLER user characteristic (§2.3); new SN-08 stakeholder need (§2.7); two new Feature Groups, FG-11 Seller & Marketplace Management and FG-12 Location-Based Matching, with placeholder FR-SEL-* / FR-LOC-* requirement rows (§3.2.11, §3.2.12) — all rows explicitly Phase=Ph-3 and unverified (nothing in this addendum is implemented yet; no backend/frontend code changed in this revision). Updated §1.2 Scope and §1.3.2/§2.2 Product Functions Summary to list the two new groups. This is additive only — no existing FR row was removed, renumbered, or reinterpreted; existing Ph-1/Ph-2 requirements and their RTM traceability are unaffected. SDD/RTM/Test Plan updates to follow as separate revisions once this addendum is reviewed | Pending |
| 5.1 | 2026-07-22 17:30 IST | Technical Lead | FR-SEL-01 (seller registration) implemented (#553) — first requirement delivered from the Ph-3 marketplace-pivot addendum. Updated §4.2's Seller & Marketplace aggregate status row (0→1 Implemented, 8→7 Not started); Grand Total unaffected (category stays explicitly excluded per v5.0) | Pending |
| 5.2 | 2026-07-28 IST | Technical Lead | Resolved OQ-01/OQ-02 (§3.2.12) via [ADR 0001](../design/adr/0001-district-matching-strategy-for-location-based-seller-buyer-matching.md) (#561): district matching is radius/seller-declared (`Seller ──[N:M]──► District`), and district is sourced from a fixed, admin-maintained reference table. Updated FR-LOC-03's requirement text to state the resolved mechanism instead of "TBD, see OQ-01"; removed the Open Questions table and its blocking status note, replaced with a resolved-decision note citing the ADR | Pending |
| 5.3 | 2026-07-29 IST | Technical Lead | FR-LOC-01/02 implemented (#562) — district reference-data model: `District`/`SellerDistrict` entities, `seller_districts` join table, buyer's own `users.district_id` derived from `Address`. Updated §3.2.12's status note and the §4.2 Location-Based Matching aggregate row (0→2 Implemented, 4→2 Not started). FR-LOC-03/04 remain Ph-3, Planned — tracked by #563/#564 | Pending |
| 5.4 | 2026-07-29 IST | Technical Lead | FR-LOC-03 implemented (#563) — district-scoped catalogue/search filtering added to the existing Elasticsearch-backed product search (FG-02). Updated §3.2.12's status note and the §4.2 Location-Based Matching aggregate row (2→3 Implemented, 2→1 Not started). FR-LOC-04 remains Ph-3, Planned — tracked by #564 | Pending |
| 5.5 | 2026-07-29 IST | Technical Lead | FR-LOC-04 implemented (#564), completing FG-12 (Location-Based Matching) — `CheckoutServiceImpl.validateCheckout` enforces district membership server-side at checkout, fail-closed when the buyer's district can't be determined, unrestricted when the seller has no declared districts. Updated §3.2.12's header/status note from "Ph-3, Planned" to "Ph-3, complete", and the §4.2 Location-Based Matching aggregate row (3→4 Implemented, 1→0 Not started) | Pending |
| 5.6 | 2026-07-29 IST | Technical Lead | SEC-14 (#110): backend CSP was already `unsafe-inline`-free since #237, but the frontend's own document CSP (`frontend/security-headers.conf`) still carried `unsafe-inline` on `style-src`; removed after confirming React's `style={{}}` prop doesn't trigger the inline-style CSP restriction (JS property assignment, not the `style=` HTML attribute). Updated the SEC-14 Note in §7 (or equivalent security-requirements section) from "known gap for Ph-1" to resolved | Pending |
| 5.7 | 2026-07-29 IST | Technical Lead | Added SEC-15 (#111): OWASP Top 10 2021 assessment requirement, correcting a filing-time traceability mismatch where the issue cited "(SEC-02)" (an unrelated, already-satisfied JWT-length requirement) and a blanket "SEC-01 to SEC-15" range that didn't exist yet. Updated SN-06 and the Coverage Summary Security row from SEC-01–14 to SEC-01–15 (14→15 requirements, 9→10 High). See `docs/SDLC-docs/reports/security-assessment.md` for the full A01–A10 assessment | Pending |
| 5.8 | 2026-07-30 IST | Technical Lead | Periodic 15-issue SDLC documentation sync (overdue — last performed at #452, 2026-07-17; 53 issues closed since). Corrected a stale Spring Boot version claim (3.5.10 → 3.5.16, verified directly against `backend/pom.xml`'s `spring-boot-starter-parent`) in 3 places (REF-08, §4.2's Backend Framework row, CON-02) — this had drifted through several patch releases with no single issue's own scope covering a re-verification. MySQL 8.2 / Redis 7 / Elasticsearch 8.17 claims re-checked against `docker-compose.yml`'s active (non-commented) service definitions — still accurate, no change needed | Pending |
| 5.9 | 2026-08-01 IST | Technical Lead | Added SEC-16 (#112): HTTP security headers (HSTS/X-Frame-Options/X-Content-Type-Options/Referrer-Policy/Permissions-Policy), correcting a filing-time traceability mismatch where the issue cited "SRS SEC-11, SEC-12" (unrelated: search rate limiting, JWT rotation). Verified against the Spring Security 6.5 reference docs (context7) that HSTS/X-Frame-Options/X-Content-Type-Options are already framework defaults; Referrer-Policy and Permissions-Policy were genuinely missing and implemented. Updated the Coverage Summary Security row from SEC-01–15 to SEC-01–16 (15→16 requirements, 10→11 High) | Pending |
| 5.10 | 2026-08-01 IST | Technical Lead | Updated the Testing Frameworks table's E2E Testing row: added Playwright 1.62 (#117, `frontend/e2e/`) as the frontend-owned E2E tool per ADR 0002; the pre-existing Selenium WebDriver 4.16 row marked "being retired" pending #647 | Pending |
| 5.11 | 2026-08-03 IST | Technical Lead | Added §3.8.4 Compliance (COMP-01–03: GDPR right-to-access data export, right-to-erasure with 30-day anonymization retention, registration consent capture) for #128 — the issue's own "SRS NFR-COMP-01 to NFR-COMP-03" citation referenced a range that did not exist at filing time, same filing-time traceability-mismatch shape as SEC-15/SEC-16 (#111/#112). Renumbered §3.8.4–3.8.7 (Maintainability/Portability/Scalability) to §3.8.5–3.8.8 to make room. Updated the Coverage Summary Totals row (Non-Functional 61→64, Grand Total 156→159) | Pending |

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
| Seller & Marketplace Management *(Ph-3, Planned)* | Seller onboarding/verification, seller-owned product catalogue, district-based seller-buyer matching — see §3.2.11–§3.2.12 |

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
| FG-11 | Seller & Marketplace Management *(Ph-3, Planned)* | Seller onboarding/verification, seller-owned product catalogue, seller dashboard |
| FG-12 | Location-Based Matching *(Ph-3, complete)* | District-scoped seller/product visibility for buyers, based on transport-cost constraints |

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
| REF-08 | Spring Boot Reference Documentation | 3.5.16 |
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
| Backend Framework | Spring Boot 3.5.16 |
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
| FG-11 | Seller & Marketplace Management *(Ph-3, Planned)* | Seller onboarding/verification, seller-owned product catalogue, seller dashboard |
| FG-12 | Location-Based Matching *(Ph-3, complete)* | District-scoped seller/product visibility for buyers, based on transport-cost constraints |

### 2.3 User Characteristics

| User Role | Description | Technical Expertise | Primary Functions |
| :--- | :--- | :--- | :--- |
| **End User (USER)** | Registered customer browsing and purchasing products | Low to moderate; interacts via frontend SPA | Browse products, manage cart, place orders, write reviews, manage wishlist |
| **Administrator (ADMIN)** | Platform operator managing products, inventory, and system health | Moderate to high; may use admin UI or direct API | Manage products/inventory, view analytics/reports, manage users, audit logs |
| **Seller (SELLER)** *(Ph-3, Planned)* | Verified offline shop owner (construction materials or décor) selling through the platform to buyers within their district | Low to moderate; interacts via seller dashboard | Manage own product listings/inventory, view own orders, fulfil within served districts |
| **API Consumer (Developer)** | Frontend or third-party developer integrating with the API | High; direct REST API interaction | All API endpoints per granted role |
| **DevOps / SRE** | Operations team managing deployment and monitoring | High; infrastructure and monitoring tools | Deployment, health monitoring, alerting, disaster recovery |

### 2.4 Constraints

| ID | Constraint | Description |
| :--- | :--- | :--- |
| CON-01 | Language & Runtime | Java 21 (LTS) is required |
| CON-02 | Backend Framework | Spring Boot **3.5.16** with Spring Security, Spring Data JPA |
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
| FUT-02 | Multi-vendor marketplace support — scoped in §3.2.11 (FG-11) / §3.2.12 (FG-12); B2B bulk/RFQ extension remains deferred beyond Ph-3 | Ph-3 |
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
| SN-06 | Security Auditors | Compliance with OWASP, PCI-DSS, and GDPR standards | SEC-01–16 |
| SN-07 | QA Engineers | A test suite whose pass signal is trustworthy and reflects real system behaviour | TIR-01–05 |
| SN-08 | Sellers *(Ph-3, Planned)* | A way to reach buyers beyond their existing offline shop's foot traffic, within a service area they can realistically fulfil | FG-11, FG-12 |

### 2.8 Delivery Phases

All requirements are classified by delivery phase. Phase 1 is a prerequisite for Phase 2. Phase 3 is a business-model expansion, not a further stabilisation step — it is independent of Ph-1/Ph-2's technical-debt scope and may proceed once Ph-2 is reasonably stable, without requiring Ph-2's full completion criteria to be met first.

| Phase | Name | Goal | Completion Criteria |
| :--- | :--- | :--- | :--- |
| **Ph-1** | **Stable** | Backend build gate is trustworthy; zero test failures | `./mvnw test -P unit-tests` reports 0 failures, 0 errors |
| **Ph-2** | **Production Ready** | System is deployable, observable, secure, and end-user accessible | All Ph-2 requirements met; staging validation passed; frontend functional |
| **Ph-3** | **Marketplace Expansion** *(Planned)* | Platform supports multiple verified sellers, each district-scoped, selling to buyers via a B2C flow; B2B (bulk/RFQ) is an explicit later sub-phase, not required for Ph-3 completion | All Ph-3 FR-SEL-*/FR-LOC-* requirements implemented and tested; seller onboarding, district matching, and B2C checkout against seller-owned products functional end-to-end |

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
| FR-CHK-09 | The system shall allow users to apply a coupon/discount code during checkout | Medium | Ph-1 | Test |

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
| FR-ADM-10 | The system shall allow admins to create, view, update, and delete product tags | Medium | Ph-1 | Test |
| FR-ADM-11 | The system shall allow admins to view, create, and deactivate coupons | Medium | Ph-1 | Test |

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
| FR-FE-25 | Admin Order Management: all orders with status filters, order status updates, and refund processing | Medium | Ph-2 | Test |
| FR-FE-31 | Admin Category Management: table with CRUD actions and hierarchy support for product categories | Medium | Ph-2 | Test |

##### 3.2.10.3 Shared Components

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-FE-26 | Navbar: logo, search bar, cart badge, user menu — present on all pages | High | Ph-2 | Demonstration |
| FR-FE-27 | Footer: links (About, Contact, Terms, Privacy) and copyright | Low | Ph-2 | Demonstration |
| FR-FE-28 | ProductCard: product image, name, price, rating, "Add to Cart" | High | Ph-2 | Demonstration |
| FR-FE-29 | Breadcrumb: navigation hierarchy on product and category pages | Low | Ph-2 | Demonstration |
| FR-FE-30 | ErrorBoundary: catches React rendering errors and displays a fallback UI | Medium | Ph-2 | Test |

---

#### 3.2.11 Seller & Marketplace Management (FG-11) — *Ph-3, Planned*

> **Status note**: Every requirement in this subsection is planned, not implemented. No `Seller` entity, onboarding flow, or seller-scoped catalogue exists in the codebase as of v5.0 of this document. Verification method stated is the intended method once built; none of these rows are currently verifiable.

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-SEL-01 | The system shall allow a prospective seller to register an account distinct from a buyer (USER) account | High | Ph-3 | Test |
| FR-SEL-02 | The system shall require admin verification/approval of a seller registration (e.g. shop registration/GST details) before the seller can list products | High | Ph-3 | Test |
| FR-SEL-03 | The system shall associate each product with exactly one owning seller | High | Ph-3 | Test |
| FR-SEL-04 | The system shall allow a verified seller to create, update, and remove their own product listings, scoped to only their own products | High | Ph-3 | Test |
| FR-SEL-05 | The system shall allow a verified seller to manage inventory/stock for their own products only | High | Ph-3 | Test |
| FR-SEL-06 | The system shall allow a verified seller to view and manage orders containing their own products only | High | Ph-3 | Test |
| FR-SEL-07 | The system shall allow buyers to rate and review individual sellers, in addition to existing per-product reviews (FG-07) | Medium | Ph-3 | Test |
| FR-SEL-08 | The system shall prevent a seller from accessing or modifying another seller's products, inventory, or orders (defense in depth: URL authorisation + method-level `@PreAuthorize` per existing RBAC convention) | High | Ph-3 | Test |

#### 3.2.12 Location-Based Matching (FG-12) — *Ph-3, complete*

> **Status note**: FR-LOC-01/02 (the district reference-data model) are implemented (#562). FR-LOC-03 (catalogue/search district filtering) is implemented (#563) — the existing product search/catalogue (FG-02, Elasticsearch-backed) now carries a `districtId` filter dimension. FR-LOC-04 (checkout restriction) is implemented (#564) — `CheckoutServiceImpl.validateCheckout` enforces district membership server-side at checkout time, fail-closed when the buyer's district can't be determined. The district-matching mechanism was previously undecided (OQ-01/OQ-02); both questions are now resolved via [ADR 0001](../design/adr/0001-district-matching-strategy-for-location-based-seller-buyer-matching.md) (#561): matching is radius/seller-declared (`Seller ──[N:M]──► District`), and district is sourced from a fixed, admin-maintained reference table.

| ID | Requirement | Priority | Phase | Verification |
| :--- | :--- | :--- | :--- | :--- |
| FR-LOC-01 | The system shall record a district (or equivalent administrative area) for each seller | High | Ph-3 | Test |
| FR-LOC-02 | The system shall record a district (or equivalent) for each buyer, derived from their registered/delivery address | High | Ph-3 | Test |
| FR-LOC-03 | The system shall restrict the product catalogue a buyer browses/searches to sellers whose declared delivery districts include the buyer's district (radius/seller-declared matching, per ADR 0001) | High | Ph-3 | Test |
| FR-LOC-04 | The system shall prevent checkout of a product from a seller whose declared delivery districts do not include the buyer's district | High | Ph-3 | Test |

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
| SEC-15 | A full OWASP Top 10 (2021) assessment (A01–A10) shall be performed and documented before the M5 production-readiness gate, with zero open Critical findings and a remediation timeline for any open High findings | Ph-2 | High | Test |
| SEC-16 | API responses shall include HSTS (`max-age` ≥31536000, `includeSubDomains`), `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, and `Permissions-Policy` headers | Ph-2 | High | Test |

> **Note on SEC-16** (#112): no prior SRS row covered this header bundle — the issue's own References cited SEC-11/SEC-12 (search rate limiting, JWT rotation), a stale/incorrect citation unrelated to HTTP headers. HSTS/X-Frame-Options/X-Content-Type-Options were already correctly enforced by Spring Security defaults; Referrer-Policy and Permissions-Policy were genuinely missing and added.

> **Note on SEC-14**: Resolved. Backend `SecurityConfig`/`SecurityHeaderPolicies.MAIN_CSP` removed `unsafe-inline` from the API CSP in #237 (SEC-14); the frontend's own document CSP (`frontend/security-headers.conf`) retained `unsafe-inline` on `style-src` until #110, which removed it after confirming (live-browser verification) that React's `style={{}}` prop does not trigger the `style-src` inline restriction, since it sets styles via JS property assignment rather than the HTML `style` attribute.

> **Note on SEC-15** (#111): This requirement did not previously exist as its own SRS row — issue #111 was originally filed and titled "(SEC-02)", but SEC-02 is a narrower, already-satisfied requirement (JWT secret key length/externalisation) unrelated to a Top 10 assessment, and the issue's blanket "SRS SEC-01 to SEC-15" citation referenced a SEC-15 row that did not exist at filing time (SRS previously ended at SEC-14). Added here as the correct, dedicated FR this issue actually satisfies; see `docs/SDLC-docs/reports/security-assessment.md` for the full A01–A10 assessment this row traces to.

#### 3.8.4 Compliance

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| COMP-01 | Users shall be able to export all personal data associated with their account as JSON (GDPR right to access) | Ph-2 | 100% coverage of §3.5 PII fields | Test |
| COMP-02 | Users shall be able to request account deletion; the account shall be deactivated immediately and personal data irreversibly anonymised no later than 30 days after deletion, while financial/order records are retained for statutory retention periods (GDPR right to erasure) | Ph-2 | ≤ 30 days | Test |
| COMP-03 | Consent to the privacy policy shall be captured (with timestamp) at registration and shall be a precondition of account creation | Ph-2 | 100% of new registrations | Test |

> **Note on COMP-01–03** (#128): This requirement did not previously exist as its own SRS row — issue #128 cited "SRS NFR-COMP-01 to NFR-COMP-03", a range that did not exist at filing time (this SRS had no COMP-prefixed IDs before this change), the same filing-time traceability-mismatch shape already seen on SEC-15/SEC-16 (#111/#112). Added here as the correct, dedicated NFRs this issue actually satisfies. See `docs/compliance/pii-inventory.md` for the full PII field/table/retention inventory these requirements trace to.

#### 3.8.5 Maintainability

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

#### 3.8.6 Portability

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| PRT-01 | The system shall be containerised via Docker with multi-stage builds | Ph-2 | Docker | Inspection |
| PRT-02 | The system shall provide Kubernetes deployment manifests (Deployment, Service, ConfigMap, Secrets, Ingress) | Ph-2 | K8s | Inspection |
| PRT-03 | The system shall provide Terraform IaC for AWS deployment | Ph-2 | AWS | Inspection |
| PRT-04 | All configuration shall be environment-variable-driven per 12-Factor App methodology | Ph-1 | 12-Factor | Inspection |

#### 3.8.7 Scalability

| ID | Requirement | Phase | Target | Verification |
| :--- | :--- | :--- | :--- | :--- |
| SCL-01 | Stateless JWT authentication shall enable horizontal pod scaling without session affinity | Ph-1 | Horizontal | Inspection |
| SCL-02 | HikariCP connection pool sizing shall be configurable per deployment environment | Ph-1 | Configurable | Inspection |
| SCL-03 | Redis-backed rate limiting shall be shared across all application instances in a multi-pod deployment | Ph-2 | Distributed | Test |
| SCL-04 | The system shall sustain at least 1,000 concurrent users as validated by Gatling simulations | Ph-2 | 1,000 users | Analysis |

#### 3.8.8 Safety

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
| Authentication (FR-AUTH-01–11) | 11 | Ph-1 / Ph-2 | 9 High, 2 Medium | Test, Inspection |
| Product Catalogue (FR-PROD-01–09) | 9 | Ph-1 | 3 High, 4 Medium, 2 Low | Test, Inspection |
| Shopping Cart (FR-CART-01–06) | 6 | Ph-1 | 5 High, 1 Medium | Test, Inspection |
| Checkout (FR-CHK-01–09) | 9 | Ph-1 / Ph-2 | 6 High, 3 Medium | Test |
| Payment (FR-PAY-01–05) | 5 | Ph-2 | 4 High, 1 Medium | Test, Inspection |
| Inventory (FR-INV-01–07) | 7 | Ph-1 / Ph-2 | 3 High, 4 Medium | Test |
| Reviews / Wishlists (FR-REV, FR-WISH) | 5 | Ph-1 | 0 High, 3 Medium, 2 Low | Test |
| Admin Operations (FR-ADM-01–11) | 11 | Ph-1 / Ph-2 | 2 High, 8 Medium, 1 Low | Test |
| Monitoring (FR-MON-01–08) | 8 | Ph-1 / Ph-2 | 3 High, 3 Medium, 2 Low | Test, Inspection |
| Frontend (FR-FE-01–31) | 31 | Ph-2 | 16 High, 12 Medium, 3 Low | Test, Inspection, Demonstration |
| **Total Functional** | **99** | | | |
| Usability (UR-01–05, UR-FE-01–03) | 8 | Ph-1 / Ph-2 | Mixed | Test, Inspection |
| Performance (PR-01–08) | 8 | Ph-1 / Ph-2 | 4 High, 3 Medium, 1 Low | Analysis, Inspection |
| Reliability (REL-01–05) | 5 | Ph-1 / Ph-2 | Mixed | Analysis, Inspection |
| Availability (AVL-01–04) | 4 | Ph-2 | High | Test |
| Security (SEC-01–16) | 16 | Ph-1 / Ph-2 | 11 High, 5 Medium | Inspection, Test |
| Compliance (COMP-01–03) | 3 | Ph-2 | High | Test |
| Maintainability (MNT-01–06) | 6 | Ph-1 / Ph-2 | Mixed | Build, Inspection |
| Portability (PRT-01–04) | 4 | Ph-1 / Ph-2 | Mixed | Inspection |
| Scalability (SCL-01–04) | 4 | Ph-1 / Ph-2 | Mixed | Analysis, Inspection |
| Safety (SAF-01–03) | 3 | Ph-1 / Ph-2 | High | Test |
| Test Integrity (TIR-01–05) | 5 | Ph-1 / Ph-2 | 2 High, 3 Medium | Build, Inspection |
| **Total Non-Functional** | **64** | | | |
| **Grand Total** | **159** | | | |
| Seller & Marketplace (FR-SEL-01–08) *(Ph-3, Planned — excluded from Grand Total above)* | 8 | Ph-3 | 6 High, 2 Medium | 1 Implemented (#553), 7 Not started |
| Location-Based Matching (FR-LOC-01–04) *(Ph-3, complete — excluded from Grand Total above)* | 4 | Ph-3 | 4 High | 4 Implemented (#562, #563, #564) |

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
| E2E Testing | Rest Assured, Selenium WebDriver 4.16 (backend, being retired — see #647), Playwright 1.62 (frontend, `frontend/e2e/`, #117) | End-to-end API and browser testing |
| Load Testing | Gatling 3.10.3, JMeter | Performance and concurrency validation |
| Mutation Testing | PIT (pitest) 1.16.1 | Test quality validation (≥ 75% threshold) |
| Coverage | JaCoCo 0.8.11 | Code coverage enforcement |
| Security Scanning | OWASP Dependency-Check 9.0.9 | CVE vulnerability scanning |
| Contract Testing | Pact (infrastructure present; tests pending — FUT-05) | Consumer-driven contract validation |
| Structured Logging | Logback + Logstash Encoder 7.4 | JSON log output |

---

## 5. Appendices

### Appendix A: API Endpoint Catalogue

**Re-derived 2026-07-17 (#456) directly from every controller's `@RequestMapping`/`@GetMapping`/etc.
source, not by prefix substitution on the prior version** — the prior catalogue had wrong path
prefixes throughout and omitted entire endpoint groups (categories, tags, coupons, shipping-methods,
search reindex, inventory-threshold/analytics/reports, the public webhook receiver, product reviews,
auth token-validate/csrf). Controller class name is cited per group so a future drift check can
`grep` the exact source file rather than guessing.

#### A.1 Authentication Endpoints (`AuthController`, base `/api/auth`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/auth/login` | Public | User login (returns JWT access + refresh cookies) |
| POST | `/api/auth/register` | Public | User registration |
| POST | `/api/auth/refresh` | Public | Refresh access token via refresh-token cookie |
| POST | `/api/auth/validate-token` | Public | Validate an access token |
| POST | `/api/auth/logout` | Authenticated | Invalidate refresh token, clear cookies |
| GET | `/api/auth/csrf` | Public | Bootstrap the `XSRF-TOKEN` cookie (see `spring-security.md` CSRF section) |

#### A.2 Password Reset Endpoints (`PasswordResetController`, base `/api/password`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/password/forgot` | Public | Request password reset email |
| POST | `/api/password/reset` | Public | Reset password with token |
| POST | `/api/password/change` | Authenticated (USER or ADMIN) | Change password while logged in |

#### A.3 Product Endpoints — Legacy Public (`HomeController`, base `/api/public`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/public` | Public | API welcome/info message |
| GET | `/api/public/health` | Public | Liveness check |
| GET | `/api/public/products` | Public | List all products (unpaginated) |
| GET | `/api/public/products/{id}` | Public | Get product by ID |
| GET | `/api/public/products/search` | Public | Search products by keyword |
| GET | `/api/public/products/featured` | Public | Featured products |
| GET | `/api/public/categories` | Public | List all categories |

#### A.4 Product Endpoints — V1 (Deprecated) (`ProductControllerV1`, base `/api/v1/products`)

`@Deprecated(since = "2.0", forRemoval = true)`, sunset 2026-12-31, `X-API-Deprecated` response
header on every method — see `ApiSunsetInterceptor`.

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/products` | Public | List products, paginated (legacy response shape) |
| GET | `/api/v1/products/{id}` | Public | Get product by ID (legacy response shape) |

#### A.5 Product Endpoints — V2 (Current) (`ProductControllerV2`, base `/api/v2/products`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v2/products` | Public | List products, paginated (enhanced response) |
| GET | `/api/v2/products/{id}` | Public | Get product detail |
| GET | `/api/v2/products/search` | Public | Search products |
| GET | `/api/v2/products/category/{categoryId}` | Public | Filter by category |
| GET | `/api/v2/products/{id}/related` | Public | Related products |

#### A.6 User Profile Endpoints (`UserController`, base `/api/user`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/user/profile` | USER | Get user profile |
| PUT | `/api/user/profile` | USER | Update user profile |

#### A.7 Address Endpoints (`AddressController`, base `/api/user/addresses`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/user/addresses` | USER | List saved addresses |
| POST | `/api/user/addresses` | USER | Add address |
| PUT | `/api/user/addresses/{id}` | USER | Update address |
| DELETE | `/api/user/addresses/{id}` | USER | Delete address |
| PUT | `/api/user/addresses/{id}/default` | USER | Set as default address |

#### A.8 Cart Endpoints (`CartController`, base `/api/user/cart`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/user/cart/add` | USER (ownership-checked) | Add item to cart |
| GET | `/api/user/cart/{userId}` | USER (ownership-checked) | Get cart contents |
| DELETE | `/api/user/cart/item/{cartItemId}` | USER | Remove cart item |
| DELETE | `/api/user/cart/clear/{userId}` | USER (ownership-checked) | Clear entire cart |
| GET | `/api/user/cart/total/{userId}` | USER (ownership-checked) | Get cart total |

#### A.9 Checkout Endpoints — Legacy Single-Step (`CheckoutController`, base `/api/checkout`)

Not `@Deprecated`-annotated in code, but superseded by A.10's multi-step flow (#76/CHK-01); no
frontend caller references `/api/checkout` (`frontend/src/api/checkout.ts` targets `/api/v1/checkout`
exclusively) — kept for direct payment-linked checkout, not removed.

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/checkout/process/{cartId}` | USER | Process checkout (no payment) |
| POST | `/api/checkout/process-with-payment/{cartId}` | USER | Checkout with Razorpay |
| GET | `/api/checkout/validate/{cartId}` | USER | Validate cart for checkout |
| GET | `/api/checkout/calculate-total/{cartId}` | USER | Calculate checkout total |

#### A.10 Checkout Endpoints — Multi-Step (Current) (`MultiStepCheckoutController`, base `/api/v1/checkout`)

Address → shipping → coupon → payment → confirm; session stored in Redis, 30-minute TTL. This is
the flow the frontend's `CheckoutPage` actually consumes.

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/checkout/shipping-options` | USER | Get available shipping options |
| POST | `/api/v1/checkout/address` | USER | Set checkout address |
| POST | `/api/v1/checkout/coupon` | USER | Apply coupon |
| POST | `/api/v1/checkout/shipping` | USER | Select shipping method |
| POST | `/api/v1/checkout/payment` | USER | Submit payment step |
| POST | `/api/v1/checkout/confirm` | USER | Confirm and place order |

#### A.11 Order Endpoints (`UserOrderController`, base `/api/user/orders`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/user/orders` | USER | Get order history |
| GET | `/api/user/orders/{id}` | USER | Get order by ID |

#### A.12 Wishlist Endpoints (`WishlistController`, base `/api/user/wishlist`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/user/wishlist/items/{productId}` | USER | Add product to wishlist |
| DELETE | `/api/user/wishlist/items/{productId}` | USER | Remove product from wishlist |
| GET | `/api/user/wishlist` | USER | Get wishlist contents |
| GET | `/api/user/wishlist/contains/{productId}` | USER | Check if product in wishlist |
| DELETE | `/api/user/wishlist` | USER | Clear entire wishlist |
| GET | `/api/user/wishlist/count` | USER | Get wishlist item count |

#### A.13 Product Review Endpoints (`ProductReviewController`, base `/api/products/{productId}/reviews`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/products/{productId}/reviews` | USER | Submit a review |
| GET | `/api/products/{productId}/reviews` | Public | Get reviews for product |
| GET | `/api/products/{productId}/reviews/summary` | Public | Get rating summary |
| GET | `/api/products/{productId}/reviews/top-helpful` | Public | Most-helpful reviews |
| POST | `/api/products/{productId}/reviews/{reviewId}/helpful` | Authenticated | Mark review as helpful |
| PUT | `/api/products/{productId}/reviews/{reviewId}` | USER | Update own review |
| DELETE | `/api/products/{productId}/reviews/{reviewId}` | USER | Delete own review |

#### A.14 Notification Endpoints (`NotificationController`, base `/api/user/notifications`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/user/notifications/stream` | USER | SSE stream of real-time notifications |

#### A.15 Public Inventory Status Endpoints (`InventoryStatusController`, base `/api/inventory`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/inventory/{productId}/status` | Public | Get stock status |
| GET | `/api/inventory/{productId}/details` | Public | Get inventory details |
| GET | `/api/inventory/{productId}/available` | Public | Check available quantity |

#### A.16 Public Webhook Receiver (`PaymentWebhookController`, base `/api/v1/webhooks`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/webhooks/payment` | Public (signature-verified) | Payment provider server-to-server callback |

#### A.17 Admin Product Management (`AdminProductController`, base `/api/v1/admin/products`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/products` | ADMIN | List all products |
| GET | `/api/v1/admin/products/{id}` | ADMIN | Get product by ID |
| POST | `/api/v1/admin/products` | ADMIN | Create product |
| PUT | `/api/v1/admin/products/{id}` | ADMIN | Update product |
| DELETE | `/api/v1/admin/products/{id}` | ADMIN | Delete product |
| GET | `/api/v1/admin/products/{id}/images` | ADMIN | List product images |
| POST | `/api/v1/admin/products/{id}/images` | ADMIN | Upload product image (multipart) |
| PATCH | `/api/v1/admin/products/{id}/images/reorder` | ADMIN | Reorder product images |
| DELETE | `/api/v1/admin/products/{id}/images/{imageId}` | ADMIN | Delete product image |
| GET | `/api/v1/admin/products/{productId}/variants` | ADMIN | List product variants |
| POST | `/api/v1/admin/products/{productId}/variants` | ADMIN | Create product variant |
| PUT | `/api/v1/admin/products/{productId}/variants/{variantId}` | ADMIN | Update product variant |
| DELETE | `/api/v1/admin/products/{productId}/variants/{variantId}` | ADMIN | Delete product variant |

#### A.18 Admin Category Management (`AdminCategoryController`, base `/api/v1/admin/categories`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/categories` | ADMIN | List all categories (hierarchical) |
| GET | `/api/v1/admin/categories/{id}` | ADMIN | Get category by ID |
| POST | `/api/v1/admin/categories` | ADMIN | Create category |
| PUT | `/api/v1/admin/categories/{id}` | ADMIN | Update category |
| DELETE | `/api/v1/admin/categories/{id}` | ADMIN | Delete category (blocked if it has subcategories) |

#### A.19 Admin Product Tagging (`AdminProductTagController`, base `/api/v1/admin/tags`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/tags` | ADMIN | List all tags |
| GET | `/api/v1/admin/tags/{id}` | ADMIN | Get tag by ID |
| POST | `/api/v1/admin/tags` | ADMIN | Create tag |
| PUT | `/api/v1/admin/tags/{id}` | ADMIN | Update tag |
| DELETE | `/api/v1/admin/tags/{id}` | ADMIN | Delete tag |

#### A.20 Admin Coupon Management (`AdminCouponController`, base `/api/v1/admin/coupons`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/coupons` | ADMIN | List all coupons |
| POST | `/api/v1/admin/coupons` | ADMIN | Create coupon |
| DELETE | `/api/v1/admin/coupons/{id}` | ADMIN | Deactivate coupon |

#### A.21 Admin Shipping Method Management (`AdminShippingController`, base `/api/v1/admin/shipping-methods`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/shipping-methods` | ADMIN | List shipping methods |
| GET | `/api/v1/admin/shipping-methods/{id}` | ADMIN | Get shipping method by ID |
| POST | `/api/v1/admin/shipping-methods` | ADMIN | Create shipping method |
| PUT | `/api/v1/admin/shipping-methods/{id}` | ADMIN | Update shipping method |
| DELETE | `/api/v1/admin/shipping-methods/{id}` | ADMIN | Delete shipping method |

#### A.22 Admin Search Management (`AdminSearchController`, base `/api/v1/admin/search`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/admin/search/reindex` | ADMIN | Trigger Elasticsearch product reindex |

#### A.23 Admin Order Management (`AdminOrderController`, base `/api/v1/admin/orders`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/orders` | ADMIN | List all orders |
| GET | `/api/v1/admin/orders/{id}` | ADMIN | Get order details |
| PATCH | `/api/v1/admin/orders/{id}/status` | ADMIN | Update order status |
| POST | `/api/v1/admin/orders/{id}/refund` | ADMIN | Process refund |

#### A.24 Admin User Management (`AdminUserController`, base `/api/admin/users`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/users` | ADMIN | List all users |
| GET | `/api/admin/users/{id}` | ADMIN | Get user details |
| PUT | `/api/admin/users/{id}` | ADMIN | Update user |
| DELETE | `/api/admin/users/{id}` | ADMIN | Soft-delete user |

#### A.25 Admin Inventory Management (`AdminInventoryController`, base `/api/v1/admin/inventory`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/inventory` | ADMIN | List inventory records |
| PATCH | `/api/v1/admin/inventory/{productId}` | ADMIN | Adjust inventory record |
| GET | `/api/v1/admin/inventory/product/{productId}` | ADMIN | Get inventory for product |
| POST | `/api/v1/admin/inventory/add-stock/{productId}` | ADMIN | Add stock |
| POST | `/api/v1/admin/inventory/update-stock/{productId}` | ADMIN | Update stock |
| GET | `/api/v1/admin/inventory/check-availability/{productId}` | ADMIN | Check available quantity |

#### A.26 Admin Inventory Threshold Management (`AdminInventoryThresholdController`, base `/api/admin/inventory-threshold`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/admin/inventory-threshold/product/{productId}` | ADMIN | Set product-level threshold |
| POST | `/api/admin/inventory-threshold/category/{categoryId}` | ADMIN | Set category-level threshold |
| GET | `/api/admin/inventory-threshold/product/{productId}` | ADMIN | Get product-level threshold |
| GET | `/api/admin/inventory-threshold/category/{categoryId}` | ADMIN | Get category-level threshold |
| GET | `/api/admin/inventory-threshold/product/{productId}/effective` | ADMIN | Get effective threshold (product overrides category) |
| PUT | `/api/admin/inventory-threshold/product/{productId}/use-category` | ADMIN | Revert product to category-level threshold |

#### A.27 Admin Inventory Analytics (`AdminInventoryAnalyticsController`, base `/api/admin/inventory-analytics`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/inventory-analytics/high-demand-low-inventory` | ADMIN | High-demand/low-stock products |
| GET | `/api/admin/inventory-analytics/seasonal-patterns` | ADMIN | Seasonal demand patterns |
| GET | `/api/admin/inventory-analytics/stock-turnover` | ADMIN | Stock turnover rate |
| GET | `/api/admin/inventory-analytics/restocking-plan` | ADMIN | Suggested restocking plan |

#### A.28 Admin Inventory Reports (`AdminInventoryReportController`, base `/api/admin/inventory-reports`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/inventory-reports/below-threshold` | ADMIN | Products below threshold |
| GET | `/api/admin/inventory-reports/breaches` | ADMIN | Threshold breach history |
| GET | `/api/admin/inventory-reports/frequent-problems` | ADMIN | Recurring low-stock products |
| GET | `/api/admin/inventory-reports/product/{productId}` | ADMIN | Report for a specific product |
| GET | `/api/admin/inventory-reports/summary` | ADMIN | Summary report |

#### A.29 Admin Reports (`AdminReportController`, base `/api/admin/reports`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/reports/dashboard` | ADMIN | Dashboard summary |
| GET | `/api/admin/reports/users/count` | ADMIN | User count |
| GET | `/api/admin/reports/products/count` | ADMIN | Product count |
| GET | `/api/admin/reports/orders/count` | ADMIN | Order count |
| GET | `/api/admin/reports/revenue` | ADMIN | Revenue report |

#### A.30 Admin Sales Analytics (`SalesAnalyticsController`, base `/api/v1/admin/analytics/sales`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/admin/analytics/sales/dashboard` | ADMIN | Sales dashboard |
| GET | `/api/v1/admin/analytics/sales/revenue/daily` | ADMIN | Daily revenue |
| GET | `/api/v1/admin/analytics/sales/conversion-rate` | ADMIN | Conversion rate |
| GET | `/api/v1/admin/analytics/sales/cart-abandonment-rate` | ADMIN | Cart abandonment rate |
| GET | `/api/v1/admin/analytics/sales/average-order-value` | ADMIN | Average order value |
| GET | `/api/v1/admin/analytics/sales/customer-lifetime-value/{userId}` | ADMIN | Customer lifetime value |

#### A.31 Admin Analytics (Audit/Metrics) (`AdminAnalyticsController`, base `/api/admin/analytics`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/analytics/audit-logs/user/{userId}` | ADMIN | Audit logs for a user |
| GET | `/api/admin/analytics/audit-logs/action/{action}` | ADMIN | Audit logs for an action type |
| GET | `/api/admin/analytics/audit-logs/range` | ADMIN | Audit logs within a time range |
| GET | `/api/admin/analytics/metrics/range` | ADMIN | Ingested metrics within a time range |
| GET | `/api/admin/analytics/metrics/recent` | ADMIN | Recent metrics |
| GET | `/api/admin/analytics/alerts/summary` | ADMIN | Alert summary |
| GET | `/api/admin/analytics/dashboard` | ADMIN | Analytics dashboard |
| GET | `/api/admin/analytics/api-errors/by-status` | ADMIN | API errors grouped by HTTP status |
| GET | `/api/admin/analytics/api-errors/by-endpoint` | ADMIN | API errors grouped by endpoint |
| GET | `/api/admin/analytics/behaviour` | ADMIN | User behaviour analytics |

#### A.32 Admin Audit Log (`AuditLogController`, base `/api/admin/audit`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/audit` | ADMIN | List audit log entries |

#### A.33 Admin Webhook Management (`WebhookAdminController`, base `/api/admin/webhooks`)

Distinct from A.16's public receiver — this is subscription management.

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/admin/webhooks` | ADMIN | Create webhook subscription |
| GET | `/api/admin/webhooks` | ADMIN | List all webhooks |
| PUT | `/api/admin/webhooks/{id}/deactivate` | ADMIN | Deactivate webhook |
| DELETE | `/api/admin/webhooks/{id}` | ADMIN | Delete webhook |

#### A.34 Admin Monitoring (`MonitoringController`, base `/api/admin/monitoring`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/monitoring/performance` | ADMIN | Performance metrics |
| GET | `/api/admin/monitoring/performance/sla-status` | ADMIN | SLA compliance status |
| POST | `/api/admin/monitoring/performance/reset` | ADMIN | Reset performance counters |
| GET | `/api/admin/monitoring/uptime` | ADMIN | Uptime (raw) |
| GET | `/api/admin/monitoring/uptime/formatted` | ADMIN | Uptime (formatted) |
| POST | `/api/admin/monitoring/uptime/reset` | ADMIN | Reset uptime counter |
| GET | `/api/admin/monitoring/health-status` | ADMIN | Health status summary |
| GET | `/api/admin/monitoring/sla-status` | ADMIN | Overall SLA status |

#### A.35 Admin Adaptive Thresholds (`AdminThresholdController`, base `/api/admin/thresholds`)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/admin/thresholds` | ADMIN | Get all alerting thresholds |
| GET / PUT | `/api/admin/thresholds/cpu` | ADMIN | Get/set CPU alert threshold |
| GET / PUT | `/api/admin/thresholds/memory` | ADMIN | Get/set memory alert threshold |
| GET / PUT | `/api/admin/thresholds/error-rate` | ADMIN | Get/set error-rate alert threshold |
| GET / PUT | `/api/admin/thresholds/response-time` | ADMIN | Get/set response-time alert threshold |
| GET / PUT | `/api/admin/thresholds/failed-logins` | ADMIN | Get/set failed-login alert threshold |
| GET / PUT | `/api/admin/thresholds/jwt-refresh` | ADMIN | Get/set JWT-refresh alert threshold |
| GET / PUT | `/api/admin/thresholds/admin-operations` | ADMIN | Get/set admin-operations alert threshold |
| POST | `/api/admin/thresholds/reset` | ADMIN | Reset all thresholds to defaults |

#### A.36 Actuator and Monitoring Endpoints

`/actuator/prometheus` uses a dedicated Basic Auth credential, isolated from the app's real user
accounts (`actuatorMonitoringSecurityFilterChain`, `@Order(0)` — see `spring-security.md`); every
other `/actuator/**` path requires `ROLE_ADMIN` via the main filter chain.

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| GET | `/actuator/health/**` | Public | Liveness/readiness health groups |
| GET | `/actuator/prometheus` | Basic Auth (dedicated monitoring credential) | Prometheus scrape target |
| GET | `/actuator/**` (all other paths, e.g. `/actuator/metrics`, `/actuator/env`) | ADMIN | Full actuator surface |
| GET | `/actuator/custom/performance-metrics` | ADMIN | Custom performance metrics (`PerformanceMetricsController`) |
| GET | `/actuator/custom/cache-metrics` | ADMIN | Cache metrics |
| GET | `/actuator/custom/database-metrics` | ADMIN | Database connection metrics |
| GET | `/actuator/custom/performance-report` | ADMIN | Performance report |
| GET | `/actuator/custom/pool-status` | ADMIN | Connection pool status (`PoolMetricsController`) |
| GET | `/actuator/custom/pool-health` | ADMIN | Connection pool health |

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

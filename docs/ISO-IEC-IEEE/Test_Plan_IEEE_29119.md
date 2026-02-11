# Test Plan

## BuildNest E-Commerce Platform

---

## DOCUMENT INFORMATION

| Attribute                | Value                                                                      |
| :----------------------- | :------------------------------------------------------------------------- |
| **Document Title**       | Test Plan                                                                  |
| **Document ID**          | TP-BUILDNEST-001                                                           |
| **Version**              | 3.0                                                                        |
| **Date**                 | February 11, 2026                                                          |
| **Status**               | Baselined                                                                  |
| **Classification**       | Internal Use                                                               |
| **Prepared For**         | CDAC Project                                                               |
| **Conformance Standard** | ISO/IEC/IEEE 29119-3:2021                                                  |
| **Related Documents**    | TCS-BUILDNEST-001, TDS-BUILDNEST-001, TER-BUILDNEST-001, DBR-BUILDNEST-001 |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date       | Author       | Changes                                                                                                     | Approval    |
| :------ | :--------- | :----------- | :---------------------------------------------------------------------------------------------------------- | :---------- |
| 1.0     | 2026-02-10 | BuildNest QA | Initial draft — 27 TC scope                                                                                 | ✅ Approved |
| 2.0     | 2026-02-11 | BuildNest QA | Expanded to 124 TCs, 22 categories, 12 modules; updated tools                                               | ✅ Approved |
| 3.0     | 2026-02-11 | BuildNest QA | ISO 29119-3 compliance: added Conformance, Definitions, Responsibilities, Deliverables, Suspension criteria | ✅ Pending  |

### Document Approval

| Role                  | Name         | Signature  | Date         |
| :-------------------- | :----------- | :--------- | :----------- |
| **Test Manager**      | QA Lead      | ****\_**** | ****\_\_**** |
| **Project Manager**   | Project Lead | ****\_**** | ****\_\_**** |
| **Technical Lead**    | Dev Lead     | ****\_**** | ****\_\_**** |
| **Quality Assurance** | QA Reviewer  | ****\_**** | ****\_\_**** |

---

## 1. Introduction

### 1.1 Purpose

This Test Plan defines the overall test strategy, scope, schedule, resource requirements, and risk analysis for the BuildNest E-Commerce Platform. It governs the execution of **124 test cases** documented in the [Test Case Specification (TCS)](Test_Case_Specification_IEEE_29119.md) across **22 test categories**.

### 1.2 Scope

| Module                        | In Scope                                                                   | Out of Scope                |
| :---------------------------- | :------------------------------------------------------------------------- | :-------------------------- |
| **Authentication & Password** | Login, Register, JWT lifecycle, Password Reset/Change, Rate limiting       | OAuth/SSO (not implemented) |
| **Product Catalog**           | Product CRUD, Search, Categories, API Versioning (V1/V2 sunset)            | Media CDN upload            |
| **Shopping Cart**             | Add/Remove/Update/Clear/Total                                              | Cross-device cart sync      |
| **Checkout & Orders**         | Order creation, Inventory reservation/deduction, Rollback, Order history   | Coupon/discount engine      |
| **Payment**                   | Razorpay integration, Signature verification                               | Multi-gateway               |
| **Inventory**                 | Stock tracking, Threshold monitoring, Optimistic locking, Analytics        | Warehouse management        |
| **Wishlist**                  | Add/Remove/Check/Clear/Count                                               | Wishlist sharing            |
| **Product Reviews**           | Submit/View/Update/Delete, Ratings (1-5), Helpful votes, Verified purchase | Image attachment            |
| **Admin**                     | Product/Order/User/Inventory/Analytics management                          | Role creation UI            |
| **Monitoring**                | Performance metrics, Pool metrics, Health indicators                       | APM tool integration        |
| **Notifications & Events**    | Domain event publishing, Email, Webhook delivery                           | Push notifications          |
| **Security**                  | JWT, RBAC, Rate limiting, Input validation, XSS, SQLi, CSRF                | Pen test                    |

### 1.3 Normative References

| Reference                     | Description                                           |
| :---------------------------- | :---------------------------------------------------- |
| **ISO/IEC/IEEE 29119-1:2022** | Concepts and Definitions for Software Testing         |
| **ISO/IEC/IEEE 29119-2:2021** | Test Processes                                        |
| **ISO/IEC/IEEE 29119-3:2021** | Test Documentation (governing standard for this plan) |
| **ISO/IEC/IEEE 29119-4:2021** | Test Techniques                                       |
| **ISO/IEC/IEEE 29148:2018**   | Requirements Engineering (SRS reference)              |
| **OWASP Testing Guide v4.2**  | Security testing methodology                          |

### 1.4 Related Project Documents

| Document                                              | Standard   | Relationship             |
| :---------------------------------------------------- | :--------- | :----------------------- |
| [SRS](SRS_IEEE_29148_2018.md)                         | IEEE 29148 | Test requirements source |
| [TCS](Test_Case_Specification_IEEE_29119.md)          | IEEE 29119 | Test case definitions    |
| [TDS](Test_Data_Specification_IEEE_29119.md)          | IEEE 29119 | Test data sets           |
| [TER](Test_Execution_Report_IEEE_29119.md)            | IEEE 29119 | Execution results        |
| [DBR](Defect_Bug_Report_IEEE_29119.md)                | IEEE 29119 | Defect tracking          |
| [RTM](Requirements_Traceability_Matrix_IEEE_29148.md) | IEEE 29148 | Requirement coverage     |

### 1.5 Definitions & Abbreviations

| Term / Abbr | Definition                                            |
| :---------- | :---------------------------------------------------- |
| **SUT**     | System Under Test — the BuildNest E-Commerce Platform |
| **TC**      | Test Case                                             |
| **TCS**     | Test Case Specification                               |
| **TDS**     | Test Data Specification                               |
| **TER**     | Test Execution Report                                 |
| **DBR**     | Defect/Bug Report                                     |
| **SRS**     | Software Requirements Specification                   |
| **RTM**     | Requirements Traceability Matrix                      |
| **JWT**     | JSON Web Token — stateless authentication mechanism   |
| **RBAC**    | Role-Based Access Control                             |
| **XSS**     | Cross-Site Scripting                                  |
| **SQLi**    | SQL Injection                                         |
| **CSRF**    | Cross-Site Request Forgery                            |
| **E2E**     | End-to-End testing                                    |
| **CI/CD**   | Continuous Integration / Continuous Delivery          |
| **HPA**     | Horizontal Pod Autoscaler                             |
| **OWASP**   | Open Web Application Security Project                 |
| **PII**     | Personally Identifiable Information                   |
| **SLA**     | Service Level Agreement                               |

### 1.6 Conformance Statement

> This document conforms to **ISO/IEC/IEEE 29119-3:2021**, _Software and Systems Engineering — Software Testing — Part 3: Test Documentation_. All mandatory ("shall") information elements defined in Clause 9 (Test Plan) of the standard have been addressed. Optional ("should"/"may") elements have been included where applicable to the BuildNest project scope.

---

## 2. Test Strategy

### 2.1 Test Levels

| Level           | Description                                    | Tools                                                    | Target Count |
| :-------------- | :--------------------------------------------- | :------------------------------------------------------- | :----------- |
| **Unit**        | Individual class/method testing in isolation   | JUnit 5, Mockito                                         | ~200+        |
| **Integration** | Module interaction and persistence layer tests | `@SpringBootTest`, `@DataJpaTest`, `MockMvc`             | ~80          |
| **System**      | Full-stack API flow testing                    | `@SpringBootTest(webEnvironment=RANDOM_PORT)`, `MockMvc` | ~30          |
| **E2E**         | Browser-based user journey testing             | Selenium WebDriver                                       | 6            |

### 2.2 Test Types

| Type                   | Focus Area                                                              | Test Cases                                                                 |
| :--------------------- | :---------------------------------------------------------------------- | :------------------------------------------------------------------------- |
| **Functional**         | Feature correctness for all 12 modules                                  | TC-AUTH, TC-PROD, TC-CART, TC-CHK, TC-ORD, TC-WISH, TC-REV, TC-ADM, TC-PWD |
| **Security**           | Authentication bypass, injection, XSS, RBAC violations                  | TC-SEC-001 to TC-SEC-019                                                   |
| **Performance**        | Response time, throughput under load                                    | TC-PERF-001 to TC-PERF-003                                                 |
| **Stress**             | Behavior under extreme load, 2x/3x capacity                             | TC-STRESS-001 to TC-STRESS-003                                             |
| **Reliability**        | Flakiness prevention, concurrent safety, failover, data integrity       | TC-REL-001 to TC-REL-005                                                   |
| **Edge Case**          | Boundary values, race conditions, error recovery, Unicode, empty states | TC-EDGE-001 to TC-EDGE-010                                                 |
| **End-to-End**         | Full user journeys through browser                                      | TC-E2E-001 to TC-E2E-006                                                   |
| **Integration (Cart)** | Cart-Order-Inventory flow                                               | TC-INT-001 to TC-INT-005                                                   |

### 2.3 Test Case Distribution

| Category        | ID Range                         | Count   |
| :-------------- | :------------------------------- | :------ |
| Authentication  | TC-AUTH-001 to TC-AUTH-015       | 15      |
| Checkout        | TC-CHK-001 to TC-CHK-011         | 11      |
| Cart            | TC-CART-001                      | 1       |
| Product         | TC-PROD-001 to TC-PROD-005       | 5       |
| Order           | TC-ORD-001 to TC-ORD-004         | 4       |
| Wishlist        | TC-WISH-001                      | 1       |
| Review          | TC-REV-001 to TC-REV-005         | 5       |
| Admin Product   | TC-ADM-PRD-001 to TC-ADM-PRD-002 | 2       |
| Admin Order     | TC-ADM-ORD-001 to TC-ADM-ORD-002 | 2       |
| Admin User      | TC-ADM-USR-001 to TC-ADM-USR-002 | 2       |
| Admin Inventory | TC-ADM-INV-001 to TC-ADM-INV-004 | 4       |
| Admin Analytics | TC-ADM-ANL-001                   | 1       |
| Password        | TC-PWD-001 to TC-PWD-010         | 10      |
| Integration     | TC-INT-001 to TC-INT-005         | 5       |
| Security        | TC-SEC-001 to TC-SEC-019         | 19      |
| Performance     | TC-PERF-001 to TC-PERF-003       | 3       |
| Stress          | TC-STRESS-001 to TC-STRESS-003   | 3       |
| Reliability     | TC-REL-001 to TC-REL-005         | 5       |
| Edge Cases      | TC-EDGE-001 to TC-EDGE-010       | 10      |
| E2E             | TC-E2E-001 to TC-E2E-006         | 6       |
| Monitoring      | TC-MON-001 to TC-MON-005         | 5       |
| **Total**       |                                  | **124** |

---

## 3. Test Schedule

| Phase                     | Activities                                 | Duration | Dependencies               |
| :------------------------ | :----------------------------------------- | :------- | :------------------------- |
| **Phase 1: Planning**     | Finalize TC, TD, environment setup         | 3 days   | SRS approved               |
| **Phase 2: Unit Testing** | Execute unit tests, fix failures           | 5 days   | Codebase stable            |
| **Phase 3: Integration**  | Cart-Order-Inventory flows, DB integration | 4 days   | Phase 2 complete           |
| **Phase 4: Security**     | Injection, AuthZ bypass, rate limit tests  | 3 days   | Phase 2 complete           |
| **Phase 5: Performance**  | Load testing, stress, reliability          | 3 days   | Phase 3 complete           |
| **Phase 6: E2E**          | Selenium browser tests, user journeys      | 3 days   | Phase 3 complete           |
| **Phase 7: Regression**   | Full regression after bug fixes            | 2 days   | Phase 4-6 defects resolved |

---

## 4. Test Environment

### 4.1 Infrastructure

| Component       | Test Configuration                 | Production Configuration            |
| :-------------- | :--------------------------------- | :---------------------------------- |
| **Application** | Spring Boot (profile: `test`)      | Spring Boot (profile: `production`) |
| **Database**    | H2 In-Memory / MySQL testcontainer | MySQL 8.x                           |
| **Cache**       | Disabled / Embedded Redis          | Redis 7.x                           |
| **Search**      | Mocked / Testcontainer             | Elasticsearch 8.x                   |
| **Payment**     | Razorpay Test Mode (`key_test_*`)  | Razorpay Live Mode                  |

### 4.2 Test Tools

| Tool                   | Purpose                  | Used In               |
| :--------------------- | :----------------------- | :-------------------- |
| **JUnit 5**            | Test framework           | All test levels       |
| **Mockito**            | Mocking dependencies     | Unit tests            |
| **Spring MockMvc**     | Controller testing       | Integration, System   |
| **@SpringBootTest**    | Full context loading     | System tests          |
| **@DataJpaTest**       | Repository testing       | Persistence tests     |
| **@WebMvcTest**        | Controller slice testing | Controller unit tests |
| **Selenium WebDriver** | Browser automation       | E2E tests             |
| **Maven Surefire**     | Test execution           | CI/CD pipeline        |
| **JaCoCo**             | Code coverage            | All tests             |
| **Custom assertions**  | Domain-specific          | All levels            |

---

## 5. Roles & Responsibilities

| Role                | Responsibilities                                                                       |
| :------------------ | :------------------------------------------------------------------------------------- |
| **Test Manager**    | Approve test plan, monitor progress, escalate risks, approve test completion           |
| **Test Lead**       | Design test strategy, review TCS/TDS, assign test execution, produce TER               |
| **Test Engineer**   | Execute test cases, log defects, verify fixes, maintain test data                      |
| **Developer**       | Fix defects, provide unit tests, support integration testing, resolve S1/S2 within SLA |
| **DevOps Engineer** | Maintain test environment, CI/CD pipeline, Docker containers, database provisioning    |
| **Security Tester** | Execute TC-SEC test cases, validate OWASP compliance, conduct injection testing        |
| **Product Owner**   | Validate acceptance criteria, approve release readiness, sign-off on UAT               |

---

## 6. Test Deliverables

| Deliverable                                                                          | Owner         | Delivery Phase |
| :----------------------------------------------------------------------------------- | :------------ | :------------- |
| This Test Plan (TP-BUILDNEST-001)                                                    | Test Lead     | Phase 1        |
| [Test Case Specification](Test_Case_Specification_IEEE_29119.md) (TCS-BUILDNEST-001) | Test Lead     | Phase 1        |
| [Test Data Specification](Test_Data_Specification_IEEE_29119.md) (TDS-BUILDNEST-001) | Test Engineer | Phase 1        |
| [Test Execution Report](Test_Execution_Report_IEEE_29119.md) (TER-BUILDNEST-001)     | Test Lead     | Phase 7        |
| [Defect/Bug Reports](Defect_Bug_Report_IEEE_29119.md) (DBR-BUILDNEST-001)            | Test Engineer | Ongoing        |
| Code Coverage Report (JaCoCo HTML)                                                   | CI Pipeline   | Phase 7        |
| Release Readiness Report                                                             | Test Manager  | Phase 7        |

---

## 7. Entry / Exit Criteria

### 7.1 Entry Criteria

| Criterion                    | Measurement                                      |
| :--------------------------- | :----------------------------------------------- |
| SRS and SDD approved         | Documented approval                              |
| Test environment operational | Health check passes                              |
| Test data seeded             | See [TDS](Test_Data_Specification_IEEE_29119.md) |
| All test cases reviewed      | TCS peer review sign-off                         |
| Build compilable (no errors) | `mvn clean compile` succeeds                     |

### 7.2 Exit Criteria (Test Completion)

| Criterion                  | Target                         | Current |
| :------------------------- | :----------------------------- | :------ |
| All P1 TCs executed        | 100%                           | —       |
| All S1/S2 defects resolved | 0 open                         | —       |
| Code coverage (line)       | ≥ 80%                          | 78%     |
| Code coverage (branch)     | ≥ 70%                          | —       |
| Pass rate                  | ≥ 95%                          | —       |
| Performance thresholds met | < 500ms for API, < 2s checkout | —       |

---

## 8. Suspension & Resumption Criteria

### 8.1 Suspension Criteria

Testing shall be suspended when any of the following conditions occur:

| Condition                                      | Action                                                      |
| :--------------------------------------------- | :---------------------------------------------------------- |
| S1 defect blocks > 30% of remaining test cases | Suspend testing; escalate to development for hotfix         |
| Test environment becomes unavailable           | Suspend testing; escalate to DevOps for restoration         |
| Build has > 5 compilation errors               | Suspend testing; return build to development                |
| Critical test data corruption                  | Suspend affected test suite; restore from backup or re-seed |
| External dependency outage (Razorpay sandbox)  | Suspend affected TCs only; continue independent tests       |

### 8.2 Resumption Criteria

Testing shall resume when:

| Condition                                 | Verification                                            |
| :---------------------------------------- | :------------------------------------------------------ |
| S1 defect is resolved and verified        | Re-run failed TC + regression suite for affected module |
| Test environment is restored              | Health check passes for all infrastructure components   |
| Build compiles and passes smoke tests     | `mvn clean test -Dtest=SmokeTest*` succeeds             |
| Test data is restored to known-good state | Data seeding script completes without errors            |
| External dependency is available          | Manual connectivity check + TC-CHK-001 passes           |

---

## 9. Risk Analysis

|  #   | Risk                                           |  Impact  | Likelihood | Mitigation                                                        |
| :--: | :--------------------------------------------- | :------: | :--------: | :---------------------------------------------------------------- |
| R-01 | Razorpay sandbox flakiness                     |   High   |   Medium   | Use test mode keys, mock for unit tests                           |
| R-02 | Concurrent stock deduction race conditions     |   High   |   Medium   | Optimistic locking via `@Version`, retry logic                    |
| R-03 | Elasticsearch cluster unavailability           |  Medium  |    Low     | Graceful degradation, skip analytics tests                        |
| R-04 | Test data corruption between runs              |  Medium  |   Medium   | `@Transactional` with rollback, `@DirtiesContext`                 |
| R-05 | XSS vulnerability in review comments (DEF-003) | Critical |    High    | Input sanitization, output encoding — **must fix before release** |
| R-06 | JWT token expiry timing in parallel tests      |   Low    |   Medium   | Use long-lived tokens in test profile                             |
| R-07 | Selenium E2E flakiness                         |  Medium  |    High    | Implicit waits, retry on stale element exceptions                 |

---

## 10. Defect Management

### 10.1 Severity Classification

| Severity          | Description                                 | SLA (Resolution) | Example                   |
| :---------------- | :------------------------------------------ | :--------------- | :------------------------ |
| **S1 - Critical** | System unusable, data loss, security breach | 4 hours          | XSS in reviews            |
| **S2 - Major**    | Major feature broken, no workaround         | 1 business day   | Checkout rollback failure |
| **S3 - Minor**    | Feature defect with workaround available    | 3 business days  | Incorrect error message   |
| **S4 - Trivial**  | Cosmetic, typo, UI alignment                | Next sprint      | Misaligned button         |

### 10.2 Defect Workflow

```
NEW → ASSIGNED → IN PROGRESS → FIXED → VERIFIED → CLOSED
                      │                     │
                      └─── REJECTED ◄───────┘
                                            └─── REOPENED
```

---

**— End of Document —**

_This document was prepared in compliance with ISO/IEC/IEEE 29119-3:2021 for the BuildNest E-Commerce Platform._

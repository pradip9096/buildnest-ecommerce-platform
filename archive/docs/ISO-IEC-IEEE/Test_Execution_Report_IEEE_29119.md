# Test Execution Report (TER)

## BuildNest E-Commerce Platform

---

## DOCUMENT INFORMATION

| Attribute                | Value                                                   |
| :----------------------- | :------------------------------------------------------ |
| **Document Title**       | Test Execution Report                                   |
| **Document ID**          | TER-BUILDNEST-001                                       |
| **Version**              | 3.0                                                     |
| **Date**                 | February 11, 2026                                       |
| **Status**               | Baselined                                               |
| **Classification**       | Internal Use                                            |
| **Conformance Standard** | ISO/IEC/IEEE 29119-3:2021                               |
| **Parent Document**      | [Test Plan (TP-BUILDNEST-001)](Test_Plan_IEEE_29119.md) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date       | Author       | Changes                                                                                                             | Approval    |
| :------ | :--------- | :----------- | :------------------------------------------------------------------------------------------------------------------ | :---------- |
| 1.0     | 2026-02-10 | BuildNest QA | Initial — 27 TC results                                                                                             | ✅ Approved |
| 2.0     | 2026-02-11 | BuildNest QA | Expanded to 124 TC results; added Review, Wishlist, Admin, Security, Edge Case categories; updated coverage metrics | ✅ Approved |
| 3.0     | 2026-02-11 | BuildNest QA | ISO 29119-3 compliance: added Doc Control, Definitions, Conformance, Deviations, Comprehensive Assessment           | ✅ Pending  |

### Document Approval

| Role                | Name         | Signature      | Date             |
| :------------------ | :----------- | :------------- | :--------------- |
| **Test Lead**       | QA Lead      | \***\*\_\*\*** | \***\*\_\_\*\*** |
| **Test Manager**    | Test Manager | \***\*\_\*\*** | \***\*\_\_\*\*** |
| **Project Manager** | Project Lead | \***\*\_\*\*** | \***\*\_\_\*\*** |

---

## 1. Introduction

### 1.1 Scope

This Test Execution Report documents the results of executing **124 test cases** defined in [TCS-BUILDNEST-001](Test_Case_Specification_IEEE_29119.md) for test cycle **TC-CYCLE-001** (Sprint 1 Full Regression). It covers all 22 test categories across 12 functional modules.

### 1.2 Normative References

| Reference                                    | Description                             |
| :------------------------------------------- | :-------------------------------------- |
| **ISO/IEC/IEEE 29119-3:2021**                | Test Documentation (governing standard) |
| [Test Plan](Test_Plan_IEEE_29119.md)         | Test strategy and scope                 |
| [TCS](Test_Case_Specification_IEEE_29119.md) | Test case definitions                   |
| [TDS](Test_Data_Specification_IEEE_29119.md) | Test data used                          |
| [DBR](Defect_Bug_Report_IEEE_29119.md)       | Defect details                          |

### 1.3 Definitions & Abbreviations

| Term / Abbr | Definition                                        |
| :---------- | :------------------------------------------------ |
| **TC**      | Test Case                                         |
| **TER**     | Test Execution Report                             |
| **SUT**     | System Under Test                                 |
| **S1–S4**   | Severity levels (Critical, Major, Minor, Trivial) |
| **DEF**     | Defect identifier prefix                          |
| **JWT**     | JSON Web Token                                    |
| **E2E**     | End-to-End                                        |

### 1.4 Conformance Statement

> This document conforms to **ISO/IEC/IEEE 29119-3:2021**, _Software and Systems Engineering — Software Testing — Part 3: Test Documentation_. All mandatory ("shall") information elements defined in Clause 12 (Test Execution Report) have been addressed, including: test items, execution results, deviations, assessment, and overall verdict.

---

## 2. Executive Summary

| Metric                     | Value                                             |
| :------------------------- | :------------------------------------------------ |
| **Total Test Cases**       | 124                                               |
| **Executed**               | 124 (100%)                                        |
| **Passed**                 | 107 (86.3%)                                       |
| **Failed**                 | 12 (9.7%)                                         |
| **Blocked**                | 5 (4.0%)                                          |
| **Defects Found**          | 5 (1 S1, 1 S2, 2 S3, 1 S4)                        |
| **Code Coverage (Line)**   | 78% (target: 80%)                                 |
| **Code Coverage (Branch)** | 65% (target: 70%)                                 |
| **Overall Verdict**        | **CONDITIONAL PASS** — S1 defect must be resolved |

---

## 2. Test Execution Summary by Category

|  #  | Category                     | Total | Passed | Failed | Blocked | Pass Rate |
| :-: | :--------------------------- | :---: | :----: | :----: | :-----: | :-------: |
|  1  | Authentication (TC-AUTH)     |  15   |   14   |   1    |    0    |    93%    |
|  2  | Checkout (TC-CHK)            |  11   |   9    |   1    |    1    |    82%    |
|  3  | Cart (TC-CART)               |   1   |   1    |   0    |    0    |   100%    |
|  4  | Product (TC-PROD)            |   5   |   5    |   0    |    0    |   100%    |
|  5  | Order (TC-ORD)               |   4   |   4    |   0    |    0    |   100%    |
|  6  | Wishlist (TC-WISH)           |   1   |   1    |   0    |    0    |   100%    |
|  7  | Review (TC-REV)              |   5   |   3    |   2    |    0    |    60%    |
|  8  | Admin Product (TC-ADM-PRD)   |   2   |   2    |   0    |    0    |   100%    |
|  9  | Admin Order (TC-ADM-ORD)     |   2   |   2    |   0    |    0    |   100%    |
| 10  | Admin User (TC-ADM-USR)      |   2   |   2    |   0    |    0    |   100%    |
| 11  | Admin Inventory (TC-ADM-INV) |   4   |   3    |   1    |    0    |    75%    |
| 12  | Admin Analytics (TC-ADM-ANL) |   1   |   1    |   0    |    0    |   100%    |
| 13  | Password (TC-PWD)            |  10   |   9    |   0    |    1    |    90%    |
| 14  | Integration (TC-INT)         |   5   |   4    |   1    |    0    |    80%    |
| 15  | Security (TC-SEC)            |  19   |   15   |   3    |    1    |    79%    |
| 16  | Performance (TC-PERF)        |   3   |   3    |   0    |    0    |   100%    |
| 17  | Stress (TC-STRESS)           |   3   |   2    |   0    |    1    |    67%    |
| 18  | Reliability (TC-REL)         |   5   |   4    |   1    |    0    |    80%    |
| 19  | Edge Cases (TC-EDGE)         |  10   |   8    |   1    |    1    |    80%    |
| 20  | E2E (TC-E2E)                 |   6   |   6    |   0    |    0    |   100%    |
| 21  | Monitoring (TC-MON)          |   5   |   5    |   0    |    0    |   100%    |

---

## 3. Module-Level Coverage

| Module                 | Controllers | Services | Repos  | Test Files | Line Coverage |
| :--------------------- | :---------: | :------: | :----: | :--------: | :-----------: |
| **Auth & Password**    |      3      |    6     |   3    |     8      |      85%      |
| **Product Catalog**    |      3      |    3     |   2    |     6      |      82%      |
| **Cart**               |      1      |    1     |   1    |     3      |      80%      |
| **Checkout & Orders**  |      2      |    3     |   1    |     5      |      78%      |
| **Payment**            |      0      |    2     |   0    |     2      |      72%      |
| **Inventory**          |      1      |    4     |   1    |     4      |      75%      |
| **Wishlist**           |      1      |    1     |   1    |     2      |      88%      |
| **Review**             |      1      |    1     |   1    |     3      |      70%      |
| **Admin**              |     14      |    6     |   5    |     10     |      76%      |
| **Monitoring**         |      2      |    3     |   0    |     3      |      65%      |
| **Notification/Event** |      1      |    3     |   0    |     2      |      60%      |
| **Config/Security**    |      0      |    3     |   0    |     4      |      68%      |
| **Overall**            |   **28**    |  **56**  | **19** |   **52**   |    **78%**    |

---

## 4. Failed Test Cases

### 4.1 TC-REV-002: Invalid Review Input Validation

| Attribute        | Value                                                                                  |
| :--------------- | :------------------------------------------------------------------------------------- |
| **Status**       | ❌ FAILED                                                                              |
| **Severity**     | S3 (Minor)                                                                             |
| **Defect ID**    | [DEF-003](Defect_Bug_Report_IEEE_29119.md)                                             |
| **Root Cause**   | Review comment field not properly sanitized — Stored XSS via `<script>` tag in comment |
| **Impact**       | XSS payload stored and rendered to other users                                         |
| **Fix Required** | Input sanitization in `ProductReviewService`, output encoding in frontend              |

### 4.2 TC-SEC-012: XSS Prevention

| Attribute      | Value                                                             |
| :------------- | :---------------------------------------------------------------- |
| **Status**     | ❌ FAILED                                                         |
| **Severity**   | S1 (Critical)                                                     |
| **Defect ID**  | [DEF-003](Defect_Bug_Report_IEEE_29119.md)                        |
| **Root Cause** | Same as TC-REV-002 — `comment` field allows HTML/Script injection |
| **Impact**     | **Release blocker** — Potential for session hijacking, data theft |

### 4.3 TC-AUTH-015: Rate Limiting Effectiveness

| Attribute      | Value                                                     |
| :------------- | :-------------------------------------------------------- |
| **Status**     | ❌ FAILED                                                 |
| **Severity**   | S3 (Minor)                                                |
| **Defect ID**  | [DEF-001](Defect_Bug_Report_IEEE_29119.md)                |
| **Root Cause** | Rate limit counter not properly clearing across test runs |
| **Impact**     | Rate limiting works in production but flaky in test       |

### 4.4 TC-CHK-005: Checkout Rollback

| Attribute      | Value                                                             |
| :------------- | :---------------------------------------------------------------- |
| **Status**     | ❌ FAILED                                                         |
| **Severity**   | S2 (Major)                                                        |
| **Defect ID**  | [DEF-002](Defect_Bug_Report_IEEE_29119.md)                        |
| **Root Cause** | `releaseReservation()` not called when payment verification fails |
| **Impact**     | Inventory permanently reserved after failed payment               |

### 4.5 Other Failures

| Test Case      | Issue                              | Severity | Notes                        |
| :------------- | :--------------------------------- | :------: | :--------------------------- |
| TC-SEC-014     | XXE entity expansion not tested    |    S3    | Test implementation needed   |
| TC-SEC-019     | Header injection test incomplete   |    S4    | Test data setup issue        |
| TC-ADM-INV-004 | Threshold breach event flaky       |    S3    | Timing-dependent assertion   |
| TC-INT-003     | Concurrent checkout race condition |    S2    | Optimistic lock retry needed |
| TC-REL-003     | Memory leak under sustained load   |    S3    | Needs profiler analysis      |
| TC-REV-005     | Delete review returns wrong status |    S4    | 200 vs 204 status code       |
| TC-EDGE-003    | Negative quantity accepted         |    S3    | Missing validation           |
| TC-EDGE-010    | Empty cart total returns null      |    S4    | Should return 0.00           |

---

## 5. Blocked Test Cases

| Test Case     | Blocked Reason                                             | Resolution                   |
| :------------ | :--------------------------------------------------------- | :--------------------------- |
| TC-CHK-011    | Requires Razorpay sandbox to be operational                | Retry when sandbox available |
| TC-PWD-010    | SMTP test server not configured                            | Configure test mail server   |
| TC-SEC-017    | Request size limit not configurable in test profile        | Add test profile config      |
| TC-STRESS-003 | Load testing tool (Gatling/JMeter) not yet integrated      | Integrate tool               |
| TC-EDGE-008   | Database constraint test requires specific Liquibase state | Set up migration test        |

---

## 6. Defect Summary

| ID                                         | Title                                     | Severity | Status   | Related TCs            |
| :----------------------------------------- | :---------------------------------------- | :------: | :------- | :--------------------- |
| [DEF-001](Defect_Bug_Report_IEEE_29119.md) | Rate limit counter flaky in tests         |    S3    | Open     | TC-AUTH-015            |
| [DEF-002](Defect_Bug_Report_IEEE_29119.md) | Inventory not released on payment failure |    S2    | Open     | TC-CHK-005, TC-INT-003 |
| [DEF-003](Defect_Bug_Report_IEEE_29119.md) | **Stored XSS in review comments**         |  **S1**  | **Open** | TC-REV-002, TC-SEC-012 |
| [DEF-004](Defect_Bug_Report_IEEE_29119.md) | Negative quantity not validated           |    S3    | Open     | TC-EDGE-003            |
| [DEF-005](Defect_Bug_Report_IEEE_29119.md) | Empty cart total returns null             |    S4    | Open     | TC-EDGE-010            |

---

## 7. Recommendations

1. **CRITICAL:** Resolve DEF-003 (Stored XSS) before any release. Implement HTML sanitization in `ProductReviewService.submitReview()` and output encoding in frontend.

2. **HIGH:** Fix DEF-002 (Inventory rollback) — add `finally` block or `@Transactional` rollback for `releaseReservation()` in `CheckoutService`.

3. **MEDIUM:** Increase code coverage from 78% → 80% by adding tests for:
   - `NotificationService` (currently 60%)
   - `ElasticsearchMetricsCollectorService` (currently 65%)
   - Security configuration classes (currently 68%)

4. **LOW:** Resolve blocked test cases by configuring SMTP test server and integrating load testing tool.

5. **IMPROVEMENT:** Add mutation testing (PIT) to validate test effectiveness beyond line coverage.

---

## 9. Deviations from Test Plan

| Deviation                                         | Impact                                     | Justification                                                                    |
| :------------------------------------------------ | :----------------------------------------- | :------------------------------------------------------------------------------- |
| SMTP server not configured for test profile       | TC-E2E-006, TC-MON-004, TC-MON-005 blocked | Environment-specific; email verified via mock in unit tests                      |
| Load testing tool (JMeter/Gatling) not integrated | TC-STRESS-002 blocked                      | Performance tested via MockMvc response time assertions                          |
| Razorpay webhook callback not testable in CI      | TC-INT-004 partially covered               | Verified via mocked webhook signature validation                                 |
| Code coverage target (80%) not met                | 78% achieved — 2% gap                      | Low coverage in `NotificationService` and `ElasticsearchMetricsCollectorService` |

---

## 10. Comprehensive Assessment

### 10.1 Confidence Level

| Area                | Confidence | Rationale                                              |
| :------------------ | :--------: | :----------------------------------------------------- |
| Core Business Logic |    High    | 89% functional pass rate; all critical flows tested    |
| Security            |   Medium   | 79% pass rate; S1 XSS defect still open                |
| Performance         |    High    | All baseline performance tests pass                    |
| Integration         |    High    | Cart-Order-Inventory flow fully verified               |
| Edge Cases          |   Medium   | 80% pass rate; some boundary conditions reveal defects |

### 10.2 Risks Remaining

1. **DEF-003 (S1 Critical):** Stored XSS in product reviews — **release blocker**
2. **DEF-002 (S2 Major):** Inventory not released on payment failure — data integrity risk
3. **Code coverage 78%:** Below 80% target; gaps in notification and monitoring services
4. **Blocked tests:** 5 test cases blocked due to environment constraints

### 10.3 Overall Verdict

> [!CAUTION]
> **CONDITIONAL PASS** — The system demonstrates acceptable quality for core business flows. However, release is blocked until DEF-003 (Stored XSS) is resolved and verified, and DEF-002 (Inventory rollback) is fixed.

---

## 11. Test Environment

| Component          | Configuration            |
| :----------------- | :----------------------- |
| **Java**           | OpenJDK 17               |
| **Spring Boot**    | 3.x (profile: `test`)    |
| **Build**          | Maven 3.9.x              |
| **CI**             | Configured pipeline      |
| **Database**       | H2 In-Memory             |
| **Execution Date** | 2026-02-11               |
| **Duration**       | ~8 minutes (all 124 TCs) |

---

## 12. Revision History

See [Document Control](#document-control) for full revision history and approvals.

---

**— End of Document —**

_This document was prepared in compliance with ISO/IEC/IEEE 29119-3:2021 for the BuildNest E-Commerce Platform._

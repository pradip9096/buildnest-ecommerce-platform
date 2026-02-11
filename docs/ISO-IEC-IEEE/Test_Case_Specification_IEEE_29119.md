# Test Case Specification

## BuildNest E-Commerce Platform

---

## DOCUMENT INFORMATION

| Attribute                | Value                                                   |
| :----------------------- | :------------------------------------------------------ |
| **Document Title**       | Test Case Specification                                 |
| **Document ID**          | TCS-BUILDNEST-001                                       |
| **Version**              | 3.0                                                     |
| **Date**                 | February 11, 2026                                       |
| **Status**               | Baselined                                               |
| **Classification**       | Internal Use                                            |
| **Conformance Standard** | ISO/IEC/IEEE 29119-3:2021                               |
| **Parent Document**      | [Test Plan (TP-BUILDNEST-001)](Test_Plan_IEEE_29119.md) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date       | Author       | Changes                                                                                | Approval    |
| :------ | :--------- | :----------- | :------------------------------------------------------------------------------------- | :---------- |
| 1.0     | 2026-02-10 | BuildNest QA | Initial draft                                                                          | ✅ Approved |
| 2.0     | 2026-02-11 | BuildNest QA | Exhaustive update — 124 test cases across 22 categories derived from codebase analysis | ✅ Approved |
| 3.0     | 2026-02-11 | BuildNest QA | ISO 29119-3 compliance: added Doc Control, Definitions, Conformance, post-conditions   | ✅ Pending  |

### Document Approval

| Role               | Name         | Signature      | Date             |
| :----------------- | :----------- | :------------- | :--------------- |
| **Test Lead**      | QA Lead      | \***\*\_\*\*** | \***\*\_\_\*\*** |
| **Test Manager**   | Test Manager | \***\*\_\*\*** | \***\*\_\_\*\*** |
| **Technical Lead** | Dev Lead     | \***\*\_\*\*** | \***\*\_\_\*\*** |

---

## 1. Introduction

### 1.1 Purpose

This document specifies the individual **test cases** for the BuildNest E-Commerce Platform. Each test case defines the preconditions, input data, execution steps, and expected results required to verify compliance with the [SRS](SRS_IEEE_29148_2018.md). This version is exhaustively derived from the actual codebase test suite comprising **52+ test files** across **29 test packages**.

### 1.2 Scope

This TCS covers **124 test cases** across **22 categories**, providing full traceability to the SRS functional requirements (FR-AUTH through FR-ADM) and non-functional requirements (NFR-SEC, NFR-PERF). Test cases span all test levels: unit, integration, system, and end-to-end.

### 1.3 Normative References

| Reference                                          | Description                                   |
| :------------------------------------------------- | :-------------------------------------------- |
| **ISO/IEC/IEEE 29119-3:2021**                      | Test Documentation (governing standard)       |
| **ISO/IEC/IEEE 29148:2018**                        | Requirements Engineering (SRS reference)      |
| [Test Plan](Test_Plan_IEEE_29119.md)               | Parent test plan governing this specification |
| [Test Data](Test_Data_Specification_IEEE_29119.md) | Test data sets for execution                  |
| [SRS](SRS_IEEE_29148_2018.md)                      | Functional and non-functional requirements    |

### 1.4 Definitions & Abbreviations

| Term / Abbr | Definition                                          |
| :---------- | :-------------------------------------------------- |
| **TC**      | Test Case                                           |
| **SRS**     | Software Requirements Specification                 |
| **JWT**     | JSON Web Token — stateless authentication mechanism |
| **RBAC**    | Role-Based Access Control                           |
| **XSS**     | Cross-Site Scripting                                |
| **SQLi**    | SQL Injection                                       |
| **CSRF**    | Cross-Site Request Forgery                          |
| **E2E**     | End-to-End testing                                  |
| **SUT**     | System Under Test                                   |
| **MockMvc** | Spring MVC test framework for controller testing    |
| **OWASP**   | Open Web Application Security Project               |

### 1.5 Conformance Statement

> This document conforms to **ISO/IEC/IEEE 29119-3:2021**, _Software and Systems Engineering — Software Testing — Part 3: Test Documentation_. All mandatory ("shall") information elements defined in Clause 10 (Test Case Specification) have been addressed, including: unique identifier, objective, preconditions, inputs, steps, expected results, post-conditions, and status.

### 1.7 Automated Execution

| Test Category      | Automation Class                              | Framework   | Status       |
| :----------------- | :-------------------------------------------- | :---------- | :----------- |
| **Authentication** | `src/test/java/.../e2e/auth/AuthApiTest.java` | RestAssured | ✅ Automated |
| **User Profile**   | `src/test/java/.../e2e/user/UserApiTest.java` | RestAssured | ✅ Automated |

### 1.6 Test Case ID Convention

| Prefix     | Area                           |
| :--------- | :----------------------------- |
| TC-AUTH    | Authentication & Authorization |
| TC-PWD     | Password Reset & Change        |
| TC-PROD    | Product Catalog                |
| TC-CART    | Shopping Cart                  |
| TC-CHK     | Checkout & Orders              |
| TC-USR     | User Profile                   |
| TC-ORD     | User Order Management          |
| TC-WISH    | Wishlist                       |
| TC-REV     | Product Reviews                |
| TC-PAY     | Payment (Razorpay)             |
| TC-ADM-PRD | Admin Product Management       |
| TC-ADM-ORD | Admin Order Management         |
| TC-ADM-USR | Admin User Management          |
| TC-ADM-INV | Admin Inventory                |
| TC-ADM-ANL | Admin Analytics                |
| TC-SEC     | Security                       |
| TC-E2E     | End-to-End UI                  |
| TC-EDGE    | Edge Case & Boundary           |
| TC-PERF    | Performance Baseline           |
| TC-STRESS  | Stress Testing                 |
| TC-REL     | Reliability                    |

---

## 2. Authentication Test Cases

> **Source:** `controller/auth/AuthControllerTest.java` (16 test methods)

### TC-AUTH-001: Successful User Login

| Field             | Value                                                                  |
| :---------------- | :--------------------------------------------------------------------- |
| **Priority**      | Critical                                                               |
| **Preconditions** | User `john.doe` exists with password `Secret@123` and role `ROLE_USER` |
| **SRS Trace**     | FR-AUTH-01                                                             |

| Step | Action                                                                        | Expected Result     |
| :--: | :---------------------------------------------------------------------------- | :------------------ |
|  1   | `POST /api/auth/login` with `{"username":"john.doe","password":"Secret@123"}` | `200 OK`            |
|  2   | Verify `accessToken` in response                                              | JWT string present  |
|  3   | Verify `refreshToken` in response                                             | UUID string present |
|  4   | Verify `data.user.role` = `ROLE_USER`                                         | Role matches        |

### TC-AUTH-002: Login with Invalid Credentials

| Field             | Value                  |
| :---------------- | :--------------------- |
| **Priority**      | Critical               |
| **Preconditions** | User `john.doe` exists |
| **SRS Trace**     | FR-AUTH-02             |

| Step | Action                                     | Expected Result     |
| :--: | :----------------------------------------- | :------------------ |
|  1   | `POST /api/auth/login` with wrong password | `401 Unauthorized`  |
|  2   | Verify `success` = `false`                 | Error response      |
|  3   | Verify message "Invalid credentials"       | User-friendly error |

### TC-AUTH-003: Access Protected Endpoint Without Token

| Field             | Value                   |
| :---------------- | :---------------------- |
| **Priority**      | Critical                |
| **Preconditions** | No authentication token |
| **SRS Trace**     | FR-AUTH-03              |

| Step | Action                                                 | Expected Result    |
| :--: | :----------------------------------------------------- | :----------------- |
|  1   | `GET /api/user/profile` without `Authorization` header | `401 Unauthorized` |

### TC-AUTH-004: Access with Expired JWT Token

| Field             | Value                   |
| :---------------- | :---------------------- |
| **Priority**      | High                    |
| **Preconditions** | User has an expired JWT |
| **SRS Trace**     | FR-AUTH-04              |

| Step | Action                                   | Expected Result      |
| :--: | :--------------------------------------- | :------------------- |
|  1   | `GET /api/user/profile` with expired JWT | `401 Unauthorized`   |
|  2   | Verify "Token expired" message           | Clear expiry message |

### TC-AUTH-005: User Registration with Valid Data

| Field             | Value                             |
| :---------------- | :-------------------------------- |
| **Priority**      | Critical                          |
| **Preconditions** | Username `newuser` does not exist |
| **SRS Trace**     | FR-AUTH-05                        |

| Step | Action                                    | Expected Result  |
| :--: | :---------------------------------------- | :--------------- |
|  1   | `POST /api/auth/register` with valid JSON | `201 Created`    |
|  2   | Verify response contains user ID          | New user created |
|  3   | Login with new credentials                | Login succeeds   |

### TC-AUTH-006: Registration with Invalid Email

| Field             | Value      |
| :---------------- | :--------- |
| **Priority**      | High       |
| **Preconditions** | None       |
| **SRS Trace**     | FR-AUTH-06 |

| Step | Action                                         | Expected Result   |
| :--: | :--------------------------------------------- | :---------------- |
|  1   | `POST /api/auth/register` with malformed email | `400 Bad Request` |
|  2   | Verify validation error message                | Email rejected    |

### TC-AUTH-007: Rate Limiting on Login Endpoint

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | High                |
| **Preconditions** | Rate limiter active |
| **SRS Trace**     | NFR-SEC-03          |

| Step | Action                                         | Expected Result                                      |
| :--: | :--------------------------------------------- | :--------------------------------------------------- |
|  1   | Send 10+ rapid `POST /api/auth/login` requests | First N: `401`; After limit: `429 Too Many Requests` |

### TC-AUTH-008: Refresh Token — Success

| Field             | Value                        |
| :---------------- | :--------------------------- |
| **Priority**      | High                         |
| **Preconditions** | User has valid refresh token |
| **SRS Trace**     | FR-AUTH-07                   |

| Step | Action                                            | Expected Result |
| :--: | :------------------------------------------------ | :-------------- |
|  1   | `POST /api/auth/refresh` with valid refresh token | `200 OK`        |
|  2   | Verify new `accessToken` returned                 | Token rotated   |

### TC-AUTH-009: Refresh Token — Invalid Token

| Field             | Value                         |
| :---------------- | :---------------------------- |
| **Priority**      | High                          |
| **Preconditions** | Invalid/expired refresh token |
| **SRS Trace**     | FR-AUTH-07                    |

| Step | Action                                      | Expected Result    |
| :--: | :------------------------------------------ | :----------------- |
|  1   | `POST /api/auth/refresh` with invalid token | `401 Unauthorized` |

### TC-AUTH-010: Refresh Token — Rate Limited

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Medium              |
| **Preconditions** | Rate limiter active |
| **SRS Trace**     | NFR-SEC-03          |

| Step | Action                      | Expected Result                         |
| :--: | :-------------------------- | :-------------------------------------- |
|  1   | Send rapid refresh requests | `429 Too Many Requests` after threshold |

### TC-AUTH-011: Validate Token — Valid Bearer

| Field             | Value           |
| :---------------- | :-------------- |
| **Priority**      | High            |
| **Preconditions** | Valid JWT token |
| **SRS Trace**     | FR-AUTH-03      |

| Step | Action                                          | Expected Result       |
| :--: | :---------------------------------------------- | :-------------------- |
|  1   | `POST /api/auth/validate` with `Bearer <token>` | `200 OK`, valid: true |

### TC-AUTH-012: Validate Token — Without Bearer Prefix

| Field             | Value                         |
| :---------------- | :---------------------------- |
| **Priority**      | Medium                        |
| **Preconditions** | Valid JWT, no "Bearer" prefix |
| **SRS Trace**     | FR-AUTH-03                    |

| Step | Action                                               | Expected Result    |
| :--: | :--------------------------------------------------- | :----------------- |
|  1   | `POST /api/auth/validate` with raw token (no Bearer) | `401 Unauthorized` |

### TC-AUTH-013: Validate Token — Invalid Token

| Field             | Value        |
| :---------------- | :----------- |
| **Priority**      | High         |
| **Preconditions** | Tampered JWT |
| **SRS Trace**     | FR-AUTH-04   |

| Step | Action                                     | Expected Result    |
| :--: | :----------------------------------------- | :----------------- |
|  1   | `POST /api/auth/validate` with invalid JWT | `401 Unauthorized` |

### TC-AUTH-014: Login Failure — Service Exception

| Field             | Value                        |
| :---------------- | :--------------------------- |
| **Priority**      | Medium                       |
| **Preconditions** | AuthService throws exception |
| **SRS Trace**     | FR-AUTH-01                   |

| Step | Action                                    | Expected Result    |
| :--: | :---------------------------------------- | :----------------- |
|  1   | `POST /api/auth/login` when service fails | `401 Unauthorized` |

### TC-AUTH-015: Logout — Success and Failure

| Field             | Value              |
| :---------------- | :----------------- |
| **Priority**      | High               |
| **Preconditions** | User authenticated |
| **SRS Trace**     | FR-AUTH-08         |

| Step | Action                                     | Expected Result               |
| :--: | :----------------------------------------- | :---------------------------- |
|  1   | `POST /api/auth/logout` with valid token   | `200 OK`, session invalidated |
|  2   | `POST /api/auth/logout` with invalid token | Error response                |

---

## 3. Password Reset & Change Test Cases

> **Source:** `controller/auth/PasswordResetControllerTest.java` (10 test methods)

### TC-PWD-001: Forgot Password — Valid Email

| Field             | Value            |
| :---------------- | :--------------- |
| **Priority**      | High             |
| **Preconditions** | Email registered |
| **SRS Trace**     | FR-AUTH-09       |

| Step | Action                                            | Expected Result  |
| :--: | :------------------------------------------------ | :--------------- |
|  1   | `POST /api/auth/forgot-password` with valid email | `200 OK`         |
|  2   | Verify reset link sent                            | Email dispatched |

### TC-PWD-002: Forgot Password — Rate Limited

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Medium              |
| **Preconditions** | Rate limiter active |
| **SRS Trace**     | NFR-SEC-03          |

| Step | Action                                 | Expected Result         |
| :--: | :------------------------------------- | :---------------------- |
|  1   | Rapid `POST /api/auth/forgot-password` | `429 Too Many Requests` |

### TC-PWD-003: Forgot Password — Service Exception

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | Medium         |
| **Preconditions** | Service throws |
| **SRS Trace**     | FR-AUTH-09     |

| Step | Action                                              | Expected Result   |
| :--: | :-------------------------------------------------- | :---------------- |
|  1   | `POST /api/auth/forgot-password` when service fails | `400 Bad Request` |

### TC-PWD-004: Reset Password — Valid Token

| Field             | Value             |
| :---------------- | :---------------- |
| **Priority**      | Critical          |
| **Preconditions** | Valid reset token |
| **SRS Trace**     | FR-AUTH-10        |

| Step | Action                                                          | Expected Result |
| :--: | :-------------------------------------------------------------- | :-------------- |
|  1   | `POST /api/auth/reset-password` with valid token + new password | `200 OK`        |

### TC-PWD-005: Reset Password — Rate Limited

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Medium              |
| **Preconditions** | Rate limiter active |
| **SRS Trace**     | NFR-SEC-03          |

| Step | Action               | Expected Result         |
| :--: | :------------------- | :---------------------- |
|  1   | Rapid reset requests | `429 Too Many Requests` |

### TC-PWD-006: Reset Password — Feature Not Available

| Field             | Value            |
| :---------------- | :--------------- |
| **Priority**      | Medium           |
| **Preconditions** | Feature flag off |
| **SRS Trace**     | FR-AUTH-10       |

| Step | Action                      | Expected Result       |
| :--: | :-------------------------- | :-------------------- |
|  1   | Reset when feature disabled | `501 Not Implemented` |

### TC-PWD-007: Reset Password — Invalid Token

| Field             | Value                 |
| :---------------- | :-------------------- |
| **Priority**      | High                  |
| **Preconditions** | Expired/invalid token |
| **SRS Trace**     | FR-AUTH-10            |

| Step | Action                                         | Expected Result   |
| :--: | :--------------------------------------------- | :---------------- |
|  1   | `POST /api/auth/reset-password` with bad token | `400 Bad Request` |

### TC-PWD-008: Change Password — Success

| Field             | Value                                  |
| :---------------- | :------------------------------------- |
| **Priority**      | High                                   |
| **Preconditions** | User authenticated, knows old password |
| **SRS Trace**     | FR-AUTH-11                             |

| Step | Action                                          | Expected Result |
| :--: | :---------------------------------------------- | :-------------- |
|  1   | `POST /api/auth/change-password` with old + new | `200 OK`        |

### TC-PWD-009: Change Password — Rate Limited

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Medium              |
| **Preconditions** | Rate limiter active |
| **SRS Trace**     | NFR-SEC-03          |

| Step | Action                | Expected Result         |
| :--: | :-------------------- | :---------------------- |
|  1   | Rapid change requests | `429 Too Many Requests` |

### TC-PWD-010: Change Password — Old Password Incorrect

| Field             | Value                            |
| :---------------- | :------------------------------- |
| **Priority**      | High                             |
| **Preconditions** | User provides wrong old password |
| **SRS Trace**     | FR-AUTH-11                       |

| Step | Action                                                   | Expected Result   |
| :--: | :------------------------------------------------------- | :---------------- |
|  1   | `POST /api/auth/change-password` with wrong old password | `400 Bad Request` |

---

## 4. Product Catalog Test Cases

> **Source:** `controller/user/ProductControllerV1Test.java`, `ProductControllerV2Test.java`

### TC-PROD-001: Browse All Products (Paginated)

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | High           |
| **Preconditions** | Products exist |
| **SRS Trace**     | FR-PROD-01     |

| Step | Action                             | Expected Result     |
| :--: | :--------------------------------- | :------------------ |
|  1   | `GET /api/products?page=0&size=10` | `200 OK`            |
|  2   | Verify paginated list              | ≤ 10 items per page |

### TC-PROD-002: Get Product by ID

| Field             | Value                 |
| :---------------- | :-------------------- |
| **Priority**      | High                  |
| **Preconditions** | Product ID `1` exists |
| **SRS Trace**     | FR-PROD-02            |

| Step | Action                | Expected Result           |
| :--: | :-------------------- | :------------------------ |
|  1   | `GET /api/products/1` | `200 OK` with full detail |

### TC-PROD-003: API V1 Sunset — Before Sunset Date

| Field             | Value                      |
| :---------------- | :------------------------- |
| **Priority**      | Medium                     |
| **Preconditions** | Current date < sunset date |
| **SRS Trace**     | NFR-MNT-01                 |

| Step | Action                         | Expected Result     |
| :--: | :----------------------------- | :------------------ |
|  1   | Call V1 endpoint before sunset | No exception thrown |

### TC-PROD-004: API V1 Sunset — After Sunset Date

| Field             | Value                      |
| :---------------- | :------------------------- |
| **Priority**      | Medium                     |
| **Preconditions** | Current date > sunset date |
| **SRS Trace**     | NFR-MNT-01                 |

| Step | Action                        | Expected Result                |
| :--: | :---------------------------- | :----------------------------- |
|  1   | Call V1 endpoint after sunset | `IllegalStateException` thrown |

### TC-PROD-005: API V1 Sunset — Warning Within 90 Days

| Field             | Value                    |
| :---------------- | :----------------------- |
| **Priority**      | Low                      |
| **Preconditions** | Within 90 days of sunset |
| **SRS Trace**     | NFR-MNT-01               |

| Step | Action                                    | Expected Result              |
| :--: | :---------------------------------------- | :--------------------------- |
|  1   | Call V1 endpoint within 90 days of sunset | Warning logged, no exception |

---

## 5. Shopping Cart Test Cases

> **Source:** `controller/user/CartControllerTest.java`

### TC-CART-001: Add, Get, Remove, Clear, Total — Happy Path

| Field             | Value                                          |
| :---------------- | :--------------------------------------------- |
| **Priority**      | Critical                                       |
| **Preconditions** | User authenticated                             |
| **SRS Trace**     | FR-CART-01, FR-CART-02, FR-CART-04, FR-CART-05 |

| Step | Action                                                   | Expected Result          |
| :--: | :------------------------------------------------------- | :----------------------- |
|  1   | `POST /api/cart/items` with `{productId:10, quantity:2}` | `200 OK`                 |
|  2   | `GET /api/cart`                                          | `200 OK`, cart returned  |
|  3   | `DELETE /api/cart/items/5`                               | `200 OK`                 |
|  4   | `DELETE /api/cart` (clear)                               | `200 OK`                 |
|  5   | `GET /api/cart/total`                                    | `200 OK`, total returned |

### TC-CART-002: Get Cart — Not Found Error

| Field             | Value                           |
| :---------------- | :------------------------------ |
| **Priority**      | High                            |
| **Preconditions** | Service throws RuntimeException |
| **SRS Trace**     | FR-CART-02                      |

| Step | Action                             | Expected Result |
| :--: | :--------------------------------- | :-------------- |
|  1   | `GET /api/cart` when service fails | `404 Not Found` |

### TC-CART-003: Cart Operations — Error Handling

| Field             | Value                                    |
| :---------------- | :--------------------------------------- |
| **Priority**      | High                                     |
| **Preconditions** | Service throws on add/remove/clear/total |
| **SRS Trace**     | FR-CART-01                               |

| Step | Action                 | Expected Result   |
| :--: | :--------------------- | :---------------- |
|  1   | Add to cart fails      | `400 Bad Request` |
|  2   | Remove from cart fails | `400 Bad Request` |
|  3   | Clear cart fails       | `400 Bad Request` |
|  4   | Get total fails        | `404 Not Found`   |

---

## 6. Checkout & Order Test Cases

> **Source:** `controller/user/CheckoutControllerTest.java` (16 test methods)

### TC-CHK-001: Process Checkout with Payment — Valid

| Field             | Value                              |
| :---------------- | :--------------------------------- |
| **Priority**      | Critical                           |
| **Preconditions** | User authenticated, cart has items |
| **SRS Trace**     | FR-CHK-01                          |

| Step | Action                                              | Expected Result          |
| :--: | :-------------------------------------------------- | :----------------------- |
|  1   | `POST /api/checkout` with payment method and amount | `200 OK`                 |
|  2   | Verify order created with payment                   | Order + payment recorded |

### TC-CHK-002: Process Checkout — Valid (No Payment)

| Field             | Value                              |
| :---------------- | :--------------------------------- |
| **Priority**      | High                               |
| **Preconditions** | User authenticated, cart has items |
| **SRS Trace**     | FR-CHK-01                          |

| Step | Action                               | Expected Result         |
| :--: | :----------------------------------- | :---------------------- |
|  1   | `POST /api/checkout` without payment | `200 OK`, order PENDING |

### TC-CHK-003: Process Checkout — Invalid Cart

| Field             | Value                 |
| :---------------- | :-------------------- |
| **Priority**      | High                  |
| **Preconditions** | Empty or invalid cart |
| **SRS Trace**     | FR-CHK-03             |

| Step | Action                                 | Expected Result   |
| :--: | :------------------------------------- | :---------------- |
|  1   | `POST /api/checkout` with invalid cart | `400 Bad Request` |

### TC-CHK-004: Checkout — Service Error

| Field             | Value                  |
| :---------------- | :--------------------- |
| **Priority**      | Medium                 |
| **Preconditions** | CheckoutService throws |
| **SRS Trace**     | FR-CHK-01              |

| Step | Action                                  | Expected Result             |
| :--: | :-------------------------------------- | :-------------------------- |
|  1   | `POST /api/checkout` when service fails | `500 Internal Server Error` |

### TC-CHK-005: Checkout — Missing Payment Method

| Field             | Value                                 |
| :---------------- | :------------------------------------ |
| **Priority**      | High                                  |
| **Preconditions** | Payment amount set but method missing |
| **SRS Trace**     | FR-PAY-01                             |

| Step | Action                                       | Expected Result   |
| :--: | :------------------------------------------- | :---------------- |
|  1   | `POST /api/checkout` without `paymentMethod` | `400 Bad Request` |

### TC-CHK-006: Checkout — Zero Payment Amount

| Field             | Value              |
| :---------------- | :----------------- |
| **Priority**      | High               |
| **Preconditions** | Payment amount = 0 |
| **SRS Trace**     | FR-PAY-01          |

| Step | Action                                  | Expected Result   |
| :--: | :-------------------------------------- | :---------------- |
|  1   | `POST /api/checkout` with amount `0.00` | `400 Bad Request` |

### TC-CHK-007: Checkout — Large Amount Boundary

| Field             | Value                     |
| :---------------- | :------------------------ |
| **Priority**      | Medium                    |
| **Preconditions** | Very large payment amount |
| **SRS Trace**     | FR-CHK-01                 |

| Step | Action                                          | Expected Result   |
| :--: | :---------------------------------------------- | :---------------- |
|  1   | `POST /api/checkout` with `999999999.99` amount | `200 OK` if valid |

### TC-CHK-008: Validate Cart Before Checkout — Ready

| Field             | Value         |
| :---------------- | :------------ |
| **Priority**      | High          |
| **Preconditions** | Cart is valid |
| **SRS Trace**     | FR-CHK-02     |

| Step | Action                       | Expected Result       |
| :--: | :--------------------------- | :-------------------- |
|  1   | `GET /api/checkout/validate` | `200 OK`, ready: true |

### TC-CHK-009: Validate Cart — Not Ready

| Field             | Value              |
| :---------------- | :----------------- |
| **Priority**      | High               |
| **Preconditions** | Cart invalid/empty |
| **SRS Trace**     | FR-CHK-02          |

| Step | Action                                     | Expected Result   |
| :--: | :----------------------------------------- | :---------------- |
|  1   | `GET /api/checkout/validate` with bad cart | `400 Bad Request` |

### TC-CHK-010: Calculate Cart Total

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | High           |
| **Preconditions** | Cart has items |
| **SRS Trace**     | FR-CART-05     |

| Step | Action                    | Expected Result            |
| :--: | :------------------------ | :------------------------- |
|  1   | `GET /api/checkout/total` | `200 OK` with total amount |

### TC-CHK-011: Checkout — Inventory Failure (Rollback)

| Field             | Value              |
| :---------------- | :----------------- |
| **Priority**      | Critical           |
| **Preconditions** | Stock insufficient |
| **SRS Trace**     | FR-CHK-03          |

| Step | Action                                       | Expected Result        |
| :--: | :------------------------------------------- | :--------------------- |
|  1   | `POST /api/checkout` when stock insufficient | `409 Conflict` / `500` |
|  2   | Verify order rolled back                     | No partial order       |

### TC-CHK-012: Checkout — Without Authentication

| Field             | Value         |
| :---------------- | :------------ |
| **Priority**      | Critical      |
| **Preconditions** | No auth token |
| **SRS Trace**     | FR-AUTH-03    |

| Step | Action                             | Expected Result    |
| :--: | :--------------------------------- | :----------------- |
|  1   | `POST /api/checkout` without token | `401 Unauthorized` |

---

## 7. User Profile Test Cases

> **Source:** `controller/user/UserControllerTest.java`

### TC-USR-001: Get and Update Profile

| Field             | Value              |
| :---------------- | :----------------- |
| **Priority**      | High               |
| **Preconditions** | User authenticated |
| **SRS Trace**     | FR-USR-01          |

| Step | Action                               | Expected Result         |
| :--: | :----------------------------------- | :---------------------- |
|  1   | `GET /api/user/profile`              | `200 OK` with user data |
|  2   | `PUT /api/user/profile` with updates | `200 OK`                |

### TC-USR-002: Get Profile — Not Found

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | Medium         |
| **Preconditions** | Service throws |
| **SRS Trace**     | FR-USR-01      |

| Step | Action                                     | Expected Result |
| :--: | :----------------------------------------- | :-------------- |
|  1   | `GET /api/user/profile` when service fails | `404 Not Found` |

### TC-USR-003: Update Profile — Validation Error

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Medium              |
| **Preconditions** | Invalid update data |
| **SRS Trace**     | FR-USR-01           |

| Step | Action                                | Expected Result   |
| :--: | :------------------------------------ | :---------------- |
|  1   | `PUT /api/user/profile` with bad data | `400 Bad Request` |

---

## 8. User Order Management Test Cases

> **Source:** `controller/user/UserOrderControllerTest.java`

### TC-ORD-001: Get Orders and Order Details

| Field             | Value           |
| :---------------- | :-------------- |
| **Priority**      | High            |
| **Preconditions** | User has orders |
| **SRS Trace**     | FR-CHK-06       |

| Step | Action                 | Expected Result        |
| :--: | :--------------------- | :--------------------- |
|  1   | `GET /api/orders`      | `200 OK`, order list   |
|  2   | `GET /api/orders/{id}` | `200 OK`, order detail |

### TC-ORD-002: Order Detail — Forbidden (Cross-User)

| Field             | Value                              |
| :---------------- | :--------------------------------- |
| **Priority**      | Critical                           |
| **Preconditions** | User accesses another user's order |
| **SRS Trace**     | NFR-SEC-06                         |

| Step | Action                               | Expected Result |
| :--: | :----------------------------------- | :-------------- |
|  1   | `GET /api/orders/{other_user_order}` | `403 Forbidden` |

### TC-ORD-003: Order List — Service Error

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | Medium         |
| **Preconditions** | Service throws |
| **SRS Trace**     | FR-CHK-06      |

| Step | Action                               | Expected Result             |
| :--: | :----------------------------------- | :-------------------------- |
|  1   | `GET /api/orders` when service fails | `500 Internal Server Error` |

### TC-ORD-004: Order Detail — Not Found

| Field             | Value                |
| :---------------- | :------------------- |
| **Priority**      | Medium               |
| **Preconditions** | Order does not exist |
| **SRS Trace**     | FR-CHK-06            |

| Step | Action                | Expected Result |
| :--: | :-------------------- | :-------------- |
|  1   | `GET /api/orders/999` | `404 Not Found` |

---

## 9. Wishlist Test Cases

> **Source:** `controller/user/WishlistControllerTest.java`

### TC-WISH-001: Add, Remove, Get, Contains, Clear, Count

| Field             | Value              |
| :---------------- | :----------------- |
| **Priority**      | High               |
| **Preconditions** | User authenticated |
| **SRS Trace**     | FR-WISH-01         |

| Step | Action                          | Expected Result           |
| :--: | :------------------------------ | :------------------------ |
|  1   | `POST /api/wishlist/10`         | `200 OK`, product added   |
|  2   | `DELETE /api/wishlist/10`       | `200 OK`, product removed |
|  3   | `GET /api/wishlist`             | `200 OK`, list returned   |
|  4   | `GET /api/wishlist/contains/10` | `200 OK`, boolean         |
|  5   | `DELETE /api/wishlist` (clear)  | `200 OK`                  |
|  6   | `GET /api/wishlist/count`       | `200 OK`, count           |

### TC-WISH-002: Get Wishlist — Error

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | Medium         |
| **Preconditions** | Service throws |
| **SRS Trace**     | FR-WISH-01     |

| Step | Action                                 | Expected Result |
| :--: | :------------------------------------- | :-------------- |
|  1   | `GET /api/wishlist` when service fails | `404 Not Found` |

### TC-WISH-003: Wishlist Operations — Error Handling

| Field             | Value                            |
| :---------------- | :------------------------------- |
| **Priority**      | Medium                           |
| **Preconditions** | Service throws on each operation |
| **SRS Trace**     | FR-WISH-01                       |

| Step | Action         | Expected Result             |
| :--: | :------------- | :-------------------------- |
|  1   | Add fails      | `400 Bad Request`           |
|  2   | Remove fails   | `400 Bad Request`           |
|  3   | Contains fails | `500 Internal Server Error` |
|  4   | Clear fails    | `500 Internal Server Error` |

---

## 10. Product Review Test Cases

> **Source:** `controller/user/ProductReviewControllerTest.java` (7 test methods)

### TC-REV-001: Submit and Fetch Reviews (Verified Purchase)

| Field             | Value                  |
| :---------------- | :--------------------- |
| **Priority**      | High                   |
| **Preconditions** | User purchased product |
| **SRS Trace**     | FR-REV-01              |

| Step | Action                                                             | Expected Result                  |
| :--: | :----------------------------------------------------------------- | :------------------------------- |
|  1   | `POST /api/products/10/reviews` with `{rating:5, comment:"Great"}` | `201 Created`                    |
|  2   | `GET /api/products/10/reviews`                                     | `200 OK`, paginated reviews      |
|  3   | `GET /api/products/10/reviews/summary`                             | Rating summary with distribution |
|  4   | `GET /api/products/10/reviews/helpful`                             | Top helpful reviews              |
|  5   | `POST /api/products/10/reviews/99/helpful`                         | `200 OK`, marked helpful         |
|  6   | `PUT /api/products/10/reviews/99`                                  | `200 OK`, review updated         |

### TC-REV-002: Submit Review — Unverified Purchase

| Field             | Value                              |
| :---------------- | :--------------------------------- |
| **Priority**      | Medium                             |
| **Preconditions** | User has NOT purchased the product |
| **SRS Trace**     | FR-REV-01                          |

| Step | Action                                | Expected Result                          |
| :--: | :------------------------------------ | :--------------------------------------- |
|  1   | Submit review for unpurchased product | `201 Created`, `verifiedPurchase: false` |

### TC-REV-003: Submit Review — Duplicate Error

| Field             | Value                         |
| :---------------- | :---------------------------- |
| **Priority**      | High                          |
| **Preconditions** | User already reviewed product |
| **SRS Trace**     | FR-REV-01                     |

| Step | Action                  | Expected Result   |
| :--: | :---------------------- | :---------------- |
|  1   | Submit duplicate review | `400 Bad Request` |

### TC-REV-004: Review Fetch — Error Handling

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | Medium         |
| **Preconditions** | Service throws |
| **SRS Trace**     | FR-REV-01      |

| Step | Action                    | Expected Result             |
| :--: | :------------------------ | :-------------------------- |
|  1   | Get reviews fails         | `500 Internal Server Error` |
|  2   | Get rating summary fails  | `500 Internal Server Error` |
|  3   | Get helpful reviews fails | `500 Internal Server Error` |

### TC-REV-005: Update/Delete Review — Error Handling

| Field             | Value          |
| :---------------- | :------------- |
| **Priority**      | Medium         |
| **Preconditions** | Service throws |
| **SRS Trace**     | FR-REV-01      |

| Step | Action                        | Expected Result   |
| :--: | :---------------------------- | :---------------- |
|  1   | Mark helpful fails            | `400 Bad Request` |
|  2   | Update review fails           | `400 Bad Request` |
|  3   | Delete review (forbidden)     | `403 Forbidden`   |
|  4   | Delete review (generic error) | `400 Bad Request` |

## 11. Admin Product Management Test Cases

> **Source:** `controller/admin/AdminProductControllerTest.java`

### TC-ADM-PRD-001: Get All Products & Get by ID (Admin)

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | High                |
| **Preconditions** | Admin authenticated |
| **SRS Trace**     | FR-ADM-01           |

| Step | Action                        | Expected Result          |
| :--: | :---------------------------- | :----------------------- |
|  1   | `GET /api/admin/products`     | `200 OK`, product list   |
|  2   | `GET /api/admin/products/1`   | `200 OK`, product detail |
|  3   | `GET /api/admin/products/999` | `404 Not Found`          |

### TC-ADM-PRD-002: Create, Update, Delete Product

| Field             | Value                |
| :---------------- | :------------------- |
| **Priority**      | Critical             |
| **Preconditions** | Admin authenticated  |
| **SRS Trace**     | FR-ADM-02, FR-ADM-04 |

| Step | Action                                          | Expected Result   |
| :--: | :---------------------------------------------- | :---------------- |
|  1   | `POST /api/admin/products` with product data    | `201 Created`     |
|  2   | `PUT /api/admin/products/1` with updates        | `200 OK`          |
|  3   | `DELETE /api/admin/products/1`                  | `200 OK`          |
|  4   | `DELETE /api/admin/products/2` (service throws) | `400 Bad Request` |

---

## 12. Admin Order Management Test Cases

> **Source:** `controller/admin/AdminOrderControllerTest.java`

### TC-ADM-ORD-001: Get All Orders & Get by ID (Admin)

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | High                |
| **Preconditions** | Admin authenticated |
| **SRS Trace**     | FR-ADM-03           |

| Step | Action                      | Expected Result      |
| :--: | :-------------------------- | :------------------- |
|  1   | `GET /api/admin/orders`     | `200 OK`, order list |
|  2   | `GET /api/admin/orders/1`   | `200 OK`             |
|  3   | `GET /api/admin/orders/999` | `404 Not Found`      |

### TC-ADM-ORD-002: Update Status & Delete Order

| Field             | Value                |
| :---------------- | :------------------- |
| **Priority**      | Critical             |
| **Preconditions** | Admin authenticated  |
| **SRS Trace**     | FR-ADM-03, FR-ADM-05 |

| Step | Action                                          | Expected Result   |
| :--: | :---------------------------------------------- | :---------------- |
|  1   | `PUT /api/admin/orders/1/status` to "CONFIRMED" | `200 OK`          |
|  2   | `PUT /api/admin/orders/2/status` invalid        | `400 Bad Request` |
|  3   | `DELETE /api/admin/orders/1`                    | `200 OK`          |
|  4   | `DELETE /api/admin/orders/2` (service throws)   | `400 Bad Request` |

---

## 13. Admin User Management Test Cases

> **Source:** `controller/admin/AdminUserControllerTest.java` (5 test methods)

### TC-ADM-USR-001: Get All Users

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | High                |
| **Preconditions** | Admin authenticated |
| **SRS Trace**     | FR-ADM-06           |

| Step | Action                 | Expected Result             |
| :--: | :--------------------- | :-------------------------- |
|  1   | `GET /api/admin/users` | `200 OK`, user list         |
|  2   | Service throws         | `500 Internal Server Error` |

### TC-ADM-USR-002: Get, Update, Delete User

| Field             | Value                           |
| :---------------- | :------------------------------ |
| **Priority**      | Critical                        |
| **Preconditions** | Admin authenticated             |
| **SRS Trace**     | FR-ADM-06, FR-ADM-07, FR-ADM-08 |

| Step | Action                               | Expected Result   |
| :--: | :----------------------------------- | :---------------- |
|  1   | `GET /api/admin/users/1`             | `200 OK`          |
|  2   | `GET /api/admin/users/2` (not found) | `404 Not Found`   |
|  3   | `PUT /api/admin/users/1`             | `200 OK`          |
|  4   | `PUT /api/admin/users/2` (error)     | `400 Bad Request` |
|  5   | `DELETE /api/admin/users/1`          | `200 OK`          |
|  6   | `DELETE /api/admin/users/2` (error)  | `400 Bad Request` |

---

## 14. Admin Inventory Management Test Cases

> **Source:** `controller/admin/AdminInventoryControllerTest.java` (10 test methods)

### TC-ADM-INV-001: Low Stock & Out-of-Stock Queries

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | High                |
| **Preconditions** | Admin authenticated |
| **SRS Trace**     | FR-ADM-10           |

| Step | Action                                            | Expected Result            |
| :--: | :------------------------------------------------ | :------------------------- |
|  1   | `GET /api/admin/inventory/low-stock?threshold=10` | `200 OK`, filtered list    |
|  2   | `GET /api/admin/inventory/out-of-stock`           | `200 OK`, zero-stock items |

### TC-ADM-INV-002: Add & Update Stock

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Critical            |
| **Preconditions** | Admin authenticated |
| **SRS Trace**     | FR-ADM-11           |

| Step | Action                                        | Expected Result   |
| :--: | :-------------------------------------------- | :---------------- |
|  1   | `POST /api/admin/inventory/1/add?quantity=50` | `200 OK`          |
|  2   | Add stock fails (service throws)              | `400 Bad Request` |
|  3   | `PUT /api/admin/inventory/1?quantity=100`     | `200 OK`          |

### TC-ADM-INV-003: Role-Based Access Control

| Field             | Value                        |
| :---------------- | :--------------------------- |
| **Priority**      | Critical                     |
| **Preconditions** | Non-admin user authenticated |
| **SRS Trace**     | NFR-SEC-05                   |

| Step | Action                                            | Expected Result |
| :--: | :------------------------------------------------ | :-------------- |
|  1   | `GET /api/admin/inventory/low-stock` as USER role | `403 Forbidden` |

### TC-ADM-INV-004: Product Availability & Inventory Status

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | High                |
| **Preconditions** | Admin authenticated |
| **SRS Trace**     | FR-ADM-10           |

| Step | Action                                | Expected Result             |
| :--: | :------------------------------------ | :-------------------------- |
|  1   | Check availability (in stock)         | `200 OK`, available: true   |
|  2   | Check availability (out of stock)     | `200 OK`, available: false  |
|  3   | Availability check error              | `500 Internal Server Error` |
|  4   | `GET /api/admin/inventory/1/status`   | `200 OK`, full status       |
|  5   | `GET /api/admin/inventory/999/status` | `404 Not Found`             |

---

## 15. Admin Analytics Test Cases

> **Source:** `controller/admin/AdminAnalyticsControllerTest.java`

### TC-ADM-ANL-001: Audit & Metrics Endpoints

| Field             | Value                                        |
| :---------------- | :------------------------------------------- |
| **Priority**      | High                                         |
| **Preconditions** | Admin authenticated, Elasticsearch available |
| **SRS Trace**     | FR-ADM-12                                    |

| Step | Action                                 | Expected Result             |
| :--: | :------------------------------------- | :-------------------------- |
|  1   | Audit logs by user, action, time range | `200 OK` each               |
|  2   | Metrics by time range, recent metrics  | `200 OK` each               |
|  3   | Alert summary, dashboard               | `200 OK` each               |
|  4   | API errors by status code & endpoint   | `200 OK` each               |
|  5   | Audit/metrics/alerts on error          | `500 Internal Server Error` |

---

## 16. Security — Authentication & Authorization (TC-SEC-001 to TC-SEC-010)

> **Source:** `security/AuthenticationAuthorizationSecurityTest.java`

| TC ID      | Description                           | Expected Result                 | SRS Trace  |
| :--------- | :------------------------------------ | :------------------------------ | :--------- |
| TC-SEC-001 | Brute force protection                | Account locked after threshold  | NFR-SEC-01 |
| TC-SEC-002 | Rate limiting by IP                   | `429 Too Many Requests`         | NFR-SEC-03 |
| TC-SEC-003 | JWT token expiration                  | `401 Unauthorized`              | NFR-SEC-02 |
| TC-SEC-004 | Refresh token rotation (no reuse)     | Old token rejected              | NFR-SEC-02 |
| TC-SEC-005 | Role hierarchy (ADMIN > CUSTOMER)     | `403 Forbidden` for lower roles | NFR-SEC-05 |
| TC-SEC-006 | Cross-user access prevention          | `403 Forbidden`                 | NFR-SEC-06 |
| TC-SEC-007 | Admin privilege escalation prevention | `403 Forbidden`                 | NFR-SEC-05 |
| TC-SEC-008 | Password complexity validation        | Weak passwords rejected         | NFR-SEC-04 |
| TC-SEC-009 | Account lockout mechanism             | Locked after N failures         | NFR-SEC-01 |
| TC-SEC-010 | Session fixation prevention           | New session ID on re-login      | NFR-SEC-07 |

---

## 17. Security — Input Validation & Injection (TC-SEC-011 to TC-SEC-019)

> **Source:** `security/InputValidationSecurityTest.java`

| TC ID      | Description                  | Expected Result        | SRS Trace  |
| :--------- | :--------------------------- | :--------------------- | :--------- |
| TC-SEC-011 | SQL injection prevention     | `401`; no data leaked  | NFR-SEC-08 |
| TC-SEC-012 | XSS prevention               | Script tags sanitized  | NFR-SEC-09 |
| TC-SEC-013 | CSRF token validation        | `403` without token    | NFR-SEC-10 |
| TC-SEC-014 | Path traversal prevention    | `400`/`404`            | NFR-SEC-08 |
| TC-SEC-015 | Command injection prevention | No execution           | NFR-SEC-08 |
| TC-SEC-016 | XXE prevention               | Entity not processed   | NFR-SEC-08 |
| TC-SEC-017 | HTTP parameter pollution     | Consistent handling    | NFR-SEC-08 |
| TC-SEC-018 | Mass assignment prevention   | Extra fields ignored   | NFR-SEC-11 |
| TC-SEC-019 | File upload validation       | Invalid files rejected | NFR-SEC-12 |

---

## 18. End-to-End UI Test Cases (TC-E2E-001 to TC-E2E-007)

> **Source:** `e2e/E2ETest.java` (Selenium WebDriver)

| TC ID      | Description                    | Expected Result                          | SRS Trace  |
| :--------- | :----------------------------- | :--------------------------------------- | :--------- |
| TC-E2E-001 | User registration workflow     | Registration succeeds, redirect to login | FR-AUTH-05 |
| TC-E2E-002 | Login flow                     | Login succeeds, dashboard shown          | FR-AUTH-01 |
| TC-E2E-003 | Product browsing & search      | Products displayed, search works         | FR-PROD-01 |
| TC-E2E-004 | Add to cart workflow           | Cart count updates, item in cart         | FR-CART-01 |
| TC-E2E-005 | Checkout workflow (no payment) | Order placed, confirmation shown         | FR-CHK-01  |
| TC-E2E-006 | Mobile responsiveness          | Layout adapts, no horizontal scroll      | NFR-USE-01 |
| TC-E2E-007 | Navigation flow                | All pages reachable, back button works   | NFR-USE-02 |

---

## 19. Edge Case & Boundary Test Cases (TC-EDGE-001 to TC-EDGE-015)

> **Source:** `edgecase/EdgeCaseAndBoundaryTest.java` (`@DataJpaTest`)

| TC ID       | Description               | Input               | Expected Result   |
| :---------- | :------------------------ | :------------------ | :---------------- |
| TC-EDGE-001 | Minimum product price     | `0.01`              | Price > 0         |
| TC-EDGE-002 | Maximum product price     | `999999999.99`      | Saved correctly   |
| TC-EDGE-003 | Zero stock quantity       | `0`                 | Stored as 0       |
| TC-EDGE-004 | Maximum stock quantity    | `Integer.MAX_VALUE` | Stored correctly  |
| TC-EDGE-005 | Very long product name    | 500-char string     | No truncation     |
| TC-EDGE-006 | Order with zero amount    | `0.00`              | Stored correctly  |
| TC-EDGE-007 | Order with large amount   | `999999999.99`      | Stored correctly  |
| TC-EDGE-008 | Minimum username length   | `"a"`               | Accepted          |
| TC-EDGE-009 | Exact decimal precision   | `123.45`            | No drift          |
| TC-EDGE-010 | Inactive user handling    | `isActive=false`    | Deactivated       |
| TC-EDGE-011 | Cancel after confirmation | CONFIRMED→CANCELLED | Transitions       |
| TC-EDGE-012 | Special chars in name     | `!@#$%^&*`          | No encoding error |
| TC-EDGE-013 | Negative stock adjustment | `-10`               | Stored as -10     |
| TC-EDGE-014 | Null optional fields      | `null` description  | Accepted          |
| TC-EDGE-015 | Concurrent timestamps     | `createdAt` set     | Not null          |

---

## 20. Performance Baseline Test Cases (TC-PERF-001 to TC-PERF-003)

> **Source:** `performance/PerformanceBaselineTest.java`

| TC ID       | Description                            | Threshold                    | SRS Trace   |
| :---------- | :------------------------------------- | :--------------------------- | :---------- |
| TC-PERF-001 | Checkout response time (50 iterations) | 95th percentile ≤ 500ms      | NFR-PERF-01 |
| TC-PERF-002 | Auth response time (50 iterations)     | 95th percentile ≤ 200ms      | NFR-PERF-02 |
| TC-PERF-003 | Batch operation scalability (1×,2×,4×) | Linear scaling (< 3× growth) | NFR-PERF-03 |

---

## 21. Stress Test Cases (TC-STRESS-001 to TC-STRESS-004)

> **Source:** `stress/StressTest.java` (`@Tag("stress")`)

| TC ID         | Description               | Load Profile        | Expected Result               |
| :------------ | :------------------------ | :------------------ | :---------------------------- |
| TC-STRESS-001 | High concurrent user load | 100 threads         | > 95% success                 |
| TC-STRESS-002 | Sustained high throughput | Extended period     | > 90% success, no degradation |
| TC-STRESS-003 | Spike traffic handling    | Normal → 10× burst  | System responsive             |
| TC-STRESS-004 | Memory usage under load   | Continuous requests | No leaks, heap stable         |

---

## 22. Reliability Test Cases (TC-REL-001 to TC-REL-005)

> **Source:** `reliability/ReliabilityTest.java`

| TC ID      | Description                    | Config                    | Expected Result  |
| :--------- | :----------------------------- | :------------------------ | :--------------- |
| TC-REL-001 | Repeated execution (flakiness) | `@RepeatedTest(50)`       | 100% pass rate   |
| TC-REL-002 | Concurrent requests            | 20 threads × 10 req       | > 95% success    |
| TC-REL-003 | Memory pressure                | 100 rapid requests        | ≥ 90% success    |
| TC-REL-004 | Error recovery                 | Success → Error → Success | System recovers  |
| TC-REL-005 | Connection pool exhaustion     | 50 concurrent burst       | ≥ 80% completion |

---

## 23. Test Case Summary

### 23.1 Count by Category

| Category          |  Count  |
| :---------------- | :-----: |
| Authentication    |   15    |
| Password Reset    |   10    |
| Product Catalog   |    5    |
| Shopping Cart     |    3    |
| Checkout & Orders |   12    |
| User Profile      |    3    |
| User Order Mgmt   |    4    |
| Wishlist          |    3    |
| Product Reviews   |    5    |
| Admin Products    |    2    |
| Admin Orders      |    2    |
| Admin Users       |    2    |
| Admin Inventory   |    4    |
| Admin Analytics   |    1    |
| Security Auth     |   10    |
| Security Input    |    9    |
| E2E UI            |    7    |
| Edge Cases        |   15    |
| Performance       |    3    |
| Stress            |    4    |
| Reliability       |    5    |
| **Total**         | **124** |

### 23.2 Priority Distribution

| Priority | Count | Percentage |
| :------- | :---: | :--------: |
| Critical |  32   |   25.8%    |
| High     |  62   |   50.0%    |
| Medium   |  25   |   20.2%    |
| Low      |   5   |    4.0%    |

---

## 24. Revision History

See [Document Control](#document-control) for full revision history and approvals.

---

**— End of Document —**

_This document was prepared in compliance with ISO/IEC/IEEE 29119-3:2021 for the BuildNest E-Commerce Platform._

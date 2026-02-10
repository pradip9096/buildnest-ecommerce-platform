# Test Case Specification

## BuildNest E-Commerce Platform

**Document ID:** TCS-BUILDNEST-001
**Version:** 1.0
**Date:** 2026-02-10
**Standard:** ISO/IEC/IEEE 29119-3:2021 — Software Testing — Part 3: Test Documentation
**Reference:** [Test Plan (TP-BUILDNEST-001)](Test_Plan_IEEE_29119.md)

---

## 1. Introduction

### 1.1 Purpose

This document specifies the individual **test cases** for the BuildNest E-Commerce Platform. Each test case defines the preconditions, input data, execution steps, and expected results required to verify compliance with the [SRS](SRS_IEEE_29148_2018.md).

### 1.2 Test Case Format

Each test case follows the structure:

| Field               | Description                            |
| :------------------ | :------------------------------------- |
| **TC ID**           | Unique identifier (e.g., TC-AUTH-001)  |
| **Title**           | Short description of what is tested    |
| **Priority**        | Critical / High / Medium / Low         |
| **Preconditions**   | System state required before execution |
| **Steps**           | Numbered action sequence               |
| **Expected Result** | Observable outcome on success          |
| **SRS Trace**       | Linked requirement ID                  |

---

## 2. Authentication Test Cases

### TC-AUTH-001: Successful User Login

| Field             | Value                                                                  |
| :---------------- | :--------------------------------------------------------------------- |
| **Priority**      | Critical                                                               |
| **Preconditions** | User `john.doe` exists with password `Secret@123` and role `ROLE_USER` |
| **SRS Trace**     | FR-AUTH-01                                                             |

| Step | Action                                                                             | Expected Result          |
| :--: | :--------------------------------------------------------------------------------- | :----------------------- |
|  1   | Send `POST /api/auth/login` with `{"username":"john.doe","password":"Secret@123"}` | Status `200 OK`          |
|  2   | Verify response body contains `accessToken`                                        | JWT token string present |
|  3   | Verify response body contains `refreshToken`                                       | UUID string present      |
|  4   | Verify `data.user.role` equals `ROLE_USER`                                         | Role matches             |

---

### TC-AUTH-002: Login with Invalid Credentials

| Field             | Value                  |
| :---------------- | :--------------------- |
| **Priority**      | Critical               |
| **Preconditions** | User `john.doe` exists |
| **SRS Trace**     | FR-AUTH-02             |

| Step | Action                                                                        | Expected Result           |
| :--: | :---------------------------------------------------------------------------- | :------------------------ |
|  1   | Send `POST /api/auth/login` with `{"username":"john.doe","password":"wrong"}` | Status `401 Unauthorized` |
|  2   | Verify response body `success` is `false`                                     | Error response returned   |
|  3   | Verify `message` contains "Invalid credentials"                               | User-friendly error       |

---

### TC-AUTH-003: Access Protected Endpoint Without Token

| Field             | Value                   |
| :---------------- | :---------------------- |
| **Priority**      | Critical                |
| **Preconditions** | No authentication token |
| **SRS Trace**     | FR-AUTH-03              |

| Step | Action                                                      | Expected Result           |
| :--: | :---------------------------------------------------------- | :------------------------ |
|  1   | Send `GET /api/user/profile` without `Authorization` header | Status `401 Unauthorized` |
|  2   | Verify error message indicates missing authentication       | Access denied             |

---

### TC-AUTH-004: Access with Expired JWT Token

| Field             | Value                         |
| :---------------- | :---------------------------- |
| **Priority**      | High                          |
| **Preconditions** | User has an expired JWT token |
| **SRS Trace**     | FR-AUTH-04                    |

| Step | Action                                                                             | Expected Result           |
| :--: | :--------------------------------------------------------------------------------- | :------------------------ |
|  1   | Send `GET /api/user/profile` with expired JWT in `Authorization: Bearer <expired>` | Status `401 Unauthorized` |
|  2   | Verify message contains "Token expired"                                            | Clear expiry message      |

---

### TC-AUTH-005: User Registration with Valid Data

| Field             | Value                             |
| :---------------- | :-------------------------------- |
| **Priority**      | Critical                          |
| **Preconditions** | Username `newuser` does not exist |
| **SRS Trace**     | FR-AUTH-05                        |

| Step | Action                                                                                                    | Expected Result      |
| :--: | :-------------------------------------------------------------------------------------------------------- | :------------------- |
|  1   | Send `POST /api/auth/register` with `{"username":"newuser","email":"new@test.com","password":"Pass@123"}` | Status `201 Created` |
|  2   | Verify response contains user ID                                                                          | New user created     |
|  3   | Login with new credentials                                                                                | Login succeeds       |

---

### TC-AUTH-006: Registration with Duplicate Username

| Field             | Value                              |
| :---------------- | :--------------------------------- |
| **Priority**      | High                               |
| **Preconditions** | Username `john.doe` already exists |
| **SRS Trace**     | FR-AUTH-06                         |

| Step | Action                                                            | Expected Result       |
| :--: | :---------------------------------------------------------------- | :-------------------- |
|  1   | Send `POST /api/auth/register` with `{"username":"john.doe",...}` | Status `409 Conflict` |
|  2   | Verify message indicates "User already exists"                    | Duplicate rejected    |

---

## 3. Product Catalog Test Cases

### TC-PROD-001: Browse All Products

| Field             | Value                                 |
| :---------------- | :------------------------------------ |
| **Priority**      | High                                  |
| **Preconditions** | At least 5 products exist in database |
| **SRS Trace**     | FR-PROD-01                            |

| Step | Action                                               | Expected Result                |
| :--: | :--------------------------------------------------- | :----------------------------- |
|  1   | Send `GET /api/products?page=0&size=10`              | Status `200 OK`                |
|  2   | Verify response contains paginated product list      | Products array with ≤ 10 items |
|  3   | Verify each product has `id`, `name`, `price`, `sku` | All fields present             |

---

### TC-PROD-002: Search Products by Keyword

| Field             | Value                                |
| :---------------- | :----------------------------------- |
| **Priority**      | High                                 |
| **Preconditions** | Products with "laptop" in name exist |
| **SRS Trace**     | FR-PROD-03                           |

| Step | Action                                                            | Expected Result  |
| :--: | :---------------------------------------------------------------- | :--------------- |
|  1   | Send `GET /api/products/search?q=laptop`                          | Status `200 OK`  |
|  2   | Verify all returned products contain "laptop" in name/description | Relevant results |

---

### TC-PROD-003: View Product Details

| Field             | Value                        |
| :---------------- | :--------------------------- |
| **Priority**      | High                         |
| **Preconditions** | Product with ID `101` exists |
| **SRS Trace**     | FR-PROD-02                   |

| Step | Action                                                                                | Expected Result      |
| :--: | :------------------------------------------------------------------------------------ | :------------------- |
|  1   | Send `GET /api/products/101`                                                          | Status `200 OK`      |
|  2   | Verify response includes `name`, `price`, `description`, `stock_quantity`, `category` | Full detail returned |

---

### TC-PROD-004: View Non-Existent Product

| Field             | Value                             |
| :---------------- | :-------------------------------- |
| **Priority**      | Medium                            |
| **Preconditions** | Product ID `99999` does not exist |
| **SRS Trace**     | FR-PROD-02                        |

| Step | Action                                   | Expected Result        |
| :--: | :--------------------------------------- | :--------------------- |
|  1   | Send `GET /api/products/99999`           | Status `404 Not Found` |
|  2   | Verify error message "Product not found" | Proper error handling  |

---

## 4. Shopping Cart Test Cases

### TC-CART-001: Add Product to Cart

| Field             | Value                                         |
| :---------------- | :-------------------------------------------- |
| **Priority**      | Critical                                      |
| **Preconditions** | User authenticated; Product ID `101` in stock |
| **SRS Trace**     | FR-CART-01                                    |

| Step | Action                                                            | Expected Result                               |
| :--: | :---------------------------------------------------------------- | :-------------------------------------------- |
|  1   | Send `POST /api/cart/items` with `{"productId":101,"quantity":2}` | Status `200 OK`                               |
|  2   | Send `GET /api/cart`                                              | Cart contains product `101` with quantity `2` |

---

### TC-CART-002: Update Cart Item Quantity

| Field             | Value                                            |
| :---------------- | :----------------------------------------------- |
| **Priority**      | High                                             |
| **Preconditions** | User has product `101` in cart with quantity `2` |
| **SRS Trace**     | FR-CART-03                                       |

| Step | Action                                               | Expected Result                       |
| :--: | :--------------------------------------------------- | :------------------------------------ |
|  1   | Send `PUT /api/cart/items/101` with `{"quantity":5}` | Status `200 OK`                       |
|  2   | Send `GET /api/cart`                                 | Product `101` quantity updated to `5` |

---

### TC-CART-003: Remove Item from Cart

| Field             | Value                          |
| :---------------- | :----------------------------- |
| **Priority**      | High                           |
| **Preconditions** | User has product `101` in cart |
| **SRS Trace**     | FR-CART-04                     |

| Step | Action                            | Expected Result                 |
| :--: | :-------------------------------- | :------------------------------ |
|  1   | Send `DELETE /api/cart/items/101` | Status `200 OK`                 |
|  2   | Send `GET /api/cart`              | Product `101` no longer in cart |

---

### TC-CART-004: Checkout with Empty Cart

| Field             | Value                             |
| :---------------- | :-------------------------------- |
| **Priority**      | High                              |
| **Preconditions** | User authenticated; Cart is empty |
| **SRS Trace**     | FR-CART-06                        |

| Step | Action                         | Expected Result          |
| :--: | :----------------------------- | :----------------------- |
|  1   | Send `POST /api/orders`        | Status `400 Bad Request` |
|  2   | Verify message "Cart is empty" | Prevented from ordering  |

---

## 5. Checkout & Order Test Cases

### TC-ORD-001: Successful Order Placement

| Field             | Value                                                    |
| :---------------- | :------------------------------------------------------- |
| **Priority**      | Critical                                                 |
| **Preconditions** | User authenticated; Cart has 2 items; All items in stock |
| **SRS Trace**     | FR-CHK-01                                                |

| Step | Action                                                   | Expected Result      |
| :--: | :------------------------------------------------------- | :------------------- |
|  1   | Send `POST /api/orders` with shipping address            | Status `201 Created` |
|  2   | Verify response contains `orderNumber` and `totalAmount` | Order created        |
|  3   | Verify order `status` is `PENDING`                       | Awaiting payment     |
|  4   | Verify stock quantities reduced                          | Inventory updated    |

---

### TC-ORD-002: Order with Out-of-Stock Product

| Field             | Value                                                       |
| :---------------- | :---------------------------------------------------------- |
| **Priority**      | Critical                                                    |
| **Preconditions** | Product `101` has `stock_quantity = 0`; User has it in cart |
| **SRS Trace**     | FR-CHK-03                                                   |

| Step | Action                              | Expected Result         |
| :--: | :---------------------------------- | :---------------------- |
|  1   | Send `POST /api/orders`             | Status `409 Conflict`   |
|  2   | Verify message "Insufficient stock" | Order rejected          |
|  3   | Verify no order record created      | Transaction rolled back |

---

### TC-ORD-003: View Order History

| Field             | Value                            |
| :---------------- | :------------------------------- |
| **Priority**      | High                             |
| **Preconditions** | User has placed at least 1 order |
| **SRS Trace**     | FR-CHK-06                        |

| Step | Action                                                                    | Expected Result    |
| :--: | :------------------------------------------------------------------------ | :----------------- |
|  1   | Send `GET /api/orders`                                                    | Status `200 OK`    |
|  2   | Verify response is paginated list of orders                               | Orders returned    |
|  3   | Verify each order has `orderNumber`, `status`, `totalAmount`, `createdAt` | All fields present |

---

## 6. Payment Test Cases

### TC-PAY-001: Successful Payment Flow

| Field             | Value                                        |
| :---------------- | :------------------------------------------- |
| **Priority**      | Critical                                     |
| **Preconditions** | Order `ORD-001` exists with status `PENDING` |
| **SRS Trace**     | FR-PAY-01                                    |

| Step | Action                                                                      | Expected Result              |
| :--: | :-------------------------------------------------------------------------- | :--------------------------- |
|  1   | Send `POST /api/payments/initiate` with `{"orderId":"ORD-001"}`             | Razorpay `order_id` returned |
|  2   | Simulate successful Razorpay payment                                        | Payment processed            |
|  3   | Send `POST /api/payments/verify` with `payment_id`, `order_id`, `signature` | Status `200 OK`              |
|  4   | Verify order status changed to `CONFIRMED`                                  | Payment verified             |

---

### TC-PAY-002: Payment Signature Verification Failure

| Field             | Value                                        |
| :---------------- | :------------------------------------------- |
| **Priority**      | Critical                                     |
| **Preconditions** | Order `ORD-002` exists with status `PENDING` |
| **SRS Trace**     | FR-PAY-02                                    |

| Step | Action                                                   | Expected Result          |
| :--: | :------------------------------------------------------- | :----------------------- |
|  1   | Send `POST /api/payments/verify` with tampered signature | Status `400 Bad Request` |
|  2   | Verify message "Payment verification failed"             | Fraud detected           |
|  3   | Verify order status remains `PENDING`                    | No state change          |

---

## 7. Admin Test Cases

### TC-ADM-001: Admin Creates Product

| Field             | Value                                |
| :---------------- | :----------------------------------- |
| **Priority**      | High                                 |
| **Preconditions** | User authenticated with `ROLE_ADMIN` |
| **SRS Trace**     | FR-ADM-01                            |

| Step | Action                                            | Expected Result      |
| :--: | :------------------------------------------------ | :------------------- |
|  1   | Send `POST /api/admin/products` with product JSON | Status `201 Created` |
|  2   | Verify response contains new product ID           | Product created      |
|  3   | Send `GET /api/products/{id}`                     | New product visible  |

---

### TC-ADM-002: Non-Admin Denied Access to Admin Endpoint

| Field             | Value                               |
| :---------------- | :---------------------------------- |
| **Priority**      | Critical                            |
| **Preconditions** | User authenticated with `ROLE_USER` |
| **SRS Trace**     | FR-ADM-06                           |

| Step | Action                                               | Expected Result        |
| :--: | :--------------------------------------------------- | :--------------------- |
|  1   | Send `POST /api/admin/products` with `ROLE_USER` JWT | Status `403 Forbidden` |
|  2   | Verify message "Access Denied"                       | Authorization enforced |

---

### TC-ADM-003: Admin Updates Order Status

| Field             | Value                                                      |
| :---------------- | :--------------------------------------------------------- |
| **Priority**      | High                                                       |
| **Preconditions** | Admin authenticated; Order `ORD-001` status is `CONFIRMED` |
| **SRS Trace**     | FR-ADM-03                                                  |

| Step | Action                                                           | Expected Result          |
| :--: | :--------------------------------------------------------------- | :----------------------- |
|  1   | Send `PUT /api/admin/orders/ORD-001` with `{"status":"SHIPPED"}` | Status `200 OK`          |
|  2   | Verify order status is `SHIPPED`                                 | State transition applied |

---

## 8. Security Test Cases

### TC-SEC-001: SQL Injection Prevention

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Critical            |
| **Preconditions** | Application running |
| **SRS Trace**     | NFR-SEC-01          |

| Step | Action                                        | Expected Result                            |
| :--: | :-------------------------------------------- | :----------------------------------------- |
|  1   | Send `GET /api/products/search?q=' OR 1=1 --` | Status `200 OK` with empty or safe results |
|  2   | Verify no database error exposed              | Input sanitized                            |
|  3   | Verify no unintended data returned            | Injection ineffective                      |

---

### TC-SEC-002: XSS Prevention

| Field             | Value               |
| :---------------- | :------------------ |
| **Priority**      | Critical            |
| **Preconditions** | Application running |
| **SRS Trace**     | NFR-SEC-02          |

| Step | Action                                                                              | Expected Result                 |
| :--: | :---------------------------------------------------------------------------------- | :------------------------------ |
|  1   | Send `POST /api/products/review` with `{"comment":"<script>alert('xss')</script>"}` | Input sanitized or rejected     |
|  2   | Retrieve the review via API                                                         | Script tags escaped or stripped |

---

### TC-SEC-003: Rate Limiting on Login

| Field             | Value                                          |
| :---------------- | :--------------------------------------------- |
| **Priority**      | High                                           |
| **Preconditions** | Application running with rate limiting enabled |
| **SRS Trace**     | NFR-SEC-03                                     |

| Step | Action                                        | Expected Result                                  |
| :--: | :-------------------------------------------- | :----------------------------------------------- |
|  1   | Send 10 failed login attempts within 1 minute | First 5: `401`; After 5: `429 Too Many Requests` |
|  2   | Wait for lockout period to expire             | Login available again                            |

---

## 9. Performance Test Cases

### TC-PERF-001: Product Listing Response Time

| Field             | Value                                |
| :---------------- | :----------------------------------- |
| **Priority**      | High                                 |
| **Preconditions** | Database seeded with 10,000 products |
| **SRS Trace**     | NFR-PERF-01                          |

| Step | Action                                  | Expected Result         |
| :--: | :-------------------------------------- | :---------------------- |
|  1   | Send `GET /api/products?page=0&size=20` | Response time ≤ 500ms   |
|  2   | Repeat 100 times                        | 95th percentile ≤ 500ms |

---

### TC-PERF-002: Concurrent Checkout Load

| Field             | Value                                 |
| :---------------- | :------------------------------------ |
| **Priority**      | High                                  |
| **Preconditions** | 500 virtual users prepared with carts |
| **SRS Trace**     | NFR-PERF-02                           |

| Step | Action                                              | Expected Result                     |
| :--: | :-------------------------------------------------- | :---------------------------------- |
|  1   | Simulate 500 concurrent `POST /api/orders` requests | All orders processed without errors |
|  2   | Verify no data corruption (stock counts consistent) | Inventory integrity maintained      |
|  3   | Verify average response time ≤ 2 seconds            | Performance threshold met           |

---

## 10. Test Case Summary

| Section               | Test Cases | Critical |  High  | Medium |
| :-------------------- | :--------: | :------: | :----: | :----: |
| **Authentication**    |     6      |    4     |   2    |   0    |
| **Product Catalog**   |     4      |    0     |   3    |   1    |
| **Shopping Cart**     |     4      |    1     |   3    |   0    |
| **Checkout & Orders** |     3      |    2     |   1    |   0    |
| **Payment**           |     2      |    2     |   0    |   0    |
| **Admin**             |     3      |    1     |   2    |   0    |
| **Security**          |     3      |    2     |   1    |   0    |
| **Performance**       |     2      |    0     |   2    |   0    |
| **Total**             |   **27**   |  **12**  | **14** | **1**  |

---

**— End of Document —**

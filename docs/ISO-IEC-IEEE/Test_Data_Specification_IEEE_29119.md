# Test Data Specification

## BuildNest E-Commerce Platform

**Document ID:** TDS-BUILDNEST-001
**Version:** 1.0
**Date:** 2026-02-10
**Standard:** ISO/IEC/IEEE 29119:2021 — Software and Systems Engineering — Software Testing
**Reference:** [Test Case Specification (TCS-BUILDNEST-001)](Test_Case_Specification_IEEE_29119.md)

---

## 1. Introduction

### 1.1 Purpose

This document specifies the **test data** required to execute the test cases defined in the [Test Case Specification](Test_Case_Specification_IEEE_29119.md). It provides exact data values, boundary conditions, and seeding scripts to ensure repeatable, deterministic testing.

### 1.2 Data Classification

| Category          | Description                                              |
| :---------------- | :------------------------------------------------------- |
| **Valid Data**    | Inputs that the system must accept and process correctly |
| **Invalid Data**  | Inputs the system must reject with proper error messages |
| **Boundary Data** | Values at the edges of valid ranges                      |
| **Security Data** | Malicious payloads to verify input sanitization          |

---

## 2. User Data Sets

### 2.1 Valid Users (Pre-seeded)

| ID        | Username     | Email                | Password (Raw) | Role       | Used By                          |
| :-------- | :----------- | :------------------- | :------------- | :--------- | :------------------------------- |
| TD-USR-01 | `john.doe`   | `john@buildnest.com` | `Secret@123`   | ROLE_USER  | TC-AUTH-001, TC-CART-_, TC-ORD-_ |
| TD-USR-02 | `jane.admin` | `jane@buildnest.com` | `Admin@456`    | ROLE_ADMIN | TC-ADM-\*                        |
| TD-USR-03 | `test.buyer` | `buyer@test.com`     | `Buyer@789`    | ROLE_USER  | TC-PAY-_, TC-PERF-_              |

### 2.2 Registration Data (Dynamic)

| ID        | Username    | Email            | Password   | Expected             | Used By     |
| :-------- | :---------- | :--------------- | :--------- | :------------------- | :---------- |
| TD-REG-01 | `newuser`   | `new@test.com`   | `Pass@123` | ✅ Success           | TC-AUTH-005 |
| TD-REG-02 | `john.doe`  | `dup@test.com`   | `Pass@123` | ❌ 409 Conflict      | TC-AUTH-006 |
| TD-REG-03 | _(empty)_   | `no@user.com`    | `Pass@123` | ❌ 400 Validation    | Negative    |
| TD-REG-04 | `u`         | `short@test.com` | `Pass@123` | ❌ 400 Too short     | Boundary    |
| TD-REG-05 | `validuser` | `invalid-email`  | `Pass@123` | ❌ 400 Invalid email | Negative    |

### 2.3 Authentication Data

| ID         | Username      | Password        | Expected  | Used By     |
| :--------- | :------------ | :-------------- | :-------- | :---------- |
| TD-AUTH-01 | `john.doe`    | `Secret@123`    | ✅ 200 OK | TC-AUTH-001 |
| TD-AUTH-02 | `john.doe`    | `wrongpassword` | ❌ 401    | TC-AUTH-002 |
| TD-AUTH-03 | `nonexistent` | `any`           | ❌ 401    | TC-AUTH-002 |
| TD-AUTH-04 | _(empty)_     | _(empty)_       | ❌ 400    | Negative    |

---

## 3. Product Data Sets

### 3.1 Catalog Products (Pre-seeded)

| ID        | Product Name        | SKU       | Price (₹) | Stock | Category    | Used By                   |
| :-------- | :------------------ | :-------- | :-------- | :---- | :---------- | :------------------------ |
| TD-PRD-01 | Laptop Pro 15       | `LAP-001` | 75,000.00 | 50    | Electronics | TC-PROD-_, TC-CART-_      |
| TD-PRD-02 | Wireless Mouse      | `MOU-001` | 1,200.00  | 200   | Accessories | TC-CART-_, TC-ORD-_       |
| TD-PRD-03 | USB-C Hub           | `USB-001` | 2,500.00  | 0     | Accessories | TC-ORD-002 (Out-of-stock) |
| TD-PRD-04 | Mechanical Keyboard | `KEY-001` | 4,500.00  | 5     | Accessories | TC-PERF-\*                |
| TD-PRD-05 | Monitor 27" 4K      | `MON-001` | 35,000.00 | 15    | Electronics | TC-PROD-003               |

### 3.2 Product Search Data

| ID         | Search Query     | Expected Results         | Used By     |
| :--------- | :--------------- | :----------------------- | :---------- |
| TD-SRCH-01 | `laptop`         | TD-PRD-01 returned       | TC-PROD-002 |
| TD-SRCH-02 | `xyznonexistent` | Empty results            | Negative    |
| TD-SRCH-03 | _(empty)_        | All products (paginated) | TC-PROD-001 |

---

## 4. Order & Cart Data Sets

### 4.1 Cart Payloads

| ID         | Items                               | Expected                 | Used By     |
| :--------- | :---------------------------------- | :----------------------- | :---------- |
| TD-CART-01 | `[{productId: 1, quantity: 2}]`     | ✅ Added                 | TC-CART-001 |
| TD-CART-02 | `[{productId: 1, quantity: 0}]`     | ❌ 400 Invalid qty       | Boundary    |
| TD-CART-03 | `[{productId: 1, quantity: -1}]`    | ❌ 400 Negative qty      | Boundary    |
| TD-CART-04 | `[{productId: 99999, quantity: 1}]` | ❌ 404 Product not found | Negative    |

### 4.2 Shipping Addresses

| ID         | Street          | City      | Zip Code  | Valid  | Used By    |
| :--------- | :-------------- | :-------- | :-------- | :----- | :--------- |
| TD-ADDR-01 | 123 Main Street | Mumbai    | 400001    | ✅     | TC-ORD-001 |
| TD-ADDR-02 | _(empty)_       | _(empty)_ | _(empty)_ | ❌ 400 | Negative   |
| TD-ADDR-03 | A               | B         | 0         | ❌ 400 | Boundary   |

### 4.3 Order Scenarios

| ID        | Cart State                | Stock State  | Expected            | Used By     |
| :-------- | :------------------------ | :----------- | :------------------ | :---------- |
| TD-ORD-01 | 2 items, valid quantities | All in stock | ✅ 201 Created      | TC-ORD-001  |
| TD-ORD-02 | 1 item (TD-PRD-03)        | Stock = 0    | ❌ 409 Out-of-stock | TC-ORD-002  |
| TD-ORD-03 | Empty cart                | N/A          | ❌ 400 Cart empty   | TC-CART-004 |

---

## 5. Payment Data Sets

### 5.1 Razorpay Payloads

| ID        | Payment ID     | Order ID         | Signature            | Valid  | Used By    |
| :-------- | :------------- | :--------------- | :------------------- | :----- | :--------- |
| TD-PAY-01 | `pay_test_001` | `order_test_001` | Valid HMAC-SHA256    | ✅     | TC-PAY-001 |
| TD-PAY-02 | `pay_test_002` | `order_test_002` | `tampered_signature` | ❌     | TC-PAY-002 |
| TD-PAY-03 | _(empty)_      | `order_test_003` | _(empty)_            | ❌ 400 | Negative   |

---

## 6. Boundary Value Data

### 6.1 Numeric Fields

| Field               | Min Valid | Max Valid    | Below Min | Above Max     | Used By     |
| :------------------ | :-------- | :----------- | :-------- | :------------ | :---------- |
| `quantity` (Cart)   | 1         | 100          | 0, -1     | 101, 999999   | TC-CART-\*  |
| `price` (Product)   | 0.01      | 9,999,999.99 | 0, -0.01  | 10,000,000.00 | TC-ADM-001  |
| `page` (Pagination) | 0         | N/A          | -1        | N/A           | TC-PROD-001 |
| `size` (Pagination) | 1         | 100          | 0         | 101           | TC-PROD-001 |

### 6.2 String Fields

| Field          | Min Length | Max Length | Below Min             | Above Max       |
| :------------- | :--------- | :--------- | :-------------------- | :-------------- |
| `username`     | 3          | 255        | `"ab"` (2 chars)      | 256-char string |
| `password`     | 8          | 128        | `"Short1!"` (7 chars) | 129-char string |
| `email`        | 5          | 255        | `"a@b"` (3 chars)     | 256-char string |
| `product name` | 1          | 255        | _(empty)_             | 256-char string |
| `sku`          | 1          | 50         | _(empty)_             | 51-char string  |

---

## 7. Security Test Data

### 7.1 SQL Injection Payloads

| ID        | Payload                       | Target Field | Used By    |
| :-------- | :---------------------------- | :----------- | :--------- |
| TD-SEC-01 | `' OR 1=1 --`                 | Search query | TC-SEC-001 |
| TD-SEC-02 | `'; DROP TABLE users; --`     | Username     | TC-SEC-001 |
| TD-SEC-03 | `1 UNION SELECT * FROM users` | Product ID   | TC-SEC-001 |

### 7.2 XSS Payloads

| ID        | Payload                             | Target Field   | Used By    |
| :-------- | :---------------------------------- | :------------- | :--------- |
| TD-SEC-04 | `<script>alert('xss')</script>`     | Review comment | TC-SEC-002 |
| TD-SEC-05 | `<img onerror="alert(1)" src=x>`    | Product name   | TC-SEC-002 |
| TD-SEC-06 | `javascript:alert(document.cookie)` | URL input      | TC-SEC-002 |

### 7.3 Oversized Input

| ID        | Payload                 | Target Field  | Expected                 |
| :-------- | :---------------------- | :------------ | :----------------------- |
| TD-SEC-07 | 10,000-character string | Username      | ❌ 400                   |
| TD-SEC-08 | 1 MB JSON body          | Order payload | ❌ 413 Payload Too Large |

---

## 8. Performance Test Data

### 8.1 Load Test Seed Requirements

| Scenario                    | Data Volume                              | Used By     |
| :-------------------------- | :--------------------------------------- | :---------- |
| Product listing performance | 10,000 products seeded                   | TC-PERF-001 |
| Concurrent checkout         | 500 users with pre-filled carts          | TC-PERF-002 |
| Search performance          | 10,000 products indexed in Elasticsearch | TC-PERF-001 |

---

## 9. Data Seeding Strategy

### 9.1 Seed Script (SQL)

```sql
-- Users (passwords are BCrypt of the raw values above)
INSERT INTO users (username, email, password_hash, role, created_at) VALUES
('john.doe', 'john@buildnest.com', '$2a$10$...hash...', 'ROLE_USER', NOW()),
('jane.admin', 'jane@buildnest.com', '$2a$10$...hash...', 'ROLE_ADMIN', NOW()),
('test.buyer', 'buyer@test.com', '$2a$10$...hash...', 'ROLE_USER', NOW());

-- Products
INSERT INTO products (name, sku, price, stock_quantity, category_id) VALUES
('Laptop Pro 15', 'LAP-001', 75000.00, 50, 1),
('Wireless Mouse', 'MOU-001', 1200.00, 200, 2),
('USB-C Hub', 'USB-001', 2500.00, 0, 2),
('Mechanical Keyboard', 'KEY-001', 4500.00, 5, 2),
('Monitor 27" 4K', 'MON-001', 35000.00, 15, 1);
```

### 9.2 Environment Guidance

| Environment  | Seeding Method                | Data Reset             |
| :----------- | :---------------------------- | :--------------------- |
| **Dev**      | Flyway migration + `data.sql` | On each restart        |
| **Staging**  | CI pipeline runs seed script  | Before each test cycle |
| **Pre-Prod** | Manual seed via admin script  | Before UAT             |

---

## 10. Traceability

| Data Set       | Test Cases Supported       |
| :------------- | :------------------------- |
| TD-USR-\*      | TC-AUTH-001 to TC-AUTH-006 |
| TD-PRD-\*      | TC-PROD-001 to TC-PROD-004 |
| TD-CART-\*     | TC-CART-001 to TC-CART-004 |
| TD-ORD-\*      | TC-ORD-001 to TC-ORD-003   |
| TD-PAY-\*      | TC-PAY-001, TC-PAY-002     |
| TD-SEC-\*      | TC-SEC-001 to TC-SEC-003   |
| TD-PERF (Load) | TC-PERF-001, TC-PERF-002   |

---

**— End of Document —**

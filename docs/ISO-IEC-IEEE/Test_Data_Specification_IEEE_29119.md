# Test Data Specification

## BuildNest E-Commerce Platform

**Document ID:** TDS-BUILDNEST-001
**Version:** 2.0
**Date:** 2026-02-11
**Standard:** ISO/IEC/IEEE 29119-3:2021

---

## 1. Introduction

### 1.1 Purpose

This document specifies all test data sets required to execute the **124 test cases** defined in the [Test Case Specification](Test_Case_Specification_IEEE_29119.md). It provides valid, invalid, and boundary data for every functional module.

### 1.2 Data Categories

| Category                      | Sections    |
| :---------------------------- | :---------- |
| **Identity & Authentication** | §2.1, §2.2  |
| **Product Catalog**           | §2.3, §2.4  |
| **Shopping Cart & Checkout**  | §2.5, §2.6  |
| **Order & Payment**           | §2.7, §2.8  |
| **Wishlist & Reviews**        | §2.9, §2.10 |
| **Admin Management**          | §2.11       |
| **Security Payloads**         | §2.12       |
| **Performance & Stress**      | §2.13       |
| **Boundary Values**           | §2.14       |

---

## 2. Test Data Sets

### 2.1 User Accounts

| User ID  | Username        | Email                    | Password      | Role  | Status   | Used In                      |
| :------: | :-------------- | :----------------------- | :------------ | :---- | :------- | :--------------------------- |
| TD-U-001 | `john_doe`      | `john@buildnest.com`     | `Test@1234`   | USER  | Active   | TC-AUTH-001, TC-CART, TC-CHK |
| TD-U-002 | `admin_user`    | `admin@buildnest.com`    | `Admin@1234`  | ADMIN | Active   | TC-ADM-\*, TC-SEC-005        |
| TD-U-003 | `inactive_user` | `inactive@buildnest.com` | `Test@1234`   | USER  | Inactive | TC-AUTH-007                  |
| TD-U-004 | `deleted_user`  | `deleted@buildnest.com`  | `Test@1234`   | USER  | Deleted  | TC-AUTH-017                  |
| TD-U-005 | `new_register`  | `newuser@buildnest.com`  | `NewPass@123` | USER  | —        | TC-AUTH-005, TC-E2E-001      |

### 2.2 Authentication Credentials

|   Data ID   | Scenario         | Username         | Password    | Expected           | Used In     |
| :---------: | :--------------- | :--------------- | :---------- | :----------------- | :---------- |
| TD-AUTH-001 | Valid login      | `john_doe`       | `Test@1234` | 200 + tokens       | TC-AUTH-001 |
| TD-AUTH-002 | Wrong password   | `john_doe`       | `wrongpass` | 401                | TC-AUTH-002 |
| TD-AUTH-003 | Nonexistent user | `ghost_user`     | `anything`  | 401                | TC-AUTH-002 |
| TD-AUTH-004 | Empty username   | ``               | `Test@1234` | 400                | TC-AUTH-004 |
| TD-AUTH-005 | Empty password   | `john_doe`       | ``          | 400                | TC-AUTH-004 |
| TD-AUTH-006 | SQL in username  | `' OR 1=1 --`    | `anything`  | 401 (no injection) | TC-SEC-011  |
| TD-AUTH-007 | Expired JWT      | —                | —           | 401                | TC-AUTH-011 |
| TD-AUTH-008 | Malformed JWT    | `not.a.jwt`      | —           | 401                | TC-AUTH-012 |
| TD-AUTH-009 | Tampered JWT     | Modified payload | —           | 401                | TC-AUTH-013 |

### 2.3 Products

| Data ID  | Name               |    Price     | SKU     | Category    | Stock  | Active | Used In                |
| :------: | :----------------- | :----------: | :------ | :---------- | :----: | :----: | :--------------------- |
| TD-P-001 | Wireless Laptop    |    999.99    | LAP-001 | Electronics |   50   |   ✓    | TC-PROD-001, TC-CART   |
| TD-P-002 | USB-C Cable        |    19.99     | CBL-001 | Accessories |  200   |   ✓    | TC-PROD-002            |
| TD-P-003 | Out of Stock Phone |    699.99    | PHN-OOS | Electronics |   0    |   ✓    | TC-CHK-003, TC-ADM-INV |
| TD-P-004 | Inactive Product   |    49.99     | INX-001 | Misc        |   10   |   ✗    | TC-PROD-005            |
| TD-P-005 | Boundary Price     |     0.01     | BND-001 | Test        |   1    |   ✓    | TC-EDGE-001            |
| TD-P-006 | Max Price          |   99999.99   | BND-002 | Test        | 999999 |   ✓    | TC-EDGE-002            |
| TD-P-007 | Unicode Product    | 日本語の製品 | UNI-001 | Test        |   5    |   ✓    | TC-EDGE-005            |
| TD-P-008 | V2-Only Product    | V2 Exclusive | V2-001  | Electronics |   10   |   ✓    | TC-PROD-005            |

### 2.4 Categories

|  Data ID   | Name                          | Used In     |
| :--------: | :---------------------------- | :---------- |
| TD-CAT-001 | Electronics                   | TC-PROD-004 |
| TD-CAT-002 | Accessories                   | TC-PROD-004 |
| TD-CAT-003 | Clothing                      | TC-PROD-004 |
| TD-CAT-004 | Nonexistent Category (ID 999) | TC-EDGE-006 |

### 2.5 Shopping Cart

|   Data ID   | User     | Products         | Quantities | Used In                 |
| :---------: | :------- | :--------------- | :--------- | :---------------------- |
| TD-CART-001 | john_doe | LAP-001, CBL-001 | 1, 2       | TC-CART-001, TC-CHK-001 |
| TD-CART-002 | john_doe | (empty)          | —          | TC-CHK-009              |
| TD-CART-003 | john_doe | PHN-OOS          | 1          | TC-CHK-003              |

### 2.6 Checkout Data

|  Data ID   | Address                        | Payment Method    | Used In                |
| :--------: | :----------------------------- | :---------------- | :--------------------- |
| TD-CHK-001 | 123 Main St, Mumbai, MH 400001 | RAZORPAY          | TC-CHK-001, TC-E2E-005 |
| TD-CHK-002 | Invalid address (null)         | RAZORPAY          | TC-CHK-010             |
| TD-CHK-003 | Valid address                  | Invalid signature | TC-CHK-006             |

### 2.7 Orders

|  Data ID   | Order Number     | Status    |  Total  | Used In    |
| :--------: | :--------------- | :-------- | :-----: | :--------- |
| TD-ORD-001 | ORD-20260211-001 | CONFIRMED | 1039.97 | TC-ORD-001 |
| TD-ORD-002 | ORD-20260211-002 | SHIPPED   | 699.99  | TC-ORD-002 |
| TD-ORD-003 | ORD-20260211-003 | DELIVERED |  49.99  | TC-ORD-003 |
| TD-ORD-004 | ORD-20260211-004 | CANCELLED |  99.99  | TC-ORD-004 |

### 2.8 Payment Data (Razorpay Test Mode)

|  Data ID   | Payment ID         | Order ID         | Signature        | Valid | Used In    |
| :--------: | :----------------- | :--------------- | :--------------- | :---: | :--------- |
| TD-PAY-001 | `pay_test_valid`   | `order_test_001` | `valid_hmac_sig` |   ✓   | TC-CHK-004 |
| TD-PAY-002 | `pay_test_invalid` | `order_test_002` | `tampered_sig`   |   ✗   | TC-CHK-006 |

### 2.9 Wishlist Data

|  Data ID   | User     | Products                      | Used In                 |
| :--------: | :------- | :---------------------------- | :---------------------- |
| TD-WSH-001 | john_doe | LAP-001                       | TC-WISH-001             |
| TD-WSH-002 | john_doe | (already in wishlist) LAP-001 | TC-WISH-001 (duplicate) |
| TD-WSH-003 | john_doe | Nonexistent product (ID 999)  | TC-WISH-001 (error)     |

### 2.10 Review Data

|  Data ID   | User     | Product | Rating | Comment                         | Used In                              |
| :--------: | :------- | :------ | :----: | :------------------------------ | :----------------------------------- |
| TD-REV-001 | john_doe | LAP-001 |   5    | "Excellent laptop!"             | TC-REV-001                           |
| TD-REV-002 | john_doe | LAP-001 |   0    | "Bad"                           | TC-REV-002 (invalid rating <1)       |
| TD-REV-003 | john_doe | LAP-001 |   6    | "Good"                          | TC-REV-002 (invalid rating >5)       |
| TD-REV-004 | john_doe | LAP-001 |   3    | (2001 chars)                    | TC-REV-002 (exceeds 2000 char limit) |
| TD-REV-005 | john_doe | LAP-001 |   4    | `<script>alert('XSS')</script>` | TC-SEC-012                           |

### 2.11 Admin Data

|  Data ID   | Scenario                  | Data                                | Used In        |
| :--------: | :------------------------ | :---------------------------------- | :------------- |
| TD-ADM-001 | Add stock                 | Product: LAP-001, quantity: +50     | TC-ADM-INV-002 |
| TD-ADM-002 | Update order status       | Order: ORD-001, new status: SHIPPED | TC-ADM-ORD-002 |
| TD-ADM-003 | Deactivate user           | User: inactive_user                 | TC-ADM-USR-002 |
| TD-ADM-004 | Analytics date range      | 2026-01-01 to 2026-02-11            | TC-ADM-ANL-001 |
| TD-ADM-005 | Low stock threshold query | threshold: 10                       | TC-ADM-INV-001 |

### 2.12 Security Payloads

|  Data ID   | Type                  | Payload                                                      | Expected Result       | Used In    |
| :--------: | :-------------------- | :----------------------------------------------------------- | :-------------------- | :--------- |
| TD-SEC-001 | SQL Injection         | `' OR 1=1 --`                                                | Rejected/escaped      | TC-SEC-011 |
| TD-SEC-002 | SQL Injection (UNION) | `' UNION SELECT * FROM users --`                             | Rejected              | TC-SEC-013 |
| TD-SEC-003 | Stored XSS            | `<script>alert('xss')</script>`                              | Sanitized             | TC-SEC-012 |
| TD-SEC-004 | Reflected XSS         | `<img onerror=alert(1) src=x>`                               | Sanitized             | TC-SEC-012 |
| TD-SEC-005 | Path Traversal        | `../../../etc/passwd`                                        | 400 Bad Request       | TC-SEC-015 |
| TD-SEC-006 | Command Injection     | `; ls -la`                                                   | Rejected              | TC-SEC-016 |
| TD-SEC-007 | XXE                   | `<!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>` | Rejected              | TC-SEC-014 |
| TD-SEC-008 | Mass Assignment       | `{"role": "ADMIN", "isActive": true}`                        | Extra fields ignored  | TC-SEC-018 |
| TD-SEC-009 | Oversized Payload     | 10MB JSON body                                               | 413 Payload Too Large | TC-SEC-017 |
| TD-SEC-010 | Header Injection      | `\r\nInjected-Header: value`                                 | Sanitized             | TC-SEC-019 |

### 2.13 Performance & Stress Data

|   Data ID   | Scenario           | Dataset                          | Used In       |
| :---------: | :----------------- | :------------------------------- | :------------ |
| TD-PERF-001 | Normal load        | 100 concurrent users, 10 req/sec | TC-PERF-001   |
| TD-PERF-002 | Catalog browsing   | 1000 products, paginated         | TC-PERF-002   |
| TD-PERF-003 | Search performance | 50 parallel search queries       | TC-PERF-003   |
| TD-STR-001  | 2x capacity        | 200 concurrent users             | TC-STRESS-001 |
| TD-STR-002  | 3x capacity        | 300 concurrent users             | TC-STRESS-002 |
| TD-STR-003  | Sustained load     | 100 users for 30 min             | TC-STRESS-003 |

### 2.14 Boundary Values

|  Data ID   | Field                 | Valid Min | Valid Max | Below Min | Above Max | Used In                  |
| :--------: | :-------------------- | :-------: | :-------: | :-------: | :-------: | :----------------------- |
| TD-BND-001 | Product Price         |   0.01    | 99999.99  |   0.00    | 100000.00 | TC-EDGE-001, TC-EDGE-002 |
| TD-BND-002 | Cart Quantity         |     1     |    999    |     0     |   1000    | TC-EDGE-009              |
| TD-BND-003 | Review Rating         |     1     |     5     |     0     |     6     | TC-REV-002               |
| TD-BND-004 | Review Comment Length |     0     |   2000    |     —     |   2001    | TC-REV-002               |
| TD-BND-005 | Username Length       |     3     |    50     |  2 chars  | 51 chars  | TC-AUTH-005              |
| TD-BND-006 | Password Length       |     8     |    128    |  7 chars  | 129 chars | TC-AUTH-005              |
| TD-BND-007 | Phone Number          | 10 digits | 15 digits | 9 digits  | 16 digits | TC-AUTH-005              |
| TD-BND-008 | Stock Quantity        |     0     |  999999   |    -1     |  1000000  | TC-ADM-INV-002           |

---

## 3. Data Seeding Strategy

### 3.1 Automated Seeding

| Method                            | When            | Data                                |
| :-------------------------------- | :-------------- | :---------------------------------- |
| Liquibase migrations              | Schema creation | Tables, indexes, constraints        |
| `@Sql` annotation                 | Per-test class  | Test-specific data                  |
| `@BeforeEach` / `TestDataFactory` | Per-test method | Fresh entities via builder patterns |
| `@Transactional(rollback)`        | After each test | Automatic cleanup                   |

### 3.2 Test Data Factory Pattern

```java
public class TestDataFactory {
    public static User createUser(String username, Role role) { ... }
    public static Product createProduct(String sku, BigDecimal price, int stock) { ... }
    public static Cart createCartWithItems(User user, Product... products) { ... }
    public static Order createOrder(User user, OrderStatus status) { ... }
    public static ProductReview createReview(User user, Product product, int rating) { ... }
    public static Wishlist createWishlist(User user, Product... products) { ... }
}
```

---

## 4. Data Cross-Reference to Test Cases

| TCS Category             | Primary Data Sets                                  |
| :----------------------- | :------------------------------------------------- |
| Authentication (TC-AUTH) | TD-U-001..005, TD-AUTH-001..009                    |
| Checkout (TC-CHK)        | TD-CART-001..003, TD-CHK-001..003, TD-PAY-001..002 |
| Cart (TC-CART)           | TD-P-001..003, TD-CART-001                         |
| Product (TC-PROD)        | TD-P-001..008, TD-CAT-001..004                     |
| Order (TC-ORD)           | TD-ORD-001..004                                    |
| Wishlist (TC-WISH)       | TD-WSH-001..003                                    |
| Review (TC-REV)          | TD-REV-001..005                                    |
| Admin (TC-ADM)           | TD-U-002, TD-ADM-001..005                          |
| Password (TC-PWD)        | TD-U-001, TD-AUTH-001                              |
| Security (TC-SEC)        | TD-SEC-001..010                                    |
| Performance (TC-PERF)    | TD-PERF-001..003                                   |
| Stress (TC-STRESS)       | TD-STR-001..003                                    |
| Boundary (TC-EDGE)       | TD-BND-001..008                                    |

---

## 5. Revision History

| Version | Date       | Author       | Changes                                                                                                             |
| :------ | :--------- | :----------- | :------------------------------------------------------------------------------------------------------------------ |
| 1.0     | 2026-02-10 | BuildNest QA | Initial — Auth, Product, Cart data                                                                                  |
| 2.0     | 2026-02-11 | BuildNest QA | Added Wishlist, Review, Admin, Security payloads, boundary values, performance/stress data; 124 TC cross-references |

---

**— End of Document —**

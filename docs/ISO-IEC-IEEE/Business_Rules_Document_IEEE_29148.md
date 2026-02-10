# Business Rules Document (BRD)

## BuildNest E-Commerce Platform

**Document ID:** BRD-BUILDNEST-001
**Version:** 1.0
**Date:** 2026-02-10
**Standard:** ISO/IEC/IEEE 29148:2018

---

## 1. Introduction

### 1.1 Purpose

The purpose of this Business Rules Document (BRD) is to define the specific constraints, logic, and policies that govern the behavior of the **BuildNest E-Commerce Platform**. While the **SRS** defines _what_ the system must do, this document defines the _rules_ under which those functions operate.

### 1.2 Scope

This document covers:

1.  **Access Control Rules:** Policies regarding user roles and authentication sessions.
2.  **Operational Rules:** Logic governing core business flows (Order, Inventory, Cart).
3.  **Data Integrity Rules:** Constraints on data quality and validation.
4.  **Financial Rules:** Policies for payments and refunds.

### 1.3 Definitions

- **Business Rule (BR):** A statement that defines or constrains some aspect of the business.
- **Enforcement Level:** Indicates how strictly the rule is applied (Strict = System Error, Warning = Alert).

---

## 2. Business Rule Catalog

### 2.1 Access Control Rules (BR-ACC)

| ID             | Rule Name               | Description                                                                                                                       | Enforcement | Source                                   |
| :------------- | :---------------------- | :-------------------------------------------------------------------------------------------------------------------------------- | :---------- | :--------------------------------------- |
| **BR-ACC-001** | **Role-Based Access**   | The system shall distinguish between `USER` (customer) and `ADMIN` (manager) roles. Guests have read-only access to public pages. | Strict      | [SRS §3.2.1](SRS_IEEE_29148_2018.md)     |
| **BR-ACC-002** | **Token Expiry**        | JWT Access Tokens must expire after **15 minutes**. Refresh Tokens must expire after **30 days**.                                 | Strict      | [SRS FR-AUTH-03](SRS_IEEE_29148_2018.md) |
| **BR-ACC-003** | **Password Security**   | Passwords must be hashed using **BCrypt** with a work factor (strength) of **10 rounds**.                                         | Strict      | [SRS FR-AUTH-10](SRS_IEEE_29148_2018.md) |
| **BR-ACC-004** | **Session Termination** | Logout invalidates the user's Refresh Token immediately, preventing new access token generation.                                  | Strict      | [SRS FR-AUTH-07](SRS_IEEE_29148_2018.md) |

### 2.2 Operational Rules (BR-OPS)

| ID             | Rule Name                 | Description                                                                                                       | Enforcement | Source                                   |
| :------------- | :------------------------ | :---------------------------------------------------------------------------------------------------------------- | :---------- | :--------------------------------------- |
| **BR-OPS-001** | **Single Active Cart**    | A user account can have exactly **one** active shopping cart.                                                     | Strict      | [SRS FR-CART-06](SRS_IEEE_29148_2018.md) |
| **BR-OPS-002** | **Inventory Deduction**   | Stock specific to an order is deducted from the global inventory **immediately upon successful order placement**. | Strict      | [SRS FR-CHK-06](SRS_IEEE_29148_2018.md)  |
| **BR-OPS-003** | **Order Finality**        | An order in `SHIPPED` or `DELIVERED` state cannot be cancelled by the user.                                       | Strict      | [SDD §4.9.1](SDD_IEEE_1016_2017.md)      |
| **BR-OPS-004** | **Low Stock Threshold**   | If product stock falls below the configured threshold (default: 10), a `LowStockWarningEvent` is triggered.       | Warning     | [SRS FR-INV-06](SRS_IEEE_29148_2018.md)  |
| **BR-OPS-005** | **Zero Stock Prevention** | A product with stock quantity **0** cannot be added to the cart or purchased.                                     | Strict      | [SDD §4.9.3](SDD_IEEE_1016_2017.md)      |

### 2.3 Data Integrity Rules (BR-DAT)

| ID             | Rule Name                | Description                                                            | Enforcement | Source                                   |
| :------------- | :----------------------- | :--------------------------------------------------------------------- | :---------- | :--------------------------------------- |
| **BR-DAT-001** | **Unique Identity**      | Email addresses and Usernames must be unique across the entire system. | Strict      | [SRS FR-AUTH-01](SRS_IEEE_29148_2018.md) |
| **BR-DAT-002** | **Price Positivity**     | Product prices must be greater than or equal to zero.                  | Strict      | Domain Constraint                        |
| **BR-DAT-003** | **Address Completeness** | Shipping addresses must include Street, City, State, and Zip Code.     | Strict      | [SRS FR-CHK-01](SRS_IEEE_29148_2018.md)  |

### 2.4 Financial Rules (BR-PAY)

| ID             | Rule Name                  | Description                                                                                                 | Enforcement | Source                                  |
| :------------- | :------------------------- | :---------------------------------------------------------------------------------------------------------- | :---------- | :-------------------------------------- |
| **BR-PAY-001** | **Payment Signature Info** | All Razorpay payment callbacks must have their signature verified against the secret key before processing. | Strict      | [SRS FR-PAY-02](SRS_IEEE_29148_2018.md) |
| **BR-PAY-002** | **Exact Amount Match**     | The amount paid via Razorpay must exactly match the calculated Order Total.                                 | Strict      | Domain Constraint                       |

---

## 3. Decision Tables & State Matrices

### 3.1 Order Status Transition Matrix

Defines allowed transitions for an Order.

- **Row:** Current State
- **Column:** Target State
- **Cell:** Trigger / Condition

| Current \ Target | PENDING | CONFIRMED       | SHIPPED    | DELIVERED          | CANCELLED       |
| :--------------- | :------ | :-------------- | :--------- | :----------------- | :-------------- |
| **PENDING**      | -       | Payment Success | -          | -                  | User/Sys Cancel |
| **CONFIRMED**    | -       | -               | Admin Ship | -                  | Admin Cancel    |
| **SHIPPED**      | -       | -               | -          | Confirmed Delivery | -               |
| **DELIVERED**    | -       | -               | -          | -                  | -               |
| **CANCELLED**    | -       | -               | -          | -                  | -               |

_(Derived from [SDD §4.9.1](SDD_IEEE_1016_2017.md))_

### 3.2 Inventory Status Logic

Defines how the system determines the status of a product based on quantity.

| Quantity (Q)   | Threshold (T) | Status         | Action                                 |
| :------------- | :------------ | :------------- | :------------------------------------- |
| **Q > T**      | Any           | `IN_STOCK`     | User can buy.                          |
| **0 < Q <= T** | Any           | `LOW_STOCK`    | User can buy. Admin alerted.           |
| **Q = 0**      | Any           | `OUT_OF_STOCK` | Purchase disabled. "Notify Me" active. |

_(Derived from [SDD §4.9.3](SDD_IEEE_1016_2017.md))_

---

## 4. Traceability

Mapping Business Rules to Functional Requirements (SRS).

| Business Rule  | Functional Requirement(s) |
| :------------- | :------------------------ |
| **BR-ACC-001** | FR-AUTH-09                |
| **BR-ACC-002** | FR-AUTH-03, FR-AUTH-04    |
| **BR-ACC-003** | FR-AUTH-10                |
| **BR-ACC-004** | FR-AUTH-07                |
| **BR-OPS-001** | FR-CART-06                |
| **BR-OPS-002** | FR-CHK-06                 |
| **BR-OPS-004** | FR-INV-06                 |
| **BR-DAT-001** | FR-AUTH-01                |
| **BR-PAY-001** | FR-PAY-02                 |

---

**— End of Document —**

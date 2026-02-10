# Requirements Traceability Matrix (RTM)

## BuildNest E-Commerce Platform

**Document ID:** RTM-BUILDNEST-001
**Version:** 1.0
**Date:** 2026-02-10
**Standard:** ISO/IEC/IEEE 29148:2018

---

## 1. Introduction

### 1.1 Purpose

The purpose of this Requirements Traceability Matrix (RTM) is to ensure that all requirements defined for the **BuildNest E-Commerce Platform** are linked to their origins (backward traceability) and to their design and implementation counterparts (forward traceability). This ensures that the system is complete, consistent, and that every requirement is accounted for.

### 1.2 Scope

This matrix traces relationships between:

1.  **Stakeholder Needs (SN)** defined in the SRS.
2.  **Functional Requirements (FR)** defined in the SRS.
3.  **Use Cases (UC)** defined in the Use Case Specification.
4.  **Design Elements (DE)** defined in the SDD.

### 1.3 References

1.  **SRS-BUILDNEST-001** - _Software Requirements Specification_.
2.  **SDD-BUILDNEST-001** - _Software Design Description_.
3.  **UCS-BUILDNEST-001** - _Use Case Specification_.
4.  **ISO/IEC/IEEE 29148:2018** - _Systems and software engineering — Life cycle processes — Requirements engineering_.

---

## 2. Traceability Methodology

The matrix uses a bidirectional traceability approach:

- **Forward Traceability:** Ensures that every stakeholder need evolves into a functional requirement, and every requirement is realized by specific design components.
- **Backward Traceability:** Ensures that every design component and requirement can be traced back to a valid business justification or stakeholder need.

### ID Conventions

- **SN-XX:** Stakeholder Need.
- **FR-XX:** Functional Requirement.
- **UC-XX:** Use Case.
- **DE-XX:** Design Element (Class, Service, Module, or Frontend Component).

---

## 3. Traceability Matrices

### 3.1 Table 1: Stakeholder Needs to Functional Requirements

| Stakeholder Need ID | Need Description                        | Functional Requirements (FR)                                |
| :------------------ | :-------------------------------------- | :---------------------------------------------------------- |
| **SN-01**           | Intuitive product browsing and checkout | FR-PROD-01..07, FR-CART-01..06, FR-CHK-01..08, FR-FE-11..15 |
| **SN-02**           | Secure account management & payments    | FR-AUTH-01..11, FR-PAY-01..05, FR-FE-16..18                 |
| **SN-03**           | Admin visibility into sales/inventory   | FR-INV-01..07, FR-ADM-01..08, FR-FE-22..25                  |
| **SN-04**           | Scalable platform (1000+ users)         | FR-MON-01..08, PR-01..04 (Perf)                             |
| **SN-05**           | Observable, container-ready app         | FR-MON-01..08, CI-01..05                                    |
| **SN-06**           | Compliance (OWASP, PCI-DSS)             | FR-AUTH-05, FR-AUTH-08, FR-AUTH-10, FR-PAY-02, FR-PAY-05    |

### 3.2 Table 2: Requirements to Implementation

This table maps Functional Requirements to Use Cases and specific Design Elements (Backend Classes/Frontend Components).

#### Authentication & Security (FG-01)

| Requirement ID                 | Use Case | Backend Component (SDD)                    | Frontend Component (SDD)           |
| :----------------------------- | :------- | :----------------------------------------- | :--------------------------------- |
| **FR-AUTH-01** (Register)      | UC-01    | `AuthService`, `AuthController`            | `RegisterPage`, `AuthService (JS)` |
| **FR-AUTH-02** (Login)         | UC-02    | `AuthService`, `JwtUtil`                   | `LoginPage`, `AuthProvider`        |
| **FR-AUTH-03** (Access Token)  | UC-02    | `JwtTokenProvider`, `SecurityConfig`       | `AuthProvider`, `AxiosInterceptor` |
| **FR-AUTH-06** (Refresh Token) | UC-02    | `RefreshTokenService`                      | `AuthProvider`, `AxiosInterceptor` |
| **FR-AUTH-09** (RBAC)          | All      | `UserDetailsServiceImpl`, `SecurityConfig` | `ProtectedRoute`, `RoleGuard`      |

#### Product Catalog (FG-02)

| Requirement ID              | Use Case | Backend Component (SDD)               | Frontend Component (SDD)          |
| :-------------------------- | :------- | :------------------------------------ | :-------------------------------- |
| **FR-PROD-01** (List)       | UC-03    | `ProductService`, `ProductController` | `ProductListPage`, `ProductCard`  |
| **FR-PROD-02** (Details)    | UC-03    | `ProductService`                      | `ProductDetailPage`               |
| **FR-PROD-03** (Categories) | UC-03    | `CategoryService`                     | `CategoryFilter`, `Navbar`        |
| **FR-PROD-04** (Admin CRUD) | UC-07    | `ProductService`, `AdminController`   | `AdminProductPage`, `ProductForm` |
| **FR-PROD-06** (Caching)    | -        | `ProductService` (@Cacheable)         | -                                 |

#### Shopping Cart (FG-03)

| Requirement ID              | Use Case | Backend Component (SDD)         | Frontend Component (SDD)  |
| :-------------------------- | :------- | :------------------------------ | :------------------------ |
| **FR-CART-01** (Add Item)   | UC-04    | `CartService`, `CartController` | `AddToCartButton`         |
| **FR-CART-02** (View Cart)  | UC-04    | `CartService`                   | `CartPage`, `CartItemRow` |
| **FR-CART-04** (Clear Cart) | UC-04    | `CartService`                   | `CartPage`                |
| **FR-CART-05** (Total Calc) | UC-04    | `CartService`                   | `CartSummary`             |

#### Checkout & Orders (FG-04)

| Requirement ID                | Use Case | Backend Component (SDD)           | Frontend Component (SDD) |
| :---------------------------- | :------- | :-------------------------------- | :----------------------- |
| **FR-CHK-01** (Validate)      | UC-05    | `CheckoutService`                 | `CheckoutPage`           |
| **FR-CHK-05** (Create Order)  | UC-05    | `OrderService`, `OrderController` | `CheckoutPage`           |
| **FR-CHK-06** (Deduct Stock)  | UC-05    | `InventoryService`                | -                        |
| **FR-CHK-07** (Order History) | UC-06    | `OrderService`                    | `OrderHistoryPage`       |
| **FR-CHK-08** (Admin Orders)  | UC-08    | `OrderService`, `AdminController` | `AdminOrderPage`         |

#### Payment (FG-05)

| Requirement ID                | Use Case | Backend Component (SDD)            | Frontend Component (SDD) |
| :---------------------------- | :------- | :--------------------------------- | :----------------------- |
| **FR-PAY-01** (Razorpay Init) | UC-05    | `PaymentService`, `RazorpayClient` | `RazorpayCheckout (JS)`  |
| **FR-PAY-02** (Verify Sig)    | UC-05    | `PaymentService`                   | -                        |
| **FR-PAY-04** (Webhooks)      | -        | `PaymentController` (Webhook)      | -                        |

#### Inventory (FG-06)

| Requirement ID                  | Use Case | Backend Component (SDD)             | Frontend Component (SDD) |
| :------------------------------ | :------- | :---------------------------------- | :----------------------- |
| **FR-INV-01** (Check Stock)     | UC-03    | `InventoryService`                  | `ProductDetailPage`      |
| **FR-INV-03** (Add Stock)       | UC-09    | `InventoryService`                  | `AdminInventoryPage`     |
| **FR-INV-06** (Low Stock Event) | -        | `InventoryService` (EventPublisher) | -                        |

#### Frontend Specific (FG-10)

| Requirement ID            | Use Case | Backend Component (SDD)        | Frontend Component (SDD)              |
| :------------------------ | :------- | :----------------------------- | :------------------------------------ |
| **FR-FE-03** (Routing)    | All      | -                              | `AppRouter`, `React Router`           |
| **FR-FE-05** (State Mgmt) | All      | -                              | `Redux Toolkit` / `Context Providers` |
| **FR-FE-11** (Home Page)  | UC-03    | `ProductController` (Featured) | `HomePage`                            |
| **FR-FE-22** (Admin Dash) | UC-10    | `AnalyticsService`             | `AdminDashboard`                      |

---

**— End of Document —**

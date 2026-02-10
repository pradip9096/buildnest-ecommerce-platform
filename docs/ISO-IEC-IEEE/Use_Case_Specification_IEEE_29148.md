# Use Case Specification

## BuildNest E-Commerce Platform

**Document ID:** UCS-BUILDNEST-001
**Version:** 1.0
**Date:** 2026-02-10
**Standard:** ISO/IEC/IEEE 29148:2018

---

## 1. Introduction

### 1.1 Purpose

The purpose of this Use Case Specification is to capture the behavioral requirements of the **BuildNest E-Commerce Platform** by describing the interactions between users (actors) and the system. This document compliments the Software Requirements Specification (SRS) by providing a narrative, flow-based view of the functional requirements.

### 1.2 Scope

This document covers the core functional areas of the platform:

- User authentication and identity management.
- Product catalog browsing and searching.
- Shopping cart management.
- Checkout and order processing.
- Administrative operations (product, inventory, and user management).

### 1.3 Definitions and Acronyms

| Term                 | Definition                                                         |
| :------------------- | :----------------------------------------------------------------- |
| **Actor**            | An entity (human or system) that interacts with the system.        |
| **Precondition**     | The state of the system required before the use case can start.    |
| **Postcondition**    | The state of the system after the use case completes successfully. |
| **Alternative Flow** | A path that varies from the main flow but still achieves the goal. |
| **Exception Flow**   | A path where an error occurs or the goal is abandoned.             |
| **JWT**              | JSON Web Token used for stateless authentication.                  |

### 1.4 References

1.  **ISO/IEC/IEEE 29148:2018** - _Systems and software engineering — Life cycle processes — Requirements engineering_.
2.  **SRS-BUILDNEST-001** - _Software Requirements Specification for BuildNest_.
3.  **SDD-BUILDNEST-001** - _Software Design Description for BuildNest_.

---

## 2. Actors

| Actor               | Type   | Description                                                                                 |
| :------------------ | :----- | :------------------------------------------------------------------------------------------ |
| **Guest**           | Human  | An unregistered or unauthenticated user who can browse products and view public pages.      |
| **Registered User** | Human  | A customer who has authenticated into the system to perform shopping activities.            |
| **Administrator**   | Human  | A privileged user responsible for site management, inventory control, and order processing. |
| **System Timer**    | System | An internal trigger for scheduled tasks like token cleanup and inventory monitoring.        |
| **Payment Gateway** | System | The external Razorpay service that processes payment transactions.                          |

---

## 3. Use Case Diagrams

### 3.1 Customer Use Cases

```mermaid
usecaseDiagram
    actor "Guest" as G
    actor "Registered User" as U
    actor "Payment Gateway" as PG

    package "BuildNest Storefront" {
        usecase "UC-01: Register User" as UC1
        usecase "UC-02: Login" as UC2
        usecase "UC-03: Browse Products" as UC3
        usecase "UC-04: Manage Cart" as UC4
        usecase "UC-05: Checkout" as UC5
        usecase "UC-06: View Order History" as UC6
    }

    G --> UC1
    G --> UC2
    G --> UC3

    U --> UC2
    U --> UC3
    U --> UC4
    U --> UC5
    U --> UC6

    UC5 ..> PG : Initiates Payment
```

### 3.2 Admin Use Cases

---

## 4. Use Case Specifications

### UC-01: Register User

| Field                 | Description                                                                                                                                                                                                                                                                                                                                                     |
| :-------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**       | UC-01                                                                                                                                                                                                                                                                                                                                                           |
| **Title**             | Register User                                                                                                                                                                                                                                                                                                                                                   |
| **Primary Actor**     | Guest                                                                                                                                                                                                                                                                                                                                                           |
| **Description**       | A guest user creates a new account to access registered features.                                                                                                                                                                                                                                                                                               |
| **Preconditions**     | User is not logged in.                                                                                                                                                                                                                                                                                                                                          |
| **Postconditions**    | A new user account is created in the database.                                                                                                                                                                                                                                                                                                                  |
| **Main Flow**         | 1. User navigates to the Registration page.<br>2. User enters Name, Email, Username, and Password.<br>3. User submits the form.<br>4. System validates the input format (email regex, password strength).<br>5. System checks for existing email or username.<br>6. System creates the account.<br>7. System displays a success message and redirects to Login. |
| **Alternative Flows** | **A1: Validation Error**<br>If input is invalid, system highlights errors and asks user to retry.<br>**A2: User Exists**<br>If email/username exists, system displays specific error message.                                                                                                                                                                   |
| **Exceptions**        | **E1: Database Unavailable**<br>System displays a "Service Unavailable" message.                                                                                                                                                                                                                                                                                |
| **Traceability**      | FR-AUTH-01, FR-AUTH-10                                                                                                                                                                                                                                                                                                                                          |

### UC-02: Login

| Field                 | Description                                                                                                                                                                                                                                                                                                  |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**       | UC-02                                                                                                                                                                                                                                                                                                        |
| **Title**             | User Login                                                                                                                                                                                                                                                                                                   |
| **Primary Actor**     | Guest, Registered User, Administrator                                                                                                                                                                                                                                                                        |
| **Description**       | A user authenticates to access their account and protected features.                                                                                                                                                                                                                                         |
| **Preconditions**     | User has an existing account.                                                                                                                                                                                                                                                                                |
| **Postconditions**    | User is authenticated; JWT access and refresh tokens are issued.                                                                                                                                                                                                                                             |
| **Main Flow**         | 1. User navigates to the Login page.<br>2. User enters Username and Password.<br>3. System validates credentials against stored hash.<br>4. System generates JWT Access Token and Refresh Token.<br>5. System returns tokens to the client.<br>6. Client stores tokens and redirects user to Home/Dashboard. |
| **Alternative Flows** | **A1: Invalid Credentials**<br>System increments login attempt counter and displays "Invalid username or password".                                                                                                                                                                                          |
| **Exceptions**        | **E1: Account Locked**<br>If logic attempts exceed threshold (e.g., 5), account is locked for a duration.<br>**E2: Service Down**<br>Authentication service timeout.                                                                                                                                         |
| **Traceability**      | FR-AUTH-02, FR-AUTH-03, FR-AUTH-04, FR-AUTH-06                                                                                                                                                                                                                                                               |

### UC-03: Browse Products

| Field                 | Description                                                                                                                                                                                                                                                              |
| :-------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**       | UC-03                                                                                                                                                                                                                                                                    |
| **Title**             | Browse and Search Products                                                                                                                                                                                                                                               |
| **Primary Actor**     | Guest, Registered User                                                                                                                                                                                                                                                   |
| **Description**       | User views the product catalog, filters by category, and searches by keyword.                                                                                                                                                                                            |
| **Preconditions**     | System is online.                                                                                                                                                                                                                                                        |
| **Postconditions**    | User sees list of products matching criteria.                                                                                                                                                                                                                            |
| **Main Flow**         | 1. User views the Product List page.<br>2. System retrieves paginated list of products.<br>3. User applies filters (Category, Price Range) or enters Search keyword.<br>4. System queries database/Elasticsearch with criteria.<br>5. System displays matching products. |
| **Alternative Flows** | **A1: No Results**<br>System displays "No products found" message.                                                                                                                                                                                                       |
| **Traceability**      | FR-PROD-01, FR-PROD-02, FR-PROD-03, FR-PROD-06                                                                                                                                                                                                                           |

### UC-04: Manage Cart

| Field                 | Description                                                                                                                                                                                                                                                                                                                                             |
| :-------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Use Case ID**       | UC-04                                                                                                                                                                                                                                                                                                                                                   |
| **Title**             | Manage Ecosystem Cart                                                                                                                                                                                                                                                                                                                                   |
| **Primary Actor**     | Registered User                                                                                                                                                                                                                                                                                                                                         |
| **Description**       | User adds items to cart, updates quantities, or removes items.                                                                                                                                                                                                                                                                                          |
| **Preconditions**     | User is logged in.                                                                                                                                                                                                                                                                                                                                      |
| **Postconditions**    | Cart state is updated in the database.                                                                                                                                                                                                                                                                                                                  |
| **Main Flow**         | 1. User views a Product Detail page.<br>2. User selects quantity and clicks "Add to Cart".<br>3. System validates stock availability.<br>4. System adds item to user's persistent cart.<br>5. User views Cart page.<br>6. System calculates total price.<br>7. User updates quantity or removes item.<br>8. System updates cart and recalculates total. |
| **Alternative Flows** | **A1: Out of Stock**<br>System prevents adding item and displays "Out of Stock".<br>**A2: Max Quantity Exceeded**<br>System restricts quantity based on available stock.                                                                                                                                                                                |
| **Traceability**      | FR-CART-01, FR-CART-02, FR-CART-03, FR-CART-04, FR-CART-05                                                                                                                                                                                                                                                                                              |

### UC-05: Checkout with Payment

| Field                 | Description                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| :-------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**       | UC-05                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Title**             | Checkout with Payment                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Primary Actor**     | Registered User                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| **Secondary Actor**   | Payment Gateway (Razorpay)                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **Description**       | User completes a purchase by paying for items in their cart.                                                                                                                                                                                                                                                                                                                                                                                        |
| **Preconditions**     | Cart is not empty; User is logged in.                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Postconditions**    | Order is created; Inventory is deducted; Payment is recorded; Cart is cleared.                                                                                                                                                                                                                                                                                                                                                                      |
| **Main Flow**         | 1. User clicks "Checkout" from Cart.<br>2. System validates cart inventory.<br>3. User confirms shipping details.<br>4. System creates Razorpay Order.<br>5. User completes payment on Razorpay modal.<br>6. Razorpay returns `payment_id` and `signature`.<br>7. Client submits payment details to backend.<br>8. System verifies signature.<br>9. System creates Order, deducts inventory, clears cart.<br>10. System returns Order Confirmation. |
| **Alternative Flows** | **A1: Payment Failed**<br>Razorpay returns failure; System prompts user to retry.<br>**A2: Inventory Changed**<br>If item goes out of stock during checkout, system alerts user and prevents checkout.                                                                                                                                                                                                                                              |

### UC-06: View Order History

| Field              | Description                                                                                                                                                                                      |
| :----------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**    | UC-06                                                                                                                                                                                            |
| **Title**          | View Order History                                                                                                                                                                               |
| **Primary Actor**  | Registered User                                                                                                                                                                                  |
| **Description**    | User views a list of their past orders and order details.                                                                                                                                        |
| **Preconditions**  | User is logged in.                                                                                                                                                                               |
| **Postconditions** | User views order details.                                                                                                                                                                        |
| **Main Flow**      | 1. User navigates to the Orders page.<br>2. System retrieves list of orders for the user.<br>3. User selects an order.<br>4. System displays order details (items, total, status, payment info). |
| **Traceability**   | FR-CHK-07                                                                                                                                                                                        |

### UC-07: Manage Products (Admin)

| Field                 | Description                                                                                                                                                                                                                                                                                       |
| :-------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Use Case ID**       | UC-07                                                                                                                                                                                                                                                                                             |
| **Title**             | Manage Products                                                                                                                                                                                                                                                                                   |
| **Primary Actor**     | Administrator                                                                                                                                                                                                                                                                                     |
| **Description**       | Admin creates, updates, or deletes products in the catalog.                                                                                                                                                                                                                                       |
| **Preconditions**     | User is logged in as ADMIN.                                                                                                                                                                                                                                                                       |
| **Postconditions**    | Product catalog is updated.                                                                                                                                                                                                                                                                       |
| **Main Flow**         | 1. Admin navigates to Product Management.<br>2. Admin clicks "Add Product".<br>3. Admin enters product details (name, price, stock, category).<br>4. Admin submits form.<br>5. System validates input.<br>6. System saves product to database.<br>7. System updates search index (if configured). |
| **Alternative Flows** | **A1: Edit Product**<br>Admin selects existing product, modifies fields, and saves.<br>**A2: Delete Product**<br>Admin selects delete, system confirms, and product is soft-deleted.                                                                                                              |
| **Traceability**      | FR-PROD-04, FR-ADM-08                                                                                                                                                                                                                                                                             |

### UC-08: Manage Orders (Admin)

| Field              | Description                                                                                                                                                                                                                                                       |
| :----------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**    | UC-08                                                                                                                                                                                                                                                             |
| **Title**          | Manage Orders                                                                                                                                                                                                                                                     |
| **Primary Actor**  | Administrator                                                                                                                                                                                                                                                     |
| **Description**    | Admin views all orders and changes their status (e.g., Ship, Cancel).                                                                                                                                                                                             |
| **Preconditions**  | User is logged in as ADMIN.                                                                                                                                                                                                                                       |
| **Postconditions** | Order status is updated.                                                                                                                                                                                                                                          |
| **Main Flow**      | 1. Admin navigates to Order Management.<br>2. Admin views list of orders.<br>3. Admin selects an order with status `CONFIRMED`.<br>4. Admin clicks "Mark as Shipped".<br>5. System updates status to `SHIPPED`.<br>6. System triggers email notification to user. |
| **Traceability**   | FR-CHK-08, FR-ADM-08                                                                                                                                                                                                                                              |

### UC-09: Manage Inventory

| Field              | Description                                                                                                                                                                                                      |
| :----------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**    | UC-09                                                                                                                                                                                                            |
| **Title**          | Manage Inventory                                                                                                                                                                                                 |
| **Primary Actor**  | Administrator                                                                                                                                                                                                    |
| **Description**    | Admin manually adjusts stock levels for products.                                                                                                                                                                |
| **Preconditions**  | User is logged in as ADMIN.                                                                                                                                                                                      |
| **Postconditions** | Product stock level is updated.                                                                                                                                                                                  |
| **Main Flow**      | 1. Admin searches for a product.<br>2. Admin selects "Update Stock".<br>3. Admin enters new quantity or adjustment amount.<br>4. System updates inventory record.<br>5. System logs the adjustment in audit log. |
| **Traceability**   | FR-INV-03, FR-INV-04, FR-ADM-04                                                                                                                                                                                  |

### UC-10: View Analytics

| Field              | Description                                                                                                                                                                              |
| :----------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Use Case ID**    | UC-10                                                                                                                                                                                    |
| **Title**          | View Analytics                                                                                                                                                                           |
| **Primary Actor**  | Administrator                                                                                                                                                                            |
| **Description**    | Admin views sales and inventory performance reports.                                                                                                                                     |
| **Preconditions**  | User is logged in as ADMIN.                                                                                                                                                              |
| **Postconditions** | Analytics dashboard is displayed.                                                                                                                                                        |
| **Main Flow**      | 1. Admin navigates to Analytics Dashboard.<br>2. System aggregates sales data (total revenue, top products).<br>3. System displays charts and graphs.<br>4. Admin filters by date range. |
| **Traceability**   | FR-ADM-01, FR-ADM-02, FR-ADM-05                                                                                                                                                          |

---

## 5. Traceability Matrix

The following table maps Use Cases to the Functional Requirements defined in the SRS (SRS-BUILDNEST-001).

| Use Case ID | Use Case Title     | Related Functional Requirements (SRS)                                            |
| :---------- | :----------------- | :------------------------------------------------------------------------------- |
| **UC-01**   | Register User      | FR-AUTH-01, FR-AUTH-10                                                           |
| **UC-02**   | Login              | FR-AUTH-02, FR-AUTH-03, FR-AUTH-04, FR-AUTH-06, FR-AUTH-07, FR-AUTH-09           |
| **UC-03**   | Browse Products    | FR-PROD-01, FR-PROD-02, FR-PROD-03, FR-PROD-06, FR-PROD-07                       |
| **UC-04**   | Manage Cart        | FR-CART-01, FR-CART-02, FR-CART-03, FR-CART-04, FR-CART-05, FR-CART-06, FR-FE-02 |
| **UC-05**   | Checkout           | FR-CHK-01 to FR-CHK-06, FR-PAY-01 to FR-PAY-03, FR-INV-06                        |
| **UC-06**   | View Order History | FR-CHK-07, FR-REV-01                                                             |
| **UC-07**   | Manage Products    | FR-PROD-04, FR-ADM-08, FR-ADM-04                                                 |
| **UC-08**   | Manage Orders      | FR-CHK-08, FR-ADM-08, FR-ADM-04                                                  |
| **UC-09**   | Manage Inventory   | FR-INV-03, FR-INV-04, FR-INV-05, FR-ADM-06                                       |
| **UC-10**   | View Analytics     | FR-ADM-01, FR-ADM-02, FR-ADM-05                                                  |

---

**— End of Document —**

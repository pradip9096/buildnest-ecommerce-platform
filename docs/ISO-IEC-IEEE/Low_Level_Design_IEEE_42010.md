# Low-Level Design (LLD) Document

## BuildNest E-Commerce Platform

**Document ID:** LLD-BUILDNEST-001
**Version:** 1.0
**Date:** 2026-02-10
**Standard:** Aligned with ISO/IEC/IEEE 42010:2022 principles

---

## 1. Introduction

### 1.1 Purpose

The purpose of this Low-Level Design (LLD) document is to provide the **detailed implementation specifications** for the BuildNest platform. It serves as the primary reference for developers to write code, defining the physical database schema, exact API contracts, and internal algorithmic logic.

### 1.2 Scope

This document covers:

1.  **Physical Data Model:** SQL table definitions, indexes, and constraints.
2.  **API Specifications:** Request/Response JSON structures for key endpoints.
3.  **Component Logic:** Pseudo-code for complex business workflows.
4.  **Security Implementation:** Filter chains and standard encryption patterns.

---

## 2. Physical Data Model (Database Design)

### 2.1 Entity Relationship Diagram (Physical)

```mermaid
erDiagram
    users {
        BIGINT id PK
        VARCHAR(255) username UK
        VARCHAR(255) email UK
        VARCHAR(255) password_hash
        VARCHAR(50) role
        TIMESTAMP created_at
    }

    products {
        BIGINT id PK
        VARCHAR(255) name
        DECIMAL(10,2) price
        INT stock_quantity
        VARCHAR(50) sku UK
        BIGINT category_id FK
    }

    orders {
        BIGINT id PK
        VARCHAR(50) order_number UK
        DECIMAL(10,2) total_amount
        VARCHAR(20) status
        BIGINT user_id FK
        TIMESTAMP created_at
    }

    order_items {
        BIGINT id PK
        BIGINT order_id FK
        BIGINT product_id FK
        INT quantity
        DECIMAL(10,2) price_at_purchase
    }

    users ||--o{ orders : "places"
    orders ||--|{ order_items : "contains"
    products ||--o{ order_items : "referenced_in"
```

### 2.2 Table Definitions (MySQL)

#### 2.2.1 Users Table (`users`)

| Column          | Type           | Constraints            | Description                  |
| :-------------- | :------------- | :--------------------- | :--------------------------- |
| `id`            | `BIGINT`       | `PK`, `AUTO_INCREMENT` | Unique user identifier.      |
| `username`      | `VARCHAR(255)` | `NOT NULL`, `UNIQUE`   | User login handle.           |
| `email`         | `VARCHAR(255)` | `NOT NULL`, `UNIQUE`   | User email address.          |
| `password_hash` | `VARCHAR(255)` | `NOT NULL`             | BCrypt encoded password.     |
| `role`          | `VARCHAR(50)`  | `NOT NULL`             | `ROLE_USER` or `ROLE_ADMIN`. |

#### 2.2.2 Products Table (`products`)

| Column           | Type            | Constraints            | Description                |
| :--------------- | :-------------- | :--------------------- | :------------------------- |
| `id`             | `BIGINT`        | `PK`, `AUTO_INCREMENT` | Unique product identifier. |
| `name`           | `VARCHAR(255)`  | `NOT NULL`             | Product display name.      |
| `price`          | `DECIMAL(10,2)` | `NOT NULL`, `>= 0`     | Unit price.                |
| `stock_quantity` | `INT`           | `NOT NULL`, `>= 0`     | Available inventory.       |
| `sku`            | `VARCHAR(50)`   | `NOT NULL`, `UNIQUE`   | Stock Keeping Unit.        |

### 2.3 Redis Schema

- **Product Cache:** `product:view:{id}` (TTL: 1 hour) - Stores `ProductDTO` JSON.
- **User Session:** `auth:refresh:{username}` (TTL: 30 days) - Stores Refresh Token.

---

## 3. API Interface Specifications

### 3.1 Authentication

**Endpoint:** `POST /api/auth/login`

**Request:**

```json
{
  "username": "john.doe",
  "password": "secretPassword123"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJh... (JWT)",
    "refreshToken": "ud82... (UUID)",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "username": "john.doe",
      "role": "ROLE_USER"
    }
  }
}
```

### 3.2 Order Management

**Endpoint:** `POST /api/orders`

**Request:**

```json
{
  "items": [
    { "productId": 101, "quantity": 2 },
    { "productId": 205, "quantity": 1 }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "Mumbai",
    "zipCode": "400001"
  }
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "orderId": 5002,
    "orderNumber": "ORD-20260210-5002",
    "totalAmount": 1500.0,
    "status": "PENDING"
  }
}
```

---

## 4. Component Internal Logic (Algorithms)

### 4.1 Checkout Process Transaction

Logic for `OrderService.placeOrder(User user, OrderRequest request)`:

```text
START TRANSACTION
    1. Validate User is active.
    2. Initialize totalAmount = 0.
    3. FOR EACH item IN request.items:
        a. Lock Product row (SELECT ... FOR UPDATE).
        b. IF product.stock < item.quantity THEN
             ROLLBACK & THROW OutOfStockException.
        c. Deduct Stock: product.stock -= item.quantity.
        d. Calculate subtotal: item.price * quantity.
        e. totalAmount += subtotal.
        f. Create OrderItem entity.
    4. Create Order entity with totalAmount and status='PENDING'.
    5. Save Order and OrderItems to DB.
    6. Publish OrderCreatedEvent (Async).
COMMIT TRANSACTION
RETURN OrderResponse
```

### 4.2 JWT Validation Filter

Logic for `JwtTokenFilter.doFilterInternal()`:

```text
1. Extract "Authorization" header.
2. IF header starts with "Bearer ":
    a. Token = header.substring(7).
    b. TRY:
        i. Claims = Jwts.parser().setSigningKey(secret).parse(Token).
        ii. Username = Claims.getSubject().
        iii. Load UserDetails from UserDetailsService.
        iv. IF UserDetails is valid:
             Create UsernamePasswordAuthenticationToken.
             Set Context.Authentication = token.
    c. CATCH ExpiredJwtException:
        Send 401 Unauthorized ("Token Expired").
    d. CATCH SignatureException:
        Send 401 Unauthorized ("Invalid Signature").
3. Chain.doFilter(request, response).
```

---

## 5. Security Implementation Details

### 5.1 Spring Security Filter Chain

The application uses a standard Spring Security chain order:

1.  `CorsFilter` (Handle Cross-Origin requests).
2.  `CsrfFilter` (Disabled for REST API, State-changing ops guarded by JWT).
3.  `JwtAuthenticationFilter` (Custom filter for Token validation).
4.  `UsernamePasswordAuthenticationFilter` (Standard login processing).
5.  `ExceptionTranslationFilter` (Handle AccessDenied/AuthenticationException).
6.  `FilterSecurityInterceptor` (Final URL-based authorization check).

### 5.2 Password Encryption

- **Algorithm:** BCrypt.
- **Strength:** 10 rounds.
- **Salt:** Generated automatically by `BCryptPasswordEncoder`.

---

## 6. Traceability

Mapping LLD Components to HLD Modules.

| LLD Component                   | HLD Module         |
| :------------------------------ | :----------------- |
| `users` table, `JwtTokenFilter` | **Auth Module**    |
| `products` table, Product API   | **Catalog Module** |
| `orders` table, Checkout Logic  | **Order Module**   |
| `order_items` table             | **Order Module**   |

---

**— End of Document —**

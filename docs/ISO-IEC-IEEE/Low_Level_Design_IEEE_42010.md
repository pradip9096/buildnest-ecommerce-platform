# Low-Level Design (LLD) Document

## BuildNest E-Commerce Platform

---

## DOCUMENT INFORMATION

| Attribute                | Value                                                      |
| :----------------------- | :--------------------------------------------------------- |
| **Document Title**       | Low-Level Design Document                                  |
| **Document ID**          | LLD-BUILDNEST-001                                          |
| **Version**              | 2.0                                                        |
| **Date**                 | February 11, 2026                                          |
| **Status**               | Baselined                                                  |
| **Classification**       | Internal Use                                               |
| **Conformance Standard** | ISO/IEC/IEEE 42010:2022                                    |
| **Parent Document**      | [HLD (HLD-BUILDNEST-001)](High_Level_Design_IEEE_42010.md) |

---

## DOCUMENT CONTROL

### Revision History

| Version | Date       | Author   | Changes                                                                          | Approval    |
| :------ | :--------- | :------- | :------------------------------------------------------------------------------- | :---------- |
| 1.0     | 2026-02-10 | Dev Team | Initial draft — Physical Data Model, API Specs, Component Logic                  | ✅ Approved |
| 2.0     | 2026-02-11 | Dev Team | ISO 42010 compliance: added Doc Control, Definitions, Conformance, Security Impl | ✅ Pending  |

### Document Approval

| Role                   | Name     | Signature      | Date             |
| :--------------------- | :------- | :------------- | :--------------- |
| **Technical Lead**     | Dev Lead | \***\*\_\*\*** | \***\*\_\_\*\*** |
| **Database Architect** | DBA      | \***\*\_\*\*** | \***\*\_\_\*\*** |
| **Security Lead**      | Sec Lead | \***\*\_\*\*** | \***\*\_\_\*\*** |

---

## 1. Introduction

### 1.1 Purpose

The purpose of this Low-Level Design (LLD) document is to provide the **detailed implementation specifications** for the BuildNest platform. It serves as the primary reference for developers to write code, defining the physical database schema, exact API contracts, internal algorithmic logic, and security implementation.

### 1.2 Scope

1. **Physical Data Model:** SQL table definitions, indexes, and constraints for all 17+ entities.
2. **API Specifications:** Request/Response JSON structures for all endpoint groups.
3. **Component Logic:** Pseudo-code for complex business workflows.
4. **Security Implementation:** Filter chains, CORS, HTTPS enforcement, and encryption patterns.

### 1.3 Normative References

| Reference                                       | Description                                   |
| :---------------------------------------------- | :-------------------------------------------- |
| **ISO/IEC/IEEE 42010:2022**                     | Architecture Description (governing standard) |
| [HLD](High_Level_Design_IEEE_42010.md)          | High-Level Design                             |
| [ICD](Interface_Control_Document_IEEE_42010.md) | Interface Controls                            |

### 1.4 Definitions & Abbreviations

| Term / Abbr | Definition                      |
| :---------- | :------------------------------ |
| **LLD**     | Low-Level Design                |
| **DTO**     | Data Transfer Object            |
| **DAO**     | Data Access Object (Repository) |
| **CORS**    | Cross-Origin Resource Sharing   |
| **JWT**     | JSON Web Token                  |

### 1.5 Conformance Statement

> This document is detailed in accordance with **ISO/IEC/IEEE 42010:2022**, specifically focusing on the physical data/schematic Viewpoint to guide implementation.

---

## 2. Physical Data Model (Database Design)

### 2.1 Entity Relationship Diagram (Physical)

```mermaid
erDiagram
    users {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR email UK
        VARCHAR password
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR phone_number
        BOOLEAN is_active
        BOOLEAN is_deleted
        DATETIME deleted_at
        DATETIME created_at
        DATETIME updated_at
        DATETIME last_login
    }

    roles {
        BIGINT id PK
        VARCHAR name UK
    }

    permissions {
        BIGINT id PK
        VARCHAR name UK
    }

    user_roles {
        BIGINT user_id FK
        BIGINT role_id FK
    }

    role_permissions {
        BIGINT role_id FK
        BIGINT permission_id FK
    }

    addresses {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR street
        VARCHAR city
        VARCHAR state
        VARCHAR zip_code
        VARCHAR country
    }

    products {
        BIGINT id PK
        VARCHAR name
        TEXT description
        DECIMAL price
        DECIMAL discount_price
        INT stock_quantity
        VARCHAR sku UK
        BIGINT category_id FK
        VARCHAR image_url
        DATE expiry_date
        BOOLEAN is_active
        DATETIME created_at
        DATETIME updated_at
    }

    categories {
        BIGINT id PK
        VARCHAR name UK
        TEXT description
    }

    inventory {
        BIGINT id PK
        BIGINT product_id FK UK
        INT quantity_in_stock
        INT quantity_reserved
        INT minimum_stock_level
        BOOLEAN use_category_threshold
        VARCHAR status
        DATETIME last_restocked
        DATETIME updated_at
        BIGINT version
        DATETIME last_threshold_breach
    }

    inventory_threshold_breach_event {
        BIGINT id PK
        BIGINT inventory_id FK
        VARCHAR breach_type
        INT threshold_value
        INT actual_value
        DATETIME breached_at
    }

    carts {
        BIGINT id PK
        BIGINT user_id FK UK
        DATETIME created_at
        DATETIME updated_at
    }

    cart_items {
        BIGINT id PK
        BIGINT cart_id FK
        BIGINT product_id FK
        INT quantity
        DECIMAL unit_price
    }

    orders {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR order_number UK
        VARCHAR status
        DECIMAL total_amount
        DECIMAL discount_amount
        DECIMAL tax_amount
        DECIMAL shipping_amount
        BIGINT shipping_address_id FK
        VARCHAR tracking_number
        BOOLEAN is_deleted
        DATETIME deleted_at
        DATETIME created_at
        DATETIME updated_at
    }

    order_items {
        BIGINT id PK
        BIGINT order_id FK
        BIGINT product_id FK
        INT quantity
        DECIMAL unit_price
    }

    payments {
        BIGINT id PK
        BIGINT order_id FK
        VARCHAR payment_id UK
        VARCHAR razorpay_order_id
        VARCHAR razorpay_signature
        DECIMAL amount
        VARCHAR status
        DATETIME created_at
    }

    product_review {
        BIGINT id PK
        BIGINT product_id FK
        BIGINT user_id FK
        INT rating
        VARCHAR comment
        INT helpful_count
        BOOLEAN verified_purchase
        BOOLEAN is_visible
        DATETIME created_at
        DATETIME updated_at
    }

    wishlist {
        BIGINT id PK
        BIGINT user_id FK UK
        DATETIME created_at
        DATETIME updated_at
    }

    wishlist_products {
        BIGINT wishlist_id FK
        BIGINT product_id FK
    }

    refresh_tokens {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR token UK
        DATETIME expiry_date
        DATETIME created_at
    }

    password_reset_tokens {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR token UK
        DATETIME expiry_date
        BOOLEAN used
        DATETIME created_at
    }

    audit_log {
        BIGINT id PK
        BIGINT user_id
        VARCHAR action
        VARCHAR entity_type
        VARCHAR entity_id
        TEXT details
        DATETIME created_at
    }

    webhook_subscription {
        BIGINT id PK
        VARCHAR event_type
        VARCHAR target_url
        VARCHAR secret
        BOOLEAN is_active
        INT failure_count
        VARCHAR last_delivery_status
        DATETIME created_at
        DATETIME updated_at
    }

    users ||--o{ user_roles : ""
    roles ||--o{ user_roles : ""
    roles ||--o{ role_permissions : ""
    permissions ||--o{ role_permissions : ""
    users ||--o{ addresses : ""
    users ||--|| carts : ""
    users ||--o{ orders : ""
    users ||--o| wishlist : ""
    users ||--o{ product_review : ""
    users ||--o{ refresh_tokens : ""
    users ||--o{ password_reset_tokens : ""
    carts ||--|{ cart_items : ""
    cart_items }|--|| products : ""
    orders ||--|{ order_items : ""
    orders ||--o| payments : ""
    orders }|--o| addresses : ""
    order_items }|--|| products : ""
    products }|--|| categories : ""
    products ||--|| inventory : ""
    products ||--o{ product_review : ""
    wishlist ||--o{ wishlist_products : ""
    wishlist_products }|--|| products : ""
    inventory ||--o{ inventory_threshold_breach_event : ""
```

### 2.2 Indexes (from JPA annotations and Liquibase)

| Table                                                                 | Index Name                   | Columns        | Type   |
| :-------------------------------------------------------------------- | :--------------------------- | :------------- | :----- |
| `product_review`                                                      | `idx_product_id`             | `product_id`   | B-Tree |
| `product_review`                                                      | `idx_product_review_user_id` | `user_id`      | B-Tree |
| `product_review`                                                      | `idx_rating`                 | `rating`       | B-Tree |
| `product_review`                                                      | `idx_created_at`             | `created_at`   | B-Tree |
| `products`                                                            | `idx_sku`                    | `sku`          | Unique |
| `orders`                                                              | `idx_order_number`           | `order_number` | Unique |
| `users`                                                               | `idx_username`               | `username`     | Unique |
| `users`                                                               | `idx_email`                  | `email`        | Unique |
| Additional performance indexes from `005-add-performance-indexes.xml` |                              |                |        |

### 2.3 Optimistic Locking

The `inventory` table uses `@Version` for optimistic locking:

```sql
-- Concurrent stock update protection
UPDATE inventory SET quantity_in_stock = ?, version = version + 1
WHERE id = ? AND version = ?;
-- If version mismatch → OptimisticLockException → retry
```

---

## 3. API Specifications

### 3.1 Authentication APIs

#### POST `/api/auth/login`

**Request:**

```json
{
  "username": "john.doe",
  "password": "Secret@123"
}
```

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
    "tokenType": "Bearer"
  }
}
```

#### POST `/api/auth/register`

**Request:**

```json
{
  "username": "new.user",
  "email": "new@buildnest.com",
  "password": "StrongP@ss1",
  "firstName": "New",
  "lastName": "User"
}
```

**Success Response (201):**

```json
{
  "success": true,
  "message": "User registered successfully"
}
```

#### POST `/api/auth/refresh`

**Request:**

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

**Response (200):** New access + refresh token pair (rotation).

#### GET `/api/auth/validate`

**Headers:** `Authorization: Bearer <accessToken>`
**Response (200):** Token validity confirmation.

---

### 3.2 Product APIs

#### GET `/api/v2/products?page=0&size=10`

**Response (200):**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Wireless Laptop",
        "description": "High-performance laptop",
        "price": 999.99,
        "discountPrice": 899.99,
        "sku": "LAP-001",
        "category": "Electronics",
        "imageUrl": "/images/laptop.jpg",
        "isActive": true
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "number": 0
  }
}
```

#### GET `/api/v2/products/{id}`

**Response (200):** Single product detail.
**Response (404):** `{"success": false, "message": "Product not found"}`

#### GET `/api/v1/products` (Deprecated)

Returns same data but adds sunset headers:

```http
Sunset: Sat, 01 Mar 2026 00:00:00 GMT
Deprecation: true
Link: </api/v2/products>; rel="successor-version"
```

---

### 3.3 Cart APIs

| Method | Endpoint                        | Request Body                      | Response                  |
| :----- | :------------------------------ | :-------------------------------- | :------------------------ |
| POST   | `/api/user/cart/{userId}`       | `{"productId": 1, "quantity": 2}` | 200 + updated cart        |
| GET    | `/api/user/cart/{userId}`       | —                                 | 200 + cart with items     |
| DELETE | `/api/user/cart/item/{itemId}`  | —                                 | 200 + confirmation        |
| DELETE | `/api/user/cart/{userId}/clear` | —                                 | 200 + empty cart          |
| GET    | `/api/user/cart/{userId}/total` | —                                 | 200 + `{"total": 199.98}` |

---

### 3.4 Checkout & Order APIs

#### POST `/api/orders`

**Request:**

```json
{
  "shippingAddressId": 1,
  "paymentMethod": "RAZORPAY",
  "razorpayPaymentId": "pay_xxx",
  "razorpayOrderId": "order_xxx",
  "razorpaySignature": "sig_xxx"
}
```

**Success Response (200):**

```json
{
  "success": true,
  "data": {
    "orderNumber": "ORD-20260211-ABC123",
    "totalAmount": 199.98,
    "status": "CONFIRMED",
    "items": [...]
  }
}
```

#### GET `/api/user/orders` — User order history (paginated)

#### GET `/api/user/orders/{orderId}` — Order detail

---

### 3.5 Wishlist APIs

| Method | Endpoint                                | Response                 |
| :----- | :-------------------------------------- | :----------------------- |
| POST   | `/api/user/wishlist/{productId}`        | 200 + product added      |
| DELETE | `/api/user/wishlist/{productId}`        | 200 + product removed    |
| GET    | `/api/user/wishlist`                    | 200 + product list       |
| GET    | `/api/user/wishlist/{productId}/exists` | 200 + `{"exists": true}` |
| DELETE | `/api/user/wishlist/clear`              | 200 + cleared            |
| GET    | `/api/user/wishlist/count`              | 200 + `{"count": 5}`     |

---

### 3.6 Product Review APIs

#### POST `/api/user/reviews/product/{productId}`

**Request:**

```json
{
  "rating": 4,
  "comment": "Great product, fast delivery!"
}
```

**Response (201):**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "rating": 4,
    "comment": "Great product, fast delivery!",
    "verifiedPurchase": true,
    "helpfulCount": 0
  }
}
```

| Method | Endpoint                                        | Purpose                            |
| :----- | :---------------------------------------------- | :--------------------------------- |
| GET    | `/api/user/reviews/product/{productId}`         | Get all reviews for product        |
| GET    | `/api/user/reviews/product/{productId}/summary` | Rating summary (avg, distribution) |
| POST   | `/api/user/reviews/{reviewId}/helpful`          | Mark review as helpful             |
| PUT    | `/api/user/reviews/{reviewId}`                  | Update own review                  |
| DELETE | `/api/user/reviews/{reviewId}`                  | Delete own review                  |

---

### 3.7 Admin APIs

#### Product Management

| Method | Endpoint                   | Purpose           |
| :----- | :------------------------- | :---------------- |
| GET    | `/api/admin/products`      | List all products |
| GET    | `/api/admin/products/{id}` | Get product by ID |
| POST   | `/api/admin/products`      | Create product    |
| PUT    | `/api/admin/products/{id}` | Update product    |
| DELETE | `/api/admin/products/{id}` | Delete product    |

#### Order Management

| Method | Endpoint                        | Purpose             |
| :----- | :------------------------------ | :------------------ |
| GET    | `/api/admin/orders`             | List all orders     |
| GET    | `/api/admin/orders/{id}`        | Get order by ID     |
| PUT    | `/api/admin/orders/{id}/status` | Update order status |
| DELETE | `/api/admin/orders/{id}`        | Delete order        |

#### User Management

| Method | Endpoint                | Purpose            |
| :----- | :---------------------- | :----------------- |
| GET    | `/api/admin/users`      | List all users     |
| GET    | `/api/admin/users/{id}` | Get user by ID     |
| PUT    | `/api/admin/users/{id}` | Update user        |
| DELETE | `/api/admin/users/{id}` | Delete (soft) user |

#### Inventory Management

| Method | Endpoint                                           | Purpose               |
| :----- | :------------------------------------------------- | :-------------------- |
| GET    | `/api/admin/inventory/low-stock?threshold=10`      | Low stock products    |
| GET    | `/api/admin/inventory/out-of-stock`                | Out-of-stock products |
| POST   | `/api/admin/inventory/{productId}/add?quantity=50` | Add stock             |
| PUT    | `/api/admin/inventory/{productId}?quantity=100`    | Set stock level       |
| GET    | `/api/admin/inventory/{productId}/available`       | Check availability    |
| GET    | `/api/admin/inventory/{productId}/status`          | Full inventory status |

#### Analytics

| Method | Endpoint                                       | Purpose                |
| :----- | :--------------------------------------------- | :--------------------- |
| GET    | `/api/admin/analytics/audit/user/{userId}`     | Audit logs by user     |
| GET    | `/api/admin/analytics/audit/action/{action}`   | Audit by action type   |
| GET    | `/api/admin/analytics/audit/range?from=&to=`   | Audit by date range    |
| GET    | `/api/admin/analytics/metrics/range?from=&to=` | Metrics by date range  |
| GET    | `/api/admin/analytics/metrics/recent?limit=5`  | Recent metrics         |
| GET    | `/api/admin/analytics/alerts/summary`          | Alert summary          |
| GET    | `/api/admin/analytics/dashboard`               | Dashboard aggregate    |
| GET    | `/api/admin/analytics/errors/status-code`      | API errors by status   |
| GET    | `/api/admin/analytics/errors/endpoint`         | API errors by endpoint |

---

### 3.8 Password Reset APIs

| Method | Endpoint               | Request                                        | Response               |
| :----- | :--------------------- | :--------------------------------------------- | :--------------------- |
| POST   | `/api/password/forgot` | `{"email": "user@mail.com"}`                   | 200 + reset email sent |
| POST   | `/api/password/reset`  | `{"token": "...", "newPassword": "..."}`       | 200 + password changed |
| POST   | `/api/password/change` | `{"oldPassword": "...", "newPassword": "..."}` | 200 + password changed |

---

### 3.9 Monitoring & Health APIs

| Method | Endpoint                      | Purpose                 |
| :----- | :---------------------------- | :---------------------- |
| GET    | `/actuator/health`            | Overall health (public) |
| GET    | `/actuator/health/db`         | MySQL health            |
| GET    | `/actuator/health/redis`      | Redis health            |
| GET    | `/api/monitoring/performance` | Performance metrics     |
| GET    | `/api/monitoring/pool`        | Connection pool status  |

---

## 4. Component Logic (Algorithms)

### 4.1 Checkout Process

```
FUNCTION processCheckout(userId, checkoutRequest):
    cart ← cartService.getCart(userId)
    IF cart IS EMPTY:
        THROW CartEmptyException

    // Step 1: Validate inventory
    FOR EACH item IN cart.items:
        inventory ← inventoryService.getByProductId(item.productId)
        IF inventory.availableQuantity < item.quantity:
            THROW InsufficientStockException(item.productId)

    // Step 2: Reserve inventory (optimistic locking)
    FOR EACH item IN cart.items:
        inventoryService.reserve(item.productId, item.quantity)

    TRY:
        // Step 3: Process payment
        IF checkoutRequest.hasPayment:
            paymentService.verifySignature(
                checkoutRequest.razorpayPaymentId,
                checkoutRequest.razorpayOrderId,
                checkoutRequest.razorpaySignature
            )

        // Step 4: Create order
        order ← orderService.createOrder(userId, cart, checkoutRequest)

        // Step 5: Deduct stock
        FOR EACH item IN cart.items:
            inventoryService.deductStock(item.productId, item.quantity)

        // Step 6: Clear cart
        cartService.clearCart(userId)

        // Step 7: Publish event
        eventPublisher.publish(new OrderPlacedEvent(order))

        RETURN order
    CATCH Exception:
        // Rollback: Release reserved inventory
        FOR EACH item IN cart.items:
            inventoryService.releaseReservation(item.productId, item.quantity)
        THROW CheckoutFailedException
```

### 4.2 JWT Authentication Filter

```
FUNCTION doFilterInternal(request, response, filterChain):
    token ← extractTokenFromHeader(request)

    IF token IS NOT NULL AND jwtProvider.validateToken(token):
        username ← jwtProvider.getUsernameFromToken(token)
        userDetails ← userDetailsService.loadByUsername(username)
        authentication ← new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        )
        SecurityContextHolder.setAuthentication(authentication)

    filterChain.doFilter(request, response)
```

### 4.3 Refresh Token Rotation

```
FUNCTION refreshToken(refreshTokenStr):
    storedToken ← refreshTokenRepo.findByToken(refreshTokenStr)

    IF storedToken IS NULL OR storedToken.isExpired():
        THROW InvalidRefreshTokenException

    // Rotate: invalidate old, generate new
    refreshTokenRepo.delete(storedToken)
    newAccessToken ← jwtProvider.generateAccessToken(storedToken.user)
    newRefreshToken ← refreshTokenService.create(storedToken.user)

    RETURN {accessToken: newAccessToken, refreshToken: newRefreshToken.token}
```

### 4.4 Inventory Threshold Monitoring

```
FUNCTION checkInventoryThresholds():
    FOR EACH product IN productRepository.findAll():
        inventory ← product.inventory
        IF inventory.quantityInStock <= inventory.minimumStockLevel:
            IF inventory.status != LOW_STOCK AND inventory.status != OUT_OF_STOCK:
                inventory.status ← LOW_STOCK
                eventPublisher.publish(new LowStockWarningEvent(product, inventory))
                inventoryService.recordThresholdBreach(inventory)

        IF inventory.quantityInStock == 0:
            inventory.status ← OUT_OF_STOCK
```

---

## 5. Security Implementation

### 5.1 Security Filter Chain

The request processing pipeline applies filters in order:

```
Incoming Request
    │
    ├─► HttpsEnforcementFilter (redirects HTTP→HTTPS in production)
    │
    ├─► AdminRateLimitFilter (rate limits admin endpoints)
    │
    ├─► JwtAuthenticationFilter (extracts + validates JWT)
    │
    ├─► Spring Security FilterChain
    │       ├── CORS Configuration (buildnest.com origins)
    │       ├── CSRF Disabled (stateless JWT)
    │       ├── Session: STATELESS
    │       ├── Content-Security-Policy header
    │       ├── X-Frame-Options: DENY
    │       └── HSTS: max-age=31536000; includeSubDomains; preload
    │
    ├─► Authorization Rules
    │       ├── /api/public/**          → permitAll
    │       ├── /api/auth/login,register → permitAll
    │       ├── /api/password/**        → permitAll
    │       ├── /swagger-ui/**          → permitAll
    │       ├── /actuator/health/**     → permitAll
    │       ├── /actuator/**            → ROLE_ADMIN
    │       ├── /api/admin/**           → ROLE_ADMIN
    │       ├── /api/user/**            → ROLE_USER or ROLE_ADMIN
    │       └── anyRequest              → authenticated
    │
    └─► Controller / Service / Repository
```

### 5.2 Password Encoding

- **Algorithm:** BCrypt
- **Strength:** Default (10 rounds)
- **Bean:** `BCryptPasswordEncoder` from `SecurityConfig.passwordEncoder()`

### 5.3 HTTPS Enforcement

```java
@PostConstruct
public void validateHttpsInProduction() {
    // Production profile MUST have SSL enabled
    // SSL enabled MUST have keystore path + password configured
    // Fail-fast on startup if misconfigured
}
```

### 5.4 CORS Configuration

| Setting             | Value                                                |
| :------------------ | :--------------------------------------------------- |
| **Allowed Origins** | `https://buildnest.com`, `https://www.buildnest.com` |
| **Allowed Methods** | `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`            |
| **Allowed Headers** | `Authorization`, `Content-Type`, `Accept`            |
| **Exposed Headers** | `Authorization`                                      |
| **Credentials**     | `true`                                               |
| **Max Age**         | `3600s`                                              |

---

## 6. Elasticsearch Document Models

### 6.1 Audit Log Document

```json
{
  "_index": "buildnest-audit-logs",
  "userId": 1,
  "action": "LOGIN",
  "entityType": "User",
  "entityId": "1",
  "details": "Successful login from 192.168.1.1",
  "timestamp": "2026-02-11T12:00:00Z"
}
```

### 6.2 Performance Metrics Document

```json
{
  "_index": "buildnest-metrics",
  "endpoint": "/api/orders",
  "method": "POST",
  "responseTimeMs": 245,
  "statusCode": 200,
  "timestamp": "2026-02-11T12:00:00Z"
}
```

---

## 7. Database Migration Strategy

Managed by **Liquibase** with changelog:

| File                                      | Purpose                       |
| :---------------------------------------- | :---------------------------- |
| `db.changelog-master.yaml`                | Master changelog entry point  |
| `changes/005-add-performance-indexes.xml` | Performance-specific indexes  |
| `db.changelog-performance-indexes.xml`    | Additional performance tuning |

---

## 8. Revision History

See [Document Control](#document-control) for full revision history and approvals.

---

**— End of Document —**

_This document was prepared in compliance with ISO/IEC/IEEE 42010:2022 for the BuildNest E-Commerce Platform._

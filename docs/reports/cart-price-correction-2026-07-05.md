# Cart/Order Price Correction — #305 Follow-up (2026-07-05)

## Context

Issue #305 found that `CartServiceImpl.addToCart` snapshotted `product.price` (full price)
instead of `product.discountPrice` when adding a discounted product to cart. The code fix
(committed alongside this report) corrects the add-to-cart path going forward, and Liquibase
changeset `20260705-014-correct-cart-item-prices-for-discounted-products` re-snapshots price
on any **existing, not-yet-checked-out** `cart_items` rows affected by the bug.

This report covers the remaining, deliberately-not-automated part of that issue's checklist:
auditing whether **already-placed orders** (`order_items`) were overcharged by the same bug.

## Why this isn't an automatic data migration

`order_items` rows are historical, already-billed financial records — once an order is placed,
its line-item price is what the customer was actually charged (and, for `PAID` orders, what
was actually captured via the payment gateway). Silently rewriting `order_items.price` after
the fact would make the stored order record disagree with:

- Any payment amount already captured/settled for that order
- Any invoice, receipt, or order-confirmation the customer already saw
- Analytics/reporting that has already run against the original figures

Correcting this requires a business decision (partial refund? store credit? no action for
orders below a threshold?) that belongs to whoever owns billing/finance, not a code fix.
This report is the audit input for that decision — it identifies the affected orders; it does
not change any data.

## Query to identify affected historical orders

Run against the production/staging database (read-only):

```sql
SELECT
    oi.id            AS order_item_id,
    oi.order_id,
    o.user_id,
    o.status          AS order_status,
    o.created_at      AS order_placed_at,
    oi.product_id,
    p.name            AS product_name,
    oi.quantity,
    oi.price          AS charged_price,
    p.discount_price   AS current_discount_price,
    p.price            AS current_full_price,
    (oi.price - p.discount_price) * oi.quantity AS overcharge_amount
FROM order_items oi
JOIN orders o    ON o.id = oi.order_id
JOIN products p  ON p.id = oi.product_id
WHERE p.discount_price IS NOT NULL
  AND p.discount_price < p.price
  AND oi.price = p.price
ORDER BY overcharge_amount DESC;
```

**Caveat:** this query compares against the product's *current* `discount_price`, not the
discount price that was in effect at the time each order was placed (no price-history table
exists in this schema). If a product's discount has changed since an order was placed, this
query may under- or over-count that order. Treat the result as a starting point for manual
review, not a final reconciliation figure.

## Recommended next steps (not performed by this change)

1. Run the query above against production data.
2. For each flagged order, cross-check `order_status` — `PENDING`/`PAYMENT_FAILED` orders were
   never actually charged the wrong amount (no successful capture occurred), so they need no
   remediation beyond the code fix already applied.
3. For `PAID`/`CONFIRMED`/`SHIPPED`/`DELIVERED` orders in the result set, decide on a
   remediation path (refund the difference, issue store credit, or accept as a known
   historical discrepancy below a materiality threshold) — a business decision, not an
   engineering one.
4. If a price-history table would prevent this class of ambiguity in the future
   (e.g. an `order_item_price_at_time` audit trail), file a separate enhancement issue —
   out of scope for #305 itself.

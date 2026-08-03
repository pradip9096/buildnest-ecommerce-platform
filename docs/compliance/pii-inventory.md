# PII Inventory — BuildNest (GDPR, #128 / COMP-01)

This document satisfies issue #128's acceptance criterion "PII inventory
documented: fields, tables, retention period". It is the authoritative map of
where personally identifiable information lives in BuildNest's schema, and how
it is exported/erased under the features this issue introduces.

## Fields, Tables, Retention

| Table | PII field(s) | Retention | Erasure mechanism |
| :--- | :--- | :--- | :--- |
| `users` | `username`, `email`, `first_name`, `last_name`, `phone_number` | Until account deletion + 30 days | `AccountAnonymizationScheduler` overwrites with an irreversible placeholder 30 days after `deleted_at` |
| `users` | `password` (bcrypt hash) | Until account deletion | Row retained (see below), hash is not directly identifying |
| `users` | `last_login` | Until account deletion + 30 days | Cleared implicitly — no longer meaningful post-anonymization |
| `users` | `consent_given`, `consent_at` | Lifetime of account | Not erased — the consent record itself is the compliance evidence (GDPR Art. 7(1) requires being able to demonstrate consent was given) |
| `addresses` | `street_address`, `city`, `state`, `postal_code`, `country` | Lifetime of account | Retained after anonymization — addresses stay linked to historical `orders` for financial/audit retention; not independently exported/anonymized in this iteration (tracked as a known scope gap, see Follow-ups) |
| `orders` | Linked to `user_id`; `shipping_address` | 7 years (financial record retention) | Never anonymized — the `user_id` FK is preserved so historical orders stay attributable for tax/audit purposes; the `User` row's own identity fields are anonymized instead |
| `product_review`, `seller_review` | `user_id` FK; free-text `comment` may contain PII the reviewer chose to include | Lifetime of review | Not anonymized in this iteration — reviews reference the anonymized `User` row and continue to display under the placeholder name |
| `wishlist`, `cart` | `user_id` FK only, no direct PII | Lifetime of account | Deleted naturally on account use; no separate erasure needed |
| `refresh_token`, `password_reset_token` | `user_id` FK only | Short-lived (minutes–days) | Already covered by the existing `TokenCleanupScheduler`; also explicitly revoked at deletion time |

## Why Anonymize, Not Delete

`Order`, `ProductReview`, `SellerReview`, `Cart`, `Wishlist`, and `Address` all
carry a non-nullable `@ManyToOne User` foreign key (`spring/jpa.md`'s cascade
table). Deleting the `User` row outright would either cascade-delete years of
order/financial history (unacceptable — 7-year tax retention) or violate the
FK constraint. GDPR's right to erasure is satisfied by rendering the data
**no longer identifying** (anonymization), which does not require deleting
rows that reference it — see GDPR Recital 26.

## Export Scope (`GET /api/user/data-export`)

Returns the caller's own: profile, addresses, orders (order number, status,
total, date — not full line items), product reviews, seller reviews, wishlist
product names, cart product names. See
`model/dto/UserDataExportDTO.java` for the exact projected shape.

## Erasure Scope (`DELETE /api/user/account`)

Immediate: soft-delete (`is_deleted=true`), account deactivation
(`is_active=false`, blocking further login), refresh-token revocation.
30 days later: `AccountAnonymizationScheduler` overwrites `username`,
`email`, `first_name`, `last_name`, `phone_number` with an irreversible
per-account placeholder and stamps `anonymized_at`.

## Known Scope Gaps (Follow-ups)

- `addresses` and free-text review `comment` fields are not themselves
  anonymized — only the owning `User` row's identity fields are. If a
  review's `comment` text contains self-disclosed PII (a name, phone number
  typed into the text), it is not scrubbed by this iteration.

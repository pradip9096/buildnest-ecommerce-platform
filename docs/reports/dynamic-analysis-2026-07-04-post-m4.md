# BuildNest — Dynamic Analysis Report (Post-M4 Runtime Assessment)

**Date:** 2026-07-04
**Analyst:** Claude Code (autonomous dynamic analysis)
**Method:** Live execution via `./start.sh`, browser-driven exploration (chrome-devtools MCP), direct API verification via `curl`, direct DB inspection via `docker exec mysql`, and source-level root-cause tracing for every finding.
**Relationship to prior report:** [`dynamic-analysis-2026-07-04.md`](dynamic-analysis-2026-07-04.md) was run *before* this session's M4 frontend work (issues #293–#300, #244–#249). This report re-validates that all fixes from that pass hold under live execution, and independently discovers issues untouched by that work — primarily backend defects the prior pass never reached because its cart bug blocked checkout before this session fixed it.

---

## 1. Executive Summary

BuildNest was started end-to-end via `start.sh` (MySQL, Redis, Elasticsearch already running; Spring Boot backend and Vite frontend launched fresh) and exercised through anonymous browsing, registration, login, cart, checkout, all five account tabs, and — after promoting a test account to `ADMIN` via a direct, user-authorized DB grant (no seeded admin existed) — all five admin dashboard tabs.

**The good news first:** every defect from the prior dynamic-analysis pass that this session's M4 work claimed to fix does hold up live. `user.id`/`user.roles` resolve correctly (not `0`/empty), the Décor category renders without double-encoding, the global Navbar shows Sign In/Sign Out everywhere, quick-add-to-cart works with correct sign-in gating, the address book persists and pre-fills at checkout, and the password-change endpoint sends a JSON body with no credentials in the URL.

**The bad news:** this pass found **6 new defects that block or degrade core commerce flows**, none of which were touched by this session's M4 frontend work because they live entirely in backend services and environment/seed-data gaps:

1. **Checkout is completely blocked in a fresh environment** — zero rows in `shipping_methods`, so every user hits "No shipping options available for your area" at step 2 with no way past it.
2. **A recurring backend anti-pattern makes empty per-user resources look like errors.** Both Cart and Wishlist throw a "not found" exception (surfaced as a red error banner) the first time a brand-new user visits them, instead of returning empty. This is not a one-off bug — it's the same pattern in two different services.
3. **Cart charges full price on discounted products.** `CartServiceImpl` never reads `product.getDiscountPrice()` — every discounted item is added to cart, and would be checked out, at its full undiscounted price. Confirmed by direct DB inspection: a product listed at ₹350 (discounted from ₹380) is stored in `cart_items.price` as `380.00`.
4. **The entire Admin Overview and Users tabs are down** due to an unguarded `!Boolean.getIsDeleted()` unboxing a `null` value from one specific user row — and the exception is caught and silently discarded with no server-side logging at all, making this invisible without manual testing.
5. **The Admin Audit Log tab is broken by a Redis caching bug**: a `@Cacheable` method returns a Spring Data `Page<T>` directly; the first request succeeds and populates the cache, but every subsequent request for the same page fails to deserialize the cached `PageImpl` back into an object, for the full 15-minute cache TTL.
6. **The "Forgot password?" link leads to a blank page.** The backend has full support (`/api/password/forgot`, `/api/password/reset`), but no frontend route or page exists for it.

Additionally, the Admin Inventory tab shows "No inventory data found" for all 8 seeded products — a genuine data gap (the `inventory` table has 0 rows despite `products` having 8), though whether this is a bug or an expected admin-provisioning step is a modeling question addressed in the findings below.

| Severity | Count |
|---|---|
| Critical | 3 |
| High | 3 |
| Medium | 2 |
| Low | 2 |
| Informational | 3 |

---

## 2. Runtime Environment

| Component | Status | Detail |
|---|---|---|
| MySQL 8.2 | ✅ Healthy | Already running (5-day-old container), confirmed healthy by `start.sh`'s wait loop |
| Redis 7 | ✅ Up | Connected, 3ms response time |
| Elasticsearch 8.17.6 | ⚠️ Degraded | `security_exception: missing authentication credentials` — same pre-existing condition as the prior report; not a regression |
| Backend (Spring Boot) | ✅ Started | `Started CivilEcommerceApplication in 67.369 seconds` |
| Frontend (Vite) | ✅ Started | Served on `:5173`, HMR connected |
| Mail (SMTP) | ⚠️ Degraded | `AuthenticationFailedException: no password specified` — expected in local dev |

`/actuator/health` reports overall `DOWN` solely from the ES and mail sub-components; `database`, `redis`, both circuit breakers, `livenessState`, and `readinessState` are all `UP`.

**Startup note:** no failures this run. `start.sh` correctly detected the already-healthy MySQL container and proceeded straight to the backend build without re-provisioning infrastructure.

---

## 3. Discovered Application Model

**Purpose:** E-commerce storefront for construction/home-décor materials (cement, steel, bricks, décor/tiles) — 8 seeded products across 4 categories.

**Frontend routes exercised:** `/` (home, categories, featured products), `/products` (listing, search, filter, sort), `/products/:id`, `/register`, `/login`, `/forgot-password` (broken — see Finding 6), `/cart`, `/checkout` (address step reached; shipping step blocked), `/account` (all 5 tabs: Profile, Orders, Addresses, Wishlist, Security), `/admin` (all 5 tabs: Overview, Orders, Inventory, Users, Audit Log).

**Backend surfaces exercised:** `/api/public/products`, `/api/public/categories`, `/api/auth/register`, `/api/auth/login`, `/api/user/profile`, `/api/user/cart/**`, `/api/user/wishlist`, `/api/user/addresses`, `/api/password/change`, `/api/v1/checkout/**`, `/api/admin/reports/dashboard`, `/api/admin/users`, `/api/admin/audit`, `/api/v1/admin/inventory`, `/api/v1/admin/orders`, `/actuator/health`.

**Auth model:** JWT access + refresh token pair issued on login/register, role-gated (`ROLE_USER`, `ROLE_ADMIN`). Confirmed live: the JWT carries only `sub`/`iat`/`exp` (no roles/id), and the frontend correctly re-fetches `/api/user/profile` to resolve identity and roles rather than trusting the token payload — this is the fix from #279/#280/#292 holding under live execution.

**Data model gap discovered:** `Product.stockQuantity` (used by the storefront for "In Stock" display) and the `inventory` table (used by the Admin Inventory tab) are two independent representations of stock. Products can exist and display correctly in the storefront with zero corresponding `inventory` rows.

---

## 4. Analysis Coverage

| Area | Covered | Result |
|---|---|---|
| Home page, category browse, featured products | ✅ | Works; "no featured products" correctly shown (none flagged `is_featured`) |
| Product listing: search, category filter, sort | ✅ | Works; Décor category renders correctly (double-encoding bug from #283 confirmed fixed) |
| Registration → login → profile | ✅ | End-to-end via real DB-backed responses; identity/roles resolve correctly |
| Quick-add-to-cart (unauthenticated) | ✅ | Correctly shows "Sign in to add" (initial test used a flawed snapshot-timing methodology — see Finding 9 — corrected via direct JS evaluation) |
| Quick-add-to-cart (authenticated) | ✅ | Succeeds, badge updates — but see Finding 3 (price) |
| Cart page (empty, first visit) | ❌ Broken | Finding 1 |
| Cart page (with items) | ✅ | Renders correctly once populated |
| Checkout: address step | ✅ | Saves and links to Address Book correctly |
| Checkout: shipping step | ❌ Blocked | Finding 2 — no shipping methods in DB |
| Checkout: payment/confirm steps | ❌ Not reached | Blocked by Finding 2 |
| Account → Profile | ✅ | Loads and pre-fills correctly |
| Account → Orders | ✅ (empty state only) | No orders exist yet (checkout never completes — Finding 2); empty state renders correctly |
| Account → Addresses | ✅ | Address from checkout correctly persisted and shown as default |
| Account → Wishlist | ❌ Broken | Finding 1 (same pattern as Cart) |
| Account → Security (password change) | ✅ | End-to-end; confirmed JSON body, no credentials in URL (#249 fix holds) |
| Admin RBAC boundary | ✅ | `/admin` correctly inaccessible pre-promotion, correctly accessible post-promotion after re-login (role not cached in stale JWT) |
| Admin → Overview | ❌ Broken | Finding 4 |
| Admin → Orders | ✅ (empty state only) | Correctly empty; not exercised with real data (blocked by Finding 2) |
| Admin → Inventory | ❌ Empty | Finding 7 — data gap |
| Admin → Users | ❌ Broken | Finding 4 (same root cause as Overview) |
| Admin → Audit Log | ❌ Broken | Finding 5 |
| Forgot password flow | ❌ Broken | Finding 6 |

---

## 5. Dynamic Analysis Findings

### Finding 1 (CRITICAL) — Cart and Wishlist throw "not found" errors instead of returning empty for a brand-new user

- **Expected:** A user who hasn't added anything to their cart or wishlist yet should see the existing, already-built empty state ("Your cart is empty" / "Your wishlist is empty").
- **Actual:** They see a red error banner: `"Cart not found: Cart not found for user: 5"` on `/cart`, and `"Error fetching wishlist: Wishlist not found for user: 5"` on the Wishlist tab.
- **Reproduction:** Register a new account → do not add anything to cart or wishlist → visit `/cart` or the Wishlist account tab.
- **Evidence:**
  - Screenshot: `/cart` for a fresh user shows the red banner with a "Retry" link instead of the empty-cart illustration.
  - Network: `GET /api/user/cart/5` → `404`.
  - `CartController.getCart()` (lines 66–75):
    ```java
    try {
        CartResponseDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(new ApiResponse(true, "Cart retrieved successfully", cart));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Cart not found: " + e.getMessage(), null));
    }
    ```
  - `CartServiceImpl.getCartByUserId()` (lines 85–86): `cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId))`.
  - `WishlistServiceImpl` (lines 108–110): identical pattern — `wishlistRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Wishlist not found for user: " + userId))`.
- **Root cause:** Both Cart and Wishlist are lazily created on first *write* (first `addToCart`/`addToWishlist`), but their *read* paths treat "no row yet" as an error rather than "empty." This is not a one-off bug in one controller — it is the same architectural anti-pattern independently implemented in two separate services.
- **Impact:** Every single new user who clicks the cart icon or wishlist tab before adding their first item — which, for a brand-new registration, is the majority of first-session behavior — sees what looks like a broken/erroring application. This is a first-impression defect with very high reach.
- **Recommendation:** Change `getCartByUserId` and `WishlistServiceImpl`'s read methods to return an empty `CartResponseDTO`/`WishlistDTO` when no row exists, rather than throwing. If lazy-creation-on-read is preferred instead (creating an empty Cart/Wishlist row the first time a user is seen), that also works and is consistent with how `addToCart` already behaves — either fix works; throwing on a normal, expected "nothing yet" state does not.

---

### Finding 2 (CRITICAL) — Checkout is completely blocked in this environment: zero shipping methods configured

- **Expected:** After entering a delivery address, the shipping step should offer at least one shipping method for a valid Indian postal code.
- **Actual:** "No shipping options available for your area," with "Continue to Payment" permanently disabled. No path forward exists in the UI.
- **Reproduction:** Log in → add any product to cart → proceed to checkout → fill and submit the address form → arrive at the shipping step.
- **Evidence:**
  - Screenshot: shipping step showing the message and disabled button.
  - Direct DB query: `SELECT id, name, is_active FROM shipping_methods;` → **zero rows returned**.
- **Root cause:** The `shipping_methods` table is never seeded by Liquibase or any other bootstrap mechanism — it is designed to be populated exclusively through the Admin → Shipping Methods CRUD endpoints (`AdminShippingController`, `/api/v1/admin/shipping-methods`), and nothing populates it by default.
- **Impact:** In a genuinely fresh environment (a new developer's first `docker compose up` + `start.sh`, or a freshly provisioned staging/production database), **the entire purchase flow — the core function of an e-commerce application — is unusable until an administrator manually configures at least one shipping method.** This is not a code defect in the traditional sense, but an operational-readiness gap with the same practical severity as one: a new deployment cannot process a single order out of the box.
- **Recommendation:** Add a Liquibase changeset seeding at least one default, always-active shipping method (e.g., "Standard Delivery," flat-rate, all zones) so checkout is functional immediately after a fresh deploy, with admins free to add/adjust methods afterward. Alternatively (or additionally), document this as a required first-run admin setup step if seeding default shipping data is undesirable for business reasons.

---

### Finding 3 (CRITICAL) — Cart charges full price on discounted products, ignoring `discountPrice`

- **Expected:** A product shown with a discounted price (e.g., "OPC 53 Grade Cement," listed at ₹350, struck-through ₹380) should be added to cart and charged at ₹350.
- **Actual:** The cart shows and charges ₹380.00 — the full, undiscounted price — for every unit.
- **Reproduction:** Add "OPC 53 Grade Cement" (or any product with a `discountPrice` set) to cart via quick-add or the product detail page → view the cart.
- **Evidence:**
  - Screenshot: cart line item shows "₹380.00 each" for a product whose listing page shows "₹350 ~~₹380~~".
  - Direct DB query: `SELECT id, name, price, discount_price FROM products WHERE id = 1;` → `price=380.00, discount_price=350.00`. `SELECT * FROM cart_items;` → `price=380.00` stored for both cart line items created during this session.
  - `CartServiceImpl` (line 69): `item.setPrice(product.getPrice());` — no reference to `product.getDiscountPrice()` anywhere in the add-to-cart path.
- **Root cause:** The cart-add logic always snapshots `Product.price`, never checking whether `discountPrice` is set and lower.
- **Impact:** Every discounted product on the storefront is silently overcharged relative to its advertised price. This is a direct pricing/billing integrity defect — customers see one price and would be charged another, all the way through to order confirmation (the same `price` snapshot flows into `OrderItem` at checkout confirmation, per the existing order-creation code path).
- **Recommendation:** Change `item.setPrice(product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice())` (or equivalent null-safe logic) in the add-to-cart path. Audit whether existing carts/orders created before this fix need a data correction pass.

---

### Finding 4 (HIGH) — Admin Overview and Users tabs are completely broken by an unguarded `Boolean` unboxing NPE, silently swallowed with no logging

- **Expected:** The Admin Overview tab shows platform stats (user/product/order counts, revenue); the Users tab lists all registered users.
- **Actual:** Both show a generic error ("Error retrieving dashboard statistics" / "Error retrieving users") and no data.
- **Reproduction:** Log in as an admin → visit `/admin` (Overview tab, default) → click "Users" tab.
- **Evidence:**
  - Screenshots: both tabs showing their respective error text with `0`/no data.
  - Direct DB query: `SELECT id, username, is_deleted FROM users WHERE id = 1;` → `is_deleted = NULL` for user `testuser` (id 1).
  - `AdminServiceImpl.getTotalUsers()` (lines 80–84):
    ```java
    return userRepository.findAll().stream()
            .filter(u -> !u.getIsDeleted())
            .count();
    ```
    `getIsDeleted()` returns a `Boolean` (nullable wrapper, per this project's own JPA convention for nullable columns). `!null` triggers an NPE during auto-unboxing.
  - `AdminReportController.getDashboardStats()` (lines 21–33) and the equivalent user-listing endpoint both catch the exception with a bare `catch (Exception e)` and return a generic message — **with no `log.error(...)` call anywhere in either method.** The real exception is completely invisible server-side.
  - **This same unguarded pattern recurs in 7 call sites** across the codebase (`grep -rn "!.*\.getIsDeleted()"`): `UserServiceImpl.java:63,72`, `AdminServiceImpl.java:29,82,94`, `OrderServiceImpl.java:71,153`. Only the two in `AdminServiceImpl` are currently triggered (confirmed: 0 orders have `is_deleted IS NULL`, but 1 user does), but the same class of failure is latent in every other call site if any future row — from a bulk import, a migration, or a direct DB write — ends up with a `NULL` soft-delete flag.
- **Root cause:** Two compounding defects — (a) at least one legacy user row has `is_deleted = NULL` rather than `false`, and (b) every `!x.getIsDeleted()` call site in the codebase assumes the column is never `NULL`, with no defensive `Boolean.TRUE.equals(...)` or null-coalescing check.
- **Impact:** A single bad data row makes two major admin features (Overview, Users) completely unusable, with zero server-side log trace to diagnose why — an on-call engineer investigating this in production would see generic 500s with no stack trace anywhere.
- **Recommendation:**
  1. Immediate: backfill any `NULL` `is_deleted` values to `false` via a data migration.
  2. Root fix: replace every `!x.getIsDeleted()` with `!Boolean.TRUE.equals(x.getIsDeleted())` (or add a `NOT NULL DEFAULT false` constraint at the DB level via Liquibase and guarantee the application never writes `NULL`, per the project's own JPA nullable-column convention already documented).
  3. Process fix: add `log.error(...)` before every generic `catch (Exception e)` in admin report/user endpoints — silently swallowing exceptions with no logging directly contradicts this project's own resilience/observability conventions.

---

### Finding 5 (HIGH) — Admin Audit Log tab broken by a Redis cache deserialization failure on Spring Data `Page<T>`

- **Expected:** The Audit Log tab lists paginated audit entries.
- **Actual:** "An unexpected error occurred," with "0 entries" — even though 57 real audit log entries exist in the database.
- **Reproduction:** As an admin, visit the Audit Log tab twice in a row (or once via `curl` then once via the browser) for the same page number.
- **Evidence:**
  - First direct `curl` request to `GET /api/admin/audit?page=0&size=20` with a fresh JWT **succeeded**, returning real paginated data (57 total entries, 3 pages).
  - A second, otherwise-identical request from the browser for the same page (`page=0`) **failed** with HTTP 500:
    ```json
    {"statusCode":500,"message":"An unexpected error occurred","error":"Could not read JSON:Cannot construct instance of `org.springframework.data.domain.PageImpl` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)..."}
    ```
  - `AuditLogService.java` (line 163): `@Cacheable(value = "auditLogs", key = "'all-page-' + #pageable.pageNumber") public Page<AuditLog> getAllAuditLogs(Pageable pageable)`.
  - `AuditLogController.java` (line 28) returns this method's result directly as the HTTP response body.
- **Root cause:** This is a well-documented Spring Data + Jackson + Redis pitfall: `PageImpl` serializes to JSON fine (via `GenericJackson2JsonRedisSerializer`) on a cache *write* (first call, cache miss), but Jackson cannot *deserialize* JSON back into a `PageImpl` on a cache *read* (subsequent calls, cache hit) — `PageImpl` has no default constructor or Jackson-compatible creator. Every request for the same page, for the full 15-minute cache TTL after the first one, throws.
- **Impact:** The Audit Log admin feature is broken essentially permanently in normal use — the very first request "poisons" the cache for that page for 15 minutes, and since page 0 is the default/most-requested page, almost every real-world visit after the first hits the failure.
- **Recommendation:** Do not cache a raw `Page<T>` object. Either (a) map to a plain serializable DTO (e.g., a simple `record PageResult<T>(List<T> content, int totalPages, long totalElements, int number)` — exactly the shape `AuditLogPage` already reconstructs on the frontend) before returning/caching, or (b) register a custom Jackson module/mixin for `PageImpl` deserialization on the `ObjectMapper` used by the Redis serializer. Option (a) is simpler and removes the coupling to Spring Data internals entirely.

---

### Finding 6 (MEDIUM) — "Forgot password?" links to a route that doesn't exist; renders a blank page with no message

- **Expected:** Clicking "Forgot password?" on the login page should reach a form to request a password reset (the backend already fully supports this via `/api/password/forgot` and `/api/password/reset`, per `PasswordResetController`).
- **Actual:** Navigating to `/forgot-password` renders a completely blank page — just the Navbar, no content, no error message, no 404 page.
- **Reproduction:** From `/login`, click "Forgot password?" (or navigate directly to `/forgot-password`).
- **Evidence:** Screenshot showing only the Navbar with nothing below it. `App.tsx`'s route table has no entry for `/forgot-password` and no catch-all (`*`) route, so React Router renders nothing for any unmatched path.
- **Root cause:** The frontend has a link to a page that was never built. This is a "half-implemented feature" gap — the backend capability exists and works, but there's no UI for it.
- **Impact:** Medium, not critical — password reset is a secondary flow, not the primary purchase journey — but it's a real dead end for any user who forgets their password, with zero guidance (not even a "coming soon" message), and it's also evidence that the app has no global catch-all/404 page at all, which is worth fixing independently of this specific link.
- **Recommendation:** Either build the `/forgot-password` page (backend is ready) or remove the link until it exists. Separately, add a catch-all `<Route path="*" element={<NotFoundPage />} />` so any future broken/mistyped link degrades to a clear "page not found" instead of a silent blank screen.

---

### Finding 7 (LOW / data-modeling question) — Admin Inventory tab shows no data for any of the 8 seeded products

- **Expected:** Some indication of stock level for each product an admin can manage.
- **Actual:** "No inventory data found," despite 8 real products existing and displaying "In Stock" correctly on the storefront.
- **Evidence:** Direct DB query: `SELECT COUNT(*) FROM inventory;` → `0`. `SELECT COUNT(*) FROM products;` → `8`.
- **Root cause:** `Product.stockQuantity` (a field on the `products` table, used by the storefront's "In Stock (N available)" display) and the separate `inventory` table (used exclusively by the Admin Inventory tab and its adjust/audit workflow) are two independent representations of the same concept. Seed data populated the former but never created corresponding rows in the latter.
- **Impact:** Low severity functionally (the storefront's stock display is unaffected), but it means an admin cannot use the Inventory management tab at all for any of the seeded catalog — every "Adjust" action would need a matching `inventory` row that doesn't exist. This is either a seed-data gap (same category as Finding 2's shipping methods) or a sign that `Product.stockQuantity` and `Inventory` were meant to be kept in sync automatically and aren't.
- **Recommendation:** Clarify the intended relationship between `Product.stockQuantity` and the `Inventory` table. If they're meant to be the same underlying stock figure, either seed `inventory` rows for existing products via Liquibase/a migration, or make the storefront read stock from the `Inventory` table instead of `Product.stockQuantity` to eliminate the dual-source-of-truth risk entirely.

---

### Finding 8 (LOW) — API error response body's `statusCode` field doesn't match the actual HTTP status code

- **Expected:** An error response's `statusCode` field should reflect the actual HTTP status returned.
- **Actual:** `GET /api/admin/reports/dashboard` returns HTTP `500`, but the JSON body reports `"statusCode":400`.
- **Evidence:** `curl -o /dev/null -w "%{http_code}"` → `500`; same request's body → `{"success":false,"message":"Error retrieving dashboard statistics","data":null,...,"statusCode":400}`.
- **Root cause:** Not investigated to the exact line (out of scope for this pass given time budget), but the mismatch is 100% reproducible and suggests the `ApiResponse`/error-wrapper model has a hardcoded or miscalculated `statusCode` field that isn't derived from the actual `ResponseEntity` status in at least this code path.
- **Impact:** Low — doesn't break functionality since the frontend's `client.ts` reads the real HTTP status from the `Response` object, not this field — but it's a data-integrity issue in the API contract itself, and any API consumer (a future mobile client, a third-party integration) trusting this field would be misled about what actually happened.
- **Recommendation:** Audit `ApiResponse`'s error-construction paths and ensure `statusCode` is always derived from the same value used to set the actual HTTP response status, not set independently.

---

### Finding 9 (INFORMATIONAL) — Testing-methodology note: accessibility-snapshot-based verification of transient UI state is timing-sensitive

- **What happened:** Early in this session's testing, clicking the unauthenticated "Add to Cart" button and then immediately taking an accessibility-tree snapshot showed no state change (button still read "Add to Cart"), which looked like the sign-in-gating feature was broken.
- **Investigation:** Direct `evaluate_script` calls — a synchronous click followed by an explicit `setTimeout(200ms)` wait before reading `textContent` — confirmed the button correctly changes to "Sign in to add" and the feature works exactly as designed. The snapshot-based check's round-trip latency (tool call → CDP → browser → response) was long enough to either catch the button *after* its 2-second auto-revert, or the snapshot request itself raced the React re-render.
- **Recommendation for future dynamic-analysis passes:** for any UI feedback with a short-lived transient state (loading spinners, "Added ✓" confirmations, toast messages), prefer `evaluate_script` with an explicit, deliberate wait over `take_snapshot`/`take_screenshot` immediately after a `click` action — the latter's inherent round-trip latency makes it an unreliable way to catch fast state transitions.

---

### Finding 10 (INFORMATIONAL) — Minor cosmetic redundancy and a11y notes, no functional impact

- Login and Register pages both render their own centered "🏗️ BuildNest" logo/link in addition to the global Navbar's logo — a minor visual redundancy (two identical links to `/` on the same page), consistent with the layout decision already made for these two pages during this session's Navbar work (#244) and not a new issue.
- Chrome DevTools flagged the Navbar's search input as "should have an id or name attribute" and noted 3 elements without `autocomplete` attributes on the login/register password fields — both are minor, non-blocking accessibility/autofill heuristic warnings, not WCAG failures (the search input does have an `aria-label`).
- The register→login redirect briefly shows the login form's password field with placeholder bullet characters that, at a glance in a screenshot, resemble a pre-filled value. Confirmed via direct DOM inspection (`input.value === ""`) that this is purely the `placeholder` text, not an actual retained password — not a real defect.

---

## 6. Root Cause Analysis Summary

| Finding | Root Cause Category | Fix Locus |
|---|---|---|
| 1. Cart/Wishlist "not found" | Architectural anti-pattern: read path throws on the same condition the write path treats as normal (lazy creation) | `CartServiceImpl`, `WishlistServiceImpl` |
| 2. Checkout blocked (no shipping) | Missing seed data / operational-readiness gap | Liquibase seed changeset, or documented first-run admin step |
| 3. Cart ignores discount price | Business logic gap — price snapshot never checks `discountPrice` | `CartServiceImpl.addToCart` |
| 4. Admin Overview/Users NPE | Unguarded `Boolean` unboxing + one legacy `NULL` data row + silent exception swallowing (no logging) | `AdminServiceImpl` (+ 5 other latent call sites), `AdminReportController` logging |
| 5. Audit Log cache failure | Well-known Spring Data + Jackson + Redis `Page<T>` (de)serialization incompatibility | `AuditLogService.getAllAuditLogs` — cache a DTO, not a `Page` |
| 6. Forgot-password blank page | Incomplete feature — backend built, frontend page/route never built; no catch-all route exists either | `App.tsx` routing, new `ForgotPasswordPage` |
| 7. Inventory tab empty | Dual data model for stock (`Product.stockQuantity` vs `Inventory` table) with no sync/seed | Data model clarification + seed migration |
| 8. `statusCode` mismatch | API error-response construction not deriving `statusCode` from the actual HTTP status | `ApiResponse` error-construction paths |

---

## 7. Risk and Severity Prioritization

| # | Finding | Severity | Business Risk | Reach |
|---|---|---|---|---|
| 2 | Checkout blocked (no shipping methods) | Critical | Zero orders can complete in a fresh deployment | Every user, every environment without manual admin setup |
| 3 | Cart ignores discount price | Critical | Direct pricing/billing integrity defect; customers charged more than advertised | Every discounted product, every cart |
| 1 | Cart/Wishlist "not found" errors | Critical | Severe first-impression defect — looks like the app is broken | Every new user's first cart/wishlist visit |
| 4 | Admin Overview/Users NPE | High | Admins cannot see platform stats or manage users at all; invisible in logs | Any environment with even one legacy `NULL` `is_deleted` row |
| 5 | Audit Log cache failure | High | Compliance/security audit trail effectively unusable after first read | Any environment with Redis caching enabled (always, per architecture) |
| 6 | Forgot-password blank page | Medium | Dead end for account-recovery flow; no guidance at all | Any user who forgets their password |
| 7 | Inventory tab empty | Low | Admin inventory management non-functional for existing catalog | Any environment relying on `Inventory` table for stock ops |
| 8 | `statusCode` field mismatch | Low | API contract inconsistency; misleading to API consumers, no functional break | Any error response inspected directly |

---

## 8. Remediation Plan

**Immediate (blocks core commerce function):**
1. Seed at least one default active shipping method via a Liquibase changeset (Finding 2).
2. Fix `CartServiceImpl` to snapshot `discountPrice` when set (Finding 3).
3. Change `getCartByUserId` and the equivalent Wishlist read method to return empty results instead of throwing (Finding 1).

**High priority (breaks admin operability, low visibility):**
4. Backfill `NULL` `is_deleted` values; guard all 7 `!x.getIsDeleted()` call sites with a null-safe check (Finding 4).
5. Add `log.error(...)` to every admin-report/user-listing `catch (Exception e)` block (Finding 4).
6. Stop caching raw `Page<T>` in `AuditLogService`; cache a plain DTO instead (Finding 5).

**Medium priority:**
7. Build the `/forgot-password` page, or remove the dead link; add a global catch-all 404 route (Finding 6).

**Lower priority / cleanup:**
8. Clarify and reconcile `Product.stockQuantity` vs. the `Inventory` table; seed or sync as appropriate (Finding 7).
9. Fix the `statusCode` field in `ApiResponse` error construction to match the actual HTTP status (Finding 8).

---

## 9. Final Recommendations

- **This session's M4 frontend fixes hold up under live execution with no regressions found** — identity/roles resolution, the Navbar, quick-add-to-cart, the address book, and the password-change security fix all behave correctly end-to-end in a real browser against a real backend.
- **The highest-value next work is backend, not frontend.** Every new defect this pass found lives in `CartServiceImpl`, `WishlistServiceImpl`, `AdminServiceImpl`, `AuditLogService`, or seed data — none of it touched by the M4 frontend issue backlog just completed.
- **Findings 1 and 4 share a common lesson**: both are instances of "the exact-same anti-pattern independently reimplemented in more than one place" (lazy-resource-throws-on-read in 2 services; unguarded null-Boolean-unboxing in 7 call sites). A single fix pass addressing the *pattern*, not just the one call site that happened to be exercised, would close more latent risk than fixing each symptom individually.
- **Finding 2 (no shipping methods) is worth treating as a release-blocker for any real deployment**, not just a nice-to-have seed-data improvement — as observed directly, checkout cannot complete at all without it.
- Recommend filing GitHub issues for Findings 1–8 individually (matching this project's established workflow of one issue per defect, verified against the repo before implementing) rather than a single umbrella issue, given they have independent root causes and fix locations.

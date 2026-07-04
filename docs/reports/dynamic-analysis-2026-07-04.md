# BuildNest — Dynamic Analysis Report (Runtime Assessment)

**Date:** 2026-07-04
**Analyst:** Claude Code (autonomous dynamic analysis)
**Method:** Live execution via `./start.sh`, browser-driven exploration (chrome-devtools MCP), API verification via `curl`, and source-level root-cause tracing for every finding.

---

## 1. Executive Summary

BuildNest was started end-to-end (MySQL, Redis, Spring Boot backend, Vite frontend) and exercised through its primary customer journeys: browsing, product detail, search, registration, login, and add-to-cart. The application starts successfully and core browsing/auth flows work, but the assessment surfaced **two critical defects** that break real functionality and represent a security weakness, plus several medium/low issues affecting data integrity, API contract quality, and accessibility.

Most significant: **every logged-in user's client-side `id` resolves to `0`**, which cascades into a broken "Add to Cart" flow for all authenticated users, and the backend cart endpoints accept a client-supplied `userId` with **no ownership check** — a textbook IDOR that was independently confirmed to be exploitable regardless of the frontend bug.

| Severity | Count |
|---|---|
| Critical | 2 |
| High | 1 |
| Medium | 3 |
| Low | 3 |
| Informational | 2 |

---

## 2. Runtime Environment

| Component | Status | Detail |
|---|---|---|
| MySQL 8.2 | ✅ Healthy | `docker compose up -d mysql` via `start.sh`; health check passed |
| Redis 7 | ✅ Up | Connected, 3ms response time |
| Elasticsearch 8.10 | ⚠️ Degraded | `security_exception: missing authentication credentials` — ES requires auth that isn't configured for local health checks |
| Backend (Spring Boot) | ✅ Started | `Started CivilEcommerceApplication in 55.792 seconds`, Tomcat on :8080 |
| Frontend (Vite) | ✅ Started | Ready in 2.5s, served on :5173 |
| Mail (SMTP) | ⚠️ Degraded | `AuthenticationFailedException: no password specified` — expected in local dev, not a code defect |

`/actuator/health` reports overall `DOWN` solely because of the ES and mail sub-components above; `database`, `redis`, `livenessState`, and `readinessState` are all `UP`.

**Startup note:** `start.sh` initially failed with `docker: command not found` when launched via a bare `nohup` background shell — Docker Desktop's WSL integration only initializes `docker` on the `PATH` inside an interactive login shell (`bash -lc`). This is an environment/PATH quirk of this workstation, not a defect in `start.sh` itself, but worth documenting since it can make the script appear to fail non-deterministically depending on how it's invoked.

---

## 3. Discovered Application Model

**Purpose:** E-commerce storefront for construction/home-décor materials (cement, steel, bricks, décor/tiles).

**Frontend routes exercised:** `/` (catalog + search + category filter), `/products/:id`, `/register`, `/login`, `/account` (profile/orders/addresses/wishlist/security tabs), `/cart`.

**Backend surfaces exercised:** `/api/public/products`, `/api/public/categories`, `/api/products/:id/reviews`, `/api/auth/register`, `/api/auth/login`, `/api/user/profile`, `/api/user/cart/**`, `/api/admin/**` (RBAC boundary check), `/actuator/health`, `/swagger-ui`.

**Auth model:** JWT access + refresh token pair issued on login/register, role-gated (`ROLE_USER`, `ROLE_ADMIN`) per `spring-security.md`.

---

## 4. Analysis Coverage

| Area | Covered | Notes |
|---|---|---|
| Catalog browsing, search, filtering | ✅ | Search-by-form works; deep-link search does not (see Finding 5) |
| Registration → login → profile | ✅ | End-to-end, verified via network + DB-backed responses |
| Add-to-cart (unauthenticated) | ✅ | Correctly blocked with user-facing message |
| Add-to-cart (authenticated) | ✅ | **Fails** — Finding 1/2 |
| RBAC boundary (admin vs user) | ✅ | Correctly enforced: 401 anonymous, 403 for `ROLE_USER` |
| IDOR probing on user-scoped endpoints | ✅ | Cart confirmed vulnerable; other `user/**` controllers derive identity server-side, not client-supplied |
| Checkout, orders, wishlist, reviews submission | ❌ Not reached | Blocked by Finding 1 (cart never populates) — checkout requires a non-empty cart |
| Admin dashboard UI | ❌ Not exercised | No admin credentials available in this session; RBAC boundary was verified at the API layer instead |

---

## 5. Findings

### Finding 1 (CRITICAL) — Authenticated users cannot add items to cart; every session resolves to `user.id = 0`

- **Expected:** After login, `Add to Cart` should associate the cart with the logged-in user's real ID.
- **Actual:** `POST /api/user/cart/add?userId=0` is sent for every authenticated user, and the backend returns `400 Bad Request`.
- **Evidence:** Browser network trace: `reqid=297 POST http://localhost:5173/api/user/cart/add?userId=0 [400]`, console: `Failed to load resource: 400`.
- **Root cause:** `frontend/src/contexts/AuthContext.tsx`:
  - `tokenToUser()` decodes the JWT payload and reads `payload['id'] ?? payload['userId'] ?? 0`. Per this project's own security rule (`spring-security.md`: *"Token contains only subject (username) + issuedAt + expiration — no roles, no PII in the payload"*), the JWT **never** contains `id` — so `tokenToUser()` always falls through to `0`.
  - In `login()`, the code is: `setUser(parsed ?? { id: tokens.userId, username: tokens.username, roles: ['USER'] })`. Because `tokenToUser()` returns a valid (non-null) object — just with `id: 0` — the `??` fallback containing the **correct** `tokens.userId` from the login API response is never used. The real ID is silently discarded.
  - On page reload, the bug is even more direct: only the JWT-derived (`id: 0`) path runs; `tokens.userId` isn't available at all at that point.
- **Impact:** Cart, and any other client feature keyed off `user.id`, is broken for 100% of authenticated sessions.
- **Fix direction:** Store the real user ID (from the login/register response, or from `/api/user/profile`) in a piece of state that survives reload — do not derive identity from the JWT payload, since the JWT deliberately excludes it.

### Finding 2 (CRITICAL / Security) — IDOR on `CartController`: client-supplied `userId` has no ownership check

- **Expected:** A cart endpoint scoped to "the current user" must derive that user from the authenticated principal, or verify `#userId == authentication.principal.id`, per this project's own documented pattern in `spring-security.md` (`@PreAuthorize("... or #userId == authentication.principal.id")`).
- **Actual:** `CartController` (`addToCart`, `getCart`, `clearCart`, `getCartTotal`) accepts `userId` as a plain `@RequestParam`/`@PathVariable` with only `@PreAuthorize("hasRole('USER')")` at the class level — no ownership check.
- **Evidence (exploit reproduction):** Using a valid JWT for `testuser_dyn1` (a newly-registered account), requested another user's cart directly:
  ```
  curl -H "Authorization: Bearer <testuser_dyn1 token>" http://localhost:8080/api/user/cart/1
  → {"success":false,"message":"Cart not found: Cart not found for user: 1", ...} HTTP 404
  ```
  The request was **not rejected for authorization** — it reached the service layer and only failed because cart ID 1 doesn't exist. Any authenticated user can query, and (via `addToCart`/`clearCart`) mutate, any other user's cart by ID.
- **Impact:** Broken Object Level Authorization (OWASP API1:2023). An attacker with any valid account can enumerate `userId` values to read or manipulate other customers' carts.
- **Fix direction:** Derive `userId` server-side from `SecurityContextHolder`/the authenticated principal rather than trusting the client-supplied value, or add the ownership-check pattern already used elsewhere in the codebase via `RolePermissionEvaluator`.

### Finding 3 (HIGH / Security) — JWT access + refresh tokens stored in `localStorage`

- **Expected:** Per this project's own rule (`react/security.md`: *"Never store sessions in `localStorage` — accessible to any XSS. Use httpOnly secure cookies."*).
- **Actual:** Confirmed via `localStorage` dump after login:
  ```json
  {"refresh_token": "43f93b2c-...", "access_token": "eyJhbGciOiJIUzUxMiJ9..."}
  ```
- **Impact:** Any XSS vulnerability anywhere in the app (including in third-party scripts) can exfiltrate both the access and refresh token, leading to full session takeover with no expiry-limited blast radius (refresh tokens are long-lived, per `jwt.secret.previous`/rotation design in `spring-security.md`).
- **Fix direction:** Move to httpOnly, `Secure`, `SameSite` cookies issued by the backend, with CSRF protection reintroduced for the cookie-auth surface (currently disabled, which is correct *only* for the current stateless-header model).

### Finding 4 (MEDIUM / Data Integrity) — "Décor" category name is corrupted (`DÃ©cor` / `DÃ©COR`) at the database layer

- **Expected:** Category name renders as "Décor".
- **Actual:** API and UI consistently show `"DÃ©cor"` (and uppercased `"DÃ©COR"` in product listings) — classic UTF-8-interpreted-as-Latin-1 double-encoding (mojibake).
- **Evidence:** Raw API response: `curl http://localhost:8080/api/public/categories` → `"name":"DÃ©cor","description":"Home dÃ©cor and finishes"`. This is not a frontend rendering bug — the corrupted bytes come from the database itself.
- **Root cause investigation:** Searched all Liquibase changesets and `data.sql` in the repo for this seed value — none contain it (the versioned `data.sql` seed uses different category names entirely, e.g. "Steel & Iron", "Bricks & Blocks", which don't match what's actually in the running DB). This indicates the row was inserted outside of any versioned migration (e.g., manually, or by a now-lost seeding step) into the persistent Docker volume, with the client connection encoding mismatched at insert time. The JDBC URL (`SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/buildnest_ecommerce`) also lacks explicit `useUnicode=true&characterEncoding=UTF-8` — while MySQL Connector/J 8.x normally negotiates this automatically, adding it explicitly removes ambiguity for future inserts.
- **Impact:** User-visible garbled text in the "Décor" category everywhere it's displayed.
- **Fix direction:** Correct the stored value with a targeted `UPDATE` (or a proper Liquibase changeset if this data should be versioned/reproducible), and add `characterEncoding=UTF-8` to the datasource URL to prevent recurrence.

### Finding 5 (MEDIUM) — Deep-linked/bookmarked search URLs are silently ignored on page load

- **Expected:** Navigating directly to `http://localhost:5173/?search=nonexistentxyz` should show filtered results, since the UI reflects the term in the URL after a manual search.
- **Actual:** Loading that URL directly shows all 8 unfiltered products with no search applied; the search box appears empty. Manually re-typing the same term into the search box and submitting **does** correctly filter (`GET /api/public/products/search?keyword=nonexistentxyz` → 0 results).
- **Impact:** Bookmarked/shared search links, and browser back/forward through search history, don't restore the expected filtered view — a real (if lower-severity) functional/UX defect.
- **Fix direction:** On mount, read the `search` query param and hydrate both the search box value and the initial fetch from it, rather than relying solely on the form's `onSubmit` handler.

### Finding 6 (LOW / API Contract) — Dates serialize as JSON integer arrays instead of ISO-8601 strings

- **Actual:** Every timestamp field (`createdAt`, `updatedAt`, `timestamp`) returns as `[2026,6,29,15,46,13]` rather than a string like `"2026-06-29T15:46:13"`.
- **Root cause:** `config/JacksonConfig.java`: `return new ObjectMapper().findAndRegisterModules();` — this registers `JavaTimeModule` but does not disable `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS` (defaults to `true` in a bare `ObjectMapper`). Spring Boot's autoconfigured `ObjectMapper` normally disables this by default, but this custom `@Bean` overrides that with a fresh, un-configured instance.
- **Impact:** Non-standard, harder-to-consume API responses for any client (including the current frontend, which appears not to render these dates and so hasn't surfaced the problem yet).
- **Fix direction:** `new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)`.

### Finding 7 (LOW / Accessibility) — Form fields missing labels/ids across auth and account forms

- **Evidence:** Browser console accessibility issues: *"No label associated with a form field (count: 5)"*, *"A form field element should have an id or name attribute (count: 5)"* — observed on both the login form and the account/profile form.
- **Impact:** Screen reader users cannot reliably identify these fields; fails WCAG label-association requirements.
- **Fix direction:** Add `id`/`htmlFor` pairing or `aria-label` to each input in `LoginPage.tsx` and the account profile form.

### Finding 8 (LOW / Performance) — Duplicate API calls per page load

- **Evidence:** Every page observed (`/`, `/products/:id`) issued each `GET` request twice in immediate succession (e.g., `products`, `categories`, `reviews`, `reviews/summary` all doubled).
- **Likely cause:** React 19 `StrictMode` double-invoking effects in development — this is expected dev-only behavior, not a production defect, but was not confirmed against a production build in this session.
- **Recommendation:** Confirm with a production build (`npm run build && npm run preview`) that requests are not actually duplicated outside of dev StrictMode; if they are, look for a missing dependency-array guard or duplicate `useEffect` registration.

### Informational — Elasticsearch and Mail health checks fail in local dev

- Elasticsearch: `security_exception: missing authentication credentials` — local ES instance has security enabled but the app's ES client isn't configured with credentials for the health check.
- Mail: `AuthenticationFailedException: no password specified` — expected, since no SMTP credentials are configured for local dev.
- Both are graceful-degradation paths already, consistent with `resilience4j.md`'s documented fallback tiers; flagged here only for completeness, not as defects.

---

## 6. Root Cause Analysis Summary

| Finding | Root Cause Location | Class of Bug |
|---|---|---|
| 1 | `frontend/src/contexts/AuthContext.tsx` — `tokenToUser()` / `??` fallback logic | Logic error (nullish coalescing misuse against a value that's never actually null) |
| 2 | `backend/.../controller/user/CartController.java` | Missing authorization check (IDOR) |
| 3 | `frontend/src/contexts/AuthContext.tsx` — `storeTokens()` | Insecure storage choice, contradicts project's own security rule |
| 4 | Persisted DB row (not present in any versioned migration) + missing explicit JDBC charset params | Data corruption / encoding mismatch |
| 5 | Frontend catalog page — URL search param not read on mount | Missing state hydration |
| 6 | `backend/.../config/JacksonConfig.java` | Misconfigured `ObjectMapper` bean |
| 7 | Various auth/account form JSX | Missing accessibility attributes |
| 8 | Dev-mode React `StrictMode` (likely) | Expected dev behavior, unconfirmed in prod build |

---

## 7. Risk and Severity Prioritization

1. **Finding 2 (IDOR)** — exploitable now, affects data confidentiality/integrity of other users' carts. Fix immediately.
2. **Finding 1 (user.id=0)** — blocks a core purchase flow (cart) for every user; also the reason Finding 2 wasn't caught by normal use. Fix immediately, alongside Finding 2 (fixing 1 alone does not close the IDOR).
3. **Finding 3 (localStorage tokens)** — latent risk, requires a separate XSS to exploit, but violates the project's own hard security rule. Fix soon.
4. **Finding 4 (Décor encoding)** — user-visible but not functionally blocking. Fix soon.
5. **Finding 5 (deep-link search)** — moderate UX defect. Fix at convenience.
6. **Finding 6 (date serialization)** — contract quality issue, no current consumer breakage observed. Fix at convenience.
7. **Finding 7 (a11y labels)** — compliance gap. Fix at convenience.
8. **Finding 8 (duplicate requests)** — verify against prod build before prioritizing further.

---

## 8. Remediation Plan

| Priority | Finding | Action | Owner Area |
|---|---|---|---|
| P0 | 2 | Derive `userId` from `SecurityContextHolder` in `CartController`, or enforce `#userId == authentication.principal.id` via `RolePermissionEvaluator`, matching the pattern already documented in `spring-security.md` | Backend / Security |
| P0 | 1 | Fix `AuthContext.login()` to always trust the login-response `userId` over the (deliberately ID-less) JWT payload; persist it (e.g., via a follow-up `/api/user/profile` call on reload) so it survives page refresh | Frontend |
| P1 | 3 | Migrate access/refresh tokens to httpOnly Secure cookies; reintroduce CSRF protection scoped to the cookie-auth path | Frontend + Backend |
| P1 | 4 | Correct the "Décor" category row via a proper migration; add `characterEncoding=UTF-8&useUnicode=true` to the JDBC URL | Backend / Data |
| P2 | 5 | Hydrate search state from the URL on mount in the catalog page | Frontend |
| P2 | 6 | Disable `WRITE_DATES_AS_TIMESTAMPS` in `JacksonConfig` | Backend |
| P2 | 7 | Add proper label/id associations to auth and account forms | Frontend |
| P3 | 8 | Verify duplicate requests disappear in a production build; investigate further only if they persist | Frontend |

---

## 9. Final Recommendations

- Treat Findings 1 and 2 as a single remediation unit — they compound each other (the frontend bug masked the backend vulnerability from ever manifesting visibly during normal QA, since `userId=0` just 400s instead of silently succeeding against a real stranger's cart). Add an integration test that specifically asserts a second authenticated user cannot read/mutate another user's cart, to prevent regression.
- Add a lint/code-review check for any `@RequestParam`/`@PathVariable` named `userId` (or similar) in `user/**` controllers that isn't cross-checked against `authentication.principal` — this project already has the correct pattern documented in `spring-security.md`; it just wasn't applied to `CartController`.
- Blocked/untested areas for a follow-up session: checkout flow, order placement, wishlist, review submission, and the admin dashboard UI — all require a working cart (Finding 1) or admin credentials to reach.

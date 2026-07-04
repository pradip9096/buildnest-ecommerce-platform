# Frontend Anti-Pattern Audit — 2026-07-04

**Scope:** `frontend/src/` (React 19 + Vite, TypeScript). 65 source files reviewed (pages, components, hooks, contexts, API client layer, types). No `.tsx`/`.ts` files were excluded.

**Method:** Full directory read of `src/`, targeted `grep` sweeps for known anti-pattern signatures (duplicated layout markup, `localStorage` usage, `any` typing, swallowed errors, missing route guards, missing `res.ok` checks, index-as-key, unlabeled inputs, `dangerouslySetInnerHTML`, unsafe `target="_blank"`), followed by full reads of the files each sweep implicated. Findings are graded by confirmed evidence (file + line), not inference.

**Not in scope:** backend code, build/CI configuration, visual/design review (see `#291`), performance profiling under load.

---

## Summary Table

| # | Finding | Severity | Status |
|---|---|---|---|
| 1 | No shared layout — every page duplicates its own header | High | Tracked: `#244` (open) |
| 2 | Zero automated tests; no test framework installed | High | Untracked |
| 3 | JWT access/refresh tokens stored in `localStorage` | High | Tracked: `#282` (open, deferred) |
| 4 | No route/role guarding at the router level — duplicated inline per page | High | Untracked |
| 5 | `apiRefresh()` defined but never called — no silent token renewal | Medium | Untracked |
| 6 | No shared HTTP client — 11 hand-rolled API modules, inconsistent `res.ok` handling | Medium | Untracked |
| 7 | `OrdersTab.tsx` bypasses the `api/` layer with an inline `fetch()`, no status check | Medium | Untracked |
| 8 | Repeated fetch/loading/error boilerplate instead of a shared data hook | Medium | Untracked |
| 9 | 48 `<button>` elements without explicit `type="button"` | Medium | Untracked |
| 10 | No error boundary anywhere in the tree | Medium | Untracked |
| 11 | `key={idx}` used for mutable order-item lists (2 sites) | Low | Untracked |
| 12 | Duplicate component name `OrdersTab` in two unrelated directories | Low | Untracked |

Everything checked and **ruled out** (evidence-based, not just "not found"): `dangerouslySetInnerHTML`, unsafe `target="_blank"`, `any`/`as any` typing, missing `alt` text on `<img>`, `console.log`/`console.debug` in source, `window.location` navigation bypassing the router.

---

## Findings

### 1. No shared layout — duplicated per-page headers
**Severity: High** · **Files:** `pages/HomePage.tsx:29-53`, `CheckoutPage.tsx`, `OrderConfirmationPage.tsx`, `ProductDetailPage.tsx`, `CartPage.tsx`, `ProductListingPage.tsx`, `AccountPage.tsx`, `AdminDashboardPage.tsx` (8 of 10 pages)

Every page inlines its own `<header>` JSX — logo text, search bar, nav links — rather than composing a single layout component mounted once in `App.tsx`. None of these local headers read from `AuthContext`, so there is no sign-in/account/logout affordance anywhere in the app despite the auth system being fully implemented underneath. This was the originating observation for this audit (see `#244`).

**Recommendation:** Extract a single `<Navbar>` (or `<AppShell>`) component, mount it once around `<Routes>` in `App.tsx`, wire it to `useAuth()`, and delete the duplicated per-page `<header>` blocks.

**Remediation steps:**
1. Build `components/common/Navbar.tsx` per the acceptance criteria in `#244` (logo, search, cart badge, account/sign-in control, responsive hamburger).
2. Mount it once in `App.tsx`, outside `<Routes>`.
3. Remove the local `<header>` from each of the 8 pages listed above.
4. Confirm no page-specific header behavior (e.g., checkout step context) is lost in the removal — port anything essential into page body content instead of the header.

---

### 2. Zero automated tests; no test framework installed
**Severity: High** · **Files:** entire `frontend/` tree; `package.json`

`find src -name "*.test.*"` returns nothing. `package.json` has no `vitest`, `@testing-library/react`, `jsdom`, or any test runner in `devDependencies`, and no `test` script. This violates the project's own mandatory rule (`.claude/rules/common/testing.md`): 80% minimum coverage, unit + integration + E2E required, TDD workflow mandatory for new code.

This is the single largest gap in the audit — every other finding below (route guards, token refresh, error handling) is currently unverifiable by anything other than manual inspection, because there is no regression safety net.

**Recommendation:** Stand up a test harness before further feature work lands (bad interaction with `#244`, `#246`, `#247`, `#248` all being open and untested-by-construction).

**Remediation steps:**
1. Add `vitest`, `@testing-library/react`, `@testing-library/user-event`, `jsdom` as dev dependencies; wire a `test` script.
2. Prioritize coverage on `AuthContext` (token lifecycle), `useCart`, and the API client layer first — these carry the highest correctness risk (see findings 3, 5, 6, 7).
3. Add a CI gate mirroring the backend's coverage enforcement once a baseline exists.

---

### 3. JWT tokens stored in `localStorage`
**Severity: High** · **File:** `contexts/AuthContext.tsx:54-55, 59-60, 72, 111`

`storeTokens()` / `clearTokens()` read and write `access_token` and `refresh_token` via `localStorage`. Per this project's own `react/security.md`: *"Never store sessions in `localStorage` — accessible to any XSS. Use httpOnly secure cookies."* Any successful XSS anywhere in the app (including a future third-party dependency) can exfiltrate both tokens.

**Status:** Already tracked as `#282` and explicitly deferred by product decision in a prior session. Restating here for completeness of the audit, not as a new finding.

**Recommendation (unchanged from #282):** Migrate to httpOnly, secure, `SameSite` cookies issued by the backend, with CSRF protection added per the security rules for cookie-based auth. Requires backend coordination (`/api/auth/login` and `/refresh` would need to set cookies rather than return tokens in the JSON body).

---

### 4. No route/role guarding at the router level
**Severity: High** · **Files:** `App.tsx:14-33`, `pages/AccountPage.tsx:21-52`, `pages/AdminDashboardPage.tsx:22-40`

`App.tsx` registers `/account` and `/admin` as plain `<Route>` entries with no wrapper — every route is reachable by an unauthenticated client-side navigation. Each page then repeats its own inline guard:

```tsx
// AdminDashboardPage.tsx
const isAdmin = isAuthenticated && (user?.roles?.includes('ADMIN') ?? false);
if (!isAuthenticated) { /* render lock screen */ }
```

```tsx
// AccountPage.tsx
if (!isAuthenticated) { /* ... */ }
```

Functionally this is not a security hole — the backend enforces RBAC independently (`SecurityConfig`, `@PreAuthorize`, confirmed in `spring-security.md`) — but it is the same duplicated-cross-cutting-concern anti-pattern as finding 1: the same guard logic is hand-copied into every protected page instead of factored into one place, and any new protected page (e.g., a future `/admin/settings`) has to remember to re-implement it correctly.

**Recommendation:** Add a `<RequireAuth>` / `<RequireRole>` wrapper component and apply it at the route-definition level in `App.tsx`.

**Remediation steps:**
1. Create `components/common/RequireAuth.tsx` (redirect to `/login` if `!isAuthenticated`, optionally accepting a `role` prop for admin-only routes).
2. Wrap `/account` and `/admin` routes in `App.tsx`:
   ```tsx
   <Route path="/admin" element={<RequireAuth role="ADMIN"><AdminDashboardPage /></RequireAuth>} />
   ```
3. Remove the duplicated inline guard blocks from `AccountPage.tsx` and `AdminDashboardPage.tsx`.

---

### 5. `apiRefresh()` defined but never invoked
**Severity: Medium** · **Files:** `api/auth.ts:30-39` (definition), no call sites found anywhere in `src/`

`apiRefresh` exists and correctly calls `/api/auth/refresh`, but nothing in `AuthContext.tsx` or elsewhere calls it. The only place a token's expiry is checked is `restoreSession()` on initial app load (`AuthContext.tsx:71-77`) — if the access token expires *during* an active session (backend default 24h, production-recommended 15min per `spring-security.md`), there is no silent renewal. The user will start receiving 401s from every subsequent API call until they reload the page, at which point `restoreSession` sees the expired token and force-logs them out — losing in-progress work (e.g., mid-checkout).

**Recommendation:** Implement transparent refresh-on-401, or a proactive refresh timer keyed to the token's `exp` claim (already decoded via `decodeJwtPayload`, so the expiry timestamp is available).

**Remediation steps:**
1. In the shared HTTP client (see finding 6), catch `401` responses, attempt one `apiRefresh()` call using the stored refresh token, retry the original request once, and only then force logout on repeated failure.
2. Alternatively/additionally, schedule a refresh via `setTimeout` in `AuthContext` a few seconds before `exp`.
3. Add a test (per finding 2) asserting a session survives across a simulated access-token expiry.

---

### 6. No shared HTTP client — 11 hand-rolled API modules
**Severity: Medium** · **Files:** all of `api/*.ts` (507 lines total: `addresses.ts`, `admin.ts`, `auth.ts`, `cart.ts`, `categories.ts`, `checkout.ts`, `orders.ts`, `products.ts`, `reviews.ts`, `user.ts`, `wishlist.ts`)

Every API module reimplements `fetch()`, header construction, and JSON parsing independently. There is no `apiClient`/`httpClient` module (`grep` for one returned nothing). This is directly responsible for two concrete inconsistencies found during this audit:

- `res.ok` status checks are present in only some call sites per file — e.g. `api/orders.ts` has one fetch and one `res.ok` check (consistent), but files like `api/categories.ts` (1 fetch, 1 check) versus others show the check is not applied uniformly across every exported function in every file; each function must remember to add it individually, and at least one call site downstream (finding 7) skips it entirely.
- Auth-header construction (`Authorization: Bearer ${token}`) is copy-pasted per file rather than centralized, so a future change to token format or an added header (e.g., a CSRF token, relevant if finding 3 is ever remediated) requires touching 11 files.

**Recommendation:** Introduce a single `api/client.ts` wrapping `fetch` with: base URL, standard headers, automatic `Authorization` injection, uniform `res.ok` → thrown-`Error` handling, and a single seam for the 401-refresh-retry logic from finding 5.

**Remediation steps:**
1. Create `api/client.ts` exporting `request<T>(path, options)` that throws on non-2xx with a consistent `Error` shape (message from `ApiResponse.message` where present).
2. Migrate the 11 existing modules to call `request()` instead of `fetch()` directly, function by function — safe to do incrementally since each module's public exports (function signatures) don't need to change.
3. Wire the 401-retry-with-refresh logic (finding 5) into this single seam.

---

### 7. `OrdersTab.tsx` bypasses the API layer, silently masks failures
**Severity: Medium** · **File:** `components/account/OrdersTab.tsx:24-30`

```tsx
useEffect(() => {
  fetch('/api/user/orders', { headers: { Authorization: `Bearer ${token}` } })
    .then(r => r.json())
    .then(body => setOrders(Array.isArray(body.data) ? body.data : []))
    .catch(e => setError(e instanceof Error ? e.message : 'Failed to load orders'))
    .finally(() => setLoading(false));
}, [token, userId]);
```

Two problems, both confirmed by reading the code:

- It calls `fetch()` directly instead of using `api/orders.ts` (which only exports `fetchOrderById`, not a list function — the API layer is genuinely incomplete here, not just bypassed by choice).
- **There is no `res.ok` check.** If the request 401s (e.g., an expired token — see finding 5) or 500s, `r.json()` will still resolve (the backend's `GlobalExceptionHandler` returns a JSON error body), `body.data` will be `undefined`, `Array.isArray(undefined)` is `false`, and the component silently renders **"You haven't placed any orders yet"** — indistinguishable from a genuinely empty order history. A real auth or server failure is presented to the user as a false negative, not surfaced as an error.

**Recommendation:** Add `fetchOrders(userId, token)` to `api/orders.ts` with a proper `res.ok` check, and have `OrdersTab.tsx` call it instead of using `fetch()` inline.

**Remediation steps:**
1. Add to `api/orders.ts`:
   ```ts
   export async function fetchOrders(token: string): Promise<Order[]> {
     const res = await fetch(BASE, { headers: authHeaders(token) });
     if (!res.ok) throw new Error(`Failed to fetch orders (${res.status})`);
     const body: ApiResponse<Order[]> = await res.json();
     return body.data;
   }
   ```
2. Replace the inline `fetch` in `OrdersTab.tsx` with a call to `fetchOrders`.
3. Verify the empty-state message only renders when `orders.length === 0` **and** no error occurred (already structurally true once the `catch` path is reachable correctly).

---

### 8. Repeated fetch/loading/error boilerplate instead of a shared data hook
**Severity: Medium** · **Files:** `hooks/useProduct.ts`, `hooks/useProducts.ts`, `hooks/useCategories.ts`, `hooks/useFeaturedProducts.ts`, `hooks/useReviews.ts`, plus inline duplicates in `components/admin/{OverviewTab,InventoryTab,UsersTab,AuditLogTab,OrdersTab}.tsx`, `components/account/{ProfileTab,WishlistTab,OrdersTab}.tsx`, and page-level fetches in `CheckoutPage.tsx`, `OrderConfirmationPage.tsx`

The same three-state pattern (`data`/`loading`/`error`, fetch-in-`useEffect`, `cancelled` flag or `.finally(() => setLoading(false))`) is hand-written at least 15 times across hooks, admin tabs, and account tabs, rather than factored into one generic hook (e.g., `useAsync<T>(fetcher)` or a small data-fetching library). This is not a correctness bug on its own, but it is the same root cause as findings 1 and 4 — cross-cutting concerns re-implemented per call site instead of centralized — and it means a fix like finding 7's missing status check has to be manually verified in every one of these ~15 sites rather than fixed once.

**Recommendation:** Extract a shared `useAsync`/`useFetch` hook (or adopt a small library such as TanStack Query, which also solves caching/refetch/dedup for free) and migrate the hand-rolled call sites incrementally.

**Remediation steps:**
1. Write `hooks/useAsync.ts` encapsulating the `data`/`loading`/`error` + cancellation pattern already used correctly in `useProduct.ts`/`useReviews.ts` (those two are the cleanest existing examples to generalize from).
2. Migrate the admin and account tab components (highest duplication count) first.
3. Longer-term, evaluate TanStack Query given the project already has repeated cache-invalidation-shaped needs (cart reload after mutation in `useCart.ts`, orders reload, etc.).

---

### 9. 48 `<button>` elements without explicit `type="button"`
**Severity: Medium** · **Files:** widespread (48 occurrences across the component tree)

HTML defaults an un-typed `<button>` inside a `<form>` to `type="submit"`. Any of these 48 buttons that render inside a `<form>` (checkout steps, login, register, address forms, security/password forms) will trigger an unintended form submission if clicked — a classic, well-documented React/HTML footgun. This audit did not individually verify which of the 48 sit inside a `<form>` versus a plain `<div>`, so severity is Medium pending that triage rather than High.

**Recommendation:** Audit the 48 sites; any button inside a `<form>` that is not the intended submit action must be explicitly `type="button"`.

**Remediation steps:**
1. `grep -rn "<button" --include="*.tsx" src | grep -v "type="` to enumerate all 48.
2. Cross-reference against files containing `<form` (checkout, login, register, address, security tabs).
3. Add `type="button"` to every non-submit button in those files; leave the genuine submit button as `type="submit"` (or explicit) for clarity.
4. Consider an ESLint rule (`react/button-has-type`) to prevent regression.

---

### 10. No error boundary anywhere in the tree
**Severity: Medium** · **Files:** `main.tsx`, `App.tsx` — no `ErrorBoundary`/`componentDidCatch` found anywhere in `src/`

A single unhandled render-time exception in any component (e.g., a malformed API response reaching a component that doesn't null-check it) will unmount the entire React tree to a blank white screen, with no recovery UI. Given the volume of hand-rolled fetch/parsing logic identified in findings 6-8, this is a real exposure, not a theoretical one.

**Recommendation:** Add a top-level error boundary.

**Remediation steps:**
1. Add `components/common/ErrorBoundary.tsx` (class component — React error boundaries cannot be function components) rendering a fallback UI with a reload action.
2. Wrap `<App />` in `main.tsx`, or wrap `<Routes>` inside `App.tsx` so navigation still works if one page's content throws.

---

### 11. `key={idx}` used for lists whose items are not guaranteed stable
**Severity: Low** · **Files:** `pages/OrderConfirmationPage.tsx:127`, `components/account/OrderDetailModal.tsx:47`

Both map over an order's line items using the array index as the React `key`. The majority of other `key={i}`/`key={idx}` usages found in this audit (loading skeletons in `HomePage`, `CartPage`, `ProfileTab`, `WishlistTab`, admin tables, `StarRating`, `ImageGallery` thumbnails) are legitimate — they render a fixed-count, non-reorderable placeholder or derived list where index-as-key is the correct choice, not an anti-pattern. These two sites are different: they render actual order-item data, which does carry a stable identifier (`OrderItem` rows have their own DB-backed `id` per the backend entity model). Low severity because order items are fetched once and not reordered/mutated client-side, so no visible bug results today — but it's incorrect by convention and would misbehave if either list ever became editable.

**Recommendation:** Use the item's real `id` field as the key in both sites.

**Remediation steps:**
1. Confirm `OrderItem` (from `types/index.ts`) carries an `id`.
2. Replace `key={idx}` with `key={item.id}` in both files.

---

### 12. Duplicate component name `OrdersTab` in two unrelated directories
**Severity: Low** · **Files:** `components/account/OrdersTab.tsx`, `components/admin/OrdersTab.tsx`

Two distinct components share the identical name, differing only by directory (`account/` vs `admin/`). This is valid TypeScript/React (module-scoped names don't collide), but it is a readability and search-cost hazard: `grep -rn "OrdersTab"`, IDE symbol search, and stack traces all conflate the two, and this audit itself had to re-verify which file was in play multiple times during investigation (see finding 7, which is about the account-side one specifically).

**Recommendation:** Rename one or both to disambiguate, e.g. `AccountOrdersTab.tsx` / `AdminOrdersTab.tsx`, matching the project's naming convention of PascalCase-matches-filename (`react/coding-style.md`).

**Remediation steps:**
1. Rename `components/account/OrdersTab.tsx` → `components/account/AccountOrdersTab.tsx` (component + file + import sites).
2. Rename `components/admin/OrdersTab.tsx` → `components/admin/AdminOrdersTab.tsx` (component + file + import sites).

---

## Recommended Sequencing

Given dependencies between findings (a shared HTTP client makes the refresh fix and the OrdersTab fix trivial instead of three separate patches; a test harness should exist before further refactors so regressions are caught):

1. **#2 — stand up a test harness** (nothing else here is verifiable without it).
2. **#244 / finding 1 — shared `Navbar`** (already scoped, already an open issue, unblocks visible auth UX).
3. **Finding 4 — `RequireAuth` wrapper** (small, removes duplicated guard logic, natural pairing with the Navbar work).
4. **Finding 6 — shared HTTP client**, which subsumes and simplifies findings 5 and 7.
5. **Finding 8 — `useAsync` hook**, opportunistic migration alongside 6.
6. **Findings 9, 10, 11, 12** — low-effort, can be picked up independently at any point.
7. **Finding 3 (`#282`)** — remains a deliberate backend+frontend coordinated effort, out of scope for a frontend-only pass; unchanged from prior deferral.

---

## Appendix — Commands Used

```bash
find src -type f \( -name "*.tsx" -o -name "*.ts" \) | grep -v ".test."
find src -name "*.test.*"
grep -rl "<header" --include="*.tsx" src
grep -rn "localStorage" --include="*.tsx" --include="*.ts" src
grep -rn "dangerouslySetInnerHTML" --include="*.tsx" src
grep -rn ": any\|<any>\|as any" --include="*.ts" --include="*.tsx" src
grep -rn "catch" --include="*.tsx" --include="*.ts" src
grep -rn "apiRefresh" --include="*.tsx" --include="*.ts" src
grep -rn "key={i}\|key={idx}\|key={index}" --include="*.tsx" src
grep -rn "<button" --include="*.tsx" src | grep -v "type="
grep -rln "ErrorBoundary\|componentDidCatch" --include="*.tsx" src
find . -iname "*protected*" -o -iname "*guard*" -o -iname "*requireauth*"
```

# WCAG 2.1 AA Accessibility Audit

## BuildNest — E-Commerce Platform for Home Construction and Décor Products

---

## Document Information

| Attribute | Value |
| :--- | :--- |
| **Document Title** | WCAG 2.1 AA Accessibility Audit |
| **Related Issue** | #129 — `[chore] compliance: accessibility audit (WCAG 2.1 AA) for frontend` |
| **Traces to** | SRS COMP-04 (added in this issue — see "Requirement Traceability Note" below) |
| **Date** | 2026-08-09 |
| **Author** | Technical Lead |
| **Milestone** | M5 — Production Readiness |

---

## 1. Requirement Traceability Note

Issue #129 was filed titled `(COMP-02)` and its body cited "SRS NFR-COMP-04." Neither
citation is correct: `COMP-02` is an existing, unrelated SRS requirement (GDPR
account-deletion/anonymization), and no `COMP-04` or `NFR-COMP-*` row existed in the SRS
at filing time. This is the same filing-time traceability-mismatch pattern already
corrected for `SEC-15`/`SEC-16` (#111/#112), `COMP-01–03` (#128), `RET-01–03` (#88),
`FR-AUTH-12` (#91), `OBS-02`/`OBS-05` (#108/#123), and `MNT-07` (#127) — see those
issues' own SRS/RTM revision-history entries for the established correction convention.

This audit is instead traced to a newly added **COMP-04**, added to the SRS (§3.8.4
Compliance) and RTM in this same change, which is the correct requirement for "the
frontend shall conform to WCAG 2.1 Level AA."

---

## 2. Scope and Methodology

### 2.1 Scope

All 12 customer-facing frontend pages reachable without an admin/seller role:

| Page | Auth required |
| :--- | :--- |
| Home | No |
| Product Listing | No |
| Product Detail | No |
| Login | No |
| Register | No |
| Forgot Password | No |
| Privacy Policy | No |
| 404 (Not Found) | No |
| Cart | Yes |
| Checkout — Address step | Yes |
| Checkout — Shipping step | Yes |
| Account | Yes |

**Explicitly out of scope for this pass**: the Admin Dashboard and Seller Dashboard
(`AdminDashboardPage.tsx`, `SellerDashboardPage.tsx`) and their ~25 constituent tab/modal
components. These are internal-operator surfaces, not named in the issue's own acceptance
criteria ("product listing and checkout" for the screen-reader criterion; no page list
given for the automated-scan criterion), and auditing them requires admin/seller test
accounts this issue's test infrastructure doesn't provision. Filed as a follow-up — see
§6.

### 2.2 Methodology

Per `testing.md`'s test-type decision procedure, this is a **tier-4 (E2E / real browser)**
concern: color-contrast, focus-visibility, and ARIA-in-context rules require real
layout/paint that a jsdom/RTL unit test cannot produce. All checks below run against a
production build (`vite build` + `vite preview`) driven by real Chromium via Playwright,
against a real backend (MySQL + Redis, `--e2e.seed.enabled=true` seed data), mirroring
this repo's existing `happy-path.spec.ts`/`error-paths.spec.ts` E2E pattern.

Test suite: `frontend/e2e/accessibility.spec.ts` (15 specs, all passing as of this audit).

| Acceptance criterion | How verified | Automated? |
| :--- | :--- | :--- |
| axe-core automated scan passes on all pages (zero critical violations) | `@axe-core/playwright`, WCAG 2.1 A/AA tag set (`wcag2a`, `wcag2aa`, `wcag21a`, `wcag21aa`), asserting zero `critical`/`serious`-impact violations per page | Yes — real |
| Manual keyboard navigation verified for checkout flow | **Automated proxy** — see §4 | Automated proxy, not literal manual QA |
| Screen reader test for product listing and checkout | **Automated structural proxy** — see §5 | Automated proxy, not a real AT pass |
| Colour contrast ratio ≥4.5:1 for all text | Covered by axe's `color-contrast` rule, included in the WCAG 2.1 AA tag set above | Yes — real |
| Report stored in `docs/SDLC-docs/reports/accessibility-audit.md` | This document | N/A |

---

## 3. Automated Scan Results (axe-core)

**Result: 0 critical/serious violations across all 12 in-scope pages**, after remediation
(§3.2). The scan is wired as a CI-runnable Playwright suite, not a one-off manual run —
it will catch regressions on future changes to these pages.

### 3.1 Defects found and fixed

| # | Rule | Page(s) | Impact | Root cause | Fix |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | `color-contrast` | Login, Register | serious | Password show/hide toggle button: `text-gray-400` (#99a1af) on white — 2.6:1, needs 4.5:1 | Changed to `text-gray-600`; added `aria-label` |
| 2 | `autocomplete-valid` | Register | serious | `autoComplete={name}` used the raw field name (`"firstName"`, `"lastName"`) directly — not valid WHATWG autocomplete tokens | Mapped to `given-name`/`family-name` via a lookup table |
| 3 | `color-contrast` | Product Detail | serious | Stock-status text `text-green-600`/`text-red-500`/`text-amber-600` on white — 3.21:1 for green | Darkened to `text-green-700`/`text-red-600`/`text-amber-700` |
| 4 | `color-contrast` | Product Detail | serious | SKU label `text-gray-400` on white — 2.6:1 | Changed to `text-gray-600` |
| 5 | `color-contrast` | Checkout (address step) | serious | `CheckoutStepper.tsx` inactive step labels/circles `text-gray-400` on `#f9fafb` — 2.48:1 | Changed to `text-gray-600`/`text-green-700` |
| 6 | `color-contrast` | Account | serious | "Member account" subtitle `text-gray-400` on `#f9fafb` — 2.48:1 | Changed to `text-gray-600` |

### 3.2 A 7th defect caught only by real CI, not local runs

CI's `Playwright E2E Tests` job (which runs this same suite against a freshly-seeded backend)
failed on `checkout page — shipping step` with 2 `color-contrast` violations
(`text-gray-500` on white, `ShippingStep.tsx`'s shipping-option description/estimated-days text)
that never reproduced locally — the local seed data path exercised only the "no shipping options"
empty state, which uses different text, while CI's seed produced real shipping options with the
`text-gray-500` lines. Fixed the same way as the other color-contrast defects (`text-gray-600`),
and swept `text-gray-500` across the rest of the customer-facing page/component tree (`HomePage`,
`OrderConfirmationPage`, `NotFoundPage`, `ProductDetailPage`, `ProductListingPage`,
`CheckoutPage`, `RegisterPage`, `ForgotPasswordPage`, `ResetPasswordPage`, `CartPage`,
`LoginPage`, `ReviewsSection`, `PaymentStep`, `NotificationBell`, `ErrorBoundary`,
`CartItemRow`, `ErrorMessage`, `RequireAuth`) rather than waiting for CI to catch each instance
individually — `text-gray-500` sits close enough to the 4.5:1 threshold (Tailwind's default hex
value computes right around it) that its actual pass/fail is dependent on exact font-weight/size
context, making it an unreliable choice project-wide. Re-verified: 15/15 specs pass locally with
a real (non-empty) shipping-options seed.

### 3.3 Same-pattern sweep (Definition of Done — search beyond the cited instance)

Per this repo's own process rule (search for the defect pattern everywhere it recurs, not
just the cited location), all customer-facing components using `text-gray-400` on a
light/white background were swept and corrected, beyond the pages axe's per-page scan
happened to visit directly:

- `ProductGrid.tsx` (empty-state text)
- `ProductCard.tsx` (strikethrough original price)
- `CategorySidebar.tsx` ("No categories" text)
- `ReviewsSection.tsx` (review date, helpful-count text)
- `OrderConfirmationPage.tsx` (Order Number label)
- `CartPage.tsx` ("Shipping calculated at checkout" text)

**Not swept**: `text-gray-400` usages inside the Admin/Seller dashboard component tree
(~30 files) — out of scope per §2.1, tracked as a follow-up (§6). Also not changed:
icon-only elements (SVG fill colors, decorative emoji) where axe's `color-contrast` rule
did not flag a violation — bumping those without a positive finding would be a
speculative change outside this audit's evidence base.

### 3.4 A defect axe did not catch, found via the screen-reader structural check

`AddressStep.tsx`'s form fields used a `<label>` sibling to each `<input>` with **no**
`htmlFor`/`id` pairing — the same defect class #286 already fixed on `LoginPage.tsx` and
the account profile form, but on a form #286 never touched. Neither the WCAG-tagged nor
the full (untagged) axe rule set flagged this as a violation in this environment. This is
documented as a genuine gap between what an automated tool *did* flag and what §5's
structural screen-reader proxy check (built specifically because axe's own coverage isn't
assumed complete) caught instead — fixed regardless, since correct label association is
unambiguously required (WCAG 2.1 SC 1.3.1, 4.1.2) irrespective of whether this specific
tool surfaced it. See `frontend/src/components/checkout/AddressStep.tsx`.

The same gap was independently confirmed on `RegisterPage.tsx` (the `field()` helper used
for first/last name, username, email, confirm-password, plus the standalone Password
field) by the `react-reviewer` agent's own review pass (DoD Item 2) — axe's scan of
`/register` also reported zero violations despite the same missing association, matching
the `AddressStep.tsx` pattern. Fixed the same way (`id`/`htmlFor` pairing), and the
`AUTOCOMPLETE_TOKENS` lookup was tightened from `Partial<Record<Field, string>>` to
`Record<Field, string>` (every field requires an explicit, valid autocomplete token) so a
future field addition without one fails the type check rather than silently falling back
to a possibly-invalid raw field name.

---

## 4. Keyboard Navigation — Checkout Flow

**Acceptance criterion as filed**: "Manual keyboard navigation verified for checkout
flow." A literal human-operated manual QA pass is not available in this environment (no
interactive terminal/browser session for a human tester). This criterion is satisfied via
an **automated keyboard-only traversal** instead — `frontend/e2e/accessibility.spec.ts`,
`Keyboard navigation — checkout flow` describe block — which drives the checkout Address
step using only `page.keyboard.press('Tab')`/`type()`/`press('Enter')`, with **zero**
`click()` calls anywhere in the test:

- Confirms every field (`fullName` → `line1` → `line2` → `city` → `state` →
  `postalCode` → `country` → `phone`) receives focus in document order via `Tab`.
- Confirms `Tab` from the last field lands on the "Continue to Shipping" submit button
  next — no keyboard trap, no skip past interactive content.
- Fills all fields via keyboard and submits with `Enter` (not a mouse click), confirming
  the form actually advances to the next checkout step.

This is disclosed here as a **documented proxy**, not silently substituted for a real
manual pass — if a literal human keyboard-only QA session is later required (e.g. for a
formal accessibility certification), that is separate, out-of-scope follow-up work, not
something this automated test can itself satisfy retroactively.

---

## 5. Screen Reader — Product Listing and Checkout

**Acceptance criterion as filed**: "Screen reader test for product listing and
checkout." A real NVDA/VoiceOver/JAWS pass is not available in this environment — there
is no screen-reader binary and no audio-output verification path in a headless CI-style
browser session. This criterion is satisfied via an **automated structural proxy** —
`frontend/e2e/accessibility.spec.ts`, `Screen reader structural proxy` describe block —
which checks the same signals a screen reader actually relies on, via the accessibility
tree Chromium/Playwright expose:

- **Product listing**: a `banner` landmark (`<header>`) and a `main` landmark exist; the
  search input exposes an accessible `searchbox` role with name "Search products"; the
  first product card link has a non-empty accessible name (not an icon-only, unlabeled
  link).
- **Checkout**: the step-progress indicator exposes a `navigation` landmark labeled
  "Checkout progress" (`CheckoutStepper.tsx`'s existing `aria-label`); every address field
  resolves via `getByLabel` with its exact visible label text — the same lookup mechanism
  a screen reader uses to announce a form field, which is what caught the `AddressStep.tsx`
  label-association defect in §3.3.

This is narrower than a full screen-reader pass (it does not verify announcement order,
live-region behavior, or verbosity settings a real AT would exercise) and is disclosed as
such rather than presented as equivalent to real assistive-technology testing.

---

## 6. Follow-up (Out of Scope for This Issue)

Per Mid-Implementation Scope Discovery (separate concern, not folded into #129's own
scope): the Admin Dashboard and Seller Dashboard surfaces were never in this issue's
acceptance criteria and were not audited here. Filed as **#716** — extend
`accessibility.spec.ts`'s coverage to those surfaces once admin/seller test-account
provisioning exists in the E2E test infrastructure.

---

## 7. Summary

| Criterion | Status |
| :--- | :--- |
| axe-core automated scan passes on all pages (zero critical violations) | ✅ Met — 0 critical/serious across 12 pages |
| Manual keyboard navigation verified for checkout flow | ✅ Met via automated proxy (disclosed) |
| Screen reader test for product listing and checkout | ✅ Met via automated structural proxy (disclosed) |
| Colour contrast ratio ≥4.5:1 for all text | ✅ Met — 7 defects found and fixed (6 local, 1 caught only by real CI seed data), zero remaining per axe |
| Report stored in `docs/SDLC-docs/reports/accessibility-audit.md` | ✅ This document |

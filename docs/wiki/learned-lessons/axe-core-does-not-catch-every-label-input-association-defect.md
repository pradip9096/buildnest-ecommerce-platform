---
name: axe-core-does-not-catch-every-label-input-association-defect
description: "axe-core's label/name-role-value rules failed to flag a genuine, unambiguous missing htmlFor/id association between a <label> and its sibling <input> on two separate React forms — caught instead by a structural screen-reader proxy check and an agent-based code review, not by the automated scanner the AC named"
root_cause: "The specific defect shape — a <label> rendered as a plain sibling of its <input>, with neither wrapping nor an explicit htmlFor/id pair — did not trigger axe-core's label rule (nor any other WCAG-tagged or untagged rule) in this environment/version combination, despite being an unambiguous WCAG 1.3.1/4.1.2 violation confirmed by direct DOM inspection and by Playwright's own getByLabel() failing to resolve the field"
impact: "A session that treats 'axe-core scan passes' as sufficient evidence of WCAG conformance would ship a real accessibility defect undetected — the acceptance criterion's own named tool has a documented false-negative on this exact defect class, at least twice in the same codebase"
metadata:
  type: lesson
  originSessionId: work-on-issue-129
---

## The pattern

An accessibility audit's acceptance criterion said "axe-core automated scan passes on all pages
(zero critical violations)." Two forms in this codebase (`AddressStep.tsx`, `RegisterPage.tsx`)
had genuinely broken label/input association — a `<label>` rendered as a plain sibling of its
`<input>`, with no `htmlFor`/`id` pairing and no wrapping:

```tsx
// WRONG — no programmatic association, axe-core does not flag this in practice
<div>
  <label className="...">{label}</label>
  <input value={...} onChange={...} />
</div>
```

Both `@axe-core/playwright`'s WCAG-2.1-tagged rule set (`wcag2a`, `wcag2aa`, `wcag21a`,
`wcag21aa`) and its full, untagged rule set returned **zero violations** for these pages, in a
real Chromium browser against a real production build. This is not a false-positive-suppression
config issue — no rules were disabled, no `withRules()`/`disableRules()` calls exist in the test
file.

The defect was caught two other ways instead:

1. **A structural screen-reader proxy check** written specifically because axe's own coverage
   wasn't assumed complete — `page.getByLabel('Full name', { exact: true })` (Playwright's own
   accessible-name resolution, independent of axe) failed to find the element, because Playwright
   computes the accessible name the same way a real screen reader would and found none.
2. **An agent-based code review pass** (`react-reviewer`), invoked per this repo's Definition of
   Done requirement for a dedicated review tool on any non-trivial change, independently spotted
   the identical defect on a second form (`RegisterPage.tsx`) that the first check never visited.

## Why this matters

"The automated scanner named in the acceptance criterion reports zero violations" is not
equivalent to "the page is accessible." Any WCAG audit whose only verification method is a single
scanner tool — however well-regarded — has an unquantified false-negative rate for at least this
defect class. Treat a scanner's clean report as one data point, not proof, and pair it with at
least one structurally-different check method (a second tool, a manual accessibility-tree query,
or a dedicated code-review pass) for defect classes a scanner is known to sometimes miss —
particularly label/name/role/value association, since that's exactly the shape that slipped
through here twice.

## How to apply

- Don't treat "axe-core (or any single automated scanner) reports zero violations" as the sole
  acceptance evidence for a WCAG/accessibility requirement — pair it with at least one
  structurally different verification (Playwright's own `getByLabel`/`getByRole` resolution is a
  free, already-available second check if the E2E stack includes Playwright; a real AT pass is
  the strongest but often unavailable).
- When writing an accessibility E2E suite, add a check that actually *uses* the accessible name
  the way a real consumer would (`getByLabel`, `getByRole` with a `name` filter) rather than only
  asserting "the scanner found no rule violations" — the former positively exercises the same
  computation path a screen reader relies on, the latter only proves one tool's implementation of
  that computation didn't flag it.
- Don't assume a defect fixed once (e.g. on `LoginPage.tsx` in a prior issue, #286) is fixed
  everywhere the same pattern appears — grep for the pattern across all forms before considering
  a "fix this defect class" issue complete, and don't rely on the scanner to find every instance
  for you.

---
title: Nested `<form>` Elements Pass Mocked RTL/jsdom Tests but Submit the Wrong Form in a Real Browser
category: testing
tags: [react, forms, jsdom, react-testing-library, html-validity, live-browser-verification]
keywords: [nested form, invalid HTML, form onSubmit, jsdom form nesting, RTL button click, real browser navigation, type=submit inside form]
source_conversations: [Session 2026-07-19 (#436)]
last_updated: 2026-07-19
confidence: high
evidence_strength: strong
root_cause: "jsdom does not enforce the HTML5 constraint that <form> elements cannot nest, so a component with a <form> inside another <form> renders and behaves exactly as written under Vitest/RTL — but a real browser's HTML parser auto-closes/reparents the invalid structure, so a submit button that RTL sees as belonging to the inner form actually belongs to (and submits) the outer one"
impact: medium — a real, user-facing feature (checkout coupon apply) silently did a full page reload instead of calling its handler, caught only by live browser verification, not by 6 passing mocked unit tests
related_lessons:
  - docs/wiki/learned-lessons/cors-allowedmethods-restriction-invisible-to-curl-and-mockmvc.md
---

# Nested `<form>` Elements Pass Mocked RTL/jsdom Tests but Submit the Wrong Form in a Real Browser

## Problem

Building a coupon-apply UI on BuildNest's checkout `ShippingStep` (#436), the coupon input
and "Apply" button were placed inside their own `<form onSubmit={handleApplyCoupon}>`,
nested inside the step's existing outer `<form onSubmit={handleSubmit}>` (the shipping
method selector's submit form). This is invalid HTML — the HTML5 spec forbids nesting
`<form>` elements — but nothing in the toolchain caught it:

- TypeScript/JSX compiled cleanly (JSX doesn't validate DOM nesting rules).
- ESLint had no rule flagging it.
- `npx vitest run` (RTL + jsdom) passed all 6 new tests, including a `userEvent.click()` on
  the "Apply" button correctly triggering the coupon handler.

Only live browser verification (Chrome DevTools MCP driving the real Vite dev server)
caught the bug: clicking "Apply" caused a full page navigation (`http://localhost:5173/checkout?`)
instead of calling `handleApplyCoupon` — a real `<form>` GET submission, not a React event.

## Root Cause

jsdom (the DOM implementation RTL runs against) does not implement the HTML parsing
algorithm's form-nesting suppression. A real browser's HTML parser, on encountering a
`<form>` start tag while already inside an open `<form>`, ignores the inner tag entirely —
the "inner form" never actually exists as a separate form element; its children (including
the submit button) belong to the outer form. jsdom, by contrast, builds the DOM tree
literally as written, creating two real nested `<form>` elements. A `type="submit"` button
inside the inner `<form>` therefore submits *that* form under jsdom/RTL — matching what the
test expects — but submits the *outer* form in a real browser, since the inner form never
existed there.

This means the exact form-nesting bug is **structurally invisible to jsdom-based tests
regardless of how thorough they are** — the test isn't wrong, the environment lacks the
capability to reproduce the real DOM's own parsing behavior for this specific case.

## Fix

Don't nest `<form>` elements. When a sub-action (like "apply coupon") needs to live inside
an existing form's markup without being a step in that form's own submit flow, use a plain
container (`<div>`) with a `type="button"` and an `onClick` handler instead of `type="submit"`
inside a nested `<form onSubmit={...}>`. If Enter-to-submit inside the sub-action's own input
is desired, handle it explicitly via `onKeyDown` (checking `e.key === 'Enter'`, calling
`e.preventDefault()` to stop it from bubbling to the outer form, then invoking the handler
directly) rather than relying on a second `<form>` element to capture it.

```tsx
// WRONG — nested form, works under jsdom/RTL, breaks in a real browser
<form onSubmit={handleOuterSubmit}>
  ...
  <form onSubmit={handleCouponSubmit}>
    <input ... />
    <button type="submit">Apply</button>
  </form>
  ...
</form>

// CORRECT — no nesting; explicit click handler, explicit Enter handling
<form onSubmit={handleOuterSubmit}>
  ...
  <div>
    <input onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); handleCoupon(); } }} />
    <button type="button" onClick={handleCoupon}>Apply</button>
  </div>
  ...
</form>
```

## Generalization

This is not BuildNest-specific — it applies to any React (or other framework) codebase using
jsdom-based unit/component tests (Vitest, Jest + jsdom, RTL) for form-heavy UI. The specific
takeaway:

- **A component test suite passing under jsdom is not proof that the component's real DOM
  structure is valid HTML.** jsdom renders whatever the virtual DOM produces literally; it
  does not apply the browser's parsing-time corrections for things like form nesting,
  `<tr>` outside `<table>`, or other parser-repaired invalid structures.
- Whenever a sub-form-like control (a search box, a coupon apply, an inline edit) is placed
  inside a page/step that already has its own outer `<form>`, treat that as a deliberate
  design decision requiring a non-nested-form implementation from the start — don't rely on
  "the tests pass" as evidence the markup is valid.
- This is one more concrete instance of the general pattern BuildNest's own
  `development-workflow.md`/`testing.md` already document for CI-green-but-wrong gotchas and
  mocked-test blind spots (see the CI-green-gotchas and mocked-unit-test-blind-spots lesson
  clusters) — live browser verification remains the only check that can catch a defect whose
  root cause is a gap between jsdom's DOM model and a real browser's.

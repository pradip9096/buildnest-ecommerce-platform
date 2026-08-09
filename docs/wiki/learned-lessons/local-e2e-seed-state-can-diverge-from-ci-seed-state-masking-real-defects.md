---
name: local-e2e-seed-state-can-diverge-from-ci-seed-state-masking-real-defects
description: "A local E2E test run against a manually-recreated MySQL volume produced different seed data than CI's fresh-per-run seed, so a local accessibility scan exercised an empty-state code path (no violation) while CI's run exercised the real-data code path (a genuine color-contrast violation) — 15/15 local passes gave false confidence right up until CI caught it"
root_cause: "Local verification reused a manually-managed MySQL Docker volume across several restarts during the session; by the time the final local Playwright run happened, the product/shipping-option seed data present didn't match what a genuinely fresh CI run's E2ESeedDataRunner produces, so the two environments exercised different conditional render branches (ShippingStep.tsx's `options.length === 0` empty-state text vs. its real per-option description/estimated-days text) despite both claiming to run the same '--e2e.seed.enabled=true' seed path"
impact: "A CI-verified real defect (color-contrast on shipping-option text) shipped past 2 full local test runs (15/15 passing both times) because neither run's environment state actually matched CI's — the acceptance evidence ('tests pass locally') was accurate for the state tested, but that state wasn't representative of what a fresh deploy/CI run would see"
metadata:
  type: lesson
  originSessionId: work-on-issue-129
---

## The pattern

An accessibility E2E suite (`frontend/e2e/accessibility.spec.ts`) was run locally against a
MySQL database that had been recreated (`docker compose down -v` + `up`) and re-seeded multiple
times over the course of one session, each time via `--e2e.seed.enabled=true`. Two consecutive
local runs reported 15/15 passing, including the checkout "shipping step" scan. The PR was pushed
and CI's own `Playwright E2E Tests` job — which spins up a genuinely fresh H2/MySQL instance and
runs the identical seed step — failed the exact same test with a real, reproducible (2/2 retry
attempts) `color-contrast` violation on `ShippingStep.tsx`'s per-option description text.

The root cause: `ShippingStep.tsx` renders one of two mutually exclusive branches —

```tsx
{options.length === 0 ? (
  <p className="text-gray-500 text-sm">No shipping options available for your area.</p>
) : (
  // ...text-gray-500 description/estimated-days lines, only rendered when options exist
)}
```

The local database, after several manual `docker compose down -v` cycles during iterative
debugging, ended up with a state where the checkout flow's shipping step showed the empty-state
message (no real options) — a code path the color-contrast defect wasn't in. CI's single, clean
seed run produced real shipping options, exercising the branch that actually had the violation.
Both environments ran the identical test file, the identical seed command, and both self-reported
"seeding succeeded" — but the *data* that resulted differed enough to change which conditional
branch got scanned.

## Why this matters

"Tests pass locally" is only strong evidence when the local environment's state is known to match
what CI/production will produce. A test suite that depends on seeded data (rather than data it
creates and controls entirely within the test itself) inherits whatever drift accumulates in that
seed path across manual environment resets — and that drift is invisible from the test's own
passing output, since the test genuinely did pass, correctly, for the state it was given.

## How to apply

- After any manual `docker compose down -v` / re-seed cycle during a debugging session, don't
  assume the resulting local state matches a fresh CI run — if a test's very design depends on
  specific seeded data shapes (e.g. "at least one shipping option with a description"), verify
  that shape is actually present (`curl` the relevant endpoint, or add an explicit assertion the
  test itself checks before scanning) rather than trusting "seeding completed without error."
- When a test exercises a conditional/branching UI state (empty-state vs. populated-state), a
  single local pass only proves one branch is clean — prefer asserting *which* branch was
  actually exercised (e.g. `await expect(page.getByTestId('product-grid')).toBeVisible()`-style
  precondition checks already present in this suite) so a divergent environment fails loudly
  instead of silently skipping the untested branch.
- Treat a CI failure that doesn't reproduce in 1-2 local retries as a signal to check *environment
  state parity* first (what data actually exists locally vs. what a fresh run produces), not just
  "maybe it's flaky" — in this case it wasn't flaky, it was a genuine defect in a branch the local
  environment had stopped exercising.

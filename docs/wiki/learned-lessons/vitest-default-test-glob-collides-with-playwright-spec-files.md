---
name: vitest-default-test-glob-collides-with-playwright-spec-files
description: "Vitest's default include glob (**/*.{test,spec}.ts) silently picks up Playwright's own *.spec.ts files in an e2e/ directory, and Playwright's test.describe() throws when invoked outside Playwright's runner — invisible until someone actually runs the coverage/test command locally, since neither tool's own CI wiring (when either exists) would ever cross-invoke the other"
root_cause: "Both Vitest and Playwright use the *.spec.ts naming convention by default, and Vitest's default `include` pattern has no awareness of a sibling e2e/ directory meant for a different test runner — the two tools' file-discovery globs simply overlap unless one is explicitly scoped away from the other's directory"
impact: "Every Vitest run (including any future CI wiring, e.g. a coverage-gate check) fails 2 test suites outright with a confusing 'did not expect test.describe() to be called here' error that looks like a Playwright config bug, not a Vitest include-glob problem — caught only by running `npm run test:coverage` locally, since no CI job in this repo invoked Vitest at all until this was found (see #649)"
metadata:
  type: lesson
  originSessionId: work-on-issue-117
---

## The pattern

Adding a Playwright E2E suite (`frontend/e2e/*.spec.ts`) to a project that already uses Vitest
for unit/component tests creates an immediate, silent collision: Vitest's default `include`
pattern (`**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}`) has no directory scoping, so it picks
up the new `e2e/*.spec.ts` files as if they were its own tests. When Vitest tries to execute them,
Playwright's `test.describe()` throws:

```
Error: Playwright Test did not expect test.describe() to be called here.
```

This reads like a Playwright configuration mistake, but the actual cause is on the Vitest side —
Playwright's own config (`playwright.config.ts`'s `testDir: './e2e'`) was never the problem.

## Why this stayed invisible

In BuildNest specifically, this was caught only by manually running `npm run test:coverage`
locally to answer a coverage question — it would have stayed invisible indefinitely otherwise,
since:
- The Playwright suite's own CI job (`playwright-e2e`) only ever invokes `npx playwright test`,
  never Vitest — it has no reason to trip this.
- No CI job in this repo invoked Vitest at all before this was found (a separate, independently
  discovered gap — see the `an-implemented-capability-...` and CI-wiring lessons from the same
  session). A repo that already had Vitest wired into CI would have caught this on the very first
  PR that added Playwright files, via a red CI check — the invisibility here was compounded by a
  second, unrelated gap, not solely this collision itself.

## The fix

Explicitly exclude the E2E directory from Vitest's own test discovery:

```ts
// vite.config.ts
export default defineConfig({
  test: {
    // Vitest's `exclude` REPLACES its defaults entirely rather than extending them — the
    // node_modules/dist entries must be restated explicitly alongside the new exclusion.
    exclude: ['**/node_modules/**', '**/dist/**', 'e2e/**'],
  },
})
```

## Generalizable takeaway

Any repo adopting both Vitest and Playwright (or any two test runners that both default to a
`*.spec.ts`/`*.test.ts` naming convention) needs an explicit mutual exclusion the moment the
second tool's directory is created — don't assume `testDir`/`testMatch` scoping on one tool's side
is sufficient to keep the other tool from also picking up the same files. Verify by actually running
the *other* tool's test command once, immediately after adding the new test directory, rather than
assuming directory separation alone prevents the collision.

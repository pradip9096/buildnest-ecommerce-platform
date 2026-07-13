---
title: React Testing Library Auto-Cleanup Needs an Explicit afterEach When Vitest globals Is False
category: technical
tags: [vitest, react-testing-library, testing, frontend, test-isolation]
keywords: [cleanup, afterEach, globals false, multiple elements found, test pollution, RTL auto cleanup]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
root_cause: "RTL's automatic unmount-after-each-test cleanup listens for Vitest's global afterEach hook, but the project's globals: false config doesn't inject that global, so cleanup silently never fires without an explicit afterEach(cleanup) in the shared setup file"
impact: medium — two of five tests in a new suite failed with cross-test DOM pollution, a symptom easy to misdiagnose as a component/test logic bug rather than missing cleanup
related_lessons:
  - docs/wiki/learned-lessons/vitest-needs-triple-slash-reference-in-vite-config.md
---

# React Testing Library Auto-Cleanup Needs an Explicit `afterEach` When Vitest `globals` Is `false`

## Problem

While implementing `RequireAuth.test.tsx` (`#294`), two of five tests failed with `getMultipleElementsFoundError` from `@testing-library/dom` — the DOM from a previous test's `render()` call was still present when the next test queried the document. Test bodies looked correct in isolation; the failure only appeared when multiple tests in the same file rendered into the DOM.

`@testing-library/react`'s automatic unmount-after-each-test behavior depends on registering a callback against the test framework's global `afterEach`. The project's `vite.config.ts` test block (set up in `#293`) has `globals: false` — meaning Vitest does not inject `describe`/`it`/`afterEach`/etc. as globals, and RTL's own auto-cleanup wiring (which listens for the global test lifecycle hooks) silently never fires.

## Fix

`src/test/setup.ts` must explicitly import `afterEach` from `vitest` and call RTL's `cleanup()`:

```ts
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';

afterEach(() => {
  cleanup();
});
```

This file is already wired as `setupFiles: ['./src/test/setup.ts']` in `vite.config.ts`, so the fix applies globally with no per-test-file changes needed.

## Rule

When `globals: false` (explicit imports of `describe`/`it`/etc. per file — the stricter, more common convention for larger projects), RTL's automatic cleanup does not activate on its own. Always pair `globals: false` with an explicit `afterEach(cleanup)` in the shared setup file. If a test suite shows "multiple elements found" errors only when run alongside other tests in the same file (never in isolation), suspect missing cleanup before suspecting a logic bug in the component or the test.

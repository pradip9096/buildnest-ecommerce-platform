---
title: vi.useFakeTimers() Cleanup in the Test Body Cascades into Later Tests on Failure
category: technical
tags: [vitest, fake-timers, testing, test-isolation, frontend]
keywords: [useFakeTimers, useRealTimers, test timeout, cascading test failure, afterEach cleanup, testing-library waitFor fake timers]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/rtl-cleanup-needs-explicit-aftereach-with-globals-false.md
---

# `vi.useFakeTimers()` Cleanup in the Test Body Cascades into Later Tests on Failure

## Problem

While writing `ProductCard.test.tsx` (`#248`) for the quick-add-to-cart button's "reverts to idle after 2s" behavior, a test called `vi.useFakeTimers()` at the top and `vi.useRealTimers()` at the bottom — the common pattern for testing a `setTimeout`-based revert. That test failed with `Test timed out in 5000ms`. The *next* test in the file — an unrelated one that never touched fake timers itself — also failed with the identical timeout error.

Root cause: `vi.useRealTimers()` was the last line of the test body. When the test failed or timed out *before* reaching that line (which is exactly what a timeout does — the test never completes normally), the cleanup never ran. Vitest's fake timers are global state, not scoped to the failing test, so they stayed active into the next test. That next test used `userEvent.click()` and `waitFor()`, both of which depend on real timers internally (`userEvent` for its internal delays, `waitFor` for its polling interval) — under leftover fake timers with nothing advancing them, both hung until their own 5s timeout.

A second contributing factor: mixing `@testing-library/react`'s `waitFor` (which polls via `setInterval`/`setTimeout` under the hood) with `vi.useFakeTimers()` is itself fragile — `waitFor` won't naturally resolve unless something advances the fake clock, and `userEvent`'s own async delays need `advanceTimers` wired in to cooperate.

## Fix

Two changes, either of which would have prevented the cascade on its own:

1. **Cleanup must live in `afterEach`, not at the end of the test body**, so it runs even when the test fails or times out:
   ```ts
   afterEach(() => {
     vi.useRealTimers();
   });
   ```
2. **Avoid the fake-timers/`waitFor` combination entirely when a simpler option exists.** For a short, fixed delay (here, 2 seconds), it was simpler and more reliable to drop fake timers altogether and just `await screen.findByRole(...)` for the intermediate state, then `await waitFor(..., { timeout: 3000 })` for the reverted state — accepting a slightly slower test in exchange for removing an entire class of fake-timer/polling-library interaction bugs.

## Rule

Any `vi.useFakeTimers()` call needs its matching `vi.useRealTimers()` registered in `afterEach`, never as the last line of the test body — a test can fail or time out before reaching that line, and Vitest's fake-timer state is global, so it silently leaks into every subsequent test in the file. When a test failure cascades into an unrelated *next* test with an identical symptom (especially a timeout), suspect leaked global test state — fake timers, unrestored mocks, or `vi.stubGlobal` — before suspecting a new bug in the second test. Where the delay being tested is short and fixed, prefer real timers with a generous `waitFor` timeout over fake timers, unless the delay is too long to wait out in real time.

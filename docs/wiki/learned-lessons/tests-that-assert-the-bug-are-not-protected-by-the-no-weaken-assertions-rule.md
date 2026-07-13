---
title: "A Test That Asserts the Bug Itself Is Not Protected by 'Never Weaken Assertions'"
category: testing
tags: [tdd, regression-testing, bug-fixing, assertThrows, test-maintenance]
keywords: [test encodes bug as expected behavior, assertThrows on buggy behavior, fixing a test vs weakening an assertion, orElseThrow read path anti-pattern, lazy-created resource empty state]
source_conversations: [Session 2026-07-05]
last_updated: 2026-07-05
confidence: high
evidence_strength: strong
root_cause: "read-path service methods for lazily-created per-user singleton resources (cart, wishlist) called .orElseThrow() on 'no row yet', a design smell that treats 'doesn't exist yet' and 'is empty' as distinguishable when they should read the same to a caller, and existing tests encoded that exact defect as the expected/passing behavior"
impact: high — a real user-facing bug (brand-new users got a 'not found' error instead of an empty cart/wishlist) was already codified as passing, green test coverage, which would have permanently blocked a correct fix from landing without recognizing the distinction from assertion-weakening
related_lessons:
  - docs/wiki/learned-lessons/verify-issue-premises-against-repo-before-implementing.md
  - docs/wiki/learned-lessons/stale-test-classes-false-failures.md
---

# A Test That Asserts the Bug Itself Is Not Protected by "Never Weaken Assertions"

## Problem

Fixing #303 (Cart/Wishlist throwing "not found" for a brand-new user instead of returning empty) required changing `CartServiceImplTest.testGetCartByUserIdCartNotFoundThrows` and two `WishlistServiceImplTest` tests. Each of these tests asserted `assertThrows(...)` on exactly the behavior the issue identified as the bug — i.e. the test suite had a passing, green test whose entire purpose was "verify the defect happens."

This looks, on the surface, like it conflicts with the project's Failure Response Protocol ("never weaken or remove a test assertion to make a test pass"). It doesn't.

## Distinction

- **Weakening an assertion** = the test was correctly describing desired behavior, and the assertion is loosened/removed so a still-broken implementation passes anyway. This hides a real defect.
- **Correcting a test that encodes the bug** = the test's assertion *was the bug, written down*. The implementation changes to do the right thing; the test must change to check for the right thing, or it becomes a regression guard for the wrong behavior — permanently blocking the fix from ever being landed correctly.

The signal that a test falls into the second category: the GitHub issue's own reproduction steps or fix checklist describe the exact scenario the test asserts, as the thing to be corrected. If the issue says "X should return empty, not throw" and a test says `assertThrows(..., () -> service.X())`, that test is not a safety net — it is the documented bug.

## How this played out

Both `getCartByUserId` and `getWishlist`/`getWishlistProducts` are **read** paths on a resource that's lazily created on first **write** (`addToCart`/`addProduct`). The old tests asserted `orElseThrow` behavior for "no row yet," which is exactly backwards for a read path — "doesn't exist yet" and "is empty" should be indistinguishable to a reader for lazily-created, per-user singleton resources (cart, wishlist, preferences, etc.). This is a reusable design smell to watch for elsewhere in this codebase: any `service.getX(userId)` that calls `.orElseThrow()` on a resource whose only creation path is a separate lazy-write method is a candidate for the same class of bug.

## What NOT to do differently

Don't leave the stale assertion in place "for safety" and add a second test alongside it — that produces two contradictory tests, one of which is permanently red or permanently describes wrong behavior. Replace the test outright, and use the commit/PR description (not a code comment) to record that the old assertion was itself the defect being fixed — matching this project's convention of referencing the issue number, not the historical assertion, in the code.

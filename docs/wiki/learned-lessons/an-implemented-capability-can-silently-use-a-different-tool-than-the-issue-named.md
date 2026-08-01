---
name: an-implemented-capability-can-silently-use-a-different-tool-than-the-issue-named
description: "An issue's acceptance criteria can name a specific tool (Playwright) while the actual capability was already built with a different one (Selenium) under a duplicate/sibling issue — closed-issue-status and grep-for-the-capability both miss this, since both are satisfied"
root_cause: "Two issues (#117, #214) were filed with identical titles/acceptance criteria for 'Playwright E2E tests'; #214 was implemented and closed as COMPLETED, but the actual work used Selenium+JUnit, not Playwright. #117 stayed open with a still-accurate-looking acceptance criteria list, and nothing re-checked whether the *tool* named in the criteria matched the tool actually used, only whether the *capability* (E2E coverage) existed."
impact: "Without catching this before implementation, the natural next step would have been building a second, fully redundant Playwright suite alongside the already-working Selenium one with no framework consolidation plan — or worse, closing #117 as a stale duplicate without noticing the tool itself was never actually adopted, leaving #632's own open tooling-mismatch question unresolved."
metadata:
  type: lesson
  originSessionId: work-on-issue-117
---

## The pattern

`closed-issue-status-is-not-evidence-of-implementation.md` already covers "a closed issue doesn't
prove the capability was built." This is the inverse edge case: **a genuinely-implemented
capability doesn't prove the issue's *named tool* was the one actually used.** An issue can read as
fully satisfied — the feature exists, tests pass, a sibling issue closed as COMPLETED — while
quietly having been built with a different library/framework than the one the acceptance criteria
literally name.

This is easy to miss because both of the obvious verification steps pass:
- Grepping for the capability ("is there E2E coverage?") → yes.
- Checking issue status ("was the E2E issue closed?") → yes, a sibling was.

Neither step checks whether the *tool identity* in the acceptance criteria matches reality, so a
tooling-mismatch issue (in this case, a second, independently-filed issue — #632 — had to notice
and flag it explicitly) is the only thing that catches it, and only if someone thinks to search
for a sibling/related tracking issue at all.

## How this surfaced

BuildNest's #117 asked for "Playwright E2E tests." The actual E2E suite
(`backend/.../e2e/E2ETest.java`) uses Selenium WebDriver + JUnit — no Playwright anywhere in the
repo. A duplicate-titled issue (#214) had already been closed as `COMPLETED` for what was really
Selenium work. A separate audit issue (#632) had independently caught the same mismatch weeks
earlier but stayed open as an unresolved decision.

## The generalizable check

Before implementing an issue whose acceptance criteria name a specific tool/library, verify the
tool identity itself, not just the capability:

1. Search for a sibling/duplicate-titled issue (open or closed) — a matching title is a strong
   signal the capability may already exist under a different tool.
2. If a closed sibling exists, read what was *actually* built (grep the implementation, don't
   trust the issue title or "COMPLETED" status alone), and diff that against the tool the current
   issue's acceptance criteria name.
3. If they diverge, this is a real decision point (migrate / coexist / correct-the-issue), not a
   simple "already done, close as duplicate" — surface it explicitly rather than resolving it
   unilaterally in either direction.

## Generalizes beyond BuildNest

Any project with issue-driven development is susceptible: a ticket's acceptance criteria are
written once, at filing time, and nothing re-verifies the *tool* named in them stays accurate
after implementation — only whether the ticket eventually gets marked done. The check above (diff
tool identity, not just capability presence) applies to any tracker, not just GitHub Issues.

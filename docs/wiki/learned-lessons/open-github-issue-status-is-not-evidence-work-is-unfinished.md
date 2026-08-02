---
name: open-github-issue-status-is-not-evidence-work-is-unfinished
description: "An issue can sit OPEN on the tracker for many sessions after its actual scope was fully implemented across a long chain of merged PRs from prior sessions — nothing re-checks an open issue against real repo state until someone works it again, the inverse of the already-documented closed-issue-status gap"
root_cause: "Nothing in this repo's workflow re-syncs an issue's tracker state against implementation reality except the session that eventually picks that issue up again — nightly closure discipline exists (Closes #N in a PR body), but there is no periodic sweep that checks whether an OPEN issue's acceptance criteria actually already got satisfied by other issues' work along the way"
impact: "Without checking git log for the issue number before starting implementation, the natural next step for an agent handed an open issue is to re-implement work that already merged days earlier under different issue numbers — wasted effort at best, a redundant/conflicting second implementation at worst"
metadata:
  type: lesson
  originSessionId: work-on-issue-117
---

## The pattern

`closed-issue-status-is-not-evidence-of-implementation.md` already covers "closed doesn't prove
built." This is the inverse: **open doesn't prove unbuilt.** A multi-session agent workflow
routinely does real work against an issue's scope under a *different* issue number (a bug found
mid-investigation, a follow-up split out via Mid-Implementation Scope Discovery) and never
circles back to close the original ticket, even though its acceptance criteria are now fully — or
almost fully — satisfied.

Grepping for the capability ("does the feature exist?") and checking issue status both look
authoritative from outside the session, but neither one is actually checked before starting a
fresh `/work-on-issue N` pass unless the agent specifically thinks to.

## How this surfaced

BuildNest's #117 ("implement Playwright E2E suite") was still OPEN when picked up again. `git log
--oneline --all --grep="117"` immediately surfaced ~25 commits and 4 merged PRs (#648, #653, #659,
#660) spanning 3 prior sessions that had already fully implemented, CI-wired, and iteratively
debugged the suite — including splitting off a genuinely separate architectural decision into its
own tracked issue (#662) along the way. The issue's own checklist in the GitHub body was still
all-unchecked, since nothing had gone back to tick it or close the issue once the last dependent
PR (#660) merged.

## The generalizable check

Before starting implementation on any issue, especially one whose title suggests a large or
multi-session scope (a full test suite, an epic-shaped feature):

1. Run `git log --oneline --all --grep="<issue-number>"` before assuming the issue is
   unimplemented — an OPEN status is not evidence either way.
2. If commits reference the issue number, read them to determine what was actually done, then
   diff that against the issue's stated acceptance criteria one line at a time — some may be
   fully done, some partially (carved into a follow-up), some not started.
3. Close out what's actually finished with a comment tracing the real PR chain, rather than either
   blindly re-implementing or blindly closing without checking.

## Generalizes beyond BuildNest

Any issue-tracker-driven workflow where implementation sessions are discontinuous (a different
session, possibly a different agent, resumes work later) is susceptible — the tracker's own state
field is only as fresh as the last session that remembered to update it, in either direction.

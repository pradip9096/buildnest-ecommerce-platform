---
title: GitHub Issue Hygiene — Stale Issue Root Causes and Closure Protocol
category: process
tags: [github, issues, housekeeping, project-management, ci]
keywords: [stale issues, auto-generated noise, issue backlog, closure, quality gate, superseded]
source_conversations: [Session 2026-07-01]
last_updated: 2026-07-01
confidence: high
evidence_strength: strong
root_cause: "issues accumulate stale because no single mechanism (auto-close on fix, linked PR closure, threshold re-check) reliably retires them once their triggering condition resolves"
impact: medium — an unmanaged backlog obscures which issues represent real, actionable work and wastes reviewer attention across many issues over time
related_lessons:
  - docs/wiki/learned-lessons/shell-pipeline-exit-code-masking.md
---

# GitHub Issue Hygiene — Stale Issue Root Causes and Closure Protocol

## Why Stale Issues Accumulate

A large backlog of open issues typically falls into a small set of root causes:

| Root cause | Description | Signal |
|---|---|---|
| Auto-generated noise | CI or bots open issues on each run (coverage drops, lint violations). Fixed issues are never auto-closed. | Many issues with identical or templated titles |
| Superseded target | The quality threshold the issue was tracking has already been exceeded. | Issue says "reach X%" but current value exceeds X |
| Work done, not closed | The feature or fix was merged but the issue was never linked via `Closes #N`. | PR exists; issue still open |
| Scope absorbed | The work was folded into a larger issue or epic and tracked there. | Duplicate or parent issue exists |
| Requirements changed | The requirement was dropped or redesigned. | No activity; no longer referenced |
| Unclear ownership | No assignee, no label, no milestone — fell through the cracks. | Age > 90 days, no comments |
| Ambiguous scope | Issue title is too vague to action; blocked on clarification that never came. | Only the opening comment, no further discussion |

## Closure Protocol

When doing a periodic backlog review:

1. **Filter by age** — sort by `created:< DATE` and work oldest-first.
2. **Check for PRs** — search for `Closes #N` or `Fixes #N` in merged PRs. If found, close with "Closed by #PR-number."
3. **Check for superseded targets** — compare the issue's stated threshold against the current metric. If exceeded, close with a note showing the current value.
4. **Label before closing** — apply `wontfix`, `duplicate`, `superseded`, or `noise` before closing so the decision is auditable.
5. **Do not batch-close without reading** — skimming titles is insufficient; read at least the first comment to confirm.

## Preventing Auto-generated Noise

Auto-generated issues (e.g., from coverage bots) should be:

- Closed automatically when the triggering condition is resolved, or
- Replaced with a single persistent tracking issue that is updated in place rather than re-opened.

If the CI pipeline opens a new issue on each run, fix the workflow to update or reopen a single issue instead.

## Closing Message Template

```
Closing — [reason].

[Supporting evidence: current metric value / PR number / superseding issue number.]

If this needs to be revisited, re-open with updated scope.
```

Keep it brief. The audit trail is in the label and the linked PR or metric, not the closing comment.

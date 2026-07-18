---
title: "gh issue list --milestone Silently Undercounts — Use gh search issues Instead"
category: tooling
tags: [gh-cli, github-api, milestone, issue-count, readme-staleness]
keywords: [gh issue list milestone wrong count, gh search issues state open closed, milestone open_issues closed_issues includes PRs, README roadmap table stale count, gh CLI pagination cap]
source_conversations: [Session 2026-07-18 (#438)]
last_updated: 2026-07-18
confidence: high
evidence_strength: strong
root_cause: "gh issue list --milestone silently returned an incomplete/wrong result set (30 total for a milestone with 198 real issues) with no error or truncation warning, and gh search issues (which gives the correct count) does not accept --state all — it must be queried with --state open and --state closed separately and summed"
impact: medium — would have caused a stale README fact (137/150) to be replaced with a still-wrong one derived from a silently-undercounting command, propagating the staleness instead of fixing it
related_lessons: []
---

# `gh issue list --milestone` Silently Undercounts — Use `gh search issues` Instead

## Problem

While closing #438 (an unrelated order-refund UI feature), the task list included updating
README's M4 Roadmap row issue count (per `development-workflow.md` step 22's "README aggregate
facts change by construction whenever an issue closes" rule). The obvious command,
`gh issue list --milestone "M4 — Feature Development" --state all -L 300 --json number`, returned
exactly 30 issues total — which happened to look plausible at a glance (a milestone could easily
have ~30 issues) but was silently wrong: the real total was 198.

Cross-checking against `gh api repos/.../milestones` (which reports `open_issues`/`closed_issues`
directly from GitHub's own milestone object) showed 198 for M4 — but that count also turned out to
include the possibility of counting mixed issue/PR milestone assignment, so a third method was
needed to confirm the number was specifically about *issues*, not issues+PRs conflated.

`gh search issues --milestone "M4 — Feature Development" --repo ... --state open --json number` and
the same with `--state closed`, summed, gave 198 (31 open + 167 closed) — matching the milestone
API's own reported figures, and confirmed as issues-only via `gh search issues` (as opposed to
`gh search prs`, which would return pull requests).

## Why It Happened

- `gh issue list --milestone <title>` accepts a milestone *title string*, not a number, and appears
  to silently degrade to some partial or capped result set when title-based milestone filtering
  doesn't resolve cleanly — no error, no truncation warning, just a plausible-looking wrong number.
  This was not investigated to its root GitHub API cause (whether it's a `gh` CLI bug, a REST vs.
  GraphQL backend difference, or a title-matching ambiguity) — only confirmed as *wrong*, via
  cross-checking two independent methods that agreed with each other and disagreed with it.
- `gh search issues` (the search-API-backed command) does **not** accept `--state all` at all —
  passing it produces a CLI usage error (`invalid argument "all" for "--state" flag: valid values
  are {open|closed}`), unlike `gh issue list`, which does accept `--state all`. This asymmetry is
  easy to miss when switching between the two commands, since they look like drop-in equivalents
  for "list issues in a milestone" but are not.

## Fix / Correct Pattern

Never trust `gh issue list --milestone` for an aggregate count that's about to be written into a
document as fact. Use `gh search issues --milestone "<exact title>" --repo <owner>/<repo> --state
open --json number` and the same with `--state closed`, sum the two counts, and cross-check against
`gh api repos/<owner>/<repo>/milestones --jq '.[] | select(.number==N)'`'s own `open_issues`/
`closed_issues` fields as a second independent source before writing the number into any doc.
Agreement between the milestone API and the summed `gh search issues` result is the actual
confirmation — a single command's output, however plausible, is not enough on its own once it has
already been shown capable of being silently wrong.

## Generalization

This is a `gh` CLI behavior, not a BuildNest-specific fact — it applies to any repository that uses
`gh issue list --milestone` or `gh search issues --state all` for milestone-based counting, in any
project.

## Related Articles

None yet — first instance of this specific `gh` CLI gotcha in this repo's lesson set.

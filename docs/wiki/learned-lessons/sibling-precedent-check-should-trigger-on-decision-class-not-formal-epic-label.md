---
title: "The Sibling-Precedent Check Should Trigger on Decision-Class Similarity, Not Just Formal Epic/Label Linkage"
category: process
tags: [development-workflow, sibling-consistency, adr, github-issues, precedent-check]
keywords: [solution-options-adr, sibling issue precedent, decision class vs epic label, AskUserQuestion consistency, codeql placement decision, sonarcloud precedent]
source_conversations: ["Session 2026-07-14, workflow retrospective on issue #358 against development-workflow.md"]
last_updated: 2026-07-14
confidence: high
evidence_strength: strong
root_cause: "development-workflow.md's solution-options-adr step frames the sibling-precedent check in terms of formal epic/sub-issue membership ('when an issue is one of several sibling sub-issues under the same parent epic'), which only fires when GitHub's own issue hierarchy (a shared parent, native sub-issue links, or an explicit label) makes the relationship visible. Two issues can require the same class of decision — 'how do we wire a new static-analysis capability into CI: a new job in the existing security workflow, or a new dedicated workflow file?' — without ever being formally linked as siblings, and the check has no trigger condition for that case at all."
impact: medium — #358 (CodeQL) independently re-derived and AskUserQuestion'd a placement decision (dedicated codeql.yml vs. a job inside security.yml) that #350 (SonarCloud) had already resolved months earlier by adding sonar:sonar as a job inside the existing security.yml job. The precedent existed, was directly comparable, and was never checked, purely because #358 and #350 were never formally linked under a shared epic the way #316's SpotBugs/PMD/CheckStyle/SonarCloud family was
related_lessons:
  - docs/wiki/learned-lessons/keyword-search-github-issues-before-filing-a-new-one.md
  - docs/wiki/learned-lessons/check-sibling-branches-before-filing-a-duplicate-issue.md
---

# The Sibling-Precedent Check Should Trigger on Decision-Class Similarity, Not Just Formal Epic/Label Linkage

## Problem

A retrospective of #358 ("enable real CodeQL semantic analysis") against `development-workflow.md`
found that #358 presented a genuine architectural decision — should the new CodeQL `init`/`analyze`
steps live in a new dedicated `codeql.yml`, or as a new job inside the existing `security.yml`? —
via `AskUserQuestion`, exactly as the workflow's `solution-options-adr` step requires for a real
decision. What it did *not* do was check for precedent first.

`security.yml`'s `code-quality` job already runs SonarCloud's `sonar:sonar` goal as a job inside
that same existing workflow file — a decision made in #350 (fixing SonarQube's CI wiring) months
earlier. #350 and #358 are the same class of decision in every load-bearing respect: both are "wire
a new static-analysis tool's real invocation into CI," both had the identical two options (extend
`security.yml` vs. add a dedicated workflow), and #350's choice (extend `security.yml`) was directly
comparable to #358's actual choice (a new dedicated workflow) — meaning #358 diverged from an
existing precedent without ever citing it as a deliberate departure. The final placement decision
for #358 may well still be correct (CodeQL's `init`/`analyze` shape and GitHub's own recommended
workflow template genuinely differ from a bare Maven goal invocation), but that reasoning was never
weighed against the precedent, because the precedent was never surfaced.

## Why the existing check missed it

`development-workflow.md`'s `solution-options-adr` step's sibling-consistency language reads:

> when an issue is one of several sibling sub-issues under the same parent epic that each
> independently make the same class of decision... check how prior siblings handled that decision
> class before implementing

This is scoped to *formal* sibling relationships — a shared parent epic, GitHub's native
sub-issue links, or a shared tracking label (exactly how #317/#318/#319/#320 were checked against
each other under #316). #350 and #358 share none of that formal structure: #350 was filed to fix a
specific bug, #358 was self-filed later in an unrelated audit session, and nothing in either issue's
metadata declares them related. The check has no trigger for "these two issues require the same
category of decision, even though nothing formally links them."

## The generalizable lesson

Sibling-consistency should be evaluated by **what kind of decision an issue is asking you to make**,
not by **whether GitHub's own issue-tracking metadata happens to already declare a relationship**.
Formal epic/label linkage is a *sufficient* signal to trigger the check (as #316's family
correctly demonstrates) but it is not a *necessary* one — two issues can require the same decision
class while being filed months apart, by different triggers, with no shared parent at all.

**How to apply:** before using `AskUserQuestion` (or making any other real architectural call) on an
issue, ask explicitly: "has this *kind* of decision been made before in this repo, regardless of
whether the prior instance is formally linked to this issue?" A quick keyword/pattern search across
recent CI-related PRs/issues (e.g. "did we add a new CI tool before, and if so, where did it live?")
costs a few minutes and would have surfaced #350 as a directly comparable case. This generalizes
beyond CI-tool placement to any repeated shape of decision in a codebase's history — the check
should be driven by the *question being asked*, not by whether an issue tracker's own hierarchy
happens to make the precedent easy to find.

## What generalizes vs. what's repo-specific

The "trigger on decision-class similarity, not formal linkage" principle generalizes to any
project using an issue tracker with optional epic/sub-issue structure. The specific precedent
(#350's SonarCloud-in-`security.yml` choice) and the specific gap (#358 never checking it) are
BuildNest-specific instances used here as the worked example.

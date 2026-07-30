---
title: "Search Existing GitHub Issues by Keyword Before Filing a New One"
category: process
tags: [github-issues, duplicate-work, issue-creation, ci, quality-gates]
keywords: [duplicate issue, gh issue list --search, file new issue, issue-creation hygiene]
source_conversations: ["#397", "#398"]
last_updated: 2026-07-13
confidence: high
evidence_strength: direct-repo-verification
root_cause: "a new issue was drafted and filed straight from a conversation-level recommendation without ever running a keyword search against the repo's existing open issues first; the fix for the first occurrence (run a search) recurred immediately because the search term chosen was the narrowest specific keyword instead of the parent topic/tool family"
impact: low — two duplicate issues (#397, #398) filed and closed within the same session; caught before either accumulated stale/misleading status, but the second happened even after the lesson from the first was already written down, because the corrective search itself was scoped too narrowly
related_lessons:
  - check-sibling-branches-before-filing-a-duplicate-issue
---

# Search Existing GitHub Issues by Keyword Before Filing a New One

## What happened

After discussing whether to convert the SonarQube/SpotBugs advisory-only CI checks to blocking
gates, the recommendation was drafted and filed directly as a new issue (#397) — titled "Convert
SonarQube/SpotBugs quality gate from advisory to blocking on BLOCKER/CRITICAL severity." No search
of existing open issues was run first. It turned out #316 ("graduate non-blocking quality gates to
enforced/blocking") already existed as a parent tracking issue, with #317 as its dedicated SpotBugs
sub-issue — both already open, both in Milestone 5. #397 was a straight duplicate of already-
tracked work, caught only because the user separately asked "which issue addresses SpotBugs?" as a
follow-up question, not because any step in the filing process itself checked.

## Why it matters

This is a distinct failure mode from
[Check In-Flight Sibling PRs Before Filing a "New" Finding as a Separate Issue](check-sibling-branches-before-filing-a-duplicate-issue.md):
that lesson is about checking *other work already active in the same session's working set*. This
one is more basic — no search was run against the *existing open-issue backlog at all*, sibling
work or not. `gh issue list --search "<keyword>"` (or `gh issue list --search "spotbugs"` in this
case) takes seconds and would have surfaced #316/#317 immediately; the gap wasn't difficulty, it
was simply never running the check.

A duplicate issue isn't harmless even when caught quickly: closing #397 required a comment
explaining the duplication, and — had it not been caught in the same session — it would have sat
alongside #316/#317 indefinitely, splitting future scoping/discussion across issues that should
have been one thread, and potentially causing #316/#317's scope (which already outlines SpotBugs
+ PMD + CheckStyle + SonarCloud together) to drift out of sync with a narrower duplicate covering
only two of those four tools.

## How to apply

Before running `gh issue create` for any newly-identified problem, defect, or improvement —
whether it surfaced from a conversation recommendation, a CI finding, or an incidental discovery
mid-implementation (per `development-workflow.md`'s Mid-Implementation Scope Discovery) — run a
keyword search first: `gh issue list --search "<topic keyword(s)>" --state all`. Check titles and,
if any look plausibly related, read the body before concluding it's genuinely new. This applies
even when the idea originated from the current conversation and feels novel — novelty in the
conversation doesn't mean novelty in the tracker; the same ground may already be filed from a
different angle or session. If an existing issue covers the same ground but from a narrower or
broader scope, fold the new detail into it (as a comment or an amended acceptance criterion) rather
than filing a sibling.

### Recurrence: the fix itself needs a broad-enough search term

The very next issue filed in the same session (#398, "make SonarCloud blocking") repeated the
mistake despite this lesson already being written minutes earlier — because the search that *was*
run used the narrowest specific keyword ("spotbugs", for the #397 check) rather than the parent
topic. #316's sub-issue family (#317 SpotBugs, #318 PMD, #319 CheckStyle, #320 SonarCloud) was only
fully visible under the broader term "static-analysis." A single narrow search is not sufficient
cover — when a new issue is a specific instance of a broader theme (a tool within a category, a
sub-case within a wider concern), search **both** the specific keyword *and* the parent
category/theme term, and if a parent tracking issue turns up at all, always read its full
sub-issue list (`gh api .../issues/{parent}/sub_issues`, or the body's own scope bullets) before
concluding which piece — if any — is still unfiled, rather than stopping at the first search hit.

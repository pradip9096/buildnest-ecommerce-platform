---
title: "Prose-Based Process Checks Need a Recurrence-Tracking and Escalation Mechanism, or a Stateless Agent Will Silently Re-Break Them"
category: process
tags: [development-workflow, self-attested-checks, recurrence, escalation, stateless-agent, amendment-log]
keywords: [defect-class id, recurrence tracking, escalation rule, prose-based check failure, stateless agent memory, checklist compliance gap, sibling-precedent-scope]
source_conversations: ["Session 2026-07-14, follow-up to the #358 workflow retrospective and sibling-precedent-check fix"]
last_updated: 2026-07-14
confidence: high
evidence_strength: strong
root_cause: "A process rule enforced only by an agent reading and remembering a paragraph has no forcing function across sessions — a stateless agent starts every session cold, so a rule fixed once in prose competes with everything else in context on the next occasion it matters, with nothing external to guarantee it's actually applied. The project's own process docs already carried a self-attested/not-mechanically-enforced disclaimer acknowledging this, but had no mechanism to detect when the same gap recurred after being 'fixed' — only an admission that recurrence would probably show up as repeated wiki-lesson entries on the same theme, findable by manually scanning for it."
impact: medium — a sibling-consistency check (development-workflow.md's solution-options-adr step) was fixed once after a real miss (#320), then missed again in the very next comparable case (#358), with nothing flagging that the second miss was a *repeat* of the first until an explicitly-requested retrospective happened to compare both entries side by side. Without that retrospective, the recurrence would have gone unnoticed indefinitely — the failure mode itself (checks silently degrading between sessions) had already been named once before, for a different check (feedback_reasoning_walkthrough.md's 4-signal test going unapplied across multiple sessions), showing this is a recurring failure *class*, not a one-off
related_lessons:
  - docs/wiki/learned-lessons/sibling-precedent-check-should-trigger-on-decision-class-not-formal-epic-label.md
---

# Prose-Based Process Checks Need a Recurrence-Tracking and Escalation Mechanism, or a Stateless Agent Will Silently Re-Break Them

## Problem

BuildNest's `development-workflow.md` fixed a real gap at Amendment #37: a sibling-consistency
check had been missed on #320 (an architectural decision made without checking how prior sibling
issues had handled the same class of decision). The fix was prose — a paragraph added to the
`solution-options-adr` step instructing future sessions to check sibling precedent before using
`AskUserQuestion`.

The very next comparable case, #358, missed the same underlying check again — for a subtly
different but structurally identical reason (the new precedent wasn't formally linked as a
"sibling" the way the first case's precedent was). Nothing detected this as a *second occurrence*
of the same defect class. It was only caught because a full retrospective was explicitly requested
and happened to place both incidents side by side in the same context window.

This is not an isolated incident. `feedback_reasoning_walkthrough.md`'s own "Compliance gap"
section documents the identical failure shape for a completely different check (a 4-signal
richness-scoring step going unapplied across multiple sessions) — meaning *the failure mode itself
recurs across different checks*, not just within one check.

## Why prose-only fixes don't hold

A rule stated only as prose in a process document has exactly one enforcement mechanism: an agent
reading the document and choosing to apply the rule. There is no compiler, linter, or CI gate
checking whether the rule was actually followed — the project's own process docs already carry an
explicit "self-attested, not mechanically enforced" disclaimer acknowledging this. A first fix
demonstrates the rule *can* be applied; it says nothing about whether it *will* be, unprompted, in
a future session that starts with no memory of the fix ever happening. Each session begins cold,
and a paragraph buried among dozens of other amendment entries competes for attention with
everything else relevant to the current task.

## The generalizable mechanism

Three linked pieces, applied together (not independently useful in isolation):

1. **Recurrence tracking via stable defect-class IDs.** Tag each process-doc fix that could
   plausibly recur with a stable slug (e.g. `[defect-class: sibling-precedent-scope]`) in whatever
   changelog/amendment mechanism the doc already has. A later fix for the same underlying gap reuses
   the same slug — turning "did this happen before?" into a `grep`, not a manual recollection.
2. **An escalation rule keyed to occurrence count.** A 1st occurrence gets a normal prose fix. A
   **2nd** occurrence of the same slug means prose has already been shown not to survive one full
   session-reset cycle — the response must escalate to something less dependent on an agent
   recalling a paragraph (e.g. seeding the check as its own explicit task-list item, so it's
   structurally present rather than optionally remembered). A **3rd** occurrence escalates further
   (requiring visible proof-of-check, e.g. pasting a verification command's actual output, so the
   check leaves evidence rather than trust). Beyond that — or immediately, for a high-stakes
   check — escalate to an actual mechanically-enforced hook if the tooling supports one.
3. **A proactive scan, not just reactive retrospectives.** Grep the tagged defect-class IDs for
   keyword overlap with the current task *before* the decision point where the check matters
   (not only when someone explicitly asks for a retrospective) — catching a live recurrence in the
   moment, rather than discovering it only in hindsight.

This mirrors a pattern the same project already applies to its own *source code*: CheckStyle/PMD's
baseline-and-ratchet mechanism and SpotBugs's severity-threshold gate both escalate a check's
strictness based on observed violation history rather than trusting a developer's memory not to
regress. The insight here is that the identical escalation logic applies to a project's *process
documentation* governing an AI agent, not just to its compiled code — a prose rule that has already
failed once needs a stronger enforcement tier the second time, exactly the way a code quality gate
does.

## What generalizes vs. what's repo-specific

The three-part mechanism (tag → escalate by occurrence count → proactively scan) generalizes to any
project maintaining prose-based process rules for an AI agent across stateless sessions — the
specific gap (`sibling-precedent-scope`) and the specific document (`development-workflow.md`) are
BuildNest-specific instances used here as the worked example. The mechanism does not require any
tooling beyond what already exists in most repos (a changelog-style table, `grep`, and — at the
top escalation tier — whatever hook/automation system the host agent framework provides).

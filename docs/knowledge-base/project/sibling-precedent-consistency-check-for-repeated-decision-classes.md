---
title: "Sibling-Precedent Consistency Check for Repeated Decision Classes"
category: tooling
tags: [process, code-review, decision-making, ci-cd, static-analysis]
keywords: [sibling precedent, decision class, AskUserQuestion, epic sub-issues, architectural consistency, defect-class sibling-precedent-scope]
objective: "Before making a real architectural decision, how do I check whether this same *kind* of decision has already been made elsewhere in the repo — even if the two issues were never formally linked?"
audience: "Anyone about to make a real architectural/placement decision on a GitHub issue in this repo — which mechanism to use, where to wire a new check, which of several viable designs to pick."
scope: BuildNest-specific mechanics (GitHub issue/PR search), general principle (decision-class precedent matching)
source_conversations: ["#320", "#358", "#359"]
last_updated: 2026-07-16
confidence: high
evidence_strength: strong
related_articles: [recurrence-escalation-for-process-and-amendment-mechanisms.md]
status: draft
---

# Sibling-Precedent Consistency Check for Repeated Decision Classes

## What Is It?

A check that runs before any real architectural decision (which of several viable designs to
implement, where to wire a new mechanism into the codebase): search the repo's issue/PR history
for a prior instance of the *same kind* of decision, regardless of whether the current issue is
formally linked (shared epic, shared label) to that prior instance. If a precedent exists and used
a particular process (e.g. presenting the user with real alternatives via `AskUserQuestion`), match
that process unless there's a stated reason not to.

This is distinct from `development-workflow.md`'s **Proactive Recurrence Scan** — that scan only
greps the file's own `[defect-class: <slug>]` tags for keyword overlap with the current issue. The
sibling-precedent check is broader: it searches actual repo history (`gh issue list --search`,
`gh pr list --search`) for a comparable prior decision, and must run regardless of whether any
defect-class tag matches. A defect-class miss is not evidence that no precedent exists.

## Why It Matters

Two issues can require the same *class* of decision — "wire a new static-analysis tool's
invocation into CI: extend an existing workflow, or add a dedicated one?" is one recurring
example in this repo — without ever being formally linked as siblings at all. Different filing
sessions, no shared parent issue, no shared label. Without an explicit check, each such issue
independently re-derives the decision from scratch, sometimes landing on the opposite answer from
a directly comparable precedent, with no stated reason for the divergence. That's not just
wasted effort — it's a silent consistency failure a later reader has no way to detect, since
nothing records that the two decisions were ever compared.

## How It Works

1. Before using `AskUserQuestion` (or making any other real architectural call), ask explicitly:
   has this *kind* of decision been made before in this repo, regardless of formal linkage?
2. Run an actual search — `gh issue list --search "<keyword>" --state all` and
   `gh pr list --search "<keyword>" --state all` — not a mental scan of what feels familiar.
3. If a precedent is found (formally linked or not) and it used `AskUserQuestion` over real
   alternatives, match that unless there's a stated reason not to. Don't silently make the call
   unilaterally, and don't silently diverge from an unlinked-but-comparable precedent either.
4. State the search result explicitly in the transcript — a match found (cite it) or none — so
   the check is inspectable rather than an opaque judgment call.

### Escalation on repeat failure

This check has itself failed to survive as prose across multiple sessions (see Examples below).
The occurrence-count escalation ladder that governs it — and the cross-field vocabulary for "a
process fix that regresses" — is covered in full in [Recurrence Escalation for Process and
Amendment Mechanisms](recurrence-escalation-for-process-and-amendment-mechanisms.md), which uses
this exact check as its own worked example; not restated here. The operative fact for this
article: `sibling-precedent-scope` is at its 3rd confirmed occurrence as of this writing, so the
3rd-tier requirement (pasting the actual precedent-search command output as an explicit
precondition — "no precedent found" without shown output no longer satisfies the check) is
currently mandatory for every `solution-options-adr` step.

## When to Use It

- Before any `solution-options-adr` step (`development-workflow.md`, step ID `solution-options-adr`)
  that involves a real design choice — not a mechanical/obvious step.
- Whenever the decision at hand belongs to a recognizable *class* — "which CI-blocking mechanism
  for a new static-analysis tool," "extend an existing workflow vs. add a dedicated one," "which of
  several viable non-root/security-hardening approaches" — even if no formal epic or shared label
  connects the current issue to a prior one.

## Examples

**#320 (SonarCloud blocking) vs. #317/#318/#354 (formal siblings under epic #316).** Each of
#317/#318/#354 presented the user with 2-4 real blocking-mechanism options via `AskUserQuestion`
before implementing. #320 — the last sub-issue in the same formally-linked family — unilaterally
chose `sonar.qualitygate.wait=true` over two other viable mechanisms with no `AskUserQuestion`,
breaking a pattern its own formal siblings had already established. First occurrence of this
defect class.

**#358 (CodeQL) vs. #350 (SonarCloud CI wiring) — no formal linkage at all.** #350 had already
chosen "extend the existing `security.yml`" for the class of decision "wire a new scan into CI."
#358 was never formally linked to #350 (different filing sessions, no shared parent/label), so a
check that only fires on formal linkage never triggered — #358 independently re-derived the
identical placement decision via `AskUserQuestion` and landed on the *opposite* answer (a new
dedicated workflow), with no stated reason for the divergence. This is what motivated reframing the
check around decision-class similarity rather than formal linkage. Second occurrence.

**#359 retrospective — the check conflated with the narrower Proactive Recurrence Scan.** Finding
no defect-class tag match, the session went straight to presenting `AskUserQuestion` alternatives
without running a separate precedent search — a reminder that this check and the Proactive
Recurrence Scan are two different searches, not one. (Third occurrence; see the Escalation article
above for the ladder mechanics this triggered.)

## Synthesis

The underlying mental model is **precedent-matching, then letting the new instance's own data
decide which precedent transfers** — not "has this exact issue been seen before" (too narrow,
misses #358/#350) and not "does this feel novel" (unreliable, exactly how #320 and #358 both
slipped through). The recognition cue: *when an issue requires picking between multiple viable
technical approaches for the same general kind of problem this repo has solved before, search for
that decision class explicitly before deciding — regardless of whether GitHub's own linkage
features (epics, labels, sub-issues) connect the two instances.*

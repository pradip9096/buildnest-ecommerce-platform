---
title: "Sibling-Precedent Consistency Check for Repeated Decision Classes"
category: tooling
tags: [process, code-review, decision-making, ci-cd, static-analysis]
keywords: [sibling precedent, decision class, AskUserQuestion, epic sub-issues, architectural consistency, defect-class sibling-precedent-scope]
objective: "Before making a real architectural decision, how do I check whether this same *kind* of decision has already been made elsewhere in the repo — even if the two issues were never formally linked?"
audience: "Anyone about to make a real architectural/placement decision on a GitHub issue in this repo — which mechanism to use, where to wire a new check, which of several viable designs to pick."
scope: BuildNest-specific mechanics (GitHub issue/PR search), general principle (decision-class precedent matching)
source_conversations: ["#320", "#358", "#359", "#441", "#489", "#559", "#130", "#652", "#630", "#650", "#558"]
last_updated: 2026-08-02
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

## Documented Edge Cases

The base check ("search for a prior instance of the same decision class, match its process
unless there's a stated reason not to") has accumulated eight documented edge cases, each
surfaced by a real issue where the base check's plain wording didn't obviously cover the
situation encountered. Numbered first-through-eighth in discovery order; `development-workflow.md`
step 8 (`solution-options-adr`)'s own Notes cell keeps only a one-line pointer to this section —
this is the operative content when applying the check.

**First case — a single, unambiguous precedent file to mirror.** E.g. building `ComponentB.tsx`
by directly reading and matching an already-completed sibling `ComponentA.tsx` line-for-line, with
no competing design to weigh. Reading that precedent file directly satisfies the check — a
`gh search`-and-paste-output pass is not required on top of it.

**Second case — no single clear precedent, or only a partial/by-analogy match.** Fall back to the
`gh search` route (`gh issue list --search`, `gh pr list --search`) whenever the first case doesn't
apply — that's when the self-determination "no real decision needed" judgment call actually needs
external verification instead of being taken on faith.

**Third case, distinct from both** (confirmed on #441): *multiple* partial/non-matching candidates
exist (none a clean 1:1 mirror), and the agent judges "no real decision needed" without running a
search. Defaulting this to "no search needed" on the first case's reasoning would be an unstated
extension of that allowance — treat it the same as the second case: run the `gh search` route
before concluding no decision is needed.

**Fourth case, distinct from all three** (confirmed on #489): the issue contains **no decision
class at all** — no competing design, no UI-placement choice, no prior-art question, nothing this
check is built to compare against (e.g. a pure investigation-and-numeric-correction issue). No
`gh search` is required (there is nothing to search for), but the check must still be explicitly
stated as N/A with the reason ("no decision class present") in the visible response — silently
skipping the step because "obviously nothing to decide" is indistinguishable, on a later audit,
from having forgotten to check it at all.

**Fifth case, distinct from all four** (confirmed on #559): *multiple* sibling files exist (not
just one), and reading all of them directly shows they all mutually agree on the same pattern — no
partial/non-matching divergence between them. Treat mutually-consistent multiple siblings the same
as the first case: reading all of them directly satisfies the check, no `gh search` required — the
consistency across siblings is itself the confirmation a `gh search` would otherwise establish.

**Sixth case, distinct from all five** (confirmed on #130): a real decision class only *emerges*
mid-task — the Proactive Recurrence Scan's own disposition of this check (made before the decision
existed) correctly said N/A at the time, but nothing re-triggers the check once the decision
actually surfaces later in the same session. The moment a genuine decision-class question emerges
mid-task (even if resolved correctly via `AskUserQuestion`), seed this check as its own explicit
`TaskCreate` item at that point — don't let a stale, pre-decision N/A stand uncorrected. This
explicitly includes a Mid-Implementation Scope Discovery SAME-vs-SEPARATE classification
(`development-workflow.md`'s own decision tree) — confirmed missed on #652 (two such calls made
inline, neither seeded as its own `TaskCreate` item, since the "architectural/precedent decision"
framing wasn't read as covering a scope classification). A SAME/SEPARATE call is a real
decision-class question in the sense this case already means; don't require it to also resemble a
design/tooling choice before it counts.

**Seventh case, distinct from all six** (confirmed on #630): a real precedent existed and *was*
found and correctly followed, but only incidentally — via an ordinary code-read while implementing
(e.g. grepping for how a sibling test class seeds its own data and mirroring it), not through a
deliberate "let me check for a sibling pattern" step. This satisfies the check in substance the
same way the first case does, but never gets classified as such at the time, leaving a later audit
unable to credit the process for it. When a precedent is found this way, name it explicitly as
satisfying this check in the same task-list item/commit message that uses it (e.g. "mirrors `X`'s
existing pattern, found via grep") — the citation itself is the artifact, no separate search step
required on top of it.

**Eighth case, distinct from all seven** (confirmed on #650): the "precedent" isn't a sibling
GitHub issue or file at all — it's a wiki lesson (`docs/wiki/learned-lessons/`) whose full body
already states the correct answer to the exact decision being made (not just a related gotcha).
This is a distinct precedent *source* from the other seven cases, which all frame precedent as
repo-history/sibling-file search — and it carries the same rigor requirement as any of them:
reading the wiki-lessons Index grep's one-line description and confirming topical relevance is
**not** sufficient; the full lesson file must actually be opened and its stated fix content
applied, not just cited as "found." On #650, the matched lesson's body stated the correct fix
verbatim across 3 separate re-greps, but only the index description was ever read — the wrong
assumption was caught only by a live test failure, not by this check. See `work-on-issue.md`'s
`[defect-class: wiki-second-leg-not-regrepped]` paragraph for the matching seeding-side fix.

**A related but distinct risk — SonarCloud duplication** (confirmed on #558): mirroring a sibling
closely (per the first or fifth case above) can itself trip SonarCloud's new-code duplication
gate. If the mirrored file's control-flow blocks end up byte-for-byte identical to the precedent's
(not just similarly shaped), extract the shared logic into a small utility rather than copy-pasting
it — see [Sibling-Precedent Mirroring Can Trip SonarCloud's New-Code Duplication
Gate](../../wiki/learned-lessons/sibling-precedent-mirroring-can-trip-sonarcloud-new-code-duplication-gate.md).

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

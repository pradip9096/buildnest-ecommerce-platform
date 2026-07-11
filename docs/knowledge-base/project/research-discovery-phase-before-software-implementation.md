---
title: Research/Discovery Phase Before Software Implementation
category: documentation
tags: [sdlc, requirements-analysis, feasibility, discovery, process-design, research]
keywords: [research phase, discovery phase, requirements analysis, feasibility study, solution options, ADR, context7, web search, pre-implementation, problem identification, scope definition]
objective: Define what the research/discovery phase before writing implementation code actually consists of, why skipping it is expensive, and show how this repo's own development-workflow.md instantiates it as a concrete, named sequence of steps.
audience: engineers planning any non-trivial feature or fix, in this repo or elsewhere, who want to know what "do the research first" concretely means before touching code
scope: general SDLC pattern (requirements analysis / discovery / feasibility), with a BuildNest-specific worked example
source_conversations: [Session 2026-07-11]
last_updated: 2026-07-11
confidence: high
evidence_strength: strong
related_articles:
  - closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md
  - post-implementation-learning-activities.md
  - adaptive-knowledge-governance-advanced-amendment-concepts.md
  - claude-code-extension-mechanisms.md
  - ../../../.claude/rules/common/development-workflow.md
status: published
---

# Research/Discovery Phase Before Software Implementation

## What Is It?

The **research (or discovery) phase** is the set of activities performed *before* writing
implementation code, whose job is to establish what should be built, why, and whether it's
feasible — before committing to how. It answers three questions in order: is the problem real and
correctly understood, what are the actual requirements and constraints, and which solution
approach is viable — strictly before any of "how do I implement approach X" gets asked.

It goes by different names depending on the methodology: **requirements analysis** (Waterfall),
**discovery** (Agile/Scrum), **empathize + define** (Design Thinking), or simply **planning &
analysis** in a generic SDLC description. The vocabulary varies; the underlying activity — front-
loading understanding before front-loading code — is the same across all of them.

## Why It Matters

Code written against a misunderstood requirement, an infeasible design, or a solution that
duplicates something already in the codebase is wasted work. Worse, that waste is usually
discovered *after* the code exists — during review, testing, or production — which is a more
expensive point to discover it than before a single line was written. The research phase is cheap
insurance bought early, against a cost that only grows the later a misunderstanding surfaces.

The phase is also where architecture decisions belong. A design choice made mid-implementation,
under the pressure of "I've already started, might as well," is a worse decision-making context
than one made deliberately with the actual alternatives laid out — even though in practice, some
architectural decisions only reveal themselves as necessary once implementation is underway (see
`solution-options-adr` in the worked example below, which explicitly accounts for this).

## How It Works

The research phase decomposes into a small number of distinct sub-activities, each answering a
different question. They don't have to happen in strict sequence, and not every activity applies
to every piece of work — but conflating two of them into one blurred activity is the same
mixed-scope failure mode documented in
[Stable ID Columns Decouple Cross-References from Display Order](stable-id-columns-decouple-cross-references-from-display-order.md)'s
"When to Split" reasoning, just applied to process activities instead of table rows.

### Problem identification

Is the stated problem real, and correctly described? Don't take a ticket's framing as
automatically accurate — verify it against the actual system. A surprising number of "bugs" turn
out to be already-fixed, mischaracterized, or not bugs at all once checked against current reality
rather than trusted as-filed.

### Requirements gathering

Functional requirements (what it must do), non-functional requirements (performance, security,
scale), constraints, and acceptance criteria. In some workflows this is a dedicated activity; in
others (see the worked example below) it's implicit because the work-item format already forces
it at creation time.

### Existing-system assessment

Does something like this already exist? Building a second version of an existing capability, or
missing that a "new" requirement is already satisfied, is a specific and avoidable form of wasted
research-phase-worthy effort.

### Feasibility and solution-option research

Can this actually be built, and if there's more than one viable approach, what are the tradeoffs
between them? This is where external research earns its keep — but *which kind* of external
research matters:

| Research question | Right tool | Why |
|---|---|---|
| "What's the exact API/config for library X?" | Official docs (e.g. an MCP documentation server) | Needs the *current*, authoritative API surface — not a summary, not training-data recall of a possibly-stale version |
| "What's the idiomatic way people use library X for this?" | Web search | Needs real-world usage patterns, common gotchas, and community consensus — not covered by API reference docs alone |
| "Which of these two architectural approaches is better, and why?" | Web search / prior art / tradeoff discussion | This is feasibility/design research, not API lookup — it belongs to the solution-options sub-activity, not the "how do I call this function" sub-activity |

The first two rows are the same *kind* of research (implementation mechanics for something already
decided on); the third is a genuinely different kind (deciding between options), and conflating it
with the first two is exactly the mistake worth avoiding — see the worked example below for how
this repo separated them into two distinct process steps after initially merging them.

### Scope definition

Drawing the actual boundary of what's in vs. out for this specific piece of work — separate from
requirements gathering (what it must do) and closer to "what it explicitly does *not* need to do
right now."

## When to Use It

Apply research-phase rigor proportionally to risk and ambiguity, not uniformly to every change:

- **Skip or minimize** for a scoped, well-understood bug fix where the problem, fix, and impact are
  all already clear — full ceremony here is pure overhead.
- **Apply in full** when the feature touches an unfamiliar library/pattern, involves a real
  architectural decision (schema changes, cascading behavior, a new cross-cutting concern), or
  when the problem statement itself is ambiguous enough that two people could reasonably disagree
  on what's actually being asked for.
- **Revisit mid-implementation** when a design decision surfaces that wasn't visible during initial
  research — this is normal, not a process failure, provided it's actually written up once it
  surfaces rather than silently absorbed.

## Examples

### Real example: `development-workflow.md`'s first ten steps (this repo)

`.claude/rules/common/development-workflow.md` names this phase as an explicit, ordered sequence
of steps — not a single "do research" checkbox, but the sub-activities above given their own
identity:

| Step ID | Sub-activity | Maps to |
|---|---|---|
| `init-context` | Repo familiarity | Precondition for everything else |
| `external-research` | Docs/API lookup + implementation-pattern research | Feasibility research, narrow sense — "how do I build this" |
| `identify-problem` | Problem identification | Verify the issue's premise against the real repo |
| `assess-existing` | Existing-system assessment | Don't duplicate or miss something already built |
| `gap-analysis` | — | Worth it for ambiguous/large features only |
| `define-scope` | Requirements gathering | Already carried in the GitHub issue body in this repo's convention |
| `requirement-traceability` | — | Links to a formal requirement, when one exists |
| `solution-options-adr` | Feasibility/design-option research | "Which approach is better, and why" — deliberately separated from `external-research`, see below |
| `create-epics` / `decompose-subissues` | Scope decomposition | Only for genuinely large, multi-issue work |

`external-research` and `solution-options-adr` were originally a single, informally-blurred
concern; a 2026-07-11 session split them explicitly after noticing the same tool question — "should
`context7` and web search both live under one research step?" — kept surfacing a distinction that
the single step's wording couldn't cleanly express. The fix wasn't to remove either activity, but
to give each its own place: `external-research` answers "how do I build the thing I've decided on,"
`solution-options-adr` answers "which thing should I decide on." Only after all of this does the
sequence move into `task-list-plan`, `create-branch`, and actual implementation.

### Illustrative example: a feature touching an unfamiliar library

Consider adding a new real-time notification channel to an application that has never used
Server-Sent Events before. The research phase for that work would concretely mean: confirm the
problem statement's acceptance criteria actually match what the codebase's security/auth
conventions can support (`identify-problem`); check whether anything resembling a push-notification
mechanism already exists (`assess-existing`); look up the framework's actual SSE API surface via
official docs (`external-research`, docs half); check how others have typically wired authentication
into a long-lived SSE connection given the constraint that some client APIs can't set custom headers
(`external-research`, pattern half); and, only if the endpoint's path or security-rule placement
turns out to be a genuine architectural fork rather than an obvious choice, write up why one option
was chosen over the other (`solution-options-adr`).

## Synthesis

The research phase isn't a single monolithic "think before you code" instruction — it's a small set
of distinct questions (is the problem real, what's already there, is it feasible, which approach,
what's the scope) that are easy to blur together into one vague activity, and that blurring is
itself a real cost: a mixed-scope step is exactly as hard to reason about and act on as a
mixed-scope code module. Naming each sub-activity separately, proportioning how much of each
applies to the risk of the work at hand, and being willing to split further the moment two
questions start needing different answers, is what turns "do the research" from a slogan into
something that actually gets done consistently.

## Related Articles

- [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md) — the amendment-log discipline that let `development-workflow.md`'s research steps evolve (split, reworded) without losing the history of why
- [Post-Implementation Learning Activities](post-implementation-learning-activities.md) — the closing bookend to this phase: research happens before implementation, lessons-learned happens after; together they frame the whole non-implementation surface of an issue's lifecycle
- [Advanced Amendment Concepts: Toward an Adaptive Knowledge Governance Framework](adaptive-knowledge-governance-advanced-amendment-concepts.md) — covers ADRs and traceability matrices in depth, both directly relevant to the `solution-options-adr` and `requirement-traceability` sub-activities above
- [Claude Code Extension Mechanisms](claude-code-extension-mechanisms.md) — covers sourcing a Claude Code capability (plugin/agent/hook) itself via the official marketplace, as distinct from researching how to implement an application feature
- [development-workflow.md](../../../.claude/rules/common/development-workflow.md) — the live, real-world instantiation of this pattern as a concrete step sequence in this repo

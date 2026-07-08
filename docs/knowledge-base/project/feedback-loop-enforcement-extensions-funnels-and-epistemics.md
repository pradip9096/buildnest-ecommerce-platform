---
title: "Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness"
category: documentation
tags: [feedback-loop, funnel, open-loop, population-throughput, known-unknown, unknown-known, loop-observability]
keywords: [funnel, funnel structure, funnel pattern, sales funnel, conversion funnel, open-loop, population throughput, single-instance trajectory, known unknown, unknown known, Rumsfeld matrix, Johari window, tacit knowledge, Polanyi, Zizek, disavowal, loop observability]
objective: Place the funnel (a chain of filters/quality-gates introducing population-throughput and open-loop-by-default axes) and the known-unknown/unknown-known distinction (loop observability vs. loop correctness) onto the feedback-loop taxonomy — split out from the core enforcement-and-safety companion to keep each article focused.
audience: anyone who has read the enforcement-and-safety-vocabulary companion and wants the funnel-structure and epistemic-awareness depth specifically
scope: general (process design, epistemology) with BuildNest-specific worked examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-enforcement-and-safety-vocabulary.md
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md
  - quality-gate-ratchet-pattern.md
status: published
---

# Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness

## What Is It?

[Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)
maps fourteen core terms onto "how does a criterion get teeth." This companion covers two later
additions that don't share the same theme as those fourteen but are both genuinely structural: the
**funnel** (a chain of filters/quality-gates that introduces two axes — open-loop-by-default and
population throughput — nothing else in this family needed until a funnel forced the question), and
**known unknown vs. unknown known** (the observability of a loop's own sense/compare stage, distinct
from whether that loop actually works).

## Why It Matters

Both additions answer a version of the same underlying question the core companion doesn't: not
"does the enforcement mechanism exist," but "does the loop even have the shape a feedback loop
needs" (a funnel usually doesn't, by default) and "is the loop's own operation visible to whoever
would need to act on it" (not always, and for two structurally different reasons).

## How It Works

### Funnel — a chain of filters/quality-gates, and the two axes it introduces

A **funnel** (also called a funnel structure, pattern, or shape — informal register variants, not
distinct concepts) is a sequence of stages that progressively narrows: a population enters at the
wide end, each stage applies a **filter** or **quality gate**
([Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)),
and only a shrinking subset proceeds — a sales funnel, a hiring funnel, a CI pipeline (lint → unit
tests → integration tests → security scan → merge). Structurally it isn't a new mechanism — it's
several filters/quality gates chained in sequence — but it exposes two axes nothing else in this
family has needed:

1. **Open-loop by default.** A raw funnel has no "repeat" arrow feeding later-stage outcomes back
   to correct earlier stages — an item dropped at stage 3 doesn't cause stage 1 to change. This is
   the open-loop/closed-loop distinction from
   [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md),
   applied to a multi-stage pipeline instead of a single action.
2. **Population throughput, not single-instance trajectory.** Every other domain instance in this
   family (gradient descent's parameter, PIT's mutation score, a person's development) tracks *one*
   system's state evolving over repeated cycles. A funnel is the opposite shape: *many* independent
   entities, each undergoing the same fixed sequence exactly once.

A funnel can still be wrapped in a feedback loop **one level up, around it, not inside it**: a team
watching per-stage conversion rates and redesigning a stage release over release is ordinary
closed-loop control (conversion rate = the compare-stage signal, redesigning a stage = the act) —
the funnel itself stays open-loop; the process of *iterating on* the funnel is where the loop
actually lives. BuildNest's own CI pipeline is a literal funnel — no PR sent back to "re-enter" at
lint after failing the PIT gate — with the milestone-over-milestone `mutationThreshold` ratchet as
the closed-loop process wrapped around that otherwise open-loop pipeline.

### Known unknown vs. unknown known — loop observability vs. loop correctness

Everything mapped in this family so far quietly assumes the sense/compare stage's output is
*observable* to whatever level is doing the describing. Rumsfeld's 2002 briefing named three
categories precisely (known knowns; known unknowns — "we know there are some things we do not
know"; unknown unknowns) but never "unknown known" — that term was introduced separately, and it's
worth being precise that it actually names **two different things**, not one:

- **Polanyi's tacit knowledge** (*The Tacit Dimension*, 1966) — "we know more than we can tell."
  A cyclist's balance-correction *is* a real, functioning closed-loop control system (tilt sensed,
  steering corrected) — the loop works perfectly. What's missing isn't the loop, it's a reporting
  channel from that loop up to the conscious, articulate layer. Benign: nothing is being hidden,
  the mechanism just isn't introspectable.
- **Žižek's unknown known** (2004, on Abu Ghraib) — *disavowed* knowledge: the compare stage
  produced a correct signal (a **criterion** correctly flagged a violation), and a higher-level
  actor suppressed or ignored the **enforcement mechanism** rather than acting on it. Not a missing
  sensor — a working sensor whose output was overridden. Structurally closer to "the criteria
  caught it and the report was buried" than to "there was no way to know."

Naming these separately matters because they imply different fixes: a Polanyi-style unknown known
needs an introspection/reporting channel added to an already-working loop; a Žižek-style one needs
the enforcement mechanism protected from override, not a new sensor. **Known unknown** — the fourth
distinct case worth completing the picture — is a documented gap: the compare stage is *known* to
be missing an input, which is exactly what a **prerequisite** or a coverage-gate finding makes
explicit before the gap gets closed.

## When to Use It

- A new check is being added to a pipeline with multiple stages → decide whether it's genuinely a
  funnel (population throughput, open-loop by default) before assuming it needs a "repeat" arrow —
  most funnels don't, and the correction loop belongs one level up around the whole funnel.
- A rate limit, hiring process, or CI pipeline is chosen as a design pattern → check whether the
  team also intends to watch and improve its per-stage conversion rates over time; if not, it's a
  purely open-loop funnel, which is often fine, but worth being deliberate about.
- Something was "known" but nobody acted on it → determine whether the gap was a missing reporting
  channel (Polanyi) or a suppressed enforcement mechanism (Žižek) before proposing a fix — the two
  need different remedies.
- A documented but unaddressed gap surfaces → that's a known unknown, distinct from a true blind
  spot (unknown unknown) where nothing was ever being measured at all.

## Examples

BuildNest's own CI pipeline (lint → unit tests → integration tests → security scan → PIT gate →
merge) is a funnel in the structural sense defined above — population throughput (each PR passes
through once), open-loop by default (a PR that fails the PIT gate doesn't cause lint to change its
rules). The milestone-over-milestone ratchet on `mutationThreshold` is the closed-loop process
wrapped around that otherwise open-loop funnel, exactly the two-layer relationship described above.

## Synthesis

A funnel and the known-unknown/unknown-known distinction don't share a common origin the way the
fourteen terms in the core enforcement companion do, but they answer the same kind of question in
different registers: does this system actually have the shape it's assumed to have (a funnel is
usually assumed to be a feedback loop and usually isn't, by default), and is what the system knows
actually visible to whoever needs to act on it (sometimes it is and nobody's listening, sometimes
it isn't and nobody could have). Both are cautions against taking a system's apparent structure or
apparent awareness at face value.

## Quick Reference

| Question | Answer |
|---|---|
| Is a funnel a feedback loop? | No, not by itself — a raw funnel is open-loop (no stage corrects an earlier one). It can be *wrapped* in a feedback loop one level up, when someone observes per-stage conversion rates and redesigns a stage over time |
| Is a funnel the same kind of thing as gradient descent or a ratchet? | No — those track one system's state over repeated cycles; a funnel tracks many independent entities each passing through a fixed sequence once. Population throughput vs. single-instance trajectory |
| Is "unknown known" one concept? | No — Polanyi's tacit knowledge (a working but non-introspectable loop, benign) and Žižek's disavowed knowledge (a working criterion whose enforcement was suppressed) are genuinely different, despite sharing the label |
| Is a known unknown the same as a missing sensor? | Not quite — it's a *documented* gap: the compare stage is known to be missing an input. An unknown unknown is the true blind spot, where nothing was ever being measured at all |

## References

- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md) — the filter, quality gate, criteria, and enforcement mechanism definitions this article's funnel and epistemics sections build on.
- [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md) — the open-loop/closed-loop distinction the funnel section applies to a multi-stage pipeline.
- Rumsfeld, D. (2002). U.S. Department of Defense news briefing, February 12, 2002 — origin of "known knowns," "known unknowns," and "unknown unknowns"; did not name "unknown known."
- Polanyi, M. (1966). *The Tacit Dimension*. University of Chicago Press. — origin of tacit knowledge, the benign sense of "unknown known" used above.
- Žižek, S. (2004). "What Rumsfeld Doesn't Know That He Knows About Abu Ghraib". *In These Times*. — origin of the disavowal sense of "unknown known," distinct from Polanyi's tacit knowledge.
- `~/.claude/rules/spring/resilience4j.md` — this repo's own circuit-breaker/pipeline conventions.

## Related Articles

- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)
- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)

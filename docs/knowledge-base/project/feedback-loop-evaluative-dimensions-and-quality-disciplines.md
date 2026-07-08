---
title: "Feedback Loop Extension: Evaluative Dimensions and Quality Disciplines"
category: documentation
tags: [feedback-loop, quality-assurance, quality-control, quality-management, optimization, efficiency, effectiveness, excellence, sustainable-growth, trajectory-descriptors, satisficing]
keywords: [efficiency, effectiveness, efficacy, excellence, optimization, fitness function, quality assurance, quality control, quality management, ISO 9001, Baldrige, EFQM, Drucker, refinement, progression, improvement, sustainable growth, satisficing, bounded rationality, Herbert Simon]
objective: Place efficiency, effectiveness, excellence, optimization, satisficing, quality assurance, quality control, quality management, refinement, progression, improvement, and sustainable growth onto the feedback-loop taxonomy — as evaluative dimensions (what "better" means), a domain-specific quality discipline, and trajectory descriptors (patterns observed across cycles), not as new loop-shape concepts. Satisficing is the corrective to treating optimization as this family's universal goal.
audience: anyone who has read the base taxonomy and wants the "what does the loop measure against, and what pattern does it produce" extension
scope: general (quality management, business strategy) with BuildNest-specific worked examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - quality-gate-ratchet-pattern.md
  - feedback-loop-ml-training-optimization-landscape.md
  - feedback-loop-ml-search-strategy-and-generalization.md
  - feedback-loop-enforcement-and-safety-vocabulary.md
  - feedback-loop-extrema-equilibria-and-physics-grounding.md
status: published
---

# Feedback Loop Extension: Evaluative Dimensions and Quality Disciplines

## What Is It?

[Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
maps seven core terms onto the shape of a cybernetic loop (sense → compare → act → repeat). This
article extends that map with eleven further terms — **efficiency, effectiveness (efficacy),
excellence, optimization, quality assurance, quality control, quality management, refinement,
progression, improvement, sustainable growth** — that answer a different question than the base
seven: not "what shape is the loop" but **"what does the loop measure against"** and **"what
pattern does it produce across many cycles."** None of these are new loop-shape concepts; each
slots into one of three categories below.

## Why It Matters

Without this extension it's easy to conflate *measuring* something with *correcting* it. A metric
can be perfectly well-monitored (live monitoring, already in the base taxonomy) and still not
improve anything, because nothing has defined what "better" means for that metric — that's the gap
evaluative dimensions fill. Separately, "quality" gets used as if it were one thing, when in
practice it splits into a preventive discipline (QA), a detection discipline (QC), and the
organizational system containing both (QM) — conflating them makes it hard to diagnose whether a
quality failure is a design problem, an inspection problem, or a systemic one.

## How It Works

### Evaluative dimensions — candidates for the compare stage's reference value

These are candidate fitness functions, in the sense
[Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) already formalizes the term:

| Term | What it is | Maps to |
|---|---|---|
| **Efficiency** | Output/input ratio — "doing things right" | One candidate fitness function |
| **Effectiveness / efficacy** | Degree of goal attainment — "doing the right things," regardless of cost | A distinct candidate fitness function (Drucker's classic efficiency/effectiveness pair — a system can be efficient at the wrong goal, or effective but wasteful) |
| **Excellence** | A broader, often qualitative composite of several dimensions at once (efficiency + effectiveness + innovation + culture — how the Baldrige and EFQM Excellence Models are structured) | An aspirational, holistic reference point — closer to a watermark (best-ever-reached state) than a single crisp fitness function |
| **Optimization** | The practice of pursuing a defined objective function's best value | What makes the compare stage mathematically explicit; presupposes a fitness function already exists |

**Why "dimension" is the literal right word here, not loose metaphor.** A **dimension** is one
independent coordinate needed to specify a point in a space — efficiency and effectiveness are
called evaluative dimensions because they're genuinely independent axes in exactly that
mathematical sense: efficiency doesn't determine effectiveness or vice versa (a system can be
efficient at the wrong goal, or effective but wasteful), the same way a point's x-coordinate
doesn't determine its y-coordinate. See
[Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
for dimension in its more technical sense (search-space size, the mechanism behind why saddle
points dominate in high-dimensional optimization) — same underlying concept, applied here to
evaluation criteria instead of a loss landscape.

### Satisficing — the correction to treating "optimum" as the universal goal

The **optimization** row above ("pursuing a defined objective function's best value") describes
only one mode the compare stage can run in. Herbert Simon's **satisficing** (1956) names the far
more common one: choosing an option that clears a *threshold* — good enough, given the cost, risk,
or plain infeasibility of searching further — rather than exhaustively pursuing the best possible
value. A large share of the mechanisms already mapped across this family are satisficing, not
optimizing:

- A **ratchet** ([Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)) locks in whatever
  has been achieved — it never claims that value is *the* optimum, only that it clears the current
  bar and won't be allowed to fall back below it.
- **Regularization** ([Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md))
  deliberately accepts a *worse* fit on the training data in exchange for better generalization —
  a calculated departure from the training-set optimum, not a pursuit of it.
- **Guardrails, hysteresis, and control limits** ([Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md),
  [Feedback Loop Domain Depth: PID Control, Hysteresis, and Feedback Delay](feedback-loop-control-engineering-pid-hysteresis-and-delay.md))
  don't search for a best value at all — their success condition is "stayed within bounds," not
  "found the best point."
- A **Nash equilibrium** ([Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md))
  is frequently *not* optimal for the group (the prisoner's dilemma is the canonical case) — it's a
  stable state, not a best one.
- **Criteria** and **quality gates** ([Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md))
  ask "did this clear the bar," a satisficing question, not "is this the best achievable," an
  optimizing one.

The precise takeaway: this family's actual throughline is **sense → compare → act → repeat**, where
the compare stage's reference can be an optimum (sometimes), a satisficing threshold (often), a
stable-but-not-best equilibrium (sometimes), or simply a boundary not to cross (frequently).
Optimization is one mode this structure runs in — not the destination all of it is secretly aimed
at, and treating it that way erases exactly the mechanisms (the ratchet's and regularization's
deliberate departures from pure optimization) that make this family's distinctions worth having.

### Quality disciplines — domain-specific instantiations of the whole loop

Same layer as Closed-Loop Control System and Continuous Improvement in the base taxonomy, but for
the product/service-quality domain specifically:

| Term | What it is | Maps to |
|---|---|---|
| **Quality Control (QC)** | Sense + compare stages applied to already-produced output — reactive, inspection-based, checked against a defined standard after the fact | A discrete/sampled, evaluative sibling of live monitoring |
| **Quality Assurance (QA)** | Designing and validating the loop itself (sensors, standards, process) *before* defects occur — prevention-focused, not detection-focused | A meta-level discipline one layer above a single loop's runtime |
| **Quality Management (QM)** | The organizational umbrella containing both QA and QC, structured around continuous improvement (this is literally how ISO 9001's QMS is built — around PDCA) | A full loop instance — same layer as Closed-Loop Control System (engineering domain) and Continuous Improvement (organizational domain), but for the quality domain specifically |

### Trajectory descriptors — patterns observed across many cycles, not stages of a single loop

| Term | What it is | Maps to |
|---|---|---|
| **Refinement** | A small, incremental correction within one cycle | The verb for what a successful *act* stage does in a single iteration |
| **Progression** | Advancing through cycles over time, without necessarily claiming each cycle was better | Iteration, viewed longitudinally |
| **Improvement** | A directional claim: this cycle's measured value moved closer to the reference than the last | The atomic unit continuous improvement (PDCA) repeats — continuous improvement is many improvements chained by iteration |
| **Sustainable growth** | A positive trend validated against resource/systemic constraints so it doesn't quietly accumulate hidden debt or collapse later | Closest to a ratchet (a trend that shouldn't reverse), but adds a constraint the ratchet concept alone doesn't carry — awareness of what the growth is costing elsewhere in the system. Best described as a *debt-aware ratchet* rather than forced into an existing box |

## When to Use It

Reach for this extension when a discussion conflates *measuring* with *correcting toward a goal*,
or conflates the three faces of "quality":

- A metric is tracked (live monitoring exists) but nobody has agreed what "better" means for it →
  name the evaluative dimension (efficiency? effectiveness? some composite excellence standard?)
  before arguing about whether the number is good.
- A "quality problem" surfaces → ask whether it's a QA gap (nothing designed to prevent this), a QC
  gap (nothing designed to detect this before shipping), or a QM gap (no organizational process ties
  QA and QC together) — the fix differs by which one is actually missing.
- Someone claims "sustainable growth" for a rising metric → check whether anyone has actually
  validated it against resource/systemic constraints, or whether it's just an unqualified ratchet
  dressed in reassuring language.

## Examples

BuildNest's PIT mutation gate (fully detailed in
[Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)) uses **mutation score** as its
evaluative dimension — a specific, narrow fitness function, not a composite "excellence" standard.
The milestone-by-milestone climb (77% → 79% → 81% → 83%) is **progression**; each individual bump
clearing its gate is one **improvement**; the discipline of finding survived mutants and writing
stronger assertions before the gate is manually raised is closer to **QA** (designing better tests
in advance) than QC (the gate itself is the QC/detection layer, catching regressions after the
fact).

## Synthesis

These eleven terms don't add a new shape to the loop — they answer "what's the loop for" and "what
does its output look like over time." Evaluative dimensions name the reference value the compare
stage measures against. Quality disciplines split "quality" into its preventive (QA), detective
(QC), and organizational (QM) faces, all instances of the same abstract loop applied to the
product/service-quality domain. Trajectory descriptors describe the pattern the loop produces
across many cycles, from a single refinement up to a validated, resource-aware sustainable trend.
None of it works without the base taxonomy's loop shape underneath it — this article is what you
plug into that shape once you're asking not "how does it correct itself" but "toward what, and
what does the resulting trend look like."

## Quick Reference

| Question | Answer |
|---|---|
| Are efficiency and effectiveness the same thing? | No — efficiency is output/input ratio ("doing things right"); effectiveness is goal attainment ("doing the right things"), regardless of cost |
| Is Quality Management the same as Quality Control? | No — QC is the sense+compare stages (inspecting output); QM is the full organizational loop instance containing QC, QA, and continuous improvement |
| Is sustainable growth just a ratchet? | Not quite — it's ratchet-like (a trend that shouldn't reverse) plus an added constraint the ratchet concept alone doesn't carry: awareness of what the growth is costing elsewhere in the system |
| Is excellence a single fitness function? | Not usually — it's typically a composite of several evaluative dimensions at once (efficiency + effectiveness + innovation + culture), closer to an aspirational watermark than one crisp metric |
| Is "reach the optimum" the universal goal behind all of this family's mechanisms? | No — a ratchet, regularization, guardrails, hysteresis, criteria, and Nash equilibria are all satisficing or stability mechanisms, not optimization ones. Optimization is one mode the compare stage runs in, not the destination everything else secretly aims at |
| Is satisficing just a weaker form of optimization? | Not quite — it's a different objective: clear a threshold given real costs to searching further, rather than exhaustively pursue the best value regardless of cost |

## References

- Drucker, P.F. (1963). "Managing for Business Effectiveness". *Harvard Business Review*. — origin of the efficiency ("doing things right") vs. effectiveness ("doing the right things") distinction.
- International Organization for Standardization. *ISO 9001:2015 — Quality management systems — Requirements*. — QMS structured around the PDCA cycle; source for the Quality Management mapping above.
- Baldrige Performance Excellence Program (NIST) and European Foundation for Quality Management (EFQM) Excellence Model — sources for the multi-dimensional "excellence" framing.
- Simon, H.A. (1956). "Rational choice and the structure of the environment". *Psychological Review*, 63(2), 129–138. — origin of satisficing and bounded rationality, the corrective to treating optimization as the universal goal above.
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) — the ratchet mechanism and fitness-function formalization this article's evaluative dimensions build on.
- [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md) — regularization as a deliberate departure from the training-set optimum.
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md) — guardrails and criteria as satisficing mechanisms.
- [Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md) — the Nash equilibrium example cited above.

## Related Articles

- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md) — the ML instantiation of this article's optimization definition
- [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md) — completes this article's optimization definition with the local-vs-global-minimum distinction
- [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md) — regularization
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)
- [Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md)

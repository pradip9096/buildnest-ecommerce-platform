---
title: "Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process"
category: documentation
tags: [feedback-loop, dynamical-systems, cyclical-process, control-theory]
keywords: [dynamic, dynamics, dynamical systems theory, Poincare, static, plant dynamics, cyclical process, periodic, adaptive, water cycle, circadian rhythm]
objective: Place dynamic, dynamical systems theory, and cyclical process beneath the base feedback-loop taxonomy's substrate layer — the most primitive terms in the whole family, existing prior to and independent of any feedback, correction, or control. This is the base article for this substrate depth; see the companion for extremum/optimum, extremum principles, and equilibrium.
audience: anyone who has read the base taxonomy and wants the substrate layer explored — dynamical-systems grounding for the "control flow / iteration" substrate row
scope: general (dynamical systems theory) with cross-references back to BuildNest's own worked CI and ML examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - feedback-loop-extrema-equilibria-and-physics-grounding.md
  - feedback-loop-domain-instance-machine-learning-training.md
  - feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md
  - quality-gate-ratchet-pattern.md
status: published
---

# Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process

## What Is It?

[Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
places control flow and iteration as the substrate a feedback loop runs on. This article goes one
level deeper: **dynamic** and **cyclical process** are more primitive still — concepts that exist
in mathematics and physics with no reference to feedback, correction, or control at all. Naming
them precisely resolves several confusions that surface once the taxonomy is applied outside pure
computing: whether a repeating process is automatically a feedback loop (it isn't), and where the
line falls between description and control. This is the base article for this substrate depth; see
[Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md)
for extremum vs. optimum, extremum principles, and equilibrium.

## Why It Matters

Every domain instance mapped elsewhere in this family — closed-loop control, continuous
improvement, machine-learning training, agentic reflection loops — is a *prescriptive* use of
these substrate concepts: deliberately steering a system toward a goal. But the concepts
themselves are older and more general than any of that, and conflating the descriptive fact with
the prescriptive use of it is a real, recurring error: assuming a system is "correcting toward a
target" just because it's dynamic or repeating, when in fact it may just be following an ordinary
physical law with no sensor, comparator, or actuator anywhere in the picture.

## How It Works

### Dynamic — more primitive than cyclical process

**Dynamic** describes a system whose state changes over time; **the dynamics** is the rule
governing *how* that state changes. The opposite is static. Dynamic is **more primitive than
cyclical process**, not a peer of it: cyclical process requires a specific pattern — the state
returning to something resembling its starting point. A system can be dynamic without that: a
ratchet's own tracked value only ever climbs, never returning to where it started, but it is
clearly dynamic; a system converging to a stable equilibrium and stopping is dynamic without being
cyclical; a chaotic, non-repeating system (weather, financial markets) is dynamic without being
cyclical either. Control flow, iteration, cyclical process, feedback loops, and ratchets are all
specific ways of *shaping or describing* dynamic behavior — "dynamic" itself just names that state
is changing at all, with no claim about the pattern.

This also names the field the whole feedback-loop family has been implicitly drawing a narrower
slice from. Everything mapped elsewhere in this family comes from **control theory** —
deliberately steering a system toward a target via feedback. The broader, purely descriptive field
is **dynamical systems theory** (Poincaré), which studies how a system's state evolves over time
with no assumption anyone is steering it anywhere. Control theory is what results from taking a
dynamical system and adding a feedback loop on top to shape its otherwise-uncontrolled dynamics
toward a goal — a rock rolling downhill has dynamics (position changing under gravity and
friction) but no control, because nothing is comparing its position to a target and correcting.

This also resolves a term used without definition in
[Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md)'s
adaptive-control mapping: "the plant's dynamics" changing (an aircraft's mass shifting as fuel
burns) is exactly this concept — the rule connecting control input to system behavior over time.
Adaptive control exists specifically because that rule can drift, and the controller needs an
internal model of it to keep hitting a fixed reference despite the drift.

Everything about extremum, optimum, extremum principles, and equilibrium — the natural next
question once "dynamic" is precise — is covered in
[Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md).

### Cyclical process vs. iteration — domain-general substrate

**Cyclical process** is a broader, domain-general term sitting at the same substrate layer as
**iteration**: any process whose sequence of stages repeats, returning it to a state resembling
where it started. That definition, on its own, requires no sensing, comparison, or correction —
it's necessary but not sufficient for a feedback loop, exactly the relationship iteration already
has in the base taxonomy. The water cycle, the day/night cycle, and the cell cycle are all
genuinely cyclical with no compare stage at all; PDCA, a reflection loop, and PIT's ratchet
schedule are cyclical *and* feedback loops, because each cycle includes a comparison and the next
cycle's behavior depends on its outcome. Iteration is the computing-specific register of the same
idea (a `for`/`while` loop, a control-flow construct); cyclical process is the same substrate
concept used just as naturally in physics, biology, or economics.

Within this, a further distinction is worth keeping separate: **periodic** cyclical processes
repeat on a fixed schedule regardless of outcome (the day/night cycle doesn't care what happened
yesterday); **adaptive** cyclical processes have a next cycle that depends on the previous cycle's
result. A feedback loop requires the adaptive kind — periodicity alone, with no dependence on
prior output, is recurrence, not correction.

## When to Use It

- A system repeats but nothing about it seems to be "correcting" anything → check whether it's
  periodic (the day/night cycle) rather than adaptive; periodicity alone isn't feedback.
- Deciding whether "iteration" or "cyclical process" is the right word for a non-computing context
  → cyclical process is the domain-general register; use it outside software/control-flow contexts
  (physics, biology, economics) where "iteration" would read as a category error.
- Wanting the extremum/equilibrium depth beneath "dynamic" →
  [Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md).

## Examples

BuildNest's PIT mutation gate (see [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md))
is dynamic (the mutation score changes over time) and cyclical *and* adaptive (each milestone's
target depends on the previous milestone's outcome, not a fixed schedule) — contrast the day/night
cycle, which is dynamic and cyclical but purely periodic, with no dependence on prior output at all.

## Synthesis

Dynamic and cyclical process are the floor beneath everything else in this family — more primitive
than control flow, more primitive than iteration, existing in physics, biology, and economics long
before anyone applied them to CI gates or neural networks. A system can be dynamic without being
cyclical, and cyclical without being adaptive (a feedback loop specifically requires the adaptive
kind). None of this diminishes the taxonomy built on top of it — it's what makes clear which parts
of that taxonomy are genuinely engineered control, and which parts are just ordinary recurrence.

## Quick Reference

| Question | Answer |
|---|---|
| Is every cyclical process a feedback loop? | No — a cyclical process only requires repetition returning to a similar starting state; it becomes a feedback loop only once it also has a comparison stage and the next cycle depends on that comparison's outcome |
| Is a periodic process (like the day/night cycle) a feedback loop? | No — periodic means fixed-schedule repetition regardless of outcome; a feedback loop requires the adaptive kind, where the next cycle depends on the previous cycle's result |
| Is "dynamic" the same as "cyclical process"? | No — dynamic just means state changes over time, with no claim about pattern; cyclical process requires the specific pattern of returning near its starting state. Dynamic is more primitive |
| Is dynamical systems theory the same as control theory? | No — dynamical systems theory descriptively studies how state evolves over time; control theory is the special case of deliberately adding a feedback loop to steer that evolution toward a target |

## References

- Poincaré, H. (1890s–1900s). Foundational work establishing dynamical systems theory — the descriptive study of how a system's state evolves over time, the broader field control theory (and the base taxonomy) draws a narrower slice from.
- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md) — the base article this substrate layer sits beneath.
- [Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md) — extremum, optimum, extremum principles, and equilibrium, split out to keep this article focused on "dynamic" and "cyclical process."

## Related Articles

- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md)
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
- [Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md)
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)

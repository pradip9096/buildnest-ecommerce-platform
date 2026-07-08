---
title: "Feedback Loop Domain Depth: ML Training's Optimization Landscape"
category: documentation
tags: [feedback-loop, machine-learning, local-minima, global-minima, optimization-landscape, minimization, maximization, dimension, curse-of-dimensionality]
keywords: [local minimum, global minimum, local maximum, global maximum, basin of attraction, stable equilibrium, critical point, saddle point, minimization, maximization, sign convention, fitness function direction, dimension, dimensionality, curse of dimensionality, Bellman]
objective: Cover the loss-landscape topology beneath gradient descent — local vs. global minima, the critical-point/saddle-point precision, why saddle points dominate in high dimension, and minimization/maximization as the same sign-flipped operation. This is the base article for this depth; see the companion for search strategy (exploration/exploitation) and generalization (bias-variance, regularization).
audience: anyone who has read the ML-training domain-instance mapping and wants the landscape-topology depth specifically — why gradient descent gets stuck and what "stuck" precisely means
scope: general (optimization theory, machine learning) with cross-references to BuildNest's own worked CI examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-domain-instance-machine-learning-training.md
  - feedback-loop-ml-search-strategy-and-generalization.md
  - feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md
  - feedback-loop-extrema-equilibria-and-physics-grounding.md
  - quality-gate-ratchet-pattern.md
status: published
---

# Feedback Loop Domain Depth: ML Training's Optimization Landscape

## What Is It?

[Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
maps forward propagation, backpropagation, the chain rule, and gradient descent onto the base
feedback-loop taxonomy. This companion goes one level deeper into the *shape* of what gradient
descent is searching: which points it can stop at, why, and what "local" vs. "global" precisely
means. This is the base article for this depth; see
[Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md)
for exploration/exploitation and bias-variance/regularization — the searcher's behavior and the
model's capacity, rather than the landscape's topology.

## Why It Matters

"Gradient descent minimizes the loss" sounds like a complete description until you ask *which*
minimum, guaranteed how, and why the stopping condition doesn't mean what it sounds like it means.
Each of those questions has a precise answer already scattered across other companions in this
family; this article collects the ML-specific instances of the topology-related ones in one place.

## How It Works

### Local vs. global minima — the precision "optimization" was missing

[Feedback Loop Extension: Evaluative Dimensions and Quality Disciplines](feedback-loop-evaluative-dimensions-and-quality-disciplines.md)
defined optimization as "pursuing a defined objective function's best value" without distinguishing
*local* best from *global* best. Gradient descent is where that ambiguity has real teeth: it only
ever has **local** information — the gradient at its current position — with no view of the rest of
the loss landscape. It will follow that local slope downhill until it reaches *any* point where the
gradient is zero, and stop there, with no guarantee that point is the **global minimum** (the single
lowest point anywhere in the landscape) rather than a **local minimum** (lower than everything
nearby, but not necessarily lower than a different valley elsewhere). Every global minimum is
trivially also a local one; the reverse doesn't hold.

**A more precise correction to "stops where the gradient is zero":** a point with zero gradient is
a **critical point**, and not every critical point is an **extremum** (the general term for either
a minimum or a maximum). It can be a **saddle point** — flat in one direction, curving up in
another, curving down in a third — satisfying gradient descent's stopping condition without being a
local minimum, maximum, or anything meaningful as an answer. In high-dimensional neural network
loss landscapes, saddle points are now understood to be the *more common* practical obstacle to
gradient descent, not true local minima (Dauphin et al., 2014). "Stops at a local minimum" is the
popular simplification; "stops at a critical point, which is often a saddle" is the accurate one.

**Why saddle points dominate in high dimension, not just that they do.** Each unknown variable
(weight) being solved for is one **dimension** of the search space — a network with N weights
searches an N-dimensional loss landscape, one axis per weight. A saddle point — "flat in one
direction, curving up in another, curving down in a third" — is only *possible* with more than one
dimension: in 1D, a critical point can only be a minimum or a maximum, since there's no second
direction available to curve the opposite way. As dimension count grows, the odds that *every*
dimension happens to curve the same way at a given critical point (making it a true extremum)
shrink fast, while the odds that *at least one* dimension curves the other way (making it a saddle)
grow. That's the mechanism behind Dauphin et al.'s finding above, not just an empirical curiosity —
it's a direct consequence of how many independent directions a high-dimensional landscape has to
get right simultaneously. This is one face of the **curse of dimensionality** (Bellman, 1957): the
general phenomenon that search, optimization, and distance-based reasoning all get exponentially
harder as dimension count grows — the same underlying reason exploration
([Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md))
matters more in high dimensions, since there's vastly more space for a purely exploitative search
to miss.

This connects to distinctions already mapped elsewhere in this family, not just to gradient descent
in isolation:

- **Dynamical systems** ([Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md)):
  a local minimum is exactly what that field calls a **stable equilibrium** or **attractor** — a
  state gradient-descent-like dynamics settle into and remain at. The set of starting points that
  converge to a particular one is its **basin of attraction**. Gradient descent has no way to know
  which basin it started in.
- **The ratchet** ([Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)): locking in a
  value once achieved is structurally the same trap as converging to a local minimum — an
  irreversible commitment to "the best reachable from here without backtracking," with no guarantee
  it's the best achievable anywhere. A ratchet locked in too early is a local optimum wearing a
  mechanical floor.
- **First-order vs. second-order change** ([Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md)):
  escaping a local minimum needs a different *kind* of move, not a bigger or smaller version of the
  same one. Adjusting `learning_rate` alone won't reliably escape a deep local minimum, because the
  gradient still points the same direction regardless of step size — actual escape requires
  momentum, the inherent noise in stochastic mini-batch gradients, simulated annealing, or a random
  restart from a different starting point. See
  [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md)
  for the framework unifying those four escape mechanisms.

### Minimization and maximization are the same operation, sign-flipped

"Optimum" is the umbrella term for either a maximum or a minimum — the two aren't different
operations. **Maximizing f(x) is mathematically identical to minimizing −f(x).** Flip the sign of
the function and the two problems become the same problem: find the extremum. Minimization vs.
maximization is a per-domain sign convention on which direction gets called "better," not a
structural difference in the compare stage.

This is the precise vocabulary for something scattered across every companion in this family
without ever being named as one axis. Every compare-stage example used throughout this KB has
silently picked a direction, and they don't agree:

| Term (already used in this family) | Direction |
|---|---|
| Fitness function (biology/genetic algorithms — the term this whole family borrows "fitness function" from) | **Maximized** — higher fitness is better |
| Mutation score / PIT gate ([Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)) | **Maximized** — the ratchet only ever moves up |
| Efficiency, effectiveness, excellence ([Feedback Loop Extension: Evaluative Dimensions and Quality Disciplines](feedback-loop-evaluative-dimensions-and-quality-disciplines.md)) | **Maximized** |
| Loss function / cost function | **Minimized** — gradient descent walks *downhill* |
| Drift / over-correction rate ([Feedback Loop Domain Instance: Agentic Reflection Loops](feedback-loop-domain-instance-agentic-reflection-loops.md)) | **Minimized** |

That's not a coincidence of vocabulary — it's the same compare-stage mechanism every time, with the
sign chosen per-domain: biology and ratchets talk in terms of a quantity you want *more* of; ML and
error-tracking talk in terms of a quantity you want *less* of. A "loss" is a negated "fitness"
wearing different words; a ratchet that only moves up and a loss curve that only moves down are the
same shape of commitment, pointed in opposite directions because their domains chose opposite
conventions for what "better" looks like on the page.

**"Local optimum" and "global optimum" fall out of this directly, not as separate concepts.**
Whether a given optimum *is* a minimum or a maximum is entirely determined by which direction the
problem points. In a minimization problem, every local minimum is a local optimum and vice versa;
in a maximization problem, the same holds with local maximum in place of local minimum. "Local
optimum" is not a third kind of point, just the goal-relative name for whichever of {local minimum,
local maximum} matches the problem's direction. (A local minimum is sometimes glossed as "the
worst nearby outcome" in a maximization context — that's an informal intuition, not a formal
claim: it says nothing about points elsewhere in the landscape that aren't extrema at all, some of
which can be worse than a shallow local minimum.)

## When to Use It

- Debugging why a training run has plateaued → check whether the stopping point is actually a
  local minimum or a saddle point before concluding the model has converged as well as it can.
- Comparing two metrics that seem to move in opposite directions during training → check whether
  one is being maximized and the other minimized before assuming they conflict.
- Wanting the search-strategy or generalization depth (exploration/exploitation, bias-variance,
  regularization) → see
  [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md).

## Examples

BuildNest's PIT mutation gate (see [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md))
uses **mutation score** as its evaluative dimension, maximized, exactly the direction genetic
algorithms and fitness functions use — while a neural network's loss function, minimized, is the
same compare-stage mechanism wearing the opposite sign convention. Both are also vulnerable to the
same local-optimum trap: `mutationThreshold` locked in at 77% is structurally identical to gradient
descent settling into a shallow local minimum — the best reachable from the current state, with no
guarantee it's the best achievable anywhere.

## Synthesis

Gradient descent's stopping point is governed by more than the update rule itself: whether it's a
genuine minimum or a saddle, and which sign convention the loss function happens to use. None of
this changes the loop's shape — it's all detail inside the act stage and the fitness function
already named in the core ML-training mapping. Recognizing the pattern makes it possible to import
distinctions developed for other domains — equilibria, ratchets, first-order vs. second-order
change — directly into an ML context without rediscovering them from scratch.

## Quick Reference

| Question | Answer |
|---|---|
| Does gradient descent find the global minimum? | Not guaranteed — it only has local gradient information and stops at whichever local minimum it's nearest to, the same structural trap as a ratchet locking in a value too early |
| Does zero gradient guarantee gradient descent has reached a local minimum? | No — zero gradient means a critical point, which can be a saddle point instead. In high-dimensional networks, saddle points are now considered the more common practical obstacle, not true local minima |
| Why do saddle points get more common as dimension grows? | A saddle point needs at least one dimension to curve the opposite way from the rest — impossible in 1D. As dimension count grows, the odds that every dimension curves the same way (a true extremum) shrink fast, while the odds at least one doesn't (a saddle) grow |
| Is "dimension" the same idea as "unknown variable"? | Closely related — each unknown variable being solved for is one dimension of the search space. A network with N weights searches an N-dimensional landscape, one axis per weight |
| Are minimization and maximization structurally different operations? | No — maximizing f(x) is mathematically identical to minimizing −f(x). The direction is a per-domain sign convention, not a difference in the compare stage itself |
| Is "local optimum" a third kind of point, distinct from local minimum and local maximum? | No — it's the goal-relative name for whichever one matches the problem's direction |

## References

- Dauphin, Y.N. et al. (2014). "Identifying and attacking the saddle point problem in high-dimensional non-convex optimization". *NeurIPS 2014*. — establishes that saddle points, not local minima, are the dominant obstacle to gradient descent in high dimensions.
- Bellman, R. (1957). *Dynamic Programming*. Princeton University Press. — origin of "the curse of dimensionality," the general phenomenon behind why saddle points proliferate.
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md) — the core loop mapping this article's landscape-topology depth builds on.
- [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md) — exploration/exploitation and bias-variance/regularization, split out to keep this article focused on landscape topology.
- [Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md) — extremum and equilibrium vocabulary this article applies to gradient descent.
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) — the ratchet mechanism's structural parallel to a local-minimum trap.

## Related Articles

- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
- [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md)
- [Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md)
- [Feedback Loop Extrema Equilibria and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)

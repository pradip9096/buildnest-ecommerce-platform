---
title: "Feedback Loop Domain Instance: Machine Learning Training"
category: documentation
tags: [feedback-loop, backpropagation, gradient-descent, machine-learning, chain-rule, self-improving-loop]
keywords: [backpropagation, gradient descent, chain rule, forward propagation, loss function, learning rate, neural network training, meta-learning, self-play, adaptive control, first-order change, second-order change]
objective: Place forward propagation, backpropagation, the chain rule, and gradient descent onto the feedback-loop taxonomy as a fifth domain instance — the neural-network training loop — alongside engineering, organizational, quality, and personal-development instances already mapped. This is the base article for this domain; see the companion for the optimization-landscape depth (local minima, exploration/exploitation, bias-variance, regularization).
audience: anyone who has read the base taxonomy and wants the machine-learning-training mapping specifically
scope: general (machine learning, calculus) with a cross-reference back to BuildNest's own worked CI examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - feedback-loop-ml-training-optimization-landscape.md
  - feedback-loop-ml-search-strategy-and-generalization.md
  - feedback-loop-enforcement-and-safety-vocabulary.md
  - feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md
status: published
---

# Feedback Loop Domain Instance: Machine Learning Training

## What Is It?

[Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
maps the base shape of a cybernetic loop and names four domain instances of it: closed-loop control
(engineering), continuous improvement/PDCA (organizational), quality management (product/service
quality), and personal development. This article adds a fifth: **forward propagation,
backpropagation, the chain rule, and gradient descent** together describe one coherent unit — the
neural-network training loop. This is the base article for that domain; see
[Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
for the deeper material on local minima, exploration/exploitation, bias-variance, and
regularization.

## Why It Matters

Machine learning terminology is often taught as a self-contained pipeline (forward pass, loss,
backward pass, update) without connecting it to the general feedback-loop vocabulary used
everywhere else in engineering. Once the connection is made explicit, a lot of ML-specific
confusion resolves using vocabulary already established elsewhere in this KB: whether "the model is
improving itself" is really a self-improving loop or just ordinary closed-loop control at the
parameter level, and whether a given change to a training run is a first-order or second-order
change in Watzlawick's sense.

## How It Works

| Term | Role | Maps to |
|---|---|---|
| **Forward propagation** | Computes the model's output (prediction) from current parameters | The "plant" producing its output — what will subsequently be measured, not the sensing itself |
| **Loss function** (implied backdrop) | Compares the prediction to ground truth, producing a scalar error | The **compare** stage — a concrete fitness function, same slot as efficiency/effectiveness/quality attributes in [Feedback Loop Extension: Evaluative Dimensions and Quality Disciplines](feedback-loop-evaluative-dimensions-and-quality-disciplines.md) |
| **Backpropagation** | Translates that scalar error into a per-parameter correction signal, layer by layer, in reverse | Sits *between* compare and act — the "controller logic" decomposing one aggregate error into many individual actuator commands |
| **Chain rule** | The composite-function differentiation rule making backpropagation mathematically possible | **Substrate** — same layer as control flow and iteration in the base taxonomy, just domain-specific to calculus. Necessary but not sufficient alone, the same way iteration alone doesn't produce feedback |
| **Gradient descent** | The concrete update rule: `parameter -= learning_rate × gradient` | The **act** stage. `learning_rate` is a **parameter** in exactly the sense mapped in [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md); gradient descent is also literally the most common concrete algorithm for **optimization** |

Repeat (the next batch/epoch) is ordinary iteration from the base taxonomy — no new placement
needed.

### Two boundary clarifications

- **Ordinary SGD is *not* a self-improving loop.** The loss function and target labels stay fixed
  throughout training — it's standard closed-loop control at the *parameter* level. It only becomes
  a genuine self-improving loop (meta-loop, defined in
  [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md))
  where the reference itself evolves — adaptive learning-rate schedules, curriculum learning, or
  self-play systems (AlphaGo Zero), where each generation's target is generated by the system's own
  prior version.
- **Gradient descent's updates are first-order change** (Watzlawick, per
  [Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md)) —
  tuning parameters within a fixed network architecture. Changing the architecture itself (adding
  layers, neural architecture search) is second-order change.

Watching loss/accuracy curves during training (TensorBoard-style dashboards) is a direct instance
of **live monitoring**, the continuous sense stage from the base taxonomy.

## When to Use It

- Explaining ML training to someone already familiar with CI/CD quality gates or control systems —
  the vocabulary maps directly (loss function = fitness function, gradient descent = the act stage,
  learning rate = a parameter) rather than needing to be learned as an unrelated pipeline.
- Deciding whether a proposed change to a training setup is "just tuning" or a bigger redesign — the
  first-order/second-order distinction gives a concrete criterion: does it change the loss function
  or target (a redesign of what's being optimized) or only the update magnitude/schedule (tuning
  within the existing setup)?
- Asked whether a system "learns to learn" — check specifically whether the *reference* changes
  across cycles (self-improving loop) or only the *parameters* do (ordinary closed-loop control).
- Wanting the optimization-landscape depth (why training gets stuck, how to escape, generalization)
  → see [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md).

## Examples

BuildNest's PIT mutation gate (see
[Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)) and neural-network training share
the same abstract shape despite looking unrelated: both have a fitness function (mutation score /
loss), both correct toward that reference via an iterative update, and both can be described as
first-order change (tuning `mutationThreshold` / tuning `learning_rate`) versus second-order change
(swapping the quality mechanism entirely / redesigning the network architecture). The difference is
domain, not structure.

## Synthesis

Forward propagation, backpropagation, the chain rule, and gradient descent aren't a separate,
ML-specific vocabulary — they're the machine-learning instantiation of the same sense-compare-act-
repeat loop mapped everywhere else in this KB, with the chain rule playing the substrate role
iteration and control flow play elsewhere, and gradient descent playing the act-stage role a
CI pipeline's build step plays for BuildNest's own quality gates. Recognizing the pattern makes it
possible to import distinctions developed for other domains — first-order vs. second-order change,
self-improving vs. ordinary closed-loop control — directly into an ML context without having to
rediscover them from scratch.

## Quick Reference

| Question | Answer |
|---|---|
| Is forward propagation the sense stage? | No — it produces the output that then gets sensed/compared against ground truth via the loss function |
| Is backpropagation the same as gradient descent? | No — backpropagation computes the per-parameter gradient (the correction signal); gradient descent is the rule that applies it |
| Is the chain rule a loop stage? | No — it's substrate, the mathematical law that makes backpropagation computable, same layer as control flow and iteration |
| Is ordinary neural-network training a self-improving loop? | No — the loss function/target is fixed; it's ordinary closed-loop control at the parameter level. Self-improving loops appear in meta-learning, adaptive schedules, and self-play specifically |

## References

- Rumelhart, D.E., Hinton, G.E. & Williams, R.J. (1986). "Learning representations by back-propagating errors". *Nature*, 323(6088), 533–536. — the paper that popularized backpropagation for training multi-layer neural networks.
- Cauchy, A.L. (1847). "Méthode générale pour la résolution des systèmes d'équations simultanées". *Comptes Rendus de l'Académie des Sciences*. — earliest known description of gradient descent as a general method.
- Silver, D. et al. (2017). "Mastering the game of Go without human knowledge". *Nature*, 550(7676), 354–359. — AlphaGo Zero, the self-play example cited above where the training target evolves rather than staying fixed.
- [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md) — local minima, saddle points, and dimension, split out to keep this article focused on the core loop mapping.
- [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md) — exploration/exploitation, bias-variance, and regularization.
- [Feedback Loop Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md) — the self-improving loop and parameter definitions this article builds on.
- [Feedback Loop Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md) — the first-order/second-order change distinction this article applies to gradient descent.
- [Feedback Loop Extension: Evaluative Dimensions and Quality Disciplines](feedback-loop-evaluative-dimensions-and-quality-disciplines.md) — the "optimization" and fitness-function vocabulary this article's loss function maps onto.
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) — the ratchet mechanism this article's Examples section contrasts against gradient descent.

## Related Articles

- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
- [Feedback Loop Domain Depth: ML Search Strategy and Generalization](feedback-loop-ml-search-strategy-and-generalization.md)
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)
- [Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md)
- [Feedback Loop Extension: Evaluative Dimensions and Quality Disciplines](feedback-loop-evaluative-dimensions-and-quality-disciplines.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)
- [Feedback Loop Domain Instance: Agentic Reflection Loops](feedback-loop-domain-instance-agentic-reflection-loops.md) — contrasts this article's parametric correction against reflection loops' contextual correction

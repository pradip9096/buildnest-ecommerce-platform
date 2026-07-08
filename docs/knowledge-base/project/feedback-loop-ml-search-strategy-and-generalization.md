---
title: "Feedback Loop Domain Depth: ML Search Strategy and Generalization"
category: documentation
tags: [feedback-loop, machine-learning, exploration-exploitation, bias-variance, regularization]
keywords: [exploration, exploitation, multi-armed bandit, Sutton Barto, reinforcement learning, bias-variance trade-off, underfitting, overfitting, regularization, L1, L2, weight decay, Tikhonov, momentum, simulated annealing, stochastic gradient descent]
objective: Cover how gradient descent searches (exploration vs. exploitation) and whether the model being fit is even the right shape for the problem (bias-variance, regularization) — split out from the optimization-landscape companion to keep each article focused on one question.
audience: anyone who has read the ML-training optimization-landscape companion and wants the search-strategy and generalization depth specifically — why escape mechanisms work, and why a model that fits training data well can still fail
scope: general (reinforcement learning, statistical learning theory) with cross-references to BuildNest's own worked examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-ml-training-optimization-landscape.md
  - feedback-loop-domain-instance-machine-learning-training.md
  - feedback-loop-control-engineering-pid-hysteresis-and-delay.md
  - feedback-loop-enforcement-and-safety-vocabulary.md
status: published
---

# Feedback Loop Domain Depth: ML Search Strategy and Generalization

## What Is It?

[Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
covers the shape of the loss landscape — local vs. global minima, saddle points, dimension. This
companion covers two different questions about the search itself: **how** gradient descent looks
for a better point (exploration vs. exploitation), and whether **what's being fit** — the model's
own capacity — is even the right shape for the problem (bias-variance, regularization). Neither is
about the landscape's topology; both are about the searcher and the thing being searched for.

## Why It Matters

A training run can get the landscape topology right — genuinely reach a good minimum — and still
fail for either of these reasons: a search that never explores can settle for a mediocre basin of
attraction close to where it started, and a model with mismatched capacity can fit its training
data perfectly while learning nothing that generalizes. Neither failure is visible from the
topology alone.

## How It Works

### Exploration vs. exploitation — the framework four escape mechanisms already instantiate

[Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
lists momentum, mini-batch noise, simulated annealing, and random restarts as four separate ways to
escape a local minimum. They're not separate — they're four different implementations of one named
trade-off from reinforcement learning and multi-armed bandit theory (Sutton & Barto, 2018):
**exploitation** (follow the known-good gradient — greedy, first-order, maximizes immediate
progress toward the nearest optimum) vs. **exploration** (deliberately try something uncertain —
a different region of the landscape that might contain a better optimum than the one currently
being descended toward). Pure exploitation is exactly what plain gradient descent does, which is
why it's vulnerable to the local-minimum trap: it never tries anything except the locally
best-looking move. Momentum, stochastic noise, annealing's temperature parameter, and random
restarts are four different mechanisms for injecting some exploration into an otherwise purely
exploitative search — each trades a little short-term progress for a chance at finding a better
basin of attraction than the one closest to the starting point.

### Bias-variance trade-off — capacity, not search strategy

Exploration vs. exploitation, above, is about *how the act stage searches* for a better point.
The **bias-variance trade-off** is a different axis entirely: whether the thing being fit — the
model's own capacity, how flexible it's allowed to be — is well-matched to the problem at all,
independent of how well the search itself is conducted.

- **High bias (underfitting):** the model is too rigid to represent the true relationship, so it
  settles into a *persistent* error no amount of further training closes. This is the ML-domain
  instance of exactly the gap [Feedback Loop Domain Depth: PID Control, Hysteresis, and Feedback Delay](feedback-loop-control-engineering-pid-hysteresis-and-delay.md)
  describes for a pure-P controller with no integral term: correct-but-insufficient capacity to
  ever fully close the error.
- **High variance (overfitting):** the model is flexible enough to fit the *noise* in the training
  data, not just the true underlying signal — it looks excellent on training data and generalizes
  poorly. This is the same "reacting to noise instead of signal" failure the same companion's
  statistical-process-control section names formally in a manufacturing context: common-cause
  variation (noise) shouldn't drive a correction, but an overfit model treats every fluctuation in
  the training set as if it were meaningful.

Neither exploration/exploitation nor bias-variance is about the loop's *shape* — both are about
whether the act stage (search strategy in one case, model capacity in the other) is well-suited to
the actual problem, a question the loop shape alone can't answer.

### Regularization — a fourth, softer boundary-setting mechanism

[Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)
names three ways of constraining the act stage's output space: **guardrails** (a hard constraint,
preventive), a **ratchet** (a permanent one-way lock), and **hysteresis** (asymmetric but
reversible). **Regularization** is a fourth, softer mechanism, and the practical tool for managing
the bias-variance trade-off above: instead of a hard boundary, it adds a *penalty term* directly
into the loss function itself, biasing the search away from excessive model complexity without
forbidding it outright (L2/"weight decay," penalizing large weights; L1, penalizing nonzero
weights and encouraging sparsity). The model is still technically free to use its full capacity —
regularization just makes doing so cost something in the same currency the compare stage already
optimizes, rather than blocking it externally the way a guardrail would.

## When to Use It

- A training run has plateaued and the landscape topology looks fine (not a saddle point) → check
  whether the search strategy has any exploration at all before assuming this is the best reachable
  result.
- A model performs well in training but poorly in production → check bias-variance before assuming
  it's a data problem; an overfit model needs regularization or reduced capacity, not more epochs.
- Deciding how to constrain a model's complexity → regularization (soft, cost-based) is often
  preferable to a hard capacity cap (a guardrail) when some flexibility is still wanted, just priced.

## Examples

BuildNest doesn't train ML models directly, but the same shape appears in its own CI: adding more
and more special-case exceptions to a linter rule to make it pass on every current file is the
software-engineering analog of overfitting — the rule now perfectly "fits" today's codebase but
has lost the general property it was meant to enforce. A stricter, simpler rule with fewer
exceptions is the regularization equivalent: it costs some short-term convenience to preserve
long-term generality.

## Synthesis

Exploration/exploitation and bias-variance answer two different questions neither the landscape's
topology nor the loop's shape can answer on their own: is the search actually looking anywhere new,
and is the thing being fit even capable of representing the right answer. Regularization is the
practical lever for the second question, applied the same way a fitness function's own criteria get
applied everywhere else in this family — as a cost baked into the compare stage, not a wall bolted
onto the act stage.

## Quick Reference

| Question | Answer |
|---|---|
| Are momentum, mini-batch noise, annealing, and random restarts four unrelated tricks? | No — all four are ways of injecting exploration into an otherwise purely exploitative (greedy, first-order) search; the exploration/exploitation trade-off is the single framework behind all of them |
| Is bias-variance the same axis as exploration/exploitation? | No — exploration/exploitation is about search strategy; bias-variance is about whether the model's own capacity is well-matched to the problem, independent of how the search is conducted |
| Is regularization the same as a guardrail? | No — a guardrail is a hard constraint on the output space; regularization is a soft penalty added into the loss function itself, making excess complexity costly rather than forbidden |

## References

- Sutton, R.S. & Barto, A.G. (2018). *Reinforcement Learning: An Introduction* (2nd ed.). MIT Press. — the standard reference for the exploration/exploitation trade-off.
- Geman, S., Bienenstock, E. & Doursat, R. (1992). "Neural networks and the bias/variance dilemma". *Neural Computation*, 4(1), 1–58. — origin of the bias-variance trade-off framing.
- Tikhonov, A.N. (1943). "On the stability of inverse problems". *Doklady Akademii Nauk SSSR*, 39(5), 195–198. — foundational reference for regularization (Tikhonov/L2 regularization).
- [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md) — the local-minima/saddle-point/dimension material this article's escape mechanisms respond to.
- [Feedback Loop Domain Depth: PID Control, Hysteresis, and Feedback Delay](feedback-loop-control-engineering-pid-hysteresis-and-delay.md) — the missing-integral-term and statistical-process-control parallels the bias-variance section draws on.
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md) — guardrails, ratchet, and hysteresis, the three boundary-setting mechanisms regularization is contrasted against.

## Related Articles

- [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
- [Feedback Loop Domain Depth: PID Control, Hysteresis, and Feedback Delay](feedback-loop-control-engineering-pid-hysteresis-and-delay.md)
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)

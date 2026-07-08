---
title: "Feedback Loop Extension: Enforcement and Safety Vocabulary"
category: documentation
tags: [feedback-loop, guardrails, quality-gates, checkpoints, fallback, safety-net, enforcement-mechanism, mechanical-floor, self-improving-loop, prerequisite, unknown-variable, decision-variable]
keywords: [guardrails, quality gate, checkpoint, prerequisite, precondition, fallback, safety net, filter, parameter, criteria, negative quality detection, tightening mechanism, one-way tightening, enforcement mechanism, mechanical floor, self-improving loop, adaptive control, meta-loop, circuit breaker, unknown variable, decision variable, fixed parameter, root-finding, equation-solving, Newton's method]
objective: Place self-improving loop, guardrails, quality gates, checkpoints, fallback, safety net, filter, parameter, criteria, negative quality detection pattern, tightening mechanism, one-way-tightening philosophy, enforcement mechanism, mechanical floor, prerequisite, and the unknown-variable/fixed-parameter distinction onto the feedback-loop taxonomy — as timing-axis gates, boundary-setting mechanisms, failure-handling mechanisms, the enforcement layer itself, a meta-loop, or the precise mathematical object every act stage is ultimately solving for, not as new loop shapes. This is the base article for this extension; see the companion for the funnel and known-unknown/unknown-known material.
audience: anyone who has read the base taxonomy and wants the "how does a criterion get teeth" extension — CI/CD gate design, resilience engineering, process enforcement
scope: general (control theory, resilience engineering, process design, optimization) with BuildNest-specific worked examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - feedback-loop-enforcement-extensions-funnels-and-epistemics.md
  - quality-gate-ratchet-pattern.md
  - closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md
  - feedback-loop-domain-instance-machine-learning-training.md
  - feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md
status: published
---

# Feedback Loop Extension: Enforcement and Safety Vocabulary

## What Is It?

[Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
maps the base shape of a cybernetic loop. This article extends that map with fourteen further
terms — **self-improving loop, guardrails, quality gates, checkpoints, fallback, safety net,
filter, parameter, criteria, negative quality detection pattern, tightening mechanism,
one-way-tightening philosophy, enforcement mechanism, mechanical floor, prerequisite** — that
aren't about loop *shape* or *what "better" means*. They're about **how a criterion gets teeth**:
the concrete mechanisms and philosophies that turn "we'd like this to be true" into "this cannot be
bypassed." This is the base article for this extension; see
[Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness](feedback-loop-enforcement-extensions-funnels-and-epistemics.md)
for the funnel structure and the known-unknown/unknown-known distinction.

## Why It Matters

A criterion without an enforcement mechanism is a suggestion. This repo's own review of
`~/.claude/rules/definition-of-done.md` found exactly this gap: rigor described in prose with no
mechanical backstop is a norm, not a floor — nothing stops a future edit from quietly weakening it.
Naming these terms precisely makes it possible to ask the right diagnostic question about any given
constraint: is it enforced before or after the thing it's gating? Does it prevent a bad *state*, or
handle *failure of the sensing mechanism itself*? And critically — is there an actual mechanism
behind it, or just a documented intention?

## How It Works

### Timing axis — before vs. after the act stage

The cleanest structural distinction this batch introduces:

| Term | When it fires | Example |
|---|---|---|
| **Prerequisite** | *Before* a stage is allowed to run at all — a precondition check, skip-if-not-applicable | Liquibase's `<preConditions>` — a changeset doesn't run if the target table doesn't exist yet |
| **Checkpoint** | A discrete point *within* an iterative sequence where state is captured, often doubling as a rollback/fallback anchor | A CI pipeline stage boundary; a save point |
| **Quality gate** | *After* the act stage produces output, at a boundary before progressing further — already formally defined in [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) as "a threshold that fails the build when violated" | `jacoco-check` / `mutationThreshold` failing `mvn verify` |

The useful axis: **prerequisite gates entry, quality gate/checkpoint gates exit** — same underlying
idea (a boolean pass/fail check), different position in the sequence.

### Boundary-setting / preventive mechanisms

Define the acceptable space rather than correct toward an optimum:

| Term | What it is | Maps to |
|---|---|---|
| **Guardrails** | Hard constraints on the *act* stage's output space, preventive rather than corrective — forbid catastrophic states rather than optimize toward a target | Closer to QA (designed-in, a priori) than to a ratchet (earned, a posteriori) |
| **Filter** | The compare stage operating in classification mode (pass/reject on individual items) rather than measurement mode (a continuous scalar error) | Input validation, a WAF rule, spam filtering |
| **Parameter** | The tunable setting that *defines* the compare stage's reference or the act stage's response magnitude | `mutationThreshold`, a rate limit's `requests`/`duration` |
| **Criteria** | The explicit, human-readable rule that defines what counts as pass/fail at the compare stage | This repo's own DoD "acceptance criteria" |

**"Parameter" above is quietly conflating two different roles, and the precise term separating them
is the unknown variable.** A **fixed parameter** is set externally, by a human, before the loop
runs, and never touched by the loop's own act stage — `mutationThreshold`, a rate limit's
`requests`/`duration`. An **unknown variable** (or **decision variable**) is a quantity the loop's
own act stage is actively solving for, cycle after cycle — a neural network's weights in
`parameter -= learning_rate × gradient`
([Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md))
are exactly this: `learning_rate` is a fixed parameter, but the weights it updates are unknowns
gradient descent exists specifically to determine. Both get called "parameter" in casual usage;
worth being precise, since one is external configuration and the other is exactly what the
compare-and-act cycle exists to determine.

This also completes the formal shape of optimization, stated precisely: "find the unknown variable
`x` that extremizes `f(x)`" — `x` is the unknown, `f` is the fitness/loss function
([Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)). **Root-finding / equation-solving
is optimization's sibling**, using a different criterion type: "find the unknown `x` such that
`f(x) = target`" — an equality criterion instead of an extremal-ordering one. Newton's method is the
same loop shape as gradient descent — sense `f(x)`, compare to the target, adjust `x`, repeat — just
solving an equality constraint instead of extremizing; see
[Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md)
(see its "Dynamic" section, or its companion
[Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding](feedback-loop-extrema-equilibria-and-physics-grounding.md)
for "Extremum vs. optimum" specifically) for the extremum/optimum vocabulary this borrows.

The funnel structure — a chain of filters/quality-gates that introduces population-throughput and
open-loop-by-default axes — is covered in
[Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness](feedback-loop-enforcement-extensions-funnels-and-epistemics.md).

### Failure-handling mechanisms

Distinct from guardrails (which prevent bad *states*): these handle failure of the
sensing/comparing mechanism *itself*:

| Term | What it is | Example |
|---|---|---|
| **Fallback** | A predefined, usually component-local degraded-mode act stage that activates when sense/compare can't run normally | Redis circuit breaker OPEN → fall through to database, don't throw ([`resilience4j.md`](../../../.claude/rules/spring/resilience4j.md)) |
| **Safety net** | The systemic/global version of fallback — catches failures broadly rather than one component at a time | `ErrorBoundary.tsx` catching any unhandled render exception, keeping the rest of the app alive |

### The enforcement layer itself

What gives a criterion teeth — the exact distinction surfaced during the `definition-of-done.md`
review:

| Term | What it is |
|---|---|
| **Enforcement mechanism** | The general term for whatever actually blocks a violation — a build failure, a blocked merge, a rejected request. Turns a criterion from a suggestion into a constraint |
| **Mechanical floor** | The *outcome* produced when a ratchet's direction and an enforcement mechanism combine — a concrete, unavoidable lower bound. Precisely what `definition-of-done.md` was found to lack: rigor without a mechanism behind it is a norm, not a floor |

The known-unknown/unknown-known distinction — whether that enforcement mechanism's own signal is
observable to whoever needs to act on it — is covered in
[Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness](feedback-loop-enforcement-extensions-funnels-and-epistemics.md).

### The ratchet, restated from two other angles

| Term | Relationship to ratchet |
|---|---|
| **Tightening mechanism** | Same concept as ratchet, named for the *procedure* (incrementing the parameter) rather than the resulting constraint |
| **One-way-tightening philosophy** | The weaker, norm-based version used in `definition-of-done.md`'s own Amendment Log rule: amendments may tighten freely, loosening requires a logged reason. Not a true ratchet (no mechanical floor), but borrows its directional intent |

### A detection strategy, not a loop stage

**Negative quality detection pattern** describes *how* the compare stage's criteria are framed:
testing for the *absence* of known-bad patterns (grep for secrets, a linter's forbidden-pattern
rule) rather than the *presence* of a desired property (a passing test asserting correct behavior).
This repo does both: `security.md`'s secret-scan is negative detection; a passing unit test is
positive detection. It's an authoring choice within the compare stage, not a new stage.

### A genuinely new structural layer: Self-improving loop

This one doesn't fit the base taxonomy's seven concepts cleanly. **Continuous improvement**
corrects output toward a reference that's still externally, manually set — a human bumps
`mutationThreshold` from 77 to 79 at each milestone. A **self-improving loop** goes one level of
recursion higher: the loop's own reference or policy is itself updated automatically, as an output
of a prior cycle — not by a human editing `pom.xml`. Example: an autoscaler that adjusts its own
scaling thresholds based on observed load trends, or a self-play training system whose next target
is generated by its own prior version. This is a *meta-loop* — a loop that tunes its own tuning —
best treated as an eighth structural node alongside the base taxonomy's seven, not folded into
continuous improvement.

## When to Use It

- A constraint exists only in prose ("we should always X") → ask what the enforcement mechanism is.
  If there isn't one, it's a philosophy, not a floor — see the `definition-of-done.md` case study in
  [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md).
- Something is failing intermittently and it's unclear whether that's a bug or a designed fallback →
  check whether a circuit breaker or similar failure-handling mechanism is deliberately degrading
  behavior rather than something being broken.
- A new check is being added to a pipeline → decide explicitly whether it's a prerequisite (gate
  entry) or a quality gate (gate exit) — conflating the two produces confusing failure modes (a
  check that silently skips when you expected it to block, or vice versa).
- Wanting the funnel-structure or epistemic-awareness depth → see
  [Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness](feedback-loop-enforcement-extensions-funnels-and-epistemics.md).

## Examples

This repo's rate-limiting and resilience configuration is a working instance of several of these at
once: `RateLimitUtil` builds **parameters** (`requests`, `duration`) per endpoint category;
`RateLimiterService.isAllowed()` acts as a **filter** (pass/reject per request); Redis being
unavailable triggers a **fallback** (allow the request through rather than deny, per
[`resilience4j.md`](../../../.claude/rules/spring/resilience4j.md)'s graceful-degradation rule);
and `ErrorBoundary.tsx` on the frontend is the **safety net** catching anything that still gets
through unhandled.

## Synthesis

These fourteen terms all answer variations of one question: what stands between a stated rule and
an actual, unavoidable consequence? Timing (prerequisite vs. quality gate) tells you *where* in the
sequence a check fires. Boundary-setting mechanisms (guardrails, filters, parameters, criteria)
define the acceptable space. Failure-handling mechanisms (fallback, safety net) cover what happens
when the loop itself can't run normally. And the enforcement layer — the distinction between a
mechanical floor and a one-way-tightening philosophy — is the one that matters most: it's the
difference between a rule that cannot be violated and a rule that is merely not supposed to be.

## Quick Reference

| Question | Answer |
|---|---|
| Is a prerequisite the same as a quality gate? | No — prerequisite gates *entry* to a stage (before it runs); quality gate gates *exit* (after it runs, before progressing further) |
| Is a fallback the same as a guardrail? | No — a guardrail prevents a bad *state*; a fallback handles failure of the *sensing/comparing mechanism itself* |
| Is "one-way-tightening philosophy" the same as a ratchet? | No — it's the weaker, self-attested version (used in `definition-of-done.md`); a true ratchet requires an enforcement mechanism producing a mechanical floor |
| Is a self-improving loop the same as continuous improvement? | No — continuous improvement corrects toward a fixed, externally-set reference; a self-improving loop updates its own reference automatically, one level of recursion higher |
| Is a "parameter" always something a loop solves for? | No — a fixed parameter (`mutationThreshold`) is set externally and never touched by the act stage; an unknown/decision variable (neural network weights) is exactly what the act stage solves for, cycle after cycle |
| Is root-finding a different kind of problem than optimization? | No — same shape, different criterion: optimization finds the unknown that extremizes a function; root-finding finds the unknown that makes a function equal a target. Newton's method and gradient descent are the same loop, different compare-stage criterion |

## References

- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) — the `definition-of-done.md` case study on mechanical floors vs. self-attested norms, referenced throughout this article.
- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md) — the base loop shape this article's terms attach constraints to.
- [Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness](feedback-loop-enforcement-extensions-funnels-and-epistemics.md) — the funnel structure and known-unknown/unknown-known material, split out to keep this article focused on the core fourteen terms.
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md) — gradient descent's weights as the concrete unknown-variable example.
- [Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md) — the extremum/optimum vocabulary the unknown-variable/root-finding distinction builds on.
- `~/.claude/rules/spring/resilience4j.md` — this repo's own fallback/circuit-breaker conventions.
- `~/.claude/rules/spring/liquibase.md` — this repo's own `<preConditions>` prerequisite convention.

## Related Articles

- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Feedback Loop Enforcement Extensions: Funnels and Epistemic Awareness](feedback-loop-enforcement-extensions-funnels-and-epistemics.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)
- [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md)
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
- [Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md)
- [Feedback Loop Domain Depth: PID Control, Hysteresis, and Feedback Delay](feedback-loop-control-engineering-pid-hysteresis-and-delay.md) — hysteresis, contrasted against the ratchet mechanism defined here

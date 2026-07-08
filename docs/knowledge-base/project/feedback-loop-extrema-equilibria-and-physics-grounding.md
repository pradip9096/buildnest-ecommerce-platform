---
title: "Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding"
category: documentation
tags: [feedback-loop, extremum, extremum-principles, equilibrium, thermodynamics, control-theory, nash-equilibrium, steady-state, inclination]
keywords: [extremum, optimum, extremum principles, principle of stationary action, least action, Fermat least time, minimum energy, minimum total potential energy, thermodynamic potentials, Helmholtz free energy, Gibbs free energy, enthalpy, entropy, Onsager, Prigogine, dissipative structures, saddle point, critical point, equilibrium, stable equilibrium, unstable equilibrium, steady state, nonequilibrium steady state, NESS, Nash equilibrium, basin of attraction, inclination, slope, tilt, gradient]
objective: Place extremum, optimum, extremum principles (physics/thermodynamics), and equilibrium (stable/unstable, steady-state vs. true equilibrium, Nash equilibrium) beneath the substrate companion — the physics- and mathematics-grounded depth beneath "dynamic," split out to keep each article focused.
audience: anyone who has read the substrate companion's "Dynamic" section and wants the physics/thermodynamics/game-theory depth beneath extremum and equilibrium specifically
scope: general (classical mechanics, thermodynamics, game theory) with cross-references to BuildNest's own worked CI and ML examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - feedback-loop-domain-instance-machine-learning-training.md
  - feedback-loop-ml-training-optimization-landscape.md
  - quality-gate-ratchet-pattern.md
status: published
---

# Feedback Loop Substrate Depth: Extrema, Equilibria, and Physics Grounding

## What Is It?

[Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md)
names "dynamic" as more primitive than cyclical process, and gestures at extremum and equilibrium
as related concepts one level down. This companion is that depth in full: **extremum vs. optimum**,
the physics **extremum principles** that ground "minimum energy" precisely, and **equilibrium**
(stable/unstable, true equilibrium vs. steady state, and the multi-agent Nash sense) — the
mathematics and physics underneath every "settles into a stable state" claim made elsewhere in this
family.

## Why It Matters

"The system settles into a minimum" and "the system reaches equilibrium" get used almost
interchangeably throughout this family, and in gradient-flow systems (a ball rolling under gravity,
gradient descent) they really are the same point. But they're not the same *concept*, and outside
gradient-flow systems they can come apart entirely — an oscillator has no equilibrium at all despite
being perfectly well-behaved, and a system far from equilibrium can violate an extremum principle
that holds close to it. Getting this precise is what prevents overclaiming: a system that looks like
it's optimizing something may just be obeying an ordinary physical law with no correction happening
at all.

## How It Works

### Extremum vs. optimum — the same descriptive/prescriptive divide as "dynamic vs. control"

**Extremum** (plural extrema) is the purely descriptive term: a point where a function attains a
local or global maximum or minimum — a fact about the function, existing whether or not anyone is
trying to optimize anything. **Optimum** is the same point interpreted through a declared goal (we
want this because we're minimizing cost, or maximizing fitness). Extremum is to optimum exactly
what dynamics is to control ([Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md)):
the neutral fact, and the goal-relative use of that fact.

### Extremum principles — where "minimum energy" gets precise

**Extremum principles** — a family of physical laws stating that a system's actual behavior
corresponds to an extremum of some quantity — are the clearest illustration of why this distinction
isn't academic. The **principle of stationary action** (Maupertuis, Euler, Lagrange, Hamilton;
popularly "least action," though it's technically about a *stationary*, not always minimal, value)
underlies classical mechanics, electromagnetism, general relativity, and quantum mechanics.
**Fermat's principle of least time** derives Snell's law of refraction from the same shape of
argument. **Minimum energy** explains why a ball settles at the bottom of a valley — the physical
version of exactly the local-minimum-seeking behavior mapped in
[Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md).
Thermodynamics gives the precise, constraint-specific form of "minimum energy" rather than one
vague principle: internal energy is minimized at constant entropy and volume; enthalpy at constant
entropy and pressure; Helmholtz free energy at constant temperature and volume; Gibbs free energy
at constant temperature and pressure; entropy itself is *maximized* for an isolated system at
equilibrium (Callen, 1985). Mechanics and materials science have their own instance, the
**principle of minimum total potential energy** — among kinematically admissible displacement
fields, the equilibrium one yields a *stationary* total potential energy. This is a **static
special case** of Hamilton's principle, not a separate, parallel example: remove time-dependence
from the general dynamic principle and this is what's left.

**Not every stationary configuration is a stable minimum**, and this precision is worth carrying
across domains explicitly. A ball balanced on a hilltop is also a stationary point (zero
variation) but is an unstable equilibrium, structurally a local maximum, not a minimum. This is
the mechanics-domain instance of the same critical-point/saddle-point correction added to
[Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
(Dauphin et al., 2014) — the same imprecision, the same fix, a third domain.

None of these systems are running a feedback loop. A ball rolling downhill isn't sensing its
position, comparing it to a target, and correcting — it's obeying F=ma, and happens to end at the
energy minimum as a mathematical consequence of that law, with no sensor, comparator, or actuator
anywhere in the picture. The end state looks identical to what a deliberately engineered optimizer
would reach; the mechanism producing it is entirely different. Don't let a system settling into an
extremum imply a feedback loop is running underneath it.

**A sharper version of that same caution: don't assume an extremum principle is running at all,
once far enough from equilibrium.** Onsager's minimum-entropy-production result (1931) is real,
but it holds only in the *near-equilibrium, linear* regime — small deviations from equilibrium,
linear flux-force relations, and technically describes a **nonequilibrium steady state (NESS)**,
not a true equilibrium (see below for the distinction). It does not generalize, and citing it
alongside Prigogine's name without that caveat risks the opposite mistake: Prigogine's own, far
more consequential contribution is the demonstration that **far from equilibrium, no such extremum
principle holds at all** — systems can instead spontaneously self-organize into complex, ordered
"dissipative structures" (Bénard convection cells, the Belousov-Zhabotinsky reaction), precisely
because they've left the regime where minimum entropy production applies (Nicolis & Prigogine,
1977). Prigogine's legacy is substantially about the *limits* of this whole family of principles,
not an extension of it — one of the more important findings in 20th-century thermodynamics, not a
footnote to soften.

### Equilibrium — formalizing "stable" and "unstable" used above

**Equilibrium** is a state of a system with no net tendency to change — a point where the dynamics
produce zero net change, so the system stays there unless perturbed. It's not the same thing as
**extremum**, even though the two have been used almost interchangeably above. Equilibrium is a
property of the *dynamics* (rate of change = 0); extremum is a property of a *function's value*
(highest or lowest point). They coincide exactly in **gradient-flow systems** — where the dynamics
literally are "move in the direction that decreases some potential" (a ball rolling under gravity,
gradient descent) — which is why the ball-in-a-valley and stationary-action examples above treat
them as interchangeable. They don't have to coincide in general: an undamped pendulum has no
equilibrium at all — it settles into a repeating orbit instead — with no function being
extremized anywhere in the picture.

**Stable vs. unstable, formalized:** a **stable equilibrium** is one where small perturbations
decay back toward it; an **unstable equilibrium** is one where small perturbations grow away from
it — exactly what was meant, without being named, by calling a valley-bottom "stable" and a
hilltop "unstable" above. This is the formal grounding for **basin of attraction**
([Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)):
the basin is the set of states that decay back to a particular stable equilibrium under the
system's dynamics.

**The unifying reframe this enables:** closed-loop control's entire purpose, in dynamical-systems
vocabulary, is to **engineer a desired state to be both an equilibrium and a stable one** — even
when the uncontrolled, open-loop system wouldn't naturally settle there at all. A thermostat isn't
just "comparing and correcting" in the abstract — mechanically, it manufactures a stable
equilibrium at the target temperature that wouldn't exist in the room's uncontrolled dynamics (a
room with no thermostat just drifts toward outdoor temperature). Every closed-loop control system
in this family — the PIT gate, gradient descent, a reflection loop — can be redescribed the same
way: taking a system whose natural equilibrium isn't where you want it, and adding feedback
specifically to relocate and stabilize the equilibrium at the reference value.

**Inclination — the everyday word for the same thing gradient already names precisely.**
Inclination is ordinary language for slope or tilt: the angle a surface makes relative to a
reference. A ball's inclination to roll downhill *is* the terrain's local slope acting on it via
gravity — the same mechanism as the extremum principles above, and, formalized, exactly what
**gradient** means in gradient descent
([Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)):
the gradient at a point is the local inclination of the loss surface. It inherits the same
local-information-only limitation — inclination, like gradient, only describes the *immediate*
slope where something is standing, saying nothing about the landscape further away, which is
exactly why neither can distinguish a local minimum from a global one on its own. And **zero
inclination is equilibrium, almost by definition** — equilibrium was just defined above as "no net
tendency to change"; flat ground, zero inclination, is precisely the condition under which nothing
rolls anywhere. "The gradient is zero" (calculus), "the system is at equilibrium" (dynamical
systems), and "the ground has no inclination here" (everyday language) all name the same event.

**Equilibrium vs. steady state — the precision that refines the Onsager/Prigogine material
above.** Strict equilibrium (zero net flow — nothing moving, no dissipation) is technically
different from a **steady state**: properties look constant over time, but there's ongoing flow or
dissipation underneath (a river holding constant depth while water continuously passes through).
Onsager's near-equilibrium result describes a **nonequilibrium steady state**, not true
equilibrium — entropy production is constant and *nonzero*, not absent. Regime boundaries matter
here exactly as much as they do for the extremum principles themselves.

**One more sense worth naming, since it's the multi-agent version of the same idea:** in game
theory, a **Nash equilibrium** is a state where no individual agent can improve their own outcome
by unilaterally changing strategy, given everyone else's strategy fixed — the multi-agent analog
of a single system settling at a fixed point. Each agent is individually at its own "no net
tendency to change," even though nothing is being globally extremized the way single-agent
gradient descent minimizes one loss function.

## When to Use It

- A system settles into a stable state and someone describes it as "optimizing" → check whether
  there's an actual sensor/comparator/actuator, or whether it's just an extremum principle playing
  out as ordinary physics — the end state can look identical either way.
- Someone claims a system minimizes some quantity in general → check whether that claim holds
  everywhere, or only in a specific regime (Onsager's near-equilibrium result is the cautionary
  example) before treating it as universal.
- Describing what closed-loop control actually does mechanically → "engineers a stable equilibrium
  at the reference value" is the precise dynamical-systems framing.
- A multi-agent system (a market, a set of interacting services) settles into a state where no
  individual actor benefits from changing behavior → that's a Nash equilibrium, not a single
  extremum being jointly minimized.

## Examples

BuildNest's PIT mutation gate (see [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md))
is dynamic (the mutation score changes over time) and its `mutationThreshold` is an optimum, not
merely an extremum — the number is chosen because the team wants it high, not because it's simply
where some natural process happened to settle. Contrast a ball settling at the bottom of a valley:
also dynamic, also reaching an extremum (minimum potential energy) and a stable equilibrium, but
with no declared goal and no comparator — an extremum with no optimum attached, because nothing is
"trying" for it.

## Synthesis

Extremum, extremum principles, and equilibrium are three tightly related but distinct pieces of the
same underlying picture: extremum is a fact about a function's value, equilibrium is a fact about a
system's dynamics, and they coincide only in the specific (common, but not universal) case of
gradient-flow systems. Extremum principles are where physics makes "minimum energy" precise instead
of a slogan, and they come with their own regime boundaries — valid near equilibrium, not
necessarily valid far from it. None of this changes anything about the taxonomy built on top of it;
it's what makes clear which parts of that taxonomy are genuinely engineered control, and which parts
would happen anyway as ordinary physics.

## Quick Reference

| Question | Answer |
|---|---|
| Is extremum the same as optimum? | No — extremum is a descriptive fact about a function (it has a local/global min or max); optimum is that same point interpreted through a declared goal. Same divide as dynamics vs. control, one level down |
| Does a system settling at an energy minimum mean it's running a feedback loop? | No — a ball settling in a valley is obeying F=ma, not sensing and correcting. The end state looks like optimization; the mechanism is ordinary, uncontrolled dynamics |
| Is minimum total potential energy the same principle as least action? | No — it's a static special case: remove time-dependence from Hamilton's general dynamic principle and this is what's left |
| Does minimum entropy production hold everywhere? | No — only near equilibrium, in the linear regime (Onsager). Far from equilibrium, no known extremum principle holds; systems can instead self-organize into dissipative structures (Prigogine) |
| Is equilibrium the same as extremum? | No — equilibrium is a property of the dynamics (rate of change = 0); extremum is a property of a function's value. They coincide in gradient-flow systems but not in general (an undamped pendulum has no equilibrium at all) |
| What does closed-loop control actually do, in dynamical-systems terms? | Engineers a desired state to be both an equilibrium and a *stable* one — relocating and stabilizing the equilibrium at the reference value, even when the uncontrolled system wouldn't naturally settle there |
| Is a nonequilibrium steady state the same as true equilibrium? | No — true equilibrium has zero net flow; a steady state looks constant but has ongoing, nonzero flow or dissipation underneath. Onsager's near-equilibrium result describes the latter, not the former |
| Is inclination a different concept from gradient? | No — inclination is the everyday word for slope/tilt; gradient is the same concept formalized. Zero inclination and zero gradient both describe the same condition equilibrium is defined by |

## References

- Maupertuis, P.L.M. de; Euler, L.; Lagrange, J.L.; Hamilton, W.R. (18th–19th c.) — collectively established the principle of stationary ("least") action underlying classical mechanics and, later, quantum mechanics via Feynman's path-integral formulation.
- Fermat, P. de (1662). Principle of least time — derives Snell's law of refraction from an extremum principle in optics.
- Jaynes, E.T. (1957). "Information Theory and Statistical Mechanics". *Physical Review*, 106(4), 620. — the maximum entropy principle connecting thermodynamics and information theory.
- Callen, H.B. (1985). *Thermodynamics and an Introduction to Thermostatistics* (2nd ed.). Wiley. — the standard reference for the constraint-specific thermodynamic potentials cited above.
- Onsager, L. (1931). "Reciprocal Relations in Irreversible Processes". *Physical Review*, 37(4), 405 and 38(12), 2265. — origin of the minimum entropy production result, valid only in the near-equilibrium linear regime.
- Nicolis, G. & Prigogine, I. (1977). *Self-Organization in Nonequilibrium Systems*. Wiley. — demonstrates that far from equilibrium, no general extremum principle holds; systems instead self-organize into dissipative structures.
- Dauphin, Y.N. et al. (2014). "Identifying and attacking the saddle point problem in high-dimensional non-convex optimization". *NeurIPS 2014*. — the stationary-vs-minimum distinction applied to gradient descent.
- Nash, J. (1950). "Equilibrium Points in N-Person Games". *Proceedings of the National Academy of Sciences*, 36(1), 48–49. — origin of the Nash equilibrium.
- [Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md) — the "dynamic" material this article's extremum/equilibrium content extends.
- [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md) — the ML-domain instance of the saddle-point and basin-of-attraction concepts used above.

## Related Articles

- [Feedback Loop Substrate: Dynamics, Extrema, and Cyclical Process](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md)
- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Feedback Loop Domain Depth: ML Training's Optimization Landscape](feedback-loop-ml-training-optimization-landscape.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)

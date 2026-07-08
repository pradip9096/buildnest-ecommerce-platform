---
title: "Feedback Loop Domain Depth: PID Control, Hysteresis, and Feedback Delay"
category: documentation
tags: [feedback-loop, closed-loop-control, pid-control, hysteresis, feedback-delay, control-theory, oscillation, damping, feedforward-control, statistical-process-control]
keywords: [PID controller, proportional integral derivative, setpoint, process variable, manipulated variable, controlled variable, hysteresis, dead-band, chattering, overshoot, damping, underdamped, overdamped, critically damped, feedback delay, latency, dead-time, Smith predictor, oscillation, instability, circuit breaker, feedforward control, anticipatory control, statistical process control, SPC, control chart, control limits, common cause variation, special cause variation, Shewhart]
objective: Fill a real gap in the base taxonomy's "closed-loop control system" entry — name PID control's three-way decomposition of the compare stage's error signal, hysteresis as a third kind of asymmetry distinct from a ratchet, feedback delay as the root cause both respond to, feedforward control as the anticipatory complement to reactive feedback, statistical process control as the rigorous source of a dead-band's width, plus the standard industrial-control vocabulary (SP/PV/MV) this family's informal terms map onto.
audience: anyone who has read the base taxonomy's closed-loop control entry and wants the real control-engineering depth beneath it — PID tuning, resilience/circuit-breaker design, anti-flapping mechanisms
scope: general (control theory, industrial automation) with BuildNest-specific worked examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: high
evidence_strength: strong
related_articles:
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - feedback-loop-enforcement-and-safety-vocabulary.md
  - feedback-loop-domain-instance-machine-learning-training.md
  - quality-gate-ratchet-pattern.md
status: published
---

# Feedback Loop Domain Depth: PID Control, Hysteresis, and Feedback Delay

## What Is It?

[Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
names "closed-loop control system" as a domain instance but never opens it up. This article fills
that gap with three real control-engineering concepts: **PID control** (how the compare stage's
error signal actually gets decomposed and used in most real controllers), **feedback delay**
(the time cost between action and observable effect — the root cause of oscillation and
instability), and **hysteresis** (a third kind of asymmetry, distinct from both ordinary
bidirectional correction and a ratchet's permanent lock). A translation table to the standard
industrial-control vocabulary (setpoint, process variable, manipulated variable) closes the gap
between this family's informal language and what a control engineer would actually call the same
things.

## Why It Matters

This family has used "compare and correct" as if it were a single, uniform operation for the
entire session. It isn't. Real controllers split the error signal into three different temporal
uses, and getting that split wrong — or ignoring the delay between acting and observing the
result — is the actual mechanical cause of a system oscillating, overshooting, or chattering
rather than settling cleanly. Naming these precisely turns "the system is unstable" from a vague
complaint into a diagnosable question: is the correction too aggressive relative to the delay, is
there no memory of past error, or is a boundary condition (like a rate limit) flapping because
there's no dead-band around its threshold?

## How It Works

### PID — the compare stage's error signal, decomposed three ways

A **PID controller** (Proportional–Integral–Derivative) doesn't use the current error alone. It
combines three terms:

| Term | Reacts to | What it corrects for |
|---|---|---|
| **P** (Proportional) | The *current* error | The baseline correction — bigger error, bigger response |
| **I** (Integral) | *Accumulated past* error | Persistent small biases a pure-P controller never fully closes (a P-only thermostat settles slightly off-target forever; the integral term keeps pushing until the accumulated error is actually zero) |
| **D** (Derivative) | The error's *rate of change* | Anticipates where the system is heading, damping overshoot before it happens rather than reacting after |

This is a real decomposition of the **compare** stage into three temporal perspectives on the same
signal — past (I), present (P), and predicted future (D) — not three different mechanisms. Every
domain instance in this family that's ever used "compare and correct" as a single step has
implicitly been doing pure-P control; most real controllers use all three because P alone leaves
persistent error and invites overshoot.

### Feedback delay — the root cause PID's D-term and hysteresis both respond to

Every loop mapped in this family has implicitly assumed the compare stage's signal arrives
instantly. Real systems have **delay** between the act stage's output and the observable effect of
that output — interest-rate policy takes months to show economic impact; a shower's classic
hot/cold overcorrection is caused by pipe delay, not a bad controller turning the tap the wrong
way. Delay is the textbook cause of oscillation and instability: if the correction keeps
compounding before its previous effect has even been observed, the system overshoots, corrects
too far the other way, overshoots again, and can spiral rather than converge. Control theory calls
these **dead-time systems**, and one standard mitigation is a **Smith predictor** — a controller
that models the expected delay and corrects based on a *predicted* current state rather than the
stale, delayed measurement.

This is the connective tissue between the two other concepts in this article: PID's **D** term
exists specifically to counter the overshoot risk that delay creates, by damping the correction
before the delayed error signal has fully caught up. Hysteresis (below) is a second, independent
response to the same underlying risk — instead of predicting through the delay, it simply refuses
to react to noise near the boundary at all.

### Hysteresis — a third kind of asymmetry, not a restatement of the ratchet

A **ratchet** ([Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)) permanently blocks
reversal — once a value is locked in, it cannot move backward, full stop. **Hysteresis** is
weaker and far more common: a system requires a *bigger push* to reverse direction than it took to
advance, but reversal is still possible. A thermostat with a dead-band won't turn the heater on
until the temperature drops 2° below target, and won't turn it off until it rises 2° above —
deliberately, to prevent rapid on/off **chattering** right at the boundary. This fills a real gap
between "fully symmetric, correct either direction identically" (ordinary closed-loop control) and
"fully asymmetric, permanently" (a ratchet) — hysteresis is the middle case, asymmetric but not
irreversible.

BuildNest's own circuit breaker (`resilience4j.md`) exhibits a version of this shape without being
a textbook symmetric dead-band: `CLOSED → OPEN` fires on a failure-rate threshold, but
`OPEN → HALF_OPEN` only fires after a fixed cool-down (`waitDurationInOpenState`), and
`HALF_OPEN → CLOSED` requires probe calls to actually succeed — different conditions govern the
forward and backward transitions, plus a mandatory delay before even attempting recovery, which is
exactly the anti-chattering purpose hysteresis and dead-bands serve elsewhere.

### Feedforward control — the anticipatory pairing this whole family has been missing

Every domain instance in every companion so far — PID, gradient descent, the PIT gate, a
reflection loop — is **reactive**: it corrects *after* the compare stage observes an error.
**Feedforward control** is the classic textbook complement: adjust the actuator *preemptively*,
based on a model of an anticipated disturbance, before that disturbance ever shows up as a
measurable error at all. A furnace controller that starts heating the moment a door opens (using a
door sensor), rather than waiting for the room to actually cool down and the thermostat to notice,
is feedforward. It requires a model of the disturbance — you have to know a cold draft is coming
to act on it before it arrives — which is exactly why it's normally used *alongside* feedback, not
instead of it: feedforward handles disturbances you can predict, feedback (with all the PID/delay
machinery above) cleans up whatever feedforward's model got wrong or didn't anticipate. The Smith
predictor above is one specific, delay-focused instance of this more general anticipatory strategy.

### Statistical process control — where the hysteresis boundary actually comes from

The hysteresis section above asserts that a dead-band should exist, without saying how wide it
should be. **Statistical process control** (Shewhart, 1931 — the same lineage Deming's PDCA,
already cited elsewhere in this family, comes from) gives the rigorous answer. A **control chart**
plots a process's measurements against **control limits** — typically set at ±3 standard
deviations from the process mean — and distinguishes two kinds of variation:

- **Common-cause variation**: ordinary noise inherent to the process, staying within the control
  limits. The correct response is *no response* — reacting to this is exactly the chattering
  hysteresis exists to prevent.
- **Special-cause variation**: a measurement outside the control limits, signaling something
  genuinely changed. This *should* trigger a correction — it's a real signal, not noise.

This is the formal, statistically-grounded version of the boundary hysteresis only asserted
informally: the width of a dead-band isn't arbitrary, it's answerable by asking how much variation
the process exhibits when nothing is actually wrong.

### Translation table: this family's vocabulary vs. standard control engineering

| This family's term | Standard control-engineering term |
|---|---|
| Reference / target value | **Setpoint (SP)** |
| The sensed/measured output | **Process variable (PV)** |
| The act stage's output to the plant | **Manipulated variable (MV)** |
| The quantity actually being regulated (often but not always the same as PV) | **Controlled variable (CV)** |

Useful when cross-referencing this family's informal language against real control-engineering
literature or hardware datasheets, which will use SP/PV/MV/CV, not "reference" and "compare stage."

## When to Use It

- A system oscillates or overshoots its target instead of settling → check whether the correction
  is proportional-only (no D term) relative to how much delay exists between action and observed
  effect; the two are the same diagnosis from different angles.
- A rate limit, circuit breaker, or autoscaler flaps rapidly at its threshold → this is chattering;
  the fix is a dead-band (hysteresis), not a stricter or looser threshold in the same symmetric
  sense.
- A metric persistently settles slightly off-target and never fully closes the gap → that's the
  signature of missing integral (I) action, not a broken sensor.
- Translating a control-engineering conversation into this family's vocabulary, or vice versa → use
  the SP/PV/MV/CV table above.
- A disturbance is predictable in advance (a scheduled event, a known upcoming load spike) →
  consider feedforward alongside feedback, rather than waiting for the compare stage to notice the
  effect after the fact.
- Deciding how wide a dead-band or alert threshold should be → don't guess; use the process's own
  observed variation (control limits) to separate real signal from ordinary noise.

## Examples

BuildNest's rate-limiting and circuit-breaker configuration
([`resilience4j.md`](../../../.claude/rules/spring/resilience4j.md)) is effectively proportional-only
control with a delay-based hysteresis bolt-on: the breaker reacts to the *current* failure rate
(P-like), has no memory of past failure trends (no I term) and no anticipatory damping (no D term),
and uses `waitDurationInOpenState` as a hard delay before even probing recovery — a blunter,
simpler anti-chattering mechanism than a true dead-band, but serving the same purpose: don't let a
system near a threshold flap.

## Synthesis

"Closed-loop control system" has been treated as a single, opaque box throughout this family. PID
opens that box and shows the compare stage isn't one operation but three temporal perspectives on
the same error. Feedback delay explains *why* a naive, undamped version of that box goes unstable —
correcting on stale information compounds rather than converges. Hysteresis is the practical,
widely-used answer to the specific failure mode delay creates at a boundary: don't react to every
fluctuation, require a real push before reversing — and statistical process control gives that
dead-band a rigorous width instead of an arbitrary one. Feedforward is the one genuine complement to
everything else in this family: every other mechanism here reacts after the fact; feedforward acts
before the fact, on a model of what's coming rather than a measurement of what already happened.
None of these are new loop shapes — they're the actual engineering detail inside the "closed-loop
control system" box this family has been citing by name since the base article, without ever
describing what's inside it.

## Quick Reference

| Question | Answer |
|---|---|
| Is PID a different mechanism than "compare and correct"? | No — it's the same compare stage, decomposed into three temporal uses of the error signal (current, accumulated, predicted) instead of one |
| Is hysteresis the same as a ratchet? | No — a ratchet permanently blocks reversal; hysteresis just requires a bigger push to reverse than to advance. Reversal is still possible |
| Why does feedback delay cause oscillation? | Because corrections compound before their previous effect has been observed — the system overshoots, corrects too far the other way, and can spiral instead of converging |
| What's the standard term for "reference" in real control engineering? | Setpoint (SP). The sensed output is the process variable (PV); the actuator's output is the manipulated variable (MV) |
| Is feedforward a replacement for feedback? | No — feedforward requires a model of the disturbance and handles what it can predict; feedback (PID and the rest of this article) cleans up whatever feedforward's model missed. They're normally used together |
| How wide should a dead-band be? | Not arbitrary — statistical process control derives it from the process's own observed variation (typically ±3 standard deviations), separating real signal (special-cause variation) from ordinary noise (common-cause variation) |

## References

- Åström, K.J. & Hägglund, T. (2006). *Advanced PID Control*. ISA. — the standard reference for PID controller design and tuning.
- Åström, K.J. & Murray, R.M. (2008). *Feedback Systems: An Introduction for Scientists and Engineers*. Princeton University Press. — general control-theory reference covering delay, dead-time systems, the Smith predictor, and feedforward control.
- Smith, O.J.M. (1957). "Closer Control of Loops with Dead Time". *Chemical Engineering Progress*, 53(5), 217–219. — origin of the Smith predictor for dead-time compensation.
- Shewhart, W.A. (1931). *Economic Control of Quality of Manufactured Product*. Van Nostrand. — origin of the control chart and statistical process control, source for the control-limits framing above.
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md) — the ratchet mechanism hysteresis is contrasted against above.
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md) — fallback and circuit-breaker vocabulary the hysteresis example builds on.
- `~/.claude/rules/spring/resilience4j.md` — this repo's own circuit-breaker state machine, used as the worked example above.

## Related Articles

- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Feedback Loop Extension: Enforcement and Safety Vocabulary](feedback-loop-enforcement-and-safety-vocabulary.md)
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
- [Quality Gate Ratchet Pattern](quality-gate-ratchet-pattern.md)

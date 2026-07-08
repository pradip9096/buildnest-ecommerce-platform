---
title: "Feedback Loop Domain Instance: Agentic Reflection Loops"
category: documentation
tags: [feedback-loop, reflection-loop, self-reflection-loop, agentic-ai, self-improving-loop, llm, parametric-correction, contextual-correction, iteration-granularity, grounded-verification]
keywords: [reflection loop, self-reflection loop, generation verification reflection, generate evaluate correct, self-refine, reflexion, agentic AI, LLM critique, in-context learning, parametric correction, contextual correction, iteration granularity, self-correction, verbal reinforcement learning, grounded verification, self-referential verification, lift, drift, over-correction]
objective: Place the generate-verify-reflect (and generate-evaluate-correct/"self-reflection loop") pattern used in agentic/LLM systems onto the feedback-loop taxonomy as a sixth domain instance, and introduce three axes the taxonomy used implicitly but never named — parametric vs. contextual correction, iteration granularity, and grounded vs. self-referential verification.
audience: anyone who has read the base taxonomy and wants the agentic-AI mapping, or anyone designing generate/verify/reflect loops for LLM agents
scope: general (agentic AI system design) with a worked comparison against BuildNest's own CI examples
source_conversations: [Session 2026-07-08]
last_updated: 2026-07-08
confidence: medium
evidence_strength: moderate
related_articles:
  - feedback-loop-taxonomy-substrate-instance-stage-symmetry.md
  - feedback-loop-domain-instance-machine-learning-training.md
  - feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md
status: published
---

# Feedback Loop Domain Instance: Agentic Reflection Loops

## What Is It?

A **reflection loop** is a closed-loop pattern used in agentic/LLM systems: an agent **generates**
a candidate output, an evaluator **verifies** it against criteria, and a critic **reflects** —
producing structured feedback that gets fed back into the next generation attempt. This article
places that pattern as a **sixth domain instance** of the base feedback loop mapped in
[Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md),
alongside engineering, organizational, quality, personal-development, and machine-learning-training
instances already covered — and introduces two axes the taxonomy has been using implicitly across
all of those without ever naming.

## Why It Matters

The generate/verify/reflect pattern is close enough to the base loop shape that it's tempting to
treat "reflect" as just another name for backpropagation's role (translating an error into a
correction). It isn't, and the difference matters for anyone designing one: a reflection loop's
correction is usually applied to the *next input*, not the system's *parameters*. Missing that
distinction leads to designing evaluation metrics (like measuring "did accuracy improve") that
implicitly assume parametric learning is happening, when in a frozen-model agent, nothing about the
model has actually changed — only the context has.

## How It Works

| Term | Role | Maps to |
|---|---|---|
| **Generation** | The model produces one or more candidate outputs | The **act** stage |
| **Verification** | An evaluator (LLM-based or programmatic) scores candidates against criteria | The **compare** stage — a concrete fitness function/criterion, same slot mapped in the enforcement-vocabulary companion |
| **Reflection** | A critic produces structured feedback, injected into the next generation attempt | Sits *between* compare and act, the same *position* backpropagation occupies — translating an error signal into an actionable correction |

Repeat (the next generation attempt) is ordinary iteration from the base taxonomy.

### New axis: parametric vs. contextual correction

Backpropagation's correction is **parametric** — it changes the model's weights. A reflection
loop's correction is normally **contextual** — the critique is injected as text into the next
prompt, while the underlying model is typically frozen; nothing about its parameters changes
between iterations. This is a real distinction the taxonomy hasn't named before, despite every
prior domain instance implicitly picking one side of it without saying so:

| Domain instance | Correction type |
|---|---|
| Gradient descent (ML training) | Parametric — weights change |
| Ratchet threshold bump (PIT gate) | Parametric — a configuration value changes |
| Reflection loop | Contextual — the next prompt's input changes, model frozen |
| Few-shot prompt refinement | Contextual |
| A person incorporating peer feedback into a next draft | Arguably contextual — the person's underlying skill may or may not change, but the immediate correction is "write differently next time," not "become a different person" |

### New axis: iteration granularity

Every domain instance mapped so far runs at a different timescale: PIT's ratchet moves per
milestone (weeks), PDCA per organizational cycle, personal development over years, ML training in
milliseconds per step. A reflection loop typically runs 2–5 iterations *within a single agent
task*, seconds apart — tighter than any other instance mapped in this family. Nothing in the base
taxonomy names "how tight is one cycle" as a dimension, even though it has been varying by orders
of magnitude across every example without comment.

### New axis: grounded vs. self-referential verification

A closely related term, **self-reflection loop**, is usually described the same three-stage way
(Generate → Evaluate → Correct) but with one explicit difference: the evaluator has **no external
reference** — the same system critiques its own output, "without external human intervention."
That's a third real axis, separate from the two above: does the compare stage have access to
something the generate stage didn't (retrieved documents, executed tests, a human, ground truth),
or is it the same process judging itself with no independent signal?

| Verification source | Example |
|---|---|
| **Grounded** | RAG-verified facts, an executed test suite, human review, PIT's mutation analysis |
| **Self-referential** | A model asked to critique its own prior response with no external check |

This distinction carries a real, documented risk, not just a definitional nicety: **self-referential
verification inherits the blind spots of the process that made the error.** If a model produced a
wrong answer because it didn't know it was wrong, asking the same weights to critique themselves
frequently fails to catch it — the critique and the error share a cause. This is a known limitation
raised against naive self-correction approaches (see Huang et al. below), not a hypothetical
concern. A self-reflection loop should be treated as the *weaker*, higher-risk case of the general
pattern — self-critique alone is not a substitute for a grounded verifier.

**Terminology note:** "self-reflection loop," described with Generate/Evaluate/Correct phases and
**lift**/**drift**/**cost-per-improvement** metrics, is otherwise the same pattern as the
Generate/Verify/Reflect loop above — lift ≈ pre/post accuracy delta, drift ≈ over-correction rate,
cost-per-improvement is the same term. Treat these as synonyms of the same instance rather than a
separate one. Likewise, "Plan, Act, Review, Adapt" as a personal-development framing is **PDCA
relabeled** (Plan=Plan, Act=Do, Review=Check, Adapt=the correction) — already covered by the base
taxonomy's continuous-improvement instance, not a new domain.

### Is a reflection loop a self-improving loop?

Not by default. The verification criteria are normally fixed *within* a task — the loop refines the
*candidate*, not the *standard*. That's ordinary closed-loop control at a very tight cadence, the
same structural claim
[Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
makes about plain SGD. It crosses into self-improving-loop territory only for an agent with
persistent memory whose *critique rubric itself* updates across tasks — reflection that changes
future reflection, not just future generation.

### A case that looks like it fits, but doesn't

Not every use of the phrase "reflection loop" is this mechanism. A reactive sculpture that reflects
viewers back at themselves may share the word "reflection," but without a criterion, a comparison,
or an iterative correction toward a target, it isn't an instance of this taxonomy's loop — it's a
homonym, not a structural match. Worth naming explicitly as a boundary case: matching vocabulary
isn't the same as matching structure, the same caution already applied elsewhere in this family
(distinguishing supersede from ratchet, or evolution from the ratchet effect).

### A case that does fit, but isn't new

Structured peer/supervisor feedback in academic or professional development (present work → receive
feedback → iterate) does map onto this taxonomy — but as the socially-mediated version of the
**personal-development domain instance** already covered in
[Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md),
not a reason for a separate domain of its own.

## When to Use It

- Designing evaluation metrics for an agentic system → check whether the loop is parametric or
  contextual before choosing a metric. "Pre-vs-post accuracy delta" only means what it sounds like
  if something about the system's underlying capability actually changed; for a frozen-model
  reflection loop, an accuracy improvement measures better-targeted context, not learning.
- Comparing a reflection loop to a training loop → use iteration granularity to explain why they
  feel different in practice even though both are sense-compare-act-repeat: a reflection loop's
  entire lifecycle can complete in the time a single gradient-descent step takes to log.
- Someone calls something a "reflection loop" outside an agentic/LLM or structured-feedback context
  → verify there's an actual criterion and correction before accepting the label; the sculpture case
  above is the cautionary example.

## Examples

BuildNest doesn't currently implement an agentic generate/verify/reflect loop, but the structural
parallel to its own CI is direct: `mvn verify -P ci` generates a build, PIT's mutation analysis
verifies it against `mutationThreshold`, and a developer reading the survived-mutants report and
writing a stronger assertion is the human equivalent of "reflection" — except that correction is
parametric (the test suite itself changes, permanently) rather than contextual, and it runs at
milestone granularity, not within seconds. The same three-stage shape, opposite ends of both new
axes.

## Synthesis

The generate/verify/reflect pattern earns its place as a real sixth domain instance, but the more
durable contribution isn't the instance itself — it's the three axes it forced into the open.
Parametric vs. contextual correction explains why a reflection loop's "improvement" and a training
run's "improvement" aren't measuring the same kind of change, even though both are legitimate
closed-loop control. Iteration granularity explains why the same abstract loop shape can span six
orders of magnitude in cycle time across the domain instances mapped in this family, from seconds
to years, without changing its underlying structure. Grounded vs. self-referential verification is
the one with a real safety implication attached: a compare stage judging output it had no
independent way to check is a structurally weaker loop, not just a differently-named one, and
"self-reflection loop" is exactly that weaker case wearing a near-identical vocabulary to its
grounded sibling. All three axes were present in every prior mapping; this is just the first time
something forced them to be named.

## Quick Reference

| Question | Answer |
|---|---|
| Is "reflection" in a reflection loop the same as backpropagation? | No — same *position* in the loop (translating an error into a correction), different *mechanism*: backprop is parametric (changes weights), reflection is normally contextual (changes the next prompt, model stays frozen) |
| Is a reflection loop a self-improving loop? | Not by default — the verification criteria are usually fixed within a task. Only becomes self-improving if the critique rubric itself updates across tasks |
| Does the Kelly Heaton sculpture belong in this taxonomy? | No — it shares the phrase "reflection loop" but has no criterion, comparison, or iterative correction; a homonym, not a structural instance |
| Is the education/psychology "reflection loop" a new domain? | No — it's the socially-mediated version of the personal-development domain instance already mapped |
| Is a "self-reflection loop" a different mechanism than a "reflection loop"? | No — same pattern, same metrics under different names (lift/drift ≈ accuracy-delta/over-correction). The one real difference is verification source: self-reflection implies no external reference, which is a genuine, higher-risk case, not a different structure |
| Is self-critique alone a reliable substitute for grounded verification? | No — self-referential verification inherits the blind spots of the process that made the error; a model that doesn't know it's wrong often can't catch its own mistake by asking itself again |

## References

- Shinn, N. et al. (2023). "Reflexion: Language Agents with Verbal Reinforcement Learning". *NeurIPS 2023*. — a well-known agentic-AI paper implementing this generate/verify/reflect pattern with natural-language self-critique in place of parametric updates.
- Madaan, A. et al. (2023). "Self-Refine: Iterative Refinement with Self-Feedback". *NeurIPS 2023*. — another established reference for the same iterative generate-critique-refine pattern.
- Huang, J. et al. (2023/2024). "Large Language Models Cannot Self-Correct Reasoning Yet". *ICLR 2024*. — documents the blind-spot limitation of self-referential verification without external feedback, cited above for the grounded-vs-self-referential distinction.
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md) — the parametric-correction domain instance this article contrasts against.
- [Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md) — the personal-development domain instance the education/psychology usage maps onto.
- **Note on confidence**: the specific named framework terminology, metrics (e.g. "over-correction rate," "cost-per-improvement"), and the Kelly Heaton sculpture attribution were supplied in conversation and have not been independently verified this session — treat those specifics as unconfirmed pending a citation check, which is why this article's `confidence` and `evidence_strength` are set lower than its sibling companions.

## Related Articles

- [Feedback Loop Taxonomy: Substrate, Instance, Stage, and Symmetry](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md)
- [Feedback Loop Domain Instance: Machine Learning Training](feedback-loop-domain-instance-machine-learning-training.md)
- [Feedback Loop Extension: Cycle Vocabulary, Personal Archetypes, and Change Theory](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md)

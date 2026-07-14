# Knowledge Base — Project

Durable, reusable knowledge about BuildNest's engineering practices, tooling decisions, and project-specific patterns. Each article is self-contained and independently discoverable.

For operational lessons extracted from specific sessions (tooling gotchas, one-time fixes, process hygiene), see [`docs/wiki/learned-lessons/`](../../wiki/learned-lessons/README.md).

This file does two distinct jobs, kept in two separate sections below: **Orientation** (what this
KB is, how to categorize and author an article) and **Index** (the manifest — the authoritative
list of every article, as one-line surrogate rows). See
[Manifest and Surrogate Pattern for Index Files](manifest-and-surrogate-pattern-for-index-files.md)
for why this split matters — orientation content is prose that can grow; the Index table is a
manifest and should stay a clean, scannable set of rows, not accumulate explanation of its own.

---

## Orientation

### Taxonomy

#### Categories

| Category | Description |
|---|---|
| `quality-engineering` | Test strategy, coverage gates, mutation testing, fitness functions |
| `tooling` | Developer tools, CI configuration, IDE integration, shell patterns |
| `infrastructure` | Environment setup, Docker, databases, WSL2 |
| `documentation` | Diagram conventions, doc formats, knowledge organization |
| `product` | Business model, domain concepts, product decisions |
| `security` | Auth/authz mechanics, framework-specific security precedence and pitfalls |

#### Naming Convention

Files use lowercase hyphen-separated names describing the topic: `<topic-noun>-<qualifier>.md`. Avoid dates in filenames — use `last_updated` in frontmatter instead.

### Frontmatter Schema

Every article must include the fields below (see the Article Template section further down for
the full copy-paste YAML block combined with the body skeleton):

- `title`, `category`, `tags`, `keywords` — for discoverability and the index table.
- `objective` — one sentence: what question does this article answer?
- `audience` — who reads this and in what situation?
- `scope` — BuildNest-specific, general, or both?
- `confidence` — how certain is the content?
- `evidence_strength` — how well-evidenced is the claim (strong = reproduced/cited, moderate = observed once, weak = inferred)?

### Article Template

Copy this as the starting point for a new Topic-format article (this KB's default — see Format
Selection below if you're genuinely writing a Q&A instead). It combines the frontmatter schema
above with the Topic anatomy from
[Structural Anatomy](../learning/knowledge-organization-formats-qa-topic-chapter.md#structural-anatomy):

Each section below is tagged with the same necessity vocabulary used in
[development-workflow.md](../../../.claude/rules/common/development-workflow.md#necessity-tags)
(Mandatory / Mandatory if Applicable / Conditionally Required / Recommended / Optional) — reused
here for one consistent necessity language across the repo, not to add ceremony. Delete any tag
comment before publishing; it's authoring guidance, not part of the article.

```markdown
---
title: 
category: 
tags: []
keywords: []
objective: 
audience: 
scope: 
source_conversations: []
last_updated: YYYY-MM-DD
confidence: high|medium|low
evidence_strength: strong|moderate|weak
related_articles: []
status: draft
---

# <Title>

<!-- Table of Contents — Conditionally Required: add one only once the article outgrows a
     single-scroll skim (see Size Discipline below), not before. -->

## What Is It? <!-- Mandatory -->

<Definition. One or two paragraphs — the narrow point the hourglass intro is narrowing toward.>

## Why It Matters <!-- Mandatory -->

<Motivation — what problem this solves, what breaks without it, why a reader should keep reading.>

## How It Works <!-- Mandatory -->

<The core mechanism. Use a diagram or table wherever it clarifies more than prose would. May be
decomposed into tagged `###` sub-sections if the topic genuinely has multiple distinct mechanisms
or steps (e.g. triggers, workflow stages, a template artifact) — see
[Closed-Loop Feedback and Amendment Mechanisms for Process Documents](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md)'s
own sub-structure for a real precedent. Don't pre-decompose by default; let it emerge from what
this specific topic actually needs. If you find yourself reaching for many such sub-sections,
that's the same signal from "When to Split" below — ask whether this is still one topic before
adding more.>

## When to Use It <!-- Mandatory -->

<Concrete triggers/conditions — not "use this sometimes," but the specific signals that mean this
applies now.>

## Examples <!-- Mandatory if Applicable -->

<At least one worked example. Prefer a real one from this repo's own history if `evidence_strength`
is meant to be strong or moderate — label any generic/illustrative example explicitly as such if
the article also contains real, citable ones, so a reader can't confuse the two. Skip only for a
genuinely abstract/definitional topic where a worked example wouldn't add anything. Same
sub-section note as "How It Works" applies if multiple distinct examples are needed.>

## Synthesis <!-- Mandatory -->

<Closing — genuinely tie it together, don't just restate. This is the widening-back-out half of
the hourglass; "See also" below is a separate, later element, not a substitute for this one.>

## Quick Reference <!-- Conditionally Required -->

<Worth adding once an article grows past a few sections; skip for a short one — a lookup table of
short question/answer pairs for skimming.>

## References <!-- Mandatory if Applicable -->

<Required whenever the article cites a named standard, framework, or external claim — see
`~/.claude/CLAUDE.md`'s "Verifying Standards, Best Practices, and Anti-Patterns" section for when a
claim needs to be verified before being asserted, not just cited from memory. Skip entirely if the
article makes no such claims.>

## Related Articles <!-- Recommended -->

<Same-folder link: `[Title](bare-filename.md)`. Cross-folder link: `[Title](../folder/file.md)`.
Always use a real relative markdown link, never a bare `[[wikilink]]` — see Cross-Referencing
Between Articles below for why. List the same targets in the `related_articles` frontmatter field
too.>
```

Start with `status: draft` and flip to `published` once the article is actually complete — not
before. Delete any section above that genuinely doesn't apply rather than leaving it as an empty
placeholder; the template is a starting shape, not a mandatory checklist every article must fill
every box of.

### Authoring Guidelines

Actionable rules for creating or editing an article — not a formal requirement specification.
This KB is maintained by a small team (a human + an AI assistant), so the guidance below optimizes
for "an author can check this in one glance," not for audit/traceability ceremony.

#### Format Selection

Before writing, pick the right format for the reader's actual intent — see
[Differences Between Q&A, Topic, and Chapter](../learning/knowledge-organization-formats-qa-topic-chapter.md)
for the full decision framework:

- **Q&A** — answers one specific question, standalone, no reading order.
- **Topic** — this KB's default format. Maps one concept fully. No mandatory reading order.
- **Chapter** — a sequential, dependent unit of a larger curriculum. This KB does not currently
  use this format — if you find yourself writing prerequisites, learning objectives, and
  exercises, you're writing a chapter, not a KB article; that belongs in a different location
  (e.g. `docs/knowledge-base/learning/` still applies, but structure it explicitly as a chapter
  sequence, not a lone Topic file).

#### Article Structure

Once you've picked Topic (this KB's default), structure the article as:
**What is it? → Why it matters → How it works → When to use it → Examples → Synthesis/Closing →
See also.** See
[Structural Anatomy](../learning/knowledge-organization-formats-qa-topic-chapter.md#structural-anatomy)
for the full breakdown, including why the "Synthesis/Closing" element matters and shouldn't be
skipped: the whole shape is an hourglass — broad context narrowing to the specific concept, a
developed middle, then widening back out to a real synthesis at the close, not just a pointer
list. "See also" is *not* a substitute for that closing synthesis — it's the separate, final
element for pointing elsewhere.

#### Size Discipline

There is no hard line-count limit — see
[Why Keep a Topic Article Short](../learning/knowledge-organization-formats-qa-topic-chapter.md#why-keep-a-topic-article-short)
for the reasoning (this is a documented target derived from first principles, not a rule copied
from elsewhere in this repo). In practice, most articles in this KB land in the ~90–200 line
range. If a draft is running well past that, it's a signal to check the next section before
publishing, not just a soft ceiling to shrug at.

#### When to Split (One Topic Per File)

One topic per file — split if an article covers two unrelated subjects. Concrete signals, adapted
from the same article's Topic-vs-Chapter criteria and applied here to *splitting within* the Topic
format rather than changing format entirely:

- A whole section could stand alone and be linked to from elsewhere, independent of the rest of
  the article.
- The section's actual audience differs from the article's stated `audience` frontmatter field.
- Removing the section wouldn't make the remaining article incomplete — it would just make it
  about one thing instead of two.
- You catch yourself writing a sentence like "this is a different topic, but related, so I'll
  include it here anyway."

When in doubt, split and cross-link both directions (`related_articles` in frontmatter, plus a
one-line pointer in the body) rather than leaving one article to cover both.

#### Extracting Inline Content Out of Other Project Docs (Rule Files, etc.)

This section governs extraction *into* this KB from somewhere else — a `.claude/rules/**/*.md`
rule file, a `CLAUDE.md`, or any other project doc that isn't itself part of this KB — as
distinct from "When to Split" above, which governs splitting *within* the KB. It applies
repo-wide, to any project doc, not to one specific file — this generalizes a narrower version of
the same rule first written into `development-workflow.md`'s own Amendment Log (2026-07-12,
entries 18-20) for that file's Sequence table, then found to apply just as directly to a rule
file with no table at all (`spring-security.md`'s CORS rule, same date).

**Qualitative trigger, not a length cap.** Extract when a paragraph, table cell, or section in
another doc contains a standalone, generalizable technique, definition, or worked example — the
kind of thing that would be useful knowledge in a *different* codebase or context, not just a
cross-reference specific to that doc's own internal structure. Raw length is a weak proxy: a
doc's own tight, self-referential rule (e.g. "step 2 answers X, not Y — see step 8 for Y") can be
long and still belong exactly where it is; a short paragraph that happens to restate a reusable
fact (a framework's documented precedence rule, a verification technique) can be short and still
be worth extracting. The signal is *duplication of reusable knowledge inline*, not size.

**Mechanics:**
1. Write the full explanation as its own KB article here, following this README's normal
   structure/frontmatter/category conventions.
2. In the source doc, replace the extracted content with a short pointer sentence and a real
   relative markdown link to the new article (see Cross-Referencing Between Articles below for
   link format — the same convention applies even though the source doc lives outside this KB).
3. Add the new article to this README's index table (see Housekeeping below).
4. If the source doc has its own change-tracking mechanism (e.g. `development-workflow.md`'s
   Amendment Log), log the extraction there too, per that doc's own rules — this KB's index
   entry is not a substitute for a source doc's own audit trail.

**Periodically re-check, don't assume a prior pass caught everything.** Citing one doc's fix as
"the existing pattern" does not verify that doc (or a different one with the same shape) was
actually already brought up to the same standard — this exact gap surfaced twice in one session
(`external-research`'s own row hadn't actually been shrunk despite being cited as precedent, and
`spring-security.md` had the same bloat as `development-workflow.md` with no rule yet saying the
pattern applied there too). When applying this pattern once, it's worth a quick scan of sibling
docs (other rule files, other tables) for the same shape before considering the job done.

See [Content Extraction: DRY/SSOT as the Decision Principle, Not Size](content-extraction-dry-ssot-as-the-decision-principle.md)
for the underlying *why* behind the qualitative trigger above — DRY/SSOT and Separation of
Concerns are the actual decision principles; abstraction, progressive disclosure, indirection,
and "modular documentation" describe the resulting shape, not the reason to produce it.

#### Cross-Referencing Between Articles

Always use a real relative markdown link — `[Title](filename.md)` for a same-folder target,
`[Title](../learning/filename.md)` for a cross-folder one. **Do not use a bare `[[wikilink]]`
anywhere in this KB.** This convention previously recommended bare wikilinks for same-folder
targets; that guidance was reversed on 2026-07-08 after a session found it produces links that
render as literal double-bracket text — not clickable — in GitHub, VS Code's default markdown
preview, and every other standard renderer this KB is actually read in. This KB has no automated
`[[wikilink]]` resolver, so a bare `[[name]]` never worked reliably even for same-folder targets,
and was actively misleading for cross-folder ones (genuinely ambiguous which folder to search). A
real relative link has zero downside and works everywhere.

List every cross-referenced article in the `related_articles` frontmatter field too, using the
same folder-relative path convention, so the relationship is machine-readable, not just prose.

#### Housekeeping

- Add a row to the Index table below when creating a new article.
- Update `last_updated` in frontmatter on any substantive edit.
- Cross-reference `docs/wiki/learned-lessons/` for operational lessons rather than duplicating content here.

---

## Index

The manifest — one row per article, no prose beyond this table. See
[Manifest and Surrogate Pattern for Index Files](manifest-and-surrogate-pattern-for-index-files.md)
for why this section is kept separate from Orientation above.

| File | Topic | Category | Last Updated |
|---|---|---|---|
| [quality-gate-ratchet-pattern.md](quality-gate-ratchet-pattern.md) | Fitness functions, ratchet mechanism, PIT mutation threshold schedule, broken-windows rationale, ratchet-vs-supersede/continuous-improvement/evolution distinctions | quality-engineering | 2026-07-12 |
| [claude-code-extension-mechanisms.md](claude-code-extension-mechanisms.md) | Claude Code hooks, MCP servers, slash commands, skills, and plugin marketplaces (official `claude-plugins-official`, screened community, and caution on unvetted third-party sites) — extension points and when to use each | tooling | 2026-07-11 |
| [claude-code-hooks-reference.md](claude-code-hooks-reference.md) | Hook types, event lifecycle, settings.json configuration | tooling | — |
| [claude-code-progressive-disclosure.md](claude-code-progressive-disclosure.md) | Progressive disclosure pattern in Claude Code UX | tooling | — |
| [claude-code-session-conversation-turn.md](claude-code-session-conversation-turn.md) | Session and conversation turn semantics in Claude Code | tooling | — |
| [mermaid-diagram-quality-attributes.md](mermaid-diagram-quality-attributes.md) | Mermaid diagram conventions for quality attribute documentation | documentation | — |
| [open-core-business-model.md](open-core-business-model.md) | Open-core business model pattern and examples | product | — |
| [check-mysql-installation-on-wsl2.md](check-mysql-installation-on-wsl2.md) | Verifying MySQL installation state on WSL2 | infrastructure | — |
| [claude-code-memory-directory.md](claude-code-memory-directory.md) | Memory directory structure, the four memory types, current BuildNest files, and how to shape memory via instruction, feedback, or direct editing | tooling | 2026-07-02 |
| [loop-engineering-vs-claude-code-loop.md](loop-engineering-vs-claude-code-loop.md) | "Loop engineering" as a 2026 industry trend (generator/verifier, ReAct) vs. Claude Code's narrower `/loop` scheduling skill and the closer `/goal` analog | tooling | 2026-07-02 |
| [post-implementation-learning-activities.md](post-implementation-learning-activities.md) | Classifies "lessons learned" against retrospective, PIR, postmortem/RCA, AAR, and continuous improvement; maps each onto a GitHub-issue-driven SDLC workflow; the 80/20 activity set for a solo-developer workflow | documentation | 2026-07-04 |
| [vscode-wsl2-responsiveness-during-heavy-maven-test-runs.md](vscode-wsl2-responsiveness-during-heavy-maven-test-runs.md) | VS Code freezing during `./mvnw test` on WSL2 — candidate mechanisms, evidence gathered against this repo, watcher-exclude + `.wslconfig` fixes, backgrounded-test-run fallback | infrastructure | 2026-07-07 |
| [closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md) | Open-loop vs. closed-loop control, negative/positive feedback, PDCA/Kaizen framing, and the amendment-mechanism pattern (trigger + correction + backfilled log) applied to this repo's own rule files | documentation | 2026-07-11 |
| [compliance-and-production-readiness-closed-loop-patterns.md](compliance-and-production-readiness-closed-loop-patterns.md) | The same closed-loop pattern applied at compliance-program scale (IIA/SOC 2/ISO 27001/NIST CSF) and to "production-grade software" quality attributes and a 6-level requirement-specification structure | documentation | 2026-07-07 |
| [adaptive-knowledge-governance-advanced-amendment-concepts.md](adaptive-knowledge-governance-advanced-amendment-concepts.md) | 15 concepts extending the base amendment-mechanism pattern — decision logs/ADRs, assumption tracking, traceability matrices, change impact scoring, knowledge ownership — synthesized as an Adaptive Knowledge Governance Framework | documentation | 2026-07-07 |
| [feedback-loop-taxonomy-substrate-instance-stage-symmetry.md](feedback-loop-taxonomy-substrate-instance-stage-symmetry.md) | **Base article.** Structural map placing feedback loop, iteration, control flow, closed-loop control, continuous improvement, live monitoring, and the ratchet mechanism on one taxonomy (substrate / whole-loop instance / single stage / symmetry-breaking modifier). See the companion extensions below for further vocabulary mapped onto the same taxonomy | documentation | 2026-07-08 |
| [feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md](feedback-loop-substrate-dynamics-extrema-and-cyclical-process.md) | Companion to the base taxonomy, one layer beneath control flow/iteration: dynamic vs. cyclical process, dynamical systems theory vs. control theory. See its own companion below for extremum/optimum/equilibrium | documentation | 2026-07-08 |
| [feedback-loop-extrema-equilibria-and-physics-grounding.md](feedback-loop-extrema-equilibria-and-physics-grounding.md) | Companion to the substrate companion: extremum vs. optimum, extremum principles (stationary action, Fermat's least time, thermodynamic potentials, minimum total potential energy), equilibrium (stable/unstable, steady state vs. true equilibrium, Nash equilibrium), and the Onsager/Prigogine near-vs-far-from-equilibrium caveat | documentation | 2026-07-08 |
| [feedback-loop-evaluative-dimensions-and-quality-disciplines.md](feedback-loop-evaluative-dimensions-and-quality-disciplines.md) | Companion to the base taxonomy: efficiency, effectiveness, excellence, optimization, and satisficing (evaluative dimensions — the corrective to treating optimization as the universal goal); quality assurance/control/management (a domain instance); refinement, progression, improvement, sustainable growth (trajectory descriptors) | documentation | 2026-07-08 |
| [feedback-loop-enforcement-and-safety-vocabulary.md](feedback-loop-enforcement-and-safety-vocabulary.md) | Companion to the base taxonomy: guardrails, quality gates, checkpoints, prerequisites, fallback, safety nets, filters, parameters, criteria, negative detection patterns, tightening mechanisms, enforcement mechanisms, mechanical floors, the self-improving loop, and the fixed-parameter vs. unknown/decision-variable distinction (optimization vs. root-finding). See its own companion below for the funnel and epistemic-awareness material | documentation | 2026-07-08 |
| [feedback-loop-enforcement-extensions-funnels-and-epistemics.md](feedback-loop-enforcement-extensions-funnels-and-epistemics.md) | Companion to the enforcement-and-safety companion: the funnel (open-loop-by-default filter chain; population throughput vs. single-instance trajectory), and known unknown vs. unknown known (loop observability vs. loop correctness — Polanyi's tacit knowledge vs. Žižek's disavowal) | documentation | 2026-07-08 |
| [feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md](feedback-loop-cycle-vocabulary-personal-archetypes-and-change-theory.md) | Companion to the base taxonomy: virtuous circle, self-correction, self-perpetuating; a personal-development domain instance (Prokopton, go-getter, self-actualizer); the Watzlawick first-order/second-order change distinction behind a ~40-verb "tune vs. redesign" cluster; quality attributes; and a three-way breakdown of "adaptation" (adaptive control vs. contextual vs. evolutionary) | documentation | 2026-07-08 |
| [feedback-loop-domain-instance-machine-learning-training.md](feedback-loop-domain-instance-machine-learning-training.md) | Companion to the base taxonomy: forward propagation, backpropagation, the chain rule, and gradient descent as a fifth domain instance — the neural-network training loop. See its own companion below for the optimization-landscape depth | documentation | 2026-07-08 |
| [feedback-loop-ml-training-optimization-landscape.md](feedback-loop-ml-training-optimization-landscape.md) | Companion to the ML-training companion: local vs. global minima, the critical-point/saddle-point precision, why saddle points dominate in high dimension (curse of dimensionality), and minimization/maximization as the same sign-flipped operation | documentation | 2026-07-08 |
| [feedback-loop-ml-search-strategy-and-generalization.md](feedback-loop-ml-search-strategy-and-generalization.md) | Companion to the optimization-landscape companion: exploration vs. exploitation (the framework behind momentum/noise/annealing/restarts), the bias-variance trade-off, and regularization as a fourth, softer boundary-setting mechanism | documentation | 2026-07-08 |
| [feedback-loop-domain-instance-agentic-reflection-loops.md](feedback-loop-domain-instance-agentic-reflection-loops.md) | Companion to the base taxonomy: generate/verify/reflect (and self-reflection) in LLM agents as a sixth domain instance; introduces parametric-vs-contextual correction, iteration-granularity, and grounded-vs-self-referential verification as new axes | documentation | 2026-07-08 |
| [feedback-loop-control-engineering-pid-hysteresis-and-delay.md](feedback-loop-control-engineering-pid-hysteresis-and-delay.md) | Companion to the base taxonomy — opens up "closed-loop control system": PID's three-way decomposition of the compare stage (proportional/integral/derivative), feedback delay/latency as the cause of oscillation and overshoot, hysteresis as a third asymmetry distinct from the ratchet, feedforward control, statistical process control, and a translation table to standard control-engineering vocabulary (setpoint/process variable/manipulated variable) | documentation | 2026-07-08 |
| [env-example-template-vs-env-local-secrets.md](env-example-template-vs-env-local-secrets.md) | Why `.env.example` (committed template) and `.env` (gitignored real secrets) are split, how the split works mechanically, and a real incident where a secret was pasted into the wrong file | tooling | 2026-07-09 |
| [stable-id-columns-decouple-cross-references-from-display-order.md](stable-id-columns-decouple-cross-references-from-display-order.md) | Stable ID columns vs. display order in growable numbered tables — the surrogate-key pattern generalized to any cross-referenced list; worked example: `development-workflow.md`'s Sequence table migration | documentation | 2026-07-11 |
| [research-discovery-phase-before-software-implementation.md](research-discovery-phase-before-software-implementation.md) | Problem identification, requirements gathering, existing-system assessment, feasibility/solution-option research (context7 vs. web search), and scope definition — the sub-activities of "research before you code," mapped onto `development-workflow.md`'s own `external-research`/`solution-options-adr` step split | documentation | 2026-07-11 |
| [devops-toolchain-inventory-and-verified-status.md](devops-toolchain-inventory-and-verified-status.md) | Full CI/CD and quality-gate tool inventory (JaCoCo, PIT, Codecov, OWASP Dependency-Check, SonarQube, CheckStyle, SpotBugs, Docker) with verified status per tool; the generalizable failure pattern behind three real incidents (#350/#353/#354) where a tool appeared configured but silently never ran or never surfaced its findings; blocking vs. advisory-only checks; the direct-CLI verification method | tooling | 2026-07-12 |
| [smoke-sanity-and-regression-testing-vs-ci-test-suite-coverage.md](smoke-sanity-and-regression-testing-vs-ci-test-suite-coverage.md) | Why a green CI run doesn't prove a runtime/infra change actually works; the #122→#363 case (3 real bugs — WSL2 mount propagation, YAML command mis-tokenization, an exporter's silently-removed env-var config method — none reachable by syntax validation or the unit/integration suite); smoke vs. sanity vs. regression as three distinct questions; when the step applies and when it doesn't; how to flag an unverifiable-in-this-environment gap honestly | quality-engineering | 2026-07-12 |
| [external-research-context7-vs-web-search-and-when-to-skip.md](external-research-context7-vs-web-search-and-when-to-skip.md) | What `development-workflow.md`'s `external-research` step covers ("how do I build this," not "which approach"); context7 (official API surface) vs. web search (idiomatic pattern, version-drift catches) tool selection; when to skip; pointer to `claude-code-extension-mechanisms.md` for sourcing a Claude Code capability itself | documentation | 2026-07-12 |
| [spring-security-cors-configurationsource-precedence-over-webmvc.md](spring-security-cors-configurationsource-precedence-over-webmvc.md) | Why an explicit `corsConfigurationSource` on a Spring Security filter chain makes any `WebMvcConfigurer.addCorsMappings` bean fully unreachable dead code, not merged/layered; the #352 case; the negative/positive-control live-preflight technique for verifying which config actually governs requests | security | 2026-07-12 |
| [content-extraction-dry-ssot-as-the-decision-principle.md](content-extraction-dry-ssot-as-the-decision-principle.md) | Why DRY/SSOT (not size, not a 7-item principle list) is the actual test for whether to extract content into a KB article; SoC as the complementary container-side question; abstraction/progressive-disclosure/indirection/modular-docs as consequences of extraction, not reasons for it; worked counter-example (`create-project-board` is the longest row and correctly stays inline) | documentation | 2026-07-12 |
| [manifest-and-surrogate-pattern-for-index-files.md](manifest-and-surrogate-pattern-for-index-files.md) | Manifest defined (enumerates artifacts + per-entry metadata, no primary content, authoritative inventory); surrogate records and indirection as the underlying mechanism; distinguishes manifest from table of contents, index, registry, changelog; applied to `MEMORY.md` and `docs/wiki/learned-lessons/README.md` | documentation | 2026-07-13 |
| [readme-manifest-blueprint.md](readme-manifest-blueprint.md) | Copy-paste Orientation/Index two-section skeleton for any directory README that needs to double as orientation content and an authoritative file manifest; adaptation rules; this KB's own README as the worked reference instance | documentation | 2026-07-13 |
| [slack-incoming-webhooks-for-alertmanager-notifications.md](slack-incoming-webhooks-for-alertmanager-notifications.md) | What a Slack incoming webhook is and how Alertmanager's `slack_configs` receiver uses one; setup via `api.slack.com/apps`; wiring through the `alertmanager-config-render` sidecar; the #122 case (a `403 invalid_token` root-caused to a zero-vs-capital-O transcription error, plus a literal-`\n` template formatting bug found during live verification) | infrastructure | 2026-07-13 |
| [software-testing-techniques-functional-and-non-functional.md](software-testing-techniques-functional-and-non-functional.md) | Functional (Positive/Negative/EP/BVA/Decision Table/State Transition/Use Case/Error Guessing) and non-functional (Performance/Load/Stress/Scalability/Security/Reliability/Usability/Soak) test-design techniques, each mapped to a real BuildNest example — distinct from test *type* (testing.md) and *scenario identification* (development-workflow.md); includes the Decision Table `findRelatedProducts` should have started from (#84, PR #370) | quality-engineering | 2026-07-13 |
| [false-positives-in-static-analysis-and-security-tooling.md](false-positives-in-static-analysis-and-security-tooling.md) | False positive/negative confusion-matrix definition; why CodeQL/SpotBugs/CheckStyle/pre-commit hooks all produce them (syntactic pattern matching vs. real context); four recurring causes with real BuildNest instances; the #358 CSRF alert false-positive triage vs. the same session's real ReDoS findings as a counter-example; how to triage a finding instead of reflexively trusting or dismissing it | quality-engineering | 2026-07-14 |

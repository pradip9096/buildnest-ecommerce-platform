---
title: "Recurrence Escalation: Cross-Field Terminology and an Occurrence-Count Ladder for When the Same Process Gap Reappears After Being 'Fixed'"
category: documentation
tags: [amendment-mechanism, recurrence, escalation, capa, stateless-agent, audit-terminology, configuration-drift]
keywords: [repeat finding, operating effectiveness deficiency, configuration drift, reconciliation loop, organizational forgetting, latent condition, swiss cheese model, defect-class id, escalation ladder, sibling-precedent-scope]
objective: "When a process/amendment fix regresses (the same gap recurs after being fixed once), what's the established cross-field vocabulary for this, and what concrete mechanism closes the gap for a stateless AI agent governed by prose rule files rather than mechanically-enforced code?"
audience: "Maintainers of AI-agent process/rule files (or any prose-governed checklist, runbook, or amendment log) who've had a fix regress and want to know both what to call it and what to do differently the second time."
scope: both
source_conversations: ["Session 2026-07-14, follow-up to the #358 sibling-precedent-check fix (development-workflow.md Amendment #37 → #38) and the resulting recurrence-tracking mechanism"]
last_updated: 2026-07-14
confidence: high
evidence_strength: strong
related_articles: [closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md, ../learning/capa-corrective-and-preventive-action.md, ../learning/process-improvement-frameworks.md]
status: published
---

# Recurrence Escalation: Cross-Field Terminology and an Occurrence-Count Ladder for When the Same Process Gap Reappears After Being "Fixed"

## What Is It?

This article covers the specific case where a process/amendment fix **regresses** — the same
underlying gap reappears after already being corrected once — and two things that follow from it:
the vocabulary different fields use for this exact pattern, and a concrete escalation mechanism for
closing it when the artifact being fixed is a prose rule file governing a **stateless** agent (no
persistent memory across sessions beyond the file itself).

This is a narrow extension of ground already covered elsewhere in this knowledge base, not a
restatement of it. [Closed-Loop Feedback and Amendment Mechanisms](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md)
already covers *why* a process document needs a feedback loop at all (control theory, the
trigger/correction/log amendment pattern, ratchets). [CAPA](../learning/capa-corrective-and-preventive-action.md)'s
"Beyond one incident" section and [Process Improvement](../learning/process-improvement-frameworks.md)'s
"Evidence It's Recurring" template field already cover *escalating fix scope* — instance fix →
class fix → system fix. What none of them cover: the cross-field names for "a fix that didn't
hold," and an escalation ladder for *enforcement mechanism strength* (not fix scope) keyed to how
many times the same tagged gap has recurred.

## Why It Matters

A rule stated only in prose has exactly one enforcement mechanism: an agent reading the document
and choosing to apply it. A first fix demonstrates the rule *can* be followed; it says nothing
about whether it *will* be, unprompted, in a future session that starts with no memory of the fix
ever happening. Confirmed directly in this repo: `development-workflow.md`'s sibling-consistency
check was fixed in prose at Amendment #37 (after being missed on #320), then missed again on #358
— a structurally different manifestation of the identical underlying gap. Nothing flagged the
second miss as a *repeat* until an explicitly-requested retrospective happened to compare both
entries side by side. The same failure shape had already been named once before, for an unrelated
check (`feedback_reasoning_walkthrough.md`'s 4-signal test going silently unapplied across multiple
sessions) — meaning this is a recurring failure *class*, not a one-off.

## How It Works

### Cross-field terminology for "a fix that didn't hold"

Five established fields have already named this pattern, each from a different angle. None of
these currently appear in this KB's existing amendment/CAPA/process-improvement articles — they're
genuinely new vocabulary, not synonyms already covered:

| Field | Term | What it names |
|---|---|---|
| Internal audit / SOC 2 / ISO 27001 / COSO | **Repeat finding** (or recurring deficiency) | A control gap identified again in a later audit cycle after remediation was already tracked as closed — treated as a strictly worse signal than a first-time finding |
| Internal audit / COSO | **Operating effectiveness deficiency** (vs. design deficiency) | The control is correctly *designed*; it just isn't consistently *executed*. Distinct from a design deficiency, where the control itself is wrong. Our sibling-consistency check was an operating-effectiveness deficiency: the rule was fine after #37, execution wasn't reliable |
| DevOps / SRE / GitOps | **Configuration drift**, corrected by a **reconciliation loop** | A system's actual state gradually diverging from its declared/intended state absent active reconciliation. Terraform/Ansible/ArgoCD are built around continuously diffing actual vs. desired state rather than trusting a one-time apply to hold forever |
| Organizational behavior / knowledge management | **Organizational forgetting** | Documented decay of operational knowledge over time absent active reinforcement, especially under discontinuity/turnover — a stateless agent is, functionally, 100% turnover every session |
| Safety science / human factors (aviation, healthcare) | **Latent condition** (James Reason, Swiss Cheese Model) | Distinguished from an *active failure* (the specific visible slip). A latent condition is the underlying systemic weakness that keeps producing slips; fixing only the active failure (reword the rule again) leaves the latent condition — "this rule has no forcing function" — untouched |

### The escalation ladder

CAPA already escalates *fix scope* once a defect class is shown to recur (instance → corrective
action → preventive action → process improvement — see [CAPA's "Beyond one incident"
section](../learning/capa-corrective-and-preventive-action.md#7-effectiveness-verification)). This
ladder escalates something different: **enforcement mechanism strength**, keyed to how many times
the *same tagged gap* has recurred, for the specific case where the artifact is a prose rule file
with no external forcing function. A repeat is evidence that the previous enforcement tier already
failed to survive one full stateless-session reset — the response has to get structurally stronger,
not just be restated more clearly.

| Occurrence (same defect-class ID) | Required response |
|---|---|
| 1st | Normal prose fix — edit the rule, log the change. |
| 2nd | Prose alone already failed once — seed the check as its own explicit task-list line item (structurally present, not optionally recalled). |
| 3rd | Task-list seeding still depends on the agent choosing to seed it — require pasting the actual output of a concrete verification command as a precondition, so the check leaves visible evidence rather than trust. |
| 4th+, or judged high-stakes on 1st recurrence | Build a real mechanically-enforced hook (in this repo's tooling: a Claude Code `PreToolUse` hook) — prose recall has failed repeatedly at every softer tier. |

Recurrence only becomes checkable at all if occurrences of the same underlying gap are tagged with
a shared, stable identifier — otherwise "has this happened before" depends on someone manually
noticing, the same problem this whole mechanism exists to remove.

## When to Use It

Reach for this specifically when a process/rule-file fix **regresses** — not merely when a new gap
is found for the first time (that's ordinary CAPA/amendment territory, already covered elsewhere).
The trigger is: a defect-class tag that was already used once shows up again on an unrelated
occasion.

## Examples

**Worked example (BuildNest, real, strong evidence):** `development-workflow.md`'s
`solution-options-adr` step's sibling-consistency check was fixed at **Amendment #37** after #320
picked a CI-blocking mechanism unilaterally, without checking how prior formal siblings (#317/#318/
#354) had already handled the same class of decision. It broke again at **#358**, for a related but
distinct reason: the missed precedent (#350) was never formally linked as a sibling at all, so the
existing check's *formal-linkage* trigger condition didn't fire. **Amendment #38** fixed the check's
scope (decision-class similarity, not just formal linkage) and retroactively tagged both entries
`[defect-class: sibling-precedent-scope]` — the 2nd occurrence of that tag, which per the ladder
above means the check must now be seeded as its own explicit `TaskCreate` item for any future issue
carrying a real architectural decision, not left as prose alone.

## Synthesis

A process document that's been fixed once is not thereby durable — durability has to be
demonstrated by surviving a fresh, stateless read, and a single fix proves only that the rule *can*
be followed, not that it *will* be. Five different fields have independently converged on naming
this exact gap (repeat finding, configuration drift, organizational forgetting, latent condition),
which is itself a signal that it's a structural property of any system relying on a rule stated
once and trusted to hold, not a quirk specific to AI agents or to this repo. The escalation ladder
above is the concrete response: treat a recurrence as proof the previous enforcement tier failed,
and respond with a *structurally* stronger mechanism, the same way this project already ratchets
code-quality gates (CheckStyle/PMD's baseline+ratchet, SpotBugs's severity threshold) rather than
trusting a developer's memory not to regress — applied here to the project's own process compliance
instead of its source code.

## Quick Reference

| Question | Answer |
|---|---|
| What's the audit/compliance term for a fix that didn't hold? | Repeat finding / recurring deficiency; more precisely an operating-effectiveness deficiency if the control's design was fine |
| What's the DevOps term for state silently diverging from what was declared? | Configuration drift, corrected by a reconciliation loop |
| What's the organizational-behavior term for knowledge decaying without reinforcement? | Organizational forgetting |
| What's the safety-science term for the systemic weakness beneath a visible failure? | Latent condition (Swiss Cheese Model) |
| How is this different from CAPA's own recurrence handling? | CAPA escalates fix *scope* (instance → class → system); this escalates enforcement *mechanism strength*, keyed to occurrence count of the same tagged gap |
| What triggers the next rung of the ladder? | The same defect-class ID appearing a second (or further) time — not any new, unrelated gap |

## References

- The Institute of Internal Auditors — repeat finding / recurring deficiency terminology in audit standards; COSO's design-vs-operating-effectiveness distinction for internal controls.
- HashiCorp / Terraform, Ansible, ArgoCD documentation — configuration drift and reconciliation-loop concepts in infrastructure-as-code and GitOps tooling.
- Reason, J. (1990). *Human Error*. Cambridge University Press — origin of the Swiss Cheese Model and the active-failure/latent-condition distinction, from aviation and healthcare safety literature.
- [CAPA – Corrective and Preventive Action](../learning/capa-corrective-and-preventive-action.md) — the fix-scope escalation (corrective → preventive → process improvement) this article's enforcement-strength ladder runs alongside, not in place of.

## Related Articles

- [Closed-Loop Feedback and Amendment Mechanisms for Process Documents](closed-loop-feedback-and-amendment-mechanisms-for-process-documents.md) — the base amendment mechanism (trigger/correction/log) this article assumes and extends specifically for the regression case
- [CAPA – Corrective and Preventive Action](../learning/capa-corrective-and-preventive-action.md) — fix-scope escalation and the "Beyond one incident" recurrence-at-category-level trigger this article's ladder complements
- [Process Improvement Frameworks](../learning/process-improvement-frameworks.md) — the "Evidence It's Recurring" template field this article's defect-class-ID tagging makes checkable rather than anecdotal

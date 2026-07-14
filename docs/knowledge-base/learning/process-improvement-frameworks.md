# Process Improvement

This article is written for a software engineering audience. Four of the five frameworks below (Six Sigma, Lean, CMMI, Kaizen) originate in manufacturing and quality management, not software — this document adapts them rather than inventing new ones. **It's conceptual orientation, not certification or appraisal preparation** — Six Sigma Black Belt certification and CMMI maturity-level appraisals are real, formal, often-audited processes with their own training paths; reading this article doesn't substitute for either.

> **Relationship to CAPA:** Process Improvement is the broader, ongoing discipline that CAPA feeds into — a single CAPA cycle fixes one incident and prevents its recurrence; Process Improvement asks whether the *system* that produced that incident (and others like it) needs to change. See [CAPA – Corrective and Preventive Action](./capa-corrective-and-preventive-action.md) for the incident-level, 9-step lifecycle this builds on — that article's Step 9 (Process Improvement) is the entry point into everything below; this file zooms into what happens inside that one step.

**Quick reference — at a glance:**
- **The 8-step lifecycle:** Identify Opportunity → Analyze Current Process → Identify Gaps/Root Causes → Design Improvement → Implement Change → Verify Effectiveness → Standardize → Continuously Improve
- **Five frameworks, roughly lightest to heaviest:** PDCA (any change, any scale) → Kaizen (team culture of small fixes) → Lean (workflow/waste) → Six Sigma (statistical defect reduction, needs volume) → CMMI (org-wide maturity assessment)
- **The core risk this article keeps returning to:** applying a framework heavier than the problem needs, or applying one whose *type* doesn't fit the context at all (see "Scaling" and the frameworks table below)

**Process Improvement** is a structured approach for analyzing an existing process, identifying weaknesses or opportunities, and making changes to improve **quality, efficiency, reliability, consistency, or performance**.

In simple terms:

> **Process Improvement = Learn from experience → improve the way work is done → prevent future problems**

It focuses on improving the **system that produced the result**, not only fixing one individual issue.

## Scaling the Process to the Size of the Gap

Not every recurring annoyance needs a formal maturity program. The same proportionality principle from the CAPA article applies here: a two-person team noticing they keep forgetting the same checklist item needs a five-minute conversation and a checklist edit, not a CMMI appraisal. Reach for the heavier frameworks below (Six Sigma, CMMI) when the problem is high-volume, high-stakes, or has resisted lighter fixes; reach for the lighter ones (a Kaizen-style small change, or just PDCA run once) for everything else.

**Size isn't the only mismatch to watch for — context-fit matters independently.** Six Sigma's statistical methods (DMAIC, control charts, defect-rate analysis) need enough incident *volume* to be statistically meaningful — a 5-person team with 3 incidents a year can't apply Six Sigma meaningfully no matter how serious those incidents feel; there just isn't enough data. CMMI is an *organizational* appraisal framework — one team adopting "CMMI practices" informally isn't the same thing as an org going through appraisal, and doing the former while calling it the latter overstates what actually happened. Both are context-fit problems, not just scale problems — a "big enough" problem still doesn't fit either framework if the surrounding structure (data volume, organizational scope) isn't there.

## Example

Problem:
> A developer repeatedly forgets to update documentation after code changes.

A simple fix:
> Update the missing documentation.

Process improvement:
> Add a Definition of Done checklist requiring documentation review before closing every GitHub issue.

This is a small, low-stakes example — deliberately so, since it doesn't need Six Sigma or CMMI (per the scaling note above). A concrete case where a *heavier* process actually changed as a direct result of an incident — not just a doc reminder — is the [CAPA article's own worked example](./capa-corrective-and-preventive-action.md#worked-example-from-this-project-sec-15-csrf-cookie-bug): a CSRF/cookie defect led to adding "create a task list before starting non-trivial implementation, seeded with DoD closure steps" to the assisting agent's global Definition-of-Done checklist, specifically because several closure steps had nearly been skipped after the technical fix landed. Same shape as the documentation example above — a recurring gap led to a standing rule — just a higher-stakes trigger.

## General Process Improvement Lifecycle

```text
1. Identify Opportunity
        ↓
2. Analyze Current Process
        ↓
3. Identify Gaps / Root Causes
        ↓
4. Design Improvement
        ↓
5. Implement Change
        ↓
6. Verify Effectiveness
        ↓
7. Standardize the Improved Process
        ↓
8. Continuously Improve
```

Applied to the documentation example above:

1. **Identify Opportunity:** a reviewer notices, for the third time, that a merged PR shipped without a docs update.
2. **Analyze Current Process:** the PR template and Definition of Done don't mention documentation at all.
3. **Identify Gaps / Root Causes:** the gap isn't any one developer forgetting — it's that nothing in the process *asks* the question.
4. **Design Improvement:** add an explicit "Documentation updated (or N/A)" line to the Definition of Done checklist.
5. **Implement Change:** update the checklist template.
6. **Verify Effectiveness:** confirm the next several PRs actually check that box, and that at least one would have shipped without a docs update if the check hadn't caught it.
7. **Standardize the Improved Process:** the checklist item becomes mandatory for every PR going forward, not just a one-time reminder to the developer who was reprimanded.
8. **Continuously Improve:** revisit the checklist itself periodically — is this item still needed, is it being rubber-stamped rather than genuinely checked, does it need to expand to cover API docs specifically?

**Beyond one change — is the program itself working?** Steps 6 and 8 above verify *this one* change. A separate, harder question is whether process improvement *as a practice* is actually working across the team over time — e.g., what fraction of retro/postmortem action items actually get implemented (not just written down), or whether a lagging outcome metric (incident rate, recurring-defect rate) is trending down across many cycles of this lifecycle, not just holding steady after any one of them. A team that faithfully runs all 8 steps every time but never checks this aggregate question can still be accumulating checklist items that nobody follows, one satisfying-feeling cycle at a time.

## Common Academic/Industry Frameworks

Each of these is a large enough topic to deserve its own dedicated article if you need real depth — the one line each gets here is orientation (which one to reach for and why), not sufficient grounding to actually apply any of them.

| Framework | Focus | Origin | Best fit | Known critique |
|---|---|---|---|---|
| **Plan–Do–Check–Act (PDCA)** | Continuous small improvements, run as a repeatable cycle | Walter Shewhart's application of the scientific method to quality control, later popularized by W. Edwards Deming (see [Learn Lean Sigma: W. Edwards Deming's PDCA](https://www.learnleansigma.com/lean_visionaries/w-edwards-deming-pdca/)) | Any single process change, at any scale — the lightest-weight, most general-purpose option; Steps 1–8 above are effectively PDCA expanded | Vague enough to mean almost anything if not paired with a concrete method for the "Check" step — easy to claim you're "doing PDCA" without ever really verifying effectiveness |
| **Kaizen** | Ongoing incremental improvement culture, team-driven | Japanese ("kai" = change, "zen" = good); central to Toyota's post-WWII manufacturing culture (see [Learn Lean Sigma](https://www.learnleansigma.com/lean_visionaries/w-edwards-deming-pdca/)) | Building a team habit of noticing and fixing small friction points continuously, not a one-off fix | Only works as a genuine bottom-up culture — imposed top-down as a mandate, it tends to produce the exact "process improvement theater" described below |
| **Six Sigma** | Reducing defects and variation using statistical methods | Introduced by engineer Bill Smith at Motorola in 1986 (see [Six Sigma — Wikipedia](https://en.wikipedia.org/wiki/Six_Sigma)) | High-volume processes where defect rate is measurable and worth reducing statistically — usually overkill below a certain volume | The structured DMAIC phases and certification hierarchy (Green/Black Belt) can add real bureaucratic overhead that slows down fast-moving teams — a frequent criticism from lean-startup/agile practitioners |
| **Lean** | Removing waste, improving flow | Derived from the Toyota Production System | Workflows with excess handoffs, waiting, or rework — optimizing the *shape* of a process, not just its defect rate | "Removing waste" can be used to justify cutting anything inconvenient (including genuine slack/buffer that absorbs variability) if not paired with judgment about what's actually waste |
| **Capability Maturity Model Integration (CMMI)** | Improving organizational process maturity | Developed by the Software Engineering Institute at Carnegie Mellon University | Organization-wide process assessment/certification — the heaviest option, rarely justified below a large-team or regulated-industry scale | Widely criticized in software engineering specifically for measuring process documentation/maturity rather than actual product quality — an organization can reach a high maturity level and still ship poor software |

## Ownership: Who Actually Owns a Process Change?

This varies far more than for a single CAPA incident (see the CAPA article's own ownership table), because the scope of "the process" itself varies wildly:

| Scope of change | Typical owner |
|---|---|
| One team's checklist/template item | The team's tech lead or whoever runs its retros — doesn't need approval beyond the team |
| A cross-team standard (e.g., a shared PR template, a shared CI gate) | Whoever owns that shared tooling/convention, with buy-in from the affected teams — a unilateral change here is itself a process-improvement-theater risk (see below) |
| An org-wide framework adoption (Six Sigma program, CMMI appraisal) | A dedicated quality/process function, or engineering leadership — never a single team acting alone, since the whole point of these frameworks is organizational scope |

## A Risk: Process Improvement Theater

The direct parallel to the CAPA article's "preventive action theater" pitfall: a process change mandated top-down, without input from the people who actually do the work, tends to produce **compliance without belief** — people check the new box because they're told to, not because they understand why it exists, and the underlying problem it was meant to address often continues unaddressed underneath the appearance of a fix. This is precisely why Kaizen's own definition (see the table above) insists on being team-driven and bottom-up rather than imposed — it's a deliberate response to this failure mode, not an arbitrary stylistic choice. Before rolling out a process change beyond one team, it's worth asking whether the people it affects were actually involved in designing it, not just informed of it afterward.

## How This Fits Into the CAPA Lifecycle

The CAPA article's own flow, for reference (see that article for the full worked explanation of each step):

```text
1. Problem Detection
        ↓
2. Assessment + Containment
        ↓
3. Investigation
        ↓
4. Root Cause Analysis
        ↓
5. Corrective Action
        ↓
6. Preventive Action
        ↓
7. Effectiveness Verification
        ↓
8. Lessons Learned
        ↓
9. Process Improvement  ←── this article picks up from here
```

Step 9 (Process Improvement) is where this file's own 8-step lifecycle (Identify Opportunity → ... → Continuously Improve) actually runs — it isn't a separate, competing flow; it's what "Process Improvement" *means* when unpacked in detail.

## Examples in Software Engineering

- Creating reusable checklists
- Updating coding standards
- Adding automated tests
- Improving CI/CD quality gates
- Updating templates and documentation
- Improving review processes
- Creating better workflows
- Adding monitoring/feedback loops

## Mental Model

> **Correction fixes the output.**
> **Corrective action fixes the cause.**
> **Preventive action prevents recurrence.**
> **Process improvement upgrades the system.**

## Process Change Proposal Template

A minimal, copy-pasteable structure for Step 4 (Design Improvement) through Step 7 (Standardize) — fill in each field rather than proposing a change verbally in a meeting and having it evaporate afterward:

```text
## Problem
[What keeps happening — the recurring gap, not one instance of it]

## Evidence It's Recurring
[How many times, over what period — "this is the third time" is evidence; "this feels like it keeps happening" is not]

## Proposed Change
[The specific process/checklist/template/tooling change — not "be more careful"]

## Scope
[One team / cross-team / org-wide — see the Ownership table above; this determines who needs to sign off]

## Owner
[Who implements it, and who owns it going forward]

## How We'll Know It Worked
[The specific, checkable signal — e.g., "the next 5 PRs all have this checkbox checked and at least one would have shipped wrong without it" — not "things should get better"]

## Review Date
[When to revisit whether this change is still needed / still working — Step 8, Continuously Improve]
```

## Glossary

See the [CAPA article's glossary](./capa-corrective-and-preventive-action.md#glossary) for acronyms shared across both files (PDCA, CMMI, DMAIC, etc.).

## Related: When the Fix Itself Regresses

The "Evidence It's Recurring" field above assumes a *new* recurring gap being surfaced for the
first time. A related but distinct case is when a gap that was **already fixed once** (a checklist
item added, a rule reworded) reappears later — that's not a fresh process-improvement cycle, it's a
sign the previous fix's enforcement mechanism didn't hold. See [Recurrence Escalation](../project/recurrence-escalation-for-process-and-amendment-mechanisms.md)
for the cross-field terminology (repeat finding, configuration drift, latent condition) and an
occurrence-count ladder for escalating enforcement strength in that specific case.

## Sources

- [Learn Lean Sigma: W. Edwards Deming's PDCA](https://www.learnleansigma.com/lean_visionaries/w-edwards-deming-pdca/)
- [Six Sigma — Wikipedia](https://en.wikipedia.org/wiki/Six_Sigma)
- [PDCA vs PDSA: Deming's Wheel, and Why Toyota's Culture Makes PDCA Work](https://www.geneo.co.uk/pdca-vs-pdsa-deming-toyota-culture/)

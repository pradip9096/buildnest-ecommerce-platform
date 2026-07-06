# CAPA – Corrective and Preventive Action (executed through PDCA or 8D methodology)

This article is written for a software engineering audience, borrowing vocabulary from manufacturing and regulated-industry quality management — the terms below (Correction, Corrective Action, Preventive Action, CAPA) originate outside software, and this document adapts them rather than inventing new ones. **It is educational, not compliance guidance** — if you work in an actually FDA-regulated context (medical devices, pharma), your organization's formal CAPA procedure and QA sign-off requirements govern, not this document.

Questions like *"What preventive measures should we put in place to ensure this doesn't happen again?"* have a specific place in a broader problem-solving lifecycle — several earlier activities are typically performed before asking them, and this article walks through the full flow those questions sit inside. (See "Classification of the Question" below for exactly where this question lands.)

**Quick reference — the 9 steps at a glance** (each gets its own section under "Typical Flow" below):

1. **Problem Detection** — what happened?
2. **Assessment + Containment** — how serious is it, and does it need stabilizing right now?
3. **Investigation** — why did it happen, mechanically?
4. **Root Cause Analysis** — what's the *underlying* cause, not just the symptom?
5. **Corrective Action** — fix this specific instance
6. **Preventive Action** — stop the *class* of problem from recurring elsewhere
7. **Effectiveness Verification** — did the fix actually work?
8. **Lessons Learned** — what's worth remembering, written down durably?
9. **Process Improvement** — does the system itself need to change?

One running example (a missing-input-validation bug) is used throughout for continuity — the same 9 steps apply just as well to a performance regression, a security vulnerability, or a failed deployment; the example is narrow for teaching purposes, not because CAPA is validation-specific.

## Correction vs. Corrective Action vs. Preventive Action

These three terms are often used interchangeably in casual conversation but are formally distinct in quality management (ISO 9001 §10.2 draws exactly this line — see [ISO 9001 Clause 10.2: Nonconformity and Corrective Action](https://www.thecoresolution.com/clause-10-2-iso-90012015-explained)):

| Term | Definition | Example |
|---|---|---|
| **Correction** | Immediate action to eliminate the *detected* nonconformity itself — a fix, with no guarantee the underlying cause is addressed. | Manually restart the crashed service; patch the one input that triggered the 500 error. |
| **Corrective Action** | Action to eliminate the *root cause* of a nonconformity that already occurred, so *that specific defect class* doesn't recur. | Add validation logic to the endpoint that crashed, plus a test covering the missing case. |
| **Preventive Action** | Action to address a *potential* nonconformity — a failure mode that **hasn't happened yet** but is plausible given what was just learned. | Audit every other endpoint in the service for the same missing-validation pattern and add a lint rule / contract test that fails the build if any endpoint skips input validation. |

**Important accuracy note:** ISO 9001:2015 removed "preventive action" as a standalone clause, replacing it with *risk-based thinking* woven through the whole standard — the reasoning being that a genuinely risk-based Quality Management System (QMS) makes prevention continuous rather than a discrete after-the-fact step (see [ISO 9001:2015 Risk-Based Thinking vs. Preventive Action](https://advisera.com/9001academy/knowledgebase/risk-based-thinking-replacing-preventive-action-in-iso-90012015-the-benefits/)). The term **CAPA** — with corrective *and* preventive action as two explicit, separate steps — remains the standard, formally required terminology in FDA-regulated industries: 21 CFR §820.100 mandates medical device manufacturers maintain CAPA procedures with both elements (as of Feb 2026, harmonized with ISO 13485:2016 — see [21 CFR § 820.100](https://www.law.cornell.edu/cfr/text/21/820.100)). Software teams borrowing "CAPA" from these regulated disciplines should know the two-step "corrective + preventive" framing is a deliberate choice, not the only current industry default.

## Scaling the Process to the Size of the Problem

The full 9-step flow below is for a genuine incident — something that broke, shipped wrong, or recurred. It is deliberately overkill for a one-line typo fix or a cosmetic (P3/Sev4) issue: forcing a 5-Whys and an effectiveness-verification window onto every trivial change trains people to skip the process altogether, including on the incidents that actually need it. Scale rigor to the size and severity of the problem — this is the same proportionality principle behind the Claude Code agent's own global `definition-of-done.md` checklist (`~/.claude/rules/definition-of-done.md` — a cross-project config file, not part of this repository), which states explicitly: *"nothing in scope should be skipped silently,"* not *"every step must be maximal every time."*

**When CAPA is the wrong framework entirely, not just over-scaled:** CAPA assumes a *nonconformity* — something that deviated from an expected, correct behavior. It doesn't fit a failed experiment or spike (nothing was "wrong," you learned something and moved on), a reversed product/business decision (a judgment call, not a defect), or an intentional trade-off that had a known downside from the start. Forcing the CAPA vocabulary onto those cases is a category error — the fix isn't "use it more lightly," it's "don't use it at all" for that kind of situation.

## How CAPA, PDCA, and 8D Map to Each Other

The three frameworks below aren't independent alternatives — they're different levels of granularity over the same underlying flow. This table is the explicit mapping the rest of this document assumes:

| This article's step | CAPA | PDCA | 8D |
|---|---|---|---|
| 1. Problem Detection | Problem Identification | Plan (start) | D2: Describe problem |
| 2. Assessment + Containment | Impact Assessment | Plan | D1: Form team · D3: Containment |
| 3–4. Investigation + RCA | Investigation, Root Cause Analysis | Plan | D4: Root cause analysis |
| 5. Corrective Action | Corrective Action | Do | D5: Permanent corrective action · D6: Implement |
| 6. Preventive Action | Preventive Action | Do | D7: Prevent recurrence |
| 7. Effectiveness Verification | Effectiveness Verification | Check | (implicit in D6/D7 sign-off) |
| 8. Lessons Learned | Continuous Improvement | Act | D8: Lessons learned |
| 9. Process Improvement | Continuous Improvement | Act | *(not distinguished from D8 in 8D)* |

PDCA is the coarsest (4 phases, general-purpose, not incident-specific); CAPA is the mid-level framing most natural for a single incident; 8D is the most granular and adds two steps PDCA/CAPA leave implicit — forming a team (D1) and containment (D3) as a distinct step rather than folding it into assessment. Neither CAPA's classic diagram nor 8D distinguish Lessons Learned from Process Improvement as two separate steps the way this article does — see the "Related Methodologies" section below, where the CAPA-specific diagram is deliberately drawn with a single closing "Continuous Improvement" box rather than the two-step split used in this article's own flow.

## Ownership: Who Does Each Step?

CAPA processes typically assign a distinct **CAPA owner** for the incident as a whole, separate from whoever writes the actual fix — specifically so the person who introduces the corrective/preventive action isn't also the one who signs off that it worked (Step 7). A typical split:

| Step | Typical owner |
|---|---|
| 1. Detection | Whoever notices it — on-call, monitoring alert, user report, any engineer |
| 2. Assessment/Containment | On-call or incident commander — decides severity and whether to contain immediately |
| 3–4. Investigation/RCA | Engineer(s) closest to the affected code, but not necessarily the original author |
| 5–6. Corrective/Preventive Action | Assigned implementer(s) — may or may not be the RCA author |
| 7. Effectiveness Verification | **CAPA owner or a reviewer distinct from the implementer** — the review, not just the fix, is the safeguard |
| 8. Lessons Learned | Whoever facilitates the postmortem — often the incident commander, not the implementer |
| 9. Process Improvement | Whoever owns the underlying process/standard being changed (e.g., a tech lead updating a checklist or template) — often not anyone who touched the original incident at all |

## A Note on Blameless Postmortems

CAPA's roots are in manufacturing and regulated-industry quality management, where a "root cause" can legitimately be a specific operator error. Software incident response has largely adopted a different framing — the **blameless postmortem** (popularized by Google's SRE practice and Etsy's engineering culture): RCA is conducted on the assumption that people acted reasonably given what they knew at the time, and the 5 Whys should terminate at a *systemic* cause (missing test, absent checklist item, unclear ownership) rather than "engineer X made a mistake." This isn't just a tone preference — a blame-focused RCA actively produces worse preventive actions, because "be more careful next time" isn't a preventive action at all (it can't be verified in Step 7), while "add a lint rule that makes the mistake impossible to ship" can be.

## Common Pitfalls

- **RCA-as-checkbox-exercise** — naming *a* cause and stopping, without evidence that it's the actual root cause (e.g., stopping at "missing validation" instead of continuing to "why wasn't validation required by our process," as the worked example below does)
- **Preventive action theater** — writing a plausible-sounding bullet list (tests! docs! monitoring!) with no owner, no deadline, and no Step 7 follow-up, so it never actually happens
- **Skipping containment when it's actually needed** — going straight to RCA on a live P0/P1 while the system is still actively broken, instead of stabilizing first
- **Treating Effectiveness Verification as a formality** — closing the incident the moment the fix merges, without confirming the specific failing scenario now behaves correctly or that the preventive control (test, gate, alert) actually fires when it should

## Typical Flow

### 1. Problem Detection / Identification
**Question:** *What happened?*

Activities:
- Detect issue, defect, failure, incident, or improvement opportunity
- Capture evidence (logs, screenshots, reports, user feedback)
- Define the problem clearly

Example: *"The API returns a 500 error when invalid input is provided."*

---

### 2. Problem Assessment / Triage (a.k.a. Impact Assessment)
**Question:** *How serious is it?*

Activities:
- Assess impact
- Identify affected users/components
- Determine severity and priority — using a concrete scale, not just a gut call. A common one:

  | Level | Meaning | Response time |
  |---|---|---|
  | **P0 / Sev1** | Production down, or data loss/corruption in progress | Immediate, all-hands |
  | **P1 / Sev2** | Major feature broken, no workaround, affects many users | Same day |
  | **P2 / Sev3** | Feature degraded, workaround exists | Next sprint |
  | **P3 / Sev4** | Cosmetic, edge case, low-traffic path | Backlog |

- Decide whether immediate **containment** is needed before root cause work starts — e.g., roll back a bad deploy, disable a broken feature flag, or add a temporary input filter at the edge (a *correction*, not yet a *corrective action*) so the system is safe while the real fix is investigated. This is the same idea as 8D's D3 (see below); it belongs here in the flow, not only in the 8D-specific section.

Example: *"Does this break production? Is it blocking users?"* → classified P1: feature broken (order submission fails on certain inputs), no workaround, affects all users hitting that input shape.

---

### 3. Investigation / Diagnosis
**Question:** *Why did it happen?*

Activities:
- Reproduce the problem
- Analyze code/configuration/process
- Review logs and history
- Compare expected vs actual behavior

Example: *"Validation logic was missing before database insertion."*

---

### 4. Root Cause Analysis (RCA)
**Question:** *What is the real underlying cause?*

Activities:
- Perform 5 Whys analysis
- Identify technical, process, or knowledge gaps
- Separate symptoms from causes

Example — worked 5 Whys for the running scenario:
1. Why did the API return a 500? → An unhandled exception was thrown during database insertion.
2. Why was the exception unhandled? → The insert path has no validation layer before it reaches the database.
3. Why is there no validation layer? → The endpoint was written to a "happy path" spec; invalid-input handling wasn't in the acceptance criteria.
4. Why wasn't invalid-input handling in the acceptance criteria? → The team's endpoint checklist/template doesn't require it.
5. Why doesn't the checklist require it? → No prior incident had forced the team to add it. *(Root cause: process gap, not just a code gap.)*

> Symptom: Application crashed
> Root cause: Missing validation rule *and* no template/checklist step that would have caught its absence — a process gap, not only a code gap.

---

### 5. Corrective Action Planning
**Question:** *How do we fix the current problem?*

Activities:
- Define implementation changes
- Create fix checklist
- Assign ownership
- Estimate effort

Example: *Add validation logic to the specific endpoint, plus a regression test asserting invalid input returns 400, not 500.*

---

### 6. Preventive Action Planning
**Question:** *What preventive measures should we put in place to ensure this doesn't happen again?*

Per the definitions above, this step is about the *class* of problem, not the one instance already fixed in Step 5 — the instance is corrective; this step is preventive.

Activities, worked against the same running scenario (not a generic list):
- **Add automated tests** — not just for this endpoint, but a shared test suite / contract test applied to *every* endpoint asserting invalid input is rejected with 400, so the same gap can't reappear elsewhere in the codebase undetected.
- **Update documentation** — add "input validation required" to the team's endpoint-creation checklist/template referenced in the RCA above, closing the process gap directly, not just the code gap.
- **Improve standards/checklists** — require the checklist item to be checked off in PR review before merge.
- **Add quality gates** — add a static-analysis or CI rule that flags a new DB-insert call path with no preceding validation call.
- **Improve monitoring** — alert on 500-rate spikes so a *different* future validation gap is caught by monitoring even if it slips through the other gates.

---

### 7. Effectiveness Verification
**Question:** *Did the corrective and preventive actions actually work?*

This step is often named in CAPA diagrams but rarely explained. Concretely, it means:
- Re-run the original failing scenario (the exact invalid input that caused the 500) and confirm it now returns 400
- Monitor the 500-error-rate metric for a defined window (e.g., 2 weeks) post-deploy and confirm no recurrence
- Confirm the new contract test actually fails if the validation is removed (a test that can't fail isn't a valid check — verify the check, not just its presence)
- Confirm the checklist/template change is actually being followed on the next 1–2 new endpoints built, not just merged into a doc nobody re-reads

If verification fails (the problem recurs, or the fix has an unintended side effect), the cycle returns to Step 3/4 — effectiveness verification is a checkpoint, not a rubber stamp.

**Beyond one incident — tracking recurrence at the category level:** everything above verifies *this* fix worked *once*. A separate, longer-horizon question is whether the same *category* of defect (missing input validation, in the running example) keeps showing up across *different* incidents despite individual fixes each verifying clean — that pattern means Step 6/9 isn't working at the system level even though every individual Step 7 passed. This is the trigger that should escalate a recurring category from "keep patching instances" to Step 9 (Process Improvement) — see the [Process Improvement Frameworks](./process-improvement-frameworks.md) article for what that escalation looks like in practice.

---

### 8. Lessons Learned
**Question:** *What should we remember from this, and where does it live?*

Activities:
- Write up the RCA — including any wrong hypotheses rejected along the way (see the worked project example below) — somewhere durable, not just stated in a closing comment or chat message
- Distinguish *reusable* lessons (patterns likely to recur elsewhere) from incident-specific detail that doesn't need to outlive the ticket
- If nothing clears the bar for a genuinely new lesson, say so explicitly rather than silently skipping the step — silence is indistinguishable from having forgotten to check

Example: *documented that "input validation missing" recurred because the endpoint template didn't require it — the reusable lesson is about the template gap, not about this one endpoint.*

---

### 9. Process Improvement
**Question:** *Does the system that produced this problem need to change, not just this one instance of it?*

This is the broadest step and the one most likely to be skipped, since Steps 1–8 already feel like "done." It's a distinct topic with its own frameworks (PDCA, Kaizen, Six Sigma, Lean, CMMI) — covered in **[Process Improvement Frameworks](./process-improvement-frameworks.md)** rather than duplicated here.

---

## Complete Lifecycle

```text
Problem Detection
        ↓
Problem Assessment (Impact Assessment) + Containment if needed
        ↓
Investigation
        ↓
Root Cause Analysis
        ↓
Corrective Action
        ↓
Preventive Action
        ↓
Effectiveness Verification
        ↓
Lessons Learned
        ↓
Process Improvement
```

This is essentially the same thinking model used in **CAPA, incident management, software postmortems, ISO-style continual improvement, and mature SDLC processes** — the diagram above reconciles the "Problem Assessment" vs. "Impact Assessment" naming and the containment step called out separately in the CAPA/8D sections below, so it can be read as one consistent flow rather than several slightly different ones.

## Related Methodologies

The formal academic/process names depend on the discipline. The flow above is most commonly associated with these established methodologies. *(Note: this section's diagrams necessarily repeat some of what "How CAPA, PDCA, and 8D Map to Each Other" already covers in table form above — that table is the detailed, step-by-step mapping; the diagrams below are the quick-reference version of each framework on its own, useful if you only want to recall one framework's shape without cross-referencing this article's specific step numbers.)*

### Corrective and Preventive Action (CAPA)
Most accurate match for the question set above. Used in quality management, engineering, regulated industries, and process improvement — see the accuracy note above on its current standing relative to ISO 9001:2015 vs. FDA-regulated contexts.

This is the classic CAPA diagram as usually drawn, ending in one "Continuous Improvement" box (matching the mapping table above) — this article's own 9-step flow deliberately splits that final box into Steps 8 and 9 for teaching purposes, since Lessons Learned (documenting what happened) and Process Improvement (changing the system) are different activities with different owners in practice, even though classic CAPA treats them as one closing phase.

```text
Problem Identification
        ↓
Impact Assessment (+ Containment)
        ↓
Investigation
        ↓
Root Cause Analysis
        ↓
Corrective Action
        ↓
Preventive Action
        ↓
Effectiveness Verification
        ↓
Continuous Improvement
```

### Root Cause Analysis (RCA)
Focuses mainly on discovering why a problem occurred. Common techniques:
- 5 Whys (worked example above) — simple and fast, but assumes a single linear causal chain. If a failure actually has multiple independent contributing causes (not just one deep chain), 5 Whys will follow whichever branch the investigator happens to ask about first and miss the others.
- Fishbone/Ishikawa diagram — categorizes candidate causes (People, Process, Tools, Environment, etc.) into branches to avoid fixating on the first plausible cause; prefer this over 5 Whys specifically when you suspect *multiple* contributing causes rather than one chain (e.g., an incident where a bad deploy, a missing alert, and an on-call gap all had to align for the outage to become severe)
- Fault tree analysis — works backward from the failure through a logic tree of necessary/sufficient prior conditions; more common in safety-critical/hardware contexts than typical web software incidents

### Plan–Do–Check–Act (PDCA)
Broader continuous improvement framework.

```text
Plan  → Identify problem + analyze cause + plan actions
Do    → Implement corrective/preventive actions
Check → Verify effectiveness
Act   → Standardize improvements
```

### Eight Disciplines Problem Solving (8D)
More structured engineering problem-solving method — note D3 (Containment) sits *before* root cause analysis, which is why the "Typical Flow" section above folds a containment decision into Step 2 rather than only mentioning it here.

```text
D1: Form team
D2: Describe problem
D3: Containment action
D4: Root cause analysis
D5: Permanent corrective action
D6: Implement solution
D7: Prevent recurrence
D8: Lessons learned
```

### Classification of the Question

For the exact question *"What preventive measures should we put in place to ensure this doesn't happen again?"* — the strongest academic/process classification is:

> **CAPA → Preventive Action phase**, supported by **RCA**, usually executed through **PDCA or 8D methodology**.

---

## Worked Example From This Project: SEC-15 CSRF Cookie Bug

The hypothetical 500-error scenario above is useful for a clean first pass, but a real incident from this repo's own history maps onto the same 9 steps and is worth having as a second reference point:

1. **Detection:** during manual browser verification of the SEC-15 cookie-auth migration, a password-change request unexpectedly returned 403.
2. **Assessment:** classified as blocking the SEC-15 change itself (no production impact yet, since the change wasn't deployed) — not a live incident, but a release-blocking defect. No containment needed; the change simply wasn't shipped yet.
3. **Investigation:** curl-based reproduction initially failed to reproduce it (sequential single-shot requests worked fine); the bug only reproduced under the app's real concurrent request pattern in a browser.
4. **Root Cause Analysis:** traced through two wrong hypotheses (a session-strategy misconfiguration, then an incompatible token-handler choice) before confirming the actual cause — a documented Spring Security defect ([GH-12141](https://github.com/spring-projects/spring-security/issues/12141)) interacting with this project's stateless, per-request JWT authentication.
5. **Corrective Action:** added `NonClearingCsrfTokenRepository` to wrap the token repository and stop the token from being cleared.
6. **Preventive Action:** documented the defect and fix rationale directly in the class's javadoc and in `spring-security.md`, so a future refactor doesn't remove the wrapper without understanding why it exists; a follow-up task was also opened to add a regression test asserting the CSRF cookie survives multiple consecutive authenticated requests (Step 6 isn't complete until that test exists — see Common Pitfalls above on "preventive action theater").
7. **Effectiveness Verification:** re-ran the exact failing request sequence via curl and confirmed the cookie persisted and the mutating request succeeded; then independently confirmed via live browser testing against the real app, not just the API layer.
8. **Lessons Learned:** saved as a durable memory file, distinct from the fix itself — documenting that sequential single-request testing gave false confidence, and that the underlying defect is a named, citable Spring Security issue rather than a one-off misconfiguration.
9. **Process Improvement:** the incident led to two standing process changes beyond the code fix itself — this knowledge-base article, and adding "create a task list before starting non-trivial implementation, seeded with DoD closure steps" to the assisting agent's global `definition-of-done.md` checklist, precisely because several closure steps (changelog, follow-up issues, commit) were nearly skipped after the technical fix landed.

The two wrong hypotheses in step 4 are themselves a small illustration of the "RCA-as-checkbox-exercise" pitfall — the first plausible-sounding cause was accepted, acted on, and only rejected after empirical re-verification showed it hadn't actually fixed anything. That's the RCA discipline working correctly (reject a fix that doesn't verify), not a failure of it.

---

## Postmortem Template

A minimal, copy-pasteable structure for Step 8 (Lessons Learned) — fill in each field rather than writing free-form prose, so nothing gets skipped silently:

```text
## Incident Summary
[One or two sentences: what broke, for whom, for how long]

## Timeline
- [timestamp] Detected via [monitoring alert / user report / manual testing / etc.]
- [timestamp] Contained via [rollback / feature flag / nothing needed]
- [timestamp] Root cause identified
- [timestamp] Fix deployed / merged
- [timestamp] Effectiveness verified

## Severity
[P0/P1/P2/P3 — see the severity table earlier in this article]

## Root Cause
[The actual underlying cause from the 5 Whys / Fishbone — not just the symptom]

## Corrective Action
[What fixed this specific instance]
- Owner: [name]
- Status: [done / in progress]

## Preventive Action
[What stops this *class* of problem elsewhere — not the same fix restated]
- Owner: [name]
- Deadline: [date]
- Status: [done / in progress — "in progress" forever is preventive action theater]

## Effectiveness Verification
- Verified on: [date]
- Method: [re-ran failing scenario / monitored metric for N days / confirmed test fails without the fix]

## Lessons Learned
[Reusable pattern only — not incident-specific detail that doesn't need to outlive this document]
```

## Glossary

Acronyms used across this article and [Process Improvement Frameworks](./process-improvement-frameworks.md), gathered in one place for quick lookup:

| Term | Meaning |
|---|---|
| **CAPA** | Corrective and Preventive Action |
| **RCA** | Root Cause Analysis |
| **PDCA** | Plan–Do–Check–Act |
| **8D** | Eight Disciplines Problem Solving |
| **QMS** | Quality Management System |
| **CMMI** | Capability Maturity Model Integration |
| **DMAIC** | Define–Measure–Analyze–Improve–Control (Six Sigma's core method, referenced in the Process Improvement article) |
| **ISO 9001** | The international standard for quality management systems — §10.2 specifically covers nonconformity and corrective action |

## Sources

- [ISO 9001 Clause 10.2: A Guide to Nonconformity and Corrective Actions](https://www.thecoresolution.com/clause-10-2-iso-90012015-explained)
- [ISO 9001:2015 Risk-Based Thinking vs. Preventive Action](https://advisera.com/9001academy/knowledgebase/risk-based-thinking-replacing-preventive-action-in-iso-90012015-the-benefits/)
- [21 CFR § 820.100 – Corrective and Preventive Action](https://www.law.cornell.edu/cfr/text/21/820.100) (Cornell Legal Information Institute)
- [Spring Security Issue #12141](https://github.com/spring-projects/spring-security/issues/12141) — the confirmed defect underlying the Worked Example above

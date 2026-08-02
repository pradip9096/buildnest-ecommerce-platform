---
title: Occurrence History Behind work-on-issue.md's Operative Instructions
category: tooling
tags: [work-on-issue, defect-class, occurrence-history, retrospective, extraction]
related_articles:
  - recurrence-escalation-for-process-and-amendment-mechanisms.md
  - sibling-precedent-consistency-check-for-repeated-decision-classes.md
  - pre-implementation-premise-verification-checklist.md
date: 2026-07-22
---

# Occurrence History Behind work-on-issue.md's Operative Instructions

`.claude/commands/work-on-issue.md` states each operative instruction in one or two sentences,
then — per this repo's Escalation Rule (see
[Recurrence Escalation for Process and Amendment Mechanisms](recurrence-escalation-for-process-and-amendment-mechanisms.md))
— justifies *why* that instruction exists with the specific `/critique-prompt` retrospective
finding(s) that produced it. Left inline, those justifications grew to dominate the file: by
2026-07-22, five paragraphs (Proactive Recurrence Scan, tier statement, task-list-seeding,
update-docs enumeration, follow-ups) totaled ~27KB of the file's ~57KB, each one a short rule
followed by 3-7 dense worked-example citations.

This article is the extraction target for that history, per this repo's own
[content-extraction rule](content-extraction-dry-ssot-as-the-decision-principle.md) — the
operative rules stay inline in `work-on-issue.md` (a rule an agent must act on belongs where the
agent reads it), while the *evidence for why the rule exists* moves here. `work-on-issue.md`'s
own inline text now carries only the rule, its `[defect-class: <slug>]` tag where one exists, and
a pointer to the matching section below.

## Proactive Recurrence Scan (both legs)

**Rule (stays inline):** run `development-workflow.md`'s Proactive Recurrence Scan before any
tier determination or architectural decision. State each leg — the `[defect-class:]` tag grep and
the wiki-lessons Index check — separately as "run, evidence: ..." or "not run, reason: ...". Cite
full/untruncated output. When more than one distinct slug matches, extract and read every matched
line, quoting or paraphrasing what each says. Re-run the wiki-lessons leg immediately before
creating any new file governed by a static-analysis-like gate (CheckStyle/SpotBugs/PMD/ArchUnit/
ESLint/any CI linter — not an exhaustive whitelist).

**Occurrence history:**

- **#448** — the defect-class-tag leg was executed and cited in full (counts, distinct slugs,
  quoted matched lines), but the wiki-lessons Index leg was never run at all that session, with
  nothing in the response surfacing the omission. The instruction described *how to cite* a leg's
  output once run but never required confirming both legs were *attempted* in the first place.
  → Added the paired-requirement rule: a leg silently absent (no output, no stated reason) is
  itself a named gap.
- **#439** — the defect-class-tag grep was run through `| tail -30`, truncating the result before
  a "no domain overlap" conclusion was drawn — unverifiable evidence by `retro-issue.md` Check 8's
  own standard. → Added the "cite full output, never pipe through `head`/`tail`/`grep -m`"
  requirement.
- **#432** — a grep cited by an honest, unpiped line count (19 hits) was reduced by the harness's
  own tool-result truncation to a single-match preview; the "no domain overlap" conclusion was
  drawn from that one preview match, and a re-run afterward surfaced 7 further distinct slugs
  never actually read. → Added: a bare line count only satisfies the bar when every match shares
  one distinct slug; extract distinct slugs and read every matched line when more than one is
  present; read back the saved-output file when the harness's own display truncates a result.
- **#433** — "I've read the matched lines for each" was asserted for 11 distinct slugs without
  ever quoting one in the response — an unverifiable claim by the same standard already applied to
  the count half of the check. → Added: quote or paraphrase what each matched line actually says,
  not just assert having read it.
- **#442** — the upfront scan was correctly scoped to the issue's subject-domain keywords, but no
  re-check happened before creating a new backend test file mid-session; the resulting
  `PitNamingConventionTest` naming-convention violation was already documented in a wiki lesson
  and cost a full CI round-trip a second grep would likely have caught pre-commit. `[defect-class:
  wiki-second-leg-not-regrepped]` (1st occurrence). Also: the sibling-precedent paragraph's
  tier-3 direct-mirror exception (Amendment #69) was ambiguously read as also excusing tier-2's
  `TaskCreate` seed — confirmed via `AskUserQuestion` that it does not. → Added the wiki-lessons
  re-trigger pointer and the "this exception is scoped to tier-3 only" sentence.
- **#443** — the second-leg re-check (from #442's fix) has no forcing artifact, unlike the
  sibling-precedent/sequence-table checks (both require a literal `TaskCreate` item); a narrower
  ad hoc check (one specific ArchUnit rule's scope) was substituted for the instructed fresh Index
  grep, which happened not to miss anything only by luck. `[defect-class:
  wiki-second-leg-not-regrepped]` (2nd occurrence, opened directly at tier 2 per the
  artifact-shaped-requirements principle). → Added the literal `TaskCreate`-seeding requirement,
  with the grep output pasted into the item's resolution.
- **#493** (this issue's own retrospective) — both legs were run correctly, with full untruncated
  output, distinct-slug extraction, and quoted matched lines — the first fully clean execution of
  this check observed. **Validated, not a finding.**
- **#555** — the wiki-lessons leg was run once, correctly, at session start, but never re-triggered
  before writing a new controller file (`SellerProductController.java`) created mid-implementation
  — a 3rd occurrence in this shape, distinct from #442 (mechanism didn't exist yet) and #443 (the
  leg was skipped entirely, even upfront). → Reworded the re-trigger requirement to anchor the
  `TaskCreate`/scratchpad seed to a concrete tool-call boundary ("immediately before the first
  `Write`/`Edit` tool call that creates the new gate-governed file") rather than the vaguer "at the
  moment of deciding."
- **#556** — the per-slug quoting/disposition standard was applied asymmetrically across the leg's
  3 grepped files: `development-workflow.md`'s 7 matched slugs got full quoted dispositions, but
  `work-on-issue.md`/`retro-issue.md`'s 8 matched slugs got only a count + slug list with
  dispositions inferred from memory, no matched-line text quoted. → Added: the standard applies
  independently to each of the 3 files, not satisfied for one by virtue of being satisfied for
  another.
- **#578** — both legs run correctly again, full per-file per-slug quoted dispositions for all 3
  grepped files (development-workflow.md's 7 slugs, work-on-issue.md/retro-issue.md's 9 across
  their combined 21-line saved output), no truncation. **Validated, not a finding** — the #556 fix
  held on the very next issue to exercise it.
- **#580** — regressed after #578's clean pass: `development-workflow.md`'s leg satisfied both
  the command-shape and per-slug-quoting sub-requirements, but `work-on-issue.md`/`retro-issue.md`
  (9+1 distinct slugs) used the correct `grep -n` shape while dispositioning every slug from
  memory of what it generally means — zero lines quoting actual matched text, the exact
  quote-shortcut the instruction exists to prevent, just with the command-shape half now
  compliant. This showed the two sub-requirements (shape vs. quoting) can pass/fail independently
  and the prose sentence pairing them didn't force both. → Split into two explicitly
  independently-checkable sub-requirements, with a mandatory one-line-per-distinct-slug bullet
  format (no aggregate/shared-sentence dispositions) replacing the prose "individually-stated
  disposition" wording that #580 showed was satisfiable without genuine quoting.
- **#114** — recurred a further time despite the mandatory bullet format: `development-workflow.md`
  and `retro-issue.md`'s slugs got individual quoted/dismissed lines, but `work-on-issue.md`'s 15
  distinct slugs were collapsed into one aggregate sentence ("these are standing process mechanics
  ... not separately dispositioned") — the exact pre-#580-fix shape, on the file the fix was
  written for. `[defect-class: recurrence-scan-quote-shortcut]` (recurrence, N-th occurrence).
  A `/critique-prompt` retrospective of #114 then tried to verify the underlying tier-4 hook
  (`recurrence-scan-quote-gate.sh`) by reconstructing the exact real payload and piping it through
  the script directly — the script's own logic produced a correct `block` decision, proving the
  *rule* wasn't the failure this time, something about the live dispatch was. That same test
  **corrupted the real session marker**: the gate script's block-reason JSON itself contains
  `[defect-class: ...]`-shaped text, and `recurrence-scan-marker.sh` (which fires on *any* Bash
  stdout matching that shape, not just genuine grep output) treated the test's own output as a
  fresh scan, overwriting the real marker with garbage extracted from the error message. A second,
  independent bug then surfaced live and reproduced 3 times in a row: `recurrence-scan-quote-gate.sh`
  blocks *any* `TaskUpdate` whose subject or description merely contains the substrings
  "recurrence" and "scan" — including a task about investigating the hook itself, purely because it
  named the hook script. None of these three findings were fixed this session (marker corruption
  was cleaned up via a genuine re-run of the grep; the other two are open, tracked as a follow-up
  investigation) — → No prompt-text change made; this occurrence's real lesson is that the
  enforcement layer itself now needs auditing, not another prose reword of the rule it enforces.

## Tier statement / N/A discipline / Sequence-table enumeration

**Rule (stays inline):** state tier only *after* the Proactive Recurrence Scan has run (capability
check → scan → tier). State the priority label plus any risk modifier, and the resulting effective
tier — restated explicitly again on any mid-task modifier discovery, including one surfaced by the
user's own `AskUserQuestion` answer, not just by reading code. Enumerate every Conditionally
Required / Mandatory-if-Applicable Sequence-table step exhaustively, in table order, stating
"N/A — reason" rather than silently omitting. A split-necessity row (currently: step 29 only)
needs two separate statements. Seed the enumeration as its own `TaskCreate` item; restate each
item's actual verdict when closing it, not just mark it complete.

**Occurrence history:**

- **#485** — neither `TaskCreate` nor `TodoWrite` was checked at all that session (not "checked
  and absent" — never checked), with zero disclosure and zero enumeration artifact of any kind. A
  genuine mid-task tier bump (Low→Medium, triggered by the user choosing a schema-migration design
  via `AskUserQuestion`) went unrestated in any user-visible message, surfacing only in a post-hoc
  memory note. → Added the capability-check instruction as the file's first action; extended the
  tier-restatement rule to cover design-choice answers equally with code-reading discoveries;
  named "zero enumeration artifact" as its own failure mode.
- **#487** — the tier statement was made *before* the Proactive Recurrence Scan, reversing
  `development-workflow.md`'s explicit ordering requirement — traced to the new capability-check
  paragraph's "before anything else" framing reading as resetting the ordering. The scan itself,
  once run, was executed correctly. → Added the explicit ordering cue: capability check → scan →
  tier statement.
- **#493** — the correct order was followed exactly. **Validated, not a finding.**
- **#441** — the Sequence-table `TaskCreate` item was seeded correctly at plan time, then marked
  `completed` at closure with no restatement of its actual N/A/done verdicts. → Added: seeding is
  not itself the check — closing it must restate each item's result in the same message.
- **#438** (Check 4) — only a self-selected subset of applicable steps got individually stated;
  gap-analysis/requirement-traceability/project-board/release/deploy steps were handled correctly
  in substance but never checkpointed. `[defect-class: sequence-table-partial-enumeration]` (1st
  occurrence). → Added "must be exhaustive, not curated" plus the `TaskCreate`-seeding requirement.
- **#439** — step 28/29's split necessity (Mandatory for CHANGELOG, separately CR for "formal
  release docs") was treated as one obligation, silently addressing only the Mandatory half.
  `[defect-class: sequence-table-partial-enumeration]` (2nd occurrence, split-necessity variant).
  → Added split-necessity guidance and the "known split-necessity rows" convenience list.
- **#427** — `assess-existing` was silently dropped from the enumeration despite being performed,
  and the split-necessity CR half was again unaddressed. `[defect-class:
  sequence-table-partial-enumeration]` (3rd occurrence) — but the `TaskCreate`-seeding instruction
  *was* available and simply wasn't used, a compliance failure rather than a new wording gap; no
  further prompt change was made for this specific finding (tracked as a hook candidate instead,
  since prose had already failed 3 times).
- **#556** — no instruction existed for a mid-invocation target-issue change: that session started
  as `/work-on-issue 555`, found #555 already closed, and was redirected to #556 via
  `AskUserQuestion`. Whether the capability check/scan/tier statement needed re-running for the new
  issue had to be guessed (correctly, but unstated). → Added an explicit re-scoping rule: the
  capability check doesn't need re-running (tool discoverability is session-wide, not per-issue),
  but the Proactive Recurrence Scan and tier statement must both re-run in full against the new
  issue.
- **#578** — capability check → scan → tier statement run in the correct order, tier (High, via a
  schema/data-migration risk modifier discovered and stated *before* implementation began, not
  mid-task) explicit, full Sequence-table enumeration seeded as its own `TaskCreate` item and
  restated with verdicts at closure. **Validated, not a finding.**

## Task-list-seeding (TaskCreate/TodoWrite/scratchpad fallback)

**Rule (stays inline):** check `TaskCreate` via `ToolSearch`, fall back to `TodoWrite` via
`ToolSearch` if absent; only if neither is discoverable, substitute a timestamped scratchpad
checklist file with explicit disclosure. The scratchpad file must contain one discrete,
separately-labeled line per tier-2+ requirement — not a single merged list satisfied by inline
prose. Every seeded item must state what evidence its resolution should show and meet
peer-handoff clarity (verifiable and actionable from its own text alone). Scratchpad item titles
and verdicts must be echoed into the visible chat response, in the same turn the file is written,
not deferred to a later summary.

**Occurrence history:**

- **#432** — neither `TaskCreate` nor `TodoWrite` was checked for before falling straight to
  unlogged prose, with no record of why the required artifact was skipped. → Added the
  `ToolSearch`-then-fallback chain and the "state what evidence a resolution should show" content
  standard.
- **#433** — the same absence recurred (2/2 occurrences), suggesting a standing absence for this
  environment rather than a rare pre-migration edge case. → Named the 2/2 occurrence explicitly so
  the fallback chain isn't treated as usually resolving by its second link.
- **#482** — the scratchpad-fallback file correctly performed the follow-up-sweep grep, but only
  as inline prose, not a seeded checklist entry — the "narration substituting for artifact" gap
  reproduced one level deeper inside the fallback path itself. Also (open-perspective,
  speculative): neither tool available in 3/3 sessions checked (#432, #433, #482). → Added the
  "one discrete line per tier-2+ requirement" rule; reframed the scratchpad path as this
  environment's primary expected mechanism, not a rare fallback.
- **#485** — the whole task-list-seeding apparatus was silently bypassed end to end that session
  (see Tier statement section above — same root incident). → Added the "zero artifact of any kind"
  disclosure requirement, distinct in severity from "used the documented scratchpad fallback."
- **#489** — a fully compliant scratchpad checklist file (discrete lines, evidence-bearing
  content, correctly closed out) was never once quoted or summarized in the chat — auditable in
  principle, invisible in practice. → Added the echo-into-chat requirement.
- **#493** — the same echo requirement failed again, the very next issue closed after the #489 fix
  landed: the scratchpad file was compliant, but the chat turn that wrote it never quoted its
  content. `[defect-class: scratchpad-not-echoed]` (1st occurrence, opened directly at tier 2 per
  the artifact-shaped-requirements principle). → Tightened the requirement to "same turn, not
  deferred."
- **#20/#21 era** (2026-07-21) — reading Claude Code's own docs confirmed `TodoWrite` is the
  documented pre-v2.1.142 fallback; a separate guide's "peer handoff" quality-checkpoint concept
  had no counterpart in the seeding requirements, which only specified an item's *existence*. →
  Added the peer-handoff-clarity standard for every seeded item, not just tier-2+ ones.
- **#553** — a mid-session commitment (step 4's "will re-verify at actual closure" deferral) was
  made in prose but never added to the task list as its own item, so nothing forced it to stay
  visible through to the step where it needed resolving. → Added: the task list is a living
  document through closure, not just an upfront planning artifact — a new mid-session commitment
  gets its own item at the point it's made.
- **#554** — the #553 fix (above) didn't fully hold: the deferred aggregate fact was tracked in
  prose narrative at step 7, but with no literal forcing line, it recurred a 2nd time (README's M4
  count stayed stale past closure again). `[defect-class: aggregate-fact-deferred]` (2nd
  occurrence). → Hardened step 7 with a literal checklist line —
  `Deferred aggregate fact from step 4? [ ] none / [ ] resolved, new value: ___` — rather than
  another paragraph of prose restating the same check.
- **#635/#638/#639** — the inverse of the #114 "hook-gated close succeeding is not proof the gate
  engaged" finding: `recurrence-scan-quote-gate.sh` rejected a closing `TaskUpdate` 3/3 times across
  this session's three issues, each time claiming required snippets were missing when the
  submitted description already contained every one verbatim (an encoding/dash-character mismatch
  in the hook's own substring match, not a content gap). Routed around each time via
  `TaskUpdate(status: deleted)` on the tracking task, with the actual compliance stated in the
  visible chat response instead. → Added a sanctioned-recovery-path sentence: when a hook's own
  rejection message is checked against the submitted content and the content demonstrably already
  satisfies it, delete-and-state-in-chat is the sanctioned move — don't retry indefinitely or
  silently give up. A `/critique-prompt` critique of this file (same session) separately flagged
  that the hook may now be failing *unconditionally* on this task shape rather than intermittently
  — see `development-workflow.md`'s own Amendment Log for the corresponding reliability-status
  update.

## Sibling-precedent check (tier-2 TaskCreate-seeding requirement)

**Rule (stays inline):** whenever `solution-options-adr` is relevant, seed a dedicated `TaskCreate`
item for the sibling-precedent check itself — regardless of which of the three possible answers
(matches a precedent / diverges from one with a stated reason / no precedent exists and this
was never architectural) it lands on. Tier-3's "paste `gh search` output" requirement has its own
exception (a single unambiguous 1:1 precedent file read directly satisfies it) — that exception is
scoped to tier-3 only, it does not extend to tier-2's seeding requirement.

**Occurrence history:** see `development-workflow.md`'s own Amendment Log for the full
`[defect-class: sibling-precedent-scope]` ladder (#125/#426/#440/#442 tier-2 failures, resolved by
issue #493's blocking `PreToolUse` hook — see this file's own `1-18` collapsed Amendment Log row,
entry orig. 3, for when the tier-2 requirement was first added here). #429 and #435 were the first
two consecutive clean passes after 3 straight failures. #442 additionally clarified that the
tier-3 direct-mirror exception does not extend to tier-2 (see Proactive Recurrence Scan section
above). #447 extended the "seed regardless" rule to the zero-precedent + judged-non-architectural
case, confirmed via `AskUserQuestion`. **#639** — a new variant: the `PreToolUse` hook that
resolved the single-issue version of this gap (#493) didn't catch this one, since #639's own
sibling-precedent judgment *was* reasoned through correctly, just folded into the implementation
task's description instead of seeded as its own item — the hook fires on content shape, not on
"was this issue #2 of N in one session and did issue #1 already do the ceremony." A
`/critique-prompt` critique of `work-on-issue.md` (same session) added an explicit
per-issue-even-within-one-session clause to the sibling-precedent paragraph. **#652** — a further
variant: the seeding requirement's scope was read narrowly as "architectural/precedent decision,"
which didn't obviously cover a Mid-Implementation Scope Discovery SAME-vs-SEPARATE classification
(`development-workflow.md`'s `solution-options-adr` case 6) — two such calls were made inline
(a `ShippingStep` bug judged same-concern, a Razorpay-CI-credentials gap judged separate-concern)
with no `TaskCreate` seeded for either. A `/critique-prompt` critique of `work-on-issue.md` and
`development-workflow.md` (same session) added an explicit clause to both: case 6 and this
paragraph's seeding requirement both now name MISD's SAME/SEPARATE test as an in-scope instance.

## Update-docs enumeration (six-doc checklist)

**Rule (stays inline):** apply `development-workflow.md` step 23's enumeration (README/RTM/SRS/
SDD/CHANGELOG) in full, plus `test-plan.md` as a 6th doc. State all six results together at the
same point the pre-commit `git diff --cached` read happens, as one explicit checklist — not
narration scattered across the session. Any numeric/aggregate fact identified during this
enumeration must be re-stated with its post-closure value in the same closing message that marks
the item complete — the item must stay open until the real, current value has been verified. A
correction requiring a file change after the primary PR has merged needs its own minimal nested
branch/PR/merge cycle, labeled explicitly as a self-correction (distinct from an
independently-scoped follow-up).

**Occurrence history:**

- **#436** — `test-plan.md` was omitted from `retro-issue.md` Check 4's own enumeration despite
  `testing.md` requiring it; the "stated result" requirement also had no anchor point, satisfied
  via scattered narration rather than one consolidated statement. → Added `test-plan.md` as the
  6th doc; anchored "stated result" to the pre-commit diff-read checkpoint.
- **#443** — README's M4 count was checked accurate pre-implementation, never re-verified at
  closing, deferred with a "next session should confirm" note — caught only by a follow-up
  `/critique-prompt` pass. `[defect-class: aggregate-fact-deferred]` (1st occurrence, opened
  directly at tier 2). → Required re-stating the post-closure value in the same closing message.
- **#444** — the aggregate-fact fix held in substance (the count was genuinely re-verified and
  shipped) but the task-list bookkeeping split across two mismatched tasks (task #6 marked
  `completed` citing "task #10," the real work landing under task #12 instead); separately, the
  primary PR had already merged by the time the fresh count was known, requiring a cycle the
  instruction didn't anticipate. → Required the item stay open until genuinely re-verified, and
  named the nested branch/PR/merge cycle for a post-merge correction.
- **#489** — two PRs landed in one session (#546 primary, #547 a same-session correction of #546's
  own side effect) with nothing distinguishing that relationship from an ordinary
  Mid-Implementation-Scope-Discovery follow-up in the record. → Required labeling a nested
  self-correction PR explicitly as such, distinct from a new-scope follow-up.
- **#559** — `CHANGELOG.md` was silently never mentioned or updated that session (confirmed via
  `git log --oneline -- CHANGELOG.md` and a grep for the issue number, both zero hits) while RTM
  and README were both correctly updated — a partial, self-selected subset of the 6 docs was
  treated as satisfying this step, with no stated N/A for the doc that was actually skipped. →
  Added the literal per-doc checklist format (`README: <updated|checked, still accurate|N/A —
  reason>`, one line per doc), mirroring step 7's own literal-line forcing pattern.
- **#561** — two gaps in the same session (a pure docs/ADR issue, no code): (1) the requirement-
  traceability backfill sub-clause (does the issue body cite an SRS FR ID, or state "no formal FR
  — because...") was silently skipped — the issue cited an SRS section but no FR ID, and the gap
  was never checked or backfilled, the first real exercise of this sub-clause since it was mirrored
  in from `development-workflow.md`. `[defect-class: requirement-traceability-backfill-not-seeded]`
  (1st occurrence, opened directly at tier 2 per the artifact-shaped-requirements principle). (2)
  the 6-doc checklist never states an "ADR index" line even though `development-workflow.md` step
  23 conditionally requires it whenever step 8 produces a new ADR — #561 did produce one, and the
  ADR index was only updated by judgment call, not instruction. → Added the `TaskCreate`-seeding
  requirement for the backfill check, and a 7th checklist line (`ADR index: <updated|N/A>`).

## Follow-ups closure step

**Rule (stays inline):** file a separate concern discovered mid-implementation immediately, with
`priority:`/milestone/Project #9 set at creation — no triviality exception. State a filed
follow-up's priority label inline wherever mentioned later. Link a genuinely blocking-shaped
follow-up via GitHub's native Issue Dependencies API, not prose-only cross-referencing. Seed the
closing follow-up sweep as its own `TaskCreate` item: a literal grep-and-resolve pass over the
session's own CHANGELOG entry/PR body for hedge phrases (a floor of five: "out of scope"/"not
fixed here"/"separate"/"tracked separately"/"pre-existing... not introduced" — not the complete
search). When the issue produced no CHANGELOG entry and no PR at all, state that explicitly and
grep whatever written artifact the issue did produce instead.

**Occurrence history:**

- **#439** — the Mid-Implementation Scope Discovery flowchart's "SEPARATE concern → mandatory"
  wording was read as carrying an unstated triviality carve-out; a genuinely separate concern
  (`UpdateUserDTO.address`) was noted in the CHANGELOG instead of filed. `[defect-class:
  unfiled-separate-concern]` (1st occurrence; #496 filed retroactively). → Added the explicit
  "no triviality exception" sentence.
- **#427** — the same requirement failed again: `AdminProductController`'s raw SQL/exception-
  message leakage was again only noted in CHANGELOG prose, not filed until the retrospective
  caught it (#498 filed retroactively). `[defect-class: unfiled-separate-concern]` (2nd
  occurrence). Also, per the artifact-shaped-requirements principle, the follow-up sweep itself
  was given a `TaskCreate`-seeding requirement here for the first time.
- **#444** — a `priority: high` follow-up (#516) was filed while working a `priority: low` issue;
  a dedicated structural-callout requirement was considered and judged disproportionate given a
  1-occurrence evidence base (open-perspective, not a finding). → Adopted the lighter substitute:
  state the priority label inline wherever a follow-up is mentioned later.
- **#516** (filed during #444) — a worked negative example for the blocking-link requirement: it
  was discovered mid-task but neither blocks nor is blocked by #444, so it was correctly left
  unlinked rather than force-linked.
- **#435** — the five-phrase hedge list was prefixed "e.g." with no statement of whether it was
  the required minimum or merely illustrative; #435's own sweep used a superset without the
  instruction requiring that breadth. → Clarified the five-phrase list is a floor, not the
  complete search.
- **#493** — no CHANGELOG entry and no PR existed at all (a change confined to gitignored
  `.claude/` config); the closing GitHub comment was checked for hedge phrases, but the
  substitution away from the instruction's literal targets (CHANGELOG/PR body) was never flagged
  as such. → Added the no-CHANGELOG/no-PR carve-out requiring the substitution be named explicitly.
- **#578** — all 3 follow-ups filed that session (#579/#580/#581) had their SRS FR ID in the
  *title* only, never the *body* — this step previously pointed only at `development-workflow.md`
  steps 12/14/15 and never cross-referenced step 7's requirement-traceability rule the way step 4
  already does for the originating issue. → Added the citation-at-creation requirement, folded
  into the same `gh issue create --body` call as priority/milestone/Project #9.
- **#440, #556** — `[defect-class: add-to-project-separate-call]` (2nd occurrence at #556): the
  `-p "<project>"` flag was dropped from `gh issue create` twice despite a cross-reference to
  `development-workflow.md` steps 12/14/15 by number. → Escalated directly to a literal
  pre-filing `TaskCreate` line naming the flag, per the artifact-shaped-requirements principle.
- **#635/#638/#639** — the native Issue Dependencies link between the originating issue and its
  own follow-ups was set backwards on the first attempt: recorded as "#635 blocks #638/#639" when
  the intended (and eventually corrected) relationship was the reverse — #635's own acceptance
  criteria couldn't close until #638/#639 did, so #635 is the one that should have been
  `blocked_by` them. Caught only because the post-merge unblocking check on #638 returned empty
  where #635 was expected, not by any instruction catching it proactively. The rule as written
  ("link a genuinely blocking-shaped follow-up") never stated which direction to point the link
  for this specific, common shape. → A `/critique-prompt` critique of `work-on-issue.md` added a
  one-line direction test: ask which issue's own closure is gated on the other's — for a follow-up
  spawned from an originating issue whose AC depends on it, the link is `<originating issue>
  blocked_by <follow-up>`, not the reverse.
- **#652** — the direction test itself was silently skipped rather than answered: #652 was closed
  in the same session #662 was filed (its own AC re-scoped down, the remainder handed to #662
  entirely), so there was no live gating relationship for the test's framing ("whose AC can't be
  satisfied until the follow-up lands") to apply to. Neither `blocked_by` link nor an explicit
  "no blocking relationship" statement was made — only prose cross-references (CHANGELOG, PR body,
  issue References). → A `/critique-prompt` critique of `work-on-issue.md` added an explicit
  fallback: when the originating issue is closing concurrently rather than staying open pending
  the follow-up, state that as the test's answer rather than treating "no live gating relationship"
  as license to skip the test.

## Merge closure step (Monitor-call discipline)

**Rule (stays inline):** prefer one long-lived poll-until-terminal `Monitor` call per PR over
several separate restarts; before starting a new one, check whether one is already running for
that same PR.

**Occurrence history:**

- **#433** — 4 separate `Monitor` calls were spun up across 2 PRs' CI runs rather than one
  continuous loop per PR, each restart re-announcing already-passed checks (open-perspective, not
  evidence-bound at the time). → Added the lightweight one-continuous-loop preference.
- **#489** — the preference, stated in prose since #433, failed to prevent a 2nd occurrence: 3
  overlapping `Monitor` calls were stacked for the same PR before being noticed and cleaned up via
  `TaskStop`. `[defect-class: monitor-call-stacked]` (1st occurrence). → Added the explicit
  check-before-starting-a-second-call requirement.

## Pre-commit review (git status / diff hygiene)

**Rule (stays inline):** review `git status` for pre-existing, unrelated uncommitted changes at
session start; stage only this issue's own files via targeted `git add`, never `git add -A`/`.`.
Once staged, run one consolidated `git diff --cached` read across the full staged set as its own
checkpoint, immediately pre-commit.

**Occurrence history:**

- **#429** — a clean `/retro-issue` pass still found no instruction existed for handling
  pre-existing uncommitted foreign files; 3 such files had to be excluded via improvised targeted
  `git add`, relying on general git-safety instinct rather than any instruction here. → Added the
  `git status`-review clause.
- **#435** — verification had stayed per-file (one targeted diff, a debug-artifact grep scoped to
  two directories), never one pass reading every staged file together immediately before commit.
  → Added the consolidated `git diff --cached`-read sub-step, distinct from the security checklist
  and the foreign-file exclusion check.
- **#436** — both #435 fixes held cleanly the very next issue. **Validated, not a finding.**

## Scratch-artifact cleanup

**Rule (stays inline):** remove test-only data artifacts and stop any dev server/container process
started solely for this issue's own live verification, via separate commands — never a chained
`pkill`+backgrounded-restart in one call.

**Occurrence history:**

- **#444** — the wording named only data artifacts (users/rows/uploaded files); starting backend/
  frontend dev servers and MySQL/Redis containers for live SSE verification, and stopping them
  afterward, had to be inferred unprompted. → Extended the step to explicitly cover dev-server/
  container shutdown.

## Branch currency

**Rule (stays inline):** confirm the current branch and switch to `master`+pull if it's a stale
branch from a different open issue's PR, before creating a new branch.

**Occurrence history:**

- **#482** — the working directory was found checked out on a different, still-open issue's
  branch (`fix/481-...`, PR #539 open); switching away had to be discovered and done unprompted,
  per general git-safety instinct rather than any instruction here. → Added the explicit
  branch-currency check.

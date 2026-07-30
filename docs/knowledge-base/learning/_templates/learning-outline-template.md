# [Subject] Learning Outline

## Reuse Prompt

Three ways to use this with an LLM — pick whichever matches how you're interacting with it:

- **One-off, per-message upload/attach** (e.g. ChatGPT's file-upload button): keep the filename
  as `learning-outline-template.md` (or note whatever you renamed it to) so the prompt below can
  refer to it by name. Send the prompt as your message, with the attached file. Repeat the full
  prompt every time you want a new outline.
- **One-off, paste inline**: copy this whole file (including everything below this section) and
  paste it into the `Template:` block at the bottom of the prompt, replacing the placeholder
  line. Also repeated every time.
- **Persistent — ChatGPT Project (recommended if generating more than one outline)**: create a
  Project, upload this file to its Sources/knowledge, then paste the prompt below (with
  `{{SUBJECT}}` and the "Prompt:"/"Template:" framing stripped, and `{{TEMPLATE_FILENAME}}`
  replaced with `learning-outline-template.md`) into the Project's **Instructions** field once.
  After that, every new chat inside the project only needs a bare subject name (e.g. typing just
  `Docker`) — no re-pasting, no re-uploading. Verified working: SOP Step 0 correctly ran and
  hard-stopped on an out-of-scope subject using only this setup, with no prompt text sent at all.

Prompt (replace `{{SUBJECT}}`, and `{{TEMPLATE_FILENAME}}` if uploading):

```
You are designing a learning outline for the subject: {{SUBJECT}}

Do not stop to wait for my confirmation or ask whether to continue — that checkpoint never
happens. This should easily fit in one message; if it somehow doesn't, continue automatically
across consecutive messages with no announcement or permission-asking in between.

Use the structure of the attached file {{TEMPLATE_FILENAME}} (or the Template block below, if no
file is attached) as your design process, but treat Step 0 and the SOP as YOUR internal design
process, not something to narrate to me. Run Step 0's Scope Check silently:
- If the subject **fails** either the "outline-shaped" check or the "software engineering or
  adjacent" check, that failure explanation IS your entire response — say which check failed and
  why, and produce nothing else. This is the one case where an explanation is the correct output.
- If **both checks pass**, do not report on Step 0 at all — no "Step 0 — Scope Check" section, no
  "Per the governing template..." framing, no summary of your process. Skip straight to the
  deliverable with zero preamble. Passing Step 0 is a prerequisite for producing output, not
  something worth mentioning once it's satisfied.

If both checks pass, silently continue through the SOP's remaining 10 steps as your internal
design process (brain-dump → identify prerequisites → cluster by dependency → split into modules
using the three tests → settle each Module's title and one-line scope → add a capstone → draw the
Dependency Graph → extract a Pareto Order → re-run the final draft end to end → plan Module
filenames and fill in the Index) — none of this process narration belongs in the response either.

Output format — produce ONLY the deliverable content below, with no meta-commentary, process
narration, or explanation of what you did surrounding it. **One single file only** — the outline
skeleton — with these parts IN THIS ORDER, none skipped:

1. A title heading: `# {{SUBJECT}} Learning Outline`
2. The header block, exactly this shape (fill in real values, keep the `>` blockquote format):
   ```
   > **Category:** ...
   > **Tags:** `...`, `...`, `...`
   > **Audience:** ...
   > **Last Updated:** ...
   ```
3. One line: `Learning outline for **{{SUBJECT}}** — <one-line description of scope>.`
4. Claim (with its Caveat)
5. Prerequisites (Level 0)
6. Level headings, each listing that Level's Module titles only — no bullets, no Outcome, no
   Check, no diagrams; those get written later when each Module's own file is actually authored
7. Mini Project
8. Knowledge Dependency Graph
9. Pareto Learning Order
10. Index

Parts 1-3 are not optional or decorative — do not start the response directly at the Claim
paragraph. Part 10 (Index) is the last thing in the response — do not append anything after it:
no "Source template followed," no citation of this template or file, no closing remark of any
kind. Verify all ten parts are present, and nothing else, before finishing your response.

Heading levels: `#` for the title only (part 1). `##` for every top-level section — the prose
around the header block, Claim, Prerequisites, each Level, Mini Project, Knowledge Dependency
Graph, Pareto Learning Order, and Index. Module titles are bullet list items under their Level,
never their own heading.

Do NOT generate separate per-Module files. Do NOT write out each Module's bullets, Outcome,
Check, or diagrams — only its title and, in the Index, a one-line scope description. Full
Module content is written later, outside this generation pass, when I author that Module's own
file — Content Style then governs that authoring, not this skeleton output.

Do NOT include the Definitions glossary, the SOP, the Content Style rules, the Module numbering
note, the Housekeeping section, the "Amending This Template" section, the Amendment Log, this
Reuse Prompt section, or any commentary about your process anywhere in the output — these are
this template's own guidance and maintenance history, not deliverable content about {{SUBJECT}}.
The Template block below includes the entire rest of this file, but only the ten parts listed
above under "Output format" belong in your response.

Constraints:
- Every Level ordering must satisfy strict dependency ("must know X before Y makes sense"), not
  perceived difficulty.
- Every Module must pass the Single-Outcome, Sibling-Independence, and Concrete-vs-Category tests
  from the template's SOP step 4 — split anything that fails, even though only the title and a
  one-line scope are written out. Re-apply these tests once more to the whole finished draft (SOP
  step 9), not just module-by-module on the first pass.
- Number modules continuously across the whole outline (Module 1, 2, 3...), never restarted per
  Level, and match each Module's number to its planned filename (e.g. Module 3 →
  `module-3-<name>.md`, listed in the Index).
- Include the Knowledge Dependency Graph as one whole-outline diagram.
- Include the Pareto (80/20) Order, or explicitly state why it's skipped for this subject.
- Fill in the Index table per SOP step 10 with exactly these three columns: `File`,
  `Module Description`, `Last Updated` — one row per planned Module, with its intended filename
  (not a real link, since the file doesn't exist yet) and a one-line description of what that
  Module will cover. Delete the placeholder row entirely — it must not appear in the final output.
- Delete all template comments and unfilled [bracketed placeholders] in the final output —
  nothing bracketed should remain, including the literal text "N+1" in any heading (renumber it
  to the outline's actual final Level count).

Template:

<paste everything from "STANDALONE TEMPLATE" below down to the end of the file>
```

---

<!--
STANDALONE TEMPLATE — copy this whole file anywhere; it has no dependency on this repo's other
docs. It follows a generic "Orientation + Index in one file" pattern: Orientation is prose that
can grow (the claim, the level/module skeleton, prerequisites); Index is a clean, scannable
manifest table — one row per **planned** Module (see SOP step 10). A filled-in outline is a
single skeleton file: Level headings with Module titles only, no bullets/Outcome/Check/diagrams.
Those get written later, one Module at a time, in that Module's own file, authored separately
from this skeleton-generation pass. Keep the Orientation/Index two-section split even if one side
starts small — collapsing them back into one undifferentiated block of prose is the failure mode
this shape exists to prevent.

When to use THIS template vs. a simpler article:
Use it when the subject genuinely needs sequential, multi-session progression — prerequisites
build on each other, order matters, and each module's "Outcome" gates moving to the next. If
you're really just explaining one concept from every angle with no required reading order, a
flat topic write-up is a better fit. If you're answering a single question, a short Q&A entry is
a better fit. Don't force a subject into this outline shape just because the template exists.

Delete this whole comment block once the file is filled in.
-->

> **Category:** [e.g. Frameworks / Protocols / Tooling]
> **Tags:** `[tag-1]`, `[tag-2]`, `[tag-3]`
> **Audience:** [e.g. Beginner → Intermediate]
> **Last Updated:** [YYYY-MM-DD]

Learning outline for **[Subject]** — [one-line description of scope].

**Why this template produces a skeleton, not full content:** the actual goal of a learning
outline is studying a new subject in correct **pedagogical order** — knowing what to learn first,
what depends on what, and where the boundaries between concepts sit. That's a sequencing problem,
fully answered by the Level/Module structure, the Dependency Graph, and the Pareto Order below —
none of which require full content to exist. Full Module content (bullets, worked examples,
diagrams) is a separate, optional follow-on activity, sourced however you like once you know what
to study and in what order; it is not a missing piece of this artifact.

This file does two jobs, kept in two separate sections below: **Orientation** (the claim, the
level/module skeleton, prerequisites) and **Index** (the manifest — one row per **planned**
Module; a filled-in outline is a single skeleton file, with each Module's full content written
later in that Module's own file).

---

## Orientation

### Definitions

Fixed meanings for this template's own vocabulary — read this before the SOP, so a term isn't
reinterpreted per subject. If you (or an LLM) are unsure what counts as a "Level" vs. a "Module"
while filling this in, this section is the tiebreaker, not the SOP's examples.

| Term | Definition |
|---|---|
| **Level** | A phase of the curriculum grouping one or more Modules that share a dependency depth — everything in Level N assumes everything in Level 0..N-1 is already known, and nothing in Level N is needed to understand Level 0..N-1. Not a difficulty rating. |
| **Module** | One cohesive concept within a Level — see the SOP's three splitting tests (Single-Outcome, Sibling-Independence, Concrete-vs-Category) for exactly how "cohesive" is decided. A Module is the unit that eventually gets its own Outcome, Check, and file — but the skeleton stage (this template's default output) only fixes its title, its position in a Level, and a one-line scope description for the Index. Full content is written later, in that Module's own file. |
| **Prerequisite** | Something the learner must already know *before* Level 1, that is not itself part of the subject being taught. Lives in Level 0. If it's part of what this outline teaches, it's not a Prerequisite — it's a Module. |
| **Outcome** | A single sentence stating what the learner can now explain or do, checkable by someone else observing them — not a summary of the Module's bullet list. If it needs "and" to join two unrelated capabilities, the Module is two Modules (Single-Outcome Test). Pairs with a **Check** (below). |
| **Check** | One concrete self-verification for a Module's Outcome — a question to answer, or a tiny exercise to perform, that would fail if the Outcome weren't actually met. Distinct from the Outcome itself: the Outcome states the claim, the Check is how the learner tests the claim against themselves. |
| **Capstone / Mini Project** | The one Level that requires *combining* prior Modules into an artifact, rather than introducing a new concept. It has no Outcome of its own — its bullets are the prior Modules being exercised together. |
| **Dependency Graph** | The single whole-outline diagram chaining every Level in sequence. Distinct from a per-Module diagram, which explains one concept's internal relationship and does not represent the outline's ordering. |
| **Pareto Order** | A re-ordered *subset* of existing Modules (not new content) for someone optimizing for fast working knowledge over full mastery. Every entry in it must already exist as a Module elsewhere in the outline. |

### SOP — How to Design This Outline

Follow these steps in order, whether you're writing the outline yourself or handing this whole
file to an LLM as a spec ("design a learning outline for [Subject] using this structure"):

0. **Scope Check — do this before anything else, and state the answer explicitly rather than
   silently proceeding:**
   - **Is the subject outline-shaped?** Does it have a genuine prerequisite/dependency chain
     across multiple concepts, where order matters and each module's Outcome gates the next? If
     it's really one flat concept explained from every angle with no required reading order, or
     a single question, stop — this template is the wrong shape; use a plain topic write-up or a
     Q&A entry instead.
   - **Is the subject software engineering or an adjacent technical field?** If no, stop — this
     template's Content Style rules (backticked code/CLI/config terms, API/tool names) assume a
     technical field and this subject is out of scope for it; use a general-purpose outline
     template instead. Only proceed if the answer is yes.
1. **Brain-dump every subtopic** you can think of for the subject — unordered, no filtering yet.
2. **Identify prerequisites** — what must already be known before touching this subject at all.
   These become Level 0; they are not part of the subject itself.
3. **Cluster subtopics into levels by dependency, not difficulty.** The test for "does X belong
   in an earlier level than Y" is "must you understand X before Y makes sense," not "X feels
   easier than Y." Two topics can be equally hard and still be strictly ordered.
4. **Split each level into modules** — one module per cohesive concept, not per fact. A bullet
   count over ~5-7 is only a *trigger* to check the module, not the actual test — run these three
   checks whenever it's unclear if a module is too broad or too abstract:
   - **Single-Outcome Test**: can you write one Outcome sentence for it that isn't a list? If
     stating what the learner can now do needs "...and can also configure X, and separately
     debug Y," that's two independent capabilities wearing one module name — split it.
   - **Sibling-Independence Test**: pick any two bullets. Does the second require the first to
     already make sense (shared context), or could you quiz someone on it in isolation? Bullets
     that stand alone independently are separate concepts, not sub-points of one — split it.
   - **Concrete-vs-Category Test**: is the module title a *thing* ("Bean Lifecycle") or a
     *category of things* ("Configuration Approaches")? A category title is a sign the module is
     really a mini-level — split each category member into its own module. For example, a
     Spring Framework curriculum's "Configuration Approaches" (historical evolution: XML → Java
     → annotations) should split into "Java Configuration" and "Component Scanning" as separate
     modules one level below, rather than folding both into one category-titled module.
5. **Settle each module's title and a one-line scope description.** The scope description states
   what the module will teach, concretely enough to be useful in the Index — not a restatement of
   a category ("Configuration") but a specific claim ("how to configure logging via
   `application.yml`"). A vague scope ("understands beans") is a sign the module itself is still
   too broad or too abstract — re-run the three checks above. Full Outcome and Check writing is
   deferred to when that module's own file is actually authored (see the Definitions glossary and
   Content Style for what an Outcome/Check should look like at that point) — e.g. a module scoped
   "can write a basic Dockerfile" would later pair with a Check like "write a Dockerfile for a
   static HTML site and build it without consulting notes," but that pairing happens later, not
   during skeleton generation.
6. **Add a capstone level** once the sequence is complete — a Mini Project that forces combining
   most/all prior modules, not a new concept of its own.
7. **Draw the whole-outline Dependency Graph** — a single top-to-bottom diagram chaining every
   level in order. This is distinct from the small per-module diagrams used to explain one
   concept's internal relationship.
8. **Extract a Pareto (80/20) Order** — the subset of modules someone would need to become
   productive quickly, as opposed to reaching full mastery. Not every subject needs this (skip it
   for a short subject where the full outline already is the fast path), but state explicitly
   that it was considered and skipped rather than silently omitting it.
9. **Re-run the final draft once, end to end.** Splitting decisions made early (step 4) can be
   invalidated by modules added later — e.g. adding a Level 5 module can reveal that a Level 2
   module was actually two concepts all along. After the outline is otherwise complete, re-apply
   the three splitting tests to every module once more, and re-check that Level ordering still
   holds (no later Level secretly required by an earlier one). Don't treat step 4 as a one-pass,
   module-at-a-time gate only — this step is what catches drift the first pass couldn't see yet.
10. **Plan each Module's filename and fill in the Index — this file's second job, not optional.**
    Do NOT create the Module's actual file yet — this skeleton pass only decides what its file
    *will be called* (e.g. `module-3-core-mechanism.md`, numbered to match its Module number) and
    records that plan in the Index, alongside the one-line scope description from step 5. Fill in
    the Index with exactly one row per planned Module (`File` | `Module Description` | `Last
    Updated`). **Delete the placeholder row entirely** — it must never survive into a real,
    filled-in outline.

**Module numbering:** number modules **continuously across the whole outline** (Module 1, 2, 3...
N), not restarted per Level — this keeps a Pareto Order or a cross-reference elsewhere unambiguous
("Module 14" always means the same thing, regardless of which Level it's read from) and matches
the planned filename recorded in the Index. Do not use per-Level numbering like
`Module 2.1` / `Module 3.2`.

### Content Style

**Applies later, when a Module's own file is actually authored — not during skeleton
generation**, which only produces titles and one-line scope descriptions. This template assumes
the subject is software engineering or an adjacent technical field. Within that scope, write a
Module's full content (once you get to authoring it) per these rules:

- **Bullets are noun phrases or short fragments**, not full sentences — e.g. `Bean Naming`, not
  "Beans can be given custom names."
- **Code, CLI, and config terms use backticks always** — `@Autowired`, `docker compose up`,
  `application.yml` — never plain text. This keeps terminology scannable and copy-pasteable.
- **A diagram is required, not optional, whenever a module describes a relationship, flow, or
  lifecycle** (request flow, object lifecycle, a dependency chain) — software-engineering
  concepts are disproportionately relational, so this is a trigger condition on the module's
  *content*, not a stylistic nice-to-have.
- **Gotchas and pitfalls get their own `Note:` line**, separate from the bullet list — don't fold
  a warning into a definitional bullet. This mirrors keeping `Outcome:`/`Check:` as their own
  lines instead of merged into the bullets.
- **Prefer the real API/tool/annotation name over a paraphrase** — write `@Transactional`, not
  "the transaction annotation." In this domain the exact name is the thing worth learning; a
  paraphrase loses the lookup-ability the bullet exists to provide.

### Claim

The most effective way to learn **[Subject]** is to progress from **[starting point /
motivation]**, to **[core mechanism]**, and finally to **[applied/advanced usage]**. Each level
builds on the previous one.

**Caveat:** This outline covers **[Subject]** only. It intentionally excludes [adjacent topics
out of scope], except where a brief comparison aids understanding.

### Prerequisites (Level 0)

Before starting, be comfortable with:

* [Prerequisite 1]
* [Prerequisite 2]
* [Prerequisite 3]

### Level / Module Skeleton

This template's default output is a single skeleton file — Level headings, each listing that
Level's Module titles — **this is the entire deliverable.** No bullets, Outcome, Check, or
diagrams are written at this stage; those belong to a *later, separate* authoring pass per
Module (its own file, written after this skeleton exists), described at the bottom of this
section for reference. Module numbers are continuous across the whole outline (see the SOP's
Module numbering note), never restarted per Level, and match the filename planned for that
Module in the Index.

**Heading levels:** `#` for the outline's title only. `##` for every top-level section — the
prose around the header block, Claim, Prerequisites, each Level, Mini Project, Knowledge
Dependency Graph, Pareto Learning Order, and Index. Module titles are bullet list items under
their Level, never their own heading — this applies whether you're filling this in yourself or
handing it to an LLM (the Reuse Prompt states the same rule for that path).

**The skeleton (what this template actually produces)** — Level headings with Module titles only:

```text
## Level 1 — Why [Subject] Exists (The Problem)
  - Module 1: Motivation

## Level 2 — [Subject] Fundamentals
  - Module 2: Introduction to [Subject]

## Level 3 — Core Mechanism
  - Module 3: [Core Mechanism Name]

## Level N — Applied / Advanced Usage
  - Module N: [Applied Topic]
```

**Later, when you actually write a Module's own file** (e.g. `module-1-motivation.md`, per the
Index's planned filename) — this is the shape to follow at that point, not something produced by
this template's skeleton output:

```text
Module 1: Motivation
  * [Problem it solves]
  * [Pain point without it]
  Outcome: Understand *why* [Subject] exists before learning *how* it works.
  Check: [A question or tiny exercise that fails if the Outcome isn't actually true]
```

Use an ASCII diagram wherever a relationship needs a picture — again, only when authoring a
Module's own file later, e.g.:

```text
[Concept A]
      │
      ▼
[Concept B]
      │
      ▼
[Concept C]
```

### Level [N+1] — Mini Project

*(Lives in the main outline file, not any Module file.)* Replace `[N+1]` with the outline's
actual final Level number (e.g. "Level 6") — this is a placeholder like any other bracketed one,
not literal text to keep.

Build [a small, concrete artifact] that demonstrates:

* [Module topic used]
* [Module topic used]
* [Module topic used]

### Knowledge Dependency Graph

One diagram chaining every level in order — distinct from the small per-module diagrams above,
which explain a single concept's internal relationship rather than the whole outline's sequence.

```text
[Prerequisites]
    │
    ▼
[Level 1 topic]
    │
    ▼
[Level 2 topic]
    │
    ▼
[Level 3 topic]
    │
    ▼
[...continue through every level...]
```

### Pareto Learning Order (80/20)

If the goal is to become productive with **[Subject]** quickly rather than reach full mastery,
prioritize these modules in this order:

1. [Module]
2. [Module]
3. [Module]

[One sentence on why this subset is enough for working knowledge, and what's deferred.]

*(Skip this subsection for a short subject where the full outline already is the fast path —
state that explicitly rather than leaving it silently blank.)*

### Housekeeping

- Add a row to the Index table below whenever a new Module is planned — this is SOP step 10, not
  a one-time setup task; revisit it on every substantive edit that adds/removes a Module.
- **When you later actually write a Module's own file**, update its Index row: replace the
  plain-text planned filename with a real markdown link to the file, and refresh `Last Updated`.
  This is how the Index tracks progress from "planned" to "written" over time.
- Update `Last Updated` in the header on any substantive edit.
- Keep Orientation as prose that can grow; keep the Index a clean, scannable set of rows —
  don't let explanation creep into the table.

---

## Index

The manifest — one row per **planned** Module (see SOP step 10). Its filename starts as plain
text (the file doesn't exist yet) and becomes a real link once you actually write that Module's
content, per Housekeeping above. Delete the example row once real rows replace it.

| File | Module Description | Last Updated |
|---|---|---|
| `module-1-example.md` | Planned filename (plain text, not yet a link) and a one-line description of what this Module will cover — becomes a real link once the Module's own file is written | YYYY-MM-DD |

---

## Amending This Template

This template is the standalone, single source of truth for how to design a learning outline —
not a description of a process that lives elsewhere. When a real gap is found while actually
using it (a step that doesn't work, a check that fails to catch what it's supposed to, an
ambiguity that forced a judgment call), fix it here directly rather than treating the gap as a
one-off worked around silently.

**When to amend:** the moment a real gap surfaces while filling this template in for an actual
subject — don't wait for it to recur. A single sharp miss (a rule that let bad output through, a
missing worked example that caused confusion) is enough to amend immediately.

**How to amend:**
1. Make the actual edit to the relevant section (Definitions, SOP, Content Style, Index, etc.) —
   don't just note the problem without fixing it.
2. Add one row to the Amendment Log below: date, what triggered the change, and what changed.
   Keep it terse — this is a log, not a changelog essay.
3. If the amendment reverses or contradicts an earlier entry, say so in the new entry rather than
   silently overwriting the history of why the rule existed.
4. Keep every amendment **standalone-safe** — never reference a specific project's file paths,
   repo names, or folder structure in the fix itself (see the STANDALONE TEMPLATE note near the
   top of this file). A fix that only makes sense with knowledge of one specific repo defeats the
   point of a copy-anywhere template. Illustrative examples are fine; real links or path
   citations to files outside this template are not.
5. **Log hygiene:** once the Amendment Log passes roughly 15 entries, collapse entries older than
   the most recent 8 into a single "Earlier amendments" summary row (one sentence per theme, not
   a list of every historical entry) — this template is meant to stay short and copy-paste-able,
   not accumulate an unbounded process history.

### Amendment Log

| Date | Trigger | Change |
|---|---|---|
| 2026-07-30 (1-9) | **Earlier amendments** (collapsed per the Log Hygiene rule — the log passed its own ~15-entry threshold) | (1) File created: Orientation + Index structure, Claim/Prerequisites/Level-Module Skeleton, Mini Project, Knowledge Dependency Graph, Pareto Learning Order. (2) Added the SOP as an actual design process (brain-dump → prerequisites → clustering → module splitting → Outcome writing → capstone → Dependency Graph → Pareto), plus a Definitions glossary. (3) Added the three module-splitting tests, a final-pass re-run step, continuous whole-outline module numbering, and Outcome+Check pairing. (4) Added the Content Style section for the software-engineering-only domain. (5) Added SOP Step 0's Scope Check (outline-shaped + software-engineering-adjacent, both hard stops). (6) A `/quality-review` pass added SOP step 10 (Index production), a Reuse-Prompt-exclusion instruction, bracketed the `[N+1]` placeholder, and added a single-file Index example. (7) Fixed standalone-safety breaks — removed a broken repo-only link and several citations of specific external files, replaced with generic/hypothetical examples. (8) Added the ChatGPT Project persistent-setup option to the Reuse Prompt. (9) A live Spring Boot generation surfaced that guidance sections (Definitions/SOP/Content Style) were being inconsistently included/dropped from output; user set three requirements (output-only, Index renamed `Topic Description`, one file per Module) that were implemented as the era's default output shape — later itself reversed (see the 10th entry below). |
| 2026-07-30 | A live re-test of the file-per-Module fix (subject: Spring Boot) stopped dead after only producing Step 0's Scope Check report — the prompt's phrasing ("report your answer... before doing anything else") reads as an instruction to pause and check in, especially sitting next to the hard-stop-on-failure language, so the LLM treated a passing Step 0 as a natural stopping point instead of continuing into the outline | Reuse Prompt rewritten to explicitly require the entire response (Step 0 report + full outline) in one continuous turn, with no pause for confirmation — stopping is now reserved only for an actual failed Step 0 check, stated as the sole exception up front |
| 2026-07-30 | User clarified they don't want Step 0's reasoning surfaced at all when it passes — only the deliverable itself (outline + Knowledge Dependency Graph + Pareto Learning Order + Index), with no meta-commentary or process narration wrapping it | Reuse Prompt rewritten so Step 0 (and the rest of the SOP) is run silently as internal process, never narrated, when both checks pass — the failure case is the one exception, where the explanation of which check failed IS the entire response. Also updated the standalone ChatGPT Project instructions file to match |
| 2026-07-30 | A live test with the "single response, no pausing" rule collided with a genuine length limit — a 20-Module subject (Spring Boot) doesn't fit in one message, and the LLM sent a message announcing the length problem and asking permission for multi-part delivery, which is itself the same kind of unwanted meta-commentary/checkpoint the earlier fix was meant to eliminate | Reuse Prompt now explicitly distinguishes the two cases: never pause to ask permission to continue (unchanged), but multi-message continuation for genuinely large output is expected and must happen automatically, unannounced — no message may announce a length problem or ask whether multi-part delivery is acceptable. Propagated the same fix to the standalone ChatGPT Project instructions file |
| 2026-07-30 | The same length problem recurred immediately after the previous fix (large subjects still don't fit one message even without an announcement) — user decided the actual root cause was the file-per-Module output shape itself (added two amendments ago), not the messaging around it, and reversed that decision via `AskUserQuestion`: outputs should be a single skeleton file (Level headings + Module titles only), with full Module content (bullets/Outcome/Check/diagrams) written later, one Module at a time, in a separate authoring pass outside this template's generation flow | Reverted the file-per-Module requirement entirely (2nd reversal of this decision in one session — see the two amendments above). SOP step 5 now produces a one-line scope description instead of full Outcome+Check; SOP step 10 now plans filenames into the Index rather than creating files; the Level/Module Skeleton section shows the skeleton shape as the actual deliverable and moves the per-Module content shape to a clearly-labeled "write this later" reference; Content Style is now explicitly scoped to the later authoring pass, not skeleton generation; Index rows track "planned filename (plain text)" → "real link" as Modules are actually written. Propagated the same fix to the standalone ChatGPT Project instructions file |
| 2026-07-30 | User stated their actual underlying purpose: studying a new subject in correct pedagogical order — which retroactively explains and validates the skeleton-only decision made in the amendment directly above, rather than being a new requirement | Added a "Why this template produces a skeleton, not full content" rationale paragraph right after the file's opening description — pedagogical order is a sequencing problem, fully answered by Level/Module structure + Dependency Graph + Pareto Order, none of which require full content; full content is a separate, optional, sourced-elsewhere activity, not a missing piece of this artifact |
| 2026-07-30 | A live Spring Boot generation (screenshot-verified) confirmed every other fix in this session worked correctly, but the response started directly at the Claim paragraph — the title heading and the Category/Tags/Audience/Last Updated header block were both silently omitted, even though the Reuse Prompt already listed them as part of the single file's content | The header block requirement was buried as a parenthetical inside a longer prose bullet, easy for an LLM to deprioritize under a "produce only the deliverable" instruction. Rewrote Output Format as an explicit, numbered 10-part ordered list (title heading → header block → one-line description → Claim → Prerequisites → Levels → Mini Project → Dependency Graph → Pareto → Index), with parts 1-3 called out explicitly as "not optional" and an instruction to verify all ten parts are present before finishing. Propagated the same fix to the standalone ChatGPT Project instructions file |
| 2026-07-30 | User asked why the Index column was named `Topic Description` rather than `Module Description`, since every row represents a Module — the name traced back to the user's own literal wording when the three requirements were set two amendments prior (line ~450), not a deliberate distinct-vocabulary choice; `Module Description` is more consistent with the rest of the template's terms (Definitions, SOP, Content Style all say "Module," never "Topic") | Renamed the Index column from `Topic Description` to `Module Description` everywhere it appears in the live spec (Reuse Prompt constraints, SOP step 10, the Index section header and example row) and in the standalone ChatGPT Project instructions file. The historical amendment log entry that introduced the old name was left unedited, per this file's own "don't silently overwrite history" rule |
| 2026-07-30 | A live 69-module Spring Boot generation was otherwise fully compliant with every fix in this session, but Claim/Prerequisites used `##` while every Level heading used `#` — an inverted hierarchy (Levels outranking the title's own children) — since heading levels were never specified anywhere in the template | Added an explicit heading-level convention to the Reuse Prompt's Output Format section: `#` for the title only, `##` for every other top-level section, Module titles as bullet list items under their Level rather than headings of their own. Deliberately scoped narrowly to this one gap, not a general markdown style guide (line length, table formatting, etc.) — nothing else has been observed to need one. Propagated to the standalone ChatGPT Project instructions file |
| 2026-07-30 | `/quality-review` found four gaps: (1) the Amendment Log had grown to 17 entries, past its own ~15-entry collapse threshold, uncollapsed; (2) the output-exclusion list named only 5 items (Definitions/SOP/Content Style/Module numbering note/Reuse Prompt) while the Template block pastes the entire rest of the file, leaving Housekeeping/"Amending This Template"/Amendment Log free to leak into generated output; (3) the heading-level rule (added directly above) existed only in the Reuse Prompt, not the STANDALONE template body, breaking the two-places-by-design pattern every other rule follows — a human authoring manually had no guidance; (4) the skeleton code-block example showed Level lines with no `##` prefix, the only concrete example of the syntax the new rule requires | (1) Collapsed the first 9 entries into one themed summary row. (2) Extended the exclusion list to explicitly name Housekeeping, "Amending This Template," and the Amendment Log; propagated to the standalone ChatGPT Project instructions file. (3) Added the same heading-level rule to the Level / Module Skeleton section under Orientation, cross-referencing the Reuse Prompt as stating the same thing for the LLM path. (4) Added `##` prefixes to the skeleton code-block example's Level lines |
| 2026-07-30 | A live, screenshot-verified Spring Boot generation was otherwise fully compliant (heading levels fixed, no guidance-section leakage) but appended a trailing "Source template followed: [file]" line after the Index — a new meta-commentary variant the existing "no preamble" instructions never covered, since they only guarded the *start* of the response, not the end | Extended the Reuse Prompt (Output Format section) and the standalone ChatGPT Project instructions file: the Index is now explicitly stated as the last thing in the response, with no citation, source-template reference, or closing remark permitted after it |

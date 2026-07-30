# Module [N]: [Module Name]

<!--
STANDALONE TEMPLATE — copy this file anywhere; no dependency on this repo's other docs.

What this is: the template for ONE Module's own file — the thing you write, one at a time, after
a learning-outline-template.md skeleton already exists and its Index has a planned row for this
Module. This is a fill-in-the-blank reference, not an LLM-automated Reuse Prompt — you author
each Module's content yourself.

This copy has every rule explained inline, in comments, meant to be read once. Once you
understand the rules here, use `module-file-quick-template.md` (same folder) for actual
day-to-day authoring — it's the bare skeleton with no guidance comments, so there's nothing to
delete: copy it, fill in the few Outer placeholders, and paste your explanation straight into the
one Body slot.

Structure: Header (navigation/infrastructure) → Body (the actual teaching content) → Footer
(navigation/infrastructure). Header and Footer carry no domain knowledge of their own — they
exist only to place this Module in its outline's sequence and link to its neighbors. Everything a reader is supposed to learn lives in the Body.

**This file is a real explanation, not a bullet-point reference sheet.** The outline skeleton
(learning-outline-template.md) deliberately stayed terse — titles and one-line scopes, because its job is sequencing, not teaching. This Module file has the opposite job: it's where the actual teaching happens, so it needs to read like a real explanatory article — full paragraphs that teach a concept in depth, not a shorter, denser version of the outline skeleton.

**Primary shape: prose, using Topic structure** — Introduction (establish context and direction)
→ Main Body (develop and support ideas) → Conclusion (synthesize and close). Every Module uses
this same structure; it's the one inner shape this template follows.

Write in full sentences and paragraphs — explain *why*, not just *what*. A reader should finish
the Body actually understanding the concept, not holding a list of facts about it.

**Supporting devices — used WITHIN the prose where they help, never as a replacement for it:**

- **Bullets** — for a genuinely enumerable list inside a paragraph's explanation (e.g. "Spring Boot
  solves three problems:" followed by three items) — not the whole Module's content.
- **Numbered steps** — when a piece of the explanation is procedural ("to create a project: 1) ...
  2) ...").
- **A comparison table** — when the explanation's point is fundamentally "X vs. Y."
- **A diagram** — required, not optional, whenever the explanation describes a relationship, flow,
  or lifecycle — but the diagram supports the surrounding prose, it doesn't stand in for it.
- **A worked example** (code + explanation) — showing one concrete instance in full, embedded
  within the surrounding explanation of why it works that way.

**Learning Objective sits in the Header, stated upfront** — an advance organizer telling the
reader what to look for before they read, not just how to verify it after (this is the real
pattern already used elsewhere for actual explanatory Module content in this repo, not an
invented addition). Word it prospectively ("you will be able to..."), covering the same single
claim `Outcome` used to make retrospectively — don't state the same thing twice in two places.
`Check:` alone stays at the end, tied back to the Learning Objective already stated up top —
it's this template's verification mechanism, not content.

**Self-authoring checklist** — follow in order:

1. Open the parent outline's Index row for this Module — that's your committed starting scope
   (title + one-line description). Don't drift from it without also updating that row.
2. Write the **Learning Objective** first, before any explanation — state prospectively what the
   reader will be able to do. Writing it first forces you to know your own destination before you
   start explaining.
3. Draft the **Introduction** — establish context: why this matters, and how it connects to the
   Prerequisite Module (if one exists).
4. Draft the **Main Body** — the actual explanation, in full prose. Choose supporting devices
   (bullets, steps, a table, a diagram, a worked example) only where the content genuinely calls
   for one — don't reach for a device by default.
5. Draft the **Conclusion** — synthesize, and close the loop back to the Learning Objective you
   stated in step 2.
6. Write the **Check** — one concrete self-verification tied to the Learning Objective, not a
   generic "did you understand this?"
7. Fill in **See Also** — link the Prerequisite/Next Module only if that file already exists;
   omit the line otherwise. Link the parent outline.
8. Run a **Content Style pass** — backticked code/CLI/config terms, real API/tool names (not
   paraphrases), gotchas isolated into their own `Note:` line.
9. Go back to the parent outline's Index and update this Module's row: replace the plain-text
   planned filename with a real link, and refresh `Last Updated` — this is how the outline tracks
   which Modules are actually written vs. still planned.

Delete this whole comment block once the file is filled in.
-->

Part of **Level [N] — [Level Name]** in the **[Subject] Learning Outline**.

**Learning Objective:** [A single sentence stating what the reader will be able to explain or do
by the end of this Module — checkable by someone else observing them, not a preview of the
explanation's content. If it needs "and" to join two unrelated capabilities, this Module should
have been split into two.]

---

[Real explanatory prose goes here — Introduction → Main Body → Conclusion (Topic structure), per
the comment block above. Write this the way you'd actually explain the concept to someone, in
full sentences, not a bullet list standing in for an explanation. Use bullets/tables/diagrams/
steps as supporting devices inside the explanation where they genuinely help, not as the primary
content.]

Note: [A gotcha or pitfall worth calling out separately from the main explanation — omit this line
entirely if there isn't one]

---

**Check:** [One question or tiny exercise that would fail if the Learning Objective weren't
actually met — e.g. Objective "will be able to write a basic Dockerfile" pairs with Check "write
a Dockerfile for a static HTML site and build it without consulting notes."]

---

## See Also

- **Prerequisite:** [Module N-1: <Name>](module-<N-1>-<name>.md) — the Module this one directly
  builds on, if any (omit for a Level 1 / first Module)
- **Next:** [Module N+1: <Name>](module-<N+1>-<name>.md) — the Module that builds on this one, if
  any (omit for the final content Module)
- **Outline:** [<Subject> Learning Outline](<subject>-learning-outline.md) — the parent skeleton
  this Module belongs to

Only link real, already-written files — never a placeholder for a Module that hasn't been
authored yet. If the prerequisite or next Module doesn't exist as a file yet, omit that line
rather than linking to nothing.

---

<!--
Content Style — apply these rules within the prose above:

- Code, CLI, and config terms use backticks always — `@Autowired`, `docker compose up`,
  `application.yml` — never plain text.
- Prefer the real API/tool/annotation name over a paraphrase — write `@Transactional`, not "the
  transaction annotation." A reader should be able to look up the exact term you used.
- Any bullets/table cells used as supporting devices (not the Body's main structure) are still
  noun phrases or short fragments, not full sentences — e.g. `Bean Naming`, not "Beans can be
  given custom names." Numbered procedural steps are the exception — a step is an instruction, so
  a short imperative sentence is correct there, e.g. "Run `mvn spring-boot:run`."
- Gotchas and pitfalls get their own `Note:` line, separate from the main explanation.

Frontmatter — considered, not used: the parent outline's own Index already carries this Module's
filename, description, and last-updated date. A frontmatter block here would just duplicate it.

No Table of Contents by default — most single Modules are short enough not to need one. If a
Module's real explanation grows long enough to need one (multiple named sub-sections, several
worked examples), add a `## Table of Contents` section listing numbered links to this file's own
internal headings, right after the Header's opening line — that's a legitimate outcome of a
Module needing real depth, unlike the outline skeleton's own rule (where needing a TOC-length
explanation is a signal to split the *skeleton* into more Modules). A Module file is allowed to
be long if the subject genuinely needs it; the skeleton stage already did the splitting work.
-->

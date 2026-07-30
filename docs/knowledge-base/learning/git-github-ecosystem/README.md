# Git & GitHub Ecosystem

Learning notes on Git, GitHub, and GitHub CLI/Actions workflows — PR lifecycle, issue/project
management, CI integration, and common beginner-to-practical FAQ material.

This file does two jobs, kept in two separate sections below: **Orientation** (answer/topic
structure conventions used across these files) and **Index** (the manifest — the authoritative
list of every topic file, as one-line rows). See
[Manifest and Surrogate Pattern for Index Files](../../project/manifest-and-surrogate-pattern-for-index-files.md)
and the [README-as-Manifest Blueprint](../../project/readme-manifest-blueprint.md) for why this
split exists.

---

## Orientation

### Naming Convention

Files are named `topic-<N>.md`, numbered sequentially in creation order — not by subject. Use
the Index table below to find a file by subject; don't infer topic from the number.

### Inner Structure (Answer or Topic)

Two structural conventions used for the *body* of a `topic-N.md` file (the Outer Shell Structure
below wraps it in a Header and Footer), inherited from this repo's broader KB authoring
conventions:

- **Answer structure**: Short Answer → Detailed Answer → Bottom Line
- **Topic structure**: Introduction (establish context and direction) → Main Body (develop and
  support ideas) → Conclusion (synthesize and close)

### Outer (Shell) Structure

Every `topic-N.md` file wraps its actual content in the same shell, regardless of which inner
structure (Answer or Topic, above) that content uses. Markdown has no native header/footer
syntax (unlike HTML's `<header>`/`<footer>`), but the convention below plays the same role —
everything before the body is the **Header**, everything after it is the **Footer**:

**Header** (everything before the body):

1. `# Title` — one H1, the file's subject
2. One intro paragraph summarizing what the file covers (key terms bolded)
3. `## Table of Contents` — numbered links to the file's own internal sections, followed by any
   unnumbered trailing sections (e.g. a closing command sequence, "See Also") as plain bullet links
4. `---` divider

**Body** — this is where the **inner structure** (Answer or Topic, above) applies.

**Footer** (everything after the body):

1. A closing "reusable mental model" line or blockquote — one condensed takeaway, near the end
2. `---` divider
3. `## See Also` — bullet list linking to related `topic-N.md` files (or, occasionally, a real
   repo file this topic generalizes from), each with a one-line description of the relationship

**"See Also" — deliberate term choice, not the only one in this KB.** Sibling folders use
different footer headings for the same role: `docs/knowledge-base/project/` uses `## References`
(for citations to real, verified external/internal sources) or `## Related Articles` (for links
to other KB articles). This folder standardizes on `See Also` for both cases — every entry links
to either a sibling `topic-N.md` or a real repo file, never a placeholder for content that
doesn't exist yet. If a related topic hasn't been written, don't list it under "See Also" until
it has a real file to link to.

By the stricter distinction some style guides draw (e.g. Wikipedia's Manual of Style): "See Also"
is for tangentially related material a reader might explore next, while "References" is
specifically for sources an article's own content was actually derived from. This folder
deliberately merges the References role into "See Also" rather than adding a second heading —
e.g. `topic-4.md` and `topic-20.md` each cite a real repo rule file (`.claude/rules/common/...`)
their content generalizes from, which is a References-shaped citation filed under See Also. This
is a proportionate simplification for a small, informal topic series (2 such citations across 20
files as of 2026-07-30), not an oversight — stated explicitly here so it reads as a decision, not
an unacknowledged blending of two distinct concepts.

Applied retroactively to `topic-1.md` through `topic-20.md` (2026-07-30) so every file in this
folder shares the same shell — new topics should follow this shape from creation, not just be
retrofitted later.

**Frontmatter — considered, not used.** A YAML frontmatter block (or the `**Category:**` /
`**Tags:**` / `**Audience:**` header this KB's parent `learning/` folder uses elsewhere) was
weighed as part of this shell and deliberately left out: these files are a numbered FAQ/topic
series read via the Index table above, not standalone articles that need their own discoverable
metadata — the Index already carries the one-line description a frontmatter block would
otherwise duplicate. State this explicitly rather than leaving the omission unexplained if the
question comes up again.

### Housekeeping

- Add a row to the Index table below when creating a new `topic-N.md` file.
- Keep one topic per file — if a file starts covering two unrelated questions, split it.

---

## Index

The manifest — one row per topic file, no prose beyond this table.

| File | Topic |
|---|---|
| [topic-1.md](topic-1.md) | Full PR workflow lifecycle using GitHub CLI (`gh`) — branch, commit, push, PR create/review/merge, cleanup — with a complete reusable command sequence |
| [topic-2.md](topic-2.md) | Can you switch branches mid-work and come back later? Branch-switching mechanics and safety |
| [topic-3.md](topic-3.md) | Git and GitHub FAQ, beginner → practical developer level |
| [topic-4.md](topic-4.md) | Automated Testing → Quality Gate → CI → Pull Request as a closed-loop quality control system |
| [topic-5.md](topic-5.md) | Can a Pull Request exist without CI? |
| [topic-6.md](topic-6.md) | Issues, Projects, and Views as a work management system around a repository |
| [topic-7.md](topic-7.md) | Milestone vs. Epic vs. Sprint — different ways to organize work and the questions each answers |
| [topic-8.md](topic-8.md) | GitHub issue creation walkthrough using GitHub CLI |
| [topic-9.md](topic-9.md) | GitHub issue creation with label, milestone, and project using GitHub CLI |
| [topic-10.md](topic-10.md) | Two-layer mental model for a production-style GitHub software delivery workflow |
| [topic-11.md](topic-11.md) | Worked example: BuildNest e-commerce "Add Product Review Feature" end-to-end |
| [topic-12.md](topic-12.md) | CI with Pull Request using GitHub Actions — end-to-end walkthrough |
| [topic-13.md](topic-13.md) | FAQ: GitHub professional workflow (Issue → Project → PR → CI → Merge) |
| [topic-14.md](topic-14.md) | Common interview questions on Git, GitHub, and GitHub Actions, with answers |
| [topic-15.md](topic-15.md) | Difference between Pull and Pull Request |
| [topic-16.md](topic-16.md) | Can a Pull Request exist without GitHub? |
| [topic-17.md](topic-17.md) | Difference between Git merge and GitHub merge |
| [topic-18.md](topic-18.md) | Difference between Git Push and Pull Request |
| [topic-19.md](topic-19.md) | Why Git/GitHub/GitHub Actions are hard for beginners — distributed collaboration model, state management, and automated delivery workflow as three distinct learning burdens |
| [topic-20.md](topic-20.md) | Feature branching for parallel, independent issue development |

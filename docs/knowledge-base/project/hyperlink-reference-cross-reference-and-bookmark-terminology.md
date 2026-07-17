---
title: "Hyperlink, Cross-Reference, Reference, Inline Hyperlink, and Bookmark — Precise Terminology"
category: documentation
tags: [markdown, documentation, terminology, linking]
keywords: [hyperlink, cross-reference, reference, inline link, reference-style link, bookmark, anchor, fragment identifier, clickable link, wikilink, dynamic field, static link, microsoft word, bidirectional, unidirectional, backlink, related_articles]
objective: "What's the precise difference between a reference, a cross-reference, a hyperlink, an inline hyperlink, and a bookmark — terms that get used interchangeably but name distinct, nested concepts?"
audience: "Anyone writing or reviewing markdown documentation in this repo who needs to distinguish 'named but not clickable' from 'clickable' from 'the clickable target itself.'"
scope: general
source_conversations: ["Session 2026-07-16, following work on .claude/commands/critique-prompt.md"]
last_updated: 2026-07-16
confidence: high
evidence_strength: strong
related_articles: [manifest-and-surrogate-pattern-for-index-files.md]
status: published
---

# Hyperlink, Cross-Reference, Reference, Inline Hyperlink, and Bookmark — Precise Terminology

## What Is It?

Five related but distinct terms for "pointing at something else" in documentation, each naming a
different part of the same underlying idea:

- **Reference** — the broadest term. Naming or identifying something else so a reader knows what's
  being discussed, with no requirement that it be clickable.
- **Cross-reference** — a reference specifically *between two things in the same corpus* (two
  sections, two documents in the same repo), emphasizing the relationship between them. Can be
  clickable or not.
- **Hyperlink** — any clickable, navigable pointer to a destination — a file, a section, an
  external URL. Defined by clickability, not by syntax.
- **Inline hyperlink** — the specific markdown syntax `[text](url)`, where the destination is
  written directly at the point of use, as distinct from a *reference-style* hyperlink where the
  URL is defined once elsewhere and referred to by a label.
- **Bookmark** — a named anchor point *inside* a document (an `#id`/heading-slug fragment). It
  marks a landing spot; it is not itself clickable and does not point anywhere — it's the target
  a hyperlink can point *at*, not the pointer.

## Why It Matters

These terms nest inside each other, and conflating them causes real, checkable mistakes — not just
imprecise language. A markdown reference written as a bare filename or a bare `[[wikilink]]` reads
to a human as if it's navigable, but isn't; this KB's own "Cross-Referencing Between Articles"
section (`README.md`) documents exactly this mistake being made and reversed once already (bare
`[[wikilink]]`s were briefly recommended, then found to render as literal, non-clickable text in
GitHub and every standard markdown viewer). Knowing that "reference" does not imply "hyperlink,"
and that a "bookmark" is a target rather than a pointer, is what makes it possible to say precisely
what's wrong with a given link and how to fix it — "this is a reference but not a hyperlink" is a
different, more actionable diagnosis than "this link doesn't work."

## How It Works

### The nesting relationship

```
reference
└── cross-reference (a reference between two things in the same corpus)
└── hyperlink (a reference that is also clickable/navigable)
    └── inline hyperlink   [text](url)
    └── reference-style hyperlink   [text][label] ... [label]: url

bookmark — a named anchor *inside* a document; a hyperlink's possible target, not a hyperlink itself
```

Every hyperlink is a reference (it names/points at something), but not every reference is a
hyperlink (plain prose naming a file by path is a reference, not a hyperlink, unless written as a
markdown link). A cross-reference *can* be implemented as a hyperlink — and in this repo's
convention, always should be (see When to Use It) — but the word itself describes the
*relationship* between two things, not the clickability mechanism.

### Inline vs. reference-style hyperlinks

Both are equally clickable; they differ only in where the URL is written:

```markdown
<!-- Inline hyperlink: URL written directly at the point of use -->
See [work-on-issue.md](../commands/work-on-issue.md) for the full prompt.

<!-- Reference-style hyperlink: URL defined once, referenced by a label -->
See [work-on-issue.md][woi] for the full prompt.

[woi]: ../commands/work-on-issue.md
```

Reference-style is useful when the same URL is cited many times, or to keep long URLs out of
running prose; inline is simpler for a one-off link and is what this KB's own convention (see
below) specifies.

### Bookmarks as hyperlink targets

A bookmark is a fragment identifier attached to a location inside a document — typically a heading
slug (`#contributing`, `#necessity-tags`). It has no clickable behavior on its own; a hyperlink
becomes able to jump to a specific spot within a file only by including a bookmark in its target
URL:

```markdown
[README.md's Contributing section](../../wiki/learned-lessons/README.md#contributing)
```

Here, `#contributing` is the bookmark (the landing spot), and the whole markdown link around it is
the hyperlink (the clickable arrow). The bookmark is useless without something linking to it; the
hyperlink's ability to land on a precise sub-section depends entirely on the bookmark existing.

### Dynamic vs. static cross-references (Word vs. markdown)

Word Processors like Microsoft Word have a dedicated **Cross-reference** field (References ribbon
→ Captions group) that inserts a *dynamic* field pointing at a bookmark, heading, footnote, or
caption elsewhere in the same document. Its displayed text ("see Figure 3," "see page 12")
auto-updates if the target is renumbered, retitled, or moved — the field re-resolves itself, the
author never edits the displayed text by hand.

A markdown cross-reference is the same *concept* — a pointer from one place to a related place in
the same document/corpus — but a fundamentally *static* mechanism: a hyperlink to a bookmark
(`[see Section 3](#section-3)`). If the target heading's text or slug changes, the link's display
text and its `#fragment` both have to be updated by hand; nothing re-resolves automatically. This
is a direct consequence of markdown being plain, static text with no live document model behind
it, unlike Word's field-based architecture. Practically: a markdown cross-reference is more
fragile to rename/reorder churn than Word's, and is exactly why link-checking (rather than trusting
a link was correct when written) matters more in markdown-based documentation than in a live
word-processor document.

### Unidirectional vs. bidirectional hyperlinks

This is a separate axis from everything above — it's about *navigability direction*, not
clickability or targeting. A **unidirectional hyperlink** lets a reader go from the source to the
target, but the target has no automatic way back — the relationship is known only to the source
document's markup. Plain markdown links are unidirectional by default: linking from A to B doesn't
give B any awareness that A points at it.

A **bidirectional hyperlink** is one where both documents can navigate the relationship — either
because each side independently contains a link to the other (two separate unidirectional links,
kept in sync by hand), or because the linking system itself tracks the relationship and
auto-generates the reverse pointer (the "backlinks" feature in tools like Obsidian, Roam, or
Notion — the target gains a "linked from here" entry automatically the moment the source links to
it, with no edit required on the target's side). Plain markdown and GitHub-rendered `.md` files
have no such automatic mechanism.

This directly governs how this KB's own `related_articles` frontmatter field behaves: listing
`related_articles: [X]` in this article only creates a unidirectional link to X. For the
relationship to be bidirectional, X's own frontmatter needs `related_articles` updated by hand to
point back — this KB's "Cross-Referencing Between Articles" convention doesn't currently require
or automate that reciprocal edit, so cross-references here are unidirectional unless an author
deliberately adds the link on both sides.

## When to Use It

- **Use a hyperlink, not a bare reference**, whenever you're pointing a reader at another file or
  section they might plausibly want to open — this repo's own convention (README.md's
  "Cross-Referencing Between Articles" section) mandates real relative markdown links for every
  cross-reference in this KB and explicitly bans bare `[[wikilink]]`s, for the reason given above.
- **Use inline hyperlinks by default** in this repo — it's the form this KB's Article Template and
  every existing article already use; reach for reference-style only if the same URL needs citing
  many times in one document.
- **Use a bookmark (`#fragment`)** whenever the destination is a specific section of a longer
  document, not the whole file — e.g. pointing at one step in `development-workflow.md`'s Sequence
  table or one subsection of a long README, rather than making a reader search the whole file.
- **Add the reciprocal link by hand when a relationship genuinely matters both ways** — this KB's
  `related_articles` field doesn't auto-generate backlinks, so a one-sided `related_articles` entry
  is a deliberate choice (X is relevant context for reading this article, but this article isn't
  necessarily relevant to X) unless both sides are updated to make it bidirectional.

## Examples

**Reference, not a hyperlink (before a fix):** `.claude/commands/critique-prompt.md` originally
read `Prompt: see \`.claude/commands/work-on-issue.md\`` — a plain reference (named, in backticks)
with no navigability. Flagged as "not clickable" and fixed.

**Inline hyperlink (the fix):** Changed to
`Prompt: see [work-on-issue.md](../commands/work-on-issue.md)` — now a real inline hyperlink,
clickable in any standard markdown renderer, and also a cross-reference (both files live in the
same repo).

**Bookmark as a hyperlink target (real, existing in this repo):**
`development-workflow.md`'s `lessons-learned` step links
`docs/wiki/learned-lessons/README.md#contributing` — `#contributing` is the bookmark marking that
README's "Contributing" heading; the surrounding markdown link is the hyperlink that lands on it
directly, rather than requiring a reader to scroll through the whole file.

## Synthesis

The five terms describe one continuum from *loosest* to *most specific*: a reference just names
something; a cross-reference names something in the same corpus; a hyperlink is a reference made
clickable; an inline hyperlink is one specific clickable syntax; and a bookmark is the named target
a hyperlink can land on inside a document. Getting the vocabulary precise isn't pedantry — it's
what makes "this isn't working" resolvable into a specific, fixable diagnosis (missing markdown
syntax vs. missing bookmark vs. correct but non-cross-referential) rather than a vague complaint.

## Quick Reference

| Question | Answer |
|---|---|
| Is every reference a hyperlink? | No — a reference just names something; it needs markdown link syntax to become a hyperlink |
| Is every hyperlink a cross-reference? | No — a hyperlink to an external URL (not part of this repo/corpus) is a hyperlink but not a cross-reference |
| What's the difference between inline and reference-style hyperlinks? | Both are clickable; inline writes the URL at the point of use, reference-style defines it once and refers to it by a label |
| Is a bookmark clickable? | No — it's the target a hyperlink points at, not a pointer itself |
| Does this repo allow bare `[[wikilink]]`s? | No — banned repo-wide in this KB and in `docs/wiki/learned-lessons/`; they render as literal, non-clickable text |

## Related Articles

- [Manifest and Surrogate Pattern for Index Files](manifest-and-surrogate-pattern-for-index-files.md) — a related but distinct concept: a manifest's rows are typically hyperlinks to their target files, using exactly the inline-hyperlink convention this article defines

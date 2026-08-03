---
title: README-as-Manifest Blueprint
category: documentation
tags: [manifest, index-file, readme, template, blueprint]
keywords: [README template, manifest template, orientation section, index section, directory index]
objective: Provide a copy-paste starting structure for any directory README that needs to double as orientation (what is this, how to contribute) and a manifest (authoritative inventory of its files).
audience: Anyone starting a new directory-level README in this or another repo that will grow to hold multiple independent files needing an index.
scope: general
source_conversations: []
last_updated: 2026-07-13
confidence: high
evidence_strength: moderate
related_articles: [manifest-and-surrogate-pattern-for-index-files.md, content-extraction-dry-ssot-as-the-decision-principle.md]
status: published
---

# README-as-Manifest Blueprint

A reusable structure for a directory `README.md` that needs to do two jobs at once: orient a
reader (what this directory is, how to add to it) and serve as the authoritative index of every
file inside it. See
[Manifest and Surrogate Pattern for Index Files](manifest-and-surrogate-pattern-for-index-files.md)
for the concept this blueprint implements — this article is the practical template, that one is
the underlying theory.

## When to Use This

Reach for this structure when a directory:

- Holds a growing set of **independent, standalone files** (no required reading order between
  them) — if files depend on being read in sequence, this isn't the right shape; see that
  concept article's "When to Use It" section for the Chapter-format alternative.
- Is genuinely expected to **accumulate files over time**, not a fixed, small set that will never
  grow past a handful of entries (a manifest earns its keep once scanning becomes cheaper than
  opening every file).
- Needs a **landing page** a reader hits automatically (GitHub/GitLab auto-render, an IDE file
  browser) where combining orientation and inventory in one file is more convenient than two.

Skip it for a directory with 2-3 files that will stay that way, or where a plain flat list of
links is already sufficient — this structure is worth the ceremony only once the manifest half
needs its own note-taking (categories, dates, one-line descriptions) to stay scannable.

## The Two-Section Skeleton

```markdown
# <Directory Name>

<One or two sentences: what durable purpose this directory serves. Point to a sibling
directory if there's a related-but-distinct concern that lives elsewhere, so a reader
doesn't file something in the wrong place.>

This file does two jobs, kept in two separate sections below: **Orientation** (what this
directory is, how to categorize and add to it) and **Index** (the manifest — the
authoritative list of every file, as one-line rows). Orientation content is prose that can
grow; the Index table should stay a clean, scannable set of rows, not accumulate
explanation of its own.

---

## Orientation

### Taxonomy
<!-- Optional — only if files are grouped by category/type. A table of category → description
     is enough; skip this subsection entirely if there's no meaningful grouping. -->

### Naming Convention
<!-- One or two sentences: the filename pattern new files should follow, and where any
     structured metadata (date, version, owner) belongs instead of the filename. -->

### <File-Type Template>
<!-- If files in this directory share a common internal structure (frontmatter schema, required
     sections), put the copy-paste template here. Skip if files are genuinely free-form. -->

### Authoring Guidelines
<!-- The rules for adding or editing a file: format selection, structure, size discipline,
     when to split one file into several, cross-referencing conventions. Keep each as its own
     ### subsection so a contributor can jump straight to the one they need. -->

#### Housekeeping
- Add a row to the Index table below when creating a new file.
- Update any "last updated" metadata on substantive edits.
- Cross-reference sibling directories instead of duplicating their content here.

---

## Index

The manifest — one row per file, no prose beyond this table.

| Sr. No. | File | Topic | Category | Last Updated |
|---|---|---|---|---|
| 1 | [example-file.md](example-file.md) | One-line description of what this file covers | <category> | YYYY-MM-DD |
```

**`Sr. No.` exists solely so the last row's number answers "how many files are in this manifest"
without counting rows or running `grep -c` — it is not a reference key.** It is purely positional:
renumber it whenever a row is inserted, deleted, or reordered, the same way you'd renumber a
numbered list. Never cite a row by its `Sr. No.` from outside this table (that number shifts the
next time the table changes) — the `File` column's filename is the stable, citable identifier, if
one is ever needed. This is a deliberate difference from a real ID column: an ID is assigned once
and never renumbered even as the table changes; `Sr. No.` has no such requirement precisely
because its only job is a live count, not identity.

## How to Adapt It

1. **Keep the two-section split even if one side starts small.** A one-paragraph Orientation
   section is still worth separating from the Index — it's the boundary that matters, not the
   current size of either side. Collapsing them back into one undifferentiated file is the
   failure mode this blueprint exists to prevent.
2. **Drop subsections that don't apply.** Taxonomy and a file-type template are both optional —
   a directory of genuinely free-form, uncategorized files doesn't need either. Don't leave an
   empty placeholder subsection; delete it.
3. **Match the Index table's columns to what's actually useful to scan by.** Sr. No. / File /
   Topic / Category / Last Updated is a reasonable default (used in this KB and in
   `docs/wiki/learned-lessons/README.md`), but a directory with no category concept, or one
   where recency doesn't matter, should drop those columns rather than leaving them always `—`.
   Keep `Sr. No.` even when dropping others — it's the cheapest column in the table and the only
   one that answers "how many files does this manifest have" without counting.
4. **Point Index consumers back at this pattern once, not per-directory.** A single line linking
   to [Manifest and Surrogate Pattern for Index Files](manifest-and-surrogate-pattern-for-index-files.md)
   is enough — don't re-explain the theory in every directory that uses this blueprint.

## Worked Reference

[docs/knowledge-base/project/README.md](README.md) is the concrete instance this blueprint was
extracted from — its Orientation section covers Taxonomy, Frontmatter Schema, Article Template,
and Authoring Guidelines (with Format Selection, Article Structure, Size Discipline, When to
Split, Extraction, Cross-Referencing, and Housekeeping as its own subsections), and its Index
section is exactly the bare manifest table shape shown above. Use it as a live example when this
blueprint's abstract skeleton needs a concrete comparison.

## Related Articles

- [Manifest and Surrogate Pattern for Index Files](manifest-and-surrogate-pattern-for-index-files.md) — the underlying concept (indirection, surrogate records) this blueprint's Index section implements
- [Content Extraction: DRY/SSOT as the Decision Principle, Not Size](content-extraction-dry-ssot-as-the-decision-principle.md) — the decision test for what belongs as its own file (an Index row) vs. inline in Orientation prose

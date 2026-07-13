---
title: Manifest and Surrogate Pattern for Index Files
category: documentation
tags: [manifest, indirection, surrogate, index-file, knowledge-organization]
keywords: [manifest, table of contents, registry, index, surrogate record, indirection, pointer table, MEMORY.md, learned-lessons README]
objective: What is the technical term and underlying mechanism for a file that lists other files with one-line metadata rows instead of containing their content, and how does it differ from a table of contents, registry, or changelog?
audience: Anyone authoring or maintaining an index-style file in this repo (a directory README, a memory index, a KB index) who needs to know what pattern they're implementing and why it works.
scope: general
source_conversations: []
last_updated: 2026-07-13
confidence: high
evidence_strength: moderate
related_articles: [content-extraction-dry-ssot-as-the-decision-principle.md, stable-id-columns-decouple-cross-references-from-display-order.md]
status: published
---

# Manifest and Surrogate Pattern for Index Files

## What Is It?

A **manifest** is a file that declares the contents of a set of related artifacts by listing
each one alongside descriptive metadata, without embedding the artifacts' actual content. Its
job is enumeration and description, not storage. Four properties make a file a manifest rather
than just "a doc with links in it":

1. **Enumerates discrete external artifacts** — each entry points to a separate file/resource
   that exists independently of the manifest.
2. **Carries metadata per entry** — not just a name and a link, but fields (category, date,
   type, version) that let a reader act on the entry without opening the target.
3. **Is authoritative for "what exists in the set"** — adding or removing an artifact requires
   updating the manifest; it is the canonical inventory, not an optional convenience.
4. **Holds no primary content itself** — the substance lives entirely in the files it points to.

Each row inside a manifest is a **surrogate record** (also called a pointer record, or in
library science a catalog record): a minimal stand-in for a full artifact that lets a reader
know *about* something without loading it. This is the same principle as a pointer in
programming — an address plus a small descriptor, not the object itself — applied to documents
instead of memory addresses.

## Why It Matters

Two systems in this repo already are manifests, whether or not they were built with that word
in mind: `docs/wiki/learned-lessons/README.md` and `~/.claude/projects/<project>/memory/MEMORY.md`.
Both are a table of one-line rows, each pointing at a standalone file carrying the actual lesson
or memory content. Naming the pattern explicitly matters for three reasons:

- **It clarifies the file's job when editing it.** A manifest should never grow prose content of
  its own past a short definition/contributing section — content additions belong in a new
  target file plus a new row, not in the manifest body. Recognizing "this is a manifest" heads
  off the temptation to just write the new material inline.
- **It explains the truncation/read-cost design already in place.** `MEMORY.md` is explicitly
  capped and truncated past 200 lines (per its own governing instructions) precisely because a
  manifest is supposed to stay cheap to scan even as the underlying content set grows — that's
  the indirection principle's whole payoff (see How It Works).
- **It gives a vocabulary for distinguishing it from lookalikes** — a table of contents, a
  registry, an index, a changelog — which matters when deciding whether a new file being
  planned should behave like one of these or something else entirely.

## How It Works

### The mechanism: indirection via surrogate

A manifest is built from **indirection**: decoupling the *reference* to an artifact from the
artifact's *content*. Instead of one large file containing everything, the structure splits
into:

- **N target files**, each self-contained and independently readable.
- **One manifest**, containing N surrogate rows, each a minimal descriptor of one target file.

```
manifest.md
├── row 1 (surrogate) ──points to──> target-file-1.md (full content)
├── row 2 (surrogate) ──points to──> target-file-2.md (full content)
└── row N (surrogate) ──points to──> target-file-N.md (full content)
```

This buys three properties, all visible in this repo's own manifests:

1. **Bounded read cost.** Scanning the manifest costs one line per artifact regardless of how
   long any individual target file is. A reader — human or an AI assistant loading memory files
   into context — pays the full cost of a target file only when it's actually relevant. This is
   exactly why `MEMORY.md` truncates past 200 lines: past that point the manifest itself would
   stop being cheap to scan, defeating the point of having one.
2. **Independent churn.** The surrogate row and its target file can change on different
   schedules. Editing a lesson's internal wording never requires touching the index; adding a
   new lesson touches exactly one new row plus one new file.
3. **Authoritative inventory.** The manifest is the single place that answers "what exists in
   this set" — this is why both `docs/wiki/learned-lessons/README.md`'s Contributing section and
   this KB's own Housekeeping section (below) require updating the manifest whenever a file is
   added, not backfilling it later.

### Manifest vs. lookalikes

Raw length or "has links in it" is a weak test — several structures look superficially similar
but solve a different problem:

| Pattern | Distinguishing trait | Example in this repo |
|---|---|---|
| **Manifest** | Enumerates independent artifacts + per-entry metadata; order-agnostic; authoritative inventory | `docs/wiki/learned-lessons/README.md`, `MEMORY.md`, this KB's own Index table |
| **Table of contents** | Ordered for sequential reading — entries are dependent on reading order (chapters) | Not currently used in this repo (see Format Selection in this KB's own README — chapters aren't a format this KB uses) |
| **Index (lookup)** | Optimized for keyed search, may lack rich per-entry metadata | A book's back-of-book index; a database index |
| **Registry** | Implies a formal register/deregister process with identity tracking, not just "add a row" | Not used in this repo's docs; closer to a runtime concept (service registry) |
| **Changelog** | Chronological record of *changes over time*, not a current-state inventory | `CHANGELOG.md` — records what happened, not what currently exists |

The practical test: **if removing the manifest would make it impossible to know what artifacts
exist and what they're for, but each artifact would still work fine standalone, it's a
manifest.** A table of contents fails this test in the opposite direction — remove it and the
content still exists and is still readable, just harder to navigate in order.

## When to Use It

Reach for the manifest + surrogate pattern when:

- A set of documents is genuinely independent (each stands alone, no required reading order) —
  see this KB's own "One Topic Per File" rule, which produces exactly this shape.
- The set is expected to grow over time, and an unbounded single file would eventually become
  too expensive to read in full on every access (this is the same trigger reasoning behind this
  KB's Size Discipline section and `~/.claude/rules/memory-hygiene.md`'s lesson-file
  consolidation threshold).
- Readers need to scan for relevance *before* committing to reading a full artifact — the
  surrogate's metadata columns (category, date, one-line topic) exist specifically to support
  this triage step.

Do not reach for it when the content genuinely has a required reading order (that calls for a
Chapter-style sequence per this KB's Format Selection guidance, or a numbered procedural
document like `development-workflow.md`'s Sequence table — which is itself a manifest-adjacent
structure, but ordered and ID-referenced rather than an unordered lookup set).

## Examples

**`docs/wiki/learned-lessons/README.md`** — a manifest whose surrogate rows carry File / Topic /
Category / Last Updated columns, pointing at standalone lesson files. Its own Contributing
section states the authoritative-inventory property explicitly: "Update this README when adding
or significantly updating a file."

**`MEMORY.md`** (this project's auto-memory index) — a manifest with a stricter constraint: rows
past line 200 are truncated by the harness itself, which is the indirection principle's cost
bound made mechanical rather than just a style guideline.

**This KB's own Index table** (above, in `docs/knowledge-base/project/README.md`) — the same
structure one level up: File / Topic / Category / Last Updated rows pointing at every article in
this directory, including this one once added.

## Synthesis

"Manifest" and "surrogate record" aren't just more precise vocabulary for something already
understood intuitively as "a table of pointers" — naming the pattern surfaces the actual design
constraint it exists to satisfy: bounded read cost via indirection. That constraint is what
explains otherwise-arbitrary-looking details already present in this repo, like `MEMORY.md`'s
200-line truncation and the repeated instruction across multiple rule files ("update the index
when you add a file," "don't inline new content into the manifest") that would otherwise read as
disconnected housekeeping rules rather than one consistent structural principle applied in three
places.

## Related Articles

- [Content Extraction: DRY/SSOT as the Decision Principle, Not Size](content-extraction-dry-ssot-as-the-decision-principle.md) — the companion decision about *when* to split content out into its own target file, which is the other half of what makes a manifest useful (a manifest with nothing extracted into standalone targets is just a list)
- [Stable ID Columns Decouple Cross-References from Display Order](stable-id-columns-decouple-cross-references-from-display-order.md) — a related surrogate-key pattern, applied to rows within a single ordered table rather than to a manifest's cross-file rows

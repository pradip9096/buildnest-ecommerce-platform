---
title: "YAML Frontmatter Needs Parser Validation, Not Just Visual Review"
category: documentation
tags: [yaml, frontmatter, markdown, validation, tooling]
keywords: [yaml comment bare hash, flow sequence unclosed, plain scalar colon space, mapping values are not allowed here, source_conversations issue number, yaml.safe_load]
source_conversations: ["Session 2026-07-13"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "two YAML syntax rules are easy to violate while writing prose-like frontmatter values by hand and invisible on visual review: a bare # preceded by whitespace starts a comment even inside a flow sequence or plain scalar, and a colon immediately followed by whitespace inside an unquoted plain scalar is read as a mapping-key indicator — both silently break parsing while still looking correct to a human skim"
impact: "medium — 10 of 82 frontmatter files across this repo's KB/lesson directories failed to parse, 6 of which were written in the same session that introduced this lesson, discovered only because a tool downstream of frontmatter (not visual review) surfaced the failure"
related_lessons: []
---

# YAML Frontmatter Needs Parser Validation, Not Just Visual Review

## Problem

While creating a new KB article with `source_conversations: [Session 2026-07-13, issues #122 and
#326]` in its frontmatter, a downstream tool reported:

```
Failed to parse frontmatter
Flow sequence in block collection must be sufficiently indented and end with a ] at line 9, column 1
```

The file looked completely correct on visual review — properly closed brackets, sensible content,
no obvious typo. A repo-wide scan (`yaml.safe_load` against every frontmatter block under
`docs/wiki/learned-lessons/`, `docs/knowledge-base/project/`, and `docs/knowledge-base/learning/`)
found **10 of 82 files** had the same class of bug, silently broken the entire time. Six of those
ten had been written or edited in the *same session*, during an earlier frontmatter-backfill pass
— meaning the bug had already been introduced and gone unnoticed across multiple prior tool calls
in the same conversation, since nothing ever actually parsed the YAML.

## Why This Is Non-Obvious

Three distinct YAML syntax rules, all invisible to a human visually skimming prose-like
frontmatter values:

1. **A bare `#` preceded by whitespace starts a comment**, even inside a flow sequence
   (`[a, b, #123]`) or an unquoted plain scalar. `source_conversations: [Session X, issue #329]`
   reads naturally as English to a human — "issue number 329" — but to a YAML parser, the space
   before `#` triggers comment-start, silently truncating everything after it, including the
   sequence's closing `]`.
2. **A colon immediately followed by whitespace inside an unquoted plain scalar is read as a
   mapping-key indicator.** `impact: low — ... the run:-vs-uses: distinction was identified ...`
   looks like ordinary prose, but the second `uses:` (colon-space) makes YAML think a nested
   mapping is starting mid-scalar.
3. **The same colon-space rule applies to `title:` values that themselves describe something
   containing a colon** — e.g. a title like `Apply Every Applicable domain: Label, Not Just the
   Most Obvious One` breaks for the identical reason.

All three produce a parse failure only when something actually calls a YAML parser against the
file — `git diff`, a text editor, and a careful human read all show a file that looks completely
fine, because the visual rendering doesn't distinguish "prose that happens to contain `#`/`:`"
from "YAML syntax that means something."

## Fix

Quote the entire scalar value whenever it contains a bare `#` (after whitespace) or a colon
followed by whitespace:

```yaml
# Wrong — silently truncates at the space before #
source_conversations: [Session 2026-07-07, issue #68]

# Correct — quoted, the # and everything after it is literal text
source_conversations: ["Session 2026-07-07, issue #68"]
```

```yaml
# Wrong — "uses:" mid-sentence is read as a new mapping key
impact: low — ... the run:-vs-uses: distinction was identified ...

# Correct
impact: "low — ... the run:-vs-uses: distinction was identified ..."
```

```yaml
# Wrong
title: Apply Every Applicable domain: Label, Not Just the Most Obvious One

# Correct
title: "Apply Every Applicable domain: Label, Not Just the Most Obvious One"
```

For a flow sequence where only one element is the problem, quoting just that element is enough
(`keywords: [normal phrase, "project #9", another phrase]`) — the whole sequence doesn't need
quoting, only the offending item.

## How to Apply

**Run a parser, don't eyeball it.** After writing or editing any file with YAML frontmatter,
validate it mechanically before considering the edit done:

```bash
python3 -c "
import yaml
with open('path/to/file.md') as f:
    content = f.read()
fm = content.split('---')[1]
yaml.safe_load(fm)
print('OK')
"
```

For a bulk check across many files at once (worth running periodically, not just after a single
edit — this is the same "periodically re-check, don't assume a prior pass caught everything"
principle already established elsewhere in this repo's own conventions):

```bash
python3 -c "
import yaml, glob
failed = []
for path in glob.glob('docs/**/*.md', recursive=True):
    with open(path, encoding='utf-8') as f:
        content = f.read()
    if not content.startswith('---'):
        continue
    parts = content.split('---', 2)
    if len(parts) < 3:
        continue
    try:
        yaml.safe_load(parts[1])
    except Exception as e:
        failed.append((path, str(e).splitlines()[0]))
for p, e in failed:
    print(p, '-', e)
"
```

As a rule of thumb: any frontmatter value that references a GitHub issue number (`#NNN`) or
describes something containing a colon (a step name, a config key, a label like `domain:`) is a
candidate for quoting — check it specifically rather than assuming plain prose is always safe.

This generalizes beyond BuildNest to any project using YAML frontmatter (Jekyll, Hugo, Obsidian,
this KB's own convention) — the two syntax rules are properties of YAML itself, not anything
repo-specific.

## Related

- [Manifest and Surrogate Pattern for Index Files](../../knowledge-base/project/manifest-and-surrogate-pattern-for-index-files.md) — frontmatter fields are exactly the kind of surrogate-record data this lesson's validation gap put at risk of being silently wrong

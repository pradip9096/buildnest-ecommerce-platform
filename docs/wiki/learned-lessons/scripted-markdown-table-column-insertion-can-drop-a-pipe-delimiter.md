---
title: Scripted Markdown Table Column Insertion Can Silently Drop a Pipe Delimiter
category: tooling
tags: [markdown, scripting, python, table, regex, validation]
keywords: [pipe delimiter, table column count, string slicing, sed, markdown table, silent corruption]
source_conversations: [Session 2026-08-03]
last_updated: 2026-08-03
confidence: high
evidence_strength: moderate
related_articles: []
---

# Scripted Markdown Table Column Insertion Can Silently Drop a Pipe Delimiter

## Problem

Adding a new leading column (e.g. `Sr. No.`) to an existing large markdown table by script,
rather than by hand, is the only practical option once a table has 40-100+ rows. A naive
approach — take each row string, strip the leading `|`, and prepend the new cell — silently
drops the pipe that used to separate column 1 from column 2, because `line[1:]` removes only the
first character (the leading `|`) without accounting for the leading space before the next `|`
already being consumed differently depending on whether `.lstrip()` was applied first.

Concretely: `"| File | Topic |"[1:]` produces `" File | Topic |"`. Prepending `"| Sr. No. "` gives
`"| Sr. No.  File | Topic |"` — two spaces, one missing pipe, silently mis-parsed by every
markdown renderer as a merged cell (`Sr. No.  File`) rather than two columns. The rendered table
looks almost right at a glance (row count and rough shape are unchanged), which is exactly what
makes it easy to miss in a quick visual scan — the corruption only becomes obvious cell-by-cell.

## Fix

Two things, together:

1. When scripting a column insertion, don't reuse the row's own remaining leading whitespace —
   strip the leading `|` and any following whitespace explicitly (`line[1:].lstrip()`), then
   prepend the new cell with its own trailing `| ` delimiter: `f"| {value} | " + rest`.
2. **Verify programmatically after the script runs, not by eyeballing a few rows.** Count `|`
   occurrences per row and confirm every row (header, separator, and every data row) has the same
   count. A quick way: `line.count("|")` for every row in the table, assert they're all equal to
   the header's count. Don't trust a visual spot-check of the first and last row — the corruption
   here happened in every row uniformly, so a spot-check of a "clean-looking" row proves nothing.

## Generalizes To

Any script-driven edit to a delimited text format (CSV via string ops instead of a real CSV
library, markdown tables, pipe-delimited logs) where a naive string-slice operation can drop or
duplicate a delimiter without raising any error — the file remains syntactically parseable, just
wrong. The general rule: after any scripted structural edit to a delimited format, write a
verification pass that checks the structural invariant directly (column count, field count) —
don't rely on the edit "looking right" in a sample.

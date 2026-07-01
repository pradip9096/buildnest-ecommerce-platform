# Claude Code Progressive Disclosure — Token Cost by Component

**Category:** Developer Tools > Claude Code  
**Tags:** `claude-code`, `tokens`, `context-window`, `performance`, `progressive-disclosure`  
**Last Updated:** 2026-06-30

---

## Overview

Claude Code uses **progressive disclosure** to keep the startup context window cost low. Not all installed components are fully loaded at startup — many load only their summary or description, with full content fetched on demand. Understanding this determines how aggressively you can install agents, skills, and rules without degrading performance.

---

## What Progressive Disclosure Means

Instead of loading every installed component in full at the start of each conversation, Claude Code loads a lightweight summary. The full content is only fetched when that component is actually needed.

**Analogy:** A library catalogue tells you what books exist (lightweight). You only pull a book off the shelf when you need it (on demand).

---

## Token Cost by Component

| Component | At startup | When fully loaded | Practical implication |
|---|---|---|---|
| **Skills** | Description field only | When `/skill-name` is invoked | Safe to have many skills |
| **Agents** | `description:` frontmatter only | When agent is spawned | Safe to have many agents |
| **MCP tools** | Tool names only (deferred) | When `ToolSearch` fetches schema | Large MCP servers have low idle cost |
| **Rules (`.claude/rules/`)** | Not loaded | When a matching file type is edited | Conditional — not always loaded |
| **Hooks** | Registered only | Run as external processes — never in context | Zero token cost always |
| **CLAUDE.md** | Fully loaded | Always | Size matters — keep concise |
| **Memory files** | Fully loaded | Always | Size matters — keep concise |
| **Commands (built-in)** | Part of system prompt | Always | Fixed cost |

---

## Real Numbers — ECC as a Case Study

ECC (Everything Claude Code) ships 67 agents, 448 skills, 122 rules, and 92 commands.

| Component | Files | Raw size | Tokens if fully loaded |
|---|---|---|---|
| Agents | 67 | 432 KB | ~108,000 |
| Skills | 448 | 3.3 MB | ~825,000 |
| Rules | 122 | 283 KB | ~70,800 |
| Commands | 92 | 362 KB | ~90,600 |
| **Total** | **729** | **4.4 MB** | **~1,100,000** |

**With progressive disclosure, the realistic startup cost is ~50,000 tokens** — only the description fields of agents and skills, not their full content. Full content loads only when an agent is spawned or a skill is invoked.

---

## Context Window Implications

A typical Claude context window is 200,000 tokens. Here is how a session fills up:

| Category | Typical tokens | % of 200k |
|---|---|---|
| System prompt | ~7,000 | 3.5% |
| System tools | ~8,600 | 4.3% |
| MCP tool schemas (deferred) | ~4,500 | 2.3% |
| Memory files (fully loaded) | ~2,000 | 1.0% |
| Skills descriptions | ~1,300 | 0.6% |
| Messages (conversation) | ~94,000 | 47% |
| **Remaining free space** | **~83,000** | **~41%** |

Installing ECC globally with progressive disclosure would add approximately 50,000 tokens at startup — pushing startup context to ~25% before a single message is written.

---

## What You Should and Should Not Be Careful About

### Safe to have many of:
- Agents (description only at startup)
- Skills (description only at startup)
- MCP servers (schemas deferred)

### Be careful about size:
- **CLAUDE.md** — fully loaded every session. Every line costs tokens for the entire conversation lifetime.
- **Memory files** — same as CLAUDE.md. Keep entries concise.
- **Rules** — conditional, but can add up if many file types are active simultaneously.

### Zero concern:
- Hooks — external processes, never in context

---

## Practical Guidance

### Installing agents and skills
Install selectively based on what you actually use. Progressive disclosure makes large collections cheap at startup, but irrelevant agents still add noise to Claude's decision-making about when to invoke them.

**Rule of thumb:** Install an agent when you have a recurring task that fits its specialisation. Remove agents that are never invoked.

### Writing CLAUDE.md and memory files
Every token in CLAUDE.md is paid on every turn. Write concisely:
- Avoid prose where a table works
- Avoid examples where a rule statement suffices
- Remove stale entries — they cost tokens and mislead

### Rules
Rules are the best value for passive enforcement — low token cost (conditional load) with high impact (always enforced when the file type is active). Prioritise rules for the languages and frameworks in active use.

---

## Checking Your Current Token Usage

Run `/context` in Claude Code to see a breakdown:

```
System prompt:  6.9k tokens (3.5%)
System tools:   8.6k tokens (4.3%)
MCP tools:      4.5k tokens (2.3%)
Memory files:   1.9k tokens (0.9%)
Skills:         1.3k tokens (0.6%)
Messages:      93.8k tokens (46.9%)
Free space:    50.1k (25.1%)
```

The `Messages` category grows as the conversation progresses. When it approaches 80–90%, Claude Code will auto-compact the conversation.

---

## See Also

- `docs/knowledge-base/claude-code-extension-mechanisms.md`
- `docs/knowledge-base/claude-code-hooks-reference.md`

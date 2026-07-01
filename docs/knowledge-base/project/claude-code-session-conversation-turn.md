# Claude Code — Session, Conversation, and Turn

**Category:** Developer Tools > Claude Code  
**Tags:** `claude-code`, `session`, `conversation`, `turn`, `context-window`, `clear`  
**Last Updated:** 2026-06-30

---

## Overview

Claude Code uses three distinct terms to describe the structure of interaction: **session**, **conversation**, and **turn**. Understanding the difference matters for reasoning about context loss, the `/clear` command, compaction, and hooks.

---

## Definitions

### Session
A Claude Code process instance — from when you launch Claude Code to when you close it.

- Has a unique `session_id`
- Has its own context window
- Hooks fire on session lifecycle events (`SessionStart`, `SessionEnd`)
- CLAUDE.md, memory files, agents, skills, and settings are loaded at session start
- Can be resumed (`source: "resume"` in SessionStart hook)
- Can be compacted when the context window fills

### Conversation
The message history within a session — the full back-and-forth exchange of prompts and responses.

- Lives inside a session
- Grows with each turn until it approaches the context window limit
- Gets compacted (summarised) automatically when context fills up
- Resets when you run `/clear`

### Turn
One user prompt plus one assistant response — the atomic unit of exchange.

- **User turn** — the message you submit
- **Assistant turn** — Claude's response
- Together they form one complete turn (also called a round trip)

---

## Hierarchy

```
Session
└── Conversation
    ├── Turn 1
    │   ├── User turn   (your prompt)
    │   └── Assistant turn (Claude's response)
    ├── Turn 2
    │   ├── User turn
    │   └── Assistant turn
    ├── [compaction point — history summarised]
    └── Turn N
```

---

## Real-World Analogy

| Concept | Real world | Claude Code |
|---|---|---|
| **Session** | Sitting down at your desk to work | Claude Code process running |
| **Conversation** | The notes written while sitting there | Message history in that process |
| **Turn** | One question asked + one answer given | One prompt + one response |

---

## What `/clear` Does

Running `/clear` resets the conversation but keeps the session running.

| | After `/clear` |
|---|---|
| Message history | ❌ Discarded — not recoverable |
| In-progress context | ❌ Gone |
| CLAUDE.md | ✅ Reloaded |
| Memory files | ✅ Reloaded |
| Agents / skills | ✅ Reloaded |
| Hooks | ✅ Re-registered (`SessionStart` fires with `source: "clear"`) |
| Files on disk | ✅ Untouched |
| Git history | ✅ Untouched |

**Important:** `/clear` is irreversible. Anything worth preserving across conversations must be written to disk — memory files, CLAUDE.md, or knowledge base articles — before clearing.

---

## `/clear` vs Compaction

| | `/clear` | Compaction |
|---|---|---|
| **Triggered by** | You explicitly | Automatically when context fills |
| **History** | Fully discarded | Summarised and kept |
| **Continuity** | Broken — fresh start | Preserved — conversation continues |
| **Context after** | Empty | Summary + recent messages |
| **SessionStart fires?** | Yes (`source: "clear"`) | Yes (`source: "compact"`) |

---

## API Mapping

In the Anthropic API, turns map directly to the `messages` array:

```json
[
  { "role": "user",      "content": "What is a session?" },
  { "role": "assistant", "content": "A session is ..." },
  { "role": "user",      "content": "What is a turn?" },
  { "role": "assistant", "content": "A turn is ..." }
]
```

Each object is one turn. The `role` field is either `"user"` or `"assistant"`.

---

## See Also

- `docs/knowledge-base/claude-code-extension-mechanisms.md`
- `docs/knowledge-base/claude-code-hooks-reference.md`
- `docs/knowledge-base/claude-code-progressive-disclosure.md`

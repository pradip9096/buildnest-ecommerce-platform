# Knowledge Base — Project

Durable, reusable knowledge about BuildNest's engineering practices, tooling decisions, and project-specific patterns. Each article is self-contained and independently discoverable.

For operational lessons extracted from specific sessions (tooling gotchas, one-time fixes, process hygiene), see [`docs/wiki/learned-lessons/`](../../wiki/learned-lessons/README.md).

---

## Index

| File | Topic | Category | Last Updated |
|---|---|---|---|
| [quality-gate-ratchet-pattern.md](quality-gate-ratchet-pattern.md) | Fitness functions, ratchet mechanism, PIT mutation threshold schedule, broken-windows rationale | quality-engineering | 2026-07-01 |
| [claude-code-extension-mechanisms.md](claude-code-extension-mechanisms.md) | Claude Code hooks, MCP servers, slash commands, skills — extension points and when to use each | tooling | — |
| [claude-code-hooks-reference.md](claude-code-hooks-reference.md) | Hook types, event lifecycle, settings.json configuration | tooling | — |
| [claude-code-progressive-disclosure.md](claude-code-progressive-disclosure.md) | Progressive disclosure pattern in Claude Code UX | tooling | — |
| [claude-code-session-conversation-turn.md](claude-code-session-conversation-turn.md) | Session and conversation turn semantics in Claude Code | tooling | — |
| [mermaid-diagram-quality-attributes.md](mermaid-diagram-quality-attributes.md) | Mermaid diagram conventions for quality attribute documentation | documentation | — |
| [open-core-business-model.md](open-core-business-model.md) | Open-core business model pattern and examples | product | — |
| [check-mysql-installation-on-wsl2.md](check-mysql-installation-on-wsl2.md) | Verifying MySQL installation state on WSL2 | infrastructure | — |
| [claude-code-memory-directory.md](claude-code-memory-directory.md) | Memory directory structure, the four memory types, current BuildNest files, and how to shape memory via instruction, feedback, or direct editing | tooling | 2026-07-02 |
| [loop-engineering-vs-claude-code-loop.md](loop-engineering-vs-claude-code-loop.md) | "Loop engineering" as a 2026 industry trend (generator/verifier, ReAct) vs. Claude Code's narrower `/loop` scheduling skill and the closer `/goal` analog | tooling | 2026-07-02 |

---

## Taxonomy

### Categories

| Category | Description |
|---|---|
| `quality-engineering` | Test strategy, coverage gates, mutation testing, fitness functions |
| `tooling` | Developer tools, CI configuration, IDE integration, shell patterns |
| `infrastructure` | Environment setup, Docker, databases, WSL2 |
| `documentation` | Diagram conventions, doc formats, knowledge organization |
| `product` | Business model, domain concepts, product decisions |

### Naming Convention

Files use lowercase hyphen-separated names describing the topic: `<topic-noun>-<qualifier>.md`. Avoid dates in filenames — use `last_updated` in frontmatter instead.

---

## Frontmatter Schema

Every article must include:

```markdown
---
title: 
category: 
tags: []
keywords: []
objective: 
audience: 
scope: 
source_conversations: []
last_updated: YYYY-MM-DD
confidence: high|medium|low
evidence_strength: strong|moderate|weak
related_articles: []
status: published|draft
---
```

- `objective` — one sentence: what question does this article answer?
- `audience` — who reads this and in what situation?
- `scope` — BuildNest-specific, general, or both?
- `confidence` — how certain is the content?
- `evidence_strength` — how well-evidenced is the claim (strong = reproduced/cited, moderate = observed once, weak = inferred)?

---

## Contributing

- Add a row to the index table above when creating a new article.
- One topic per file — split if an article covers two unrelated subjects.
- Update `last_updated` in frontmatter on any substantive edit.
- Cross-reference `docs/wiki/learned-lessons/` for operational lessons rather than duplicating content here.

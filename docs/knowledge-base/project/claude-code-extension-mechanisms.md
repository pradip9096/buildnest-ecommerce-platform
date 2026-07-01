# Claude Code Extension Mechanisms — Skills, Agents, Hooks, Commands, Settings, MCP, and Loop

**Category:** Developer Tools > Claude Code  
**Tags:** `claude-code`, `agents`, `skills`, `hooks`, `mcp`, `commands`, `settings`, `plugins`  
**Last Updated:** 2026-06-30

---

## Overview

Claude Code can be extended and configured through several distinct mechanisms. Each serves a different purpose and operates at a different layer of the system. Understanding the difference prevents confusion and enables deliberate, effective configuration.

---

## The Seven Mechanisms

### 1. Skills (`/skill-name`)

Reusable instruction sets stored as `SKILL.md` files. When you type `/skill-name` in the CLI, Claude loads that skill's full instructions and follows them for that task.

- **What they are:** Prompt templates for specific task types
- **Stored in:** `~/.claude/skills/` (global) or `.claude/skills/` (project)
- **Token cost:** Description only at startup — full content loads on invocation (progressive disclosure)
- **Example:** `/graphify` loads graphify skill instructions

### 2. Agents (`.claude/agents/*.md`)

Isolated Claude sub-processes that handle tasks independently. When Claude spawns an agent, it starts with no memory of the parent conversation and gets its own tool access.

- **What they are:** Specialist workers for complex or repeated task types
- **Stored in:** `~/.claude/agents/` (global) or `.claude/agents/` (project)
- **Token cost:** Description frontmatter only at startup — full content loads only when spawned (progressive disclosure)
- **Used by:** Claude (the parent), not the user directly. You can hint: "use the frontend-developer agent"
- **Example:** `frontend-developer`, `code-reviewer`, `qa-expert`

### 3. Hooks (`.claude/hooks/*.sh`)

Shell commands that execute automatically at specific points in Claude Code's lifecycle. They run outside Claude's process as external shell scripts.

- **What they are:** Deterministic automation triggers — they always fire, no AI decision required
- **Configured in:** `.claude/settings.json` (project) or `~/.claude/settings.json` (global)
- **Token cost:** Zero — hooks run as external processes, not loaded into context
- **Use for:** Auto-formatting, blocking dangerous commands, injecting context, auditing, notifications
- **Example:** Run Prettier after every file edit; block `rm -rf`; inject project state on session start

### 4. Commands (Slash Commands)

User-typed triggers in the CLI. Some are built-in to Claude Code; others invoke skills.

- **What they are:** UI-level triggers — the surface the user interacts with
- **Token cost:** Built-in commands are part of the system prompt (always loaded); skill-backed commands use progressive disclosure
- **Examples:** `/context`, `/compact`, `/hooks` (built-in); `/graphify` (skill-backed)

### 5. Settings (`settings.json`)

Static configuration files read at startup. No behaviour logic — just declarative key-value configuration.

- **What they are:** Configuration, not behaviour
- **Stored in:** `.claude/settings.json` (project) or `~/.claude/settings.json` (global)
- **Controls:** Hook registration, permission modes, allowed tools, environment variables, trusted directories
- **Token cost:** Not loaded into context

### 6. MCP (Model Context Protocol)

A protocol for connecting external tool servers to Claude. Each MCP server exposes tools that Claude can call like any built-in tool, but the implementation runs in an external process.

- **What they are:** External tool providers accessed over a protocol
- **Configured in:** `claude_desktop_config.json` or `.mcp.json`
- **Token cost:** Tool schemas are deferred — loaded on demand via ToolSearch
- **Examples:** `mcp__github__create_issue`, `mcp__playwright__browser_click`, `mcp__filesystem__read_file`

### 7. Engineering Loop (`/loop`)

A built-in skill that enables Claude to self-schedule repeated work. Claude calls `ScheduleWakeup` to wake itself at a future time with the same prompt, creating an autonomous iteration cycle.

- **What it is:** Self-pacing mechanism for long-running or polling tasks
- **Use for:** Watching CI runs, autonomous fix loops, scheduled monitoring

---

## Comparison Table

| Mechanism | What it is | Who triggers it | Token cost at startup | Stored where |
|---|---|---|---|---|
| **Skill** | Prompt template | User types `/name` | Description only | `.claude/skills/` |
| **Agent** | Isolated sub-Claude | Claude spawns it | Description only | `.claude/agents/` |
| **Hook** | Shell script on events | Events (automatic) | Zero | `.claude/hooks/` + `settings.json` |
| **Command** | User-typed trigger | User types it | Built-ins: always; skills: deferred | System / skills dir |
| **Settings** | Static config | Read at startup | Not in context | `settings.json` |
| **MCP** | External tool server | Claude calls tools | Deferred schemas | Config file |
| **Loop** | Self-scheduling | Claude calls ScheduleWakeup | Minimal | Built-in |

---

## Progressive Disclosure

Not all components load fully at startup. Claude Code uses **progressive disclosure** to keep startup token cost low:

| Component | At startup | When fully loaded |
|---|---|---|
| Skills | Description only | On `/skill-name` invocation |
| Agents | Description frontmatter only | When agent is spawned |
| MCP tools | Names only (deferred) | When `ToolSearch` fetches schema |
| Hooks | Registered (not in context) | Run as external processes |
| CLAUDE.md | Fully loaded | Always |
| Memory files | Fully loaded | Always |
| Rules | Conditional | When matching file type is edited |

**Implication:** You can have many agents and skills without significant startup token overhead. CLAUDE.md and memory files are where you should be most careful about size.

---

## Scope: Global vs Project

Each mechanism can be scoped globally (all projects) or to a single project:

| Scope | Path | When to use |
|---|---|---|
| **Global** | `~/.claude/` | Generic tools useful in any project (code-reviewer, etc.) |
| **Project** | `.claude/` in project root | Domain-specific tools for this project only |

Start project-scoped. Promote to global only when the same tool proves useful across multiple projects.

---

## Analogy: npm Ecosystem

| Claude Code | npm Equivalent |
|---|---|
| Skill | A script in `package.json` |
| Agent | A CLI tool / npx command |
| Hook | A git hook or npm lifecycle script |
| MCP server | An npm package providing APIs |
| Settings | `.npmrc` or `package.json` config |

---

## See Also

- `docs/knowledge-base/claude-code-hooks-reference.md`
- `docs/knowledge-base/claude-code-progressive-disclosure.md`

# Claude Code Hooks — How They Work and When to Use Them

**Category:** Developer Tools > Claude Code  
**Tags:** `claude-code`, `hooks`, `automation`, `settings`, `shell-scripts`  
**Last Updated:** 2026-06-30

---

## Overview

Hooks are shell commands (or other handler types) that execute automatically at specific points in Claude Code's lifecycle. They provide **deterministic control** — they always fire when triggered, with no AI decision involved. This makes them suitable for enforcement, automation, and safety guardrails.

---

## How Hooks Work

1. A lifecycle event fires (e.g., user submits a prompt, Claude runs a Bash command, a file changes)
2. Claude Code sends event data as JSON to the hook script's stdin
3. The script reads the JSON, does its work, and communicates back via stdout, stderr, and exit code
4. Claude Code reads the result and proceeds (or blocks, or injects context)

Hooks run as **external processes** — they do not consume context window tokens.

---

## Hook Events

| Event | When it fires | Can block? |
|---|---|---|
| `SessionStart` | Session begins or resumes | No |
| `UserPromptSubmit` | User submits a prompt | Yes |
| `PreToolUse` | Before a tool call executes | Yes |
| `PostToolUse` | After a tool call succeeds | Yes (decision: block) |
| `PermissionRequest` | Permission dialog is about to appear | Yes (can auto-approve) |
| `FileChanged` | A watched file changes on disk | No |
| `Stop` | Claude finishes responding | Yes (return ok:false to keep working) |
| `Notification` | Claude sends a notification | No |
| `ConfigChange` | A settings file changes | Yes |
| `CwdChanged` | Working directory changes | No |
| `PreCompact` / `PostCompact` | Before/after context compaction | No |
| `SessionEnd` | Session terminates | No |
| `SubagentStart` / `SubagentStop` | Agent spawns or finishes | No |

---

## Exit Codes

| Exit code | Meaning |
|---|---|
| `0` | No objection — normal flow continues. For SessionStart/UserPromptSubmit, stdout is added to context |
| `2` | Block the action. Write reason to stderr — Claude receives it as feedback |
| Any other | Non-blocking error — transcript shows a hook error notice |

---

## Context Injection

Only certain events automatically inject stdout into Claude's context:

| Event | Stdout injected? |
|---|---|
| `SessionStart` | ✓ Yes — automatically added to context |
| `UserPromptSubmit` | ✓ Yes |
| `UserPromptExpansion` | ✓ Yes |
| All others | ✗ No — use `additionalContext` JSON field instead |

For events that do not auto-inject, output structured JSON:
```json
{"additionalContext": "text to inject into Claude's context"}
```

---

## Hook Types

| Type | What it does |
|---|---|
| `command` | Runs a shell command (most common) |
| `prompt` | Single-turn LLM evaluation — returns `{"ok": true/false, "reason": "..."}` |
| `agent` | Spawns a subagent with tool access for complex verification |
| `http` | POSTs event data to an HTTP endpoint |
| `mcp_tool` | Calls a tool on a connected MCP server |

---

## Configuration Format

Hooks are configured in `settings.json`. Project-level hooks go in `.claude/settings.json`; global hooks in `~/.claude/settings.json`.

```json
{
  "hooks": {
    "SessionStart": [
      {
        "matcher": "startup|compact",
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/my-hook.sh"
          }
        ]
      }
    ],
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/protect-files.sh"
          }
        ]
      }
    ]
  }
}
```

---

## Matchers

Matchers filter which hook instances fire for a given event:

| Event | Matcher filters on |
|---|---|
| `PreToolUse`, `PostToolUse` | Tool name (`Bash`, `Edit\|Write`, `mcp__github__.*`) |
| `SessionStart` | Session source (`startup`, `resume`, `compact`, `clear`) |
| `FileChanged` | Literal filename (`pom.xml`, `.envrc\|.env`) |
| `Notification` | Notification type (`permission_prompt`, `idle_prompt`) |
| `SubagentStart/Stop` | Agent type name |
| `ConfigChange` | Config source (`user_settings`, `project_settings`) |

### The `if` Field (fine-grained filtering)

For tool events, the `if` field filters by both tool name and arguments:

```json
{
  "type": "command",
  "if": "Bash(git tag*)",
  "command": "my-hook.sh"
}
```

This fires only when the Bash command starts with `git tag`, not on every Bash call.

---

## Common Patterns

### Auto-format after file edits
```json
{
  "hooks": {
    "PostToolUse": [{
      "matcher": "Edit|Write",
      "hooks": [{"type": "command", "command": "jq -r '.tool_input.file_path' | xargs npx prettier --write"}]
    }]
  }
}
```

### Block edits to protected files
```bash
#!/bin/bash
INPUT=$(cat)
FILE=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
[[ "$FILE" == *".env"* ]] && echo "Blocked: .env is protected" >&2 && exit 2
exit 0
```

### Re-inject context after compaction
```json
{
  "hooks": {
    "SessionStart": [{
      "matcher": "compact",
      "hooks": [{"type": "command", "command": "echo 'Reminder: use Maven, not Gradle. Current milestone: M4.'"}]
    }]
  }
}
```

### Auto-approve plan mode exit
```json
{
  "hooks": {
    "PermissionRequest": [{
      "matcher": "ExitPlanMode",
      "hooks": [{"type": "command", "command": "echo '{\"hookSpecificOutput\":{\"hookEventName\":\"PermissionRequest\",\"decision\":{\"behavior\":\"allow\"}}}'"}]
    }]
  }
}
```

### Prompt-based Stop hook (keep Claude working until done)
```json
{
  "hooks": {
    "Stop": [{
      "hooks": [{"type": "prompt", "prompt": "Check if all requested tasks are complete. If not, respond with {\"ok\": false, \"reason\": \"what remains\"}."}]
    }]
  }
}
```

**Important:** For Stop hooks, check `stop_hook_active` in the input JSON and exit 0 if true, to avoid infinite loops (Claude Code blocks after 8 consecutive Stop hook blocks).

---

## Environment Variables Available in Hooks

| Variable | Value |
|---|---|
| `CLAUDE_PROJECT_DIR` | Absolute path to the project root |
| `CLAUDE_ENV_FILE` | Path to a file Claude sources before each Bash command (for env var injection) |

---

## Scope

| Location | Scope |
|---|---|
| `~/.claude/settings.json` | All projects on your machine |
| `.claude/settings.json` | This project only (can be committed to repo) |
| `.claude/settings.local.json` | This project only (gitignored) |

---

## Debugging Hooks

- Run `/hooks` in Claude Code to see all registered hooks
- Start Claude with `claude --debug-file /tmp/claude.log` for full execution details
- Test a hook manually: `echo '{"tool_name":"Bash","tool_input":{"command":"ls"}}' | ./my-hook.sh`
- Check exit code: `echo $?`

---

## BuildNest Hook: Automated Configuration Audit

BuildNest uses a `SessionStart` hook that automatically audits agent/rule coverage against active service domains and reports gaps into context at the start of every session. See `.claude/hooks/config-audit.sh`.

---

## See Also

- `docs/knowledge-base/claude-code-extension-mechanisms.md`
- `.claude/hooks/config-audit.sh`
- `.claude/settings.json`

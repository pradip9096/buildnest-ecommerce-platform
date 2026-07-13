---
title: "`gh` CLI Availability Is Inconsistent Across Bash Tool Invocations in the Same Session"
category: tooling
tags: [gh, github-cli, path, mcp, github-mcp, bash-tool]
keywords: [gh command not found, exit 127 gh, github mcp write auth failure, gh vs github mcp server, PATH inconsistent bash tool]
source_conversations: [Session 2026-07-03]
last_updated: 2026-07-03
confidence: medium
evidence_strength: moderate
root_cause: "an early Bash tool invocation likely ran before PATH was fully initialized, producing a spurious exit-127 for gh that was misread as 'not installed' rather than a shell-startup timing artifact"
impact: medium — led to routing GitHub writes through the MCP server instead, which then failed authentication on every write call
related_lessons:
  - docs/wiki/learned-lessons/github-issue-hygiene.md
---

# `gh` CLI Availability Is Inconsistent Across Bash Tool Invocations in the Same Session

## Problem

Early in a session, `gh pr view 259 ...` returned exit code `127` (`command not found`), which was read as "the `gh` CLI is not installed in this environment." That conclusion led to routing all GitHub read/write operations through the GitHub MCP server instead. Reads via the MCP server worked (`get_issue`, `search_issues`), but writes failed with `MCP error -32603: Authentication Failed: Requires authentication` on both `add_issue_comment` and `update_issue` — even though the same repo's issues had just been read successfully seconds earlier via the same server.

Later in the same session, `which gh` / `command -v gh` both resolved to `/usr/bin/gh`, and `gh issue comment` / `gh issue close` succeeded immediately with no environment changes made in between.

## Root Cause

Not fully isolated — no PATH-modifying command was run between the two `gh` invocations. The most likely explanation is that the first `gh pr view` call ran in a shell context where `gh`'s directory hadn't yet been added to `PATH` (profile/rc sourcing timing), while later Bash tool calls got a fully-initialized interactive shell. This is a property of how the sandbox/harness spins up shell state per invocation, not of `gh` itself.

## Rule

- Do not conclude a CLI tool is "not installed" from a single `127` exit code inside this harness — verify with `which <tool>` / `command -v <tool>` before ruling it out and switching to an alternative (e.g., an MCP server).
- Prefer `gh` CLI over the GitHub MCP server for **write** operations (comment, close, label, create) on this project — the MCP server's write auth has failed while its read auth succeeded in the same session. Use the MCP server for reads/searches where its structured JSON output and pagination are convenient, but fall back to `gh` for any mutating call.

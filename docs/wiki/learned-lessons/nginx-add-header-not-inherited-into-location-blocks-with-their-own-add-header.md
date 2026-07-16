---
title: "nginx add_header Directives Are Not Inherited Into a location Block That Defines Its Own"
category: infrastructure
tags: [nginx, security-headers, docker, frontend, csp]
keywords: [add_header inheritance, nginx location block, security headers silently missing, Cache-Control add_header, nginx-unprivileged]
source_conversations: [Session 2026-07-16, issue #125]
last_updated: 2026-07-16
confidence: high
evidence_strength: strong
root_cause: "nginx only inherits a parent context's add_header directives into a child context (a location block) when that child context defines no add_header of its own; a location block that sets even one add_header (e.g. Cache-Control) silently drops every server-level add_header (CSP, HSTS, X-Frame-Options, etc.) for requests handled by that location"
impact: medium — frontend/nginx.conf shipped an initial version where all 5 security headers (CSP, HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy) were set at server level but never appeared in any actual response, since every location block also set its own Cache-Control; caught only by curl-ing a real running container, not by reading the config
related_lessons:
  - docs/wiki/learned-lessons/verify-issue-premises-against-repo-before-implementing.md
---

# nginx `add_header` Directives Are Not Inherited Into a `location` Block That Defines Its Own

## Problem

While building `frontend/Dockerfile` + `frontend/nginx.conf` for #125 (production Dockerfile for
the React/Vite frontend), security headers (Content-Security-Policy, Strict-Transport-Security,
X-Frame-Options, X-Content-Type-Options, Referrer-Policy) were declared once at the `server`
block level, intending them to apply to every response. `curl -D -` against a real running
container showed **none** of them present on any response — not a subset, all of them silently
gone.

## Root Cause

nginx's `add_header` directive has an inheritance rule that is easy to miss: a child context
(a `location` block) inherits the parent (`server`) context's `add_header` directives **only if
the child context defines no `add_header` of its own**. The moment a `location` block adds even
one `add_header` (in this case, `Cache-Control` for asset caching / SPA-fallback behavior), nginx
treats that location as having "opted out" of the parent's header set entirely — none of the
parent's headers apply anymore for requests served by that location. This is not documented as a
gotcha in most beginner nginx tutorials, which usually show `add_header` used only at the server
level with no location-level overrides.

Every `location` block in `nginx.conf` (asset caching, `index.html`'s no-cache rule, and the SPA
fallback `location /`) declared its own `Cache-Control` header, so all three silently dropped the
server-level security headers.

## Fix

Extract the security headers into a separate file (`security-headers.conf`) and `include` it
explicitly inside every `location` block that also sets its own `add_header` — rather than
relying on inheritance from the parent `server` block.

```nginx
# security-headers.conf
add_header X-Frame-Options "DENY" always;
add_header Content-Security-Policy "..." always;
# ... etc.
```

```nginx
location / {
    include /etc/nginx/security-headers.conf;
    try_files $uri $uri/ /index.html;
}
```

## Verification

Config inspection alone would not have caught this — the directives were present and syntactically
correct, just silently inapplicable. Only running the built image and inspecting real response
headers (`curl -D -`) surfaced the gap; re-verified the same way after the fix, confirming all 5
headers now present on every response path (root, SPA-fallback, and asset paths).

## Generalization

This applies to any nginx configuration (not specific to this repo, this Dockerfile, or React/Vite)
where security or other cross-cutting headers are set at a `server` or `http` level while
`location` blocks below also declare their own `add_header` directives for unrelated purposes
(caching, CORS, etc.). Whenever a `location` block needs its own `add_header`, assume any
higher-level `add_header` directives are now silently dropped for that location unless explicitly
re-included — verify with a real request, not by reading the config.

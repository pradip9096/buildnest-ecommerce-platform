---
title: "nginx proxy_pass with a Bare Hostname Resolves Once at Config Load, Not Per-Request"
category: infrastructure
tags: [nginx, docker, dns, proxy_pass, reverse-proxy, resolver]
keywords: [proxy_pass host not found in upstream, nginx dns resolution, docker embedded dns 127.0.0.11, nginx resolver directive, reverse proxy startup failure]
source_conversations: [Session 2026-08-02, issue #119]
last_updated: 2026-08-02
confidence: high
evidence_strength: strong
root_cause: "nginx resolves a bare hostname used directly in proxy_pass (e.g. proxy_pass http://backend:8080;) exactly once, at config load time (worker startup) — not per-request. If that hostname isn't resolvable at that instant, nginx refuses to start at all (emerg: host not found in upstream); if it later changes IP (a container recreated), nginx keeps proxying to the stale IP until reloaded."
impact: "medium — building nginx-proxy/nginx.conf.template for #119's production Docker Compose stack, a live standalone test (mounting the rendered config + certs into a bare nginx container, deliberately outside the compose network to isolate TLS termination behavior) failed outright with `nginx: [emerg] host not found in upstream \"backend\"`. Config inspection and `docker compose config` static validation both passed cleanly — only running the real container surfaced it."
related_lessons:
  - docs/wiki/learned-lessons/nginx-add-header-not-inherited-into-location-blocks-with-their-own-add-header.md
---

# nginx `proxy_pass` with a Bare Hostname Resolves Once at Config Load, Not Per-Request

## Problem

While building `nginx-proxy/nginx.conf.template` for #119 (production Docker Compose stack:
nginx-proxy terminates TLS and reverse-proxies to `backend`/`frontend` containers), a live
verification test — running the rendered config in a real `nginx:1.27-alpine` container —
failed immediately:

```
nginx: [emerg] host not found in upstream "backend" in /etc/nginx/conf.d/nginx.conf:42
```

The config used the ordinary, tutorial-standard form:

```nginx
location ~ ^/(api|actuator)/ {
    proxy_pass http://backend:8080;
    ...
}
```

`docker compose config` (static YAML/interpolation validation) and reading the config both
looked correct — the failure only appeared when nginx itself actually tried to start.

## Root Cause

When `proxy_pass` names a plain hostname directly (no variable), nginx resolves that hostname
**once**, at config-load time (i.e., when each worker process starts), using the system
resolver — not per-request. Two consequences:

1. **Startup-order fragility**: if the target hostname isn't resolvable at that exact instant
   (e.g. the upstream container hasn't started yet, or — as in this case — the test ran
   `nginx-proxy` outside the Compose network entirely, with no `backend` container present at
   all), nginx refuses to start, full stop. In the real Compose stack this is usually masked by
   `depends_on: backend: condition: service_healthy`, but it's still fragile: it depends on
   Compose's dependency ordering being correct and complete for every path that could start
   nginx-proxy.
2. **Stale-IP caching**: even once started successfully, if the upstream container is later
   recreated (a redeploy, a crash-restart that gets a new IP from Docker's embedded DNS), nginx
   keeps sending traffic to the old, now-invalid IP until the nginx process is reloaded or
   restarted — it does not re-resolve on its own.

## Fix

Force per-request resolution by routing `proxy_pass` through a variable, with an explicit
`resolver` directive pointing at Docker's embedded DNS server (`127.0.0.11` inside any container
on a user-defined Docker network):

```nginx
resolver 127.0.0.11 valid=10s;

location ~ ^/(api|actuator)/ {
    set $backend_upstream http://backend:8080;
    proxy_pass $backend_upstream;
    ...
}
```

Using a `proxy_pass` **variable** (rather than a bare literal) is what changes nginx's
resolution timing from load-time to request-time — this is a documented nginx quirk, not
specific to Docker, but Docker's container-recreation-changes-IP behavior is what makes it
matter in practice. With this fix, nginx starts successfully even if the upstream container
isn't up yet (it will 502 per-request until the upstream becomes resolvable, rather than
refusing to start at all), and correctly picks up a new IP within the `valid=10s` TTL if the
upstream container is recreated.

## Verification

Re-rendered the config, mounted it into a fresh nginx container (again standalone, no
`backend`/`frontend` container present) — this time nginx started successfully (previously: hard
`emerg` exit). Confirmed via `curl`: HTTP→HTTPS redirect worked (301), and the HTTPS location
returned `502 Bad Gateway` (correct — no real upstream present in this isolated test) instead of
nginx failing to start at all. This is the expected/correct behavior difference the fix produces:
a missing upstream now degrades to a per-request 502 instead of preventing the whole proxy from
starting.

## Generalization

Applies to any nginx config (not specific to this repo or Docker Compose) where `proxy_pass`
targets a hostname that may not be resolvable at nginx startup, or may change IP over the
container's lifetime — which is the normal case for any container-orchestrated reverse proxy
(Docker Compose, Swarm, bare Kubernetes without a service mesh sidecar). Default to the
`resolver` + variable-indirection pattern for any `proxy_pass` target that isn't a static,
externally-stable hostname — verify with a real container start, not config inspection alone,
since (like the `add_header` inheritance gotcha above) this is syntactically valid and looks
correct on read.

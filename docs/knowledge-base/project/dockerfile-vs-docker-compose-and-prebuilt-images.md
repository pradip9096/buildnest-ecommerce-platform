---
title: Dockerfile vs. docker-compose.yml, and Pre-Built vs. Custom-Built Images
category: infrastructure
tags: [docker, docker-compose, containers, dockerfile, images]
keywords: [dockerfile, docker-compose, container image, official image, docker hub, image vs container, service networking]
objective: What does a Dockerfile do that docker-compose.yml doesn't, and why do MySQL/Redis/Elasticsearch need no Dockerfile of their own?
audience: beginner software developer new to containerization
scope: general (Docker fundamentals), illustrated with BuildNest's own compose setup
source_conversations: []
last_updated: 2026-07-15
confidence: high
evidence_strength: strong
related_articles: [../learning/ideal-initial-project-setup-from-scratch.md]
status: published
---

# Dockerfile vs. docker-compose.yml, and Pre-Built vs. Custom-Built Images

## What Is It?

A **Dockerfile** is a recipe for building one container **image** — a step-by-step script
(`FROM`, `COPY`, `RUN`, `CMD`, etc.) that turns an application's source code into a runnable,
self-contained artifact. A **`docker-compose.yml`** is a different kind of file entirely: it
doesn't build anything by itself — it declares a set of **services** (each backed by an image,
either built from a Dockerfile or pulled ready-made) and how those services run together as one
multi-container application, including the private network that lets them address each other by
name.

These two files answer two different questions. Dockerfile answers "how do I turn *this* code
into an image?" Compose answers "which containers does my application need, and how do they talk
to each other?"

## Why It Matters

A beginner's natural assumption is that "Docker setup" is one file and one step. It's actually
two layers, and conflating them causes two common points of confusion:

1. **"Do I need a Dockerfile for every service in my compose file?"** — No. Only for the
   services that run code unique to this project. Off-the-shelf infrastructure (a database, a
   cache, a search engine) already has a maintained, versioned image published by its vendor —
   writing a Dockerfile for MySQL would mean re-implementing what the MySQL team already built,
   tested, and hardened.
2. **"How do my containers find each other?"** — Not via `localhost`, and not via manually
   configured IP addresses. Compose creates a private network per project and registers each
   service's name as a resolvable hostname on it, automatically.

Without this distinction, it's easy to either write an unnecessary Dockerfile for a database, or
to hardcode an IP/hostname that breaks the moment the container is recreated.

## How It Works

### Dockerfile — builds one image from source

A Dockerfile is only needed for the piece of the system that is *this project's own code* —
there is no pre-existing image for it anywhere, because nobody else has built this exact
application before. It typically:

- Starts `FROM` a base image (e.g. a JDK or Node runtime)
- `COPY`s in source code and dependency manifests
- `RUN`s a build step (compile, install dependencies)
- Declares the `CMD`/`ENTRYPOINT` that starts the application when a container is run from the
  resulting image

Building it (`docker build -t buildnest-backend .`) produces a named image sitting in the local
image store — nothing is running yet until a container is started from that image (`docker run`,
or via Compose).

### docker-compose.yml — orchestrates multiple containers

A Compose file lists one `services:` block per container the application needs. Each service
entry points at either:

- `build: .` — build this service's image from a local Dockerfile (the application itself), or
- `image: <name>:<tag>` — pull an already-built image from a registry (Docker Hub by default)
  and run it as-is, no Dockerfile involved.

```yaml
services:
  backend:
    build: .              # this project's own code — needs the Dockerfile
  mysql:
    image: mysql:8.2       # official, pre-built — no Dockerfile needed
  redis:
    image: redis:7
  elasticsearch:
    image: elasticsearch:8.10.0
```

Running `docker compose up` builds whatever needs building, pulls whatever needs pulling, and
starts every service together as one unit.

### Automatic service-name networking

Compose creates a private Docker network scoped to the project and attaches every service to it.
On that network, each service's own key in the `services:` block (`backend`, `mysql`, `redis`,
`elasticsearch`) becomes a DNS name the *other* containers can resolve — the backend connects to
the database at the hostname `mysql`, not `localhost` and not a manually tracked IP address. This
is what lets a Spring Boot container's `application.yml` point at `jdbc:mysql://mysql:3306/...`
and have it resolve correctly regardless of which host machine or which run it is.

## When to Use It

- **Write a Dockerfile** when the thing you're containerizing is code you own — there is no
  existing image for "your application" anywhere in the world, so you have to describe how to
  build one.
- **Skip the Dockerfile and use `image:` directly** when the thing you need is standard,
  widely-used infrastructure that already has an official (or well-maintained community) image —
  a database, a cache, a message broker, a search engine. Reach for the vendor's published image
  on Docker Hub first; only build a custom image for infrastructure if you need something the
  official image genuinely doesn't support (a custom plugin set, a hardened base image for a
  compliance requirement).
- **Reach for `docker-compose.yml`** the moment an application needs more than one container to
  function together in local development — a single `docker run` command per container quickly
  becomes unmanageable once networking, startup order, and shared configuration enter the
  picture.

## Examples

BuildNest's own setup is the direct worked example this article documents. `backend/Dockerfile`
builds the Spring Boot application's own image from source — nothing else in the stack has one.
`backend/docker-compose.yml` (see [Ideal Initial Project Setup From Scratch — Steps 16 and
17](../learning/ideal-initial-project-setup-from-scratch.md)) declares four services: the
application itself (`build:`), plus `mysql:8.2`, `redis:7`, and `elasticsearch:8.10.0` — all three
pulled as official pre-built images, with zero custom Dockerfiles behind them. The backend
connects to each by its Compose service name (`mysql`, `redis`, `elasticsearch`), never by a
hardcoded IP or `localhost`.

## Synthesis

The Dockerfile/Compose split mirrors a broader pattern worth internalizing: **build what's
unique, reuse what's standard.** A Dockerfile exists only where original, uncontainerized-before
code needs a first image; a Compose file's job is purely to wire already-available building
blocks — some built locally, some pulled ready-made — into one running system, with the network
plumbing handled automatically rather than configured by hand. Recognizing which of the two
questions you're actually answering ("how do I build this" vs. "how do these pieces run
together") prevents both wasted effort (writing a Dockerfile nobody needs) and fragile setups
(hand-wired IPs that break on the next container restart).

## Related Articles

- [Ideal Initial Project Setup From Scratch](../learning/ideal-initial-project-setup-from-scratch.md) — the full walkthrough this article's worked example is drawn from (Steps 16-17 cover the Dockerfile and docker-compose.yml in BuildNest's own setup)

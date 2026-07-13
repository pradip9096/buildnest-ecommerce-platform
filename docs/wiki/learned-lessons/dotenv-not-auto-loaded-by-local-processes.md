---
title: .env Files Are Not Auto-Loaded by Locally-Run Processes
category: tooling
tags: [dotenv, docker-compose, spring-boot, mvnw, environment-variables, local-dev]
keywords: [.env, SPRING_DATASOURCE_PASSWORD, access denied, Liquibase, docker compose env, mvnw spring-boot:run]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
root_cause: "docker compose auto-loads .env but a locally-launched process (mvnw, npm run dev) does not, and application.properties fell back to a blank DB password default instead of failing fast on the missing variable"
impact: medium — a real time-lost debugging session tracing an obscured Spring bean-creation chain back to a silently-blank credential
related_lessons:
  - docs/wiki/learned-lessons/shell-pipeline-exit-code-masking.md
---

# .env Files Are Not Auto-Loaded by Locally-Run Processes

## Problem

A single `.env` file can serve two different consumers with two different loading
mechanisms — one implicit, one requiring explicit sourcing:

- **Docker Compose** reads a `.env` file in the compose project directory automatically
  and substitutes its variables (`${MYSQL_ROOT_PASSWORD}`, etc.) into container
  environments. No extra step needed.
- **A locally-run process** (`./mvnw spring-boot:run`, `npm run dev`, a plain `java -jar`)
  only sees environment variables that are actually exported into its shell. It does
  **not** read `.env` unless something explicitly loads it (`source .env`, a dotenv
  library, `spring.config.import`, etc.).

When a startup script brings up infra via `docker compose up` and then launches the app
process directly in the same shell without sourcing `.env`, the container gets correct
credentials but the local process does not.

### Symptom

```
Caused by: java.sql.SQLException: Access denied for user 'root'@'localhost'
Caused by: liquibase.exception.DatabaseException: ...
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'jpaSharedEM_entityManagerFactory'
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: ...
```

The real cause (blank/wrong DB password) is buried at the bottom of a long Spring bean
creation chain — the top-level exceptions (`UnsatisfiedDependencyException`,
`Unable to start web server`) are just the container unwinding around it. Verifying the
container itself accepts the credentials (`docker exec <container> mysql -uroot -p... `)
rules out a stale-volume password mismatch and points at the local process's environment
instead.

Root cause in this case: `application.properties` had
`spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}` — a default fallback to
**blank**, not an error, so the app silently tried `root` with no password instead of
failing fast on a missing variable.

## Fix

Explicitly export the `.env` file into the shell before launching the local process:

```bash
(cd "$BACKEND_DIR" && set -o allexport && source .env && set +o allexport && ./mvnw spring-boot:run)
```

`set -o allexport` makes every variable assigned by `source .env` exported automatically,
without needing to prefix each line with `export`. `set +o allexport` immediately after
scopes the behavior to just that one file.

## Rule

When a startup script mixes `docker compose` (which auto-loads `.env`) with a directly
launched local process (which does not), always source `.env` into that process's shell
before starting it. If a container has the right config but a co-located local process
doesn't, check whether the local process ever actually read the `.env` file — don't
assume both consumers loaded it the same way.

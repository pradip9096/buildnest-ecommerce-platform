# Smoke, Sanity, and Regression Testing vs. CI Test-Suite Coverage

**Category:** quality-engineering
**Last Updated:** 2026-07-12

## The gap this closes

A green CI run (`add-run-tests` in `development-workflow.md`) proves the unit/integration test
suite passes. It does not prove the change works when actually run, because unit and integration
tests execute in-process against mocks or an H2 test profile — they never start a real container,
mount a real volume, or exercise a shell script the way a container runtime does. For code with a
runtime/environment surface (Docker Compose services, CI/deployment config, shell-script config
rendering, service wiring), CI green and "this actually works" are two different claims, and
conflating them is the specific failure mode this article documents.

## The concrete case (#122 → #363)

A PR (#362) added Grafana, Alertmanager, and three Prometheus exporters to
`docker-compose.yml`, plus the `sed`-based config-render sidecars that generate their config
files from environment variables. Docker wasn't available in the session that wrote it, so
verification was YAML/JSON syntax validation (`python3 -c "import yaml; yaml.safe_load(...)"`)
and standalone simulation of the render scripts in `/tmp`. CI (which never starts a real
container for this kind of change) was green. The PR merged.

A later live smoke test — `docker compose up` on the real stack — found three bugs, none of
which were reachable by anything CI or the syntax validation had checked:

1. **`node-exporter`'s `/:/host:ro,rslave` bind mount failed outright** under Docker Desktop's
   WSL2 backend (`path / is mounted on / but it is not a shared mount`). `rslave` mount
   propagation requires the host's `/` to already be a shared mount, which isn't guaranteed
   under every Docker backend. Fix: plain `:ro` — the exporter doesn't need to observe new
   host mounts created after container start.
2. **A multi-line `command: >` folded-scalar script silently mis-tokenized** into 8+ separate
   shell arguments (observed as `sh: -e: not found` repeated per flag, then a `Permission
   denied` trying to execute the template file directly as a command). YAML's folding rule —
   lines indented *more* than the block's own indentation keep their newlines rather than being
   folded to spaces — interacted badly with a script that used escaped inner quotes across many
   continuation lines, producing a token stream `shlex` split into the wrong argv. Fix: rewrite
   as a YAML list (`command: [sh, -c, <literal-block-script>]`) so the whole script is one
   unambiguous argv element, never fed through folding-then-shlex at all. This is a general
   YAML-authoring gotcha, not specific to Alertmanager — any `command: >` block with multiple
   quoted, multi-line arguments is at risk.
3. **`mysqld_exporter:v0.15.1` has removed `DATA_SOURCE_NAME` env var support entirely**,
   despite that being the long-standing documented way to configure it in older versions and in
   most blog posts/Stack Overflow answers still in circulation. It now requires a mounted
   `--config.my-cnf` file. The failure mode was silent-ish: the container started, logged two
   info/error lines about being unable to parse a nonexistent `.my.cnf`, and exited — nothing
   about it said "your env var is being ignored." This is the general shape of a *dependency
   version drift* bug: a config method that worked (or was believed to work, from
   documentation/memory) silently stops being supported in a newer pinned version, and only a
   live run surfaces it.

None of these three would be caught by: reading the YAML carefully, unit-testing the app code,
or validating config-file syntax. All three required starting the actual process/container and
observing what it does.

## Three distinct concerns, not one

- **Smoke test** — start the real service(s) and confirm the golden path works end-to-end. Answers
  "does this even run." Coarse-grained, fast, done once per meaningful change to runtime behavior.
- **Sanity test** — after a fix, a narrow, fast re-check of specifically the thing that was just
  changed, before re-running anything broader. Answers "did *this* fix work," not "is everything
  still fine." In the #122→#363 sequence, this was e.g. re-curling `mysqld-exporter:9104/metrics`
  immediately after the my.cnf fix, before moving on to the next bug.
- **Regression test** — confirm the change didn't break something *else* that was previously
  working. The existing CI suite is largely this, for code with test coverage; for
  infrastructure/config changes, it also means re-checking adjacent services after a fix (e.g.
  confirming Prometheus's other targets were still `up` after fixing the exporter, not just the
  one that was broken).

These three answer different questions and are easy to conflate into "I ran the tests." A full
CI pass is a regression test for code with unit/integration coverage; it is not a smoke test for
anything CI's own test jobs don't actually start.

## When this applies (and when it doesn't)

Applies whenever a change touches: Docker Compose service definitions, CI/deployment
configuration, environment-dependent shell scripts (config renderers, entrypoints), or service
wiring (new inter-service dependencies, new env vars consumed at runtime). The common thread is
"this class of change is exercised by whether the *real environment* + *runtime behavior*
cooperate, not by whether the diff typechecks or parses as valid YAML/JSON."

Does not apply, and shouldn't be forced, when the change is a pure unit-testable service method
with no infrastructure/environment surface at all — the existing test suite already exercises
its real runtime behavior (Spring's actual bean wiring under `@SpringBootTest`, a real HTTP
call under `MockMvc`), so a separate smoke test would just be re-running the same suite by hand.

## If the environment genuinely can't run the change

Sometimes the tool needed to actually test something (Docker, a cloud credential, a hardware
dependency) isn't available in the current session. The correct response is not to skip
verification silently and report success — it's to do what verification *is* possible (syntax
validation, standalone simulation of any scripted logic) and **explicitly flag the gap** to
whoever will act on the result, the same way a UI change should be flagged as unverified if the
browser can't be driven. "I validated the YAML" and "I confirmed this works" are different
claims; only make the one that's actually true. Also don't assume unavailability is permanent —
check again (`docker info`, etc.) rather than carrying a stale "tool X isn't available here"
assumption across sessions once the underlying blocker (e.g. a stopped daemon) is gone.

## See also

- `development-workflow.md`'s `smoke-sanity-regression-test` step (Amendment Log entry #17) —
  the process-level rule this article backs, including its trigger condition and where it sits
  in the sequence (between `add-run-tests` and `update-docs`)
- [quality-gate-ratchet-pattern.md](quality-gate-ratchet-pattern.md) — the broader family of
  "a passing gate is not the same claim as 'this is actually correct'"

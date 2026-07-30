---
title: "Docker Desktop on WSL2: `--network host` / `host.docker.internal` Can't Reach a Process Listening on the WSL2 Distro's Own Loopback"
category: infrastructure
tags: [docker, wsl2, docker-desktop, networking, zap, ssrf-testing]
keywords: [docker network host wsl2 not reachable, host.docker.internal unreachable wsl2, docker desktop separate vm networking, zap scan localhost unreachable, reach process on wsl2 distro from container]
source_conversations: [Session 2026-07-29, issue #111]
last_updated: 2026-07-29
confidence: high
evidence_strength: strong
root_cause: "Docker Desktop's containers run inside a separate lightweight VM from the user's own WSL2 distro, not directly in that distro's network namespace — so --network host, host.docker.internal, and the docker bridge gateway IP all fail to reach a process bound on the distro's own loopback/all-interfaces address, even though the docker CLI itself appears to run natively inside the same WSL2 kernel"
impact: medium — cost real debugging time isolating why a real OWASP ZAP active scan container couldn't reach a locally-running Spring Boot backend, before the actual security assessment could proceed
related_lessons:
  - spring-boot-run-does-not-read-env-and-checksums-drift-across-fixes.md
---

# Docker Desktop on WSL2: `--network host` can't reach a process on the distro's own loopback

While running an OWASP ZAP full active scan (`zaproxy/zap-stable` Docker
image) against a locally-running Spring Boot backend (`./mvnw
spring-boot:run`, listening on `*:8080`) for #111, the scan container
could not connect to the backend under any of the "obvious" Docker
networking options:

1. `docker run --network host ...` targeting `http://localhost:8080` —
   failed with `[Errno 5] ZAP failed to access: http://localhost:8080`.
2. `http://host.docker.internal:8080` (with `--add-host=host.docker.internal:host-gateway`) —
   `curl` exit code 7 (connection refused).
3. The Docker bridge gateway IP (`172.17.0.1`, from `docker network inspect bridge`) — same failure.

`docker info` confirmed the operating system as `Docker Desktop` running
on this WSL2 distro's own kernel (`Kernel Version:
6.18.33.2-microsoft-standard-WSL2`) — easy to assume this means containers
share the WSL2 distro's network namespace the way a native Linux Docker
Engine install would. They don't: Docker Desktop runs its containers
inside its own separate lightweight VM (`docker-desktop`), reachable from
Windows/WSL2 distros via the Docker CLI, but not sharing loopback or
`eth0` with the WSL2 distro the `docker` command is being run from. A
`docker0` bridge interface didn't even exist in `ip addr show docker0`
inside this distro — because the real Docker daemon (and its bridge)
lives in the separate VM, not here.

**Fix:** find the calling WSL2 distro's own routable IP address
(`hostname -I` or `ip -4 addr show eth0`) and target that instead —
Docker Desktop's VM *can* reach other WSL2 distros over the shared
Hyper-V virtual switch, even though it can't reach a bare loopback bind.

```bash
hostname -I   # e.g. 172.31.225.38
docker run zaproxy/zap-stable zap-full-scan.py -t http://172.31.225.38:8080 ...
```

**How to apply:** Before assuming a container-to-host connectivity
failure means the target process isn't actually running, check `docker
info`'s `Operating System` field — `Docker Desktop` is the signal this
lesson applies, a plain `Docker Engine` value on native Linux usually
does not have this problem and `--network host`/the bridge gateway work
as expected there. On Docker Desktop specifically, try the calling
distro/host's own routable IP before spending more time debugging
`--network host` or `host.docker.internal`, both of which look like they
should work and silently don't.

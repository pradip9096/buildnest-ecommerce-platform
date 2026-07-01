# Quality Gate Ratchet Pattern

## What It Is

A **ratchet mechanism** is a one-way constraint: a quality metric is allowed to improve but never regress. Once a threshold is reached and locked in, any future drop fails the build automatically.

In software engineering this is formally called a **fitness function** — an objective, automated measure that guides architectural decisions and is enforced in CI. The term comes from Neal Ford & Rebecca Parsons, *Building Evolutionary Architectures* (O'Reilly, 2017).

## Formal Terms

| Term | Definition |
|---|---|
| **Fitness function** | An objective, automated metric that evaluates a system property (coverage, mutation score, latency, coupling). Gated in CI. |
| **Quality gate** | The enforcement mechanism — a threshold that fails the build when violated. Used formally in tools like SonarQube and PIT. |
| **Ratchet mechanism** | A one-way constraint: progress is allowed, regression is not. The gate only ever moves forward. |
| **Monotonic improvement constraint** | The academic framing: a metric required to be non-decreasing over time. |
| **Progressive tightening** | The strategy of incrementally raising standards at defined checkpoints (milestones, releases). |
| **Broken windows theory** | The rationale: once a metric is allowed to slip, further slippage accelerates. A floor prevents the spiral. Originated in criminology (Wilson & Kelling, 1982); applied to software by the Pragmatic Programmers. |

## How It Is Applied in BuildNest

The PIT mutation score is gated in `pom.xml` via `<mutationThreshold>`. Once the actual score reaches a target, the threshold is bumped to lock it in.

```xml
<!-- mutationThreshold gates on MUTATION SCORE = killed / generated.
     Ratchet plan: 77% mid-M4 → 79% end-M4 → 81% start-M5 → 83% end-M5. -->
<mutationThreshold>77</mutationThreshold>
```

### Ratchet Schedule

| Checkpoint | Target | Milestone |
|---|---|---|
| Mid-M4 | 77% | Lock in after issue #277 work |
| End-M4 | 79% | Before v0.5.0 tag |
| Start-M5 | 81% | Gate raised at milestone boundary |
| End-M5 | 83% | Production bar before v1.0.0 |

Each step is ~2 percentage points (~30–35 additional mutation kills). The approach per step: find service classes with high survived-mutation counts and zero no-coverage mutations (assertions too weak, not missing tests), then add `ArgumentCaptor` state assertions and return-value checks.

## Why Not Just Set the Final Target Now

Setting `<mutationThreshold>83</mutationThreshold>` today would immediately fail the build. The ratchet gives the team time to earn each step through deliberate test improvement rather than forcing a single large spike of work. It also makes the progress visible and auditable at each milestone.

## References

- Ford, N. & Parsons, R. (2017). *Building Evolutionary Architectures*. O'Reilly. — source of the fitness function concept.
- Wilson, J.Q. & Kelling, G.L. (1982). "Broken Windows". *The Atlantic*. — origin of the broken-windows theory.
- Hunt, A. & Thomas, D. (1999). *The Pragmatic Programmer*. — applied broken-windows theory to software quality.
- [PIT Mutation Testing](https://pitest.org) — the tool implementing the gate in this project.
- BuildNest issue #277 — initial PIT remediation that established the ratchet baseline.
- BuildNest issue #278 — ArchUnit naming convention rule preventing silent PIT exclusions.

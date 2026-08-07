# Secrets Management and Rotation Procedure

Tracks #132 (CFG-01), SDP Appendix B, RGAR §11. Covers where every production secret lives,
minimum-strength requirements, and the rotation procedure for each.

## Where Secrets Live

No secret is ever committed to the repository. `backend/.env.example` is the tracked template —
every `Class: SECRET` variable in it must always be empty (verify with
`grep -nE '^VAR_NAME='` before any commit that touches the file). Real values live in one of two
places:

- **Local development**: `backend/.env` (gitignored — confirm with `git check-ignore -v backend/.env`).
- **CI / production**: [GitHub Actions Secrets](https://github.com/pradip9096/buildnest-ecommerce-platform/settings/secrets/actions),
  set via `gh secret set VAR_NAME --repo pradip9096/buildnest-ecommerce-platform`.

Currently configured repo secrets (`gh secret list`): `CODECOV_TOKEN`, `DOCKER_PASSWORD`,
`DOCKER_USERNAME`, `NVD_API_KEY`, `SONAR_TOKEN`. The deployment workflow (`deploy.yml`) also
references `PROD_SSH_HOST`/`PROD_SSH_USER`/`PROD_SSH_KEY`/`PROD_GHCR_PAT`,
`STAGING_SSH_HOST`/`STAGING_SSH_USER`/`STAGING_SSH_KEY`/`STAGING_GHCR_PAT`, and
`SLACK_WEBHOOK_URL` — **none of these are configured yet**, because no production or staging host
has been provisioned (see the `project_hosting_decision_deferred` memory / ADR
`0003-ssh-docker-compose-plus-ghcr-as-the-deployment-mechanism.md`). Set them via `gh secret set`
at the point a real host is chosen — they are not needed before then.

## Minimum Strength Requirements

| Secret | Minimum | Generation |
|---|---|---|
| `JWT_SECRET` | 512 bits (64 bytes) | `openssl rand -base64 64` |
| `MYSQL_ROOT_PASSWORD` / `SPRING_DATASOURCE_PASSWORD` | 32 random characters | `openssl rand -base64 32` |
| `REDIS_PASSWORD` | 32 random characters | `openssl rand -base64 32` |
| `MONITORING_PASSWORD` | Non-default, no fixed length — `SecurityConfig` fails startup on the default value | `openssl rand -base64 24` |

**`MONITORING_PASSWORD` deployment caveat**: `backend/docker-compose.yml` sets
`MONITORING_PASSWORD: ${MONITORING_PASSWORD:-changeme-monitoring-password}` — a local-dev
convenience default. If the env var isn't exported before `docker compose up` in production,
Compose silently substitutes the weak default into the container's environment; `SecurityConfig`'s
`@PostConstruct` check does catch this and fails the app at startup (it compares against the
literal `changeme-monitoring-password` marker), but the container itself still starts with the
weak value attempted first. **Before any production `docker compose up`, explicitly verify
`MONITORING_PASSWORD` is exported in the shell/CI environment** — don't rely solely on the
in-app fail-fast check as the only safety net.
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | Issued by Razorpay dashboard | N/A — copy from Razorpay dashboard |
| `RAZORPAY_WEBHOOK_SECRET` | Issued by Razorpay dashboard | N/A — copy from Razorpay webhook config |

**Payment gateway note**: this codebase integrates Razorpay, not Stripe (no Stripe code exists in
the repo). Use **test-mode keys** (`rzp_test_...`) in staging/non-production environments and
**live keys** (`rzp_live_...`) only in the production secret store, per `RAZORPAY_KEY_ID`'s
`.env.example` comment ("Use test_* keys in non-production environments").

## Rotation Procedure

1. Generate the new value with the command in the table above (or from the third-party dashboard
   for Razorpay/OAuth credentials).
2. Set it in the target store:
   - Production/CI: `gh secret set VAR_NAME --repo pradip9096/buildnest-ecommerce-platform`
     (prompts for the value on stdin — never pass it as a CLI argument, which would leak it into
     shell history).
   - Local dev: edit `backend/.env` directly (never `.env.example`).
3. Redeploy / restart the affected service so it picks up the new value. `JWT_SECRET` rotation
   supports zero-downtime rollover: set the **old** value as `JWT_SECRET_PREVIOUS`
   (`jwt.secret.previous`) before rotating `JWT_SECRET` itself — `JwtTokenProvider` validates
   against the current secret first and falls back to the previous one on `SecurityException`,
   so already-issued tokens keep working until they expire naturally (see
   `spring/spring-security.md`).
4. Confirm the rotation took effect (a failed auth/DB/cache connection after restart means the
   new value wasn't picked up — check the target store, not the app code).
5. If a secret is ever found **committed** (not just staged) to a tracked file, treat it as
   compromised regardless of whether it was pushed — git history retains it even after a later
   commit removes it. Rotate immediately and see
   [`real-secret-pasted-into-env-example-instead-of-env.md`](../wiki/learned-lessons/real-secret-pasted-into-env-example-instead-of-env.md)
   for the specific `.env.example` failure mode this guards against.

## Recommended Rotation Cadence

Per SRS SEC-12/SEC-13: `JWT_SECRET` every **90 days**, database password
(`SPRING_DATASOURCE_PASSWORD`/`MYSQL_ROOT_PASSWORD`) every **180 days**. No formal
schedule/expiry-policy enforcement is wired in today (no secrets manager — repo secrets are
managed manually via `gh secret set`); this document is the operational runbook those two SRS
rows require, tracked via manual calendar reminder until an automated policy exists. Rotate
immediately, regardless of schedule, on suspected exposure (e.g. an accidentally committed value,
a departed team member who had access, a third-party breach notification for Razorpay/OAuth
providers).

# Database Backup and Restore Procedure

Tracks #121 (OPS-03), SRS NFR-AVL-01. Target: RPO ≤4h, RTO ≤1h.

## Backup

`backend/scripts/backup-db.sh` dumps the `buildnest_ecommerce` database from the
running `buildnest-mysql` Docker container, gzips it with a timestamped filename
(`buildnest_ecommerce_YYYYMMDD_HHMMSS.sql.gz`), and prunes backups older than
`RETENTION_DAYS` (default 30).

```bash
cd backend
./scripts/backup-db.sh
```

Requires `MYSQL_ROOT_PASSWORD` and `MYSQL_DATABASE` — read automatically from
`backend/.env` if present, or from the environment. Backups are written to
`backend/backups/` by default (gitignored — never committed).

### Schedule

`backend/scripts/backup-db.cron` installs a daily 02:00 UTC cron entry:

```bash
crontab -l | { cat; cat backend/scripts/backup-db.cron; } | crontab -
```

Edit the absolute path inside `backup-db.cron` to match the deployment host's
actual checkout location before installing — the shipped path
(`/opt/buildnest/...`) is a placeholder, since no production host has been
provisioned yet (see `project_hosting_decision_deferred` — this is being
delivered as tooling ready to install on whichever host is chosen, not tied to
a specific one). Cron output is appended to `backend/scripts/backup-db.log`.

## Restore

```bash
cd backend
./scripts/restore-db.sh --latest
# or restore a specific file:
./scripts/restore-db.sh backups/buildnest_ecommerce_20260803_020000.sql.gz
```

This overwrites the current contents of `buildnest_ecommerce` in the running
container. The script prints the elapsed restore time so it can be checked
against the RTO target.

When run interactively, it prompts for the database name to confirm the
destructive overwrite. Set `RESTORE_YES=1` to skip the prompt for scripted/
non-interactive DR automation (it's also skipped automatically when stdin
isn't a terminal, e.g. under cron).

## DR Drill — Verified Restore Procedure

Run this drill to validate RTO <1h end-to-end (repeat periodically, not just once):

1. Take a fresh backup: `./scripts/backup-db.sh`.
2. Note the row counts of a few key tables (`SELECT COUNT(*) FROM orders;`, etc.) for
   later comparison.
3. Simulate data loss: drop and recreate the database inside the container, or stop
   the container and destroy its volume in a disposable test environment — never
   against a database holding real data outside of a drill.
4. Time the restore: `time ./scripts/restore-db.sh --latest` (the script also
   self-reports elapsed seconds).
5. Re-run the same row-count queries and confirm they match step 2.
6. Record the elapsed time and confirm it is under 1 hour (60 minutes / 3600s).

### Verified drill result

| Date | Backup size | Restore time | RTO target met? | Notes |
|---|---|---|---|---|
| 2026-08-03 | 4.4M | 30s | Yes (30s << 1h) | Local dev `buildnest-mysql` container: dropped+recreated the database, restored `--latest`, verified `users` table row count matched exactly (5=5) pre/post. `audit_logs`'s `information_schema.table_rows` estimate differed slightly between runs — expected InnoDB approximate-statistics behavior, not a data-loss signal; no app was running to write new rows during the drill. |

## Retention

Backups older than 30 days are deleted automatically by `backup-db.sh` on every
run (`RETENTION_DAYS`, default 30) — no separate cleanup job is needed.

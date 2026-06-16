# AGENTS.md

> Canonical agent/operator working rules for Passport-IAM. Read this, then `TRUTH.md` + `PROJECT_STATE.md`, before working. Standards live in `TRUTH/standards/`.

## Role
- Passport-IAM / Passport-Pro is a Keycloak-derived IAM fork providing human/service/agent identity, Agency/LBAC delegation, and APIS v2.0 Agent Passport issuance. It is the trust layer under PresenceOS, COLLAB, and the BlackBox node family.
- Application code lives under `Passport-Pro/`.
- Preserve the running production system. Verify claims with commands before reporting success.

## Environment
- Repo root: `/home/cory/Documents/Passport-IAM` (workstation) — Claude edits here.
- VM deploy host: `/home/ubuntu/Passport-IAM/Passport-IAM` — Codex deploys here.
- Public admin URL: `https://passport.aetherpro.us/admin/master/console/`
- Git remote: `git@github.com:AetharaAI/Passport-IAM.git`
- Working branch: `deploy/agency-405`; stable baseline: `main`.

## Infra Truth
- Host nginx owns 80/443 → `proxy_pass http://127.0.0.1:8080`. Services: `passport-server`, `passport-postgres`, `passport-redis-stack`.
- Stack: Quarkus 3.31.1, RESTEasy Reactive. Agency extension is baked into `lib/lib/main`, NOT `providers/`.

## Current Mission
1. Verify end-to-end Agent Passport minting against the live server.
2. Productize Passport into a pullable Docker/OCI image for PresenceOS / Echo Pro nodes.
3. Support the multi-issuer model (PassportAlliance.org) and APIS v2.1.

## Operating Rules
- Work from observed truth, not assumptions. Read before editing. Make the smallest safe change; no scope-creep refactors or unrelated file churn.
- Do not commit secrets, private keys, tokens, `.env`, DB dumps, or generated folders.
- Do not touch Postgres or Redis during Passport deploys. Stop/start only the `passport` service.
- `127.0.0.1`/`localhost` = the local machine; it does not reach the VM without a tunnel. Inspect listeners/containers before starting anything (`ss -ltnp`, `docker ps`, `docker compose ps`); existing services have priority.
- Claude/Codex split: Claude edits locally on a shared branch; Codex deploys on the VM. Codex VM prompts go in chat as a copyable fenced block, never as a committed `.md`.
- Agency REST gotcha: any admin REST extension jar needs `META-INF/beans.xml` or RESTEasy Reactive won't dispatch its methods (405). Fix the `lib/lib/main` copy; never add a `providers/` duplicate (breaks Liquibase). Run `kc.sh build` after any jar swap.
- Don't claim success without build/test/runtime verification. Report root cause, files changed, commands run, results, blockers, and the exact next command.

## Canonical Docs (in `TRUTH/`)
- `TRUTH/AGENTS.md` (this file) · `TRUTH/PROJECT_STATE.md` · `TRUTH/CHANGELOG.md` · `TRUTH/TRUTH.md`
- Standards (do not modify unless instructed): `TRUTH/standards/` — operator profile, git-workflow discipline, IP hygiene, AI-readable crawl support.

## Standard Workflow
1. Verify repo/runtime state (`git status -sb`, `git branch --show-current`).
2. Make scoped changes on the working branch.
3. Update canonical docs when production truth or workflow changes.
4. Build/test the smallest relevant surface.
5. Review diff; commit at a validated checkpoint; push the branch.
6. Merge to `main` only at validated checkpoints; tag; return to the working branch.
7. On the VM: pull, rebuild, re-augment, restart `passport`, verify live.

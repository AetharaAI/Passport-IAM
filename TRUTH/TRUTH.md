# TRUTH.md

> Canonical production-truth snapshot for Passport-IAM. Keep terse. If production truth changed, update this file. Last updated: 2026-06-16.

## Identity
- Project name: Passport-IAM / Passport-Pro
- Purpose: Keycloak-derived identity authority for AetherPro — human/service/agent identity, Agency/LBAC delegation, and APIS v2.0 Agent Passport issuance. The trust layer under PresenceOS / COLLAB / BlackBox nodes.
- Code root: `Passport-Pro/`
- GitHub remote: `git@github.com:AetharaAI/Passport-IAM.git`

## Runtime
- Public admin console: `https://passport.aetherpro.us/admin/master/console/`
- Public agency JWKS: `GET https://passport.aetherpro.us/realms/{realm}/agency/jwks`
- Realms in use: `master`, `syndicate`
- Local repo root: `/home/cory/Documents/Passport-IAM`
- VM repo root (deploy host): `/home/ubuntu/Passport-IAM/Passport-IAM`
- VM mounted distribution: `Passport-Pro/quarkus/dist/target/passport-999.0.0-SNAPSHOT`, run via `docker compose` service `passport` (`./bin/kc.sh start`, non-optimized → re-augments at start).

## Infra
- Public host fronted by host nginx (ports 80/443) → `proxy_pass http://127.0.0.1:8080`. Traefik exists but is only on 8081/8082 and is not in this path.
- Runtime services: `passport-server`, `passport-postgres`, `passport-redis-stack`. Do not touch Postgres or Redis during Passport deploys.
- Provider/region/instance: OVHcloud-class VM (verify exact details before using in any formal/financial/gov material).

## Current Production Truth
- Passport admin console is live; Agency/LBAC (issuer console) tab is visible and functional.
- Agency admin REST API is LIVE and dispatching as of 2026-06-16 — the long-standing 405-on-every-verb failure is RESOLVED. Verified live (realm=master, authed admin token): GET config/principals/principals-count → 200; OPTIONS → 200; all verbs non-405.
- APIS v2.0 mint endpoint `POST /admin/realms/{realm}/agency/passports/mint` is reachable (dispatch unblocked). A full end-to-end mint + JWT-verify-against-JWKS has NOT yet been run.
- Agency extension is packaged into the augmented app at `lib/lib/main/com.aetherpro.passport.passport-agency-*.jar` (NOT `providers/`). It must carry `META-INF/beans.xml` (application-archive marker) or RESTEasy Reactive will not index its JAX-RS methods (→ 405). A duplicate copy in `providers/` breaks startup (Liquibase finds the changelog twice).

## Runtime Safety
- `127.0.0.1`/`localhost` always mean the machine running the command; a localhost URL on the workstation does not reach the VM without an SSH tunnel.
- Inspect listeners/containers before starting any service (`ss -ltnp`, `docker ps`, `docker compose ps`). Existing services have priority; do not displace a port without explicit approval.
- Browser URLs do not expand shell variables — use a real realm (`master`/`syndicate`), not `$realm`.

## Operator Mechanics
- Agency backend build: `cd Passport-Pro && ./mvnw -pl passport-extensions/agency -am -DskipTests clean install` (jar must contain `META-INF/beans.xml`).
- Full distribution build/deploy reference: `BUILD-AND-DEPLOY.sh` (then `kc.sh build` + restart `passport`).
- Admin UI build: `cd Passport-Pro/js/apps/admin-ui && pnpm build`.
- Verification (live): authenticated verb matrix against `/admin/realms/{realm}/agency/*`; expect non-405, OPTIONS 200.
- Active working branch: `deploy/agency-405`. Main (`main`): stable, clean, deployable.
- Checkpoint rule: merge to `main` only at validated checkpoints; tag the checkpoint; return to the working branch. See `standards/GIT-WORKFLOW-DISCIPLINE.md`.
- Secrets: `AGENCY_KEY_ENCRYPTION_SECRET` (16/24/32 bytes); `APIS_REALM_ISSUER_PRIVATE_KEY_PATH` (EC P-256 PKCS8). Compose mounts `Passport-Pro/secrets/` read-only at `/opt/passport/secrets/`. Never commit secrets.

## Operator Profile Reference
- See `TRUTH/standards/AETHER_OPERATOR_PROFILE.md` for operator identity, company facts, product naming, and public-positioning rules. Use when operator identity or standing company facts materially affect execution. Do not publish internal details from it.

## Ownership
- Responsible operator: Cory Gibson / AetherPro Technologies.
- Responsible agent role: preserve production reality, make changes verifiable, keep this truth current.

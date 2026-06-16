# TRUTH.md

## Identity
- Project name: Passport-IAM / Passport-Pro
- Purpose: Keycloak-derived identity authority for AetherPro, Agency/LBAC, and APIS Agent Passport issuance.
- Primary code root: `Passport-Pro/`
- Related platform direction: PresenceOS / Aether node deployments.

## Runtime
- Public admin URL observed: `https://passport.aetherpro.us/admin/master/console/`
- Live realm observed in screenshots: `syndicate`
- Local repo root: `/home/cory/Documents/Passport-IAM`
- Local code root: `/home/cory/Documents/Passport-IAM/Passport-Pro`
- Deploy path on VM (verified 2026-06-16): repo at `/home/ubuntu/Passport-IAM/Passport-IAM`; mounted distribution at `Passport-Pro/quarkus/dist/target/passport-999.0.0-SNAPSHOT`; server runs via `docker compose` service `passport` (`./bin/kc.sh start`, non-optimized, so it re-augments at start).
- The agency extension is a declared dependency of `quarkus/server`, so it is packaged into the augmented app at `lib/lib/main/com.aetherpro.passport.passport-agency-999.0.0-SNAPSHOT.jar` — NOT in `providers/` (providers/ holds only README). A duplicate copy in `providers/` breaks startup (Liquibase finds the changelog twice).

## Runtime Safety
- `127.0.0.1` and `localhost` always mean the machine where the browser or command is running. A localhost URL on the operator workstation does not reach the VM unless an SSH tunnel or another explicit port forward is active.
- Before starting any dev server, container, listener, tunnel, or long-running service on any machine, verify current listeners and running services first.
- Existing services have priority over new work. Do not kill, restart, replace, or bind over an existing port unless Cory explicitly approves that action.
- Useful read-only checks before runtime work include `ss -ltnp`, `docker ps`, `docker compose ps`, and service-specific status/log commands.
- Browser URLs do not expand shell variables. A literal `$realm` in a browser path is not a real realm name; use the actual realm such as `master` or `syndicate`, or run a shell command where the variable is defined.

## Infra
- Provider: unknown
- Region: unknown
- Instance type: unknown
- Tailscale IP: unknown
- Runtime: likely Docker-based from operator notes, but exact compose/service path is not yet verified.

## Current Production Truth
- The Passport admin console is live at `passport.aetherpro.us`.
- The Agency/LBAC tab is visible.
- The live UI currently shows working Agency dashboard and Create Principal screens.
- As of 2026-06-16, the Agency admin REST API is LIVE and dispatching: the prior 405-on-every-verb failure is RESOLVED in production. Verified live (realm=master, authed admin token): GET config/principals/principals-count → 200, OPTIONS → 200, all verbs non-405. Root cause and deploy steps are in CHANGELOG 2026-06-16.
- The APIS v2.0 mint endpoint `POST /admin/realms/{realm}/agency/passports/mint` is reachable; a full end-to-end mint + JWT verification has not yet been run.
- The agency public JWKS is served at `GET /realms/{realm}/agency/jwks`.

## Operator Mechanics
- Frontend admin UI build command: `cd Passport-Pro/js/apps/admin-ui && pnpm build`
- Targeted Agency backend compile: `cd Passport-Pro && ./mvnw -pl passport-extensions/agency -DskipTests clean compile`
- Targeted Agency backend test command: `cd Passport-Pro && ./mvnw -pl passport-extensions/agency test`
- Documentation Maven preflight, if IDE shows docs POM/plugin errors:
  - `cd Passport-Pro && ./mvnw -f docs/documentation/header-maven-plugin/pom.xml -DskipTests install`
  - `cd Passport-Pro && ./mvnw -f docs/documentation/pom.xml -DskipProjectTests validate`
- License processor Maven preflight:
  - `cd Passport-Pro && ./mvnw -f distribution/pom.xml -pl licenses-common,maven-plugins/licenses-processor -am validate`
- Active working branch observed locally: `main`
- Main branch policy: keep stable, clean, and deployable.
- Checkpoint merge rule: merge to main only at validated checkpoint stages.

## Deployment Notes
- This fork has local Maven artifacts and generated build outputs that may be required for a successful VM rebuild.
- The initial VM deployment reportedly required manually copying build artifacts because `.gitignore` and Docker context rules excluded files needed by the running node.
- Current deploy shape is still source-build and mounted-distribution driven; it is not yet a clean pull-and-run published Passport image workflow.
- Productization target: publish Passport as a pullable Docker/OCI image so PresenceOS nodes and future customer installs can deploy it without bespoke source builds on the target host.
- Agency private-key encryption requires `AGENCY_KEY_ENCRYPTION_SECRET` to be set to a 16, 24, or 32 byte value.
- Docker Compose passes Agency/APIS env vars into the `passport` container and mounts `Passport-Pro/secrets/` read-only at `/opt/passport/secrets/`.
- Before productizing PresenceOS node installs, capture the exact VM commands for:
  - clone/pull location
  - Maven build/package command
  - admin UI build command
  - Docker image build command
  - compose/service restart command
  - post-restart verification URL

## Operator Profile Reference
- Template source: local `TRUTH/` folder provided during documentation setup.
- PresenceOS node context was provided locally as planning input; it is not yet canonical production deploy truth for this repo.

## Ownership
- Responsible operator: Cory Gibson / AetherPro.
- Responsible agent role: preserve production reality, make changes verifiable, and document deployment truth as it is discovered.

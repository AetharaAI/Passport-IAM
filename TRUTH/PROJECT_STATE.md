# PROJECT_STATE.md

> Current implementation state for Passport-IAM. Last updated: 2026-06-16.

## Repo
- Name: Passport-IAM
- Root: `/home/cory/Documents/Passport-IAM`
- Code root: `Passport-Pro/`
- Public URL: `https://passport.aetherpro.us/admin/master/console/`
- GitHub remote: `git@github.com:AetharaAI/Passport-IAM.git`
- Deploy target: VM `/home/ubuntu/Passport-IAM/Passport-IAM`, mounted dist + `docker compose` service `passport`.

## Production Status
- Passport-Pro is live; Agency/LBAC tab visible and functional.
- Agency admin REST is LIVE and dispatching (2026-06-16). The 405-on-every-verb failure is RESOLVED in production. Verified live (realm=master): GET config/principals/principals-count → 200, OPTIONS → 200, all verbs non-405.
- APIS v2.0 mint endpoint `POST /admin/realms/{realm}/agency/passports/mint` is reachable; a real end-to-end mint (enable agent passports → create principal → mint → verify Passport JWT against live JWKS) has NOT been run yet.

## Deploy Reality
- Keycloak-derived Java/Quarkus 3.31.1 (RESTEasy Reactive / `quarkus-rest`) backend + React admin UI fork.
- Distribution `passport-999.0.0-SNAPSHOT`; agency artifact `passport-agency-999.0.0-SNAPSHOT`, baked into `lib/lib/main/com.aetherpro.passport.passport-agency-999.0.0-SNAPSHOT.jar` (declared dependency of `quarkus/server`).
- Maven wrapper `Passport-Pro/mvnw`; `pnpm` for the admin UI; `gh` installed locally.
- Deploy is still source-build + mounted-dist + hand-patched jar; not yet a clean pullable image.

## Repo Alignment Status
- Working branch: `deploy/agency-405` (ahead of `origin` until pushed). `main`: stable baseline.
- Worktree may contain pre-existing modified/untracked files outside the current task — do not assume every dirty file is ours.

## Dependencies
- Quarkus backend; React admin UI (`Passport-Pro/js/apps/admin-ui`); Agency extension (`Passport-Pro/passport-extensions/agency`).
- Postgres + Redis Stack for runtime. APIS minting needs the issuer EC key; DNS publication needs Cloudflare env (currently the insecure 1.1.1.1 path — slated for change in APIS v2.1).

## Remaining Gaps
- End-to-end Agent Passport minting not yet exercised live (endpoint dispatches; mint + JWT verification still to be smoke-tested).
- No clean pullable Docker/OCI image yet — productization for PresenceOS / Echo Pro nodes is the next infra milestone.
- Multi-issuer / verified Issuance Authority model (PassportAlliance.org-registered issuers) is design-stage.
- `lib/lib/main` jar swap on the VM was a hand-patch; canonical full-dist rebuild path should be captured.
- No automated Agency minting test exists.
- APIS v2.1 (new definitions + security changes, replacing the 1.1.1.1 Cloudflare dependency) is in-progress in browser sessions; spec not finalized. Draft `aetherspace-workspace-isolation-spec.md` downloaded to repo root.

## Key Files
- `Passport-Pro/passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/admin/AgencyAdminResource.java`
- `Passport-Pro/passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/protocol/AgencyJwksEndpoint.java`
- `Passport-Pro/js/apps/admin-ui/src/agency/` (AgencyDashboard, AgencyConfig, MintPassportForm, DelegateForm, PrincipalsList)
- `BUILD-AND-DEPLOY.sh`, `Passport-Pro/docker-compose.yml`
- `TRUTH/` (canonical docs + standards)

## Next Steps
1. End-to-end mint smoke test (the "ultimate question"): enable agent passports, create a principal, `POST /agency/passports/mint`, verify the returned Passport JWT validates against `GET /realms/{realm}/agency/jwks`.
2. Productize into a pullable Docker/OCI image (AetherOps registry) so PresenceOS / Echo Pro nodes `docker pull` and run Passport like Keycloak.
3. Capture the canonical full-dist rebuild + deploy commands (replace the hand-patch path).
4. Design the multi-issuer / verified Issuance Authority model (PassportAlliance.org).
5. APIS v2.1: definitions + security changes (replace insecure 1.1.1.1 Cloudflare dependency). Docs in `~/Aether-Admin-Platform/COLLAB`.

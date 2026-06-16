# PROJECT_STATE.md

## Repo
- Name: Passport-IAM
- Root: `/home/cory/Documents/Passport-IAM`
- Code root: `Passport-Pro/`
- Public URL: `https://passport.aetherpro.us/admin/master/console/`
- GitHub remote: `git@github.com:AetharaAI/Passport-IAM.git`

## Production Status
- Passport-Pro is live.
- Agency/LBAC tab is visible in the admin console.
- Agency admin REST is LIVE and dispatching as of 2026-06-16. The long-standing 405-on-every-verb failure is RESOLVED in production (root cause was a stale, un-indexed agency jar in `lib/lib/main`; fix was the `beans.xml` application-archive marker from commit `57bae5d` reaching a re-augmented distribution). See CHANGELOG 2026-06-16.
- Verified live (realm=master): GET config/principals/principals-count all 200, OPTIONS 200; all verbs non-405.
- The APIS v2.0 mint endpoint `POST /admin/realms/{realm}/agency/passports/mint` is now reachable (dispatch unblocked). A full end-to-end mint (enable agent passports → create principal → mint → verify Passport JWT against the live JWKS) has NOT yet been run/verified.

## Deploy Reality
- The project is a Keycloak-derived Java/Quarkus + React admin UI fork.
- Maven wrapper exists at `Passport-Pro/mvnw`; plain `mvn` is not installed locally.
- `pnpm` is available and admin UI builds through Wireit/Vite.
- GitHub CLI was installed locally as `~/.local/bin/gh` at version `2.92.0`.
- Current node deploy shape still depends on locally built Quarkus distribution artifacts and mounted runtime files rather than a published pullable Passport image.
- Productization goal: Passport should become a pullable Docker/OCI image that a node can run without a bespoke source build on the target host.

## Repo Alignment Status
- Local branch observed: `main`
- Worktree contains many pre-existing modified/untracked files outside the Agency route/minting edits.
- Do not assume every dirty file was changed by the current agent.

## Dependencies
- Passport-Pro Java/Quarkus backend.
- React admin UI under `Passport-Pro/js/apps/admin-ui`.
- Agency extension under `Passport-Pro/passport-extensions/agency`.
- Postgres/Redis/Docker services are used for runtime verification, but long-running startup scripts should not be treated as quick tests.

## Remaining Gaps
- End-to-end Agent Passport minting has not been exercised against the live server yet (endpoint now dispatches; mint flow + JWT verification still to be smoke-tested).
- No clean pullable Docker/OCI image yet — deploy is still source-build + mounted-distribution; productization into a `docker pull`-style flow (for PresenceOS / Echo Pro nodes) is the next infra milestone.
- Multi-issuer / verified Issuance Authority model (PassportAlliance.org-registered issuers) is design-stage, not implemented.
- The `lib/lib/main` jar swap on the VM was a hand-patch; a clean full dist rebuild reproduces it from source (57bae5d), but the rebuild path should be captured as the canonical deploy.
- No automated Agency minting unit test exists yet.
- APIS DNS publication is optional and only runs when Cloudflare env vars are set.
- Runtime APIS minting requires issuer key configuration.
- Agency private-key encryption requires `AGENCY_KEY_ENCRYPTION_SECRET` to be set to a 16, 24, or 32 byte value.
- Compose mounts `Passport-Pro/secrets/` into the container at `/opt/passport/secrets/`; keep this directory untracked.

## Key Files
- `agency-tab-plan.md`
- `Passport-Pro/js/apps/admin-ui/src/agency/routes.tsx`
- `Passport-Pro/js/apps/admin-ui/src/agency/AgencyDashboard.tsx`
- `Passport-Pro/js/apps/admin-ui/src/agency/DelegateForm.tsx`
- `Passport-Pro/js/apps/admin-ui/src/agency/MintPassportForm.tsx`
- `Passport-Pro/js/apps/admin-ui/src/agency/AgencyConfig.tsx`
- `Passport-Pro/passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/admin/AgencyAdminResource.java`
- `Passport-Pro/passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/admin/representations/AgentPassportRepresentation.java`
- `PresenceOS/PresenceOS_Aether_Node_Structure.md`

## Next Steps
1. End-to-end mint smoke test (the "ultimate question"): enable agent passports on a realm, create a principal, `POST /agency/passports/mint` with a real `AgentPassportRepresentation`, and verify the returned Passport JWT validates against the live JWKS (`GET /realms/{realm}/agency/jwks`).
2. Productize the deploy into a pullable Docker/OCI image (AetherOps registry) so PresenceOS / Echo Pro nodes can `docker pull` and run Passport like Keycloak, without bespoke source builds on the host.
3. Capture the canonical full-dist rebuild + deploy commands in `TRUTH.md` (replacing the hand-patch path).
4. Design the multi-issuer / verified Issuance Authority model (PassportAlliance.org-registered providers minting verifiable Agent Passports).
5. APIS v2.1 work (definitions + changes, including replacing the insecure 1.1.1.1 Cloudflare dependency) — docs in `~/Aether-Admin-Platform/COLLAB`.

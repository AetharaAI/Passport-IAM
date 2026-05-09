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
- Local changes implement missing Agency frontend routes/forms and backend APIS v2.0 passport minting.
- Local changes have not yet been deployed to the VM in this session.

## Deploy Reality
- The project is a Keycloak-derived Java/Quarkus + React admin UI fork.
- Maven wrapper exists at `Passport-Pro/mvnw`; plain `mvn` is not installed locally.
- `pnpm` is available and admin UI builds through Wireit/Vite.
- GitHub CLI was installed locally as `~/.local/bin/gh` at version `2.92.0`.

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
- VM deploy commands are not fully documented.
- Exact Docker build context and ignored artifact issue must be verified on the VM.
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
1. Push a scoped commit with Agency route/minting/docs changes after confirming the intended file list.
2. On the VM, pull the commit and run the targeted rebuild commands.
3. Verify live admin routes:
   - `/admin/master/console/#/syndicate/agency`
   - `/admin/master/console/#/syndicate/agency/principals/new`
   - `/admin/master/console/#/syndicate/agency/delegates/new`
   - `/admin/master/console/#/syndicate/agency/passports/mint`
   - `/admin/master/console/#/syndicate/agency/configure`
4. Capture the exact VM deploy/restart commands in `TRUTH.md`.

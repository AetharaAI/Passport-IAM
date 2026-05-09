# AGENTS.md

## Role
- This repository is Passport-IAM / Passport-Pro, a Keycloak-derived IAM fork for Agency/LBAC and APIS Agent Passport minting.
- The actual application codebase currently lives under `Passport-Pro/`.
- Agents must preserve existing user changes and verify claims with commands before reporting success.

## Environment
- Repo root: `/home/cory/Documents/Passport-IAM`
- Code root: `/home/cory/Documents/Passport-IAM/Passport-Pro`
- Public admin URL observed from screenshots: `https://passport.aetherpro.us/admin/master/console/`
- Git remote: `git@github.com:AetharaAI/Passport-IAM.git`
- Current branch observed locally: `main`

## Infra Truth
- Production host, deploy path, and Docker/runtime layout are not fully documented yet.
- Known deploy reality: the live system is already running at `passport.aetherpro.us`, and the VM deploy previously required manual handling of build artifacts that were ignored by `.gitignore`/Docker context rules.

## Current Mission
- Make the Agency/LBAC admin tab functional.
- Preserve the existing live Passport-Pro deployment shape.
- Document the repeatable build/deploy steps enough that the VM can pull and rebuild safely.

## Operating Rules
- Work from observed truth, not assumptions.
- Read before editing.
- Do not revert unrelated worktree changes.
- Prefer minimal reversible changes.
- Do not claim success without build/test/runtime verification.
- Treat Maven local artifact state as part of the build reality for this fork.
- Do not commit secrets, private keys, database dumps, `.env` files, or generated dependency folders.

## Canonical Docs
- `AGENTS.md`
- `PROJECT_STATE.md`
- `CHANGELOG.md`
- `TRUTH.md`

## Known Production Facts
- Agency tab is visible in the admin console.
- Existing live screenshots show the Agency dashboard and Create Principal route loading.
- Some Agency routes/buttons previously landed on Page Not Found.

## Known Gaps
- Exact VM deploy path and rebuild command need live verification on the VM.
- Docker build context and `.gitignore` artifact requirements need to be confirmed before productizing appliance installs.
- APIS minting requires `APIS_REALM_ISSUER_PRIVATE_KEY_PATH` to point to a valid EC P-256 PKCS8 private key.
- Agency private-key encryption requires `AGENCY_KEY_ENCRYPTION_SECRET` to be set to a 16, 24, or 32 byte value.

## Standard Workflow
1. Verify current repo/runtime state.
2. Make scoped changes.
3. Update canonical docs when production truth or operator workflow changes.
4. Build/test the smallest relevant surface.
5. Push only after checking worktree scope.
6. On the VM, pull, rebuild, restart, and verify the live URL.

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
- Deploy path on VM: unknown, must be verified before documenting as fact.

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
- The local repo contains new Agency route/form and APIS minting work that has not yet been pulled/rebuilt on the VM.

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
- Agency private-key encryption requires `AGENCY_KEY_ENCRYPTION_SECRET` to be set to a 16, 24, or 32 byte value.
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

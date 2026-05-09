# CHANGELOG.md

## 2026-05-09

### Agency / LBAC
- Wired Agency admin console routes for delegate creation, passport minting, and Agency configuration.
- Fixed Agency dashboard navigation that pointed at missing pages.
- Added backend compatibility endpoint for `POST /admin/realms/{realm}/agency/delegates`.
- Added backend APIS v2.0 minting endpoint for `POST /admin/realms/{realm}/agency/passports/mint`.
- Extended Agent Passport representation with APIS minting request/response fields.
- Replaced hardcoded Agency private-key encryption material with required `AGENCY_KEY_ENCRYPTION_SECRET` configuration.

### Verification
- `cd Passport-Pro && ./mvnw -pl passport-extensions/agency -DskipTests clean compile` succeeded.
- `cd Passport-Pro && ./mvnw -pl passport-extensions/agency test` succeeded; no test sources were present.
- `cd Passport-Pro/js/apps/admin-ui && pnpm build` succeeded.
- `cd Passport-Pro && ./mvnw -f docs/documentation/header-maven-plugin/pom.xml -DskipTests install` succeeded.
- `cd Passport-Pro && ./mvnw -f docs/documentation/pom.xml -DskipProjectTests validate` succeeded.
- `cd Passport-Pro && ./mvnw -f distribution/pom.xml -pl licenses-common,maven-plugins/licenses-processor -am validate` succeeded.

### Tooling
- Installed GitHub CLI locally at `~/.local/bin/gh`; observed version `2.92.0`.

### Documentation
- Added canonical root docs: `AGENTS.md`, `TRUTH.md`, `PROJECT_STATE.md`, and `CHANGELOG.md`.
- Captured current Maven/local-artifact and VM deployment caveats for future rebuild documentation.

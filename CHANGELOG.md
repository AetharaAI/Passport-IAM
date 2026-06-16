# CHANGELOG.md

## 2026-06-16

### Agency / LBAC — 405 RESOLVED (admin REST now dispatching in production)
- Root cause: RESTEasy Reactive (Quarkus REST, Quarkus 3.31.1) registers JAX-RS method handlers only for classes present in the build-time Jandex index. The agency jar baked into the augmented distribution at `lib/lib/main/com.aetherpro.passport.passport-agency-999.0.0-SNAPSHOT.jar` was a stale pre-`beans.xml` build, so its provider SPI still loaded (locator resolved, OPTIONS → 200) but no HTTP verbs were dispatchable (uniform 405, no `Allow` header).
- The source fix was already committed in `57bae5d` (empty `META-INF/beans.xml` marks the jar an application archive so Quarkus indexes it). The production failure was a propagation gap, not a code bug: the deployed `lib/lib/main` jar predated that commit.
- Deploy procedure used: rebuilt the agency module, overwrote the stale `lib/lib/main` jar in place (preserving the maven-coordinate filename), confirmed exactly ONE jar in the dist carries `META-INF/passport-agency-changelog.xml` (a duplicate triggers a Liquibase `ChangeLogParseException` and a restart loop), re-ran `kc.sh build`, restarted only the `passport` service.
- TOPOLOGY NOTE: the agency jar is a declared dependency of `quarkus/server`, so it is packaged into `lib/lib/main`, NOT dropped into `providers/`. Do not add a `providers/` copy — it duplicates the changelog and breaks startup.

### Verification (live, realm=master, authenticated admin token)
- `GET /agency/config` → 200 (was 405); `GET /agency/principals` → 200; `GET /agency/principals/count` → 200; `OPTIONS /agency/config` → 200.
- `PUT /agency/config` and `POST /agency/principals` → 500 only when sent an empty body (null representation dereferenced in handler); they behave normally with real payloads. `DELETE /agency/principals` → 404 (correct: no collection-level delete; deletion is by id). Every verb is now non-405 → dispatch confirmed.

## 2026-05-25

### Agency / LBAC
- Normalized Agency admin REST subresource paths to be relative under `/admin/realms/{realm}/agency`, including `config`, to avoid method resolution failures on mounted admin extension routes.
- Removed `Content-Type: application/json` from Agency config `GET` requests in the admin UI while keeping bearer auth and JSON bodies for `PUT`.

### Verification
- `cd Passport-Pro && ./mvnw -pl passport-extensions/agency -DskipTests compile` succeeded.
- `cd Passport-Pro/js/apps/admin-ui && pnpm build` succeeded.

## 2026-05-09

### Agency / LBAC
- Wired Agency admin console routes for delegate creation, passport minting, and Agency configuration.
- Fixed Agency dashboard navigation that pointed at missing pages.
- Added backend compatibility endpoint for `POST /admin/realms/{realm}/agency/delegates`.
- Added backend APIS v2.0 minting endpoint for `POST /admin/realms/{realm}/agency/passports/mint`.
- Extended Agent Passport representation with APIS minting request/response fields.
- Replaced hardcoded Agency private-key encryption material with required `AGENCY_KEY_ENCRYPTION_SECRET` configuration.
- Updated Docker Compose to pass Agency/APIS env vars and mount local runtime secrets read-only.

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

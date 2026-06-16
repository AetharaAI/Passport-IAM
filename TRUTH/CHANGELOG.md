# CHANGELOG.md

> Dated record of material changes (deploys, infra, API, auth, releases). Canonical location for Passport-IAM history.

## 2026-06-16

### TRUTH system
- Adopted the new AetherPro TRUTH documentation system. Canonical project docs (`TRUTH.md`, `PROJECT_STATE.md`, `CHANGELOG.md`, `AGENTS.md`) now live in `TRUTH/`; `standards/` and `templates/` carry durable doctrine. Repo-root `AGENTS.md` is now a thin pointer into `TRUTH/`. Removed stale flat duplicates superseded by `standards/`/`templates/`.

### Agency / LBAC — 405 RESOLVED (admin REST now dispatching in production)
- Root cause: RESTEasy Reactive (Quarkus REST, Quarkus 3.31.1) registers JAX-RS method handlers only for classes in the build-time Jandex index. The agency jar baked into the augmented distribution at `lib/lib/main/com.aetherpro.passport.passport-agency-999.0.0-SNAPSHOT.jar` was a stale pre-`beans.xml` build, so its provider SPI loaded (locator resolved, OPTIONS → 200) but no HTTP verbs dispatched (uniform 405, no `Allow` header).
- The source fix was already committed in `57bae5d` (empty `META-INF/beans.xml` marks the jar an application archive so Quarkus indexes it). Production failure was a propagation gap, not a code bug.
- Deploy: rebuilt the agency module, overwrote the stale `lib/lib/main` jar in place (preserving the maven-coordinate filename), confirmed exactly ONE jar carries `META-INF/passport-agency-changelog.xml` (a duplicate triggers a Liquibase `ChangeLogParseException` / restart loop), re-ran `kc.sh build`, restarted only `passport`.
- TOPOLOGY: the agency jar is a declared dependency of `quarkus/server` → packaged into `lib/lib/main`, NOT dropped into `providers/`.

### Verification (live, realm=master, authenticated admin token)
- `GET /agency/config` → 200 (was 405); `GET /agency/principals` → 200; `GET /agency/principals/count` → 200; `OPTIONS /agency/config` → 200.
- `PUT /agency/config` and `POST /agency/principals` → 500 only on empty body (null representation NPE); behave normally with real payloads. `DELETE /agency/principals` → 404 (no collection-level delete; deletion is by id). All verbs non-405 → dispatch confirmed.

## 2026-05-25

### Agency / LBAC
- Normalized Agency admin REST subresource paths to be relative under `/admin/realms/{realm}/agency` (superseded; the real fix was the beans.xml indexing marker, see 2026-06-16).
- Removed `Content-Type: application/json` from Agency config `GET` requests in the admin UI; kept bearer auth and JSON bodies for `PUT`.

## 2026-05-09

### Agency / LBAC
- Wired Agency admin console routes for delegate creation, passport minting, and Agency configuration; fixed dashboard navigation.
- Added backend compatibility endpoint `POST /admin/realms/{realm}/agency/delegates`.
- Added backend APIS v2.0 minting endpoint `POST /admin/realms/{realm}/agency/passports/mint`; extended Agent Passport representation with APIS minting fields.
- Replaced hardcoded Agency private-key encryption material with required `AGENCY_KEY_ENCRYPTION_SECRET`.
- Updated Docker Compose to pass Agency/APIS env vars and mount runtime secrets read-only.

### Documentation
- Added canonical root docs (`AGENTS.md`, `TRUTH.md`, `PROJECT_STATE.md`, `CHANGELOG.md`) — later migrated into `TRUTH/` (2026-06-16).

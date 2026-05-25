# Agency/LBAC 405 Handoff

## Purpose

This handoff exists to stop repeated nginx/cache/auth loops while debugging the live Agency/LBAC tab.

Current failure: the deployed admin UI loads the Agency page, then the config fetch fails:

```text
GET https://passport.aetherpro.us/admin/realms/master/agency/config
HTTP/1.1 405 Method Not Allowed
response body: {"error":"HTTP 405 Method Not Allowed"}
```

The browser request includes a bearer token. This is not the unauthenticated curl case.

## Observed Production State

- Public admin console: `https://passport.aetherpro.us/admin/master/console/`
- Browser route showing the failure: `#/master/agency`
- Agency dashboard JS chunk is loaded successfully.
- The failing request is XHR/fetch to:
  - method: `GET`
  - URL: `/admin/realms/master/agency/config`
  - headers include:
    - `Authorization: Bearer ...`
    - `Content-Type: application/json`
    - `Accept: */*`
- Response:
  - status: `405 Method Not Allowed`
  - content-type: `application/json`
  - body: `{"error":"HTTP 405 Method Not Allowed"}`
- Fresh Brave/incognito reproduced the same failure, so simple browser cache is not the explanation.

## Deployment State

The second deploy completed and smoke checks passed:

- `passport-server`: up
- `passport-postgres`: up/healthy
- `passport-redis-stack`: up
- Admin console `/admin/master/console/`: `200`
- Unauthenticated Agency config request: `401`, which proves the route is auth-gated and reachable for unauthenticated curl.
- `APIS_REALM_ISSUER_PRIVATE_KEY_PATH` and `AGENCY_KEY_ENCRYPTION_SECRET` are present inside the container.
- Live mounted dist contains:
  - `com.aetherpro.passport.passport-agency-999.0.0-SNAPSHOT.jar`
  - `org.passport.passport-admin-ui-999.0.0-SNAPSHOT.jar`
  - `org.passport.passport-rest-admin-ui-ext-999.0.0-SNAPSHOT.jar`

## Reverse Proxy Findings

Do not spend another loop assuming Traefik.

- Host ports `80` and `443` are owned by host nginx.
- Traefik container exists, but it is only published on `8081` and `8082`.
- `passport.aetherpro.us` is handled by nginx.
- Active nginx config for Passport:
  - `server_name passport.aetherpro.us`
  - `location / { proxy_pass http://127.0.0.1:8080; ... }`
- No nginx rules were found for limit/except, method filtering, `auth_request`, `/admin/realms`, `/agency`, or custom 405 handling.
- Direct backend and public HTTPS unauthenticated requests behaved the same:
  - `GET /admin/realms/master/agency/config`: `401 Unauthorized`
  - `OPTIONS /admin/realms/master/agency/config`: `200 OK`

Conclusion: nginx is very unlikely to be creating the 405. The important delta is authenticated browser request versus unauthenticated curl.

## Local Code Facts

Backend file:

```text
Passport-Pro/passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/admin/AgencyAdminResource.java
```

Relevant methods currently exist locally:

```java
@GET
@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
public Response getAgencyConfig()

@PUT
@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response updateAgencyConfig(AgencyConfigRepresentation rep)
```

Provider registration:

```text
AgencyAdminResourceProviderFactory.getId() == "agency"
AgencyAdminResourceProvider implements AdminRealmResourceProvider
```

Keycloak/Passport dispatch point:

```text
RealmAdminResource.extension(@PathParam("extension"))
session.getProvider(AdminRealmResourceProvider.class, extension)
```

So `/admin/realms/{realm}/agency/...` should dispatch to the Agency admin resource provider with provider id `agency`.

Frontend files using the config route:

```text
Passport-Pro/js/apps/admin-ui/src/agency/AgencyDashboard.tsx
Passport-Pro/js/apps/admin-ui/src/agency/AgencyConfig.tsx
```

The dashboard currently sends a `Content-Type: application/json` header even on `GET`.

## Important Negative Evidence

- This is not a missing deployed frontend chunk. The chunk is present and loaded.
- This is not a missing deployed Agency provider jar. The jar is present.
- This is not a simple unauthenticated request. DevTools shows an `Authorization: Bearer ...` header.
- This is not just a stale browser cache. Brave/incognito reproduced.
- This is not a literal `$realm` browser URL issue. The actual failing browser URL uses `master`.
- Do not use `http://127.0.0.1:8080/...` from Cory's workstation unless an SSH tunnel exists. `127.0.0.1` means the local machine, not the VM.

## Most Likely Causes To Check Next

1. Deployed bytecode/resource method mismatch:
   - Confirm the live `passport-agency` jar actually contains `getAgencyConfig` with `@GET` and path `config`.
   - Do not just check the source checkout. Inspect the live mounted jar or decompile/list annotations.

2. JAX-RS subresource path matching edge:
   - Agency methods use leading-slash paths like `@Path("/config")`.
   - Passport's built-in admin UI extension examples use relative subresource paths like `@Path("info")`.
   - Normalize Agency admin resource method paths from `@Path("/...")` to `@Path("...")`.

3. GET request header mismatch:
   - The admin UI sends `Content-Type: application/json` on `GET`.
   - Remove `Content-Type` from GET requests. Keep it only for requests with JSON bodies such as POST/PUT.

4. Authenticated permission path:
   - Unauthenticated curl reaches auth and returns `401`; authenticated browser returns `405`.
   - If the route exists after path/header cleanup, inspect whether `auth.realm().requireViewRealm()` or resource matching behaves differently for the admin token.

## Recommended Patch

Make the route boring and conventional:

1. In `AgencyAdminResource.java`, change method-level `@Path("/...")` annotations to relative paths:

```text
@Path("/config") -> @Path("config")
@Path("/principals") -> @Path("principals")
...
```

This should apply to all Agency admin methods because they are mounted below `/admin/realms/{realm}/agency`.

2. In `AgencyDashboard.tsx` and `AgencyConfig.tsx`, remove `Content-Type: application/json` from GET fetches.

3. Consider removing `Content-Type` from other Agency GET fetches:

```text
DelegateForm.tsx GET /principals
MintPassportForm.tsx GET /principals
PrincipalsList.tsx GET /principals
PrincipalDetail.tsx GET /principals/{id}
```

Keep `Content-Type` for POST/PUT requests.

## Verification Commands For Local Repo

From `/home/cory/Documents/Passport-IAM`:

```bash
cd Passport-Pro
./mvnw -pl passport-extensions/agency -DskipTests clean compile

cd js/apps/admin-ui
pnpm build
```

Do not start local dev servers until current ports/listeners are checked first:

```bash
ss -ltnp
docker ps
```

## VM Deploy Safety

Do not deploy straight from a partial patch.

Use the same production-safe process that already worked:

1. Build in an isolated worktree.
2. Verify the tarball contains Agency/admin UI jars.
3. Back up live dist and database before live deploy.
4. Stop only `passport`.
5. `sudo rsync -a --delete --exclude 'data/'` from staged dist into the live mount.
6. Recreate only `passport` with `Passport-Pro/.env` loaded.
7. Smoke test.

Do not touch Postgres or Redis.

## If 405 Persists After Patch

Stop guessing and collect these exact facts:

1. DevTools Network details for the failed request:
   - URL
   - method
   - status
   - request headers, excluding token value
   - response body
   - response headers, especially `Allow` if present

2. Live jar inspection:
   - exact jar path in mounted dist
   - whether `AgencyAdminResource.class` contains `getAgencyConfig`
   - whether runtime annotations show GET and relative `config`

3. Passport logs during one browser refresh:
   - filter for `Agency`, `405`, `Method Not Allowed`, `RESTEasy`, `Unauthorized`, `Forbidden`, `ERROR`, `WARN`

4. Direct authenticated curl from the browser token, if safe:
   - same token, same method, same URL
   - do not print or save the token

## Current Working Theory

The browser is calling the intended URL, and the deployed code bundle is present, but runtime method selection does not see a valid GET method for the subresource path. The cleanest fix is to align Agency admin resource paths with the rest of the Passport admin extension style and remove unnecessary `Content-Type` headers from GET requests.

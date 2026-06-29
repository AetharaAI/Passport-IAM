# Implementation Plan - APIS v2.0 Passport Minting & Agency UI Fixes

This plan outlines the steps to fix the broken Agency routes in the React frontend and implement the APIS v2.0 JWT minting logic in the Quarkus backend.

## Phase 1: Frontend Fixes (js/apps/admin-ui)

The goal is to eliminate "Page Not Found" errors for Agency routes and fix the Principal creation form.

### 1. Update Agency Routes
Modify `js/apps/admin-ui/src/agency/routes.tsx` to include:
- `/:realm/agency/delegates/new` -> `DelegateForm`
- `/:realm/agency/passports/mint` -> `MintPassportForm`
- `/:realm/agency/configure` -> `AgencyConfig`

### 2. Create Missing Components
Create the following minimal functional components in `js/apps/admin-ui/src/agency/`:
- **`DelegateForm.tsx`**:
    - Fields: `delegateName`, `principalId` (dropdown), `scope`, `expiryDays`.
    - POST to `/admin/realms/${realm}/agency/delegates`.
- **`MintPassportForm.tsx`**:
    - Fields: `agentName`, `principalId` (dropdown), `tier` (select: tpm/dns/software/dev), `publicKeyPem` (textarea), `mandate` (textarea JSON), `machinePassportId` (optional text).
    - POST to `/admin/realms/${realm}/agency/passports/mint`.
- **`AgencyConfig.tsx`**:
    - GET from `/admin/realms/${realm}/agency/config`.
    - Fields: `complianceMode`, `auditLevel`.
    - PUT to `/admin/realms/${realm}/agency/config`.

### 3. Fix `CreatePrincipal.tsx`
Verify the submit handler in `CreatePrincipal.tsx`. It currently POSTs to `/admin/realms/${realm}/agency/principals`. Confirm this matches the backend `AgencyAdminResource` endpoint and ensures the payload matches `PrincipalRepresentation`.

### 4. Build and Deploy
- Run `pnpm build` in `js/apps/admin-ui`.
- Execute `TEST-AGENCY.sh` to redeploy and verify routes.

## Phase 2: Backend Implementation (AgencyAdminResource.java)

Implement the `mintAgentPassport` endpoint according to the APIS v2.0 specification.

### 1. Validate Request
- Ensure `mandate` is provided (mandatesRequired=true).

### 2. Build JWT Payload
Construct the JWT payload with the following claims:
- `did`: `did:passport:{realmName}:{agentName}`
- `principal_id`: From request.
- `machine_passport_id`: From request (nullable).
- `tier`: From request.
- `mandate`: Mandate object.
- `public_key_fingerprint`: `sha256/` + base64url(SHA-256(publicKeyDER)).
- `iat`: Current Unix timestamp.
- `exp`: `iat + 7776000` (90 days).
- `revocation_nonce`: 0.
- `jti`: UUID.

### 3. Sign JWT
- Sign with **ES256** using the private key from `APIS_REALM_ISSUER_PRIVATE_KEY_PATH`.
- Use **SmallRye JWT**.

### 4. Cloudflare Integration (DNS Tier)
If `tier == "dns"`:
- Publish TXT record to Cloudflare using `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ZONE_ID`.
- Record: `_apis.{agentName}.{realmDomain}`
- Content: `v=APIS2; eid={jti}; pubkey={fingerprint}; tier=dns; exp={exp}`

### 5. Persistence
- Store passport details in the database (JPA).

### 6. Client Registration
- Add `collab-server` as a registered client for token introspection.

## Verification Plan

### Automated Tests
- **Backend Unit Test**:
    - Generate ECDSA P-256 keypair.
    - Call `mintAgentPassport` with `tier=software`.
    - Fetch JWKS from `/agency/jwks`.
    - Verify JWT signature using the public key from JWKS.

### Manual Verification
- Navigate to all four new/fixed routes in the Admin UI.
- Submit each form and verify successful responses/database updates.
- Check network tab for correct POST/PUT payloads.
- Verify Cloudflare TXT record creation (if tier=dns and credentials provided).

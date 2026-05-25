# PASSPORT-PRO: Three-Gap Closure & MCPFabric Integration
## Engineered Implementation Prompt for Claude Opus 4.6 — Antigravity IDE

> **CONTEXT**: You are working on Passport-Pro, a fork of Keycloak 26.2 created by AetherPro Technologies LLC. Passport-Pro extends Keycloak with an "Agency" domain model implementing Legal-Based Access Control (LBAC) for AI agents. The system is LIVE at passport.aetherpro.us. The codebase uses Keycloak's SPI (Service Provider Interface) pattern. All new code lives under `com.aetherpro.passport.agency.*`. The database is PostgreSQL. The admin UI uses React + PatternFly.
>
> **IP NOTICE**: Passport-Pro is Trademarked, Copyright, and Patent Pending by AetherPro Technologies LLC. All code produced under this prompt is work product of AetherPro Technologies LLC.
>
> **YOUR ROLE**: You are a senior backend engineer and cryptographic systems architect. You write production-grade Java 17+ code following Keycloak's existing patterns. You never stub, mock, or leave TODOs. Every file you produce compiles and integrates with the existing SPI architecture.

---

## EXISTING ARCHITECTURE (Do Not Modify — Extend Only)

### Domain Model Already Implemented
```
Principal    → The legal entity (Human, Org, or AI Agent) that holds rights
Delegate     → Authority binding between a user/agent and a principal  
Mandate      → Scoped, time-bound authorization attached to a delegate
AgentPassport → DID-based identity for AI agents (did:passport:<uuid>)
```

### Existing SPIs and Classes
```
AgencySpi / AgencyProviderFactory / AgencyProvider  — Core SPI
JpaAgencyProvider                                    — JPA persistence layer
AgencyClaimProtocolMapper                            — OIDC token mapper (WORKING)
AgencyAdminResource                                  — Admin REST API
AgencyRealmConfig                                    — Per-realm configuration

Entity Classes (JPA):
  PrincipalEntity   → agency_principals table
  DelegateEntity    → agency_delegates table  
  MandateEntity     → agency_mandates table
  AgentPassportEntity → agency_agent_passports table
```

### Current Token Output (Working)
```json
{
  "agency": {
    "principals": [
      { "id": "uuid", "name": "Cory", "type": "human", "delegate_id": "uuid", "delegate_type": "agent" }
    ],
    "mandates": [
      { "id": "uuid", "scope": "email.read", "principal_id": "uuid", "valid_until": 1739500800000 }
    ],
    "passports": [
      { "id": "did:passport:uuid", "principal_id": "uuid", "capabilities": ["reason","code"], "valid_until": 1739500800000 }
    ]
  }
}
```

---

## GAP 1: THREE-PARTY CRYPTOGRAPHIC SIGNATURE CHAIN

### Objective
Implement the full Issuer → Principal → Delegate signature chain so that every agent action can be cryptographically traced back through the mandate to the principal to the issuer. This is the core of the patent claim.

### What Must Be Built

#### 1.1 — Keypair Management Service
**File**: `com/aetherpro/passport/agency/crypto/AgencyKeyManager.java`

Create a service that manages Ed25519 keypairs for all three parties:

```
Issuer Keypair   — One per Passport realm. Stored in realm config. Signs Agent Passports and Principal registrations.
Principal Keypair — One per Principal entity. Generated at Principal creation. Signs Mandates.
Delegate Keypair  — One per AgentPassport. Generated at passport issuance. Signs actions/requests.
```

Requirements:
- Use Ed25519 (EdDSA) via `java.security.KeyPairGenerator` with `"Ed25519"` algorithm (Java 17+ native support, no BouncyCastle needed)
- Private keys stored encrypted in PostgreSQL using AES-256-GCM with a master key derived from the realm's keypair
- Public keys exposed via a JWKS-like endpoint: `GET /realms/{realm}/agency/jwks`
- Key rotation support: each key has a `kid` (key ID), `created_at`, `expires_at`, and `status` (ACTIVE, ROTATED, REVOKED)
- Add JPA entity `AgencyKeypairEntity` → `agency_keypairs` table with columns: `id`, `entity_type` (ISSUER/PRINCIPAL/DELEGATE), `entity_id`, `kid`, `public_key_bytes`, `encrypted_private_key_bytes`, `iv`, `algorithm`, `status`, `created_at`, `expires_at`

#### 1.2 — Signature Service  
**File**: `com/aetherpro/passport/agency/crypto/AgencySignatureService.java`

Create a service that produces and verifies signatures across the three-party chain:

```
signPassport(issuerPrivateKey, agentPassportData) → SignedPassport
  - Produces: { passport_data, issuer_signature, issuer_kid }
  
signMandate(principalPrivateKey, mandateData) → SignedMandate  
  - Produces: { mandate_data, principal_signature, principal_kid }
  
signAction(delegatePrivateKey, actionData) → SignedAction
  - Produces: { action_data, delegate_signature, delegate_kid, mandate_id, passport_id }

verifyChain(signedAction) → ChainVerificationResult
  - Resolves: action.delegate_kid → passport → passport.issuer_kid → issuer
  - Verifies all three signatures in the chain
  - Returns: { valid: boolean, issuer_verified: boolean, principal_verified: boolean, delegate_verified: boolean, chain: [...] }
```

Signature format: JWS Compact Serialization (RFC 7515) with `alg: EdDSA`
- The payload is the canonical JSON of the signed object
- Canonicalization: Sort keys alphabetically, no whitespace (RFC 8785 - JSON Canonicalization Scheme)

#### 1.3 — Signed Passport Issuance Flow
**Modify**: `JpaAgencyProvider.java` — `createAgentPassport()` method

When an Agent Passport is created:
1. Generate Ed25519 keypair for the delegate (agent)
2. Build passport payload: `{ did, principal_id, capabilities, issued_at, expires_at, delegate_public_key }`
3. Sign with issuer's private key → `issuer_signature`
4. Store: passport entity + keypair entity + signature in `agency_agent_passports.issuer_signature` column
5. Return the signed passport (payload + signature + issuer_kid)

#### 1.4 — Signed Mandate Issuance Flow
**Modify**: `JpaAgencyProvider.java` — `createMandate()` method

When a Mandate is created:
1. Build mandate payload: `{ id, scope, principal_id, delegate_id, valid_from, valid_until, constraints }`
2. Sign with principal's private key → `principal_signature`
3. Store: mandate entity + signature in `agency_mandates.principal_signature` column
4. Return the signed mandate

#### 1.5 — Action Signing Endpoint
**New File**: `com/aetherpro/passport/agency/protocol/AgencyActionEndpoint.java`

REST endpoint for agents to sign actions before executing them:
```
POST /realms/{realm}/agency/sign-action
Authorization: Bearer <passport-token>
Body: { "action": "email.read", "target": "inbox", "mandate_id": "uuid" }
Response: { "signed_action": "<JWS>", "chain": { "delegate_kid": "...", "mandate_kid": "...", "issuer_kid": "..." } }
```

#### 1.6 — Chain Verification Endpoint
**New File**: `com/aetherpro/passport/agency/protocol/AgencyVerifyEndpoint.java`

REST endpoint for any service to verify the full chain:
```
POST /realms/{realm}/agency/verify-chain
Body: { "signed_action": "<JWS>" }
Response: { "valid": true, "issuer": { "kid": "...", "verified": true }, "principal": { "kid": "...", "verified": true }, "delegate": { "kid": "...", "verified": true }, "mandate": { "scope": "email.read", "valid_until": "..." } }
```

#### 1.7 — Enhanced Token Claims
**Modify**: `AgencyClaimProtocolMapper.java`

Add signature metadata to the token so downstream services can verify without calling back to Passport:
```json
{
  "agency": {
    "issuer_kid": "realm-key-001",
    "principals": [...],
    "mandates": [
      { "id": "uuid", "scope": "email.read", "principal_signature": "<JWS>", "principal_kid": "principal-key-001", ... }
    ],
    "passports": [
      { "id": "did:passport:uuid", "issuer_signature": "<JWS>", "issuer_kid": "realm-key-001", "delegate_kid": "agent-key-001", ... }
    ]
  }
}
```

#### 1.8 — Database Migration
**File**: `META-INF/agency-changelog-002-crypto.xml` (Liquibase)

Add columns and tables:
```sql
-- New table for keypairs
CREATE TABLE agency_keypairs (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(20) NOT NULL,  -- ISSUER, PRINCIPAL, DELEGATE
  entity_id UUID NOT NULL,
  kid VARCHAR(64) NOT NULL UNIQUE,
  public_key_bytes BYTEA NOT NULL,
  encrypted_private_key_bytes BYTEA NOT NULL,
  iv BYTEA NOT NULL,
  algorithm VARCHAR(20) NOT NULL DEFAULT 'Ed25519',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMP,
  realm_id VARCHAR(36) NOT NULL REFERENCES realm(id)
);

-- Add signature columns to existing tables
ALTER TABLE agency_agent_passports ADD COLUMN issuer_signature TEXT;
ALTER TABLE agency_agent_passports ADD COLUMN issuer_kid VARCHAR(64);
ALTER TABLE agency_agent_passports ADD COLUMN delegate_kid VARCHAR(64);

ALTER TABLE agency_mandates ADD COLUMN principal_signature TEXT;
ALTER TABLE agency_mandates ADD COLUMN principal_kid VARCHAR(64);

-- Audit log for signed actions
CREATE TABLE agency_audit_log (
  id UUID PRIMARY KEY,
  signed_action TEXT NOT NULL,
  delegate_kid VARCHAR(64) NOT NULL,
  mandate_id UUID REFERENCES agency_mandates(id),
  passport_id UUID REFERENCES agency_agent_passports(id),
  action_type VARCHAR(128) NOT NULL,
  chain_valid BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  realm_id VARCHAR(36) NOT NULL REFERENCES realm(id)
);

CREATE INDEX idx_agency_keypairs_entity ON agency_keypairs(entity_type, entity_id);
CREATE INDEX idx_agency_keypairs_kid ON agency_keypairs(kid);
CREATE INDEX idx_agency_audit_delegate ON agency_audit_log(delegate_kid);
CREATE INDEX idx_agency_audit_mandate ON agency_audit_log(mandate_id);
```

---

## GAP 2: FEDERATION — ALLIANCE REGISTRY & CROSS-ISSUER TRUST

### Objective
Move from realm-local trust to a federated model where multiple Passport issuers can discover each other, share Agent Cards, propagate revocations, and enable cross-issuer agent communication.

### What Must Be Built

#### 2.1 — Agent Card Specification
**File**: `com/aetherpro/passport/agency/federation/AgentCard.java`

The Agent Card is the "DNS record" for agents — the public metadata shared across the federation:

```java
public class AgentCard {
    private String did;                    // did:passport:<uuid>
    private String issuerDid;              // did:passport-issuer:<realm-id>
    private String issuerEndpoint;         // https://passport.example.com/realms/agents
    private String principalId;            // UUID of the owning principal
    private String name;                   // Human-readable agent name
    private List<String> capabilities;     // ["reason", "code", "vision", ...]
    private String publicKey;              // Base64url-encoded Ed25519 public key
    private String issuerSignature;        // JWS of the card signed by issuer
    private String status;                 // ACTIVE, SUSPENDED, REVOKED
    private int trustLevel;                // 0-100, determined by issuer + federation consensus
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant lastSeen;              // Last heartbeat
    private Map<String, String> metadata;  // Extensible metadata
}
```

#### 2.2 — Issuer Discovery Protocol
**File**: `com/aetherpro/passport/agency/federation/IssuerDiscovery.java`

Each Passport issuer publishes a well-known discovery document:
```
GET /.well-known/passport-issuer
Response: {
  "issuer_did": "did:passport-issuer:<realm-id>",
  "issuer_name": "MCPFabric.space",
  "jwks_uri": "https://passport.example.com/realms/agents/agency/jwks",
  "agent_cards_uri": "https://passport.example.com/realms/agents/agency/cards",
  "revocation_uri": "https://passport.example.com/realms/agents/agency/revocations",
  "nats_cluster": "nats://federation.mcpfabric.space:4222",
  "alliance_version": "1.0",
  "accreditation_status": "CERTIFIED",
  "accreditation_expires": "2027-01-01T00:00:00Z"
}
```

#### 2.3 — Federated Trust Store
**File**: `com/aetherpro/passport/agency/federation/FederatedTrustStore.java`

A per-realm store of trusted issuers (analogous to a browser's CA trust store):

- Admin UI to add/remove trusted issuers
- Import issuer's JWKS for local signature verification
- Trust levels: SELF (own realm), ALLIANCE (accredited), CUSTOM (manually trusted), UNTRUSTED
- Periodic refresh of issuer JWKS (hourly) and accreditation status (daily)
- JPA entity `TrustedIssuerEntity` → `agency_trusted_issuers` table

#### 2.4 — NATS Federation Bridge
**File**: `com/aetherpro/passport/agency/federation/NatsFederationBridge.java`

A service that connects the local Passport instance to the NATS supercluster for federation events:

NATS Subjects:
```
passport.federation.cards.publish    — New/updated Agent Cards
passport.federation.cards.query      — Request Agent Cards matching criteria
passport.federation.revocations      — Revocation events (immediate propagation)
passport.federation.heartbeat        — Issuer liveness checks
passport.federation.accreditation    — Accreditation status changes
```

Requirements:
- Use `io.nats:jnats:2.20+` (NATS Java client)
- JetStream for durable delivery of revocations and card updates
- Core NATS pub/sub for heartbeats and queries
- All messages signed with the issuer's Ed25519 key
- Consume revocations from other issuers and update local trust store
- Publish own agent card changes to the federation

#### 2.5 — Cross-Issuer Passport Validation
**Modify**: Token validation flow

When a service receives a JWT from an agent whose issuer is NOT the local realm:
1. Extract `issuer_kid` from agency claims
2. Look up issuer in FederatedTrustStore
3. If trusted: fetch issuer's JWKS (cached), verify issuer_signature on passport
4. If valid: verify principal_signature on mandate, verify delegate_signature on action
5. If all valid: accept the request with the foreign agent's trust level
6. If untrusted or invalid: reject with 403 and reason

#### 2.6 — Database Migration for Federation
**File**: `META-INF/agency-changelog-003-federation.xml`

```sql
CREATE TABLE agency_trusted_issuers (
  id UUID PRIMARY KEY,
  issuer_did VARCHAR(256) NOT NULL UNIQUE,
  issuer_name VARCHAR(256),
  issuer_endpoint VARCHAR(512) NOT NULL,
  jwks_cache TEXT,
  jwks_cached_at TIMESTAMP,
  trust_level VARCHAR(20) NOT NULL DEFAULT 'UNTRUSTED',
  accreditation_status VARCHAR(20),
  accreditation_expires TIMESTAMP,
  nats_cluster VARCHAR(512),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  realm_id VARCHAR(36) NOT NULL REFERENCES realm(id)
);

CREATE TABLE agency_agent_cards (
  id UUID PRIMARY KEY,
  did VARCHAR(256) NOT NULL,
  issuer_did VARCHAR(256) NOT NULL,
  name VARCHAR(256),
  capabilities TEXT,  -- JSON array
  public_key VARCHAR(512),
  issuer_signature TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  trust_level INTEGER DEFAULT 0,
  issued_at TIMESTAMP,
  expires_at TIMESTAMP,
  last_seen TIMESTAMP,
  metadata TEXT,  -- JSON
  realm_id VARCHAR(36) NOT NULL REFERENCES realm(id),
  UNIQUE(did, realm_id)
);

CREATE TABLE agency_revocations (
  id UUID PRIMARY KEY,
  target_did VARCHAR(256) NOT NULL,
  target_type VARCHAR(20) NOT NULL,  -- PASSPORT, MANDATE, ISSUER
  issuer_did VARCHAR(256) NOT NULL,
  reason VARCHAR(512),
  revoked_at TIMESTAMP NOT NULL,
  propagated_at TIMESTAMP,
  realm_id VARCHAR(36) NOT NULL REFERENCES realm(id)
);

CREATE INDEX idx_agent_cards_did ON agency_agent_cards(did);
CREATE INDEX idx_agent_cards_issuer ON agency_agent_cards(issuer_did);
CREATE INDEX idx_revocations_target ON agency_revocations(target_did);
```

---

## GAP 3: AUTOMATED COMPLIANCE TEST SUITE

### Objective
Build a CLI tool that any prospective Passport issuer can run against their instance to verify compliance with the Alliance spec. This is the accreditation gatekeeper.

### What Must Be Built

#### 3.1 — Compliance Test CLI
**File**: `tools/compliance/passport-compliance-check.py`

A Python CLI (single file, no framework dependencies beyond `requests`, `pyjwt`, `cryptography`) that runs a full compliance check:

```bash
# Usage
python passport-compliance-check.py --issuer-url https://passport.example.com/realms/agents

# Output
╔══════════════════════════════════════════════════════╗
║  Passport Alliance Compliance Check v1.0             ║
║  Target: https://passport.example.com/realms/agents  ║
╚══════════════════════════════════════════════════════╝

[PASS] Discovery endpoint (.well-known/passport-issuer) ✓
[PASS] JWKS endpoint returns valid Ed25519 keys ✓
[PASS] Agent Card endpoint returns valid cards ✓
[PASS] Revocation endpoint accessible ✓
[PASS] Passport issuance includes issuer signature ✓
[PASS] Mandate issuance includes principal signature ✓
[PASS] Three-party chain verification works end-to-end ✓
[PASS] Token claims include agency object with required fields ✓
[PASS] DID format matches did:passport:<uuid> ✓
[PASS] Time-bound mandates enforced (expired mandate rejected) ✓
[PASS] Revocation propagation (revoked passport returns 403) ✓
[WARN] NATS federation bridge not detected (optional for Tier 1)
[PASS] TLS on all endpoints ✓
[PASS] Key rotation endpoint accessible ✓

Results: 13/14 PASSED, 0 FAILED, 1 WARNINGS
Status: TIER 1 COMPLIANT ✓
Note: NATS federation required for Tier 2 (Certified Issuer)
```

#### Test Categories:

**Tier 1 — Basic Issuer (Self-Certified)**
1. Discovery document at `/.well-known/passport-issuer` with required fields
2. JWKS endpoint returns at least one Ed25519 public key with valid `kid`
3. Agent Card endpoint (`/agency/cards`) returns valid JSON matching schema
4. Passport creation returns a signed passport with `issuer_signature` and `issuer_kid`
5. Mandate creation returns a signed mandate with `principal_signature` and `principal_kid`
6. Chain verification endpoint validates a three-party signed action
7. JWT tokens include `agency` claim with `principals`, `mandates`, `passports` arrays
8. DIDs follow `did:passport:<uuid>` format
9. Expired mandates are rejected (create mandate with past `valid_until`, attempt to use it)
10. Revoked passports are rejected (revoke a passport, attempt to authenticate with it)
11. All endpoints served over TLS
12. Key rotation endpoint exists and accepts rotation requests

**Tier 2 — Certified Issuer (Alliance Accredited)**
All Tier 1 tests PLUS:
13. NATS federation bridge operational (connects to federation NATS cluster)
14. Agent Card changes published to `passport.federation.cards.publish`
15. Revocations published to `passport.federation.revocations`
16. Cross-issuer passport validation works (present a token from a different issuer)
17. Issuer heartbeat published every 60 seconds
18. Revocation propagation latency < 30 seconds
19. JWKS refresh cycle operational (keys update within 1 hour of rotation)
20. Published issuance policy document accessible at `/agency/policy`

#### 3.2 — Compliance Report Generator
The CLI outputs a JSON report suitable for submission to the Alliance:

```json
{
  "issuer_did": "did:passport-issuer:...",
  "issuer_url": "https://...",
  "test_version": "1.0",
  "tested_at": "2026-02-13T...",
  "tier_1": { "passed": 12, "failed": 0, "total": 12 },
  "tier_2": { "passed": 7, "failed": 1, "total": 8 },
  "overall_status": "TIER_1_COMPLIANT",
  "details": [ { "test_id": "T1-01", "name": "Discovery endpoint", "status": "PASS", "duration_ms": 45 }, ... ]
}
```

---

## INTEGRATION: PASSPORT + MCPFABRIC

### Connecting the Identity Layer to the Communication Layer

When Passport issues an Agent Passport with a signed credential, that agent can register on MCPFabric by presenting its Passport token. MCPFabric validates the token, extracts the Agent Card, and makes the agent discoverable.

#### Integration Points (implement in the Fabric A2A Python codebase, not in Passport Java):

**In `fabric/server.py` — `/mcp/register_agent` endpoint:**
1. Require `Authorization: Bearer <passport-token>` header
2. Decode the JWT and extract the `agency` claims
3. Verify the JWT signature against the issuer's JWKS (fetch from `agency.issuer_kid`)
4. Extract the Agent Card from the passport claims
5. Store the agent registration with its verified identity in PostgreSQL
6. Publish the Agent Card to NATS `fabric.agents.registered`

**In `fabric/server.py` — `/mcp/call` endpoint:**
1. Both the caller and callee must present valid Passport tokens
2. Extract mandates from the caller's token
3. Verify the requested action falls within the caller's mandate scope
4. If the callee is from a different issuer, verify the cross-issuer trust chain
5. Route the call and log the signed action to the audit trail

**In `fabric/fabric_message_bus.py` — NATS migration:**
1. Replace Redis Streams with NATS JetStream for inter-agent messaging
2. Keep Redis for local caching and session state
3. NATS subjects: `fabric.agents.{agent_did}.inbox`, `fabric.agents.{agent_did}.outbox`
4. All messages carry the sender's signed action proof
5. NATS accounts map to Passport issuers for multi-tenancy

---

## IMPLEMENTATION ORDER

Execute in this exact sequence:

1. **agency-changelog-002-crypto.xml** — Database migration for keypairs and signatures
2. **AgencyKeypairEntity.java** — JPA entity for keypairs
3. **AgencyKeyManager.java** — Keypair generation, storage, rotation
4. **AgencySignatureService.java** — Sign and verify operations
5. **Modify JpaAgencyProvider** — Wire signing into passport and mandate creation
6. **AgencyActionEndpoint.java** — Action signing REST endpoint
7. **AgencyVerifyEndpoint.java** — Chain verification REST endpoint  
8. **Modify AgencyClaimProtocolMapper** — Add signature metadata to tokens
9. **agency-changelog-003-federation.xml** — Database migration for federation
10. **AgentCard.java** — Agent Card data model
11. **IssuerDiscovery.java** — Well-known discovery endpoint
12. **FederatedTrustStore.java** — Trusted issuer management
13. **NatsFederationBridge.java** — NATS supercluster integration
14. **Cross-issuer validation** — Foreign token verification flow
15. **passport-compliance-check.py** — Compliance test CLI
16. **MCPFabric integration** — Wire Passport validation into Fabric endpoints

---

## CODE STANDARDS

- Java 17+ features allowed (records, sealed classes, pattern matching)
- Follow existing Keycloak SPI patterns — look at how `org.keycloak.models` and `org.keycloak.protocol.oidc.mappers` are structured and mirror that
- All new REST endpoints go through `AgencyAdminResource` or new JAX-RS resource classes registered via SPI
- All new JPA entities follow the existing `agency_` table prefix convention
- Use SLF4J logging (`org.jboss.logging.Logger`) consistent with Keycloak
- All cryptographic operations use `java.security` (Ed25519 is native in Java 17+)
- No external crypto libraries (no BouncyCastle) — the JDK has everything needed
- Write Javadoc for all public methods
- Include null checks and validation on all inputs
- Return `Optional<T>` for lookups that may not find a result
- Throw `AgencyException` (custom, create if not exists) for business rule violations

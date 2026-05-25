Below is a **repo-ready set of files** you can drop straight into the structure you described. They are written in a clean, serious, standards-style voice suitable for GitHub, security reviewers, and outside partners.

You can hand these to Claude to paste into the repo exactly as shown.

---

# **1) `README.md` — Public introduction**

```md
# Passport Alliance

The **Passport Alliance** is an open standards initiative dedicated to trustworthy, interoperable identity for autonomous software agents.

Modern AI agents can act, transact, and control systems — yet there is no common way to know **who an agent is, who authorized it, what it is allowed to do, and how to revoke it** when something goes wrong. The Passport Alliance exists to fill this gap.

## Mission

The Passport Alliance defines, maintains, and evolves the **Agent Passport Issuance Standard (APIS)** — a technical and governance framework that enables:

- Cryptographically bound agent identities  
- Verifiable delegation of authority  
- Instant revocation  
- Cross-system interoperability  
- Auditable agent activity  
- Multiple independent issuers under a shared standard  

Our goal is not to create a single identity provider. It is to create a **shared language of trust** for autonomous systems.

## What this repository contains

This repository has three pillars:

```

SPEC/        # The canonical technical standard (APIS)
GOVERNANCE/  # How issuers are recognized, certified, and governed
REFERENCE/   # Reference implementations and examples
TESTS/       # Conformance tests for compliant issuers

```

### SPEC — The technical core

APIS defines what an **Agent Passport** is, what is required to issue one, how it is verified, and how it is revoked. It is intentionally independent of any model, framework, or runtime.

### GOVERNANCE — The social layer

The Passport Alliance governance model defines:

- Who can be an issuer  
- How issuers are certified  
- How trust is scoped  
- How disputes are resolved  
- How the public registry operates  

Code can be copied. **Trust cannot.**

### REFERENCE — Proof it works

A live reference implementation (“Passport-Pro”) demonstrates that APIS is practical, not theoretical.

### TESTS — Conformance

Automated tests define what it means to be APIS-compliant, so independent issuers can interoperate safely.

## Why this matters

Autonomous agents are becoming economic actors. They will:

- Access APIs  
- Move money  
- Change infrastructure  
- Control devices  
- Operate on behalf of people and organizations  

Without a shared identity layer, this future is unsafe. With APIS, it can be trustworthy.

## Getting involved

- Review the spec in `SPEC/APIS-v1.md`
- Read governance in `GOVERNANCE/PASSPORT-ALLIANCE-GOVERNANCE.md`
- Run the conformance tests in `TESTS/`
- Open issues or propose changes via pull request

The Passport Alliance welcomes security experts, IAM providers, enterprises, researchers, and open-source contributors.

**Identity for machines. Trust for everyone.**
```

---

# **2) `SPEC/APIS-v1.md` — Canonical technical standard**

```md
# APIS v1 — Agent Passport Issuance Standard

**Version:** 1.0  
**Status:** Canonical  
**Scope:** Defines a portable, verifiable, and revocable identity for autonomous agents.

---

## 1. Purpose

APIS v1 defines what it means for an autonomous software agent to possess a durable, cryptographically bound identity that can be trusted across systems, organizations, and issuers.

APIS is intentionally independent of any model, framework, or runtime.

---

## 2. Core Model

Identity is composed of three layers:

1. **Passport Layer — who the agent is** (APIS responsibility)  
2. **Memory Layer — continuity of self** (linked via a memory anchor)  
3. **Runtime Layer — what the agent does** (signed actions, mediated by policy)

**Rule:**  
- Passport **never stores memory.**  
- Passport **always links to memory via an anchor.**

---

## 3. Definitions

- **Agent:** A software actor capable of holding a keypair, proving possession, accepting mandates, signing actions, and being revoked.  
- **Issuer:** An entity authorized under Alliance governance to mint passports.  
- **Holder:** The agent controlling the private key bound to the passport.  
- **Principal:** The legally accountable party responsible for the agent.  
- **Delegate:** An entity authorized to act on behalf of a Principal.  
- **Mandate:** A scoped authorization attached to a Delegate.  
- **Realm:** A trust boundary within which passports operate.

---

## 4. What qualifies as an Agent

An agent qualifies for a passport **if and only if** it can:

1. Generate and hold a cryptographic keypair  
2. Prove possession of the private key  
3. Accept scoped mandates  
4. Sign actions or requests  
5. Be revoked  

Framework, model, or hosting environment are irrelevant.

---

## 5. Minimum requirements for issuance

### 5.1 Ownership and context (required)

The requester MUST provide:

- `realm_id` — trust boundary  
- `principal_id` — legally accountable owner  
- Optional: `owner_user_id` — human responsible party  

### 5.2 Cryptographic binding (required)

The agent MUST provide:

- `public_key` (JWK preferred)  
- `key_type` (Ed25519 recommended; P-256 acceptable)

No public key = no passport.

### 5.3 Proof of possession (mandatory in v1)

Issuance follows a challenge-response protocol:

1. Issuer generates a 256-bit random nonce  
2. Agent signs nonce with its private key  
3. Issuer verifies signature using the public key  

Failure invalidates issuance.

### 5.4 Agent declaration (required)

The requester must declare:

- `agent_type`:
  - `ai-assistant`
  - `autonomous-agent`
  - `human-proxy`
  - `service-agent`

- `capabilities` — declarative list (not enforced by issuer)

### 5.5 Provenance (strongly recommended)

- `software_id` (repo hash, image digest, or package ID)  
- `software_version`  
- Optional: `model_id`, `model_version`

---

## 6. Memory anchor (canonical rule)

Every passport MUST have a `memory_anchor_id`.

**Case A — Provided by requester**  
- Issuer stores it as an opaque string.

**Case B — Not provided**  
- Issuer computes:  
```

memory_anchor_id = SHA-256(public_key)

```

---

## 7. Passport contents (required schema)

A valid passport MUST include:

### Core identifiers
- `passport_id` (UUID)  
- `passport_did` — `did:passport:<uuid>`  
- `issuer_id`  
- `issued_at`  
- Optional: `expires_at`  
- `status` — `active | suspended | revoked`

### Cryptographic binding
- `public_key`  
- `key_fingerprint = SHA-256(public_key)`

### Authority context
- `principal_id`  
- Optional: `delegate_id`  
- Optional: embedded mandates (JWT claims)

### Memory and provenance
- `memory_anchor_id`  
- Optional: `software_id`, `software_version`

### Revocation control
- `revocation_nonce` (starts at 0)

---

## 8. Authentication

### Humans  
Use standard OIDC Authorization Code + PKCE.

### Agents  
Use OIDC Client Credentials **plus** proof-of-possession via signed challenge, DPoP, or mTLS.

---

## 9. Revocation

Each passport has:

```

revocation_nonce = 0

```

Each access token includes:

```

passport_revocation_nonce = current value

````

If revoked, issuer increments nonce; all existing tokens become invalid.

---

## 10. Sub-delegation

At mint time, the Principal selects one mode:

**Mode A — No sub-delegation (default)**  
Delegates cannot create sub-delegates.

**Mode B — Controlled sub-delegation**  
Requires:
- `subdelegation_max_depth`  
- `scope_reduction_required = true`

---

## 11. Rate limits

Rate limits are:
- Declared by the Principal in the Mandate  
- Enforced by runtime (gateway, host, or platform)  

Example shape:
```json
{
  "requests_per_minute": 120,
  "max_daily_actions": 1000,
  "budget_cap": "$500"
}
````

---

## 12. Verification

A verifier MUST be able to:

1. Discover issuer via `/.well-known/openid-configuration`
2. Fetch issuer JWKS
3. Validate JWT signature
4. Check `passport_revocation_nonce`
5. Query status:

```
GET /agency/passports/{passport_did}
```

---

## 13. Multi-issuer trust (v1)

Trust is realm-local:

Each realm maintains:

```
trusted_issuers = [{ issuer_id, jwks_uri, policy_url }]
```

A passport is valid only if:

* Signature verifies
* Issuer is trusted
* Passport status is active
* Mandates pass policy

---

## 14. Compliance checklist

An APIS v1-compliant issuer MUST implement:

* `public_key` on passport
* `key_fingerprint`
* `memory_anchor_id`
* `revocation_nonce`
* Signed nonce challenge at issuance
* Immutable audit log
* Public passport status endpoint
* JWKS publication

---

## 15. Threat model

APIS mitigates:

* Identity cloning
* Credential theft persistence
* Unchecked delegation chains
* Untraceable autonomous behavior
* Vendor lock-in

````

---

# **3) `SPEC/apis-v1.schema.json`**

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "APIS v1 Agent Passport",
  "type": "object",
  "required": [
    "passport_id",
    "passport_did",
    "issuer_id",
    "issued_at",
    "status",
    "public_key",
    "key_fingerprint",
    "principal_id",
    "memory_anchor_id",
    "revocation_nonce"
  ],
  "properties": {
    "passport_id": { "type": "string", "format": "uuid" },
    "passport_did": { "type": "string", "pattern": "^did:passport:[a-f0-9\\-]+$" },
    "issuer_id": { "type": "string" },
    "issued_at": { "type": "string", "format": "date-time" },
    "expires_at": { "type": ["string", "null"], "format": "date-time" },
    "status": {
      "type": "string",
      "enum": ["active", "suspended", "revoked"]
    },
    "public_key": {
      "type": "object",
      "properties": {
        "kty": { "type": "string" },
        "crv": { "type": "string" },
        "x": { "type": "string" },
        "y": { "type": ["string", "null"] }
      },
      "required": ["kty", "crv", "x"]
    },
    "key_fingerprint": { "type": "string" },
    "principal_id": { "type": "string" },
    "delegate_id": { "type": ["string", "null"] },
    "memory_anchor_id": { "type": "string" },
    "revocation_nonce": { "type": "integer", "minimum": 0 },
    "software_id": { "type": ["string", "null"] },
    "software_version": { "type": ["string", "null"] }
  }
}
````

---

# **4) First three conformance tests (`TESTS/conformance/`)**

### **a) `test-proof-of-possession.yaml`**

```yaml
name: proof_of_possession
description: Issuer must verify private key ownership before minting a passport.

steps:
  - action: request_passport_without_signed_nonce
    expected: reject

  - action: request_passport_with_invalid_signature
    expected: reject

  - action: request_passport_with_valid_signature
    expected: accept

success_criteria:
  - No passport may be issued without a valid signed nonce.
```

---

### **b) `test-revocation.yaml`**

```yaml
name: revocation_behavior
description: Tokens must become invalid when revocation nonce increments.

steps:
  - action: issue_passport
  - action: issue_access_token
  - action: revoke_passport
  - action: validate_existing_token
    expected: invalid

success_criteria:
  - Existing tokens must fail after revocation_nonce changes.
```

---

### **c) `test-jwks.yaml`**

```yaml
name: jwks_publication
description: Issuer must publish valid JWKS.

steps:
  - action: fetch_jwks
    endpoint: "/.well-known/jwks.json"
    expected:
      - valid_json
      - contains_public_key_matching_passport

success_criteria:
  - JWKS must be publicly accessible and cryptographically correct.
```

---

If you want, next I can:

* generate an **OpenAPI file** for all required endpoints, or
* write **reference Python examples** (`issue-passport.py` and `verify-passport.py`) for your `REFERENCE/` folder.


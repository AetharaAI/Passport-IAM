Below is a **clean, publishable APIS v1 specification** that synthesizes:

* Your intent and constraints
* Your earlier APIS draft
* Claude’s technical critiques and additions 
* Your new requirements about **issuer scope, registry, and governance**

This is written **as if it were already a public standard document** you could put on GitHub, share with security engineers, or circulate to serious institutions. It is opinionated where it must be, minimal where it can be, and designed to survive real scrutiny.

You can copy this directly into a repo as `APIS-v1.md`.

---

# **APIS v1 — Agent Passport Issuance Standard**

**Status:** Canonical v1.0
**Purpose:** Define what it means for an autonomous agent to have a durable, verifiable, and revocable identity that can be trusted across systems, issuers, and organizations.

---

## 1. Scope and Intent

APIS v1 defines:

1. What qualifies as an **Agent** for the purpose of identity.
2. What is **required to mint** an Agent Passport.
3. What an **Agent Passport contains**.
4. How **proof of possession** is verified.
5. How passports are **validated, revoked, and audited**.
6. How multiple **issuers** can exist without collapsing trust.
7. How a **registry and foundation model** can coordinate issuers.

APIS is intentionally **independent of any model, framework, or runtime.**
Identity is not tied to code, hosting, or AI model choice.

---

## 2. Core Philosophy

An Agent Passport is not a token, not a credential, and not a login.

It is a **cryptographically-bound, revocable, auditable identity** for a software actor that can hold delegated authority.

Identity is composed of three layers:

1. **Passport Layer — who the agent is** (APIS responsibility)
2. **Memory Layer — continuity of self** (linked via a memory anchor)
3. **Runtime Layer — what the agent does** (signed actions, mediated by policy)

Passport **never stores memory.**
Passport **always links to memory via an anchor.**

---

## 3. Definitions

* **Agent** — A software actor capable of holding a keypair, authenticating, accepting mandates, signing actions, and being revoked.
* **Issuer** — An entity authorized under APIS governance to mint passports.
* **Holder** — The agent that controls the private key bound to the passport.
* **Principal** — The legally accountable party responsible for the agent.
* **Delegate** — An entity authorized to act on behalf of a Principal.
* **Mandate** — A scoped authorization attached to a Delegate.
* **Realm** — A trust boundary within which passports operate.

---

## 4. What Qualifies as an Agent (Canonical Rule)

An agent qualifies for a passport **if and only if** it can:

1. Generate and hold a cryptographic keypair.
2. Prove possession of the private key.
3. Accept scoped mandates.
4. Sign actions or requests.
5. Be revoked.

It does **not matter**:

* What model it uses
* What framework it runs on
* Whether it is hosted or local
* Whether it is OpenClaw, Agent Zero, Goose, or proprietary

Passport identity is implementation-agnostic.

---

## 5. Minimum Requirements to Mint a Passport

### 5.1 Ownership & Context (Required)

The requester MUST provide:

* `realm_id` — trust boundary
* `principal_id` — legally accountable owner
* Optional: `owner_user_id` — human responsible party

### 5.2 Cryptographic Binding (Required)

The agent MUST present:

* `public_key` (JWK preferred)
* `key_type` (Ed25519 recommended, P-256 acceptable)

Without a public key, **no passport is issued.**

### 5.3 Proof of Possession (Mandatory in v1)

Issuance follows a challenge-response flow:

1. Issuer generates a random 256-bit nonce
2. Agent signs nonce with private key
3. Issuer verifies signature using public key

If verification fails, issuance fails.

This prevents cloned or stolen identities.

### 5.4 Agent Declaration (Required)

The requester must declare:

* `agent_type`

  * `ai-assistant`
  * `autonomous-agent`
  * `human-proxy`
  * `service-agent`

* `capabilities` — declarative list (not enforced by issuer)

### 5.5 Provenance (Strongly Recommended)

* `software_id` (repo hash, image digest, or package ID)
* `software_version`
* `model_id` (optional)
* `model_version` (optional)

---

## 6. Memory Anchor (Canonical Rule)

Every passport MUST have a `memory_anchor_id`.

Two valid cases:

### Case A — Provided by requester

* Issuer stores it as an opaque string.
* Passport does not interpret or validate it.

### Case B — Not provided

* Issuer computes:

```
memory_anchor_id = SHA-256(public_key)
```

and returns it to the agent.

This guarantees continuity of identity across redeployments.

---

## 7. What a Passport Contains (Canonical Schema)

Upon issuance, the passport MUST include:

### Core Identifiers

* `passport_id` (UUID)
* `passport_did` — `did:passport:<uuid>`
* `issuer_id`
* `issued_at`
* `expires_at` (optional)
* `status` — `active | suspended | revoked`

### Cryptographic Binding

* `public_key`
* `key_fingerprint = SHA-256(public_key)`

### Authority Context

* `principal_id`
* Optional: `delegate_id`
* Optional: embedded mandates (JWT claims)

### Memory & Provenance

* `memory_anchor_id`
* `software_id` (optional)
* `software_version` (optional)

### Revocation Control

* `revocation_nonce` (starts at 0)

---

## 8. Authentication Model

### Humans

Use standard OIDC Authorization Code + PKCE.

### Agents

Use OIDC Client Credentials **plus proof of possession** via signed challenge or DPoP/mTLS.

---

## 9. Revocation (Instant Invalidation)

Each passport has:

```
revocation_nonce = 0
```

Each access token includes:

```
passport_revocation_nonce = current value
```

If a passport is revoked:

* Issuer increments nonce
* All existing tokens immediately become invalid

No blacklist needed.

---

## 10. Sub-Delegation (Canonical Shape)

At mint time, the Principal selects one mode:

### Mode A — No Sub-Delegation (Default)

Delegates **cannot create sub-delegates.**

### Mode B — Controlled Sub-Delegation

If enabled, must specify:

* `subdelegation_max_depth` (e.g., 1 or 2)
* `scope_reduction_required = true`

Meaning sub-delegates can **only receive a strict subset of mandates.**

This prevents authority sprawl.

---

## 11. Rate Limits (Who Sets Them)

Rate limits are:

* **Declared by the Principal in the Mandate.**
* **Enforced by runtime (gateway, host, or platform).**

APIS v1 does not mandate where enforcement happens.

Example canonical shape:

```
rate_limit = {
  "requests_per_minute": 120,
  "max_daily_actions": 1000,
  "budget_cap": "$500"
}
```

---

## 12. Verifying a Passport (Anyone, Anywhere)

A verifier MUST be able to:

1. Discover issuer via `/.well-known/openid-configuration`
2. Fetch issuer’s JWKS
3. Validate JWT signature
4. Check `passport_revocation_nonce`
5. Query passport status via:

```
GET /agency/passports/{passport_did}
```

---

## 13. Multi-Issuer Model (Critical Section)

### Principle: Many issuers, one standard.

Trust is **realm-local in v1.**

Each realm maintains:

```
trusted_issuers = [
  { issuer_id, jwks_uri, policy_url }
]
```

A passport is valid in a realm only if:

* Signature verifies
* Issuer is in trusted list
* Passport status is active
* Mandates pass policy

There is **no global root authority in v1.**

---

## 14. Registry & Foundation (Your Governance Layer)

To avoid centralization while preventing abuse, APIS defines a two-tier model:

### Tier 1 — Issuers

Entities that can mint passports under APIS. Examples:

* Enterprises
* Government agencies
* Universities
* Large platforms
* Trusted open-source foundations

Issuers must publish:

* Issuance policy
* Revocation policy
* Key rotation policy

### Tier 2 — Passport Alliance (Recommended)

A neutral foundation that:

* Maintains a **public issuer registry**
* Defines best practices
* Audits compliance
* Curates security standards
* Does **not** issue passports itself

Think:

* Linux Foundation, but for agent identity
* Standards body, not a central authority

### Marketplace Role (AetherAgentForge)

Your marketplace can act as:

* A public registry for community agents
* A default trusted issuer for sandbox agents
* A gateway for hosted execution

But not the only issuer.

---

## 15. Anti-Fraud Rules (Non-Negotiable)

1. No passport without public key.
2. No passport without proof of possession.
3. No delegation without explicit principal consent.
4. No unlimited sub-delegation.
5. Every passport must be revocable.
6. Every issuance must be auditable.

---

## 16. Minimal Implementation Checklist

To be APIS v1 compliant, an issuer must implement:

* `public_key` on passport
* `key_fingerprint`
* `memory_anchor_id`
* `revocation_nonce`
* Signed nonce challenge at issuance
* Immutable `agency_audit_log`
* Public passport status endpoint
* JWKS publication

---

## 17. Threat Model (Why This Matters)

APIS defends against:

* Cloned agents impersonating others
* Stolen credentials becoming permanent access
* Unchecked delegation chains
* Untraceable autonomous behavior
* Centralized identity lock-in

---

## 18. Why This Is Not a Toy

APIS enables:

* Trusted autonomous finance
* Regulated AI operations
* Defense and classified automation
* Enterprise delegation at scale
* Persistent digital persons

This is the boring, necessary layer beneath all agent systems.

---

## 19. Roadmap to APIS v2 (Not in v1)

Future enhancements may include:

* Key rotation without re-minting
* Identity inheritance for model upgrades
* Capability attestation frameworks
* Cross-realm federation
* Global trust registry

---

## 20. Closing Statement

APIS v1 defines a **portable, interoperable, legally-accountable identity layer for autonomous agents.**

Code can be cloned.
**Trust cannot.**

---

If you want, I can next:

* Turn this into a GitHub-ready repo (`apis-spec/`)
* Add JSON schemas for all objects
* Add OpenAPI endpoint definitions
* Add sample issuance flows
* Add threat diagrams

Tell me the format you want:
**GitHub repo, PDF whitepaper, or website draft.**


Perfect. We’re staying in **identity platform mode**.

You’re asking for a **canonical, never-changes “Agent Passport Issuance Standard”** so:

* developers know what qualifies as an “agent” (requirements),
* verifiers can confirm a passport is real,
* other issuers can exist without fragmenting the ecosystem,
* copycats can’t fake legitimacy by slapping “passport” on a JWT.

That’s exactly the right obsession.

Below is the **canonical spec v1** you can publish as your “birth certificate process.” It’s designed to sit *on top of* existing standards (OIDC/JWT/DID) and your Agency model (Principal → Delegate → Mandate → Passport) , while staying compatible with your current SSO integration flow .

---

# Agent Passport Issuance Standard (APIS)

**Version:** 1.0
**Status:** Canonical / Stable
**Scope:** Defines what an “agent” is, what must exist to mint a Passport, what is contained in a Passport, and how third parties verify authenticity.

## 0) Core idea

An **Agent Passport** is a persistent identity for an autonomous software actor that can:

1. authenticate,
2. accept scoped authority,
3. sign actions,
4. be revoked,
5. be auditable.

The Passport is **not** “the model” and not “the code.”
It’s the identity + cryptographic binding + authority chain.

---

## 1) Definitions

**Agent**: A software actor that can initiate or execute actions using delegated authority.
**Issuer**: An entity authorized to mint passports under this standard.
**Holder**: The agent instance that controls the private key bound to the passport.
**Principal**: The legal entity that owns the agent identity. 
**Delegate**: The authority binding to act for a Principal. 
**Mandate**: A scoped authorization attached to a Delegate. 

---

## 2) Minimum requirements to mint a Passport

To request issuance, the requester MUST provide:

### 2.1 Ownership + realm context

* `realm_id` (tenant boundary)
* `principal_id` (who owns the agent)
* optional `owner_user_id` (human accountable owner)

### 2.2 Cryptographic binding (non-negotiable)

The agent MUST have a keypair and present the **public key** at issuance:

* `public_key` (JWK preferred)
* `key_type` (Ed25519 recommended; P-256 acceptable)

This closes the “cryptographic binding” gap you already identified as HIGH priority. 

### 2.3 Agent declaration (identity metadata)

* `agent_type` (ai-assistant | autonomous-agent | human-proxy | service-agent)
* `capabilities` (JSON array, declarative only)
* `rate_limits` (declared policy; enforcement can be runtime)

### 2.4 Provenance (soft requirement, but standardized now)

* `software_id` (e.g., repo hash, package name, or image digest)
* `software_version`
* `model_id` and `model_version` (optional but strongly recommended) — aligns with your “Agent Provenance” gap. 

### 2.5 Memory anchor (optional in v1, standardized field exists)

* `memory_anchor_id` (string)
* If unknown, issuer MAY mint it (recommended), but Passport does not store memory.

**Why this matters:** It lets “same code, different person” be real, without forcing your memory architecture on everyone.

---

## 3) Issuance output (what a Passport contains)

When minted, a Passport MUST contain these fields:

### 3.1 Core identifiers

* `passport_id` (internal UUID)
* `passport_did` formatted: `did:passport:<uuid>` 
* `issuer_id`
* `issued_at`
* `expires_at` (optional but recommended)
* `status` (active | suspended | revoked)

### 3.2 Cryptographic binding

* `public_key` (as received)
* `key_fingerprint` = `SHA-256(public_key)` (canonical tracking handle)

### 3.3 Ownership + authority context

* `principal_id`
* optional: `delegate_id` and `delegate_type` 
* optional: mandates embedded in token claims (not in the passport record)

### 3.4 Capability + policy hints

* `capabilities` (declarative)
* `rate_limits` (declarative)
* `constraints` (JSON; future-proof hook)

### 3.5 Provenance + memory hooks

* `software_id`, `software_version`
* optional: `model_id`, `model_version` 
* optional: `memory_anchor_id`

---

## 4) How authentication works (standards-aligned)

### 4.1 Human apps

Humans continue to use OIDC Authorization Code + PKCE. 

### 4.2 Agents (v1)

Agents use OIDC **Client Credentials** (works today). 
But **v1.1** upgrades this to require signed proof of key possession (DPoP or mTLS). Keep reading.

---

## 5) Verifiability: how anyone confirms “this passport is real”

This is where copycats die.

A verifier MUST be able to perform:

### 5.1 Issuer discovery

Issuer publishes:

* OIDC discovery (`/.well-known/openid-configuration`) 
* JWKS endpoint (public signing keys for tokens)

### 5.2 Token verification

Verifier validates:

* JWT signature (issuer’s JWKS)
* `iss`, `aud`, `exp`, `iat`
* `agency` claims if present (principals/mandates/passports) 

### 5.3 Passport status check

Verifier checks status via a standardized endpoint:

* `GET /agency/passports/{passport_id}` (or DID-resolve)
* must return: `status`, `revoked_at`, `expires_at`

Your Admin REST API list already defines Passport management endpoints. 

---

## 6) Multi-issuer ecosystem (so you’re not the only issuer)

You want multiple issuers, but one standard.

So we define:

### 6.1 Trust registry

A realm MAY define a list of trusted issuers:

* `trusted_issuers[] = { issuer_id, jwks_uri, policy }`

### 6.2 Issuer policy

Issuer MUST publish:

* issuance policy URL
* revocation policy
* key rotation policy

### 6.3 Cross-issuer verification rule

A passport is “valid” in a realm only if:

* token verifies against issuer JWKS
* issuer is trusted (registry)
* passport status is active
* mandate constraints pass

This keeps the ecosystem coherent.

---

## 7) Anti-fraud + anti-copycat controls

### 7.1 Cryptographic binding is mandatory

If a “passport” is not bound to a keypair, it’s not a passport. Period. 

### 7.2 Proof-of-possession (upgrade path)

**APIS v1.1** requires one of:

* DPoP (JWT-based proof-of-possession)
* mTLS client auth
* signed nonce challenge at token request

This prevents “stolen client secret == stolen identity.”

### 7.3 Immutable audit trail

Issuers MUST keep an append-only log of:

* issuance
* key updates
* mandate attach/detach
* revocation/suspension

You already flagged the missing audit log as HIGH. 

---

## 8) What “an agent” is (canonical answer to your marketplace users)

An agent qualifies for a Passport if it can:

1. hold a cryptographic keypair,
2. authenticate to an issuer,
3. accept scoped mandates,
4. sign tool actions or requests (v1.1),
5. be revoked.

It does **not** matter:

* what model it uses,
* what framework it’s built on,
* whether it runs locally or hosted,
* whether it’s OpenClaw, OpenCode, Goose, Agent Zero, or someone’s vibe-coded bot.

Passport identity is independent of implementation.

---

# What you implement next (minimum changes, maximum correctness)

These are the exact “do this now” deltas that make APIS v1 real:

1. **Add `publicKey` to AgentPassport** (your own HIGH gap). 
2. Add **`keyFingerprint`** (SHA-256 of public key).
3. Add **`software_id` + `software_version`** (provenance).
4. Add **`memory_anchor_id`** field (optional, but standardized).
5. Add **`agency_audit_log`** table (your HIGH gap). 
6. Add a public **passport status endpoint** for verifiers.

That’s it. That locks the “birth certificate” process and gives you an enforcement surface.

---

If you want the next deliverable, I’ll write **APIS v1 as a publishable markdown spec** with:

* exact JSON schemas,
* endpoint definitions,
* error codes,
* threat model notes,
* and a short “Implementer’s checklist” for third-party issuers.

That becomes your canonical document that doesn’t change.


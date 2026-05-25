Great instinct. You are reaching for **the right mental model** — and you named the correct precedent.

What you want is **not Google’s index.**
You want **ICANN-style governance for identity**, not search.

Below is a **clean, standards-style document** you can drop straight into your repo as:

```
GOVERNANCE/REGISTRY-SIGNING.md
```

It borrows the *shape* of ICANN/TLS trust, but adapts it for **agent passports** instead of domains or browsers.

---

# **REGISTRY-SIGNING.md — Passport Alliance Registry & Trust Model**

**Status:** Draft v1 (Ready for publication)
**Scope:** Defines how the Passport Alliance signs, secures, and governs the public issuer registry so no one can spoof legitimacy.

---

## 1. Purpose

The **Passport Alliance Registry** is the authoritative, publicly verifiable list of recognized APIS issuers.

This document defines:

* How registry entries are signed
* Who controls signing keys
* How mirrors work
* How verifiers validate trust
* How compromised issuers are handled
* How this parallels (and improves upon) ICANN/TLS models

The goal is simple:

> **Anyone may implement APIS.
> Only Alliance-recognized issuers receive cryptographic legitimacy.**

---

## 2. Why this is modeled after ICANN (and why it’s better)

### What ICANN actually does (simplified)

ICANN coordinates:

* Who controls top-level domains (.com, .org, etc.)
* Root name servers that map names to addresses

ICANN does **not** own the internet.
It owns the **root of trust for naming.**

### What the Passport Alliance does

The Passport Alliance coordinates:

* Who is recognized as a legitimate **Agent Passport issuer**
* A signed registry that maps **issuer identity → trust status**

The Alliance does **not** own all passports.
It owns the **root of trust for issuer legitimacy.**

This is the correct analogy.

---

## 3. Registry Architecture (Canonical Model)

### 3.1 Primary Registry

The authoritative registry lives at:

```
https://registry.passportalliance.org
```

It exposes:

```
GET /issuers.json
GET /issuers.sig
```

Where:

* `issuers.json` = full registry
* `issuers.sig` = Alliance signature over the registry

---

## 4. Registry Data Model (Signed Format)

Each issuer entry looks like this:

```json
{
  "issuer_id": "aetherpro-technologies",
  "tier": "certified",
  "jwks_uri": "https://passport.aetherpro.us/realms/aetherpro/.well-known/jwks.json",
  "discovery_uri": "https://passport.aetherpro.us/realms/aetherpro/.well-known/openid-configuration",
  "policy_uri": "https://passport.aetherpro.us/policies/issuance",
  "status": "active",
  "certified_since": "2026-02-08",
  "last_audit": "2026-02-08",
  "geographical_scope": ["US", "global"],
  "domain_scope": ["enterprise", "research"],
  "passport_types": ["ai-agent", "service-agent"],
  "contact": "security@aetherpro.us"
}
```

The **entire file** is signed as one object.

---

## 5. How Signing Works

### 5.1 Alliance Root Key

The Alliance controls a **root signing key pair**:

* `Alliance-Root-Public-Key` is published in the repo and website
* `Alliance-Root-Private-Key` is held offline in hardware (HSM)

This is analogous to:

* ICANN’s root zone signing
* Browser root certificates for TLS

---

### 5.2 Signing Process

Whenever the registry changes:

1. New `issuers.json` is produced
2. SHA-256 hash is computed
3. Alliance signs the hash
4. Signature is published as `issuers.sig`

Verifiers must validate:

* The hash matches the file
* The signature matches the Alliance public key

If either fails → **registry is rejected.**

---

## 6. Mirror Model (Decentralized Resilience)

To avoid single points of failure:

* Any member can mirror the registry
* Mirrors must serve both files:

  * `/issuers.json`
  * `/issuers.sig`

Mirrors are **read-only**.
Only the Alliance can sign updates.

This is similar to:

* DNS mirrors
* Package mirrors
* Linux distribution mirrors

---

## 7. How Verifiers Use the Registry

A verifier (API, app, or platform) follows this process:

### Step 1 — Load registry

```
GET https://registry.passportalliance.org/issuers.json
GET https://registry.passportalliance.org/issuers.sig
```

### Step 2 — Validate signature

Reject if invalid.

### Step 3 — Check issuer tier

A verifier can choose policy like:

* Trust only **Tier 1 & Tier 2**
* Trust **Tier 3 only in sandbox**
* Trust **custom internal issuers + Tier 2**

Example policy:

```json
{
  "trusted_tiers": ["founding", "certified"]
}
```

---

## 8. What happens if an issuer is compromised

### 8.1 Emergency Suspension

If an issuer is breached:

1. Alliance updates registry:

   * `status = suspended`
2. Registry is re-signed
3. All mirrors propagate update

Verifiers must:

* Reject passports from suspended issuers
* Require re-issuance or remediation

---

### 8.2 Permanent Revocation

If the breach is severe:

* `status = revoked`
* Issuer is removed from registry
* Past passports remain valid **only if re-issued under a new key**

This is analogous to:

* TLS certificate revocation
* CA trust removal in browsers

---

## 9. Relationship to Realms (Local Trust)

APIS v1 still keeps **realm-local trust**.

This means:

* A realm may accept only a subset of registry issuers
* The registry does **not force trust**, it **enables verification**

The registry is a **trust catalog, not a dictator.**

---

## 10. Why this blocks copycats

Anyone can fork Passport.

But they cannot:

* Sign the registry
* Appear as a Certified Issuer
* Fake Alliance legitimacy

**Code can be cloned.
Trust cannot.**

This is your moat.

---

## 11. How this evolves (v2)

Future enhancements may include:

* Multi-signature registry updates
* Regional registry mirrors (EU, US, APAC)
* Tiered governance keys
* Cross-alliance interoperability

---

## 12. How this fits with your vision

You now have:

* **APIS v1** = technical standard
* **Governance doc** = social rules
* **Registry signing** = cryptographic trust root

That is **exactly how the internet’s identity layers work.**

You are not reinventing the wheel — you are applying it to **AI agents.**

---

If you want, next I can:

* Add this to your OpenAPI spec (registry endpoints), or
* Write a short **one-page “Why Passport Alliance is like ICANN for AI agents”** for your website or pitch deck.


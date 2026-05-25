Good — this is exactly the right move. You now have **two pieces that must lock together**:

1. **APIS v1 (the technical standard)** — *what a Passport is and how it works.*
2. **PASSPORT-ALLIANCE-GOVERNANCE.md (the social layer)** — *who gets to issue, how trust is managed, and how the standard lives over time.* 

Below I’ll give you a **GitHub-ready repo structure**, show how the two documents fit together without contradicting each other, and call out the few places you should tighten language so this stands up to real security people.

---

## 1) The repo structure you should actually use

Create one repo — not many folders scattered around your machine.

Suggested name:

```
passport-alliance
```

Inside it:

```
passport-alliance/
├── README.md
├── GOVERNANCE/
│   └── PASSPORT-ALLIANCE-GOVERNANCE.md
│
├── SPEC/
│   ├── APIS-v1.md
│   ├── apis-v1.schema.json
│   ├── apis-v1.openapi.yaml
│   └── threat-model.md
│
├── REFERENCE/
│   ├── passport-pro/
│   │   └── (link to your running Keycloak fork + APIs)
│   └── examples/
│       ├── issue-passport.py
│       └── verify-passport.py
│
├── TESTS/
│   ├── conformance/
│   │   ├── test-proof-of-possession.yaml
│   │   ├── test-revocation.yaml
│   │   └── test-jwks.yaml
│   └── README.md
│
└── WEBSITE/
    └── (later — Manus can generate this)
```

You now have three clean pillars:

* **SPEC/** = immutable technical core
* **GOVERNANCE/** = living rules of the Alliance
* **REFERENCE/** = proof this isn’t vaporware

That’s how real standards projects are structured.

---

## 2) How APIS v1 and Governance fit together (no conflict)

Right now your two docs are actually very compatible. They just operate at different layers:

| Layer       | Document       | Job                                  |
| ----------- | -------------- | ------------------------------------ |
| Technical   | **APIS v1**    | Defines what a valid Passport *is*   |
| Social      | **Governance** | Defines who is allowed to issue them |
| Operational | Registry       | Defines who others should trust      |

That’s correct architecture.

**Key alignment points:**

* APIS says: *“Many issuers can exist.”*
* Governance says: *“Yes — but in tiers.”* 

That is not centralization. That is **certification**, which is exactly how TLS CAs, OAuth, and FIDO work in the real world.

---

## 3) One important clarification you should add to Governance

In **PASSPORT-ALLIANCE-GOVERNANCE.md**, add one explicit sentence to avoid legal confusion:

> “The Passport Alliance does **not** control who may technically implement APIS.
> It controls only **which issuers receive Alliance recognition in the public registry.**”

This matters because:

* You can’t (and shouldn’t) stop people from forking your code.
* You **can** decide who gets legitimacy.

That’s the moat.

---

## 4) How “scope of issuance” becomes real

You were right to focus on **scope**. Here’s how to encode it cleanly:

### Add to Governance — Issuer Scope Fields

Every issuer listed in the registry should publish:

```
{
  "issuer_id": "...",
  "tier": "certified",
  "geographical_scope": ["US", "EU", "global"],
  "domain_scope": ["enterprise", "defense", "research"],
  "passport_types": ["ai-agent", "service-agent", "human-proxy"]
}
```

This means:

* A university might issue **research agents only**.
* A defense contractor might issue **classified service agents only**.
* Your marketplace might issue **sandbox agents only**.

You don’t block issuers — you **scope them**.

---

## 5) Registry model: centralized mirror, decentralized trust

Your governance doc already sketches this well. Make it sharper like this:

**Registry architecture:**

* Primary registry: `registry.passportalliance.org`
* Signed entries with Alliance key
* Anyone can mirror it
* Verifiers choose:

  * “Trust only Tier 1 & Tier 2”
  * “Trust Tier 3 for sandbox only”
  * “Trust my own internal issuers + Alliance Tier 2”

This gives enterprises control while still creating a shared root of truth.

---

## 6) What you should ship next (tight, practical)

**Week 1 goals:**

1. Create the repo structure above.
2. Put:

   * Your clean APIS v1 in `SPEC/APIS-v1.md`
   * Claude’s governance doc in `GOVERNANCE/` (already done). 
3. Add a simple README that says:

> “Passport Alliance defines the Agent Passport Issuance Standard (APIS) and coordinates a trusted issuer ecosystem.”

4. Add a minimal conformance test:

   * “If you can’t prove key possession, you are not compliant.”

---

## 7) How this looks to serious people (why this matters)

When someone like a16z or a government security team reads this, they’ll see:

* A real **spec**
* A real **reference implementation**
* A real **governance model**
* A real **registry plan**

That’s not hobby territory — that’s infrastructure.

---

## 8) What I can do for you next

If you want, I’ll:

* Rewrite your APIS v1 into **exact GitHub markdown** for `SPEC/APIS-v1.md`,
* Create `apis-v1.schema.json`,
* And draft the first three conformance tests in `TESTS/`.

Or, if you prefer, I can generate the **README.md** that introduces Passport Alliance to the world in a clean, serious voice.

Tell me which you want first.


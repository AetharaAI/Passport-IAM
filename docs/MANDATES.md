# Mandates — Scoped Authority in Passport Agency

A **Mandate** is the missing middle object in the Agency authority model. It is a
scoped, revocable, time-boxed authority grant that sits between a Delegate and an
Agent Passport / runtime use.

```
Principal (grantor) → Delegate (grantee) → Mandate → Agent Passport / runtime use
```

- **Principal** — the owning/legal entity (e.g. AetherPro Technologies LLC, or a
  customer org). Accountable owner behind the authority.
- **Delegate** — a human or agent actor *eligible* to receive delegated authority.
  A delegate is **not** the permission; it is the eligible actor.
- **Mandate** — the scoped grant. *What* authority was delegated, under *what*
  scope, *until when*, and whether it is revocable.
- **Agent Passport** — the issued credential/proof for an agent, optionally backed
  by a mandate.

The Delegate answers "who may receive authority." The Mandate answers "what
authority, scoped how, until when." Keeping them separate is the whole point:
minting a delegate does not grant capability; a mandate does.

## Where it lives

Agency tab (admin console) → **Mandates** lane:

```
Agency
├── Principals
├── Delegates
├── Mandates        ← this
├── Agent Passports (Mint)
└── Configure
```

- **List** — `/:realm/agency/mandates`
- **Create** — `/:realm/agency/mandates/new`
- **Detail** — `/:realm/agency/mandates/:mandateId`

## The mandate object

A mandate is stored against its grantee **delegate** (preserving the existing
Agency storage pattern — it is *not* a second authority point) and signed by the
grantor **principal**'s key, exactly like the pre-existing delegate-scoped
mandate. The first-class fields add the grant metadata:

| Field | Meaning |
|---|---|
| `name` | Human-friendly mandate name |
| `kind` | One of the mandate kinds below |
| `grantorPrincipalId` | The granting principal (must own the grantee delegate) |
| `delegateId` / `delegateName` | The grantee delegate |
| `scope` (capability_scope) | Space/comma-separated capability strings |
| `modelScope` | JSON: which model classes/models this grant covers |
| `resourceScope` | JSON: nodes, repos, datasets, ... |
| `harnessScope` | JSON list: where this authority may execute |
| `metadata` | Free-form JSON |
| `revocable` | Whether the mandate may be revoked (default true) |
| `validUntil` | Expiry (`expiryDays` or an ISO `notAfter`); null = no expiry |
| `status` | Derived: `active` / `expired` / `suspended` / `revoked` |

### Mandate kinds

```
operator | support | integration | model_route | benchmark | collab | break_glass_reserved
```

> **`break_glass_reserved` is reserved only.** The enum value exists so the model
> is complete, but **no break-glass behaviour is implemented**. Creating a mandate
> of this kind stores a record and does nothing special.

### Harness scope must be explicit

`harnessScope` is a list of harnesses (runtime execution envelopes such as
`faraday`, `polymorph`, `openclaw`/`hermes`, `omniagent`). An empty/absent
`harnessScope` means the grant is **not** valid in any harness by default.

> **Rule:** An Agent Passport identifies the agent. A Mandate authorizes the
> agent. Harness scope limits *where* that authority can execute. **No Agent
> Passport implies authority across all harnesses.**

## Creating a mandate

1. Agency → **Mandates** → **Create mandate**.
2. Pick the **Grantor Principal**. The **Grantee Delegate** dropdown is scoped to
   that principal (create a delegate first if none exist).
3. Name it, pick a **Kind**, set capability scope and the JSON scope envelopes.
4. Set expiry (days) and whether it is revocable.
5. Save. Invalid JSON in any envelope is rejected before persistence.

API equivalent:

```bash
curl -sX POST "$B/mandates" -H "Authorization: Bearer $TOK" \
  -H "Content-Type: application/json" -d '{
    "name": "faraday-operator",
    "kind": "operator",
    "grantorPrincipalId": "<principalId>",
    "granteeDelegateId": "<delegateId>",
    "scope": "operator:cli agent:route model:inference collab:coordinate",
    "modelScope": "{\"allowed_classes\":[\"general\",\"stem-agentic\"],\"policy\":\"deny-overrides\"}",
    "resourceScope": "{\"nodes\":[\"anchor-0-lab\"]}",
    "harnessScope": "[\"faraday\"]",
    "expiryDays": 30,
    "revocable": true,
    "metadata": "{\"note\":\"first live COLLAB test\"}"
  }'
```

## Agent Passport minting references a mandate

The mint form (Agency → Mint Agent Passport) has an optional **Backing Mandate**
selector, scoped to the chosen principal. When set, the minted passport carries a
`mandate_ref` claim (`{id, name, kind}`) pointing at the persisted mandate. The
reference **does not widen authority** and never implies cross-harness authority —
it just records which mandate backs the passport. Leaving it as *None* keeps the
existing inline-mandate-JSON behaviour.

Proper flow:

```
Principal → Delegate → Mandate → Agent Passport
```

## What is implemented vs. stubbed

**Implemented**
- First-class Mandates lane (list / create / detail) in the Agency tab.
- Persistence via the existing `PASSPORT_MANDATE` table (extended, changeset
  `passport-agency-003-mandates`).
- Create / list / detail / suspend / revoke REST endpoints under
  `/:realm/agency/mandates`.
- JSON validation of `modelScope` / `resourceScope` / `harnessScope` / `metadata`
  on both client and server.
- Grantor-principal / grantee-delegate integrity check on create.
- Optional `mandate_ref` on Agent Passport mint.

**Stubbed / not wired**
- **RedWatch evidence gap.** Mandate create / suspend / revoke call an internal
  `emitRedWatchEvidence(...)` hook that currently only logs
  (`[redwatch-stub] ...`). The real RedWatch emit path is **not** wired in this
  deployment, so **do not claim mandate lifecycle events are persisted as
  evidence.** See the `TODO(redwatch)` in `AgencyAdminResource.java`.
- **PEP enforcement.** This change is the *authority-plane* representation of a
  mandate. Runtime enforcement of `scope` / `modelScope` / `harnessScope` lives in
  the existing PEP / gateway path and is out of scope here — no enforcement
  behaviour is claimed for mandates created through this lane beyond what the PEP
  already does.
- **break-glass** — enum reserved only; no behaviour.

## Non-goals (intentional)

- Mandates are **not** a second authority system. They belong to Passport Agency
  and are checked by the existing PEP path.
- Mandates are **not** collapsed into delegates.
- Agent Passport minting does **not** imply universal authority.
- No compliance/certification claims are made here.

# First Minted Agents — record & lessons

Running log of the **first real APIS Agent Passports** minted on the recovered
Passport plane (Azure). Purpose: capture what was minted, the exact chain, where
the keys live, and what we learned/changed — so the process is repeatable and the
early decisions are auditable.

> Realm: `syndicate` · Issuer: recovered APIS realm issuer key (ES256), verifies
> against passportalliance.org JWKS · Principal for all AetherPro-internal agents:
> **AetherPro Technologies LLC** (`49f2875a-391c-4096-aedb-3d84bb2a1907`).
> Chain for every mint: **Principal → Delegate → Mandate → Agent Passport**.

## Herman (2.5, DNS-anchored) — the original proof
- **DID:** `did:passport:syndicate:herman` · tier 2.5 (DNS-anchored).
- First conforming APIS v2.1 passport; anchored at `herman._apis.syndicateai.co`
  (registrar-managed zone, no Cloudflare — deliberate provider-neutral proof).
- **Lost from the DB** (minted after the 2026-06-24 backup; not in the restore).
  The issued passport still verifies against the published JWKS. Needs re-mint
  once Herman's runtime is rebuilt (blocked on GPU/models).

## Anchor-1 (TPM / Tier 1) — 2026-07-14
- **DID:** `did:passport:syndicate:anchor-1` · **tier=tpm**.
- Chain: AetherPro Technologies LLC → delegate `anchor-1` → mandate
  `anchor-1-operator` (`harness_scope=["anchor-1"]`, scope `operator:cli agent:route
  model:inference collab:coordinate`) → passport.
- **TPM key:** EC P-256 in the workstation fTPM, persisted at handle **0x81010010**.
  Private never leaves the chip. Bundle: `~/.anchor-1/apis/` (passport.jwt/json).
- Anchor-1 = the customer-facing Operator Agent (ex-Echo-1), re-homed to the local
  data plane, running on OpenRouter/hy3.

## PolyMorph (TPM / Tier 1) — 2026-07-14
- **DID:** `did:passport:syndicate:polymorph` · **tier=tpm**.
- Chain: AetherPro Technologies LLC → delegate `polymorph` → mandate
  `polymorph-operator` (`harness_scope=["polymorph"]`) → passport.
- **TPM key:** EC P-256, persisted at handle **0x81010011**. Bundle: `~/.polymorph/apis/`.
- PolyMorph = model-agnostic Agentic Harness (any provider). Second passport →
  unblocks the CollabFabric **2-passport A2A test**.

## Lessons / decisions from the first mints
- **Principal type enum = `ORGANIZATION`** (not "corporation"). `INDIVIDUAL`,
  `ORGANIZATION`, `SYSTEM`, `AI_AGENT`.
- **Delegate create needs realmId** — was a NOT-NULL bug (fixed, commit 29b5a1b).
- **TPM on the workstation:** no passwordless sudo; run tpm2 via `sg tss -c "..."`
  (cory is in the `tss` group). Persist the child key with `tpm2_evictcontrol` to a
  handle; export the public PEM with `tpm2_readpublic -f pem` — that PEM is what the
  mint takes. Private stays in the TPM; agent proves possession via `tpm2_sign`.
- **One handle per agent** (0x81010010 anchor-1, 0x81010011 polymorph) — keep a
  registry here as more are minted.
- **`mandate_ref`** is embedded in the passport JWT (id/name/kind) — reference only,
  does not widen authority.
- **Mint only real agents.** No throwaway/test principals — it becomes a mess.
- **Next:** each agent's runtime must present its passport + sign A2A challenges with
  its TPM key (the CollabFabric integration). And the **Principal Context Pack**
  (sibling repo `principal-context`) supplies the per-owner operational context that
  the passport/mandate authorize — see that repo's design.

## TPM handle registry
| Agent | Handle | Curve | Minted |
|---|---|---|---|
| anchor-1 | 0x81010010 | EC P-256 | 2026-07-14 |
| polymorph | 0x81010011 | EC P-256 | 2026-07-14 |

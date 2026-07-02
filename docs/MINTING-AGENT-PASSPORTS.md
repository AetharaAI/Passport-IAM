# Minting Agent Passports in the Passport Agency Tab

How to mint an APIS Agent Passport through the Passport admin UI (Agency / LBAC
tab). This is the same operation the Herman mint script performed — just done
through the console.

> **Where:** `https://passport.aetherpro.us/admin/master/console/` → switch to the
> **`syndicate`** realm (top-left realm selector) → **Agency** in the left nav.
> The issuer key that signs passports (`apis-realm-issuer-2026-05-09`) is bound to
> the `syndicate` realm, so mint there — that's what makes a passport verify
> against `passportalliance.org`'s published JWKS.

## The authority flow

The proper flow is:

```
Principal → Delegate → Mandate → Agent Passport
```

- **Principal** — accountable owner (e.g. AetherPro Technologies LLC).
- **Delegate** — the actor *eligible* to receive authority (e.g. `faraday`).
- **Mandate** — the scoped, revocable, time-boxed grant (capabilities, model
  scope, resource scope, **explicit** harness scope). Create these in the Agency
  → **Mandates** lane. See [MANDATES.md](MANDATES.md).
- **Agent Passport** — the issued credential, optionally **backed by a mandate**
  (the mint form's *Backing Mandate* selector adds a `mandate_ref` claim).

Historically the mint form embedded a mandate JSON blob inline (the "escape
hatch"). That still works, but the first-class path is: create the Delegate,
create a Mandate for it, then mint the passport **referencing that mandate**. A
passport never implies authority across all harnesses — harness scope lives on
the mandate and is always explicit.

## Prerequisites (once per realm / principal)

1. **Agency enabled** with **Agent Passports = Enabled** (Agency tab → Configure
   Agency). Already on for `syndicate`.
2. **A Principal exists** — the accountable owner behind the agent. For AetherPro's
   own agents this is **AetherPro Technologies LLC**. Create via **Create
   Principal** if missing (type ORGANIZATION, jurisdiction US).
3. **A Delegate + Mandate exist** (recommended) — create the delegate under the
   principal, then a Mandate granting its scope in the **Mandates** lane. You can
   then reference that mandate at mint time.
4. **The agent's public key.** Every agent has its **own** EC P-256 keypair. The
   **private key stays on the agent's machine**; only the **public** key goes into
   the passport.

## Step 1 — Generate the agent's keypair (on the agent's machine)

```bash
mkdir -p ~/.<agent>/apis            # e.g. ~/.faraday/apis, or the agent's data dir
openssl ecparam -name prime256v1 -genkey -noout -out ~/.<agent>/apis/agent-key.pem
chmod 600 ~/.<agent>/apis/agent-key.pem
openssl ec -in ~/.<agent>/apis/agent-key.pem -pubout          # <-- copy this PUBLIC key
```

Copy the `-----BEGIN PUBLIC KEY----- … -----END PUBLIC KEY-----` block.

## Step 2 — Mint in the UI

Agency tab → **Quick Actions → Mint Agent Passport**, then fill the form:

| Field | What to enter |
|---|---|
| **Agent Name** | short id, e.g. `faraday` (becomes `did:passport:syndicate:faraday`) |
| **Principal** | select the owner (e.g. AetherPro Technologies LLC) |
| **Backing Mandate** | *(recommended)* select the Mandate that grants this agent's scope. Adds a `mandate_ref` claim. Leave **None** to embed inline mandate JSON instead. |
| **Trust Tier** | see tiers below — for a normal VM/agent use **Tier 2.5 (DNS-Anchored)**; for a local operator tool use **Tier 3 (Software HSM)** |
| **Public Key PEM** | paste the PUBLIC key from Step 1 |
| **Mandate (JSON)** | the agent's authority — **must be non-empty** (mandates are required on `syndicate`). See the shape below. |
| **Machine Passport ID** | optional; leave blank for now |

Click **Mint**. On success the passport is signed by the realm issuer key and
persisted (so it's tracked and revocable).

### Trust tiers (dropdown)
- **Tier 1 (Hardware TPM)** — physical TPM with a cert chain. Rare on cloud VMs.
- **Tier 2.5 (DNS-Anchored)** — no TPM; identity anchored by a DNS TXT record you
  publish (see Step 4). This is the normal tier for OVH/VPS agents (e.g. Herman).
- **Tier 3 (Software HSM)** — key on disk, no anchor. Honest choice for a local
  operator tool.
- **Tier 4 (Development Key)** — throwaway/dev.

> Don't hand-pick a tier higher than what the box can actually prove. On an OVH
> KVM with no TPM, the honest tier is **2.5** (DNS-anchored) — see
> `attestation_resolver.py`.

### Mandate JSON shape (APIS v2.1)

```json
{
  "scope": ["<capability>", "..."],
  "model_scope": {
    "allowed_classes": ["general", "stem-agentic"],
    "allowed_models": ["grm-2.6-plus", "qwen3.6-27b", "qwen3.6-35b-a3b"],
    "denied_models": ["claude-fable-5", "claude-mythos-5"],
    "policy": "deny-overrides"
  }
}
```

`model_scope` is enforced at Aether Gateway (deny-overrides wins). Keep `scope`
to least privilege — no `docker:*`, `ssh:*`, `secrets:*`, or gateway-admin.

## Step 3 — Verify the mint

> ⚠️ **Known display bug:** the dashboard's "Agent Passports" tile currently
> always shows **0** (the backend never populates `passportCount`). The passport
> is still real. Verify with the API instead:

```bash
# on the flex VM (issuer host), on-box admin token:
B=localhost:8080/admin/realms/syndicate/agency
curl -s -H "Authorization: Bearer $TOK" "$B/principals/<principalId>/passports"
# -> lists the minted passports (agentName, passportDid, active)
```

The returned passport JWT verifies against
`https://passportalliance.org/.well-known/apis-issuer-jwks.json` (ES256).

## Step 4 — (Tier 2.5 only) publish the DNS anchor

Issuer-side (never on the agent node). In your DNS provider (Namecheap, etc.),
add a TXT record under a zone you control:

```text
Host:  <agent>._apis          (full: <agent>._apis.<yourzone>)
Value: v=APIS1; t=2.5; k=ec-p256; p=<the passport's public_key_fingerprint>
TTL:   Automatic
```

Then verify: `dig +short TXT <agent>._apis.<yourzone>` and confirm `p=` matches
the passport's `public_key_fingerprint`.

## Step 5 — Install the passport on the agent

Put the returned passport JSON where the agent reads it (its workspace / `~/.<agent>/apis/passport.json`),
owned by the agent, `chmod 600`. The agent presents this passport to COLLAB /
gateway as its identity.

---

## Known issues to fix (batch)
- **Dashboard passport count always 0** — `getAgencyConfig()` never calls
  `setPassportCount()`. One-line backend fix + redeploy.
- **Tier value mismatch** — the UI sends `dns`/`software`/`tpm`/`dev`; the Herman
  script used `2.5`. Normalize so UI-minted and script-minted tiers match the spec
  labels (`1`/`1.5`/`2.0`/`2.5`/`4`).

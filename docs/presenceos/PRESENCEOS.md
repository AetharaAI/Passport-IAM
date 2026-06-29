# PresenceOS — Canonical Composition Spec

> **Status:** Canonical target architecture. This document is the source of truth for
> *how PresenceOS is composed*, not a claim about current build state. Agents acting on
> this doc MUST reconcile it against actual repo state before implementing — inspect what
> exists, report what doesn't map, do not assume a clean slate.

---

## 1. What PresenceOS Is

PresenceOS is a collapsed, single-node **sovereign AI environment** — the operating
environment layer that wraps model serving, agent runtime, memory, identity, and audit
into one deployable unit the customer **owns** and runs on-premises.

Target customers: law firms, CPA offices, govcon-adjacent organizations — buyers for whom
data sovereignty, auditability, and provable security posture are the purchase, not a feature.

The product is delivered as an appliance (**Anchor** compute nodes). The customer's data and
inference run entirely on-box. Cloud is used only for the vendor's own build/staging pipeline
and optional peak capacity — never for the customer's sensitive workload.

---

## 2. Composition Vocabulary

The whole system is the **PresenceOS Composition Model**. Five terms, layered:

| Term | Definition |
|------|------------|
| **Service** | A single container (one row in `docker-compose.yml`). |
| **Profile** | A toggleable group of services (Compose `profiles:`). |
| **Edition** | A named set of profiles, expressed as an **Edition Manifest** (a `.env` file setting `COMPOSE_PROFILES`). |
| **Tier** | Hardware class — **Anchor 100 / 200 / 400** — which determines which inference profiles are viable on the box. |
| **Build** | A deployed unit = **Edition × Tier** (e.g., "Anchor 200, Government Edition"). |

Rule of composition: **Services → Profiles → Editions → land on Tiers → produce a Build.**

---

## 3. Service Catalog

### Control plane (CPU — always BASE)
| Service | Role |
|---------|------|
| `postgres` | Primary relational store (workspace RLS, IAM, COLLAB state). |
| `redis` | Cache / ephemeral coordination. |
| `nats` | JetStream message bus (COLLAB + MCP transport). |
| `qdrant` | Vector store (collection-per-workspace). |
| `passport` | Identity layer (Keycloak-based). Issues APIS passports. |
| `redwatch` | Append-only, hash-chained audit ledger. |
| `gateway` | **Aether Gateway** — LiteLLM-based, OpenAI-compatible router. Fronts the vLLM model servers. |
| `mcp-fabric` | Tool / document connectors (MCP). |
| `collab` | Agent coordination — MCP server over NATS JetStream + Postgres. |
| `sentinel` | **Default-deny egress.** Always on. (See §7.) |
| `polymorph` | Onboard UI / agentic harness surface. Kiosk-boots as the only customer-visible surface. |
| `kokoro-tts` | TTS **floor** — guaranteed fallback voice. Always on. (See §8.) |

### Inference & voice (profiled)
| Service | Profile | Role |
|---------|---------|------|
| `grm-inference` | `gpu` | GRM-2.6-Plus (Qwen3.6-27B finetune, ~19GB 4-bit AWQ) via vLLM. Primary STEM/agentic model. |
| `qwen-moe` | `heavy` | Qwen3.6-35B-A3B MoE via vLLM. |
| `voice-x` | `voice` | Voice/audio gateway (the l4-360 substrate). Fronts voice models. |
| `voxtral-asr` | `voice` | voxtral-4b-mini-realtime-asr (speech-to-text). |
| `voxstream2` | `voice` | Zero-shot voice-clone TTS (premium path; uses reference audio). |
| `scriber` | `voice` | ASR/voice routing. |
| `agent-fleet` | `fleet` | Multi-agent orchestration. *(Name pending rename — formerly "Echo Fleet".)* |

---

## 4. Profile Definitions

```
BASE                # no `profiles:` key — always starts
  postgres · redis · nats · qdrant
  passport · redwatch · gateway · mcp-fabric · collab
  sentinel · polymorph · kokoro-tts

profile: gpu        # primary LLM inference tier
  grm-inference

profile: voice      # the voice/audio substrate
  voice-x · voxtral-asr · voxstream2 · scriber
  # NOTE: kokoro-tts is NOT here — it lives in BASE as the always-on floor.

profile: heavy      # Anchor 400 class — larger/concurrent inference
  qwen-moe · (additional inference replicas)

profile: fleet      # multi-agent orchestration
  agent-fleet

profile: gov        # hardening OVERLAY (does not replace BASE security)
  airgap-policy · tpm-gate · (strict egress allowlist, extended audit retention)
```

**Footguns (Compose semantics):**
- No `profiles:` key = always on. That is the definition of BASE.
- A service may list multiple profiles; it starts if **any** active profile matches (OR).
- If a BASE service `depends_on` a profiled service, Compose auto-starts that profiled
  service to satisfy the dependency — even if its profile is off. Do not let BASE depend
  on something meant to stay optional.
- `docker compose up <service>` / `run <service>` starts a profiled service regardless of
  active profiles. Useful for debugging one model; surprising if forgotten.
- **Profiles do not isolate GPUs.** They decide which containers come up. GPU pinning is the
  `deploy.resources.reservations.devices` (`device_ids`) block / `CUDA_VISIBLE_DEVICES`.

---

## 5. Editions (Edition Manifests)

Each edition is a `.env` file. Deploy with `docker compose --env-file <file> up -d`.

```
# .env.commercial
COMPOSE_PROFILES=gpu,voice

# .env.government        (commercial + hardening overlay)
COMPOSE_PROFILES=gpu,voice,gov
```

Edition is **orthogonal to Tier**. A 200 box can ship Commercial or Government; the manifest
decides, the hardware does not.

---

## 6. Tiers (Anchor) and the Build Matrix

| Tier | Reference hardware | Default profiles | Slot |
|------|--------------------|------------------|------|
| **Anchor 100** | single GPU | `gpu,voice` | small office |
| **Anchor 200** | dual GPU ~64GB VRAM (e.g. 2×5090) | `gpu,voice` (headroom) | standard commercial |
| **Anchor 400** | multi-GPU (L40S-class+) | `gpu,voice,heavy,fleet` | large firm / heavy / gov |

Naming maps capacity the way an electrical panel does: Anchor 100/200/400 ≈ 100A/200A/400A
service. Internal shorthand: **Anchor 1 / Anchor 2 / Anchor 4**.

A **Build** is the intersection, e.g. `Anchor 200 × Government` →
`COMPOSE_PROFILES=gpu,voice,gov` on the dual-GPU reference box.

---

## 7. Security Invariants

- **Sentinel (default-deny egress) is BASE, never optional.** Security is the product, not a
  checkbox. Every Build ships with default-deny.
- The **`gov` profile is a hardening overlay** on top of BASE security — it adds air-gap
  enforcement, TPM attestation gating, stricter egress allowlists, and extended audit
  retention. It does not introduce security that was previously absent.
- **RedWatch logs everything material** — model loads/swaps, passport mints, egress decisions,
  agent actions — append-only and hash-chained. This is both the audit story and a govcon
  selling point.

---

## 8. Reliability Invariants

- **kokoro-82m is the TTS floor and lives in BASE.** It must always be loadable. Premium
  voice (`voxstream2`, zero-shot clone) rides the `voice` profile; the system falls back to
  kokoro if the clone model is unprovisioned or fails. Fancy voice is the upgrade — the floor
  cannot drop.
- **Weights never go in container images.** They load from a mounted volume (GPU block
  storage). Updating a model = swap files on the volume + restart the model server; the image
  stays small and stable. (See §10 for distribution.)

---

## 9. Identity & Attestation Tiers

Agents running on PresenceOS receive **APIS passports** (signed JWTs covering principal,
scoped mandate, and machine identity) from the `passport` service. The **passport tier the
agent receives is bounded by the node's available hardware root of trust:**

| Attestation available | Passport tier |
|-----------------------|---------------|
| TPM 2.0, hardware-attested | Tier 1 (highest) |
| Partial / virtualized | Tier 1.5–2 |
| No accessible TPM → DNS-anchored | Tier 2.5 |

This is by design. A box without an accessible TPM is **not** denied identity — it mints a
**lower, DNS-anchored tier**. Agents and operators must read the tier off the passport and not
assume Tier 1.

**Verifiable issuance chain** (Passport-IAM-Website / `root-site`, served from `.well-known`):
1. `/.well-known/alliance-root.jwk` — alliance root JWK (live at apex).
2. `/.well-known/apis-issuer-jwks.json` — issuer EC P-256 public key as a JWK.
3. `/.well-known/apis-issuer-delegation.json` — root-signed issuer delegation.

A verifier can fetch and pin the root, the issuer key, **and** the root→issuer delegation —
completing end-to-end verifiability without wiring the Keycloak JWKS endpoint. Use the static
`.well-known` route pattern (mirroring the existing `alliance-root.jwk` route); do not revive
the abandoned Keycloak `AgencyJwksEndpoint` path.

**Reference first agent:** Herman (Hermes-based, broad connections/exposure) is the first
agent minted under this chain. Expect a DNS-anchored tier given its host has no accessible TPM.

---

## 10. Repo & Registry Topology

PresenceOS is assembled from **separate service repos** — they are **not** merged.

- Each service repo stays where it is and gains **one** CI step: build its image, push to the
  registry (`ghcr.io/<org>/<service>:<tag>`).
- A **single thin deploy repo** (`anchor` / `presenceos-deploy`) holds *only* the
  `docker-compose.yml` + Edition Manifests (`.env.*`). It references published images by tag.
  No service code lives there.
- Deploy = pull images, `compose up`. The registry is the integration point that turns
  separate repos into one product.

**Registry:** GHCR (`ghcr.io`) for build/staging now — least friction given code is already on
GitHub, private images included, native Actions integration. Self-hosted **Harbor** + cosign
signing is the *later* sovereign/air-gap distribution path, paired with signed weight
artifacts (deferred until boxes are in the field).

---

## 11. How Agents Should Use This Doc

1. **Reconcile before acting.** Inspect actual repos/services; report anything that doesn't map
   to §3–§6 instead of assuming.
2. **Honor the invariants** in §7 and §8 — Sentinel in BASE, kokoro as the TTS floor, weights
   out of images — without prompting.
3. **Do not introduce** the deferred items (Harbor, signed-weight updater, public benchmark)
   unless explicitly tasked.
4. **Names marked "pending rename"** (e.g. `agent-fleet`) are placeholders; do not propagate
   the old name into new code.

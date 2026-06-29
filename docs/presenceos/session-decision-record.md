# Session Decision Record — Identity, Bus, Memory, Integration

**Date:** 2026-06-19
**Status:** Canonical decisions + rationale. Pointers to companion docs, not full specs.
**Companion artifacts:** `AEGIS-knowledge-layer-PARKED.md`, `APIS-threat-control-map.md`.
**Agent instructions:** Integrate decisions into TRUTH / AGENTS.md. Preserve the critical-path vs parked tags. Where a decision says "gated on inspecting current state," do not generate implementation code until the relevant repo/readme is reviewed.

---

## 1. Machine liveness — CRITICAL PATH (APIS 2.3)

**Decision:** Liveness is proven by hardware, not by token generation. An LLM's output is fungible (any copy of the weights produces identical tokens), so tokens cannot bind to an instance and cannot serve as a liveness or identity primitive. The only non-copyable anchor is the TPM/secure-element key and measured enclave state.

**Decision:** Attestation must be **challenge-bound**, not static. Verifier issues nonce → TPM produces a quote over (nonce + PCR/code state), signed by an AK chained to the EK → verifier checks freshness + expected state. A static "this box has a TPM" cert is the replayable static-photo equivalent and is rejected.

**Open design knob:** per-session vs per-message attestation. TPM quotes are too slow (tens of ms) for per-token. Resolution direction: per-session attestation establishes the channel; cheap symmetric MAC chain per message inside; re-attest on interval or on privilege escalation. Spec the quote-cost budget and re-attestation triggers when 2.3 is implemented.

## 2. Async bus trust model — CRITICAL PATH

**Decision:** Do NOT channel-bind persisted NATS JetStream messages. A persistent bus replays stored messages by design; point-to-point liveness fights that. Split the three properties:
- **Liveness/attestation** secures the *connection to the bus*, not the stored message.
- **Authority** travels *in* the message: signed Passport envelope → tamper-proof, non-repudiable, explicitly not "live."
- **Replay protection** is consumer-side: re-check Passport status/revocation at **consume time** + **idempotency keys** so a replayed message can't re-trigger an action.

## 3. Memory architecture — PARKED build, decisions stand

**Decision:** NATS/Redis/Postgres/Qdrant are not four databases; they are four tiers of one memory subsystem: JetStream = episodic (= audit substrate, shared artifact with RedWatch); Redis = working memory (ephemeral, TTL); Postgres = semantic/structured + system of record; Qdrant = associative recall.

**Decision:** The missing component is the **consolidation policy** (episodic → semantic → embedded; promote/decay/forget). This is the real "memory model" — a pipeline + policy, NOT a trained ML model. Do not build a memory ML model.

**Decision:** Reference knowledge ≠ memory. Static authoritative corpora are shared, versioned, read-only (one copy, many readers); memory is per-workspace, mutable, isolated. Do not put reference corpora in per-workspace collections. See AEGIS park doc.

## 4. COLLAB ↔ MCP Fabric boundary — CRITICAL PATH

**Decision:** Three layers, one responsibility each: **Fabric transports** (MCP connectors + A2A wire), **COLLAB coordinates** (roles, blackboard, claim_role, delegation; Postgres + JetStream + Redis), **APIS authorizes** (identity, mandate, attestation, revocation).

**Decision:** MCP and A2A are orthogonal, not interchangeable. COLLAB is MCP-fronted (agents invoke coordination as tools) over an A2A substrate (peer propagation). Not "literally an MCP server."

**Decision — the hooks:** (1) inbound route Fabric→COLLAB; (2) outbound send COLLAB→Fabric; (3) **trust hook = Policy Enforcement Point at COLLAB ingress.** Fabric must never make an authority decision (confused-deputy hole). Keep the trust decision in one place.

**Decision:** Agent Card must carry/reference the Passport. One identity source of truth — no Fabric peer registry independent of APIS, or split-brain identity results.

**Gated on current state:** exact hook signatures await review of `fabric-a2a` readme + COLLAB ingress. Demo-minimal hook set = inbound + outbound + ingress PEP; defer full role model + discovery reconciliation.

## 5. Generic retraction/revocation primitive — PROMOTED TO APIS-CORE BACKLOG

**Decision:** Passport revocation, bus consume-time recheck, and AEGIS fact retraction are the same problem. **System law: any trust that can be granted must be revocable, and revocation must propagate to everyone who relied on it.** Build ONE primitive — `grant → rely → revoke → propagate → invalidate-downstream` — serving all three. This is the top OPEN item in the threat map (#9) and is not AEGIS-parked; the identity core needs it regardless.

## 6. A2A / standards integration — POST-CONTRACT (parallel track)

**Decision:** Integrate, do not compete. A2A and MCP are complementary; APIS is the identity/authority/audit layer across both.

**Correction — claims discipline (public-facing):**
- Do NOT call A2A "insecure." Accurate framing: A2A manages identity at the HTTP transport layer (OAuth2/JWT/TLS), keeps identity out of the payload, and as of v1.0 (early 2026) added Signed Agent Cards. The defensible gap APIS fills: no agent-native scoped/attested/revocable mandate, no hardware root of trust, no tamper-evident audit in the spec.
- Target is the **Linux Foundation A2A project**, not Google (donated June 2025, vendor-neutral, 150+ orgs). Adoption mechanism is a named **extension** (AP2-for-payments is the precedent). APIS = identity/authority/audit extension.
- The field is contested: AIP paper + multiple IETF agent-identity/authorization drafts. Read AIP first. Position APIS's wedge as sovereign + hardware-attested (TPM) + auditable + air-gappable (govcon/CMMC), which the OAuth-cloud drafts don't own. Cite the field; do not claim to be first in standards. (Nov/Dec 2025 public release is a real priority/seriousness asset — use as "working this before v1.0," not "first, therefore lead.")

**Near-term action (the only one on critical path):** make `fabric-a2a` / mcpfabric A2A-v1.0 wire-compatible — signed Agent Cards, OAuth scheme declaration — so interoperability is real now and the extension proposal is credible later.

## 7. Security posture — discipline

**Decision:** Building Sentinel + RedWatch is an *assertion* of security, not a *demonstration*. Attack resistance is earned by threat-model + adversarial validation, not by component presence. Apply the "no CMMC without an assessment" rule to internal security claims too. See `APIS-threat-control-map.md`; no control is "Closed" without a validation reference.

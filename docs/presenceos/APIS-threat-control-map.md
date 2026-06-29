# APIS — Threat → Control Map

**Status:** Working draft. Provisional status values — NOT a security attestation.
**Date:** 2026-06-19
**Purpose:** Map the published agent-protocol attack taxonomy to APIS / Sentinel / RedWatch / Passport controls, with honest status. This is the credibility + integration + security artifact: it speaks the field's threat language (integrate-not-compete), shows which control closes each threat (defensible security), and cites the prior work (current). It is also the input to adversarial validation.

**Agent instructions:** Integrate the table and §Open/Partial into canonical docs. Keep the status discipline in §Status semantics intact — do not let any cell be reported as "Closed" without a validation reference. Correct provisional statuses against actual implementation state; the author has not confirmed build state for every row.

---

## Methodology / sourcing

Attack taxonomy adopted (not invented) from published agent-protocol security work: the comparative MCP/A2A/Agora/ANP threat-modeling literature, A2A v1.0 risk guidance (signed Agent Cards, card poisoning), the Agent Identity Protocol (AIP) paper, and the in-flight IETF agent-identity/authorization drafts. Speaking their taxonomy is deliberate: it is the integration story and the credibility signal in one move.

## Status semantics (read before using)

- **Closed-by-design (CbD)** — a control exists in the architecture that *should* close the threat. **This is not "Closed."** It has not been adversarially validated. No row graduates to Closed without a red-team/test reference in §Validation backlog.
- **Partial** — control partially addresses the threat, or the design is decided but not implemented.
- **Open** — no control yet, or a known gap. **These rows are the most valuable in the document.**

---

## Map

| # | Attack class | What it does | Primary control | Status |
|---|---|---|---|---|
| 1 | Agent impersonation | Attacker presents as a legitimate agent | Passport: realm-scoped DID + signed JWT; hardware-anchored identity (TPM tier) | CbD |
| 2 | Agent Card forgery | Fake card stood up to redirect peers | A2A v1.0 signed Agent Card + Passport reference in card | Partial |
| 3 | Agent Card tampering (in transit) | Modify a legitimate card | Signed card + TLS; card signature verification at ingress | CbD |
| 4 | Agent Card context poisoning | Malicious metadata in card compromises downstream consumer | Input validation at COLLAB ingress; card-field allowlist | Open |
| 5 | Replay (credential/message) | Capture and re-send a valid artifact | Bus: consume-time recheck + idempotency keys; channel: nonce-bound attestation | Partial |
| 6 | Confused deputy | Transport (Fabric) tricked into acting on authority it can't evaluate | PEP at COLLAB ingress; Fabric never makes authority decisions | Partial |
| 7 | Credential theft / token exfiltration | Steal the Passport/JWT | Short TTL; hardware-bound key (non-exportable); Sentinel egress control | Partial |
| 8 | Privilege escalation / mandate-scope violation | Agent acts beyond scoped mandate | Passport scoped mandate enforced at every privileged op (claim_role gate) | CbD |
| 9 | Stale-authority / revocation evasion | Use a revoked credential before revocation propagates | **Generic retraction/revocation propagation primitive** | **Open** |
| 10 | Audit tampering / log forgery | Alter the audit trail to hide actions | RedWatch append-only, hash-chained ledger; per-workspace chain | CbD |
| 11 | Egress / data exfiltration | Compromised agent leaks data or phones home | Sentinel default-deny egress | CbD |
| 12 | Issuance/enrollment poisoning | Subvert the mint ceremony so bad identity is born trusted | Air-gapped issuance (2.2); issuance-time proof requirements | Partial |
| 13 | Root-key compromise | Compromise the signing root | Air-gapped root key (2.2) | Partial |
| 14 | MITM on agent↔agent channel | Intercept/alter peer comms | mTLS + per-session challenge-bound attestation + channel binding | Partial |
| 15 | Liveness spoof / replay-as-presence | Replay a captured proof to fake "present and live" | Challenge-bound TPM quote (2.3), not static cert | Partial |

---

## Open / Partial — the rows that matter

**#9 Revocation evasion — OPEN, highest priority.** No propagation primitive exists yet. A revoked Passport is only as good as the speed and reach of revocation. This is the generic `grant → rely → revoke → propagate → invalidate-downstream` primitive promoted to the APIS-core backlog — it also serves bus-message recheck (#5) and AEGIS fact retraction. Closing #9 partially closes #5. Build once, generically.

**#4 Card context poisoning — OPEN.** A2A signed cards prove *origin*, not *content safety*. A correctly-signed card can still carry hostile metadata. Needs ingress validation + field allowlisting on anything consumed from a card. Not addressed by signing.

**#6 Confused deputy — PARTIAL.** The fix is architectural (PEP at COLLAB ingress; Fabric transports but never authorizes) but the hooks aren't integrated yet. Until wired, the boundary is the exposure. This is on the critical path with the Fabric/COLLAB hook work.

**#2 Card forgery / #5 replay / #14 MITM / #15 liveness — PARTIAL.** All gated on the attestation + channel-binding design (2.3) being implemented, and on Agent-Card↔Passport binding being wired so discovery identity and APIS identity are one fact (no split-brain registry). Designed, not shipped.

**#12 issuance / #13 root key — PARTIAL.** Gated on 2.2 (air-gapped root + air-gapped issuance). Issuance is the load-bearing weak link: everything downstream inherits the assurance of the mint ceremony, so this graduating from Partial matters more than most.

**#7 credential theft — PARTIAL.** Hardware-bound non-exportable keys are the real mitigation; TTL and egress control are compensating. Strong only where keys are truly hardware-sealed, not where a JWT sits in a process.

---

## Validation backlog (what turns CbD → Closed)

No "Closed-by-design" row is "Closed" until validated. Minimum to graduate each:

- **#10 RedWatch:** demonstrate tamper-evidence — attempt mid-chain edit, show detection; confirm offsite/independent anchoring so a host-level compromise can't rewrite history silently.
- **#11 Sentinel:** prove default-deny is complete — attempt exfil over DNS, ICMP, allowed-domain piggybacking; confirm no egress path bypasses policy.
- **#1 / #8 Passport:** attempt forged-DID and out-of-scope-mandate operations; confirm rejection at the enforcement point, not just at issuance.
- **#3 card tampering:** attempt in-transit card modification; confirm signature verification rejects.

A threat model with no Open rows is the tell that the modeling wasn't real. This one has Open rows on purpose.

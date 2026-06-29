# APIS v2.0 → v2.1 Changelog

APIS v2.1 is an **additive** update to APIS v2.0. It supersedes nothing; v2.0
remains the canonical historical release. Full detail in `APIS-v2.1-DELTA.md`.

## Added
- **Tier 2.0 (confidential-compute attestation).** New tier between 1.5 and 2.5
  for nodes that can produce an AMD SEV-SNP report or Intel TDX quote but have no
  TPM. The VM measurement is hardware-signed even though the key is on disk.
- **Algorithmic attestation-tier resolution.** Tier is detected, never hand-set:
  a node mints at the highest tier it can actually attest. Reference
  implementation: `attestation_resolver.py`. Rule: *device presence is not
  attestability.*
- **Issuer-written DNS anchor (normative).** The DNS TXT anchor is written by the
  issuer where the realm signing key lives; agent nodes never hold zone-write
  tokens.
- **Provider-neutral namespace proof (ACME/certbot model).** APIS requires proof
  of namespace control, **not** Cloudflare or any specific DNS provider. Methods:
  DNS-TXT, HTTPS `.well-known`, provider API automation, or manual. Providers are
  adapters, not protocol. Identity proof is separated from optional node binding.
- **`evidence_kind`** on the anchor: `dns-txt`, `https-well-known`, `dns-pending`.
- **`model_scope` mandate.** Model access becomes a mandate-scope and revocation
  concern (allow/deny classes and models, `deny-overrides`), enforced at the
  gateway as a policy-enforcement point.
- **Reference proof: Herman** (`did:passport:syndicate:herman`) — first conforming
  mint, chain-validated from published artifacts, DNS-anchored on a
  registrar-managed zone with no Cloudflare.

## Corrected
- **CMMC claim softened.** v2.0's "enables CMMC Level 2 compliance" → "designed to
  support NIST SP 800-171 / CMMC Level 2 control objectives; compliance is
  established by formal assessment, not by adopting this standard."

## Unchanged
- The `.well-known` issuance chain (`alliance-root.jwk`, `apis-issuer-jwks.json`,
  `apis-issuer-delegation.json`) and the v2.0 credential-chain model.

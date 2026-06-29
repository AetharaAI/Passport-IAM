# APIS v2.1 — Release Package

Publish-ready package for **APIS v2.1**, to be uploaded as a **new version of the
existing APIS v2.0 Zenodo record** (not a separate record). v2.1 is an additive
delta; v2.0 stays as the prior version and legitimacy trail.

## Contents

| File | Purpose |
|---|---|
| `APIS-v2.1-DELTA.md` | The normative spec delta (the v2.1 changes themselves) |
| `HERMAN-PROOF.md` | Reference proof — first conforming mint, reproducible |
| `attestation_resolver.py` | Reference implementation of algorithmic tier resolution |
| `CHANGELOG-v2.0-to-v2.1.md` | What changed, additive |
| `CITATION.cff` | Citation metadata |
| `ZENODO-DESCRIPTION.txt` | Paste-in description for the Zenodo version |
| `*.pdf` | PDF renders of the delta and the proof |

> Carry the **v2.0 PDF forward** into the new version too (it remains part of the
> v2.1 record). It lives at `ANCHOR/APIS_FOUNDATION/APIS_v2_Specification.docx`.

## Zenodo steps (operator)

1. Open the existing APIS v2.0 Zenodo record.
2. Click **New version**.
3. Carry forward v2.0 metadata/files; add the files in this package.
4. Set version to **2.1**.
5. Paste `ZENODO-DESCRIPTION.txt` into the description; state it is additive, not
   a replacement.
6. Include the Herman proof summary (from `HERMAN-PROOF.md`).
7. **Publish.** Do NOT delete or overwrite v2.0.
8. After publish, update PassportAlliance.org / docs:
   - "current spec" → latest concept DOI
   - link the version-specific v2.1 DOI
   - keep v2.0 visible as the prior version
   - add a "Herman reference proof" section

## Canonical boundary language (use everywhere)

> **Passport Alliance issues and governs APIS identities. Anchor nodes verify and
> enforce them.** Not every Anchor node is an issuer — standard nodes consume
> passports, verify issuer chains, enforce mandates, and log evidence. Issuance
> remains controlled by AetherPro or formally delegated issuers.

## Live artifacts referenced

- Root JWK: https://passportalliance.org/.well-known/alliance-root.jwk
- Issuer JWKS: https://passportalliance.org/.well-known/apis-issuer-jwks.json
- Issuer delegation: https://passportalliance.org/.well-known/apis-issuer-delegation.json
- Signed payload: https://passportalliance.org/.well-known/apis-issuer-delegation-payload.json
- Herman anchor (DNS TXT): `herman._apis.syndicateai.co`
- Concept DOI (all versions): https://doi.org/10.5281/zenodo.18820876

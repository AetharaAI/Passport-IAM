"""
attestation_resolver.py — APIS v2.1 tier resolution (TPM-first, descend the stack).

Resolves the HIGHEST attestation tier a node can actually PROVE, auto-detected.
Never returns a tier higher than the evidence the box can produce.

Tier order (high -> low):
    1    physical TPM 2.0 with a manufacturer cert chain
    1.5  attestable vTPM (cert chain present)
    2.0  confidential compute: AMD SEV-SNP report OR Intel TDX quote
    2.5  DNS anchor reachable for the realm (DKIM-pattern key publication)
    4    nothing -> software-only, warn loudly

Design rule (from the v2.0 OVH-root review): DEVICE PRESENCE IS NOT ATTESTABILITY.
A /dev/tpmrm0 node existing does not mean the cloud hands you a manufacturer cert
chain. We probe for the *evidence*, and when we can produce a measurement but
cannot anchor its chain, we record the tier CONSERVATIVELY.

This module performs detection only. It does not sign. The mint tool consumes the
resolved tier + evidence pointer and writes them into the passport claims.

Stdlib only. Real probes use modern Linux configfs-tsm where available
(kernel >= 6.5: /sys/kernel/config/tsm/report) and fall back to legacy guest
device nodes. vTPM/TPM cert-chain check is delegated to an injectable callback
because "is this vTPM chain anchorable" is cloud-specific and must not be guessed.
"""

from __future__ import annotations

import os
import socket
from dataclasses import dataclass, field
from enum import Enum
from typing import Callable, Optional


class Tier(str, Enum):
    T1 = "1"      # physical TPM, cert chain
    T1_5 = "1.5"  # attestable vTPM
    T2_0 = "2.0"  # SEV-SNP / TDX
    T2_5 = "2.5"  # DNS-anchored (DKIM pattern)
    T4 = "4"      # software only


@dataclass
class AttestationResult:
    tier: Tier
    evidence_kind: str                 # "tpm-cert" | "vtpm-cert" | "sev-snp" | "tdx" | "dns" | "none"
    evidence_ref: Optional[str] = None # path/handle/record the mint tool records
    conservative: bool = False         # True if we downgraded due to unprovable chain
    notes: list[str] = field(default_factory=list)


# --- low-level probes -------------------------------------------------------

# configfs-tsm: unified report interface (kernel >= ~6.5). Presence of the
# provider subdir is the strongest cheap signal that the guest can emit a
# hardware-signed report.
_TSM_REPORT = "/sys/kernel/config/tsm/report"

# legacy guest device nodes
_SEV_GUEST = "/dev/sev-guest"
_TDX_GUEST_NODES = ("/dev/tdx_guest", "/dev/tdx-guest")

# TPM device nodes (resource-manager preferred)
_TPM_NODES = ("/dev/tpmrm0", "/dev/tpm0")


def _has_tpm_device() -> bool:
    return any(os.path.exists(p) for p in _TPM_NODES)


def _has_sev_snp() -> bool:
    if os.path.exists(_SEV_GUEST):
        return True
    # configfs-tsm provider hint
    prov = _tsm_provider()
    return prov is not None and "sev" in prov.lower()


def _has_tdx() -> bool:
    if any(os.path.exists(p) for p in _TDX_GUEST_NODES):
        return True
    prov = _tsm_provider()
    return prov is not None and "tdx" in prov.lower()


def _tsm_provider() -> Optional[str]:
    """Read the configfs-tsm provider name if the subsystem is mounted."""
    prov_path = os.path.join(_TSM_REPORT, "provider")
    try:
        # provider can be a top-level file on some kernels; tolerate both layouts
        if os.path.exists(prov_path):
            with open(prov_path) as fh:
                return fh.read().strip()
        if os.path.isdir(_TSM_REPORT):
            # directory exists but no provider file -> subsystem present, unknown vendor
            return ""
    except OSError:
        return None
    return None


def _dns_anchor_reachable(realm: str, timeout: float = 2.0) -> bool:
    """Cheap reachability check: can we resolve the realm at all.

    NOTE: this only proves the zone resolves, not that the agent's TXT anchor is
    correct. Anchor correctness is verified at issuance, not here.
    """
    try:
        socket.setdefaulttimeout(timeout)
        socket.getaddrinfo(realm, None)
        return True
    except (socket.gaierror, socket.timeout, OSError):
        return False


# --- resolver ---------------------------------------------------------------

def resolve_attestation_tier(
    realm: str,
    *,
    tpm_cert_chain_check: Optional[Callable[[], bool]] = None,
    vtpm_is_virtual: Optional[Callable[[], bool]] = None,
) -> AttestationResult:
    """Resolve the highest provable tier for this node.

    Args:
        realm: the realm domain (e.g. "aetherpro.us"), used for the Tier 2.5
            reachability fallback.
        tpm_cert_chain_check: cloud/host-specific callback returning True iff the
            present TPM exposes an anchorable manufacturer (or cloud) cert chain.
            If a TPM device exists but this is None or returns False, we DO NOT
            claim Tier 1/1.5 — we downgrade conservatively and say why.
        vtpm_is_virtual: optional callback returning True iff the present TPM is a
            vTPM (vs physical). Distinguishes Tier 1 from Tier 1.5 when the chain
            check passes. If unknown, we default the chain-backed TPM to 1.5
            (the safer, lower claim).

    Returns:
        AttestationResult with tier, evidence kind/ref, and conservative flag.
    """
    notes: list[str] = []

    # 1 / 1.5: TPM path -----------------------------------------------------
    if _has_tpm_device():
        chain_ok = bool(tpm_cert_chain_check()) if tpm_cert_chain_check else False
        if chain_ok:
            is_virtual = bool(vtpm_is_virtual()) if vtpm_is_virtual else True
            if is_virtual:
                return AttestationResult(
                    Tier.T1_5, "vtpm-cert",
                    evidence_ref=_first_existing(_TPM_NODES),
                    notes=["vTPM with anchorable cert chain"],
                )
            return AttestationResult(
                Tier.T1, "tpm-cert",
                evidence_ref=_first_existing(_TPM_NODES),
                notes=["physical TPM with manufacturer cert chain"],
            )
        notes.append(
            "TPM device present but no anchorable cert chain (or no chain check "
            "supplied) -> NOT claiming Tier 1/1.5. Device presence != attestability."
        )

    # 2.0: confidential compute --------------------------------------------
    if _has_sev_snp():
        return AttestationResult(
            Tier.T2_0, "sev-snp",
            evidence_ref=_SEV_GUEST if os.path.exists(_SEV_GUEST) else _TSM_REPORT,
            conservative=bool(notes), notes=notes + ["AMD SEV-SNP report available"],
        )
    if _has_tdx():
        return AttestationResult(
            Tier.T2_0, "tdx",
            evidence_ref=_first_existing(_TDX_GUEST_NODES) or _TSM_REPORT,
            conservative=bool(notes), notes=notes + ["Intel TDX quote available"],
        )

    # 2.5: DNS anchor -------------------------------------------------------
    if _dns_anchor_reachable(realm):
        return AttestationResult(
            Tier.T2_5, "dns", evidence_ref=realm,
            conservative=bool(notes),
            notes=notes + [
                "DNS-anchored (DKIM pattern). Security reduces to DNS-account "
                "security; key lives on disk."
            ],
        )

    # 4: nothing ------------------------------------------------------------
    return AttestationResult(
        Tier.T4, "none", conservative=bool(notes),
        notes=notes + ["NO attestation anchor. Software-only identity. WARN."],
    )


def _first_existing(paths) -> Optional[str]:
    for p in paths:
        if os.path.exists(p):
            return p
    return None


if __name__ == "__main__":
    import json
    import sys

    realm = sys.argv[1] if len(sys.argv) > 1 else "aetherpro.us"
    # On OVH public-cloud KVM the expected result is Tier 2.5: no TPM cert chain,
    # no SEV-SNP/TDX, DNS reachable.
    res = resolve_attestation_tier(realm)
    print(json.dumps({
        "realm": realm,
        "tier": res.tier.value,
        "evidence_kind": res.evidence_kind,
        "evidence_ref": res.evidence_ref,
        "conservative": res.conservative,
        "notes": res.notes,
    }, indent=2))

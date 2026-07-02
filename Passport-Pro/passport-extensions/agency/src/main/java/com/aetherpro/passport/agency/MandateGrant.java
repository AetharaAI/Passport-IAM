package com.aetherpro.passport.agency;

import java.time.Instant;

/**
 * Parameters for creating a first-class, scoped mandate.
 *
 * A Mandate is a scoped, revocable, time-boxed authority grant that sits between
 * a Delegate and an Agent Passport / runtime use:
 *
 *   Principal (grantor) -> Delegate (grantee) -> Mandate -> Agent Passport / runtime use
 *
 * The scope envelopes are stored verbatim as JSON strings so the authority plane
 * stays declarative; enforcement lives in the existing PEP path, not here.
 */
public class MandateGrant {

    private String name;
    private String kind;
    private String grantorPrincipalId;
    /** Capability scope, stored in the mandate SCOPE column (space/comma separated). */
    private String capabilityScope;
    private String modelScope;
    private String resourceScope;
    private String harnessScope;
    private String metadata;
    private boolean revocable = true;
    private Instant validFrom;
    private Instant validUntil;

    public String getName() {
        return name;
    }

    public MandateGrant setName(String name) {
        this.name = name;
        return this;
    }

    public String getKind() {
        return kind;
    }

    public MandateGrant setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public String getGrantorPrincipalId() {
        return grantorPrincipalId;
    }

    public MandateGrant setGrantorPrincipalId(String grantorPrincipalId) {
        this.grantorPrincipalId = grantorPrincipalId;
        return this;
    }

    public String getCapabilityScope() {
        return capabilityScope;
    }

    public MandateGrant setCapabilityScope(String capabilityScope) {
        this.capabilityScope = capabilityScope;
        return this;
    }

    public String getModelScope() {
        return modelScope;
    }

    public MandateGrant setModelScope(String modelScope) {
        this.modelScope = modelScope;
        return this;
    }

    public String getResourceScope() {
        return resourceScope;
    }

    public MandateGrant setResourceScope(String resourceScope) {
        this.resourceScope = resourceScope;
        return this;
    }

    public String getHarnessScope() {
        return harnessScope;
    }

    public MandateGrant setHarnessScope(String harnessScope) {
        this.harnessScope = harnessScope;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public MandateGrant setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public boolean isRevocable() {
        return revocable;
    }

    public MandateGrant setRevocable(boolean revocable) {
        this.revocable = revocable;
        return this;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public MandateGrant setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public MandateGrant setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }
}

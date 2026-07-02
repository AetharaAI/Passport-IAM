package com.aetherpro.passport.agency.jpa;

import com.aetherpro.passport.agency.MandateModel;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity for a mandate
 */
@Entity
@Table(name = "PASSPORT_MANDATE")
@NamedQueries({
    @NamedQuery(
        name = "MandateEntity.findByDelegate",
        query = "SELECT m FROM MandateEntity m WHERE m.delegateId = :delegateId ORDER BY m.createdAt DESC"
    ),
    @NamedQuery(
        name = "MandateEntity.findByRealm",
        query = "SELECT m FROM MandateEntity m WHERE m.realmId = :realmId ORDER BY m.createdAt DESC"
    ),
    @NamedQuery(
        name = "MandateEntity.findActiveByDelegateAndScope",
        query = "SELECT m FROM MandateEntity m WHERE m.delegateId = :delegateId AND m.scope = :scope AND m.active = true AND m.suspendedAt IS NULL AND (m.validUntil IS NULL OR m.validUntil > :now)"
    ),
    @NamedQuery(
        name = "MandateEntity.findActiveByAgentAndScope",
        query = "SELECT m FROM MandateEntity m JOIN DelegateEntity d ON m.delegateId = d.id WHERE d.agentId = :agentId AND m.scope LIKE :scopePattern AND m.active = true AND m.suspendedAt IS NULL AND (m.validUntil IS NULL OR m.validUntil > :now)"
    )
})
public class MandateEntity implements MandateModel {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id;
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "DELEGATE_ID", nullable = false, length = 36)
    private String delegateId;
    
    @Column(name = "SCOPE", nullable = false, length = 255)
    private String scope;
    
    @Column(name = "CONSTRAINTS", length = 4000)
    private String constraints;
    
    @Column(name = "MAX_AMOUNT")
    private Double maxAmount;
    
    @Column(name = "REQUIRES_SECOND_FACTOR", nullable = false)
    private boolean requiresSecondFactor = false;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "VALID_FROM")
    private Instant validFrom;
    
    @Column(name = "VALID_UNTIL")
    private Instant validUntil;
    
    @Column(name = "USAGE_COUNT")
    private Integer usageCount = 0;
    
    @Column(name = "LAST_USED_AT")
    private Instant lastUsedAt;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;
    
    @Column(name = "SUSPENDED_AT")
    private Instant suspendedAt;
    
    @Column(name = "SUSPENSION_REASON", length = 500)
    private String suspensionReason;

    // First-class mandate fields (from agency-changelog-003-mandates.xml)
    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "KIND", length = 40)
    private String kind;

    @Column(name = "GRANTOR_PRINCIPAL_ID", length = 36)
    private String grantorPrincipalId;

    @Column(name = "MODEL_SCOPE", columnDefinition = "TEXT")
    private String modelScope;

    @Column(name = "RESOURCE_SCOPE", columnDefinition = "TEXT")
    private String resourceScope;

    @Column(name = "HARNESS_SCOPE", columnDefinition = "TEXT")
    private String harnessScope;

    @Column(name = "METADATA", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "REVOCABLE")
    private boolean revocable = true;

    // Cryptographic signature fields (from agency-changelog-002-crypto.xml)
    @Column(name = "PRINCIPAL_SIGNATURE", length = 4000)
    private String principalSignature;

    @Column(name = "PRINCIPAL_KID", length = 64)
    private String principalKid;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
    
    // Getters and Setters
    
    @Override
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String getRealmId() {
        return realmId;
    }
    
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
    
    @Override
    public String getDelegateId() {
        return delegateId;
    }
    
    public void setDelegateId(String delegateId) {
        this.delegateId = delegateId;
    }
    
    @Override
    public String getScope() {
        return scope;
    }
    
    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    @Override
    public String getConstraints() {
        return constraints;
    }
    
    @Override
    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }
    
    @Override
    public Double getMaxAmount() {
        return maxAmount;
    }
    
    @Override
    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    @Override
    public boolean requiresSecondFactor() {
        return requiresSecondFactor;
    }
    
    @Override
    public void setRequiresSecondFactor(boolean requires) {
        this.requiresSecondFactor = requires;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public Instant getValidFrom() {
        return validFrom;
    }
    
    @Override
    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }
    
    @Override
    public Instant getValidUntil() {
        return validUntil;
    }
    
    @Override
    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }
    
    @Override
    public Integer getUsageCount() {
        return usageCount;
    }
    
    @Override
    public void setUsageCount(Integer count) {
        this.usageCount = count;
    }
    
    @Override
    public Instant getLastUsedAt() {
        return lastUsedAt;
    }
    
    @Override
    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public Instant getSuspendedAt() {
        return suspendedAt;
    }
    
    @Override
    public void setSuspendedAt(Instant suspendedAt) {
        this.suspendedAt = suspendedAt;
    }
    
    @Override
    public String getSuspensionReason() {
        return suspensionReason;
    }
    
    @Override
    public void setSuspensionReason(String reason) {
        this.suspensionReason = reason;
    }

    // First-class mandate getters/setters

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getKind() {
        return kind;
    }

    @Override
    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public String getGrantorPrincipalId() {
        return grantorPrincipalId;
    }

    @Override
    public void setGrantorPrincipalId(String grantorPrincipalId) {
        this.grantorPrincipalId = grantorPrincipalId;
    }

    @Override
    public String getModelScope() {
        return modelScope;
    }

    @Override
    public void setModelScope(String modelScope) {
        this.modelScope = modelScope;
    }

    @Override
    public String getResourceScope() {
        return resourceScope;
    }

    @Override
    public void setResourceScope(String resourceScope) {
        this.resourceScope = resourceScope;
    }

    @Override
    public String getHarnessScope() {
        return harnessScope;
    }

    @Override
    public void setHarnessScope(String harnessScope) {
        this.harnessScope = harnessScope;
    }

    @Override
    public String getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean isRevocable() {
        return revocable;
    }

    @Override
    public void setRevocable(boolean revocable) {
        this.revocable = revocable;
    }

    // Cryptographic signature getters/setters

    public String getPrincipalSignature() {
        return principalSignature;
    }

    public void setPrincipalSignature(String principalSignature) {
        this.principalSignature = principalSignature;
    }

    public String getPrincipalKid() {
        return principalKid;
    }

    public void setPrincipalKid(String principalKid) {
        this.principalKid = principalKid;
    }
}

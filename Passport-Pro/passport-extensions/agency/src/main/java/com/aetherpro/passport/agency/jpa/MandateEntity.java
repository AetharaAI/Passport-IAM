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
}

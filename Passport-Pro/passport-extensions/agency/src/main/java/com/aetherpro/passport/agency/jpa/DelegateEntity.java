package com.aetherpro.passport.agency.jpa;

import com.aetherpro.passport.agency.DelegateModel;
import com.aetherpro.passport.agency.DelegationType;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity for a delegate relationship
 */
@Entity
@Table(name = "PASSPORT_DELEGATE")
@NamedQueries({
    @NamedQuery(
        name = "DelegateEntity.findByPrincipal",
        query = "SELECT d FROM DelegateEntity d WHERE d.principalId = :principalId ORDER BY d.createdAt DESC"
    ),
    @NamedQuery(
        name = "DelegateEntity.findByAgent",
        query = "SELECT d FROM DelegateEntity d WHERE d.agentId = :agentId ORDER BY d.createdAt DESC"
    ),
    @NamedQuery(
        name = "DelegateEntity.findActiveByAgent",
        query = "SELECT d FROM DelegateEntity d WHERE d.agentId = :agentId AND d.active = true AND d.revokedAt IS NULL AND (d.validUntil IS NULL OR d.validUntil > :now) ORDER BY d.createdAt DESC"
    ),
    @NamedQuery(
        name = "DelegateEntity.findActiveByAgentAndPrincipal",
        query = "SELECT d FROM DelegateEntity d WHERE d.agentId = :agentId AND d.principalId = :principalId AND d.active = true AND d.revokedAt IS NULL AND (d.validUntil IS NULL OR d.validUntil > :now)"
    )
})
public class DelegateEntity implements DelegateModel {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id;
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "AGENT_ID", nullable = false, length = 36)
    private String agentId;
    
    @Column(name = "PRINCIPAL_ID", nullable = false, length = 36)
    private String principalId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 50)
    private DelegationType type;
    
    @Column(name = "CONSTRAINTS", length = 4000)
    private String constraints;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "VALID_FROM")
    private Instant validFrom;
    
    @Column(name = "VALID_UNTIL")
    private Instant validUntil;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;
    
    @Column(name = "REVOKED_AT")
    private Instant revokedAt;
    
    @Column(name = "REVOCATION_REASON", length = 500)
    private String revocationReason;
    
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
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    @Override
    public String getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }
    
    @Override
    public DelegationType getType() {
        return type;
    }
    
    @Override
    public void setType(DelegationType type) {
        this.type = type;
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
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public Instant getRevokedAt() {
        return revokedAt;
    }
    
    @Override
    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    @Override
    public String getRevocationReason() {
        return revocationReason;
    }
    
    @Override
    public void setRevocationReason(String reason) {
        this.revocationReason = reason;
    }
}

package com.aetherpro.passport.agency.jpa;

import com.aetherpro.passport.agency.PrincipalModel;
import com.aetherpro.passport.agency.PrincipalType;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity for a legal principal
 */
@Entity
@Table(name = "PASSPORT_PRINCIPAL")
@NamedQueries({
    @NamedQuery(
        name = "PrincipalEntity.findByRealm",
        query = "SELECT p FROM PrincipalEntity p WHERE p.realmId = :realmId ORDER BY p.name"
    ),
    @NamedQuery(
        name = "PrincipalEntity.findByRealmAndType",
        query = "SELECT p FROM PrincipalEntity p WHERE p.realmId = :realmId AND p.type = :type ORDER BY p.name"
    ),
    @NamedQuery(
        name = "PrincipalEntity.findByRealmAndJurisdiction",
        query = "SELECT p FROM PrincipalEntity p WHERE p.realmId = :realmId AND p.jurisdiction = :jurisdiction ORDER BY p.name"
    ),
    @NamedQuery(
        name = "PrincipalEntity.searchByName",
        query = "SELECT p FROM PrincipalEntity p WHERE p.realmId = :realmId AND LOWER(p.name) LIKE :search ORDER BY p.name"
    ),
    @NamedQuery(
        name = "PrincipalEntity.countByRealm",
        query = "SELECT COUNT(p) FROM PrincipalEntity p WHERE p.realmId = :realmId"
    )
})
public class PrincipalEntity implements PrincipalModel {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id;
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "NAME", nullable = false, length = 255)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 50)
    private PrincipalType type;
    
    @Column(name = "JURISDICTION", length = 10)
    private String jurisdiction;
    
    @Column(name = "METADATA", length = 4000)
    private String metadata;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;
    
    @Column(name = "UPDATED_AT")
    private Instant updatedAt;
    
    @Column(name = "SUSPENDED_AT")
    private Instant suspendedAt;
    
    @Column(name = "SUSPENSION_REASON", length = 500)
    private String suspensionReason;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
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
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public PrincipalType getType() {
        return type;
    }
    
    @Override
    public void setType(PrincipalType type) {
        this.type = type;
    }
    
    @Override
    public String getJurisdiction() {
        return jurisdiction;
    }
    
    @Override
    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
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
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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

package com.aetherpro.passport.agency.jpa;

import com.aetherpro.passport.agency.AgentPassport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

/**
 * JPA Entity for an Agent Passport
 * 
 * Agent Passports are the REVENUE FEATURE - persistent DID-based identity for AI agents.
 */
@Entity
@Table(name = "PASSPORT_AGENT_IDENTITY")
@NamedQueries({
    @NamedQuery(
        name = "AgentPassportEntity.findByPrincipal",
        query = "SELECT a FROM AgentPassportEntity a WHERE a.principalId = :principalId ORDER BY a.mintedAt DESC"
    ),
    @NamedQuery(
        name = "AgentPassportEntity.findByDid",
        query = "SELECT a FROM AgentPassportEntity a WHERE a.passportDid = :passportDid"
    ),
    @NamedQuery(
        name = "AgentPassportEntity.findActiveByPrincipal",
        query = "SELECT a FROM AgentPassportEntity a WHERE a.principalId = :principalId AND a.active = true AND a.revokedAt IS NULL AND (a.expiresAt IS NULL OR a.expiresAt > :now)"
    ),
    @NamedQuery(
        name = "AgentPassportEntity.countByPrincipal",
        query = "SELECT COUNT(a) FROM AgentPassportEntity a WHERE a.principalId = :principalId AND a.active = true AND a.revokedAt IS NULL"
    )
})
public class AgentPassportEntity implements AgentPassport {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Id
    @Column(name = "ID", length = 36)
    private String id;
    
    @Column(name = "PASSPORT_DID", nullable = false, unique = true, length = 100)
    private String passportDid;
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "PRINCIPAL_ID", nullable = false, length = 36)
    private String principalId;
    
    @Column(name = "AGENT_TYPE", nullable = false, length = 50)
    private String agentType;
    
    @Column(name = "CAPABILITIES", length = 4000)
    private String capabilities;
    
    @Column(name = "RATE_LIMITS", length = 1000)
    private String rateLimits;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "MINTED_AT", nullable = false)
    private Instant mintedAt;
    
    @Column(name = "EXPIRES_AT")
    private Instant expiresAt;
    
    @Column(name = "REVOKED_AT")
    private Instant revokedAt;
    
    @Column(name = "REVOCATION_REASON", length = 500)
    private String revocationReason;
    
    @Column(name = "LAST_USED_AT")
    private Instant lastUsedAt;
    
    @Column(name = "USAGE_COUNT")
    private Integer usageCount = 0;
    
    @PrePersist
    protected void onCreate() {
        if (mintedAt == null) {
            mintedAt = Instant.now();
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
    public String getPassportDid() {
        return passportDid;
    }
    
    public void setPassportDid(String passportDid) {
        this.passportDid = passportDid;
    }
    
    @Override
    public String getRealmId() {
        return realmId;
    }
    
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
    
    @Override
    public String getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }
    
    @Override
    public String getAgentType() {
        return agentType;
    }
    
    @Override
    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }
    
    @Override
    public String getCapabilities() {
        return capabilities;
    }
    
    @Override
    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }
    
    @Override
    public List<String> getCapabilitiesList() {
        if (capabilities == null || capabilities.isEmpty()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(capabilities, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
    
    @Override
    public String getRateLimits() {
        return rateLimits;
    }
    
    @Override
    public void setRateLimits(String rateLimits) {
        this.rateLimits = rateLimits;
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
    public Instant getMintedAt() {
        return mintedAt;
    }
    
    public void setMintedAt(Instant mintedAt) {
        this.mintedAt = mintedAt;
    }
    
    @Override
    public Instant getExpiresAt() {
        return expiresAt;
    }
    
    @Override
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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
    
    @Override
    public Instant getLastUsedAt() {
        return lastUsedAt;
    }
    
    @Override
    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    @Override
    public Integer getUsageCount() {
        return usageCount;
    }
    
    @Override
    public void setUsageCount(Integer count) {
        this.usageCount = count;
    }
}

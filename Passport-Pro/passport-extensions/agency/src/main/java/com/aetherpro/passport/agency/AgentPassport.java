package com.aetherpro.passport.agency;

import java.time.Instant;
import java.util.List;

/**
 * Model interface for an Agent Passport
 * 
 * An Agent Passport is a persistent, DID-based identity for AI agents.
 * This is a REVENUE FEATURE - agents pay for persistent identity.
 */
public interface AgentPassport {
    
    /** Unique identifier (internal) */
    String getId();
    
    /** DID-format passport ID: did:passport:<uuid> */
    String getPassportDid();
    
    /** Realm this passport belongs to */
    String getRealmId();
    
    /** Principal ID that owns this passport */
    String getPrincipalId();
    
    /** Agent type: ai-assistant | autonomous-agent | human-proxy */
    String getAgentType();
    void setAgentType(String agentType);
    
    /** JSON array of capabilities this agent has */
    String getCapabilities();
    void setCapabilities(String capabilities);
    
    /** Parsed capabilities as list */
    default List<String> getCapabilitiesList() {
        // Implementation will parse JSON
        return List.of();
    }
    
    /** JSON rate limiting configuration */
    String getRateLimits();
    void setRateLimits(String rateLimits);
    
    /** Whether this passport is currently active */
    boolean isActive();
    void setActive(boolean active);
    
    /** When this passport was minted */
    Instant getMintedAt();
    
    /** When this passport expires */
    Instant getExpiresAt();
    void setExpiresAt(Instant expiresAt);
    
    /** When this passport was revoked (if applicable) */
    Instant getRevokedAt();
    void setRevokedAt(Instant revokedAt);
    
    /** Reason for revocation (if applicable) */
    String getRevocationReason();
    void setRevocationReason(String reason);
    
    /** When this passport was last used */
    Instant getLastUsedAt();
    void setLastUsedAt(Instant lastUsedAt);
    
    /** Number of times this passport has been used */
    Integer getUsageCount();
    void setUsageCount(Integer count);
    
    /**
     * Check if this passport is currently valid
     */
    default boolean isCurrentlyValid() {
        if (!isActive()) return false;
        if (getRevokedAt() != null) return false;
        
        Instant now = Instant.now();
        if (getExpiresAt() != null && now.isAfter(getExpiresAt())) return false;
        
        return true;
    }
}

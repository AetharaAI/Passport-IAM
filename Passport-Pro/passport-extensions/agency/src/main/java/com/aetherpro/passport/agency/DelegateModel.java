package com.aetherpro.passport.agency;

import java.time.Instant;

/**
 * Model interface for a delegate relationship
 * 
 * A Delegate represents a user who is authorized to act on behalf of a Principal.
 * Delegates have constraints and time limits on their authority.
 */
public interface DelegateModel {
    
    /** Unique identifier */
    String getId();
    
    /** Realm this delegate belongs to */
    String getRealmId();
    
    /** User ID of the agent */
    String getAgentId();
    
    /** Principal ID being represented */
    String getPrincipalId();
    
    /** Type of delegation */
    DelegationType getType();
    void setType(DelegationType type);
    
    /** JSON constraint rules */
    String getConstraints();
    void setConstraints(String constraints);
    
    /** Whether this delegation is currently active */
    boolean isActive();
    void setActive(boolean active);
    
    /** When this delegation becomes valid */
    Instant getValidFrom();
    void setValidFrom(Instant validFrom);
    
    /** When this delegation expires (null = indefinite) */
    Instant getValidUntil();
    void setValidUntil(Instant validUntil);
    
    /** When this delegation was created */
    Instant getCreatedAt();
    
    /** When this delegation was revoked (if applicable) */
    Instant getRevokedAt();
    void setRevokedAt(Instant revokedAt);
    
    /** Reason for revocation (if applicable) */
    String getRevocationReason();
    void setRevocationReason(String reason);
    
    /**
     * Check if this delegation is currently valid
     * (active, within time window, not revoked)
     */
    default boolean isCurrentlyValid() {
        if (!isActive()) return false;
        if (getRevokedAt() != null) return false;
        
        Instant now = Instant.now();
        if (getValidFrom() != null && now.isBefore(getValidFrom())) return false;
        if (getValidUntil() != null && now.isAfter(getValidUntil())) return false;
        
        return true;
    }
}

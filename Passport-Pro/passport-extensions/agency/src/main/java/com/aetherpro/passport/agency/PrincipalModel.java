package com.aetherpro.passport.agency;

import java.time.Instant;

/**
 * Model interface for a legal principal
 * 
 * A Principal represents a legal entity that can hold rights and grant delegations.
 * Examples: individuals, corporations, government agencies, AI systems
 */
public interface PrincipalModel {
    
    /** Unique identifier */
    String getId();
    
    /** Realm this principal belongs to */
    String getRealmId();
    
    /** Display name */
    String getName();
    void setName(String name);
    
    /** Type of principal */
    PrincipalType getType();
    void setType(PrincipalType type);
    
    /** Legal jurisdiction (ISO 3166-2 code) */
    String getJurisdiction();
    void setJurisdiction(String jurisdiction);
    
    /** Additional JSON metadata */
    String getMetadata();
    void setMetadata(String metadata);
    
    /** Whether this principal is currently active */
    boolean isActive();
    void setActive(boolean active);
    
    /** When this principal was created */
    Instant getCreatedAt();
    
    /** When this principal was last updated */
    Instant getUpdatedAt();
    void setUpdatedAt(Instant updatedAt);
    
    /** When this principal was suspended (if applicable) */
    Instant getSuspendedAt();
    void setSuspendedAt(Instant suspendedAt);
    
    /** Reason for suspension (if applicable) */
    String getSuspensionReason();
    void setSuspensionReason(String reason);
}

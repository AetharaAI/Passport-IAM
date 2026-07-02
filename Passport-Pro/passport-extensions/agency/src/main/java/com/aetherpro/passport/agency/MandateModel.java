package com.aetherpro.passport.agency;

import java.time.Instant;

/**
 * Model interface for a mandate
 * 
 * A Mandate is a specific, scoped authorization granted to a delegate.
 * Mandates can have financial limits, time constraints, and usage tracking.
 */
public interface MandateModel {
    
    /** Unique identifier */
    String getId();
    
    /** Realm this mandate belongs to */
    String getRealmId();
    
    /** Delegate ID this mandate is attached to */
    String getDelegateId();
    
    /** Action scope (e.g., "contracts.sign", "payments.approve:limit:10000") */
    String getScope();
    void setScope(String scope);
    
    /** JSON constraint rules for detailed validation */
    String getConstraints();
    void setConstraints(String constraints);
    
    /** Maximum authorized amount (for financial mandates) */
    Double getMaxAmount();
    void setMaxAmount(Double maxAmount);
    
    /** Whether 2FA is required when using this mandate */
    boolean requiresSecondFactor();
    void setRequiresSecondFactor(boolean requires);
    
    /** Whether this mandate is currently active */
    boolean isActive();
    void setActive(boolean active);
    
    /** When this mandate becomes valid */
    Instant getValidFrom();
    void setValidFrom(Instant validFrom);
    
    /** When this mandate expires (null = indefinite) */
    Instant getValidUntil();
    void setValidUntil(Instant validUntil);
    
    /** Number of times this mandate has been used */
    Integer getUsageCount();
    void setUsageCount(Integer count);
    
    /** When this mandate was last used */
    Instant getLastUsedAt();
    void setLastUsedAt(Instant lastUsedAt);
    
    /** When this mandate was created */
    Instant getCreatedAt();
    
    /** When this mandate was suspended (if applicable) */
    Instant getSuspendedAt();
    void setSuspendedAt(Instant suspendedAt);
    
    /** Reason for suspension (if applicable) */
    String getSuspensionReason();
    void setSuspensionReason(String reason);

    // ----- First-class mandate attributes -----
    // Defaulted so existing implementations remain source-compatible; the JPA
    // entity overrides these with persisted columns.

    /** Human-friendly mandate name */
    default String getName() { return null; }
    default void setName(String name) { }

    /** Mandate kind (operator, support, integration, model_route, benchmark,
     *  collab, break_glass_reserved). break_glass_reserved is reserved only —
     *  no break-glass behaviour is implemented. */
    default String getKind() { return null; }
    default void setKind(String kind) { }

    /** Grantor principal id (the owning/legal entity granting this authority) */
    default String getGrantorPrincipalId() { return null; }
    default void setGrantorPrincipalId(String grantorPrincipalId) { }

    /** JSON model_scope envelope (which model classes/models this grant covers) */
    default String getModelScope() { return null; }
    default void setModelScope(String modelScope) { }

    /** JSON resource_scope envelope (nodes, repos, datasets, ...) */
    default String getResourceScope() { return null; }
    default void setResourceScope(String resourceScope) { }

    /** JSON harness_scope list. Empty/absent means the grant is NOT valid in any
     *  harness by default — an Agent Passport must never imply authority across
     *  all harnesses. Harness scope is always explicit. */
    default String getHarnessScope() { return null; }
    default void setHarnessScope(String harnessScope) { }

    /** Free-form JSON metadata */
    default String getMetadata() { return null; }
    default void setMetadata(String metadata) { }

    /** Whether this mandate may be revoked (default true) */
    default boolean isRevocable() { return true; }
    default void setRevocable(boolean revocable) { }

    /**
     * Check if this mandate is currently valid
     */
    default boolean isCurrentlyValid() {
        if (!isActive()) return false;
        if (getSuspendedAt() != null) return false;
        
        Instant now = Instant.now();
        if (getValidFrom() != null && now.isBefore(getValidFrom())) return false;
        if (getValidUntil() != null && now.isAfter(getValidUntil())) return false;
        
        return true;
    }
    
    /**
     * Check if the requested amount is within mandate limits
     */
    default boolean isWithinAmountLimit(Double requestedAmount) {
        if (getMaxAmount() == null) return true;
        if (requestedAmount == null) return true;
        return requestedAmount <= getMaxAmount();
    }
}

package com.aetherpro.passport.agency.admin.representations;

import java.time.Instant;

/**
 * REST representation of a Mandate
 */
public class MandateRepresentation {
    
    private String id;
    private String delegateId;
    private String scope;
    private String constraints;
    private Double maxAmount;
    private Boolean requiresSecondFactor;
    private Boolean active;
    private String validFrom;
    private String validUntil;
    private Integer usageCount;
    private String lastUsedAt;
    private String createdAt;
    private String suspendedAt;
    private String suspensionReason;
    
    // Derived/enriched fields
    private Boolean isCurrentlyValid;
    private String delegateName;
    private String principalName;
    
    public MandateRepresentation() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDelegateId() {
        return delegateId;
    }
    
    public void setDelegateId(String delegateId) {
        this.delegateId = delegateId;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getConstraints() {
        return constraints;
    }
    
    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }
    
    public Double getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public Boolean getRequiresSecondFactor() {
        return requiresSecondFactor;
    }
    
    public void setRequiresSecondFactor(Boolean requiresSecondFactor) {
        this.requiresSecondFactor = requiresSecondFactor;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }
    
    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom != null ? validFrom.toString() : null;
    }
    
    public String getValidUntil() {
        return validUntil;
    }
    
    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }
    
    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil != null ? validUntil.toString() : null;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
    
    public String getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(String lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt != null ? lastUsedAt.toString() : null;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt != null ? createdAt.toString() : null;
    }
    
    public String getSuspendedAt() {
        return suspendedAt;
    }
    
    public void setSuspendedAt(String suspendedAt) {
        this.suspendedAt = suspendedAt;
    }
    
    public void setSuspendedAt(Instant suspendedAt) {
        this.suspendedAt = suspendedAt != null ? suspendedAt.toString() : null;
    }
    
    public String getSuspensionReason() {
        return suspensionReason;
    }
    
    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }
    
    public Boolean getIsCurrentlyValid() {
        return isCurrentlyValid;
    }
    
    public void setIsCurrentlyValid(Boolean isCurrentlyValid) {
        this.isCurrentlyValid = isCurrentlyValid;
    }
    
    public String getDelegateName() {
        return delegateName;
    }
    
    public void setDelegateName(String delegateName) {
        this.delegateName = delegateName;
    }
    
    public String getPrincipalName() {
        return principalName;
    }
    
    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }
}

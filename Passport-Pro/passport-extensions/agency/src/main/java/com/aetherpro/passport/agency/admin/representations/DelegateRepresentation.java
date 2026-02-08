package com.aetherpro.passport.agency.admin.representations;

import com.aetherpro.passport.agency.DelegationType;
import java.time.Instant;

/**
 * REST representation of a Delegate relationship
 */
public class DelegateRepresentation {
    
    private String id;
    private String agentId;
    private String agentUsername;
    private String agentEmail;
    private String principalId;
    private String principalName;
    private String type;
    private String constraints;
    private Boolean active;
    private String validFrom;
    private String validUntil;
    private String createdAt;
    private String revokedAt;
    private String revocationReason;
    
    // Derived fields
    private Boolean isCurrentlyValid;
    private Long mandateCount;
    
    public DelegateRepresentation() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getAgentUsername() {
        return agentUsername;
    }
    
    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }
    
    public String getAgentEmail() {
        return agentEmail;
    }
    
    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }
    
    public String getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }
    
    public String getPrincipalName() {
        return principalName;
    }
    
    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public DelegationType getTypeEnum() {
        if (type == null) return null;
        try {
            return DelegationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public String getConstraints() {
        return constraints;
    }
    
    public void setConstraints(String constraints) {
        this.constraints = constraints;
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
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt != null ? createdAt.toString() : null;
    }
    
    public String getRevokedAt() {
        return revokedAt;
    }
    
    public void setRevokedAt(String revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt != null ? revokedAt.toString() : null;
    }
    
    public String getRevocationReason() {
        return revocationReason;
    }
    
    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }
    
    public Boolean getIsCurrentlyValid() {
        return isCurrentlyValid;
    }
    
    public void setIsCurrentlyValid(Boolean isCurrentlyValid) {
        this.isCurrentlyValid = isCurrentlyValid;
    }
    
    public Long getMandateCount() {
        return mandateCount;
    }
    
    public void setMandateCount(Long mandateCount) {
        this.mandateCount = mandateCount;
    }
}

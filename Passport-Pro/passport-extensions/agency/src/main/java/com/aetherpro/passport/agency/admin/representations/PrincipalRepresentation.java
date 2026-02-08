package com.aetherpro.passport.agency.admin.representations;

import com.aetherpro.passport.agency.PrincipalType;
import java.time.Instant;

/**
 * REST representation of a Principal
 */
public class PrincipalRepresentation {
    
    private String id;
    private String name;
    private String type;
    private String jurisdiction;
    private String metadata;
    private Boolean active;
    private String createdAt;
    private String updatedAt;
    private String suspendedAt;
    private String suspensionReason;
    
    // Count fields for related entities
    private Long delegateCount;
    private Long passportCount;
    
    public PrincipalRepresentation() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public PrincipalType getTypeEnum() {
        if (type == null) return null;
        try {
            return PrincipalType.valueOf(type.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public String getJurisdiction() {
        return jurisdiction;
    }
    
    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
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
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt != null ? updatedAt.toString() : null;
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
    
    public Long getDelegateCount() {
        return delegateCount;
    }
    
    public void setDelegateCount(Long delegateCount) {
        this.delegateCount = delegateCount;
    }
    
    public Long getPassportCount() {
        return passportCount;
    }
    
    public void setPassportCount(Long passportCount) {
        this.passportCount = passportCount;
    }
}

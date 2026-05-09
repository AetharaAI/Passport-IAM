package com.aetherpro.passport.agency.admin.representations;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST representation of an Agent Passport
 */
public class AgentPassportRepresentation {
    
    private String id;
    private String passportDid;
    private String principalId;
    private String principalName;
    private String agentType;
    private String agentName;
    private String tier;
    private String publicKeyPem;
    private Map<String, Object> mandate;
    private String machinePassportId;
    private String jwt;
    private String publicKeyFingerprint;
    private String jti;
    private String capabilities;
    private List<String> capabilitiesList;
    private String rateLimits;
    private Boolean active;
    private String mintedAt;
    private String expiresAt;
    private String revokedAt;
    private String revocationReason;
    private String lastUsedAt;
    private Integer usageCount;
    
    // Derived fields
    private Boolean isCurrentlyValid;
    
    public AgentPassportRepresentation() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPassportDid() {
        return passportDid;
    }
    
    public void setPassportDid(String passportDid) {
        this.passportDid = passportDid;
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
    
    public String getAgentType() {
        return agentType;
    }
    
    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public Map<String, Object> getMandate() {
        return mandate;
    }

    public void setMandate(Map<String, Object> mandate) {
        this.mandate = mandate;
    }

    public String getMachinePassportId() {
        return machinePassportId;
    }

    public void setMachinePassportId(String machinePassportId) {
        this.machinePassportId = machinePassportId;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getPublicKeyFingerprint() {
        return publicKeyFingerprint;
    }

    public void setPublicKeyFingerprint(String publicKeyFingerprint) {
        this.publicKeyFingerprint = publicKeyFingerprint;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }
    
    public String getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }
    
    public List<String> getCapabilitiesList() {
        return capabilitiesList;
    }
    
    public void setCapabilitiesList(List<String> capabilitiesList) {
        this.capabilitiesList = capabilitiesList;
    }
    
    public String getRateLimits() {
        return rateLimits;
    }
    
    public void setRateLimits(String rateLimits) {
        this.rateLimits = rateLimits;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getMintedAt() {
        return mintedAt;
    }
    
    public void setMintedAt(String mintedAt) {
        this.mintedAt = mintedAt;
    }
    
    public void setMintedAt(Instant mintedAt) {
        this.mintedAt = mintedAt != null ? mintedAt.toString() : null;
    }
    
    public String getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt != null ? expiresAt.toString() : null;
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
    
    public String getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(String lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt != null ? lastUsedAt.toString() : null;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
    
    public Boolean getIsCurrentlyValid() {
        return isCurrentlyValid;
    }
    
    public void setIsCurrentlyValid(Boolean isCurrentlyValid) {
        this.isCurrentlyValid = isCurrentlyValid;
    }
}

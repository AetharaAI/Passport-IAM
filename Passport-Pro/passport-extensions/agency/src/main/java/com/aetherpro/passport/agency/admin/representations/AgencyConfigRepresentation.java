package com.aetherpro.passport.agency.admin.representations;

/**
 * REST representation of Agency Realm Configuration
 */
public class AgencyConfigRepresentation {
    
    private Boolean enabled;
    private String defaultJurisdiction;
    private String complianceMode;
    private Boolean mandatesRequired;
    private Integer defaultMandateValidityDays;
    private Boolean qualificationsEnforced;
    private String auditLevel;
    private Boolean agentPassportsEnabled;
    private Integer maxPassportsPerPrincipal;
    private String customPolicyScript;
    
    // Statistics
    private Long principalCount;
    private Long delegateCount;
    private Long mandateCount;
    private Long passportCount;
    
    public AgencyConfigRepresentation() {}
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getDefaultJurisdiction() {
        return defaultJurisdiction;
    }
    
    public void setDefaultJurisdiction(String defaultJurisdiction) {
        this.defaultJurisdiction = defaultJurisdiction;
    }
    
    public String getComplianceMode() {
        return complianceMode;
    }
    
    public void setComplianceMode(String complianceMode) {
        this.complianceMode = complianceMode;
    }
    
    public Boolean getMandatesRequired() {
        return mandatesRequired;
    }
    
    public void setMandatesRequired(Boolean mandatesRequired) {
        this.mandatesRequired = mandatesRequired;
    }
    
    public Integer getDefaultMandateValidityDays() {
        return defaultMandateValidityDays;
    }
    
    public void setDefaultMandateValidityDays(Integer defaultMandateValidityDays) {
        this.defaultMandateValidityDays = defaultMandateValidityDays;
    }
    
    public Boolean getQualificationsEnforced() {
        return qualificationsEnforced;
    }
    
    public void setQualificationsEnforced(Boolean qualificationsEnforced) {
        this.qualificationsEnforced = qualificationsEnforced;
    }
    
    public String getAuditLevel() {
        return auditLevel;
    }
    
    public void setAuditLevel(String auditLevel) {
        this.auditLevel = auditLevel;
    }
    
    public Boolean getAgentPassportsEnabled() {
        return agentPassportsEnabled;
    }
    
    public void setAgentPassportsEnabled(Boolean agentPassportsEnabled) {
        this.agentPassportsEnabled = agentPassportsEnabled;
    }
    
    public Integer getMaxPassportsPerPrincipal() {
        return maxPassportsPerPrincipal;
    }
    
    public void setMaxPassportsPerPrincipal(Integer maxPassportsPerPrincipal) {
        this.maxPassportsPerPrincipal = maxPassportsPerPrincipal;
    }
    
    public String getCustomPolicyScript() {
        return customPolicyScript;
    }
    
    public void setCustomPolicyScript(String customPolicyScript) {
        this.customPolicyScript = customPolicyScript;
    }
    
    public Long getPrincipalCount() {
        return principalCount;
    }
    
    public void setPrincipalCount(Long principalCount) {
        this.principalCount = principalCount;
    }
    
    public Long getDelegateCount() {
        return delegateCount;
    }
    
    public void setDelegateCount(Long delegateCount) {
        this.delegateCount = delegateCount;
    }
    
    public Long getMandateCount() {
        return mandateCount;
    }
    
    public void setMandateCount(Long mandateCount) {
        this.mandateCount = mandateCount;
    }
    
    public Long getPassportCount() {
        return passportCount;
    }
    
    public void setPassportCount(Long passportCount) {
        this.passportCount = passportCount;
    }
}

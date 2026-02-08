package com.aetherpro.passport.agency;

/**
 * Agency configuration for a realm
 */
public class AgencyRealmConfig {
    
    public enum ComplianceMode {
        NONE,
        GDPR,
        CCPA,
        HIPAA,
        SOX,
        PCI_DSS,
        CUSTOM
    }
    
    public enum AuditLevel {
        MINIMAL,
        STANDARD,
        DETAILED,
        FULL
    }
    
    /** Whether Agency features are enabled for this realm */
    private boolean enabled;
    
    /** Default jurisdiction for principals in this realm */
    private String defaultJurisdiction;
    
    /** Compliance mode governing data handling */
    private ComplianceMode complianceMode;
    
    /** Whether mandates are required for agency actions */
    private boolean mandatesRequired;
    
    /** Default validity period for mandates in days */
    private int defaultMandateValidityDays;
    
    /** Whether qualifications are enforced */
    private boolean qualificationsEnforced;
    
    /** Audit logging level */
    private AuditLevel auditLevel;
    
    /** Whether agent passports can be minted */
    private boolean agentPassportsEnabled;
    
    /** Maximum concurrent agent passports per principal */
    private int maxPassportsPerPrincipal;
    
    /** Custom policy script for complex decisions (JavaScript/Groovy) */
    private String customPolicyScript;
    
    public AgencyRealmConfig() {
        // Defaults
        this.enabled = false;
        this.defaultJurisdiction = "US-XX";
        this.complianceMode = ComplianceMode.NONE;
        this.mandatesRequired = true;
        this.defaultMandateValidityDays = 365;
        this.qualificationsEnforced = false;
        this.auditLevel = AuditLevel.STANDARD;
        this.agentPassportsEnabled = false;
        this.maxPassportsPerPrincipal = 10;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getDefaultJurisdiction() {
        return defaultJurisdiction;
    }
    
    public void setDefaultJurisdiction(String defaultJurisdiction) {
        this.defaultJurisdiction = defaultJurisdiction;
    }
    
    public ComplianceMode getComplianceMode() {
        return complianceMode;
    }
    
    public void setComplianceMode(ComplianceMode complianceMode) {
        this.complianceMode = complianceMode;
    }
    
    public boolean isMandatesRequired() {
        return mandatesRequired;
    }
    
    public void setMandatesRequired(boolean mandatesRequired) {
        this.mandatesRequired = mandatesRequired;
    }
    
    public int getDefaultMandateValidityDays() {
        return defaultMandateValidityDays;
    }
    
    public void setDefaultMandateValidityDays(int defaultMandateValidityDays) {
        this.defaultMandateValidityDays = defaultMandateValidityDays;
    }
    
    public boolean isQualificationsEnforced() {
        return qualificationsEnforced;
    }
    
    public void setQualificationsEnforced(boolean qualificationsEnforced) {
        this.qualificationsEnforced = qualificationsEnforced;
    }
    
    public AuditLevel getAuditLevel() {
        return auditLevel;
    }
    
    public void setAuditLevel(AuditLevel auditLevel) {
        this.auditLevel = auditLevel;
    }
    
    public boolean isAgentPassportsEnabled() {
        return agentPassportsEnabled;
    }
    
    public void setAgentPassportsEnabled(boolean agentPassportsEnabled) {
        this.agentPassportsEnabled = agentPassportsEnabled;
    }
    
    public int getMaxPassportsPerPrincipal() {
        return maxPassportsPerPrincipal;
    }
    
    public void setMaxPassportsPerPrincipal(int maxPassportsPerPrincipal) {
        this.maxPassportsPerPrincipal = maxPassportsPerPrincipal;
    }
    
    public String getCustomPolicyScript() {
        return customPolicyScript;
    }
    
    public void setCustomPolicyScript(String customPolicyScript) {
        this.customPolicyScript = customPolicyScript;
    }
}

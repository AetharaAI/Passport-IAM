package com.aetherpro.passport.agency;

/**
 * Result of an agency access decision
 */
public class AgencyDecision {
    
    public enum Decision {
        /** Access is granted */
        ALLOW,
        
        /** Access is denied */
        DENY,
        
        /** Requires additional approval */
        REQUIRE_APPROVAL,
        
        /** Requires step-up authentication */
        REQUIRE_STEP_UP,
        
        /** Requires human confirmation */
        REQUIRE_HUMAN_CONFIRM
    }
    
    private Decision decision;
    private String reason;
    private String mandateId;
    private String principalId;
    private boolean auditRequired;
    
    public AgencyDecision() {}
    
    public AgencyDecision(Decision decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }
    
    public static AgencyDecision allow(String mandateId, String principalId) {
        AgencyDecision d = new AgencyDecision(Decision.ALLOW, "Mandate valid");
        d.setMandateId(mandateId);
        d.setPrincipalId(principalId);
        return d;
    }
    
    public static AgencyDecision deny(String reason) {
        return new AgencyDecision(Decision.DENY, reason);
    }
    
    public static AgencyDecision requireApproval(String reason) {
        return new AgencyDecision(Decision.REQUIRE_APPROVAL, reason);
    }
    
    public static AgencyDecision requireStepUp(String reason) {
        return new AgencyDecision(Decision.REQUIRE_STEP_UP, reason);
    }
    
    public static AgencyDecision requireHumanConfirm(String reason) {
        AgencyDecision d = new AgencyDecision(Decision.REQUIRE_HUMAN_CONFIRM, reason);
        d.setAuditRequired(true);
        return d;
    }
    
    public Decision getDecision() {
        return decision;
    }
    
    public void setDecision(Decision decision) {
        this.decision = decision;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getMandateId() {
        return mandateId;
    }
    
    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }
    
    public String getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }
    
    public boolean isAuditRequired() {
        return auditRequired;
    }
    
    public void setAuditRequired(boolean auditRequired) {
        this.auditRequired = auditRequired;
    }
    
    public boolean isAllowed() {
        return decision == Decision.ALLOW;
    }
}

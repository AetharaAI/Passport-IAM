package com.aetherpro.passport.agency;

import java.util.List;
import java.util.Set;

/**
 * Agency context for a user session
 * 
 * Contains all agency-related information needed for token claims
 * and access control decisions during a user's session.
 */
public class AgencyContext {
    
    /** Whether user is acting as an agent */
    private boolean isAgent;
    
    /** Primary passport DID if user is an AI agent */
    private String primaryPassportId;
    
    /** List of principals this user is representing */
    private List<PrincipalReference> representing;
    
    /** Currently active scopes from all active mandates */
    private Set<String> activeScopes;
    
    /** List of valid qualification names */
    private List<String> qualifications;
    
    public AgencyContext() {}
    
    public AgencyContext(
        boolean isAgent,
        String primaryPassportId,
        List<PrincipalReference> representing,
        Set<String> activeScopes,
        List<String> qualifications
    ) {
        this.isAgent = isAgent;
        this.primaryPassportId = primaryPassportId;
        this.representing = representing;
        this.activeScopes = activeScopes;
        this.qualifications = qualifications;
    }
    
    public boolean isAgent() {
        return isAgent;
    }
    
    public void setAgent(boolean agent) {
        isAgent = agent;
    }
    
    public String getPrimaryPassportId() {
        return primaryPassportId;
    }
    
    public void setPrimaryPassportId(String primaryPassportId) {
        this.primaryPassportId = primaryPassportId;
    }
    
    public List<PrincipalReference> getRepresenting() {
        return representing;
    }
    
    public void setRepresenting(List<PrincipalReference> representing) {
        this.representing = representing;
    }
    
    public Set<String> getActiveScopes() {
        return activeScopes;
    }
    
    public void setActiveScopes(Set<String> activeScopes) {
        this.activeScopes = activeScopes;
    }
    
    public List<String> getQualifications() {
        return qualifications;
    }
    
    public void setQualifications(List<String> qualifications) {
        this.qualifications = qualifications;
    }
    
    /**
     * Reference to a principal being represented
     */
    public static class PrincipalReference {
        private String principalId;
        private String principalName;
        private Set<String> activeMandateScopes;
        
        public PrincipalReference() {}
        
        public PrincipalReference(
            String principalId, 
            String principalName, 
            Set<String> activeMandateScopes
        ) {
            this.principalId = principalId;
            this.principalName = principalName;
            this.activeMandateScopes = activeMandateScopes;
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
        
        public Set<String> getActiveMandateScopes() {
            return activeMandateScopes;
        }
        
        public void setActiveMandateScopes(Set<String> activeMandateScopes) {
            this.activeMandateScopes = activeMandateScopes;
        }
    }
}

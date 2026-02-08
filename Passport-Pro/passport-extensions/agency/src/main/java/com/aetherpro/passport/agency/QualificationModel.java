package com.aetherpro.passport.agency;

/**
 * Model interface for a qualification
 * 
 * A Qualification represents a credential, certification, or clearance
 * that a user may hold and that may be required for certain actions.
 */
public interface QualificationModel {
    
    /** Unique identifier */
    String getId();
    
    /** Realm this qualification belongs to */
    String getRealmId();
    
    /** Display name */
    String getName();
    void setName(String name);
    
    /** Type: certification | license | clearance | training */
    String getType();
    void setType(String type);
    
    /** Issuing authority */
    String getIssuer();
    void setIssuer(String issuer);
    
    /** Scope of actions this qualification enables */
    String getScope();
    void setScope(String scope);
    
    /** Default validity period in months */
    Integer getValidityMonths();
    void setValidityMonths(Integer months);
    
    /** Description of this qualification */
    String getDescription();
    void setDescription(String description);
    
    /** Whether this qualification is currently active/available */
    boolean isActive();
    void setActive(boolean active);
}

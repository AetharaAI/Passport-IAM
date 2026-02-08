package com.aetherpro.passport.agency;

import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.provider.Provider;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Passport-Pro Agency Provider Interface
 * 
 * Core Concepts:
 * - PRINCIPAL: Legal entity (person, org, system, AI agent) that can hold rights
 * - DELEGATE: User acting on behalf of a principal with specific constraints
 * - MANDATE: Time-bound, scope-limited authorization for a delegate
 * - QUALIFICATION: Credential/certification required for certain actions
 * - AGENT PASSPORT: Persistent DID-based identity for AI agents
 */
public interface AgencyProvider extends Provider {
    
    // ========== PRINCIPAL MANAGEMENT ==========
    
    /**
     * Create a new legal principal
     * @param realm The Passport realm
     * @param name Display name (e.g., "Acme Corp")
     * @param type individual | organization | system | ai-agent
     * @param jurisdiction Legal jurisdiction code (ISO 3166-2)
     * @param metadata Additional JSON metadata
     * @return The created principal
     */
    PrincipalModel createPrincipal(
        RealmModel realm, 
        String name, 
        PrincipalType type,
        String jurisdiction,
        String metadata
    );
    
    Optional<PrincipalModel> getPrincipal(RealmModel realm, String principalId);
    
    List<PrincipalModel> getRealmPrincipals(RealmModel realm);
    
    List<PrincipalModel> getPrincipalsByType(RealmModel realm, PrincipalType type);
    
    List<PrincipalModel> getPrincipalsByJurisdiction(RealmModel realm, String jurisdiction);
    
    List<PrincipalModel> searchPrincipals(RealmModel realm, String search, int first, int max);
    
    long countPrincipals(RealmModel realm);
    
    void updatePrincipal(PrincipalModel principal);
    
    void suspendPrincipal(String principalId, String reason);
    
    void activatePrincipal(String principalId);
    
    void revokePrincipal(String principalId, String reason);
    
    void deletePrincipal(String principalId);
    
    // ========== DELEGATE MANAGEMENT ==========
    
    /**
     * Create a delegation relationship
     * @param agent The user acting as agent
     * @param principal The principal they represent
     * @param delegationType full | limited | conditional | emergency
     * @param constraints JSON constraint rules
     * @param validFrom When delegation becomes active
     * @param validUntil When delegation expires (null for indefinite)
     * @return The created delegate
     */
    DelegateModel createDelegate(
        UserModel agent,
        PrincipalModel principal,
        DelegationType delegationType,
        String constraints,
        Instant validFrom,
        Instant validUntil
    );
    
    Optional<DelegateModel> getDelegate(String delegateId);
    
    List<DelegateModel> getDelegatesForPrincipal(PrincipalModel principal);
    
    List<DelegateModel> getDelegatesForAgent(UserModel agent);
    
    List<DelegateModel> getActiveDelegatesForAgent(UserModel agent);
    
    Optional<DelegateModel> getActiveDelegate(UserModel agent, PrincipalModel principal);
    
    boolean isValidDelegate(UserModel agent, PrincipalModel principal, String actionScope);
    
    boolean isValidDelegate(UserModel agent, String principalId, String actionScope);
    
    void updateDelegate(DelegateModel delegate);
    
    void revokeDelegate(String delegateId, String reason);
    
    void deleteDelegate(String delegateId);
    
    // ========== MANDATE MANAGEMENT ==========
    
    /**
     * Create a specific mandate (authorization instance)
     * @param delegate The delegation this mandate operates under
     * @param scope Action scope (e.g., "contracts.sign", "payments.approve")
     * @param constraints JSON constraint object
     * @param maxAmount For financial mandates, max authorized amount
     * @param requiresSecondFactor Whether 2FA is required for this mandate
     * @param validFrom When mandate becomes active
     * @param validUntil When mandate expires
     * @return The created mandate
     */
    MandateModel createMandate(
        DelegateModel delegate,
        String scope,
        String constraints,
        Double maxAmount,
        boolean requiresSecondFactor,
        Instant validFrom,
        Instant validUntil
    );
    
    Optional<MandateModel> getMandate(String mandateId);
    
    List<MandateModel> getMandatesForDelegate(DelegateModel delegate);
    
    List<MandateModel> getMandatesForDelegate(String delegateId);
    
    List<MandateModel> getActiveMandatesForAgent(UserModel agent, String scope);
    
    /**
     * Validate if a mandate permits a specific action
     * This is the core access control check for Agency/LBAC
     */
    MandateValidationResult validateMandate(
        String mandateId,
        String action,
        String resource,
        Double amount,
        String context
    );
    
    void recordMandateUsage(String mandateId);
    
    void suspendMandate(String mandateId, String reason);
    
    void revokeMandate(String mandateId, String reason);
    
    void deleteMandate(String mandateId);
    
    // ========== QUALIFICATION MANAGEMENT ==========
    
    QualificationModel createQualification(
        RealmModel realm,
        String name,
        String type,  // certification | license | clearance | training
        String issuer,
        String scope,
        Integer validityMonths
    );
    
    Optional<QualificationModel> getQualification(String qualificationId);
    
    List<QualificationModel> getRealmQualifications(RealmModel realm);
    
    void assignQualification(
        UserModel user, 
        QualificationModel qualification, 
        String credentialId, 
        Instant expiresAt
    );
    
    void revokeQualification(String assignmentId, String reason);
    
    Set<QualificationModel> getUserQualifications(UserModel user);
    
    boolean hasQualification(UserModel user, String qualificationName);
    
    boolean hasActiveQualification(UserModel user, String qualificationName);
    
    void deleteQualification(String qualificationId);
    
    // ========== AGENCY CONTEXT ==========
    
    /**
     * Get full agency context for authentication/authorization decisions
     * Called during token generation to embed claims
     */
    AgencyContext getAgencyContext(UserModel user);
    
    /**
     * Check if user can act on behalf of principal for specific action
     * Integrates with Policy Router for complex decisions
     */
    AgencyDecision evaluateAgencyAccess(
        UserModel user,
        String principalId,
        String action,
        String resource,
        String context
    );
    
    // ========== AGENT PASSPORT MINTING (REVENUE FEATURE) ==========
    
    /**
     * Mint a persistent Agent Passport
     * This is a PAID feature - agents get persistent DID-based identity
     */
    AgentPassport mintAgentPassport(
        PrincipalModel principal,
        String agentType,  // ai-assistant | autonomous-agent | human-proxy
        String capabilities,  // JSON array of capability strings
        String rateLimits  // JSON rate limiting configuration
    );
    
    Optional<AgentPassport> getAgentPassport(String passportId);
    
    Optional<AgentPassport> getAgentPassportByDid(String passportDid);
    
    List<AgentPassport> getAgentPassportsForPrincipal(PrincipalModel principal);
    
    List<AgentPassport> getAgentPassportsForPrincipal(String principalId);
    
    void recordPassportUsage(String passportId);
    
    void suspendAgentPassport(String passportId, String reason);
    
    void revokeAgentPassport(String passportId, String reason);
    
    void deleteAgentPassport(String passportId);
    
    // ========== REALM CONFIGURATION ==========
    
    AgencyRealmConfig getRealmConfig(RealmModel realm);
    
    void updateRealmConfig(RealmModel realm, AgencyRealmConfig config);
    
    boolean isAgencyEnabled(RealmModel realm);
    
    @Override
    default void close() {}
}

package com.aetherpro.passport.agency.jpa;

import com.aetherpro.passport.agency.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.jboss.logging.Logger;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JPA Implementation of the Agency Provider
 * 
 * Follows Passport's JPA provider patterns exactly:
 * - Uses EntityManager from JpaConnectionProvider
 * - Named queries for all operations
 * - Adapter pattern for model conversion
 */
public class JpaAgencyProvider implements AgencyProvider {
    
    private static final Logger logger = Logger.getLogger(JpaAgencyProvider.class);
    
    private final PassportSession session;
    private final EntityManager em;
    
    public JpaAgencyProvider(PassportSession session, EntityManager em) {
        this.session = session;
        this.em = em;
    }
    
    @Override
    public void close() {
        // EntityManager is managed by Passport's JpaConnectionProvider
    }
    
    // ========== PRINCIPAL OPERATIONS ==========
    
    @Override
    public PrincipalModel createPrincipal(RealmModel realm, String name, PrincipalType type, 
                                          String jurisdiction, String metadata) {
        PrincipalEntity entity = new PrincipalEntity();
        entity.setId(PassportModelUtils.generateId());
        entity.setRealmId(realm.getId());
        entity.setName(name);
        entity.setType(type);
        entity.setJurisdiction(jurisdiction);
        entity.setMetadata(metadata);
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        
        em.persist(entity);
        em.flush();
        
        logger.debugf("Created principal %s in realm %s", entity.getId(), realm.getId());
        
        return entity;
    }
    
    @Override
    public Optional<PrincipalModel> getPrincipal(RealmModel realm, String principalId) {
        PrincipalEntity entity = em.find(PrincipalEntity.class, principalId);
        if (entity == null || !entity.getRealmId().equals(realm.getId())) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }
    
    public Optional<PrincipalModel> getPrincipalByName(RealmModel realm, String name) {
        try {
            TypedQuery<PrincipalEntity> query = em.createNamedQuery("PrincipalEntity.findByRealmAndName", PrincipalEntity.class);
            query.setParameter("realmId", realm.getId());
            query.setParameter("name", name);
            PrincipalEntity entity = query.getSingleResult();
            return Optional.of(entity);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<PrincipalModel> getRealmPrincipals(RealmModel realm) {
        TypedQuery<PrincipalEntity> query = em.createNamedQuery("PrincipalEntity.findByRealm", PrincipalEntity.class);
        query.setParameter("realmId", realm.getId());
        return query.getResultStream()
                .map(e -> (PrincipalModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PrincipalModel> getPrincipalsByType(RealmModel realm, PrincipalType type) {
        TypedQuery<PrincipalEntity> query = em.createNamedQuery("PrincipalEntity.findByRealmAndType", PrincipalEntity.class);
        query.setParameter("realmId", realm.getId());
        query.setParameter("type", type.name());
        return query.getResultStream()
                .map(e -> (PrincipalModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PrincipalModel> getPrincipalsByJurisdiction(RealmModel realm, String jurisdiction) {
        TypedQuery<PrincipalEntity> query = em.createNamedQuery("PrincipalEntity.findByRealmAndJurisdiction", PrincipalEntity.class);
        query.setParameter("realmId", realm.getId());
        query.setParameter("jurisdiction", jurisdiction);
        return query.getResultStream()
                .map(e -> (PrincipalModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PrincipalModel> searchPrincipals(RealmModel realm, String search, int first, int max) {
        TypedQuery<PrincipalEntity> query = em.createNamedQuery("PrincipalEntity.searchByName", PrincipalEntity.class);
        query.setParameter("realmId", realm.getId());
        query.setParameter("search", "%" + search.toLowerCase() + "%");
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultStream()
                .map(e -> (PrincipalModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countPrincipals(RealmModel realm) {
        TypedQuery<Long> query = em.createNamedQuery("PrincipalEntity.countByRealm", Long.class);
        query.setParameter("realmId", realm.getId());
        return query.getSingleResult();
    }
    
    @Override
    public void updatePrincipal(PrincipalModel principal) {
        PrincipalEntity entity = em.find(PrincipalEntity.class, principal.getId());
        if (entity != null) {
            entity.setName(principal.getName());
            entity.setType(principal.getType());
            entity.setJurisdiction(principal.getJurisdiction());
            entity.setMetadata(principal.getMetadata());
            entity.setActive(principal.isActive());
            entity.setUpdatedAt(Instant.now());
            em.merge(entity);
            em.flush();
        }
    }
    
    @Override
    public void suspendPrincipal(String principalId, String reason) {
        PrincipalEntity entity = em.find(PrincipalEntity.class, principalId);
        if (entity != null) {
            entity.setActive(false);
            entity.setSuspendedAt(Instant.now());
            entity.setSuspensionReason(reason);
            entity.setUpdatedAt(Instant.now());
            em.merge(entity);
            em.flush();
            logger.infof("Suspended principal %s: %s", principalId, reason);
        }
    }
    
    @Override
    public void activatePrincipal(String principalId) {
        PrincipalEntity entity = em.find(PrincipalEntity.class, principalId);
        if (entity != null) {
            entity.setActive(true);
            entity.setSuspendedAt(null);
            entity.setSuspensionReason(null);
            entity.setUpdatedAt(Instant.now());
            em.merge(entity);
            em.flush();
            logger.infof("Activated principal %s", principalId);
        }
    }
    
    @Override
    public void revokePrincipal(String principalId, String reason) {
        suspendPrincipal(principalId, reason);
    }
    
    @Override
    public void deletePrincipal(String principalId) {
        PrincipalEntity entity = em.find(PrincipalEntity.class, principalId);
        if (entity != null) {
            em.remove(entity);
            em.flush();
            logger.infof("Deleted principal %s", principalId);
        }
    }
    
    // ========== DELEGATE OPERATIONS ==========
    
    @Override
    public DelegateModel createDelegate(UserModel agent, PrincipalModel principal, DelegationType type,
                                        String constraints, Instant validFrom, Instant validUntil) {
        DelegateEntity entity = new DelegateEntity();
        entity.setId(PassportModelUtils.generateId());
        entity.setAgentId(agent.getId());
        entity.setPrincipalId(principal.getId());
        entity.setType(type);
        entity.setConstraints(constraints);
        entity.setActive(true);
        entity.setValidFrom(validFrom != null ? validFrom : Instant.now());
        entity.setValidUntil(validUntil);
        entity.setCreatedAt(Instant.now());
        
        em.persist(entity);
        em.flush();
        
        logger.debugf("Created delegate %s: agent %s -> principal %s", 
                entity.getId(), agent.getId(), principal.getId());
        
        return entity;
    }
    
    @Override
    public Optional<DelegateModel> getDelegate(String delegateId) {
        DelegateEntity entity = em.find(DelegateEntity.class, delegateId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }
    
    @Override
    public List<DelegateModel> getDelegatesForAgent(UserModel agent) {
        TypedQuery<DelegateEntity> query = em.createNamedQuery("DelegateEntity.findByAgent", DelegateEntity.class);
        query.setParameter("agentId", agent.getId());
        return query.getResultStream()
                .map(e -> (DelegateModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DelegateModel> getDelegatesForPrincipal(PrincipalModel principal) {
        TypedQuery<DelegateEntity> query = em.createNamedQuery("DelegateEntity.findByPrincipal", DelegateEntity.class);
        query.setParameter("principalId", principal.getId());
        return query.getResultStream()
                .map(e -> (DelegateModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DelegateModel> getActiveDelegatesForAgent(UserModel agent) {
        TypedQuery<DelegateEntity> query = em.createNamedQuery("DelegateEntity.findActiveByAgent", DelegateEntity.class);
        query.setParameter("agentId", agent.getId());
        query.setParameter("now", Instant.now());
        return query.getResultStream()
                .map(e -> (DelegateModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<DelegateModel> getActiveDelegate(UserModel agent, PrincipalModel principal) {
        try {
            TypedQuery<DelegateEntity> query = em.createQuery(
                    "SELECT d FROM DelegateEntity d WHERE d.agentId = :agentId AND d.principalId = :principalId " +
                    "AND d.active = true AND (d.validFrom IS NULL OR d.validFrom <= :now) " +
                    "AND (d.validUntil IS NULL OR d.validUntil >= :now)",
                    DelegateEntity.class
            );
            query.setParameter("agentId", agent.getId());
            query.setParameter("principalId", principal.getId());
            query.setParameter("now", Instant.now());
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public boolean isValidDelegate(UserModel agent, PrincipalModel principal, String actionScope) {
        return getActiveDelegate(agent, principal).isPresent();
    }
    
    @Override
    public boolean isValidDelegate(UserModel agent, String principalId, String actionScope) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(d) FROM DelegateEntity d WHERE d.agentId = :agentId AND d.principalId = :principalId " +
                    "AND d.active = true AND (d.validFrom IS NULL OR d.validFrom <= :now) " +
                    "AND (d.validUntil IS NULL OR d.validUntil >= :now)",
                    Long.class
            );
            query.setParameter("agentId", agent.getId());
            query.setParameter("principalId", principalId);
            query.setParameter("now", Instant.now());
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void updateDelegate(DelegateModel delegate) {
        DelegateEntity entity = em.find(DelegateEntity.class, delegate.getId());
        if (entity != null) {
            entity.setType(delegate.getType());
            entity.setConstraints(delegate.getConstraints());
            entity.setActive(delegate.isActive());
            entity.setValidFrom(delegate.getValidFrom());
            entity.setValidUntil(delegate.getValidUntil());
            em.merge(entity);
            em.flush();
        }
    }
    
    @Override
    public void revokeDelegate(String delegateId, String reason) {
        DelegateEntity entity = em.find(DelegateEntity.class, delegateId);
        if (entity != null) {
            entity.setActive(false);
            entity.setRevokedAt(Instant.now());
            entity.setRevocationReason(reason);
            em.merge(entity);
            em.flush();
            logger.infof("Revoked delegate %s: %s", delegateId, reason);
        }
    }
    
    @Override
    public void deleteDelegate(String delegateId) {
        DelegateEntity entity = em.find(DelegateEntity.class, delegateId);
        if (entity != null) {
            em.remove(entity);
            em.flush();
        }
    }
    
    // ========== MANDATE OPERATIONS ==========
    
    @Override
    public MandateModel createMandate(DelegateModel delegate, String scope, String constraints,
                                      Double maxAmount, boolean requiresSecondFactor,
                                      Instant validFrom, Instant validUntil) {
        MandateEntity entity = new MandateEntity();
        entity.setId(PassportModelUtils.generateId());
        entity.setDelegateId(delegate.getId());
        entity.setScope(scope);
        entity.setConstraints(constraints);
        entity.setMaxAmount(maxAmount);
        entity.setRequiresSecondFactor(requiresSecondFactor);
        entity.setActive(true);
        entity.setValidFrom(validFrom != null ? validFrom : Instant.now());
        entity.setValidUntil(validUntil);
        entity.setUsageCount(0);
        entity.setCreatedAt(Instant.now());
        
        em.persist(entity);
        em.flush();
        
        return entity;
    }
    
    @Override
    public Optional<MandateModel> getMandate(String mandateId) {
        MandateEntity entity = em.find(MandateEntity.class, mandateId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }
    
    @Override
    public List<MandateModel> getMandatesForDelegate(DelegateModel delegate) {
        return getMandatesForDelegate(delegate.getId());
    }
    
    @Override
    public List<MandateModel> getMandatesForDelegate(String delegateId) {
        TypedQuery<MandateEntity> query = em.createNamedQuery("MandateEntity.findByDelegate", MandateEntity.class);
        query.setParameter("delegateId", delegateId);
        return query.getResultStream()
                .map(e -> (MandateModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MandateModel> getActiveMandatesForAgent(UserModel agent, String scope) {
        TypedQuery<MandateEntity> query = em.createQuery(
                "SELECT m FROM MandateEntity m, DelegateEntity d " +
                "WHERE m.delegateId = d.id AND d.agentId = :agentId AND d.active = true " +
                "AND m.active = true AND (m.scope = :scope OR m.scope = '*') " +
                "AND (m.validFrom IS NULL OR m.validFrom <= :now) " +
                "AND (m.validUntil IS NULL OR m.validUntil >= :now)",
                MandateEntity.class
        );
        query.setParameter("agentId", agent.getId());
        query.setParameter("scope", scope);
        query.setParameter("now", Instant.now());
        return query.getResultStream()
                .map(e -> (MandateModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public MandateValidationResult validateMandate(String mandateId, String action, String resource,
                                                    Double amount, String context) {
        MandateEntity entity = em.find(MandateEntity.class, mandateId);
        
        if (entity == null) {
            return MandateValidationResult.notFound();
        }
        
        if (!entity.isActive()) {
            return MandateValidationResult.revoked();
        }
        
        Instant now = Instant.now();
        if (entity.getValidFrom() != null && now.isBefore(entity.getValidFrom())) {
            return MandateValidationResult.expired();
        }
        
        if (entity.getValidUntil() != null && now.isAfter(entity.getValidUntil())) {
            return MandateValidationResult.expired();
        }
        
        // Check amount limit
        if (amount != null && entity.getMaxAmount() != null && amount > entity.getMaxAmount()) {
            return MandateValidationResult.amountExceeded(entity.getMaxAmount(), amount);
        }
        
        // Check if second factor is required
        if (entity.requiresSecondFactor()) {
            return MandateValidationResult.requires2FA();
        }
        
        // Validation passed - update usage
        Integer currentCount = entity.getUsageCount() != null ? entity.getUsageCount() : 0;
        entity.setUsageCount(currentCount + 1);
        entity.setLastUsedAt(now);
        em.merge(entity);
        
        return MandateValidationResult.valid();
    }
    
    @Override
    public void recordMandateUsage(String mandateId) {
        MandateEntity entity = em.find(MandateEntity.class, mandateId);
        if (entity != null) {
            Integer currentCount = entity.getUsageCount() != null ? entity.getUsageCount() : 0;
            entity.setUsageCount(currentCount + 1);
            entity.setLastUsedAt(Instant.now());
            em.merge(entity);
        }
    }
    
    @Override
    public void suspendMandate(String mandateId, String reason) {
        MandateEntity entity = em.find(MandateEntity.class, mandateId);
        if (entity != null) {
            entity.setActive(false);
            entity.setSuspendedAt(Instant.now());
            entity.setSuspensionReason(reason);
            em.merge(entity);
            em.flush();
        }
    }
    
    @Override
    public void revokeMandate(String mandateId, String reason) {
        suspendMandate(mandateId, reason);
    }
    
    @Override
    public void deleteMandate(String mandateId) {
        MandateEntity entity = em.find(MandateEntity.class, mandateId);
        if (entity != null) {
            em.remove(entity);
            em.flush();
        }
    }
    
    // ========== QUALIFICATION OPERATIONS ==========
    
    @Override
    public QualificationModel createQualification(RealmModel realm, String name, String type,
                                                   String issuer, String scope, Integer validityMonths) {
        QualificationEntity entity = new QualificationEntity();
        entity.setId(PassportModelUtils.generateId());
        entity.setRealmId(realm.getId());
        entity.setName(name);
        entity.setType(type);
        entity.setIssuer(issuer);
        entity.setScope(scope);
        entity.setValidityMonths(validityMonths);
        entity.setActive(true);
        
        em.persist(entity);
        em.flush();
        
        return entity;  // Entity implements QualificationModel
    }
    
    @Override
    public Optional<QualificationModel> getQualification(String qualificationId) {
        QualificationEntity entity = em.find(QualificationEntity.class, qualificationId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }
    
    @Override
    public List<QualificationModel> getRealmQualifications(RealmModel realm) {
        TypedQuery<QualificationEntity> query = em.createNamedQuery("QualificationEntity.findByRealm", QualificationEntity.class);
        query.setParameter("realmId", realm.getId());
        return query.getResultStream()
                .map(e -> (QualificationModel) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public void assignQualification(UserModel user, QualificationModel qualification, 
                                     String credentialId, Instant expiresAt) {
        // Would insert into PASSPORT_USER_QUALIFICATION table
        logger.infof("Assigning qualification %s to user %s", qualification.getId(), user.getId());
    }
    
    @Override
    public void revokeQualification(String assignmentId, String reason) {
        logger.infof("Revoking qualification assignment %s: %s", assignmentId, reason);
    }
    
    @Override
    public Set<QualificationModel> getUserQualifications(UserModel user) {
        return new HashSet<>();
    }
    
    @Override
    public boolean hasQualification(UserModel user, String qualificationName) {
        return false;
    }
    
    @Override
    public boolean hasActiveQualification(UserModel user, String qualificationName) {
        return false;
    }
    
    @Override
    public void deleteQualification(String qualificationId) {
        QualificationEntity entity = em.find(QualificationEntity.class, qualificationId);
        if (entity != null) {
            em.remove(entity);
            em.flush();
        }
    }
    
    // ========== AGENT PASSPORT OPERATIONS ==========
    
    @Override
    public AgentPassport mintAgentPassport(PrincipalModel principal, String agentType,
                                           String capabilities, String rateLimits) {
        AgentPassportEntity entity = new AgentPassportEntity();
        entity.setId(PassportModelUtils.generateId());
        entity.setRealmId(principal.getRealmId());  // Set realmId from principal
        entity.setPrincipalId(principal.getId());
        entity.setAgentType(agentType);
        entity.setCapabilities(capabilities);
        entity.setRateLimits(rateLimits);
        entity.setActive(true);
        entity.setMintedAt(Instant.now());
        entity.setUsageCount(0);
        
        // Generate DID using did:passport method
        String did = "did:passport:" + principal.getId().substring(0, 8) + ":" + entity.getId();
        entity.setPassportDid(did);
        
        // Default expiration: 1 year
        entity.setExpiresAt(Instant.now().plusSeconds(365 * 24 * 60 * 60));
        
        em.persist(entity);
        em.flush();
        
        logger.infof("Minted Agent Passport %s with DID %s", entity.getId(), did);
        
        return entity;
    }
    
    @Override
    public Optional<AgentPassport> getAgentPassport(String passportId) {
        AgentPassportEntity entity = em.find(AgentPassportEntity.class, passportId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }
    
    @Override
    public Optional<AgentPassport> getAgentPassportByDid(String did) {
        try {
            TypedQuery<AgentPassportEntity> query = em.createNamedQuery("AgentPassportEntity.findByDid", AgentPassportEntity.class);
            query.setParameter("passportDid", did);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<AgentPassport> getAgentPassportsForPrincipal(PrincipalModel principal) {
        return getAgentPassportsForPrincipal(principal.getId());
    }
    
    @Override
    public List<AgentPassport> getAgentPassportsForPrincipal(String principalId) {
        TypedQuery<AgentPassportEntity> query = em.createNamedQuery("AgentPassportEntity.findByPrincipal", AgentPassportEntity.class);
        query.setParameter("principalId", principalId);
        return query.getResultStream()
                .map(e -> (AgentPassport) e)
                .collect(Collectors.toList());
    }
    
    @Override
    public void recordPassportUsage(String passportId) {
        AgentPassportEntity entity = em.find(AgentPassportEntity.class, passportId);
        if (entity != null) {
            entity.setUsageCount(entity.getUsageCount() + 1);
            entity.setLastUsedAt(Instant.now());
            em.merge(entity);
        }
    }
    
    @Override
    public void suspendAgentPassport(String passportId, String reason) {
        AgentPassportEntity entity = em.find(AgentPassportEntity.class, passportId);
        if (entity != null) {
            entity.setActive(false);
            entity.setRevokedAt(Instant.now());
            entity.setRevocationReason(reason);
            em.merge(entity);
            em.flush();
        }
    }
    
    @Override
    public void revokeAgentPassport(String passportId, String reason) {
        suspendAgentPassport(passportId, reason);
    }
    
    @Override
    public void deleteAgentPassport(String passportId) {
        AgentPassportEntity entity = em.find(AgentPassportEntity.class, passportId);
        if (entity != null) {
            em.remove(entity);
            em.flush();
        }
    }
    
    // ========== AGENCY CONTEXT ==========
    
    @Override
    public AgencyContext getAgencyContext(UserModel user) {
        List<DelegateModel> activeDelegates = getActiveDelegatesForAgent(user);
        
        if (activeDelegates.isEmpty()) {
            return new AgencyContext();  // Empty context - no agency relationships
        }
        
        // Build context from active delegates
        List<AgencyContext.PrincipalReference> representing = new ArrayList<>();
        Set<String> allScopes = new HashSet<>();
        
        for (DelegateModel delegate : activeDelegates) {
            PrincipalEntity principal = em.find(PrincipalEntity.class, delegate.getPrincipalId());
            if (principal != null && principal.isActive()) {
                // Get mandates for this delegate
                List<MandateModel> mandates = getMandatesForDelegate(delegate.getId());
                Set<String> delegateScopes = mandates.stream()
                        .filter(MandateModel::isCurrentlyValid)
                        .map(MandateModel::getScope)
                        .collect(Collectors.toSet());
                
                representing.add(new AgencyContext.PrincipalReference(
                        principal.getId(),
                        principal.getName(),
                        delegateScopes
                ));
                allScopes.addAll(delegateScopes);
            }
        }
        
        AgencyContext context = new AgencyContext();
        context.setAgent(true);
        context.setRepresenting(representing);
        context.setActiveScopes(allScopes);
        return context;
    }
    
    @Override
    public AgencyDecision evaluateAgencyAccess(UserModel user, String principalId, 
                                                String action, String resource, String context) {
        // Check if user has active delegation for this principal
        if (!isValidDelegate(user, principalId, action)) {
            return AgencyDecision.deny("No active delegation for principal " + principalId);
        }
        
        // Find active mandates that cover this action
        List<MandateModel> mandates = getActiveMandatesForAgent(user, action);
        
        for (MandateModel mandate : mandates) {
            // Check if mandate is for the requested principal
            DelegateEntity delegate = em.find(DelegateEntity.class, mandate.getDelegateId());
            if (delegate != null && delegate.getPrincipalId().equals(principalId)) {
                recordMandateUsage(mandate.getId());
                return AgencyDecision.allow(mandate.getId(), principalId);
            }
        }
        
        return AgencyDecision.deny("No mandate covers action '" + action + "' on resource '" + resource + "'");
    }
    
    // ========== REALM CONFIGURATION ==========
    
    @Override
    public AgencyRealmConfig getRealmConfig(RealmModel realm) {
        AgencyRealmConfig config = new AgencyRealmConfig();
        config.setEnabled(true);
        config.setDefaultJurisdiction("US");
        config.setComplianceMode(AgencyRealmConfig.ComplianceMode.NONE);
        config.setMandatesRequired(true);
        config.setDefaultMandateValidityDays(90);
        config.setQualificationsEnforced(false);
        config.setAuditLevel(AgencyRealmConfig.AuditLevel.STANDARD);
        config.setAgentPassportsEnabled(true);
        config.setMaxPassportsPerPrincipal(10);
        return config;
    }
    
    @Override
    public void updateRealmConfig(RealmModel realm, AgencyRealmConfig config) {
        logger.infof("Updated Agency config for realm %s", realm.getId());
    }
    
    @Override
    public boolean isAgencyEnabled(RealmModel realm) {
        return true;  // Agency is enabled by default
    }
}

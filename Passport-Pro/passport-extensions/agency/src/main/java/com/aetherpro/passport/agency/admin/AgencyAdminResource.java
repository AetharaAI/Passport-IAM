package com.aetherpro.passport.agency.admin;

import com.aetherpro.passport.agency.*;
import com.aetherpro.passport.agency.admin.representations.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.services.resources.admin.fgap.AdminPermissionEvaluator;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Passport-Pro Agency Admin REST Resource
 * 
 * Provides admin endpoints for managing Agency/LBAC features:
 * - Principals (legal entities)
 * - Delegates (agent-principal relationships)
 * - Mandates (scoped authorizations)
 * - Agent Passports (persistent AI identities)
 * - Realm Configuration
 */
public class AgencyAdminResource {
    
    private static final Logger logger = Logger.getLogger(AgencyAdminResource.class);
    
    private final PassportSession session;
    private final RealmModel realm;
    private final AdminPermissionEvaluator auth;
    
    public AgencyAdminResource(PassportSession session, RealmModel realm, AdminPermissionEvaluator auth) {
        this.session = session;
        this.realm = realm;
        this.auth = auth;
    }
    
    private AgencyProvider getAgencyProvider() {
        return session.getProvider(AgencyProvider.class);
    }
    
    // ========== PRINCIPALS ==========
    
    @GET
    @Path("/principals")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PrincipalRepresentation> getPrincipals(
            @QueryParam("search") String search,
            @QueryParam("type") String type,
            @QueryParam("jurisdiction") String jurisdiction,
            @QueryParam("first") @DefaultValue("0") Integer first,
            @QueryParam("max") @DefaultValue("100") Integer max
    ) {
        auth.realm().requireViewRealm();
        
        AgencyProvider agency = getAgencyProvider();
        List<PrincipalModel> principals;
        
        if (search != null && !search.isEmpty()) {
            principals = agency.searchPrincipals(realm, search, first, max);
        } else if (type != null && !type.isEmpty()) {
            try {
                PrincipalType pt = PrincipalType.valueOf(type.toUpperCase().replace("-", "_"));
                principals = agency.getPrincipalsByType(realm, pt);
            } catch (IllegalArgumentException e) {
                principals = agency.getRealmPrincipals(realm);
            }
        } else if (jurisdiction != null && !jurisdiction.isEmpty()) {
            principals = agency.getPrincipalsByJurisdiction(realm, jurisdiction);
        } else {
            principals = agency.getRealmPrincipals(realm);
        }
        
        return principals.stream()
                .skip(first)
                .limit(max)
                .map(this::toPrincipalRep)
                .collect(Collectors.toList());
    }
    
    @GET
    @Path("/principals/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrincipalCount() {
        auth.realm().requireViewRealm();
        long count = getAgencyProvider().countPrincipals(realm);
        return Response.ok("{\"count\":" + count + "}").build();
    }
    
    @POST
    @Path("/principals")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPrincipal(PrincipalRepresentation rep) {
        auth.realm().requireManageRealm();
        
        if (rep.getName() == null || rep.getName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Name is required\"}")
                    .build();
        }
        
        if (rep.getType() == null || rep.getTypeEnum() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Valid type is required\"}")
                    .build();
        }
        
        AgencyProvider agency = getAgencyProvider();
        AgencyRealmConfig config = agency.getRealmConfig(realm);
        
        PrincipalModel principal = agency.createPrincipal(
                realm,
                rep.getName(),
                rep.getTypeEnum(),
                rep.getJurisdiction() != null ? rep.getJurisdiction() : config.getDefaultJurisdiction(),
                rep.getMetadata()
        );
        
        logger.infof("Created principal %s in realm %s", principal.getId(), realm.getName());
        
        return Response.status(Response.Status.CREATED)
                .entity(toPrincipalRep(principal))
                .build();
    }
    
    @GET
    @Path("/principals/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrincipal(@PathParam("id") String id) {
        auth.realm().requireViewRealm();
        
        return getAgencyProvider().getPrincipal(realm, id)
                .map(p -> Response.ok(toPrincipalRep(p)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
    
    @PUT
    @Path("/principals/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePrincipal(@PathParam("id") String id, PrincipalRepresentation rep) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        return agency.getPrincipal(realm, id)
                .map(principal -> {
                    if (rep.getName() != null) principal.setName(rep.getName());
                    if (rep.getTypeEnum() != null) principal.setType(rep.getTypeEnum());
                    if (rep.getJurisdiction() != null) principal.setJurisdiction(rep.getJurisdiction());
                    if (rep.getMetadata() != null) principal.setMetadata(rep.getMetadata());
                    if (rep.getActive() != null) principal.setActive(rep.getActive());
                    
                    agency.updatePrincipal(principal);
                    logger.infof("Updated principal %s in realm %s", id, realm.getName());
                    
                    return Response.ok(toPrincipalRep(principal)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
    
    @POST
    @Path("/principals/{id}/suspend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response suspendPrincipal(@PathParam("id") String id, SuspendRequest request) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        if (!agency.getPrincipal(realm, id).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        agency.suspendPrincipal(id, request != null ? request.getReason() : "Administrative action");
        logger.infof("Suspended principal %s in realm %s", id, realm.getName());
        
        return Response.ok("{\"status\":\"suspended\"}").build();
    }
    
    @POST
    @Path("/principals/{id}/activate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activatePrincipal(@PathParam("id") String id) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        if (!agency.getPrincipal(realm, id).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        agency.activatePrincipal(id);
        logger.infof("Activated principal %s in realm %s", id, realm.getName());
        
        return Response.ok("{\"status\":\"active\"}").build();
    }
    
    @DELETE
    @Path("/principals/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePrincipal(@PathParam("id") String id) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        if (!agency.getPrincipal(realm, id).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        agency.deletePrincipal(id);
        logger.infof("Deleted principal %s in realm %s", id, realm.getName());
        
        return Response.noContent().build();
    }
    
    // ========== DELEGATES ==========
    
    @GET
    @Path("/principals/{principalId}/delegates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDelegatesForPrincipal(@PathParam("principalId") String principalId) {
        auth.realm().requireViewRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        return agency.getPrincipal(realm, principalId)
                .map(principal -> {
                    List<DelegateRepresentation> delegates = agency.getDelegatesForPrincipal(principal)
                            .stream()
                            .map(this::toDelegateRep)
                            .collect(Collectors.toList());
                    return Response.ok(delegates).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
    
    @GET
    @Path("/users/{userId}/delegates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDelegatesForUser(@PathParam("userId") String userId) {
        auth.realm().requireViewRealm();
        
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"User not found\"}")
                    .build();
        }
        
        AgencyProvider agency = getAgencyProvider();
        List<DelegateRepresentation> delegates = agency.getDelegatesForAgent(user)
                .stream()
                .map(d -> {
                    DelegateRepresentation rep = toDelegateRep(d);
                    rep.setAgentUsername(user.getUsername());
                    rep.setAgentEmail(user.getEmail());
                    return rep;
                })
                .collect(Collectors.toList());
        
        return Response.ok(delegates).build();
    }
    
    @POST
    @Path("/users/{userId}/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDelegate(@PathParam("userId") String userId, DelegateRepresentation rep) {
        auth.realm().requireManageRealm();
        
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"User not found\"}")
                    .build();
        }
        
        if (rep.getPrincipalId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Principal ID is required\"}")
                    .build();
        }
        
        AgencyProvider agency = getAgencyProvider();
        
        return agency.getPrincipal(realm, rep.getPrincipalId())
                .map(principal -> {
                    DelegationType delegationType = rep.getTypeEnum() != null 
                            ? rep.getTypeEnum() 
                            : DelegationType.LIMITED;
                    
                    Instant validFrom = rep.getValidFrom() != null 
                            ? Instant.parse(rep.getValidFrom()) 
                            : Instant.now();
                    
                    Instant validUntil = rep.getValidUntil() != null 
                            ? Instant.parse(rep.getValidUntil()) 
                            : null;
                    
                    DelegateModel delegate = agency.createDelegate(
                            user,
                            principal,
                            delegationType,
                            rep.getConstraints(),
                            validFrom,
                            validUntil
                    );
                    
                    logger.infof("Created delegate %s for user %s -> principal %s in realm %s",
                            delegate.getId(), userId, rep.getPrincipalId(), realm.getName());
                    
                    DelegateRepresentation result = toDelegateRep(delegate);
                    result.setAgentUsername(user.getUsername());
                    result.setPrincipalName(principal.getName());
                    
                    return Response.status(Response.Status.CREATED).entity(result).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Principal not found\"}")
                        .build());
    }
    
    @DELETE
    @Path("/delegates/{delegateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeDelegate(
            @PathParam("delegateId") String delegateId,
            @QueryParam("reason") String reason
    ) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        if (!agency.getDelegate(delegateId).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        agency.revokeDelegate(delegateId, reason != null ? reason : "Administrative revocation");
        logger.infof("Revoked delegate %s in realm %s", delegateId, realm.getName());
        
        return Response.ok("{\"status\":\"revoked\"}").build();
    }
    
    // ========== MANDATES ==========
    
    @GET
    @Path("/delegates/{delegateId}/mandates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMandatesForDelegate(@PathParam("delegateId") String delegateId) {
        auth.realm().requireViewRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        List<MandateRepresentation> mandates = agency.getMandatesForDelegate(delegateId)
                .stream()
                .map(this::toMandateRep)
                .collect(Collectors.toList());
        
        return Response.ok(mandates).build();
    }
    
    @POST
    @Path("/delegates/{delegateId}/mandates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMandate(@PathParam("delegateId") String delegateId, MandateRepresentation rep) {
        auth.realm().requireManageRealm();
        
        if (rep.getScope() == null || rep.getScope().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Scope is required\"}")
                    .build();
        }
        
        AgencyProvider agency = getAgencyProvider();
        
        return agency.getDelegate(delegateId)
                .map(delegate -> {
                    Instant validFrom = rep.getValidFrom() != null 
                            ? Instant.parse(rep.getValidFrom()) 
                            : Instant.now();
                    
                    Instant validUntil = rep.getValidUntil() != null 
                            ? Instant.parse(rep.getValidUntil()) 
                            : null;
                    
                    MandateModel mandate = agency.createMandate(
                            delegate,
                            rep.getScope(),
                            rep.getConstraints(),
                            rep.getMaxAmount(),
                            rep.getRequiresSecondFactor() != null && rep.getRequiresSecondFactor(),
                            validFrom,
                            validUntil
                    );
                    
                    logger.infof("Created mandate %s for delegate %s in realm %s",
                            mandate.getId(), delegateId, realm.getName());
                    
                    return Response.status(Response.Status.CREATED)
                            .entity(toMandateRep(mandate))
                            .build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Delegate not found\"}")
                        .build());
    }
    
    @POST
    @Path("/mandates/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateMandate(MandateValidationRequest request) {
        auth.realm().requireViewRealm();
        
        if (request.getMandateId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Mandate ID is required\"}")
                    .build();
        }
        
        AgencyProvider agency = getAgencyProvider();
        MandateValidationResult result = agency.validateMandate(
                request.getMandateId(),
                request.getAction(),
                request.getResource(),
                request.getAmount(),
                request.getContext()
        );
        
        return Response.ok(result).build();
    }
    
    @POST
    @Path("/mandates/{mandateId}/suspend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response suspendMandate(@PathParam("mandateId") String mandateId, SuspendRequest request) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        if (!agency.getMandate(mandateId).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        agency.suspendMandate(mandateId, request != null ? request.getReason() : "Administrative action");
        logger.infof("Suspended mandate %s in realm %s", mandateId, realm.getName());
        
        return Response.ok("{\"status\":\"suspended\"}").build();
    }
    
    @DELETE
    @Path("/mandates/{mandateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeMandate(
            @PathParam("mandateId") String mandateId,
            @QueryParam("reason") String reason
    ) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        if (!agency.getMandate(mandateId).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        agency.revokeMandate(mandateId, reason != null ? reason : "Administrative revocation");
        logger.infof("Revoked mandate %s in realm %s", mandateId, realm.getName());
        
        return Response.ok("{\"status\":\"revoked\"}").build();
    }
    
    // ========== AGENT PASSPORTS ==========
    
    @GET
    @Path("/principals/{principalId}/passports")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPassportsForPrincipal(@PathParam("principalId") String principalId) {
        auth.realm().requireViewRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        List<AgentPassportRepresentation> passports = agency.getAgentPassportsForPrincipal(principalId)
                .stream()
                .map(this::toPassportRep)
                .collect(Collectors.toList());
        
        return Response.ok(passports).build();
    }
    
    @POST
    @Path("/principals/{principalId}/passports")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mintAgentPassport(
            @PathParam("principalId") String principalId,
            AgentPassportRepresentation rep
    ) {
        auth.realm().requireManageRealm();
        
        if (rep.getAgentType() == null || rep.getAgentType().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Agent type is required\"}")
                    .build();
        }
        
        AgencyProvider agency = getAgencyProvider();
        AgencyRealmConfig config = agency.getRealmConfig(realm);
        
        if (!config.isAgentPassportsEnabled()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Agent Passports are not enabled for this realm\"}")
                    .build();
        }
        
        return agency.getPrincipal(realm, principalId)
                .map(principal -> {
                    // Check passport limit
                    long currentCount = agency.getAgentPassportsForPrincipal(principalId)
                            .stream()
                            .filter(AgentPassport::isActive)
                            .count();
                    
                    if (currentCount >= config.getMaxPassportsPerPrincipal()) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity("{\"error\":\"Maximum passport limit reached for this principal\"}")
                                .build();
                    }
                    
                    AgentPassport passport = agency.mintAgentPassport(
                            principal,
                            rep.getAgentType(),
                            rep.getCapabilities(),
                            rep.getRateLimits()
                    );
                    
                    logger.infof("Minted Agent Passport %s for principal %s in realm %s",
                            passport.getPassportDid(), principalId, realm.getName());
                    
                    AgentPassportRepresentation result = toPassportRep(passport);
                    result.setPrincipalName(principal.getName());
                    
                    return Response.status(Response.Status.CREATED).entity(result).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Principal not found\"}")
                        .build());
    }
    
    @GET
    @Path("/passports/{passportId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPassport(@PathParam("passportId") String passportId) {
        auth.realm().requireViewRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        return agency.getAgentPassport(passportId)
                .map(p -> Response.ok(toPassportRep(p)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
    
    @GET
    @Path("/passports/by-did/{did}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPassportByDid(@PathParam("did") String did) {
        auth.realm().requireViewRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        return agency.getAgentPassportByDid(did)
                .map(p -> Response.ok(toPassportRep(p)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
    
    @POST
    @Path("/passports/{passportId}/revoke")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokePassport(@PathParam("passportId") String passportId, SuspendRequest request) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        
        if (!agency.getAgentPassport(passportId).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        agency.revokeAgentPassport(passportId, request != null ? request.getReason() : "Administrative revocation");
        logger.infof("Revoked Agent Passport %s in realm %s", passportId, realm.getName());
        
        return Response.ok("{\"status\":\"revoked\"}").build();
    }
    
    // ========== REALM CONFIGURATION ==========
    
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgencyConfig() {
        auth.realm().requireViewRealm();
        
        AgencyProvider agency = getAgencyProvider();
        AgencyRealmConfig config = agency.getRealmConfig(realm);
        
        AgencyConfigRepresentation rep = new AgencyConfigRepresentation();
        rep.setEnabled(config.isEnabled());
        rep.setDefaultJurisdiction(config.getDefaultJurisdiction());
        rep.setComplianceMode(config.getComplianceMode().name());
        rep.setMandatesRequired(config.isMandatesRequired());
        rep.setDefaultMandateValidityDays(config.getDefaultMandateValidityDays());
        rep.setQualificationsEnforced(config.isQualificationsEnforced());
        rep.setAuditLevel(config.getAuditLevel().name());
        rep.setAgentPassportsEnabled(config.isAgentPassportsEnabled());
        rep.setMaxPassportsPerPrincipal(config.getMaxPassportsPerPrincipal());
        rep.setCustomPolicyScript(config.getCustomPolicyScript());
        
        // Add statistics
        rep.setPrincipalCount(agency.countPrincipals(realm));
        
        return Response.ok(rep).build();
    }
    
    @PUT
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAgencyConfig(AgencyConfigRepresentation rep) {
        auth.realm().requireManageRealm();
        
        AgencyProvider agency = getAgencyProvider();
        AgencyRealmConfig config = agency.getRealmConfig(realm);
        
        if (rep.getEnabled() != null) config.setEnabled(rep.getEnabled());
        if (rep.getDefaultJurisdiction() != null) config.setDefaultJurisdiction(rep.getDefaultJurisdiction());
        if (rep.getComplianceMode() != null) {
            try {
                config.setComplianceMode(AgencyRealmConfig.ComplianceMode.valueOf(rep.getComplianceMode()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (rep.getMandatesRequired() != null) config.setMandatesRequired(rep.getMandatesRequired());
        if (rep.getDefaultMandateValidityDays() != null) config.setDefaultMandateValidityDays(rep.getDefaultMandateValidityDays());
        if (rep.getQualificationsEnforced() != null) config.setQualificationsEnforced(rep.getQualificationsEnforced());
        if (rep.getAuditLevel() != null) {
            try {
                config.setAuditLevel(AgencyRealmConfig.AuditLevel.valueOf(rep.getAuditLevel()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (rep.getAgentPassportsEnabled() != null) config.setAgentPassportsEnabled(rep.getAgentPassportsEnabled());
        if (rep.getMaxPassportsPerPrincipal() != null) config.setMaxPassportsPerPrincipal(rep.getMaxPassportsPerPrincipal());
        if (rep.getCustomPolicyScript() != null) config.setCustomPolicyScript(rep.getCustomPolicyScript());
        
        agency.updateRealmConfig(realm, config);
        logger.infof("Updated Agency config for realm %s", realm.getName());
        
        return getAgencyConfig();
    }
    
    // ========== HELPER METHODS ==========
    
    private PrincipalRepresentation toPrincipalRep(PrincipalModel model) {
        PrincipalRepresentation rep = new PrincipalRepresentation();
        rep.setId(model.getId());
        rep.setName(model.getName());
        rep.setType(model.getType().name().toLowerCase().replace("_", "-"));
        rep.setJurisdiction(model.getJurisdiction());
        rep.setMetadata(model.getMetadata());
        rep.setActive(model.isActive());
        rep.setCreatedAt(model.getCreatedAt());
        rep.setUpdatedAt(model.getUpdatedAt());
        rep.setSuspendedAt(model.getSuspendedAt());
        rep.setSuspensionReason(model.getSuspensionReason());
        return rep;
    }
    
    private DelegateRepresentation toDelegateRep(DelegateModel model) {
        DelegateRepresentation rep = new DelegateRepresentation();
        rep.setId(model.getId());
        rep.setAgentId(model.getAgentId());
        rep.setPrincipalId(model.getPrincipalId());
        rep.setType(model.getType().name().toLowerCase());
        rep.setConstraints(model.getConstraints());
        rep.setActive(model.isActive());
        rep.setValidFrom(model.getValidFrom());
        rep.setValidUntil(model.getValidUntil());
        rep.setCreatedAt(model.getCreatedAt());
        rep.setRevokedAt(model.getRevokedAt());
        rep.setRevocationReason(model.getRevocationReason());
        rep.setIsCurrentlyValid(model.isCurrentlyValid());
        return rep;
    }
    
    private MandateRepresentation toMandateRep(MandateModel model) {
        MandateRepresentation rep = new MandateRepresentation();
        rep.setId(model.getId());
        rep.setDelegateId(model.getDelegateId());
        rep.setScope(model.getScope());
        rep.setConstraints(model.getConstraints());
        rep.setMaxAmount(model.getMaxAmount());
        rep.setRequiresSecondFactor(model.requiresSecondFactor());
        rep.setActive(model.isActive());
        rep.setValidFrom(model.getValidFrom());
        rep.setValidUntil(model.getValidUntil());
        rep.setUsageCount(model.getUsageCount());
        rep.setLastUsedAt(model.getLastUsedAt());
        rep.setCreatedAt(model.getCreatedAt());
        rep.setSuspendedAt(model.getSuspendedAt());
        rep.setSuspensionReason(model.getSuspensionReason());
        rep.setIsCurrentlyValid(model.isCurrentlyValid());
        return rep;
    }
    
    private AgentPassportRepresentation toPassportRep(AgentPassport model) {
        AgentPassportRepresentation rep = new AgentPassportRepresentation();
        rep.setId(model.getId());
        rep.setPassportDid(model.getPassportDid());
        rep.setPrincipalId(model.getPrincipalId());
        rep.setAgentType(model.getAgentType());
        rep.setCapabilities(model.getCapabilities());
        rep.setCapabilitiesList(model.getCapabilitiesList());
        rep.setRateLimits(model.getRateLimits());
        rep.setActive(model.isActive());
        rep.setMintedAt(model.getMintedAt());
        rep.setExpiresAt(model.getExpiresAt());
        rep.setRevokedAt(model.getRevokedAt());
        rep.setRevocationReason(model.getRevocationReason());
        rep.setLastUsedAt(model.getLastUsedAt());
        rep.setUsageCount(model.getUsageCount());
        rep.setIsCurrentlyValid(model.isCurrentlyValid());
        return rep;
    }
    
    // ========== HELPER DTOs ==========
    
    public static class SuspendRequest {
        private String reason;
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class MandateValidationRequest {
        private String mandateId;
        private String action;
        private String resource;
        private Double amount;
        private String context;
        
        public String getMandateId() { return mandateId; }
        public void setMandateId(String mandateId) { this.mandateId = mandateId; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
}

package com.aetherpro.passport.agency.admin;

import com.aetherpro.passport.agency.*;
import com.aetherpro.passport.agency.admin.representations.*;
import com.aetherpro.passport.agency.crypto.SignedAction;
import com.aetherpro.passport.agency.jpa.AgentPassportEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.services.resources.admin.fgap.AdminPermissionEvaluator;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long APIS_PASSPORT_TTL_SECONDS = 7_776_000L;

    private final PassportSession session;
    private final RealmModel realm;
    private final AdminPermissionEvaluator auth;

    public AgencyAdminResource(PassportSession session, RealmModel realm, AdminPermissionEvaluator auth) {
        this.session = session;
        this.realm = realm;
        this.auth = auth;
    }

    private AgencyProvider getAgencyProvider() {
        AgencyProvider provider = session.getProvider(AgencyProvider.class);
        if (provider == null) {
            logger.error(
                    "AgencyProvider is NULL - SPI not loaded. Check META-INF/services files and Agency JAR placement.");
            throw new jakarta.ws.rs.WebApplicationException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\":\"Agency provider not available. Extension may not be properly installed.\"}")
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .build());
        }
        return provider;
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
            @QueryParam("max") @DefaultValue("100") Integer max) {
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
                rep.getMetadata());

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
                    if (rep.getName() != null)
                        principal.setName(rep.getName());
                    if (rep.getTypeEnum() != null)
                        principal.setType(rep.getTypeEnum());
                    if (rep.getJurisdiction() != null)
                        principal.setJurisdiction(rep.getJurisdiction());
                    if (rep.getMetadata() != null)
                        principal.setMetadata(rep.getMetadata());
                    if (rep.getActive() != null)
                        principal.setActive(rep.getActive());

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
                            validUntil);

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

    @POST
    @Path("/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDelegateFromAgencyForm(DelegateFormRequest request) {
        auth.realm().requireManageRealm();

        if (request == null || isBlank(request.delegateName)) {
            return jsonError(Response.Status.BAD_REQUEST, "Delegate name is required");
        }

        if (isBlank(request.principalId)) {
            return jsonError(Response.Status.BAD_REQUEST, "Principal ID is required");
        }

        AgencyProvider agency = getAgencyProvider();

        return agency.getPrincipal(realm, request.principalId)
                .map(principal -> {
                    UserModel user = session.users().getUserByUsername(realm, request.delegateName);
                    if (user == null) {
                        user = session.users().addUser(realm, null, request.delegateName, true, false);
                        user.setEnabled(true);
                    }

                    Instant validFrom = Instant.now();
                    Instant validUntil = validFrom.plusSeconds((long) Math.max(request.expiryDays, 1) * 24 * 60 * 60);

                    DelegateModel delegate = agency.createDelegate(
                            user,
                            principal,
                            DelegationType.LIMITED,
                            request.scope,
                            validFrom,
                            validUntil);

                    DelegateRepresentation result = toDelegateRep(delegate);
                    result.setAgentUsername(user.getUsername());
                    result.setAgentEmail(user.getEmail());
                    result.setPrincipalName(principal.getName());
                    return Response.status(Response.Status.CREATED).entity(result).build();
                })
                .orElse(jsonError(Response.Status.NOT_FOUND, "Principal not found"));
    }

    @DELETE
    @Path("/delegates/{delegateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeDelegate(
            @PathParam("delegateId") String delegateId,
            @QueryParam("reason") String reason) {
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
                            validUntil);

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
                request.getContext());

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
            @QueryParam("reason") String reason) {
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
            AgentPassportRepresentation rep) {
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
                            rep.getRateLimits());

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

    @POST
    @Path("/passports/mint")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mintApisV2AgentPassport(AgentPassportRepresentation rep) {
        auth.realm().requireManageRealm();

        if (rep == null) {
            return jsonError(Response.Status.BAD_REQUEST, "Request body is required");
        }
        if (isBlank(rep.getAgentName())) {
            return jsonError(Response.Status.BAD_REQUEST, "Agent name is required");
        }
        if (isBlank(rep.getPrincipalId())) {
            return jsonError(Response.Status.BAD_REQUEST, "Principal ID is required");
        }
        if (isBlank(rep.getTier())) {
            return jsonError(Response.Status.BAD_REQUEST, "Tier is required");
        }
        if (isBlank(rep.getPublicKeyPem())) {
            return jsonError(Response.Status.BAD_REQUEST, "Public key PEM is required");
        }

        AgencyProvider agency = getAgencyProvider();
        AgencyRealmConfig config = agency.getRealmConfig(realm);

        if (!config.isAgentPassportsEnabled()) {
            return jsonError(Response.Status.FORBIDDEN, "Agent Passports are not enabled for this realm");
        }
        if (config.isMandatesRequired() && (rep.getMandate() == null || rep.getMandate().isEmpty())) {
            return jsonError(Response.Status.BAD_REQUEST, "Mandate is required for this realm");
        }

        return agency.getPrincipal(realm, rep.getPrincipalId())
                .map(principal -> mintApisV2AgentPassportForPrincipal(agency, config, principal, rep))
                .orElse(jsonError(Response.Status.NOT_FOUND, "Principal not found"));
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

    // ========== CRYPTO & SIGNATURE CHAIN ==========

    @POST
    @Path("/keys/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateKeypair(KeypairRepresentation rep) {
        auth.realm().requireManageRealm();

        if (rep.getEntityType() == null || rep.getEntityId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Entity type and ID are required\"}")
                    .build();
        }

        AgencyProvider agency = getAgencyProvider();
        agency.generateKeypair(realm, rep.getEntityType(), rep.getEntityId());

        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/keys/{kid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeypair(@PathParam("kid") String kid) {
        auth.realm().requireViewRealm();

        return getAgencyProvider().getKeypair(kid)
                .<Response>map(k -> Response.ok(toKeypairRep(k)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/actions/sign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signAction(SignedAction request) {
        auth.realm().requireManageRealm();

        if (request.getDelegateKid() == null || request.getActionData() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Delegate KID and action data are required\"}")
                    .build();
        }

        AgencyProvider agency = getAgencyProvider();
        String signature = agency.signAction(realm, request.getDelegateKid(), request.getActionData());

        return Response.ok("{\"signature\":\"" + signature + "\"}").build();
    }

    @POST
    @Path("/actions/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyAction(String signedActionJson) {
        auth.realm().requireViewRealm();

        boolean valid = getAgencyProvider().verifySignatureChain(signedActionJson);
        return Response.ok("{\"valid\":" + valid + "}").build();
    }

    // ========== PUBLIC KEY DISCOVERY (JWKS) ==========

    /**
     * JSON Web Key Set endpoint - PUBLIC (no auth required)
     * Returns all active public keys for cryptographic verification
     */
    @GET
    @Path("/jwks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJwks() {
        // NO AUTH REQUIRED - public endpoint for key discovery
        AgencyProvider agency = getAgencyProvider();

        List<java.util.Map<String, Object>> keys = new java.util.ArrayList<>();

        try {
            // Get issuer (realm) keypair
            List<com.aetherpro.passport.agency.jpa.AgencyKeypairEntity> issuerKeys = agency.getKeypairs(realm, "ISSUER",
                    realm.getId());

            for (com.aetherpro.passport.agency.jpa.AgencyKeypairEntity keypair : issuerKeys) {
                if ("ACTIVE".equals(keypair.getStatus())) {
                    keys.add(buildJwk(keypair, "sig"));
                }
            }

            logger.debugf("JWKS: Returning %d active keys for realm %s", keys.size(), realm.getName());

        } catch (Exception e) {
            logger.error("Error fetching JWKS for realm " + realm.getName(), e);
        }

        java.util.Map<String, Object> jwks = new java.util.HashMap<>();
        jwks.put("keys", keys);
        return Response.ok(jwks).build();
    }

    /**
     * Build a JWK (JSON Web Key) object for Ed25519 public key
     */
    private java.util.Map<String, Object> buildJwk(com.aetherpro.passport.agency.jpa.AgencyKeypairEntity keypair,
            String use) {
        java.util.Map<String, Object> jwk = new java.util.HashMap<>();
        jwk.put("kty", "OKP"); // Octet Key Pair (for EdDSA)
        jwk.put("crv", "Ed25519");
        jwk.put("kid", keypair.getKid());
        jwk.put("alg", "EdDSA");
        jwk.put("use", use);

        // Public key (base64url encoded)
        String publicKeyBase64 = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(keypair.getPublicKeyBytes());
        jwk.put("x", publicKeyBase64);

        jwk.put("entity_type", keypair.getEntityType());
        jwk.put("entity_id", keypair.getEntityId());

        return jwk;
    }

    // ========== REALM CONFIGURATION ==========

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgencyConfig() {
        logger.infof(">>> AGENCY getAgencyConfig() called for realm: %s", realm.getName());
        auth.realm().requireViewRealm();
        logger.info(">>> AGENCY auth check passed");

        try {
            AgencyProvider agency = null;
            try {
                agency = getAgencyProvider();
                logger.infof(">>> AGENCY provider loaded: %s", agency.getClass().getName());
            } catch (jakarta.ws.rs.WebApplicationException e) {
                // SPI not loaded - return a default disabled config so the UI still renders
                logger.warnf(">>> AGENCY provider NOT available for realm %s - returning fallback. Cause: %s",
                        realm.getName(), e.getMessage());
                AgencyConfigRepresentation fallback = new AgencyConfigRepresentation();
                fallback.setEnabled(false);
                fallback.setDefaultJurisdiction("US-XX");
                fallback.setComplianceMode("NONE");
                fallback.setMandatesRequired(true);
                fallback.setDefaultMandateValidityDays(365);
                fallback.setQualificationsEnforced(false);
                fallback.setAuditLevel("STANDARD");
                fallback.setAgentPassportsEnabled(false);
                fallback.setMaxPassportsPerPrincipal(10);
                fallback.setPrincipalCount(0L);
                fallback.setDelegateCount(0L);
                fallback.setMandateCount(0L);
                fallback.setPassportCount(0L);
                logger.info(">>> AGENCY returning fallback config (200 OK)");
                return Response.ok(fallback).build();
            }

            logger.info(">>> AGENCY calling getRealmConfig()...");
            AgencyRealmConfig config = agency.getRealmConfig(realm);
            logger.infof(">>> AGENCY getRealmConfig() returned: enabled=%s", config.isEnabled());

            AgencyConfigRepresentation rep = new AgencyConfigRepresentation();
            rep.setEnabled(config.isEnabled());
            rep.setDefaultJurisdiction(config.getDefaultJurisdiction());
            rep.setComplianceMode(config.getComplianceMode() != null ? config.getComplianceMode().name() : "NONE");
            rep.setMandatesRequired(config.isMandatesRequired());
            rep.setDefaultMandateValidityDays(config.getDefaultMandateValidityDays());
            rep.setQualificationsEnforced(config.isQualificationsEnforced());
            rep.setAuditLevel(config.getAuditLevel() != null ? config.getAuditLevel().name() : "STANDARD");
            rep.setAgentPassportsEnabled(config.isAgentPassportsEnabled());
            rep.setMaxPassportsPerPrincipal(config.getMaxPassportsPerPrincipal());
            rep.setCustomPolicyScript(config.getCustomPolicyScript());

            // Add statistics (wrapped in try-catch for resilience)
            try {
                rep.setPrincipalCount(agency.countPrincipals(realm));
            } catch (Exception e) {
                logger.warn(">>> AGENCY Could not count principals: " + e.getMessage());
                rep.setPrincipalCount(0L);
            }

            logger.info(">>> AGENCY returning config (200 OK)");
            return Response.ok(rep).build();
        } catch (Exception e) {
            logger.errorf(e, ">>> AGENCY ERROR in getAgencyConfig for realm %s: %s", realm.getName(), e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
    }

    @PUT
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAgencyConfig(AgencyConfigRepresentation rep) {
        auth.realm().requireManageRealm();

        AgencyProvider agency = getAgencyProvider();
        AgencyRealmConfig config = agency.getRealmConfig(realm);

        if (rep.getEnabled() != null)
            config.setEnabled(rep.getEnabled());
        if (rep.getDefaultJurisdiction() != null)
            config.setDefaultJurisdiction(rep.getDefaultJurisdiction());
        if (rep.getComplianceMode() != null) {
            try {
                config.setComplianceMode(AgencyRealmConfig.ComplianceMode.valueOf(rep.getComplianceMode()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (rep.getMandatesRequired() != null)
            config.setMandatesRequired(rep.getMandatesRequired());
        if (rep.getDefaultMandateValidityDays() != null)
            config.setDefaultMandateValidityDays(rep.getDefaultMandateValidityDays());
        if (rep.getQualificationsEnforced() != null)
            config.setQualificationsEnforced(rep.getQualificationsEnforced());
        if (rep.getAuditLevel() != null) {
            try {
                config.setAuditLevel(AgencyRealmConfig.AuditLevel.valueOf(rep.getAuditLevel()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (rep.getAgentPassportsEnabled() != null)
            config.setAgentPassportsEnabled(rep.getAgentPassportsEnabled());
        if (rep.getMaxPassportsPerPrincipal() != null)
            config.setMaxPassportsPerPrincipal(rep.getMaxPassportsPerPrincipal());
        if (rep.getCustomPolicyScript() != null)
            config.setCustomPolicyScript(rep.getCustomPolicyScript());

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

    private KeypairRepresentation toKeypairRep(com.aetherpro.passport.agency.jpa.AgencyKeypairEntity model) {
        KeypairRepresentation rep = new KeypairRepresentation();
        rep.setId(model.getId());
        rep.setRealmId(model.getRealmId());
        rep.setEntityType(model.getEntityType());
        rep.setEntityId(model.getEntityId());
        rep.setKid(model.getKid());
        rep.setAlgorithm(model.getAlgorithm());
        rep.setStatus(model.getStatus());
        rep.setCreatedAt(model.getCreatedAt());
        rep.setExpiresAt(model.getExpiresAt());
        rep.setPublicKeyBase64(java.util.Base64.getEncoder().encodeToString(model.getPublicKeyBytes()));
        return rep;
    }

    private Response mintApisV2AgentPassportForPrincipal(
            AgencyProvider agency,
            AgencyRealmConfig config,
            PrincipalModel principal,
            AgentPassportRepresentation rep) {
        try {
            long currentCount = agency.getAgentPassportsForPrincipal(principal.getId())
                    .stream()
                    .filter(AgentPassport::isActive)
                    .count();

            if (currentCount >= config.getMaxPassportsPerPrincipal()) {
                return jsonError(Response.Status.FORBIDDEN, "Maximum passport limit reached for this principal");
            }

            byte[] publicKeyDer = decodePem(rep.getPublicKeyPem());
            String fingerprint = "sha256/" + base64Url(MessageDigest.getInstance("SHA-256").digest(publicKeyDer));
            long iat = Instant.now().getEpochSecond();
            long exp = iat + APIS_PASSPORT_TTL_SECONDS;
            String jti = UUID.randomUUID().toString();
            String did = "did:passport:" + realm.getName() + ":" + rep.getAgentName();

            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("did", did);
            claims.put("principal_id", principal.getId());
            claims.put("machine_passport_id", emptyToNull(rep.getMachinePassportId()));
            claims.put("tier", rep.getTier());
            claims.put("mandate", rep.getMandate());
            claims.put("public_key_fingerprint", fingerprint);
            claims.put("iat", iat);
            claims.put("exp", exp);
            claims.put("revocation_nonce", 0);
            claims.put("jti", jti);

            String jwt = signApisJwt(claims);

            AgentPassport passport = agency.mintAgentPassport(
                    principal,
                    rep.getTier(),
                    MAPPER.writeValueAsString(claims),
                    fingerprint);

            if (passport instanceof AgentPassportEntity entity) {
                entity.setPassportDid(did);
                entity.setExpiresAt(Instant.ofEpochSecond(exp));
                entity.setIssuerSignature(jwt);
                entity.setIssuerKid("apis-es256-env");
            }

            ensureCollabClient();
            publishCloudflareDnsRecordIfConfigured(rep.getTier(), rep.getAgentName(), fingerprint, jti, exp);

            AgentPassportRepresentation result = toPassportRep(passport);
            result.setPassportDid(did);
            result.setPrincipalName(principal.getName());
            result.setAgentName(rep.getAgentName());
            result.setTier(rep.getTier());
            result.setMachinePassportId(rep.getMachinePassportId());
            result.setMandate(rep.getMandate());
            result.setPublicKeyFingerprint(fingerprint);
            result.setJti(jti);
            result.setJwt(jwt);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (BadRequestException e) {
            return jsonError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to mint APIS v2.0 Agent Passport", e);
            return jsonError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String signApisJwt(Map<String, Object> claims) throws Exception {
        PrivateKey privateKey = loadApisIssuerPrivateKey();
        if (!(privateKey instanceof ECPrivateKey)) {
            throw new IllegalStateException("APIS issuer private key must be an EC P-256 private key");
        }

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "ES256");
        header.put("typ", "JWT");

        String signingInput = base64Url(MAPPER.writeValueAsBytes(header))
                + "."
                + base64Url(MAPPER.writeValueAsBytes(claims));

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        return signingInput + "." + base64Url(derToJose(signature.sign(), 64));
    }

    private PrivateKey loadApisIssuerPrivateKey() throws Exception {
        String keyPath = System.getenv("APIS_REALM_ISSUER_PRIVATE_KEY_PATH");
        if (isBlank(keyPath)) {
            throw new IllegalStateException("APIS_REALM_ISSUER_PRIVATE_KEY_PATH is required to mint APIS passports");
        }

        byte[] der = decodePem(Files.readString(java.nio.file.Path.of(keyPath)));
        return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private byte[] decodePem(String pem) {
        if (isBlank(pem)) {
            throw new BadRequestException("PEM content is required");
        }

        String base64 = pem
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");

        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid PEM content");
        }
    }

    private byte[] derToJose(byte[] derSignature, int outputLength) {
        int offset = derSignature[1] > 0 ? 2 : 3;
        int rLength = derSignature[offset + 1];
        byte[] r = Arrays.copyOfRange(derSignature, offset + 2, offset + 2 + rLength);

        int sOffset = offset + 2 + rLength;
        int sLength = derSignature[sOffset + 1];
        byte[] s = Arrays.copyOfRange(derSignature, sOffset + 2, sOffset + 2 + sLength);

        int rawPartLength = outputLength / 2;
        byte[] jose = new byte[outputLength];
        copyUnsigned(r, jose, 0, rawPartLength);
        copyUnsigned(s, jose, rawPartLength, rawPartLength);
        return jose;
    }

    private void copyUnsigned(byte[] source, byte[] destination, int destinationOffset, int length) {
        byte[] unsigned = new BigInteger(1, source).toByteArray();
        int sourceOffset = Math.max(0, unsigned.length - length);
        int copyLength = Math.min(unsigned.length, length);
        System.arraycopy(unsigned, sourceOffset, destination, destinationOffset + length - copyLength, copyLength);
    }

    private void publishCloudflareDnsRecordIfConfigured(String tier, String agentName, String fingerprint, String jti, long exp) {
        if (!"dns".equalsIgnoreCase(tier)) {
            return;
        }

        String token = System.getenv("CLOUDFLARE_API_TOKEN");
        String zoneId = System.getenv("CLOUDFLARE_ZONE_ID");
        if (isBlank(token) || isBlank(zoneId)) {
            logger.info("Skipping APIS DNS publication because Cloudflare environment variables are not configured");
            return;
        }

        String realmDomain = firstNonBlank(realm.getAttribute("realmDomain"), realm.getAttribute("agency.realmDomain"));
        if (isBlank(realmDomain)) {
            logger.warnf("Skipping APIS DNS publication for realm %s because no realmDomain attribute is configured", realm.getName());
            return;
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("type", "TXT");
            body.put("name", "_apis." + agentName + "." + realmDomain);
            body.put("content", "v=APIS2; eid=" + jti + "; pubkey=" + fingerprint + "; tier=dns; exp=" + exp);
            body.put("ttl", 300);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cloudflare.com/client/v4/zones/" + zoneId + "/dns_records"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warnf("Cloudflare APIS DNS publication failed with HTTP %d: %s", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            logger.warn("Cloudflare APIS DNS publication failed", e);
        }
    }

    private void ensureCollabClient() {
        ClientModel client = realm.getClientByClientId("collab-server");
        if (client != null) {
            return;
        }

        client = realm.addClient(PassportModelUtils.generateId(), "collab-server");
        client.setEnabled(true);
        client.setProtocol("openid-connect");
        client.setPublicClient(false);
        client.setBearerOnly(false);
        client.setServiceAccountsEnabled(true);
        client.setDirectAccessGrantsEnabled(false);
        logger.infof("Registered collab-server client for realm %s", realm.getName());
    }

    private Response jsonError(Response.Status status, String message) {
        try {
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(MAPPER.writeValueAsString(Map.of("error", message != null ? message : status.getReasonPhrase())))
                    .build();
        } catch (JsonProcessingException e) {
            return Response.status(status).entity("{\"error\":\"" + status.getReasonPhrase() + "\"}").build();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // ========== HELPER DTOs ==========

    public static class DelegateFormRequest {
        public String delegateName;
        public String principalId;
        public String scope;
        public int expiryDays = 365;
    }

    public static class SuspendRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class MandateValidationRequest {
        private String mandateId;
        private String action;
        private String resource;
        private Double amount;
        private String context;

        public String getMandateId() {
            return mandateId;
        }

        public void setMandateId(String mandateId) {
            this.mandateId = mandateId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }
    }
}

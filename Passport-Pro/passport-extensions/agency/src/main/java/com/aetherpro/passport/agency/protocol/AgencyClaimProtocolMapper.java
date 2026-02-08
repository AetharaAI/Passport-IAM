/*
 * Copyright 2024 AetherPro Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aetherpro.passport.agency.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.passport.models.ClientSessionContext;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.passport.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.passport.protocol.oidc.mappers.OIDCAccessTokenResponseMapper;
import org.passport.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.passport.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.passport.protocol.oidc.mappers.TokenIntrospectionTokenMapper;
import org.passport.protocol.oidc.mappers.UserInfoTokenMapper;
import org.passport.provider.ProviderConfigProperty;
import org.passport.representations.AccessTokenResponse;
import org.passport.representations.IDToken;

import com.aetherpro.passport.agency.AgencyProvider;
import com.aetherpro.passport.agency.AgencyRealmConfig;
import com.aetherpro.passport.agency.AgentPassport;
import com.aetherpro.passport.agency.DelegateModel;
import com.aetherpro.passport.agency.MandateModel;
import com.aetherpro.passport.agency.PrincipalModel;

/**
 * OIDC Protocol Mapper that adds Agency/LBAC context to access tokens.
 * 
 * This mapper adds claims containing:
 * - principals: List of principals the user is a delegate for
 * - mandates: List of active mandate scopes the user can act under
 * - passports: List of active agent passport IDs (for AI agents)
 * 
 * @author AetherPro Technologies
 */
public class AgencyClaimProtocolMapper extends AbstractOIDCProtocolMapper 
    implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper,
        OIDCAccessTokenResponseMapper, TokenIntrospectionTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    public static final String PROVIDER_ID = "passport-agency-claim-mapper";
    
    public static final String INCLUDE_PRINCIPALS = "include.principals";
    public static final String INCLUDE_MANDATES = "include.mandates";
    public static final String INCLUDE_PASSPORTS = "include.passports";
    public static final String CLAIM_PREFIX = "claim.prefix";

    static {
        ProviderConfigProperty prefixProperty = new ProviderConfigProperty();
        prefixProperty.setName(CLAIM_PREFIX);
        prefixProperty.setLabel("Claim Prefix");
        prefixProperty.setDefaultValue("agency");
        prefixProperty.setType(ProviderConfigProperty.STRING_TYPE);
        prefixProperty.setHelpText("Prefix for agency claims in the token. Default is 'agency'.");
        configProperties.add(prefixProperty);

        ProviderConfigProperty includePrincipals = new ProviderConfigProperty();
        includePrincipals.setName(INCLUDE_PRINCIPALS);
        includePrincipals.setLabel("Include Principals");
        includePrincipals.setDefaultValue("true");
        includePrincipals.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        includePrincipals.setHelpText("If true, include list of principals the user is a delegate for.");
        configProperties.add(includePrincipals);

        ProviderConfigProperty includeMandates = new ProviderConfigProperty();
        includeMandates.setName(INCLUDE_MANDATES);
        includeMandates.setLabel("Include Mandates");
        includeMandates.setDefaultValue("true");
        includeMandates.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        includeMandates.setHelpText("If true, include list of active mandate scopes.");
        configProperties.add(includeMandates);

        ProviderConfigProperty includePassports = new ProviderConfigProperty();
        includePassports.setName(INCLUDE_PASSPORTS);
        includePassports.setLabel("Include Agent Passports");
        includePassports.setDefaultValue("true");
        includePassports.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        includePassports.setHelpText("If true, include list of active agent passport IDs (for AI agents).");
        configProperties.add(includePassports);

        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AgencyClaimProtocolMapper.class);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Agency Claims";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Adds Agency/LBAC context claims to the token including delegate principals, mandates, and agent passports.";
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        // Agency claims are populated by the overload with PassportSession
    }

    @Override
    protected void setClaim(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel, 
                            UserSessionModel userSession, PassportSession passportSession, 
                            ClientSessionContext clientSessionCtx) {
        // Agency claims are added to the ID/access token, not the response wrapper
    }

    @Override
    public IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel, PassportSession session,
                                    UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        if (!OIDCAttributeMapperHelper.includeInIDToken(mappingModel)) {
            return token;
        }
        addAgencyClaims(token, mappingModel, userSession, session);
        return token;
    }

    @Override
    public org.passport.representations.AccessToken transformAccessToken(
            org.passport.representations.AccessToken token, ProtocolMapperModel mappingModel, 
            PassportSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        if (!OIDCAttributeMapperHelper.includeInAccessToken(mappingModel)) {
            return token;
        }
        addAgencyClaims(token, mappingModel, userSession, session);
        return token;
    }

    private void addAgencyClaims(IDToken token, ProtocolMapperModel mappingModel, 
                                 UserSessionModel userSession, PassportSession session) {
        if (userSession == null || session == null) return;
        
        RealmModel realm = userSession.getRealm();
        UserModel user = userSession.getUser();
        
        if (user == null || realm == null) return;
        
        AgencyProvider agencyProvider = session.getProvider(AgencyProvider.class);
        if (agencyProvider == null) return;
        
        // Check if agency is enabled for this realm
        AgencyRealmConfig config = agencyProvider.getRealmConfig(realm);
        if (config == null || !config.isEnabled()) return;
        
        Map<String, String> mapperConfig = mappingModel.getConfig();
        String prefix = mapperConfig.getOrDefault(CLAIM_PREFIX, "agency");
        boolean includePrincipals = Boolean.parseBoolean(mapperConfig.getOrDefault(INCLUDE_PRINCIPALS, "true"));
        boolean includeMandates = Boolean.parseBoolean(mapperConfig.getOrDefault(INCLUDE_MANDATES, "true"));
        boolean includePassports = Boolean.parseBoolean(mapperConfig.getOrDefault(INCLUDE_PASSPORTS, "true"));
        
        Map<String, Object> agencyClaim = new HashMap<>();
        
        // Get all delegates for this user
        List<DelegateModel> delegates = agencyProvider.getDelegatesForAgent(user);
        
        // Add delegate principals
        if (includePrincipals && delegates != null && !delegates.isEmpty()) {
            List<Map<String, Object>> principalsList = delegates.stream()
                .filter(DelegateModel::isActive)
                .map(delegate -> {
                    Optional<PrincipalModel> principalOpt = agencyProvider.getPrincipal(realm, delegate.getPrincipalId());
                    if (!principalOpt.isPresent()) return null;
                    PrincipalModel principal = principalOpt.get();
                    Map<String, Object> p = new HashMap<>();
                    p.put("id", principal.getId());
                    p.put("name", principal.getName());
                    p.put("type", principal.getType().name().toLowerCase());
                    p.put("delegate_id", delegate.getId());
                    p.put("delegate_type", delegate.getType().name().toLowerCase());
                    return p;
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
            
            if (!principalsList.isEmpty()) {
                agencyClaim.put("principals", principalsList);
            }
        }
        
        // Add active mandates
        if (includeMandates && delegates != null) {
            List<Map<String, Object>> mandatesList = new ArrayList<>();
            for (DelegateModel delegate : delegates) {
                if (!delegate.isActive()) continue;
                List<MandateModel> mandates = agencyProvider.getMandatesForDelegate(delegate);
                if (mandates != null) {
                    for (MandateModel mandate : mandates) {
                        if (!mandate.isActive()) continue;
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", mandate.getId());
                        m.put("scope", mandate.getScope());
                        m.put("principal_id", delegate.getPrincipalId());
                        m.put("valid_until", mandate.getValidUntil() != null ? 
                            mandate.getValidUntil().toEpochMilli() : null);
                        mandatesList.add(m);
                    }
                }
            }
            if (!mandatesList.isEmpty()) {
                agencyClaim.put("mandates", mandatesList);
            }
        }
        
        // Add agent passports (for AI agents) - check if user has associated passports via their delegates
        if (includePassports && delegates != null) {
            List<Map<String, Object>> passportsList = new ArrayList<>();
            for (DelegateModel delegate : delegates) {
                if (!delegate.isActive()) continue;
                Optional<PrincipalModel> principalOpt = agencyProvider.getPrincipal(realm, delegate.getPrincipalId());
                if (!principalOpt.isPresent()) continue;
                
                List<AgentPassport> passports = agencyProvider.getAgentPassportsForPrincipal(principalOpt.get());
                if (passports != null) {
                    for (AgentPassport passport : passports) {
                        if (!passport.isActive()) continue;
                        Map<String, Object> pp = new HashMap<>();
                        pp.put("id", passport.getId());
                        pp.put("principal_id", passport.getPrincipalId());
                        pp.put("capabilities", passport.getCapabilities());
                        pp.put("valid_until", passport.getExpiresAt() != null ? 
                            passport.getExpiresAt().toEpochMilli() : null);
                        passportsList.add(pp);
                    }
                }
            }
            if (!passportsList.isEmpty()) {
                agencyClaim.put("passports", passportsList);
            }
        }
        
        // Only add the claim if there's agency context
        if (!agencyClaim.isEmpty()) {
            token.getOtherClaims().put(prefix, agencyClaim);
        }
    }

    /**
     * Factory method to create a pre-configured Agency Claims mapper.
     */
    public static ProtocolMapperModel create(String name, String prefix,
                                             boolean includePrincipals, 
                                             boolean includeMandates,
                                             boolean includePassports,
                                             boolean accessToken, 
                                             boolean idToken, 
                                             boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(CLAIM_PREFIX, prefix);
        config.put(INCLUDE_PRINCIPALS, String.valueOf(includePrincipals));
        config.put(INCLUDE_MANDATES, String.valueOf(includeMandates));
        config.put(INCLUDE_PASSPORTS, String.valueOf(includePassports));
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);
        return mapper;
    }
}

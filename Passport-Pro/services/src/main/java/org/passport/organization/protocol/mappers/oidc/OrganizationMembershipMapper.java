/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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

package org.passport.organization.protocol.mappers.oidc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.passport.Config;
import org.passport.OAuth2Constants;
import org.passport.common.Profile;
import org.passport.models.ClientSessionContext;
import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.OrganizationModel;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.models.utils.ModelToRepresentation;
import org.passport.models.utils.RepresentationToModel;
import org.passport.organization.OrganizationProvider;
import org.passport.protocol.ProtocolMapperUtils;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.passport.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.passport.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.passport.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.passport.protocol.oidc.mappers.TokenIntrospectionTokenMapper;
import org.passport.protocol.oidc.mappers.UserInfoTokenMapper;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;
import org.passport.representations.IDToken;

import static org.passport.protocol.oidc.mappers.OIDCAttributeMapperHelper.JSON_TYPE;
import static org.passport.protocol.oidc.mappers.OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME;

public class OrganizationMembershipMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper, EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "oidc-organization-membership-mapper";
    public static final String ADD_ORGANIZATION_ATTRIBUTES = "addOrganizationAttributes";
    public static final String ADD_ORGANIZATION_ID = "addOrganizationId";

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> properties = new ArrayList<>();
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(properties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(properties, OrganizationMembershipMapper.class);
        OIDCAttributeMapperHelper.addJsonTypeConfig(properties, List.of("String", "JSON"), "String");
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.MULTIVALUED);
        property.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        property.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue(Boolean.TRUE.toString());
        properties.add(property);
        property = new ProviderConfigProperty();
        property.setName(ADD_ORGANIZATION_ATTRIBUTES);
        property.setLabel(ADD_ORGANIZATION_ATTRIBUTES + ".label");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue(Boolean.FALSE.toString());
        property.setHelpText(ADD_ORGANIZATION_ATTRIBUTES + ".help");
        properties.add(property);
        property = new ProviderConfigProperty();
        property.setName(ADD_ORGANIZATION_ID);
        property.setLabel(ADD_ORGANIZATION_ID + ".label");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue(Boolean.FALSE.toString());
        property.setHelpText(ADD_ORGANIZATION_ID + ".help");
        properties.add(property);
        return properties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Organization Membership";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Map user Organization membership";
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel model, UserSessionModel userSession, PassportSession session, ClientSessionContext clientSessionCtx) {
        String orgId = clientSessionCtx.getClientSession().getNote(OrganizationModel.ORGANIZATION_ATTRIBUTE);
        Stream<OrganizationModel> organizations;

        if (orgId == null) {
            organizations = resolveFromRequestedScopes(session, userSession, clientSessionCtx);
        } else {
            organizations = Stream.of(session.getProvider(OrganizationProvider.class).getById(orgId));
        }

        PassportContext context = session.getContext();
        RealmModel realm = context.getRealm();
        ProtocolMapperModel effectiveModel = getEffectiveModel(session, realm, model);
        UserModel user = userSession.getUser();
        Object claim = resolveValue(effectiveModel, user, organizations.toList());

        if (claim == null) {
            return;
        }

        OIDCAttributeMapperHelper.mapClaim(token, effectiveModel, claim);
    }

    private Stream<OrganizationModel> resolveFromRequestedScopes(PassportSession session, UserSessionModel userSession, ClientSessionContext context) {
        String rawScopes = context.getScopeString(true);
        OrganizationScope scope = OrganizationScope.valueOfScope(session, rawScopes);

        if (scope == null) {
            return Stream.empty();
        }

        return scope.resolveOrganizations(userSession.getUser(), rawScopes, session);
    }

    private Object resolveValue(ProtocolMapperModel model, UserModel user, List<OrganizationModel> organizations) {
        if (organizations.isEmpty()) {
            return null;
        }

        if (!OIDCAttributeMapperHelper.isMultivalued(model)) {
            return organizations.get(0).getAlias();
        }

        Map<String, Map<String, Object>> value = new HashMap<>();

        for (OrganizationModel o : organizations) {
            if (o == null || !o.isEnabled() || user == null || !o.isMember(user)) {
                continue;
            }

            Map<String, Object> claims = new HashMap<>();

            // Add organization attributes first
            if (isAddOrganizationAttributes(model)) {
                claims.putAll(o.getAttributes());
            }
            // Add organization ID last so it overrides any custom "id" attribute
            if (isAddOrganizationId(model)) {
                claims.put(OAuth2Constants.ORGANIZATION_ID, o.getId());
            }

            value.put(o.getAlias(), claims);
        }

        if (value.isEmpty()) {
            return null;
        }

        if (isJsonType(model)) {
            return value;
        }

        return value.keySet();
    }

    private static boolean isJsonType(ProtocolMapperModel model) {
        return "JSON".equals(model.getConfig().getOrDefault(JSON_TYPE, "JSON"));
    }

    @Override
    public ProtocolMapperModel getEffectiveModel(PassportSession session, RealmModel realm, ProtocolMapperModel model) {
        // Effectively clone
        ProtocolMapperModel copy = RepresentationToModel.toModel(ModelToRepresentation.toRepresentation(model));
        Map<String, String> config = Optional.ofNullable(copy.getConfig()).orElseGet(HashMap::new);

        config.putIfAbsent(JSON_TYPE, "String");

        if (!OIDCAttributeMapperHelper.isMultivalued(copy)) {
            config.put(ADD_ORGANIZATION_ATTRIBUTES, Boolean.FALSE.toString());
            config.put(ADD_ORGANIZATION_ID, Boolean.FALSE.toString());
        }

        if (isAddOrganizationAttributes(copy) || isAddOrganizationId(copy)) {
            config.put(JSON_TYPE, "JSON");
        }

        setDefaultValues(config);

        return copy;
    }

    private void setDefaultValues(Map<String, String> config) {
        config.putIfAbsent(TOKEN_CLAIM_NAME, OAuth2Constants.ORGANIZATION);

        for (ProviderConfigProperty property : getConfigProperties()) {
            Object defaultValue = property.getDefaultValue();

            if (defaultValue != null) {
                config.putIfAbsent(ProtocolMapperUtils.MULTIVALUED, defaultValue.toString());
            }
        }
    }

    private boolean isAddOrganizationAttributes(ProtocolMapperModel model) {
        return Boolean.parseBoolean(model.getConfig().getOrDefault(ADD_ORGANIZATION_ATTRIBUTES, Boolean.FALSE.toString()));
    }

    private boolean isAddOrganizationId(ProtocolMapperModel model) {
        return Boolean.parseBoolean(model.getConfig().getOrDefault(ADD_ORGANIZATION_ID, Boolean.FALSE.toString()));
    }

    public static ProtocolMapperModel create(String name, boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        config.put(TOKEN_CLAIM_NAME, OAuth2Constants.ORGANIZATION);
        config.put(JSON_TYPE, "String");
        config.put(ProtocolMapperUtils.MULTIVALUED, Boolean.TRUE.toString());
        mapper.setConfig(config);

        return mapper;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION);
    }

}

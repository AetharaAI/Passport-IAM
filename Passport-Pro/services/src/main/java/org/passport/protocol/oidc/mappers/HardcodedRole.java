/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.passport.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.passport.models.ClientSessionContext;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.protocol.ProtocolMapperUtils;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.provider.ProviderConfigProperty;
import org.passport.representations.AccessToken;
import org.passport.representations.IDToken;
import org.passport.utils.RoleResolveUtil;

/**
 * Add a role to a token
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HardcodedRole extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    public static final String ROLE_CONFIG = "role";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ROLE_CONFIG);
        property.setLabel("Role");
        property.setHelpText("Role you want added to the token.  Click 'Select Role' button to browse roles, or just type it in the textbox.  To reference a client role the syntax is clientname.clientrole, i.e. myclient.myrole");
        property.setType(ProviderConfigProperty.ROLE_TYPE);
        configProperties.add(property);
    }

    public static final String PROVIDER_ID = "oidc-hardcoded-role-mapper";


    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Hardcoded Role";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Hardcode a role into the access token.";
    }

    @Override
    public int getPriority() {
        return ProtocolMapperUtils.PRIORITY_HARDCODED_ROLE_MAPPER;
    }

    @Override
    public AccessToken transformUserInfoToken(AccessToken token, ProtocolMapperModel mappingModel, PassportSession session,
                                              UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // the mapper is always executed and then other role mappers decide if the claims are really set to the token
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, PassportSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // the mapper is always executed and then other role mappers decide if the claims are really set to the token
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    @Override
    public AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, PassportSession session,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        // the mapper is always executed and then other role mappers decide if the claims are really set to the token
        setClaim(token, mappingModel, userSession, session, clientSessionCtx);
        return token;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, PassportSession session,
                            ClientSessionContext clientSessionCtx) {

        String role = mappingModel.getConfig().get(ROLE_CONFIG);
        String[] scopedRole = PassportModelUtils.parseRole(role);
        String appName = scopedRole[0];
        String roleName = scopedRole[1];
        if (appName != null) {
            AccessToken.Access access = RoleResolveUtil.getResolvedClientRoles(session, clientSessionCtx, appName, true);
            access.addRole(roleName);
        } else {
            AccessToken.Access access = RoleResolveUtil.getResolvedRealmRoles(session, clientSessionCtx, true);
            access.addRole(role);
        }
    }

    public static ProtocolMapperModel create(String name,
                                             String role) {
        String mapperId = PROVIDER_ID;
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(ROLE_CONFIG, role);
        mapper.setConfig(config);
        return mapper;

    }

}

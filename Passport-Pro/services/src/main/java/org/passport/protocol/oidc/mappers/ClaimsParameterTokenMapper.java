/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.passport.models.ClientSessionContext;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.OIDCWellKnownProvider;
import org.passport.provider.ProviderConfigProperty;
import org.passport.representations.IDToken;
import org.passport.util.JsonSerialization;
import org.passport.util.TokenUtil;

import com.fasterxml.jackson.databind.JsonNode;

public class ClaimsParameterTokenMapper extends AbstractOIDCProtocolMapper implements OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "oidc-claims-param-token-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, ClaimsParameterTokenMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Claims parameter Token";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Claims specified by Claims parameter are put into tokens.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, PassportSession passportSession, ClientSessionContext clientSessionCtx) {
        String claims = clientSessionCtx.getClientSession().getNote(OIDCLoginProtocol.CLAIMS_PARAM);
        if (claims == null) return;

        if (TokenUtil.TOKEN_TYPE_ID.equals(token.getType())) {
            // ID Token
            putClaims("id_token", claims, token, mappingModel, userSession);
        } else {
            // UserInfo
            putClaims("userinfo", claims, token, mappingModel, userSession);
        }
    }

    private void putClaims(String tokenType, String claims, IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        JsonNode requestParams = null;

        try {
            requestParams = JsonSerialization.readValue(claims, JsonNode.class);
        } catch (IOException e) {
            return;
        }
        if (!requestParams.has(tokenType)) return;

        JsonNode tokenNode = requestParams.findValue(tokenType);

        OIDCWellKnownProvider.DEFAULT_CLAIMS_SUPPORTED.stream()
            .filter(i->tokenNode.has(i))
            .filter(i->tokenNode.findValue(i).has("essential"))
            .filter(i->tokenNode.findValue(i).findValue("essential").isBoolean())
            .filter(i->tokenNode.findValue(i).findValue("essential").asBoolean())
            .forEach(i -> {
                    // insert claim to Token
                    // "aud", "sub", "iss", "auth_time", "acr" are set as default.
                    // "name", "given_name", "family_name", "preferred_username", "email" need to be set explicitly using existing mapper.
                    if (i.equals(IDToken.NAME)) {
                        FullNameMapper fullNameMapper = new FullNameMapper();
                        fullNameMapper.setClaim(token, mappingModel, userSession);
                    } else if (i.equals(IDToken.GIVEN_NAME)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested firstName", "firstName", IDToken.GIVEN_NAME, "String", false, true, false), userSession);
                    } else if (i.equals(IDToken.FAMILY_NAME)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested lastName", "lastName", IDToken.FAMILY_NAME, "String", false, true, false), userSession);
                    } else if (i.equals(IDToken.PREFERRED_USERNAME)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested username", "username", IDToken.PREFERRED_USERNAME, "String", false, true, false), userSession);
                    } else if (i.equals(IDToken.EMAIL)) {
                        UserAttributeMapper userPropertyMapper = new UserAttributeMapper();
                        userPropertyMapper.setClaim(token, UserAttributeMapper.createClaimMapper("requested email", "email", IDToken.EMAIL, "String", false, true, false), userSession);
                    }
            });
    }

    public static ProtocolMapperModel createMapper(String name, boolean idToken, boolean userInfo) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<String, String>();
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        if (userInfo) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, "true");
        mapper.setConfig(config);
        return mapper;
    }

}

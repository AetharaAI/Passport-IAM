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
package org.passport.broker.oidc;

import java.io.IOException;
import java.util.Map;

import org.passport.broker.provider.AbstractIdentityProviderFactory;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.passport.util.JsonSerialization;

/**
 * @author Pedro Igor
 */
public class OIDCIdentityProviderFactory extends AbstractIdentityProviderFactory<OIDCIdentityProvider> {

    public static final String PROVIDER_ID = "oidc";

    @Override
    public String getName() {
        return "OpenID Connect v1.0";
    }

    @Override
    public OIDCIdentityProvider create(PassportSession session, IdentityProviderModel model) {
        return new OIDCIdentityProvider(session, new OIDCIdentityProviderConfig(model));
    }

    @Override
    public OIDCIdentityProviderConfig createConfig() {
        return new OIDCIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Map<String, String> parseConfig(PassportSession session, String config) {
        return parseOIDCConfig(session, config);
    }

    protected static Map<String, String> parseOIDCConfig(PassportSession session, String configString) {
        OIDCConfigurationRepresentation rep;
        try {
            rep = JsonSerialization.readValue(configString, OIDCConfigurationRepresentation.class);
        } catch (IOException e) {
            throw new RuntimeException("failed to load openid connect metadata", e);
        }
        OIDCIdentityProviderConfig config = new OIDCIdentityProviderConfig();
        config.setIssuer(rep.getIssuer());
        config.setLogoutUrl(rep.getLogoutEndpoint());
        config.setAuthorizationUrl(rep.getAuthorizationEndpoint());
        config.setTokenUrl(rep.getTokenEndpoint());
        config.setUserInfoUrl(rep.getUserinfoEndpoint());
        if (rep.getJwksUri() != null) {
            config.setValidateSignature(true);
            config.setUseJwksUrl(true);
            config.setJwksUrl(rep.getJwksUri());
        }

        // Introspection URL may or may not be available in the configuration. It is available in RFC8414 , but not in the OIDC discovery specification.
        // Hence some servers may not add it to their well-known responses
        if (rep.getIntrospectionEndpoint() != null) {
            config.setTokenIntrospectionUrl(rep.getIntrospectionEndpoint());
        }
        return config.getConfig();
    }

}

/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.passport.protocol.oidc.grants;


import org.passport.Config;
import org.passport.OAuth2Constants;
import org.passport.common.Profile;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.EnvironmentDependentProviderFactory;

public class JWTAuthorizationGrantTypeFactory implements OAuth2GrantTypeFactory, EnvironmentDependentProviderFactory {

    @Override
    public String getId() {
        return OAuth2Constants.JWT_AUTHORIZATION_GRANT;
    }

    @Override
    public String getShortcut() {
        return "ag";
    }

    @Override
    public OAuth2GrantType create(PassportSession session) {
        return new JWTAuthorizationGrantType();
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.JWT_AUTHORIZATION_GRANT);
    }

}

/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.passport.protocol.oidc.tokenexchange;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.protocol.oidc.TokenExchangeProvider;
import org.passport.protocol.oidc.TokenExchangeProviderFactory;
import org.passport.provider.EnvironmentDependentProviderFactory;

/**
 * Provider factory for internal-internal token exchange, which is compliant with the token exchange specification https://datatracker.ietf.org/doc/html/rfc8693
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class StandardTokenExchangeProviderFactory implements TokenExchangeProviderFactory, EnvironmentDependentProviderFactory {

    @Override
    public TokenExchangeProvider create(PassportSession session) {
        return new StandardTokenExchangeProvider();
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
    public String getId() {
        return "standard";
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.TOKEN_EXCHANGE_STANDARD_V2);
    }

    @Override
    public int order() {
        // Bigger priority than V1, so it has preference if both V1 and V2 enabled
        return 10;
    }
}

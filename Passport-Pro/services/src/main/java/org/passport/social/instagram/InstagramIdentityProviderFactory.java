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
package org.passport.social.instagram;

import org.passport.Config.Scope;
import org.passport.broker.oidc.OAuth2IdentityProviderConfig;
import org.passport.broker.provider.AbstractIdentityProviderFactory;
import org.passport.broker.social.SocialIdentityProviderFactory;
import org.passport.common.Profile;
import org.passport.common.Profile.Feature;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.provider.EnvironmentDependentProviderFactory;

/**
 * @author Pedro Igor
 */
public class InstagramIdentityProviderFactory extends AbstractIdentityProviderFactory<InstagramIdentityProvider> implements SocialIdentityProviderFactory<InstagramIdentityProvider>, EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "instagram";

    @Override
    public String getName() {
        return "Instagram";
    }

    @Override
    public InstagramIdentityProvider create(PassportSession session, IdentityProviderModel model) {
        return new InstagramIdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    @Override
    public OAuth2IdentityProviderConfig createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.INSTAGRAM_BROKER);
    }
}

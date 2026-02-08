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
package org.passport.social.facebook;

import java.util.List;

import org.passport.broker.oidc.OAuth2IdentityProviderConfig;
import org.passport.broker.provider.AbstractIdentityProviderFactory;
import org.passport.broker.social.SocialIdentityProviderFactory;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;

/**
 * @author Pedro Igor
 */
public class FacebookIdentityProviderFactory extends AbstractIdentityProviderFactory<FacebookIdentityProvider> implements SocialIdentityProviderFactory<FacebookIdentityProvider> {

    public static final String PROVIDER_ID = "facebook";

    @Override
    public String getName() {
        return "Facebook";
    }

    @Override
    public FacebookIdentityProvider create(PassportSession session, IdentityProviderModel model) {
        return new FacebookIdentityProvider(session, new FacebookIdentityProviderConfig(model));
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
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name("fetchedFields")
                .label("Additional user's profile fields")
                .helpText("Provide additional fields which would be fetched using the profile request. This will be appended to the default set of 'id,name,email,first_name,last_name'.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add().build();
    }
}

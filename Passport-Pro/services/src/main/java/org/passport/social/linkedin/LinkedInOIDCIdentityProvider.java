/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.passport.social.linkedin;

import org.passport.broker.oidc.OIDCIdentityProvider;
import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.broker.social.SocialIdentityProvider;
import org.passport.crypto.KeyWrapper;
import org.passport.jose.jws.JWSInput;
import org.passport.keys.PublicKeyLoader;
import org.passport.keys.PublicKeyStorageProvider;
import org.passport.keys.PublicKeyStorageUtils;
import org.passport.models.PassportSession;

/**
 * <p>Specific OIDC LinkedIn provider for <b>Sign In with LinkedIn using OpenID Connect</b>
 * product app.</p>
 *
 * @author rmartinc
 */
public class LinkedInOIDCIdentityProvider extends OIDCIdentityProvider implements SocialIdentityProvider<OIDCIdentityProviderConfig> {

    public static final String DEFAULT_SCOPE = "openid profile email";

    public LinkedInOIDCIdentityProvider(PassportSession session, OIDCIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    @Override
    protected KeyWrapper getIdentityProviderKeyWrapper(JWSInput jws) {
        // workaround to load keys published with no "use" as signature
        PublicKeyLoader loader = new LinkedInPublicKeyLoader(session, getConfig());
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(session.getContext().getRealm().getId(), getConfig().getInternalId());
        return keyStorage.getPublicKey(modelKey, jws.getHeader().getKeyId(), jws.getHeader().getRawAlgorithm(), loader);
    }
}

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

import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.crypto.PublicKeysWrapper;
import org.passport.jose.jwk.JSONWebKeySet;
import org.passport.jose.jwk.JWK;
import org.passport.keys.PublicKeyLoader;
import org.passport.models.PassportSession;
import org.passport.protocol.oidc.utils.JWKSHttpUtils;
import org.passport.util.JWKSUtils;

/**
 * <p>Specific public key loader that assumes that use for the keys is the requested one.
 * The LinkedIn OpenID Connect implementation does not add the compulsory
 * <em>use</em> claim in the <a href="https://www.linkedin.com/oauth/openid/jwks">jwks endpoint</a>.</p>
 *
 * @author rmartinc
 */
public class LinkedInPublicKeyLoader implements PublicKeyLoader {

    private final PassportSession session;
    private final OIDCIdentityProviderConfig config;

    public LinkedInPublicKeyLoader(PassportSession session, OIDCIdentityProviderConfig config) {
        this.session = session;
        this.config = config;
    }

    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        String jwksUrl = config.getJwksUrl();
        JSONWebKeySet jwks = JWKSHttpUtils.sendJwksRequest(session, jwksUrl);
        return JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG, true);
    }
}

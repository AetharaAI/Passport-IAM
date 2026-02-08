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

package org.passport.keys.loader;

import java.security.PublicKey;

import org.passport.broker.jwtauthorizationgrant.JWTAuthorizationGrantConfig;
import org.passport.crypto.KeyWrapper;
import org.passport.jose.jwk.JWK;
import org.passport.jose.jws.JWSInput;
import org.passport.keys.PublicKeyLoader;
import org.passport.keys.PublicKeyStorageProvider;
import org.passport.keys.PublicKeyStorageUtils;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PublicKeyStorageManager {

    private static final Logger logger = Logger.getLogger(PublicKeyStorageManager.class);

    public static PublicKey getClientPublicKey(PassportSession session, ClientModel client, JWSInput input) {
        KeyWrapper keyWrapper = getClientPublicKeyWrapper(session, client, input);
        PublicKey publicKey = null;
        if (keyWrapper != null) {
            publicKey = (PublicKey)keyWrapper.getPublicKey();
        }
        return publicKey;
    }

    public static KeyWrapper getClientPublicKeyWrapper(PassportSession session, ClientModel client, JWSInput input) {
        String kid = input.getHeader().getKeyId();
        String alg = input.getHeader().getRawAlgorithm();
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getClientModelCacheKey(client.getRealm().getId(), client.getId());
        ClientPublicKeyLoader loader = new ClientPublicKeyLoader(session, client);
        return keyStorage.getPublicKey(modelKey, kid, alg, loader);
    }

    public static KeyWrapper getClientPublicKeyWrapper(PassportSession session, ClientModel client, JWK.Use keyUse, String algAlgorithm) {
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getClientModelCacheKey(client.getRealm().getId(), client.getId(), keyUse);
        ClientPublicKeyLoader loader = new ClientPublicKeyLoader(session, client, keyUse);
        return keyStorage.getFirstPublicKey(modelKey, algAlgorithm, loader);
    }

    public static KeyWrapper getIdentityProviderKeyWrapper(PassportSession session, RealmModel realm, JWTAuthorizationGrantConfig idpConfig, JWSInput input) {
        String kid = input.getHeader().getKeyId();
        String alg = input.getHeader().getRawAlgorithm();

        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);

        String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(realm.getId(), idpConfig.getInternalId());
        PublicKeyLoader loader;
        if (idpConfig.isUseJwksUrl()) {
            loader = new OIDCIdentityProviderPublicKeyLoader(session, idpConfig);
        } else {
            String pem = idpConfig.getPublicKeySignatureVerifier();
            if (StringUtil.isNotBlank(pem) && pem.trim().startsWith("{")) {
                loader = new OIDCIdentityProviderPublicKeyLoader(session, idpConfig);
            } else if (StringUtil.isNotBlank(pem)) {
                loader = new HardcodedPublicKeyLoader(
                        StringUtil.isNotBlank(idpConfig.getPublicKeySignatureVerifierKeyId())
                                ? idpConfig.getPublicKeySignatureVerifierKeyId().trim()
                                : kid, pem, alg);
            } else {
                logger.warnf("No public key saved on identityProvider %s", idpConfig.getAlias());
                return null;
            }
        }

        return keyStorage.getPublicKey(modelKey, kid, alg, loader);
    }
}

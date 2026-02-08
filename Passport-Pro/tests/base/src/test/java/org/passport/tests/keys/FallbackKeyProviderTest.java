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

package org.passport.tests.keys;

import java.util.LinkedList;
import java.util.List;

import org.passport.crypto.Algorithm;
import org.passport.models.Constants;
import org.passport.representations.idm.ComponentRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.tests.common.BasicRealmWithUserConfig;
import org.passport.tests.utils.Assert;
import org.passport.testsuite.util.oauth.AccessTokenResponse;
import org.passport.testsuite.util.oauth.AuthorizationEndpointResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PassportIntegrationTest
public class FallbackKeyProviderTest {

    @InjectRealm(lifecycle = LifeCycle.METHOD, config = BasicRealmWithUserConfig.class)
    ManagedRealm realm;

    @InjectOAuthClient
    OAuthClient oauth;

    @Test
    public void fallbackAfterDeletingAllKeysInRealm() {
        String realmId = realm.getId();

        List<ComponentRepresentation> providers = realm.admin().components().query(realmId, "org.passport.keys.KeyProvider");
        assertEquals(4, providers.size());

        for (ComponentRepresentation p : providers) {
            realm.admin().components().component(p.getId()).remove();
        }

        providers = realm.admin().components().query(realmId, "org.passport.keys.KeyProvider");
        assertEquals(0, providers.size());

        AuthorizationEndpointResponse authorizationEndpointResponse = oauth.doLogin(BasicRealmWithUserConfig.USERNAME, BasicRealmWithUserConfig.PASSWORD);

        AccessTokenResponse response = oauth.doAccessTokenRequest(authorizationEndpointResponse.getCode());
        Assertions.assertTrue(response.isSuccess());

        providers = realm.admin().components().query(realmId, "org.passport.keys.KeyProvider");
        Assert.assertNames(providers, "fallback-RS256", "fallback-AES", "fallback-" + Constants.INTERNAL_SIGNATURE_ALGORITHM);
    }

    @Test
    public void differentAlgorithms() {
        String realmId = realm.admin().toRepresentation().getId();

        String[] algorithmsToTest = new String[] {
                Algorithm.RS384,
                Algorithm.RS512,
                Algorithm.PS256,
                Algorithm.PS384,
                Algorithm.PS512,
                Algorithm.ES256,
                Algorithm.ES384,
                Algorithm.ES512
        };

        oauth.doLogin(BasicRealmWithUserConfig.USERNAME, BasicRealmWithUserConfig.PASSWORD);

        for (String algorithm : algorithmsToTest) {
            RealmRepresentation rep = realm.admin().toRepresentation();
            rep.setDefaultSignatureAlgorithm(algorithm);
            realm.admin().update(rep);

            AccessTokenResponse response = oauth.doClientCredentialsGrantAccessTokenRequest();
            Assertions.assertTrue(response.isSuccess());
        }

        List<ComponentRepresentation> providers = realm.admin().components().query(realmId, "org.passport.keys.KeyProvider");

        List<String> expected = new LinkedList<>();
        expected.add("rsa-generated");
        expected.add("rsa-enc-generated");
        expected.add("hmac-generated-hs512");
        expected.add("aes-generated");

        for (String a : algorithmsToTest) {
            expected.add("fallback-" + a);
        }

        Assert.assertNames(providers, expected.toArray(new String[providers.size()]));
    }

}

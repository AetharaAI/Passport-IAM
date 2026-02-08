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

package org.passport.tests.admin;

import java.util.Map;

import org.passport.admin.client.Passport;
import org.passport.common.Version;
import org.passport.crypto.Algorithm;
import org.passport.keys.Attributes;
import org.passport.keys.GeneratedRsaKeyProviderFactory;
import org.passport.keys.KeyProvider;
import org.passport.representations.idm.ComponentTypeRepresentation;
import org.passport.representations.idm.ConfigPropertyRepresentation;
import org.passport.representations.info.ProviderRepresentation;
import org.passport.representations.info.ServerInfoRepresentation;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectCryptoHelper;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.crypto.CryptoHelper;
import org.passport.tests.utils.Assert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@PassportIntegrationTest
public class ServerInfoTest {

    @InjectAdminClient
    Passport adminClient;

    @InjectCryptoHelper
    CryptoHelper cryptoHelper;

    @Test
    public void testServerInfo() {
        ServerInfoRepresentation info = adminClient.serverInfo().getInfo();
        assertNotNull(info);

        assertNotNull(info.getProviders());
        assertNotNull(info.getProviders().get("realm"));
        assertNotNull(info.getProviders().get("user"));
        assertNotNull(info.getProviders().get("authenticator"));

        assertNotNull(info.getThemes());
        assertNotNull(info.getThemes().get("account"));
        Assert.assertNames(info.getThemes().get("account"), "passport.v3");
        Assert.assertNames(info.getThemes().get("admin"), "passport.v2");
        Assert.assertNames(info.getThemes().get("email"), "passport");
        Assert.assertNames(info.getThemes().get("login"), "passport", "passport.v2");
        Assert.assertNames(info.getThemes().get("welcome"), "passport");

        assertNotNull(info.getEnums());

        assertNotNull(info.getMemoryInfo());
        assertNotNull(info.getSystemInfo());

        assertNotNull(info.getCryptoInfo());
        Assert.assertNames(info.getCryptoInfo().getSupportedKeystoreTypes(), cryptoHelper.getExpectedSupportedKeyStoreTypes());
        Assert.assertNames(info.getCryptoInfo().getClientSignatureSymmetricAlgorithms(), Algorithm.HS256, Algorithm.HS384, Algorithm.HS512);
        Assert.assertNames(info.getCryptoInfo().getClientSignatureAsymmetricAlgorithms(),
                Algorithm.ES256, Algorithm.ES384, Algorithm.ES512,
                Algorithm.EdDSA, Algorithm.PS256, Algorithm.PS384,
                Algorithm.PS512, Algorithm.RS256, Algorithm.RS384,
                Algorithm.RS512);

        ComponentTypeRepresentation rsaGeneratedProviderInfo = info.getComponentTypes().get(KeyProvider.class.getName())
                .stream()
                .filter(componentType -> GeneratedRsaKeyProviderFactory.ID.equals(componentType.getId()))
                .findFirst().orElseThrow(() -> new RuntimeException("Not found provider with ID 'rsa-generated'"));
        ConfigPropertyRepresentation keySizeRep = rsaGeneratedProviderInfo.getProperties()
                .stream()
                .filter(configProp -> Attributes.KEY_SIZE_KEY.equals(configProp.getName()))
                .findFirst().orElseThrow(() -> new RuntimeException("Not found provider with ID 'rsa-generated'"));
        Assert.assertNames(keySizeRep.getOptions(), cryptoHelper.getExpectedSupportedRsaKeySizes());

        assertEquals(Version.VERSION, info.getSystemInfo().getVersion());
        assertNotNull(info.getSystemInfo().getServerTime());
        assertNotNull(info.getSystemInfo().getUptime());

        Map<String, ProviderRepresentation> jpaProviders = info.getProviders().get("connectionsJpa").getProviders();
        ProviderRepresentation jpaProvider = jpaProviders.values().iterator().next();
        Assertions.assertNotNull(jpaProvider.getOperationalInfo());
    }
}

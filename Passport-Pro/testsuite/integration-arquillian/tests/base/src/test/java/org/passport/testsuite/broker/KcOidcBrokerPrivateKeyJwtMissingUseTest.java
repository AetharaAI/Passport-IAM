/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.passport.testsuite.broker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.passport.authentication.authenticators.client.JWTClientAuthenticator;
import org.passport.common.util.MultivaluedHashMap;
import org.passport.keys.KeyProvider;
import org.passport.models.IdentityProviderSyncMode;
import org.passport.models.utils.DefaultKeyProviders;
import org.passport.protocol.oidc.OIDCConfigAttributes;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.ComponentExportRepresentation;
import org.passport.representations.idm.IdentityProviderRepresentation;
import org.passport.representations.idm.RealmRepresentation;

import static org.passport.testsuite.broker.BrokerTestConstants.IDP_OIDC_ALIAS;
import static org.passport.testsuite.broker.BrokerTestConstants.IDP_OIDC_PROVIDER_ID;
import static org.passport.testsuite.broker.BrokerTestTools.createIdentityProvider;

public class KcOidcBrokerPrivateKeyJwtMissingUseTest extends AbstractBrokerTest {

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return new KcOidcBrokerConfigurationWithJWTAuthentication();
    }

    private class KcOidcBrokerConfigurationWithJWTAuthentication extends KcOidcBrokerConfiguration {

        @Override
        public List<ClientRepresentation> createProviderClients() {
            List<ClientRepresentation> clientsRepList = super.createProviderClients();
            log.info("Update provider clients to accept JWT authentication");
            for (ClientRepresentation client: clientsRepList) {
                client.setClientAuthenticatorType(JWTClientAuthenticator.PROVIDER_ID);
                // use the JWKS from the consumer realm to perform the signing
                if (client.getAttributes() == null) {
                    client.setAttributes(new HashMap<String, String>());
                }
                client.getAttributes().put(OIDCConfigAttributes.USE_JWKS_URL, "true");

                // use a custom realm resource provider to expose a jwks with an empty use
                // a custom key provider returning a null use wouldn't work due to the standard
                // jwks defaulting the use and other portions expecting the use to be set
                // see org.passport.testsuite.broker.oidc.MissingUseJwksRestResource
                client.getAttributes().put(OIDCConfigAttributes.JWKS_URL, BrokerTestTools.getConsumerRoot() +
                        "/auth/realms/" + BrokerTestConstants.REALM_CONS_NAME + "/missing-use-jwks/jwks");

            }
            return clientsRepList;
        }

        @Override
        public IdentityProviderRepresentation setUpIdentityProvider(IdentityProviderSyncMode syncMode) {
            IdentityProviderRepresentation idp = createIdentityProvider(IDP_OIDC_ALIAS, IDP_OIDC_PROVIDER_ID);
            Map<String, String> config = idp.getConfig();
            applyDefaultConfiguration(config, syncMode);
            config.put("clientSecret", null);
            config.put("clientAuthMethod", OIDCLoginProtocol.PRIVATE_KEY_JWT);
            config.put("clientAssertionSigningAlg", "ES384");
            return idp;
        }

        @Override
        public RealmRepresentation createConsumerRealm() {
            RealmRepresentation realm = super.createConsumerRealm();

            // create the ECDSA key
            ComponentExportRepresentation component = new ComponentExportRepresentation();
            component.setName("ecdsa-generated");
            component.setProviderId("ecdsa-generated");

            MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
            config.putSingle("priority", DefaultKeyProviders.DEFAULT_PRIORITY);
            config.putSingle("ecdsaEllipticCurveKey", "P-384");
            component.setConfig(config);

            MultivaluedHashMap<String, ComponentExportRepresentation> components = realm.getComponents();
            if (components == null) {
                components = new MultivaluedHashMap<>();
                realm.setComponents(components);
            }
            components.add(KeyProvider.class.getName(), component);

            return realm;
        }

    }

}
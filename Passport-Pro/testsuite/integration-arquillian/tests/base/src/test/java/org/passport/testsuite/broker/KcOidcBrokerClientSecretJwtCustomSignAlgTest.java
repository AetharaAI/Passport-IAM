package org.passport.testsuite.broker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.passport.authentication.authenticators.client.JWTClientSecretAuthenticator;
import org.passport.crypto.Algorithm;
import org.passport.models.IdentityProviderSyncMode;
import org.passport.protocol.oidc.OIDCConfigAttributes;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.IdentityProviderRepresentation;

import static org.passport.testsuite.broker.BrokerTestConstants.IDP_OIDC_ALIAS;
import static org.passport.testsuite.broker.BrokerTestConstants.IDP_OIDC_PROVIDER_ID;
import static org.passport.testsuite.broker.BrokerTestTools.createIdentityProvider;

public class KcOidcBrokerClientSecretJwtCustomSignAlgTest extends AbstractBrokerTest {

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return new KcOidcBrokerConfigurationWithJWTAuthentication();
    }

    private class KcOidcBrokerConfigurationWithJWTAuthentication extends KcOidcBrokerConfiguration {

        String clientSecret = UUID.randomUUID().toString();
        String signAlg = Algorithm.HS384;
        
        @Override
        public List<ClientRepresentation> createProviderClients() {
            List<ClientRepresentation> clientsRepList = super.createProviderClients();
            log.info("Update provider clients to accept JWT authentication");
            for (ClientRepresentation client : clientsRepList) {
                if (client.getAttributes() == null) {
                    client.setAttributes(new HashMap<String, String>());
                }
                client.setClientAuthenticatorType(JWTClientSecretAuthenticator.PROVIDER_ID);
                client.setSecret(clientSecret);
                client.getAttributes().put(OIDCConfigAttributes.TOKEN_ENDPOINT_AUTH_SIGNING_ALG, signAlg);
            }
            return clientsRepList;
        }

        @Override
        public IdentityProviderRepresentation setUpIdentityProvider(IdentityProviderSyncMode syncMode) {
            IdentityProviderRepresentation idp = createIdentityProvider(IDP_OIDC_ALIAS, IDP_OIDC_PROVIDER_ID);
            Map<String, String> config = idp.getConfig();
            applyDefaultConfiguration(config, syncMode);
            config.put("clientAuthMethod", OIDCLoginProtocol.CLIENT_SECRET_JWT);
            config.put("clientSecret", clientSecret);
            config.put("clientAssertionSigningAlg", signAlg);
            return idp;
        }
    }
}

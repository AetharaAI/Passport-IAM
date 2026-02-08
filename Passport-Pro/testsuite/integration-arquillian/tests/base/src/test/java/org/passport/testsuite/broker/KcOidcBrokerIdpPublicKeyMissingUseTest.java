package org.passport.testsuite.broker;

import java.util.Map;

import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.models.IdentityProviderSyncMode;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.representations.idm.IdentityProviderRepresentation;

import static org.passport.testsuite.broker.BrokerTestConstants.IDP_OIDC_ALIAS;
import static org.passport.testsuite.broker.BrokerTestConstants.IDP_OIDC_PROVIDER_ID;
import static org.passport.testsuite.broker.BrokerTestConstants.REALM_PROV_NAME;
import static org.passport.testsuite.broker.BrokerTestTools.createIdentityProvider;
import static org.passport.testsuite.broker.BrokerTestTools.getProviderRoot;

public class KcOidcBrokerIdpPublicKeyMissingUseTest extends AbstractBrokerTest {

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return new KcOidcBrokerConfigurationWithIdpPublicKeyMissingUse();
    }

    private class KcOidcBrokerConfigurationWithIdpPublicKeyMissingUse extends KcOidcBrokerConfiguration {

        @Override
        public IdentityProviderRepresentation setUpIdentityProvider(IdentityProviderSyncMode syncMode) {
            IdentityProviderRepresentation idp = createIdentityProvider(IDP_OIDC_ALIAS, IDP_OIDC_PROVIDER_ID);
            Map<String, String> config = idp.getConfig();
            applyDefaultConfiguration(config, syncMode);
            config.put("clientAuthMethod", OIDCLoginProtocol.CLIENT_SECRET_BASIC);
            config.put(OIDCIdentityProviderConfig.JWKS_URL,
                    getProviderRoot() + "/auth/realms/" + REALM_PROV_NAME + "/missing-use-jwks/jwks");
            return idp;
        }

    }
}

package org.passport.testframework.oauth;

import org.passport.protocol.oidc.OIDCConfigAttributes;
import org.passport.testframework.realm.ClientConfig;
import org.passport.testframework.realm.ClientConfigBuilder;

public class DefaultOAuthClientConfiguration implements ClientConfig {

    @Override
    public ClientConfigBuilder configure(ClientConfigBuilder client) {
        return client.clientId("test-app")
                .serviceAccountsEnabled(true)
                .directAccessGrantsEnabled(true)
                .attribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_ENABLED, "true")
                .attribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_IDP, "authorization-grant-idp-alias")
                .secret("test-secret");
    }

}

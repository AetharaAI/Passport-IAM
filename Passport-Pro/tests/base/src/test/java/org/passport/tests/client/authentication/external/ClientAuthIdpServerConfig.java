package org.passport.tests.client.authentication.external;

import org.passport.common.Profile;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

public class ClientAuthIdpServerConfig implements PassportServerConfig {

    @Override
    public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
        return config.features(Profile.Feature.CLIENT_AUTH_FEDERATED);
    }

}

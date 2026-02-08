package org.passport.tests.model;

import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

public class CustomProvidersServerConfig implements PassportServerConfig {

    @Override
    public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
        return config.dependency("org.passport.tests", "passport-tests-custom-providers");
    }
}

package org.passport.tests.workflow.config;

import org.passport.common.Profile.Feature;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

public class WorkflowsServerConfig implements PassportServerConfig {

    @Override
    public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
        return config.features(Feature.WORKFLOWS);
    }
}

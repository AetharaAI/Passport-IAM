package org.passport.testframework.database;

import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.config.Config;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.server.PassportServerConfigBuilder;

public class RemoteDatabaseSupplier extends AbstractDatabaseSupplier {

    public static final String NAME = "remote";

    @Override
    public String getAlias() {
        return NAME;
    }

    @Override
    TestDatabase getTestDatabase() {
        return new RemoteTestDatabase();
    }

    private String getDriverDependencyArtifact() {
        return Config.getValueTypeConfig(TestDatabase.class, "driver.artifact", null, String.class);
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        serverConfig = super.intercept(serverConfig, instanceContext);

        String dependencyArtifact = getDriverDependencyArtifact();
        if (dependencyArtifact != null) {
            String[] artifact = dependencyArtifact.split(":");
            if (artifact.length != 2) {
                throw new IllegalArgumentException("Invalid dependency artifact " + dependencyArtifact);
            }
            serverConfig.dependency(artifact[0], artifact[1]);
        }
        return serverConfig;
    }

}

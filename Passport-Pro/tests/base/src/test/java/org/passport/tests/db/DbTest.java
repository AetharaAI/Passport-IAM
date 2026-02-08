package org.passport.tests.db;

import org.passport.config.DatabaseOptions;
import org.passport.quarkus.runtime.configuration.Configuration;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class DbTest {

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    @Test
    public void ensurePostgreSQLSettingsAreApplied() {
        runOnServer.run(session -> {
            if (Configuration.getConfigValue(DatabaseOptions.DB).getValue().equals("postgres") &&
                Configuration.getConfigValue(DatabaseOptions.DB_DRIVER).getValue().equals("org.postgresql.Driver")) {
                Assertions.assertEquals("primary", Configuration.getConfigValue(DatabaseOptions.DB_POSTGRESQL_TARGET_SERVER_TYPE).getValue());
            } else {
                Assertions.assertNull(Configuration.getConfigValue(DatabaseOptions.DB_POSTGRESQL_TARGET_SERVER_TYPE).getValue());
            }
        });
    }

}
